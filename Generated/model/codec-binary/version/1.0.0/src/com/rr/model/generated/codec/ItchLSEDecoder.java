/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.codec;

import com.rr.core.book.BookFactory;
import com.rr.core.book.IntBookFactory;
import com.rr.core.book.MapIntBookFactory;

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

public final class ItchLSEDecoder extends AbstractBinaryDecoder {

   // Attrs
    private static final byte      MSG_ITCHBookAddOrder = (byte)'A';
    private static final byte      MSG_ITCHBookDeleteOrder = (byte)'D';
    private static final byte      MSG_ITCHBookModifyOrder = (byte)'U';
    private static final byte      MSG_ITCHBookClear = (byte)'y';

    private boolean _debug = false;

    private BinaryDecodeBuilder _builder;

    private       byte _msgType;
    private final byte                        _protocolVersion;
    private       int                         _msgStatedLen;
    private final ViewString                  _lookup = new ViewString();
    private final ReusableString _dump  = new ReusableString(256);

    // dict var holders for conditional mappings and fields with no corresponding event entry .. useful for hooks
    private       byte                        _subLen;
    private       int                         _nanosecond;
    private       long                        _orderId;
    private       byte                        _side;
    private       int                         _orderQty;
    private       int                         _instrumentId;
    private       double                      _price;

   // Pools

    private final SuperPool<BookAddOrderImpl> _bookAddOrderPool = SuperpoolManager.instance().getSuperPool( BookAddOrderImpl.class );
    private final BookAddOrderFactory _bookAddOrderFactory = new BookAddOrderFactory( _bookAddOrderPool );

    private final SuperPool<BookDeleteOrderImpl> _bookDeleteOrderPool = SuperpoolManager.instance().getSuperPool( BookDeleteOrderImpl.class );
    private final BookDeleteOrderFactory _bookDeleteOrderFactory = new BookDeleteOrderFactory( _bookDeleteOrderPool );

    private final SuperPool<BookModifyOrderImpl> _bookModifyOrderPool = SuperpoolManager.instance().getSuperPool( BookModifyOrderImpl.class );
    private final BookModifyOrderFactory _bookModifyOrderFactory = new BookModifyOrderFactory( _bookModifyOrderPool );

    private final SuperPool<BookClearImpl> _bookClearPool = SuperpoolManager.instance().getSuperPool( BookClearImpl.class );
    private final BookClearFactory _bookClearFactory = new BookClearFactory( _bookClearPool );


