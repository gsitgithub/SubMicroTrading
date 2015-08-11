/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import com.rr.core.lang.Constants;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.MultiByteLookup;
import com.rr.core.utils.NumberFormatUtils;

/**
 * helper for the FIX decode process
 * 
 * doesnt check for buffer overrun, so ensure buffer big enough !
 * 
 * can throw RuntimeEncodingException
 * 
 * @author Richard Rose
 */
public abstract class BaseFixEncodeBuilderImpl implements FixEncodeBuilder {

    private static final int     DECIMAL_PLACES_PLUS_DOT = Constants.PRICE_DP + 1;

    private static final int     CHECKSUM_TAG_LEN = 7;

    private final int            _maxLen;

    private TimeZoneCalculator   _tzCalculator = new TimeZoneCalculator();

    protected final byte[]         _buffer;
    protected final int            _startOffset;

    protected int _idx;
    protected int _msgOffset;
    protected int _msgLen;

    public BaseFixEncodeBuilderImpl( byte[] buffer, int offset ) {
        _buffer            = buffer;
        _maxLen            = buffer.length;
        _idx               = offset;
        _startOffset       = offset;
    }
    
    @Override
    public final int  getNextFreeIdx(){
        return _idx;
    }

    public final TimeZoneCalculator getTimeZoneCalculator() {
        return _tzCalculator;
    }

