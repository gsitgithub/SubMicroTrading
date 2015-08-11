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
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.utils.NumberFormatUtils;

public class PerfTestDateDecode extends BaseTestCase {

    protected final byte[] _today = new byte[ TimeZoneCalculator.DATE_STR_LEN ];
    
    protected TimeZoneCalculator   _tzCalculator = new TimeZoneCalculator();

    protected int    _idx = 0;
    protected byte[] _fixMsg;
    protected int    _maxIdx;
    protected int    _offset;
    
    public void testDecodeDateTime() {
        
        int runs = 5;
        int iterations = 10000000;
        
        doRunDateTimeDecode( runs, iterations );
    }

    private void doRunDateTimeDecode( int runs, int iterations ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long mult  = mult( iterations );
            long table = table( iterations );
            
            System.out.println( "Run " + idx + " mult=" + mult + ", table=" + table );
        }
    }

    private long mult( int iterations ) {
        
        _tzCalculator.getDateUTC( _today );
        _fixMsg = ( new String(_today) + "12:01:01.100; ").getBytes();
        _maxIdx = _fixMsg.length;
        _offset = 0;
        
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
            
            _idx = 0;
            
            int todayLen = _today.length;
            
            for ( int j=0 ; j < todayLen ; ++j ) {
                if ( _fixMsg[ _idx+j ] != _today[ j ] ) {
                    assertEquals( _fixMsg[ _idx+j ], _today[ j ] );
                }
            }
            
            _idx += todayLen;
            
            int hTen = _fixMsg[ _idx++ ] - '0';
            int hDig = _fixMsg[ _idx++ ] - '0';
            _idx++;
            int mTen = _fixMsg[ _idx++ ] - '0';
            int mDig = _fixMsg[ _idx++ ] - '0';
            _idx++;
            int sTen = _fixMsg[ _idx++ ] - '0';
            int sDig = _fixMsg[ _idx++ ] - '0';

            int hour = ((hTen) * 10)  + (hDig);
            int min  = ((mTen) * 10)  + (mDig);
            int sec  = ((sTen) * 10)  + (sDig);

            if ( hour < 0 || hour > 23 ) {
                assertTrue( hour >= 0 && hour < 24 );
            }
            
            if ( min < 0 || min > 59 ) {
                assertTrue( min >= 0 && min < 60 );
            }
            
            if ( sec < 0 || sec > 59 ) {
                assertTrue( sec >= 0 && sec < 60 );
            }

            int ms = ((hour * 3600) + (min*60) + sec) * 1000 + _tzCalculator.getOffset();

            if ( _fixMsg[ _idx ] == '.' ) {
                _idx++;
                int msHun = _fixMsg[ _idx++ ] - '0';
                int msTen = _fixMsg[ _idx++ ] - '0';
                int msDig = _fixMsg[ _idx++ ] - '0';

                ms += ((msHun*100) + (msTen*10) + msDig); 
            }

            if ( ms < 0 || ms > 999 ) {
                assertTrue( ms >= 0 && ms < 1000 );
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }

    private long table( int iterations ) {
        
        _tzCalculator.getDateUTC( _today );
        _fixMsg = (new String(_today) + "12:01:01.100; ").getBytes();
        _maxIdx = _fixMsg.length;
        _offset = 0;
        
        long startTime = System.currentTimeMillis();
                
        for( int i=0 ; i < iterations ; ++i ) {
            
            _idx = 0;
            
            int todayLen = _today.length;
            
            for ( int j=0 ; j < todayLen ; ++j ) {
                if ( _fixMsg[ _idx+j ] != _today[ j ] ) {
                    assertEquals( _fixMsg[ _idx+j ], _today[ j ] );
                }
            }
            
            _idx += todayLen;
            
            int hTen = _fixMsg[ _idx++ ] - '0';
            int hDig = _fixMsg[ _idx++ ] - '0';
            _idx++;
            int mTen = _fixMsg[ _idx++ ] - '0';
            int mDig = _fixMsg[ _idx++ ] - '0';
            _idx++;
            int sTen = _fixMsg[ _idx++ ] - '0';
            int sDig = _fixMsg[ _idx++ ] - '0';

            int hour = NumberFormatUtils._tens[ hTen ] + hDig;
            int min  = NumberFormatUtils._tens[ mTen ] + mDig;
            int sec  = NumberFormatUtils._tens[ sTen ] + sDig;

            if ( hour < 0 || hour > 23 ) {
                assertTrue( hour >= 0 && hour < 24 );
            }
            
            if ( min < 0 || min > 59 ) {
                assertTrue( min >= 0 && min < 60 );
            }
            
            if ( sec < 0 || sec > 59 ) {
                assertTrue( sec >= 0 && sec < 60 );
            }

            int ms = TimeZoneCalculator._hourToMS[ hour ]  +
                     TimeZoneCalculator._minToMS[ min ] +
                     TimeZoneCalculator._secToMS[ sec ] +
                     _tzCalculator.getOffset();

            if ( _fixMsg[ _idx ] == '.' ) {
                _idx++;
                int msHun = _fixMsg[ _idx++ ] - '0';
                int msTen = _fixMsg[ _idx++ ] - '0';
                int msDig = _fixMsg[ _idx++ ] - '0';

                ms += NumberFormatUtils._hundreds[ msHun ] + NumberFormatUtils._tens[ msTen ] + msDig;
            }

            if ( ms < 0 || ms > 999 ) {
                assertTrue( ms >= 0 && ms < 1000 );
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }
}
