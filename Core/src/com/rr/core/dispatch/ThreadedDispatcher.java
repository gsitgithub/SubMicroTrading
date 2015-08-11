/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.dispatch;

import com.rr.core.collections.MessageQueue;
import com.rr.core.component.SMTStartContext;
import com.rr.core.lang.Constants;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ErrorCode;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.NullMessage;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;

public final class ThreadedDispatcher implements MessageDispatcher {

    static final Logger     _log            = LoggerFactory.create( ThreadedDispatcher.class );
    
    private static final class Dispatcher extends Thread {

        private static final long      MAX_PAUSE         = Constants.LOW_PRI_LOOP_WAIT_MS;
        private static final ErrorCode ERR_DISPATCH_PROC = new ErrorCode( "THD100", "Error processing dispatched message"); 
        
        private final    MessageQueue   _queue;
        private final    ThreadPriority _threadPriority;
        private          MessageHandler _handler;
        private volatile boolean        _finished;

        public Dispatcher( String threadName, ThreadPriority threadPriority, MessageQueue queue ) {
            super( threadName );
            
            _queue          = queue;
            _threadPriority = threadPriority;
            
            setDaemon( true );
        }

        public void setHandler( MessageHandler handler ) {
            _handler = handler;
        }

        public void setFinished()  {
            _finished = true;
            _queue.add( new NullMessage() ); // wake up queue
        }
        
        @Override
        public void run() {
            
            ThreadUtils.setPriority( this, _threadPriority );
            
            _handler.threadedInit();
            
            while ( !_finished ) { 
                connectedHandler();
                disconnectedHandler();
            }

            _log.info( "ThreadedDispatcher " + getName() + " finished" );
        }

        private void disconnectedHandler() {
            if ( ! _handler.canHandle() && !_finished ) {
                synchronized( _queue ) {
                    try {
                        _queue.wait( MAX_PAUSE );
                    } catch( InterruptedException e ) {
                        // dont care
                    }
                }
            }
        }

        private void connectedHandler() {
            if ( _handler.canHandle() ) {
                try {
                    Message msg;
                    while( !_finished && _handler.canHandle() ) {
                        msg = _queue.next(); 
     
                        if ( msg.getReusableType() != CoreReusableType.NullMessage ) {
                            _handler.handleNow( msg );
                        }
                    }
                } catch( Exception e ) {
                    _log.error( ERR_DISPATCH_PROC, "ThreadedDispatcher " + getName() + " exception ", e );
                }
            }
        }
    }
    
    private final    MessageQueue   _queue;
    private final    Dispatcher     _threadDispatcher;
    private final    String         _id;

    private volatile boolean        _started = false;

    public ThreadedDispatcher( String id, MessageQueue queue, ThreadPriority threadPriority ) {
        this( id, queue, threadPriority, null );
    }
    
    public ThreadedDispatcher( String id, MessageQueue queue, ThreadPriority threadPriority, MessageHandler handler ) {
        super();
        _id               = id;
        _queue            = queue;
        
        String dispatchName = "DISPATCH_" + id.toString();

        _threadDispatcher = new Dispatcher( dispatchName, threadPriority, queue );
        
        if ( handler != null ) {
            _threadDispatcher.setHandler( handler );
        }
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
    public synchronized void start() {
        if ( !_started ) {
            _threadDispatcher.setDaemon( true );
            _threadDispatcher.start();
            _started = true;
        }
    }
    
    @Override
    public void setHandler( MessageHandler handler ) {
        _threadDispatcher.setHandler( handler );
    }

    @Override
    public void dispatch( Message msg ) {
        _queue.add( msg );
    }

    @Override
    public void setStopping() {
        _threadDispatcher.setFinished();
    }

    @Override
    public void handlerStatusChange( MessageHandler handler, boolean isOk ) {
        // nothing
    }

    @Override
    public boolean canQueue() {
        return true;
    }

    @Override
    public String info() {
        return "ThreadedDispatcher( " + _threadDispatcher.getName() + ", queue=" + _queue.getClass().getSimpleName() + ")";
    }

    @Override
    public void dispatchForSync( Message msg ) {
        _queue.add( msg );
    }
    
    @Override
    public void startWork() {
        start();
    }

    @Override
    public void stopWork() {
        setStopping();
    }

    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }

    @Override
    public void prepare() {
        // nothing
    }
}
