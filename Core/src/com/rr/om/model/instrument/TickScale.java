/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.instrument;

import java.util.ArrayList;
import java.util.List;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;

public class TickScale implements TickType {

    private static final ZString NOT_IN_BAND = new ViewString( " Price doesnt fall within any tick band, price=" );
    
    private final ZString        _scaleName;
    private       List<TickBand> _bands      = new ArrayList<TickBand>();

    public TickScale( ZString scaleName, TickBand[] bands ) {
        this( scaleName );
        
        for( TickBand band : bands ) {
            _bands.add( band );
        }
    }
    
    public TickScale( ZString scaleName ) {
        _scaleName = scaleName;
    }

    public void replaceAll( TickBand[] bands ) {
        _bands.clear();
        
        for( TickBand band : bands ) {
            _bands.add( band );
        }
    }
    
    public void addBand( TickBand band ) {
        final double lower    = band.getLower();
        final double upper    = band.getUpper();
        final double tickSize = band.getTickSize();
        
        if ( lower < 0 || lower > upper || tickSize <= 0 ) {
            throw new RuntimeException( "Invalid band low must be >=0, > high, tick > 0, low=" + lower + ", high=" + upper + ", tick=" + tickSize );
        }
        
        final int curNumBands = _bands.size();
        for( int  i=0 ; i < curNumBands ; ++i ) {
            final TickBand cur = _bands.get( i );
            
            if ( lower < cur.getLower() ) {
                if ( upper > cur.getLower() ) {
                    throw new RuntimeException( "Invalid band overlaps with band idx=" + i + 
                                                ", lower=" + cur.getLower() + ", upper=" + cur.getUpper() );
                }
                
                _bands.add( i, band );
                
                return;
            }
        }
        
        if ( lower <= 0 && curNumBands > 0 ) {
            throw new RuntimeException( "Invalid band cant be inserted at top of list, lower=" + lower + ", upper=" + upper );
        }

        _bands.add( band );
    }
    
    @Override
    public boolean isValid( double price ) {
        
        final int max = _bands.size();

        for( int i=0 ; i < max ; i++ ) {
            final TickBand band = _bands.get( i ); 
            
            if ( band.inBand( price ) ) {
                return band.isValid( price );
            }
        }
        
        return false;
    }

    @Override
    public void writeError( double price, ReusableString err ) {

        final int max = _bands.size();

        for( int i=0 ; i < max ; i++ ) {
            final TickBand band = _bands.get( i ); 
            
            if ( band.inBand( price ) ) {
                band.writeError( price, err );
                
                return;
            }
        }
        
        err.copy( NOT_IN_BAND );
        err.append( price );
    }

    @Override
    public boolean canVerifyPrice() {
        return true;
    }

    @Override
    public ZString getId() {
        return _scaleName;
    }
}
