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
import com.rr.core.lang.*;
import com.rr.core.model.*;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.pool.SuperPool;
import com.rr.core.codec.BinaryEncoder;
import com.rr.codec.emea.exchange.eti.ETIEncodeBuilderImpl;
import com.rr.core.codec.binary.BinaryEncodeBuilder;
import com.rr.core.codec.binary.DebugBinaryEncodeBuilder;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.model.internal.type.*;
import com.rr.model.generated.internal.events.factory.*;
import com.rr.model.generated.internal.events.impl.*;
import com.rr.model.generated.internal.events.interfaces.*;
import com.rr.model.generated.internal.type.*;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.core.FullEventIds;

@SuppressWarnings( {"unused", "cast"} )

public final class ETIEurexHFTEncoder implements com.rr.codec.emea.exchange.eti.ETIEncoder {

   // Member Vars
    private static final int      MSG_ConnectionGatewayRequest = 10020;
    private static final int      MSG_ConnectionGatewayResponse = 10021;
    private static final int      MSG_SessionLogonRequest = 10000;
    private static final int      MSG_SessionLogonResponse = 10001;
    private static final int      MSG_SessionLogoutRequest = 10002;
    private static final int      MSG_SessionLogoutResponse = 10003;
    private static final int      MSG_UserLogonRequest = 10018;
    private static final int      MSG_UserLogonResponse = 10019;
    private static final int      MSG_UserLogoutRequest = 10029;
    private static final int      MSG_UserLogoutResponse = 10024;
    private static final int      MSG_ThrottleUpdateNotification = 10028;
    private static final int      MSG_Subscribe = 10025;
    private static final int      MSG_SubscribeResponse = 10005;
    private static final int      MSG_Unsubscribe = 10006;
    private static final int      MSG_UnsubscribeResponse = 10007;
    private static final int      MSG_Retransmit = 10008;
    private static final int      MSG_RetransmitResponse = 10009;
    private static final int      MSG_RetransmitOrderEvents = 10026;
    private static final int      MSG_RetransmitOrderEventsResponse = 10027;
    private static final int      MSG_Heartbeat = 10011;
    private static final int      MSG_HeartbeatNotification = 10023;
    private static final int      MSG_SessionLogoutNotification = 10012;
    private static final int      MSG_CancelOrderSingleRequest = 10109;
    private static final int      MSG_CancelOrderNotification = 10112;
    private static final int      MSG_ImmediateExecResponse = 10103;
    private static final int      MSG_BookOrderExecution = 10104;
    private static final int      MSG_Reject = 10010;
    private static final int      MSG_NewOrderRequestSimple = 10125;
    private static final int      MSG_ReplaceOrderSingleShortRequest = 10126;
    private static final int      MSG_NewOrderLeanResponse = 10102;
    private static final int      MSG_ReplaceOrderLeanResponse = 10108;
    private static final int      MSG_CancelOrderLeanResponse = 10111;

    private static final byte      DEFAULT_Side = 0x00;
    private static final byte      DEFAULT_LiquidityInd = (byte)2;
    private static final byte      DEFAULT_OrdType = 0x00;
    private static final byte      DEFAULT_TimeInForce = (byte)0;
    private static final byte      DEFAULT_OrderCapacity = (byte)5;
    private static final byte      DEFAULT_ExecInst = (byte)2;

    private final byte[]                  _buf;
    private final int                     _offset;
    private final ZString                 _binaryVersion;

    private BinaryEncodeBuilder     _builder;

    private       TimeZoneCalculator      _tzCalculator = new TimeZoneCalculator();
    private       SingleByteLookup        _sv;
    private       TwoByteLookup           _tv;
    private       MultiByteLookup         _mv;
    private final ReusableString          _dump  = new ReusableString(256);

    private boolean                 _debug = false;

   // Constructors
    public ETIEurexHFTEncoder( byte[] buf, int offset ) {
        if ( buf.length < SizeType.MIN_ENCODE_BUFFER.getSize() ) {
            throw new RuntimeException( "Encode buffer too small only " + buf.length + ", min=" + SizeType.MIN_ENCODE_BUFFER.getSize() );
        }
        _buf = buf;
        _offset = offset;
        _binaryVersion   = new ViewString( "1");
        setBuilder();
    }


   // encode methods

