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

import com.rr.core.codec.binary.BinaryLittleEndianEncoderUtils;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ZString;

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
public final class MilleniumEncodeBuilderImpl extends BinaryLittleEndianEncoderUtils {

    public  static final double  KEEP_DECIMAL_PLACE_FACTOR = 100000000D;
    private static final int     HEADER_UPTO_TYPE          = 3;

    private final byte _protocolVersion;

    public MilleniumEncodeBuilderImpl( byte[] buffer, int offset, ZString protocolVersion ) {
        super( buffer, offset );
        
        _protocolVersion = protocolVersion.getByte( 0 );
        _tzCalc.setLocalTimezone( TimeZone.getTimeZone( "GMT" ) );
    }
    
    @Override
    public void start( final int msgType ) {
        _idx              = _startOffset;
        _buffer[ _idx++ ] = _protocolVersion;
        _idx             += 2;                  // spacer for MsgLen
        _buffer[ _idx++ ] = (byte) msgType;
    }

    @Override
    public int end() {
        _msgLen = _idx - _startOffset;
        _idx    = _startOffset + 1;        // correct position for len 
        encodeShort( _msgLen - HEADER_UPTO_TYPE );
        return _msgLen;
    }

    @Override
    public void encodeTimeLocal( int msFromStartofDayUTC ) {
        final long msUTC = _tzCalc.msFromStartofDayToFullLocal( msFromStartofDayUTC );
        final int  unixTime = (int)(msUTC / 1000);
        encodeInt( unixTime );
    }
    
    @Override
    public void encodeTimeUTC( int msFromStartofDayUTC ) {
        _idx = _tzCalc.msFromStartofDayUTCToHHMMSS( _buffer, _idx, msFromStartofDayUTC );
    }
    
    @Override
    public void encodeTimestampLocal( int msFromStartofDayUTC ) {
        final long msUTC    = _tzCalc.msFromStartofDayToFullLocal( msFromStartofDayUTC );
        final int  unixTime = (int)(msUTC / 1000);
        final int  micros   = (int) ((msUTC - (unixTime * 1000)) * 10);
        encodeInt( unixTime );
        encodeInt( micros );
    }

    @Override
    public void encodeTimestampUTC( int msFromStartofDayUTC ) {
        final long msUTC    = _tzCalc.msFromStartofDayToFullUTC( msFromStartofDayUTC );
        final int  unixTime = (int)(msUTC / 1000);
        final int  micros   = (int) ((msUTC - (unixTime * 1000)) * 10);
        encodeInt( unixTime );
        encodeInt( micros );
    }
    
    @Override
    public final void encodePrice( final double price ) {
        
        long value = priceToExternalLong( price );

        encodeLong( value );
    }
    
    public static long priceToExternalLong( double price ) {
        
        if ( price >= 0 ) {
            if ( price == Double.MAX_VALUE ) {
                return Long.MAX_VALUE;
            }
            return (long) ((price + Constants.WEIGHT) * KEEP_DECIMAL_PLACE_FACTOR);
        }
        
        if ( price == Constants.UNSET_DOUBLE ) { // NULL
            return 0;
        } else if ( price == Double.MIN_VALUE ) {
            return Long.MIN_VALUE;
        }

        return (long) ((price - Constants.WEIGHT) * KEEP_DECIMAL_PLACE_FACTOR);
    }
}
