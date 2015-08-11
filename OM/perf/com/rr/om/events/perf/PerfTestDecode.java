/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import com.rr.core.codec.TstFixDecoder;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.recycle.ClientNewOrderSingleRecycler;
import com.rr.om.warmup.FixTestUtils;

/**
 * test the performance difference between using ReusableStrings in a NOS and ViewStrings
 *
 * @author Richard Rose
 */

public class PerfTestDecode extends BaseTestCase {

    private Logger             _log = LoggerFactory.console( PerfTestDecode.class );
    
    private TstFixDecoder     _testDecoder;
    private String             _dateStr = "20100510";
    private TimeZoneCalculator _calc;
    
    /*
    public void testPowerOfTwoModOpt() {

        //        int i = h & _table.length - 1;

        for( int tblLen=16; tblLen < 1024; tblLen *= 2 ){
            for ( int hashVal=0 ; hashVal < 1000000 ;hashVal++ ) {
                int a = hashVal % tblLen;
                int b = hashVal & (tblLen-1);
                
                if ( a != b ){
                    System.out.println( "hash=" + hashVal + ", tblLen=" + tblLen + ", byMod=" + a + ", anded=" + b );
                }
            }
        }
        
        System.out.println( "DONE" );
    }
    */
    
    public void testStringPerf() {
    
        int runs = 4;
        int iterations = 100000;
        
//        int[] poolSizes       = { 100, 100000, 5000, 100 };
        int[] poolSizes       = { 100, 100, 100, 100 };
        int[] extraAllocSizes = {  10,     10,   10,  10 };
        
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            int chainSize  = poolSizes[idx];
            int chains = iterations / chainSize + 1;
            int extraSize = extraAllocSizes[idx];
            
            long duration = perfTestReusableDecode( iterations, chains, chainSize, extraSize, false );
            
            _log.info( "Run " + idx + ", NO recycling, poolSize=" + chainSize + ", extra=" + extraSize + 
                       ", duration=" + duration + ", aveNano=" + (duration / iterations) );

//            duration = perfTestReusableDecode( iterations, chains, chainSize, extraSize, true );
//            
//            _log.info( "Run " + idx + ", RECYCLING ON, poolSize=" + chainSize + ", extra=" + extraSize + 
//                       ", duration=" + duration + ", aveNano=" + (duration / iterations) );
        }
    }
    
    public long perfTestReusableDecode( int iterations, int chains, int chainSize, int extraAlloc, boolean recycle ) {
        
        String nos = "8=FIX.4.4; 9=152; 35=D; 34=12243; 49=PROPA; 52="+ _dateStr + "-12:01:01.100; " +
                            "56=ME; 59=0; 22=5; 48=BT.L; 40=2; 54=1; 55=BT.L; 11=XX2100; 21=1; " +
                            "60=" + _dateStr  + "-12:01:01.000; 38=50; 44=10.23; 10=345; ";

        nos = FixTestUtils.toFixDelim( nos );
        // Map<String,String> vals = FixTestUtils.msgToMap( nos );
        
        byte[] fixMsg = nos.getBytes();
        
        _testDecoder = new TstFixDecoder( (byte)'4', (byte)'4' );
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _testDecoder.setTimeZoneCalculator( _calc );
        
        _testDecoder.presize( chains, chainSize, extraAlloc );
        
        System.gc();
        
        long duration;

        if ( recycle ) {
            duration = doDecodeWithRecycle( iterations, fixMsg );
        } else {
            duration = doDecode( iterations, fixMsg );
        }
        
        _testDecoder.logStats();

        System.gc();

        return duration;
    }

    private long doDecodeWithRecycle( int iterations, byte[] fixMsg ) {
        final SuperPool<ClientNewOrderSingleImpl>      sp       = SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class );
        final ClientNewOrderSingleRecycler recycler = new ClientNewOrderSingleRecycler( sp.getChainSize(), sp );

        long errors=0;
        long startTime = Utils.nanoTime();
        
        for ( int i=0 ; i < iterations ; i++ ) {
            try {
                Message msg = _testDecoder.decode( fixMsg, 0, fixMsg.length );
                recycler.recycle( (ClientNewOrderSingleImpl) msg );
            } catch( Exception e )  {
                ++errors;
            }
        }
        
        long endTime = Utils.nanoTime();
        long duration = endTime - startTime;

        assertEquals( 0, errors );
        
        return duration;
    }

    private long doDecode( int iterations, byte[] fixMsg ) {
        long startTime = Utils.nanoTime();
        
        int errors=0;

        for ( int i=0 ; i < iterations ; i++ ) {
            try {
                _testDecoder.decode( fixMsg, 0, fixMsg.length );
            } catch( Exception e ) {
                ++errors;
            }
        }
        
        long endTime = Utils.nanoTime();
        long duration = endTime - startTime;
        
        assertEquals( 0, errors );
        
        return duration;
    }
}
