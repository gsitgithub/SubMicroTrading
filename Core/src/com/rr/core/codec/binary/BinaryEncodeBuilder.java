/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary;

import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;

public interface BinaryEncodeBuilder {
    
    public void   start();
    
    /**
     * start new messaqe, encode msgType as first byte of message
     * @param msgType
     */
    public void   start( int msgType );
    public int    getNextFreeIdx();
    public int    getOffset();
    /**
     * @return actual length of full message ... must call end() first to set this value
     */
    public int    getLength();
    /**
     * @return current length of message (as message is encoded this will change) 
     */
    public int    getCurLength();
    public int    end();
    public void   encodeChar( final byte code );
    public void   encodeString( final ZString str );
    public void   encodeString( final ZString str, int maxLen );
    public void   encodeString( final byte[] buf, final int offset, int len );
    public void   encodeBytes( final byte[] buf );
    public void   encodeDecimal( final double price );
    public void   encodePrice( final double price );
    public void   encodeLong( final long value );
    public void   encodeInt( final int value );
    public void   encodeQty( final int value );
    public void   encodeShort( final int val );
    public void   encodeByte( final byte code );

    // other string methods for fixed widths
    public void   encodeZStringFixedWidth( ZString str, int len );
    public void   encodeZStringFixedWidth( byte[] value, int offset, int fixedDataSize );
    public void   encodeStringFixedWidth( ZString str, int len );
    public void   encodeStringFixedWidth( byte[] value, int offset, int fixedDataSize );
    public void   encodeData( ZString str, int len );

    // iunternal String to external log/int
    public void   encodeStringAsLong( ZString strLong );
    public void   encodeStringAsInt( ZString intLong );
    
    // unsigned encoders ... required by ETI as uses different NULL for unsigned and signed
    public void   encodeULong( final long value );
    public void   encodeUInt( final int value );
    public void   encodeUShort( final short val );
    public void   encodeUByte( final byte code );
    
    /**
     * encodeDate - most encoders dont need this as date generally encoded as int or long
     * @param yyyymmdd
     */
    public void   encodeDate( final int yyyymmdd );
    public void   encodeTimestampUTC( int msFromStartofDayUTC );
    public void   encodeTimeUTC( int msFromStartofDayUTC );
    public void   encodeBool( final boolean isOn );
    public void   clear();
    public byte[] getBuffer();
    public int    getCurrentIndex();

    public void   setTimeZoneCalculator( TimeZoneCalculator calc );

    public void   encodeFiller( int len );

}
