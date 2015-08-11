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
 * test the performance difference in implmenting next pointer
 * 
 * a) explicit type
 * b) object reference with cast
 * c) generic type
 *
 * @author Richard Rose
 */

@SuppressWarnings( "unchecked" )

public class PerfTestNextPointer extends BaseTestCase {

    private int _dontOpt = 0;

    public static class Explicit {
        int      _someVal;
        Explicit _next;
        
        public Explicit getNext() { 
            return _next;
        }
    }

    public static class Gen<T> {
        int      _someVal;
        T        _next;
        
        public T getNext() { 
            return _next;
        }
    }

    public static class Cast {
        int      _someVal;
        Object   _next;
        
        public Object getNext() { 
            return _next;
        }
    }

    public void testNext() {
    
        int runs = 5;
        int iterations = 100000000;
        int chainSize = 1000000;
        
        doRun( runs, iterations, chainSize );
    }

    private void doRun( int runs, int iterations, int chainSize ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long explicit = explicitNextMethod( iterations, chainSize );
            long gen      = genNextMethod( iterations, chainSize );
            long cast     = castNextMethod( iterations, chainSize );
            
            System.out.println( "Run " + idx + " NEXT by METHOD, generics=" + gen + ", cast=" + cast + ", explicit=" + explicit );

            long genD     = genNextDirect( iterations, chainSize );
            long castD    = castNextDirect( iterations, chainSize );
            long expD     = explicitNextDirect( iterations, chainSize );
            
            System.out.println( "Run " + idx + " NEXT by DIRECT, generics=" + genD + ", cast=" + castD + ", explicit=" + expD );
        }
        assertTrue( _dontOpt > 0 );
    }

    @SuppressWarnings( "rawtypes" )
    private long genNextMethod( int iterations, int chainSize ) {
        Gen<Gen> root = new Gen<Gen>();
        Gen<Gen> last = root;
        for ( int i = 0 ; i < chainSize ; i++ ) {
            Gen<Gen> n = new Gen<Gen>();
            n._next = root;
            n._someVal = i;
            root = n;
        }

        last._next = root;
        
        long startTime = System.currentTimeMillis();
                
        Gen<Gen> tmp = root;
        for( int i=0 ; i < iterations ; ++i ) {
            tmp = tmp.getNext();
        }
        
        long endTime = System.currentTimeMillis();
        _dontOpt += tmp._someVal;
        
        return endTime - startTime;
    }

    private long castNextMethod( int iterations, int chainSize ) {
        Cast root = new Cast();
        Cast last = root;
        for ( int i = 0 ; i < chainSize ; i++ ) {
            Cast n = new Cast();
            n._next = root;
            n._someVal = i;
            root = n;
        }

        last._next = root;
        
        long startTime = System.currentTimeMillis();
                
        Cast tmp = root;
        for( int i=0 ; i < iterations ; ++i ) {
            tmp = (Cast) tmp.getNext();
        }
        
        long endTime = System.currentTimeMillis();
        _dontOpt += tmp._someVal;
        
        return endTime - startTime;
    }

    private long explicitNextMethod( int iterations, int chainSize ) {

        Explicit root = new Explicit();
        Explicit last = root;
        for ( int i = 0 ; i < chainSize ; i++ ) {
            Explicit n = new Explicit();
            n._next = root;
            n._someVal = i;
            root = n;
        }

        last._next = root;
        
        long startTime = System.currentTimeMillis();
                
        Explicit tmp = root;
        for( int i=0 ; i < iterations ; ++i ) {
            tmp = tmp.getNext();
        }
        
        long endTime = System.currentTimeMillis();
        _dontOpt += tmp._someVal;
        
        return endTime - startTime;
    }

    @SuppressWarnings( "rawtypes" )
    private long genNextDirect( int iterations, int chainSize ) {
        Gen<Gen> root = new Gen<Gen>();
        Gen<Gen> last = root;
        for ( int i = 0 ; i < chainSize ; i++ ) {
            Gen<Gen> n = new Gen<Gen>();
            n._next = root;
            n._someVal = i;
            root = n;
        }

        last._next = root;
        
        long startTime = System.currentTimeMillis();
                
        Gen<Gen> tmp = root;
        for( int i=0 ; i < iterations ; ++i ) {
            tmp = tmp._next;
        }
        
        long endTime = System.currentTimeMillis();
        _dontOpt += tmp._someVal;
        
        return endTime - startTime;
    }

    private long castNextDirect( int iterations, int chainSize ) {
        Cast root = new Cast();
        Cast last = root;
        for ( int i = 0 ; i < chainSize ; i++ ) {
            Cast n = new Cast();
            n._next = root;
            n._someVal = i;
            root = n;
        }

        last._next = root;
        
        long startTime = System.currentTimeMillis();
                
        Cast tmp = root;
        for( int i=0 ; i < iterations ; ++i ) {
            tmp = (Cast) tmp._next;
        }
        
        long endTime = System.currentTimeMillis();
        _dontOpt += tmp._someVal;
        
        return endTime - startTime;
    }

    private long explicitNextDirect( int iterations, int chainSize ) {

        Explicit root = new Explicit();
        Explicit last = root;
        for ( int i = 0 ; i < chainSize ; i++ ) {
            Explicit n = new Explicit();
            n._next = root;
            n._someVal = i;
            root = n;
        }

        last._next = root;
        
        long startTime = System.currentTimeMillis();
                
        Explicit tmp = root;
        for( int i=0 ; i < iterations ; ++i ) {
            tmp = tmp._next;
        }
        
        long endTime = System.currentTimeMillis();
        _dontOpt += tmp._someVal;
        
        return endTime - startTime;
    }
}
