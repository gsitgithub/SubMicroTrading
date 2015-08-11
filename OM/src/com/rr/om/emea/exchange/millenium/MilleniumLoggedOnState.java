/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium;

import com.rr.core.codec.RejectDecodeException;
import com.rr.core.lang.CoreReusableType;
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
import com.rr.model.generated.internal.events.interfaces.TestRequest;
import com.rr.om.session.state.SessionState;

public class MilleniumLoggedOnState implements SessionState {

    private static final Logger  _log = LoggerFactory.create( MilleniumLoggedOnState.class );

    private static final ErrorCode ERR_NO_SEQ_NUM           = new ErrorCode( "ULS100", "Non decodable message, cant find seqNum, assume its nextExpected" );
    
    private static final ZString SESSION_REJECT_RECEIVED    = new ViewString( "Session-level reject message received" );

    private final Session              _session;
    private final MilleniumController  _controller;

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logMsgBase;
    
    public MilleniumLoggedOnState( SeqNumSession session, MilleniumController sessionController ) {
        _session   = session;
        _controller = sessionController;
        _logMsgBase = new ReusableString( "[MillLoggedIn-" + _session.getComponentId() + "] " );
    }
    
    @Override
    public final void handle( Message msg ) throws SessionStateException { 
        
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERSINGLE:
        case EventIds.ID_NEWORDERACK:
        case EventIds.ID_TRADENEW:
        case EventIds.ID_CANCELREPLACEREQUEST:
        case EventIds.ID_CANCELREQUEST:
        case EventIds.ID_CANCELREJECT:
        case EventIds.ID_REJECTED:
        case EventIds.ID_CANCELLED:
        case EventIds.ID_REPLACED:
        case EventIds.ID_DONEFORDAY:
        case EventIds.ID_STOPPED:
        case EventIds.ID_EXPIRED:
        case EventIds.ID_SUSPENDED:
        case EventIds.ID_RESTATED:
        case EventIds.ID_TRADECORRECT:
        case EventIds.ID_TRADECANCEL:
        case EventIds.ID_ORDERSTATUS:
            acceptMessage( msg );
            _session.dispatchInbound( msg );
            return;                          // dont recycle
        case EventIds.ID_HEARTBEAT:
            _controller.setHeartbeatReceived();
            break;
        case EventIds.ID_TESTREQUEST:
            ZString reqId = ((TestRequest)msg).getTestReqID();
            
            if ( reqId.length() == 0 ) {
                _log.warn( "No valid TestReqID on TestRequest message" );
            }
            
            _controller.enqueueHeartbeat( reqId );
            break;
        case EventIds.ID_SESSIONREJECT:
            logWarn( SESSION_REJECT_RECEIVED );
            break;
        case EventIds.ID_SEQUENCERESET:
        case EventIds.ID_RESENDREQUEST:
        case EventIds.ID_LOGON:
        case EventIds.ID_LOGOUT:
            throw new SessionStateException( "Unsupported event type " + msg.getClass().getSimpleName() );
        default:
            switch( msg.getReusableType().getSubId() ) {
            case EventIds.ID_MILLENIUMLOGONREPLY:
                throw new SessionStateException( "Already Logged in" );
            case EventIds.ID_MILLENIUMLOGOUT:
                _log.warn( "Logout requested" );
                _session.disconnect( false );
                break;
            case EventIds.ID_MILLENIUMMISSEDMESSAGEREQUEST:
            case EventIds.ID_MILLENIUMMISSEDMSGREQUESTACK:
            case EventIds.ID_MILLENIUMMISSEDMSGREPORT:
                throw new SessionStateException( "Recovery messages not allowed on trading session" );
            case EventIds.ID_MILLENIUMLOGON:
                throw new SessionStateException( "Already Logged in" );
            default:
                if ( msg.getReusableType() == CoreReusableType.RejectDecodeException ) {
                    if ( msg.getMsgSeqNum() == 0 ) {
                        // unable to decode message avoid perm loop by assuming seqNum is next expected !
                        // CAN CAUSE SYNC ISSUES SO LOG ERROR
                        
                        RejectDecodeException e = (RejectDecodeException) msg;
                        
                        _log.error( ERR_NO_SEQ_NUM, e.getMessage() );
                    }
                }
                acceptMessage( msg );
                _session.dispatchInbound( msg );
                return;
            }
        }
        _session.inboundRecycle( msg );
    }

    private void acceptMessage( Message msg ) {
        _controller.persistPosDupMsg( msg.getMsgSeqNum() );
    }

    private void logWarn( ZString msg ) {
        _logMsg.copy( _logMsgBase ).append( msg );
        _log.warn( msg );
    }

    @Override
    public void connected() {
        _log.warn( "Unexpected connected event when already loggedIn" );
    }
}
