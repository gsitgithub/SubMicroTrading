/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order.collections;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.om.warmup.FixTestUtils;

/**
 * slow producer due to creating decoder for every message
 * 
 * leave in as a good different test to the fast one
 *
 * @author Richard Rose
 */
public class ConcLinkedMsgQueueSingleSlowTest extends BaseTestCase {

    private static final Logger _log = LoggerFactory.console( ConcLinkedMsgQueueSingleSlowTest.class );
    
    private static class ConsumerRunnable implements Runnable {

        private static final long MAX_CONSUME_WAIT_MS = 1000;
        
        private int _tot;
        private ConcLinkedMsgQueueSingle _q;
        private volatile boolean _finished = false;
        private int _count = 0;
        private long _totQty = 0;
        private CountDownLatch _cdl;

        public ConsumerRunnable( int tot, ConcLinkedMsgQueueSingle q, CountDownLatch cdl ) {
            _tot = tot;
            _q = q;
            _cdl = cdl;
        }

        @Override
        public void run() {
            long last = System.currentTimeMillis();
            long now;
            
            for ( int i=0 ; i < _tot ; ++i ) {
                Message m = null;
                
                do {
                    m = _q.poll();
            
                    now = System.currentTimeMillis();
                    
                    if ( now - last > MAX_CONSUME_WAIT_MS ) {
                        _finished = true;
                    }
                    
                } while( m == null && !_finished );
                
                if ( m != null ) {

                    _cdl.countDown();
                    ++_count;

                    ClientNewOrderSingleImpl nos = (ClientNewOrderSingleImpl) m;
                    
                    _totQty += nos.getOrderQty();
                }
                
                last = now; 
            }
            
            _finished = true;
        }

        public boolean finished() {
            return _finished ;
        }

        public int getConsumed() {
            return _count;
        }

        public long getTotalQty() {
            return _totQty;
        }
    }
    
    private static class ProducerRunnable implements Runnable {

        private ConcLinkedMsgQueueSingle _q;
        private int _count;
        private int _producerIdx;
        private int _errs = 0;
        private int _sent;

        public ProducerRunnable( int producerIdx, 
                                 int count, 
                                 ConcLinkedMsgQueueSingle q ) {
            _q = q;
            _count = count;
            _sent = 0;
            _producerIdx = producerIdx;
        }

