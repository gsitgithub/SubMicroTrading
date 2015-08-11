/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.tasks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.ThreadPriority;

/**
 * all registered callbacks must finish in good time so as not to affect other timer events
 */
public class Scheduler {

    private static final Logger  _log = LoggerFactory.create( Scheduler.class );
    
    public static interface Callback {
        public ZString getName();

        /**
         * @param event the registered event
         */
        public void event( ScheduledEvent event );
    }

    private static final class GroupListenerTask extends ZTimerTask {

        private static final Logger  _glog = LoggerFactory.create( GroupListenerTask.class );

        private static final ErrorCode TASK_FAILED = new ErrorCode( "GLT100", "Failed to run task, name=" );
        
        private final Set<Callback> _callbackSet = new LinkedHashSet<Callback>();
        private final ScheduledEvent _event;

        private boolean _scheduled;

        public GroupListenerTask( Callback listener, ScheduledEvent event ) {
            super( event.name() );
            
            if ( listener != null ) {
                _callbackSet.add( listener );
            }
            _event    = event;
        }
        
        public void addListener( Callback listener ) {
            synchronized( _callbackSet ) {
                _callbackSet.add( listener );
            }
        }
        
        @Override
        public void fire() {
            synchronized( _callbackSet ) {
                for( Callback listener : _callbackSet ) {
                    try {
                        listener.event( _event );
                    } catch( Exception e ) {
                        _glog.error( TASK_FAILED, listener.getName(), e );
                    }
                }
            }
        }

        public void setScheduled( boolean scheduled ) {
            _scheduled = scheduled;
        }

        public boolean isScheduled() {
            return _scheduled;
        }
    }
        
    private static final class SingleCallbackTask extends ZTimerTask {

        private static final Logger  _sclog = LoggerFactory.create( SingleCallbackTask.class );

        private static final ErrorCode TASK_FAILED = new ErrorCode( "SLT100", "Failed to run task, name=" );
        
        private final Callback       _listener;
        private final ScheduledEvent _event;

        public SingleCallbackTask( Callback listener, ScheduledEvent event ) {
            super( event.name() + ":" + listener.getName() );

            _listener = listener;
            _event    = event;
        }
        
        @Override
        public void fire() {
            try {
                _listener.event( _event );
            } catch( Exception e ) {
                _sclog.error( TASK_FAILED, _listener.getName(), e );
            }
        }
    }
        
    private Map<ScheduledEvent, Map<Callback,SingleCallbackTask>> _eventNotifierMap = new HashMap<ScheduledEvent, Map<Callback,SingleCallbackTask>>();
    private Map<ScheduledEvent, GroupListenerTask>   _eventNotifierGroupMap = new HashMap<ScheduledEvent, GroupListenerTask>();

    private ZTimer _timer = new ZTimer( "Scheduler", ThreadPriority.Scheduler );
    
    private static Scheduler _instance = new Scheduler();

    public static Scheduler instance() {
        return _instance;
    }

    private Scheduler() {
        //
    }
    
    public synchronized void registerGroupRepeating( ScheduledEvent event, Calendar fireNext, long repeatPeriodMS ) {
        GroupListenerTask task = _eventNotifierGroupMap.get( event );
        
        if ( task == null ) {
            task = new GroupListenerTask( null, event );
            
            _eventNotifierGroupMap.put( event, task );
        } else {
            _log.info( "Event was registered with scheduler, setting new time " + event );
        }

        DateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss (z)" );
        df.setCalendar( fireNext );

        DateFormat dfUTC = new SimpleDateFormat( "HH:mm:ss (z)" );
        dfUTC.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

        DateFormat dfSYS = new SimpleDateFormat( "HH:mm:ss (z)" );
        dfSYS.setTimeZone( new TimeZoneCalculator().getLocalTimeZone() );

        _log.info( "Scheduler.registerGroupRepeating " + event + 
                   ", nextFire="       + df.format( fireNext.getTime() ) + 
                   ", nextFireUTC="    + dfUTC.format( fireNext.getTime() ) + 
                   ", nextFireSYS="    + dfSYS.format( fireNext.getTime() ) + 
                   ", repeatPeriodMS=" + repeatPeriodMS );
        
        task.setScheduled( true );

        _timer.schedule( task, fireNext.getTime().getTime(), repeatPeriodMS );
    }
    
    public synchronized void registerForGroupEvent( ScheduledEvent event, Callback listener ) {

        GroupListenerTask task = _eventNotifierGroupMap.get( event );
        
        if ( task == null ) {
            task = new GroupListenerTask( listener, event );
            
            _eventNotifierGroupMap.put( event, task );
        } else {
            task.addListener( listener );
        }
        
        _log.info( "Scheduler.registerForGroupEvent " + event + ", listener " + listener.getName() + ", isEventScheduled=" + task.isScheduled() );
    }

    public synchronized void registerIndividualRepeating( ScheduledEvent event, Callback listener, long nextFireMS, long repeatPeriodMS ) {
        Map<Callback,SingleCallbackTask> eventMap = _eventNotifierMap.get( event );
        
        if ( eventMap == null ) {
            eventMap = new LinkedHashMap<Callback,SingleCallbackTask>();
            
            _eventNotifierMap.put( event, eventMap );
        }
        
        SingleCallbackTask task = eventMap.get( listener );
        
        if ( task != null ) {
            _log.info( "Scheduler.registerIndividualRepeating cancel previous event " + event + " for listener " + listener.getName() );
            
            task.cancel();
        }

        _log.info( "Scheduler.registerIndividualRepeating " + event + ", listener " + listener.getName() + 
                   ", nextFireMS=" + nextFireMS + ", repeatPeriodMS=" + repeatPeriodMS );
        
        task = new SingleCallbackTask( listener, event );
        
        eventMap.put( listener, task );

        _timer.schedule( task, System.currentTimeMillis() + nextFireMS, repeatPeriodMS );
    }

    public synchronized void cancelIndividual( ScheduledEvent event, Callback listener ) {
        Map<Callback,SingleCallbackTask> eventMap = _eventNotifierMap.get( event );
        
        if ( eventMap == null ) {
            return;
        }
        
        SingleCallbackTask task = eventMap.get( listener );
        
        if ( task != null && !task.isCancelled() ) {
            _log.info( "Scheduler.cancelIndividual event " + event + " for listener " + listener.getName() );

            task.cancel();
        }
    }
}
