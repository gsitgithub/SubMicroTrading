/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.codec.emea.exchange.cme.sbe;

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
public class SBEDecodeBuilderImpl extends BinaryLittleEndianDecoderUtils {

    public SBEDecodeBuilderImpl() {
        super();
        _tzCalc.setLocalTimezone( TimeZone.getDefault() );
    }

    @Override
    public int decodeTimestampUTC() {
        long unixTimeMS = decodeLong() / 1000000; // drop the nanos
        return _tzCalc.getTimeUTC( unixTimeMS );
    }
    
    @Override
    public double decodePrice() {
        
        final long value = decodeLong();

        double price;
        
        if ( value == Constants.UNSET_LONG)  {
            price = Constants.UNSET_DOUBLE;
        } else {
            price = value / SBEConstants.KEEP_DECIMAL_PLACE_FACTOR;
        }
        
        return price;
    }
    
    @Override
    public long  decodeLong() {
        long val = super.decodeLong();
        
        if ( val == 0x8000000000000000L ) {
            return Constants.UNSET_LONG;
        }
        
        return val;
    }
    
    @Override
    public long decodeULong() {
        long val = super.decodeLong();
        
        if ( val == 0xFFFFFFFFFFFFFFFFL ) {
            return Constants.UNSET_LONG;
        }
        
        return val;
    }
    
    @Override
    public int   decodeInt() {
        int val = super.decodeInt();
        
        if ( val == 0x80000000 ) {
            return Constants.UNSET_INT;
        }
        
        return val;
    }

    @Override
    public int decodeUInt() {
        int val = super.decodeInt();
        
        if ( val == 0xFFFFFFFF ) {
            return Constants.UNSET_INT;
        }
        
        return val;
    }
    
    @Override
    public short decodeShort() {
        short val = super.decodeShort();
        
        if ( val == (short)0x8000 ) {
            return Constants.UNSET_SHORT;
        }
        
        return val;
    }

    @Override
    public short decodeUShort() {
        short val = super.decodeShort();
        
        if ( val == (short)0xFFFF ) {
            return Constants.UNSET_SHORT;
        }
        
        return val;
    }
    
    @Override
    public boolean decodeBool() {
        return _buffer[ _idx++ ] == 'Y' ? true : false;
    }
    
    @Override
    public byte decodeByte() {
        byte val = super.decodeByte();
        
        if ( val == (byte)0x80 ) {
            return Constants.UNSET_BYTE;
        }
        
        return val;
    }

    @Override
    public byte decodeUByte() {
        byte val = super.decodeByte();
        
        if ( val == (byte)0xFF ) {
            return Constants.UNSET_BYTE;
        }
        
        return val;
    }
    
    @Override
    public byte decodeChar() {
        byte val = super.decodeByte();
        
        if ( val == 0x00 ) {
            return Constants.UNSET_CHAR;
        }
        
        return val;
    }
}
