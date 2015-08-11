/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.sbe;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.utils.HexUtils;
import com.rr.model.generated.codec.CMESimpleBinaryDecoder;
import com.rr.model.generated.codec.CMESimpleBinaryEncoder;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;

public class SBECodecTest extends BaseTestCase {

    private CMESimpleBinaryDecoder  _decoder;
    private CMESimpleBinaryEncoder  _encoder;
    private byte[]                  _bufEn;
    
    private String             _dateStr = "20100510";
    private TimeZoneCalculator _calc;

    
    
    @Override
    public void setUp(){
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        
        _bufEn   = new byte[8192];
        _decoder = SBETestUtils.getDecoder( _dateStr );
        _encoder = new CMESimpleBinaryEncoder( _bufEn, 0 );

        _decoder.setDebug( true );
        _encoder.setDebug( true );
        
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
    }
    
    public void testPassiveFill() {

        // fill taken from log file hex dump
        
        String msgLen           = "5400"; // 10 + (14 + 30 + 30) = 84
        String rootBlockLen     = "0D00"; // 14 as defined in template
        String msgType          = "0200";
        String schemaVersion    = "0100";
        String curMsgSchemaVer  = "0100";
        
        String msgHdr = msgLen + rootBlockLen + msgType + schemaVersion + curMsgSchemaVer;  // excludes packet header
        String body   = "C0C21C023D010000" + 
                        "68030000" + 
                        "80"       + 
                        "02"       + // # entries 
                        
                        "007B0000000C000000A0475F3B000000000C00000002C900000080000000" +
                        
                        "007B0000000D000000A0475F3B0000000000000000000000000000000000";

        String raw    = msgHdr + body;

        ReusableString msgBuf = new ReusableString( raw );
        HexUtils.hexStringToBytes( raw.getBytes(), 0, msgBuf );

        MDIncRefreshImpl inc = (MDIncRefreshImpl) _decoder.decode( msgBuf.getBytes(), msgBuf.getOffset(), msgBuf.length() );

        assertNotNull( inc );
        
/* DISABLE UNTIL GET REAL EXAMPLE
        assertEquals( 20130222133432l, inc.getSendingTime() );
        assertEquals( 2, inc.getNoMDEntries() );

        MDEntryImpl entry = (MDEntryImpl) inc.getMDEntries();
        
        assertEquals( MDUpdateAction.New, entry.getMdUpdateAction() );
        assertEquals( 2, entry.getMdEntryType() );                          // CONSTANT
        assertEquals( 123, entry.getSecurityID() );
        assertEquals( 8, entry.getSecurityIDSource() );                     // CONSTANT
        assertEquals( 12, entry.getRepeatSeq() );
        assertEquals( 99.61, entry.getMdEntryPx(), 0.00000005 );
        assertEquals( 12, entry.getMdEntrySize() );
        assertEquals( Constants.UNSET_INT, entry.getMdPriceLevel() );
        
        entry = entry.getNext();
        
        assertEquals( MDUpdateAction.New, entry.getMdUpdateAction() );
        assertEquals( 2, entry.getMdEntryType() );                          // CONSTANT
        assertEquals( 123, entry.getSecurityID() );
        assertEquals( 8, entry.getSecurityIDSource() );                     // CONSTANT
        assertEquals( 13, entry.getRepeatSeq() );
        assertEquals( 99.61, entry.getMdEntryPx(), 0.00000005 );
        assertEquals( Constants.UNSET_INT, entry.getMdPriceLevel() );
        
        entry = entry.getNext();

        _encoder.encode( inc );

        byte[] expBody = body.getBytes();
        
        for( int i=0 ; i < expBody.length ; i++ ) {
            assertEquals( "idx " + i + " is different, exp=" + expBody[i] + ", got=" + _bufEn[i], expBody[i], _bufEn[i] );
        }
        
        assertNull( entry );
*/        
    }
}
