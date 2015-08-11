/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l3;

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableCategory;
import com.rr.core.lang.ReusableCategoryEnum;
import com.rr.core.lang.ReusableType;

/**
 * orderId not included as thats the key in the orderEntry map
 * 
 * price not included as thats in the BookLevelEntry
 *
 * @author Richard Rose
 */
public final class OrderBookEntry implements Reusable<OrderBookEntry> {

    // this type will not be used in a generic recycler
    private static final ReusableType _type = new ReusableType() {
        @Override public int              getId()               { return 0; }
        @Override public int              getSubId()            { return 0; }
        @Override public ReusableCategory getReusableCategory() { return ReusableCategoryEnum.MDS; }
    };

    private FullBookLevelEntry _bookEntry;
    private int            _qty;
    private OrderBookEntry _next;
    private boolean        _isBuySide;
    
    public OrderBookEntry() {
    }

    public FullBookLevelEntry getBookEntry() {
        return _bookEntry;
    }

    public int getQty() {
        return _qty;
    }

    public void setBookEntry( FullBookLevelEntry bookEntry ) {
        _bookEntry = bookEntry;
    }

    public void setQty( int qty ) {
        _qty = qty;
    }

    public double getPrice() {
        return _bookEntry.getPrice();
    }

    @Override
    public OrderBookEntry getNext() {
        return _next;
    }

    @Override
    public void setNext( OrderBookEntry nxt ) {
        _next = nxt;
    }

    @Override
    public ReusableType getReusableType() {
        return _type;
    }

    public void setBuySide( boolean isBuySide ) {
        _isBuySide = isBuySide;
    }

    @Override
    public void reset() {
        _bookEntry = null;
        _qty       = 0;
        _next      = null;
        _isBuySide = isBuySide();
    }

    public boolean isBuySide() {
        return _isBuySide;
    }
}
