/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import com.rr.codec.emea.exchange.eti.ETIDecodeContext;
import com.rr.core.codec.RejectDecodeException;
import com.rr.core.collections.IntToLongHashMap;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.socket.SessionStateException;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.factory.RecoveryRejectedFactory;
import com.rr.model.generated.internal.events.impl.RecoveryRejectedImpl;
import com.rr.model.generated.internal.events.interfaces.ETIRetransmitOrderEvents;
import com.rr.model.generated.internal.events.interfaces.SessionReject;
import com.rr.model.generated.internal.events.interfaces.TestRequest;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.SessionRejectReason;
import com.rr.model.internal.type.ExecType;
import com.rr.om.session.state.SessionState;

public class ETILoggedOnState implements SessionState {

    private static final Logger  _log = LoggerFactory.create( ETILoggedOnState.class );

    private static final ErrorCode ERR_NO_SEQ_NUM           = new ErrorCode( "ETI100", "Non decodable message, cant find seqNum, assume its nextExpected" );
    
    private final ETISession     _session;
    private final ETIController  _controller;

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logMsgBase;

    private final ApplMsgID      _tmpApplMsgId = new ApplMsgID();
    
    private final SuperPool<RecoveryRejectedImpl> _rejectedPool = SuperpoolManager.instance().getSuperPool( RecoveryRejectedImpl.class );
    private final RecoveryRejectedFactory _rejectedFactory = new RecoveryRejectedFactory( _rejectedPool );
    
    public ETILoggedOnState( ETISession session, ETIController sessionController ) {
        _session   = session;
        _controller = sessionController;
        _logMsgBase = new ReusableString( "[ETILoggedIn-" + _session.getComponentId() + "] " );
    }
    
    @Override
    public final void handle( Message msg ) throws SessionStateException { 
        
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERACK:
        case EventIds.ID_TRADENEW:
        case EventIds.ID_CANCELREJECT:
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
        case EventIds.ID_REJECTED:
            handleExec( msg );
            return;                          // dont recycle
        case EventIds.ID_SESSIONREJECT:
            handleReject( msg );
            return;                          // dont recycle
        case EventIds.ID_NEWORDERSINGLE:
        case EventIds.ID_CANCELREPLACEREQUEST:
        case EventIds.ID_CANCELREQUEST:
            _controller.acceptMessage( msg );
            _session.dispatchInbound( msg );
            return;                          // dont recycle
        case EventIds.ID_HEARTBEAT:
            _controller.setHeartbeatReceived();
            break;
        case EventIds.ID_TESTREQUEST:
            ZString reqId = ((TestRequest)msg).getTestReqID();
            
            if ( reqId.length() == 0 ) {
                logWarn( "No valid TestReqID on TestRequest message" );
            }
            
            _controller.enqueueHeartbeat( reqId );
            break;
        case EventIds.ID_SEQUENCERESET:
        case EventIds.ID_RESENDREQUEST:
        case EventIds.ID_LOGON:
        case EventIds.ID_LOGOUT:
            throw new SessionStateException( "Unsupported event type " + msg.getClass().getSimpleName() );
        default: 
            // push outlying case values to sub lookupswitch, allows main switch to be tableswitch 
            
            switch( msg.getReusableType().getSubId() ) {
            case EventIds.ID_ETISESSIONLOGONRESPONSE:
                throw new SessionStateException( "Already Logged in" );
            case EventIds.ID_ETISESSIONLOGOUTRESPONSE:
            case EventIds.ID_ETISESSIONLOGOUTNOTIFICATION:
                logWarn( "Logout requested" );
                _session.disconnect( false );
                break;
            case EventIds.ID_ETIRETRANSMITORDEREVENTS:
                if ( _controller.isServer() ) {
                    _controller.synthRetransmitResponse( (ETIRetransmitOrderEvents) msg );
                }
                break;
            case EventIds.ID_ETIUSERLOGONREQUEST:
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
                handleExec( msg );
                return;
            }
        }
        _session.inboundRecycle( msg );
    }

    private void handleReject( Message msg ) {
        SessionReject reject = (SessionReject) msg;
        final ETIDecodeContext ctx      = _session.getDecodeContext();

        long mktClOrdId;
        
        IntToLongHashMap map = ctx.getMapSeqNumClOrdId();
        
        int refSeqNum = reject.getRefSeqNum();
        
        synchronized( map ) {
            mktClOrdId = map.get( refSeqNum );
        }
        
        if ( mktClOrdId > 0 ) { // MORPH REJECT
            
            RecoveryRejectedImpl newReject = _rejectedFactory.get();

            newReject.getClOrdIdForUpdate().append( mktClOrdId );
            newReject.getTextForUpdate().copy( reject.getText() );
            if ( reject.getSessionRejectReason() != SessionRejectReason.Other ) {
                newReject.getTextForUpdate().append( " : " ).append( reject.getSessionRejectReason() );
            }
            newReject.setExecType( ExecType.Rejected );
            newReject.setOrdStatus( OrdStatus.Rejected );
            newReject.setMsgSeqNum( reject.getRefSeqNum() );
            newReject.getExecIdForUpdate().append( "EX" ).append( mktClOrdId );
            
            _logMsg.copy( _logMsgBase ).append( "Morphing session reject into reject for seqNum " ).append( refSeqNum )
                   .append( ", clOrdId=" ).append( mktClOrdId ).append( " : " );
            
            newReject.dump( _logMsg );
            
            _log.info( _logMsg );
     
            _session.inboundRecycle( msg );
            
            msg = newReject;
        }

        handleExec( msg );
    }

    private void handleExec( Message msg ) {
        final ETIDecodeContext ctx      = _session.getDecodeContext();
        final int       partitionID     = ctx.getLastPartitionID();
        final ZString   newApplMsgIdNum = ctx.getLastApplMsgID();
        final ApplMsgID lastAppMsgId    = _controller.getLastApplMsgID( partitionID );
        
        _tmpApplMsgId.set( newApplMsgIdNum );

        lastAppMsgId.set( _tmpApplMsgId );
        
        _controller.acceptMessage( msg );
        _session.dispatchInbound( msg );
    }

    private void logWarn( String msg ) {
        _logMsg.copy( _logMsgBase ).append( msg );
        _log.warn( msg );
    }

    @Override
    public void connected() {
        _log.warn( "Unexpected connected event when already loggedIn" );
    }
}