        @Override
        public void run() {
            int baseQty = _producerIdx * _count;

            Standard44Decoder decoder = FixTestUtils.getDecoder44();
            
            for ( int i=0 ; i < _count ; ++i ) {
                Message msg = FixTestUtils.getClientNOS( decoder, mkKey( true, i ) + _producerIdx, baseQty + i, 1 );
                if ( msg != null ) {
                    if ( _q.add( msg ) == false ) {
                        ++_errs;
                    } else {
                        ++_sent;
                    }
                } else {
                    ++_errs;
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
    
    public void testAddOneElem() {
        
        Standard44Decoder decoder = FixTestUtils.getDecoder44();

        ConcLinkedMsgQueueSingle q = new ConcLinkedMsgQueueSingle();
        
        Message msg1 = FixTestUtils.getClientNOS( decoder, mkKey( true, 1 ), 1, 1 ); 
            
        q.add( msg1 );
        
        assertEquals( 1, q.size() );
        
        Message pop1 = q.poll();
     
        assertSame( msg1, pop1 );
        assertEquals( 0, q.size() );

        assertNull( q.poll() );
        assertNull( q.poll() );
        
        Message msg2 = FixTestUtils.getClientNOS( decoder, mkKey( true, 2 ), 2, 2 ); 
        
        q.add( msg2 );
        
        assertEquals( 1, q.size() );
        
        Message pop2 = q.poll();
        
        assertEquals( 0, q.size() );
        
        String ckey = mkKey( true, 2 );

        assertSame( ClientNewOrderSingleImpl.class, pop2.getClass() );

        ClientNewOrderSingleImpl cnos = (ClientNewOrderSingleImpl) pop2;
        
        assertEquals( ckey, cnos.getClOrdId().toString() );
    }
    
    public void testAdd() {
        
        Standard44Decoder decoder = FixTestUtils.getDecoder44();
        
        ConcLinkedMsgQueueSingle q = new ConcLinkedMsgQueueSingle();
        
        int numOrders = 1000;
        
        ZString mktOrdId = new ViewString( "ORDID" );
        ZString execId   = new ViewString( "EXEID" );
        
        for ( int i=0 ; i < numOrders ; ++i ) {
            Message msg = FixTestUtils.getClientNOS( decoder, mkKey( true, i ), i, i ); 
            Message ack = FixTestUtils.getMarketACK( decoder, mkKey( false, i ), i, i, mktOrdId, execId ); 
            
            q.add( msg );
            q.add( ack );
        }
        
        assertEquals( numOrders*2, q.size() );

        for ( int i=0 ; i < numOrders ; ++i ) {
            String ckey = mkKey( true, i );
            String mkey = mkKey( false, i );

            Message nos = q.poll();
            Message ack = q.poll();
            
            assertSame( ClientNewOrderSingleImpl.class, nos.getClass() );
            assertSame( MarketNewOrderAckImpl.class,    ack.getClass() );

            ClientNewOrderSingleImpl cnos = (ClientNewOrderSingleImpl) nos;
            MarketNewOrderAckImpl    mack = (MarketNewOrderAckImpl)    ack;
            
            assertEquals( ckey, cnos.getClOrdId().toString() );
            assertEquals( mkey, mack.getClOrdId().toString() );
            assertEquals( i, cnos.getOrderQty() );
        }
        
        assertNull( q.poll() );
    }
    
    static String mkKey( boolean isClient, int i ) {
        return ((isClient) ? "C" : "M") + "SOMEKEY" + (1000000+i);
    }
    
    public void testThreaded() {
     
//        doTestThreaded( 3, 1 );
        doTestThreaded( 500, 1 );
        doTestThreaded( 1000, 1 );
        doTestThreaded( 100000, 2 );
        doTestThreaded( 100000, 8 );
    }

    private void doTestThreaded( int count, int threads ) {

        int total = count * threads;
        
        ConcLinkedMsgQueueSingle q = new ConcLinkedMsgQueueSingle();

        CountDownLatch cdl = new CountDownLatch( total );
        
        ConsumerRunnable c = new ConsumerRunnable( total, q,  cdl );
        
        Thread ct = new Thread( c, "Consumer" );

        Set<ProducerRunnable> producers = new LinkedHashSet<ProducerRunnable>();
        
        for( int i=0 ; i < threads ; i++ ) {

            ProducerRunnable r = new ProducerRunnable( i, count, q );

            producers.add( r );
            
            Thread rt = new Thread( r, "Thread" + i );
            rt.start();
        }

        ct.start();

        // wait until all sent
        
        long totalSent;
        long errs;

        do {
            Utils.delay( 1000 );

            totalSent = count( producers );
            errs      = countErrs( producers );
            
            _log.info( "Waited another sec .... produced =" + totalSent + " of expected " + total + ", errs=" + errs );

        } while ( totalSent + errs < total );
        
        assertEquals( 0, errs );
        assertEquals( total, totalSent );

        long consumed;
        long tmpConsumed;
        
        do {
            consumed = c.getConsumed();

            Utils.delay( 5000 );

            tmpConsumed = c.getConsumed();
            
            _log.info( "Waited another sec .... before wait consumed=" + consumed + " of expected " + total + 
                       ", after wait consumed=" + tmpConsumed );
            
        } while ( consumed != tmpConsumed );

        try {
            cdl.await(30, TimeUnit.SECONDS);
        } catch( InterruptedException e ) {
            // ignore
        }
        
        assertTrue( "Consumed " + c.getConsumed() + " of " + total + ", threads=" + threads, c.finished() );
        assertEquals( total, c.getConsumed() );
        
        long expectedQty = 0;

        for( int i=0 ; i < threads ; i++ ) {
            int baseQty = i * count;
            for ( int j=0 ; j < count ; ++j ) {
                int qty = baseQty + j;
                expectedQty += qty;
            }
        }
        
        assertEquals( expectedQty, c.getTotalQty() );
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
