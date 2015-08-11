/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.codec;

import java.util.HashMap;
import java.util.Map;
import com.rr.core.codec.AbstractBinaryDecoder;
import com.rr.core.lang.*;
import com.rr.core.model.*;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.internal.type.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.BinaryDecodeBuilder;
import com.rr.core.codec.binary.DebugBinaryDecodeBuilder;
import com.rr.model.generated.internal.events.factory.*;
import com.rr.model.generated.internal.events.impl.*;
import com.rr.model.generated.internal.events.interfaces.*;
import com.rr.model.generated.internal.type.*;
import com.rr.model.generated.internal.core.SizeType;

@SuppressWarnings( "unused" )

public final class MilleniumLSEDecoder extends AbstractBinaryDecoder {

   // Attrs
    private static final byte      MSG_Logon = (byte)'A';
    private static final byte      MSG_LogonReply = (byte)'B';
    private static final byte      MSG_Logout = (byte)'5';
    private static final byte      MSG_MissedMessageRequest = (byte)'M';
    private static final byte      MSG_MissedMsgRequestAck = (byte)'N';
    private static final byte      MSG_MissedMsgReport = (byte)'P';
    private static final byte      MSG_Heartbeat = (byte)'0';
    private static final byte      MSG_NewOrder = (byte)'D';
    private static final byte      MSG_OrderCancelRequest = (byte)'F';
    private static final byte      MSG_CancelReject = (byte)'9';
    private static final byte      MSG_Reject = (byte)'3';
    private static final byte      MSG_BusinessReject = (byte)'j';
    private static final byte      MSG_OrderReplaceRequest = (byte)'G';
    private static final byte      MSG_ExecutionReport = (byte)'8';

    private boolean _debug = false;

    private BinaryDecodeBuilder _builder;

    private       byte _msgType;
    private final byte                        _protocolVersion;
    private       int                         _msgStatedLen;
    private final ViewString                  _lookup = new ViewString();
    private final ReusableString _dump  = new ReusableString(256);

    // dict var holders for conditional mappings and fields with no corresponding event entry .. useful for hooks
    private       ReusableString              _userName = new ReusableString(30);
    private       ReusableString              _password = new ReusableString(30);
    private       ReusableString              _newPassword = new ReusableString(30);
    private       byte                        _msgVersion;
    private       int                         _rejectCode;
    private       ReusableString              _pwdExpiryDayCount = new ReusableString(30);
    private       ReusableString              _reason = new ReusableString(30);
    private       byte                        _appId;
    private       int                         _lastMsgSeqNum;
    private       byte                        _missedMsgReqAckType;
    private       byte                        _missedMsgReportType;
    private       ReusableString              _clOrdId = new ReusableString(30);
    private       ReusableString              _account = new ReusableString(30);
    private       byte                        _clearingAccount;
    private       int                         _instrumentId;
    private       byte                        _mesQualifier;
    private       byte                        _ordType;
    private       byte                        _timeInForce;
    private       int                         _expireDateTime;
    private       byte                        _side;
    private       int                         _orderQty;
    private       int                         _displayQty;
    private       double                      _price;
    private       byte                        _orderCapacity;
    private       byte                        _autoCancel;
    private       byte                        _ordSubType;
    private       byte                        _anonymity;
    private       double                      _stopPrice;
    private       ReusableString              _origClOrdId = new ReusableString(30);
    private       ReusableString              _orderId = new ReusableString(30);
    private       int                         _msgSeqNum;
    private       int                         _sendingTime;
    private       ReusableString              _rejectReason = new ReusableString(30);
    private       byte                        _rejectMsgType;
    private       ReusableString              _execId = new ReusableString(30);
    private       byte                        _execType;
    private       ReusableString              _execRefID = new ReusableString(30);
    private       byte                        _ordStatus;
    private       double                      _lastPx;
    private       int                         _lastQty;
    private       int                         _leavesQty;
    private       byte                        _container;
    private       ReusableString              _counterparty = new ReusableString(30);
    private       byte                        _liquidityInd;
    private       long                        _tradeMatchId;

