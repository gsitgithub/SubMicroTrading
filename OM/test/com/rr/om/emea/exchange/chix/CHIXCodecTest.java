/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.chix;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.rr.core.codec.AbstractFixDecoder;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.model.Message;
import com.rr.model.generated.fix.codec.Standard42Encoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.internal.type.IllegalFieldAccess;
import com.rr.om.warmup.FixTestUtils;

public class CHIXCodecTest extends BaseTestCase {
    
    // NOS
    private String mnos = "8=FIX.4.2; 9=173; 35=D; 49=LLT1_CHIX; 56=CHIX_TST; 34=963; 43=N; 52=20100510-13:00:50.232; 60=20100510-13:00:50.234; 11=A0TUQG; 55=VODl; 15=GBp; 21=1; 38=124; 40=2; 44=12; 54=1; 59=0; 47=P; 207=CIX; 58=A0TUQG; 10=242; ";
    private String mack = "8=FIX.4.2; 9=226; 35=8; 49=CHIX_TST; 56=LLT1_CHIX; 34=963; 43=N; 52=20100510-13:00:50.243; 20=0; 60=20100510-13:00:50.243; 37=236117; 150=0; 39=0; 40=2; 54=1; 38=124; 55=VODl; 44=12.00; 47=P; 59=0; 14=0; 17=A236117; 32=0; 31=0.00; 151=124; 11=A0TUQG; 10=093; ";  
    
    private AbstractFixDecoder _testDecoder;
    private String             _dateStr = "20100510";
    private TimeZoneCalculator _calc;
    private ReusableString     _tmp1 = new ReusableString( 30 );
    private ReusableString     _tmp2 = new ReusableString( 30 );
    private byte[]             _buf;
    private Standard42Encoder  _testEncoder;
    
    @Override
    public void setUp(){
        _buf = new byte[8192];
        _testDecoder = FixTestUtils.getDecoder42();
        _testEncoder = new Standard42Encoder( (byte)'4', (byte)'2', _buf );
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _testDecoder.setTimeZoneCalculator( _calc );
    }
    
    public void testNOSDecodeReEncode() {
        String nos = FixTestUtils.toFixDelim( mnos );
        
        Map<String,String> vals = FixTestUtils.msgToMap( nos );
        
        _testDecoder.setCompIds( vals.get( "56" ), null, vals.get( "49" ), null );
        _testEncoder.setCompIds( vals.get( "49" ), null, vals.get( "56" ), null );
        
        ClientNewOrderSingleImpl nosEvent = doTestNOS( nos.getBytes(), 0, nos.length(), vals );
        
        _testEncoder.encode( nosEvent );
        
        String resultEncoded = new String( _buf, _testEncoder.getOffset(), _testEncoder.getLength() );

        FixTestUtils.compare( nos, resultEncoded, true );
    }
    
    public ClientNewOrderSingleImpl doTestNOS( byte[] fixMsg, int offset, int len, Map<String, String> vals ) {
        
        Message msg = _testDecoder.decode( fixMsg, offset, len );
        
        assertTrue( msg.getClass() == ClientNewOrderSingleImpl.class );
        
        ClientNewOrderSingleImpl nos = (ClientNewOrderSingleImpl) msg;

        // must toString ZString's
        
        assertEquals( vals.get( "11" ), nos.getClOrdId().toString() );
        assertEquals( vals.get( "38" ), Integer.toString( nos.getOrderQty() ) );
        assertEquals( vals.get( "59" ), "" + (char)nos.getTimeInForce().getVal() );
        assertEquals( vals.get( "40" ), "" + (char)nos.getOrdType().getVal() );
        assertEquals( vals.get( "54" ), "" + (char)nos.getSide().getVal() );
        assertEquals( vals.get( "55" ), nos.getSymbol().toString() );
        assertEquals( vals.get( "21" ), "" + (char)nos.getHandlInst().getVal() );
        assertEquals( Double.parseDouble( vals.get( "44" ) ), nos.getPrice(), Constants.TICK_WEIGHT );
        assertEquals( vals.get( "21" ), "" + (char)nos.getHandlInst().getVal() );
        assertEquals( vals.get( "52" ), _calc.dateTimeToUTC( _tmp1, nos.getSendingTime() ).toString() );
        assertEquals( vals.get( "60" ), _calc.dateTimeToUTC( _tmp2, nos.getTransactTime() ).toString() );
        assertEquals( vals.get( "47" ), "" + (char)nos.getOrderCapacity().getVal() );
        
        // another test will check encode & decode of date/time strings for 52 and 60
        
        return nos;
    }

    public void testACKDecodeReEncode() {

        // get a NOS to use as src in ClientACK
        
        String nos = FixTestUtils.toFixDelim( mnos );

        ClientNewOrderSingleImpl nosEvent = (ClientNewOrderSingleImpl) _testDecoder.decode( nos.getBytes(), 0, nos.length() );
        
        // decode ack string to market ack pojo
        String ack = FixTestUtils.toFixDelim( mack );
        
        Map<String,String> vals = FixTestUtils.msgToMap( ack );
        
        _testDecoder.setCompIds( vals.get( "49" ), null, vals.get( "56" ), null );
        _testEncoder.setCompIds( vals.get( "49" ), null, vals.get( "56" ), null );
        
        MarketNewOrderAckImpl ackEvent = doTestACK( ack.getBytes(), 0, ack.length(), vals );

        // create a client ack based on the market ack
        // (as market ack optimises out decoding of attrs on the exec report)
        ClientNewOrderAckImpl clientAck = FixTestUtils.getClientAck( ackEvent, nosEvent );
        _testEncoder.encode( clientAck );
        
        String resultEncoded = new String( _buf, _testEncoder.getOffset(), _testEncoder.getLength() );

        // optional tags are not encoded
        Set<String> tagsToIgnore = new LinkedHashSet<String>();
        tagsToIgnore.add( "9" );
        tagsToIgnore.add( "14" );
        tagsToIgnore.add( "40" );
        tagsToIgnore.add( "47" );
        tagsToIgnore.add( "21" );
        tagsToIgnore.add( "59" );
        tagsToIgnore.add( "1111" );
          
        FixTestUtils.compare( ack, resultEncoded, tagsToIgnore, true, true );
    }
    
    public MarketNewOrderAckImpl doTestACK( byte[] fixMsg, int offset, int len, Map<String, String> vals ) {
        
        Message msg = _testDecoder.decode( fixMsg, offset, len );
        
        assertTrue( msg.getClass() == MarketNewOrderAckImpl.class );
        
        MarketNewOrderAckImpl ack = (MarketNewOrderAckImpl) msg;

        // must toString ZString's
        
        assertEquals( vals.get( "34" ), "" + ack.getMsgSeqNum() );
        assertEquals( vals.get( "11" ), ack.getClOrdId().toString() );
        assertEquals( vals.get( "37" ), ack.getOrderId().toString() );
        assertEquals( vals.get( "150" ), "" + (char)ack.getExecType().getVal() );
        assertEquals( vals.get( "39" ), "" + (char)ack.getOrdStatus().getVal() );
        assertEquals( vals.get( "54" ), "" + (char)ack.getSide().getVal() );
        
        try { ack.getOrderQty();  fail();
        } catch( IllegalFieldAccess e ) { /* expected */ }
        
        try { ack.getPrice();  fail();
        } catch( IllegalFieldAccess e ) { /* expected */ }
        
        try { ack.getCurrency();  fail();
        } catch( IllegalFieldAccess e ) { /* expected */ }
        
        // another test will check encode & decode of date/time strings for 52 and 60
        
        return ack;
    }
}