    @Override
    public final void encode( final Message msg ) {
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_ETICONNECTIONGATEWAYREQUEST:
            encodeConnectionGatewayRequest( (ETIConnectionGatewayRequest) msg );
            break;
        case EventIds.ID_ETICONNECTIONGATEWAYRESPONSE:
            encodeConnectionGatewayResponse( (ETIConnectionGatewayResponse) msg );
            break;
        case EventIds.ID_ETISESSIONLOGONREQUEST:
            encodeSessionLogonRequest( (ETISessionLogonRequest) msg );
            break;
        case EventIds.ID_ETISESSIONLOGONRESPONSE:
            encodeSessionLogonResponse( (ETISessionLogonResponse) msg );
            break;
        case EventIds.ID_ETISESSIONLOGOUTREQUEST:
            encodeSessionLogoutRequest( (ETISessionLogoutRequest) msg );
            break;
        case EventIds.ID_ETISESSIONLOGOUTRESPONSE:
            encodeSessionLogoutResponse( (ETISessionLogoutResponse) msg );
            break;
        case EventIds.ID_ETIUSERLOGONREQUEST:
            encodeUserLogonRequest( (ETIUserLogonRequest) msg );
            break;
        case EventIds.ID_ETIUSERLOGONRESPONSE:
            encodeUserLogonResponse( (ETIUserLogonResponse) msg );
            break;
        case EventIds.ID_ETIUSERLOGOUTREQUEST:
            encodeUserLogoutRequest( (ETIUserLogoutRequest) msg );
            break;
        case EventIds.ID_ETIUSERLOGOUTRESPONSE:
            encodeUserLogoutResponse( (ETIUserLogoutResponse) msg );
            break;
        case EventIds.ID_ETITHROTTLEUPDATENOTIFICATION:
            encodeThrottleUpdateNotification( (ETIThrottleUpdateNotification) msg );
            break;
        case EventIds.ID_ETISUBSCRIBE:
            encodeSubscribe( (ETISubscribe) msg );
            break;
        case EventIds.ID_ETISUBSCRIBERESPONSE:
            encodeSubscribeResponse( (ETISubscribeResponse) msg );
            break;
        case EventIds.ID_ETIUNSUBSCRIBE:
            encodeUnsubscribe( (ETIUnsubscribe) msg );
            break;
        case EventIds.ID_ETIUNSUBSCRIBERESPONSE:
            encodeUnsubscribeResponse( (ETIUnsubscribeResponse) msg );
            break;
        case EventIds.ID_ETIRETRANSMIT:
            encodeRetransmit( (ETIRetransmit) msg );
            break;
        case EventIds.ID_ETIRETRANSMITRESPONSE:
            encodeRetransmitResponse( (ETIRetransmitResponse) msg );
            break;
        case EventIds.ID_ETIRETRANSMITORDEREVENTS:
            encodeRetransmitOrderEvents( (ETIRetransmitOrderEvents) msg );
            break;
        case EventIds.ID_ETIRETRANSMITORDEREVENTSRESPONSE:
            encodeRetranOrderEventsResponse( (ETIRetransmitOrderEventsResponse) msg );
            break;
        case EventIds.ID_HEARTBEAT:
            encodeHeartbeat( (Heartbeat) msg );
            break;
        case EventIds.ID_ETISESSIONLOGOUTNOTIFICATION:
            encodeSessionLogoutNotification( (ETISessionLogoutNotification) msg );
            break;
        case EventIds.ID_CANCELREQUEST:
            encodeOrderCancelRequest( (CancelRequest) msg );
            break;
        case EventIds.ID_CANCELLED:
            encodeUnsolOrderCancelled( (Cancelled) msg );
            break;
        case EventIds.ID_TRADENEW:
            encodeImmediateExecResponse( (TradeNew) msg );
            break;
        case EventIds.ID_SESSIONREJECT:
            encodeReject( (SessionReject) msg );
            break;
        case EventIds.ID_NEWORDERSINGLE:
            encodeNewOrderFastLimit( (NewOrderSingle) msg );
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            encodeOrderReplaceFastLimitRequest( (CancelReplaceRequest) msg );
            break;
        case EventIds.ID_NEWORDERACK:
            encodeOrderAckLean( (NewOrderAck) msg );
            break;
        case EventIds.ID_REPLACED:
            encodeReplaceResponseLean( (Replaced) msg );
            break;
        case 4:
        case 5:
        case 6:
        case 9:
        case 12:
        case 13:
        case 14:
        case 15:
        case 16:
        case 17:
        case 18:
        case 19:
        case 21:
        case 22:
        case 23:
        case 25:
        case 26:
        case 27:
        case 28:
        case 29:
        case 30:
        case 31:
        case 32:
        case 33:
        case 34:
        case 35:
        case 36:
        case 37:
            _builder.start();
            break;
        default:
            _builder.start();
            break;
        }
    }

    @Override public final int getLength() { return _builder.getLength(); }
    @Override public final int getOffset() { return _builder.getOffset(); }

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
        _builder = (_debug) ? new DebugBinaryEncodeBuilder<com.rr.codec.emea.exchange.eti.ETIEncodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.eti.ETIEncodeBuilderImpl( _buf, _offset, _binaryVersion ) )
                            : new com.rr.codec.emea.exchange.eti.ETIEncodeBuilderImpl( _buf, _offset, _binaryVersion );
    }


    public final void encodeConnectionGatewayRequest( final ETIConnectionGatewayRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ConnectionGatewayRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ConnectionGatewayRequest" ).append( "  eventType=" ).append( "ETIConnectionGatewayRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "partyIDSessionID" ).append( " : " );
        _builder.encodeUInt( (int)msg.getPartyIDSessionID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "password" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getPassword(), 32 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeConnectionGatewayResponse( final ETIConnectionGatewayResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ConnectionGatewayResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ConnectionGatewayResponse" ).append( "  eventType=" ).append( "ETIConnectionGatewayResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "gatewayID" ).append( " : " );
        _builder.encodeUInt( (int)msg.getGatewayID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "gatewaySubID" ).append( " : " );
        _builder.encodeUInt( (int)msg.getGatewaySubID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "secondaryGatewayID" ).append( " : " );
        _builder.encodeUInt( Constants.UNSET_INT );    // secondaryGatewayID
        if ( _debug ) _dump.append( "\nField: " ).append( "secondaryGatewaySubID" ).append( " : " );
        _builder.encodeUInt( Constants.UNSET_INT );    // secondaryGatewaySubID
        if ( _debug ) _dump.append( "\nField: " ).append( "sessionMode" ).append( " : " );
        _builder.encodeByte( msg.getSessionMode().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "tradSesMode" ).append( " : " );
        _builder.encodeByte( msg.getTradSesMode().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 6 );
        _builder.end();
    }

    public final void encodeSessionLogonRequest( final ETISessionLogonRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_SessionLogonRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "SessionLogonRequest" ).append( "  eventType=" ).append( "ETISessionLogonRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "heartBtIntMS" ).append( " : " );
        _builder.encodeUInt( (int)msg.getHeartBtIntMS() );
        if ( _debug ) _dump.append( "\nField: " ).append( "partyIDSessionID" ).append( " : " );
        _builder.encodeUInt( (int)msg.getPartyIDSessionID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "defaultCstmApplVerID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getDefaultCstmApplVerID(), 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "password" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getPassword(), 32 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applUsageOrders" ).append( " : " );
        _builder.encodeByte( msg.getApplUsageOrders().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "applUsageQuotes" ).append( " : " );
        _builder.encodeByte( msg.getApplUsageQuotes().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderRoutingIndicator" ).append( " : " );
        _builder.encodeBool( msg.getOrderRoutingIndicator() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler4" ).append( " : " );
        _builder.encodeFiller( 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applicationSystemName" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getApplicationSystemName(), 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applicationSystemVer" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getApplicationSystemVer(), 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applicationSystemVendor" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getApplicationSystemVendor(), 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler5" ).append( " : " );
        _builder.encodeFiller( 3 );
        _builder.end();
    }

    public final void encodeSessionLogonResponse( final ETISessionLogonResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_SessionLogonResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "SessionLogonResponse" ).append( "  eventType=" ).append( "ETISessionLogonResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "throttleTimeIntervalMS" ).append( " : " );
        _builder.encodeLong( (long)msg.getThrottleTimeIntervalMS() );
        if ( _debug ) _dump.append( "\nField: " ).append( "throttleNoMsgs" ).append( " : " );
        _builder.encodeUInt( (int)msg.getThrottleNoMsgs() );
        if ( _debug ) _dump.append( "\nField: " ).append( "throttleDisconnectLimit" ).append( " : " );
        _builder.encodeUInt( (int)msg.getThrottleDisconnectLimit() );
        if ( _debug ) _dump.append( "\nField: " ).append( "heartBtIntMS" ).append( " : " );
        _builder.encodeUInt( (int)msg.getHeartBtIntMS() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sessionInstanceID" ).append( " : " );
        _builder.encodeUInt( (int)msg.getSessionInstanceID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "tradSesMode" ).append( " : " );
        _builder.encodeByte( msg.getTradSesMode().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "defaultCstmApplVerID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getDefaultCstmApplVerID(), 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        _builder.end();
    }

    public final void encodeSessionLogoutRequest( final ETISessionLogoutRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_SessionLogoutRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "SessionLogoutRequest" ).append( "  eventType=" ).append( "ETISessionLogoutRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        _builder.end();
    }

    public final void encodeSessionLogoutResponse( final ETISessionLogoutResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_SessionLogoutResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "SessionLogoutResponse" ).append( "  eventType=" ).append( "ETISessionLogoutResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeUserLogonRequest( final ETIUserLogonRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_UserLogonRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "UserLogonRequest" ).append( "  eventType=" ).append( "ETIUserLogonRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "userName" ).append( " : " );
        _builder.encodeUInt( (int)msg.getUserName() );
        if ( _debug ) _dump.append( "\nField: " ).append( "password" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getPassword(), 32 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeUserLogonResponse( final ETIUserLogonResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_UserLogonResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "UserLogonResponse" ).append( "  eventType=" ).append( "ETIUserLogonResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeUserLogoutRequest( final ETIUserLogoutRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_UserLogoutRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "UserLogoutRequest" ).append( "  eventType=" ).append( "ETIUserLogoutRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "userName" ).append( " : " );
        _builder.encodeUInt( (int)msg.getUserName() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeUserLogoutResponse( final ETIUserLogoutResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_UserLogoutResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "UserLogoutResponse" ).append( "  eventType=" ).append( "ETIUserLogoutResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeThrottleUpdateNotification( final ETIThrottleUpdateNotification msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ThrottleUpdateNotification );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ThrottleUpdateNotification" ).append( "  eventType=" ).append( "ETIThrottleUpdateNotification" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "throttleTimeIntervalMS" ).append( " : " );
        _builder.encodeLong( (long)msg.getThrottleTimeIntervalMS() );
        if ( _debug ) _dump.append( "\nField: " ).append( "throttleNoMsgs" ).append( " : " );
        _builder.encodeUInt( (int)msg.getThrottleNoMsgs() );
        if ( _debug ) _dump.append( "\nField: " ).append( "throttleDisconnectLimit" ).append( " : " );
        _builder.encodeUInt( (int)msg.getThrottleDisconnectLimit() );
        _builder.end();
    }

    public final void encodeSubscribe( final ETISubscribe msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Subscribe );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Subscribe" ).append( "  eventType=" ).append( "ETISubscribe" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "subscriptionScope" ).append( " : " );
        _builder.encodeUInt( (int)msg.getSubscriptionScope() );
        if ( _debug ) _dump.append( "\nField: " ).append( "refApplID" ).append( " : " );
        _builder.encodeByte( msg.getRefApplID().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 3 );
        _builder.end();
    }

    public final void encodeSubscribeResponse( final ETISubscribeResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_SubscribeResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "SubscribeResponse" ).append( "  eventType=" ).append( "ETISubscribeResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applSubID" ).append( " : " );
        _builder.encodeUInt( (int)msg.getApplSubID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeUnsubscribe( final ETIUnsubscribe msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Unsubscribe );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Unsubscribe" ).append( "  eventType=" ).append( "ETIUnsubscribe" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "refApplSubID" ).append( " : " );
        _builder.encodeUInt( (int)msg.getRefApplSubID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeUnsubscribeResponse( final ETIUnsubscribeResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_UnsubscribeResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "UnsubscribeResponse" ).append( "  eventType=" ).append( "ETIUnsubscribeResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeRetransmit( final ETIRetransmit msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Retransmit );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Retransmit" ).append( "  eventType=" ).append( "ETIRetransmit" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "applBegSeqNum" ).append( " : " );
        _builder.encodeULong( (long)msg.getApplBegSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "applEndSeqNum" ).append( " : " );
        _builder.encodeULong( (long)msg.getApplEndSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "subscriptionScope" ).append( " : " );
        _builder.encodeUInt( (int)msg.getSubscriptionScope() );
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _builder.encodeUShort( (short)msg.getPartitionID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "refApplID" ).append( " : " );
        _builder.encodeByte( msg.getRefApplID().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        _builder.end();
    }

    public final void encodeRetransmitResponse( final ETIRetransmitResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_RetransmitResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "RetransmitResponse" ).append( "  eventType=" ).append( "ETIRetransmitResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applEndSeqNum" ).append( " : " );
        _builder.encodeULong( (long)msg.getApplEndSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "refApplLastSeqNum" ).append( " : " );
        _builder.encodeULong( (long)msg.getRefApplLastSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "applTotalMessageCount" ).append( " : " );
        _builder.encodeUShort( (short)msg.getApplTotalMessageCount() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 6 );
        _builder.end();
    }

    public final void encodeRetransmitOrderEvents( final ETIRetransmitOrderEvents msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_RetransmitOrderEvents );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "RetransmitOrderEvents" ).append( "  eventType=" ).append( "ETIRetransmitOrderEvents" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "subscriptionScope" ).append( " : " );
        _builder.encodeUInt( (int)msg.getSubscriptionScope() );
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _builder.encodeUShort( (short)msg.getPartitionID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "refApplID" ).append( " : " );
        _builder.encodeByte( msg.getRefApplID().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "applBegMsgID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getApplBegMsgID(), 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applEndMsgID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getApplEndMsgID(), 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        _builder.end();
    }

    public final void encodeRetranOrderEventsResponse( final ETIRetransmitOrderEventsResponse msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_RetransmitOrderEventsResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "RetransmitOrderEventsResponse" ).append( "  eventType=" ).append( "ETIRetransmitOrderEventsResponse" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getRequestTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applTotalMessageCount" ).append( " : " );
        _builder.encodeUShort( (short)msg.getApplTotalMessageCount() );
        if ( _debug ) _dump.append( "\nField: " ).append( "applEndMsgID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getApplEndMsgID(), 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "refApplLastMsgID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getRefApplLastMsgID(), 16 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 6 );
        _builder.end();
    }

    public final void encodeHeartbeat( final Heartbeat msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Heartbeat );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Heartbeat" ).append( "  eventType=" ).append( "Heartbeat" ).append( " : " );
        }

        _builder.end();
    }

    public final void encodeHeartbeatNotification( final Heartbeat msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_HeartbeatNotification );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "HeartbeatNotification" ).append( "  eventType=" ).append( "Heartbeat" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "sendingTime" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeTimeUTC( now );
        _builder.end();
    }

    public final void encodeSessionLogoutNotification( final ETISessionLogoutNotification msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_SessionLogoutNotification );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "SessionLogoutNotification" ).append( "  eventType=" ).append( "ETISessionLogoutNotification" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "varTextLen" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeShort( msg.getReason().length() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 6 );
        if ( _debug ) _dump.append( "\nField: " ).append( "varText" ).append( " : " );
        _builder.encodeString( msg.getReason(), 128 );
        _builder.end();
    }

    public final void encodeOrderCancelRequest( final CancelRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_CancelOrderSingleRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "CancelOrderSingleRequest" ).append( "  eventType=" ).append( "CancelRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1b" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "marketSegmentID" ).append( " : " ).append( "encode" ).append( " : " );
        encodeMarketSegmentID( msg.getInstrument() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "simpleSecurityID" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSimpleSecurityId( msg.getInstrument() );
        if ( _debug ) _dump.append( "\nField: " ).append( "targetPartyIDSessionID" ).append( " : " );
        _builder.encodeUInt( Constants.UNSET_INT );    // targetPartyIDSessionID
        _builder.end();
    }

    public final void encodeUnsolOrderCancelled( final Cancelled msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_CancelOrderNotification );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "CancelOrderNotification" ).append( "  eventType=" ).append( "Cancelled" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeOut
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "applSubID" ).append( " : " );
        _builder.encodeUInt( Constants.UNSET_INT );    // applSubID
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // partitionID
        if ( _debug ) _dump.append( "\nHook : " ).append( "applMsgID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeStringFixedWidth( nextApplMsgID(), 16 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "applID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applResendFlag" ).append( " : " );
        _builder.encodeFiller( 1 );    // applResendFlag
        if ( _debug ) _dump.append( "\nHook : " ).append( "lastFragment" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 7 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getSecurityId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1b" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getCumQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // cxlQty
        if ( _debug ) _dump.append( "\nField: " ).append( "partyIDEnteringTrader" ).append( " : " );
        _builder.encodeUInt( Constants.UNSET_INT );    // partyIDEnteringTrader
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // execRestatementReason
        if ( _debug ) _dump.append( "\nField: " ).append( "partyIDEnteringFirm" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // partyIDEnteringFirm
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _builder.encodeByte( msg.getOrdStatus().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "productComplex" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 2 );
        _builder.end();
    }

    public final void encodeImmediateExecResponse( final TradeNew msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ImmediateExecResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ImmediateExecResponse" ).append( "  eventType=" ).append( "TradeNew" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeFiller( 8 );    // requestTime
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeIn
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeOut
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // partitionID
        if ( _debug ) _dump.append( "\nHook : " ).append( "applID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)4 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "applMsgID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeStringFixedWidth( nextApplMsgID(), 16 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "lastFragment" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeULong( Constants.UNSET_LONG );    // origClOrdId
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getSecurityId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "tranExecId" ).append( " : " );
        _builder.encodeULong( Constants.UNSET_LONG );    // tranExecId
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSEntryTime" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSEntryTime
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimePriority" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimePriority
        if ( _debug ) _dump.append( "\nField: " ).append( "marketSegmentID" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // marketSegmentID
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getCumQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // cxlQty
        if ( _debug ) _dump.append( "\nHook : " ).append( "noLegExecs" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUShort( (short)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // execRestatementReason
        if ( _debug ) _dump.append( "\nHook : " ).append( "productComplex" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _builder.encodeByte( msg.getOrdStatus().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "triggered" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // triggered
        if ( _debug ) _dump.append( "\nHook : " ).append( "noFills" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 7 );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillPx" ).append( " : " );
        _builder.encodeDecimal( msg.getLastPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getLastQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillMatchID" ).append( " : " );
        _builder.encodeStringAsInt( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillExecID" ).append( " : " );
        _builder.encodeUInt( Constants.UNSET_INT );    // fillExecID
        if ( _debug ) _dump.append( "\nField: " ).append( "fillLiquidityInd" ).append( " : " );
        final LiquidityInd tLiquidityIndBase = msg.getLiquidityInd();
        final byte tLiquidityInd = ( tLiquidityIndBase == null ) ?  DEFAULT_LiquidityInd : transformLiquidityInd( tLiquidityIndBase );
        _builder.encodeByte( tLiquidityInd );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillerFillsGrp" ).append( " : " );
        _builder.encodeFiller( 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "legSecurityID" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // legSecurityID
        if ( _debug ) _dump.append( "\nField: " ).append( "legLastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // legLastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "legLastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // legLastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "legExecID" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // legExecID
        if ( _debug ) _dump.append( "\nField: " ).append( "legSide" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // legSide
        if ( _debug ) _dump.append( "\nField: " ).append( "fillsIndex" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // fillsIndex
        if ( _debug ) _dump.append( "\nField: " ).append( "fillerLegExec" ).append( " : " );
        _builder.encodeFiller( 6 );
        _builder.end();
    }

    public final void encodeBookOrderExecution( final TradeNew msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_BookOrderExecution );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "BookOrderExecution" ).append( "  eventType=" ).append( "TradeNew" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeOut
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "applSubID" ).append( " : " );
        _builder.encodeUInt( Constants.UNSET_INT );    // applSubID
        if ( _debug ) _dump.append( "\nField: " ).append( "partitionID" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // partitionID
        if ( _debug ) _dump.append( "\nHook : " ).append( "applMsgID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeStringFixedWidth( nextApplMsgID(), 16 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "applID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "applResendFlag" ).append( " : " );
        _builder.encodeFiller( 1 );    // applResendFlag
        if ( _debug ) _dump.append( "\nHook : " ).append( "lastFragment" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 7 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderLocationID" ).append( " : " );
        _builder.encodeULong( Constants.UNSET_LONG );    // senderLocationID
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeULong( Constants.UNSET_LONG );    // origClOrdId
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getSecurityId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "tranExecId" ).append( " : " );
        _builder.encodeULong( Constants.UNSET_LONG );    // tranExecId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1b" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "marketSegmentID" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // marketSegmentID
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getCumQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // cxlQty
        if ( _debug ) _dump.append( "\nHook : " ).append( "noLegExecs" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUShort( (short)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // execRestatementReason
        if ( _debug ) _dump.append( "\nField: " ).append( "accountType" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // accountType
        if ( _debug ) _dump.append( "\nHook : " ).append( "productComplex" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _builder.encodeByte( msg.getOrdStatus().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "triggered" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // triggered
        if ( _debug ) _dump.append( "\nHook : " ).append( "noFills" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // side
        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.encodeFiller( 2 );    // account
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 39 );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillPx" ).append( " : " );
        _builder.encodeDecimal( msg.getLastPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getLastQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillMatchID" ).append( " : " );
        _builder.encodeStringAsInt( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillExecID" ).append( " : " );
        _builder.encodeUInt( Constants.UNSET_INT );    // fillExecID
        if ( _debug ) _dump.append( "\nField: " ).append( "fillLiquidityInd" ).append( " : " );
        final LiquidityInd tLiquidityIndBase = msg.getLiquidityInd();
        final byte tLiquidityInd = ( tLiquidityIndBase == null ) ?  DEFAULT_LiquidityInd : transformLiquidityInd( tLiquidityIndBase );
        _builder.encodeByte( tLiquidityInd );
        if ( _debug ) _dump.append( "\nField: " ).append( "fillerFillsGrp" ).append( " : " );
        _builder.encodeFiller( 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "legSecurityID" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // legSecurityID
        if ( _debug ) _dump.append( "\nField: " ).append( "legLastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // legLastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "legLastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // legLastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "legExecID" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // legExecID
        if ( _debug ) _dump.append( "\nField: " ).append( "legSide" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // legSide
        if ( _debug ) _dump.append( "\nField: " ).append( "fillsIndex" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // fillsIndex
        if ( _debug ) _dump.append( "\nField: " ).append( "fillerLegExec" ).append( " : " );
        _builder.encodeFiller( 6 );
        _builder.end();
    }

    public final void encodeReject( final SessionReject msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Reject );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Reject" ).append( "  eventType=" ).append( "SessionReject" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeFiller( 8 );    // requestTime
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getRefSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "sessionRejectReason" ).append( " : " );
        final SessionRejectReason tSessionRejectReasonBase = msg.getSessionRejectReason();
        final ViewString tSessionRejectReason = transformSessionRejectReason( msg.getSessionRejectReason() );
        if ( tSessionRejectReason != null ) _builder.encodeString( tSessionRejectReason );
        if ( _debug ) _dump.append( "\nHook : " ).append( "varTextLen" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeShort( msg.getText().length() );
        if ( _debug ) _dump.append( "\nField: " ).append( "sessionStatus" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // sessionStatus
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "varText" ).append( " : " );
        _builder.encodeString( msg.getText(), 128 );
        _builder.end();
    }

    public final void encodeNewOrderFastLimit( final NewOrderSingle msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_NewOrderRequestSimple );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "NewOrderRequestSimple" ).append( "  eventType=" ).append( "NewOrderSingle" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        _builder.encodeDecimal( msg.getPrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderLocationID" ).append( " : " );
        _builder.encodeULong( Constants.UNSET_LONG );    // senderLocationID
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getOrderQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1c" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "simpleSecurityID" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSimpleSecurityId( msg.getInstrument() );
        if ( _debug ) _dump.append( "\nField: " ).append( "accountType" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // accountType
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( transformSide( msg.getSide() ) );
        if ( _debug ) _dump.append( "\nHook : " ).append( "priceValidityCheckType" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        final TimeInForce tTimeInForceBase = msg.getTimeInForce();
        final byte tTimeInForce = ( tTimeInForceBase == null ) ?  DEFAULT_TimeInForce : transformTimeInForce( tTimeInForceBase );
        _builder.encodeByte( tTimeInForce );
        if ( _debug ) _dump.append( "\nField: " ).append( "execInst" ).append( " : " );
        final ExecInst tExecInstBase = msg.getExecInst();
        final byte tExecInst = ( tExecInstBase == null ) ?  DEFAULT_ExecInst : transformExecInst( tExecInstBase );
        _builder.encodeByte( tExecInst );
        if ( _debug ) _dump.append( "\nField: " ).append( "uniqueClientCode" ).append( " : " );
        _builder.encodeFiller( 12 );    // uniqueClientCode
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 3 );
        _builder.end();
    }

    public final void encodeOrderReplaceFastLimitRequest( final CancelReplaceRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ReplaceOrderSingleShortRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ReplaceOrderSingleShortRequest" ).append( "  eventType=" ).append( "CancelReplaceRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "senderSubID" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeUInt( _senderSubID ); // senderSubID;
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        _builder.encodeDecimal( msg.getPrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "senderLocationID" ).append( " : " );
        _builder.encodeULong( Constants.UNSET_LONG );    // senderLocationID
        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getOrderQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1c" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "simpleSecurityID" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSimpleSecurityId( msg.getInstrument() );
        if ( _debug ) _dump.append( "\nField: " ).append( "accountType" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // accountType
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( transformSide( msg.getSide() ) );
        if ( _debug ) _dump.append( "\nHook : " ).append( "priceValidityCheckType" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        final TimeInForce tTimeInForceBase = msg.getTimeInForce();
        final byte tTimeInForce = ( tTimeInForceBase == null ) ?  DEFAULT_TimeInForce : transformTimeInForce( tTimeInForceBase );
        _builder.encodeByte( tTimeInForce );
        if ( _debug ) _dump.append( "\nField: " ).append( "execInst" ).append( " : " );
        final ExecInst tExecInstBase = msg.getExecInst();
        final byte tExecInst = ( tExecInstBase == null ) ?  DEFAULT_ExecInst : transformExecInst( tExecInstBase );
        _builder.encodeByte( tExecInst );
        if ( _debug ) _dump.append( "\nField: " ).append( "uniqueClientCode" ).append( " : " );
        _builder.encodeFiller( 12 );    // uniqueClientCode
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 3 );
        _builder.end();
    }

    public final void encodeOrderAckLean( final NewOrderAck msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_NewOrderLeanResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "NewOrderLeanResponse" ).append( "  eventType=" ).append( "NewOrderAck" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeFiller( 8 );    // requestTime
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeIn
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeOut
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "lastFragment" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getSecurityId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "priceMkToLimitPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // priceMkToLimitPx
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _builder.encodeByte( msg.getOrdStatus().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // execRestatementReason
        if ( _debug ) _dump.append( "\nHook : " ).append( "productComplex" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 3 );
        _builder.end();
    }

    public final void encodeReplaceResponseLean( final Replaced msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ReplaceOrderLeanResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ReplaceOrderLeanResponse" ).append( "  eventType=" ).append( "Replaced" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeFiller( 8 );    // requestTime
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeIn
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeOut
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "lastFragment" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getSecurityId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "priceMkToLimitPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // priceMkToLimitPx
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getCumQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // cxlQty
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _builder.encodeByte( msg.getOrdStatus().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // execRestatementReason
        if ( _debug ) _dump.append( "\nHook : " ).append( "productComplex" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 7 );
        _builder.end();
    }

    public final void encodeOrderReplaceCancelledLean( final Cancelled msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ReplaceOrderLeanResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ReplaceOrderLeanResponse" ).append( "  eventType=" ).append( "Cancelled" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeFiller( 8 );    // requestTime
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeIn
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeOut
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "lastFragment" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getSecurityId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "priceMkToLimitPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // priceMkToLimitPx
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getCumQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // cxlQty
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _builder.encodeByte( msg.getOrdStatus().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // execRestatementReason
        if ( _debug ) _dump.append( "\nHook : " ).append( "productComplex" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 7 );
        _builder.end();
    }

    public final void encodeOrderCancelledLean( final Cancelled msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_CancelOrderLeanResponse );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "CancelOrderLeanResponse" ).append( "  eventType=" ).append( "Cancelled" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "requestTime" ).append( " : " );
        _builder.encodeFiller( 8 );    // requestTime
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeIn" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeIn
        if ( _debug ) _dump.append( "\nField: " ).append( "trdRegTSTimeOut" ).append( " : " );
        _builder.encodeFiller( 8 );    // trdRegTSTimeOut
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeUInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "lastFragment" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "securityId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getSecurityId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cumQty" ).append( " : " );
        _builder.encodeQty( (int)msg.getCumQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "cxlQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // cxlQty
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        _builder.encodeByte( msg.getOrdStatus().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRestatementReason" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // execRestatementReason
        if ( _debug ) _dump.append( "\nHook : " ).append( "productComplex" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 3 );
        _builder.end();
    }
    @Override
    public final byte[] getBytes() {
        return _buf;
    }

    @Override
    public final void setTimeZoneCalculator( final TimeZoneCalculator calc ) {
        _tzCalculator = calc;
        _builder.setTimeZoneCalculator( calc );
    }

    private byte transformSide( Side val ) {
        switch( val ) {
        case Buy:  // Buy
            return (byte)0x01;
        case Sell:  // Sell
            return (byte)0x02;
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on Side for value " + val );
    }

    private byte transformLiquidityInd( LiquidityInd val ) {
        switch( val ) {
        case AddedLiquidity:  // Added Liquidity
            return (byte)0x01;
        case RemovedLiquidity:  // Removed Liquidity
            return (byte)0x02;
        default:
            break;
        }
        return 2;
    }

    private byte transformOrdType( OrdType val ) {
        switch( val ) {
        case Market:  // Market
            return (byte)0x05;
        case Limit:  // Limit
            return (byte)0x02;
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on OrdType for value " + val );
    }

    private byte transformTimeInForce( TimeInForce val ) {
        switch( val ) {
        case Day:  // Day
            return (byte)0x00;
        case GoodTillCancel:  // GTC
            return (byte)0x01;
        case ImmediateOrCancel:  // IOC
            return (byte)0x03;
        case GoodTillDate:  // GTD
            return (byte)0x06;
        default:
            break;
        }
        return 0;
    }

    private byte transformOrderCapacity( OrderCapacity val ) {
        switch( val ) {
        case AgentForOtherMember:  // Customer (Agency)
            return (byte)0x01;
        case Principal:  // Principle/Prop
            return (byte)0x05;
        case Proprietary:  // Market maker
            return (byte)0x06;
        default:
            break;
        }
        return 5;
    }

    private byte transformExecInst( ExecInst val ) {
        switch( val ) {
        case ReinstateOnSysFail:  // Persistent Order
            return (byte)0x01;
        case CancelOnSysFail:  // Non persistent order
            return (byte)0x02;
        default:
            break;
        }
        return 2;
    }

    private static final Map<SessionRejectReason, ViewString> _sessionRejectReasonMap = new HashMap<SessionRejectReason, ViewString>( 60 );
    private static final ViewString _sessionRejectReasonDefault = new ViewString( "99" );

    static {
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "1".getBytes() ), StringFactory.hexToViewString( "0x01" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "5".getBytes() ), StringFactory.hexToViewString( "0x05" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "7".getBytes() ), StringFactory.hexToViewString( "0x07" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "11".getBytes() ), StringFactory.hexToViewString( "0x0B" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "16".getBytes() ), StringFactory.hexToViewString( "0x10" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "99".getBytes() ), StringFactory.hexToViewString( "0x63" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "20".getBytes() ), StringFactory.hexToViewString( "0x64" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "21".getBytes() ), StringFactory.hexToViewString( "0x65" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "22".getBytes() ), StringFactory.hexToViewString( "0x66" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "23".getBytes() ), StringFactory.hexToViewString( "0x67" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "24".getBytes() ), StringFactory.hexToViewString( "0x68" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "25".getBytes() ), StringFactory.hexToViewString( "0x69" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "26".getBytes() ), StringFactory.hexToViewString( "0xC8" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "27".getBytes() ), StringFactory.hexToViewString( "0x1027" ) );
         _sessionRejectReasonMap.put( SessionRejectReason.getVal( "28".getBytes() ), StringFactory.hexToViewString( "0x1127" ) );
    }

    private ViewString transformSessionRejectReason( SessionRejectReason intVal ) {
        ViewString extVal = _sessionRejectReasonMap.get( intVal );
        if ( extVal == null ) {
            return _sessionRejectReasonDefault;
        }
        return extVal;
    }

    /**     * PostPend  Common Encoder File     *     * expected to contain methods used in hooks from model     */         @Override    public void setNanoStats( boolean nanoTiming ) {        _nanoStats = nanoTiming;    }    private       boolean         _nanoStats    =  true;             private       int             _idx          = 1;        private final ClientCancelRejectFactory _canRejFactory   = SuperpoolManager.instance().getFactory( ClientCancelRejectFactory.class, ClientCancelRejectImpl.class );    private final ClientRejectedFactory     _rejectedFactory = SuperpoolManager.instance().getFactory( ClientRejectedFactory.class,     ClientRejectedImpl.class );     public static final ZString ENCODE_REJ              = new ViewString( "ERJ" );    public static final ZString NONE                    = new ViewString( "NON" );    @Override    public Message unableToSend( Message msg, ZString errMsg ) {        switch( msg.getReusableType().getSubId() ) {        case EventIds.ID_NEWORDERSINGLE:            return rejectNewOrderSingle( (NewOrderSingle) msg, errMsg );        case EventIds.ID_NEWORDERACK:            break;        case EventIds.ID_TRADENEW:            break;        case EventIds.ID_CANCELREPLACEREQUEST:            return rejectCancelReplaceRequest( (CancelReplaceRequest) msg, errMsg );        case EventIds.ID_CANCELREQUEST:            return rejectCancelRequest( (CancelRequest) msg, errMsg );        }                return null;    }    private Message rejectNewOrderSingle( NewOrderSingle nos, ZString errMsg ) {        final ClientRejectedImpl reject = _rejectedFactory.get();        reject.setSrcEvent( nos );        reject.getExecIdForUpdate().copy( ENCODE_REJ ).append( nos.getClOrdId() ).append( ++_idx );        reject.getOrderIdForUpdate().setValue( NONE );        reject.setOrdRejReason( OrdRejReason.Other );        reject.getTextForUpdate().setValue( errMsg );        reject.setOrdStatus( OrdStatus.Rejected );        reject.setExecType( ExecType.Rejected );        reject.setCumQty( 0 );        reject.setAvgPx( 0.0 );        reject.setMessageHandler( nos.getMessageHandler() );        return reject;    }    private Message rejectCancelReplaceRequest( CancelReplaceRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelReplace );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private Message rejectCancelRequest( CancelRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelRequest );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private static final byte[] STATS       = "     [".getBytes();    private static final byte   STAT_DELIM  = ',';    private static final byte   STAT_END    = ']';    @Override    public void addStats( final ReusableString outBuf, final Message msg, final long msgSent ) {                if ( msg.getReusableType().getId() == FullEventIds.ID_MARKET_NEWORDERSINGLE ) {            final MarketNewOrderSingleImpl nos = (MarketNewOrderSingleImpl) msg;            nos.setOrderSent( msgSent );                } else if ( msg.getReusableType().getId() == FullEventIds.ID_CLIENT_NEWORDERACK ) {            final ClientNewOrderAckImpl ack = (ClientNewOrderAckImpl) msg;            final long orderIn  = ack.getOrderReceived();            final long orderOut = ack.getOrderSent();            final long ackIn    = ack.getAckReceived();            final long ackOut   = msgSent;            final long microNOSToMKt    = (orderOut - orderIn)  >> 10;            final long microInMkt       = (ackIn    - orderOut) >> 10;            final long microAckToClient = (ackOut   - ackIn)    >> 10;                        outBuf.append( STATS      ).append( microNOSToMKt )                  .append( STAT_DELIM ).append( microInMkt )                  .append( STAT_DELIM ).append( microAckToClient ).append( STAT_END );        }    }    private int _senderSubID = 0;    private long _locationId = 0;    private final ReusableString _uniqueClientCode = new ReusableString("1");    @Override        public void setUniqueClientCode( ZString uniqueClientCode ) {        _uniqueClientCode.copy( uniqueClientCode );    }    @Override        public void setLocationId( long locationId ) {        _locationId = locationId;    }     @Override        public void setSenderSubID( int newId ) {        _senderSubID = newId;    }         @Override        public void setExchangeEmulationOn() {        if ( _builder instanceof DebugBinaryEncodeBuilder ) {            @SuppressWarnings( "unchecked" )            ETIEncodeBuilderImpl b = ((DebugBinaryEncodeBuilder<ETIEncodeBuilderImpl>) _builder).getBuilder();                        b.setHeaderPad( 2 );        } else {            ((ETIEncodeBuilderImpl)_builder).setHeaderPad( 2 );        }    }    private void encodeSimpleSecurityId( Instrument instrument ) {        _builder.encodeInt( (int)instrument.getLongSymbol() );    }        protected void encodeMarketSegmentID( Instrument instrument ) {        _builder.encodeInt( instrument.getIntSegment() );    }    // ApplID encoding only used by exchange simulator         private long _nextApplMsgId = 0;    private ReusableString _nextApplMsgIdStr = new ReusableString(16);        private ZString nextApplMsgID() {        byte[] bytes = _nextApplMsgIdStr.getBytes();        com.rr.core.codec.binary.BinaryBigEndianEncoderUtils.encodeLong( bytes, 0, 0 );        com.rr.core.codec.binary.BinaryBigEndianEncoderUtils.encodeLong( bytes, 8, ++_nextApplMsgId );        return _nextApplMsgIdStr;    }}
