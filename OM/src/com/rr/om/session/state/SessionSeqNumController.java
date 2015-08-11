/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.state;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MsgFlag;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.model.generated.internal.events.impl.LogoutImpl;
import com.rr.model.generated.internal.events.interfaces.LogonWrite;
import com.rr.om.Strings;

public class SessionSeqNumController extends SessionController<StatefulSessionFactory> {
    private static final Logger _log = LoggerFactory.create( SessionSeqNumController.class );

    private static final ErrorCode FAILED_GAP_FILL         = new ErrorCode( "SSN100", "Failed to persist gap fill request" );
    private static final ErrorCode FAILED_GROW_OUT         = new ErrorCode( "SSN200", "Failed to grow out seq num" );
    private static final ErrorCode FAILED_TRUNCATE_INDEX   = new ErrorCode( "SSN300", "Failed to blank out index entries" );
    
    private static final ZString BEGIN_SEQ_BELOW_ZERO      = new ViewString( "BeginSeqNo will use zero, ignore bad value of " );
    private static final ZString GROW_OUT_SEQ_NUM          = new ViewString( "EndSeqNo higher than expectedOutSeq, grow outSequm to " );
    private static final ZString SWAPPED_BEGIN_END_SEQ_NUM = new ViewString( "Swapped crossed begin and end seq nums, begin=" );
    private static final ZString STARTING_CLIENT_RESYNC    = new ViewString( "Starting client resync, begin=" );
    private static final ZString END_CLIENT_RESYNC         = new ViewString( "End client resync, begin=" );
    private static final ZString FORCED_SEQ_RESET          = new ViewString( "Forced seq numb reset, nextOut=" );
    private static final ZString ABORT_RESYNC              = new ViewString( "Abort resync as not connected / logged off" );
    private static final ZString MSGS_SENT                 = new ViewString( ", msgsSent=" );

    private static final ZString IGNORE_DUP_SEQ_BELOW_ZERO = new ViewString( "Ignore duplicate resend request, beginSeqNum=" );
    
    /**
     * SequenceGapMgr is not threadsafe and must only be invoked by the session receive thread
     */
    
    private    final SequenceNumGapMgr _seqGapMgr;

    private          int  _syncInNo             = 0; 
    private          int  _nextExpectedInSeqNo  = 1;
    private          int  _nextOutSeqNo         = 1;
    private          int  _lastBeginSeqNo       = -1;

    private          ReusableString        _logOutMsg = new ReusableString(100);

    private          boolean _forceSeqNumReset = false;
    private          boolean _manualLoggedOut  = false;

    private    final int _maxResendRequestSize;

    public SessionSeqNumController( SeqNumSession session, SessionStateFactory stateFactory, StatefulSessionFactory msgFactory ) {
        super( session, stateFactory, msgFactory );
        
        _maxResendRequestSize = session.getConfig().getMaxResendRequestSize();
        
        _seqGapMgr = (_maxResendRequestSize>0) ? new BatchedSeqNumGapMgr() : new DummySeqNumGapMgr();
    }

    @Override
    public void reset() {
        super.reset();
        
        _seqGapMgr.clear();
        
        _lastBeginSeqNo       = -1;
        _nextExpectedInSeqNo  = 1;
        _nextOutSeqNo         = 1;
        _forceSeqNumReset     = false;
        _manualLoggedOut      = false;
    }
    
    public int getNextExpectedInSeqNo() {
        return _nextExpectedInSeqNo;
    }

    @Override
    public void recoverContext( Message msg, boolean inBound ) {
        int seqNum = msg.getMsgSeqNum();

        if ( inBound ) {
            setNextExpectedInSeqNum( seqNum+1 );
        } else {
            setNextOutSeqNum( seqNum+1 );
        }
    }

    @Override
    public String logOutboundRecoveryFinished(){
        return super.logOutboundRecoveryFinished() + " nextOutSeqNum=" + getNextOutSeqNum();
    }
    
    @Override
    public String logInboundRecoveryFinished(){
        return super.logInboundRecoveryFinished() + " nextExpectedInSeqNum=" + getNextExpectedInSeqNo();
    }
    
    public void sendLogonNow( int heartBtInt, int nextExpInSeqNum ) {
        Message logon = _sessionFactory.getSessionLogOn( heartBtInt, _nextOutSeqNo, nextExpInSeqNum );
        
        if ( isForceSeqNumReset() ) {
            if ( logon instanceof LogonWrite ) {
                ((LogonWrite)logon).setResetSeqNumFlag( true );
                
                setForceSeqNumReset( false );
            }
        }
        
        _session.handleNow( logon );
    }
    
