/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import java.nio.ByteBuffer;

import com.rr.core.component.SMTControllableComponent;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;

public interface Session extends MessageHandler, SMTControllableComponent {

    public static final byte PERSIST_FLAG_CONFIRM_SENT = 1 << 3;    // in upper byte of short flags

    public enum State { Listening, Connected, Disconnected }

    public void init() throws PersisterException;
    
    /**
     * pass in explicit receiver for the session
     * @param receiver
     */
    public void attachReceiver( Receiver receiver );

    /**
     * stop the session
     * 
     * @NOTE a stopped session cannot be restarted
     */
    public void stop();
    
    public void recover( RecoveryController ctl );
    
    /**
     * request to connect, actual connection may occur off receiver thread from init
     */
    public void connect();
    
    /**
     * drop current connection, releases appropriate resources
     * 
     * @NOTE dispatch threads must cater for disconnected session and not spinlock
     * 
     * connect can be reinvoked after disconnect
     * 
     * @param tryReconnect - after a disconnection try to reconnect
     */
    public void disconnect( boolean tryReconnect );
    
    public boolean isRejectOnDisconnect();
    
    public void registerConnectionListener( ConnectionListener listener );
    
    public boolean isConnected();

    public boolean isStopping();

    /**
     * connect within the current thread context, invoked by receiver thread
     */
    public void internalConnect();

    public State getSessionState();

    /**
     * @return true is session is fully logged in
     * @TODO refactor out replace by adding LoggedIn to State
     */
    public boolean isLoggedIn();

    /**
     * call to process the next message, blocking call
     * @throws Exception, 
     *         SessionException 
     *         IOException  
     */
    public void processNextInbound() throws Exception;

    /**
     * send the message on the current thread of control (invoked from MessageDispatcher)
     * 
     * @param msg
     */
    @Override
    public void handleNow( Message msg );

    /**
     * dispatch a message for async sending as part of the sync process
     */
    public void handleForSync( Message msg );
    
    /**
     * @param reject if true then reject messages if disconnected
     */
    public void setRejectOnDisconnect( boolean reject );

    public boolean getRejectOnDisconnect();

    /**
     * process inbound messages until finished set or connection problem
     * 
     * @NOTE causes disconnect on SessionException, IOException  
     */
    public void processIncoming();
    
    /**
     * @param logStats if true timestamp message in/out 
     */
    public void setLogStats( boolean logStats );

    /**
     * @param on log in and out events
     */
    public void setLogEvents( boolean on );
    
    public boolean isLogEvents();
    
    /**
     * if disconnected then this routine is used to synth a reject and send upstream
     * 
     * session should not reject posDup messages or reconciliation messages
     * 
     * @param msg
     * @param errMsg
     * @return          true IF the message was reject was generated and sent upstream
     */
    public boolean rejectMessageUpstream( Message msg, ZString errMsg );

    /**
     * @param msg
     * @return      true IF the message should be discarded after a disconnect eg a session message like heartbeat 
     */
    public boolean discardOnDisconnect( Message msg );

    /**
     * @param sess session to dispatch all outbound messages after they have been sent over wire
     */
    public void setChainSession( Session sess );

    /**
     * @return next session in chain
     */
    public Session getChainSession();

    /**
     * @param msg use the sessions INBOUND recycler to recycle the message
     */
    public void inboundRecycle( Message msg );

    /**
     * @param msg use the sessions OUTBOUND recycler to recycle the message
     */
    public void outboundRecycle( Message msg );

    /**
     * invoke the router to dispatch inbound message (1 at a time if msg chained)
     * 
     * @param msg decoded message which is already logged and persisted
     */
    public void dispatchInbound( Message msg );

    /**
     * BLOCKING call waiting for recovery to finish replay
     * 
     * @NOTE ensure invoke recover first
     */
    public void waitForRecoveryToComplete();

    /**
     * @return if nano logging enabled then return the time of the last message sent
     */
    public long getLastSent();

    /**
     * @return a string with description of the session
     */
    public String info();

    /**
     * @param pausedif true stop the session and dont reconnect until invoked again with paused set as false
     */
    public void setPaused( boolean paused );
    
    /**
     * persist the last message received
     */
    public void persistLastInboundMesssage();
    
    public SessionConfig getConfig();

    public boolean isPaused();
    
    /**
     * session direction, mainly to identify upstream vs downstream sessions purposes
     */
    public SessionDirection getDirection();

    /**
     * set throttler for sending of messages, messages exceeding this rate will be rejected
     * 
     * @param throttleNoMsgs - restrict new messages to this many messages per period (throttler may allow cancels and reject NOS/REP)
     * @param disconnectLimit - total message limit in period (all messages rejected)
     * @param throttleTimeIntervalMS - throttle period in ms
     */
    public void setThrottle( int throttleNoMsgs, int disconnectLimit, long throttleTimeIntervalMS );
    
    /**
     * read message from persistence using persistence key
     * 
     * @NOTE this method is not on Session interface and is only for use in reconciliation/recovery
     * 
     * @WARNING threading will be a problem if invoked outside of reconciliation  
     * 
     * @param isInbound     if true retrieve message from inbound persister, otherwise use outbound persister 
     * @param persistKey    key previously returned from a write to persister ... usually start address in persister
     * @param tmpBigBuf     tmp buffer big enough to read message (usually 8k buff which is reused)
     * @param tmpCtxBuf     tmp buffer for reading the optional context (if any)
     * 
     * @return the requested event regenerated OR null if unable to regenerate
     */
    public Message recoverEvent( boolean isInbound, long persistKey, ReusableString tmpBigBuf, ByteBuffer tmpCtxBuf );
}
