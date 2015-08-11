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
import com.rr.core.lang.ZString;

// NON THREADSAFE NUMERIC COUNTER WITH STRING PREFIX

public class DailySimpleIDGenerator implements IDGenerator {

    private static final long    MS_IN_DAY = 24 * 60 * 60 * 1000;  // 86,400,000

    private final ReusableString _base = new ReusableString();
    private       long           _counter;

    public DailySimpleIDGenerator( ZString processInstanceSeed ) {
        _base.copy( processInstanceSeed );
        
        // int can hold max  2,147,483,647
        // this allows seeds of 1 to 20
        
        //worst case, if 1 ms before midnight there will be room for 13,600,000 unique ids before any chance of overlap 
        
        seed();
    }

    private void seed() {
        
        long now = System.currentTimeMillis();
        
        int msecsSinceStartOFDay = (int)(now % MS_IN_DAY);

        // so a restart of 1min would cater for (1 * 60 * 1000 * 100) = 1,500,000 orders 
        
        _counter = msecsSinceStartOFDay * 25;
    }


    @Override
    public void genID( ReusableString id ) {

        _counter++;

        id.copy( _base ).append( _counter );
    }
}
