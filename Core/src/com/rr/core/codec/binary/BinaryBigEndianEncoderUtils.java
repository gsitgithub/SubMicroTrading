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
public class BinaryBigEndianEncoderUtils extends AbstractBinaryEncoderUtils {

    public BinaryBigEndianEncoderUtils( byte[] buffer, int offset ) {
        super( buffer, offset );
    }
    
    @Override
    public final void encodeLong( final long value ) {
        
        if ( value == Constants.UNSET_LONG ) {
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
        } else {
            _buffer[_idx++] = (byte) (value >> 56);
            _buffer[_idx++] = (byte) (value >> 48);
            _buffer[_idx++] = (byte) (value >> 40);
            _buffer[_idx++] = (byte) (value >> 32);
            _buffer[_idx++] = (byte) (value >> 24);
            _buffer[_idx++] = (byte) (value >> 16);
            _buffer[_idx++] = (byte) (value >> 8);
            _buffer[_idx++] = (byte) (value >> 0);
        }
    }

    @Override
    public final void encodeInt( final int value ) {

        if ( value == Constants.UNSET_INT ) {
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
        } else {
            _buffer[_idx++] = (byte) (value >> 24);
            _buffer[_idx++] = (byte) (value >> 16);
            _buffer[_idx++] = (byte) (value >> 8);
            _buffer[_idx++] = (byte) (value >> 0);
        }
    }

    @Override
    public final void encodeShort( int val ) {
        final byte upper = (byte) (val >>> 8);
        final byte lower = (byte) (val & 0xFF);
        
        if ( val == Constants.UNSET_SHORT ) {
            _buffer[_idx++] = 0x00;
            _buffer[_idx++] = 0x00;
        } else {
            _buffer[ _idx++ ] = upper;
            _buffer[ _idx++ ] = lower;
        }
    }

    public static void encodeLong( byte[] buffer, int idx, long value ) {
        buffer[idx++] = (byte) (value >> 56);
        buffer[idx++] = (byte) (value >> 48);
        buffer[idx++] = (byte) (value >> 40);
        buffer[idx++] = (byte) (value >> 32);
        buffer[idx++] = (byte) (value >> 24);
        buffer[idx++] = (byte) (value >> 16);
        buffer[idx++] = (byte) (value >> 8);
        buffer[idx++] = (byte) (value >> 0);
    }
}
