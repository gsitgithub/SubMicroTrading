/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;

public class ThreadedReceiver extends Thread implements Receiver {
    
    private final Logger _rlog  = LoggerFactory.create( ThreadedReceiver.class );

    private final Session        _session;
    private       boolean        _stopping = false;
    private       ThreadPriority _threadPriority;

    private volatile boolean     _started = false;
    private final    String      _id;
    
    public ThreadedReceiver( Session session, ThreadPriority threadPriority ) {
        this( (session.getComponentId() + "Receiver"), session, threadPriority );
    }

    public ThreadedReceiver( String id, Session session, ThreadPriority threadPriority ) {
        super( id );
        _id = id;
        _session = session;
        _threadPriority = threadPriority; 
        setDaemon( true );
    }
    
    @Override
    public synchronized void start() {
        if ( !_started ) {
            super.start();
            _started = true;
        }
    }
    
    // when get socket error on reader or writer then both threads need to  stop processing before open new socket 
    
    @Override
    public void run() {

        ThreadUtils.setPriority( this, _threadPriority );

        _rlog.info( "Session " + getName() + " RECEIVE LOOP STARTED" );

        while( !isStopping() ) {
            
            _session.internalConnect(); // connect within context of the receiver thread
            
            if ( !isStopping() ) {
                if ( _session.getSessionState() == Session.State.Connected ) {
                    _session.processIncoming();
                }
                
                if ( _session.getSessionState() == Session.State.Disconnected ) {  // couldnt connect OR just disconnected dont retry straight away
                    Utils.delay( _session, SessionConstants.CONNECT_WAIT_DELAY_MS );
                }
            }
        }
        
        _rlog.info( "Session " + _session.getComponentId() + " RECEIVE LOOP FINISHED" );
    }

    @Override
    public synchronized void setStopping( boolean stopping ) {
        _stopping  = stopping;
    }
    
    private synchronized boolean isStopping() { 
        return _stopping;
    }

    @Override
    public boolean isStarted() {
        return     _started;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}

