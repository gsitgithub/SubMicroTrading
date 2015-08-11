/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary;


/**
 * helper for binary decoder protocols
 *
 * can throw RuntimeEncodingException
 */
public class BinaryLittleEndianDecoderUtils extends AbstractBinaryDecoderUtils {

    @Override
    public long decodeLong() {
        
        return( (((long) _buffer[_idx++] & 0xFF) << 0) |
                (((long) _buffer[_idx++] & 0xFF) << 8) |
                (((long) _buffer[_idx++] & 0xFF) << 16) |
                (((long) _buffer[_idx++] & 0xFF) << 24) |
                (((long) _buffer[_idx++] & 0xFF) << 32) |
                (((long) _buffer[_idx++] & 0xFF) << 40) |
                (((long) _buffer[_idx++] & 0xFF) << 48)  |
                (((long) _buffer[_idx++] & 0xFF) << 56) );
    }

    @Override
    public int decodeInt() {
        
        return ( ((_buffer[_idx++] & 0xFF) << 0) |
                 ((_buffer[_idx++] & 0xFF) << 8) |
                 ((_buffer[_idx++] & 0xFF) << 16)  |
                 ((_buffer[_idx++] & 0xFF) << 24) );
    }

    @Override
    public short decodeShort() {
        
        return (short) (((_buffer[_idx++] & 0xFF) << 0) | ((_buffer[_idx++] & 0xFF) << 8) );
    }

}
