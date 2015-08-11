/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix;

import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.codec.binary.BinaryEncodeBuilder;
import com.rr.core.lang.Constants;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;

/**
 * the fast fix binary encode builder 
 * used for encoding fast fix on wire
 *
 * @author Richard Rose
 */
public final class FastFixBuilder implements BinaryEncodeBuilder {

    protected final byte[]             _buffer;
    protected final int                _startOffset;
    protected       int                _idx;
    protected       int                _msgLen;

    protected       TimeZoneCalculator _tzCalc = new TimeZoneCalculator();

    public FastFixBuilder( byte[] buffer, int offset ) {
        _buffer            = buffer;
        _idx               = offset;
        _startOffset       = offset;
    }
    
    @Override
    public final void start() {
        _idx = _startOffset;
    }
    
    @Override
    public void start( final int msgType ) {
        _idx              = _startOffset;
        _buffer[ _idx++ ] = (byte)msgType;
    }
    
    /**
     * encode the seqnum to 4 byte big endian  (not fast fix format)
     * 
     * @TODO refactor out to CME instance
     * 
     * @return
     */
    public void encodeSeqNum( final int value ) {
        _buffer[_idx++] = (byte) (value >> 24);
        _buffer[_idx++] = (byte) (value >> 16);
        _buffer[_idx++] = (byte) (value >> 8);
        _buffer[_idx++] = (byte) (value >> 0);
    }

