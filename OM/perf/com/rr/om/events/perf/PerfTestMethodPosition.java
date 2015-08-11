/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.utils.Utils;

/**
 * test the performance difference in alpha order of method name and first / last methods
 *
 * @author Richard Rose
 */

public class PerfTestMethodPosition extends BaseTestCase {

    private int _dontOpt;

    public static interface ShortMethodNames {
        
        public void aa1( int qty );
        int getQty();
        public void aa2( int qty );
        public void aa3( int qty );
        public void aa4( int qty );
        public void aa5( int qty );
        public void aa6( int qty );
        public void aa7( int qty );
        public void aa8( int qty );
        public void aa9( int qty );
        public void aaA( int qty );
        public void aaB( int qty );
        public void aaC( int qty );
        public void aaD( int qty );
        public void aaE( int qty );
        public void aaF( int qty );
        public void aaG( int qty );
        public void aaH( int qty );
        public void aaI( int qty );
        public void aaJ( int qty );
        public void aaK( int qty );
        public void aaL( int qty );
        public void aaM( int qty );
        public void aaN( int qty );
        public void aaO( int qty );
        public void aaP( int qty );
        public void aaQ( int qty );
        public void aaR( int qty );
        public void aaS( int qty );
    }
    
    public static interface LongMethodNames {
        
        public void aaa11111111111111111111( int qty );
        int getQty();
        public void aaa11111111111111111112( int qty );
        public void aaa11111111111111111113( int qty );
        public void aaa11111111111111111114( int qty );
        public void aaa11111111111111111115( int qty );
        public void aaa11111111111111111116( int qty );
        public void aaa11111111111111111117( int qty );
        public void aaa11111111111111111118( int qty );
        public void aaa11111111111111111119( int qty );
        public void aaa1111111111111111111A( int qty );
        public void aaa1111111111111111111B( int qty );
        public void aaa1111111111111111111C( int qty );
        public void aaa1111111111111111111D( int qty );
        public void aaa1111111111111111111E( int qty );
        public void aaa1111111111111111111F( int qty );
        public void aaa1111111111111111111G( int qty );
        public void aaa1111111111111111111H( int qty );
        public void aaa1111111111111111111I( int qty );
        public void aaa1111111111111111111J( int qty );
        public void aaa1111111111111111111K( int qty );
        public void aaa1111111111111111111L( int qty );
        public void aaa1111111111111111111M( int qty );
        public void aaa1111111111111111111N( int qty );
        public void aaa1111111111111111111O( int qty );
        public void aaa1111111111111111111P( int qty );
        public void aaa1111111111111111111Q( int qty );
        public void aaa1111111111111111111R( int qty );
        public void aaa1111111111111111111S( int qty );
    }
    
    public static class ShortImpl implements ShortMethodNames {

        private int _qty = 0;
        private ReusableString _clOrdId;

        public ShortImpl( ReusableString clOrdId ) {
            super();
            _clOrdId = clOrdId;
        }

        @Override
        public int getQty() {
            return _qty;
        }

        @Override
        public int hashCode() {
            return _clOrdId.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            ShortImpl other = (ShortImpl) obj;
            if ( _clOrdId == null ) {
                if ( other._clOrdId != null )
                    return false;
            } else if ( !_clOrdId.equals( other._clOrdId ) )
                return false;
            return true;
     
        }

        @Override
        public void aa1( int qty ) { _qty += qty; }
        @Override
        public void aa2( int qty ) { _qty += qty; }
        @Override
        public void aa3( int qty ) { _qty += qty; }
        @Override
        public void aa4( int qty ) { _qty += qty; }
        @Override
        public void aa5( int qty ) { _qty += qty; }
        @Override
        public void aa6( int qty ) { _qty += qty; }
        @Override
        public void aa7( int qty ) { _qty += qty; }
        @Override
        public void aa8( int qty ) { _qty += qty; }
        @Override
        public void aa9( int qty ) { _qty += qty; }
        @Override
        public void aaA( int qty ) { _qty += qty; }
        @Override
        public void aaB( int qty ) { _qty += qty; }
        @Override
        public void aaC( int qty ) { _qty += qty; }
        @Override
        public void aaD( int qty ) { _qty += qty; }
        @Override
        public void aaE( int qty ) { _qty += qty; }
        @Override
        public void aaF( int qty ) { _qty += qty; }
        @Override
        public void aaG( int qty ) { _qty += qty; }
        @Override
        public void aaH( int qty ) { _qty += qty; }
        @Override
        public void aaI( int qty ) { _qty += qty; }
        @Override
        public void aaJ( int qty ) { _qty += qty; }
        @Override
        public void aaK( int qty ) { _qty += qty; }
        @Override
        public void aaL( int qty ) { _qty += qty; }
        @Override
        public void aaM( int qty ) { _qty += qty; }
        @Override
        public void aaN( int qty ) { _qty += qty; }
        @Override
        public void aaO( int qty ) { _qty += qty; }
        @Override
        public void aaP( int qty ) { _qty += qty; }
        @Override
        public void aaQ( int qty ) { _qty += qty; }
        @Override
        public void aaR( int qty ) { _qty += qty; }
        @Override
        public void aaS( int qty ) { _qty += qty; }

    }

