/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti;

import java.util.Map;

import com.rr.codec.emea.exchange.eti.ETIDecodeContext;
import com.rr.core.codec.AbstractFixDecoder;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.utils.HexUtils;
import com.rr.model.generated.codec.ETIEurexHFTDecoder;
import com.rr.model.generated.codec.ETIEurexHFTEncoder;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.RecoveryTradeNewImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.SessionReject;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.LiquidityInd;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;
import com.rr.om.dummy.warmup.DummyInstrument;
import com.rr.om.warmup.FixTestUtils;

/**
 * TESTS DISABLED AS EUREX UPGRADED FROM 0.1 TO 1.0 NEED NEW EXECS FROM ETI
 *
 * @author Richard Rose
 */
public class ETICodecTest extends BaseTestCase {

    private ETIEurexHFTDecoder _decoder;
    private ETIEurexHFTEncoder _encoder;
    private byte[]             _bufEn;
    
    private AbstractFixDecoder _testRecoveryDecoder;
    private AbstractFixDecoder _testDecoder;
    private String             _dateStr = "20100510";
    private TimeZoneCalculator _calc;
    private byte[]             _buf;
    private Standard44Encoder  _testEncoder;

    
    
    @Override
    public void setUp(){
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        
        _bufEn   = new byte[8192];
        _decoder = ETITestUtils.getEurexDecoder( _dateStr );
        _encoder = new ETIEurexHFTEncoder( _bufEn, 0 );

        _decoder.setDebug( true );
        _encoder.setDebug( true );
        
        _buf = new byte[8192];
        _testRecoveryDecoder = FixTestUtils.getRecoveryDecoder44();
        _testDecoder = FixTestUtils.getDecoder44();
        _testEncoder = new Standard44Encoder( (byte)'4', (byte)'4', _buf );
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _testDecoder.setTimeZoneCalculator( _calc );
    }
    
    public void testRejectDecode() {
        String msg = "58 00 00 00 1A 27 00 00 F2 E6 90 D0 00 19 CB 12 E0 7C 95 D0 00 19 CB 12 01 00 00 00 00 00 00 00 05 00 00 00 30 00 04 00 49 6E 76 61 6C 69 64 20 76 61 " +
                     "6C 75 65 20 27 31 27 20 69 6E 20 66 69 65 6C 64 20 4F 72 64 65 72 52 6F 75 74 69 6E 67 49 6E 64 69 63 61 74 6F 72";

        byte[] data = HexUtils.hexStringToBytes( msg );

        SessionReject reject = (SessionReject) _decoder.decode( data, 0, data.length );
        
        assertEquals( "Invalid value '1' in field OrderRoutingIndicator", reject.getText().toString() );
    }
    
    public void DISABLED_testImmediateExecResponse() {
        String fill = "B0 00 00 00 77 27 00 00 DC 28 88 EE 57 4D CC 12 1A D8 87 EE 57 4D CC 12 90 80 8B EE 57 4D CC 12 73 88 90 EE 57 4D CC 12 03 00 00 00 02 00 04 12 CB 33 " +
                      "64 26 0C 0B C2 00 00 00 00 00 00 00 01 01 D7 5F 2C A5 5E 4E CB 12 AD 6A 6F 65 A4 41 A7 10 AD 6A 6F 65 A4 41 A7 10 70 62 00 00 00 00 00 00 B0 63 88 EE " +
                      "57 4D CC 12 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF 5A 00 00 00 00 00 00 00 DB 00 00 00 00 00 00 00 00 00 65 00 01 32 46 00 01 00 00 00 00 00 " +
                      "00 00 40 BB F9 52 03 00 00 00 DB 00 00 00 4E 04 00 00 CC 5C 04 00 02 00 00 00";

            byte[] data = HexUtils.hexStringToBytes( fill );

            TradeNew trade = (TradeNew) _decoder.decode( data, 0, data.length );

            ETIDecodeContext ctx = new ETIDecodeContext(); 
            _decoder.getLastContext( ctx );

            assertTrue( ctx.getLastApplMsgID().length() == 16 );
            assertEquals( 219, trade.getLastQty() );
            assertEquals( 219, trade.getCumQty() );
            assertEquals( 0, trade.getLeavesQty() );
            assertEquals( 142.77, trade.getLastPx(), 0.0000005 );
            
            assertEquals( "1102", trade.getExecId().toString() );
            assertEquals( "1200000000049769133", trade.getClOrdId().toString() );
            assertEquals( "1354262281360007127", trade.getOrderId().toString() );

            assertEquals( ExecType.Trade, trade.getExecType() );
            assertEquals( OrdStatus.Filled, trade.getOrdStatus() );
            assertEquals( false, trade.getPossDupFlag() );
        
    }
    
