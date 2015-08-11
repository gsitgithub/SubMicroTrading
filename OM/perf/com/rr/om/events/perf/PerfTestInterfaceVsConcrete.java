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
 * test the performance difference between using an interface and using a concrete class
 *
 * @author Richard Rose
 */

public class PerfTestInterfaceVsConcrete extends BaseTestCase {

    private int _dontOpt;

    public static interface Order {
        
        public ReusableString getClOrdId();
        public int getQty();
        public void addQty( int qty );
    }
    
    public static class OrderImpl implements Order {

        private int _qty = 0;
        private ReusableString _clOrdId;

        public OrderImpl( ReusableString clOrdId ) {
            super();
            _clOrdId = clOrdId;
        }

        @Override
        public void addQty( int qty ) {
            _qty += qty;
        }

        @Override
        public ReusableString getClOrdId() {
            return _clOrdId;
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
            OrderImpl other = (OrderImpl) obj;
            if ( _clOrdId == null ) {
                if ( other._clOrdId != null )
                    return false;
            } else if ( !_clOrdId.equals( other._clOrdId ) )
                return false;
            return true;
        }
    }
    
    public void testGenerics() {
    
        int runs = 5;
        int size = 1000000;
        int iterations = 100;
        
        doRun( runs, size, iterations );
    }

    private void doRun( int runs, int size, int iterations ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long concrete  = testConcrete( size, iterations );
            long interfce  = testInterface( size, iterations );
            
            System.out.println( "Run " + idx + " concrete=" + concrete + ", interface=" + interfce );
        }
    }

    private long testConcrete( int size, int iterations ) {

        Map<ReusableString,OrderImpl> map = new HashMap<ReusableString,OrderImpl>( size );
        
        for ( int i=0 ; i < size ; ++i ) {
            ReusableString key = new ReusableString( "SOMEKEY" + (10000000+i) );
            map.put( key, new OrderImpl( key ) );
        }
        
        ReusableString[] keys = map.keySet().toArray( new ReusableString[ size ] );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        for( int i=0 ; i < iterations ; ++i ) {
            int max = keys.length;
            
            for( int j=0 ; j < max ; ++j ) {
                OrderImpl order = map.get( keys[j] );
                
                order.addQty( j );
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long testInterface( int size, int iterations ) {
        
        Map<ReusableString,Order> map = new HashMap<ReusableString,Order>( size );
        
        for ( int i=0 ; i < size ; ++i ) {
            ReusableString key = new ReusableString( "SOMEKEY" + (10000000+i) );
            map.put( key, new OrderImpl( key ) );
        }
        
        ReusableString[] keys = map.keySet().toArray( new ReusableString[ size ] );
        
        Utils.invokeGC();
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        for( int i=0 ; i < iterations ; ++i ) {
            int max = keys.length;
            
            for( int j=0 ; j < max ; ++j ) {
                Order order = map.get( keys[j] );
                
                order.addQty( j );
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }
}
