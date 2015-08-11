/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.fixsocket;

import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.session.Session;
import com.rr.core.session.socket.FixControllerConfig;
import com.rr.core.session.socket.SessionStateException;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.Logon;
import com.rr.model.generated.internal.events.interfaces.Logout;
import com.rr.model.generated.internal.type.EncryptMethod;
import com.rr.om.session.state.SessionState;

/**
 * initial state ie not logged into the fix engine or logged out of fixed engine
 */

/**
 * Logon Message NextExpectedMsgSeqNum Processing -

    The NextExpectedMsgSeqNum (789) field has been added in FIX 4.4 to the Logon message to support a proposed
    new way to resynchronize a FIX session. If accepted in the next major release of the protocol, its use will become
    mandatory. Pairs of counterparties may choose to adopt the new method in advance, but for now use of the field and
    implementation of the logic remains optional.
    
    NextExpectedMsgSeqNum (789) is used as follows:
    
    In its Logon request the session initiator supplies in NextExpectedMsgSeqNum (789) the value next expected from
    the session acceptor in MsgSeqNum (34). The outgoing header MsgSeqNum (34) of the Logon request is assigned
    the next-to-be-assigned sequence number as usual.
    
    The session acceptor validates the Logon request including that NextExpectedMsgSeqNum (789) does not represent
    a gap. It then constructs its Logon response with NextExpectedMsgSeqNum (789) containing the value next
    expected from the session initiator in MsgSeqNum (34) having incremented the number above the Logon request if
    that was the sequence expected. The outgoing header MsgSeqNum (34) is constructed as usual.
    
    The session initiator waits to begin sending application messages until it receives the Logon response. When it is
    received the initiator validates the response including that NextExpectedMsgSeqNum (789) does not represent a gap.
    
    Both sides react to NextExpectedMsgSeqNum (789) from its counterparty thus:
    • If equal to the next-to-be-assigned sequence, proceed sending new messages beginning with that number.
    • If lower than the next-to-be-assigned sequence, "recover" (see "Message Recovery") all messages from the the
    last message delivered prior to this Logon through the specified NextExpectedMsgSeqNum (789) sending them
    in order; then Gap Fill over the sequence number used in Logon and proceed sending newly queued messages
    with a sequence number one higher than the original Logon.
    • If higher than the next-to-be-assigned sequence, send Logout to abort the session.
    
    Neither side should generate a ResendRequest based on MsgSeqNum (34) of the incoming Logon message but
    should expect any gaps to be filled automatically. If a gap is produced by the Logon message MsgSeqNum (34), the
    receive logic should expect the gap to be filled automatically prior to receiving any messages with sequences above
    the gap.
    
    Sequence Reset (Gap Fill) -

    The Sequence Reset message has two modes: Gap Fill mode and Reset mode.
    
    Gap Fill mode is used in response to a Resend Request when one or more messages must be skipped over for the
    following reasons:
    • During normal resend processing, the sending application may choose not to send a message (e.g. an aged order).
    • During normal resend processing, a number of administrative messages are skipped and not resent (such as Heart Beats, Test Requests).

    Gap Fill mode is indicated by GapFillFlag (tag 123) field = "Y".
    
    If the GapFillFlag field is present (and equal to "Y"), the MsgSeqNum should conform to standard message
    sequencing rules (i.e. the MsgSeqNum of the Sequence Reset GapFill mode message should represent the
    beginning MsgSeqNum in the GapFill range because the remote side is expecting that next message sequence
    number).
    
    Reset mode involves specifying an arbitrarily higher new sequence number to be expected by the receiver of the
    Sequence Reset-Reset message, and is used to reestablish a FIX session after an unrecoverable application
    failure.
    
    Reset mode is indicated by the GapFillFlag (tag 123) field = "N" or if the field is omitted.
    If the GapFillFlag field is not present (or set to N), it can be assumed that the purpose of the Sequence Reset
    message is to recover from an out-of-sequence condition. In Sequence Reset - Reset mode, the MsgSeqNum in
    the header should be ignored (i.e. the receipt of a Sequence Reset - Reset mode message with an out of sequence
    MsgSeqNum should not generate resend requests). Sequence Reset – Reset should NOT be used as a normal
    response to a Resend Request (use Sequence Reset – Gap Fill mode). The Sequence Reset – Reset should
    ONLY be used to recover from a disaster situation which cannot be recovered via the use of Sequence Reset – Gap Fill. 
    Note that the use of Sequence Reset – Reset may result in the possibility of lost messages.
    
    Rules for processing all Sequence Reset messages
    The sending application will initiate the Sequence Reset. The message in all situations specifies NewSeqNo to
    reset to as the value of the next sequence number to be expected by the message receipient immediately
    following the messages and/or sequence numbers being skipped.
    
    The Sequence Reset can only increase the sequence number. If a sequence reset is received attempting to
    decrease the next expected sequence number the message should be rejected and treated as a serious error. It is
    possible to have multiple Resend Requests issued in a row (i.e. 5 to 10 followed by 5 to 11). If sequence
    number 8, 10, and 11 represent application messages while the 5-7 and 9 represent administrative messages, the
    series of messages as result of the Resend Request may appear as Sequence Reset-GapFill mode with
    NewSeqNo of 8, message 8, Sequence Reset-GapFill with NewSeqNo of 10, and message 10. This could then
    followed by Sequence Reset-GapFill with NewSeqNo of 8, message 8, Sequence Reset-GapFill with NewSeqNo
    of 10, message 10, and message 11. One must be careful to ignore the duplicate Sequence Reset-GapFill mode
    which is attempting to lower the next expected sequence number. This can be detected by checking to see if its
    MsgSeqNum is less than expected. If so, the Sequence Reset-GapFill mode is a duplicate and should be
    discarded.
 */