    public void DISABLED_testPassiveFill() {

        // fill taken from log file hex dump
        
        String fill = "98 00 00 00 78 27 00 00 78 C5 07 A6 AE 40 CC 12 62 2A 09 A6 AE 40 CC 12 FF FF FF FF 02 00 12 CB 33 64 26 07 FD DE 00 00 00 00 00 00 00 02 04 00 01 00 " +
                      "00 00 00 00 00 00 C5 52 2C A5 5E 4E CB 12 66 03 6C 64 A4 41 A7 10 66 03 6C 64 A4 41 A7 10 70 62 00 00 00 00 00 00 F0 46 04 A6 AE 40 CC 12 5A 00 00 00 " +
                      "77 00 00 00 62 00 00 00 00 00 00 00 00 00 6C 00 01 31 46 00 01 00 00 00 00 00 00 00 80 BE 5D 51 03 00 00 00 62 00 00 00 FC 01 00 00 74 1B 02 00 01 00 " +
                      "00 00";

        byte[] data = HexUtils.hexStringToBytes( fill );

        TradeNew trade = (TradeNew) _decoder.decode( data, 0, data.length );

        ETIDecodeContext ctx = new ETIDecodeContext(); 
        _decoder.getLastContext( ctx );

        assertTrue( ctx.getLastApplMsgID().length() == 16 );
        assertEquals( 98, trade.getLastQty() );
        assertEquals( 98, trade.getCumQty() );
        assertEquals( 119, trade.getLeavesQty() );
        assertEquals( 142.5, trade.getLastPx(), 0.0000005 );
        
        assertEquals( "508", trade.getExecId().toString() );
        assertEquals( "1200000000032768870", trade.getClOrdId().toString() );
        assertEquals( "1354262281360003781", trade.getOrderId().toString() );

        assertEquals( ExecType.Trade, trade.getExecType() );
        assertEquals( OrdStatus.PartiallyFilled, trade.getOrdStatus() );
        assertEquals( false, trade.getPossDupFlag() );
    }
    
    public void testReject() {
        String priceReject = "48 00 00 00 1A 27 00 00 1E 72 52 B0 E6 4B CC 12 95 6F 58 B0 E6 4B CC 12 04 00 00 00 00 00 00 00 11 27 00 00 1B 00 00 00 42 55 59 20 50 52 49 43 45 20 " +
                             "49 53 20 4E 4F 54 20 52 45 41 53 4F 4E 41 42 4C 45 00 00 00 00 00";

        byte[] data = HexUtils.hexStringToBytes( priceReject );

        SessionReject reject = (SessionReject) _decoder.decode( data, 0, data.length );
        
        assertEquals( "BUY PRICE IS NOT REASONABLE", reject.getText().toString() );
    }
    
