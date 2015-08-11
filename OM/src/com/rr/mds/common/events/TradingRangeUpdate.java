/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common.events;

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.mds.common.MDSReusableType;
import com.rr.om.Strings;
import com.rr.om.model.instrument.TradingRange;

public final class TradingRangeUpdate extends BaseMDSEvent implements Reusable<TradingRangeUpdate> {

    private static final ViewString LOWER  = new ViewString( ", low=" );
    private static final ViewString UPPER  = new ViewString( ", high=" );
    private static final ViewString LOW_ID = new ViewString( ", lowId=" );
    private static final ViewString UP_ID  = new ViewString( ", highId=" );

    private TradingRangeUpdate    _next;
    
    private ReusableString          _ric     = new ReusableString( SizeConstants.DEFAULT_RIC_LENGTH );
    
    private double                  _lower   = 0;
    private double                  _upper   = Double.MAX_VALUE;
    
    private long                    _lowerId = 0l;                  // tick id
    private long                    _upperId = 0l;                  // tick id ... is actually a nano timestamp

    private int                     _lowerFlags;
    private int                     _upperFlags;
    
    @Override
    public final ReusableType getReusableType() {
        return MDSReusableType.TradingBandUpdate;
    }

    @Override
    public final TradingRangeUpdate getNext() {
        return _next;
    }

    @Override
    public final void setNext( final TradingRangeUpdate nxt ) {
        _next = nxt;
    }

    public ReusableString getRicForUpdate() {
        return _ric;
    }
    
    public ViewString getRIC() {
        return _ric;
    }
    
    public final void setBands( double lower, 
                                double upper, 
                                long lowerId, 
                                long upperId,
                                int lowerFlags,
                                int upperFlags ) {
        _lower   = lower;
        _upper   = upper;
        _lowerId = lowerId;
        _upperId = upperId;
        _lowerFlags = lowerFlags;
        _upperFlags = upperFlags;
    }
    
    public void setTradingRange( TradingRange range )  {
        if ( _upper > 0 ) range.setMaxBuy(  _upperId, _upper, _upperFlags );
        range.setMinSell( _lowerId, _lower, _lowerFlags );
    }
    
    @Override
    public final void reset() {
        _next        = null;
        _nextMessage = null;
        _lower       = 0;
        _upper       = 0;
        _lowerId     = 0l;
        _upperId     = 0l;
        _lowerFlags  = 0;
        _upperFlags  = 0;
        _ric.reset();
    }

    public ReusableString getRic() {
        return _ric;
    }

    public double getLower() {
        return _lower;
    }

    public double getUpper() {
        return _upper;
    }

    public long getLowerId() {
        return _lowerId;
    }

    public long getUpperId() {
        return _upperId;
    }

    public int getLowerFlags() {
        return _lowerFlags;
    }

    public int getUpperFlags() {
        return _upperFlags;
    }

    @Override
    public void dump( ReusableString out ) {
        
        out.append( Strings.RIC ).append( _ric );
        out.append( LOWER ).append( _lower );
        out.append( UPPER ).append( _upper );
        out.append( LOW_ID ).append( _lowerId );
        out.append( UP_ID ).append( _upperId );
    }
}