/**
 * WHILE LOGGED_OUT ALL MESSAGES SENT MUST BE handleNow AS THE SESSION_THREADED_DISPATCHER WONT SEND UNTIL LOGGED IN
 */
public class LoggedOutFixState implements SessionState {

    private static final Logger _log = LoggerFactory.create( LoggedOutFixState.class );
    
    private static final ZString NOT_LOGGED_IN                  = new ViewString( "Recieved unexpected message during logon process" );
    private static final ZString FORCED_LOGOUT                  = new ViewString( "Forced logout" );
    
    private static final ZString INVALID_SENDER_COMP_ID         = new ViewString( "Invalid senderCompId of " );
    private static final ZString INVALID_TARGET_COMP_ID         = new ViewString( "Invalid targetCompId of " );
    private static final ZString INVALID_TARGET_SUB_ID          = new ViewString( "Invalid targetSubId of " );
    private static final ZString INVALID_SENDER_SUB_ID          = new ViewString( "Invalid senderSubId of " );
    private static final ZString INVALID_PASSWORD               = new ViewString( "Invalid password of (hex) " );
    private static final ZString INVALID_ENCRYPT_METHOD         = new ViewString( "Invalid encryption of " );
    private static final ZString INVALID_LOGON_INBOUND_SEQNUM   = new ViewString( "Inbound sequence number mismatch of " );
    private static final ZString EXPECTED                       = new ViewString( ", expected " );
    
    private final Session             _session;
    private final FixController       _fixEngine;
    private final FixConfig           _config;
    private final FixControllerConfig _controllerConfig;

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logMsgBase;
    
    public LoggedOutFixState( Session session, FixController engine, FixConfig config ) {
        _session          = session;
        _fixEngine        = engine;
        _logMsgBase       = new ReusableString( "[LoggedOut-" + _session.getComponentId() + "] " );
        _config           = config;
        _controllerConfig = (FixControllerConfig) engine.getControllerConfig();
    }
    
