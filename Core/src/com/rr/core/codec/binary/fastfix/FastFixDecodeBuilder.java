/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.BinaryDecodeBuilder;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.TimeZoneCalculator;

/**
 * optimistic Fast Fix decoder, if data is corrupt could generate an ArrayIndexOutOfBounds exception
 */
public final class FastFixDecodeBuilder implements BinaryDecodeBuilder {

    public static final double  KEEP_DECIMAL_PLACE_FACTOR = 100000000D;

    private static final int MAX_STRING_LEN = 1000; // remember must fit in a packet !
    
    protected byte[]             _buffer;
    protected int                _startOffset;
    protected int                _idx;
    protected int                _msgLen;
    protected   int              _maxIdx;
    protected TimeZoneCalculator _tzCalc = new TimeZoneCalculator();

    public FastFixDecodeBuilder() {
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
    public int getMaxIdx() {
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

    /**
     * decode the preamble 4 byte big endian
     * 
     * @TODO refactor out to CME instance
     * 
     * @return
     */
    public int decodeSeqNum() {
        return ( ((_buffer[_idx++] & 0xFF) << 24) |
                ((_buffer[_idx++] & 0xFF) << 16) |
                ((_buffer[_idx++] & 0xFF) << 8)  |
                ((_buffer[_idx++] & 0xFF) << 0) );
    }

    /**
     * decode long which was encoded as 8 byte vector 
     * 
     * @TODO refactor out to CME instance
     * 
     * @return
     */
    public long decodeLargeSeqNum() {
        return ( 
                ((_buffer[_idx++] & 0xFF) << 56) |
                ((_buffer[_idx++] & 0xFF) << 48) |
                ((_buffer[_idx++] & 0xFF) << 40) |
                ((_buffer[_idx++] & 0xFF) << 32) |
                ((_buffer[_idx++] & 0xFF) << 24) |
                ((_buffer[_idx++] & 0xFF) << 16) |
                ((_buffer[_idx++] & 0xFF) << 8)  |
                ((_buffer[_idx++] & 0xFF) << 0) );
    }

    public byte decodeChannel() {
        return _buffer[_idx++];
    }
    
    @Override
    public void decodeString( final ReusableString dest ) {

        // dont reset the dest it may contain delta 
        
        final int startIdx = _idx;
        
        while( (_buffer[_idx++] & FastFixUtils.STOP_BIT) == 0 ) {
            // 
        }
        
        if ( dest == null ) return; // skip value
        
        final int max = _idx;
        final int extraLen = max - startIdx;
        final int prevLen = dest.length();
        final int newLen = prevLen + extraLen;
        
        dest.ensureCapacity( newLen );
        final byte[] destBuf = dest.getBytes(); // get AFTER ensureCapacity
        
        if ( extraLen > 2 ) {
            if ( extraLen > MAX_STRING_LEN ) {
                throwDecodeException( "String too big len=" + extraLen + ", max is " + MAX_STRING_LEN );
            }
            
            final int lastIdx = newLen-1;
            int i=prevLen;
            int j=startIdx;
            
            while( j < max ) {
                destBuf[ i++ ] = _buffer[ j++ ];
            }

            destBuf[ lastIdx ] &= FastFixUtils.DATA_BIT_MASK;
            
        } else if ( extraLen == 2 ) { // check for empty string
            final byte b1 = _buffer[startIdx];
            final byte b2 = _buffer[startIdx+1];
            if ( b1 == (byte)0x00 && b2 == FastFixUtils.STOP_BIT ) {
                return;
            }

            destBuf[ prevLen ] = b1;
            destBuf[ prevLen+1 ] = (byte) (b2 & FastFixUtils.DATA_BIT_MASK);
            
        } else if ( extraLen == 1 ) {
            final byte b1 = _buffer[startIdx];
            
            if ( b1 == FastFixUtils.NULL_BYTE ) {
                return;
            }
            
            destBuf[ prevLen ] = (byte) (b1 & FastFixUtils.DATA_BIT_MASK);
        }
        
        dest.setLength( prevLen + extraLen );
    }

    public void decodeMandByteVector( final ReusableString dest ) {

        // dont reset the dest it may contain delta 

        final int len = decodeMandInt();
        
        if ( len == 0 ) {
            return;
        }
        
        final int startIdx = _idx;
        _idx += len;
        
        if ( dest == null ) return; // skip value
        
        final int max = _idx;
        final int extraLen = max - startIdx;
        final int prevLen = dest.length();
        final int newLen = prevLen + extraLen;
        
        dest.ensureCapacity( newLen );
        final byte[] destBuf = dest.getBytes(); // get AFTER ensureCapacity
        
        if ( extraLen > MAX_STRING_LEN ) {
            throwDecodeException( "ByteVector too big len=" + extraLen + ", max is " + MAX_STRING_LEN );
        }
        
        int i=prevLen;
        int j=startIdx;
        
        while( j < max ) {
            destBuf[ i++ ] = _buffer[ j++ ];
        }

        dest.setLength( prevLen + extraLen );
    }

    public void decodeOptionalByteVector( final ReusableString dest ) {

        // dont reset the dest it may contain delta 

        final int len = decodeOptionalInt();
        
        if ( len == 0 || len == Constants.UNSET_INT ) {
            return;
        }
        
        final int startIdx = _idx;
        _idx += len;
        
        if ( dest == null ) return; // skip value
        
        final int max = _idx;
        final int extraLen = max - startIdx;
        final int prevLen = dest.length();
        final int newLen = prevLen + extraLen;
        
        dest.ensureCapacity( newLen );
        final byte[] destBuf = dest.getBytes(); // get AFTER ensureCapacity
        
        if ( extraLen > MAX_STRING_LEN ) {
            throwDecodeException( "ByteVector too big len=" + extraLen + ", max is " + MAX_STRING_LEN );
        }
        
        int i=prevLen;
        int j=startIdx;
        
        while( j < max ) {
            destBuf[ i++ ] = _buffer[ j++ ];
        }

        dest.setLength( prevLen + extraLen );
    }

    public long decodeOptionalULong() {
        int idxSave = _idx;

        if ( _buffer[_idx] == FastFixUtils.NULL_BYTE ) {
            ++_idx;
            return Constants.UNSET_LONG;
        }

        long value = 0;
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00 ) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        if ( _idx - idxSave > FastFixUtils.MAX_LONG_BYTES ) {
            throwDecodeException( "FastFixDecoder.decodeOptionalULong overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_LONG_BYTES + " bytes" );
        }
        
        return --value; // optional positive numbers are incremented by one
    }

    public long decodeOptionalULongOverflow() {
        int idxSave = _idx;

        if ( _buffer[_idx] == FastFixUtils.NULL_BYTE ) {
            ++_idx;
            return Constants.UNSET_LONG;
        }

        final byte firstByte = _buffer[_idx];

        long value = 0;
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00 ) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        final int len = _idx - idxSave;
        
        if ( len < FastFixUtils.MAX_LONG_BYTES ) {
            return --value; // optional positive numbers are incremented by one
        }
        
        if ( len == FastFixUtils.MAX_LONG_BYTES ) {
            boolean overflow = (firstByte & FastFixUtils.OVERFLOW_BIT) != 0;
            if ( overflow ) {   // dont want to process overflow values
                throwDecodeException( "FastFixDecoder.decodeOptionalLongOverflow overflow idx " + 
                                                    (_idx-_startOffset) + ", val " + value );
            }
        } else {
            throwDecodeException( "FastFixDecoder.decodeOptionalLongOverflow overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_LONG_BYTES + " bytes" );
        }
        
        return --value; // optional positive numbers are incremented by one
    }

    public long decodeOptionalLong() {
        int idxSave = _idx;

        if ( _buffer[_idx] == FastFixUtils.NULL_BYTE ) {
            ++_idx;
            return Constants.UNSET_LONG;
        }

        boolean positive = ((_buffer[_idx] & FastFixUtils.SIGN_BIT) == 0);
        long value = (positive) ? 0 : -1; // neg nums require upper bits to be 1 not 0
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00 ) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        if ( _idx - idxSave > FastFixUtils.MAX_LONG_BYTES ) {
            throwDecodeException( "FastFixDecoder.decodeOptionalLong overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_LONG_BYTES + " bytes" );
        }
        
        return (positive) ? --value : value; // optional positive numbers are incremented by one
    }

    public long decodeOptionalLongOverflow() {
        int idxSave = _idx;

        if ( _buffer[_idx] == FastFixUtils.NULL_BYTE ) {
            ++_idx;
            return Constants.UNSET_LONG;
        }

        final byte firstByte = _buffer[_idx];
        boolean positive = ((firstByte & FastFixUtils.SIGN_BIT) == 0);
        long value = (positive) ? 0 : -1; // neg nums require upper bits to be 1 not 0
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00 ) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        final int len = _idx - idxSave;
        
        if ( len < FastFixUtils.MAX_LONG_BYTES ) {
            return (positive) ? --value : value; // optional positive numbers are incremented by one
        }
        
        if ( len == FastFixUtils.MAX_LONG_BYTES ) {
            boolean overflow = (firstByte & FastFixUtils.OVERFLOW_BIT) != 0;
            if ( overflow ) { // dont want to process overflow values
                throwDecodeException( "FastFixDecoder.decodeOptionalLongOverflow overflow idx " + 
                                                    (_idx-_startOffset) + ", val " + value );
            }
        } else {
            throwDecodeException( "FastFixDecoder.decodeOptionalLongOverflow overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_LONG_BYTES + " bytes" );
        }
        
        return (positive) ? --value : value; // optional positive numbers are incremented by one
    }

    public long decodeMandULong() {
        int idxSave = _idx;

        long value = 0;
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        if ( _idx - idxSave > FastFixUtils.MAX_LONG_BYTES ) {
            throwDecodeException( "FastFixDecoder.decodeMandULong overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_LONG_BYTES + " bytes" );
        }
        
        return value; 
    }

    public long decodeMandULongOverflow() {
        int idxSave = _idx;

        final byte firstByte = _buffer[_idx];

        long value = 0;
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        final int len = _idx - idxSave;
        
        if ( len < FastFixUtils.MAX_LONG_BYTES ) return value;
        
        if ( len == FastFixUtils.MAX_LONG_BYTES ) {
            boolean overflow = (firstByte & FastFixUtils.OVERFLOW_BIT) != 0;
            if ( overflow ) { // dont want to process overflow values
                throwDecodeException( "FastFixDecoder.decodeMandULongOverflow overflow idx " + 
                                                    (_idx-_startOffset) + ", val " + value );
            }
        } else {
            throwDecodeException( "FastFixDecoder.decodeMandULongOverflow overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_LONG_BYTES + " bytes" );
        }
        
        return value; 
    }

    public long decodeMandLong() {
        int idxSave = _idx;

        boolean positive = ((_buffer[_idx] & FastFixUtils.SIGN_BIT) == 0);
        long value = (positive) ? 0 : -1; // neg nums require upper bits to be 1 not 0
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        if ( _idx - idxSave > FastFixUtils.MAX_LONG_BYTES ) {
            throwDecodeException( "FastFixDecoder.decodeMandLong overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_LONG_BYTES + " bytes" );
        }
        
        return value; 
    }

    public long decodeMandLongOverflow() {
        int idxSave = _idx;

        final byte firstByte = _buffer[_idx];
        boolean positive = ((firstByte & FastFixUtils.SIGN_BIT) == 0);
        long value = (positive) ? 0 : -1; // neg nums require upper bits to be 1 not 0
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        final int len = _idx - idxSave;
        
        if ( len < FastFixUtils.MAX_LONG_BYTES ) return value;
        
        if ( len == FastFixUtils.MAX_LONG_BYTES ) {
            boolean overflow = (firstByte & FastFixUtils.OVERFLOW_BIT) != 0;
            if ( overflow ) { // dont want to process overflow values
                throwDecodeException( "FastFixDecoder.decodeMandLongOverflow overflow idx " + 
                                                    (_idx-_startOffset) + ", val " + value );
            }
        } else {
            throwDecodeException( "FastFixDecoder.decodeMandLongOverflow overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_LONG_BYTES + " bytes" );
        }
        
        return value; 
    }
    
    
    public int decodeOptionalUInt() {
        int idxSave = _idx;

        if ( _buffer[_idx] == FastFixUtils.NULL_BYTE ) {
            ++_idx;
            return Constants.UNSET_INT;
        }

        int value = 0;
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00 ) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        if ( _idx - idxSave > FastFixUtils.MAX_INT_BYTES ) {
            throwDecodeException( "FastFixDecoder.decodeOptionalUInt overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_INT_BYTES + " bytes" );
        }
        
        return --value; // optional positive numbers are incremented by one
    }

    public int decodeOptionalUIntOverflow() {
        int idxSave = _idx;

        final byte firstByte = _buffer[_idx];
        
        if ( _buffer[_idx] == FastFixUtils.NULL_BYTE ) {
            ++_idx;
            return Constants.UNSET_INT;
        }

        int value = 0;
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00 ) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        final int len = _idx - idxSave;
        
        if ( len < FastFixUtils.MAX_INT_BYTES ) {
            return --value; // optional positive numbers are incremented by one
        }
        
        if ( len == FastFixUtils.MAX_INT_BYTES ) {
            boolean overflow = (firstByte & FastFixUtils.OVERFLOW_BIT) != 0;
            if ( overflow ) { // dont want to process overflow values
                throwDecodeException( "FastFixDecoder.decodeOptionalUIntOverflow overflow idx " + 
                                                    (_idx-_startOffset) + ", val " + value );
            }
        } else {
            throwDecodeException( "FastFixDecoder.decodeOptionalUIntOverflow overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_INT_BYTES + " bytes" );
        }
        
        return --value; // optional positive numbers are incremented by one
    }

    public int decodeOptionalIntOverflow() {
        int idxSave = _idx;

        final byte firstByte = _buffer[_idx];
        
        if ( firstByte == FastFixUtils.NULL_BYTE ) {
            ++_idx;
            return Constants.UNSET_INT;
        }

        boolean positive = ((firstByte & FastFixUtils.SIGN_BIT) == 0);
        int value = (positive) ? 0 : -1; // neg nums require upper bits to be 1 not 0
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00 ) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        final int len = _idx - idxSave;
        
        if ( len < FastFixUtils.MAX_INT_BYTES ) {
            return (positive) ? --value : value; // optional positive numbers are incremented by one
        }
        
        if ( len == FastFixUtils.MAX_INT_BYTES ) {
            boolean overflow = (firstByte & FastFixUtils.OVERFLOW_BIT) != 0;
            if ( overflow ) { // dont want to process overflow values
                throwDecodeException( "FastFixDecoder.decodeOptionalIntOverflow overflow idx " + 
                                                    (_idx-_startOffset) + ", val " + value );
            }
        } else {
            throwDecodeException( "FastFixDecoder.decodeOptionalIntOverflow overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_INT_BYTES + " bytes" );
        }
        
        return (positive) ? --value : value; // optional positive numbers are incremented by one
    }

    public int decodeOptionalInt() {
        int idxSave = _idx;

        if ( _buffer[_idx] == FastFixUtils.NULL_BYTE ) {
            ++_idx;
            return Constants.UNSET_INT;
        }

        boolean positive = ((_buffer[_idx] & FastFixUtils.SIGN_BIT) == 0);
        int value = (positive) ? 0 : -1; // neg nums require upper bits to be 1 not 0
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00 ) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        if ( _idx - idxSave > FastFixUtils.MAX_LONG_BYTES ) {
            throwDecodeException( "FastFixDecoder.decodeOptionalInt overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_INT_BYTES + " bytes" );
        }
        
        return (positive) ? --value : value; // optional positive numbers are incremented by one
    }

    public int decodeMandUInt() {
        int idxSave = _idx;

        int value = 0;
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        if ( _idx - idxSave > FastFixUtils.MAX_INT_BYTES ) {
            throwDecodeException( "FastFixDecoder.decodeMandUInt overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_INT_BYTES + " bytes" );
        }
        
        return value; 
    }

    public int decodeMandUIntOverflow() {
        int idxSave = _idx;

        final byte firstByte = _buffer[_idx];

        int value = 0;
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        final int len = _idx - idxSave;
        
        if ( len < FastFixUtils.MAX_INT_BYTES ) return value;
        
        if ( len == FastFixUtils.MAX_INT_BYTES ) {
            boolean overflow = (firstByte & FastFixUtils.OVERFLOW_BIT) != 0;
            if ( overflow ) { // dont want to process overflow values
                throwDecodeException( "FastFixDecoder.decodeMandUIntOverflow overflow idx " + 
                                                    (_idx-_startOffset) + ", val " + value );
            }
        } else {
            throwDecodeException( "FastFixDecoder.decodeMandUIntOverflow overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_INT_BYTES + " bytes" );
        }
        
        return value; 
    }

    public int decodeMandInt() {
        int idxSave = _idx;

        boolean positive = ((_buffer[_idx] & FastFixUtils.SIGN_BIT) == 0);
        int value = (positive) ? 0 : -1; // neg nums require upper bits to be 1 not 0
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        if ( _idx - idxSave > FastFixUtils.MAX_INT_BYTES ) {
            throwDecodeException( "FastFixDecoder.decodeMandInt overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_INT_BYTES + " bytes" );
        }
        
        return value; 
    }
    
    public int decodeMandIntOverflow() {
        int idxSave = _idx;

        final byte firstByte = _buffer[_idx];
        boolean positive = ((firstByte & FastFixUtils.SIGN_BIT) == 0);
        int value = (positive) ? 0 : -1; // neg nums require upper bits to be 1 not 0
        
        while( (_buffer[_idx] & FastFixUtils.NULL_BYTE) == 0x00) {
            value = (value << 7) | _buffer[_idx++] ; // current byte has no stop bit so dont need mask it
        }
        
        value = (value << 7) | (_buffer[_idx++] & FastFixUtils.DATA_BIT_MASK); // if only one byte value will be zero and wont have been in while loop
        
        final int len = _idx - idxSave;
        
        if ( len < FastFixUtils.MAX_INT_BYTES ) return value;
        
        if ( len == FastFixUtils.MAX_INT_BYTES ) {
            boolean overflow = (firstByte & FastFixUtils.OVERFLOW_BIT) != 0;
            if ( overflow ) { // dont want to process overflow values
                throwDecodeException( "FastFixDecoder.decodeMandIntOverflow overflow idx " + 
                                                    (_idx-_startOffset) + ", val " + value );
            }
        } else {
            throwDecodeException( "FastFixDecoder.decodeMandIntOverflow overflow " + (_idx - idxSave) + " bytes from idx " + 
                                                (_idx-_startOffset) + ", as max is " + FastFixUtils.MAX_INT_BYTES + " bytes" );
        }
        
        return value; 
    }
    
    /**
     * when invoked the current index points to start of the presence map
     * skips the rest of the PMap so field encoding starts at next data byte after the PMap
     * 
     * @return lastIndex of the pMap
     */
    public int skipPMap() {
        while( (_buffer[_idx] & FastFixUtils.STOP_BIT) == 0 )  {
            ++_idx;
        }
        
        return( _idx++ );
    }

    @Override
    public void clear() {
        _idx    = 0;
    }

    @Override
    public byte[] getBuffer() {
        return _buffer;
    }

    @Override
    public int getCurrentIndex() {
        return _idx;
    }

    public void dump( ReusableString info ) {
        int len = _maxIdx - _startOffset;
        
        int idx       = getCurrentIndex(); 
        int msgBadIdx = idx - _startOffset;

        info.reset();
        info.append( ", len=" ).append( len ).append( ", idx=" ).append( _idx ).append( ", offset=" ).append( _startOffset );
        info.append( ", offsetWithinMsg=" ).append( msgBadIdx );
        info.append( " HEX_MSG=[" ).appendHEX( _buffer, _startOffset, len ).append( "]");
    }
    
    public final void throwDecodeException( String errMsg ) {
        int len = _maxIdx - _startOffset;
        
        ReusableString copy = TLC.instance().getString();
        
        copy.setValue( _buffer, _startOffset, len );
        
        int idx       = getCurrentIndex(); 
        int msgBadIdx = idx - _startOffset;
        
        throw new RuntimeDecodingException( errMsg + ", len=" + len + ", idx=" + idx + ", offset=" + _startOffset + 
                                            ", offsetWithinMsg=" + msgBadIdx, copy );
    }
    
    // @TODO revisit interface ... perhaps shift below to new StandardBinaryDecoder 
    
    @Override public void skip( int size ) { throw new RuntimeDecodingException( "FastFixDecoder.skip not supported by this decoder" ); }
    @Override public int decodeTimestampUTC() { throw new RuntimeDecodingException( "FastFixDecoder.decodeTimestampUTC not supported by this decoder" ); }
    @Override public int decodeTimeLocal() { throw new RuntimeDecodingException( "FastFixDecoder.decodeTimeLocal not supported by this decoder" ); }
    @Override public int decodeDate() { throw new RuntimeDecodingException( "FastFixDecoder.decodeDate not supported by this decoder" ); }
    @Override public int decodeTimeUTC() { throw new RuntimeDecodingException( "FastFixDecoder.decodeTimeUTC not supported by this decoder" ); }
    @Override public int decodeTimestampLocal() { throw new RuntimeDecodingException( "FastFixDecoder.decodeTimestampLocal not supported by this decoder" ); }
    @Override public void decodeLongToString( ReusableString dest ) { throw new RuntimeDecodingException( "FastFixDecoder.decodeLongToString not supported by this decoder" ); }
    @Override public void decodeIntToString( ReusableString dest ) { throw new RuntimeDecodingException( "FastFixDecoder.decodeIntToString not supported by this decoder" ); }
    @Override public void decodeZStringFixedWidth( ReusableString dest, int len ) { throw new RuntimeDecodingException( "FastFixDecoder.decodeZStringFixedWidth not supported by this decoder" ); }
    @Override public void decodeStringFixedWidth( ReusableString dest, int len ) { throw new RuntimeDecodingException( "FastFixDecoder.decodeStringFixedWidth not supported by this decoder" ); }
    @Override public void decodeData( ReusableString dest, int len ) { throw new RuntimeDecodingException( "FastFixDecoder.decodeData not supported by this decoder" ); }
    @Override public byte decodeByte() { throw new RuntimeDecodingException( "FastFixDecoder.decodeByte not supported by this decoder" ); }
    @Override public double decodePrice() { throw new RuntimeDecodingException( "FastFixDecoder.decodePrice not supported by this decoder" ); }
    @Override public short decodeShort() { throw new RuntimeDecodingException( "FastFixDecoder.decodeShort not supported by this decoder" ); }
    @Override public int decodeUInt() { throw new RuntimeDecodingException( "FastFixDecoder.decodeUInt not supported by this decoder" ); }
    @Override public int decodeInt() { throw new RuntimeDecodingException( "FastFixDecoder.decodeInt not supported by this decoder" ); }
    @Override public long decodeULong() { throw new RuntimeDecodingException( "FastFixDecoder.decodeULong not supported by this decoder" ); }
    @Override public long decodeLong() { throw new RuntimeDecodingException( "FastFixDecoder.decodeLong not supported by this decoder" ); }
    @Override public short decodeUShort() { throw new RuntimeDecodingException( "FastFixDecoder.decodeUShort not supported by this decoder" ); }
    @Override public byte decodeUByte() { throw new RuntimeDecodingException( "FastFixDecoder.decodeUByte not supported by this decoder" ); }
    @Override public byte decodeChar() { throw new RuntimeDecodingException( "FastFixDecoder.decodeChar not supported by this decoder" ); }
    @Override public boolean decodeBool() { throw new RuntimeDecodingException( "FastFixDecoder.decodeBool not supported by this decoder" ); }
    @Override public double decodeDecimal() { throw new RuntimeDecodingException( "FastFixDecoder.decodeDecimal not supported by this decoder" ); }
    @Override public int decodeQty() { throw new RuntimeDecodingException( "FastFixDecoder.decodeQty not supported by this decoder" ); }
}