    public void testNOSEncodeDecode() {
        
        _decoder.setExchangeEmulationOn();

        String nos = "8=FIX.4.4; 9=172; 35=D; 43=N; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=12345678; 59=0; 22=5; 48=998877; 38=50; 44=10.23; 15=GBP; 40=2; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 526=XX2100; 10=022; ";

        nos = FixTestUtils.toFixDelim( nos );
        
        Map<String,String> vals = FixTestUtils.msgToMap( nos );
        
        _testDecoder.setCompIds( vals.get( "56" ), null, vals.get( "49" ), null );
        _testEncoder.setCompIds( vals.get( "49" ), null, vals.get( "56" ), null );
        
        ClientNewOrderSingleImpl srcNOS = makeNOS( nos.getBytes(), 0, nos.length(), vals );
        
        _encoder.encode( srcNOS );

        NewOrderSingle newNOS = (NewOrderSingle) _decoder.decode( _bufEn, 0, _encoder.getLength() );

        assertEquals( srcNOS.getClOrdId(),      newNOS.getClOrdId() );
        assertEquals( srcNOS.getOrderQty(),     newNOS.getOrderQty() );
        assertEquals( srcNOS.getPrice(),        newNOS.getPrice(), 0.0000005 );
        assertEquals( srcNOS.getOrdType(),      newNOS.getOrdType() );
        assertEquals( srcNOS.getMsgSeqNum(),    newNOS.getMsgSeqNum() );
        assertEquals( srcNOS.getSide(),         newNOS.getSide() );
        
        int id = DummyInstrument.encodeSymbolToId( srcNOS.getSecurityId() );
        
        assertEquals( "" + id, newNOS.getSecurityId().toString() );
    }
    
    public ClientNewOrderSingleImpl makeNOS( byte[] fixMsg, int offset, int len, Map<String, String> vals ) {
        Message msg = _testDecoder.decode( fixMsg, offset, len );
        ClientNewOrderSingleImpl nos = (ClientNewOrderSingleImpl) msg;
        return nos;
    }

    public void testACKDecodeReEncode() {

        _encoder.setExchangeEmulationOn();

        // get a NOS to use as src in ClientACK
        
        String nos = "8=FIX.4.4; 9=151; 35=D; 43=N; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=11223344; 59=0; 22=5; 48=1199; 38=10; 44=10.23; 40=2; 54=1; 55=BT.L; 21=1; " +
                     "60=" + _dateStr  + "-12:01:01.000; 10=184; ";

        nos = FixTestUtils.toFixDelim( nos );

        ClientNewOrderSingleImpl nosEvent = (ClientNewOrderSingleImpl) _testDecoder.decode( nos.getBytes(), 0, nos.length() );
        
        // decode ack string to market ack pojo
        String ack = "8=FIX.4.4; 9=177; 35=8; 43=N; 34=12243; 49=PROPA; 52=" + _dateStr + "-12:01:01.232; " +
                     "56=ME; 37=333111; 39=0; 150=0; 40=1; 54=1; 55=BT.L; 11=11223344; 21=1; 60=" + _dateStr + "-12:01:01; 38=10; " +
                     "59=0; 22=5; 48=1199; 1111=PR; 17=998877; 10=233; ";

        ack = FixTestUtils.toFixDelim( ack );
        
        Map<String,String> vals = FixTestUtils.msgToMap( ack );
        
        _testDecoder.setCompIds( vals.get( "49" ), null, vals.get( "56" ), null );
        _testEncoder.setCompIds( vals.get( "49" ), null, vals.get( "56" ), null );
        
        MarketNewOrderAckImpl ackEvent = makeACK( ack.getBytes(), 0, ack.length(), vals );

        // create a client ack based on the market ack
        // (as market ack optimises out decoding of attrs on the exec report)
        ClientNewOrderAckImpl srcAck = FixTestUtils.getClientAck( ackEvent, nosEvent );
        
        _encoder.encode( srcAck );

        NewOrderAck newAck = (NewOrderAck) _decoder.decode( _bufEn, 0, _encoder.getLength() );

        assertEquals( srcAck.getClOrdId(),      newAck.getClOrdId() );
        assertEquals( srcAck.getOrderId(),      newAck.getOrderId() );
        assertEquals( srcAck.getExecId(),       newAck.getExecId() );
        assertEquals( srcAck.getMsgSeqNum(),    newAck.getMsgSeqNum() );
        assertEquals( srcAck.getOrdStatus(),    newAck.getOrdStatus() );
        assertEquals( srcAck.getExecType(),     newAck.getExecType() );
    }
    
