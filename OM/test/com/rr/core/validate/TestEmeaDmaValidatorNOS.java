/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.validate;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.Side;
import com.rr.om.dummy.warmup.DummyExchange;
import com.rr.om.dummy.warmup.TradingRangeImpl;
import com.rr.om.model.instrument.FixedTickSize;
import com.rr.om.model.instrument.InstrumentWrite;
import com.rr.om.order.Order;
import com.rr.om.validate.EmeaDmaValidator;
import com.rr.om.warmup.FixTestUtils;

public class TestEmeaDmaValidatorNOS extends BaseTestCase {

    protected Standard44Decoder _decoder        = FixTestUtils.getDecoder44();
    
    public void testAge() {
        EmeaDmaValidator validator = new EmeaDmaValidator( 1000 );
        
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, null );
        nos.setTransactTime( TimeZoneCalculator.instance().getTimeUTC( System.currentTimeMillis() ) );
        
        Utils.delay( 2000 );
        
        Order order = FixTestUtils.createOrder( nos  );

        doTestFail( validator, nos, order, "Request is older than max allowed seconds 1" );
    }

    public void testZeroQty() {
        
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, null );
        nos.setOrderQty( 0 );
        
        Order order = FixTestUtils.createOrder( nos  );

        doTestFail( nos, order, "Quantity must be greater than zero, qty=0" );
    }

    public void testNegQty() {

        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, null );
        nos.setOrderQty( -10 );
        
        Order order = FixTestUtils.createOrder( nos  );

        doTestFail( nos, order, "Quantity must be greater than zero, qty=-10" );
    }

    public void testTwoErrIncEmptyClOrdId() {
        
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "", 100, 25.12, null );
        nos.setOrderQty( -10 );
        
        Order order = FixTestUtils.createOrder( nos  );

        doTestFail( nos, order, "Missing clOrdId , Quantity must be greater than zero, qty=-10" );
    }

    public void testExchangeNotOpenAndRECValid() {

        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, null );

        DummyExchange ex = new DummyExchange( new ViewString("DUMMY"), null, false );
        ((InstrumentWrite)nos.getInstrument()).setExchange( ex );
        nos.getInstrument().getExchangeSession().setOpen( Long.MAX_VALUE );
        
        Order order = FixTestUtils.createOrder( nos  );

        doTestFail( nos, order, "EXDEST doesnt match the instrument REC, received TST expected DUMMY, Exchange not open " );
    }
    
    public void testHandlInstruction() {

        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, null );
        nos.setHandlInst( HandlInst.ManualBestExec );

        Order order = FixTestUtils.createOrder( nos  );

        doTestFail( nos, order, "Unsupported attribute value ManualBestExec type HandlInst" );
    }

    public void testInstrumentDisabled() {

        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, null );
        ((InstrumentWrite)nos.getInstrument()).setEnabled( false );

        Order order = FixTestUtils.createOrder( nos  );

        doTestFail( nos, order, "Instrument is disabled, RIC=ICAD.PA" );
    }

    public void testInstrumentRestricted() {

        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, null );
        ((InstrumentWrite)nos.getInstrument()).setRestricted( true );

        Order order = FixTestUtils.createOrder( nos );

        doTestFail( nos, order, "Cant trade restricted stock, bookingType=null orderCapacity=null" );

        // @ TODO TEST RESTRICTED BUT CAN TRADE -  not yet implemented as bank specific
    }

    public void testPriceBand() {

        doBandPassTest( Side.Buy,  24.5, 25.12, 25.12 );
        doBandPassTest( Side.Buy,  24.5, 25.12, 25.119 );
        
        doBandFailTest( Side.Buy,  24.5, 25.12, 25.121,   "Invalid BUY price of 25.121, maxBuy=25.12, tickID=1 flags=0" );
        doBandFailTest( Side.Buy,  24.5, 25.12, -1,       "Invalid BUY price of -1.0, maxBuy=25.12, tickID=1 flags=0" );
        
        doBandPassTest( Side.Sell, 24.5, 25.12, 25.12 );
        doBandPassTest( Side.Sell, 24.5, 25.12, 24.5 );
        doBandFailTest( Side.Sell, 24.5, 25.12, 24.49999, "Invalid SELL price of 24.49999, minSell=24.5, tickID=1 flags=0" );
        doBandFailTest( Side.Sell, 24.5, 25.12, -1,       "Invalid SELL price of -1.0, minSell=24.5, tickID=1 flags=0" );
    }

    public void doBandPassTest( Side side, double low, double high, double price ) {

        EmeaDmaValidator validator = new EmeaDmaValidator( Integer.MAX_VALUE );

        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, price, null );
        TradingRangeImpl band = (TradingRangeImpl) nos.getInstrument().getValidTradingRange();

        nos.setSide( side );
        band.setMaxBuy( 1, high, 0 );
        band.setMinSell( 1, low, 0 );
        Order order = FixTestUtils.createOrder( nos );

        if ( ! validator.validate( nos, order ) ) {
            assertTrue( false ); // ORDER SHOULD OF PASSED
        } 
    }

    public void doBandFailTest( Side side, double low, double high, double price, String expErr ) {

        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, price, null );
        TradingRangeImpl band = (TradingRangeImpl) nos.getInstrument().getValidTradingRange();

        band.setMaxBuy( 1, high, 0 );
        band.setMinSell( 1, low, 0 );
        Order order = FixTestUtils.createOrder( nos );
        nos.setSide( side );

        doTestFail( nos, order, expErr );
    }

    public void testFixedTickScale() {
        
        doTestTick( new FixedTickSize( 0.25 ),     0.27,     0.75 );
        doTestTick( new FixedTickSize( 0.1 ),      8.27,     5.9 );
        doTestTick( new FixedTickSize( 0.00001 ),  0.000005, 0.00003 );
        doTestTick( new FixedTickSize( 2 ),        4.00001,  4.0 );
    }

    private void doTestTick( FixedTickSize t, double exampleFail, double examplePass ) {
        
        EmeaDmaValidator validator = new EmeaDmaValidator( Integer.MAX_VALUE );

        {
            // GOOD PRICE
            ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, examplePass, null );
            ((InstrumentWrite)nos.getInstrument()).setTickType( t );
    
            Order order = FixTestUtils.createOrder( nos  );
    
            if ( ! validator.validate( nos, order ) ) {
                assertTrue( false ); // ORDER SHOULD OF PASSED
            } 
        }

        {
            // BAD PRICE
            ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, exampleFail, null );
            ((InstrumentWrite)nos.getInstrument()).setTickType( t );
    
            Order order = FixTestUtils.createOrder( nos  );
            ReusableString expMsg = new ReusableString( "Failed tick validation " );
            t.writeError( exampleFail, expMsg );

            doTestFail( nos, order, expMsg.toString() );
        }
    }

    private void doTestFail( ClientNewOrderSingleImpl nos, Order order, String expErr ) {
        EmeaDmaValidator validator = new EmeaDmaValidator( Integer.MAX_VALUE );

        doTestFail( validator, nos, order, expErr );
    }

    private void doTestFail( EmeaDmaValidator validator, ClientNewOrderSingleImpl nos, Order order, String expErr ) {
        if ( validator.validate( nos, order ) ) {
            assertTrue( false ); // SHOULD FAIL 
        } else {
            String       rejReason    = validator.getRejectReason().toString();
            OrdRejReason ordRejReason = validator.getOrdRejectReason();
            
            assertEquals( expErr, rejReason );
            assertSame(   OrdRejReason.UnsupOrdCharacteristic, ordRejReason );
        }
    }
}
