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

public final class ETIEurexLFTDecoder extends AbstractBinaryDecoder implements com.rr.codec.emea.exchange.eti.ETIDecoder {

   // Attrs
    private static final short      MSG_ConnectionGatewayRequest = 10020;
    private static final short      MSG_ConnectionGatewayResponse = 10021;
    private static final short      MSG_SessionLogonRequest = 10000;
    private static final short      MSG_SessionLogonResponse = 10001;
    private static final short      MSG_SessionLogoutRequest = 10002;
    private static final short      MSG_SessionLogoutResponse = 10003;
    private static final short      MSG_UserLogonRequest = 10018;
    private static final short      MSG_UserLogonResponse = 10019;
    private static final short      MSG_UserLogoutRequest = 10029;
    private static final short      MSG_UserLogoutResponse = 10024;
    private static final short      MSG_ThrottleUpdateNotification = 10028;
    private static final short      MSG_Subscribe = 10025;
    private static final short      MSG_SubscribeResponse = 10005;
    private static final short      MSG_Unsubscribe = 10006;
    private static final short      MSG_UnsubscribeResponse = 10007;
    private static final short      MSG_Retransmit = 10008;
    private static final short      MSG_RetransmitResponse = 10009;
    private static final short      MSG_RetransmitOrderEvents = 10026;
    private static final short      MSG_RetransmitOrderEventsResponse = 10027;
    private static final short      MSG_Heartbeat = 10011;
    private static final short      MSG_HeartbeatNotification = 10023;
    private static final short      MSG_SessionLogoutNotification = 10012;
    private static final short      MSG_CancelOrderSingleRequest = 10109;
    private static final short      MSG_CancelOrderNotification = 10112;
    private static final short      MSG_ImmediateExecResponse = 10103;
    private static final short      MSG_BookOrderExecution = 10104;
    private static final short      MSG_Reject = 10010;
    private static final short      MSG_NewOrderRequest = 10100;
    private static final short      MSG_ReplaceOrderSingleRequest = 10106;
    private static final short      MSG_NewOrderStandardResponse = 10101;
    private static final short      MSG_ReplaceOrderStandardResponse = 10107;
    private static final short      MSG_CancelOrderStandardResponse = 10110;

    private boolean _debug = false;

    private BinaryDecodeBuilder _builder;

    private       short _msgType;
    private final byte                        _protocolVersion;
    private       int                         _msgStatedLen;
    private final ViewString                  _lookup = new ViewString();
    private final ReusableString _dump  = new ReusableString(256);

    // dict var holders for conditional mappings and fields with no corresponding event entry .. useful for hooks
    private       int                         _msgSeqNum;
    private       int                         _senderSubID;
    private       int                         _partyIDSessionID;
    private       ReusableString              _password = new ReusableString(30);
    private       int                         _requestTime;
    private       int                         _sendingTime;
    private       int                         _gatewayID;
    private       int                         _gatewaySubID;
    private       int                         _secondaryGatewayID;
    private       int                         _secondaryGatewaySubID;
    private       byte                        _sessionMode;
    private       byte                        _tradSesMode;
    private       int                         _heartBtIntMS;
    private       ReusableString              _defaultCstmApplVerID = new ReusableString(30);
    private       byte                        _applUsageOrders;
    private       byte                        _applUsageQuotes;
    private       boolean                     _orderRoutingIndicator;
    private       ReusableString              _applicationSystemName = new ReusableString(30);
    private       ReusableString              _applicationSystemVer = new ReusableString(30);
    private       ReusableString              _applicationSystemVendor = new ReusableString(30);
    private       long                        _throttleTimeIntervalMS;
    private       int                         _throttleNoMsgs;
    private       int                         _throttleDisconnectLimit;
    private       int                         _sessionInstanceID;
    private       int                         _userName;
    private       int                         _subscriptionScope;
    private       byte                        _refApplID;
    private       int                         _applSubID;
    private       int                         _refApplSubID;
    private       long                        _applBegSeqNum;
    private       long                        _applEndSeqNum;
    private       int                         _partitionID;
    private       long                        _refApplLastSeqNum;
    private       int                         _applTotalMessageCount;
    private       ReusableString              _applBegMsgID = new ReusableString(30);
    private       ReusableString              _applEndMsgID = new ReusableString(30);
    private       ReusableString              _refApplLastMsgID = new ReusableString(30);
    private       int                         _varTextLen;
    private       ReusableString              _varText = new ReusableString(30);
    private       long                        _orderId;
    private       long                        _clOrdId;
    private       long                        _origClOrdId;
    private       int                         _marketSegmentID;
    private       int                         _simpleSecurityID;
    private       int                         _targetPartyIDSessionID;
    private       int                         _trdRegTSTimeOut;
    private       ReusableString              _applMsgID = new ReusableString(30);
    private       byte                        _applID;
    private       boolean                     _applResendFlag;
    private       byte                        _lastFragment;
    private       long                        _securityId;
    private       long                        _execId;
    private       int                         _cumQty;
    private       int                         _cxlQty;
    private       int                         _partyIDEnteringTrader;
    private       int                         _execRestatementReason;
    private       byte                        _partyIDEnteringFirm;
    private       byte                        _ordStatus;
    private       byte                        _execType;
    private       byte                        _productComplex;
    private       int                         _trdRegTSTimeIn;
    private       long                        _tranExecId;
    private       int                         _trdRegTSEntryTime;
    private       int                         _trdRegTSTimePriority;
    private       int                         _leavesQty;
    private       int                         _noLegExecs;
    private       byte                        _triggered;
    private       byte                        _noFills;
    private       double                      _fillPx;
    private       int                         _fillQty;
    private       int                         _fillMatchID;
    private       int                         _fillExecID;
    private       byte                        _fillLiquidityInd;
    private       long                        _legSecurityID;
    private       double                      _legLastPx;
    private       int                         _legLastQty;
    private       int                         _legExecID;
    private       byte                        _legSide;
    private       byte                        _fillsIndex;
    private       long                        _senderLocationID;
    private       byte                        _accountType;
    private       byte                        _side;
    private       ReusableString              _account = new ReusableString(30);
    private       int                         _sessionRejectReason;
    private       byte                        _sessionStatus;
    private       double                      _price;
    private       double                      _stopPx;
    private       double                      _maxPricePercentage;
    private       int                         _orderQty;
    private       int                         _expireDate;
    private       byte                        _applSeqIndicator;
    private       byte                        _ordType;
    private       byte                        _priceValidityCheckType;
    private       byte                        _timeInForce;
    private       byte                        _execInst;
    private       byte                        _tradingSessionSubID;
    private       byte                        _orderCapacity;
    private       byte                        _positionEffect;
    private       ReusableString              _uniqueClientCode = new ReusableString(30);
    private       ReusableString              _srcLinkId = new ReusableString(30);

   // Pools

    private final SuperPool<ETIConnectionGatewayRequestImpl> _eTIConnectionGatewayRequestPool = SuperpoolManager.instance().getSuperPool( ETIConnectionGatewayRequestImpl.class );
    private final ETIConnectionGatewayRequestFactory _eTIConnectionGatewayRequestFactory = new ETIConnectionGatewayRequestFactory( _eTIConnectionGatewayRequestPool );

    private final SuperPool<ETIConnectionGatewayResponseImpl> _eTIConnectionGatewayResponsePool = SuperpoolManager.instance().getSuperPool( ETIConnectionGatewayResponseImpl.class );
    private final ETIConnectionGatewayResponseFactory _eTIConnectionGatewayResponseFactory = new ETIConnectionGatewayResponseFactory( _eTIConnectionGatewayResponsePool );

    private final SuperPool<ETISessionLogonRequestImpl> _eTISessionLogonRequestPool = SuperpoolManager.instance().getSuperPool( ETISessionLogonRequestImpl.class );
    private final ETISessionLogonRequestFactory _eTISessionLogonRequestFactory = new ETISessionLogonRequestFactory( _eTISessionLogonRequestPool );