   // Pools

    private final SuperPool<MilleniumLogonImpl> _milleniumLogonPool = SuperpoolManager.instance().getSuperPool( MilleniumLogonImpl.class );
    private final MilleniumLogonFactory _milleniumLogonFactory = new MilleniumLogonFactory( _milleniumLogonPool );

    private final SuperPool<MilleniumLogonReplyImpl> _milleniumLogonReplyPool = SuperpoolManager.instance().getSuperPool( MilleniumLogonReplyImpl.class );
    private final MilleniumLogonReplyFactory _milleniumLogonReplyFactory = new MilleniumLogonReplyFactory( _milleniumLogonReplyPool );

    private final SuperPool<MilleniumLogoutImpl> _milleniumLogoutPool = SuperpoolManager.instance().getSuperPool( MilleniumLogoutImpl.class );
    private final MilleniumLogoutFactory _milleniumLogoutFactory = new MilleniumLogoutFactory( _milleniumLogoutPool );

    private final SuperPool<MilleniumMissedMessageRequestImpl> _milleniumMissedMessageRequestPool = SuperpoolManager.instance().getSuperPool( MilleniumMissedMessageRequestImpl.class );
    private final MilleniumMissedMessageRequestFactory _milleniumMissedMessageRequestFactory = new MilleniumMissedMessageRequestFactory( _milleniumMissedMessageRequestPool );

    private final SuperPool<MilleniumMissedMsgRequestAckImpl> _milleniumMissedMsgRequestAckPool = SuperpoolManager.instance().getSuperPool( MilleniumMissedMsgRequestAckImpl.class );
    private final MilleniumMissedMsgRequestAckFactory _milleniumMissedMsgRequestAckFactory = new MilleniumMissedMsgRequestAckFactory( _milleniumMissedMsgRequestAckPool );

    private final SuperPool<MilleniumMissedMsgReportImpl> _milleniumMissedMsgReportPool = SuperpoolManager.instance().getSuperPool( MilleniumMissedMsgReportImpl.class );
    private final MilleniumMissedMsgReportFactory _milleniumMissedMsgReportFactory = new MilleniumMissedMsgReportFactory( _milleniumMissedMsgReportPool );

    private final SuperPool<HeartbeatImpl> _heartbeatPool = SuperpoolManager.instance().getSuperPool( HeartbeatImpl.class );
    private final HeartbeatFactory _heartbeatFactory = new HeartbeatFactory( _heartbeatPool );

    private final SuperPool<RecoveryNewOrderSingleImpl> _newOrderSinglePool = SuperpoolManager.instance().getSuperPool( RecoveryNewOrderSingleImpl.class );
    private final RecoveryNewOrderSingleFactory _newOrderSingleFactory = new RecoveryNewOrderSingleFactory( _newOrderSinglePool );

    private final SuperPool<RecoveryCancelRequestImpl> _cancelRequestPool = SuperpoolManager.instance().getSuperPool( RecoveryCancelRequestImpl.class );
    private final RecoveryCancelRequestFactory _cancelRequestFactory = new RecoveryCancelRequestFactory( _cancelRequestPool );

    private final SuperPool<RecoveryCancelRejectImpl> _cancelRejectPool = SuperpoolManager.instance().getSuperPool( RecoveryCancelRejectImpl.class );
    private final RecoveryCancelRejectFactory _cancelRejectFactory = new RecoveryCancelRejectFactory( _cancelRejectPool );

    private final SuperPool<SessionRejectImpl> _sessionRejectPool = SuperpoolManager.instance().getSuperPool( SessionRejectImpl.class );
    private final SessionRejectFactory _sessionRejectFactory = new SessionRejectFactory( _sessionRejectPool );

