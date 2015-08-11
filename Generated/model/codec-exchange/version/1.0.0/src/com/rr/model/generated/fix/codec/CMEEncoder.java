/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.fix.codec;

import java.util.HashMap;
import java.util.Map;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.model.*;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.pool.SuperPool;
import com.rr.core.codec.FixEncoder;
import com.rr.core.codec.FixEncodeBuilderImpl;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.model.internal.type.*;
import com.rr.model.generated.fix.model.defn.FixDictionary42;
import com.rr.model.generated.internal.events.factory.*;
import com.rr.model.generated.internal.events.impl.*;
import com.rr.model.generated.internal.events.interfaces.*;
import com.rr.model.generated.internal.type.*;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.core.FullEventIds;

@SuppressWarnings( "unused" )

public final class CMEEncoder implements FixEncoder {

   // Member Vars
    private static final byte      MSG_NewOrderSingle = (byte)'D';
    private static final byte      MSG_CancelReplaceRequest = (byte)'G';
    private static final byte      MSG_CancelRequest = (byte)'F';
    private static final byte      MSG_CancelReject = (byte)'9';
    private static final byte      MSG_NewOrderAck = (byte)'8';
    private static final byte      MSG_Trade = (byte)'8';
    private static final byte      MSG_Rejected = (byte)'8';
    private static final byte      MSG_Cancelled = (byte)'8';
    private static final byte      MSG_Replaced = (byte)'8';
    private static final byte      MSG_DoneForDay = (byte)'8';
    private static final byte      MSG_Stopped = (byte)'8';
    private static final byte      MSG_Expired = (byte)'8';
    private static final byte      MSG_Suspended = (byte)'8';
    private static final byte      MSG_Restated = (byte)'8';
    private static final byte      MSG_TradeCorrect = (byte)'8';
    private static final byte      MSG_TradeCancel = (byte)'8';
    private static final byte      MSG_OrderStatus = (byte)'8';
    private static final byte      MSG_PendingCancel = (byte)'8';
    private static final byte      MSG_PendingNew = (byte)'8';
    private static final byte      MSG_PendingReplace = (byte)'8';
    private static final byte      MSG_Calculated = (byte)'8';
    private static final byte      MSG_Heartbeat = (byte)'0';
    private static final byte      MSG_Logon = (byte)'A';
    private static final byte      MSG_Logout = (byte)'5';
    private static final byte      MSG_SessionReject = (byte)'3';
    private static final byte      MSG_ResendRequest = (byte)'2';
    private static final byte      MSG_SequenceReset = (byte)'4';
    private static final byte      MSG_TestRequest = (byte)'1';
    private static final byte      MSG_PartialFill = (byte)'8';
    private static final byte      MSG_Fill = (byte)'8';

    private static final byte      DEFAULT_TimeInForce = (byte)'0';
    private final byte[]               _buf;
    private final byte                 _majorVersion;
    private final byte                 _minorVersion;
    private final com.rr.core.codec.FixEncodeBuilderImpl _builder;

    private final ZString              _fixVersion;
    private       TimeZoneCalculator   _tzCalculator = new TimeZoneCalculator();
    private       SingleByteLookup     _sv;
    private       TwoByteLookup        _tv;
    private       MultiByteLookup      _mv;

   // Constructors
    public CMEEncoder( byte[] buf, int offset ) {
        this( FixVersion.Fix4_2._major, FixVersion.Fix4_2._minor, buf, offset );
    }

    public CMEEncoder( byte major, byte minor, byte[] buf, int offset ) {
        if ( buf.length < SizeType.MIN_ENCODE_BUFFER.getSize() ) {
            throw new RuntimeException( "Encode buffer too small only " + buf.length + ", min=" + SizeType.MIN_ENCODE_BUFFER.getSize() );
        }
        _buf = buf;
        _majorVersion = major;
        _minorVersion = minor;
        _builder = new com.rr.core.codec.FixEncodeBuilderImpl( buf, offset, major, minor );
        _fixVersion   = new ViewString( "FIX." + major + "." + minor );
    }

