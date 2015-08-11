/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary;

import com.rr.core.lang.Constants;


/**
 * helper for binary protocols
 *
 * @NOTE DOESNT CHECK FOR BUFFER OVERRUN SO ENSURE BUF IS BIG ENOUGH
 * 
 * can throw RuntimeEncodingException
 */
public class BinaryLittleEndianEncoderUtils extends AbstractBinaryEncoderUtils {

    public BinaryLittleEndianEncoderUtils( byte[] buffer, int offset ) {
        super( buffer, offset );
    }
    
    @Override
    public final void encodeLong( final long value ) {
        
        if ( value == Constants.UNSET_LONG ) {
            doEncodeSignedLongNull();
        } else {
            doEncodeLong( value );
        }
    }

    @Override
    public void encodeInt( final int value ) {
        
        if ( value == Constants.UNSET_INT ) {
            doEncodeSignedIntNull();
        } else {
            doEncodeInt( value );
        }
    }

    @Override
    public void encodeShort( int val ) {
        if ( val == Constants.UNSET_SHORT ) {
            doEncodeSignedShortNull();
        } else {
            doEncodeShort( val );
        }
    }

    protected void doEncodeSignedLongNull() {
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
    }

    protected void doEncodeSignedIntNull() {
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
    }

    protected void doEncodeSignedShortNull() {
        _buffer[_idx++] = 0x00;
        _buffer[_idx++] = 0x00;
    }

    protected void doEncodeLong( final long value ) {
        _buffer[_idx++] = (byte) (value >> 0);
        _buffer[_idx++] = (byte) (value >> 8);
        _buffer[_idx++] = (byte) (value >> 16);
        _buffer[_idx++] = (byte) (value >> 24);
        _buffer[_idx++] = (byte) (value >> 32);
        _buffer[_idx++] = (byte) (value >> 40);
        _buffer[_idx++] = (byte) (value >> 48);
        _buffer[_idx++] = (byte) (value >> 56);
    }

    protected void doEncodeInt( final int value ) {
        _buffer[_idx++] = (byte) (value >> 0);
        _buffer[_idx++] = (byte) (value >> 8);
        _buffer[_idx++] = (byte) (value >> 16);
        _buffer[_idx++] = (byte) (value >> 24);
    }

    protected void doEncodeShort( int val ) {
        final byte upper = (byte) (val >>> 8);
        final byte lower = (byte) (val & 0xFF);
        
        _buffer[ _idx++ ] = lower;
        _buffer[ _idx++ ] = upper;
    }
}
