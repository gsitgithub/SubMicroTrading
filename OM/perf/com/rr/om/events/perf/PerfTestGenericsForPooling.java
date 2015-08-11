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
import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableType;

/**
 * test the performance overhead of generics in pooling
 *
 * @author Richard Rose
 */

// TODO check impact of replacing all the small (<=8 byte) with SmallString

public class PerfTestGenericsForPooling extends BaseTestCase {

    @SuppressWarnings( "unused" )
    private ReusableGeneric  _dontOpt;
    @SuppressWarnings( "unused" )
    private ReusableBaseImpl _dontOptCast;

    public static void main( String[] arg ) {
        PerfTestGenericsForPooling t = new PerfTestGenericsForPooling();
        
        t.testGenerics();
    }
    
    public static interface ReusableTypeInt extends Reusable<ReusableTypeInt> {
        //
    }
    
    public static class ReusableGeneric implements Reusable<ReusableGeneric> {

        private ReusableGeneric _nxt;

        public ReusableGeneric( ReusableGeneric nxt ) {
            _nxt = nxt;
        }
        
        @Override
        public ReusableType getReusableType() {
            return null;
        }

        @Override
        public void reset() {
            _nxt = _nxt._nxt;
        }

        @Override
        public ReusableGeneric getNext() {
            return _nxt;
        }

        @Override
        public void setNext( ReusableGeneric nxt ) {
            _nxt = nxt;
        }
    }
    
    public static class DummySimplePool {

        private ReusableGeneric _root;
        
        DummySimplePool( ReusableGeneric root ) {
            _root = root;
        }
        
        public ReusableGeneric get() {
            ReusableGeneric obj = _root;
            _root = _root.getNext();
            obj.setNext( obj );
            return obj;
        }
    }

    public static class DummyGenericPool<T extends Reusable<T>> {

        private T _root;
        
        DummyGenericPool( T root ) {
            _root = root;
        }
        
        public T get() {
            T obj = _root;
            _root = _root.getNext();
            obj.setNext( obj );
            return obj;
        }
    }
    
    public static class DummyArrayGenericPool<T extends Reusable<T>> {

        private T[]  _root;
        private int  _idx;
        private final int  _size;
        
        DummyArrayGenericPool( T[] root ) {
            _root = root;
            _idx = root.length;
            _size = root.length;
        }
        
        public T get() {
            --_idx;
            T obj = _root[_idx];
            _root[_idx] = obj;
            if ( _idx == 0 ) {
                _idx = _size;
            }
            return obj;
        }
    }
    
    public static interface ReusableBase {

        public void reset();
        public ReusableBase getNext();
        public void setNext( ReusableBase nxt );
    }
    
    public static class ReusableBaseImpl implements ReusableBase {

        private ReusableBase _nxt;

        public ReusableBaseImpl( ReusableBase nxt ) {
            _nxt = nxt;
        }
        
        @Override
        public void reset() {
            _nxt = _nxt.getNext();
        }

        @Override
        public ReusableBase getNext() {
            return _nxt;
        }

        @Override
        public void setNext( ReusableBase nxt ) {
            _nxt = nxt;
        }
    }
    
    public static class DummyCastPool {

        private ReusableBase _root;
        
        DummyCastPool( ReusableBase root ) {
            _root = root;
        }
        
        public ReusableBase get() {
            ReusableBase obj = _root;
            _root = _root.getNext();
            obj.setNext( obj );
            return obj;
        }
    }
    
    public void testGenerics() {
    
        int runs = 3;
        int iterations = 100000000;
        
        doRun( runs, iterations, 100 );
        doRun( runs, iterations, 1000 );
        doRun( runs, iterations, 10000 );
    }

    private void doRun( int runs, int iterations, int batch ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long gen      = gen( iterations, batch );
            long cast     = cast( iterations, batch );
            long explicit = explicit( iterations, batch );
            long array    = genArray( iterations, batch );
            
            System.out.println( "Run " + idx + " batch=" + batch + ", generics=" + gen + ", cast=" + cast + 
                                ", explicit=" + explicit + ", arr=" + array );
        }
    }

    private long gen( int iterations, int batch ) {
        
        ReusableGeneric r1 = new ReusableGeneric( null );
        ReusableGeneric end = r1;
        
        for( int i=0 ; i < batch ; i++ ) {
            ReusableGeneric r2 = new ReusableGeneric( r1 );
            r1 = r2;
        }
        
        end.setNext( r1 ); // circular LOOP
        
        DummyGenericPool<ReusableGeneric> pool = new DummyGenericPool<ReusableGeneric>( r1 );
        ReusableGeneric r3 = null;
        
        long startTime = System.currentTimeMillis();
                
        int j=0;
        for( int i=0 ; i < iterations ; ++i ) {
            r3 = pool.get();
            
            if ( ++j == batch ) {
                j=0;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = r3;
        
        return endTime - startTime;
    }

    private long genArray( int iterations, int batch ) {
        
        ReusableGeneric[] arr = new ReusableGeneric[ batch ];
        for( int i=0 ; i < batch ; ++i ) {
            arr[i] = new ReusableGeneric( null );
        }
        
        DummyArrayGenericPool<ReusableGeneric> pool = new DummyArrayGenericPool<ReusableGeneric>( arr );
        ReusableGeneric r3 = null;
        
        long startTime = System.currentTimeMillis();

        for( int i=0 ; i < iterations ; ++i ) {
            r3 = pool.get();
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = r3;
        
        return endTime - startTime;
    }

    private long cast( int iterations, int batch ) {

        ReusableBaseImpl r1 = new ReusableBaseImpl( null );
        ReusableBaseImpl end = r1;
        
        for( int i=0 ; i < batch ; i++ ) {
            ReusableBaseImpl r2 = new ReusableBaseImpl( r1 );
            r1 = r2;
        }
        
        end.setNext( r1 ); // circular LOOP
        
        DummyCastPool pool = new DummyCastPool( r1 );
        ReusableBaseImpl r3 = null;
        
        long startTime = System.currentTimeMillis();

        int j=0;
        
        for( int i=0 ; i < iterations ; ++i ) {
            r3 = (ReusableBaseImpl) pool.get();
            if ( ++j == batch ) {
                j=0;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOptCast = r3;
        
        return endTime - startTime;
    }

    private long explicit( int iterations, int batch ) {
        
        ReusableGeneric r1 = new ReusableGeneric( null );
        ReusableGeneric end = r1;
        
        for( int i=0 ; i < batch ; i++ ) {
            ReusableGeneric r2 = new ReusableGeneric( r1 );
            r1 = r2;
        }
        
        end.setNext( r1 ); // circular LOOP
        
        DummySimplePool pool = new DummySimplePool( r1 );
        ReusableGeneric r3 = null;
        
        long startTime = System.currentTimeMillis();
            
        int j=0;
        
        for( int i=0 ; i < iterations ; ++i ) {
            r3 = pool.get();

            if ( ++j == batch ) {
                j=0;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = r3;
        
        return endTime - startTime;
    }
}
