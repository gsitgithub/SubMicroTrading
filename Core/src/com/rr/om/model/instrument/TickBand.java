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

public class TickBand {

    private static final ViewString PRICE    = new ViewString( " price " );
    private static final ViewString NOT_MULT = new ViewString( " not a multiple of " );

    private double _lower;
    private double _upper;
    private double _tickSize;

    public TickBand( double lower, double upper, double tickSize ) {
        super();
        _lower = lower;
        _upper = (upper==0.0) ? Double.MAX_VALUE : upper;
        _tickSize = tickSize;
    }

    public final double getLower() {
        return _lower;
    }

    public final double getUpper() {
        return _upper;
    }

    public final double getTickSize() {
        return _tickSize;
    }

    public final boolean inBand( double price ) {
        return price >= _lower && price <= _upper;
    }

    public final boolean isValid( double price ) {
        return Math.abs( Math.IEEEremainder( price, _tickSize ) ) <= Constants.TICK_WEIGHT;
    }

    public final void writeError( double price, ReusableString err ) {
        err.append( PRICE ).append( price ).append( NOT_MULT ).append( _tickSize );
    }
}
