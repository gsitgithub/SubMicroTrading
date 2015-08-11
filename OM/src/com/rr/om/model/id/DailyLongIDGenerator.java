/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.id;

import com.rr.core.lang.ReusableString;
import com.rr.core.utils.NumberFormatUtils;

// NON THREADSAFE NUMERIC COUNTER

public class DailyLongIDGenerator implements IDGenerator {

    private static final long MS_IN_DAY = 24 * 60 * 60 * 1000;  // 86400000

    // max is 9,223,372,036,854,775,807

    private static final int MAX_SEED = 90;
    private static final int MIN_SIZE = 10;
    
    private final long _instanceSeed;
    private final int _totalFieldLen;

    private long _counter;

    public DailyLongIDGenerator( int processInstanceSeed, int totalFieldLen ) {
        _instanceSeed  = processInstanceSeed;
        _totalFieldLen = totalFieldLen;
        
        if ( _instanceSeed <= 0 || _instanceSeed > MAX_SEED ) {
            throw new RuntimeException( "DailyNumericIDGenerator seed=" + _instanceSeed + ", is invalid, max=" + MAX_SEED );
        }

        if ( _totalFieldLen < MIN_SIZE || _totalFieldLen > 19 ) {
            throw new RuntimeException( "DailyNumericIDGenerator fieldLen=" + _totalFieldLen + ", is too small, min=" + MIN_SIZE );
        }

        int tmpInstVar = processInstanceSeed;
        while( tmpInstVar > 100 ) {
            tmpInstVar = tmpInstVar / 10;
        }
        if ( _totalFieldLen == 19 && tmpInstVar > 90 ) {
            throw new RuntimeException( "DailyNumericIDGenerator instanceVar too high for fieldLen=" + _totalFieldLen + ", instVar=" + processInstanceSeed );
        }

        //worst case, if 1 ms before midnight there will be room for 13,600,000 unique ids before any chance of overlap 
        
        seed();
    }

    private void seed() {
        
        long now = System.currentTimeMillis();
        
        int msecsSinceStartOFDay = (int)(now % MS_IN_DAY);
        
        // factor the instanceId to the left of the min width
        int shift = _totalFieldLen - NumberFormatUtils.getPosLongLen( _instanceSeed );
        long factor = 1;
        
        while( shift-- > 0 ) {
            factor *= 10;
        }

        _counter = _instanceSeed * factor + msecsSinceStartOFDay;
        
        if ( _counter < factor ) {
            throw new RuntimeException( "DailyLongIDGenerator bad ID params causing overflow, seed=" + _instanceSeed + ", len=" + _totalFieldLen );
        }
    }


    @Override
    public void genID( ReusableString id ) {

        _counter++;

        id.reset();
        id.append( _counter, _totalFieldLen );
    }
}
