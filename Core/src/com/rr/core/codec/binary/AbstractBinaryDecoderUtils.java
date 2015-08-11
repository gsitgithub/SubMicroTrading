/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.stats.SizeConstants;

/**
 * helper for binary decoder protocols
 *
 * can throw RuntimeEncodingException
 */
public abstract class AbstractBinaryDecoderUtils implements BinaryDecodeBuilder {

    protected byte[]             _buffer;
    protected int                _startOffset;
    protected int                _idx;
    protected int                _msgLen;
    protected   int              _maxIdx;
    protected TimeZoneCalculator _tzCalc = new TimeZoneCalculator();

    public AbstractBinaryDecoderUtils() {
        // nothing
    }
    
    @Override
    public void start( final byte[] msg, final int offset, final int maxIdx ) {
        _buffer      = msg;
        _startOffset = offset;
        _idx         = _startOffset;
        _maxIdx      = maxIdx;
    }
    
    @Override
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        _tzCalc = calc;
    }
    
    @Override
    public final void setMaxIdx( final int maxIdx ) {
        _maxIdx      = maxIdx;
    }
    
    @Override
    public final int getMaxIdx() {
        return _maxIdx;
    }
    
    @Override
    public final int  getNextFreeIdx(){
        return _idx;
    }

    @Override
    public final int getOffset() {
        return _startOffset;
    }
    
    @Override
    public final int getLength() {
        return _msgLen;
    }
    
    @Override
    public void end() {
        _msgLen = _idx - _startOffset;
    }
    
    @Override
    public void decodeString( final ReusableString dest ) {

        final int len = readVarFieldLen();
        
        rangeCheck( len );
        
        if ( len > 0 ) {
            dest.ensureCapacity( len );

            copy( dest.getBytes(), 0, len );
            
            dest.setLength( len );
        } else {
            dest.reset();
        }
    }

    protected final void rangeCheck( int len ) {
        if ( len + _idx - 1 > _maxIdx ) {
            throw new RuntimeDecodingException( "AbstractBinaryDecoderUtils.rangeCheck cant read " + len + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + (_maxIdx-_startOffset) );
        }
    }

    // write two byte len
    private final int readVarFieldLen() {
        
        int len = 0xFF & _buffer[ _idx++ ];
        len = (len << 8) + (0xFF & _buffer[ _idx++ ]); 
        
        return len;
    }

    @Override
    public double decodeDecimal() {
        return decodePrice();
    }
    
    @Override
    public double decodePrice() {
        
        final long value = decodeLong();

        double price;
        
        if ( value >= 0 ) {
            if ( value == Long.MAX_VALUE )  {
                price = Double.MAX_VALUE;
            } else {
                price = value / Constants.KEEP_DECIMAL_PLACE_FACTOR;
            }
        } else {
            if ( value == Long.MIN_VALUE ) { // NULL
                price = Constants.UNSET_DOUBLE;
            } else if ( value == Constants.MIN_PRICE_AS_LONG ) {
                price = Double.MIN_VALUE;
            } else {
                price = value / Constants.KEEP_DECIMAL_PLACE_FACTOR;
            }
        }
        
        return price;
    }
    
    @Override public abstract long  decodeLong();
    @Override public abstract int   decodeInt();
    @Override public abstract short decodeShort();

    @Override
    public long decodeULong() {
        return decodeLong();
    }
    
    @Override
    public int  decodeQty() {
        return decodeInt();
    }
    
    @Override
    public int decodeUInt() {
        return decodeInt();
    }
    
    @Override
    public short decodeUShort() {
        return decodeShort();
    }
    
    @Override
    public byte decodeUByte() {
        return decodeByte();
    }
    
    @Override
    public byte decodeByte() {
        return _buffer[ _idx++ ];
    }
    
    @Override
    public byte decodeChar() {
        return decodeByte();
    }
    
    @Override
    public boolean decodeBool() {
        return _buffer[ _idx++ ] == '1' ? true : false;
    }
    
    @Override
    public final void clear() {
        _idx    = 0;
    }

    @Override
    public final byte[] getBuffer() {
        return _buffer;
    }

    @Override
    public final int getCurrentIndex() {
        return _idx;
    }
    
    @Override
    public int decodeTimeLocal() {
        throw new RuntimeEncodingException( "AbstractBinaryDecoder.decodeTimeLocal() not implemented for this decoder" );
    }
    
    @Override
    public int decodeDate() {
        throw new RuntimeEncodingException( "AbstractBinaryDecoder.decodeDate() not implemented for this decoder" );
    }

    @Override
    public int decodeTimeUTC() {
        throw new RuntimeEncodingException( "AbstractBinaryDecoder.decodeTimeUTC() not implemented for this decoder" );
    }

    @Override
    public int decodeTimestampLocal() {
        throw new RuntimeEncodingException( "AbstractBinaryDecoder.decodeTimestampLocal() not implemented for this decoder" );
    }
    
    @Override
    public int decodeTimestampUTC() {
        throw new RuntimeEncodingException( "AbstractBinaryDecoder.decodeTimestampUTC() not implemented for this decoder" );
    }
    
    @Override
    public final void decodeLongToString( final ReusableString dest ) {
        long val = decodeLong();
        dest.append( val );
    }
    
    @Override
    public final void decodeIntToString( final ReusableString dest ) {
        int val = decodeInt();
        dest.append( val );
    }
    
    @Override
    public final void decodeZStringFixedWidth( final ReusableString dest, final int len ) {
        rangeCheck( len );

        if ( len > 20 ) {
            // larger field worth the scan to find field length
            
            int    bufIdx     = _idx;
            int    dataLen    = 0;

            do {
                final byte b = _buffer[ bufIdx++ ];
                
                if ( b == 0x00 ) {
                    break; // EXCLUDE padded nulls
                }
            } while( ++dataLen < len );
            
            dest.ensureCapacity( dataLen );

            copy( dest.getBytes(), 0, dataLen ); // increases _idx
            
            dest.setLength( dataLen );

            _idx += (len - dataLen);        
            
        } else {
            // this faster decode assumes events sized correctly .. if not unwanted growing will occur
            
            int    bufIdx     = _idx;
            int    idx        = 0;
            int    destIdx    = 0;
            byte[] destBytes  = dest.getBytes();
            int    maxDestIdx = destBytes.length;
                  
            while( idx++ < len ) {
                final byte b = _buffer[ bufIdx++ ];
                
                if ( b == 0x00 ) {
                    break; // EXCLUDE padded nulls
                }
                
                if ( destIdx >= maxDestIdx ) {
                    dest.ensureCapacity( len );
                    destBytes  = dest.getBytes();
                    maxDestIdx = destBytes.length;
                }
                
                destBytes[ destIdx++ ] = b;
            }
            
            dest.setLength( destIdx );

            _idx += len;        
        }
    }
    
    /**
     * encodes a fixed width string, string must be null padded and null terminated
     * @param str
     * @param fixedDataSize
     */
    @Override
    public final void decodeStringFixedWidth( final ReusableString dest, final int len ) {
        
        if ( len == 0 ) {
            dest.reset();
            return;
        }
        
        rangeCheck( len );

        int    bufIdx     = _idx + len;
        int    dataLen    = len;

        do {
            final byte b = _buffer[ --bufIdx ];
            
            if ( b != 0x00 ) {
                break; // EXCLUDE padded nulls
            }
            
        } while( --dataLen > 0 );
        
        dest.ensureCapacity( dataLen );

        copy( dest.getBytes(), 0, dataLen ); // increases _idx
        
        dest.setLength( dataLen );

        _idx += (len - dataLen);        
    }

    /**
     * encodes a fixed width string, string must be null padded and null terminated
     * @param str
     * @param fixedDataSize
     */
    @Override
    public final void decodeData( final ReusableString dest, final int len ) {
        
        if ( len == 0 ) {
            dest.reset();
            return;
        }
        
        rangeCheck( len );

        int    dataLen    = len;

        dest.ensureCapacity( dataLen );

        copy( dest.getBytes(), 0, dataLen ); // increases _idx
        
        dest.setLength( dataLen );

        _idx += (len - dataLen);        
    }

    @Override
    public final void skip( int size ) {
        _idx += size;
        if ( _idx > _maxIdx ) {
            throw new RuntimeDecodingException( "BinaryDecoderUtils.skip cant skip " + size + " bytes from idx " + 
                                                (_idx-size) + ", as maxIdx is " + _maxIdx );
        }
    }
    
    protected final void copy( final byte[] dest, final int offset, final int len ) {

        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            
            final int max = offset + len;
            int i=offset;
            
            while( i < max ) {
                dest[ i++ ] = _buffer[ _idx++ ] ;
            }
        } else {
            System.arraycopy( _buffer, _idx, dest, offset, len );
            _idx += len;
        }
    }
}
