/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

public final class BookEntryImpl implements BookLevelEntry {

    public int     _qty;
    public double  _price;
    public boolean _dirty = true;
    
    @Override
    public double getPrice() {
        return _price;
    }
    
    @Override
    public int getQty() {
        return _qty;
    }
    
    @Override
    public void set( final int qty, final double price ) {
        _qty   = qty;
        _price = price;
        _dirty = false;
    }

    @Override
    public void set( final int qty, final double price, final boolean isDirty ) {
        _qty   = qty;
        _price = price;
        _dirty = isDirty;
    }

    @Override
    public void set( final BookEntryImpl that ) {
        _qty   = that._qty;
        _price = that._price;
        _dirty = that._dirty;
    }
    
    public void clear() {
        _qty = 0;
        _price = 0.0;
        _dirty = true;
    }

    public void setQty( int qty ) {
        _qty = qty;
        _dirty = false;
    }

    public void setPrice( double price ) {
        _price = price;
        _dirty = false;
    }

    public void setDirty( boolean isDirty ) {
        _dirty = isDirty;
    }

    @Override
    public boolean isDirty() {
        return _dirty;
    }

    @Override
    public String toString() {
        return "BookEntryImpl [qty=" + _qty + ", price=" + _price + ", dirty=" + _dirty + "]";
    }

    @Override
    public boolean isValid() {
        return !_dirty && _price != 0.0;
    }
}
