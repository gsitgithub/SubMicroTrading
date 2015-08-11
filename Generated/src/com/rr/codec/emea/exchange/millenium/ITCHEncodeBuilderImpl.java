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
    MsgLen          Message Length Num                 2 0->65535 
    MsgType         Message type Alpha                 1 A Logon
    
 *
 * @author Richard Rose
 */
public final class ITCHEncodeBuilderImpl extends BinaryLittleEndianEncoderUtils {

    public  static final double  KEEP_DECIMAL_PLACE_FACTOR = 100000000D;
    private int _lastStart;

    public ITCHEncodeBuilderImpl( byte[] buffer, int offset, ZString protocolVersion ) {
        super( buffer, offset );
        
        _tzCalc.setLocalTimezone( TimeZone.getDefault() );
    }
    
    public void startMultiMsg( int msgs ) {
        _idx              = _startOffset;
        _idx             += 2;                  // spacer for MsgLen
        encodeByte( (byte) msgs );
    }

    public void nextMessage( final byte msgType ) {
        
        if ( _lastStart > 0 ) {
            int len = _idx - _lastStart;
            _buffer[_lastStart] = (byte)len;
        }

        _lastStart = _idx;
        _idx  += 1;                  // spacer for MsgLen
        _buffer[ _idx++ ] = msgType;
    }

    @Override
    public void start( final int msgType ) {
        // @TODO to support itch : NEED TO REFACTOR GENERATOR TO SUPPORT NEW BUILDER METHOD FOR PACKING
    }

    @Override
    public int end() {
        if ( _lastStart > 0 ) {
            int len = _idx - _lastStart;
            _buffer[_lastStart] = (byte)len;
        }

        _msgLen = _idx - _startOffset;
        _idx    = _startOffset;        // correct position for len 
        encodeShort( _msgLen );
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
