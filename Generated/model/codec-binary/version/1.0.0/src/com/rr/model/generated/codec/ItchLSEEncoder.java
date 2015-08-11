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
import com.rr.codec.emea.exchange.millenium.ITCHEncodeBuilderImpl;
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

public final class ItchLSEEncoder implements BinaryEncoder {

   // Member Vars
    private static final byte      MSG_ITCHBookAddOrder = (byte)'A';
    private static final byte      MSG_ITCHBookDeleteOrder = (byte)'D';
    private static final byte      MSG_ITCHBookModifyOrder = (byte)'U';
    private static final byte      MSG_ITCHBookClear = (byte)'y';


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
    public ItchLSEEncoder( byte[] buf, int offset ) {
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
        case EventIds.ID_BOOKADDORDER:
            encodeITCHBookAddOrder( (BookAddOrder) msg );
            break;
        case EventIds.ID_BOOKDELETEORDER:
            encodeITCHBookDeleteOrder( (BookDeleteOrder) msg );
            break;
        case EventIds.ID_BOOKMODIFYORDER:
            encodeITCHBookModifyOrder( (BookModifyOrder) msg );
            break;
        case EventIds.ID_BOOKCLEAR:
            encodeITCHBookClear( (BookClear) msg );
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
        _builder = (_debug) ? new DebugBinaryEncodeBuilder<com.rr.codec.emea.exchange.millenium.ITCHEncodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.millenium.ITCHEncodeBuilderImpl( _buf, _offset, _binaryVersion ) )
                            : new com.rr.codec.emea.exchange.millenium.ITCHEncodeBuilderImpl( _buf, _offset, _binaryVersion );
    }


    public final void encodeITCHBookAddOrder( final BookAddOrder msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ITCHBookAddOrder );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ITCHBookAddOrder" ).append( "  eventType=" ).append( "BookAddOrder" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "subLen" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // subLen
        if ( _debug ) _dump.append( "\nField: " ).append( "nanosecond" ).append( " : " );
        _builder.encodeInt( (int)msg.getNanosecond() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeLong( (long)msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        _builder.encodeByte( msg.getSide().getVal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getOrderQty() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "instrumentId" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSymbol( msg.getBook().getInstrument() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 2 );
        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        _builder.encodeDecimal( msg.getPrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        _builder.end();
    }

    public final void encodeITCHBookDeleteOrder( final BookDeleteOrder msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ITCHBookDeleteOrder );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ITCHBookDeleteOrder" ).append( "  eventType=" ).append( "BookDeleteOrder" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "subLen" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // subLen
        if ( _debug ) _dump.append( "\nField: " ).append( "nanosecond" ).append( " : " );
        _builder.encodeInt( (int)msg.getNanosecond() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeLong( (long)msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.encodeFiller( 1 );
        _builder.end();
    }

    public final void encodeITCHBookModifyOrder( final BookModifyOrder msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ITCHBookModifyOrder );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ITCHBookModifyOrder" ).append( "  eventType=" ).append( "BookModifyOrder" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "subLen" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // subLen
        if ( _debug ) _dump.append( "\nField: " ).append( "nanosecond" ).append( " : " );
        _builder.encodeInt( (int)msg.getNanosecond() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        _builder.encodeLong( (long)msg.getOrderId() );
        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getOrderQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        _builder.encodeDecimal( msg.getPrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.encodeFiller( 1 );
        _builder.end();
    }

    public final void encodeITCHBookClear( final BookClear msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start( MSG_ITCHBookClear );
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "ITCHBookClear" ).append( "  eventType=" ).append( "BookClear" ).append( " : " );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "subLen" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // subLen
        if ( _debug ) _dump.append( "\nField: " ).append( "nanosecond" ).append( " : " );
        _builder.encodeInt( (int)msg.getNanosecond() );
        if ( _debug ) _dump.append( "\nHook : " ).append( "instrumentId" ).append( " : " ).append( "encode" ).append( " : " );
        encodeSymbol( msg.getBook().getInstrument() );
        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
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

    /**     * PostPend  Common Encoder File     *     * expected to contain methods used in hooks from model     */         @Override    public void setNanoStats( boolean nanoTiming ) {        _nanoStats = nanoTiming;    }    private       boolean         _nanoStats    =  true;             private       int             _idx          = 1;        private final ClientCancelRejectFactory _canRejFactory   = SuperpoolManager.instance().getFactory( ClientCancelRejectFactory.class, ClientCancelRejectImpl.class );    private final ClientRejectedFactory     _rejectedFactory = SuperpoolManager.instance().getFactory( ClientRejectedFactory.class,     ClientRejectedImpl.class );     public static final ZString ENCODE_REJ              = new ViewString( "ERJ" );    public static final ZString NONE                    = new ViewString( "NON" );    @Override    public Message unableToSend( Message msg, ZString errMsg ) {        switch( msg.getReusableType().getSubId() ) {        case EventIds.ID_NEWORDERSINGLE:            return rejectNewOrderSingle( (NewOrderSingle) msg, errMsg );        case EventIds.ID_NEWORDERACK:            break;        case EventIds.ID_TRADENEW:            break;        case EventIds.ID_CANCELREPLACEREQUEST:            return rejectCancelReplaceRequest( (CancelReplaceRequest) msg, errMsg );        case EventIds.ID_CANCELREQUEST:            return rejectCancelRequest( (CancelRequest) msg, errMsg );        }                return null;    }    private Message rejectNewOrderSingle( NewOrderSingle nos, ZString errMsg ) {        final ClientRejectedImpl reject = _rejectedFactory.get();        reject.setSrcEvent( nos );        reject.getExecIdForUpdate().copy( ENCODE_REJ ).append( nos.getClOrdId() ).append( ++_idx );        reject.getOrderIdForUpdate().setValue( NONE );        reject.setOrdRejReason( OrdRejReason.Other );        reject.getTextForUpdate().setValue( errMsg );        reject.setOrdStatus( OrdStatus.Rejected );        reject.setExecType( ExecType.Rejected );        reject.setCumQty( 0 );        reject.setAvgPx( 0.0 );        reject.setMessageHandler( nos.getMessageHandler() );        return reject;    }    private Message rejectCancelReplaceRequest( CancelReplaceRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelReplace );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private Message rejectCancelRequest( CancelRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelRequest );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private static final byte[] STATS       = "     [".getBytes();    private static final byte   STAT_DELIM  = ',';    private static final byte   STAT_END    = ']';    @Override    public void addStats( final ReusableString outBuf, final Message msg, final long msgSent ) {                if ( msg.getReusableType().getId() == FullEventIds.ID_MARKET_NEWORDERSINGLE ) {            final MarketNewOrderSingleImpl nos = (MarketNewOrderSingleImpl) msg;            nos.setOrderSent( msgSent );                } else if ( msg.getReusableType().getId() == FullEventIds.ID_CLIENT_NEWORDERACK ) {            final ClientNewOrderAckImpl ack = (ClientNewOrderAckImpl) msg;            final long orderIn  = ack.getOrderReceived();            final long orderOut = ack.getOrderSent();            final long ackIn    = ack.getAckReceived();            final long ackOut   = msgSent;            final long microNOSToMKt    = (orderOut - orderIn)  >> 10;            final long microInMkt       = (ackIn    - orderOut) >> 10;            final long microAckToClient = (ackOut   - ackIn)    >> 10;                        outBuf.append( STATS      ).append( microNOSToMKt )                  .append( STAT_DELIM ).append( microInMkt )                  .append( STAT_DELIM ).append( microAckToClient ).append( STAT_END );        }    }    private void encodeSymbol( Instrument instrument ) {        _builder.encodeInt( (int)instrument.getLongSymbol() );    }    }