    @Override
    public void handle( Message msg ) throws SessionStateException {
        if ( msg.getReusableType().getSubId() == EventIds.ID_LOGON ) {
            
            final Logon req = (Logon) msg;
            
            validateLogon( req );

            final int recvSeqNum       = req.getMsgSeqNum();
                  int nextExpInSeqNum  = _fixEngine.getNextExpectedInSeqNo();

            if ( recvSeqNum < _fixEngine.getNextExpectedInSeqNo() ) {

                _logMsg.copy( INVALID_LOGON_INBOUND_SEQNUM ).append( msg.getMsgSeqNum() ).append( EXPECTED ).append( _fixEngine.getNextExpectedInSeqNo() );
                
                if ( _fixEngine.getControllerConfig().isRecoverFromLoginSeqNumTooLow() ) {
                    _log.warn( _logMsg );
                    _fixEngine.truncateInDownwards( msg.getMsgSeqNum() );
                    nextExpInSeqNum = msg.getMsgSeqNum();
                } else {
                    _log.warn( _logMsg );
                    _fixEngine.sendLogoutNow( _logMsg, req );
                    throw new SessionStateException( _logMsg.toString() );
                }
            }

            final boolean haveInMsgGap   = recvSeqNum > nextExpInSeqNum;
            
            if ( !haveInMsgGap ) { // dont persist on msg gap as it will come in replay
                _fixEngine.persistInMsgAndUpdateSeqNum( nextExpInSeqNum+1 ); 
            }

            if ( _fixEngine.isServer() ) _fixEngine.sendLogonNow( req.getHeartBtInt(), nextExpInSeqNum );

            if ( haveInMsgGap ) {
                if ( _config.isUseNewFix44GapFillProtocol() ) { 
                    // other side will send us messages we are missing
                } else {
                    _fixEngine.sendResendRequest( nextExpInSeqNum, 0, true );
                }
                _fixEngine.setInSeqNumForSycn( recvSeqNum );
            }
            
            if ( _config.isUseNewFix44GapFillProtocol() ) { 
                // check to see if other side missing msgs from us, if so need to send them
                if ( req.getNextExpectedMsgSeqNum() > 0 && req.getNextExpectedMsgSeqNum() < _fixEngine.getNextOutSeqNum() ) {
                    _fixEngine.sendMissingMsgsToClientNow( req.getNextExpectedMsgSeqNum() );
                }
            }
            
            _fixEngine.startHeartbeatTimer( req.getHeartBtInt() );

            _fixEngine.changeState( (haveInMsgGap) ? _fixEngine.getStateSynchronise() : _fixEngine.getStateLoggedIn() );
            
        } else {
            if ( msg.getReusableType().getSubId() == EventIds.ID_LOGOUT ) {
                
                acceptMesssage( msg );
                
                Logout lo = (Logout)msg;
                
                int lastSeqNumOut = lo.getLastMsgSeqNumProcessed();
                
                if ( lastSeqNumOut != Constants.UNSET_INT ) {
                    if ( _fixEngine.getNextOutSeqNum() <= lastSeqNumOut ) {
                        _log.warn( "Logout received, outSeqNum too low, lastProcessed=" + lastSeqNumOut + ", engine nextOut=" + _fixEngine.getNextOutSeqNum() + 
                                   ", setting nextOut to " + (lastSeqNumOut+1) );
                        
                        _fixEngine.adjustNextOutSeqNum( lastSeqNumOut+1 );
                    }
                }
                
                int nextExpectedOutSeqNumByOtherParty = lo.getNextExpectedMsgSeqNum();

                if ( nextExpectedOutSeqNumByOtherParty != Constants.UNSET_INT ) {
                    if ( _fixEngine.getNextOutSeqNum() < nextExpectedOutSeqNumByOtherParty ) {
                        _log.warn( "Logout received, outSeqNum too low, nextExpectedOutSeqNumByOtherParty=" + nextExpectedOutSeqNumByOtherParty + ", engine nextOut=" + _fixEngine.getNextOutSeqNum() + 
                                   ", setting nextOut to " + nextExpectedOutSeqNumByOtherParty );
                        
                        _fixEngine.adjustNextOutSeqNum( nextExpectedOutSeqNumByOtherParty );
                    } else if ( _fixEngine.getNextOutSeqNum() > nextExpectedOutSeqNumByOtherParty ) {
                        _log.warn( "Logout received, outSeqNum too high, nextExpectedOutSeqNumByOtherParty=" + nextExpectedOutSeqNumByOtherParty + ", engine nextOut=" + _fixEngine.getNextOutSeqNum() + 
                                   ", truncate down, setting nextOut to " + nextExpectedOutSeqNumByOtherParty );
                        
                        _fixEngine.adjustNextOutSeqNum( nextExpectedOutSeqNumByOtherParty );
                    }
                }

                _logMsg.copy( _logMsgBase ).append( FORCED_LOGOUT );
                msg.dump( _logMsg );
            } else {
                _logMsg.copy( _logMsgBase ).append( NOT_LOGGED_IN ).append( msg.getReusableType().toString() );
                msg.dump( _logMsg );
            }
            
            _session.inboundRecycle( msg );

            if ( ! _fixEngine.isManualLoggedOut() ) {
                throw new SessionStateException( _logMsg.toString(), true );
            }
        }
    }

