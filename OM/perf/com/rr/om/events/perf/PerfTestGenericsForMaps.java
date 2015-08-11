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
import com.rr.core.lang.ReusableString;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.om.events.perf.generics.maps.DummyMapExplicit;
import com.rr.om.events.perf.generics.maps.DummyMapExplicitFinalMod;
import com.rr.om.events.perf.generics.maps.DummyMapExplicitWrapperToGenerics;
import com.rr.om.events.perf.generics.maps.DummyMapWithGenerics;
import com.rr.om.events.perf.generics.maps.DummyMapWithGenericsAndFinalMod;

/**
 * test to determine overhead of using generics in map
 *
 */
public class PerfTestGenericsForMaps extends BaseTestCase {

    private int _dontOpt = 0;

    public void testGenerics() {
        
        int runs = 5;
        int size = 100000;
        int numIterations = 1000;
        
        doRun( runs, size, numIterations );
    }

    private void doRun( int runs, int size, int numIterations) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long expFinal    = testExpFinal( size, numIterations, false );
            long genMap      = testGenMap( size, numIterations, false );
            long genMapFinal = testGenMapFinal( size, numIterations, false );
            long exp         = testExp( size, numIterations, false );
            long expWrapper  = testWrapper( size, numIterations, false );
            
            System.out.println( "Run " + idx + " GET generic=" + genMap + ", genFinalAccessor=" +  genMapFinal + 
                                ", explicit=" + exp + ", expFinalAccessor=" + expFinal + ", wrapped=" + expWrapper );
        }
        
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long expFinal    = testExpFinal( size, numIterations, true );
            long genMap      = testGenMap( size, numIterations, true );
            long genMapFinal = testGenMapFinal( size, numIterations, true );
            long exp         = testExp( size, numIterations, true );
            long expWrapper  = testWrapper( size, numIterations, true );
            
            System.out.println( "Run " + idx + " SET generic=" + genMap + ", genFinalAccessor=" +  genMapFinal + 
                                ", explicit=" + exp + ", expFinalAccessor=" + expFinal + ", wrapped=" + expWrapper );
        }
    }

    private long testGenMap( int size, int iterations, boolean isWrite ) {
        
        DummyMapWithGenerics<ReusableString,ClientNewOrderSingleImpl> map = 
                                new DummyMapWithGenerics<ReusableString,ClientNewOrderSingleImpl>( size ); 
        
        for( int i=0 ; i < size ; i++ ) {
            map.set( new ReusableString( "key" + i ), null, i );
        }
        
        ReusableString match = map.getKey( 10 );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        if ( isWrite ) {
            for( int j=0; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    map.set( match, null, i );
                }
            }
        } else {
            for( int j=0; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    ReusableString key = map.getKey( i );
                    if ( match == key ) {
                        cnt++;
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long testGenMapFinal( int size, int iterations, boolean isWrite ) {
        
        DummyMapWithGenericsAndFinalMod<ReusableString,ClientNewOrderSingleImpl> map = 
                                        new DummyMapWithGenericsAndFinalMod<ReusableString,ClientNewOrderSingleImpl>( size ); 
        
        for( int i=0 ; i < size ; i++ ) {
            map.set( new ReusableString( "key" + i ), null, i );
        }
        
        ReusableString match = map.getKey( 10 );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        if ( isWrite ) {
            for( int j=0; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    map.set( match, null, i );
                }
            }
        } else {
            for( int j=0; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    ReusableString key = map.getKey( i );
                    if ( match == key ) {
                        cnt++;
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long testExp( int size, int iterations, boolean isWrite ) {
        
        DummyMapExplicit map = new DummyMapExplicit( size ); 
        
        for( int i=0 ; i < size ; i++ ) {
            map.set( new ReusableString( "key" + i ), null, i );
        }
        
        ReusableString match = map.getKey( 10 );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        if ( isWrite ) {
            for( int j=0; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    map.set( match, null, i );
                }
            }
        } else {
            for( int j=0 ; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    ReusableString key = map.getKey( i );
                    if ( match == key ) {
                        cnt++;
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long testWrapper( int size, int iterations, boolean isWrite ) {
        
        DummyMapExplicitWrapperToGenerics map = new DummyMapExplicitWrapperToGenerics( size ); 
        
        for( int i=0 ; i < size ; i++ ) {
            map.set( new ReusableString( "key" + i ), null, i );
        }
        
        ReusableString match = map.getKey( 10 );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        if ( isWrite ) {
            for( int j=0; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    map.set( match, null, i );
                }
            }
        } else {
            for( int j=0 ; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    ReusableString key = map.getKey( i );
                    if ( match == key ) {
                        cnt++;
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long testExpFinal( int size, int iterations, boolean isWrite ) {
        
        DummyMapExplicitFinalMod map = new DummyMapExplicitFinalMod( size ); 
        
        for( int i=0 ; i < size ; i++ ) {
            map.set( new ReusableString( "key" + i ), null, i );
        }
        
        ReusableString match = map.getKey( 10 );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
        
        if ( isWrite ) {
            for( int j=0; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    map.set( match, null, i );
                }
            }
        } else {
            for( int j=0 ; j < iterations ; ++j ) {
                for( int i=0 ; i < size ; ++i ) {
                    ReusableString key = map.getKey( i );
                    if ( match == key ) {
                        cnt++;
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }
}
