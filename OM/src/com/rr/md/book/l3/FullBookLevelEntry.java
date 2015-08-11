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
import com.rr.core.model.book.BookEntryImpl;
import com.rr.core.model.book.BookLevelEntry;


/**
 * Order book entry for L3 order books
 *
 * @author Richard Rose
 */
public final class FullBookLevelEntry implements Reusable<FullBookLevelEntry>, BookLevelEntry {
    
    // this type will not be used in a generic recycler
    private static final ReusableType _type = new ReusableType() {
        @Override public int              getId()               { return 0; }
        @Override public int              getSubId()            { return 0; }
        @Override public ReusableCategory getReusableCategory() { return ReusableCategoryEnum.MDS; }
    };

    private       double          _price;
    private       int             _totalQty = 0;
    private       FullBookLevelEntry  _next     = null;
    private       FullBookLevelEntry  _prev     = null;

    public FullBookLevelEntry() {
    }
    
    public FullBookLevelEntry( double price ) {
        _price = price;
    }

    /**
     * this setter should only be used to override the accumulated total field ... eg snapshot processing
     */
    @Override
    public void set( int qty, double price ) {
        _price = price;
        _totalQty = qty;
    }

    void removeQty( int qty ) { 
        _totalQty -= qty;
        if ( _totalQty < 0 ) {
            _totalQty = 0;
        }
    }
    
    void addQty( int qty )        { _totalQty += qty; }
    void setQty( int qty )        { _totalQty = qty; }
    
    @Override
    public double getPrice()               { return _price; }

    public void setPrice( double price ) {
        _price = price;
    }

    @Override
    public int    getQty()                 { return _totalQty; }
    
    @Override
    public FullBookLevelEntry getNext()                 { return _next; }
    FullBookLevelEntry getPrev()                 { return _prev; }

    @Override
    public void setNext( FullBookLevelEntry next )      { _next = next; }
    void setPrev( FullBookLevelEntry prev )      { _prev = prev; }

    @Override
    public ReusableType getReusableType() {
        return _type;
    }

    @Override
    public void reset() {
        _price    = 0.0;
        _totalQty = 0;
        _next     = null;
        _prev     = null;
    }

    @Override
    public void set( int qty, double price, boolean isDirty ) {
        _price = price;
        _totalQty = qty;
    }

    @Override
    public void set( BookEntryImpl that ) {
        _price = that._price;
        _totalQty = that._qty;
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isValid() {
        return _price != 0.0 && _totalQty > 0;
    }
}