    public void sendLogoutNow( ReusableString logMsg, Message logon ) {
        Message hb = _sessionFactory.getLogOut( logMsg, 0, logon, _nextOutSeqNo, _nextExpectedInSeqNo );
        
        _session.handleNow( hb );
        
        _session.disconnect( false );
    }

    @Override
    public void outboundError() {
        decNextOutSeqNum();        
    }
    
    /**
     * gap fill the inbound index and set the next expected in seq num with value from sequence reset
     *  
     * @param newSeqNum
     */
    public void growInSeqNumUpwards( int newSeqNum ) {
        try {
            _session.gapFillInbound( _nextExpectedInSeqNo, newSeqNum );
        } catch( Exception e ) {
            _log.error( FAILED_GAP_FILL, " from " + _nextExpectedInSeqNo + " to " + newSeqNum + " for " + _session.getComponentId(), e );
        }
        
        _log.info(  "Override next expected IN seqNum from " + _nextExpectedInSeqNo + ", to " + newSeqNum + " for " + _session.getComponentId() );
        
        setNextExpectedInSeqNum( newSeqNum );
    }

    /**
     * rather than disconnect auto truncate down to the sequence number provided in the login as the client
     * has lost the messages
     * 
     * @NOTE THE DATA MESSAGES ARE STILL IN LOG AND WILL BE REPLAYED INTO PROCESSOR ON RESTART
     *  
     * @param newSeqNum
     */
    public void truncateInDownwards( int newSeqNum ) {
        try {
            _session.truncateInboundIndexDown( newSeqNum, _nextExpectedInSeqNo-1 );
            
        } catch( Exception e ) {
            _log.error( FAILED_TRUNCATE_INDEX, " INBOUND from " + _nextExpectedInSeqNo + " down to " + newSeqNum + " for " + 
                        _session.getComponentId(), e );
        }
        
        _log.info(  "Override next expected IN seqNum from " + _nextExpectedInSeqNo + ", DOWN to " + newSeqNum + 
                    " for " + _session.getComponentId() );
        
        setNextExpectedInSeqNum( newSeqNum );
    }

    /**
     * grow the outbound index and set next expected to newSeqNum+1
     *  
     * @param newSeqNum
     */
    public boolean growOutSeqNumUpwards( int newSeqNum ) {
        try {
            _session.gapExtendOutbound( _nextOutSeqNo, newSeqNum );
            
            _log.info(  "GrowOutSeqNum, next OUT seqNum from " + _nextOutSeqNo + ", to " + newSeqNum+1 + " for " + _session.getComponentId() );
            
            setNextOutSeqNum( newSeqNum+1 );
            
            return true;
            
        } catch( Exception e ) {
            _log.error( FAILED_GROW_OUT, " from " + _nextOutSeqNo + " to " + newSeqNum + " for " + _session.getComponentId(), e );
        }
        
        return false;
    }

    public void persistInMsgAndUpdateSeqNum( int newSeqNum ) {
        _session.persistLastInboundMesssage();
        
        _nextExpectedInSeqNo = newSeqNum;
    }

    public int getInSeqNumForSycn() {
        return _syncInNo;
    }

    public void setInSeqNumForSycn( int recvSeqNum ) {
        _log.info( "SessionController setting sync num to : " + recvSeqNum + " for " + _session.getComponentId() );
        _syncInNo = recvSeqNum;
    }

    public void forceLogOut() {
        LogoutImpl logOut = new LogoutImpl();
        logOut.setMessageHandler( _session );
        logOut.setNextExpectedMsgSeqNum( _nextExpectedInSeqNo );
        _manualLoggedOut = true;
        _session.handleNow( logOut );
        changeState( getStateLoggedOut() );
    }
    
    public boolean isManualLoggedOut() {
        return _manualLoggedOut;
    }
    
    public void forceLogOn() {
        _manualLoggedOut = false;
        sendLogonNow( _controllerConfig.getHeartBeatIntSecs(), getNextExpectedInSeqNo() );
    }

