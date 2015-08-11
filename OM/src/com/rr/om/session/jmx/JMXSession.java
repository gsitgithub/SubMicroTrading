/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.jmx;

import java.nio.ByteBuffer;

import com.rr.core.admin.AdminAgent;
import com.rr.core.codec.BaseReject;
import com.rr.core.codec.Decoder;
import com.rr.core.component.SMTStartContext;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.ConnectionListener;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.Receiver;
import com.rr.core.session.Session;
import com.rr.core.session.SessionConfig;
import com.rr.core.session.SessionDirection;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.Utils;
import com.rr.om.session.SessionManager;
import com.rr.om.utils.FixUtils;
import com.rr.om.warmup.sim.WarmupUtils;


/**
 * a JMXSession for use in exchange certification by allowing upstream order injection
 * 
 * can inject directly to specified session bypassing processor/controller
 *
 * not for use with MultiSession dispatcher/receiver
 *
 * will update tag 52 / tag 60, and tag 10
 *
 * @WARN doesnt recycle any objects
 * 
 * @author Richard Rose
 */
public class JMXSession implements Session {

    private static final Logger _log = LoggerFactory.create( JMXSession.class );
    
    private final String         _name;
    private final MessageRouter  _inboundRouter; 
    private final Decoder     _decoder;
    private final SessionConfig  _config;
    private final SessionManager _sessMgr;

    private final ZString        _logInHdr;
    private final ZString        _logOutHdr;
    private final ReusableString _logInbound     = new ReusableString();
    private final ReusableString _logOutbound    = new ReusableString();

    private final ReusableString _inMsg          = new ReusableString();

    public JMXSession( String              name,
                       SessionConfig       cfg,
                       MessageRouter       inboundRouter, 
                       Decoder          decoder,
                       SessionManager      sessionManager) {

        _name = name;
        _inboundRouter = inboundRouter;
        _decoder = decoder;
        _config = cfg;
        _sessMgr = sessionManager;
        
        _logInHdr  = new ViewString( "     IN [" + name + "]: " );
        _logOutHdr = new ViewString( "DROP OUT [" + name + "]: ");
    
        _logInbound.copy( _logInHdr );
        _logOutbound.copy( _logOutHdr );
        
        JMXSessionAdmin sma = new JMXSessionAdmin( this );
        AdminAgent.register( sma );
    }

    @Override
    public String getComponentId() {
        return _name;
    }

    @Override
    public void threadedInit() {
        // nothing
    }

    @Override
    public void init( SMTStartContext ctx ) {
        try {
            init();
        } catch( PersisterException e ) {
            throw new SMTRuntimeException( getComponentId() + " failed to initialise : " + e.getMessage(), e );
        }
    }

    @Override
    public void prepare() {
        // nothing
    }

    @Override
    public void startWork() {
        connect();
    }

    @Override
    public void stopWork() {
        stop();
    }
    
    
    @Override
    public void handle( Message msg ) {
        _logOutbound.setLength( _logOutHdr.length() );
        msg.dump( _logOutbound );         
        _log.infoLarge( _logOutbound );
    }

    @Override
    public boolean canHandle() {
        return true;
    }

    @Override
    public void init() throws PersisterException {
        // nothing
    }

