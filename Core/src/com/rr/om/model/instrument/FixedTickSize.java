/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.instrument;

import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;

public class FixedTickSize implements TickType {

    private static final ViewString PRICE    = new ViewString( " price " );
    private static final ViewString NOT_MULT = new ViewString( " not a multiple of " );
    private static final ViewString FIXED    = new ViewString( "FIXED" );
    
    private final double         _fixedTick;
    private final ReusableString _id;
    
    public FixedTickSize( double fixedTick ) {
        _fixedTick = fixedTick;
        
        _id = new ReusableString( FIXED );
        _id.append( fixedTick );
    }
    
    @Override
    public boolean isValid( double price ) {
        // is the price is a multiple of fixed tick size
        return Math.abs( Math.IEEEremainder( price, _fixedTick ) ) <= Constants.TICK_WEIGHT;
    }

    @Override
    public void writeError( double price, ReusableString err ) {

        err.append( PRICE ).append( price ).append( NOT_MULT ).append( _fixedTick );
    }

    @Override
    public boolean canVerifyPrice() {
        return true;
    }

    @Override
    public ZString getId() {
        // TODO Auto-generated method stub
        return null;
    }
}
