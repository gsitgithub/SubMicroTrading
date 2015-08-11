/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.tasks;

import java.util.ArrayList;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.tasks.ZTimerTask.TaskState;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;

/**
 * simple Timer specialisation to be able to control the thread affinity / priority of the timer thread
 * on standard linux the timer will have the O/S scheduler margin of error ... around 10ms.
 */
public final class ZTimer {
   static final Logger       _log = LoggerFactory.create( ZTimer.class );
   
   private static class TimerThread extends Thread {
 
        private static final ErrorCode TM100 = new ErrorCode( "ZTM100", "Exception in timer thread" );

        private final TaskQueue      _queue;
        private final ThreadPriority _priority;
        private final ReusableString _msg = new ReusableString();

        boolean                      _running = true;
        long                         _nextDelayMS = 0;

        private boolean _debug = false;

        TimerThread( TaskQueue queue, ThreadPriority priority ) {
            _queue = queue;
            _priority = priority;
        }

        @Override
        public void run() {
            ThreadUtils.setPriority( this, _priority );
            ZTimerTask task;

            while( _running ) {
                task = waitForNextEvent();

                if ( _running && task != null ) {     // still running and non cancelled task
                    if ( _nextDelayMS > 0 ) continue; // next task not ready to fire 

                    if ( _debug )  dumpQueue();
                    
                    fireTask( task );
    
                    postFire( task );
                }
            }
        }

        private void dumpQueue() {
            _msg.copy( "ZTimer dump before fire\n" );    
            _queue.dumpTable( _msg );
            _log.info( _msg );
        }

        private ZTimerTask waitForNextEvent() {
            ZTimerTask task;
            
            synchronized( _queue ) {
                if ( _nextDelayMS >= 0 ) {
                    try {
                        if ( _debug ) _log.info( "ZTimer sleeping for " + _nextDelayMS + "ms" );

                        _queue.wait( _nextDelayMS );
                    } catch( InterruptedException e ) {
                        // dont care
                    }
                }

                task = getNextNonCancelledTask();

                _nextDelayMS = prepTask( task ); // pops from queue and sets to executing IF ready to fire, returns 0 if no tasks left
            }
            
            return task;
        }

        private void fireTask( ZTimerTask task ) {
            if ( ! task.isCancelled() ) {
                if ( _debug ) {
                    _msg.copy( "ZTimer firing " ).append( task.getName() );
                    _log.info( _msg );
                }
                
                try {
                    task.fire();
                } catch( Exception e ) {
                    _log.error( TM100, "Task " + task.getName(), e );
                }
            }
        }

        private void postFire( ZTimerTask task ) {
            synchronized( _queue ) {
                synchronized( task ) {
                    if ( task.isCancelled() ) return;
                    
                    task.setTaskState( TaskState.EXECUTED );
                    if ( task.getTaskInterval() > 0 ) {
                        task.scheduleNext();
                        
                        if ( _debug ) {
                            _msg.copy( "ZTimer " ).append( task.getName() ).append( " reset to " ).append( task.getNextFireTime() - System.currentTimeMillis() ).append( "ms from now" );
                            _log.info( _msg );
                        }
                        
                        _queue.add( task );
                    }
                }

                task = getNextNonCancelledTask();
                _nextDelayMS = nextSleepPeriod( task );
            }
        }

        private long prepTask( ZTimerTask task ) {
            if ( task == null ) return 0;
            
            long delayMS;
            long currentTime;
            long executionTime;
            
            synchronized( task ) { 
                currentTime = System.currentTimeMillis();
                executionTime = task.getNextFireTime();

                delayMS = executionTime - currentTime;

                if ( delayMS <= 0 ) { // ready to fire
                    _queue.pop();

                    if ( ! task.isCancelled() ) task.setTaskState( TaskState.EXECUTING );
                }
            }

            return delayMS;
        }

        private long nextSleepPeriod( ZTimerTask task ) {
            if ( task == null )
                return 0; // sleep until item added

            long delayMS;
            long currentTime;
            long executionTime;
            
            synchronized( task ) { 
                currentTime = System.currentTimeMillis();
                executionTime = task.getNextFireTime();

                delayMS = executionTime - currentTime;
            }
            
            if ( delayMS == 0 ) delayMS = -1; // ready to run now
            
            return delayMS;
        }

        private ZTimerTask getNextNonCancelledTask() {
            ZTimerTask task = _queue.nextToFire(); // must be a task to run to get here

            while( task != null ) {

                synchronized( task ) {
                    if ( task.getTaskState() == TaskState.CANCELLED ) {
                        _queue.pop();
                    } else {
                        return task;
                    }
                }

                task = _queue.nextToFire(); // must be a task to run to get here
            }

            return null;
        }

        public void setDebug( boolean debug ) {
            _debug = debug;
        }
    }

    private static class TaskQueue { // elements always in order of next scheduled time.

        private ArrayList<ZTimerTask> _queue = new ArrayList<ZTimerTask>();

        public TaskQueue() { /* nothing */ }

        public void dumpTable( ReusableString msg ) {
            long now = System.currentTimeMillis();
            
            for( int i = 0 ; i < _queue.size() ; i++ ) {
                msg.append( "i=" ).append( i ).append( " : " ).append( _queue.get( i ).getName() )
                   .append( ", nextFireInMS=" ).append( _queue.get( i ).getNextFireTime() - now )
                   .append( ", state " ).append( _queue.get( i ).getTaskState() )
                   .append( "\n" );
            }
        }

        void add( ZTimerTask task ) {
            long time = task.getNextFireTime();
            int qSize = _queue.size();
            int i = 0;

            for( ; i < qSize ; i++ ) {
                final ZTimerTask t = _queue.get( i );
                if ( time < t.getNextFireTime() ) {
                    break;
                }
            }

            _queue.add( i, task );
        }

        ZTimerTask nextToFire() {
            if ( _queue.size() == 0 ) return null;

            return _queue.get( 0 );
        }

        void pop() {
            _queue.remove( 0 );
        }
    }

    private final TaskQueue   _tasksQueue = new TaskQueue();
    private final TimerThread _workerThread;

    public ZTimer( String name, ThreadPriority priority ) {
        _workerThread = new TimerThread( _tasksQueue, priority );

        _workerThread.setName( name );
        _workerThread.setDaemon( true );
        _workerThread.start();
    }

    /**
     * schedule task to run
     * 
     * @param task
     * @param time the time to fire the task (MS from epoch) ... if this is in the past will fire asap
     * @param repeatInterval 0 for 1 off firing, otherwise number of ms when to repeat fire event
     */
    public void schedule( ZTimerTask task, long time, long repeatInterval ) {
        if ( time <= 0 ) throw new SMTRuntimeException( "Bad time" );
        if ( repeatInterval < 0 ) throw new SMTRuntimeException( "Bad interval" );

        synchronized( _tasksQueue ) {
            if ( !_workerThread._running ) throw new SMTRuntimeException( "ZTimer closed" );

            synchronized( task ) {
                if ( task.getTaskState() != TaskState.INITIAL ) throw new SMTRuntimeException( "Task has previously been scheduled" );

                task.init( time, repeatInterval );
            }

            _log.info( "ZTimer Adding task " + task.getName() + " for " + (time - System.currentTimeMillis()) + "ms from now" );
            
            _tasksQueue.add( task );
            _tasksQueue.notify();       // wake up background thread as its no doubt asleep
        }
    }
    
    public void setDebug( boolean debug ) {
        _workerThread.setDebug( debug );
    }
}