    public final void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        _tzCalculator = calc;
    }

    @Override
    public abstract void encodeEnvelope();

    @Override
    public final int getOffset() {
        return _msgOffset;
    }
    
    @Override
    public final int getLength() {
        return _msgLen;
    }
    
    @Override
    public final void encodeString( final int tag, final ZString str ) {
        if ( str == null ) return;
        
        int len = str.length();

        if ( len == 0 ) return;
            
        byte[] value = str.getBytes();

        writeTag( tag );

        copy( value, str.getOffset(), len );

        writeFixDelimiter();
    }

    @Override
    public final void encodeString( final int tag, final byte[] buf, final int offset, final int len ) {
        if ( buf == null || len == 0 ) return;
        
        writeTag( tag );

        copy( buf, offset, len );

        writeFixDelimiter();
    }
    
    @Override
    public final void encodeBytes( final int tag, final byte[] buf ) {
        if ( buf == null || buf.length == 0 ) return;
        
        writeTag( tag );

        copy( buf );

        writeFixDelimiter();
    }
    
    @Override
    public final void encodePrice( final int tag, final double price ) {
        
        long asLong = (long) price;

        if ( asLong == price ) {
            encodeLong( tag, asLong );
            
            return;
        } 
        
        long value;
        int len;

        if ( price >= 0 ) {
            value = (long) ((price + Constants.WEIGHT) * Constants.KEEP_DECIMAL_PLACE_FACTOR);
            len = NumberFormatUtils.getPosLongLen( value );
            
            if ( len < DECIMAL_PLACES_PLUS_DOT ) {
                len = DECIMAL_PLACES_PLUS_DOT;
            }
        } else {
            if ( price == Constants.UNSET_DOUBLE ) { // NULL
                return;
            }
            value = (long) ((price - Constants.WEIGHT) * Constants.KEEP_DECIMAL_PLACE_FACTOR);
            len = NumberFormatUtils.getNegLongLen( value ) - 1;
            
            if ( len < DECIMAL_PLACES_PLUS_DOT ) {
                len = DECIMAL_PLACES_PLUS_DOT;
            }
            len++; // -ve 
        }

        len++; // '.'
        writeTag( tag );
        writePriceLong( value, len );
        writeFixDelimiter();
    }
    
    @Override
    public final void encodeLong( final int tag, final long value ) {
        
        if ( value == 0 ) {
            encodeZero( tag );
            return;
        } 
        
        int valLen;
        
        if ( value < 0 ) {
            valLen = NumberFormatUtils.getNegLongLen( value ); 

            if ( value == Constants.UNSET_LONG ) {
                return;
            }
        } else {
            valLen = NumberFormatUtils.getPosLongLen( value );
        }
        
        writeTag( tag );

        writeLong( value, valLen );
        
        writeFixDelimiter();
    }

    @Override
    public final void encodeInt( final int tag, final int value ) {
        
        if ( value == 0 ) {
            encodeZero( tag );
            return;
        }
        
        int valLen;
        
        if ( value < 0 ) {
            valLen = NumberFormatUtils.getNegIntLen( value ); 

            if ( value == Constants.UNSET_INT ) {
                return;
            }
        } else {
            valLen = NumberFormatUtils.getPosIntLen( value );
        }
        
        writeTag( tag );

        writeInt( value, valLen );
        
        writeFixDelimiter();
    }

    @Override
    public final void encodeByte( final int tag, final byte code ) {
        writeTag( tag );

        _buffer[ _idx++ ] = code;
        
        writeFixDelimiter();
    }
    
    @Override
    public final void encodeTwoByte( final int tag, final byte[] code ) {
        
        writeTag( tag );

        _buffer[ _idx++ ] = code[0];
        
        if ( code.length == 2 )
            _buffer[ _idx++ ] = code[1];
        
        writeFixDelimiter();
    }
    
    public final void encodeMultiByte( final int tag, final MultiByteLookup code ) {
        if ( code == null ) return;
        
        byte[] value = code.getVal();

        writeTag( tag );
        
        copy( value );

        writeFixDelimiter();
    }

    @Override
    public final void encodeBool( final int tag, final boolean isOn ) {

        writeTag( tag );

        _buffer[ _idx++ ] = (isOn) ? (byte) 'Y' : (byte) 'N';
        
        writeFixDelimiter();
        
    }
    
    @Override
    public final void encodeUTCTimestamp( final int tag, final int msFromStartofDayUTC ){
        writeTag( tag );
        
        _idx = _tzCalculator.dateTimeToUTC( _buffer, _idx, msFromStartofDayUTC );
        
        writeFixDelimiter();
    }
    
    @Override
    public final void encodeDate( int tag, int yyyymmdd ) {
        if ( yyyymmdd == 0 ) {
            return;
        }
        encodeInt( tag, yyyymmdd );
    }
    
    @Override
    public final void encodeAckStats( final long orderIn, final long orderOut, final long ackIn, final long ackOut ) {
        // convert nanos to micros
        
        int tag = Constants.FIX_TAG_ACK_STATS;
        
        long nanosNOSToMKt    = (orderOut - orderIn)  >>> 10;
        long nanosInMkt       = (ackIn    - orderOut) >>> 10;
        long nanosAckToClient = (ackOut   - ackIn)    >>> 10;

        int nosLen = (nanosNOSToMKt < 0) ? NumberFormatUtils.getNegLongLen( nanosNOSToMKt ) 
                                         : NumberFormatUtils.getPosLongLen( nanosNOSToMKt );
        
        int mktLen = (nanosInMkt < 0) ? NumberFormatUtils.getNegLongLen( nanosInMkt )  
                                      : NumberFormatUtils.getPosLongLen( nanosInMkt );
        
        int ackLen = (nanosAckToClient < 0) ? NumberFormatUtils.getNegLongLen( nanosAckToClient ) 
                                            : NumberFormatUtils.getPosLongLen( nanosAckToClient );
        
        writeTag( tag );

        writeLong( nanosNOSToMKt, nosLen );
        _buffer[_idx++] = ',';
        
        writeLong( nanosInMkt, mktLen );
        _buffer[_idx++] = ',';
        
        writeLong( nanosAckToClient, ackLen );
        
        writeFixDelimiter();
    }

    
    public final void encodeZero( final int tag ) {
        writeTag( tag );
        _buffer[_idx++] = '0';
        writeFixDelimiter();
    }

    public final void encodeEmpty( final int tag ) {
        writeTag( tag );
        writeFixDelimiter();
    }

    public final void clear() {
        _idx    = 0;
    }

    public final byte[] getBuffer() {
        return _buffer;
    }

    public final int getCurrentIndex() {
        return _idx;
    }
    
    protected final void encodeChecksum() {

        int val = 0;
        
        for( int idx=_msgOffset ; idx < _idx ; ) {
            val += _buffer[ idx++ ];
        }
        
        val = val & 0xFF;
        
        final int len = CHECKSUM_TAG_LEN;
        if ( _idx + len > _maxLen )
            throw new RuntimeEncodingException( "Buffer overflow  max=" + _maxLen + ", idx=" + _idx );
        
        _buffer[ _idx++ ] = '1';
        _buffer[ _idx++ ] = '0';
        _buffer[ _idx++ ] = '=';
        
        int div10  = (val * 205) >> 11;
        
        byte leastSigDigit = (byte)((val - (div10 * 10)) + '0');
        
        val = div10; // now at most 25

        final int t = NumberFormatUtils._dig100[ val ];
        
        _buffer[_idx++ ] = (byte)(t >> 8);      // tens
        _buffer[_idx++ ] = (byte)(t & 0xFF);    // units
        _buffer[_idx++ ] = leastSigDigit;
        
        writeFixDelimiter();
    }
    
    private void writeTag( final int tag ) {
        final int tagLen = getTagLength( tag );
        writePosInt( tag, tagLen );
        _buffer[_idx++] = '=';
    }
    
    protected final void writeFixDelimiter() {
        _buffer[_idx++] = FixField.FIELD_DELIMITER;
    }

    private int getTagLength( final int tag ) {
        if ( tag < 0 ) throw new RuntimeEncodingException( "Negative tag=" + tag );
        
        int tagLen = NumberFormatUtils.getPosIntLen( tag );
        
        return tagLen;
    }

    /**
     * COPY OF NumberFormatUtils routines so dont need to pass buffer, offset and index to routines
     */

    private void writeInt( int value, final int length ) {
        int q, r;
        int charPos = _idx + length;
        byte sign = 0;
        if ( value < 0 ) {
            sign = '-';
            value = -value;
        }
        while( value > 65536 ) {
            q = value / 100;
            r = (value - (q * 100));
            value = q;
            final int t = NumberFormatUtils._dig100[ r ];
            
            _buffer[--charPos] = (byte)(t & 0xFF);   // units
            _buffer[--charPos] = (byte)(t >> 8);     // tens
        }

        do {
            q = value / 10;
            r = (value - (q * 10));
            _buffer[--charPos] = NumberFormatUtils._dig10[r];
            value = q;
        } while( value != 0 );

        if ( sign != 0 ) {
            _buffer[--charPos] = sign;
        }
        _idx += length;
    }

    protected final void writePosInt( int value, final int length ) {
        int q, r;
        int charPos = _idx + length;
        while( value > 65536 ) {
            q = value / 100;
            r = (value - (q * 100));
            value = q;
            final int t = NumberFormatUtils._dig100[ r ];
            
            _buffer[--charPos] = (byte)(t & 0xFF);   // units
            _buffer[--charPos] = (byte)(t >> 8);     // tens
        }

        do {
            q = value / 10;
            r = (value - (q * 10));
            _buffer[--charPos] = NumberFormatUtils._dig10[r];
            value = q;
        } while( value != 0 );

        _idx += length;
    }

    private void writeLong( long value, final int length ) {
        long q;
        int r;
        int charPos = _idx + length;
        byte sign = 0;

        if ( value < 0 ) {
            sign = '-';
            value = -value;
        }

        while( value > 65536 ) {
            q = value / 100;
            r = (int) (value - (q * 100));
            value = q;
            final int t = NumberFormatUtils._dig100[ r ];
            
            _buffer[--charPos] = (byte)(t & 0xFF);   // units
            _buffer[--charPos] = (byte)(t >> 8);     // tens
        }

        do {
            q = value / 10;
            r = (int) (value - (q * 10));
            _buffer[--charPos] = NumberFormatUtils._dig10[r];
            value = q;
        } while( value != 0 );

        if ( sign != 0 ) {
            _buffer[--charPos] = sign;
        }
        _idx += length;
    }

    private void writePriceLong( long value, final int length ) {
        long q;
        int r;
        int charPos = _idx + length;
        int startPos = 0;
        byte sign = 0;

        if ( value < 0 ) {
            sign = '-';
            value = -value;
        }

        // input value will be at least 6 digits long due to 1M multiplier
        boolean startedEncoding = false;
        
        for ( int pairIdx = 0 ; pairIdx < 3 ; pairIdx++ ) {
            
            q = value / 100;
            r = (int) (value - ((q * 100)));
            
            final int t = NumberFormatUtils._dig100[ r ];
            final int units = (t & 0xFF);

            value = q;
            if ( startedEncoding ) {
                _buffer[--charPos] = (byte)units;   // units
                _buffer[--charPos] = (byte)(t >> 8);     // tens
            } else {
                if ( r == 0 ) {
                    charPos -= 2;  // avoid zero padding to RHS of least sig digit
                } else {
                    startedEncoding = true;
                    if ( units == 0x30 ) {
                        charPos--;
                        startPos = charPos;
                    } else {
                        startPos = charPos;
                        _buffer[--charPos] = (byte)units;
                    }
                    _buffer[--charPos] = (byte)(t >> 8); // tens
                }
            }
        }
        
        if ( !startedEncoding ) {
            startPos = charPos + 1;
            _buffer[charPos] = '0';
        }
        _buffer[--charPos] = '.';

        // Do the integer part now
        if ( value == 0 ) {
            _buffer[--charPos] = '0';
        } else {
            while( value > 65536 ) {
                q = value / 100;
                r = (int) (value - (q * 100));
                value = q;
                final int t = NumberFormatUtils._dig100[ r ];
                
                _buffer[--charPos] = (byte)(t & 0xFF);   // units
                _buffer[--charPos] = (byte)(t >> 8);     // tens
            }

            do {
                q = value / 10;
                r = (int) (value - (q * 10));
                _buffer[--charPos] = NumberFormatUtils._dig10[r];
                value = q;
            } while( value != 0 );
        }
        if ( sign != 0 ) {
            _buffer[--charPos] = sign;
        }
        _idx = startPos;
    }

    private void copy( final byte[] value ) {

        final int len = value.length;

        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            int i=0;
            while( i < len ) {
                _buffer[ _idx++ ] = value[ i++ ];
            }
        } else {
            System.arraycopy( value, 0, _buffer, _idx, len );
            _idx += len;
        }
    }

    private void copy( final byte[] value, final int offset, final int len ) {

        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            
            final int max = offset + len;
            int i=offset;
            
            while( i < max ) {
                _buffer[ _idx++ ] = value[ i++ ];
            }
        } else {
            System.arraycopy( value, offset, _buffer, _idx, len );
            _idx += len;
        }
    }
}
