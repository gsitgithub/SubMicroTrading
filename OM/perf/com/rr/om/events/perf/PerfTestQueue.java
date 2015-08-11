/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import java.util.LinkedHashSet;
import java.util.Set;

import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.JavaConcMessageQueue;
import com.rr.core.collections.MessageQueue;
import com.rr.core.collections.NonBlockingSyncQueue;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Percentiles;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.recycle.ClientNewOrderSingleRecycler;
import com.rr.om.warmup.FixTestUtils;

public class PerfTestQueue extends BaseTestCase {

    private static final Logger _log = LoggerFactory.create( PerfTestQueue.class );
    
    private static class ConsumerRunnable implements Runnable {

        private static final long MAX_CONSUME_WAIT_MS = 1000;
        
        private int _tot;
        private MessageQueue _q;
        private volatile boolean _finished = false;
        private int _count = 0;
        private long _totQty = 0;
        private int[] _stats;
        

        public ConsumerRunnable( int tot, MessageQueue q ) {
            _tot = tot;
            _q = q;
            _stats = new int[_tot];
        }

        @Override
        public void run() {
            long last = System.currentTimeMillis();
            long now;
            
            SuperPool<ClientNewOrderSingleImpl> nosSP = SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class );
            ClientNewOrderSingleRecycler        nosRecycler = new ClientNewOrderSingleRecycler( nosSP.getChainSize(), nosSP );

            ClientNewOrderSingleImpl nos;
            
            Message m = null;

            while( _count < _tot ) {
                
                do {
                    m = _q.poll();
            
                    now = System.currentTimeMillis();
                    
                    if ( now - last > MAX_CONSUME_WAIT_MS ) {
                        _finished = true;
                    }
                    
                } while( m == null && !_finished );
                
                if ( m != null ) {
         
                    nos = (ClientNewOrderSingleImpl) m;

                    int delay = (int) (Utils.nanoTime() - nos.getOrderReceived());
                    _stats[_count] = delay;
                    
                    _totQty += nos.getOrderQty();

                    ++_count;
                    
                    nosRecycler.recycle( nos );
                }
                
                last = now; 
            }
            
            _finished = true;
        }

        public int[] getTimes(){
            return _stats;
        }
        
        public boolean finished() {
            return _finished ;
        }

        public int getConsumed() {
            return _count;
        }

