/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium;

import com.rr.core.codec.AbstractFixDecoder;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Message;
import com.rr.model.generated.codec.MilleniumLSEDecoder;
import com.rr.model.generated.codec.MilleniumLSEEncoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.om.warmup.FixTestUtils;

public class MilleniumLSECodecTest extends BaseTestCase {
    
    private AbstractFixDecoder      _fixDecoder;
    private MilleniumLSEDecoder     _decoder;
    private MilleniumLSEEncoder     _encoder;
    private String                  _dateStr = "20100510";
    private TimeZoneCalculator      _calc;
    private byte[]                  _buf;
    
    @Override
    public void setUp(){
        
        _fixDecoder = FixTestUtils.getDecoder44();
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _fixDecoder.setTimeZoneCalculator( _calc );

        _decoder = MilleniumTestUtils.getDecoder( _dateStr );
        _decoder.setInstrumentLocator( _fixDecoder.getInstrumentLocator() );

        _buf = new byte[8192];
        _encoder = new MilleniumLSEEncoder( _buf, 0 );
    }
    
    public void testNOSDecodeReEncode() {
        String nos = "8=FIX.4.4; 9=162; 35=D; 43=N; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=99000000001; 59=0; 22=5; 48=BT.L; 38=50; 44=10.23; 15=GBP; 40=2; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=006; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        _fixDecoder.setCompIds( new ViewString( "ME" ), null, new ViewString( "PROPA" ), null );
        
        Message msg = _fixDecoder.decode( nos.getBytes(), 0, nos.length() );
        ClientNewOrderSingleImpl nosEvent = (ClientNewOrderSingleImpl) msg;
        
        _encoder.encode( nosEvent );
        
        Message decodedNOS = _decoder.decode( _buf, 0, _encoder.getLength() );
        
        assertTrue( decodedNOS instanceof NewOrderSingle );
        
        NewOrderSingle lseNOS = (NewOrderSingle) decodedNOS;

        assertEquals( nosEvent.getClOrdId(),            lseNOS.getClOrdId() );
        assertEquals( nosEvent.getOrderQty(),           lseNOS.getOrderQty() );
        assertEquals( nosEvent.getPrice(),              lseNOS.getPrice(), Constants.TICK_WEIGHT );
        assertEquals( nosEvent.getOrdType(),            lseNOS.getOrdType() );
        assertEquals( nosEvent.getSide(),               lseNOS.getSide() );
        assertEquals( nosEvent.getSymbol(),             lseNOS.getSymbol() );
        assertEquals( nosEvent.getTimeInForce(),        lseNOS.getTimeInForce() );
        assertEquals( nosEvent.getAccount(),            lseNOS.getAccount() );

        // fields defaulted
        assertEquals( OrderCapacity.Principal,          lseNOS.getOrderCapacity() );

        // fields not encoded in Millenium should be unset when decoded
        assertTrue( lseNOS.getMsgSeqNum() == 0 || lseNOS.getMsgSeqNum() == Constants.UNSET_INT);
        
        assertEquals( 0,                                lseNOS.getSecurityId().length() );
        assertEquals( null,                             lseNOS.getSecurityIDSource() );
        assertEquals( null,                             lseNOS.getHandlInst() );
    }
    
    public void testNOSDecodeReEncodeNoPrice() {
        String nos = "8=FIX.4.4; 9=153; 35=D; 43=N; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=99000000001; 59=0; 22=5; 48=BT.L; 38=50; 15=GBP; 40=2; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=108; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        _fixDecoder.setCompIds( new ViewString( "ME" ), null, new ViewString( "PROPA" ), null );
        
        Message msg = _fixDecoder.decode( nos.getBytes(), 0, nos.length() );
        ClientNewOrderSingleImpl nosEvent = (ClientNewOrderSingleImpl) msg;
        
        _encoder.encode( nosEvent );
        
        Message decodedNOS = _decoder.decode( _buf, 0, _encoder.getLength() );
        
        assertTrue( decodedNOS instanceof NewOrderSingle );
        
        NewOrderSingle lseNOS = (NewOrderSingle) decodedNOS;

        assertEquals( nosEvent.getClOrdId(),            lseNOS.getClOrdId() );
        assertEquals( nosEvent.getOrderQty(),           lseNOS.getOrderQty() );
        assertEquals( 0.0,                              lseNOS.getPrice(), Constants.TICK_WEIGHT );
        assertEquals( nosEvent.getOrdType(),            lseNOS.getOrdType() );
        assertEquals( nosEvent.getSide(),               lseNOS.getSide() );
        assertEquals( nosEvent.getSymbol(),             lseNOS.getSymbol() );
        assertEquals( nosEvent.getTimeInForce(),        lseNOS.getTimeInForce() );
        assertEquals( nosEvent.getAccount(),            lseNOS.getAccount() );

        // fields defaulted
        assertEquals( OrderCapacity.Principal,          lseNOS.getOrderCapacity() );

        // fields not encoded in UTP should be unset when decoded
        assertEquals( Constants.UNSET_INT,              lseNOS.getMsgSeqNum() );
        assertEquals( 0,                                lseNOS.getSecurityId().length() );
        assertEquals( null,                             lseNOS.getSecurityIDSource() );
        assertEquals( null,                             lseNOS.getHandlInst() );
    }
    
    public void testACKDecodeReEncode() {

        // decode ack string to market ack pojo
        String ack = "8=FIX.4.4; 9=144; 35=8; 43=N; 34=12243; 49=PROPA; 52=" + _dateStr + "-12:01:01.232; 17=12345678; " + 
                     "56=ME; 37=88000000001; 39=0; 150=0; 40=1; 54=1; 55=BT.L; 11=99000000001; 21=1; 60=" + _dateStr + "-12:01:01; 38=10; " +
                     "59=0; 22=5; 48=BT.L; 1111=PR; 10=233; ";

        ack = FixTestUtils.toFixDelim( ack );
        
        _fixDecoder.setCompIds( "PROPA", null, "ME", null );
        
        MarketNewOrderAckImpl ackEvent = doTestACK( ack.getBytes(), 0, ack.length() );

        _encoder.encode( ackEvent );
        
        Message decodedACK = _decoder.decode( _buf, 0, _encoder.getLength() );
        
        assertTrue( decodedACK instanceof NewOrderAck );
        
        NewOrderAck lseACK = (NewOrderAck) decodedACK;

        assertEquals( ackEvent.getClOrdId(),            lseACK.getClOrdId() );
        assertEquals( ackEvent.getOrderId(),            lseACK.getOrderId() );
        assertEquals( ackEvent.getMsgSeqNum(),          lseACK.getMsgSeqNum() );
    }
    
    public MarketNewOrderAckImpl doTestACK( byte[] fixMsg, int offset, int len ) {
        
        Message msg = _fixDecoder.decode( fixMsg, offset, len );
        
        assertTrue( msg.getClass() == MarketNewOrderAckImpl.class );
        
        MarketNewOrderAckImpl ack = (MarketNewOrderAckImpl) msg;

        return ack;
    }
}