    @Override
    public void attachReceiver( Receiver receiver ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void stop() {
        // nothing
    }

    @Override
    public void recover( RecoveryController ctl ) {
        // nothing
    }

    @Override
    public void connect() {
        // nothing
    }

    @Override
    public void disconnect( boolean tryReconnect ) {
        // nothing
    }

    @Override
    public boolean isRejectOnDisconnect() {
        return false;
    }

    @Override
    public void registerConnectionListener( ConnectionListener listener ) {
        // nothing
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isStopping() {
        return false;
    }

    @Override
    public void internalConnect() {
        // nothing
    }

    @Override
    public State getSessionState() {
        return State.Connected;
    }

    @Override
    public boolean isLoggedIn() {
        return true;
    }

    @Override
    public void processNextInbound() throws Exception {
        // nothing
    }

    @Override
    public void handleNow( Message msg ) {
        handle( msg );
    }

    @Override
    public void handleForSync( Message msg ) {
        handle( msg );
    }

    @Override
    public void setRejectOnDisconnect( boolean reject ) {
        // nothing
    }

    @Override
    public boolean getRejectOnDisconnect() {
        return false;
    }

    @Override
    public void processIncoming() {
        // nothing
    }

    @Override
    public void setLogStats( boolean logStats ) {
        // nothing
    }

    @Override
    public void setLogEvents( boolean on ) {
        // nothing
    }

    @Override
    public boolean isLogEvents() {
        return true;
    }

    @Override
    public boolean rejectMessageUpstream( Message msg, ZString errMsg ) {
        return false;
    }

    @Override
    public boolean discardOnDisconnect( Message msg ) {
        return false;
    }

    @Override
    public void setChainSession( Session sess ) {
        // nothing
    }

    @Override
    public Session getChainSession() {
        return null;
    }

    @Override
    public void inboundRecycle( Message msg ) {
        // nothing
    }

    @Override
    public void outboundRecycle( Message msg ) {
        // nothing
    }

    @Override
    public void dispatchInbound( Message msg ) {
        
        msg.setMessageHandler( this );
        
        _inboundRouter.handle( msg );
    }

    @Override
    public void waitForRecoveryToComplete() {
        // nothing
    }

    @Override
    public long getLastSent() {
        return 0;
    }

    @Override
    public String info() {
        return "JMXSession-" + _name;
    }

    @Override
    public void setPaused( boolean paused ) {
        // nothing
    }

    @Override
    public void persistLastInboundMesssage() {
        // nothing
    }

    @Override
    public SessionConfig getConfig() {
        return _config;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public SessionDirection getDirection() {
        return SessionDirection.Upstream;
    }

    @Override
    public void setThrottle( int throttleNoMsgs, int disconnectLimit, long throttleTimeIntervalMS ) {
        // nothing
    }

    @Override
    public Message recoverEvent( boolean isInbound, long persistKey, ReusableString tmpBigBuf, ByteBuffer tmpCtxBuf ) {
        return null;
    }

    /**
     * inject message, synchronized as could be multiple JMX sessions
     * @param rawMessage
     * @param sessionName 
     * @return status message
     */
    public synchronized String injectMessage( String rawMessage, String destSessionName ) {
        
        rawMessage = FixUtils.chkDelim( rawMessage );
        
        _log.info( getComponentId() + " INJECT IN : " + rawMessage );
        
        _inMsg.copy( rawMessage );
        
        _decoder.setReceived( Utils.nanoTime() );
        
        Message msg;
        try {
            msg = WarmupUtils.doDecode( _decoder, _inMsg.getBytes(), 0, _inMsg.length() );
        } catch( Exception e ) {
            return "Failed to decode message : " + e.getMessage();
        }

        if ( msg instanceof BaseReject ) {
            return "Error decoding message : " + ((BaseReject<?>)msg).getMessage(); 
        }
        
        if ( msg == null ) {
            return "No decodable message";
        }
        
        _logInbound.setLength( _logInHdr.length() );
        msg.dump( _logInbound );         
        _log.infoLarge( _logInbound );

        return inject( msg, destSessionName );
    }

    private String inject( Message msg, String sessionName ) {
        if ( sessionName == null ) {
            if ( _inboundRouter == null ) {
                return "ERROR : Must specify session to route too";
            }
            dispatchInbound( msg );
            return "Message Dispatched Inbound";
        }
        
        Session session = _sessMgr.getSession( sessionName );
        
        if ( session == null ) return "Unable to find session " + sessionName;
        
        try {
            session.handle( msg );

        } catch( Exception e ) {
            return "Exception " + e.getMessage();
        }

        return "Message Dispatched Inbound to " + sessionName;
    }
}
