/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.pool;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableType;
import com.rr.core.utils.Utils;

public class TestSuperPool extends BaseTestCase {

    static class TestReusable implements Reusable<TestReusable> {

        private TestReusable _next = null;
                int          _val  = 0;
        
               
        @Override
        public ReusableType getReusableType() {
            return null;
        }

        @Override
        public void reset() {
            _val = -1;
        }

        @Override
        public TestReusable getNext() {
            return _next;
        }

        @Override
        public void setNext( TestReusable nxt ) {
            _next = nxt;
        }
    }
    
    public void testSmallPool() {
        
        SuperPool<TestReusable>   pool    = new SuperPool<TestReusable>( TestReusable.class, 3, 5, 10 );
        PoolFactory<TestReusable> factory = pool.getPoolFactory();
        Set<TestReusable>         set     = new HashSet<TestReusable>();
        
        for( int i=0 ; i < 20 ; ++i ) {
            
            add( factory, set, i );
        }
        
        assertEquals( 1, pool.getCountExtraChains() );
    }

    public void testSmallPoolRecycle() {
        
        SuperPool<TestReusable>      pool     = new SuperPool<TestReusable>( TestReusable.class, 3, 5, 10 );
        PoolFactory<TestReusable>    factory  = pool.getPoolFactory();
        Recycler<TestReusable> recycler = pool.getRecycleFactory();
        Set<TestReusable>            set      = new HashSet<TestReusable>();
        
        for( int i=0 ; i < 20 ; ++i ) {
            
            TestReusable s = add( factory, set, i );
            recycle( s, set, recycler );
        }
        
        assertEquals( 0, pool.getCountExtraChains() );
        assertEquals( 4, pool.getCountRecycledChains() );
    }

    public void testSmallPoolRecycleAll() {
        
        SuperPool<TestReusable>      pool     = new SuperPool<TestReusable>( TestReusable.class, 3, 5, 10 );
        PoolFactory<TestReusable>    factory  = pool.getPoolFactory();
        Recycler<TestReusable>       recycler = pool.getRecycleFactory();
        Set<TestReusable>            set      = new HashSet<TestReusable>();
        
        for( int i=0 ; i < 20 ; ++i ) {
            
            add( factory, set, i );
        }

        assertEquals( 1, pool.getCountExtraChains() );
  
        for ( TestReusable r : set ) {
        
            recycler.recycle( r );
        }
        
        set.clear();

        assertEquals( 4, pool.getCountRecycledChains() );
        
        for( int i=0 ; i < 20 ; ++i ) {
            
            add( factory, set, i );
        }
        
        assertEquals( 1, pool.getCountExtraChains() );
        assertEquals( 4, pool.getCountRecycledChains() );
    }

    private void recycle( TestReusable s, Set<TestReusable> set, Recycler<TestReusable> recycler ) {

        set.remove( s );
        recycler.recycle( s );
    }

    static TestReusable add( PoolFactory<TestReusable> factory, Set<TestReusable> set, int i ) {
        TestReusable s;
        s  = factory.get();
        
        assertNotNull( s );
        assertTrue( ! set.contains( s ) );
        
        s._val = i;
        set.add( s );
        
        return s;
    }

    public void testSmallPoolResize() {
        
        SuperPool<TestReusable>   pool    = new SuperPool<TestReusable>( TestReusable.class, 3, 5, 10 );
        PoolFactory<TestReusable> factory = pool.getPoolFactory();
        Set<TestReusable>         set     = new HashSet<TestReusable>();
 
        pool.init( 5, 7, 10 );
        
        for( int i=0 ; i < 35 ; ++i ) {
            
            add( factory, set, i );
        }
        
        assertEquals( 0, pool.getCountExtraChains() );
    }

    public void testNoExtraPool() {
        
        SuperPool<TestReusable>   pool    = new SuperPool<TestReusable>( TestReusable.class, 3, 5, 10 );
        PoolFactory<TestReusable> factory = pool.getPoolFactory();
        Set<TestReusable>         set     = new HashSet<TestReusable>();
        
        for( int i=0 ; i < 15 ; ++i ) {
            
            add( factory, set, i );
        }
        
        assertEquals( 0, pool.getCountExtraChains() );
    }

    public void testLargePool() {
        
        SuperPool<TestReusable>   pool    = new SuperPool<TestReusable>( TestReusable.class, 3, 5, 10 );
        PoolFactory<TestReusable> factory = pool.getPoolFactory();
        Set<TestReusable>         set     = new HashSet<TestReusable>();
        
        for( int i=0 ; i < 2000 ; ++i ) {
            
            add( factory, set, i );
        }
        
        assertEquals( 199, pool.getCountExtraChains() );
    }

