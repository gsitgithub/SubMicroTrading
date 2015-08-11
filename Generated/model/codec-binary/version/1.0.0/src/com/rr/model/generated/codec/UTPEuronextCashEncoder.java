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
import com.rr.codec.emea.exchange.utp.UTPEncodeBuilderImpl;
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

public final class UTPEuronextCashEncoder implements BinaryEncoder {

   // Member Vars
    private static final byte      MSG_Logon = (byte)'A';
    private static final byte      MSG_LogonReject = (byte)'l';
    private static final byte      MSG_TradingSessionStatus = (byte)'h';
    private static final byte      MSG_TestRequest = (byte)'1';
    private static final byte      MSG_Heartbeat = (byte)'0';
    private static final byte      MSG_NewOrder = (byte)'D';
    private static final byte      MSG_OrderAck = (byte)'a';
    private static final byte      MSG_OrderCancelRequest = (byte)'F';
    private static final byte      MSG_CancelReqAck = (byte)'6';
    private static final byte      MSG_OrderKilled = (byte)'4';
    private static final byte      MSG_OrderReplaceRequest = (byte)'G';
    private static final byte      MSG_ReplaceReqAck = (byte)'E';
    private static final byte      MSG_OrderReplaced = (byte)'5';
    private static final byte      MSG_CancelReplaceReject = (byte)'8';
    private static final byte      MSG_OrderFill = (byte)'2';
    private static final byte      MSG_BustCorrect = (byte)'C';

