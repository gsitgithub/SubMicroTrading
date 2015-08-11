/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.utp;

import com.rr.core.codec.AbstractFixDecoder;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Message;
import com.rr.model.generated.codec.UTPEuronextCashDecoder;
import com.rr.model.generated.codec.UTPEuronextCashEncoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.om.warmup.FixTestUtils;

public class UTPENXCodecTest extends BaseTestCase {
    
    private AbstractFixDecoder      _fixDecoder;
    private UTPEuronextCashDecoder  _decoder;
    private UTPEuronextCashEncoder  _encoder;
    private String                  _dateStr = "20100510";
    private TimeZoneCalculator      _calc;
    private byte[]                  _buf;
    
    @Override
    public void setUp(){
        _fixDecoder = FixTestUtils.getDecoder44();
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _fixDecoder.setTimeZoneCalculator( _calc );

        _decoder = UTPTestUtils.getDecoder( _dateStr );

        _buf = new byte[8192];
        _encoder = new UTPEuronextCashEncoder( _buf, 0 );
    }
    
    public void testNOSDecodeReEncode() {
        String nos = "8=FIX.4.4; 9=162; 35=D; 43=N; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=99000000001; 59=0; 22=5; 48=BT.L; 38=50; 44=10.23; 15=GBP; 40=2; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=006; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        _fixDecoder.setCompIds( new ViewString( "ME" ), null, new ViewString( "PROPA" ), null );
        _encoder.setOnBehalfOfId( new ViewString( "ME01" ) );
        
        Message msg = _fixDecoder.decode( nos.getBytes(), 0, nos.length() );
        ClientNewOrderSingleImpl nosEvent = (ClientNewOrderSingleImpl) msg;
        
        _encoder.encode( nosEvent );
        
        Message decodedNOS = _decoder.decode( _buf, 0, _encoder.getLength() );
        
        assertTrue( decodedNOS instanceof NewOrderSingle );
        
        NewOrderSingle utpNOS = (NewOrderSingle) decodedNOS;

        assertEquals( nosEvent.getClOrdId(),            utpNOS.getClOrdId() );
        assertEquals( nosEvent.getOrderQty(),           utpNOS.getOrderQty() );
        assertEquals( nosEvent.getPrice(),              utpNOS.getPrice(), Constants.TICK_WEIGHT );
        assertEquals( nosEvent.getOrdType(),            utpNOS.getOrdType() );
        assertEquals( nosEvent.getSide(),               utpNOS.getSide() );
        assertEquals( nosEvent.getMsgSeqNum(),          utpNOS.getMsgSeqNum() );
        assertEquals( nosEvent.getSymbol(),             utpNOS.getSymbol() );
        assertEquals( nosEvent.getTimeInForce(),        utpNOS.getTimeInForce() );
        assertEquals( nosEvent.getAccount(),            utpNOS.getAccount() );

        // fields defaulted
        assertEquals( OrderCapacity.Principal,          utpNOS.getOrderCapacity() );

        // fields not encoded in UTP should be unset when decoded
        assertEquals( 0,                                utpNOS.getSecurityId().length() );
        assertEquals( null,                             utpNOS.getSecurityIDSource() );
        assertEquals( null,                             utpNOS.getHandlInst() );
    }
    
    public void testNOSDecodeReEncodeNoPrice() {
        String nos = "8=FIX.4.4; 9=153; 35=D; 43=N; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=99000000001; 59=0; 22=5; 48=BT.L; 38=50; 15=GBP; 40=2; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=108; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        _fixDecoder.setCompIds( new ViewString( "ME" ), null, new ViewString( "PROPA" ), null );
        _encoder.setOnBehalfOfId( new ViewString( "ME01" ) );
        
        Message msg = _fixDecoder.decode( nos.getBytes(), 0, nos.length() );
        ClientNewOrderSingleImpl nosEvent = (ClientNewOrderSingleImpl) msg;
        
        _encoder.encode( nosEvent );
        
        Message decodedNOS = _decoder.decode( _buf, 0, _encoder.getLength() );
        
        assertTrue( decodedNOS instanceof NewOrderSingle );
        
        NewOrderSingle utpNOS = (NewOrderSingle) decodedNOS;

        assertEquals( nosEvent.getClOrdId(),            utpNOS.getClOrdId() );
        assertEquals( nosEvent.getOrderQty(),           utpNOS.getOrderQty() );
        assertEquals( 0.0,                              utpNOS.getPrice(), Constants.TICK_WEIGHT );
        assertEquals( nosEvent.getOrdType(),            utpNOS.getOrdType() );
        assertEquals( nosEvent.getSide(),               utpNOS.getSide() );
        assertEquals( nosEvent.getMsgSeqNum(),          utpNOS.getMsgSeqNum() );
        assertEquals( nosEvent.getSymbol(),             utpNOS.getSymbol() );
        assertEquals( nosEvent.getTimeInForce(),        utpNOS.getTimeInForce() );
        assertEquals( nosEvent.getAccount(),            utpNOS.getAccount() );

        // fields defaulted
        assertEquals( OrderCapacity.Principal,          utpNOS.getOrderCapacity() );

        // fields not encoded in UTP should be unset when decoded
        assertEquals( 0,                                utpNOS.getSecurityId().length() );
        assertEquals( null,                             utpNOS.getSecurityIDSource() );
        assertEquals( null,                             utpNOS.getHandlInst() );
    }
    
    public void testACKDecodeReEncode() {

        // decode ack string to market ack pojo
        String ack = "8=FIX.4.4; 9=144; 35=8; 43=N; 34=12243; 49=PROPA; 52=" + _dateStr + "-12:01:01.232; " +
                     "56=ME; 37=8800000000123; 39=0; 150=0; 40=1; 54=1; 55=BT.L; 11=99000000001; 21=1; 60=" + _dateStr + "-12:01:01; 38=10; " +
                     "59=0; 22=5; 48=BT.L; 1111=PR; 10=233; ";

        ack = FixTestUtils.toFixDelim( ack );
        
        _fixDecoder.setCompIds( "PROPA", null, "ME", null );
        
        MarketNewOrderAckImpl ackEvent = doTestACK( ack.getBytes(), 0, ack.length() );

        _encoder.setOnBehalfOfId( new ViewString( "ME01" ) );
        
        _encoder.encode( ackEvent );
        
        Message decodedACK = _decoder.decode( _buf, 0, _encoder.getLength() );
        
        assertTrue( decodedACK instanceof NewOrderAck );
        
        NewOrderAck utpACK = (NewOrderAck) decodedACK;

        assertEquals( ackEvent.getClOrdId(),            utpACK.getClOrdId() );
        assertEquals( ackEvent.getOrderId(),            utpACK.getOrderId() );
        assertEquals( ackEvent.getMsgSeqNum(),          utpACK.getMsgSeqNum() );
    }
    
    public MarketNewOrderAckImpl doTestACK( byte[] fixMsg, int offset, int len ) {
        
        Message msg = _fixDecoder.decode( fixMsg, offset, len );
        
        assertTrue( msg.getClass() == MarketNewOrderAckImpl.class );
        
        MarketNewOrderAckImpl ack = (MarketNewOrderAckImpl) msg;

        return ack;
    }
}
