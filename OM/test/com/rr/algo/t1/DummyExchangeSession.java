/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.algo.t1;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.rr.core.component.SMTStartContext;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.ConnectionListener;
import com.rr.core.session.Receiver;
import com.rr.core.session.Session;
import com.rr.core.session.SessionConfig;
import com.rr.core.session.SessionDirection;

public class DummyExchangeSession implements Session {

    private final List<Message> _events = new ArrayList<Message>();
    private       Session       _chainSession = null;
    
    public DummyExchangeSession() {
    }

    @Override
    public void handle( Message event) {
        handleNow( event );
    }

    public List<Message> getEvents() {
        return _events;
    }

    @Override public void handleNow( Message msg ) {
        _events.add( msg ); 
        
        if ( _chainSession != null ) {
            _chainSession.handle( msg );
        }
    }

    @Override public void setChainSession( Session sess ) {
        if ( sess.getClass() != DummyExchangeSession.class ) {
            throw new RuntimeException( "UNSAFE can only use DummyExchangeSession as chain session due to potential recycling issues" );
        }
        _chainSession = sess;
    }
    
    @Override public Session getChainSession() { 
        return _chainSession;
    }

    @Override public boolean    isLoggedIn()    { return true; }
    @Override public String     getComponentId()       { return null; }
    @Override public void       threadedInit()  { /* nothing */ }
    @Override public boolean    canHandle()     { return true; }
    @Override public void init() throws PersisterException { /* nothing */ }
    @Override public void attachReceiver( Receiver receiver ) { /* nothing */ }
    @Override public void stop() { /* nothing */ }
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
    @Override public void startWork() { /* nothing */ }
    @Override public void stopWork() { /* nothing */ }
    @Override public void init( SMTStartContext ctx ) { /* nothing */ }
    @Override public void prepare() { /* nothing */ }
}
