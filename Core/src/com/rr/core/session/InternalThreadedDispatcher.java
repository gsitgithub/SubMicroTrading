/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.NullMessage;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;

/**
 * a pausable dispatcher customised for session awareness
 */
final class InternalThreadedDispatcher extends Thread {

    static final Logger     _log            = LoggerFactory.create( InternalThreadedDispatcher.class );
    static final ZString    DISCONNECTED    = new ViewString( "Unable to dispatch as destination Session Disconnected" );

    private static final ZString DROP_MSG = new ViewString( "Dropping session message as not logged in, type=" );
    
    private final int            _maxPause;
    private final MessageQueue   _queue;
    private final MessageQueue   _syncQueue;
    private final ThreadPriority _threadPriority;
    private       Session        _session;
    private       boolean        _finished;

    private       boolean        _connected = false;

    private final ReusableString _logMsg = new ReusableString( "50" );

    InternalThreadedDispatcher( String         threadName, 
                                ThreadPriority threadPriority, 
                                MessageQueue   queue, 
                                MessageQueue   syncQueue,
                                int            maxPause ) {
        super( threadName );
        
        _maxPause       = maxPause;
        _queue          = queue;
        _syncQueue      = syncQueue;
        _threadPriority = threadPriority;
        
        setDaemon( true );
    }

    void setSession( Session session ) {
        _session = session;
    }

    void setFinished()  {
        _finished = true;
        _queue.add( new NullMessage() ); // wake up queue
        
    }
    
    @Override
    public void run() {
        
        _session.threadedInit();
        
        ThreadUtils.setPriority( this, _threadPriority );

        _log.info( _session.getComponentId() + " InternalThreadedDispatcher running, disconnectPause=" + _maxPause );
        
        while ( !_finished ) { // finished is not volatile but will be updated by sync mem barrier in processor
            if ( _maxPause > 1000 ) {
                _log.info( _session.getComponentId() + " Session connected, about to processes events" );
            }
            connectedEventProcessing();
            if ( _maxPause > 1000 ) {
                _log.info( _session.getComponentId() + " Session cant handle events so invoke disconnected flush" );
            }
            disconnectedFlush();
        }
        
        _log.info( "SessionThreadedDispatcher " + getName() + " finished" );
    }

    private void disconnectedFlush() {
        
        int postFlush = 0;
        
        if ( ! _connected ) {
            flush();
            postFlush = _queue.size();
        }

        if ( _maxPause > 1000 ) {
            _log.info( "InternalThreadedDispatcher " + getName() + " disconnectedFlush START sleep pending wakeup connected=" + _connected + ", postFlush" + postFlush + ", qsize=" + _queue.size() );
        }

        // sleep until session can handle messages or more messages appear that require flushing
        
        while( !_finished && ! _session.canHandle() && _queue.size() == postFlush ) {
            synchronized( _queue ) {
                try {
                    _queue.wait( _maxPause );
                } catch( InterruptedException e ) {
                    _log.info( e.getMessage() );
                }
            }
        } 

        if ( _maxPause > 1000 ) {
            _log.info( "InternalThreadedDispatcher " + getName() + " disconnectedFlush END sleep pending wakeup connected=" + _connected + ", postFlush" + postFlush + ", qsize=" + _queue.size() );
        }
    }

    private void connectedEventProcessing() {
        if ( _session.canHandle() ) {
            try {
                sendSyncMessages();

                doProcessConnectedEvents();
            } catch( Exception e ) {
                _log.info( "InternalThreadedDispatcher " + getName() + " exception " + e.getMessage());
            }
        }
    }

    private void doProcessConnectedEvents() {
        Message msg;

        while( !_finished && _connected ) {
            msg = _queue.next(); 
    
            if ( msg.getReusableType() != CoreReusableType.NullMessage ) {
                _session.handleNow( msg );
            }
        }
    }

    private void sendSyncMessages() {
        try {
            Message msg = null;

            while( !_finished && !_session.isLoggedIn() && _connected ) {
                msg = _syncQueue.next(); 
       
                if ( msg.getReusableType() != CoreReusableType.NullMessage ) {
                    _session.handleNow( msg );
                }
            }
            
            while( !_finished && !_syncQueue.isEmpty() && _connected ) { // drain syncQ
                msg = _syncQueue.next(); 
       
                if ( msg.getReusableType() != CoreReusableType.NullMessage ) {
                    _session.handleNow( msg );
                }
            }

/*            
            while( !_finished && !_syncQueue.isEmpty() && !_session.isLoggedIn() && _connected ) {
                msg = _syncQueue.next(); 
       
                if ( msg.getReusableType() != CoreReusableType.NullMessage ) {
                    _session.handleNow( msg );
                }
            }
            
            while( !_finished && !_syncQueue.isEmpty() && _connected ) { // drain syncQ
                msg = _syncQueue.next(); 
       
                if ( msg.getReusableType() != CoreReusableType.NullMessage ) {
                    _session.handleNow( msg );
                }
            }
*/            
        } catch( Exception e ) {
            _log.info( "SessionThreadedDispatcher " + getName() + " exception " + e.getMessage());
        }
    }

    private void flush() {
        
        // throw away anything in the syncQ
        while( ! _syncQueue.isEmpty() ) {
            _syncQueue.next();
        }
        
        // disconnected drop any session messages
        // optionally keep any other messages or reject back  upstream
        
        Message head = null;
        Message tail = null;

        while( ! _queue.isEmpty() ) {
            Message msg = _queue.next();
            if ( msg == null || msg.getReusableType() == CoreReusableType.NullMessage ) break;
            
            if ( _session.discardOnDisconnect( msg ) == false ) {
            
                if ( _session.rejectMessageUpstream( msg, DISCONNECTED ) ) {
                    // message recycled by successful reject processing
                } else {

                    if ( tail == null ) {
                        head = msg;
                        tail = msg;
                    } else {
                        tail.attachQueue( msg );
                        tail = msg;
                    }
                }
            } else {
                _logMsg.copy( DROP_MSG ).append( msg.getReusableType().toString() );
                
                _log.info( _logMsg );
                
                _session.outboundRecycle( msg );
            }
        }

        // Replay outstanding messages back to the transmission queue
        
        if ( head != null ) {
            
            Message tmp = head;
            
            while( tmp != null ) {
                Message next = tmp.getNextQueueEntry();
                tmp.detachQueue();
                _queue.add( tmp );
                tmp = next;
            }
        }
        
        if ( !_finished ) {
            synchronized( _queue ) {
                try {
                    _queue.wait();
                } catch( InterruptedException e ) {
                    // dont care
                }
            }
        }
    }

    public void connected( boolean connected ) {
        if ( connected != _connected ) {
            _log.info( "Session OutDispatcher " + getName() + " : " + ((connected) ? "CONNECTED" : "DISCONNECTED") );
            _connected = connected;
            
            synchronized( _syncQueue ) {
                _syncQueue.notifyAll();
            }

            synchronized( _queue ) {
                _queue.notifyAll();
            }
        }
    }
}