    private final SuperPool<RecoveryVagueOrderRejectImpl> _vagueOrderRejectPool = SuperpoolManager.instance().getSuperPool( RecoveryVagueOrderRejectImpl.class );
    private final RecoveryVagueOrderRejectFactory _vagueOrderRejectFactory = new RecoveryVagueOrderRejectFactory( _vagueOrderRejectPool );

    private final SuperPool<RecoveryCancelReplaceRequestImpl> _cancelReplaceRequestPool = SuperpoolManager.instance().getSuperPool( RecoveryCancelReplaceRequestImpl.class );
    private final RecoveryCancelReplaceRequestFactory _cancelReplaceRequestFactory = new RecoveryCancelReplaceRequestFactory( _cancelReplaceRequestPool );

    private final SuperPool<RecoveryNewOrderAckImpl> _newOrderAckPool = SuperpoolManager.instance().getSuperPool( RecoveryNewOrderAckImpl.class );
    private final RecoveryNewOrderAckFactory _newOrderAckFactory = new RecoveryNewOrderAckFactory( _newOrderAckPool );

    private final SuperPool<RecoveryCancelledImpl> _cancelledPool = SuperpoolManager.instance().getSuperPool( RecoveryCancelledImpl.class );
    private final RecoveryCancelledFactory _cancelledFactory = new RecoveryCancelledFactory( _cancelledPool );

    private final SuperPool<RecoveryReplacedImpl> _replacedPool = SuperpoolManager.instance().getSuperPool( RecoveryReplacedImpl.class );
    private final RecoveryReplacedFactory _replacedFactory = new RecoveryReplacedFactory( _replacedPool );

    private final SuperPool<RecoveryRejectedImpl> _rejectedPool = SuperpoolManager.instance().getSuperPool( RecoveryRejectedImpl.class );
    private final RecoveryRejectedFactory _rejectedFactory = new RecoveryRejectedFactory( _rejectedPool );

    private final SuperPool<RecoveryExpiredImpl> _expiredPool = SuperpoolManager.instance().getSuperPool( RecoveryExpiredImpl.class );
    private final RecoveryExpiredFactory _expiredFactory = new RecoveryExpiredFactory( _expiredPool );

    private final SuperPool<RecoveryRestatedImpl> _restatedPool = SuperpoolManager.instance().getSuperPool( RecoveryRestatedImpl.class );
    private final RecoveryRestatedFactory _restatedFactory = new RecoveryRestatedFactory( _restatedPool );

    private final SuperPool<RecoveryTradeNewImpl> _tradeNewPool = SuperpoolManager.instance().getSuperPool( RecoveryTradeNewImpl.class );
    private final RecoveryTradeNewFactory _tradeNewFactory = new RecoveryTradeNewFactory( _tradeNewPool );

    private final SuperPool<RecoveryTradeCorrectImpl> _tradeCorrectPool = SuperpoolManager.instance().getSuperPool( RecoveryTradeCorrectImpl.class );
    private final RecoveryTradeCorrectFactory _tradeCorrectFactory = new RecoveryTradeCorrectFactory( _tradeCorrectPool );

    private final SuperPool<RecoveryTradeCancelImpl> _tradeCancelPool = SuperpoolManager.instance().getSuperPool( RecoveryTradeCancelImpl.class );
    private final RecoveryTradeCancelFactory _tradeCancelFactory = new RecoveryTradeCancelFactory( _tradeCancelPool );

    private final SuperPool<RecoverySuspendedImpl> _suspendedPool = SuperpoolManager.instance().getSuperPool( RecoverySuspendedImpl.class );
    private final RecoverySuspendedFactory _suspendedFactory = new RecoverySuspendedFactory( _suspendedPool );


   // Constructors
    public MilleniumLSEDecoder() {
        super();
        setBuilder();
        _protocolVersion = (byte)'2';
    }

   // decode methods
    @Override
    protected final int getCurrentIndex() {
        return _builder.getCurrentIndex();
    }

    @Override
    protected BinaryDecodeBuilder getBuilder() {
        return _builder;
    }

    @Override
    public boolean isDebug() {
        return _debug;
    }

