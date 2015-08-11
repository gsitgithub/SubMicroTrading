/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.dummy.warmup;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.om.model.instrument.TradingRange;

/**
 * note there is NO sync here
 * 
 * worst case is on next mem barrier the cached values will be updated
 * dont worry about not atomically updating the upper and  lower values as only 1 of them is used in each path of the valid method
 *
 */

public class TradingRangeImpl implements TradingRange {

    private static final Logger     _log = LoggerFactory.create( TradingRangeImpl.class );
    
    private static final ViewString INVALID_BUY_PRICE  = new ViewString( "Invalid BUY price of " );
    private static final ViewString INVALID_SELL_PRICE = new ViewString( "Invalid SELL price of " );
    private static final ViewString LOW                = new ViewString( ", minSell=" );
    private static final ViewString HIGH               = new ViewString( ", maxBuy=" );
    private static final ViewString TICK_ID            = new ViewString( ", tickID=" );
    
    private double _lower       = 0;
    private double _upper      = Double.MAX_VALUE;
    private long   _lowerId    = 0l;
    private long   _upperId    = 0l;
    private int    _lowerFlags = 0;
    private int    _upperFlags = 0;

    @Override
    public boolean valid( double price, boolean isBuySide, ReusableString err ) {
        boolean valid;
        
        if ( isBuySide ) {
            final double upper = _upper; // ensure the price logged is the price used in validation
            
            valid = ( price >= 0.0 && price <= upper ); 

            if ( !valid ) {
                err.append( INVALID_BUY_PRICE ).append( price ).append( HIGH ).append( upper ).append( TICK_ID ).append( _upperId );
                
                TradingRangeFlags.write( err, _upperFlags );
                
                _log.info( "Invalid buy price " );
            }
            
        } else {
            final double lower = _lower;
            
            valid = price >= lower;
            
            if ( !valid ) {
                err.append( INVALID_SELL_PRICE ).append( price ).append( LOW ).append( lower ).append( TICK_ID ).append( _lowerId );

                TradingRangeFlags.write( err, _lowerFlags );
            }
        }
        
        return valid;
    }

    /**
     * set the id second
     * worst case upperId value is cached by the proc, both updated here and then proc reads uncached upper from mem
     * and picks up the upper ... ie the gives us the previous upperId ... easy to check by looking at the tick logs  
     */
    @Override
    public void setMaxBuy( long tickId, double upper, int flags ) {
        _upperId    = tickId;
        _upper      = upper;
        _upperFlags = flags;
    }

    @Override
    public void setMinSell( long tickId, double lower, int flags ) {
        _lowerId    = tickId;
        _lower      = lower;
        _lowerFlags = flags;
    }

    public double getLower()         { return _lower; }
    public double getUpper()         { return _upper; }
    public long   getLowerId()       { return _lowerId; }
    public long   getUpperId()       { return _upperId; }
    public int    getLowerFlags()    { return _lowerFlags; }
    public int    getUpperFlags()    { return _upperFlags; }
}
