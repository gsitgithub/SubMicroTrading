/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.book;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Currency;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.core.model.book.DoubleSidedBookEntryImpl;
import com.rr.core.model.book.UnsafeL2Book;
import com.rr.md.book.l3.FullL3OrderBook;
import com.rr.md.book.l3.LongBookPoolMgr;
import com.rr.md.book.l3.OrderBookEntry;
import com.rr.model.generated.internal.events.impl.BookAddOrderImpl;
import com.rr.model.generated.internal.events.impl.BookDeleteOrderImpl;
import com.rr.model.generated.internal.events.impl.BookModifyOrderImpl;
import com.rr.model.generated.internal.events.interfaces.BookAddOrderWrite;
import com.rr.model.generated.internal.events.interfaces.BookDeleteOrderWrite;
import com.rr.model.generated.internal.events.interfaces.BookModifyOrderWrite;
import com.rr.model.generated.internal.type.Side;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.model.instrument.InstrumentWrite;

public class FullL3OrderBookTest extends BaseTestCase {

    private static InstrumentWrite inst = new DummyInstrumentLocator().getInstrument( new ViewString("BT.TST"), 
                                                                                       SecurityIDSource.RIC, 
                                                                                       new ViewString("TST"), 
                                                                                       new ViewString("TST"), 
                                                                                       Currency.EUR );
    
    
    public void testSimpleL1BuyOnly() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1000, Side.Buy,  100, 15.25 );
        
        double[][] results = {{ 100, 15.25, 0, 0.0 }};
        
        verify( book, results );
    }
    
    public void testSimpleL1SellOnly() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1000, Side.Sell,  100, 15.25 );
        
        double[][] results = {{ 0, 0.0, 100, 15.25 }};
        
        verify( book, results );
    }
    
    public void testSimpleL1BothSides() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1000, Side.Buy,  100, 15.25 );
        applyAddEvent( book, 1001, Side.Sell, 55,  15.5 );
        
        double[][] results = {{ 100, 15.25, 55, 15.5 }};
        
        verify( book, results );
    }
    
    public void testSimpleL2BothSides() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1000, Side.Buy,  100, 15.25 );
        applyAddEvent( book, 1001, Side.Sell, 55,  15.5 );
        
        applyAddEvent( book, 1002, Side.Buy,  200, 15.0 );
        applyAddEvent( book, 1003, Side.Sell, 110, 15.75 );
        
        double[][] results = {{ 100, 15.25,  55, 15.5 },
                              { 200, 15.0,  110, 15.75 } };
        
        verify( book, results );
    }
    
    public void testSimpleL5BuyOnly() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1050, Side.Buy,  300, 15.25 );
        applyAddEvent( book, 1041, Side.Buy,  500, 15.0 );
        applyAddEvent( book, 1032, Side.Buy,  400, 15.125 );
        applyAddEvent( book, 1023, Side.Buy,  200, 15.5 );
        applyAddEvent( book, 1014, Side.Buy,  100, 15.75 );
        
        double[][] results = {{ 100, 15.75,  0, 0.0 },
                              { 200, 15.5,   0, 0.0 },
                              { 300, 15.25,  0, 0.0 },
                              { 400, 15.125, 0, 0.0 },
                              { 500, 15.0,   0, 0.0 } };
        
        verify( book, results );
    }
    
    public void testSimpleConsolidateBuyOnly() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1050, Side.Buy,  300, 15.25 );
        applyAddEvent( book, 1041, Side.Buy,  500, 15.0 );
        applyAddEvent( book, 1032, Side.Buy,  400, 15.25 );
        applyAddEvent( book, 1023, Side.Buy,  200, 15.0 );
        applyAddEvent( book, 1014, Side.Buy,  100, 15.0 );
        
        double[][] results = {{ 700, 15.25,  0, 0.0 },
                              { 800, 15.0,   0, 0.0 } };
        
        verify( book, results );
    }

    public void testSimpleL1Mod() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1000, Side.Buy,  100, 15.25 );
        applyModEvent( book, 1000, Side.Buy,   75, 15.25 );
        
        double[][] results = {{ 75, 15.25, 0, 0.0 }};
        
        verify( book, results );
    }
    
    public void testSimpleL1Mod2Empty() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1000, Side.Buy,  100, 15.25 );
        applyModEvent( book, 1000, Side.Buy,   0,  15.25 );
        
        double[][] results = {{ 0, 15.25, 0, 0.0 }};
        
        verify( book, results );
    }
    
    public void testSimpleL1Mod2ShiftLevel() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1000, Side.Buy,  100, 15.25 );
        applyModEvent( book, 1000, Side.Buy,   70, 15.5 );
        
        double[][] results = {{ 70, 15.5, 0, 0.0 }};
        
        verify( book, results );
    }
    
    public void testInsertDelete() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1000, Side.Buy,  100, 15.25 );
        applyDelEvent( book, 1000 );
        
        double[][] results = {};
        
        verify( book, results );
    }
    
    public void testDelBestBuy() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1050, Side.Buy,  300, 15.25 );
        applyAddEvent( book, 1041, Side.Buy,  500, 15.0 );
        applyAddEvent( book, 1032, Side.Buy,  400, 15.125 );
        applyAddEvent( book, 1023, Side.Buy,  200, 15.5 );
        applyAddEvent( book, 1014, Side.Buy,  100, 15.75 );

        applyDelEvent( book, 1014 );
        
        double[][] results = {{ 200, 15.5,   0, 0.0 },
                              { 300, 15.25,  0, 0.0 },
                              { 400, 15.125, 0, 0.0 },
                              { 500, 15.0,   0, 0.0 } };
        
        verify( book, results );
    }
    
    public void testDelHalfLevelBestSellMoves() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1050, Side.Buy,  300, 15.25 );
        applyAddEvent( book, 1041, Side.Buy,  500, 15.0 );
        applyAddEvent( book, 1032, Side.Buy,  400, 15.125 );
        applyAddEvent( book, 1023, Side.Buy,  200, 15.5 );
        applyAddEvent( book, 1014, Side.Buy,  100, 15.75 );
        applyAddEvent( book, 1060, Side.Sell, 300, 15.8 );

        applyDelEvent( book, 1014 );
        
        double[][] results = {{ 200, 15.5,    300, 15.8 },
                              { 300, 15.25,     0,  0.0 },
                              { 400, 15.125,    0,  0.0 },
                              { 500, 15.0,      0,  0.0 } };
        
        verify( book, results );
    }
    
    public void testDelHalfInMiddleSellMoves() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1050, Side.Buy,  300, 15.25 );
        applyAddEvent( book, 1041, Side.Buy,  500, 15.0 );
        applyAddEvent( book, 1032, Side.Buy,  400, 15.125 );
        applyAddEvent( book, 1023, Side.Buy,  200, 15.5 );
        applyAddEvent( book, 1014, Side.Buy,  100, 15.75 );
        
        applyAddEvent( book, 1060, Side.Sell, 301, 16.8 );
        applyAddEvent( book, 1070, Side.Sell, 302, 15.8 );
        applyAddEvent( book, 1080, Side.Sell, 303, 15.9 );
        applyAddEvent( book, 1090, Side.Sell, 304, 16.0 );
        applyAddEvent( book, 1095, Side.Sell, 305, 16.2 );

        applyDelEvent( book, 1014 );
        
        double[][] results = {{ 200, 15.5,    302, 15.8 },
                              { 300, 15.25,   303, 15.9 },
                              { 400, 15.125,  304, 16.0 },
                              { 500, 15.0,    305, 16.2 },
                              {   0, 0.0,     301, 16.8 } };
        
        verify( book, results );
    }
    
    public void testMisc() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1050, Side.Buy,  300, 15.25 );
        applyAddEvent( book, 1041, Side.Buy,  500, 15.0 );
        applyAddEvent( book, 1032, Side.Buy,  400, 15.125 );
        applyAddEvent( book, 1023, Side.Buy,  200, 15.5 );
        applyAddEvent( book, 1014, Side.Buy,  100, 15.75 );
        
        applyAddEvent( book, 1060, Side.Sell, 301, 16.8 );
        applyAddEvent( book, 1070, Side.Sell, 302, 15.8 );
        applyAddEvent( book, 1080, Side.Sell, 303, 15.9 );
        applyAddEvent( book, 1090, Side.Sell, 304, 16.0 );
        applyAddEvent( book, 1095, Side.Sell, 305, 16.2 );

        applyDelEvent( book, 1041 );
        applyDelEvent( book, 1080 );
        
        double[][] results = {{ 100, 15.75,   302, 15.8 },
                              { 200, 15.5,    304, 16.0 },
                              { 300, 15.25,   305, 16.2 },
                              { 400, 15.125,  301, 16.8 } };
        
        verify( book, results );
    }
    
    public void testDelBestBBO() {
        FullL3OrderBook book = new FullL3OrderBook( inst, 0, new LongBookPoolMgr<OrderBookEntry>() );
        
        applyAddEvent( book, 1050, Side.Buy,  300, 15.25 );
        applyAddEvent( book, 1041, Side.Buy,  500, 15.0 );
        applyAddEvent( book, 1032, Side.Buy,  400, 15.125 );
        applyAddEvent( book, 1023, Side.Buy,  200, 15.5 );
        applyAddEvent( book, 1014, Side.Buy,  100, 15.75 );
        applyAddEvent( book, 1060, Side.Sell, 300, 15.8 );

        applyDelEvent( book, 1014 );
        applyDelEvent( book, 1060 );
        
        double[][] results = {{ 200, 15.5,      0,  0.0 },
                              { 300, 15.25,     0,  0.0 },
                              { 400, 15.125,    0,  0.0 },
                              { 500, 15.0,      0,  0.0 } };
        
        verify( book, results );
    }
    
    private void verify( FullL3OrderBook book, double[][] results ) {
        UnsafeL2Book snappedBook = new UnsafeL2Book( inst, 10 ); 
        book.snap( snappedBook );
        
        int numLevels = results.length;
        
        assertTrue( numLevels == book.getActiveLevels() );

        DoubleSidedBookEntry entry = new DoubleSidedBookEntryImpl();
        
        for( int l=0 ; l < numLevels ; l++ ) {
            boolean ok = snappedBook.getLevel( l, entry );
            
            assertTrue( ok );
            assertTrue( results[l].length == 4 );
            
            assertEquals( results[l][0], entry.getBidQty(),      Constants.TICK_WEIGHT );
            assertEquals( results[l][1], entry.getBidPx(),    Constants.TICK_WEIGHT  );
            assertEquals( results[l][2], entry.getAskQty(),     Constants.TICK_WEIGHT  );
            assertEquals( results[l][3], entry.getAskPx(),   Constants.TICK_WEIGHT  );
        }
    }

    private void applyAddEvent( FullL3OrderBook book, long orderId, Side side, int qty, double px ) {
        BookAddOrderWrite event  = new BookAddOrderImpl();
        event.setBook( book );
        event.setSide( side );
        event.setOrderQty( qty );
        event.setPrice( px );
        event.setOrderId( orderId );
        
        book.apply( event );
    }

    private void applyDelEvent( FullL3OrderBook book, long orderId ) {
        BookDeleteOrderWrite event  = new BookDeleteOrderImpl();
        event.setOrderId( orderId );
        
        book.apply( event );
    }

    private void applyModEvent( FullL3OrderBook book, long orderId, Side side, int qty, double px ) {
        BookModifyOrderWrite event  = new BookModifyOrderImpl();
        event.setOrderQty( qty );
        event.setPrice( px );
        event.setOrderId( orderId );
        
        book.apply( event );
    }
}