   // Constructors
    public ItchLSEDecoder() {
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
        _builder = (_debug) ? new DebugBinaryDecodeBuilder<com.rr.codec.emea.exchange.millenium.ITCHDecodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.millenium.ITCHDecodeBuilderImpl() )
                            : new com.rr.codec.emea.exchange.millenium.ITCHDecodeBuilderImpl();
    }

    @Override
    protected final Message doMessageDecode() {
        _builder.setMaxIdx( _maxIdx );

        switch( _msgType ) {
        case MSG_ITCHBookAddOrder:
            return decodeITCHBookAddOrder();
        case MSG_ITCHBookDeleteOrder:
            return decodeITCHBookDeleteOrder();
        case MSG_ITCHBookModifyOrder:
            return decodeITCHBookModifyOrder();
        case MSG_ITCHBookClear:
            return decodeITCHBookClear();
        case 'B':
        case 'C':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
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
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
            break;
        }
        if ( _debug ) {
            _dump.append( "Skipped Unsupported Message : " ).append( _msgType );
            _log.info( _dump );
            _dump.reset();
        }
        return null;
    }

    private final Message decodeITCHBookAddOrder() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ITCHBookAddOrder" ).append( " : " );
        }

        final BookAddOrderImpl msg = _bookAddOrderFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "subLen" ).append( " : " );
        _subLen = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "nanosecond" ).append( " : " );
        msg.setNanosecond( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        msg.setOrderId( _builder.decodeLong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "side" ).append( " : " );
        msg.setSide( Side.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        msg.setOrderQty( _builder.decodeInt() );
        msg.setBook( lookupBook( _builder.decodeInt() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 2 );

        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        msg.setPrice( _builder.decodeDecimal() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 1 );
        _builder.end();
        return msg;
    }

    private final Message decodeITCHBookDeleteOrder() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ITCHBookDeleteOrder" ).append( " : " );
        }

        final BookDeleteOrderImpl msg = _bookDeleteOrderFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "subLen" ).append( " : " );
        _subLen = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "nanosecond" ).append( " : " );
        msg.setNanosecond( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        msg.setOrderId( _builder.decodeLong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 1 );
        _builder.end();
        return msg;
    }

    private final Message decodeITCHBookModifyOrder() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ITCHBookModifyOrder" ).append( " : " );
        }

        final BookModifyOrderImpl msg = _bookModifyOrderFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "subLen" ).append( " : " );
        _subLen = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "nanosecond" ).append( " : " );
        msg.setNanosecond( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderId" ).append( " : " );
        msg.setOrderId( _builder.decodeLong() );

        if ( _debug ) _dump.append( "\nField: " ).append( "orderQty" ).append( " : " );
        msg.setOrderQty( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "price" ).append( " : " );
        msg.setPrice( _builder.decodeDecimal() );

        if ( _debug ) _dump.append( "\nField: " ).append( "filler2" ).append( " : " );
        _builder.skip( 1 );
        _builder.end();
        return msg;
    }

    private final Message decodeITCHBookClear() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "ITCHBookClear" ).append( " : " );
        }

        final BookClearImpl msg = _bookClearFactory.get();
        if ( _debug ) _dump.append( "\nField: " ).append( "subLen" ).append( " : " );
        _subLen = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "nanosecond" ).append( " : " );
        msg.setNanosecond( _builder.decodeInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "instrumentId" ).append( " : " );
        _instrumentId = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "filler1" ).append( " : " );
        _builder.skip( 3 );
        _builder.end();
        return msg;
    }


   // transform methods
    private byte _msgCount;    private byte _mktDataGroup;    private int _msgSeqNum;    private static BookFactory<Integer> _bookFactory = null;    private static int                  _preSizeSymbols = 1024;        public static <T extends BookFactory<Integer>> void setBookFactory( Class<T> bookFactory, int presizeSymbols ) {                try {            _bookFactory = bookFactory.newInstance();        } catch( Exception e ) {            throw new RuntimeException( "BookFactoryException " + e.getMessage(), e );        }        _preSizeSymbols = presizeSymbols;    }        private final IntBookFactory _bookMap = new MapIntBookFactory( _preSizeSymbols, _bookFactory );        public final Book lookupBook( int id ) {                Book book = _bookMap.find( id );                return book;    }    @Override    public final int parseHeader( final byte[] msg, final int offset, final int bytesRead ) {        _binaryMsg = msg;        _maxIdx = bytesRead + offset; // temp assign maxIdx to last data bytes in buffer        _offset = offset;        _builder.start( msg, offset, _maxIdx );                if ( bytesRead < 8 ) {            ReusableString copy = TLC.instance().getString();            if ( bytesRead == 0 )  {                copy.setValue( "{empty}" );            } else{                copy.setValue( msg, offset, bytesRead );            }            throw new RuntimeDecodingException( "ITCH Messsage too small, len=" + bytesRead, copy );        } else if ( msg.length < _maxIdx ){            throwDecodeException( "Buffer too small for specified bytesRead=" + bytesRead + ",offset=" + offset + ", bufLen=" + msg.length );        }                _msgStatedLen = _builder.decodeShort();        _msgCount = _builder.decodeByte();                 _mktDataGroup = _builder.decodeByte();        _msgSeqNum = _builder.decodeInt();                         _maxIdx = _msgStatedLen + _offset;  // correctly assign maxIdx as last bytes of current message         if ( _maxIdx > _binaryMsg.length )  _maxIdx  = _binaryMsg.length;                return _msgStatedLen;    }        public final byte getMarketDataGroup() {        return _mktDataGroup;    }    }
