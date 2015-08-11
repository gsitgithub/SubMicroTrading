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
import com.rr.core.utils.NumberFormatUtils;

/**
 * test the performance difference between using ReusableStrings in a NOS and ViewStrings
 *
 * @author Richard Rose
 */

// TODO check impact of replacing all the small (<=8 byte) with SmallString

public class PerfTestNums extends BaseTestCase {

    private static final int TEN = 10;
    private long _dontOptimise = 0;

    public void testLongToString() {
        
        int runs = 5;
        int iterations = 100000000;
        
        doLongToString( runs, iterations, 987654321L );
        doLongToString( runs, iterations, 10 );
        doLongToString( runs, iterations, 100 );
        doLongToString( runs, iterations, 1000 );
        doLongToString( runs, iterations, 5000 );
        doLongToString( runs, iterations, 1234567891234567L );
        doLongToString( runs, iterations, 5 );
    }

    public void testMultTen() {
    
        int runs = 5;
        int iterations = 100000000;
        
        doRunTen( runs, iterations, 10 );
        doRunTen( runs, iterations, 100 );
        doRunTen( runs, iterations, 1000 );
        doRunTen( runs, iterations, 5000 );
        doRunTen( runs, iterations, 1 );

        doRunTen( runs, iterations, 10 );
        doRunTen( runs, iterations, 100 );
        doRunTen( runs, iterations, 1000 );
        doRunTen( runs, iterations, 5000 );
        doRunTen( runs, iterations, 1 );
    }

    private void doLongToString( int runs, int iterations, long baseVal ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long time = longToString( iterations, baseVal );
            
            System.out.println( "Run " + idx + " val=" + baseVal + ", longToString old=" + time );
        }
    }

    private void doRunTen( int runs, int iterations, int baseVal ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long normalMult = mult10( iterations, baseVal, 10 );
            long shiftMult  = shift10( iterations, baseVal, 10 );
            long dblShift   = dblShift10( iterations, baseVal, 10 );
            
            System.out.println( "Run " + idx + " *10=" + normalMult + ", shiftAndAdd=" + shiftMult + 
                                ", dblShift=" + dblShift + ", base=" + baseVal );
        }
        assertTrue( _dontOptimise > 0 );
    }

    private long longToString( int iterations, long baseVal ) {
        long startTime = System.currentTimeMillis();
                
        final int len = NumberFormatUtils.getLongLen( baseVal );
        final byte[] buff = new byte[len+1];

        for( int i=0 ; i < iterations ; ++i ) {
            NumberFormatUtils.addPositiveLongFixedLength( buff, 0, baseVal, len );
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }

    private long dblShift10( int iterations, int baseVal, int ten ) {
        long tmpTot = 0;
        int tmp;
        
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
        
            tmp = (baseVal << 3) + (baseVal << 1);
            
            tmpTot += tmp;
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOptimise += tmpTot;
        
        return endTime - startTime;
    }

    private long shift10( int iterations, int baseVal, int ten ) {
        
        long tmpTot = 0;
        int tmp;
        
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
        
            tmp = (baseVal << 3) + baseVal + TEN;
            
            tmpTot += tmp;
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOptimise += tmpTot;
        
        return endTime - startTime;
    }

    private long mult10( int iterations, int baseVal, int ten ) {
        long tmpTot = 0;
        int tmp;
        
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
        
            tmp = baseVal * ten;
            
            tmpTot += tmp;
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOptimise += tmpTot;
        
        return endTime - startTime;
    }

    public void testIncrementTen() {
        
        int runs = 5;
        int iterations = 100000000;
        
        doRunInc( runs, iterations );
    }

    private void doRunInc( int runs, int iterations ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long pre  = pre( iterations );
            long post = post( iterations );
            
            System.out.println( "Run INC " + idx + " pre=" + pre + ", post=" + post );
        }
    }

    private long pre( int iterations ) {
        long tmpTot = 0;
        
        long startTime = System.currentTimeMillis();
             
        int i=0;
        
        while( i < iterations ) {

            tmpTot += ++i;
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOptimise += tmpTot;
        
        return endTime - startTime;
    }

    private long post( int iterations ) {
        
        long tmpTot = 0;
        
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
        
            tmpTot += i;
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOptimise += tmpTot;
        
        return endTime - startTime;
    }

}
