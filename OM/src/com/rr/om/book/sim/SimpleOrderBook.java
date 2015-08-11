/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.book.sim;

import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.ExchangeBook;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.events.factory.RecoveryTradeNewFactory;
import com.rr.model.generated.internal.events.impl.RecoveryTradeNewImpl;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.internal.type.ExecType;

// @NOTE only generates one sided fill
// @TODO implement proper book and generate fills for both sides as appropriate

public class SimpleOrderBook implements ExchangeBook {

    private static final ZString FILL = new ViewString( "TRADE" );

    private static class Level {
        private final double _price;
        private       int    _buyQty  = 0;
        private       int    _sellQty = 0;
        private       Level  _next    = null;
        private       Level  _prev    = null;

        Level( double price ) { _price = price; }
        
        void addSellQty( int sellQty )  { _sellQty += sellQty; }
        void addBuyQty( int buyQty )    { _buyQty += buyQty; }
        double getPrice()               { return _price; }
        int getBuyQty()                 { return _buyQty; }
        int getSellQty()                { return _sellQty; }
        Level getNext()                 { return _next; }
        Level getPrev()                 { return _prev; }
        void setBuyQty( int buyQty )    { _buyQty = buyQty; }
        void setSellQty( int sellQty )  { _sellQty = sellQty; }
        void setNext( Level next )      { _next = next; }
        void setPrev( Level prev )      { _prev = prev; }
    }
    
    private static RecoveryTradeNewFactory  _tradeNewFactory;

    private static int   _nextId = 10000;

    private final Level  _low;
    private final Level  _high; 
    private       int    _lastTrade;
    private       double _lastTradePrice;
    
    public static synchronized void init() {
        
        if ( _tradeNewFactory == null ) {
            SuperpoolManager sp = SuperpoolManager.instance();
            
            _tradeNewFactory   = sp.getFactory( RecoveryTradeNewFactory.class, RecoveryTradeNewImpl.class );
        }
    }

    private static synchronized int nextFillId() {
        return ++_nextId;
    }
    
    public SimpleOrderBook() {
        
        init();
        
        _low  = new Level(0);
        _high = new Level( Double.MAX_VALUE );
        
        _low.setNext( _high );
        _high.setPrev( _low );
    }
    
    public synchronized TradeNew add( final ZString mktOrdId, final int orderQty, final double price, final OrdType ordType, final Side side ) {
        
        if ( price <= 0 || orderQty <= 0 ) return null;
        
        int trade = 0;
        int needed = orderQty;
        double tradePriceSum = 0;
        
        if ( side.getIsBuySide() ) {
        
            Level curL = _low;
            
            while( curL != null && needed > 0 && (price - curL.getPrice()) > -0.0000005 ) {
                int curAvail = curL.getSellQty();
                if ( curAvail > 0 ) {
                    int diff = curAvail - needed;
                    if ( diff < 0 ) {
                        curL.setSellQty( 0 );
                        tradePriceSum += (curAvail * curL.getPrice());
                        needed -= curAvail;
                    } else { // full fill
                        curL.setSellQty( diff );
                        tradePriceSum += (needed * curL.getPrice());
                        needed = 0;
                    }
                }
                curL = curL.getNext();
            }
                
            if ( needed > 0 ) {
                Level l = getLevel( price );
                l.addBuyQty( needed );
            }

        } else  {
            Level curL = _high;
            
            while( curL != null && needed > 0 && (curL.getPrice()-price) > -0.0000005 ) {
                int curAvail = curL.getBuyQty();
                if ( curAvail > 0 ) {
                    int diff = curAvail - needed;
                    if ( diff < 0 ) {
                        curL.setBuyQty( 0 );
                        tradePriceSum += (curAvail * curL.getPrice());
                        needed -= curAvail;
                    } else { // full fill
                        curL.setBuyQty( diff );
                        tradePriceSum += (needed * curL.getPrice());
                        needed = 0;
                    }
                }
                curL = curL.getPrev();
            }
                
            if ( needed > 0 ) {
                Level l = getLevel( price );
                l.addSellQty( needed );
            }
        }
        
        trade = orderQty - needed;
        
        if ( trade > 0 ) {
            _lastTrade      = trade;
            _lastTradePrice = price; 
            return ( makeTrade( orderQty, _lastTrade, _lastTradePrice, tradePriceSum / trade, mktOrdId, side ) );
        }
        
        return null;
    }

    private TradeNew makeTrade( int orderQty, int lastTrade, double price, double avgPrice, ZString mktOrdId, Side side ) {
        RecoveryTradeNewImpl trade = _tradeNewFactory.get();
        trade.setAvgPx( avgPrice );
        trade.setPrice( price );
        trade.setOrderQty( orderQty );
        trade.setLastQty( lastTrade );
        trade.setLastPx( avgPrice );
        trade.setSide( side );
        trade.getOrderIdForUpdate().copy( mktOrdId );
        
        trade.setOrdStatus( (lastTrade == orderQty) ? OrdStatus.Filled : OrdStatus.PartiallyFilled );
        trade.setExecType( ExecType.Trade );
        
        ReusableString execId = trade.getExecIdForUpdate();
        execId.copy( FILL ).append( nextFillId() );
        
        return trade;
    }

    private Level getLevel( double price ) {
        Level l = _low;
        
        while( price > l.getPrice() ) {
            l = l.getNext();
        }
        
        // @NOTE should really ensure its in a tick bucket
        if ( Math.abs(price - l.getPrice()) > Constants.WEIGHT ) {

            Level newL = new Level( price );
            newL.setPrev( l.getPrev() );
            newL.setNext( l );
            l.getPrev().setNext( newL );
            l.setPrev( newL );
            
            l = newL;
        }
        
        return l;
    }

    public synchronized TradeNew amend( ZString marketOrderId, int newQty, int origQty, int fillQty, double newPrice, double origPrice, OrdType ordType, Side side ) {
        remove( marketOrderId, origQty-fillQty, origPrice, ordType, side );
        return add( marketOrderId, newQty-fillQty, newPrice, ordType, side );
    }

    public synchronized void remove( ZString marketOrderId, int openQty, double price, OrdType ordType, Side side ) {

        if ( price <= 0 || openQty <= 0 ) return;
        
        Level l = getLevel( price );

        if ( side.getIsBuySide() ) {
            int qty = l.getBuyQty();
            qty -= openQty;
            l.setBuyQty( (qty>=0) ? qty : 0 );
        } else {
            int qty = l.getSellQty();
            qty -= openQty;
            l.setSellQty( (qty>=0) ? qty : 0 );
        }
    }
}
