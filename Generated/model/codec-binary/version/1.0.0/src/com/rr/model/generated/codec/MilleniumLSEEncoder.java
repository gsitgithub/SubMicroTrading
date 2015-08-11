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
import com.rr.codec.emea.exchange.millenium.MilleniumEncodeBuilderImpl;
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

public final class MilleniumLSEEncoder implements BinaryEncoder {

   // Member Vars
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

    private static final byte      DEFAULT_LiquidityInd = 0x00;
    private static final byte      DEFAULT_OrderCapacity = (byte)'2';

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
    public MilleniumLSEEncoder( byte[] buf, int offset ) {
        if ( buf.length < SizeType.MIN_ENCODE_BUFFER.getSize() ) {
            throw new RuntimeException( "Encode buffer too small only " + buf.length + ", min=" + SizeType.MIN_ENCODE_BUFFER.getSize() );
        }
        _buf = buf;
        _offset = offset;
        _binaryVersion   = new ViewString( "2");
        setBuilder();
    }


   // encode methods

    @Override
    public final void encode( final Message msg ) {
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_MILLENIUMLOGON:
            encodeLogon( (MilleniumLogon) msg );
            break;
        case EventIds.ID_MILLENIUMLOGONREPLY:
            encodeLogonReply( (MilleniumLogonReply) msg );
            break;
        case EventIds.ID_MILLENIUMLOGOUT:
            encodeLogout( (MilleniumLogout) msg );
            break;
        case EventIds.ID_MILLENIUMMISSEDMESSAGEREQUEST:
            encodeMissedMessageRequest( (MilleniumMissedMessageRequest) msg );
            break;
        case EventIds.ID_MILLENIUMMISSEDMSGREQUESTACK:
            encodeMissedMsgRequestAck( (MilleniumMissedMsgRequestAck) msg );
            break;
        case EventIds.ID_MILLENIUMMISSEDMSGREPORT:
            encodeMissedMsgReport( (MilleniumMissedMsgReport) msg );
            break;
        case EventIds.ID_HEARTBEAT:
            encodeHeartbeat( (Heartbeat) msg );
            break;
        case EventIds.ID_NEWORDERSINGLE:
            encodeNewOrder( (NewOrderSingle) msg );
            break;
        case EventIds.ID_CANCELREQUEST:
            encodeOrderCancelRequest( (CancelRequest) msg );
            break;
        case EventIds.ID_CANCELREJECT:
            encodeCancelReject( (CancelReject) msg );
            break;
        case EventIds.ID_SESSIONREJECT:
            encodeReject( (SessionReject) msg );
            break;
        case EventIds.ID_VAGUEORDERREJECT:
            encodeBusinessReject( (VagueOrderReject) msg );
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            encodeOrderReplaceRequest( (CancelReplaceRequest) msg );
            break;
        case EventIds.ID_NEWORDERACK:
            encodeOrderAck( (NewOrderAck) msg );
            break;
        case EventIds.ID_CANCELLED:
            encodeOrderCancelled( (Cancelled) msg );
            break;
        case EventIds.ID_REPLACED:
            encodeOrderReplaced( (Replaced) msg );
            break;
        case EventIds.ID_REJECTED:
            encodeOrderRejected( (Rejected) msg );
            break;
        case EventIds.ID_EXPIRED:
            encodeOrderExpired( (Expired) msg );
            break;
        case EventIds.ID_RESTATED:
            encodeOrderRestated( (Restated) msg );
            break;
        case EventIds.ID_TRADENEW:
            encodeTrade( (TradeNew) msg );
            break;
        case EventIds.ID_TRADECORRECT:
            encodeTradeCorrect( (TradeCorrect) msg );
            break;
        case EventIds.ID_TRADECANCEL:
            encodeTradeCancel( (TradeCancel) msg );
            break;
        case EventIds.ID_SUSPENDED:
            encodeSuspended( (Suspended) msg );
            break;
        case 4:
        case 12:
        case 13:
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
        case 38:
        case 39:
        case 40:
        case 41:
        case 42:
        case 43:
        case 44:
        case 45:
        case 46:
        case 47:
        case 48:
        case 49:
        case 50:
        case 51:
        case 52:
        case 53:
        case 54:
        case 55:
        case 56:
        case 57:
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
        _builder = (_debug) ? new DebugBinaryEncodeBuilder<com.rr.codec.emea.exchange.millenium.MilleniumEncodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.millenium.MilleniumEncodeBuilderImpl( _buf, _offset, _binaryVersion ) )
                            : new com.rr.codec.emea.exchange.millenium.MilleniumEncodeBuilderImpl( _buf, _offset, _binaryVersion );
    }


    public final void encodeLogon( final MilleniumLogon msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Logon );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Logon" ).append( "  eventType=" ).append( "MilleniumLogon" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "userName" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getUserName(), 25 );
        if ( _debug ) _dump.append( "\nField: " ).append( "password" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getPassword(), 25 );
        if ( _debug ) _dump.append( "\nField: " ).append( "newPassword" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getNewPassword(), 25 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "msgVersion" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)'1' );
        _builder.end();
    }

    public final void encodeLogonReply( final MilleniumLogonReply msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_LogonReply );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "LogonReply" ).append( "  eventType=" ).append( "MilleniumLogonReply" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( (int)msg.getRejectCode() );
        if ( _debug ) _dump.append( "\nField: " ).append( "pwdExpiryDayCount" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getPwdExpiryDayCount(), 30 );
        _builder.end();
    }

    public final void encodeLogout( final MilleniumLogout msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Logout );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Logout" ).append( "  eventType=" ).append( "MilleniumLogout" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "reason" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getReason(), 20 );
        _builder.end();
    }

    public final void encodeMissedMessageRequest( final MilleniumMissedMessageRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_MissedMessageRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MissedMessageRequest" ).append( "  eventType=" ).append( "MilleniumMissedMessageRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "appId" ).append( " : " );
        _builder.encodeUByte( msg.getAppId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastMsgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastMsgSeqNum() );
        _builder.end();
    }

    public final void encodeMissedMsgRequestAck( final MilleniumMissedMsgRequestAck msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_MissedMsgRequestAck );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MissedMsgRequestAck" ).append( "  eventType=" ).append( "MilleniumMissedMsgRequestAck" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "missedMsgReqAckType" ).append( " : " );
        _builder.encodeByte( msg.getMissedMsgReqAckType().getVal() );
        _builder.end();
    }

    public final void encodeMissedMsgReport( final MilleniumMissedMsgReport msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_MissedMsgReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MissedMsgReport" ).append( "  eventType=" ).append( "MilleniumMissedMsgReport" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "missedMsgReportType" ).append( " : " );
        _builder.encodeByte( msg.getMissedMsgReportType().getVal() );
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

    public final void encodeNewOrder( final NewOrderSingle msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_NewOrder );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "NewOrder" ).append( "  eventType=" ).append( "NewOrderSingle" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 11 );
        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getAccount(), 10 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "clearingAccount" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (msg.getOrderCapacity() == OrderCapacity.AgentForOtherMember) ? (byte)1 : (byte)3 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "instrumentId" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSymbol( msg.getInstrument() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "mesQualifier" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordType" ).append( " : " );
        _builder.encodeByte( msg.getOrdType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        final TimeInForce tTimeInForce = msg.getTimeInForce();
        final byte tTimeInForceBytes = ( tTimeInForce != null ) ? tTimeInForce.getVal() : 0x00;
        _builder.encodeByte( tTimeInForceBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "expireDateTime" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // expireDateTime
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( msg.getSide().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getOrderQty() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "displayQty" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeInt( -1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        _builder.encodeDecimal( msg.getPrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderCapacity" ).append( " : " );
        final OrderCapacity tOrderCapacityBase = msg.getOrderCapacity();
        final byte tOrderCapacity = ( tOrderCapacityBase == null ) ?  DEFAULT_OrderCapacity : transformOrderCapacity( tOrderCapacityBase );
        _builder.encodeByte( tOrderCapacity );
        if ( _debug ) _dump.append( "\nHook : " ).append( "autoCancel" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)1 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "ordSubType" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "anonymity" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "stopPrice" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeOrderCancelRequest( final CancelRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_OrderCancelRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "OrderCancelRequest" ).append( "  eventType=" ).append( "CancelRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrigClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( msg.getSide().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeCancelReject( final CancelReject msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_CancelReject );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "CancelReject" ).append( "  eventType=" ).append( "CancelReject" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 8 );
        _builder.end();
    }

    public final void encodeReject( final SessionReject msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Reject );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Reject" ).append( "  eventType=" ).append( "SessionReject" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectReason" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getText(), 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectMsgType" ).append( " : " );
        _builder.encodeString( msg.getRefMsgType(), 1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeFiller( 20 );    // clOrdId
        _builder.end();
    }

    public final void encodeBusinessReject( final VagueOrderReject msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_BusinessReject );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "BusinessReject" ).append( "  eventType=" ).append( "VagueOrderReject" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeStringAsInt( msg.getText() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeOrderReplaceRequest( final CancelReplaceRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_OrderReplaceRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "OrderReplaceRequest" ).append( "  eventType=" ).append( "CancelReplaceRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrigClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "instrumentId" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSymbol( msg.getInstrument() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "expireDateTime" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // expireDateTime
        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getOrderQty() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "displayQty" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeInt( -1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        _builder.encodeDecimal( msg.getPrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getAccount(), 10 );
        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        final TimeInForce tTimeInForce = msg.getTimeInForce();
        final byte tTimeInForceBytes = ( tTimeInForce != null ) ? tTimeInForce.getVal() : 0x00;
        _builder.encodeByte( tTimeInForceBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( msg.getSide().getVal() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "stopPrice" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeOrderAck( final NewOrderAck msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "NewOrderAck" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeFiller( 12 );    // execRefID
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // lastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // lastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // liquidityInd
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeOrderCancelled( final Cancelled msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "Cancelled" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeFiller( 12 );    // execRefID
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // lastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // lastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // liquidityInd
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeOrderReplaced( final Replaced msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "Replaced" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeFiller( 12 );    // execRefID
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // lastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // lastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // liquidityInd
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeOrderRejected( final Rejected msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "Rejected" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeFiller( 12 );    // execRefID
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // lastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // lastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // liquidityInd
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeOrderExpired( final Expired msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "Expired" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeFiller( 12 );    // execRefID
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // lastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // lastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // liquidityInd
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeOrderRestated( final Restated msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "Restated" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeFiller( 12 );    // execRefID
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // lastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // lastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // liquidityInd
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeTrade( final TradeNew msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "TradeNew" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeFiller( 12 );    // execRefID
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodeDecimal( msg.getLastPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        final LiquidityInd tLiquidityIndBase = msg.getLiquidityInd();
        final byte tLiquidityInd = ( tLiquidityIndBase == null ) ? Constants.UNSET_BYTE : transformLiquidityInd( tLiquidityIndBase );
        _builder.encodeByte( tLiquidityInd );
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeTradeCorrect( final TradeCorrect msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "TradeCorrect" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecRefID(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodeDecimal( msg.getLastPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        final LiquidityInd tLiquidityIndBase = msg.getLiquidityInd();
        final byte tLiquidityInd = ( tLiquidityIndBase == null ) ? Constants.UNSET_BYTE : transformLiquidityInd( tLiquidityIndBase );
        _builder.encodeByte( tLiquidityInd );
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeTradeCancel( final TradeCancel msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "TradeCancel" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecRefID(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodeDecimal( msg.getLastPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        final LiquidityInd tLiquidityIndBase = msg.getLiquidityInd();
        final byte tLiquidityInd = ( tLiquidityIndBase == null ) ? Constants.UNSET_BYTE : transformLiquidityInd( tLiquidityIndBase );
        _builder.encodeByte( tLiquidityInd );
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
        _builder.end();
    }

    public final void encodeSuspended( final Suspended msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ExecutionReport );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ExecutionReport" ).append( "  eventType=" ).append( "Suspended" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nHook : " ).append( "appId" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)0 );
        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getExecId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getClOrdId(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getOrderId(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "execType" ).append( " : " );
        _builder.encodeByte( msg.getExecType().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execRefID" ).append( " : " );
        _builder.encodeFiller( 12 );    // execRefID
        if ( _debug ) _dump.append( "\nField: " ).append( "ordStatus" ).append( " : " );
        final OrdStatus tOrdStatus = msg.getOrdStatus();
        final byte tOrdStatusBytes = ( tOrdStatus != null ) ? tOrdStatus.getVal() : 0x00;
        _builder.encodeByte( tOrdStatusBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // rejectCode
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // lastPx
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // lastQty
        if ( _debug ) _dump.append( "\nField: " ).append( "leavesQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLeavesQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "container" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // container
        if ( _debug ) _dump.append( "\nField: " ).append( "displayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // displayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // instrumentId
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        final Side tSide = msg.getSide();
        final byte tSideBytes = ( tSide != null ) ? tSide.getVal() : 0x00;
        _builder.encodeByte( tSideBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "counterparty" ).append( " : " );
        _builder.encodeFiller( 11 );    // counterparty
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // liquidityInd
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeMatchId" ).append( " : " );
        _builder.encodeLong( Constants.UNSET_LONG );    // tradeMatchId
        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 10 );
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

    private byte transformLiquidityInd( LiquidityInd val ) {
        switch( val ) {
        case AddedLiquidity:  // AddedLiquidity
            return (byte)'A';
        case RemovedLiquidity:  // RemovedLiquidity
            return (byte)'R';
        case Auction:  // Opening Trade or Trade Created by MO
            return (byte)'C';
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on LiquidityInd for value " + val );
    }

    private byte transformOrderCapacity( OrderCapacity val ) {
        switch( val ) {
        case RisklessPrincipal:  // Riskless Principle
            return (byte)'1';
        case Principal:  // Principal/House
            return (byte)'2';
        case AgentForOtherMember:  // AgentForOtherMember/RelatedParty
            return (byte)'3';
        default:
            break;
        }
        return '2';
    }

    /**     * PostPend  Common Encoder File     *     * expected to contain methods used in hooks from model     */         @Override    public void setNanoStats( boolean nanoTiming ) {        _nanoStats = nanoTiming;    }    private       boolean         _nanoStats    =  true;             private       int             _idx          = 1;        private final ClientCancelRejectFactory _canRejFactory   = SuperpoolManager.instance().getFactory( ClientCancelRejectFactory.class, ClientCancelRejectImpl.class );    private final ClientRejectedFactory     _rejectedFactory = SuperpoolManager.instance().getFactory( ClientRejectedFactory.class,     ClientRejectedImpl.class );     public static final ZString ENCODE_REJ              = new ViewString( "ERJ" );    public static final ZString NONE                    = new ViewString( "NON" );    @Override    public Message unableToSend( Message msg, ZString errMsg ) {        switch( msg.getReusableType().getSubId() ) {        case EventIds.ID_NEWORDERSINGLE:            return rejectNewOrderSingle( (NewOrderSingle) msg, errMsg );        case EventIds.ID_NEWORDERACK:            break;        case EventIds.ID_TRADENEW:            break;        case EventIds.ID_CANCELREPLACEREQUEST:            return rejectCancelReplaceRequest( (CancelReplaceRequest) msg, errMsg );        case EventIds.ID_CANCELREQUEST:            return rejectCancelRequest( (CancelRequest) msg, errMsg );        }                return null;    }    private Message rejectNewOrderSingle( NewOrderSingle nos, ZString errMsg ) {        final ClientRejectedImpl reject = _rejectedFactory.get();        reject.setSrcEvent( nos );        reject.getExecIdForUpdate().copy( ENCODE_REJ ).append( nos.getClOrdId() ).append( ++_idx );        reject.getOrderIdForUpdate().setValue( NONE );        reject.setOrdRejReason( OrdRejReason.Other );        reject.getTextForUpdate().setValue( errMsg );        reject.setOrdStatus( OrdStatus.Rejected );        reject.setExecType( ExecType.Rejected );        reject.setCumQty( 0 );        reject.setAvgPx( 0.0 );        reject.setMessageHandler( nos.getMessageHandler() );        return reject;    }    private Message rejectCancelReplaceRequest( CancelReplaceRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelReplace );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private Message rejectCancelRequest( CancelRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelRequest );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private static final byte[] STATS       = "     [".getBytes();    private static final byte   STAT_DELIM  = ',';    private static final byte   STAT_END    = ']';    @Override    public void addStats( final ReusableString outBuf, final Message msg, final long msgSent ) {                if ( msg.getReusableType().getId() == FullEventIds.ID_MARKET_NEWORDERSINGLE ) {            final MarketNewOrderSingleImpl nos = (MarketNewOrderSingleImpl) msg;            nos.setOrderSent( msgSent );                } else if ( msg.getReusableType().getId() == FullEventIds.ID_CLIENT_NEWORDERACK ) {            final ClientNewOrderAckImpl ack = (ClientNewOrderAckImpl) msg;            final long orderIn  = ack.getOrderReceived();            final long orderOut = ack.getOrderSent();            final long ackIn    = ack.getAckReceived();            final long ackOut   = msgSent;            final long microNOSToMKt    = (orderOut - orderIn)  >> 10;            final long microInMkt       = (ackIn    - orderOut) >> 10;            final long microAckToClient = (ackOut   - ackIn)    >> 10;                        outBuf.append( STATS      ).append( microNOSToMKt )                  .append( STAT_DELIM ).append( microInMkt )                  .append( STAT_DELIM ).append( microAckToClient ).append( STAT_END );        }    }    private void encodeSymbol( Instrument instrument ) {        _builder.encodeInt( (int)instrument.getLongSymbol() );    }    }
