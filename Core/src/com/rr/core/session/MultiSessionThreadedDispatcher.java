/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import java.util.concurrent.atomic.AtomicBoolean;

import com.rr.core.collections.MessageQueue;
import com.rr.core.collections.SimpleMessageQueue;
import com.rr.core.component.SMTStartContext;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.NullMessage;
import com.rr.core.thread.ControlThread;
import com.rr.core.thread.ExecutableElement;
import com.rr.core.utils.SMTRuntimeException;


/**
 * a multi session threaded dispatcher for session outbound messages
 * 
 * does NOT own spinning control thread, that is shared and which round robins sessions 
 * 
 * if a session cannot send a message eg socket blocked due to slow consumer then the NonBlockingSession returns immediately
 * that session will have to wait its turn to retry writing remaining data to the socket
 * 
 * Currently max sockets is 32 but this could easily be changed to 64 with minimal delay impact IF using OS bypass eg Solarflare OpenOnload
 */
public final class MultiSessionThreadedDispatcher implements MultiSessionDispatcher, ExecutableElement {

    private static final Logger   _log = LoggerFactory.create( MultiSessionThreadedDispatcher.class );

    private static final ErrorCode MISSING_HANDLER = new ErrorCode( "MST100", "No registered dispatcher for message to session " );
            static final ZString   DISCONNECTED    = new ViewString( "Unable to dispatch as destination Session Disconnected" );
    private static final ZString   DROP_MSG        = new ViewString( "Dropping session message as not logged in, type=" );

    private class SessionWrapper  {
        final MessageQueue       _queue;
        final MessageQueue       _syncQueue;
        final MessageQueue       _preQueue;               // when disconnected flushed events that need to be kept go here
        final NonBlockingSession _session;
              boolean            _connected = false;

        SessionWrapper( NonBlockingSession session, MessageQueue queue, MessageQueue syncQueue ) {
            _queue     = queue;
            _syncQueue = syncQueue;
            _session   = session;
            _preQueue  = new SimpleMessageQueue(); 
        }
    }
    
    private          SessionWrapper[]                  _sessions = new SessionWrapper[0];

    private          int                               _nextSession=0;
    
    private final    ReusableString                    _logMsg = new ReusableString( 50 );
    private          boolean                           _allDisconnected = true;              // not volatile as mem barrier already occurs when get msg off queue 

    private final    Object                            _disconnectLock = new Object();
    
    private final    ControlThread                     _ctl;
    
    private          Message                           _curMsg   = null;
    private          SessionWrapper                    _curSessW = null;

    private volatile boolean                           _fullyFlushed = false;

    private          AtomicBoolean                     _stopping = new AtomicBoolean(false);

    private final    String                            _id;
    

    // array index assigned to session to save map lookup

    public MultiSessionThreadedDispatcher( String id, ControlThread ctl ) {
        _ctl  = ctl;
        _id   = id;
        
        ctl.register( this );
    }

