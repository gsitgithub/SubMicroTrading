/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.exchange;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.Side;
import com.rr.om.book.sim.SimpleOrderBook;

public class TestSimpleOrderBook extends BaseTestCase {

    // @NOTE this is testing the dummy order book which only gens trades for current order
    // @TODO add the trade for other side too
    
    public void testSimpleFullFillSell() {
        SimpleOrderBook book = new SimpleOrderBook();
        
        TradeNew trade1 = book.add( null, 10, 1.1, null, Side.Buy );
        TradeNew trade2 = book.add( null, 10, 1.1, null, Side.Sell );
        TradeNew trade3 = book.add( null, 1, 1.1, null, Side.Sell );
        
        assertTrue( trade1 == null );
        assertTrue( trade3 == null );
        assertNotNull( trade2 );
        assertEquals( 10, trade2.getLastQty() );
        assertEquals( Side.Sell, trade2.getSide() );
        assertEquals( OrdStatus.Filled, trade2.getOrdStatus() );
        assertEquals( 1.1, trade2.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( 1.1, trade2.getAvgPx(), Constants.TICK_WEIGHT );
    }

    public void testDoubleFullFillSell() {
        SimpleOrderBook book = new SimpleOrderBook();
        
        TradeNew trade1 = book.add( null, 11, 1.1, null, Side.Buy );
        TradeNew trade2 = book.add( null, 10, 1.1, null, Side.Sell );
        TradeNew trade3 = book.add( null, 2, 1.1, null, Side.Sell );
        
        assertTrue( trade1 == null );
        assertNotNull( trade2 );
        assertEquals( 10, trade2.getLastQty() );
        assertEquals( Side.Sell, trade2.getSide() );
        assertEquals( OrdStatus.Filled, trade2.getOrdStatus() );
        assertEquals( 1.1, trade2.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( 1.1, trade2.getAvgPx(), Constants.TICK_WEIGHT );

        assertNotNull( trade3 );
        assertEquals( 1, trade3.getLastQty() );
        assertEquals( Side.Sell, trade3.getSide() );
        assertEquals( OrdStatus.PartiallyFilled, trade3.getOrdStatus() );
        assertEquals( 1.1, trade3.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( 1.1, trade3.getAvgPx(), Constants.TICK_WEIGHT );
    }

    public void testSimpleFullFillBuy() {
        SimpleOrderBook book = new SimpleOrderBook();
        
        TradeNew trade1 = book.add( null, 10, 1.1, null, Side.Sell );
        TradeNew trade2 = book.add( null, 10, 1.1, null, Side.Buy );
        TradeNew trade3 = book.add( null, 1, 1.1, null, Side.Buy );
        
        assertTrue( trade1 == null );
        assertTrue( trade3 == null );
        assertNotNull( trade2 );
        assertEquals( 10, trade2.getLastQty() );
        assertEquals( OrdStatus.Filled, trade2.getOrdStatus() );
        assertEquals( Side.Buy, trade2.getSide() );
        assertEquals( 1.1, trade2.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( 1.1, trade2.getAvgPx(), Constants.TICK_WEIGHT );
    }

    public void testSimplePartialFillBuy() {
        SimpleOrderBook book = new SimpleOrderBook();
        
        TradeNew trade1 = book.add( null, 4, 1.1, null, Side.Sell );
        TradeNew trade2 = book.add( null, 10, 1.1, null, Side.Buy );
        
        assertTrue( trade1 == null );
        assertNotNull( trade2 );
        assertEquals( 4, trade2.getLastQty() );
        assertEquals( OrdStatus.PartiallyFilled, trade2.getOrdStatus() );
        assertEquals( Side.Buy, trade2.getSide() );
        assertEquals( 1.1, trade2.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( 1.1, trade2.getAvgPx(), Constants.TICK_WEIGHT );
    }

    public void testBuyThruLevels() {
        SimpleOrderBook book = new SimpleOrderBook();
        
        TradeNew trade1 = book.add( null, 4, 1.5, null, Side.Sell );
        TradeNew trade2 = book.add( null, 3, 1.4, null, Side.Sell );
        TradeNew trade3 = book.add( null, 3, 1.3, null, Side.Sell );
        TradeNew trade4 = book.add( null, 10, 1.4, null, Side.Buy );
        
        double expPx = (3 * 1.3 + 1.4 * 3) / 6;
        
        assertTrue( trade1 == null );
        assertTrue( trade2 == null );
        assertTrue( trade3 == null );
        assertNotNull( trade4 );
        assertEquals( 6, trade4.getLastQty() );
        assertEquals( OrdStatus.PartiallyFilled, trade4.getOrdStatus() );
        assertEquals( Side.Buy, trade4.getSide() );
        assertEquals( expPx, trade4.getLastPx(), Constants.TICK_WEIGHT );
        assertEquals( expPx, trade4.getAvgPx(), Constants.TICK_WEIGHT );
    }
}
