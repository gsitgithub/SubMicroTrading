/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.ets;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.session.socket.SessionStateException;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.UTPLogon;
import com.rr.om.session.state.SessionState;

public class ETSLoggedOutState implements SessionState {

    private static final ZString NOT_LOGGED_IN                  = new ViewString( "Recieved unexpected message during logon process" );
    
    private final Session             _session;
    private final ETSController       _controller;
    private final ETSSocketConfig     _config;

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logMsgBase;
    
    public ETSLoggedOutState( SeqNumSession session, ETSController sessionController, ETSSocketConfig config ) {
        _session          = session;
        _controller        = sessionController;
        _logMsgBase       = new ReusableString( "[LoggedOut-" + _session.getComponentId() + "] " );
        _config           = config;
    }
    
    @Override
    public void handle( Message msg ) throws SessionStateException {
        if ( msg.getReusableType().getSubId() == EventIds.ID_UTPLOGON ) {
            
            final UTPLogon req                = (UTPLogon) msg;
                  int      lastSeenSentSeqNum = req.getMsgSeqNum();
            
            if ( lastSeenSentSeqNum > 0 ) --lastSeenSentSeqNum;
            if ( _controller.isServer() ) sendLogon();

            // check to see if other side missing msgs from us, if so need to send them
            if ( lastSeenSentSeqNum < lastSeenSentSeqNum ) {
                _controller.sendMissingMsgsToClientNow( lastSeenSentSeqNum+1 );
            }
            
            // DONT START HB TIMER BEFORE RESYNC FINISHED
            _controller.startHeartbeatTimer( _config.getHeartBeatIntSecs() );
            _controller.changeState( _controller.getStateLoggedIn() );
            
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
            sendLogon();
        }
    }

    private void sendLogon() {
        int lastReceivedMsg = _controller.getNextExpectedInSeqNo();
        if ( lastReceivedMsg > 0 ) --lastReceivedMsg;
        _controller.sendLogonNow( _config.getHeartBeatIntSecs(), lastReceivedMsg );
    }
}
