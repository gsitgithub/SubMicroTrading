/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.thread;

import com.rr.core.lang.Constants;
import com.rr.core.lang.ErrorCode;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;

public abstract class AbstractControlThread extends Thread implements ControlThread {

    private   static final ErrorCode MISSING_ELEM = new ErrorCode( "ACT100", "Misconfiguration! " );

    private   final Logger            _log  = LoggerFactory.create( AbstractControlThread.class );
    private         ThreadPriority    _priority;
    private   final String            _name;
    private         boolean           _stopping = false;
    private         boolean           _started = false;
    protected final Object            _passiveLock = new Object();

    public AbstractControlThread( String name, ThreadPriority priority ) {
        
        super( name.toString() );
        
        setDaemon( true );
        
        _log.info( "AbstractControlThread: created " + getClass().getSimpleName() + " id=" + name );
        
        _priority = priority;
        _name     = name;
    }
    
    @Override
    public String getComponentId() {
        return _name;
    }
    
    @Override
    public abstract void register( ExecutableElement ex );

    @Override
    public synchronized void start() {
        if ( !_started ) {
            super.start();
            _log.info( "Starting AbstractControlThread " + getName() );
            _started = true;
        }
    }
    
    @Override
    public void run() {

        ThreadUtils.setPriority( this, _priority );

        _log.info( "CONTROL LOOP STARTED " + _name );

        runControlLoop();
        
        _log.info( "CONTROL LOOP FINISHED " + _name );
    }

    @Override
    public void setStopping( boolean stopping ) {
        synchronized( this ) {
            _stopping  = stopping;
        }
        statusChange();
    }
    
    @Override
    public synchronized boolean isStopping() { 
        return _stopping;
    }

    @Override
    public void setPriority( ThreadPriority priority ) {
        _priority = priority;
    }

    @Override
    public boolean isStarted() {
        return     _started;
    }

    @Override
    public void statusChange() {
        synchronized( _passiveLock ) {
            _passiveLock.notifyAll();    
        }
    }

    protected abstract void runControlLoop();
    
    protected void abortMissingController() {
        _log.error( MISSING_ELEM, getClass().getSimpleName() + " missing worker, controlId=" + getName() );

        ShutdownManager.instance().shutdown( -1 );
    }
    
    protected final void goPassive() {
        
        synchronized( _passiveLock ) {
            try {
                _passiveLock.wait( Constants.LOW_PRI_LOOP_WAIT_MS );
            } catch( InterruptedException e ) {
                // dont care
            }
        }
    }
}
