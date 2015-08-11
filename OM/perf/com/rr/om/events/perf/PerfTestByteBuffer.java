/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import java.nio.ByteBuffer;

import com.rr.core.lang.BaseTestCase;

/**
 * test the performance difference between using ReusableStrings in a NOS and ViewStrings
 *
 * @author Richard Rose
 */

// TODO check impact of replacing all the small (<=8 byte) with SmallString

public class PerfTestByteBuffer extends BaseTestCase {

    public void testMultTen() {
    
        int runs = 5;
        int iterations = 10000000;
        
        doRun( runs, iterations );
    }

    private void doRun( int runs, int iterations ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long heapTime   = heap( iterations, 400  );
            long directTime = direct( iterations, 400 );
            long byteArr    = byteArray( iterations, 400 );
            
            System.out.println( "Run " + idx + " heap=" + heapTime + ", direct=" + directTime + ", byteArr=" + byteArr );
        }
    }

    private long byteArray( int iterations, int size ) {
        byte[] buf = new byte[size];

        byte[] bytes = "11=CLORDID_1234".getBytes();
        int max = size - bytes.length; 
        
        long startTime = System.currentTimeMillis();
                
        int idx = 0;
        
        for( int i=0 ; i < iterations ; ++i ) {
            if ( idx >= max ) {
                idx = 0;
            }
            
            System.arraycopy( bytes, 0, buf, idx, bytes.length );
            
            idx += bytes.length;
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }

    private long heap( int iterations, int size ) {

        ByteBuffer buf = ByteBuffer.allocate( size );

        byte[] bytes = "11=CLORDID_1234".getBytes();
        int max = size - bytes.length; 
        
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
            if ( buf.position() >= max ) {
                buf.clear();
            }
            buf.put( bytes );
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }

    private long direct( int iterations, int size ) {

        ByteBuffer buf = ByteBuffer.allocateDirect( size );

        byte[] bytes = "11=CLORDID_1234".getBytes();
        int max = size - bytes.length; 
        
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
            if ( buf.position() >= max ) {
                buf.clear();
            }
            buf.put( bytes );
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }

}
