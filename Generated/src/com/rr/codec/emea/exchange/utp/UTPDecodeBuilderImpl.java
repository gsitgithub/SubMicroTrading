/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.codec.emea.exchange.utp;

import java.util.TimeZone;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.BinaryBigEndianDecoderUtils;
import com.rr.core.lang.Constants;
import com.rr.core.lang.TimeZoneCalculator;

/**
 * @NOTE dont check for max idx before each put the calling code must ensure buffer is big enough
 * (typically 8K)
 * 
 * each message has header 
 * 
    HEADER          Message type Alpha                 1 A Logon
    ProtocolVersion CCG Binary protocol version Alpha  1 2 Extended ClientOrderID protocol type P78
    MsgLen          Message Length Num                 2 0->65535 P72
 *
 * @author Richard Rose
 */
public final class UTPDecodeBuilderImpl extends BinaryBigEndianDecoderUtils {

    public UTPDecodeBuilderImpl() {
        super();
        _tzCalc.setLocalTimezone( TimeZone.getTimeZone( "CET" ) );
    }

    @Override
    public void end() {
        final byte etx = decodeByte();
        
        if ( etx != 0x0A ) {
            throw new RuntimeDecodingException( "UTPMessage unexpected ETX byte at idx=" + _idx + ", val=" + etx );
        }

        super.end();
    }

    @Override
    public double decodePrice() {
        
        final int value = decodeInt();

        double price;
        
        if ( value >= 0 ) {
            if ( value == Integer.MAX_VALUE )  {
                price = Double.MAX_VALUE;
            } else {
                price = value / Constants.KEEP_DECIMAL_PLACE_FACTOR;
            }
        } else {
            if ( value == Integer.MIN_VALUE ) { // NULL
                price = Constants.UNSET_DOUBLE;
            } else if ( value == Constants.MIN_PRICE_AS_INT ) {
                price = Double.MIN_VALUE;
            } else {
                price = value / Constants.KEEP_DECIMAL_PLACE_FACTOR;
            }
        }
        
        return price;
    }
    
    @Override
    public int decodeTimeLocal() {
        final int msLocal = getMSFromStartDay(); 
        return _tzCalc.timeLocalToUTC( msLocal );
    }
    
    @Override
    public int decodeTimeUTC() {
        final int msUTC = getMSFromStartDay(); 
        return msUTC;
    }
    
    @Override
    public int decodeTimestampLocal() {
        long unixTimeLocalSecs = decodeInt();
        return _tzCalc.localToUTC( unixTimeLocalSecs * 1000 );
    }
    
    @Override
    public int decodeTimestampUTC() {
        long unixTimeSecs = decodeInt();
        return _tzCalc.getTimeUTC( unixTimeSecs * 1000 );
    }
    
    private final int getMSFromStartDay() {

        if ( _maxIdx < (_idx + 6) ) {
            throw new RuntimeDecodingException( "Missing part of time field, idx=" + _idx + ", maxIdx is " + _maxIdx );
        }

        int hTen = _buffer[ _idx++ ] - '0';
        int hDig = _buffer[ _idx++ ] - '0';
        int mTen = _buffer[ _idx++ ] - '0';
        int mDig = _buffer[ _idx++ ] - '0';
        int sTen = _buffer[ _idx++ ] - '0';
        int sDig = _buffer[ _idx++ ] - '0';

        int hour = ((hTen) * 10)  + (hDig);
        int min  = ((mTen) * 10)  + (mDig);
        int sec  = ((sTen) * 10)  + (sDig);

        if ( hour < 0 || hour > 23 ) {
            throw new RuntimeDecodingException( "Invalid hour '" + hour + "' in time format" );
        }
        
        if ( min < 0 || min > 59 ) {
            throw new RuntimeDecodingException( "Invalid min '" + min + "' in time format" );
        }
        
        if ( sec < 0 || sec > 59 ) {
            throw new RuntimeDecodingException( "Invalid sec '" + sec + "' in time format" );
        }

        // TODO bench the multiply vs table look up on target hw & os
        
        int ms = TimeZoneCalculator._hourToMS[ hour ] + TimeZoneCalculator._minToMS[ min ] + TimeZoneCalculator._secToMS[ sec ] + _tzCalc.getOffset();
        // int ms = ((hour * 3600) + (min*60) + sec) * 1000 + _tzCalculator.getOffset();

        return ms;
    }    
}
