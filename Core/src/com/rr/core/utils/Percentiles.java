/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import java.util.Arrays;

import com.rr.core.lang.ReusableString;

public class Percentiles {

    private final int[] _sortedValues;
    private final int   _average;
    private final int   _median;

    public Percentiles( int[] values, int size ) {
        _sortedValues = new int[size];
        
        if ( values.length == 0 ) {
            _average = 0;
            _median = 0;
            return;
        }
        
        System.arraycopy( values, 0, _sortedValues, 0, size );
        Arrays.sort( _sortedValues );
        
        long total = 0;

        for ( int i = 0 ; i < size ; i++ ) {
            total += _sortedValues[i];
        }

        _average = (int) (total / size);
        
        int idx = _sortedValues.length / 2;

        _median = _sortedValues[ idx ];
    }
    
    public Percentiles( int[] values ) {
        this( values, values.length );
    }

    public int median() {
        return _median;
    }

    public int calc( final double percentile ) {

        if ( (percentile <= 0) || (percentile > 100) ) {
            throw new IllegalArgumentException( "Invalid percentile " + percentile );
        }
        
        int numEntries = _sortedValues.length;
        
        if ( numEntries == 0 ) return 0; 
        if ( numEntries == 1 ) return _sortedValues[0]; 

        double percentilePosition = percentile * (numEntries + 1) / 100;
        int    intPos             = (int) Math.floor( percentilePosition );

        if ( intPos >= numEntries ) return _sortedValues[_sortedValues.length - 1];
        
        double fractionFactor     = percentilePosition - intPos;
        
        int lower = _sortedValues[intPos - 1];
        int upper = _sortedValues[intPos];
        
        return (int) (lower + fractionFactor * (upper - lower));
    }

    public int getAverage() {
        return _average;
    }

    public int getMinimum() {
        if ( _sortedValues.length == 0 ) return 0; 
        return _sortedValues[0];
    }

    public int getMaximum() {
        if ( _sortedValues.length == 0 ) return 0; 
        return _sortedValues[_sortedValues.length - 1];
    }

    public int getEntry( int idx ) {
        if ( _sortedValues.length == 0 ) return 0; 
        return _sortedValues[ idx ];
    }
    
    public void logStats( ReusableString out ) {
        out.append( "logStats() entries=" + _sortedValues.length ).append( "\n" );
        
        out.append( ", med=" + median()        + ", ave=" + getAverage() + 
                    ", min=" + getMinimum()    + ", max=" + getMaximum() + 
                    "\n"                       +
                    ", p99.9=" + calc( 99.9 )  + 
                    ", p99="   + calc( 99 )    + 
                    ", p95="   + calc( 95 )    + 
                    ", p90="   + calc( 90 )    + 
                    ", p80="   + calc( 80 )    + 
                    ", p70="   + calc( 70 )    + 
                    ", p50="   + calc( 50 )   + "\n" );
    }
}
