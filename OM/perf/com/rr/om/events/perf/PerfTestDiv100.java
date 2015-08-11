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

/**
 * test the performance difference between using ReusableStrings in a NOS and ViewStrings
 *
 * @author Richard Rose
 */

// TODO check impact of replacing all the small (<=8 byte) with SmallString

public class PerfTestDiv100 extends BaseTestCase {

    private long _dontOptimise = 0;

    public void testDiv100() {
    
        int runs = 5;
        int iterations = 10000000;
        
        doRunTen( runs, iterations, 1 );
        doRunTen( runs, iterations, 100 );
        doRunTen( runs, iterations, 1000 );
        doRunTen( runs, iterations, 500000 );
        doRunTen( runs, iterations, 1 );
    }

    private void doRunTen( int runs, int iterations, int baseVal ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long div100 = div100( iterations, baseVal, 10 );
            long recip  = reciprical100( iterations, baseVal, 10 );
            
            System.out.println( "Run " + idx + " /100=" + div100 + ", reciprical=" + recip + ", base=" + baseVal );
        }
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long div1000 = div1000( iterations, baseVal, 10 );
            long recip   = reciprical1000( iterations, baseVal, 10 );
            
            System.out.println( "Run " + idx + " /1000=" + div1000 + ", reciprical=" + recip + ", base=" + baseVal + ", dontOptimise=" + _dontOptimise);
        }
    }

    private long reciprical100( int iterations, int baseVal, int ten ) {
        long tmpTot = 0;
        long tmp;
        
        /**
            MagicNumber = 2748779069
            mov eax,X
            mov edx, MagicNumber
            inc eax
            mul edx
            SHR edx, 6
         */

        long startTime = System.currentTimeMillis();
                
        for( long i=0 ; i < iterations ; ++i ) {
            tmp = (int) (((baseVal+1) * 0xA3D70A3Dl) >>> 38);
            tmpTot += tmp;
        }
        
        long endTime = System.currentTimeMillis();
        _dontOptimise += tmpTot;
        return endTime - startTime;
    }

    private long reciprical1000( int iterations, int baseVal, int ten ) {
        long tmpTot = 0;
        long tmp;
        
        long startTime = System.currentTimeMillis();
                
        for( long i=0 ; i < iterations ; ++i ) {
            tmp = (int)(((baseVal * 0x10624DD3l)) >>> 38);
            tmpTot += tmp;
        }
        
        long endTime = System.currentTimeMillis();
        _dontOptimise += tmpTot;
        return endTime - startTime;
    }

    private long div100( int iterations, int baseVal, int ten ) {
        long tmpTot = 0;
        int tmp;
        
        long startTime = System.currentTimeMillis();
        long base = baseVal;
                
        for( long i=0 ; i < iterations ; ++i ) {
            tmp = (int)(base / 100);
            tmpTot += tmp;
        }
        
        long endTime = System.currentTimeMillis();
        _dontOptimise += tmpTot;
        return endTime - startTime;
    }
    
    private long div1000( int iterations, int baseVal, int ten ) {
        long tmpTot = 0;
        int tmp;
        
        long startTime = System.currentTimeMillis();
        long base = baseVal;
                
        for( long i=0 ; i < iterations ; ++i ) {
            tmp = (int)(base / 1000);
            tmpTot += tmp;
        }
        
        long endTime = System.currentTimeMillis();
        _dontOptimise += tmpTot;
        return endTime - startTime;
    }
    
    public void XtestAllDiv1000() {
        
        for ( int i=0 ; i < Integer.MAX_VALUE ; ++i )  {
            int tmp = (int)(((i * 0x10624DD3l)) >>> 38);      // shift = calculated shift + 32 for upper word
            int div = i / 1000;
            
            if ( div != tmp )
                System.out.println( "i=" + i + ", tmp=" + tmp + ", div=" + div );
            
            assertEquals( div, tmp );
        }
    }
    
    public void XtestAllDiv100() {
        
        // for div10  :  0xCCCCCCCD >>>35
        // for div100 :  0xA3D70A3D >>>38
        // for div1000 : 0x10624DD3 >>>38
        
        for ( int i=1000 ; i < Integer.MAX_VALUE ; ++i )  {
            int tmp = (int) (((((long)i+1) * 0xA3D70A3Dl)) >>> 38); // shift = calculated shift + 32 for upper word
            int div = i / 100;
            
            if ( div != tmp )
                System.out.println( "i=" + i + ", tmp=" + tmp + ", div=" + div );
            
            assertEquals( div, tmp );
        }
    }    
}