    @Override
    public void persistPosDupMsg( int newSeqNum ) {
        super.persistPosDupMsg( newSeqNum );
        
        if ( _seqGapMgr.inGapRequest() ) { // actively processing a gap request
            
            SequenceGapRange gap = _seqGapMgr.checkSubGap( newSeqNum );
            
            boolean inRange = _seqGapMgr.received( newSeqNum ); // message was part of current gap fill
            
            if ( gap != null ) {
                // GAP IN THE GAP FILL
                
                sendNextResendRequest( gap, false, true );   // send additional gap fill request for new sub gap
                
            } else {
                if ( inRange ) { // message was part of current gap fill
                    if ( _seqGapMgr.pending() == 0 ) {    // no more pending in current gap fill
                        sendNextResendRequest( _seqGapMgr.next(), false, false );   // current gap finished send next if enqueued
                    }
                }
            }
        }
    }
    
    /**
     * resync the client by sending them the missing messages
     * 
     * @NOTE MUST ONLY BE CALLED IN THE RECEIVERS THREAD OF CONTROL
     * 
     * @param beginSeqNo        from beginSeqNo
     * @param endSeqNo          upto and including the endSeqNo, 0/999999 means upto last sent msg
     * @param sendImmediately   if true then send now as opposed to enqueuing on dispatch thread, only use immediate as part of logon processs
     */
    public final void clientResyncOutEvents( int beginSeqNo, int endSeqNo, boolean sendImmediately ) {

        if ( endSeqNo == 0 || endSeqNo == _controllerConfig.getMaxSeqNum() ) {
            endSeqNo = _nextOutSeqNo-1;
        } 
        
        if ( beginSeqNo < 0 ) {
            _logOutMsg.copy( BEGIN_SEQ_BELOW_ZERO ).append( beginSeqNo ).append( Strings.FOR ).append( _session.getComponentId() );
            _log.warn( _logOutMsg );
            beginSeqNo = 0;
        }

        if ( beginSeqNo > endSeqNo ) {
            int tmp    = beginSeqNo;
            beginSeqNo = endSeqNo;
            endSeqNo   = tmp;
            
            _logOutMsg.copy( SWAPPED_BEGIN_END_SEQ_NUM ).append( beginSeqNo ).append( Strings.TO ).append( _nextOutSeqNo ).append( Strings.FOR ).append( _session.getComponentId() );
            _log.warn( _logOutMsg );
        }
        
        if ( endSeqNo >= _nextOutSeqNo ) {
            _logOutMsg.copy( GROW_OUT_SEQ_NUM ).append( endSeqNo ).append( Strings.FROM ).append( _nextOutSeqNo ).append( Strings.FOR ).append( _session.getComponentId() );
            _log.warn( _logOutMsg );

            if ( ! growOutSeqNumUpwards( endSeqNo ) ) {
                _logOutMsg.copy( FAILED_GROW_OUT.getError() ).append( Strings.FROM ).append( _nextOutSeqNo )
                          .append( Strings.TO ).append( endSeqNo ).append( Strings.FOR ).append( _session.getComponentId() );

                sendLogoutNow( _logOutMsg, null );
                
                return;
            }
        }
        
        if ( _forceSeqNumReset ) {
            _forceSeqNumReset = false;

            _logOutMsg.copy( FORCED_SEQ_RESET ).append( _nextOutSeqNo+1 ).append( Strings.FOR ).append( _session.getComponentId() );
            _log.warn( _logOutMsg );
            
            final Message gap = _sessionFactory.createForceSeqNumResetMessage( _nextOutSeqNo+1 );

            if ( gap != null ) {
                send( gap, sendImmediately );
            }

            return;
        }

        if ( beginSeqNo == _lastBeginSeqNo ) {
            _logOutMsg.copy( IGNORE_DUP_SEQ_BELOW_ZERO ).append( beginSeqNo ).append( Strings.FOR ).append( _session.getComponentId() );
            _log.warn( _logOutMsg );
            return;
        }
        
        _lastBeginSeqNo = beginSeqNo;
        
        _logOutMsg.copy( STARTING_CLIENT_RESYNC ).append( beginSeqNo ).append( Strings.TO ).append( endSeqNo ).append( Strings.FOR ).append( _session.getComponentId() );
        _log.info( _logOutMsg );
        
        int gapBeginSeqNo = -1;
        int msgsSent = 0;
        
        SeqNumSession seqSession = _session;
        
        for ( int curMsgSeqNo = beginSeqNo ; curMsgSeqNo <= endSeqNo ; curMsgSeqNo++ ) {
            
            // if disconnected abort
            // if not send immediately and logged out abort
            // if loggedOut and send immediately then CONTINUE
            if ( (!sendImmediately && isLoggedOut()) || !_session.isConnected() ) {
                _logOutMsg.copy( ABORT_RESYNC ).append( Strings.FOR ).append( _session.getComponentId() );
                _log.warn( _logOutMsg );
                break;
            }
            
            final Message recMsg = seqSession.retrieve( curMsgSeqNo );
            
            if ( recMsg != null && ! seqSession.isSessionMessage( recMsg ) ) {
                if ( gapBeginSeqNo > -1 ) { // close off the gap fill and send, prior to the msg just retrieved
                    if ( seqSession.getStateConfig().isGapFillAllowed() ) {
                        msgsSent += sendGapMsgs( sendImmediately, gapBeginSeqNo, curMsgSeqNo );
                    } else {
                        msgsSent += sendDummyMsgs( sendImmediately, gapBeginSeqNo, curMsgSeqNo );
                    }

                    gapBeginSeqNo = -1;
                }
                
                recMsg.setFlag( MsgFlag.PossDupFlag, true );

                send( recMsg, sendImmediately );
                ++msgsSent;

            } else if ( gapBeginSeqNo == -1 ) {
                gapBeginSeqNo = curMsgSeqNo;
            } else {
                // gap already started, this msg will be included
            }
        }
        
        if ( gapBeginSeqNo != -1 ) {
            if ( seqSession.getStateConfig().isGapFillAllowed() ) {
                msgsSent += sendGapMsgs( sendImmediately, gapBeginSeqNo, endSeqNo+1 );
            } else {
                msgsSent += sendDummyMsgs( sendImmediately, gapBeginSeqNo, endSeqNo );
            }
        }

        _logOutMsg.copy( END_CLIENT_RESYNC ).append( beginSeqNo ).append( Strings.TO ).append( _nextOutSeqNo )
                                            .append( MSGS_SENT ).append( msgsSent ).append( Strings.FOR ).append( _session.getComponentId() );
        _log.info( _logOutMsg );
    }

