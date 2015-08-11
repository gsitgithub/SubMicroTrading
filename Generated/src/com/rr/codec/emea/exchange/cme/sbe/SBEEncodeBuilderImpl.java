/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.codec.emea.exchange.cme.sbe;

import java.util.TimeZone;

import com.rr.core.codec.binary.BinaryLittleEndianEncoderUtils;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ZString;

/**
 * @NOTE dont check for max idx before each put the calling code must ensure buffer is big enough
 * (typically 8K)
 * 
 *
 * @author Richard Rose
 */
public final class SBEEncodeBuilderImpl extends BinaryLittleEndianEncoderUtils {

    private short _nextBlockLen;

    public SBEEncodeBuilderImpl( byte[] buffer, int offset, ZString protocolVersion ) {
        super( buffer, offset );
        _tzCalc.setLocalTimezone( TimeZone.getDefault() );
    }
    
    public final short getNextBlockLen() {
        return _nextBlockLen;
    }

    public final void setNextBlockLen( short nextBlockLen ) {
        _nextBlockLen = nextBlockLen;
    }

    @Override
    public void start( final int msgType ) {
        _idx              = _startOffset;
        _idx             += 2;                  // spacer for MsgLen
        encodeUShort( _nextBlockLen );
        encodeUShort( (short) msgType );
        encodeUShort( (short) 1 );
        encodeUShort( (short) 1 );
    }

    @Override
    public void encodeBool( boolean isOn ) {
        _buffer[ _idx++ ] = (isOn) ? (byte) 'Y' : (byte) 'N';
    }
    
    @Override
    public int end() {
        _msgLen = _idx - _startOffset;
        _idx    = _startOffset;        // correct position for len 
        encodeUShort( (short)_msgLen );
        return _msgLen;
    }

    @Override
    public void encodeTimestampLocal( int msFromStartofDayUTC ) {
        final long msUTC    = _tzCalc.msFromStartofDayToFullLocal( msFromStartofDayUTC );
        encodeLong( msUTC * 1000000 ); // nano precision
    }

    @Override
    public void encodeTimestampUTC( int msFromStartofDayUTC ) {
        final long msUTC    = _tzCalc.msFromStartofDayToFullUTC( msFromStartofDayUTC );
        encodeLong( msUTC * 1000000 ); // nano precision
    }
    
    @Override
    public final void encodePrice( final double price ) {
        
        long value = priceToExternalLong( price );

        encodeLong( value );
    }
    
    public static long priceToExternalLong( double price ) {
        
        if ( price >= 0 ) {
            if ( price == Constants.UNSET_DOUBLE ) {
                return Constants.UNSET_LONG;
            }
            return (long) ((price + Constants.WEIGHT) * SBEConstants.KEEP_DECIMAL_PLACE_FACTOR);
        }
        
        if ( price == Constants.UNSET_DOUBLE ) { // NULL
            return Constants.UNSET_LONG;
        }

        return (long) ((price - Constants.WEIGHT) * SBEConstants.KEEP_DECIMAL_PLACE_FACTOR);
    }
    
    @Override
    public final void encodeULong( final long value ) {
        
        if ( value == Constants.UNSET_LONG ) {
            doEncodeUnsignedLongNull();
        } else {
            doEncodeLong( value );
        }
    }

    @Override
    public final void encodeUInt( final int value ) {
        
        if ( value == Constants.UNSET_INT ) {
            doEncodeUnsignedIntNull();
        } else {
            doEncodeInt( value );
        }
    }

    @Override
    public final void encodeUShort( short val ) {
        if ( val == Constants.UNSET_SHORT ) {
            doEncodeUnsignedShortNull();
        } else {
            doEncodeShort( val );
        }
    }

    @Override
    public void encodeByte( final byte code ) {
        if ( code == Constants.UNSET_BYTE ) {
            _buffer[ _idx++ ] = (byte) 0x80;
        } else {
            _buffer[ _idx++ ] = code;
        }
    }
    
    @Override
    public void encodeUByte( final byte code ) {
        if ( code == Constants.UNSET_BYTE ) {
            _buffer[ _idx++ ] = (byte) 0xFF;
        } else {
            _buffer[ _idx++ ] = code;
        }
    }
    
    @Override
    protected void doEncodeSignedLongNull() {
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = (byte) 0x80;
    }

    @Override
    protected void doEncodeSignedIntNull() {
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = (byte) 0x80;
    }

    @Override
    protected void doEncodeSignedShortNull() {
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = (byte) 0x80;
    }

    private void doEncodeUnsignedLongNull() {
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
    }

    private void doEncodeUnsignedIntNull() {
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
    }

    private void doEncodeUnsignedShortNull() {
        _buffer[_idx++] = (byte)0xFF;
        _buffer[_idx++] = (byte)0xFF;
    }
}
