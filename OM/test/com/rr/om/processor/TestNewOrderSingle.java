/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.core.lang.ViewString;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Currency;
import com.rr.core.model.Exchange;
import com.rr.core.model.Message;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.utils.NumberUtils;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.om.dummy.warmup.DummyExchange;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.model.id.DailyIntegerIDGenerator;
import com.rr.om.model.instrument.InstrumentWrite;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.warmup.FixTestUtils;

public class TestNewOrderSingle extends BaseProcessorTestCase {

    @SuppressWarnings( "boxing" )
    public void testNOS() {
        
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, _upMsgHandler );
        _proc.handle( nos );
        
        assertEquals( nos.getClOrdId(), nos.getSrcLinkId() );
        
        Order order = _proc.getOrder( nos.getClOrdId() );
        
        checkQueueSize( 0, 1, 0 );
        
        assertNotNull( order );
        
        assertSame( _proc.getStatePendingNew(),  order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
        
        OrderVersion ver = order.getLastAckedVerion();

        assertNotNull( ver );
        
        assertSame(   nos,                      ver.getBaseOrderRequest() );
        assertEquals( nos.getOrderQty(),        ver.getLeavesQty() );
        assertEquals( 0.0,                      ver.getAvgPx() );
        assertEquals( 0,                        ver.getCumQty() );
        assertEquals( OrderCapacity.Principal,  ver.getMarketCapacity() );
        
        ClientProfile client = order.getClientProfile();
        
        assertNotNull( client );
        
        double totVal = nos.getPrice() * nos.getOrderQty() * nos.getCurrency().toUSDFactor();
        
        assertSame( order.getClientProfile(), nos.getClient() );
        assertEquals( nos.getOrderQty(),      client.getTotalOrderQty() );
        assertEquals( totVal,                 client.getTotalOrderValueUSD() );
        
        Message m = getMessage( _downQ, MarketNewOrderSingleImpl.class );
        MarketNewOrderSingleImpl mnos = (MarketNewOrderSingleImpl) m; 

        assertEquals( mnos.getSrcLinkId(), nos.getSrcLinkId() );
        
        checkMarketNOS( nos, mnos );
    }

    public void testAckWithNumericExchangeClOrdId() {
        
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, _upMsgHandler );
        
        Exchange        ex   = new DummyExchange( new ViewString("TST"), new DailyIntegerIDGenerator( 5, 10 ), false );
        InstrumentWrite inst = new DummyInstrumentLocator().getInstrument( new ViewString("BT.TST"), 
                                                                           SecurityIDSource.RIC, 
                                                                           new ViewString("TST"), 
                                                                           new ViewString("TST"), 
                                                                           Currency.EUR );
        inst.setExchange( ex );
        nos.setInstrument( inst );
        
        _proc.handle( nos );
        
        Order order = _proc.getOrder( nos.getClOrdId() );

        checkQueueSize( 0, 1, 0 );
        Message m = getMessage( _downQ, MarketNewOrderSingleImpl.class );
        MarketNewOrderSingleImpl mnos = (MarketNewOrderSingleImpl) m; 

        assertNotNull( order );
        
        OrderVersion ver = order.getLastAckedVerion();

        assertEquals( ver.getMarketClOrdId(),     mnos.getClOrdId() );
        assertTrue( mnos.getClOrdId().equals( nos.getClOrdId() ) == false );
        
        ViewString mClOrdId = mnos.getClOrdId();
        
        int id = NumberUtils.parseInt( mClOrdId );

        assertTrue( id > 500001000 );
        assertTrue( id < 600000000 );

        Order orderByMktClOrdId  = _proc.getOrder( mClOrdId );
        
        assertSame( order, orderByMktClOrdId );
    }
}