    public void encodeChannel( final byte subchannel ) {
        _buffer[_idx++] = subchannel;
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
    public int getCurLength() {
        return _idx - _startOffset;
    }

    @Override
    public int end() {
        _msgLen = _idx - _startOffset;
        return _msgLen;
    }
    
    @Override
    public final void encodeString( final byte[] buf, final int offset, int len ) { // @TODO verify only used by unit test

        if ( len == 0 ) {
            _buffer[_idx++] = (byte)0x00; // zero byte string
            encodeNull();
            return;
        }

        copyWithStopBit( buf, offset, len );
    }
    
    @Override
    public final void encodeString( final ZString str ) { // optional String encoder
        
        if ( str == null ) {
            encodeNull();
            return;
        }
        
        int len = str.length();
        
        if ( len == 0 ) {
            _buffer[_idx++] = (byte)0x00; // zero byte string
            encodeNull();
            return;
        }
            
        byte[] value = str.getBytes();

        copyWithStopBit( value, str.getOffset(), len );
    }

    public final void encodeStringBytes( final byte[] value, int offset, int len ) {
        
        if ( len == 0 ) { 
            encodeNull();
            return;
        }
            
        copyWithStopBit( value, offset, len );
    }

    private final void copyWithStopBit( final byte[] value, final int offset, final int len ) {

        final int max = offset + len;
        int i=offset;
        
        while( i < max ) {
            _buffer[ _idx++ ] = (value[ i++ ] );
        }

        _buffer[_idx-1] |= FastFixUtils.STOP_BIT;
    }
    
    public final void encodeMandByteVector( final byte[] value, final int offset, int len ) { // optional String encoder
        
        encodeMandInt( len );

        copyNoStopBit( value, offset, len );
    }

    public final void encodeOptionalByteVector( final byte[] value, final int offset, int len ) { // optional String encoder
        
        if ( value == null ) {
            encodeOptionalInt( Constants.UNSET_INT );
            return;
        }
        
        encodeOptionalInt( len );

        copyNoStopBit( value, offset, len );
    }

    private final void copyNoStopBit( final byte[] value, final int offset, final int len ) {

        final int max = offset + len;
        int i=offset;
        
        while( i < max ) {
            _buffer[ _idx++ ] = (value[ i++ ] );
        }
    }
    
    public void encodeOptionalULong( long value ) {
        if ( value == Constants.UNSET_LONG ) {  
            _buffer[_idx++] = FastFixUtils.STOP_BIT;
            return;
        }

        value++;        // must add one for optional non negative numbers to make space for null
        
        final int len = FastFixUtils.getULongLen( value );

        switch( len ) {
        case 10: _buffer[_idx++] = (byte)((value >>> 63) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 9:  _buffer[_idx++] = (byte)((value >>> 56) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 8:  _buffer[_idx++] = (byte)((value >>> 49) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 7:  _buffer[_idx++] = (byte)((value >>> 42) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 6:  _buffer[_idx++] = (byte)((value >>> 35) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 5:  _buffer[_idx++] = (byte)((value >>> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >>> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >>> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >>> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }
    
    public void encodeOptionalULongOverflow( long value, final boolean overflow ) {
        if ( value == Constants.UNSET_LONG ) {  
            _buffer[_idx++] = FastFixUtils.STOP_BIT;
            return;
        }

        byte firstByteMask = FastFixUtils.DATA_BIT_MASK;
        
        value++;        // must add one for optional non negative numbers to make space for null
        
        final int len = (overflow) ? FastFixUtils.MAX_LONG_BYTES : FastFixUtils.getULongLen( value );

        switch( len ) {
        case 10: 
            byte d = (byte) (((value >>> 63) & FastFixUtils.OVERFLOW_MSB_BIT_MASK & firstByteMask));
            
            if ( overflow ) {
                d  |= FastFixUtils.OVERFLOW_BIT;
            }
            
            _buffer[_idx++] = d;
            
            //$FALL-THROUGH$
        case 9:  _buffer[_idx++] = (byte)((value >>> 56) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 8:  _buffer[_idx++] = (byte)((value >>> 49) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 7:  _buffer[_idx++] = (byte)((value >>> 42) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 6:  _buffer[_idx++] = (byte)((value >>> 35) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 5:  _buffer[_idx++] = (byte)((value >>> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >>> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >>> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >>> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }
    
    public void encodeOptionalLong( long value ) {
        if ( value == Constants.UNSET_LONG ) {  
            _buffer[_idx++] = FastFixUtils.STOP_BIT;
            return;
        }

        byte firstByteMask = FastFixUtils.DATA_BIT_MASK;
        
        if ( value >= 0 ) {
            value++;        // must add one for optional non negative numbers to make space for null
            
            if ( value < 0 ) { // MAX_LONG now MIN_LONG
                firstByteMask = FastFixUtils.LONG_MSB_MASK;
            }
        }
        
        final int len = FastFixUtils.getLongLen( value );

        switch( len ) {
        case 10: 
            _buffer[_idx++] = (byte)((value >> 63) & firstByteMask);
            //$FALL-THROUGH$
        case 9:  _buffer[_idx++] = (byte)((value >> 56) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 8:  _buffer[_idx++] = (byte)((value >> 49) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 7:  _buffer[_idx++] = (byte)((value >> 42) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 6:  _buffer[_idx++] = (byte)((value >> 35) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 5:  _buffer[_idx++] = (byte)((value >> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }

    public void encodeMandULong( final long value ) {
        final int len = FastFixUtils.getULongLen( value );

        switch( len ) {
        case 10: _buffer[_idx++] = (byte)((value >>> 63) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 9:  _buffer[_idx++] = (byte)((value >>> 56) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 8:  _buffer[_idx++] = (byte)((value >>> 49) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 7:  _buffer[_idx++] = (byte)((value >>> 42) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 6:  _buffer[_idx++] = (byte)((value >>> 35) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 5:  _buffer[_idx++] = (byte)((value >>> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >>> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >>> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >>> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }
    
    public void encodeMandULongOverflow( final long value, final boolean overflow ) {
        final int len = (overflow) ? FastFixUtils.MAX_LONG_BYTES : FastFixUtils.getULongLen( value );

        switch( len ) {
        case 10: 
            byte d = (byte) (((value >>> 63) & FastFixUtils.OVERFLOW_MSB_BIT_MASK));
            
            if ( overflow ) {
                d  |= FastFixUtils.OVERFLOW_BIT;
            }
            
            _buffer[_idx++] = d;
            
            //$FALL-THROUGH$
        case 9:  _buffer[_idx++] = (byte)((value >>> 56) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 8:  _buffer[_idx++] = (byte)((value >>> 49) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 7:  _buffer[_idx++] = (byte)((value >>> 42) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 6:  _buffer[_idx++] = (byte)((value >>> 35) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 5:  _buffer[_idx++] = (byte)((value >>> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >>> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >>> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >>> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }
    
    
    
    public void encodeMandLong( final long value ) {
        
        final int len = FastFixUtils.getLongLen( value );

        switch( len ) {
        case 10: _buffer[_idx++] = (byte)((value >> 63) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 9:  _buffer[_idx++] = (byte)((value >> 56) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 8:  _buffer[_idx++] = (byte)((value >> 49) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 7:  _buffer[_idx++] = (byte)((value >> 42) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 6:  _buffer[_idx++] = (byte)((value >> 35) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 5:  _buffer[_idx++] = (byte)((value >> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
        
    }

    public void encodeOptionalUInt( int value ) {
        if ( value == Constants.UNSET_INT ) {  
            _buffer[_idx++] = FastFixUtils.STOP_BIT;
            return;
        }

        value++;        // must add one for optional non negative numbers to make space for null
        
        final int len = FastFixUtils.getUIntLen( value );

        switch( len ) {
        case 5:  _buffer[_idx++] = (byte)((value >>> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >>> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >>> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >>> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }
    
    public void encodeOptionalUIntOverflow( int value, final boolean overflow ) {
        if ( value == Constants.UNSET_INT ) {  
            _buffer[_idx++] = FastFixUtils.STOP_BIT;
            return;
        }

        byte firstByteMask = FastFixUtils.DATA_BIT_MASK;
        
        value++;        // must add one for optional non negative numbers to make space for null
        
        final int len = (overflow) ? FastFixUtils.MAX_INT_BYTES : FastFixUtils.getUIntLen( value );

        switch( len ) {
        case 5: 
            byte d = (byte) (((value >>> 28) & FastFixUtils.OVERFLOW_MSB_BIT_MASK & firstByteMask));
            
            if ( overflow ) {
                d  |= FastFixUtils.OVERFLOW_BIT;
            }
            
            _buffer[_idx++] = d;
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >>> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >>> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >>> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }
    
    public void encodeOptionalInt( int value ) {
        if ( value == Constants.UNSET_INT ) {  
            _buffer[_idx++] = FastFixUtils.STOP_BIT;
            return;
        }

        byte firstByteMask = FastFixUtils.DATA_BIT_MASK;
        
        if ( value >= 0 ) {
            value++;        // must add one for optional non negative numbers to make space for null
            
            if ( value < 0 ) { // MAX_INT now MIN_INT
                firstByteMask = FastFixUtils.INT_MSB_MASK;
            }
        }
        
        final int len = FastFixUtils.getIntLen( value );

        switch( len ) {
        case 5:  _buffer[_idx++] = (byte)((value >> 28) & firstByteMask);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }

    public void encodeMandUInt( final int value ) {
        final int len = FastFixUtils.getUIntLen( value );

        switch( len ) {
        case 5:  _buffer[_idx++] = (byte)((value >>> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >>> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >>> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >>> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }
    
    public void encodeMandUIntOverflow( final int value, final boolean overflow ) {
        final int len = (overflow) ? FastFixUtils.MAX_INT_BYTES : FastFixUtils.getUIntLen( value );

        switch( len ) {
        case 5: 
            byte d = (byte) (((value >>> 28) & FastFixUtils.OVERFLOW_MSB_BIT_MASK));
            
            if ( overflow ) {
                d  |= FastFixUtils.OVERFLOW_BIT;
            }
            
            _buffer[_idx++] = d;
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >>> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >>> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >>> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }
    
    public void encodeMandInt( final int value ) {
        
        final int len = FastFixUtils.getIntLen( value );

        switch( len ) {
        case 5:  _buffer[_idx++] = (byte)((value >> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
        
    }

    public void encodeOptionalLongOverflow( long value, boolean overflow ) {
        if ( value == Constants.UNSET_LONG ) {  
            _buffer[_idx++] = FastFixUtils.STOP_BIT;
            return;
        }

        byte firstByteMask = FastFixUtils.DATA_BIT_MASK;
        
        if ( value >= 0 ) {
            value++;        // must add one for optional non negative numbers to make space for null
            
            if ( value < 0 ) { // MAX_LONG now MIN_LONG
                firstByteMask = FastFixUtils.LONG_MSB_MASK;
            }
        }
        
        final int len = (overflow) ? FastFixUtils.MAX_LONG_BYTES : FastFixUtils.getLongLen( value );

        switch( len ) {
        case 10: 
            byte d = (byte) (((value >> 63) & FastFixUtils.OVERFLOW_MSB_BIT_MASK & firstByteMask));
            
            if ( overflow ) {
                d  |= FastFixUtils.OVERFLOW_BIT;
            }
            
            _buffer[_idx++] = d;
            
            //$FALL-THROUGH$
        case 9:  _buffer[_idx++] = (byte)((value >> 56) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 8:  _buffer[_idx++] = (byte)((value >> 49) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 7:  _buffer[_idx++] = (byte)((value >> 42) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 6:  _buffer[_idx++] = (byte)((value >> 35) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 5:  _buffer[_idx++] = (byte)((value >> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }

    public void encodeMandLongOverflow( final long value, boolean overflow ) {
        
        final int len = (overflow) ? FastFixUtils.MAX_LONG_BYTES : FastFixUtils.getLongLen( value );

        switch( len ) {
        case 10: 
            byte d = (byte) (((value >> 63) & FastFixUtils.OVERFLOW_MSB_BIT_MASK));
            
            if ( overflow ) {
                d  |= FastFixUtils.OVERFLOW_BIT;
            }
            
            _buffer[_idx++] = d;
            
            //$FALL-THROUGH$
        case 9:  _buffer[_idx++] = (byte)((value >> 56) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 8:  _buffer[_idx++] = (byte)((value >> 49) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 7:  _buffer[_idx++] = (byte)((value >> 42) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 6:  _buffer[_idx++] = (byte)((value >> 35) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 5:  _buffer[_idx++] = (byte)((value >> 28) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
        
    }

    public void encodeOptionalIntOverflow( int value, boolean overflow ) {
        
        if ( value == Constants.UNSET_INT ) {  
            _buffer[_idx++] = FastFixUtils.STOP_BIT;
            return;
        }

        byte firstByteMask = FastFixUtils.DATA_BIT_MASK;
        
        if ( value >= 0 ) {
            value++;        // must add one for optional non negative numbers to make space for null
            
            if ( value < 0 ) { // MAX_INT now MIN_INT
                firstByteMask = FastFixUtils.INT_MSB_MASK;
            }
        }
        
        final int len = (overflow) ? FastFixUtils.MAX_INT_BYTES : FastFixUtils.getIntLen( value );

        switch( len ) {
        case 5: 
            byte d = (byte) (((value >> 28) & FastFixUtils.OVERFLOW_MSB_BIT_MASK & firstByteMask));
            
            if ( overflow ) {
                d  |= FastFixUtils.OVERFLOW_BIT;
            }
            
            _buffer[_idx++] = d;
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }

    public void encodeMandIntOverflow( final int value, boolean overflow ) {
        
        final int len = (overflow) ? FastFixUtils.MAX_INT_BYTES : FastFixUtils.getIntLen( value );

        switch( len ) {
        case 5: 
            byte d = (byte) (((value >> 28) & FastFixUtils.OVERFLOW_MSB_BIT_MASK));
            
            if ( overflow ) {
                d  |= FastFixUtils.OVERFLOW_BIT;
            }
            
            _buffer[_idx++] = d;
            
            //$FALL-THROUGH$
        case 4:  _buffer[_idx++] = (byte)((value >> 21) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 3:  _buffer[_idx++] = (byte)((value >> 14) & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 2:  _buffer[_idx++] = (byte)((value >> 7)  & FastFixUtils.DATA_BIT_MASK);
            //$FALL-THROUGH$
        case 1:  _buffer[_idx++] = (byte)((value & FastFixUtils.DATA_BIT_MASK) | FastFixUtils.STOP_BIT);
            break;
        }
    }

    public void insertByte( final int insertIdx ) {
        int maxIndex     = _idx++;        // INCREMENT THE CUR IDX PTR
        
        int curIdx       = maxIndex-1;
        int shiftedIndex = curIdx+1;  // strictly speaking should do a range check, however premise is encoded msg is less than the 8K buffer !
                
        while ( curIdx >= insertIdx ) {
            _buffer[shiftedIndex--] = _buffer[curIdx--];
        }
        
        _buffer[insertIdx] = 0;
    }
    
    public void insertBytes( final int insertIdx, int numBytes ) {
        
        int curIdx       = _idx-1;
        int shiftedIndex = curIdx+numBytes;  // strictly speaking should do a range check, however premise is encoded msg is less than the 8K buffer !
                
        while ( curIdx >= insertIdx ) {
            _buffer[shiftedIndex--] = _buffer[curIdx--];
        }
        
        while ( shiftedIndex >= insertIdx ) {
            _buffer[shiftedIndex--] = 0;
        }
        
        _idx += numBytes;
    }
    
    public void encodeNull() {
        _buffer[_idx++] = FastFixUtils.NULL_BYTE;
    }

    @Override
    public void encodeDate( int yyyymmdd ) {
        throw new RuntimeEncodingException( "FastFixEncoder.encodeDate method not implemented for this encoder" ); 
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
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        _tzCalc = calc;
    }
    
    // @NOTE refactor and split BinaryDecodeBuilder so the following isnt necessary
    
    @Override public void encodeFiller( int len ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeFiller() not implemented for this encoder" ); }
    @Override public void encodeByte( final byte code ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeByte() not implemented for this encoder" ); }
    @Override public void encodeChar( final byte code ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeChar() not implemented for this encoder" ); }
    @Override public void encodePrice( final double price ) { throw new RuntimeEncodingException( "FastFixEncoder.encodePrice() not implemented for this encoder" ); }
    @Override public void encodeShort( final int val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeShort() not implemented for this encoder" ); }
    @Override public void encodeQty( final int val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeQty() not implemented for this encoder" ); }
    @Override public void encodeInt( final int val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeInt() not implemented for this encoder" ); }
    @Override public void encodeLong( final long val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeLong() not implemented for this encoder" ); }
    @Override public void encodeDecimal( final double val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeDecimal() not implemented for this encoder" ); }
    @Override public void encodeUByte( final byte val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeUByte() not implemented for this encoder" ); }
    @Override public void encodeUShort( final short val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeUShort() not implemented for this encoder" ); }
    @Override public void encodeUInt( final int val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeUInt() not implemented for this encoder" ); }
    @Override public void encodeULong( final long val ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeULong() not implemented for this encoder" ); }
    @Override public void encodeTimestampUTC( int msFromStartofDayUTC ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeTimestampUTC method not implemented for this encoder" ); }
    @Override public void encodeTimeUTC( int msFromStartofDayUTC ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeTimeUTC method not implemented for this encoder" ); }
    @Override public void encodeBool( boolean isOn ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeBool method not implemented for this encoder" );     }
    @Override public void encodeZStringFixedWidth( ZString str, int len ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeZStringFixedWidth() not implemented for this encoder" ); }
    @Override public void encodeZStringFixedWidth( byte[] value, int offset, int fixedDataSize ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeZStringFixedWidth() not implemented for this encoder" ); }
    @Override public void encodeStringFixedWidth( ZString str, int len ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeStringFixedWidth() not implemented for this encoder" ); }
    @Override public void encodeStringFixedWidth( byte[] value, int offset, int fixedDataSize ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeStringFixedWidth() not implemented for this encoder" ); }
    @Override public void encodeData( ZString str, int len ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeData() not implemented for this encoder" ); }
    @Override public void encodeStringAsLong( ZString strLong ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeStringAsLong() not implemented for this encoder" ); }
    @Override public void encodeStringAsInt( ZString intLong ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeStringAsInt() not implemented for this encoder" ); }
    @Override public void encodeString( final ZString str, int maxLen ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeString() not implemented for this encoder" ); }
    @Override public void encodeBytes( byte[] buf ) { throw new RuntimeEncodingException( "FastFixEncoder.encodeBytes() not implemented for this encoder" ); }
}