    public static class LongImpl implements LongMethodNames {

        private int _qty = 0;
        private ReusableString _clOrdId;

        public LongImpl( ReusableString clOrdId ) {
            super();
            _clOrdId = clOrdId;
        }


        @Override
        public int getQty() {
            return _qty;
        }

        @Override
        public int hashCode() {
            return _clOrdId.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            LongImpl other = (LongImpl) obj;
            if ( _clOrdId == null ) {
                if ( other._clOrdId != null )
                    return false;
            } else if ( !_clOrdId.equals( other._clOrdId ) )
                return false;
            return true;
        }

        @Override
        public void aaa11111111111111111111( int qty ) { _qty += qty; }
        @Override
        public void aaa11111111111111111112( int qty ) { _qty += qty; }
        @Override
        public void aaa11111111111111111113( int qty ) { _qty += qty; }
        @Override
        public void aaa11111111111111111114( int qty ) { _qty += qty;}
        @Override
        public void aaa11111111111111111115( int qty ) { _qty += qty;}
        @Override
        public void aaa11111111111111111116( int qty ) { _qty += qty;}
        @Override
        public void aaa11111111111111111117( int qty ) {_qty += qty;}
        @Override
        public void aaa11111111111111111118( int qty ) {_qty += qty;}
        @Override
        public void aaa11111111111111111119( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111A( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111B( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111C( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111D( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111E( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111F( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111G( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111H( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111I( int qty ) { _qty += qty;}
        @Override
        public void aaa1111111111111111111J( int qty ) { _qty += qty;}
        @Override
        public void aaa1111111111111111111K( int qty ) { _qty += qty; }
        @Override
        public void aaa1111111111111111111L( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111M( int qty ) { _qty += qty;}
        @Override
        public void aaa1111111111111111111N( int qty ) { _qty += qty;}
        @Override
        public void aaa1111111111111111111O( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111P( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111Q( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111R( int qty ) {_qty += qty;}
        @Override
        public void aaa1111111111111111111S( int qty ) {_qty += qty;}
    }
    
    public void testFirstMethod() {
    
        int runs = 5;
        int size = 1000000;
        int iterations = 100;
        
        doRun( runs, size, iterations );
    }

    private void doRun( int runs, int size, int iterations ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long sh = testShort( size, iterations, true );
            long ln = testLong( size, iterations, true );
            
            System.out.println( "Run " + idx + " short=" + sh + ", long=" + ln + ", firstMethod=TRUE" );

            sh = testShort( size, iterations, false );
            ln = testLong( size, iterations, false  );
            
            System.out.println( "Run " + idx + " short=" + sh + ", long=" + ln + ", firstMethod=FALSE" );
        }
    }

    private long testShort( int size, int iterations, boolean firstMethod ) {

        Map<ReusableString,ShortImpl> map = new HashMap<ReusableString,ShortImpl>( size );
        
        for ( int i=0 ; i < size ; ++i ) {
            ReusableString key = new ReusableString( "SOMEKEY" + (10000000+i) );
            map.put( key, new ShortImpl( key ) );
        }
        
        ReusableString[] keys = map.keySet().toArray( new ReusableString[ size ] );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        if ( firstMethod ) {
            for( int i=0 ; i < iterations ; ++i ) {
                int max = keys.length;
                
                for( int j=0 ; j < max ; ++j ) {
                    ShortImpl order = map.get( keys[j] );
                    
                    order.aa1( j );
                }
            }
            
        } else {
            for( int i=0 ; i < iterations ; ++i ) {
                int max = keys.length;
                
                for( int j=0 ; j < max ; ++j ) {
                    ShortImpl order = map.get( keys[j] );
                    
                    order.aaS( j );
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long testLong( int size, int iterations, boolean firstMethod ) {

        Map<ReusableString,LongImpl> map = new HashMap<ReusableString,LongImpl>( size );
        
        for ( int i=0 ; i < size ; ++i ) {
            ReusableString key = new ReusableString( "SOMEKEY" + (10000000+i) );
            map.put( key, new LongImpl( key ) );
        }
        
        ReusableString[] keys = map.keySet().toArray( new ReusableString[ size ] );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        if ( firstMethod ) {
            for( int i=0 ; i < iterations ; ++i ) {
                int max = keys.length;
                
                for( int j=0 ; j < max ; ++j ) {
                    LongImpl order = map.get( keys[j] );
                    
                    order.aaa11111111111111111111( j );
                }
            }
            
        } else {
            for( int i=0 ; i < iterations ; ++i ) {
                int max = keys.length;
                
                for( int j=0 ; j < max ; ++j ) {
                    LongImpl order = map.get( keys[j] );
                    
                    order.aaa1111111111111111111S( j );
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }
}
