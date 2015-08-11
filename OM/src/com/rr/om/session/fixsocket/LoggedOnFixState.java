/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.fixsocket;

import com.rr.core.codec.RejectDecodeException;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MsgFlag;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SessionStateException;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.ResendRequest;
import com.rr.model.generated.internal.events.interfaces.SequenceReset;
import com.rr.model.generated.internal.events.interfaces.TestRequest;
import com.rr.om.session.state.SessionState;

public final class LoggedOnFixState implements SessionState {

    private static final Logger  _log = LoggerFactory.create( LoggedOnFixState.class );

    private static final ErrorCode ERR_NO_SEQ_NUM              = new ErrorCode( "LOS100", "Non decodable message, cant find seqNum, assume its nextExpected" );
    private static final ErrorCode ERR_SESSION_REJECT_RECEIVED = new ErrorCode( "LOS200", "Session-level reject message received" );
    
    private static final ZString IGNORE_SEQ_RESET           = new ViewString( "Ignore SequenceReset, curInSeqNum=" );
    private static final ZString NEW_IN_SEQ_NUM             = new ViewString( ", recvInSeqNum=");
    private static final ZString NON_GAP_FILL               = new ViewString( "SequenceReset in non gap fill mode received" );

    private final Session        _session;
    private final FixController _fixEngine;
    private final FixConfig     _config;
    

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logTmpMsg  = new ReusableString(100);
    private final ReusableString _logMsgBase;

    public LoggedOnFixState( Session session, FixController engine, FixConfig config ) {
        _session   = session;
        _fixEngine = engine;
        _config    = config;
        _logMsgBase = new ReusableString( "[LoggedIn-" + _session.getComponentId() + "] " );
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
            _fixEngine.setHeartbeatReceived();
            acceptMessage( msg );
            break;
        case EventIds.ID_TESTREQUEST:
            acceptMessage( msg );
            ZString reqId = ((TestRequest)msg).getTestReqID();
            
            if ( reqId.length() == 0 ) {
                _log.warn( "No valid TestReqID on TestRequest message" );
            }
            
            _fixEngine.enqueueHeartbeat( reqId );
            break;
        case EventIds.ID_RESENDREQUEST:
            acceptMessage( msg );
            final ResendRequest resReq = (ResendRequest)msg;
            _fixEngine.clientResyncOutEvents( resReq.getBeginSeqNo(), resReq.getEndSeqNo(), false );
            break;
        case EventIds.ID_SESSIONREJECT:
            _logMsg.copy( _logMsgBase );
            msg.dump( _logMsg );
            _log.error( ERR_SESSION_REJECT_RECEIVED, _logMsg );
            acceptMessage( msg );
            break;
        case EventIds.ID_SEQUENCERESET:
            handle( (SequenceReset) msg );
            break;
        case EventIds.ID_LOGOUT:
            acceptMessage( msg );
            _session.disconnect( false );
            break;
        case EventIds.ID_LOGON:
            throw new SessionStateException( "Already Logged in" );
        default:
            if ( msg.getReusableType() == CoreReusableType.RejectDecodeException ) {
                if ( msg.getMsgSeqNum() == 0 ) {
                    // unable to decode message avoid perm loop by assuming seqNum is next expected !
                    // CAN CAUSE SYNC ISSUES SO LOG ERROR
                    
                    RejectDecodeException e = (RejectDecodeException) msg;
                    
                    _log.error( ERR_NO_SEQ_NUM, e.getMessage() );
                    
                    msg.setMsgSeqNum( _fixEngine.getNextExpectedInSeqNo() );
                }
            }
            acceptMessage( msg );
            _session.dispatchInbound( msg );
            return;
        }
        _session.inboundRecycle( msg );
    }
    
    private void handle( SequenceReset msg ) throws SessionStateException {
        
        if ( msg.getGapFillFlag() ) {
            acceptMessage( msg );

        } else { // reset request (ie non gap fill) 
            
            logWarn( NON_GAP_FILL );
        }

        int newExpectedNextInSeqNum = msg.getNewSeqNo();
        int expNextInSeqNum         = _fixEngine.getNextExpectedInSeqNo();

        if ( newExpectedNextInSeqNum > expNextInSeqNum ) {
            _fixEngine.growInSeqNumUpwards( newExpectedNextInSeqNum );
        } else { 
            _logTmpMsg.copy( IGNORE_SEQ_RESET ).append( expNextInSeqNum ).append( NEW_IN_SEQ_NUM ).append( newExpectedNextInSeqNum );
            
            logWarn( _logTmpMsg );
        }
    }

    private void acceptMessage( Message msg ) throws SessionStateException {
        final int newSeqNum          = msg.getMsgSeqNum();
        final int nextExpectedSeqNum = _fixEngine.getNextExpectedInSeqNo();
        
        if ( newSeqNum == nextExpectedSeqNum ) {
            _fixEngine.persistInMsgAndUpdateSeqNum( newSeqNum + 1 );
        } else if ( newSeqNum < nextExpectedSeqNum ) {
            
            if ( msg.isFlagSet( MsgFlag.PossDupFlag ) == false )
                throw new SessionStateException( "Sequence number mismatch. Expecting=" + nextExpectedSeqNum + ", Received=" + newSeqNum );
            
            // pos dup dont DECREASE the seqnum

            _fixEngine.persistPosDupMsg( newSeqNum );
            
        } else { // missing messages
            if ( _config.isDisconnectOnSeqGap() ) {
                throw new SessionStateException( "Message Gap: Sequence number mismatch. Expecting=" + nextExpectedSeqNum + ", Received=" + newSeqNum );
            } 
            
            _log.warn( "Message Gap: Sequence number mismatch. Expecting=" + nextExpectedSeqNum + ", Received=" + newSeqNum + ", send gap fill" );

            _fixEngine.persistInMsgAndUpdateSeqNum( newSeqNum + 1 );
            
            _fixEngine.sendResendRequest( nextExpectedSeqNum, newSeqNum-1, false );
        }
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
