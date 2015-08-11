/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

import com.rr.core.lang.ReusableString;

public final class DoubleSidedBookEntryImpl implements DoubleSidedBookEntry {

    private static final double ZERO = 0.0;
    
    public int    _buyQty;
    public double _buyPrice = ZERO;
    public int    _sellQty;
    public double _sellPrice = ZERO;

    private boolean _buyIsDirty = true;
    private boolean _sellIsDirty = true;
    
    @Override
    public void dump( final ReusableString dest ) {
        dest.append( _buyIsDirty ? "d " : "  " );
        dest.append( _buyQty ).append( " x " ).append( _buyPrice ).append( "  :  " );
        dest.append( _sellPrice ).append( " x " ).append( _sellQty );
        dest.append( _sellIsDirty ? " d" : "  " );
    }
    
    @Override
    public String toString() {
        ReusableString buf = new ReusableString();
        dump( buf );
        return buf.toString();
    }

    @Override
    public int getBidQty() {
        return _buyQty;
    }
    
    @Override
    public double getBidPx() {
        return _buyPrice;
    }
    
    @Override
    public int getAskQty() {
        return _sellQty;
    }
    
    @Override
    public double getAskPx() {
        return _sellPrice;
    }
    
    public void setBuyQty( int buyQty ) {
        _buyQty = buyQty;
    }
    
    public void setBuyPrice( double buyPrice ) {
        _buyPrice = buyPrice;
    }
    
    public void setSellQty( int sellQty ) {
        _sellQty = sellQty;
    }
    
    public void setSellPrice( double sellPrice ) {
        _sellPrice = sellPrice;
    }

    @Override
    public final boolean isBuyIsDirty() {
        return _buyIsDirty;
    }

    @Override
    public final boolean isSellIsDirty() {
        return _sellIsDirty;
    }

    @Override
    public void set( int buyQty, double buyPrice, boolean buyIsDirty, int sellQty, double sellPrice, boolean sellIsDirty ) {
        _buyQty     = buyQty;
        _buyPrice   = buyPrice;
        _buyIsDirty = buyIsDirty;
        
        _sellQty     = sellQty;
        _sellPrice   = sellPrice;
        _sellIsDirty = sellIsDirty;
    }
    
    @Override
    public void set( BookLevelEntry bid, BookLevelEntry ask ) {
        _buyQty     = bid.getQty();
        _buyPrice   = bid.getPrice();
        _buyIsDirty = bid.isDirty();
        
        _sellQty     = ask.getQty();
        _sellPrice   = ask.getPrice();
        _sellIsDirty = ask.isDirty();
    }
    
    @Override
    public void reset() {
        _buyQty = 0;
        _buyPrice = ZERO;
        _sellQty = 0;
        _sellPrice = ZERO;
        _sellIsDirty = true;
        _buyIsDirty = true;
    }
    
    @Override
    public boolean isValid() {
        return( (_buyPrice != ZERO) && (_sellPrice != ZERO) && (_sellIsDirty==false) && (_buyIsDirty == false) ); 
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (_buyIsDirty ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits( _buyPrice );
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + _buyQty;
        result = prime * result + (_sellIsDirty ? 1231 : 1237);
        temp = Double.doubleToLongBits( _sellPrice );
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + _sellQty;
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        DoubleSidedBookEntryImpl other = (DoubleSidedBookEntryImpl) obj;
        if ( _buyIsDirty != other._buyIsDirty )
            return false;
        if ( Double.doubleToLongBits( _buyPrice ) != Double.doubleToLongBits( other._buyPrice ) )
            return false;
        if ( _buyQty != other._buyQty )
            return false;
        if ( _sellIsDirty != other._sellIsDirty )
            return false;
        if ( Double.doubleToLongBits( _sellPrice ) != Double.doubleToLongBits( other._sellPrice ) )
            return false;
        if ( _sellQty != other._sellQty )
            return false;
        return true;
    }
}