    public CMEEncoder( byte major, byte minor, byte[] buf ) {
        this( major, minor, buf, 0 );
    }


   // encode methods

    @Override
    public final void encode( final Message msg ) {
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERSINGLE:
            encodeNewOrderSingle( (NewOrderSingle) msg );
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            encodeCancelReplaceRequest( (CancelReplaceRequest) msg );
            break;
        case EventIds.ID_CANCELREQUEST:
            encodeCancelRequest( (CancelRequest) msg );
            break;
        case EventIds.ID_CANCELREJECT:
            encodeCancelReject( (CancelReject) msg );
            break;
        case EventIds.ID_NEWORDERACK:
            encodeNewOrderAck( (NewOrderAck) msg );
            break;
        case EventIds.ID_TRADENEW:
            encodeTradeNew( (TradeNew) msg );
            break;
        case EventIds.ID_REJECTED:
            encodeRejected( (Rejected) msg );
            break;
        case EventIds.ID_CANCELLED:
            encodeCancelled( (Cancelled) msg );
            break;
        case EventIds.ID_REPLACED:
            encodeReplaced( (Replaced) msg );
            break;
        case EventIds.ID_DONEFORDAY:
            encodeDoneForDay( (DoneForDay) msg );
            break;
        case EventIds.ID_STOPPED:
            encodeStopped( (Stopped) msg );
            break;
        case EventIds.ID_EXPIRED:
            encodeExpired( (Expired) msg );
            break;
        case EventIds.ID_SUSPENDED:
            encodeSuspended( (Suspended) msg );
            break;
        case EventIds.ID_RESTATED:
            encodeRestated( (Restated) msg );
            break;
        case EventIds.ID_TRADECORRECT:
            encodeTradeCorrect( (TradeCorrect) msg );
            break;
        case EventIds.ID_TRADECANCEL:
            encodeTradeCancel( (TradeCancel) msg );
            break;
        case EventIds.ID_ORDERSTATUS:
            encodeOrderStatus( (OrderStatus) msg );
            break;
        case EventIds.ID_HEARTBEAT:
            encodeHeartbeat( (Heartbeat) msg );
            break;
        case EventIds.ID_LOGON:
            encodeLogon( (Logon) msg );
            break;
        case EventIds.ID_LOGOUT:
            encodeLogout( (Logout) msg );
            break;
        case EventIds.ID_SESSIONREJECT:
            encodeSessionReject( (SessionReject) msg );
            break;
        case EventIds.ID_RESENDREQUEST:
            encodeResendRequest( (ResendRequest) msg );
            break;
        case EventIds.ID_SEQUENCERESET:
            encodeSequenceReset( (SequenceReset) msg );
            break;
        case EventIds.ID_TESTREQUEST:
            encodeTestRequest( (TestRequest) msg );
            break;
        case 4:
        case 5:
        case 26:
        default:
            _builder.start();
            break;
        }
    }

    @Override public final int getLength() { return _builder.getLength(); }
    @Override public final int getOffset() { return _builder.getOffset(); }


    public final void encodeNewOrderSingle( final NewOrderSingle msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_NewOrderSingle );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        final TimeInForce tTimeInForceBase = msg.getTimeInForce();
        final byte tTimeInForce = ( tTimeInForceBase == null ) ?  DEFAULT_TimeInForce : transformTimeInForce( tTimeInForceBase );
        if ( tTimeInForce != Constants.UNSET_BYTE ) _builder.encodeByte( FixDictionary42.TimeInForce, tTimeInForce );        // tag59
            // ignore securityIDSrc;
            // ignore securityID;
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            // ignore ccy;
        _builder.encodeByte( FixDictionary42.OrdType, msg.getOrdType().getVal() );        // tag40
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final HandlInst tHandlInst = msg.getHandlInst();
        if ( tHandlInst != null ) _builder.encodeByte( FixDictionary42.HandlInst, tHandlInst.getVal() );        // tag21
        _builder.encodeUTCTimestamp( FixDictionary42.TransactTime, now );        // tag60
        _builder.encodeString( FixDictionary42.ExDest, msg.getExDest() );        // tag100
            // tag107 secDesc, encoded in enrich method;
        _builder.encodeString( FixDictionary42.Account, msg.getAccount() );        // tag1
            _builder.encodeByte( FixDictionary42.Rule80A, encodeRule47A( msg.getOrderCapacity() ) );
        _builder.encodeString( FixDictionary42.SrcLinkId, msg.getSrcLinkId() );        // tag58
        _builder.encodeString( FixDictionary42.SecurityExchange, msg.getSecurityExchange() );        // tag207
        final BookingType tBookingType = msg.getBookingType();
        if ( tBookingType != null ) _builder.encodeByte( FixDictionary42.BookingType, tBookingType.getVal() );        // tag775
        enrich( msg );        // HOOK
        _builder.encodeEnvelope();
    }

    public final void encodeCancelReplaceRequest( final CancelReplaceRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_CancelReplaceRequest );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.OrigClOrdId, msg.getOrigClOrdId() );        // tag41
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        final TimeInForce tTimeInForceBase = msg.getTimeInForce();
        final byte tTimeInForce = ( tTimeInForceBase == null ) ?  DEFAULT_TimeInForce : transformTimeInForce( tTimeInForceBase );
        if ( tTimeInForce != Constants.UNSET_BYTE ) _builder.encodeByte( FixDictionary42.TimeInForce, tTimeInForce );        // tag59
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            // ignore ccy;
        _builder.encodeByte( FixDictionary42.OrdType, msg.getOrdType().getVal() );        // tag40
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final HandlInst tHandlInst = msg.getHandlInst();
        if ( tHandlInst != null ) _builder.encodeByte( FixDictionary42.HandlInst, tHandlInst.getVal() );        // tag21
        _builder.encodeUTCTimestamp( FixDictionary42.TransactTime, now );        // tag60
        _builder.encodeString( FixDictionary42.ExDest, msg.getExDest() );        // tag100
            // tag107 secDesc, encoded in enrich method;
        _builder.encodeString( FixDictionary42.Account, msg.getAccount() );        // tag1
            _builder.encodeByte( FixDictionary42.Rule80A, encodeRule47A( msg.getOrderCapacity() ) );
        _builder.encodeString( FixDictionary42.SrcLinkId, msg.getSrcLinkId() );        // tag58
        _builder.encodeString( FixDictionary42.SecurityExchange, msg.getSecurityExchange() );        // tag207
        final BookingType tBookingType = msg.getBookingType();
        if ( tBookingType != null ) _builder.encodeByte( FixDictionary42.BookingType, tBookingType.getVal() );        // tag775
        enrich( msg );        // HOOK
        _builder.encodeEnvelope();
    }

    public final void encodeCancelRequest( final CancelRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_CancelRequest );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.OrigClOrdId, msg.getOrigClOrdId() );        // tag41
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        _builder.encodeUTCTimestamp( FixDictionary42.TransactTime, now );        // tag60
            // tag107 secDesc, encoded in enrich method;
        _builder.encodeString( FixDictionary42.Account, msg.getAccount() );        // tag1
        _builder.encodeString( FixDictionary42.SrcLinkId, msg.getSrcLinkId() );        // tag58
        enrich( msg );        // HOOK
        _builder.encodeEnvelope();
    }

    public final void encodeCancelReject( final CancelReject msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_CancelReject );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.OrigClOrdId, msg.getOrigClOrdId() );        // tag41
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        final CxlRejReason tCxlRejReason = msg.getCxlRejReason();
        if ( tCxlRejReason != null ) _builder.encodeBytes( FixDictionary42.CxlRejReason, tCxlRejReason.getVal() );        // tag102
        _builder.encodeByte( FixDictionary42.CxlRejResponseTo, msg.getCxlRejResponseTo().getVal() );        // tag434
        _builder.encodeEnvelope();
    }

    public final void encodeNewOrderAck( final NewOrderAck msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_NewOrderAck );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            _builder.encodeByte( FixDictionary42.LastQty, (byte)'0' );
            _builder.encodeByte( FixDictionary42.LastPx, (byte)'0' );
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeEnvelope();
    }

    public final void encodeTradeNew( final TradeNew msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Trade );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
        _builder.encodeInt( FixDictionary42.LastQty, msg.getLastQty() );        // tag32
        _builder.encodePrice( FixDictionary42.LastPx, msg.getLastPx() );        // tag31
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeString( FixDictionary42.LastMkt, msg.getLastMkt() );        // tag30
        _builder.encodeString( FixDictionary42.SecurityDesc, msg.getSecurityDesc() );        // tag107
        final MultiLegReportingType tMultiLegReportingType = msg.getMultiLegReportingType();
        if ( tMultiLegReportingType != null ) _builder.encodeByte( FixDictionary42.MultiLegReportingType, tMultiLegReportingType.getVal() );        // tag442
        _builder.encodeEnvelope();
    }

    public final void encodeRejected( final Rejected msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Rejected );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            _builder.encodeByte( FixDictionary42.LastQty, (byte)'0' );
            _builder.encodeByte( FixDictionary42.LastPx, (byte)'0' );
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeEnvelope();
    }

    public final void encodeCancelled( final Cancelled msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Cancelled );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            _builder.encodeByte( FixDictionary42.LastQty, (byte)'0' );
            _builder.encodeByte( FixDictionary42.LastPx, (byte)'0' );
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeString( FixDictionary42.OrigClOrdId, msg.getOrigClOrdId() );        // tag41
        _builder.encodeEnvelope();
    }

    public final void encodeReplaced( final Replaced msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Replaced );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
            encodeReplacedOrdStatus( msg );
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeString( FixDictionary42.OrigClOrdId, msg.getOrigClOrdId() );        // tag41
        _builder.encodeEnvelope();
    }

    public final void encodeDoneForDay( final DoneForDay msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_DoneForDay );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            _builder.encodeByte( FixDictionary42.LastQty, (byte)'0' );
            _builder.encodeByte( FixDictionary42.LastPx, (byte)'0' );
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeEnvelope();
    }

    public final void encodeStopped( final Stopped msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Stopped );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            _builder.encodeByte( FixDictionary42.LastQty, (byte)'0' );
            _builder.encodeByte( FixDictionary42.LastPx, (byte)'0' );
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeEnvelope();
    }

    public final void encodeExpired( final Expired msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Expired );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            _builder.encodeByte( FixDictionary42.LastQty, (byte)'0' );
            _builder.encodeByte( FixDictionary42.LastPx, (byte)'0' );
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeEnvelope();
    }

    public final void encodeSuspended( final Suspended msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Suspended );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            _builder.encodeByte( FixDictionary42.LastQty, (byte)'0' );
            _builder.encodeByte( FixDictionary42.LastPx, (byte)'0' );
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeEnvelope();
    }

    public final void encodeRestated( final Restated msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Restated );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        final ExecRestatementReason tExecRestatementReason = msg.getExecRestatementReason();
        if ( tExecRestatementReason != null ) _builder.encodeTwoByte( FixDictionary42.ExecRestatementReason, tExecRestatementReason.getVal() );        // tag378
        _builder.encodeEnvelope();
    }

    public final void encodeTradeCorrect( final TradeCorrect msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_TradeCorrect );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
        _builder.encodeInt( FixDictionary42.LastQty, msg.getLastQty() );        // tag32
        _builder.encodePrice( FixDictionary42.LastPx, msg.getLastPx() );        // tag31
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeString( FixDictionary42.LastMkt, msg.getLastMkt() );        // tag30
        _builder.encodeString( FixDictionary42.SecurityDesc, msg.getSecurityDesc() );        // tag107
        final MultiLegReportingType tMultiLegReportingType = msg.getMultiLegReportingType();
        if ( tMultiLegReportingType != null ) _builder.encodeByte( FixDictionary42.MultiLegReportingType, tMultiLegReportingType.getVal() );        // tag442
        _builder.encodeString( FixDictionary42.ExecRefID, msg.getExecRefID() );        // tag19
        _builder.encodeEnvelope();
    }

    public final void encodeTradeCancel( final TradeCancel msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_TradeCancel );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'0' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
        _builder.encodeInt( FixDictionary42.LastQty, msg.getLastQty() );        // tag32
        _builder.encodePrice( FixDictionary42.LastPx, msg.getLastPx() );        // tag31
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeString( FixDictionary42.LastMkt, msg.getLastMkt() );        // tag30
        _builder.encodeString( FixDictionary42.SecurityDesc, msg.getSecurityDesc() );        // tag107
        final MultiLegReportingType tMultiLegReportingType = msg.getMultiLegReportingType();
        if ( tMultiLegReportingType != null ) _builder.encodeByte( FixDictionary42.MultiLegReportingType, tMultiLegReportingType.getVal() );        // tag442
        _builder.encodeString( FixDictionary42.ExecRefID, msg.getExecRefID() );        // tag19
        _builder.encodeEnvelope();
    }

    public final void encodeOrderStatus( final OrderStatus msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_OrderStatus );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.ClOrdId, msg.getClOrdId() );        // tag11
        _builder.encodeString( FixDictionary42.ExecID, msg.getExecId() );        // tag17
            _builder.encodeByte( FixDictionary42.ExecTransType, (byte)'3' );
        _builder.encodeString( FixDictionary42.OrderId, msg.getOrderId() );        // tag37
        _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39
        _builder.encodeByte( FixDictionary42.ExecType, msg.getExecType().getVal() );        // tag150
        _builder.encodeInt( FixDictionary42.OrderQty, msg.getOrderQty() );        // tag38
        _builder.encodePrice( FixDictionary42.Price, msg.getPrice() );        // tag44
            _builder.encodeByte( FixDictionary42.LastQty, (byte)'0' );
            _builder.encodeByte( FixDictionary42.LastPx, (byte)'0' );
        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54
        _builder.encodeString( FixDictionary42.Symbol, msg.getSymbol() );        // tag55
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionary42.Currency, tCurrency.getVal() );        // tag15
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionary42.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeString( FixDictionary42.SecurityID, msg.getSecurityId() );        // tag48
        _builder.encodePrice( FixDictionary42.AvgPx, msg.getAvgPx() );        // tag6
        _builder.encodeInt( FixDictionary42.CumQty, msg.getCumQty() );        // tag14
        _builder.encodeInt( FixDictionary42.LeavesQty, msg.getLeavesQty() );        // tag151
        _builder.encodeEnvelope();
    }

    public final void encodeHeartbeat( final Heartbeat msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Heartbeat );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.testReqID, msg.getTestReqID() );        // tag112
        enrich(msg, now);        // HOOK
        _builder.encodeEnvelope();
    }

    public final void encodeLogon( final Logon msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Logon );
        _builder.encodeString( FixDictionary42.SenderCompID, msg.getSenderCompId() );        // tag49
        _builder.encodeString( FixDictionary42.TargetCompID, msg.getTargetCompId() );        // tag56
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeString( FixDictionary42.SenderSubID, msg.getSenderSubId() );        // tag50
        _builder.encodeString( FixDictionary42.TargetSubID, msg.getTargetSubId() );        // tag57
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        final EncryptMethod tEncryptMethod = msg.getEncryptMethod();
        if ( tEncryptMethod != null ) _builder.encodeByte( FixDictionary42.EncryptMethod, tEncryptMethod.getVal() );        // tag98
        _builder.encodeInt( FixDictionary42.heartBtInt, msg.getHeartBtInt() );        // tag108
        _builder.encodeInt( FixDictionary42.RawDataLen, msg.getRawDataLen() );        // tag95
        _builder.encodeString( FixDictionary42.RawData, msg.getRawData() );        // tag96
        _builder.encodeBool( FixDictionary42.ResetSeqNumFlag, msg.getResetSeqNumFlag() );        // tag141
        _builder.encodeInt( FixDictionary42.NextExpectedMsgSeqNum, msg.getNextExpectedMsgSeqNum() );        // tag789
            _builder.encodeString( FixDictionary42.TradingSystemName, _systemName );
            _builder.encodeString( FixDictionary42.TradingSystemVersion, _vendorVersion );
            _builder.encodeString( FixDictionary42.TradingSystemVendor, _vender );
        setLastMsgSeqNumProcessed( msg );        // HOOK
        _builder.encodeEnvelope();
    }

    public final void encodeLogout( final Logout msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Logout );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeInt( FixDictionary42.lastMsgSeqNumProcessed, msg.getLastMsgSeqNumProcessed() );        // tag369
        _builder.encodeInt( FixDictionary42.NextExpectedMsgSeqNum, msg.getNextExpectedMsgSeqNum() );        // tag789
        _builder.encodeEnvelope();
    }

    public final void encodeSessionReject( final SessionReject msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_SessionReject );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeInt( FixDictionary42.RefSeqNum, msg.getRefSeqNum() );        // tag45
        _builder.encodeInt( FixDictionary42.RefTagID, msg.getRefTagID() );        // tag371
        _builder.encodeString( FixDictionary42.RefMsgType, msg.getRefMsgType() );        // tag372
        final SessionRejectReason tSessionRejectReason = msg.getSessionRejectReason();
        if ( tSessionRejectReason != null ) _builder.encodeTwoByte( FixDictionary42.SessionRejectReason, tSessionRejectReason.getVal() );        // tag373
        _builder.encodeEnvelope();
    }

    public final void encodeResendRequest( final ResendRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_ResendRequest );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeInt( FixDictionary42.BeginSeqNo, msg.getBeginSeqNo() );        // tag7
        _builder.encodeInt( FixDictionary42.EndSeqNo, msg.getEndSeqNo() );        // tag16
        enrich(msg, now);        // HOOK
        _builder.encodeEnvelope();
    }

    public final void encodeSequenceReset( final SequenceReset msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_SequenceReset );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeBool( FixDictionary42.GapFillFlag, msg.getGapFillFlag() );        // tag123
        _builder.encodeInt( FixDictionary42.NewSeqNo, msg.getNewSeqNo() );        // tag36
        enrich(msg, now);        // HOOK
        _builder.encodeEnvelope();
    }

    public final void encodeTestRequest( final TestRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_TestRequest );
            _builder.encodeString( FixDictionary42.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionary42.TargetCompID, _targetCompId ); // tag56;
            _builder.encodeString( FixDictionary42.SenderLocationID, _senderLocationId ); // tag142;
        _builder.encodeInt( FixDictionary42.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
            _builder.encodeString( FixDictionary42.SenderSubID, _senderSubId ); // tag50;
            _builder.encodeString( FixDictionary42.TargetSubID, _targetSubId ); // tag57;
        _builder.encodeBool( FixDictionary42.PossDupFlag, msg.getPossDupFlag() );        // tag43
        _builder.encodeUTCTimestamp( FixDictionary42.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionary42.testReqID, msg.getTestReqID() );        // tag112
        _builder.encodeEnvelope();
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

    private byte transformTimeInForce( TimeInForce val ) {
        switch( val ) {
        case Day:  // Day
            return (byte)'0';
        case GoodTillCancel:  // GTC
            return (byte)'1';
        case FillOrKill:  // FOK
            return (byte)'3';
        case GoodTillDate:  // GTD
            return (byte)'6';
        default:
            break;
        }
        return '0';
    }

        /**     * PostPend  Common Encoder File     *     * expected to contain methods used in hooks from model     */         @Override    public void setNanoStats( boolean nanoTiming ) {        _nanoStats = nanoTiming;    }    private       boolean         _nanoStats    =  true;             private       int             _idx          = 1;        private final ClientCancelRejectFactory _canRejFactory   = SuperpoolManager.instance().getFactory( ClientCancelRejectFactory.class, ClientCancelRejectImpl.class );    private final ClientRejectedFactory     _rejectedFactory = SuperpoolManager.instance().getFactory( ClientRejectedFactory.class,     ClientRejectedImpl.class );     public static final ZString ENCODE_REJ              = new ViewString( "ERJ" );    public static final ZString NONE                    = new ViewString( "NON" );    @Override    public Message unableToSend( Message msg, ZString errMsg ) {        switch( msg.getReusableType().getSubId() ) {        case EventIds.ID_NEWORDERSINGLE:            return rejectNewOrderSingle( (NewOrderSingle) msg, errMsg );        case EventIds.ID_NEWORDERACK:            break;        case EventIds.ID_TRADENEW:            break;        case EventIds.ID_CANCELREPLACEREQUEST:            return rejectCancelReplaceRequest( (CancelReplaceRequest) msg, errMsg );        case EventIds.ID_CANCELREQUEST:            return rejectCancelRequest( (CancelRequest) msg, errMsg );        }                return null;    }    private Message rejectNewOrderSingle( NewOrderSingle nos, ZString errMsg ) {        final ClientRejectedImpl reject = _rejectedFactory.get();        reject.setSrcEvent( nos );        reject.getExecIdForUpdate().copy( ENCODE_REJ ).append( nos.getClOrdId() ).append( ++_idx );        reject.getOrderIdForUpdate().setValue( NONE );        reject.setOrdRejReason( OrdRejReason.Other );        reject.getTextForUpdate().setValue( errMsg );        reject.setOrdStatus( OrdStatus.Rejected );        reject.setExecType( ExecType.Rejected );        reject.setCumQty( 0 );        reject.setAvgPx( 0.0 );        reject.setMessageHandler( nos.getMessageHandler() );        return reject;    }    private Message rejectCancelReplaceRequest( CancelReplaceRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelReplace );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private Message rejectCancelRequest( CancelRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelRequest );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private static final byte[] STATS       = "     [".getBytes();    private static final byte   STAT_DELIM  = ',';    private static final byte   STAT_END    = ']';        private       ReusableString  _senderCompId      = new ReusableString();    private       ReusableString  _senderSubId       = new ReusableString();    private       ReusableString  _senderLocationId  = new ReusableString();    private       ReusableString  _targetCompId      = new ReusableString();    private       ReusableString  _targetSubId       = new ReusableString();        @Override    public void setSenderCompId( ZString senderCompId ) {        _senderCompId.copy( senderCompId );    }    @Override    public void setSenderSubId( ZString senderSubId ) {        _senderSubId.copy( senderSubId );    }    @Override    public void setSenderLocationId( ZString senderLocationId ) {        _senderLocationId.copy( senderLocationId );    }    @Override    public void setTargetCompId( ZString targetId ) {        _targetCompId.copy( targetId );    }    @Override    public void setTargetSubId( ZString targetSubId ) {        _targetSubId.copy( targetSubId );    }    public void setCompIds( String senderCompId, String senderSubId,  String targetCompId, String targetSubId ) {        setSenderCompId(     new ReusableString( senderCompId ) );        setSenderSubId(      new ReusableString( senderSubId  ) );        setTargetCompId(     new ReusableString( targetCompId ) );        setTargetSubId(      new ReusableString( targetSubId  ) );    }         private byte encodeRule47A( OrderCapacity orderCapacity ) {        if ( orderCapacity == OrderCapacity.Principal ) return 'P';         if ( orderCapacity == OrderCapacity.AgentForOtherMember ) return 'A';         return 'P';    }    private void encodeReplacedOrdStatus( Replaced msg ) {        if ( msg.getCumQty() == 0 ) {            _builder.encodeByte( FixDictionary42.OrdStatus, (byte) '5' );        // tag39 = REPLACED        } else{            _builder.encodeByte( FixDictionary42.OrdStatus, msg.getOrdStatus().getVal() );        // tag39        }    }    @Override    public void addStats( final ReusableString outBuf, final Message msg, final long msgSent ) {                if ( msg.getReusableType().getSubId() == EventIds.ID_NEWORDERSINGLE ) {            final NewOrderSingle nos = (NewOrderSingle) msg;            final long tickIn   = nos.getOrderReceived();            final long microTickToTrade = (msgSent - tickIn) / 1000;                        outBuf.append( STATS ).append( microTickToTrade ).append( STAT_END );        }    }//    private final ZString _systemName    = new ViewString( "SMT" );//    private final ZString _vendorVersion = new ViewString( "1.1" );//    private final ZString _vender        = new ViewString( "LLT" );    private final ZString _systemName    = new ViewString( "ORS" );    private final ZString _vendorVersion = new ViewString( "1.1" );    private final ZString _vender        = new ViewString( "ILLUMINATI" );    /* * HANDCODED ENCODER METHDOS */     private com.rr.core.session.socket.SeqNumSession _session;        public void setSession( com.rr.core.session.socket.SeqNumSession session ) {        _session = session;    }    private void setLastMsgSeqNumProcessed( final Message msg ) {        if ( _session != null ) {//            _builder.encodeInt( 369, _session.getLastSeqNumProcessed() );                    _builder.encodeInt( 369, 0 );                }    }    private void setOrigSendingTime( SessionHeader msg, int msFromStartDayUTC ) {        if ( msg.isFlagSet( MsgFlag.PossDupFlag ) ) _builder.encodeUTCTimestamp( 122, msFromStartDayUTC );    }    private void enrich( SessionHeader msg, int now ) {        int time = msg.getSendingTime();        if ( time == Constants.UNSET_INT ) time = now;                setOrigSendingTime( msg,  time );        setLastMsgSeqNumProcessed( msg );            }        private void enrich( NewOrderSingle msg ) {        if ( msg.getTimeInForce() == TimeInForce.FillOrKill ) {            _builder.encodeByte( 110, (byte)'1' ); // MIN QTY        }//        setLastMsgSeqNumProcessed( msg );                _builder.encodeByte( 1028, (byte)'N' ); // tag1028 - Automated        _builder.encodeByte( 204, (byte)'1' );  // tag204 - Firm        _builder.encodeByte( 9702, (byte)'1' ); // tag9702 -  Broker/Trader        _builder.encodeString( FixDictionary42.SecurityDesc, msg.getInstrument().getSecurityDesc() ); // tag107        _builder.encodeBytes( FixDictionary42.SecurityType, msg.getInstrument().getSecurityType().getVal() ); // tag167        if ( msg.isFlagSet( MsgFlag.PossDupFlag ) ) _builder.encodeUTCTimestamp( 122, msg.getSendingTime() );    }    private void enrich( CancelReplaceRequest msg ) {//        setLastMsgSeqNumProcessed( msg );                _builder.encodeByte( 1028, (byte)'N' ); // tag1028        _builder.encodeByte( 204, (byte)'1' ); // tag204        _builder.encodeByte( 9702, (byte)'1' ); // tag9702        _builder.encodeByte( 9768, (byte)'Y' ); // tag9768 - In Flight Mitigation (IFM) ...         _builder.encodeString( FixDictionary42.SecurityDesc, msg.getInstrument().getSecurityDesc() ); // tag107        _builder.encodeBytes( FixDictionary42.SecurityType, msg.getInstrument().getSecurityType().getVal() ); // tag167        if ( msg.isFlagSet( MsgFlag.PossDupFlag ) ) _builder.encodeUTCTimestamp( 122, msg.getSendingTime() );    }    private void enrich( CancelRequest msg ) {//        setLastMsgSeqNumProcessed( msg );        //        _builder.encodeEmpty( 9717 ); // tag9717 - correlationId        _builder.encodeByte( 1028, (byte)'N' ); // tag1028        _builder.encodeByte( FixDictionary42.Side, msg.getSide().getVal() );        // tag54        final Instrument inst = msg.getInstrument();        if ( inst != null ) {            _builder.encodeString( FixDictionary42.SecurityDesc, inst.getSecurityDesc() ); // tag107            _builder.encodeBytes( FixDictionary42.SecurityType, inst.getSecurityType().getVal() ); // tag167        }        if ( msg.isFlagSet( MsgFlag.PossDupFlag ) ) _builder.encodeUTCTimestamp( 122, msg.getTransactTime() );    }    }
