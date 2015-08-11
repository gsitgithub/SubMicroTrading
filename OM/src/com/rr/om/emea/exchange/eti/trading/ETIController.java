/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.rr.codec.emea.exchange.eti.ETIDecodeContext;
import com.rr.core.collections.IntHashMap;
import com.rr.core.collections.IntMap;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MsgFlag;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.model.generated.internal.events.impl.ETIRetransmitOrderEventsResponseImpl;
import com.rr.model.generated.internal.events.interfaces.ETIRetransmitOrderEvents;
import com.rr.model.generated.internal.events.interfaces.ETIRetransmitOrderEventsResponse;
import com.rr.model.generated.internal.type.ETIEurexDataStream;
import com.rr.om.session.state.SessionSeqNumController;
import com.rr.om.session.state.SessionStateFactory;
import com.rr.om.session.state.StatefulSessionFactory;

/**
 * based on native trading gateway issue 7.0 Dec 2010
 */

public class ETIController extends SessionSeqNumController {
    
    private   static final Logger _log = LoggerFactory.create( ETIController.class );

    protected static final int   MAX_SEQ_NUM      = 256;
    protected        final int[] _lastAppIdSeqNum = new int[ MAX_SEQ_NUM ];
    
    private int _sessionInstanceId = 0;

    private IntMap<ApplMsgID> _partitionLastApplMsgId = new IntHashMap<ApplMsgID>( 256, 0.75f );

    // recovery fields
    private AtomicInteger   _pendingResponses = new AtomicInteger( 0 );
    private List<Integer>   _recoveryKeyList;   
    private int             _nextRecoveryKeyIndex;

    private short _curPartitionRecovery;

    
    public ETIController( SeqNumSession session, ETISocketConfig config ) {
        super( session, new ETIStateFactory( config ), new ETISessionFactory( config ) ); 
    }

    protected ETIController( SeqNumSession session, SessionStateFactory stateFactory, StatefulSessionFactory msgFactory ) {
        super( session, stateFactory, msgFactory ); // only invoked from the recovery controller
    }

    @Override
    public final void reset() {
        super.reset();

        _recoveryKeyList = null;
        _nextRecoveryKeyIndex = 0;
        _partitionLastApplMsgId.clear();
    }

    public final void recoverContext( Message msg, boolean inBound, ETIDecodeContext ctx ) {

        if ( inBound ) {
            storeMaxSeqNum( msg, ctx );
        }
    }

    public void storeMaxSeqNum( Message msg, ETIDecodeContext ctx ) {
        if ( ctx.hasValue() ) {
            ApplMsgID curMax = getLastApplMsgID( ctx.getLastPartitionID() );

            curMax.setIfGreater( ctx.getLastApplMsgID() );
        }
    }

    @Override
    public final void recoverContext( Message msg, boolean inBound ) {
        // not used, the context appId requires special override
    }

    @Override
    public final void outboundError() {
        // nothing
    }

    public final void sendSessionLogonNow() {
        Message logon = _sessionFactory.getSessionLogOn( 0, 0, 0 );
        send( logon, true );
    }

    @Override
    public void stop() {
        super.stop();
    }
    
    // checkMaxSeqNums is used to integrate the trading session max seq nums 
    // with the recovery session seqNums ... really belongs in the recoveryController
    protected final void checkMaxSeqNums( int[] lastAppIdSeqNum ) {
        for( int i=0 ; i < MAX_SEQ_NUM ; i++ ) {
            if ( lastAppIdSeqNum[i] > _lastAppIdSeqNum[i] ) {
                _lastAppIdSeqNum[i] = lastAppIdSeqNum[i];
            }
        }
    }

    @Override
    protected final void onLogout() {
        super.onLogout();
        
        // reset the sync nums 
        
        resetSeqNums();
    }

    public void resetSeqNums() {
        setInSeqNumForSycn( 0 );
        setNextExpectedInSeqNum( 1 );
        setNextOutSeqNum( 1 );
    }

    public void setSessionInstanceID( int sessionInstanceID ) {
        _sessionInstanceId = sessionInstanceID;
        
        _log.info( "Setting session " + _session.getComponentId() + " to sessionInstanceId " + _sessionInstanceId ); 
    }

    public void setThrottle( int throttleNoMsgs, long throttleTimeIntervalMS ) {
        _session.setThrottle( throttleNoMsgs, throttleNoMsgs, throttleTimeIntervalMS );
    }

    public void sendUserLogonRequest() {
        Message logon = ((ETISessionFactory)_sessionFactory).getUserLogOn();
        send( logon, true );
    }

