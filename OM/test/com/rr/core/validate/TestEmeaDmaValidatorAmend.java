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
import com.rr.core.lang.ZString;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Currency;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.internal.events.impl.ClientCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.om.dummy.warmup.DummyExchange;
import com.rr.om.dummy.warmup.TradingRangeImpl;
import com.rr.om.model.instrument.FixedTickSize;
import com.rr.om.model.instrument.InstrumentWrite;
import com.rr.om.order.Order;
import com.rr.om.validate.EmeaDmaValidator;
import com.rr.om.warmup.FixTestUtils;

public class TestEmeaDmaValidatorAmend extends BaseTestCase {

    private ClientProfile                   _testClient;
    private Standard44Decoder               _decoder;
    private ReusableString                  _buffer;
    private int                             _qty;
    private double                          _price;
    private ClientNewOrderSingleImpl        _nos;
    private ZString                         _repClOrdId;
    private ClientCancelReplaceRequestImpl  _rep;
    private EmeaDmaValidator                _validator;

    @Override
    public  void setUp() {
        _testClient = FixTestUtils.getTestClient();
        _decoder    = FixTestUtils.getDecoder44( _testClient );
        _buffer     = new ReusableString();    
        _qty        = 100;
        _price      = 25.12;
        _nos        = FixTestUtils.getClientNOS( _decoder, "TST0000001", _qty, _price, null );
        _repClOrdId = new ViewString( "TST0000002" );
        _validator  = new EmeaDmaValidator( Integer.MAX_VALUE );
        _rep        = FixTestUtils.getClientCancelReplaceRequest( _buffer, _decoder, _repClOrdId, _nos.getClOrdId(), _qty, _price, null );
        
        _rep.setTransactTime( TimeZoneCalculator.instance().getTimeUTC( System.currentTimeMillis() ) );
        _rep.setInstrument( _nos.getInstrument() );
        _rep.setCurrency(   _nos.getCurrency() );
        _rep.setSide(       _nos.getSide() );
        _rep.setOrderQty(    _nos.getOrderQty()-10 );
        _rep.setTimeInForce( TimeInForce.ImmediateOrCancel );
        _rep.setPrice(       _nos.getPrice() +  0.1 );
    }
    
    public void testAge() {
        EmeaDmaValidator validator = new EmeaDmaValidator( 1000 );
        
        _rep.setTransactTime( TimeZoneCalculator.instance().getTimeUTC( System.currentTimeMillis() ) );
        
        Utils.delay( 2000 );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );

