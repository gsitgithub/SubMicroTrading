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

import com.rr.core.codec.binary.BinaryBigEndianEncoderUtils;
import com.rr.core.lang.ZString;
import com.rr.core.utils.NumberUtils;

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
public final class UTPEncodeBuilderImpl extends BinaryBigEndianEncoderUtils {

    private final byte _protocolVersion;

    public UTPEncodeBuilderImpl( byte[] buffer, int offset, ZString protocolVersion ) {
        super( buffer, offset );
        
        _protocolVersion = protocolVersion.getByte( 0 );
        _tzCalc.setLocalTimezone( TimeZone.getTimeZone( "CET" ) );
    }
    
    @Override
    public void start( final int msgType ) {
        _idx              = _startOffset;
        _buffer[ _idx++ ] = (byte) msgType;
        _buffer[ _idx++ ] = _protocolVersion;
        _idx             += 2;                  // spacer for MsgLen
    }

    @Override
    public int end() {
        _buffer[ _idx++ ] = 0x0A;          // END record marker
        _msgLen = _idx - _startOffset;
        _idx    = _startOffset + 2;        // correct position for len 
        encodeShort( _msgLen );
        return _msgLen;
    }

    @Override
    public void encodeTimeLocal( int msFromStartofDayUTC ) {
        _idx = _tzCalc.msFromStartofDayUTCToLocalHHMMSS( _buffer, _idx, msFromStartofDayUTC );
    }
    
    @Override
    public void encodeTimeUTC( int msFromStartofDayUTC ) {
        _idx = _tzCalc.msFromStartofDayUTCToHHMMSS( _buffer, _idx, msFromStartofDayUTC );
    }
    
    @Override
    public void encodeTimestampLocal( int msFromStartofDayUTC ) {
        final long msUTC = _tzCalc.msFromStartofDayToFullLocal( msFromStartofDayUTC );
        final int  unixTime = (int)(msUTC / 1000);
        encodeInt( unixTime );
    }

    @Override
    public void encodeTimestampUTC( int msFromStartofDayUTC ) {
        final long msUTC    = _tzCalc.msFromStartofDayToFullUTC( msFromStartofDayUTC );
        final int  unixTime = (int)(msUTC / 1000);
        encodeInt( unixTime );
    }
    
    @Override
    public final void encodePrice( final double price ) {
        
        int value = NumberUtils.priceToExternalInt( price );

        encodeInt( value );
    }
}