    public ApplMsgID getLastApplMsgID( int partitionID ) {
        ApplMsgID id = _partitionLastApplMsgId.get( partitionID );
        
        if ( id == null ) {
            id = new ApplMsgID();
            _partitionLastApplMsgId.put( partitionID, id );
        }
        
        return id;
    }

    public void sendLogonSyncMsgs() {
        
        _recoveryKeyList = new LinkedList<Integer>( _partitionLastApplMsgId.keys() );
        
        _pendingResponses.set( 0 );
        
        _nextRecoveryKeyIndex = 0;
        
        sendNextRecoveryRequest();
    }
    
    private void sendNextRecoveryRequest() {
        
        if ( _nextRecoveryKeyIndex < _recoveryKeyList.size() ) {
            short partitionId = (short) _recoveryKeyList.get( _nextRecoveryKeyIndex ).intValue();
            
            _curPartitionRecovery = partitionId;
            
            requestGapFill( ETIEurexDataStream.SessionData, partitionId, _partitionLastApplMsgId.get( partitionId ) );
            
        } else {
            _log.info( "All partitionIds are fully processed, partitionCnt=" + _nextRecoveryKeyIndex + ", lastPartitionId=" + _curPartitionRecovery );
            changeState( getStateLoggedIn() ); 
        }
    }

    private void requestGapFill( ETIEurexDataStream stream, short partitionId, ApplMsgID applMsgID ) {
        ETIRetransmitOrderEvents req = ((ETISessionFactory)_sessionFactory).createRetransmitOrderEventsRequest( stream, partitionId, applMsgID );

        _log.info( "Request recoveryOrderEvents partitionId=" + partitionId + ", lastApplMsgId=" + applMsgID.toString() );

        send( req, false );
    }

    void acceptMessage( Message msg ) {
        final int newSeqNum          = msg.getMsgSeqNum();
        final int nextExpectedSeqNum = getNextExpectedInSeqNo();
        
        if ( newSeqNum == 0 ) return;
        
        if ( newSeqNum == nextExpectedSeqNum ) {
            persistInMsgAndUpdateSeqNum( newSeqNum + 1 );
        } else if ( newSeqNum < nextExpectedSeqNum ) {
            
            if ( msg.isFlagSet( MsgFlag.PossDupFlag ) == false )
                _log.info( "Accepting out of lower sequence number mismatch. Expecting=" + nextExpectedSeqNum + ", Received=" + newSeqNum );
            
            // pos dup dont DECREASE the seqnum

            persistPosDupMsg( newSeqNum );
            
        } else { // missing messages
            _log.info( "Accepting out of higher sequence number mismatch. Expecting=" + nextExpectedSeqNum + ", Received=" + newSeqNum );

            // dont disconnect, ETI has lots of messages and this could simply be due to a message not in the model ... for now just warn
            persistInMsgAndUpdateSeqNum( newSeqNum + 1 );
        }
    }

    public void checkRetransmitOrderEventsResponse( ETIRetransmitOrderEventsResponse msg ) {

        int pendingReplies = msg.getApplTotalMessageCount();
        
        if ( pendingReplies <= 0 ) {
            _log.info( "Partition has no further recovery messages, partitionId=" + _curPartitionRecovery + ", check next partitionId" );
            ++_nextRecoveryKeyIndex;
            sendNextRecoveryRequest();
        } else {
            _log.info( "Partition partitionId=" + _curPartitionRecovery + " has " + pendingReplies + " recovery messages" );
            _pendingResponses.set( pendingReplies );
        }
    }

    /**
     * next recovery event from ETI has been processed
     * 
     * note multiple events with same applMsgId dont count towards the expected message count
     * 
     * @param sameId
     */
    public void processedRecoveredOrderEventMsg( boolean sameId ) {
        if ( sameId ) {
            _log.info( "Current ETI event has same id as previous event" );
        } else if ( _pendingResponses.decrementAndGet() <= 0 ) {
            _log.info( "Processed all recovery replies for batch with partitionId=" + _curPartitionRecovery + ", check for more" );
            sendNextRecoveryRequest();
        }
    }

    /**
     * Server Emulation Methods
     */
    final void sendSessionLogonReplyNow( ZString msg, int rejectCode ) {
        Message logon = ((ETISessionFactory)_sessionFactory).getSessionLogonReply();
        send( logon, true );
    }

    void sendUserLogonReplyNow() {
        Message msg = ((ETISessionFactory)_sessionFactory).getUserLogOnReply();
        send( msg, true );
    }

    void synthRetransmitResponse( ETIRetransmitOrderEvents msg ) {
        ETIRetransmitOrderEventsResponseImpl response = new ETIRetransmitOrderEventsResponseImpl();
        response.setApplTotalMessageCount( (short) 0 );
        send( response, false );
    }
}