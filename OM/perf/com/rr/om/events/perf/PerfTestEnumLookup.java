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

public class PerfTestEnumLookup extends BaseTestCase {

    public void testPerf() {
    
        int runs = 5;
        int iterations = 10000000;
        int size = 10;
        
        doRun( runs, iterations, size, 1 );
        doRun( runs, iterations, size, 2 );
        doRun( runs, iterations, size, 3 );
        doRun( runs, iterations, size, 9 );
        doRun( runs, iterations, size, 0 );
    }

    private void doRun( int runs, int iterations, int size, int hitPosition ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long switchDuration = perfSwitch( iterations, size, hitPosition );
            long arrayDuration  = perfArrayLookup( iterations, size, hitPosition );
            
            System.out.println( "Run " + idx + " switchDuration=" + switchDuration + ", arrayDuration=" + arrayDuration + " hitPosition=" + hitPosition );
        }
    }

    private static enum TestEnumArray {
        Buy( "1" ),
        Sell( "2" ),
        BuyMinus( "3" ),
        SellPlus( "4" ),
        SellShort( "5" ),
        SellShortExempt( "6" ),
        Undisc( "7" ),
        Cross( "8" ),
        CrossShort( "9" ),
        CrossShortExempt( "A" ),
        AsDefined( "B" ),
        Opposite( "C" ),
        Subscribe( "D" ),
        Redeem( "E" ),
        Lend( "F" ),
        Borrow( "G" );

        private static TestEnumArray[] _array = new TestEnumArray[256];
        
        static {

            for ( TestEnumArray en : TestEnumArray.values() ) {
                _array[ en.getVal() ] = en;
            }
        }
        
        private byte _val;

        TestEnumArray( String val ) {
            _val = val.getBytes()[0];
        }

        private byte getVal() {
            return _val;
        }

        public static TestEnumArray valueOf( byte val ) {
            return _array[ val ];
        }
        
    }
    
    private long perfArrayLookup( int iterations, int size, int hitPosition ) {
        
        long res=0;
        byte hit = (byte)(hitPosition % 256);

        @SuppressWarnings( "unused" )
        TestEnumArray dummy = TestEnumArray.Buy;
        
        long start = System.currentTimeMillis();
        
        for ( int i=0 ; i < iterations  ; i++ ) {
        
            TestEnumArray e = TestEnumArray.valueOf( hit );
            
            if ( e != null ) {
                ++res;
            }
        }
        
        long end = System.currentTimeMillis();
        
        assertTrue( res > 0 );
        
        return end - start;
    }

    private enum TestEnumIf {
        Buy( "1" ),
        Sell( "2" ),
        BuyMinus( "3" ),
        SellPlus( "4" ),
        SellShort( "5" ),
        SellShortExempt( "6" ),
        Undisc( "7" ),
        Cross( "8" ),
        CrossShort( "9" ),
        CrossShortExempt( "A" ),
        AsDefined( "B" ),
        Opposite( "C" ),
        Subscribe( "D" ),
        Redeem( "E" ),
        Lend( "F" ),
        Borrow( "G" );

        @SuppressWarnings( "unused" )
        private byte _val;

        TestEnumIf( String val ) {
            _val = val.getBytes()[0];
        }

        public static TestEnumIf valueOf( byte val ) {
            if ( val == '1' ) return Buy;
            if ( val == '2' ) return Sell;
            if ( val == '3' ) return BuyMinus;
            if ( val == '4' ) return SellPlus;
            if ( val == '5' ) return SellShort;
            if ( val == '6' ) return SellShortExempt;
            if ( val == '7' ) return Undisc;
            if ( val == '8' ) return Cross;
            if ( val == '9' ) return CrossShort;
            if ( val == 'A' ) return CrossShortExempt;
            if ( val == 'B' ) return AsDefined;
            if ( val == 'C' ) return Opposite;
            if ( val == 'D' ) return Subscribe;
            if ( val == 'E' ) return Redeem;
            if ( val == 'F' ) return Lend;
            if ( val == 'G' ) return Borrow;

            return null;
        }
    }

    private long perfSwitch( int iterations, int size, int hitPosition ) {
        long res=0;

        byte hit = (byte)((hitPosition % 10) + '0');
        
        long start = System.currentTimeMillis();
        
        for ( int i=0 ; i < iterations  ; i++ ) {

            TestEnumIf e = TestEnumIf.valueOf( hit );
            
            if ( e != null ) {
                ++res;
            }
        }
        
        long end = System.currentTimeMillis();
        
        assertTrue( res > 0 );
        
        return end - start;
    }
    
}
