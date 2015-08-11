/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.core.lang.Constants;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Currency;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.om.model.instrument.InstrumentWrite;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;

public class TestCcyMajorMinor extends BaseProcessorTestCase {

    @SuppressWarnings( "boxing" )
    public void testMinorToMajor() {
        
        double minorPx = 2512.0;
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, minorPx, _upMsgHandler );
        nos.setCurrency( Currency.GBp );
        ((InstrumentWrite)nos.getInstrument()).setCurrency( Currency.GBP );
        _proc.handle( nos );
        MarketNewOrderSingleImpl mnos = (MarketNewOrderSingleImpl) getMessage( _downQ, MarketNewOrderSingleImpl.class );
        
        // MARKET IS IN POUNDS
        assertSame( Currency.GBP, mnos.getCurrency() );
        assertEquals( (minorPx / 100.0), mnos.getPrice(), Constants.WEIGHT );
        
        Order order = _proc.getOrder( nos.getClOrdId() );

        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.12, _mktOrderId, _execId );
        _proc.handle( mack );
        checkQueueSize( 1, 0, 0 );
        ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );

        assertSame( Currency.GBp, cack.getCurrency() );
        assertEquals( minorPx, cack.getPrice(), Constants.WEIGHT );
        
        ClientProfile client = order.getClientProfile();
        
        double totVal = nos.getPrice() * nos.getOrderQty() * nos.getCurrency().toUSDFactor();
        
        assertEquals( totVal, client.getTotalOrderValueUSD() );
    }

    @SuppressWarnings( "boxing" )
    public void testMajorToMinor() {
        
        double majorPx = 25.12;
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, majorPx, _upMsgHandler );
        nos.setCurrency( Currency.GBP );
        ((InstrumentWrite)nos.getInstrument()).setCurrency( Currency.GBp );
        _proc.handle( nos );
        MarketNewOrderSingleImpl mnos = (MarketNewOrderSingleImpl) getMessage( _downQ, MarketNewOrderSingleImpl.class );
        
        // MARKET IS IN POUNDS
        assertSame( Currency.GBp, mnos.getCurrency() );
        assertEquals( (majorPx * 100.0), mnos.getPrice(), Constants.PRICE_DP );
        
        Order order = _proc.getOrder( nos.getClOrdId() );

        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.12, _mktOrderId, _execId );
        _proc.handle( mack );
        checkQueueSize( 1, 0, 0 );
        ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );

        assertSame( Currency.GBP, cack.getCurrency() );
        assertEquals( majorPx, cack.getPrice(), Constants.WEIGHT );
        
        ClientProfile client = order.getClientProfile();
        
        double totVal = nos.getPrice() * nos.getOrderQty() * nos.getCurrency().toUSDFactor();
        
        assertEquals( totVal, client.getTotalOrderValueUSD() );
    }

}