    private final SuperPool<ETISessionLogonResponseImpl> _eTISessionLogonResponsePool = SuperpoolManager.instance().getSuperPool( ETISessionLogonResponseImpl.class );
    private final ETISessionLogonResponseFactory _eTISessionLogonResponseFactory = new ETISessionLogonResponseFactory( _eTISessionLogonResponsePool );

    private final SuperPool<ETISessionLogoutRequestImpl> _eTISessionLogoutRequestPool = SuperpoolManager.instance().getSuperPool( ETISessionLogoutRequestImpl.class );
    private final ETISessionLogoutRequestFactory _eTISessionLogoutRequestFactory = new ETISessionLogoutRequestFactory( _eTISessionLogoutRequestPool );

    private final SuperPool<ETISessionLogoutResponseImpl> _eTISessionLogoutResponsePool = SuperpoolManager.instance().getSuperPool( ETISessionLogoutResponseImpl.class );
    private final ETISessionLogoutResponseFactory _eTISessionLogoutResponseFactory = new ETISessionLogoutResponseFactory( _eTISessionLogoutResponsePool );

    private final SuperPool<ETIUserLogonRequestImpl> _eTIUserLogonRequestPool = SuperpoolManager.instance().getSuperPool( ETIUserLogonRequestImpl.class );
    private final ETIUserLogonRequestFactory _eTIUserLogonRequestFactory = new ETIUserLogonRequestFactory( _eTIUserLogonRequestPool );

    private final SuperPool<ETIUserLogonResponseImpl> _eTIUserLogonResponsePool = SuperpoolManager.instance().getSuperPool( ETIUserLogonResponseImpl.class );
    private final ETIUserLogonResponseFactory _eTIUserLogonResponseFactory = new ETIUserLogonResponseFactory( _eTIUserLogonResponsePool );

    private final SuperPool<ETIUserLogoutRequestImpl> _eTIUserLogoutRequestPool = SuperpoolManager.instance().getSuperPool( ETIUserLogoutRequestImpl.class );
    private final ETIUserLogoutRequestFactory _eTIUserLogoutRequestFactory = new ETIUserLogoutRequestFactory( _eTIUserLogoutRequestPool );

    private final SuperPool<ETIUserLogoutResponseImpl> _eTIUserLogoutResponsePool = SuperpoolManager.instance().getSuperPool( ETIUserLogoutResponseImpl.class );
    private final ETIUserLogoutResponseFactory _eTIUserLogoutResponseFactory = new ETIUserLogoutResponseFactory( _eTIUserLogoutResponsePool );

    private final SuperPool<ETIThrottleUpdateNotificationImpl> _eTIThrottleUpdateNotificationPool = SuperpoolManager.instance().getSuperPool( ETIThrottleUpdateNotificationImpl.class );
    private final ETIThrottleUpdateNotificationFactory _eTIThrottleUpdateNotificationFactory = new ETIThrottleUpdateNotificationFactory( _eTIThrottleUpdateNotificationPool );

    private final SuperPool<ETISubscribeImpl> _eTISubscribePool = SuperpoolManager.instance().getSuperPool( ETISubscribeImpl.class );
    private final ETISubscribeFactory _eTISubscribeFactory = new ETISubscribeFactory( _eTISubscribePool );

    private final SuperPool<ETISubscribeResponseImpl> _eTISubscribeResponsePool = SuperpoolManager.instance().getSuperPool( ETISubscribeResponseImpl.class );
    private final ETISubscribeResponseFactory _eTISubscribeResponseFactory = new ETISubscribeResponseFactory( _eTISubscribeResponsePool );

    private final SuperPool<ETIUnsubscribeImpl> _eTIUnsubscribePool = SuperpoolManager.instance().getSuperPool( ETIUnsubscribeImpl.class );
    private final ETIUnsubscribeFactory _eTIUnsubscribeFactory = new ETIUnsubscribeFactory( _eTIUnsubscribePool );

    private final SuperPool<ETIUnsubscribeResponseImpl> _eTIUnsubscribeResponsePool = SuperpoolManager.instance().getSuperPool( ETIUnsubscribeResponseImpl.class );
    private final ETIUnsubscribeResponseFactory _eTIUnsubscribeResponseFactory = new ETIUnsubscribeResponseFactory( _eTIUnsubscribeResponsePool );

    private final SuperPool<ETIRetransmitImpl> _eTIRetransmitPool = SuperpoolManager.instance().getSuperPool( ETIRetransmitImpl.class );
    private final ETIRetransmitFactory _eTIRetransmitFactory = new ETIRetransmitFactory( _eTIRetransmitPool );

    private final SuperPool<ETIRetransmitResponseImpl> _eTIRetransmitResponsePool = SuperpoolManager.instance().getSuperPool( ETIRetransmitResponseImpl.class );
    private final ETIRetransmitResponseFactory _eTIRetransmitResponseFactory = new ETIRetransmitResponseFactory( _eTIRetransmitResponsePool );

    private final SuperPool<ETIRetransmitOrderEventsImpl> _eTIRetransmitOrderEventsPool = SuperpoolManager.instance().getSuperPool( ETIRetransmitOrderEventsImpl.class );
    private final ETIRetransmitOrderEventsFactory _eTIRetransmitOrderEventsFactory = new ETIRetransmitOrderEventsFactory( _eTIRetransmitOrderEventsPool );

    private final SuperPool<ETIRetransmitOrderEventsResponseImpl> _eTIRetransmitOrderEventsResponsePool = SuperpoolManager.instance().getSuperPool( ETIRetransmitOrderEventsResponseImpl.class );
    private final ETIRetransmitOrderEventsResponseFactory _eTIRetransmitOrderEventsResponseFactory = new ETIRetransmitOrderEventsResponseFactory( _eTIRetransmitOrderEventsResponsePool );

    private final SuperPool<HeartbeatImpl> _heartbeatPool = SuperpoolManager.instance().getSuperPool( HeartbeatImpl.class );
    private final HeartbeatFactory _heartbeatFactory = new HeartbeatFactory( _heartbeatPool );

    private final SuperPool<ETISessionLogoutNotificationImpl> _eTISessionLogoutNotificationPool = SuperpoolManager.instance().getSuperPool( ETISessionLogoutNotificationImpl.class );
    private final ETISessionLogoutNotificationFactory _eTISessionLogoutNotificationFactory = new ETISessionLogoutNotificationFactory( _eTISessionLogoutNotificationPool );

    private final SuperPool<RecoveryCancelRequestImpl> _cancelRequestPool = SuperpoolManager.instance().getSuperPool( RecoveryCancelRequestImpl.class );
    private final RecoveryCancelRequestFactory _cancelRequestFactory = new RecoveryCancelRequestFactory( _cancelRequestPool );

    private final SuperPool<RecoveryCancelledImpl> _cancelledPool = SuperpoolManager.instance().getSuperPool( RecoveryCancelledImpl.class );
    private final RecoveryCancelledFactory _cancelledFactory = new RecoveryCancelledFactory( _cancelledPool );

    private final SuperPool<RecoveryTradeNewImpl> _tradeNewPool = SuperpoolManager.instance().getSuperPool( RecoveryTradeNewImpl.class );
    private final RecoveryTradeNewFactory _tradeNewFactory = new RecoveryTradeNewFactory( _tradeNewPool );

    private final SuperPool<SessionRejectImpl> _sessionRejectPool = SuperpoolManager.instance().getSuperPool( SessionRejectImpl.class );
    private final SessionRejectFactory _sessionRejectFactory = new SessionRejectFactory( _sessionRejectPool );

