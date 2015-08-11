/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import com.rr.core.lang.ZString;

public interface FixEncodeBuilder {

    public void start();
    
    public int  getNextFreeIdx();

    /**
     * @return offset in buffer for start of message
     */
    public int  getOffset();

    /**
     * @return number of bytes in the message
     */
    public int getLength();

    public void encodeString(       int tag, ZString str );
    public void encodeString(       int tag, byte[]  buf, int offset, int len );
    public void encodePrice(        int tag, double  price );
    public void encodeLong(         int tag, long    val );
    public void encodeInt(          int tag, int     val );
    public void encodeByte(         int tag, byte    code );
    public void encodeTwoByte(      int tag, byte[]  code );
    public void encodeBytes(        int tag, byte[]  code );
    public void encodeBool(         int tag, boolean isOn );
    public void encodeUTCTimestamp( int tag, int     msFromStartOfDayUTC );
    public void encodeDate(         int tag, int     yyyymmdd );
    
    /**
     * write the fix version and len tags and appends checksum
     */
    public void encodeEnvelope();
    
    
    /**
     * special tag for stats
     */
    public void encodeAckStats( long orderIn, long orderOut, long ackIn, long ackOut );
}