    public void testReEncodeFullFill() {
        int    orderQty = 100;
        int    lastQty = 100;
        double lastPx  = 24.0;
        
        _encoder.setExchangeEmulationOn();
        
        ZString mktOrderId = new ViewString( "11223344" );
        ZString mktClOrdId = new ViewString( "90001" );
        ZString fillExecId = new ViewString( "99887766" );
        
        RecoveryTradeNewImpl srcFill = (RecoveryTradeNewImpl) 
                FixTestUtils.getMarketTradeNew( new ReusableString(), 
                                                _testRecoveryDecoder, 
                                                mktOrderId, 
                                                mktClOrdId, 
                                                orderQty,  
                                                lastQty, 
                                                lastPx, 
                                                fillExecId );
        
        srcFill.setLiquidityInd( LiquidityInd.RemovedLiquidity );
        srcFill.setLeavesQty( orderQty - lastQty );
        srcFill.setCumQty( lastQty );
        
        _encoder.encode( srcFill );

        TradeNew newFill = (TradeNew) _decoder.decode( _bufEn, 0, _encoder.getLength() );

        assertEquals( srcFill.getClOrdId(),      newFill.getClOrdId() );
        assertEquals( srcFill.getOrderId(),      newFill.getOrderId() );
        assertEquals( srcFill.getExecId(),       newFill.getExecId() );
        assertEquals( srcFill.getMsgSeqNum(),    newFill.getMsgSeqNum() );
        assertEquals( srcFill.getOrdStatus(),    newFill.getOrdStatus() );
        assertEquals( srcFill.getExecType(),     newFill.getExecType() );
        assertEquals( srcFill.getCumQty(),       newFill.getCumQty() );
        assertEquals( srcFill.getLeavesQty(),    newFill.getLeavesQty() );
        assertEquals( srcFill.getLastQty(),      newFill.getLastQty() );
        assertEquals( srcFill.getLastPx(),       newFill.getLastPx(), 0.0000005 );
    }
    
    public void testReEncodePartialFill() {
        int    orderQty = 100;
        int    lastQty = 25;
        double lastPx  = 24.0;
        
        _encoder.setExchangeEmulationOn();

        ZString mktOrderId = new ViewString( "11223344" );
        ZString mktClOrdId = new ViewString( "90001" );
        ZString fillExecId = new ViewString( "99887766" );
        
        RecoveryTradeNewImpl srcFill = (RecoveryTradeNewImpl) 
                FixTestUtils.getMarketTradeNew( new ReusableString(), 
                                                _testRecoveryDecoder, 
                                                mktOrderId, 
                                                mktClOrdId, 
                                                orderQty,  
                                                lastQty, 
                                                lastPx, 
                                                fillExecId );
        
        srcFill.setLiquidityInd( LiquidityInd.RemovedLiquidity );
        srcFill.setLeavesQty( orderQty - lastQty );
        srcFill.setCumQty( lastQty );
        
        _encoder.encode( srcFill );

        TradeNew newFill = (TradeNew) _decoder.decode( _bufEn, 0, _encoder.getLength() );

        assertEquals( srcFill.getClOrdId(),      newFill.getClOrdId() );
        assertEquals( srcFill.getOrderId(),      newFill.getOrderId() );
        assertEquals( srcFill.getExecId(),       newFill.getExecId() );
        assertEquals( srcFill.getMsgSeqNum(),    newFill.getMsgSeqNum() );
        assertEquals( srcFill.getOrdStatus(),    newFill.getOrdStatus() );
        assertEquals( srcFill.getExecType(),     newFill.getExecType() );
        assertEquals( srcFill.getCumQty(),       newFill.getCumQty() );
        assertEquals( srcFill.getLeavesQty(),    newFill.getLeavesQty() );
        assertEquals( srcFill.getLastQty(),      newFill.getLastQty() );
        assertEquals( srcFill.getLastPx(),       newFill.getLastPx(), 0.0000005 );
    }
    
    public MarketNewOrderAckImpl makeACK( byte[] fixMsg, int offset, int len, Map<String, String> vals ) {
        Message msg = _testDecoder.decode( fixMsg, offset, len );
        MarketNewOrderAckImpl ack = (MarketNewOrderAckImpl) msg;
        return ack;
    }
}