    private final SuperPool<RecoveryNewOrderSingleImpl> _newOrderSinglePool = SuperpoolManager.instance().getSuperPool( RecoveryNewOrderSingleImpl.class );
    private final RecoveryNewOrderSingleFactory _newOrderSingleFactory = new RecoveryNewOrderSingleFactory( _newOrderSinglePool );

    private final SuperPool<RecoveryCancelReplaceRequestImpl> _cancelReplaceRequestPool = SuperpoolManager.instance().getSuperPool( RecoveryCancelReplaceRequestImpl.class );
    private final RecoveryCancelReplaceRequestFactory _cancelReplaceRequestFactory = new RecoveryCancelReplaceRequestFactory( _cancelReplaceRequestPool );

    private final SuperPool<RecoveryNewOrderAckImpl> _newOrderAckPool = SuperpoolManager.instance().getSuperPool( RecoveryNewOrderAckImpl.class );
    private final RecoveryNewOrderAckFactory _newOrderAckFactory = new RecoveryNewOrderAckFactory( _newOrderAckPool );

    private final SuperPool<RecoveryReplacedImpl> _replacedPool = SuperpoolManager.instance().getSuperPool( RecoveryReplacedImpl.class );
    private final RecoveryReplacedFactory _replacedFactory = new RecoveryReplacedFactory( _replacedPool );