    @Override
    public void setDebug( boolean isDebugOn ) {
        _debug = isDebugOn;
        setBuilder();
    }

    private void setBuilder() {
        _builder = (_debug) ? new DebugBinaryDecodeBuilder<com.rr.codec.emea.exchange.millenium.MilleniumDecodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.millenium.MilleniumDecodeBuilderImpl() )
                            : new com.rr.codec.emea.exchange.millenium.MilleniumDecodeBuilderImpl();
    }

    @Override
    protected final Message doMessageDecode() {
        _builder.setMaxIdx( _maxIdx );

        switch( _msgType ) {
        case MSG_Logon:
            return decodeLogon();
        case MSG_LogonReply:
            return decodeLogonReply();
        case MSG_Logout:
            return decodeLogout();
        case MSG_MissedMessageRequest:
            return decodeMissedMessageRequest();
        case MSG_MissedMsgRequestAck:
            return decodeMissedMsgRequestAck();
        case MSG_MissedMsgReport:
            return decodeMissedMsgReport();
        case MSG_Heartbeat:
            return decodeHeartbeat();
        case MSG_NewOrder:
            return decodeNewOrder();
        case MSG_OrderCancelRequest:
            return decodeOrderCancelRequest();
        case MSG_CancelReject:
            return decodeCancelReject();
        case MSG_Reject:
            return decodeReject();
        case MSG_BusinessReject:
            return decodeBusinessReject();
        case MSG_OrderReplaceRequest:
            return decodeOrderReplaceRequest();
        case MSG_ExecutionReport:
            return decodeExecutionReport();
        case '1':
        case '2':
        case '4':
        case '6':
        case '7':
        case ':':
        case ';':
        case '<':
        case '=':
        case '>':
        case '?':
        case '@':
        case 'C':
        case 'E':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'O':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case '[':
        case '\\':
        case ']':
        case '^':
        case '_':
        case '`':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
            break;
        }
        if ( _debug ) {
            _dump.append( "Skipped Unsupported Message : " ).append( _msgType );
            _log.info( _dump );
            _dump.reset();
        }
        return null;
    }

    private final Message decodeLogon() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "Logon" ).append( " : " );
        }

        final MilleniumLogonImpl msg = _milleniumLogonFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "userName" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getUserNameForUpdate(), 25 );

        if ( _debug ) _dump.append( "\nField: " ).append( "password" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getPasswordForUpdate(), 25 );

        if ( _debug ) _dump.append( "\nField: " ).append( "newPassword" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getNewPasswordForUpdate(), 25 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgVersion" ).append( " : " );
        _msgVersion = _builder.decodeUByte();
        _builder.end();
        return msg;
    }

    private final Message decodeLogonReply() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "LogonReply" ).append( " : " );
        }

        final MilleniumLogonReplyImpl msg = _milleniumLogonReplyFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        msg.setRejectCode( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "pwdExpiryDayCount" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getPwdExpiryDayCountForUpdate(), 30 );
        _builder.end();
        return msg;
    }

    private final Message decodeLogout() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "Logout" ).append( " : " );
        }

        final MilleniumLogoutImpl msg = _milleniumLogoutFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "reason" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getReasonForUpdate(), 20 );
        _builder.end();
        return msg;
    }

    private final Message decodeMissedMessageRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MissedMessageRequest" ).append( " : " );
        }

        final MilleniumMissedMessageRequestImpl msg = _milleniumMissedMessageRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "appId" ).append( " : " );
        msg.setAppId( _builder.decodeUByte() );

        if ( _debug ) _dump.append( "\nField: " ).append( "lastMsgSeqNum" ).append( " : " );
        msg.setLastMsgSeqNum( _builder.decodeInt() );
        _builder.end();
        return msg;
    }

    private final Message decodeMissedMsgRequestAck() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MissedMsgRequestAck" ).append( " : " );
        }

        final MilleniumMissedMsgRequestAckImpl msg = _milleniumMissedMsgRequestAckFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "missedMsgReqAckType" ).append( " : " );
        msg.setMissedMsgReqAckType( MilleniumMissedMsgReqAckType.getVal( _builder.decodeByte() ) );
        _builder.end();
        return msg;
    }

    private final Message decodeMissedMsgReport() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MissedMsgReport" ).append( " : " );
        }

        final MilleniumMissedMsgReportImpl msg = _milleniumMissedMsgReportFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "missedMsgReportType" ).append( " : " );
        msg.setMissedMsgReportType( MilleniumMissedMsgReportType.getVal( _builder.decodeByte() ) );
        _builder.end();
        return msg;
    }

    private final Message decodeHeartbeat() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "Heartbeat" ).append( " : " );
        }

        final HeartbeatImpl msg = _heartbeatFactory.get();
        _builder.end();
        return msg;
    }

    private final Message decodeNewOrder() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "NewOrder" ).append( " : " );
        }

        final RecoveryNewOrderSingleImpl msg = _newOrderSingleFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getClOrdIdForUpdate(), 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 11 );

        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getAccountForUpdate(), 10 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clearingAccount" ).append( " : " );
        _clearingAccount = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _instrumentId = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "mesQualifier" ).append( " : " );
        _mesQualifier = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 1 );

        if ( _debug ) _dump.append( "\nField: " ).append( "ordType" ).append( " : " );
        msg.setOrdType( OrdType.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        msg.setTimeInForce( TimeInForce.getVal( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "expireDateTime" ).append( " : " );
        _expireDateTime = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        msg.setSide( Side.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        msg.setOrderQty( _builder.decodeInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _displayQty = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        msg.setPrice( _builder.decodeDecimal() );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderCapacity" ).append( " : " );
        msg.setOrderCapacity( transformOrderCapacity( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "autoCancel" ).append( " : " );
        _autoCancel = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "ordSubType" ).append( " : " );
        _ordSubType = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "anonymity" ).append( " : " );
        _anonymity = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "stopPrice" ).append( " : " );
        _stopPrice = _builder.decodePrice();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.skip( 10 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "postdecode" ).append( " : " );
        enrich( msg );
        _builder.end();
        return msg;
    }

    private final Message decodeOrderCancelRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "OrderCancelRequest" ).append( " : " );
        }

        final RecoveryCancelRequestImpl msg = _cancelRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getClOrdIdForUpdate(), 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getOrigClOrdIdForUpdate(), 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getOrderIdForUpdate(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _instrumentId = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 2 );

        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        msg.setSide( Side.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 10 );
        _builder.end();
        return msg;
    }

    private final Message decodeCancelReject() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "CancelReject" ).append( " : " );
        }

        final RecoveryCancelRejectImpl msg = _cancelRejectFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "appId" ).append( " : " );
        _appId = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getClOrdIdForUpdate(), 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getOrderIdForUpdate(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _rejectCode = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 8 );
        _builder.end();
        return msg;
    }

    private final Message decodeReject() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "Reject" ).append( " : " );
        }

        final SessionRejectImpl msg = _sessionRejectFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _rejectCode = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "rejectReason" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getTextForUpdate(), 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "rejectMsgType" ).append( " : " );
        _builder.decodeString( msg.getRefMsgTypeForUpdate() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( _clOrdId, 20 );
        _builder.end();
        return msg;
    }

    private final Message decodeBusinessReject() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "BusinessReject" ).append( " : " );
        }

        final RecoveryVagueOrderRejectImpl msg = _vagueOrderRejectFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "appId" ).append( " : " );
        _appId = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.decodeIntToString( msg.getTextForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getClOrdIdForUpdate(), 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getOrderIdForUpdate(), 12 );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 10 );
        _builder.end();
        return msg;
    }

    private final Message decodeOrderReplaceRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "OrderReplaceRequest" ).append( " : " );
        }

        final RecoveryCancelReplaceRequestImpl msg = _cancelReplaceRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getClOrdIdForUpdate(), 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getOrigClOrdIdForUpdate(), 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getOrderIdForUpdate(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _instrumentId = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "expireDateTime" ).append( " : " );
        _expireDateTime = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        msg.setOrderQty( _builder.decodeInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _displayQty = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        msg.setPrice( _builder.decodeDecimal() );

        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getAccountForUpdate(), 10 );

        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        msg.setTimeInForce( TimeInForce.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        msg.setSide( Side.getVal( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "stopPrice" ).append( " : " );
        _stopPrice = _builder.decodePrice();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 10 );
        _builder.end();
        return msg;
    }

    private final Message conditionalDecoder1( Message prevMsg ) {
        switch( _execType ) {
        case 'G': {
                final RecoveryTradeCorrectImpl msg = _tradeCorrectFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.getExecRefIDForUpdate().copy( _execRefID );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLastPx( _lastPx );
                msg.setLastQty( _lastQty );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setLiquidityInd( transformLiquidityInd( _liquidityInd ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case '5': {
                final RecoveryReplacedImpl msg = _replacedFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case '4': {
                final RecoveryCancelledImpl msg = _cancelledFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case 'D': {
                final RecoveryRestatedImpl msg = _restatedFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case 'F': {
                final RecoveryTradeNewImpl msg = _tradeNewFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLastPx( _lastPx );
                msg.setLastQty( _lastQty );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setLiquidityInd( transformLiquidityInd( _liquidityInd ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case '9': {
                final RecoverySuspendedImpl msg = _suspendedFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case '8': {
                final RecoveryRejectedImpl msg = _rejectedFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case '0': {
                final RecoveryNewOrderAckImpl msg = _newOrderAckFactory.get();
                if ( _debug ) _dump.append( "\nHook : " ).append( "predecode" ).append( " : " );
                if ( _nanoStats ) msg.setAckReceived( _received );
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case 'C': {
                final RecoveryExpiredImpl msg = _expiredFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case 'H': {
                final RecoveryTradeCancelImpl msg = _tradeCancelFactory.get();
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getExecIdForUpdate().copy( _execId );
                msg.getClOrdIdForUpdate().copy( _clOrdId );
                msg.getOrderIdForUpdate().copy( _orderId );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.getExecRefIDForUpdate().copy( _execRefID );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setLastPx( _lastPx );
                msg.setLastQty( _lastQty );
                msg.setLeavesQty( _leavesQty );
                msg.setSide( Side.getVal( _side ) );
                msg.setLiquidityInd( transformLiquidityInd( _liquidityInd ) );
                msg.setSendingTime( _sendingTime );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case 49: case 50: case 51: case 54: case 55: case 58: case 59: case 60:         
case 61: case 62: case 63: case 64: case 65: case 66: case 69: 
            break;
        }
        throw new RuntimeDecodingException( "No matching condition for conditional message type" );
    }
    private final Message decodeExecutionReport() {
        Message msg = null;
        if ( _debug ) _dump.append( "\nField: " ).append( "appId" ).append( " : " );
        _appId = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _msgSeqNum = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.decodeZStringFixedWidth( _execId, 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeZStringFixedWidth( _clOrdId, 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeZStringFixedWidth( _orderId, 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _execType = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.decodeZStringFixedWidth( _execRefID, 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _ordStatus = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _rejectCode = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _lastPx = _builder.decodePrice();
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _lastQty = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _leavesQty = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _container = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _displayQty = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _instrumentId = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _side = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.decodeZStringFixedWidth( _counterparty, 11 );
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _liquidityInd = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _tradeMatchId = _builder.decodeLong();
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _sendingTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.skip( 10 );
        msg = conditionalDecoder1( null );
        _builder.end();
        return msg;
    }


   // transform methods
    private static final LiquidityInd[] _liquidityIndMap = new LiquidityInd[19];
    private static final int    _liquidityIndIndexOffset = 'A';
    static {
        for ( int i=0 ; i < _liquidityIndMap.length ; i++ ) {
             _liquidityIndMap[i] = null;
        }
         _liquidityIndMap[ (byte)'A' - _liquidityIndIndexOffset ] = LiquidityInd.AddedLiquidity;
         _liquidityIndMap[ (byte)'R' - _liquidityIndIndexOffset ] = LiquidityInd.RemovedLiquidity;
         _liquidityIndMap[ (byte)'C' - _liquidityIndIndexOffset ] = LiquidityInd.Auction;
    }

    private LiquidityInd transformLiquidityInd( byte extVal ) {
        final int arrIdx = extVal - _liquidityIndIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _liquidityIndMap.length ) {
            throw new RuntimeDecodingException( " unsupported decoding on LiquidityInd for value " + (char)extVal );
        }
        LiquidityInd intVal = _liquidityIndMap[ arrIdx ];
        if ( intVal == null ) {
            throw new RuntimeDecodingException( " unsupported decoding on LiquidityInd for value " + (char)extVal );
        }
        return intVal;
    }

    private static final OrderCapacity[] _orderCapacityMap = new OrderCapacity[4];
    private static final int    _orderCapacityIndexOffset = '1';
    static {
        for ( int i=0 ; i < _orderCapacityMap.length ; i++ ) {
             _orderCapacityMap[i] = OrderCapacity.Principal;
        }
         _orderCapacityMap[ (byte)'1' - _orderCapacityIndexOffset ] = OrderCapacity.RisklessPrincipal;
         _orderCapacityMap[ (byte)'2' - _orderCapacityIndexOffset ] = OrderCapacity.Principal;
         _orderCapacityMap[ (byte)'3' - _orderCapacityIndexOffset ] = OrderCapacity.AgentForOtherMember;
    }

    private OrderCapacity transformOrderCapacity( byte extVal ) {
        final int arrIdx = extVal - _orderCapacityIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _orderCapacityMap.length ) {
            return OrderCapacity.Principal;
        }
        OrderCapacity intVal = _orderCapacityMap[ arrIdx ];
        return intVal;
    }

    @Override    public final int parseHeader( final byte[] msg, final int offset, final int bytesRead ) {        _appId = -1;               _binaryMsg = msg;        _maxIdx = bytesRead + offset; // temp assign maxIdx to last data bytes in buffer        _offset = offset;        _builder.start( msg, offset, _maxIdx );                if ( bytesRead < 4 ) {            ReusableString copy = TLC.instance().getString();            if ( bytesRead == 0 )  {                copy.setValue( "{empty}" );            } else{                copy.setValue( msg, offset, bytesRead );            }            throw new RuntimeDecodingException( "Millenium Messsage too small, len=" + bytesRead, copy );        } else if ( msg.length < _maxIdx ){            throwDecodeException( "Buffer too small for specified bytesRead=" + bytesRead + ",offset=" + offset + ", bufLen=" + msg.length );        }                final byte version = _builder.decodeByte();                if ( version != _protocolVersion ) {            throwDecodeException( "Expected version="  + _protocolVersion + " not " + version );        }        _msgStatedLen = _builder.decodeShort() + 3; // add 3 to pass protoVer and length        _msgType = _builder.decodeByte();                _maxIdx = _msgStatedLen + _offset;  // correctly assign maxIdx as last bytes of current message         if ( _maxIdx > _binaryMsg.length )  _maxIdx  = _binaryMsg.length;                return _msgStatedLen;    }        public final byte getAppId() {        return _appId;    }        // enrich of NOS only used by exchange emulator        private static final ViewString _lseREC = new ViewString( "L" );        private void enrich( RecoveryNewOrderSingleImpl nos ) {                Instrument instr = null;                if ( _instrumentId > 0 ) {            instr = _instrumentLocator.getInstrumentByID( _lseREC, _instrumentId );        }        if ( instr != null ) {            nos.setCurrency( instr.getCurrency() );            nos.getSymbolForUpdate().setValue( instr.getRIC() );        }                nos.setInstrument( instr );    }    }
