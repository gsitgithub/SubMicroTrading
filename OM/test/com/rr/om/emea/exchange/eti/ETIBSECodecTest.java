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

import com.rr.core.codec.AbstractFixDecoder;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Exchange;
import com.rr.core.model.Message;
import com.rr.core.utils.FileException;
import com.rr.core.utils.HexUtils;
import com.rr.inst.FixInstrumentLoader;
import com.rr.inst.SingleExchangeInstrumentStore;
import com.rr.model.generated.codec.ETIBSEDecoder;
import com.rr.model.generated.codec.ETIBSEEncoder;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.RecoveryCancelledImpl;
import com.rr.model.generated.internal.events.impl.RecoveryTradeNewImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.SessionReject;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.LiquidityInd;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;
import com.rr.om.BaseOMTestCase;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.warmup.FixTestUtils;

public class ETIBSECodecTest extends BaseOMTestCase {

    private ETIBSEDecoder       _decoder;
    private ETIBSEEncoder       _encoder;
    private byte[]             _bufEn;
    
    private AbstractFixDecoder _testRecoveryDecoder;
    private AbstractFixDecoder _testDecoder;
    private String             _dateStr = "20100510";
    private TimeZoneCalculator _calc;
    private byte[]             _buf;
    private Standard44Encoder  _testEncoder;
    
    private SingleExchangeInstrumentStore _instrumentLocator;

    
    @Override
    public void setUp() throws FileException{
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        
        loadExchanges();
        Exchange e = ExchangeManager.instance().getByREC( new ViewString("BO") );
        
        _instrumentLocator = new SingleExchangeInstrumentStore( e, 1000 );
        
        String instFile = "../data/bse/common/cdx/sim/bseInst.cdx.sim.dat";
        FixInstrumentLoader loader = new FixInstrumentLoader( _instrumentLocator );
        loader.loadFromFile( instFile, e.getRecCode() );

        _bufEn   = new byte[8192];
        _decoder = ETITestUtils.getBSEDecoder( _dateStr );
        _encoder = new ETIBSEEncoder( _bufEn, 0 );

        _decoder.setDebug( true );
        _encoder.setDebug( true );
        
        _buf = new byte[8192];
        _testRecoveryDecoder = FixTestUtils.getRecoveryDecoder44();
        _testDecoder = FixTestUtils.getDecoder44();
        _testEncoder = new Standard44Encoder( (byte)'4', (byte)'4', _buf );
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _testDecoder.setTimeZoneCalculator( _calc );

        _testDecoder.setInstrumentLocator( _instrumentLocator );
        _decoder.setInstrumentLocator( _instrumentLocator );
    }
    
