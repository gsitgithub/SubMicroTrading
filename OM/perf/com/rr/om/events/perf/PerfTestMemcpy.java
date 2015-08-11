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

public class PerfTestMemcpy extends BaseTestCase {

    public void testStringNoOffset() {
    
        int runs = 5;
        int iterations = 10000000;

        doRun( runs, iterations, 20 );
        doRun( runs, iterations, 10 );
        doRun( runs, iterations, 5 );
        doRun( runs, iterations, 4 );
        doRun( runs, iterations, 3 );
        doRun( runs, iterations, 2 );
        doRun( runs, iterations, 1 );
    }

    private void doRun( int runs, int iterations, int len ) {
        byte[] src = "the quick brown fox jumped over the lazy dog".getBytes();
        byte[] dest = new byte[len+1];
        
        assertTrue( len <= src.length );
        
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long loop  = loop( iterations, len, src, dest );
            long memcp = memcp( iterations, len, src, dest );
            
            System.out.println( "Run " + idx + " loop=" + loop + ", memcp=" + memcp + ", len=" + len );
        }
    }

    private long loop( int iterations, int len, byte[] src, byte[] dest ) {
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {

            for ( int j=0 ; j < len; ++j ) {
                dest[j] = src[j];
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }

    private long memcp( int iterations, int len, byte[] src, byte[] dest ) {
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
            System.arraycopy( src, 0, dest, 0, len );
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }
    
    public void testStringOffset() {
        
        int runs = 5;
        int iterations = 10000000;

        doRunOffset( runs, iterations, 20, 2 );
        doRunOffset( runs, iterations, 10, 2 );
        doRunOffset( runs, iterations, 5, 2 );
        doRunOffset( runs, iterations, 4, 2 );

        doRunOffset( runs, iterations, 20, 400 );
        doRunOffset( runs, iterations, 10, 400 );
        doRunOffset( runs, iterations, 5, 400 );
        doRunOffset( runs, iterations, 4, 400 );
    }

    private void doRunOffset( int runs, int iterations, int len, int offset ) {
        byte[] src = "the quick brown fox jumped over the lazy dog".getBytes();
        byte[] dest = new byte[offset+len+1];
        
        assertTrue( len <= src.length );
        
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long loop  = loopOffset( iterations, len, src, dest, offset );
            long memcp = memcpOffset( iterations, len, src, dest, offset );
            
            System.out.println( "Run " + idx + " loop=" + loop + ", memcp=" + memcp + ", len=" + len + ", off=" + offset );
        }
    }

    private long loopOffset( int iterations, int len, byte[] src, byte[] dest, int offset ) {
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {

            int k = offset;
            for ( int j=0 ; j < len; ++j ) {
                dest[k++] = src[j];
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }

    private long memcpOffset( int iterations, int len, byte[] src, byte[] dest, int offset ) {
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
            System.arraycopy( src, 0, dest, offset, len );
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }
}