    @Override
    public void threadedInit() {
        for( int idx=0 ; idx < _sessions.length ; ++ idx ) {
            _sessions[idx]._session.threadedInit();
        }
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void notReady() {
        disconnectedFlushAll();
    }
    
    @Override
    public void execute() throws Exception {
        
        _curSessW = _sessions[ _nextSession ];
        
        final MessageQueue       queue    = _curSessW._queue;
        final MessageQueue       preQueue = _curSessW._preQueue;
        final NonBlockingSession sess     = _curSessW._session;
        
        if ( ++_nextSession >= _sessions.length ) _nextSession = 0;
        
        if ( _curSessW._connected && sess.canHandle() ) {
            if ( sess.isLoggedIn() ) {
                if ( sess.isMsgPendingWrite() ) { 
                    sess.retryCompleteWrite();
                } else if ( preQueue.isEmpty() ) {
                    _curMsg = queue.poll();                     // POLL = non blocking, causes MEM_READ barrier 
                    if ( _curMsg != null && _curMsg.getReusableType() != CoreReusableType.NullMessage ) {
                        sess.handleNow( _curMsg );
                    }
                } else { // QUEUED MESSAGES FROM PREVIOUS FLUSH CALLS
                    _curMsg = preQueue.next(); 
                    if ( _curMsg.getReusableType() != CoreReusableType.NullMessage ) {
                        sess.handleNow( _curMsg );
                    }
                }
            } else { // SYNC mode
                final MessageQueue syncQueue = _curSessW._syncQueue;
                if ( sess.isMsgPendingWrite() ) { 
                    sess.retryCompleteWrite();
                } else if ( ! syncQueue.isEmpty() ) {
                    _curMsg = syncQueue.next();  
                    if ( _curMsg.getReusableType() != CoreReusableType.NullMessage ) {
                        sess.handleNow( _curMsg );
                    }
                }
            }
        } else {
            flush( _curSessW );
        }
    }

    @Override
    public void handleExecutionException( Exception ex ) {
        final NonBlockingSession sess = _curSessW._session;
        
        if ( _curMsg != null && sess != null ) {
            _log.warn( "SessionThreadedDispatcher " + getComponentId() + ", msgSeqNum=" + _curMsg.getMsgSeqNum() + 
                       ", sess=" + sess.getComponentId() + " exception " + ex.getMessage() );
        }
        
        flush( _curSessW );

        // some problem, possibly disconnect, poke controller to wake up anything waiting on controller passive lock
        _ctl.statusChange(); // Mem barrier
    }

    @Override
    public boolean checkReady() {
        return ( _allDisconnected == false );
    }

    @Override
    public synchronized void start() {
        _ctl.start();
    }
    
    @Override
    public void setHandler( MessageHandler handler ) {
        throw new SMTRuntimeException( "MultiSessionThreadedDispatcher MISCONFIGURATION : only for use with multi session and addSession()" );
    }

    /**
     * @param session - a non blocking session ie an NIO one that wont block if socket cant read/write
     */
    @Override
    public synchronized void addSession( final NonBlockingSession session ) {
        
        SessionWrapper[] newSessions = new SessionWrapper[ _sessions.length + 1 ];
        
        int idx=0;
        while( idx < _sessions.length ) {
            newSessions[idx] = _sessions[idx];
            ++idx;
        }
        
        newSessions[ idx ] = new SessionWrapper( session, session.getSendQueue(), session.getSendSyncQueue() );
        
        _sessions = newSessions;
        
        _fullyFlushed = false;
    }

    @Override
    public void dispatch( final Message msg ) {
        if ( msg != null ) {
            final MessageHandler handler = msg.getMessageHandler(); 
            final NonBlockingSession session  = (NonBlockingSession)handler;
            final MessageQueue queue = session.getSendQueue();
            if ( queue != null ) {
                queue.add( msg );
            } else {
                // should NEVER happen
                ReusableString s = TLC.instance().pop();
                s.copy( ((Session)handler).getComponentId() ).append( ": Missing Queue, unable to dispatch : " );
                msg.dump( s );
                _log.error( MISSING_HANDLER, s );
                TLC.instance().pushback( s );
            }
        }
    }

    @Override
    public void setStopping() {
        
        // dont actually stop, but wake up to force flush
        
        final int numSessions = _sessions.length;
        
        for ( int i=0 ; i < numSessions ; i++ ) {
            final SessionWrapper s = _sessions[i];
            
            MessageQueue q = s._queue;
            
            q.add( new NullMessage() ); // wake up queue
        }
        
        _fullyFlushed = false;
    }
    
    @Override
    public void stop() {
        if ( _stopping.compareAndSet( false, true ) ) {
            _ctl.setStopping( true );
            
            final int numSessions = _sessions.length;
            
            for ( int i=0 ; i < numSessions ; i++ ) {
                final SessionWrapper s = _sessions[i];
                
                MessageQueue q = s._queue;
                
                q.add( new NullMessage() ); // wake up queue
            }
    
            _fullyFlushed = false;
        }
    }
    
    @Override
    public void handlerStatusChange( MessageHandler handler, boolean connected ) {
        final int numSessions = _sessions.length;
        
        boolean allDisconnected = true;
        
        for ( int i=0 ; i < numSessions ; i++ ) {
            SessionWrapper sessW = _sessions[i];
            
            if ( sessW._session == handler ){
                if ( connected != sessW._connected ) {
                    final NonBlockingSession sess = sessW._session;
                    
                    _log.info( "MultiSession OutDispatcher " + getComponentId() + " : " + ((connected) ? "CONNECTED" : "DISCONNECTED") + 
                               " with " + sess.getComponentId() + ", canHandle=" + sess.canHandle() + ", isLoggedIn=" + sess.isLoggedIn() );
                    
                    sessW._connected = connected;
                }
            }
            
            if ( sessW._connected ) {
                allDisconnected = false;
            }
        }
        
        _fullyFlushed = false;
        
        synchronized( _disconnectLock ) {       // force mem barrier
            _allDisconnected = allDisconnected;
        }
        
        _ctl.statusChange();
    }

    @Override
    public boolean canQueue() {
        return true;
    }
    
    @Override
    public String info() {
        return "MultiSessionThreadedDispatcher( " + _id + " )";
    }

    @Override
    public void dispatchForSync( Message msg ) {
        if ( msg != null ) {
            final MessageHandler handler = msg.getMessageHandler(); // cant be null
            NonBlockingSession session  = (NonBlockingSession)handler;
            MessageQueue queue = session.getSendSyncQueue();
            if ( queue != null ) {
                queue.add( msg );
            } else {
                // @TODO add ReusableString write( ReusableString buf ) to Message and log details
                // should NEVER happen
                _log.error( MISSING_HANDLER, ((Session)handler).getComponentId() );
            }
        }
    }
    
    private void disconnectedFlushAll() {
        if ( !_fullyFlushed ) {

            final int numSessions = _sessions.length;
            
            for ( int i=0 ; i < numSessions ; i++ ){
                flush( _sessions[i] );
            }
            
            _fullyFlushed = true;
        }
    }

    // @NOTE keep flush private the preQ is not threadsafe
    private void flush( SessionWrapper sessW ) {
        // disconnected drop any session messages
        // optionally keep any other messages or reject back  upstream
        
        final Session      session = sessW._session;
        final MessageQueue queue   = sessW._queue;
        final MessageQueue preQ    = sessW._preQueue;
        final MessageQueue syncQ   = sessW._syncQueue;
        
        while( ! syncQ.isEmpty() ) {
            syncQ.next(); // DISCARD
        }
        
        Message head = null;
        Message tail = null;

        while( ! queue.isEmpty() ) {
            Message msg = queue.next();
            if ( msg.getReusableType() == CoreReusableType.NullMessage ) break;
            
            if ( session.discardOnDisconnect( msg ) == false ) {
                if ( session.rejectMessageUpstream( msg, DISCONNECTED ) ) {
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
                session.outboundRecycle( msg );
            }
        }

        // move remaining messages to the preQ
        
        if ( head != null ) {
            Message tmp = head;
            
            while( tmp != null ) {
                Message next = tmp.getNextQueueEntry();
                tmp.detachQueue();
                preQ.add( tmp );
                tmp = next;
            }
        }
    }

    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }

    @Override
    public void prepare() {
        // nothing
    }

    @Override
    public void startWork() {
        start();
    }

    @Override
    public void stopWork() {
        stop();
    }
}