    private void acceptMesssage( Message msg ) {
        final int newSeqNum          = msg.getMsgSeqNum();
        final int nextExpectedSeqNum = _fixEngine.getNextExpectedInSeqNo();
        
        if ( newSeqNum == nextExpectedSeqNum ) {
            _fixEngine.persistInMsgAndUpdateSeqNum( newSeqNum + 1 );
        }
    }

    private void validateLogon( Logon msg ) throws SessionStateException {
        
        FixConfig fsc = _config;
        
        if ( !msg.getTargetCompId().equals( fsc.getSenderCompId() )) {
            _logMsg.copy( INVALID_TARGET_COMP_ID ).append( msg.getTargetCompId() );
            _fixEngine.sendLogoutNow( _logMsg, msg );
            throw new SessionStateException( _logMsg.toString() );
        }

        if ( ! msg.getSenderCompId().equals( fsc.getTargetCompId() ) ) {
            _logMsg.copy( INVALID_SENDER_COMP_ID ).append( msg.getSenderCompId() );
            _fixEngine.sendLogoutNow( _logMsg, msg );
            throw new SessionStateException( _logMsg.toString() );
        }

        if ( fsc.getTargetSubId().length() > 0 && ! msg.getSenderSubId().equals( fsc.getTargetSubId() ) ) {
            _logMsg.copy( INVALID_SENDER_SUB_ID ).append( msg.getSenderSubId() );
            _fixEngine.sendLogoutNow( _logMsg, msg );
            throw new SessionStateException( _logMsg.toString() );
        }

        if ( fsc.getSenderSubId().length() > 0 && ! msg.getTargetSubId().equals( fsc.getSenderSubId() ) ) {
            _logMsg.copy( INVALID_TARGET_SUB_ID ).append( msg.getTargetSubId() );
            _fixEngine.sendLogoutNow( _logMsg, msg );
            throw new SessionStateException( _logMsg.toString() );
        }
        
        if ( msg.getEncryptMethod() != null && msg.getEncryptMethod() != EncryptMethod.NoneOrOther ) {
            _logMsg.copy( INVALID_ENCRYPT_METHOD ).append( msg.getEncryptMethod() );
            _fixEngine.sendLogoutNow( _logMsg, msg );
            throw new SessionStateException( _logMsg.toString() );
        }

        if ( _fixEngine.isServer() ) {
            if ( ! fsc.getPassword().equals( msg.getRawData() ) && ! fsc.getRawData().equals( msg.getRawData() ) ) {
                _logMsg.copy( INVALID_PASSWORD ).appendHEX( msg.getRawData() );
                _fixEngine.sendLogoutNow( _logMsg, msg );
                throw new SessionStateException( _logMsg.toString() );
            }
        }
    }

    @Override
    public void connected() {
        if ( ! _fixEngine.isServer() ) {
            
            // socket is connected and as this session is not the server need initiate logon
            
            _fixEngine.sendLogonNow( _controllerConfig.getHeartBeatIntSecs(), _fixEngine.getNextExpectedInSeqNo() );
        }
    }
}