    public void testRestingSingleFill() {
        //                                  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50
        String msg = "C8 00 00 00 78 27 00 00 E8 AF A7 3D AC AD 9C 13 47 57 23 3E AC AD 9C 13 FF FF FF FF 01 00 13 9C 9A 5D A1 B9 83 A5 00 00 00 00 00 00 00 02 04 00 01 00 " +
                     "00 00 00 00 00 00 57 47 0E 42 28 A5 9C 13 C0 BA 8A 3C D5 62 04 00 E5 55 2C 02 00 00 00 00 E5 55 2C 02 00 00 00 00 62 43 0F 00 00 00 00 00 A8 2B A3 3D " +
                     "AC AD 9C 13 00 00 00 00 01 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 6C 00 14 01 32 46 00 01 02 41 31 4F 57 4E 00 00 00 00 00 00 00 00 00 00 " +
                     "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 9D 96 6B 01 00 00 00 01 00 00 00 1A 01 00 00 8C EB 00 00 01 00 00 00 ";

        byte[] data = HexUtils.hexStringToBytes( msg );

        RecoveryTradeNewImpl trade = (RecoveryTradeNewImpl) _decoder.decode( data, 0, data.length );
        
        assertEquals( LiquidityInd.AddedLiquidity, trade.getLiquidityInd() );
        assertEquals( 1, trade.getLastQty() );
        assertEquals( 61.0, trade.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( "36460005", trade.getClOrdId().toString() );
        assertEquals( "282", trade.getExecId().toString() );
        assertEquals( "1413185975413000023", trade.getOrderId().toString() );
        assertEquals( OrdStatus.Filled, trade.getOrdStatus() );
        assertEquals( ExecType.Trade, trade.getExecType() );

    }

    /**
     * {    DECODE int 200,  bytes=4, offset=0, raw=[ C8 00 00 00 ] ushort 10103,  bytes=2, offset=4, raw=[ 77 27 ] skip ,  bytes=2, offset=6, raw=[ 00 00 ]
            Known Message : ImmediateExecResponse :
            Field: requestTime : timestampUTC -1167498201,  bytes=8, offset=8, raw=[ 27 54 37 C1 49 AF 9C 13 ]
            Field: trdRegTSTimeIn : timestampUTC -1174714560,  bytes=8, offset=16, raw=[ 40 37 C9 C0 49 AF 9C 13 ]
            Field: trdRegTSTimeOut : timestampUTC -1174338440,  bytes=8, offset=24, raw=[ 78 F4 CE C0 49 AF 9C 13 ]
            Field: sendingTime : timestampUTC -1166516039,  bytes=8, offset=32, raw=[ B9 50 46 C1 49 AF 9C 13 ]
            Field: msgSeqNum : uint 10,  bytes=4, offset=40, raw=[ 0A 00 00 00 ]
            Field: partitionID : ushort 1,  bytes=2, offset=44, raw=[ 01 00 ]
            Field: applID : ubyte ^D,  bytes=1, offset=46, raw=[ 04 ]
            Field: applMsgID : data ,  bytes=16, offset=47, raw=[ 13 9C 9A 5D A1 B9 83 DE 00 00 00 00 00 00 00 01 ]
            Field: lastFragment : ubyte ^A,  bytes=1, offset=63, raw=[ 01 ]
            Field: orderId : ulong 1413185975413001018,  bytes=8, offset=64, raw=[ 3A 4B 0E 42 28 A5 9C 13 ]
            Field: clOrdId : ulong 38470008,  bytes=8, offset=72, raw=[ 78 01 4B 02 00 00 00 00 ]
            Field: origClOrdId : ulong 38470008,  bytes=8, offset=80, raw=[ 78 01 4B 02 00 00 00 00 ]
            Field: securityId : long 1000627,  bytes=8, offset=88, raw=[ B3 44 0F 00 00 00 00 00 ]
            Field: tranExecId : ulong 1413197114389439000,  bytes=8, offset=96, raw=[ 18 22 CA C0 49 AF 9C 13 ]
            Field: trdRegTSEntryTime : timestampUTC -114159617,  bytes=8, offset=104, raw=[ FF FF FF FF FF FF FF FF ]
            Field: trdRegTSTimePriority : timestampUTC -114159617,  bytes=8, offset=112, raw=[ FF FF FF FF FF FF FF FF ]
            Field: marketSegmentID : int 1,  bytes=4, offset=120, raw=[ 01 00 00 00 ]
            Field: leavesQty : qty 6,  bytes=4, offset=124, raw=[ 06 00 00 00 ]
            Field: cumQty : qty 4,  bytes=4, offset=128, raw=[ 04 00 00 00 ]
            Field: cxlQty : qty 0,  bytes=4, offset=132, raw=[ 00 00 00 00 ]
            Field: noLegExecs : ushort 0,  bytes=2, offset=136, raw=[ 00 00 ]
            Field: execRestatementReason : ushort 101,  bytes=2, offset=138, raw=[ 65 00 ]
            Field: productComplex : ubyte ^A,  bytes=1, offset=140, raw=[ 01 ]
            Field: ordStatus : char 1,  bytes=1, offset=141, raw=[ 31 ]
            Field: execType : char F,  bytes=1, offset=142, raw=[ 46 ]
            Field: triggered : ubyte ^@,  bytes=1, offset=143, raw=[ 00 ]
            Field: noFills : ubyte ^B,  bytes=1, offset=144, raw=[ 02 ]
            Field: filler : skip ,  bytes=7, offset=145, raw=[ 00 00 00 00 00 00 00 ]
            Field: fillPx : price 62.8,  bytes=8, offset=152, raw=[ 00 32 51 76 01 00 00 00 ]
            Field: fillQty : qty 3,  bytes=4, offset=160, raw=[ 03 00 00 00 ]
            Field: fillMatchID : uint 291,  bytes=4, offset=164, raw=[ 23 01 00 00 ]
            Field: fillExecID : uint 62200,  bytes=4, offset=168, raw=[ F8 F2 00 00 ]
            Field: fillLiquidityInd : ubyte ^B,  bytes=1, offset=172, raw=[ 02 ]
            Field: fillerFillsGrp : skip ,  bytes=3, offset=173, raw=[ 00 00 00 ]
            Field: fillPx : price 62.9,  bytes=8, offset=176, raw=[ 80 C8 E9 76 01 00 00 00 ]
            Field: fillQty : qty 1,  bytes=4, offset=184, raw=[ 01 00 00 00 ]
            Field: fillMatchID : uint 292,  bytes=4, offset=188, raw=[ 24 01 00 00 ]
            Field: fillExecID : uint 62400,  bytes=4, offset=192, raw=[ C0 F3 00 00 ]
            Field: fillLiquidityInd : ubyte ^B,  bytes=1, offset=196, raw=[ 02 ]
            Field: fillerFillsGrp : skip ,  bytes=3, offset=197, raw=[ 00 00 00 ]
        }
     */
    public void testImmediateDualFill() {
        //                                  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50
        String msg = "C8 00 00 00 77 27 00 00 27 54 37 C1 49 AF 9C 13 40 37 C9 C0 49 AF 9C 13 78 F4 CE C0 49 AF 9C 13 B9 50 46 C1 49 AF 9C 13 0A 00 00 00 01 00 04 13 9C 9A 5D A1 B9 83 DE 00 00 00 00 00 00 00 01 01 3A 4B 0E 42 28 A5 9C 13 78 01 4B 02 00 00 00 00 78 01 4B 02 00 00 00 00 B3 44 0F 00 00 00 00 00 18 22 CA C0 49 AF 9C 13 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF 01 00 00 00 06 00 00 00 04 00 00 00 00 00 00 00 00 00 65 00 01 31 46 00 02 00 00 00 00 00 00 00 00 32 51 76 01 00 00 00 03 00 00 00 23 01 00 00 F8 F2 00 00 02 00 00 00 80 C8 E9 76 01 00 00 00 01 00 00 00 24 01 00 00 C0 F3 00 00 02 00 00 00";

        byte[] data = HexUtils.hexStringToBytes( msg );

        RecoveryTradeNewImpl trade = (RecoveryTradeNewImpl) _decoder.decode( data, 0, data.length );
        
        // RecoveryTradeNewImpl , lastQty=1, lastPx=62.9, liquidityInd=RemovedLiquidity, multiLegReportingType=null, lastMkt=, text=, securityDesc=, execId=292, clOrdId=38470008, 
        // securityId=1000627, symbol=, currency=, securityIDSource=, orderId=1413185975413001018, execType=Trade, ordStatus=PartiallyFilled, leavesQty=6, cumQty=4, avgPx=[null], 
        // orderQty=[null], price=[null], side=null, mktCapacity=null, onBehalfOfId=, msgSeqNum=10, possDupFlag=N, sendingTime=-1166516039

        assertEquals( LiquidityInd.RemovedLiquidity, trade.getLiquidityInd() );
        assertEquals( 3, trade.getLastQty() );
        assertEquals( 62.8, trade.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( "38470008", trade.getClOrdId().toString() );
        assertEquals( "291", trade.getExecId().toString() );
        assertEquals( "1413185975413001018", trade.getOrderId().toString() );
        assertEquals( OrdStatus.PartiallyFilled, trade.getOrdStatus() );
        assertEquals( ExecType.Trade, trade.getExecType() );

        trade = trade.getNext();
        
        assertEquals( LiquidityInd.RemovedLiquidity, trade.getLiquidityInd() );
        assertEquals( 1, trade.getLastQty() );
        assertEquals( 62.9, trade.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( "38470008", trade.getClOrdId().toString() );
        assertEquals( "292", trade.getExecId().toString() );
        assertEquals( "1413185975413001018", trade.getOrderId().toString() );
        assertEquals( OrdStatus.PartiallyFilled, trade.getOrdStatus() );
        assertEquals( ExecType.Trade, trade.getExecType() );
    }

    /**
     {    DECODE int 328,  bytes=4, offset=0, raw=[ 48 01 00 00 ] ushort 10103,  bytes=2, offset=4, raw=[ 77 27 ] skip ,  bytes=2, offset=6, raw=[ 00 00 ]
            Known Message : ImmediateExecResponse :
            Field: requestTime : timestampUTC -261054222,  bytes=8, offset=8, raw=[ F2 90 3E F7 00 B3 9C 13 ]
            Field: trdRegTSTimeIn : timestampUTC -257216179,  bytes=8, offset=16, raw=[ 4D 21 79 F7 00 B3 9C 13 ]
            Field: trdRegTSTimeOut : timestampUTC -256860936,  bytes=8, offset=24, raw=[ F8 8C 7E F7 00 B3 9C 13 ]
            Field: sendingTime : timestampUTC -259969262,  bytes=8, offset=32, raw=[ 12 1F 4F F7 00 B3 9C 13 ]
            Field: msgSeqNum : uint 9,  bytes=4, offset=40, raw=[ 09 00 00 00 ]
            Field: partitionID : ushort 1,  bytes=2, offset=44, raw=[ 01 00 ]
            Field: applID : ubyte ^D,  bytes=1, offset=46, raw=[ 04 ]
            Field: applMsgID : data ,  bytes=16, offset=47, raw=[ 13 9C 9A 5D A1 B9 84 77 00 00 00 00 00 00 00 01 ]
            Field: lastFragment : ubyte ^A,  bytes=1, offset=63, raw=[ 01 ]
            Field: orderId : ulong 1413185975413003004,  bytes=8, offset=64, raw=[ FC 52 0E 42 28 A5 9C 13 ]
            Field: clOrdId : ulong 41920007,  bytes=8, offset=72, raw=[ 07 A6 7F 02 00 00 00 00 ]
            Field: origClOrdId : ulong 41920007,  bytes=8, offset=80, raw=[ 07 A6 7F 02 00 00 00 00 ]
            Field: securityId : long 72057598332895242,  bytes=8, offset=88, raw=[ 0A 00 00 00 01 00 00 01 ]
            Field: tranExecId : ulong 1413201199820844000,  bytes=8, offset=96, raw=[ E0 2B 7A F7 00 B3 9C 13 ]
            Field: trdRegTSEntryTime : timestampUTC -114159617,  bytes=8, offset=104, raw=[ FF FF FF FF FF FF FF FF ]
            Field: trdRegTSTimePriority : timestampUTC -114159617,  bytes=8, offset=112, raw=[ FF FF FF FF FF FF FF FF ]
            Field: marketSegmentID : int 1,  bytes=4, offset=120, raw=[ 01 00 00 00 ]
            Field: leavesQty : qty 0,  bytes=4, offset=124, raw=[ 00 00 00 00 ]
            Field: cumQty : qty 3,  bytes=4, offset=128, raw=[ 03 00 00 00 ]
            Field: cxlQty : qty 0,  bytes=4, offset=132, raw=[ 00 00 00 00 ]
            Field: noLegExecs : ushort 4,  bytes=2, offset=136, raw=[ 04 00 ]
            Field: execRestatementReason : ushort 101,  bytes=2, offset=138, raw=[ 65 00 ]
            Field: productComplex : ubyte ^E,  bytes=1, offset=140, raw=[ 05 ]
            Field: ordStatus : char 2,  bytes=1, offset=141, raw=[ 32 ]
            Field: execType : char F,  bytes=1, offset=142, raw=[ 46 ]
            Field: triggered : ubyte ^@,  bytes=1, offset=143, raw=[ 00 ]
            Field: noFills : ubyte ^B,  bytes=1, offset=144, raw=[ 02 ]
            Field: filler : skip ,  bytes=7, offset=145, raw=[ 00 00 00 00 00 00 00 ]
            Field: fillPx : price 0.305,  bytes=8, offset=152, raw=[ A0 64 D1 01 00 00 00 00 ]
            Field: fillQty : qty 1,  bytes=4, offset=160, raw=[ 01 00 00 00 ]
            Field: fillMatchID : uint 299,  bytes=4, offset=164, raw=[ 2B 01 00 00 ]
            Field: fillExecID : uint 63800,  bytes=4, offset=168, raw=[ 38 F9 00 00 ]
            Field: fillLiquidityInd : ubyte ^B,  bytes=1, offset=172, raw=[ 02 ]
            Field: fillerFillsGrp : skip ,  bytes=3, offset=173, raw=[ 00 00 00 ]
            Field: fillPx : price 0.315,  bytes=8, offset=176, raw=[ E0 A6 E0 01 00 00 00 00 ]
            Field: fillQty : qty 2,  bytes=4, offset=184, raw=[ 02 00 00 00 ]
            Field: fillMatchID : uint 300,  bytes=4, offset=188, raw=[ 2C 01 00 00 ]
            Field: fillExecID : uint 64000,  bytes=4, offset=192, raw=[ 00 FA 00 00 ]
            Field: fillLiquidityInd : ubyte ^B,  bytes=1, offset=196, raw=[ 02 ]
            Field: fillerFillsGrp : skip ,  bytes=3, offset=197, raw=[ 00 00 00 ]

     * RecoveryTradeNewImpl , lastQty=1, lastPx=0.305, liquidityInd=RemovedLiquidity, multiLegReportingType=null, lastMkt=, text=, securityDesc=, execId=299, clOrdId=41920007, 
     * securityId=72057598332895242, symbol=, currency=, securityIDSource=, orderId=1413185975413003004, execType=Trade, ordStatus=Filled, leavesQty=0, cumQty=3, avgPx=[null], 
     * orderQty=[null], price=[null], side=null, mktCapacity=null, onBehalfOfId=, msgSeqNum=9, possDupFlag=N, sendingTime=-259969262
     */
    public void testImmediateDualMultiLegFill() {
        //                                  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50
        String msg = "48 01 00 00 77 27 00 00 F2 90 3E F7 00 B3 9C 13 4D 21 79 F7 00 B3 9C 13 F8 8C 7E F7 00 B3 9C 13 12 1F 4F F7 00 B3 9C 13 09 00 00 00 01 00 04 13 9C 9A 5D A1 B9 84 77 00 00 00 00 00 00 00 01 01 FC 52 0E 42 28 A5 9C 13 07 A6 7F 02 00 00 00 00 07 A6 7F 02 00 00 00 00 0A 00 00 00 01 00 00 01 E0 2B 7A F7 00 B3 9C 13 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF 01 00 00 00 00 00 00 00 03 00 00 00 00 00 00 00 04 00 65 00 05 32 46 00 02 00 00 00 00 00 00 00 A0 64 D1 01 00 00 00 00 01 00 00 00 2B 01 00 00 38 F9 00 00 02 00 00 00 E0 A6 E0 01 00 00 00 00 02 00 00 00 2C 01 00 00 00 FA 00 00 02 00 00 00 60 4A 0F 00 00 00 00 00 00 E2 C2 81 01 00 00 00 01 00 00 00 39 F9 00 00 01 01 00 00 00 00 00 00 1E 4B 0F 00 00 00 00 00 A0 46 94 83 01 00 00 00 01 00 00 00 3A F9 00 00 02 01 00 00 00 00 00 00 60 4A 0F 00 00 00 00 00 E0 40 BB 81 01 00 00 00 02 00 00 00 01 FA 00 00 01 02 00 00 00 00 00 00 1E 4B 0F 00 00 00 00 00 C0 E7 9B 83 01 00 00 00 02 00 00 00 02 FA 00 00 02 02 00 00 00 00 00 00";

        byte[] data = HexUtils.hexStringToBytes( msg );

        RecoveryTradeNewImpl trade = (RecoveryTradeNewImpl) _decoder.decode( data, 0, data.length );

        assertEquals( LiquidityInd.RemovedLiquidity, trade.getLiquidityInd() );
        assertEquals( 1, trade.getLastQty() );
        assertEquals( 0.305, trade.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( "41920007", trade.getClOrdId().toString() );
        assertEquals( "299", trade.getExecId().toString() );
        assertEquals( "1413185975413003004", trade.getOrderId().toString() );
        assertEquals( OrdStatus.Filled, trade.getOrdStatus() );
        assertEquals( ExecType.Trade, trade.getExecType() );

        trade = trade.getNext();
        
        assertEquals( LiquidityInd.RemovedLiquidity, trade.getLiquidityInd() );
        assertEquals( 2, trade.getLastQty() );
        assertEquals( 0.315, trade.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( "41920007", trade.getClOrdId().toString() );
        assertEquals( "300", trade.getExecId().toString() );
        assertEquals( "1413185975413003004", trade.getOrderId().toString() );
        assertEquals( OrdStatus.Filled, trade.getOrdStatus() );
        assertEquals( ExecType.Trade, trade.getExecType() );
    }

    
    /**
     * 
        {    DECODE int 104,  bytes=4, offset=0, raw=[ 68 00 00 00 ] ushort 10111,  bytes=2, offset=4, raw=[ 7F 27 ] skip ,  bytes=2, offset=6, raw=[ 00 00 ]
        Field: requestTime : timestampUTC 2009742884,  bytes=8, offset=8, raw=[ 24 2E 98 7E 41 B2 9C 13 ]
        Field: trdRegTSTimeIn : timestampUTC 2011562727,  bytes=8, offset=16, raw=[ E7 F2 B3 7E 41 B2 9C 13 ]
        Field: trdRegTSTimeOut : timestampUTC 2011802896,  bytes=8, offset=24, raw=[ 10 9D B7 7E 41 B2 9C 13 ]
        Field: sendingTime : timestampUTC 2010555436,  bytes=8, offset=32, raw=[ 2C 94 A4 7E 41 B2 9C 13 ]
        Field: msgSeqNum : uint 4,  bytes=4, offset=40, raw=[ 04 00 00 00 ]
        Field: lastFragment : ubyte ^A,  bytes=1, offset=44, raw=[ 01 ]
        Field: filler1 : skip ,  bytes=3, offset=45, raw=[ 00 00 00 ]
        Field: orderId : ulong 1413185975413000037,  bytes=8, offset=48, raw=[ 65 47 0E 42 28 A5 9C 13 ]
        Field: clOrdId : ulong 41920002,  bytes=8, offset=56, raw=[ 02 A6 7F 02 00 00 00 00 ]
        Field: origClOrdId : ulong 41920001,  bytes=8, offset=64, raw=[ 01 A6 7F 02 00 00 00 00 ]
        Field: securityId : long 1000290,  bytes=8, offset=72, raw=[ 62 43 0F 00 00 00 00 00 ]
        Field: execId : ulong 1413200377455899000,  bytes=8, offset=80, raw=[ 78 F1 B4 7E 41 B2 9C 13 ]
        Field: cumQty : qty 0,  bytes=4, offset=88, raw=[ 00 00 00 00 ]
        Field: cxlQty : qty 1,  bytes=4, offset=92, raw=[ 01 00 00 00 ]
        Field: ordStatus : char 4,  bytes=1, offset=96, raw=[ 34 ]
        Field: execType : char 4,  bytes=1, offset=97, raw=[ 34 ]
        Field: execRestatementReason : ushort 103,  bytes=2, offset=98, raw=[ 67 00 ]
        Field: productComplex : ubyte ^A,  bytes=1, offset=100, raw=[ 01 ]
        Field: filler2 : skip ,  bytes=3, offset=101, raw=[ 00 00 00 ]
        }
        
        RecoveryCancelledImpl , clOrdId=41920002, origClOrdId=41920001, execId=1413200377455899000, securityId=1000290, symbol=, currency=, 
                                securityIDSource=, orderId=1413185975413000037, execType=Canceled, ordStatus=Canceled, leavesQty=[null], cumQty=0, avgPx=[null], orderQty=[null], 
                                price=[null], side=null, mktCapacity=null, onBehalfOfId=, msgSeqNum=4, possDupFlag=N, sendingTime=2010555436
     */
    
    public void testCancelledSingleLeg() {
        //                                  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50
        String msg = "68 00 00 00 7F 27 00 00 24 2E 98 7E 41 B2 9C 13 E7 F2 B3 7E 41 B2 9C 13 10 9D B7 7E 41 B2 9C 13 2C 94 A4 7E 41 B2 9C 13 04 00 00 00 01 00 00 00 65 47 0E 42 28 A5 9C 13 02 A6 7F 02 00 00 00 00 01 A6 7F 02 00 00 00 00 62 43 0F 00 00 00 00 00 78 F1 B4 7E 41 B2 9C 13 00 00 00 00 01 00 00 00 34 34 67 00 01 00 00 00";

        byte[] data = HexUtils.hexStringToBytes( msg );

        RecoveryCancelledImpl trade = (RecoveryCancelledImpl) _decoder.decode( data, 0, data.length );
        
        assertEquals( "41920002", trade.getClOrdId().toString() );
        assertEquals( "1413200377455899000", trade.getExecId().toString() );
        assertEquals( "1413185975413000037", trade.getOrderId().toString() );
        assertEquals( OrdStatus.Canceled, trade.getOrdStatus() );
        assertEquals( ExecType.Canceled, trade.getExecType() );
    }
    
    
    public void testFillFail() {
        //                                  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50
        String msg = "B0 00 00 00 77 27 00 00 B3 1D 02 4B 24 64 9B 13 7F E4 31 4B 24 64 9B 13 E8 E4 37 4B 24 64 9B 13 EC 9E 13 4B 24 64 9B 13 05 00 00 00 01 00 04 13 9B 61 94 87 73 2A 96 00 00 00 00 00 00 00 01 01 41 FC 66 3D 16 64 9B 13 23 0B 20 00 00 00 00 00 23 0B 20 00 00 00 00 00 ED 48 0F 00 00 00 00 00 C0 DF 32 4B 24 64 9B 13 FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF 01 00 00 00 00 00 00 00 05 00 00 00 00 00 00 00 00 00 65 00 01 32 46 00 01 00 00 00 00 00 00 00 50 FB 9D 7E 01 00 00 00 05 00 00 00 0D 00 00 00 8C 0A 00 00 02 00 00 00";

        byte[] data = HexUtils.hexStringToBytes( msg );

        // ETILoggedOnState will morph to MarketRejectedImpl
        
        @SuppressWarnings( "unused" )
        RecoveryTradeNewImpl reject = (RecoveryTradeNewImpl) _decoder.decode( data, 0, data.length );
    }

    public void testSessionReject() {
        //                                  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50
        String msg = "48 00 00 00 1A 27 00 00 8E 29 29 85 7C 5A 96 13 E6 78 92 85 7C 5A 96 13 01 00 00 00 00 00 00 00 63 00 00 00 20 00 04 00 63 6F 6E 6E 65 63 74 69 6F 6E 20 61 75 74 68 65 6E 74 69 63 61 74 69 6F 6E 20 66 61 69 6C 65 64";

        byte[] data = HexUtils.hexStringToBytes( msg );

        SessionReject reject = (SessionReject) _decoder.decode( data, 0, data.length );
        
        assertEquals( "connection authentication failed", reject.getText().toString() );
    }
    
    public void testOrderRejectDecode() {
        //                                  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50
        String msg = "58 00 00 00 1A 27 00 00 B4 94 30 06 4B D6 97 13 2D 9B 72 06 4B D6 97 13 03 00 00 00 00 00 00 00 05 00 00 00 2C 00 00 00 49 6E 76 61 6C 69 64 20 76 61 6C 75 65 20 33 20 69 6E 20 66 69 65 6C 64 20 54 72 61 64 69 6E 67 53 65 73 73 69 6F 6E 53 75 62 49 44 00 00 00 00";

        byte[] data = HexUtils.hexStringToBytes( msg );

        // ETILoggedOnState will morph to MarketRejectedImpl
        
        SessionReject reject = (SessionReject) _decoder.decode( data, 0, data.length );
        
        assertEquals( "Invalid value 3 in field TradingSessionSubID", reject.getText().toString() );
        assertEquals( 3, reject.getRefSeqNum() );
    }
    
    public void testNOSEncodeDecodeLongInst() {

        _decoder.setExchangeEmulationOn();
        
        String nos = "8=FIX.4.2;9=207;35=D;49=1T1234N;52=20130812-11:26:40.313;56=CME;40=2;11=1000005;21=1;38=5;55=GBPSEPOCT14=4895;100=BO;48=72057606922829944;22=8;50=1234;54=1;57=G;59=0;60=20130812-11:26:40;107=EBPM4;142=IN;167=FUT;204=1;1028=N;9702=1;10=220;";

        nos = FixTestUtils.semiColonToFixDelimStr( nos );
        
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
        
        assertSame( srcNOS.getInstrument(), newNOS.getInstrument() );
    }

    public void testNOSEncodeDecode() {

        _decoder.setExchangeEmulationOn();
        
        String nos = "8=FIX.4.4; 9=172; 35=D; 43=N; 49=PROPA; 56=ME; 34=12243; 52="+ _dateStr + "-12:01:01.100; " +
                     "11=12345678; 59=0; 22=8; 100=BO; 48=72057606922829944; 38=50; 44=10.23; 15=GBP; 40=2; 54=1; 55=BT.L; 21=1; " +
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
        
        assertSame( srcNOS.getInstrument(), newNOS.getInstrument() );
    }
    
    public ClientNewOrderSingleImpl makeNOS( byte[] fixMsg, int offset, int len, Map<String, String> vals ) {
        Message msg = _testDecoder.decode( fixMsg, offset, len );
        ClientNewOrderSingleImpl nos = (ClientNewOrderSingleImpl) msg;
        return nos;
    }

    public MarketNewOrderAckImpl makeACK( byte[] fixMsg, int offset, int len, Map<String, String> vals ) {
        Message msg = _testDecoder.decode( fixMsg, offset, len );
        MarketNewOrderAckImpl ack = (MarketNewOrderAckImpl) msg;
        return ack;
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
}