        doTestFail( validator, order, "Request is older than max allowed seconds 1" );
    }
    
    public void testNoFieldsChange() {
        
        _rep.setOrderQty(    _nos.getOrderQty() );
        _rep.setTimeInForce( _nos.getTimeInForce() );
        _rep.setPrice(       _nos.getPrice() );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );

        doTestFail( order, "At least one of Qty/Price/TIF must change on an amend" );
    }
    
    public void testQtyChangedOk() {
        _rep.setOrderQty(    _nos.getOrderQty()-10 );
        _rep.setTimeInForce( _nos.getTimeInForce() );
        _rep.setPrice(       _nos.getPrice() );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );

        if ( ! _validator.validate( _rep, order ) ) {
            assertTrue( false ); // ORDER SHOULD OF PASSED
        } 
    }
    
    public void testPriceChangedOk() {
        _rep.setOrderQty(    _nos.getOrderQty() );
        _rep.setTimeInForce( _nos.getTimeInForce() );
        _rep.setPrice(       _nos.getPrice()+0.01 );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );

        if ( ! _validator.validate( _rep, order ) ) {
            assertTrue( false ); // ORDER SHOULD OF PASSED
        } 
    }
    
    public void testChangedTIFOk() {
        _rep.setOrderQty(    _nos.getOrderQty() );
        _rep.setTimeInForce( TimeInForce.FillOrKill );
        _rep.setPrice(       _nos.getPrice() );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );

        if ( ! _validator.validate( _rep, order ) ) {
            assertTrue( false ); // ORDER SHOULD OF PASSED
        } 
    }
    
    public void testZeroQty() {
        
        _rep.setOrderQty( 0 );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );

        doTestFail( order, "Quantity must be greater than zero, qty=0" );
    }

    public void testNegQty() {

        _rep.setOrderQty( -10 );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );

        doTestFail( order, "Cannot amend qty below cumQty, qty=-10 cumQty 0, Quantity must be greater than zero, qty=-10" );
    }

    public void testEmptyClOrdId() {

        _rep.getClOrdId().reset();
        
        Order order = FixTestUtils.createOrder( _nos, _rep );
        
        doTestFail( order, "Missing clOrdId " );
    }

    public void testCantChangeSide() {

        _nos.setSide( Side.Buy );
        _rep.setSide( Side.Sell );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );
        
        doTestFail( order, "Unable to change the side  from Buy to Sell" );
    }

    public void testCantChangeCurrency() {

        _nos.setCurrency( Currency.USD );
        _rep.setCurrency( Currency.GBP );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );
        
        doTestFail( order, "Unable to change the currency  from USD to GBP" );
    }

    public void testCantChangeOrdType() {

        _nos.setOrdType( OrdType.Limit );
        _rep.setOrdType( OrdType.Market );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );
        
        doTestFail( order, "Unable to change the order type  from Limit to Market" );
    }

    public void testExchangeNotOpenAndRECValid() {

        DummyExchange ex = new DummyExchange( new ViewString("DUMMY"), null, false );
        ((InstrumentWrite)_rep.getInstrument()).setExchange( ex );
        _rep.getInstrument().getExchangeSession().setOpen( Long.MAX_VALUE );
        
        Order order = FixTestUtils.createOrder( _nos, _rep );

        doTestFail( order, "EXDEST doesnt match the instrument REC, received TST expected DUMMY, Exchange not open " );
    }
    
    public void testHandlInstruction() {

        _rep.setHandlInst( HandlInst.ManualBestExec );

        Order order = FixTestUtils.createOrder( _nos, _rep );

        doTestFail( order, "Unsupported attribute value ManualBestExec type HandlInst" );
    }

    public void testInstrumentDisabled() {

        ((InstrumentWrite)_rep.getInstrument()).setEnabled( false );

        Order order = FixTestUtils.createOrder( _nos, _rep );

        doTestFail( order, "Instrument is disabled, RIC=ICAD.PA" );
    }

    public void testInstrumentRestricted() {

        ((InstrumentWrite)_rep.getInstrument()).setRestricted( true );

        Order order = FixTestUtils.createOrder( _nos, _rep );

        doTestFail( order, "Cant trade restricted stock, bookingType=null orderCapacity=null" );

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

        _rep.setPrice( price );
        TradingRangeImpl band = (TradingRangeImpl) _rep.getInstrument().getValidTradingRange();

        _nos.setSide( side );
        _rep.setSide( side );
        band.setMaxBuy( 1, high, 0 );
        band.setMinSell( 1, low, 0 );
        Order order = FixTestUtils.createOrder( _nos, _rep );

        if ( ! validator.validate( _rep, order ) ) {
            assertTrue( false ); // ORDER SHOULD OF PASSED
        } 
    }

    public void doBandFailTest( Side side, double low, double high, double price, String expErr ) {

        _rep.setPrice( price );
        TradingRangeImpl band = (TradingRangeImpl) _rep.getInstrument().getValidTradingRange();

        band.setMaxBuy( 1, high, 0 );
        band.setMinSell( 1, low, 0 );
        Order order = FixTestUtils.createOrder( _nos, _rep );
        _rep.setSide( side );

        doTestFail( order, expErr );
    }

    public void testFixedTickScale() {
        
        doTestTick( new FixedTickSize( 0.25 ),     0.27,     0.75 );
        doTestTick( new FixedTickSize( 0.1 ),      8.27,     5.9 );
        doTestTick( new FixedTickSize( 0.00001 ),  0.000005, 0.00003 );
    }

    private void doTestTick( FixedTickSize t, double exampleFail, double examplePass ) {
        
        EmeaDmaValidator validator = new EmeaDmaValidator( Integer.MAX_VALUE );

        {
            // GOOD PRICE
            _rep.setPrice( examplePass );
            ((InstrumentWrite)_rep.getInstrument()).setTickType( t );
    
            Order order = FixTestUtils.createOrder( _nos, _rep );
    
            if ( ! validator.validate( _rep, order ) ) {
                assertTrue( false ); // ORDER SHOULD OF PASSED
            } 
        }

        {
            // BAD PRICE
            _rep.setPrice( exampleFail );
            ((InstrumentWrite)_rep.getInstrument()).setTickType( t );
    
            Order order = FixTestUtils.createOrder( _nos, _rep );
            ReusableString expMsg = new ReusableString( "Failed tick validation " );
            t.writeError( exampleFail, expMsg );

            doTestFail( order, expMsg.toString() );
        }
    }

    private void doTestFail( Order order, String expErr ) {
        EmeaDmaValidator validator = new EmeaDmaValidator( Integer.MAX_VALUE );

        doTestFail( validator, order, expErr );
    }

    private void doTestFail( EmeaDmaValidator validator, Order order, String expErr ) {
        if ( validator.validate( _rep, order ) ) {
            assertTrue( false ); // SHOULD FAIL 
        } else {
            String       rejReason    = validator.getRejectReason().toString();
            OrdRejReason ordRejReason = validator.getOrdRejectReason();
            
            assertEquals( expErr, rejReason );
            assertSame(   OrdRejReason.UnsupOrdCharacteristic, ordRejReason );
        }
    }
}
