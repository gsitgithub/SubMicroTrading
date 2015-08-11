/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.utils.Utils;

public class PerfTestTime extends BaseTestCase {

    public void testNanoMillis() {
    
        int runs = 5;
        int iterations = 1000000000;

        doRun( runs, iterations );
    }

    private void doRun( int runs, int iterations ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long nano  = nano( iterations );
            long milli = milli( iterations );
            
            System.out.println( "Run " + idx + " nanoTime=" + nano + " (avg nanoseconds), milli=" + milli + " (avg nanoseconds)");
        }
    }

    private long nano( int iterations ) {
        long startTime = Utils.nanoTime();
                
        for( int i=0 ; i < iterations ; ++i ) {

            Utils.nanoTime();
        }
        
        long endTime = Utils.nanoTime();
        
        return (endTime - startTime) / iterations;
    }

    private long milli( int iterations ) {
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
            System.currentTimeMillis();
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }
}
