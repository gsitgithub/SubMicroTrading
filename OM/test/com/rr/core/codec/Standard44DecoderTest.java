/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import java.util.Map;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.model.Message;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.om.warmup.FixTestUtils;

public class Standard44DecoderTest extends BaseTestCase {
    
    private AbstractFixDecoder _testDecoder;
    private String             _dateStr = "20100510";
    private TimeZoneCalculator _calc;
    private ReusableString     _tmp1 = new ReusableString( 30 );
    private ReusableString     _tmp2 = new ReusableString( 30 );
    
    @Override
    public void setUp(){
        _testDecoder = FixTestUtils.getDecoder44();
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _testDecoder.setTimeZoneCalculator( _calc );
    }
    
    public void testUnknownByteType() {
        String nos = "8=FIX.4.4; 9=145; 35=D; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=XX2100; 59=0; 22=5; 48=BT.L; 38=50; 44=10.23; 40=Z; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=231; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        byte[] inMsg = nos.getBytes();
        
        checkForDecodeReject( inMsg, "Unsupported value of Z for OrdType" );
    }

    private void checkForDecodeReject( byte[] inMsg, String expectedMsg ) {
        Message msg = _testDecoder.decode( inMsg, 0, inMsg.length );
        assertEquals( RejectDecodeException.class, msg.getClass() );
        
        RejectDecodeException rej = (RejectDecodeException) msg;

        String err = rej.getMessage(); 
        assertTrue( err.contains( expectedMsg ) );
    }
    
    public void testTypeTooBig() {
        String nos = "8=FIX.4.4; 9=145; 35=D; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=XX2100; 59=0; 22=5; 48=BT.L; 38=50; 44=10.23; 40=1YZ; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=231; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        byte[] inMsg = nos.getBytes();
        
        checkForDecodeReject( inMsg, "Non numeric char in tag idx=118" );
    }
    
    public void testMissingTIF() {
        String nos = "8=FIX.4.4; 9=145; 35=D; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=XX2100; 59=; 22=5; 48=BT.L; 38=50; 44=10.23; 40=1YZ; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=231; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        byte[] inMsg = nos.getBytes();
        checkForDecodeReject( inMsg, "Non numeric char in tag idx=117, partialTagVal=0, badChar=Z, " );
    }
    
    public void testMissingTerminator() {
        String nos = "8=FIX.4.4; 9=145; 35=D; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=XX2100; 59=0; 22=5; 48=BT.L; 38=50; 44=10.23; 40=2; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=231";

        nos = FixTestUtils.toFixDelim( nos );
        
        byte[] inMsg = nos.getBytes();
        
        Message msg = _testDecoder.decode( inMsg, 0, inMsg.length );
        assertEquals( RejectIndexOutOfBounds.class, msg.getClass() );
    }
    
    public void testPartialMessage() {
        String nos = "8=FIX.4.4; 9=145; 35=D; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; 11=XX2";

        nos = FixTestUtils.toFixDelim( nos );
        
        byte[] inMsg = nos.getBytes();
        
        Message msg = _testDecoder.decode( inMsg, 0, inMsg.length );
        assertEquals( RejectIndexOutOfBounds.class, msg.getClass() );
    }
    
    public void testUnknownMultiByteType() {
        String nos = "8=FIX.4.4; 9=145; 35=D; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=XX2100; 59=0; 22=5; 48=BT.L; 38=50; 44=10.23; 40=1; 54=1; 55=BT.L; 15=AAA; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=087; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        byte[] inMsg = nos.getBytes();
        
        checkForDecodeReject( inMsg, "Unsupported value of AAA for Currency" );
    }
    
    public void testDecodeNos() {
        String nos = "8=FIX.4.4; 9=145; 35=D; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                        "11=XX2100; 59=0; 22=5; 48=BT.L; 38=50; 44=10.23; 40=2; 54=1; 55=BT.L; 21=1; " +
                        "60=" + _dateStr  + "-12:01:01.000; 10=231; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        Map<String,String> vals = FixTestUtils.msgToMap( nos );
        
        doTestDecodeNOS( nos.getBytes(), 0, nos.length(), vals );
    }
    
    public void doTestDecodeNOS( byte[] fixMsg, int offset, int len, Map<String, String> vals ) {
        
        Message msg = _testDecoder.decode( fixMsg, offset, len );
        
        _testDecoder.setCompIds( vals.get( "49" ), null, vals.get( "56" ), null );

        assertTrue( msg.getClass() == ClientNewOrderSingleImpl.class );
        
        ClientNewOrderSingleImpl nos = (ClientNewOrderSingleImpl) msg;

        /*
        8=FIX.4.4; 9=152; 35=D; 34=12243; 49=PROPA; 52=20100510-12:01:01.100; 
        56=ME; 59=0; 22=5; 48=BT.L; 40=2; 54=1; 55=BT.L; 11=XX2100; 21=1; 
        60=20100510-12:01:01; 38=50; 44=10.23; 10=345;
        */

        // must toString ZString's
        
        assertEquals( vals.get( "11" ), nos.getClOrdId().toString() );
        assertEquals( vals.get( "38" ), Integer.toString( nos.getOrderQty() ) );
        assertEquals( vals.get( "59" ), "" + (char)nos.getTimeInForce().getVal() );
        assertEquals( vals.get( "40" ), "" + (char)nos.getOrdType().getVal() );
        assertEquals( vals.get( "54" ), "" + (char)nos.getSide().getVal() );
        assertEquals( vals.get( "55" ), nos.getSymbol().toString() );
        assertEquals( vals.get( "21" ), "" + (char)nos.getHandlInst().getVal() );
        assertEquals( vals.get( "44" ), Double.toString( nos.getPrice() ) );
        assertEquals( vals.get( "21" ), "" + (char)nos.getHandlInst().getVal() );
        assertEquals( vals.get( "52" ), _calc.dateTimeToUTC( _tmp1, nos.getSendingTime() ).toString() );
        assertEquals( vals.get( "60" ), _calc.dateTimeToUTC( _tmp2, nos.getTransactTime() ).toString() );
        
        // another test will check encode & decode of date/time strings for 52 and 60
        
    }

}