   // Constructors
    public ETIEurexLFTDecoder() {
        super();
        setBuilder();
        _protocolVersion = (byte)'1';
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
        _builder = (_debug) ? new DebugBinaryDecodeBuilder<com.rr.codec.emea.exchange.eti.ETIDecodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.eti.ETIDecodeBuilderImpl() )
                            : new com.rr.codec.emea.exchange.eti.ETIDecodeBuilderImpl();
    }

    @Override
    protected final Message doMessageDecode() {
        _builder.setMaxIdx( _maxIdx );

        switch( _msgType ) {
        case MSG_ConnectionGatewayRequest:
            return decodeConnectionGatewayRequest();
        case MSG_ConnectionGatewayResponse:
            return decodeConnectionGatewayResponse();
        case MSG_SessionLogonRequest:
            return decodeSessionLogonRequest();
        case MSG_SessionLogonResponse:
            return decodeSessionLogonResponse();
        case MSG_SessionLogoutRequest:
            return decodeSessionLogoutRequest();
        case MSG_SessionLogoutResponse:
            return decodeSessionLogoutResponse();
        case MSG_UserLogonRequest:
            return decodeUserLogonRequest();
        case MSG_UserLogonResponse:
            return decodeUserLogonResponse();
        case MSG_UserLogoutRequest:
            return decodeUserLogoutRequest();
        case MSG_UserLogoutResponse:
            return decodeUserLogoutResponse();
        case MSG_ThrottleUpdateNotification:
            return decodeThrottleUpdateNotification();
        case MSG_Subscribe:
            return decodeSubscribe();
        case MSG_SubscribeResponse:
            return decodeSubscribeResponse();
        case MSG_Unsubscribe:
            return decodeUnsubscribe();
        case MSG_UnsubscribeResponse:
            return decodeUnsubscribeResponse();
        case MSG_Retransmit:
            return decodeRetransmit();
        case MSG_RetransmitResponse:
            return decodeRetransmitResponse();
        case MSG_RetransmitOrderEvents:
            return decodeRetransmitOrderEvents();
        case MSG_RetransmitOrderEventsResponse:
            return decodeRetransmitOrderEventsResponse();
        case MSG_Heartbeat:
            return decodeHeartbeat();
        case MSG_HeartbeatNotification:
            return decodeHeartbeatNotification();
        case MSG_SessionLogoutNotification:
            return decodeSessionLogoutNotification();
        case MSG_CancelOrderSingleRequest:
            return decodeCancelOrderSingleRequest();
        case MSG_CancelOrderNotification:
            return decodeCancelOrderNotification();
        case MSG_ImmediateExecResponse:
            return decodeImmediateExecResponse();
        case MSG_BookOrderExecution:
            return decodeBookOrderExecution();
        case MSG_Reject:
            return decodeReject();
        case MSG_NewOrderRequest:
            return decodeNewOrderRequest();
        case MSG_ReplaceOrderSingleRequest:
            return decodeReplaceOrderSingleRequest();
        case MSG_NewOrderStandardResponse:
            return decodeNewOrderStandardResponse();
        case MSG_ReplaceOrderStandardResponse:
            return decodeReplaceOrderStandardResponse();
        case MSG_CancelOrderStandardResponse:
            return decodeCancelOrderStandardResponse();
        case 10004:
        case 10013:
        case 10014:
        case 10015:
        case 10016:
        case 10017:
        case 10022:
        case 10030:
        case 10031:
        case 10032:
        case 10033:
        case 10034:
        case 10035:
        case 10036:
        case 10037:
        case 10038:
        case 10039:
        case 10040:
        case 10041:
        case 10042:
        case 10043:
        case 10044:
        case 10045:
        case 10046:
        case 10047:
        case 10048:
        case 10049:
        case 10050:
        case 10051:
        case 10052:
        case 10053:
        case 10054:
        case 10055:
        case 10056:
        case 10057:
        case 10058:
        case 10059:
        case 10060:
        case 10061:
        case 10062:
        case 10063:
        case 10064:
        case 10065:
        case 10066:
        case 10067:
        case 10068:
        case 10069:
        case 10070:
        case 10071:
        case 10072:
        case 10073:
        case 10074:
        case 10075:
        case 10076:
        case 10077:
        case 10078:
        case 10079:
        case 10080:
        case 10081:
        case 10082:
        case 10083:
        case 10084:
        case 10085:
        case 10086:
        case 10087:
        case 10088:
        case 10089:
        case 10090:
        case 10091:
        case 10092:
        case 10093:
        case 10094:
        case 10095:
        case 10096:
        case 10097:
        case 10098:
        case 10099:
        case 10102:
        case 10105:
        case 10108:
        case 10111:
            break;
        }
        return null;
    }

    private final Message decodeConnectionGatewayRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ConnectionGatewayRequest" ).append( " : " );
        }

        final ETIConnectionGatewayRequestImpl msg = _eTIConnectionGatewayRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "partyIDSessionID" ).append( " : " );
        msg.setPartyIDSessionID( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "password" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getPasswordForUpdate(), 32 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeConnectionGatewayResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ConnectionGatewayResponse" ).append( " : " );
        }

        final ETIConnectionGatewayResponseImpl msg = _eTIConnectionGatewayResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "gatewayID" ).append( " : " );
        msg.setGatewayID( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "gatewaySubID" ).append( " : " );
        msg.setGatewaySubID( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "secondaryGatewayID" ).append( " : " );
        _secondaryGatewayID = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "secondaryGatewaySubID" ).append( " : " );
        _secondaryGatewaySubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "sessionMode" ).append( " : " );
        msg.setSessionMode( ETISessionMode.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "tradSesMode" ).append( " : " );
        msg.setTradSesMode( ETIEnv.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 6 );
        _builder.end();
        return msg;
    }

    private final Message decodeSessionLogonRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "SessionLogonRequest" ).append( " : " );
        }

        final ETISessionLogonRequestImpl msg = _eTISessionLogonRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "heartBtIntMS" ).append( " : " );
        msg.setHeartBtIntMS( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "partyIDSessionID" ).append( " : " );
        msg.setPartyIDSessionID( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "defaultCstmApplVerID" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getDefaultCstmApplVerIDForUpdate(), 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "password" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getPasswordForUpdate(), 32 );

        if ( _debug ) _dump.append( "\nField: " ).append( "applUsageOrders" ).append( " : " );
        msg.setApplUsageOrders( ETIOrderProcessingType.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "applUsageQuotes" ).append( " : " );
        msg.setApplUsageQuotes( ETIOrderProcessingType.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderRoutingIndicator" ).append( " : " );
        msg.setOrderRoutingIndicator( _builder.decodeBool() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.skip( 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler4" ).append( " : " );
        _builder.skip( 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "applicationSystemName" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getApplicationSystemNameForUpdate(), 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "applicationSystemVer" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getApplicationSystemVerForUpdate(), 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "applicationSystemVendor" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getApplicationSystemVendorForUpdate(), 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler5" ).append( " : " );
        _builder.skip( 3 );
        _builder.end();
        return msg;
    }

    private final Message decodeSessionLogonResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "SessionLogonResponse" ).append( " : " );
        }

        final ETISessionLogonResponseImpl msg = _eTISessionLogonResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "throttleTimeIntervalMS" ).append( " : " );
        msg.setThrottleTimeIntervalMS( _builder.decodeLong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "throttleNoMsgs" ).append( " : " );
        msg.setThrottleNoMsgs( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "throttleDisconnectLimit" ).append( " : " );
        msg.setThrottleDisconnectLimit( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "heartBtIntMS" ).append( " : " );
        msg.setHeartBtIntMS( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sessionInstanceID" ).append( " : " );
        msg.setSessionInstanceID( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "tradSesMode" ).append( " : " );
        msg.setTradSesMode( ETIEnv.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "defaultCstmApplVerID" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getDefaultCstmApplVerIDForUpdate(), 30 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 1 );
        _builder.end();
        return msg;
    }

    private final Message decodeSessionLogoutRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "SessionLogoutRequest" ).append( " : " );
        }

        final ETISessionLogoutRequestImpl msg = _eTISessionLogoutRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();
        _builder.end();
        return msg;
    }

    private final Message decodeSessionLogoutResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "SessionLogoutResponse" ).append( " : " );
        }

        final ETISessionLogoutResponseImpl msg = _eTISessionLogoutResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeUserLogonRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "UserLogonRequest" ).append( " : " );
        }

        final ETIUserLogonRequestImpl msg = _eTIUserLogonRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "userName" ).append( " : " );
        msg.setUserName( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "password" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getPasswordForUpdate(), 32 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeUserLogonResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "UserLogonResponse" ).append( " : " );
        }

        final ETIUserLogonResponseImpl msg = _eTIUserLogonResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeUserLogoutRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "UserLogoutRequest" ).append( " : " );
        }

        final ETIUserLogoutRequestImpl msg = _eTIUserLogoutRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "userName" ).append( " : " );
        msg.setUserName( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeUserLogoutResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "UserLogoutResponse" ).append( " : " );
        }

        final ETIUserLogoutResponseImpl msg = _eTIUserLogoutResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeThrottleUpdateNotification() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ThrottleUpdateNotification" ).append( " : " );
        }

        final ETIThrottleUpdateNotificationImpl msg = _eTIThrottleUpdateNotificationFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "throttleTimeIntervalMS" ).append( " : " );
        msg.setThrottleTimeIntervalMS( _builder.decodeLong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "throttleNoMsgs" ).append( " : " );
        msg.setThrottleNoMsgs( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "throttleDisconnectLimit" ).append( " : " );
        msg.setThrottleDisconnectLimit( _builder.decodeUInt() );
        _builder.end();
        return msg;
    }

    private final Message decodeSubscribe() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "Subscribe" ).append( " : " );
        }

        final ETISubscribeImpl msg = _eTISubscribeFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "subscriptionScope" ).append( " : " );
        msg.setSubscriptionScope( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "refApplID" ).append( " : " );
        msg.setRefApplID( ETIEurexDataStream.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 3 );
        _builder.end();
        return msg;
    }

    private final Message decodeSubscribeResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "SubscribeResponse" ).append( " : " );
        }

        final ETISubscribeResponseImpl msg = _eTISubscribeResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "applSubID" ).append( " : " );
        msg.setApplSubID( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeUnsubscribe() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "Unsubscribe" ).append( " : " );
        }

        final ETIUnsubscribeImpl msg = _eTIUnsubscribeFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "refApplSubID" ).append( " : " );
        msg.setRefApplSubID( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeUnsubscribeResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "UnsubscribeResponse" ).append( " : " );
        }

        final ETIUnsubscribeResponseImpl msg = _eTIUnsubscribeResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 4 );
        _builder.end();
        return msg;
    }

    private final Message decodeRetransmit() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "Retransmit" ).append( " : " );
        }

        final ETIRetransmitImpl msg = _eTIRetransmitFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "applBegSeqNum" ).append( " : " );
        msg.setApplBegSeqNum( _builder.decodeULong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "applEndSeqNum" ).append( " : " );
        msg.setApplEndSeqNum( _builder.decodeULong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "subscriptionScope" ).append( " : " );
        msg.setSubscriptionScope( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        msg.setPartitionID( _builder.decodeUShort() );

        if ( _debug ) _dump.append( "\nField: " ).append( "refApplID" ).append( " : " );
        msg.setRefApplID( ETIEurexDataStream.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 1 );
        _builder.end();
        return msg;
    }

    private final Message decodeRetransmitResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "RetransmitResponse" ).append( " : " );
        }

        final ETIRetransmitResponseImpl msg = _eTIRetransmitResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "applEndSeqNum" ).append( " : " );
        msg.setApplEndSeqNum( _builder.decodeULong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "refApplLastSeqNum" ).append( " : " );
        msg.setRefApplLastSeqNum( _builder.decodeULong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "applTotalMessageCount" ).append( " : " );
        msg.setApplTotalMessageCount( _builder.decodeUShort() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 6 );
        _builder.end();
        return msg;
    }

    private final Message decodeRetransmitOrderEvents() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "RetransmitOrderEvents" ).append( " : " );
        }

        final ETIRetransmitOrderEventsImpl msg = _eTIRetransmitOrderEventsFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "subscriptionScope" ).append( " : " );
        msg.setSubscriptionScope( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        msg.setPartitionID( _builder.decodeUShort() );

        if ( _debug ) _dump.append( "\nField: " ).append( "refApplID" ).append( " : " );
        msg.setRefApplID( ETIEurexDataStream.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "applBegMsgID" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getApplBegMsgIDForUpdate(), 16 );

        if ( _debug ) _dump.append( "\nField: " ).append( "applEndMsgID" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getApplEndMsgIDForUpdate(), 16 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 1 );
        _builder.end();
        return msg;
    }

    private final Message decodeRetransmitOrderEventsResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "RetransmitOrderEventsResponse" ).append( " : " );
        }

        final ETIRetransmitOrderEventsResponseImpl msg = _eTIRetransmitOrderEventsResponseFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        msg.setRequestTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "applTotalMessageCount" ).append( " : " );
        msg.setApplTotalMessageCount( _builder.decodeUShort() );

        if ( _debug ) _dump.append( "\nField: " ).append( "applEndMsgID" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getApplEndMsgIDForUpdate(), 16 );

        if ( _debug ) _dump.append( "\nField: " ).append( "refApplLastMsgID" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getRefApplLastMsgIDForUpdate(), 16 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 6 );
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

    private final Message decodeHeartbeatNotification() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "HeartbeatNotification" ).append( " : " );
        }

        final HeartbeatImpl msg = _heartbeatFactory.get();
        _builder.decodeTimestampUTC();
        _builder.end();
        return msg;
    }

    private final Message decodeSessionLogoutNotification() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "SessionLogoutNotification" ).append( " : " );
        }

        final ETISessionLogoutNotificationImpl msg = _eTISessionLogoutNotificationFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );
        if ( _debug ) _dump.append( "\nField: " ).append( "varTextLen" ).append( " : " );
        _varTextLen = _builder.decodeUShort();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 6 );
        _builder.decodeStringFixedWidth( _varText, _varTextLen );
        _builder.end();
        return msg;
    }

    private final Message decodeCancelOrderSingleRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "CancelOrderSingleRequest" ).append( " : " );
        }

        final RecoveryCancelRequestImpl msg = _cancelRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeLongToString( msg.getOrderIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeLongToString( msg.getClOrdIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.decodeLongToString( msg.getOrigClOrdIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1b" ).append( " : " );
        _builder.skip( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "marketSegmentID" ).append( " : " );
        _marketSegmentID = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "simpleSecurityID" ).append( " : " );
        _simpleSecurityID = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "targetPartyIDSessionID" ).append( " : " );
        _targetPartyIDSessionID = _builder.decodeUInt();
        _builder.end();
        return msg;
    }

    private final Message decodeCancelOrderNotification() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "CancelOrderNotification" ).append( " : " );
        }

        final RecoveryCancelledImpl msg = _cancelledFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _trdRegTSTimeOut = _builder.decodeTimestampUTC();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );
        if ( _debug ) _dump.append( "\nField: " ).append( "applSubID" ).append( " : " );
        _applSubID = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _partitionID = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "applMsgID" ).append( " : " );
        _builder.decodeData( _applMsgID, 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applID" ).append( " : " );
        _applID = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "applResendFlag" ).append( " : " );
        _applResendFlag = _builder.decodeBool();
        if ( _debug ) _dump.append( "\nField: " ).append( "lastFragment" ).append( " : " );
        _lastFragment = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 7 );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeLongToString( msg.getOrderIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeLongToString( msg.getClOrdIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.decodeLongToString( msg.getOrigClOrdIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.decodeLongToString( msg.getSecurityIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.decodeLongToString( msg.getExecIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1b" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        msg.setCumQty( _builder.decodeQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _cxlQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "partyIDEnteringTrader" ).append( " : " );
        _partyIDEnteringTrader = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _execRestatementReason = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "partyIDEnteringFirm" ).append( " : " );
        _partyIDEnteringFirm = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        msg.setOrdStatus( OrdStatus.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        msg.setExecType( ExecType.getVal( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "productComplex" ).append( " : " );
        _productComplex = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 2 );
        _builder.end();
        return msg;
    }

    private final Message decodeImmediateExecResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ImmediateExecResponse" ).append( " : " );
        }

        RecoveryTradeNewImpl msg = null;
        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _requestTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _trdRegTSTimeIn = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _trdRegTSTimeOut = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _sendingTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _msgSeqNum = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _partitionID = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "applID" ).append( " : " );
        _applID = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "applMsgID" ).append( " : " );
        _builder.decodeData( _applMsgID, 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastFragment" ).append( " : " );
        _lastFragment = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _orderId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _clOrdId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _origClOrdId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _securityId = _builder.decodeLong();
        if ( _debug ) _dump.append( "\nField: " ).append( "tranExecId" ).append( " : " );
        _tranExecId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSEntryTime" ).append( " : " );
        _trdRegTSEntryTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimePriority" ).append( " : " );
        _trdRegTSTimePriority = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "marketSegmentID" ).append( " : " );
        _marketSegmentID = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _leavesQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _cumQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _cxlQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "noLegExecs" ).append( " : " );
        _noLegExecs = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _execRestatementReason = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "productComplex" ).append( " : " );
        _productComplex = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _ordStatus = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _execType = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "triggered" ).append( " : " );
        _triggered = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "noFills" ).append( " : " );
        _noFills = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 7 );

        {
            RecoveryTradeNewImpl firstMsg = msg;
            for( int i=0 ; i < _noFills ; ++i ) { 
                if ( msg != null ) {
                    final RecoveryTradeNewImpl nxtMsg = _tradeNewFactory.get();
                    msg.setNext( nxtMsg );
                    msg = nxtMsg;
                } else {
                    firstMsg = msg = _tradeNewFactory.get();
                }
                if ( _debug ) _dump.append( "\nField: " ).append( "fillPx" ).append( " : " );
                _fillPx = _builder.decodePrice();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillQty" ).append( " : " );
                _fillQty = _builder.decodeQty();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillMatchID" ).append( " : " );
                _fillMatchID = _builder.decodeUInt();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillExecID" ).append( " : " );
                _fillExecID = _builder.decodeUInt();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillLiquidityInd" ).append( " : " );
                _fillLiquidityInd = _builder.decodeUByte();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillerFillsGrp" ).append( " : " );
                _builder.skip( 3 );
                msg.setSendingTime( _sendingTime );
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getOrderIdForUpdate().append( _orderId );
                msg.getClOrdIdForUpdate().append( _clOrdId );
                msg.getSecurityIdForUpdate().append( _securityId );
                msg.setLeavesQty( _leavesQty );
                msg.setCumQty( _cumQty );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setLastPx( _fillPx );
                msg.setLastQty( _fillQty );
                msg.getExecIdForUpdate().append( _fillMatchID );
                msg.setLiquidityInd( transformLiquidityInd( _fillLiquidityInd ) );
            }
            msg = firstMsg;
        }
        for( int i=0 ; i < _noLegExecs ; ++i ) { 
            if ( _debug ) _dump.append( "\nField: " ).append( "legSecurityID" ).append( " : " );
            _legSecurityID = _builder.decodeLong();
            if ( _debug ) _dump.append( "\nField: " ).append( "legLastPx" ).append( " : " );
            _legLastPx = _builder.decodePrice();
            if ( _debug ) _dump.append( "\nField: " ).append( "legLastQty" ).append( " : " );
            _legLastQty = _builder.decodeQty();
            if ( _debug ) _dump.append( "\nField: " ).append( "legExecID" ).append( " : " );
            _legExecID = _builder.decodeInt();
            if ( _debug ) _dump.append( "\nField: " ).append( "legSide" ).append( " : " );
            _legSide = _builder.decodeUByte();
            if ( _debug ) _dump.append( "\nField: " ).append( "fillsIndex" ).append( " : " );
            _fillsIndex = _builder.decodeUByte();
            if ( _debug ) _dump.append( "\nField: " ).append( "fillerLegExec" ).append( " : " );
            _builder.skip( 6 );
        }
        _builder.end();
        return msg;
    }

    private final Message decodeBookOrderExecution() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "BookOrderExecution" ).append( " : " );
        }

        RecoveryTradeNewImpl msg = null;
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _trdRegTSTimeOut = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _sendingTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "applSubID" ).append( " : " );
        _applSubID = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _partitionID = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "applMsgID" ).append( " : " );
        _builder.decodeData( _applMsgID, 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applID" ).append( " : " );
        _applID = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "applResendFlag" ).append( " : " );
        _applResendFlag = _builder.decodeBool();
        if ( _debug ) _dump.append( "\nField: " ).append( "lastFragment" ).append( " : " );
        _lastFragment = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 7 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _orderId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "senderLocationID" ).append( " : " );
        _senderLocationID = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _clOrdId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _origClOrdId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _securityId = _builder.decodeLong();
        if ( _debug ) _dump.append( "\nField: " ).append( "tranExecId" ).append( " : " );
        _tranExecId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1b" ).append( " : " );
        _builder.skip( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "marketSegmentID" ).append( " : " );
        _marketSegmentID = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _leavesQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _cumQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _cxlQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "noLegExecs" ).append( " : " );
        _noLegExecs = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _execRestatementReason = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "accountType" ).append( " : " );
        _accountType = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "productComplex" ).append( " : " );
        _productComplex = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _ordStatus = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _execType = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "triggered" ).append( " : " );
        _triggered = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "noFills" ).append( " : " );
        _noFills = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _side = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.decodeZStringFixedWidth( _account, 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 39 );

        {
            RecoveryTradeNewImpl firstMsg = msg;
            for( int i=0 ; i < _noFills ; ++i ) { 
                if ( msg != null ) {
                    final RecoveryTradeNewImpl nxtMsg = _tradeNewFactory.get();
                    msg.setNext( nxtMsg );
                    msg = nxtMsg;
                } else {
                    firstMsg = msg = _tradeNewFactory.get();
                }
                if ( _debug ) _dump.append( "\nField: " ).append( "fillPx" ).append( " : " );
                _fillPx = _builder.decodePrice();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillQty" ).append( " : " );
                _fillQty = _builder.decodeQty();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillMatchID" ).append( " : " );
                _fillMatchID = _builder.decodeUInt();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillExecID" ).append( " : " );
                _fillExecID = _builder.decodeUInt();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillLiquidityInd" ).append( " : " );
                _fillLiquidityInd = _builder.decodeUByte();
                if ( _debug ) _dump.append( "\nField: " ).append( "fillerFillsGrp" ).append( " : " );
                _builder.skip( 3 );
                msg.setSendingTime( _sendingTime );
                msg.getOrderIdForUpdate().append( _orderId );
                msg.getClOrdIdForUpdate().append( _clOrdId );
                msg.getSecurityIdForUpdate().append( _securityId );
                msg.setLeavesQty( _leavesQty );
                msg.setCumQty( _cumQty );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setExecType( ExecType.getVal( _execType ) );
                msg.setLastPx( _fillPx );
                msg.setLastQty( _fillQty );
                msg.getExecIdForUpdate().append( _fillMatchID );
                msg.setLiquidityInd( transformLiquidityInd( _fillLiquidityInd ) );
            }
            msg = firstMsg;
        }
        for( int i=0 ; i < _noLegExecs ; ++i ) { 
            if ( _debug ) _dump.append( "\nField: " ).append( "legSecurityID" ).append( " : " );
            _legSecurityID = _builder.decodeLong();
            if ( _debug ) _dump.append( "\nField: " ).append( "legLastPx" ).append( " : " );
            _legLastPx = _builder.decodePrice();
            if ( _debug ) _dump.append( "\nField: " ).append( "legLastQty" ).append( " : " );
            _legLastQty = _builder.decodeQty();
            if ( _debug ) _dump.append( "\nField: " ).append( "legExecID" ).append( " : " );
            _legExecID = _builder.decodeInt();
            if ( _debug ) _dump.append( "\nField: " ).append( "legSide" ).append( " : " );
            _legSide = _builder.decodeUByte();
            if ( _debug ) _dump.append( "\nField: " ).append( "fillsIndex" ).append( " : " );
            _fillsIndex = _builder.decodeUByte();
            if ( _debug ) _dump.append( "\nField: " ).append( "fillerLegExec" ).append( " : " );
            _builder.skip( 6 );
        }
        _builder.end();
        return msg;
    }

    private final Message decodeReject() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "Reject" ).append( " : " );
        }

        final SessionRejectImpl msg = _sessionRejectFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _requestTime = _builder.decodeTimestampUTC();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setRefSeqNum( _builder.decodeUInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "sessionRejectReason" ).append( " : " );
        msg.setSessionRejectReason( transformSessionRejectReason( _binaryMsg, _builder.getCurrentIndex(), 4 ) );
        _builder.skip( 4);
        if ( _debug ) _dump.append( "\nField: " ).append( "varTextLen" ).append( " : " );
        _varTextLen = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "sessionStatus" ).append( " : " );
        _sessionStatus = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 1 );
        _builder.decodeStringFixedWidth( msg.getTextForUpdate(), _varTextLen );
        _builder.end();
        return msg;
    }

    private final Message decodeNewOrderRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "NewOrderRequest" ).append( " : " );
        }

        final RecoveryNewOrderSingleImpl msg = _newOrderSingleFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        msg.setPrice( _builder.decodeDecimal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "stopPx" ).append( " : " );
        _stopPx = _builder.decodePrice();
        if ( _debug ) _dump.append( "\nField: " ).append( "maxPricePercentage" ).append( " : " );
        _maxPricePercentage = _builder.decodePrice();
        if ( _debug ) _dump.append( "\nField: " ).append( "senderLocationID" ).append( " : " );
        _senderLocationID = _builder.decodeULong();

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeLongToString( msg.getClOrdIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1b" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        msg.setOrderQty( _builder.decodeQty() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1c" ).append( " : " );
        _builder.skip( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "expireDate" ).append( " : " );
        _expireDate = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "marketSegmentID" ).append( " : " );
        _marketSegmentID = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "simpleSecurityID" ).append( " : " );
        _simpleSecurityID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 5 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.skip( 3 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler4" ).append( " : " );
        _builder.skip( 9 );
        if ( _debug ) _dump.append( "\nField: " ).append( "accountType" ).append( " : " );
        _accountType = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "applSeqIndicator" ).append( " : " );
        _applSeqIndicator = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        msg.setSide( transformSide( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "ordType" ).append( " : " );
        msg.setOrdType( transformOrdType( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "priceValidityCheckType" ).append( " : " );
        _priceValidityCheckType = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        msg.setTimeInForce( transformTimeInForce( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "execInst" ).append( " : " );
        msg.setExecInst( transformExecInst( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "tradingSessionSubID" ).append( " : " );
        _tradingSessionSubID = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "orderCapacity" ).append( " : " );
        msg.setOrderCapacity( transformOrderCapacity( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getAccountForUpdate(), 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "positionEffect" ).append( " : " );
        _positionEffect = _builder.decodeChar();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler5" ).append( " : " );
        _builder.skip( 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "uniqueClientCode" ).append( " : " );
        _builder.decodeString( _uniqueClientCode );

        if ( _debug ) _dump.append( "\nField: " ).append( "srcLinkId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getSrcLinkIdForUpdate(), 24 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler8" ).append( " : " );
        _builder.skip( 3 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "postdecode" ).append( " : " );
        enrich( msg );
        _builder.end();
        return msg;
    }

    private final Message decodeReplaceOrderSingleRequest() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ReplaceOrderSingleRequest" ).append( " : " );
        }

        final RecoveryCancelReplaceRequestImpl msg = _cancelReplaceRequestFactory.get();

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderSubID" ).append( " : " );
        _senderSubID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeLongToString( msg.getOrderIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeLongToString( msg.getClOrdIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.decodeLongToString( msg.getOrigClOrdIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        msg.setPrice( _builder.decodeDecimal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "stopPx" ).append( " : " );
        _stopPx = _builder.decodePrice();
        if ( _debug ) _dump.append( "\nField: " ).append( "maxPricePercentage" ).append( " : " );
        _maxPricePercentage = _builder.decodePrice();
        if ( _debug ) _dump.append( "\nField: " ).append( "senderLocationID" ).append( " : " );
        _senderLocationID = _builder.decodeULong();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1b" ).append( " : " );
        _builder.skip( 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        msg.setOrderQty( _builder.decodeQty() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1c" ).append( " : " );
        _builder.skip( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "expireDate" ).append( " : " );
        _expireDate = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "marketSegmentID" ).append( " : " );
        _marketSegmentID = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "simpleSecurityID" ).append( " : " );
        _simpleSecurityID = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "targetPartyIDSessionID" ).append( " : " );
        _targetPartyIDSessionID = _builder.decodeUInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 5 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.skip( 3 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler4" ).append( " : " );
        _builder.skip( 9 );
        if ( _debug ) _dump.append( "\nField: " ).append( "accountType" ).append( " : " );
        _accountType = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "applSeqIndicator" ).append( " : " );
        _applSeqIndicator = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        msg.setSide( transformSide( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "ordType" ).append( " : " );
        msg.setOrdType( transformOrdType( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "priceValidityCheckType" ).append( " : " );
        _priceValidityCheckType = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        msg.setTimeInForce( transformTimeInForce( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "execInst" ).append( " : " );
        msg.setExecInst( transformExecInst( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "tradingSessionSubID" ).append( " : " );
        _tradingSessionSubID = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "orderCapacity" ).append( " : " );
        msg.setOrderCapacity( transformOrderCapacity( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler5" ).append( " : " );
        _builder.skip( 1 );

        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getAccountForUpdate(), 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "positionEffect" ).append( " : " );
        _positionEffect = _builder.decodeChar();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler6" ).append( " : " );
        _builder.skip( 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "srcLinkId" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getSrcLinkIdForUpdate(), 24 );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler7" ).append( " : " );
        _builder.skip( 18 );
        _builder.end();
        return msg;
    }

    private final Message decodeNewOrderStandardResponse() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "NewOrderStandardResponse" ).append( " : " );
        }

        final RecoveryNewOrderAckImpl msg = _newOrderAckFactory.get();
        if ( _debug ) _dump.append( "\nHook : " ).append( "predecode" ).append( " : " );
        if ( _nanoStats ) msg.setAckReceived( _received );
        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _requestTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _trdRegTSTimeIn = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _trdRegTSTimeOut = _builder.decodeTimestampUTC();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        msg.setMsgSeqNum( _builder.decodeUInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _partitionID = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "applID" ).append( " : " );
        _applID = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "applMsgID" ).append( " : " );
        _builder.decodeData( _applMsgID, 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastFragment" ).append( " : " );
        _lastFragment = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.decodeLongToString( msg.getOrderIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.decodeLongToString( msg.getClOrdIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.decodeLongToString( msg.getSecurityIdForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.decodeLongToString( msg.getExecIdForUpdate() );
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSEntryTime" ).append( " : " );
        _trdRegTSEntryTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimePriority" ).append( " : " );
        _trdRegTSTimePriority = _builder.decodeTimestampUTC();

        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        msg.setOrdStatus( OrdStatus.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        msg.setExecType( ExecType.getVal( _builder.decodeByte() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _execRestatementReason = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "productComplex" ).append( " : " );
        _productComplex = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 3 );
        _builder.end();
        return msg;
    }

    private final Message conditionalDecoder1( Message prevMsg ) {
        switch( _execType ) {
        case '4': {
                final RecoveryCancelledImpl msg = _cancelledFactory.get();
                msg.setSendingTime( _sendingTime );
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getOrderIdForUpdate().append( _orderId );
                msg.getClOrdIdForUpdate().append( _clOrdId );
                msg.getOrigClOrdIdForUpdate().append( _origClOrdId );
                msg.getSecurityIdForUpdate().append( _securityId );
                msg.getExecIdForUpdate().append( _execId );
                msg.setLeavesQty( _leavesQty );
                msg.setCumQty( _cumQty );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setExecType( ExecType.getVal( _execType ) );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        case '5': {
                final RecoveryReplacedImpl msg = _replacedFactory.get();
                msg.setSendingTime( _sendingTime );
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getOrderIdForUpdate().append( _orderId );
                msg.getClOrdIdForUpdate().append( _clOrdId );
                msg.getOrigClOrdIdForUpdate().append( _origClOrdId );
                msg.getSecurityIdForUpdate().append( _securityId );
                msg.getExecIdForUpdate().append( _execId );
                msg.setLeavesQty( _leavesQty );
                msg.setCumQty( _cumQty );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setExecType( ExecType.getVal( _execType ) );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        }
        throw new RuntimeDecodingException( "No matching condition for conditional message type" );
    }
    private final Message decodeReplaceOrderStandardResponse() {
        Message msg = null;
        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _requestTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _trdRegTSTimeIn = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _trdRegTSTimeOut = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _sendingTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _msgSeqNum = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _partitionID = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "applID" ).append( " : " );
        _applID = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "applMsgID" ).append( " : " );
        _builder.decodeData( _applMsgID, 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastFragment" ).append( " : " );
        _lastFragment = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _orderId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _clOrdId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _origClOrdId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _securityId = _builder.decodeLong();
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _execId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimePriority" ).append( " : " );
        _trdRegTSTimePriority = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _leavesQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _cumQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _cxlQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _ordStatus = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _execType = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _execRestatementReason = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "productComplex" ).append( " : " );
        _productComplex = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 7 );
        msg = conditionalDecoder1( null );
        _builder.end();
        return msg;
    }

    private final Message conditionalDecoder2( Message prevMsg ) {
        switch( _ordStatus ) {
        case '4': {
                final RecoveryCancelledImpl msg = _cancelledFactory.get();
                msg.setSendingTime( _sendingTime );
                msg.setMsgSeqNum( _msgSeqNum );
                msg.getOrderIdForUpdate().append( _orderId );
                msg.getClOrdIdForUpdate().append( _clOrdId );
                msg.getOrigClOrdIdForUpdate().append( _origClOrdId );
                msg.getSecurityIdForUpdate().append( _securityId );
                msg.getExecIdForUpdate().append( _execId );
                msg.setCumQty( _cumQty );
                msg.setOrdStatus( OrdStatus.getVal( _ordStatus ) );
                msg.setExecType( ExecType.getVal( _execType ) );
                if ( prevMsg != null ) prevMsg.attachQueue( msg );
                return msg;
            }
        }
        throw new RuntimeDecodingException( "No matching condition for conditional message type" );
    }
    private final Message decodeCancelOrderStandardResponse() {
        Message msg = null;
        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _requestTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _trdRegTSTimeIn = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _trdRegTSTimeOut = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _sendingTime = _builder.decodeTimestampUTC();
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _msgSeqNum = _builder.decodeUInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _partitionID = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "applID" ).append( " : " );
        _applID = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "applMsgID" ).append( " : " );
        _builder.decodeData( _applMsgID, 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastFragment" ).append( " : " );
        _lastFragment = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _orderId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _clOrdId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _origClOrdId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _securityId = _builder.decodeLong();
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _execId = _builder.decodeULong();
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _cumQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _cxlQty = _builder.decodeQty();
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _ordStatus = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _execType = _builder.decodeChar();
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _execRestatementReason = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "productComplex" ).append( " : " );
        _productComplex = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.skip( 3 );
        msg = conditionalDecoder2( null );
        _builder.end();
        return msg;
    }


   // transform methods
    private static final Side[] _sideMap = new Side[3];
    private static final int    _sideIndexOffset = 1;
    static {
        for ( int i=0 ; i < _sideMap.length ; i++ ) {
             _sideMap[i] = null;
        }
         _sideMap[ (byte)0x01 - _sideIndexOffset ] = Side.Buy;
         _sideMap[ (byte)0x02 - _sideIndexOffset ] = Side.Sell;
    }

    private Side transformSide( byte extVal ) {
        final int arrIdx = extVal - _sideIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _sideMap.length ) {
            throw new RuntimeDecodingException( " unsupported decoding on Side for value " + (char)extVal );
        }
        Side intVal = _sideMap[ arrIdx ];
        if ( intVal == null ) {
            throw new RuntimeDecodingException( " unsupported decoding on Side for value " + (char)extVal );
        }
        return intVal;
    }

    private static final LiquidityInd[] _liquidityIndMap = new LiquidityInd[3];
    private static final int    _liquidityIndIndexOffset = 1;
    static {
        for ( int i=0 ; i < _liquidityIndMap.length ; i++ ) {
             _liquidityIndMap[i] = LiquidityInd.RemovedLiquidity;
        }
         _liquidityIndMap[ (byte)0x01 - _liquidityIndIndexOffset ] = LiquidityInd.AddedLiquidity;
         _liquidityIndMap[ (byte)0x02 - _liquidityIndIndexOffset ] = LiquidityInd.RemovedLiquidity;
    }

    private LiquidityInd transformLiquidityInd( byte extVal ) {
        final int arrIdx = extVal - _liquidityIndIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _liquidityIndMap.length ) {
            return LiquidityInd.RemovedLiquidity;
        }
        LiquidityInd intVal = _liquidityIndMap[ arrIdx ];
        return intVal;
    }

    private static final OrdType[] _ordTypeMap = new OrdType[5];
    private static final int    _ordTypeIndexOffset = 2;
    static {
        for ( int i=0 ; i < _ordTypeMap.length ; i++ ) {
             _ordTypeMap[i] = null;
        }
         _ordTypeMap[ (byte)0x05 - _ordTypeIndexOffset ] = OrdType.Market;
         _ordTypeMap[ (byte)0x02 - _ordTypeIndexOffset ] = OrdType.Limit;
    }

    private OrdType transformOrdType( byte extVal ) {
        final int arrIdx = extVal - _ordTypeIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _ordTypeMap.length ) {
            throw new RuntimeDecodingException( " unsupported decoding on OrdType for value " + (char)extVal );
        }
        OrdType intVal = _ordTypeMap[ arrIdx ];
        if ( intVal == null ) {
            throw new RuntimeDecodingException( " unsupported decoding on OrdType for value " + (char)extVal );
        }
        return intVal;
    }

    private static final TimeInForce[] _timeInForceMap = new TimeInForce[8];
    private static final int    _timeInForceIndexOffset = 0;
    static {
        for ( int i=0 ; i < _timeInForceMap.length ; i++ ) {
             _timeInForceMap[i] = null;
        }
         _timeInForceMap[ (byte)0x00 - _timeInForceIndexOffset ] = TimeInForce.Day;
         _timeInForceMap[ (byte)0x01 - _timeInForceIndexOffset ] = TimeInForce.GoodTillCancel;
         _timeInForceMap[ (byte)0x03 - _timeInForceIndexOffset ] = TimeInForce.ImmediateOrCancel;
         _timeInForceMap[ (byte)0x06 - _timeInForceIndexOffset ] = TimeInForce.GoodTillDate;
    }

    private TimeInForce transformTimeInForce( byte extVal ) {
        final int arrIdx = extVal - _timeInForceIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _timeInForceMap.length ) {
            throw new RuntimeDecodingException( " unsupported decoding on TimeInForce for value " + (char)extVal );
        }
        TimeInForce intVal = _timeInForceMap[ arrIdx ];
        if ( intVal == null ) {
            throw new RuntimeDecodingException( " unsupported decoding on TimeInForce for value " + (char)extVal );
        }
        return intVal;
    }

    private static final OrderCapacity[] _orderCapacityMap = new OrderCapacity[7];
    private static final int    _orderCapacityIndexOffset = 1;
    static {
        for ( int i=0 ; i < _orderCapacityMap.length ; i++ ) {
             _orderCapacityMap[i] = OrderCapacity.Principal;
        }
         _orderCapacityMap[ (byte)0x01 - _orderCapacityIndexOffset ] = OrderCapacity.AgentForOtherMember;
         _orderCapacityMap[ (byte)0x05 - _orderCapacityIndexOffset ] = OrderCapacity.Principal;
         _orderCapacityMap[ (byte)0x06 - _orderCapacityIndexOffset ] = OrderCapacity.Proprietary;
    }

    private OrderCapacity transformOrderCapacity( byte extVal ) {
        final int arrIdx = extVal - _orderCapacityIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _orderCapacityMap.length ) {
            return OrderCapacity.Principal;
        }
        OrderCapacity intVal = _orderCapacityMap[ arrIdx ];
        return intVal;
    }

    private static final ExecInst[] _execInstMap = new ExecInst[3];
    private static final int    _execInstIndexOffset = 1;
    static {
        for ( int i=0 ; i < _execInstMap.length ; i++ ) {
             _execInstMap[i] = null;
        }
         _execInstMap[ (byte)0x01 - _execInstIndexOffset ] = ExecInst.ReinstateOnSysFail;
         _execInstMap[ (byte)0x02 - _execInstIndexOffset ] = ExecInst.CancelOnSysFail;
    }

    private ExecInst transformExecInst( byte extVal ) {
        final int arrIdx = extVal - _execInstIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _execInstMap.length ) {
            throw new RuntimeDecodingException( " unsupported decoding on ExecInst for value " + (char)extVal );
        }
        ExecInst intVal = _execInstMap[ arrIdx ];
        if ( intVal == null ) {
            throw new RuntimeDecodingException( " unsupported decoding on ExecInst for value " + (char)extVal );
        }
        return intVal;
    }

    private static final Map<ViewString,SessionRejectReason> _sessionRejectReasonMap = new HashMap<ViewString, SessionRejectReason>( 60);
    static {
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x01" ), SessionRejectReason.RequiredTagMissing );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x05" ), SessionRejectReason.ValueIncorrect );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x07" ), SessionRejectReason.DecryptProblem );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x0B" ), SessionRejectReason.InvalidMsgType );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x10" ), SessionRejectReason.BadNumInGrp );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x63" ), SessionRejectReason.Other );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x64" ), SessionRejectReason.ThrottleLimitExceeded );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x65" ), SessionRejectReason.ExposureLimitExceeded );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x66" ), SessionRejectReason.ServiceTemporarilyNotAvailable );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x67" ), SessionRejectReason.ServiceNotAvailable );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x68" ), SessionRejectReason.ResultOfTransactionUnknown );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x69" ), SessionRejectReason.OutboundConversionError );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0xC8" ), SessionRejectReason.InternalTechnicalError );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x1027" ), SessionRejectReason.OrderNotFound );
         _sessionRejectReasonMap.put( StringFactory.hexToViewString( "0x1127" ), SessionRejectReason.PriceNotReasonable );
    }

    private SessionRejectReason transformSessionRejectReason( byte[] buf, int offset, int len ) {
        _lookup.setValue( buf, offset, len );
        SessionRejectReason intVal = _sessionRejectReasonMap.get( _lookup );
        if ( intVal == null ) {
            return SessionRejectReason.Other;
        }
        return intVal;
    }

    private int _headerPad = 2; // receiving from exchange pad is 2    @Override    public final int parseHeader( final byte[] msg, final int offset, final int bytesRead ) {               _applMsgID.reset();        _binaryMsg = msg;        _maxIdx = bytesRead + offset; // temp assign maxIdx to last data bytes in buffer        _offset = offset;        _builder.start( msg, offset, _maxIdx );                if ( bytesRead < 8 ) {            ReusableString copy = TLC.instance().getString();            if ( bytesRead == 0 )  {                copy.setValue( "{empty}" );            } else{                copy.setValue( msg, offset, bytesRead );            }            throw new RuntimeDecodingException( "ETI Messsage too small, len=" + bytesRead, copy );        } else if ( msg.length < _maxIdx ){            throwDecodeException( "Buffer too small for specified bytesRead=" + bytesRead + ",offset=" + offset + ", bufLen=" + msg.length );        }                _msgStatedLen = _builder.decodeInt();                _msgType = _builder.decodeUShort();                _builder.skip( _headerPad ); // spacer for network messageId + pad(2)                _maxIdx = _msgStatedLen + _offset;  // correctly assign maxIdx as last bytes of current message        if ( _maxIdx > _binaryMsg.length )  _maxIdx  = _binaryMsg.length;                return _msgStatedLen;    }    @Override        public void setExchangeEmulationOn() {        _headerPad = 10;                    // receiving AT exchange is 10    }    @Override    public com.rr.codec.emea.exchange.eti.ETIDecodeContext getLastContext( com.rr.codec.emea.exchange.eti.ETIDecodeContext context ) {        context.reset();        if ( _applMsgID.length() > 0 ) {            context.setLastApplMsgID( _applMsgID );            context.setLastPartitionID( _partitionID );        }                return context;    }    private static final ViewString _eurexREC = new ViewString( "d" ); // XEUR        private void enrich( RecoveryNewOrderSingleImpl nos ) {                Instrument instr = null;                if ( _simpleSecurityID > 0 ) {            instr = _instrumentLocator.getInstrumentByID( _eurexREC, _simpleSecurityID );        }        if ( instr != null ) {            nos.setCurrency( instr.getCurrency() );            nos.getSymbolForUpdate().setValue( instr.getRIC() );        }                nos.setInstrument( instr );    }}