    private int sendDummyMsgs( boolean sendImmediately, int gapBeginSeqNo, int gapEndSeqNo ) {
        int msgsSent = 0; 
        for ( int seqNum = gapBeginSeqNo ; seqNum <= gapEndSeqNo ; ++seqNum ) {
            final Message msg = _sessionFactory.getHeartbeat( null );
            msg.setMsgSeqNum( seqNum );
            msg.setFlag( MsgFlag.PossDupFlag, true );
            send( msg, sendImmediately );
            ++msgsSent;
        }
        return msgsSent;
    }

    private int sendGapMsgs( boolean sendImmediately, int gapBeginSeqNo, int gapEndSeqNo ) {
        int msgsSent = 0; 
        final Message gap = _sessionFactory.createGapFillMessage( gapBeginSeqNo, gapEndSeqNo );
        if ( gap != null ) {
            send( gap, sendImmediately );
            ++msgsSent;
        }
        return msgsSent;
    }

    /**
     * rather than disconnect auto truncate down to the sequence number provided in the login as the client
     * has lost the messages
     * 
     * @NOTE THE DATA MESSAGES ARE STILL IN LOG AND WILL BE REPLAYED INTO PROCESSOR ON RESTART
     * 
     * @NOTE ONLY FOR USE IN ADMINS
     *  
     * @param newSeqNum
     */
    void truncateOutDownwards( int newSeqNum ) {
        try {
            _session.truncateOutboundIndexDown( newSeqNum, _nextOutSeqNo-1 );
            
        } catch( Exception e ) {
            _log.error( FAILED_TRUNCATE_INDEX, " OTBOUND from " + _nextOutSeqNo + " down to " + newSeqNum + " for " + 
                        _session.getComponentId(), e );
        }
        
        _log.info(  "Override next expected OUT seqNum from " + _nextOutSeqNo + ", DOWN to " + newSeqNum + 
                    " for " + _session.getComponentId() );
        
        setNextOutSeqNum( newSeqNum );
    }
    
    public int getNextOutSeqNum() {
        return _nextOutSeqNo;
    }

    @Override
    protected void onLogout() {
        setInSeqNumForSycn( 0 );
        _seqGapMgr.clear();
    }

    /**
     * as part of the 4.4 tag 789 alternate gap fill policy, need to gap fill automatically 
     * @param nextExpectedMsgSeqNum
     */
    public void sendMissingMsgsToClientNow( int fromSeqNum ) {
        clientResyncOutEvents( fromSeqNum, _nextOutSeqNo, true );
    }

