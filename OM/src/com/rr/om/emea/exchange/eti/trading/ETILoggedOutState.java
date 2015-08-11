/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.session.socket.SessionStateException;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.impl.ETISessionLogonResponseImpl;
import com.rr.model.generated.internal.events.interfaces.ETISessionLogoutNotification;
import com.rr.model.generated.internal.events.interfaces.ETISessionLogoutRequest;
import com.rr.model.generated.internal.events.interfaces.ETISessionLogoutResponse;
import com.rr.model.generated.internal.events.interfaces.SessionReject;
import com.rr.om.session.state.SessionState;

public class ETILoggedOutState implements SessionState {

    private static final Logger  _log = LoggerFactory.create( ETILoggedOutState.class );

    private static final ZString   NOT_LOGGED_IN     = new ViewString( "Recieved unexpected message during logon process" );
    private static final ErrorCode LOGOUT            = new ErrorCode( "ETI200", "Logout" );
    
    private final Session                   _session;
    private final ETIController       _controller;
    private final ETISocketConfig     _config;

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logMsgBase;
    
    public ETILoggedOutState( SeqNumSession session, ETIController sessionController, ETISocketConfig config ) {
        _session          = session;
        _controller        = sessionController;
        _logMsgBase       = new ReusableString( "[LoggedOut-" + _session.getComponentId() + "] " );
        _config           = config;
    }
    
    @Override
    public void handle( Message msg ) throws SessionStateException {
        
        if ( msg.getReusableType().getSubId() == EventIds.ID_ETISESSIONLOGONREQUEST ) {
            _controller.acceptMessage( msg );
            if ( _controller.isServer() ) {
                _controller.sendSessionLogonReplyNow( null, 0 );
                _controller.startHeartbeatTimer( _config.getHeartBeatIntSecs() );
            }

        } else if ( msg.getReusableType().getSubId() == EventIds.ID_ETISESSIONLOGONRESPONSE ) {
            
            final ETISessionLogonResponseImpl rep = (ETISessionLogonResponseImpl) msg;

            _controller.acceptMessage( msg );
            _controller.setThrottle( rep.getThrottleDisconnectLimit(), rep.getThrottleTimeIntervalMS() );
            _controller.setSessionInstanceID( rep.getSessionInstanceID() );
            
            _controller.startHeartbeatTimer( _config.getHeartBeatIntSecs() );

            // @TODO add recovery state 
            
            _controller.sendUserLogonRequest();
            
        } else if ( msg.getReusableType().getSubId() == EventIds.ID_ETIUSERLOGONREQUEST ) {

            _controller.acceptMessage( msg );
            if ( _controller.isServer() ) {
                _controller.sendUserLogonReplyNow();
                _controller.changeState( _controller.getStateLoggedIn() );
            }

        } else if ( msg.getReusableType().getSubId() == EventIds.ID_ETIUSERLOGONRESPONSE ) {
            
            // @TODO add recovery state 
            
            _controller.acceptMessage( msg );
            
            // could push recovery into discrete sync state, but at the moement the exact deliniation of end of sequence is not clear
            // @TODO in furture clarify and push into discrete state
            
            _controller.changeState( _controller.getStateSynchronise() ); 
            
            _controller.sendLogonSyncMsgs();
            
        } else if ( msg.getReusableType().getSubId() == EventIds.ID_SESSIONREJECT ) {
            _controller.acceptMessage( msg );
            final SessionReject rep = (SessionReject) msg;
            ReusableString dump = new ReusableString();
            rep.dump( dump );
            _log.error( LOGOUT, "SessionLogon Rejected =" + dump );
            _session.disconnect( false );
            
        } else if ( msg.getReusableType().getSubId() == EventIds.ID_ETISESSIONLOGOUTREQUEST ) {
            _controller.acceptMessage( msg );
            final ETISessionLogoutRequest rep = (ETISessionLogoutRequest) msg;
            ReusableString dump = new ReusableString();
            rep.dump( dump );
            _log.error( LOGOUT, " request, msg=" + dump );
            _session.disconnect( false );
            
        } else if ( msg.getReusableType().getSubId() == EventIds.ID_ETISESSIONLOGOUTRESPONSE ) {
            _controller.acceptMessage( msg );
            final ETISessionLogoutResponse rep = (ETISessionLogoutResponse) msg;
            ReusableString dump = new ReusableString();
            rep.dump( dump );
            _log.error( LOGOUT, " response, msg=" + dump );
            _session.disconnect( false );
            
        } else if ( msg.getReusableType().getSubId() == EventIds.ID_ETISESSIONLOGOUTNOTIFICATION ) {
            _controller.acceptMessage( msg );
            final ETISessionLogoutNotification rep                = (ETISessionLogoutNotification) msg;
            ReusableString dump = new ReusableString();
            rep.dump( dump );
            _log.error( LOGOUT, " notification, msg=" + dump );
            _session.disconnect( false );
            
        } else {
            _logMsg.copy( _logMsgBase ).append( NOT_LOGGED_IN ).append( msg.getReusableType().toString() );
            
            _session.inboundRecycle( msg );

            throw new SessionStateException( _logMsg.toString() );
        }
    }

    @Override
    public void connected() {
        _controller.resetSeqNums();
        if ( ! _controller.isServer() ) {
            // socket is connected and as this session is not the server need initiate logon
            _controller.sendSessionLogonNow();
        }
    }
}
