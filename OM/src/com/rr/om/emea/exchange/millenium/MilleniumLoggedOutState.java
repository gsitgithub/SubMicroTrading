/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium;

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
import com.rr.model.generated.internal.events.interfaces.MilleniumLogonReply;
import com.rr.model.generated.internal.events.interfaces.MilleniumLogout;
import com.rr.om.session.state.SessionState;

public class MilleniumLoggedOutState implements SessionState {

    private static final Logger  _log = LoggerFactory.create( MilleniumLoggedOutState.class );

    private static final ZString   NOT_LOGGED_IN     = new ViewString( "Recieved unexpected message during logon process" );
    private static final ErrorCode LOGON_REJECTED    = new ErrorCode( "MLO100", "Logon Rejected" );
    private static final ErrorCode LOGOUT            = new ErrorCode( "MLO200", "" );
    
    private final Session                   _session;
    private final MilleniumController       _controller;
    private final MilleniumSocketConfig     _config;

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logMsgBase;
    
    public MilleniumLoggedOutState( SeqNumSession session, MilleniumController sessionController, MilleniumSocketConfig config ) {
        _session          = session;
        _controller        = sessionController;
        _logMsgBase       = new ReusableString( "[LoggedOut-" + _session.getComponentId() + "] " );
        _config           = config;
    }
    
    @Override
    public void handle( Message msg ) throws SessionStateException {
        
        if ( msg.getReusableType().getSubId() == EventIds.ID_MILLENIUMLOGON ) {
            
            if ( _controller.isServer() ) {
                _controller.sendLogonReplyNow( null, 0 );
                _controller.startHeartbeatTimer( _config.getHeartBeatIntSecs() );
                _controller.changeState( _controller.getStateLoggedIn() );
            }

        } else if ( msg.getReusableType().getSubId() == EventIds.ID_MILLENIUMLOGONREPLY ) {
            
            final MilleniumLogonReply rep                = (MilleniumLogonReply) msg;
            
            if ( rep.getRejectCode() != 0 ) {
                _log.error( LOGON_REJECTED, "rejectCode=" + rep.getRejectCode() + ", expiryDayCount=" + rep.getPwdExpiryDayCount() );
                _session.disconnect( false );
            } else {
                _controller.initiateRecovery();
                _controller.startHeartbeatTimer( _config.getHeartBeatIntSecs() );
                
                // note allow trading even if not fully recovered
                
                _controller.changeState( _controller.getStateLoggedIn() );
            }
            
        } else if ( msg.getReusableType().getSubId() == EventIds.ID_MILLENIUMLOGOUT ) {
            final MilleniumLogout rep                = (MilleniumLogout) msg;
            _log.error( LOGOUT, "reason=" + rep.getReason() );
            _session.disconnect( false );
            
        } else {
            _logMsg.copy( _logMsgBase ).append( NOT_LOGGED_IN ).append( msg.getReusableType().toString() );
            
            _session.inboundRecycle( msg );

            throw new SessionStateException( _logMsg.toString() );
        }
    }

    @Override
    public void connected() {
        if ( ! _controller.isServer() ) {
            // socket is connected and as this session is not the server need initiate logon
            _controller.sendLogonNow();
        }
    }
}
