/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.hub;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.MessageQueue;
import com.rr.core.component.SMTStartContext;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.ConnectionListener;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.NonBlockingSession;
import com.rr.core.session.Receiver;
import com.rr.core.session.Session;
import com.rr.core.session.SessionConfig;
import com.rr.core.session.SessionDirection;
import com.rr.model.generated.internal.events.factory.EventRecycler;


/**
 * a session which only logs events and then recycles them
 *
 * @author Richard Rose
 */
public class AsyncLogSession implements NonBlockingSession {

    private static final Logger _log = LoggerFactory.create( AsyncLogSession.class );
    
    private String _id;

    private EventRecycler _recycler;

    private final MessageQueue   _queue           = new ConcLinkedMsgQueueSingle();
    private final MessageQueue   _sendSyncQueue   = new BlockingSyncQueue();
    private final AtomicBoolean  _isStopping      = new AtomicBoolean( false );
    private final ReusableString _logMsg          = new ReusableString( 1024 );
    
    private MultiSessionThreadedDispatcher  _outboundDispatcher;
    private MultiSessionThreadedReceiver    _inboundDispatcher;

    public AsyncLogSession( String id ) {
        _id = id;
    }

    @Override public void threadedInit()  {  
        _recycler = TLC.instance().getInstanceOf( EventRecycler.class );
    }

    @Override
    public void handle( Message event) {
        _queue.add( event );
    }

    @Override 
    public void handleNow( Message msg ) {
        _logMsg.copy( "DUMMY DROPCOPY : " );
        msg.dump( _logMsg );
        _log.info( _logMsg );
        _recycler.recycle( msg );
    }

    @Override
    public MessageQueue getSendQueue() {
        return _queue;
    }

    @Override
    public MessageQueue getSendSyncQueue() {
        return _sendSyncQueue;
    }
    
    @Override 
    public String getComponentId() { 
        return _id; 
    }

    @Override 
    public void stop() {
        if ( !_isStopping.compareAndSet( false, true ) ) {
            _inboundDispatcher.setStopping( true );
            _outboundDispatcher.setStopping();
        }
    }
    
    @Override 
    public void prepare() { 
        _outboundDispatcher.addSession( this );
        _inboundDispatcher.addSession( this );
    }
    
    @Override 
    public void startWork() {
        // control thread and dispatchers already started
    }
    
    @Override 
    public void stopWork() { 
        stop();
    }

    @Override 
    public boolean canHandle() { 
        return !_isStopping.get(); 
    }
    
    @Override public void init( SMTStartContext ctx ) { /* nothing */ }
    @Override public boolean    isLoggedIn()    { return true; }
    @Override public void init() throws PersisterException { /* nothing */ }
    @Override public void attachReceiver( Receiver receiver ) { /* nothing */ }
    @Override public void recover( RecoveryController ctl ) { /* nothing */ }
    @Override public void connect() { /* nothing */ }
    @Override public void disconnect( boolean tryReconnect ) { /* nothing */ }
    @Override public boolean isRejectOnDisconnect() { return false; }
    @Override public void registerConnectionListener( ConnectionListener listener ) { /* nothing */ }
    @Override public boolean isConnected() { return false; }
    @Override public boolean isStopping() { return false; }
    @Override public void internalConnect() { /* nothing */ }
    @Override public State getSessionState() { return null; }
    @Override public void processNextInbound() throws Exception { /* nothing */ }
    @Override public void handleForSync( Message msg ) { /* nothing */ }
    @Override public void setRejectOnDisconnect( boolean reject ) { /* nothing */ }
    @Override public boolean getRejectOnDisconnect() { return false; }
    @Override public void processIncoming() { /* nothing */ }
    @Override public void setLogStats( boolean logStats ) { /* nothing */ }
    @Override public void setLogEvents( boolean on ) { /* nothing */ }
    @Override public boolean isLogEvents() { return false; }
    @Override public boolean rejectMessageUpstream( Message msg, ZString errMsg ) { return false; }
    @Override public boolean discardOnDisconnect( Message msg ) { return false; }
    @Override public void setChainSession( Session sess ) { /* nothing */ }
    @Override public Session getChainSession() { return null; }
    @Override public void inboundRecycle( Message msg ) { /* nothing */ }
    @Override public void outboundRecycle( Message msg ) { /* nothing */ }
    @Override public void dispatchInbound( Message msg ) { /* nothing */ }
    @Override public void waitForRecoveryToComplete() { /* nothing */ }
    @Override public long getLastSent() { return 0; }
    @Override public String info() { return null; }
    @Override public void setPaused( boolean paused ) { /* nothing */ }
    @Override public void persistLastInboundMesssage() { /* nothing */ }
    @Override public SessionConfig getConfig() { return null; }
    @Override public boolean isPaused() { return false; }
    @Override public SessionDirection getDirection() { return null; }
    @Override public void setThrottle( int throttleNoMsgs, int disconnectLimit, long throttleTimeIntervalMS ) {/* nothing */ }
    @Override public Message recoverEvent( boolean isInbound, long persistKey, ReusableString tmpBigBuf, ByteBuffer tmpCtxBuf ) { return null; }
    @Override public boolean isMsgPendingWrite() { return false; }
    @Override public void retryCompleteWrite() throws IOException { /* nothing */ }
    @Override public void logInboundError( Exception e ) { /* nothing */ }
    @Override public void logInboundDecodingError( RuntimeDecodingException e ) { /* nothing */ }
    @Override public void logOutboundEncodingError( RuntimeEncodingException e ) { /* nothing */ }
    @Override public void logDisconnected( Exception ex ) { /* nothing */ }
}
