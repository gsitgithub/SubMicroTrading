/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.gateway;

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
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.ETIConnectionGatewayResponse;
import com.rr.om.emea.exchange.eti.trading.ETISocketConfig;
import com.rr.om.session.state.SessionState;

public class ETIGatewayLoggedOutState implements SessionState {

    private static final Logger  _log = LoggerFactory.create( ETIGatewayLoggedOutState.class );

    private static final ZString   NOT_LOGGED_IN     = new ViewString( "Recieved unexpected message during logon process" );
    private static final ErrorCode LOGON_REJECTED    = new ErrorCode( "ETGO100", "Logon Rejected" );
    
    private final Session                _session;
    private final ETIGatewayController   _controller;
    private final ETISocketConfig        _config;

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logMsgBase;
    
    public ETIGatewayLoggedOutState( SeqNumSession session, ETIGatewayController sessionController, ETISocketConfig config ) {
        _session          = session;
        _controller       = sessionController;
        _logMsgBase       = new ReusableString( "[LoggedOut-" + _session.getComponentId() + "] " );
        _config           = config;
    }
    
    @Override
    public void handle( Message msg ) throws SessionStateException {
        
        if ( msg.getReusableType().getSubId() == EventIds.ID_ETICONNECTIONGATEWAYREQUEST ) {
            
            if ( _controller.isServer() ) {                     // exchange simulator only
                _controller.sendConnectionGatewayResponse();
                _log.info( "disconnecting exchange simulator gateway session" );
                Utils.delay( 1000 );                            // simulate exchange pause before disconnect
                _session.setPaused( true );
            }

        } else if ( msg.getReusableType().getSubId() == EventIds.ID_ETICONNECTIONGATEWAYRESPONSE ) {
            
            final ETIConnectionGatewayResponse rep = (ETIConnectionGatewayResponse) msg;
            
            if ( rep.getTradSesMode() != _config.getEnv() ) {
                _log.error( LOGON_REJECTED, " expected TradSessMode=" + _config.getEnv() + ", but it is " + rep.getTradSesMode() );
                _session.disconnect( false );
            } else if ( rep.getSessionMode() != _config.getETISessionMode() ) {
                _log.error( LOGON_REJECTED, " expected SessMode=" + _config.getETISessionMode() + ", but it is " + rep.getSessionMode() );
                _session.disconnect( false );
            } else if ( rep.getGatewayID() == 0 && rep.getSecGatewayID() == 0 ) {
                _log.error( LOGON_REJECTED, " gateway response missing gatewayID" );
                _session.disconnect( false );
            } else {
                _controller.initiateTradingSession( rep );
                _log.info( "Recieved connection gateway response, disconnecting gateway session" );
                _session.setPaused( true );
            }
            
        } else if ( msg.getReusableType().getSubId() == EventIds.ID_SESSIONREJECT ) {
        
            _logMsg.copy( " gateway session reject : " );

            msg.dump( _logMsg );

            _log.error( LOGON_REJECTED, _logMsg );

            _session.disconnect( false );

        } else {
            _logMsg.copy( _logMsgBase ).append( NOT_LOGGED_IN ).append( msg.getReusableType().toString() ).append( " : " );

            msg.dump( _logMsg );
            
            _session.inboundRecycle( msg );

            throw new SessionStateException( _logMsg.toString() );
        }
    }

    @Override
    public void connected() {
        _controller.resetSeqNums();
        if ( ! _controller.isServer() && ! _controller.isTradingSessionConnected() ) {
            // socket is connected and as this session is not the server need initiate logon
            _controller.sendConnectionGatewayRequest();
        }
    }
}
