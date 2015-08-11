/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.threading;

import java.util.concurrent.atomic.AtomicInteger;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.utils.Percentiles;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;

public class PerfTestSyncVsSpinLock extends BaseTestCase {

    private static final int ITERATIONS = 1000000;
    
    static {
        // dont use the default thread config unless test requires the thread isolation of main SMT process
        ThreadUtils.init( ThreadUtils.DEFAULT_THREAD_CONFIG );
    }
    
    int[] stats = new int[ ITERATIONS ];

    private int _count;
                          
    public void testUncontestedLock() {
        ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.Processor );
        for( int i=0 ; i < 5 ; ++i ) {
            doNothing( ITERATIONS );
            doTestUncontestedSync( ITERATIONS );
            doTestUncontestedSpin( ITERATIONS );
        }
    }

    private void doNothing( int iterations ) {
        for( int i=0 ; i < iterations ; ++i ) {
            long start = Utils.nanoTime();
            
            long duration = Utils.nanoTime() - start;
            
            stats[i] = (int)duration;
        }
        
        Percentiles p = new Percentiles( stats );
        
        System.out.println( "[NOTHING]  NanoSecond stats " + " count=" + iterations + 
                            ", med=" + p.median() + 
                            ", ave=" + p.getAverage() + 
                            ", min=" + p.getMinimum() + 
                            ", max=" + p.getMaximum() + 
                            "\n                 " +
                            ", p99=" + p.calc( 99 ) + 
                            ", p95=" + p.calc( 95 ) + 
                            ", p90=" + p.calc( 90 ) + 
                            ", p80=" + p.calc( 80 ) + 
                            ", p70=" + p.calc( 70 ) + 
                            ", p50=" + p.calc( 50 ) + "\n" );
    }

    private void doTestUncontestedSync( int iterations ) {
        for( int i=0 ; i < iterations ; ++i ) {
            long start = Utils.nanoTime();
            
            syncMethod( i );
            
            long duration = Utils.nanoTime() - start;
            
            stats[i] = (int)duration;
        }
        
        Percentiles p = new Percentiles( stats );
        
        System.out.println( "[UNCONTESTED_SYNC]  NanoSecond stats " + " count=" + _count + 
                            ", med=" + p.median() + 
                            ", ave=" + p.getAverage() + 
                            ", min=" + p.getMinimum() + 
                            ", max=" + p.getMaximum() + 
                            "\n                 " +
                            ", p99=" + p.calc( 99 ) + 
                            ", p95=" + p.calc( 95 ) + 
                            ", p90=" + p.calc( 90 ) + 
                            ", p80=" + p.calc( 80 ) + 
                            ", p70=" + p.calc( 70 ) + 
                            ", p50=" + p.calc( 50 ) + "\n" );
    }

    private synchronized void syncMethod( int i ) {
        _count = i;
    }

    private void doTestUncontestedSpin( int iterations ) {
        AtomicInteger b = new AtomicInteger();
        
        for( int i=0 ; i < iterations ; ++i ) {
            long start = Utils.nanoTime();
            
            while( b.compareAndSet( 0, 1 ) == false ) {
                // spin to lock
            }
            
            unsyncMethod( i );

            b.set( 0 );
            
            long duration = Utils.nanoTime() - start;
            
            stats[i] = (int)duration;
        }
        
        Percentiles p = new Percentiles( stats );
        
        System.out.println( "[UNCONTEST_SPIN]  NanoSecond stats " + " count=" + _count + 
                            ", med=" + p.median() + 
                            ", ave=" + p.getAverage() + 
                            ", min=" + p.getMinimum() + 
                            ", max=" + p.getMaximum() + 
                            "\n                 " +
                            ", p99=" + p.calc( 99 ) + 
                            ", p95=" + p.calc( 95 ) + 
                            ", p90=" + p.calc( 90 ) + 
                            ", p80=" + p.calc( 80 ) + 
                            ", p70=" + p.calc( 70 ) + 
                            ", p50=" + p.calc( 50 ) + "\n" );
    }

    private void unsyncMethod( int i ) {
        _count = i;
    }
}
