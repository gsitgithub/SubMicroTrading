/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.codec.emea.exchange.millenium;

import java.util.TimeZone;

import com.rr.core.codec.binary.BinaryLittleEndianDecoderUtils;
import com.rr.core.lang.Constants;

/**
 * @NOTE dont check for max idx before each put the calling code must ensure buffer is big enough
 * (typically 8K)
 * 
 * each message has header 
 * 
    ProtocolVersion message start                      1 2 message start, hard coded value of '2'
    MsgLen          Message Length Num                 2 0->65535 
    MsgType         Message type Alpha                 1 A Logon
 *
 * @author Richard Rose
 */
public final class ITCHDecodeBuilderImpl extends BinaryLittleEndianDecoderUtils {

    public static final double  KEEP_DECIMAL_PLACE_FACTOR = 100000000D;
    
    public ITCHDecodeBuilderImpl() {
        super();
        _tzCalc.setLocalTimezone( TimeZone.getTimeZone( "GMT" ) );
    }

    @Override
    public int decodeTimeLocal() {
        final long msLocal = decodeInt() * 1000; 
        return _tzCalc.localToUTC( msLocal );
    }
    
    @Override
    public int decodeTimeUTC() {
        final long msUTC = decodeInt() * 1000; 
        return _tzCalc.getTimeUTC( msUTC );
    }
    
    @Override
    public int decodeTimestampLocal() {
        long unixTimeLocalSecs = decodeInt();
        long milliseconds = decodeInt() / 10;
        return _tzCalc.localToUTC( unixTimeLocalSecs * 1000 + milliseconds );
    }
    
    @Override
    public int decodeTimestampUTC() {
        long unixTimeSecs = decodeInt();
        long milliseconds = decodeInt() / 10;
        return _tzCalc.getTimeUTC( unixTimeSecs * 1000 + milliseconds );
    }
    
    @Override
    public double decodePrice() {
        
        final long value = decodeLong();

        double price;
        
        if ( value >= 0 ) {
            if ( value == Long.MAX_VALUE )  {
                price = Double.MAX_VALUE;
            } else {
                price = value / KEEP_DECIMAL_PLACE_FACTOR;
            }
        } else {
            if ( value == Long.MIN_VALUE ) { // NULL
                price = Constants.UNSET_DOUBLE;
            } else if ( value == Constants.MIN_PRICE_AS_LONG ) {
                price = Double.MIN_VALUE;
            } else {
                price = value / KEEP_DECIMAL_PLACE_FACTOR;
            }
        }
        
        return price;
    }
}