        @SuppressWarnings( "unused" )
        public long getTotalQty() {
            return _totQty;
        }
    }
    
    private static class ProducerRunnable implements Runnable {

        private final MessageQueue _q;
        private int _count;
        private final int _producerIdx;
        private int _errs = 0;
        private int _sent;
        private ReusableString _buffer = new ReusableString(256);
        private final int _producerDelayMS;

        public ProducerRunnable( int producerIdx, 
                                 int count, 
                                 MessageQueue q, 
                                 int producerDelayMS ) {
            _q = q;
            _count = count;
            _sent = 0;
            _producerIdx = producerIdx;
            _producerDelayMS = producerDelayMS;
        }

        @Override
        public void run() {
            int baseQty = _producerIdx * _count;
            
            Standard44Decoder decoder = FixTestUtils.getDecoder44();
            ReusableString    key     = new ReusableString(20);
            
            for ( int i=0 ; i < _count ; ++i ) {
                Thread.yield();
                decoder.setReceived( Utils.nanoTime() );
                mkKey( key, true, i );
                key.append( _producerIdx );
                Message msg = FixTestUtils.getClientNOS( _buffer, decoder, key, baseQty + i, 1, null );
                if ( msg != null ) {
                    if ( _q.add( msg ) == false ) {
                        ++_errs;
                    } else {
                        ++_sent;
                    }
                } else {
                    ++_errs;
                }
                if ( _producerDelayMS != 0 ) {
                    Utils.delay( _producerDelayMS );
                }
            }
        }

        public long sent() {
            return _sent;
        }
        
        public long getErrs() {
            return _errs;
        }
    }
    
    static void mkKey( ReusableString key, boolean isClient, int i ) {
        key.reset();
        
        key.append( (isClient) ? 'C' : 'M' );
        key.append( "SOMEKEY" );
        key.append( 1000000+i );
    }
    
    public void testThreaded() {
     
        doTestThreaded( 1000, 1, 0 );
        doTestThreaded( 5000000, 1, 0 );
        doTestThreaded( 1000, 1, 0 );
        doTestThreaded( 1000, 4, 10 );
        doTestThreaded( 100, 4, 100 );
        doTestThreaded( 100, 4, 100 );
        doTestThreaded( 100, 8, 100 );
        doTestThreaded( 50, 4, 500 );
        doTestThreaded( 50, 8, 500 );
        doTestThreaded( 200000, 1, 0 );
        doTestThreaded( 100000, 2, 0 );
        doTestThreaded( 100000, 8, 0 );
        doTestThreaded( 25000, 32, 0 );
    }

    private void doTestThreaded( int count, int threads, int prodDelayMS ) {

        int total = count * threads;

        _log.info( "\n===========================================================================\n" );
        
        _log.info( "\nCONCURRENT LINKED QUEUE tot=" + total );
        MessageQueue qDoug = new JavaConcMessageQueue();
        long dougQ = perf( "SUN", count, threads, total, qDoug, prodDelayMS );

        _log.info( "\nNEW LINKED QUEUE tot=" + total );
        ConcLinkedMsgQueueSingle q = new ConcLinkedMsgQueueSingle();
        long concQ = perf( "RRCAS", count, threads, total, q, prodDelayMS );

        _log.info( "\nNEW SYNC LINKED QUEUE tot=" + total );
        NonBlockingSyncQueue n = new NonBlockingSyncQueue();
        long multQ = perf( "RRSYNC", count, threads, total, n, prodDelayMS );

        _log.info( "\nNEW BLOCK LINKED QUEUE tot=" + total );
        BlockingSyncQueue b = new BlockingSyncQueue();
        long blockQ = perf( "RRBLOCK", count, threads, total, b, prodDelayMS );

        _log.info( "RRCASQ   delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + concQ / 1000 + " secs" );
        _log.info( "SUNQ     delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + dougQ / 1000 + " secs" );
        _log.info( "RRSYNC   delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + multQ / 1000 + " secs" );
        _log.info( "RRBLOCK  delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + blockQ / 1000 + " secs" );
    }

    private long perf( String comment, int count, int threads, int total, MessageQueue q, int producerDelayMS ) {
        ConsumerRunnable c = new ConsumerRunnable( total, q );
        
        Thread ct = new Thread( c, "Consumer" );

        Set<ProducerRunnable> producers = new LinkedHashSet<ProducerRunnable>();
        
        SuperPool<ClientNewOrderSingleImpl> nosSP = SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class );
        nosSP.init( 200, 1000, 50 );

        for( int i=0 ; i < threads ; i++ ) {

            ProducerRunnable r = new ProducerRunnable( i, count, q, producerDelayMS );

            producers.add( r );
        }

        Utils.invokeGC();
        
        long start = System.currentTimeMillis();
        
        int inst = 0;
        
        ct.start();

        for( ProducerRunnable prod : producers ) {

            Thread rt = new Thread( prod, "Thread" + (inst++) );
            rt.start();
        }

        // wait until all sent
        
        long totalSent;
        long errs;

        do {
            Utils.delay( 1000 );

            totalSent = count( producers );
            errs      = countErrs( producers );
            
            // _log.info( "Waited another sec .... produced =" + totalSent + " of expected " + total + ", errs=" + errs );

        } while ( totalSent + errs < total );
        
        assertEquals( 0, errs );
        assertEquals( total, totalSent );

        long consumed;
        long tmpConsumed;
        
        do {
            consumed = c.getConsumed();

            Utils.delay( 2000 );
            // two waits incase GC occurs during delay
            Utils.delay( 2000 );

            tmpConsumed = c.getConsumed();
            
            // _log.info( "Waited another sec .... before wait consumed=" + consumed + " of expected " + total + 
            //           ", after wait consumed=" + tmpConsumed );
            
        } while ( consumed != tmpConsumed );

        assertTrue( "Consumed " + c.getConsumed() + " of " + total + ", threads=" + threads, c.finished() );
        assertEquals( total, c.getConsumed() );
        
        long end = System.currentTimeMillis();

        int[] stats = c.getTimes();
        
        Percentiles p = new Percentiles( stats );
        
        System.out.println( "[" + comment + "]  NanoSecond stats " + " count=" + total + ", delay=" + producerDelayMS +
                            ", med=" + p.median() + 
                            ", ave=" + p.getAverage() + 
                            ", min=" + p.getMinimum() + 
                            ", max=" + p.getMaximum() + 
                            "\n                 " +
                            ", p99=" + p.calc( 99 ) + 
                            ", p95=" + p.calc( 95 ) + 
                            ", p90=" + p.calc( 90 ) + 
                            ", p80=" + p.calc( 80 ) + 
                            ", p70=" + p.calc( 70 ) + 
                            ", p50=" + p.calc( 50 ) + "\n" );
        
        nosSP.logStats();
        
        return end - start;
    }

    private long count( Set<ProducerRunnable> producers ) {
        long cnt = 0;
        
        for( ProducerRunnable pr : producers ){
        
            cnt += pr.sent();
        }
        
        return cnt;
    }

    private long countErrs( Set<ProducerRunnable> producers ) {
        long cnt = 0;
        
        for( ProducerRunnable pr : producers ){
        
            cnt += pr.getErrs();
        }
        
        return cnt;
    }
}
