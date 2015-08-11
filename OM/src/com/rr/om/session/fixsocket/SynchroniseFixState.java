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
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SessionStateException;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.ResendRequest;
import com.rr.model.generated.internal.events.interfaces.SequenceReset;
import com.rr.model.generated.internal.events.interfaces.TestRequest;
import com.rr.om.session.state.SessionState;

public class SynchroniseFixState implements SessionState {
    private static final Logger  _log = LoggerFactory.create( SynchroniseFixState.class );

    private static final ErrorCode ERR_NO_SEQ_NUM   = new ErrorCode( "SYS100", "Non decodable message, cant find seqNum, DONT assume its nextExpected" );
    
    private final Session        _session;
    private final FixController  _fixEngine;

    @SuppressWarnings( "unused" )
    private final ReusableString _logMsg     = new ReusableString(100);
    @SuppressWarnings( "unused" )
    private final ReusableString _logMsgBase;
    
    public SynchroniseFixState( Session session, FixController engine ) {
        _session   = session;
        _fixEngine = engine;
        _logMsgBase = new ReusableString( "[Sychronise-" + _session.getComponentId() + "] " );
    }
    
    @Override
    public void handle( Message msg ) throws SessionStateException {

        final int     recSeqNo = msg.getMsgSeqNum();
              boolean recycle  = true;

        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_HEARTBEAT:
            _fixEngine.setHeartbeatReceived();
            break;
        case EventIds.ID_TESTREQUEST:
            persistWithDupCheck( recSeqNo );
            
            ZString reqId = ((TestRequest)msg).getTestReqID();
            
            if ( reqId.length() == 0 ) {
                _log.warn( "No valid TestReqID on TestRequest message" );
            }
            
            _fixEngine.enqueueHeartbeat( reqId );
            break;
        case EventIds.ID_SESSIONREJECT:
        case EventIds.ID_LOGOUT:
        case EventIds.ID_LOGON:
            break;
        case EventIds.ID_RESENDREQUEST:
            final ResendRequest resReq = (ResendRequest)msg;
            if ( recSeqNo >= _fixEngine.getNextExpectedInSeqNo() ) {
                _fixEngine.clientResyncOutEvents( resReq.getBeginSeqNo(), resReq.getEndSeqNo(), false );
            }
            break;
        case EventIds.ID_SEQUENCERESET:
            SequenceReset seqReset = (SequenceReset) msg;
            /**
                One must be careful to ignore the duplicate Sequence Reset-GapFill mode
                which is attempting to lower the next expected sequence number. This can be detected by checking to see if its
                MsgSeqNum is less than expected. If so, the Sequence Reset-GapFill mode is a duplicate and should be
                discarded.
                The sequence
             */
            if ( recSeqNo >= _fixEngine.getNextExpectedInSeqNo() ) {
                _fixEngine.persistInMsgAndUpdateSeqNum( recSeqNo );
            }
            
            int nextMsg = seqReset.getNewSeqNo();
            
            if ( nextMsg > _fixEngine.getNextExpectedInSeqNo() ) {
                _fixEngine.growInSeqNumUpwards( nextMsg );
            }
            
            break;
        default:
            if ( msg.getReusableType() == CoreReusableType.RejectDecodeException ) {
                if ( msg.getMsgSeqNum() == 0 ) {
                    // unable to decode message avoid perm loop by assuming seqNum is next expected !
                    // CAN CAUSE SYNC ISSUES SO LOG ERROR
                    
                    RejectDecodeException e = (RejectDecodeException) msg;
                    
                    _log.error( ERR_NO_SEQ_NUM, e.getMessage() );
                }
            } else {
                    
                recycle = false;
                
                _session.dispatchInbound( msg );
            }
        }
        
        if ( _fixEngine.getInSeqNumForSycn() <= _fixEngine.getNextExpectedInSeqNo() ) {

            // ok now syncd move to logged in
            
            _fixEngine.setInSeqNumForSycn( 0 );
            _fixEngine.changeState( _fixEngine.getStateLoggedIn() );
        }

        if ( recycle ) _session.inboundRecycle( msg );
    }

    private void persistWithDupCheck( final int recSeqNo ) {
        if ( recSeqNo >= _fixEngine.getNextExpectedInSeqNo() ) {     
            _fixEngine.persistInMsgAndUpdateSeqNum( recSeqNo+1 );
        } else {
            _fixEngine.persistPosDupMsg( recSeqNo );                    
        }
    }

    @Override
    public void connected() {
        _log.warn( "Unexpected connected event when in synchronise mode" );
    }
}