    private static final byte      DEFAULT_Side = 0x00;
    private static final byte      DEFAULT_LiquidityInd = 0x00;
    private static final byte      DEFAULT_OrdType = 0x00;
    private static final byte      DEFAULT_TimeInForce = 0x00;
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
    public UTPEuronextCashEncoder( byte[] buf, int offset ) {
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
        case EventIds.ID_UTPLOGON:
            encodeLogon( (UTPLogon) msg );
            break;
        case EventIds.ID_UTPLOGONREJECT:
            encodeLogonReject( (UTPLogonReject) msg );
            break;
        case EventIds.ID_UTPTRADINGSESSIONSTATUS:
            encodeTradingSessionStatus( (UTPTradingSessionStatus) msg );
            break;
        case EventIds.ID_TESTREQUEST:
            encodeTestRequest( (TestRequest) msg );
            break;
        case EventIds.ID_HEARTBEAT:
            encodeHeartbeat( (Heartbeat) msg );
            break;
        case EventIds.ID_NEWORDERSINGLE:
            encodeNewOrder( (NewOrderSingle) msg );
            break;
        case EventIds.ID_NEWORDERACK:
            encodeOrderAck( (NewOrderAck) msg );
            break;
        case EventIds.ID_CANCELREQUEST:
            encodeOrderCancelRequest( (CancelRequest) msg );
            break;
        case EventIds.ID_CANCELLED:
            encodeOrderKilled( (Cancelled) msg );
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            encodeOrderReplaceRequest( (CancelReplaceRequest) msg );
            break;
        case EventIds.ID_REPLACED:
            encodeOrderReplaced( (Replaced) msg );
            break;
        case EventIds.ID_REJECTED:
            encodeCancelReplaceReject( (Rejected) msg );
            break;
        case EventIds.ID_TRADENEW:
            encodeOrderFill( (TradeNew) msg );
            break;
        case EventIds.ID_TRADECANCEL:
            encodeBust( (TradeCancel) msg );
            break;
        case EventIds.ID_TRADECORRECT:
            encodeCorrect( (TradeCorrect) msg );
            break;
        case 4:
        case 5:
        case 6:
        case 12:
        case 13:
        case 14:
        case 15:
        case 16:
        case 19:
        case 22:
        case 23:
        case 24:
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
        _builder = (_debug) ? new DebugBinaryEncodeBuilder<com.rr.codec.emea.exchange.utp.UTPEncodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.utp.UTPEncodeBuilderImpl( _buf, _offset, _binaryVersion ) )
                            : new com.rr.codec.emea.exchange.utp.UTPEncodeBuilderImpl( _buf, _offset, _binaryVersion );
    }


    public final void encodeLogon( final UTPLogon msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Logon );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Logon" ).append( "  eventType=" ).append( "UTPLogon" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "lastMsgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "userName" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getUserName(), 11 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeLogonReject( final UTPLogonReject msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_LogonReject );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "LogonReject" ).append( "  eventType=" ).append( "UTPLogonReject" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "lastMsgSeqNumRcvd" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastMsgSeqNumRcvd() );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastMsgSeqNumSent" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastMsgSeqNumSent() );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectCode" ).append( " : " );
        _builder.encodeByte( msg.getRejectCode().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectText" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getRejectText(), 40 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 1 );
        _builder.end();
    }

    public final void encodeTradingSessionStatus( final UTPTradingSessionStatus msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_TradingSessionStatus );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "TradingSessionStatus" ).append( "  eventType=" ).append( "UTPTradingSessionStatus" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "mktPhaseChgTime" ).append( " : " );
        _builder.encodeTimeUTC( msg.getMktPhaseChgTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "instClassId" ).append( " : " );
        _builder.encodeStringFixedWidth( msg.getInstClassId(), 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "instClassStatus" ).append( " : " );
        _builder.encodeStringFixedWidth( msg.getInstClassStatus(), 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderEntryAllowed" ).append( " : " );
        _builder.encodeBool( msg.getOrderEntryAllowed() );
        if ( _debug ) _dump.append( "\nField: " ).append( "tradingSessionId" ).append( " : " );
        _builder.encodeStringFixedWidth( msg.getTradingSessionId(), 4 );
        _builder.end();
    }

    public final void encodeTestRequest( final TestRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_TestRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "TestRequest" ).append( "  eventType=" ).append( "TestRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 3 );
        _builder.end();
    }

    public final void encodeHeartbeat( final Heartbeat msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_Heartbeat );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "Heartbeat" ).append( "  eventType=" ).append( "Heartbeat" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 3 );
        _builder.end();
    }

    public final void encodeNewOrder( final NewOrderSingle msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_NewOrder );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "NewOrder" ).append( "  eventType=" ).append( "NewOrderSingle" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getOrderQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        _builder.encodeDecimal( msg.getPrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 24 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "priceScale" ).append( " : " ).append( "encode" ).append( " : " );
        encodePriceScale( msg.getPrice() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "symbol" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSymbol( msg.getInstrument() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "onBehalfOfCompId" ).append( " : " ).append( "encode" ).append( " : " );
        encodeOnBehalfOfCompId();
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( transformSide( msg.getSide() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordType" ).append( " : " );
        _builder.encodeByte( transformOrdType( msg.getOrdType() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        final TimeInForce tTimeInForceBase = msg.getTimeInForce();
        final byte tTimeInForce = ( tTimeInForceBase == null ) ? Constants.UNSET_BYTE : transformTimeInForce( tTimeInForceBase );
        _builder.encodeByte( tTimeInForce );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderCapacity" ).append( " : " );
        final OrderCapacity tOrderCapacityBase = msg.getOrderCapacity();
        final byte tOrderCapacity = ( tOrderCapacityBase == null ) ?  DEFAULT_OrderCapacity : transformOrderCapacity( tOrderCapacityBase );
        _builder.encodeByte( tOrderCapacity );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getAccount(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler4" ).append( " : " );
        _builder.encodeFiller( 12 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "confirmFlag" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)'1' );
        if ( _debug ) _dump.append( "\nField: " ).append( "mic" ).append( " : " );
        _builder.encodeFiller( 4 );    // mic
        if ( _debug ) _dump.append( "\nField: " ).append( "currency" ).append( " : " );
        final Currency tCurrency = msg.getCurrency();
        final byte[] tCurrencyBytes = ( tCurrency != null ) ? tCurrency.getVal() : null;
        _builder.encodeStringFixedWidth( tCurrencyBytes, 0, 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "giveUpFirm" ).append( " : " );
        _builder.encodeFiller( 8 );    // giveUpFirm
        if ( _debug ) _dump.append( "\nField: " ).append( "filler5" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "srcLinkId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getSrcLinkId(), 18 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler6" ).append( " : " );
        _builder.encodeFiller( 52 );
        _builder.end();
    }

    public final void encodeOrderAck( final NewOrderAck msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_OrderAck );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "OrderAck" ).append( "  eventType=" ).append( "NewOrderAck" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "transactTime" ).append( " : " );
        _builder.encodeFiller( 4 );    // transactTime
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 31 );
        _builder.end();
    }

    public final void encodeOrderCancelRequest( final CancelRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_OrderCancelRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "OrderCancelRequest" ).append( "  eventType=" ).append( "CancelRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "onBehalfOfCompId" ).append( " : " );
        _builder.encodeFiller( 11 );    // onBehalfOfCompId
        if ( _debug ) _dump.append( "\nField: " ).append( "symbol" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getSymbol(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( transformSide( msg.getSide() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 23 );
        _builder.end();
    }

    public final void encodeOrderKilled( final Cancelled msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_OrderKilled );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "OrderKilled" ).append( "  eventType=" ).append( "Cancelled" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "transactTime" ).append( " : " );
        _builder.encodeFiller( 4 );    // transactTime
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 31 );
        _builder.end();
    }

    public final void encodeOrderReplaceRequest( final CancelReplaceRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_OrderReplaceRequest );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "OrderReplaceRequest" ).append( "  eventType=" ).append( "CancelReplaceRequest" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getOrderQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        _builder.encodeDecimal( msg.getPrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 24 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "priceScale" ).append( " : " ).append( "encode" ).append( " : " );
        encodePriceScale( msg.getPrice() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "onBehalfOfCompId" ).append( " : " ).append( "encode" ).append( " : " );
        encodeOnBehalfOfCompId();
        if ( _debug ) _dump.append( "\nHook : " ).append( "symbol" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSymbol( msg.getInstrument() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( transformSide( msg.getSide() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "ordType" ).append( " : " );
        _builder.encodeByte( transformOrdType( msg.getOrdType() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "timeInForce" ).append( " : " );
        final TimeInForce tTimeInForceBase = msg.getTimeInForce();
        final byte tTimeInForce = ( tTimeInForceBase == null ) ? Constants.UNSET_BYTE : transformTimeInForce( tTimeInForceBase );
        _builder.encodeByte( tTimeInForce );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderCapacity" ).append( " : " );
        final OrderCapacity tOrderCapacityBase = msg.getOrderCapacity();
        final byte tOrderCapacity = ( tOrderCapacityBase == null ) ?  DEFAULT_OrderCapacity : transformOrderCapacity( tOrderCapacityBase );
        _builder.encodeByte( tOrderCapacity );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler3" ).append( " : " );
        _builder.encodeFiller( 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "account" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getAccount(), 12 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler4" ).append( " : " );
        _builder.encodeFiller( 5 );
        if ( _debug ) _dump.append( "\nHook : " ).append( "confirmFlag" ).append( " : " ).append( "encode" ).append( " : " );
        _builder.encodeByte( (byte)'1' );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler5" ).append( " : " );
        _builder.encodeFiller( 14 );
        if ( _debug ) _dump.append( "\nField: " ).append( "giveUpFirm" ).append( " : " );
        _builder.encodeFiller( 8 );    // giveUpFirm
        if ( _debug ) _dump.append( "\nField: " ).append( "filler6" ).append( " : " );
        _builder.encodeFiller( 8 );
        if ( _debug ) _dump.append( "\nField: " ).append( "srcLinkId" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getSrcLinkId(), 18 );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler7" ).append( " : " );
        _builder.encodeFiller( 4 );
        _builder.end();
    }

    public final void encodeOrderReplaced( final Replaced msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_OrderReplaced );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "OrderReplaced" ).append( "  eventType=" ).append( "Replaced" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "transactTime" ).append( " : " );
        _builder.encodeFiller( 4 );    // transactTime
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 31 );
        _builder.end();
    }

    public final void encodeCancelReplaceReject( final Rejected msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_CancelReplaceReject );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "CancelReplaceReject" ).append( "  eventType=" ).append( "Rejected" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "origClOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrigClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "transactTime" ).append( " : " );
        _builder.encodeFiller( 4 );    // transactTime
        if ( _debug ) _dump.append( "\nField: " ).append( "collarRejPx" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // collarRejPx
        if ( _debug ) _dump.append( "\nField: " ).append( "errorCode" ).append( " : " );
        _builder.encodeShort( Constants.UNSET_SHORT );    // errorCode
        if ( _debug ) _dump.append( "\nField: " ).append( "rejectText" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getText(), 40 );
        if ( _debug ) _dump.append( "\nField: " ).append( "collarRejType" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // collarRejType
        if ( _debug ) _dump.append( "\nField: " ).append( "collarRejPxScale" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // collarRejPxScale
        if ( _debug ) _dump.append( "\nField: " ).append( "filler" ).append( " : " );
        _builder.encodeFiller( 33 );
        _builder.end();
    }

    public final void encodeOrderFill( final TradeNew msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_OrderFill );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "OrderFill" ).append( "  eventType=" ).append( "TradeNew" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "transactTime" ).append( " : " );
        _builder.encodeFiller( 4 );    // transactTime
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodeDecimal( msg.getLastPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 11 );
        if ( _debug ) _dump.append( "\nField: " ).append( "priceScale" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // priceScale
        if ( _debug ) _dump.append( "\nField: " ).append( "liquidityInd" ).append( " : " );
        _builder.encodeByte( transformLiquidityInd( msg.getLiquidityInd() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( transformSide( msg.getSide() ) );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 33 );
        _builder.end();
    }

    public final void encodeBust( final TradeCancel msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_BustCorrect );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "BustCorrect" ).append( "  eventType=" ).append( "TradeCancel" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "transactTime" ).append( " : " );
        _builder.encodeFiller( 4 );    // transactTime
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodeDecimal( msg.getLastPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "priceScale" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // priceScale
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeChangeType" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // tradeChangeType
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 33 );
        _builder.end();
    }

    public final void encodeCorrect( final TradeCorrect msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_BustCorrect );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "BustCorrect" ).append( "  eventType=" ).append( "TradeCorrect" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "msgSeqNum" ).append( " : " );
        _builder.encodeInt( (int)msg.getMsgSeqNum() );
        if ( _debug ) _dump.append( "\nField: " ).append( "clOrdId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getClOrdId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "execId" ).append( " : " );
        _builder.encodeStringAsLong( msg.getExecId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "transactTime" ).append( " : " );
        _builder.encodeFiller( 4 );    // transactTime
        if ( _debug ) _dump.append( "\nField: " ).append( "lastQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getLastQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "lastPx" ).append( " : " );
        _builder.encodeDecimal( msg.getLastPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "priceScale" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // priceScale
        if ( _debug ) _dump.append( "\nField: " ).append( "tradeChangeType" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // tradeChangeType
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 33 );
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
            return (byte)'1';
        case Sell:  // Sell
            return (byte)'2';
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on Side for value " + val );
    }

    private byte transformLiquidityInd( LiquidityInd val ) {
        switch( val ) {
        case AddedLiquidity:  // Opening Trade / Trade Created by MO
            return (byte)'O';
        case RemovedLiquidity:  // RemovedLiquidity
            return (byte)'R';
        case LiquidityRoutedOut:  // RoutedOut
            return (byte)'X';
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on LiquidityInd for value " + val );
    }

    private byte transformOrdType( OrdType val ) {
        switch( val ) {
        case Limit:  // Limit
            return (byte)'2';
        case LimitOrBetter:  // LimitOrBetter
            return (byte)'2';
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on OrdType for value " + val );
    }

    private byte transformTimeInForce( TimeInForce val ) {
        switch( val ) {
        case Day:  // Day
            return (byte)'0';
        case ImmediateOrCancel:  // IOC
            return (byte)'3';
        case FillOrKill:  // FOK
            return (byte)'4';
        case AtTheClose:  // Closing Auction
            return (byte)'7';
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on TimeInForce for value " + val );
    }

    private byte transformOrderCapacity( OrderCapacity val ) {
        switch( val ) {
        case Proprietary:  // Proprietary/LiquidityProvider
            return (byte)'6';
        case Individual:  // Individual/Client
            return (byte)'1';
        case Principal:  // Principal/House
            return (byte)'2';
        case RisklessPrincipal:  // Riskless Principle
            return (byte)'8';
        case AgentForOtherMember:  // AgentForOtherMember/RelatedParty
            return (byte)'7';
        default:
            break;
        }
        return '2';
    }

    /**     * PostPend  Common Encoder File     *     * expected to contain methods used in hooks from model     */         @Override    public void setNanoStats( boolean nanoTiming ) {        _nanoStats = nanoTiming;    }    private       boolean         _nanoStats    =  true;             private       int             _idx          = 1;        private final ClientCancelRejectFactory _canRejFactory   = SuperpoolManager.instance().getFactory( ClientCancelRejectFactory.class, ClientCancelRejectImpl.class );    private final ClientRejectedFactory     _rejectedFactory = SuperpoolManager.instance().getFactory( ClientRejectedFactory.class,     ClientRejectedImpl.class );     public static final ZString ENCODE_REJ              = new ViewString( "ERJ" );    public static final ZString NONE                    = new ViewString( "NON" );    @Override    public Message unableToSend( Message msg, ZString errMsg ) {        switch( msg.getReusableType().getSubId() ) {        case EventIds.ID_NEWORDERSINGLE:            return rejectNewOrderSingle( (NewOrderSingle) msg, errMsg );        case EventIds.ID_NEWORDERACK:            break;        case EventIds.ID_TRADENEW:            break;        case EventIds.ID_CANCELREPLACEREQUEST:            return rejectCancelReplaceRequest( (CancelReplaceRequest) msg, errMsg );        case EventIds.ID_CANCELREQUEST:            return rejectCancelRequest( (CancelRequest) msg, errMsg );        }                return null;    }    private Message rejectNewOrderSingle( NewOrderSingle nos, ZString errMsg ) {        final ClientRejectedImpl reject = _rejectedFactory.get();        reject.setSrcEvent( nos );        reject.getExecIdForUpdate().copy( ENCODE_REJ ).append( nos.getClOrdId() ).append( ++_idx );        reject.getOrderIdForUpdate().setValue( NONE );        reject.setOrdRejReason( OrdRejReason.Other );        reject.getTextForUpdate().setValue( errMsg );        reject.setOrdStatus( OrdStatus.Rejected );        reject.setExecType( ExecType.Rejected );        reject.setCumQty( 0 );        reject.setAvgPx( 0.0 );        reject.setMessageHandler( nos.getMessageHandler() );        return reject;    }    private Message rejectCancelReplaceRequest( CancelReplaceRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelReplace );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private Message rejectCancelRequest( CancelRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelRequest );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private static final byte[] STATS       = "     [".getBytes();    private static final byte   STAT_DELIM  = ',';    private static final byte   STAT_END    = ']';    @Override    public void addStats( final ReusableString outBuf, final Message msg, final long msgSent ) {                if ( msg.getReusableType().getId() == FullEventIds.ID_MARKET_NEWORDERSINGLE ) {            final MarketNewOrderSingleImpl nos = (MarketNewOrderSingleImpl) msg;            nos.setOrderSent( msgSent );                } else if ( msg.getReusableType().getId() == FullEventIds.ID_CLIENT_NEWORDERACK ) {            final ClientNewOrderAckImpl ack = (ClientNewOrderAckImpl) msg;            final long orderIn  = ack.getOrderReceived();            final long orderOut = ack.getOrderSent();            final long ackIn    = ack.getAckReceived();            final long ackOut   = msgSent;            final long microNOSToMKt    = (orderOut - orderIn)  >> 10;            final long microInMkt       = (ackIn    - orderOut) >> 10;            final long microAckToClient = (ackOut   - ackIn)    >> 10;                        outBuf.append( STATS      ).append( microNOSToMKt )                  .append( STAT_DELIM ).append( microInMkt )                  .append( STAT_DELIM ).append( microAckToClient ).append( STAT_END );        }    }    private final ReusableString _onBehalfOfId = new ReusableString();    private final byte           _dps          = '0' + Constants.PRICE_DP;    private final byte           _zero         = '0';    private void encodePriceScale( double price ) {        if ( price != Constants.UNSET_DOUBLE ) {            _builder.encodeByte( _dps );        } else {            _builder.encodeByte( _zero );        }    }    private void encodeOnBehalfOfCompId() {        _builder.encodeStringFixedWidth( _onBehalfOfId, SizeType.UTP_COMP_ID_LEN.getSize() );    }        public void setOnBehalfOfId( ViewString val ) {        _onBehalfOfId.copy( val );    }    private void encodeSymbol( Instrument instrument ) {        _builder.encodeStringFixedWidth( instrument.getRIC(), SizeType.UTP_SYMBOL_LEN.getSize() );    }    }