    public void sendResendRequest( int fromSeqNum, int toSeqNum, final boolean isNow ) {
        
        int messages = toSeqNum - fromSeqNum;
        
        if ( _maxResendRequestSize == 0 || toSeqNum == 0 ) {
            doSendResendRequestNow( fromSeqNum, toSeqNum, isNow );
        } else {
            // batch requests 
            
            int nextFrom = fromSeqNum;
            int nextMax = fromSeqNum + _maxResendRequestSize;
            
            while( messages > 0 ) {
                if ( nextMax > toSeqNum ) nextMax = toSeqNum;

                enqueueResendRequest( nextFrom, nextMax );
                
                messages -= _maxResendRequestSize;
                
                nextFrom += _maxResendRequestSize;
                nextMax += _maxResendRequestSize;
            }
            
            sendNextResendRequest( _seqGapMgr.next(), isNow, false );
        }
    }

    private void sendNextResendRequest( SequenceGapRange gap, boolean isNow, boolean isSubGap ) {
        
        if ( gap != null ) {
            if ( !isSubGap ) {                          // DONT override the gap fill with a child node
                _seqGapMgr.gapFillRequested( gap );
            }
            
            doSendResendRequestNow( gap._from, gap._to, isNow );
        }
    }

    private void enqueueResendRequest( int nextFrom, int nextMax ) {
        if ( ! _seqGapMgr.add( nextFrom, nextMax ) ) {
            _log.info( "Ignore resend request as already enqueued" );
        }
    }

    private void doSendResendRequestNow( int fromSeqNum, int toSeqNum, final boolean isNow ) {
        Message req = _sessionFactory.getResendRequest( fromSeqNum, toSeqNum );
        if ( isNow ) {
            _session.handleNow( req );
        } else {
            _session.handle( req );
        }
    }

    public void setNextExpectedInSeqNum( int newExpInSeqNum ) {
        _nextExpectedInSeqNo = newExpInSeqNum;
    }
    
    public void setNextOutSeqNum( int newOutSeqNum ) {
        _nextOutSeqNo = newOutSeqNum;
    }
    
    /**
     * @return next outbound seqNum, post increments ready for next send
     */
    public int getAndIncNextOutSeqNum() {
        return _nextOutSeqNo++;
    }

    public void decNextOutSeqNum() {
        if ( _nextOutSeqNo > 0 ) {
            --_nextOutSeqNo;
        }
    }

    @Override
    public String info() {
        return " nextOutSeqNo=" + _nextOutSeqNo + ", nextInSeqNo=" + _nextExpectedInSeqNo + super.info();
    }
    
    public boolean isForceSeqNumReset() {
        return _forceSeqNumReset;
    }

    public void setForceSeqNumReset( boolean forceSeqNumReset ) {
        _forceSeqNumReset = forceSeqNumReset;
    }

    @Override
    protected Message formLogoutMessage() {
        Message lo = _sessionFactory.getLogOut( ERR_MISSED_HB.getError(), 0, null, _nextOutSeqNo, _nextExpectedInSeqNo );
        return lo;
    }
    
    /**
     * admin method for overriding seqNums
     * 
     * @throws SessionException 
     */
    void setSeqNums( int nextInSeqNum, int nextOutSeqNum, boolean forceReset ) throws SessionException {
        if ( isLoggedOut() ) {
            adjustNextInSeqNum( nextInSeqNum );
            
            adjustNextOutSeqNum( nextOutSeqNum );
            
            setForceSeqNumReset( forceReset );
            
        } else {
            throw new SessionException( "Must stop the session before changing seqNums" );
        }
    }

    public void adjustNextOutSeqNum( int nextOutSeqNum ) {
        if ( nextOutSeqNum != _nextOutSeqNo ) {
            if ( nextOutSeqNum < _nextOutSeqNo ) {
                truncateOutDownwards( nextOutSeqNum );
            } else  {
                growOutSeqNumUpwards( nextOutSeqNum );
            }
        }
    }

    public void adjustNextInSeqNum( int nextInSeqNum ) {
        if ( nextInSeqNum != _nextExpectedInSeqNo ) {
            if ( nextInSeqNum < _nextExpectedInSeqNo ) {
                truncateInDownwards( nextInSeqNum );
            } else  {
                growInSeqNumUpwards( nextInSeqNum );
            }
        }
    }
}