    public void testSuperLargePool() {
        
        SuperPool<TestReusable>   pool    = new SuperPool<TestReusable>( TestReusable.class, 10000, 100, 100 );
        PoolFactory<TestReusable> factory = pool.getPoolFactory();
        Set<TestReusable>         set     = new HashSet<TestReusable>();
        
        for( int i=0 ; i < 1000000 ; ++i ) {
            
            add( factory, set, i );
        }
        
        assertEquals( 0, pool.getCountExtraChains() );
    }

    public void testMultiPoolSingleRecycler() {
        
        SuperPool<TestReusable>   pool     = new SuperPool<TestReusable>( TestReusable.class, 6, 5, 3 );
        PoolFactory<TestReusable> factoryA = pool.getPoolFactory();
        PoolFactory<TestReusable> factoryB = pool.getPoolFactory();
        PoolFactory<TestReusable> factoryC = pool.getPoolFactory();
        PoolFactory<TestReusable> factoryD = pool.getPoolFactory();
        Recycler<TestReusable>    recycler = pool.getRecycleFactory();
        
        for( int i=0 ; i < 1000000 ; ++i ) {
            
            TestReusable ta = factoryA.get();
            TestReusable tb = factoryB.get();
            TestReusable tc = factoryC.get();
            TestReusable td = factoryD.get();

            recycler.recycle( ta );
            recycler.recycle( tb );
            recycler.recycle( tc );
            recycler.recycle( td );
        }
        
        assertEquals( 0, pool.getCountExtraChains() );
    }

    public void testMultiPoolSingleRecyclerBigPools() {
        
        SuperPool<TestReusable>   pool     = new SuperPool<TestReusable>( TestReusable.class, 100, 100, 100 );
        PoolFactory<TestReusable> factoryA = pool.getPoolFactory();
        PoolFactory<TestReusable> factoryB = pool.getPoolFactory();
        PoolFactory<TestReusable> factoryC = pool.getPoolFactory();
        PoolFactory<TestReusable> factoryD = pool.getPoolFactory();
        Recycler<TestReusable>    recycler = pool.getRecycleFactory();
        
        for( int i=0 ; i < 1000000 ; ++i ) {
            
            TestReusable ta = factoryA.get();
            TestReusable tb = factoryB.get();
            TestReusable tc = factoryC.get();
            TestReusable td = factoryD.get();

            recycler.recycle( ta );
            recycler.recycle( tb );
            recycler.recycle( tc );
            recycler.recycle( td );
        }
        
        assertEquals( 0, pool.getCountExtraChains() );
    }

    public void testConcurrent() {
        final SuperPool<TestReusable>   pool     = new SuperPool<TestReusable>( TestReusable.class, 10, 10, 10 );
        
        try {
            doTestConcurrant( pool, 1,  1, 1000 );
            doTestConcurrant( pool, 2,  1, 1000 );
            doTestConcurrant( pool, 4,  1, 1000 );
            doTestConcurrant( pool, 8,  1, 10000 );
            doTestConcurrant( pool, 32, 1, 10000 );
            doTestConcurrant( pool, 128, 1, 10000 );
            doTestConcurrant( pool, 1024, 1, 1000 );
        } catch( Exception e ) {
            fail( "exception " + e.getMessage() );
        }
        
        pool.logStats();
    }

    private void doTestConcurrant( final SuperPool<TestReusable> pool, int numThreads, final int delay, final int allocs ) {
        
        System.out.println( "Start threads=" + numThreads + ", allocs=" + allocs + ", delay=" + delay );
        
        final CountDownLatch cdl = new CountDownLatch(numThreads);
        final CyclicBarrier cb = new CyclicBarrier(numThreads);

        for( int t=0 ; t < numThreads ; ++t ) {
            new Thread( new Runnable(){
                @Override
                public void run() {
                    Set<TestReusable> set = new HashSet<TestReusable>();
    
                    Recycler<TestReusable>    recycler = pool.getRecycleFactory();
                    PoolFactory<TestReusable> factory = pool.getPoolFactory();
    
                    try {
                        cb.await();
                    } catch( Exception e ) {
                        //  dont care
                    }
    
                    int runSize = 35;
                    int runs = allocs / runSize;
                    int cnt=0;
                    
                    for( int i=0 ; i < runs ; i++ ) {
                        for( int j=0 ; j < runSize ; j++ ) {
                            add( factory, set, ++cnt );
                        }
                        
                        Utils.delay( delay );
                        
                        for ( TestReusable r : set ) {
                            recycler.recycle( r );
                        }
                        
                        set.clear();
                    }
                    
                    cdl.countDown();
                    
                }} ).start();
        }
        
        try {
            cdl.await();
        } catch( InterruptedException e ) {
            fail("Wait interrupted");
        }
        
        System.out.println( "Done threads=" + numThreads + ", allocs=" + allocs + ", delay=" + delay );
    }
}
