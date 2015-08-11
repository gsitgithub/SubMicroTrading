/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;

public interface BinaryDecodeBuilder {

    public void setTimeZoneCalculator( TimeZoneCalculator calc );
    public void setMaxIdx( int maxIdx );

    public void start( byte[] msg, int offset, int maxIdx );
    public void end();
    public void clear();

    public int getNextFreeIdx();
    public int getOffset();
    public int getLength();
    public int getMaxIdx();
    public byte[] getBuffer();
    public int getCurrentIndex();

    public void skip( int size );
    
    public double decodePrice();
    public long decodeLong();
    public long decodeULong();
    public int decodeInt();
    public int decodeUInt();
    public short decodeShort();
    public short decodeUShort();
    public byte decodeByte();
    public byte decodeUByte();
    public byte decodeChar();
    public boolean decodeBool();
    public double decodeDecimal();
    public int decodeQty();

    public int decodeTimestampUTC();
    public int decodeTimeLocal();
    public int decodeDate();
    public int decodeTimeUTC();
    public int decodeTimestampLocal();

    public void decodeString( ReusableString dest );
    public void decodeLongToString( ReusableString dest );
    public void decodeIntToString( ReusableString dest );
    
    /**
     * decode a null terminated String, first 0x00 to be treated as null terminator
     */
    public void decodeZStringFixedWidth( ReusableString dest, int len );
    
    /**
     * decode a fixed width set of bytes, strips null padding
     * @param dest
     * @param len
     */
    public void decodeStringFixedWidth( ReusableString dest, int len );

    /**
     * decode a fixed width set of bytes
     * @param dest
     * @param len
     */
    public void decodeData( ReusableString dest, int len );
}
