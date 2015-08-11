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
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
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

public abstract class BaseQueueTst extends BaseTestCase {

    private static final Logger _log = LoggerFactory.create( BaseQueueTst.class );
    
    protected Standard44Decoder _decoder        = FixTestUtils.getDecoder44();
    
    private static class ConsumerRunnable implements Runnable {

        private int _tot;
        private MessageQueue _q;
        private volatile boolean _finished = false;
        private int _count = 0;
        private long _totQty = 0;
        private CountDownLatch _cdl;
        private CyclicBarrier _cb;

        public ConsumerRunnable( int tot, MessageQueue q, CountDownLatch cdl, CyclicBarrier cb ) {
            _tot = tot;
            _q = q;
            _cdl = cdl;
            _cb = cb;
        }

        @Override
        public void run() {
            
            try {
                _cb.await();
            } catch( Exception e ) {
                // dont care
            }
            
            while( !_finished && _count < _tot ) {
                Message m = null;
                
                do {
                    m = _q.poll();
            
                } while( m == null && !_finished );
                
                if ( m != null ) {

                    do {
                        ++_count;
                        _cdl.countDown();
    
                        ClientNewOrderSingleImpl nos = (ClientNewOrderSingleImpl) m;
                        
                        _totQty += nos.getOrderQty();
                        
                        m = m.getNextQueueEntry();
                        
                    } while ( m != null );
                }
            }
            
            _finished = true;
        }

        public boolean finished() {
            return _finished ;
        }

        public void setFinished() {
            _finished  = true;
        }

        public int getConsumed() {
            return _count;
        }

        public long getTotalQty() {
            return _totQty;
        }
    }
    
    private static class ProducerRunnable implements Runnable {

        private MessageQueue _q;
        private int _count;
        private int _producerIdx;
        private int _errs = 0;
        private int _sent;
        private CyclicBarrier _cb;

        public ProducerRunnable( int producerIdx, 
                                 int count, 
                                 MessageQueue q, 
                                 CyclicBarrier cb ) {
            _q = q;
            _count = count;
            _sent = 0;
            _producerIdx = producerIdx;
            _cb = cb;
        }

        @Override
        public void run() {
            
            try {
                _cb.await();
            } catch( Exception e ) {
                // dont care
            }
            
            int baseQty = _producerIdx * _count;
            
            Standard44Decoder decoder = FixTestUtils.getDecoder44();

            ReusableString key = new ReusableString(20);
            ReusableString buffer = new ReusableString(256);

            for ( int i=0 ; i < _count ; ++i ) {
                decoder.setReceived( Utils.nanoTime() );
                mkKey( key, true, i );
                key.append( _producerIdx );
                Message msg = FixTestUtils.getClientNOS( buffer, decoder, key, baseQty + i, 1, null );
                if ( msg != null ) {
                    if ( _q.add( msg ) == false ) {
                        ++_errs;
                    } else {
                        ++_sent;
                    }
                } else {
                    ++_errs;
                }
                Thread.yield();
            }
        }

        public long sent() {
            return _sent;
        }
        
        public long getErrs() {
            return _errs;
        }
    }
    
    protected abstract MessageQueue getNewQueue( int minSize );

    static void mkKey( ReusableString key, boolean isClient, int i ) {
        key.reset();
        
        key.append( (isClient) ? 'C' : 'M' );
        key.append( "SOMEKEY" );
        key.append( 1000000+i );
    }
    
    public void testAddOneElem() {
        
        MessageQueue q = getNewQueue(1);
        
        Message msg1 = FixTestUtils.getClientNOS( _decoder, mkKey( true, 1 ), 1, 1 ); 
            
        q.add( msg1 );
        
        assertEquals( 1, q.size() );
        
        Message pop1 = q.poll();
     
        assertSame( msg1, pop1 );
        assertEquals( 0, q.size() );

        assertNull( q.poll() );
        assertNull( q.poll() );
        
        Message msg2 = FixTestUtils.getClientNOS( _decoder, mkKey( true, 2 ), 2, 2 ); 
        
        q.add( msg2 );
        
        assertEquals( 1, q.size() );
        
        Message pop2 = q.poll();
        
        assertEquals( 0, q.size() );
        
        String ckey = mkKey( true, 2 );

        assertSame( ClientNewOrderSingleImpl.class, pop2.getClass() );

        ClientNewOrderSingleImpl cnos = (ClientNewOrderSingleImpl) pop2;
        
        assertEquals( ckey, cnos.getClOrdId().toString() );
    }
    
    public void testAddTwoElem() {
        
        MessageQueue q = getNewQueue(2);
        
        Message msg1 = FixTestUtils.getClientNOS( _decoder, mkKey( true, 1 ), 1, 1 ); 
        Message msg2 = FixTestUtils.getClientNOS( _decoder, mkKey( true, 2 ), 2, 2 ); 
            
        q.add( msg1 );
        q.add( msg2 );
        
        assertEquals( 2, q.size() );
        
        Message pop1 = q.poll();
     
        assertSame( msg1, pop1 );
        assertEquals( 1, q.size() );

        Message pop2 = q.poll();
        
        assertSame( msg2, pop2 );
        assertEquals( 0, q.size() );

        assertNull( q.poll() );
        assertNull( q.poll() );
        
        Message msg3 = FixTestUtils.getClientNOS( _decoder, mkKey( true, 3 ), 3, 3 ); 
        
        q.add( msg3 );
        
        assertEquals( 1, q.size() );
        
        Message pop3 = q.poll();
        
        assertEquals( 0, q.size() );
        
        String ckey = mkKey( true, 3 );

        assertSame( ClientNewOrderSingleImpl.class, pop2.getClass() );

        ClientNewOrderSingleImpl cnos = (ClientNewOrderSingleImpl) pop3;
        
        assertEquals( ckey, cnos.getClOrdId().toString() );
    }
    
    public void testAdd() {
        
        int numOrders = 1000;
        
        MessageQueue q = getNewQueue( numOrders * 2 );
        
        ZString mktOrdId = new ViewString( "ORDID" );
        ZString execId   = new ViewString( "EXEID" );
        
        Standard44Decoder decoder = FixTestUtils.getDecoder44();
        
        for ( int i=0 ; i < numOrders ; ++i ) {
            Message msg = FixTestUtils.getClientNOS( _decoder, mkKey( true, i ), i, i ); 
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
    
    public void testThreaded3_1() {
        doTestThreaded( 3, 1 );
    }

    public void testThreaded1000_1() {
        doTestThreaded( 1000, 1 );
    }

    public void testThreaded2000_2() {
        doTestThreaded( 2000, 2 );
    }

    public void testThreaded1000_8() {
        doTestThreaded( 1000, 8 );
    }

    public void testThreaded2500_32() {
        doTestThreaded( 2500, 32 );
    }

    protected void doTestThreaded( int count, int threads ) {

        int total = count * threads;
        
        MessageQueue q = getNewQueue( total );

        CyclicBarrier cb = new CyclicBarrier(threads + 1);
        
        CountDownLatch cdl = new CountDownLatch(total);
        
        ConsumerRunnable c = new ConsumerRunnable( total, q, cdl, cb );
        
        Thread ct = new Thread( c, "Consumer" );

        Set<ProducerRunnable> producers = new LinkedHashSet<ProducerRunnable>();
        
        for( int i=0 ; i < threads ; i++ ) {

            ProducerRunnable r = new ProducerRunnable( i, count, q, cb );

            producers.add( r );
            
            Thread rt = new Thread( r, "Producer" + i );
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
        
        try {
            cdl.await( 1, TimeUnit.MINUTES );
        } catch( InterruptedException e ) {
            // ignore
        }
        
        do {
            consumed = c.getConsumed();

            Utils.delay( 1000 );

            tmpConsumed = c.getConsumed();
            
            _log.info( "Waited another sec .... before wait consumed=" + consumed + " of expected " + total + 
                       ", after wait consumed=" + tmpConsumed );
            
        } while ( consumed != tmpConsumed );

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
        
        c.setFinished();
    }

    public void testMixedThreadedA() {
        doTestMixedThreaded( 3, 1 );
    }

    public void testMixedThreadedB() {
        doTestMixedThreaded( 1000, 1 );
    }

    public void testMixedThreadedC() {
        doTestMixedThreaded( 1000, 2 );
    }

    public void testMixedThreadedD() {
        doTestMixedThreaded( 1000, 8 );
    }

    public void testMixedThreadedE() {
        doTestMixedThreaded( 2500, 32 );
    }

    protected void doTestMixedThreaded( int count, int threads ) {

        int total = count * threads;
        
        MessageQueue q = getNewQueue( count / 4 + 1024 );

        CountDownLatch cdl = new CountDownLatch(total);
        
        CyclicBarrier cb = new CyclicBarrier(threads+1);

        ConsumerRunnable c = new ConsumerRunnable( total, q, cdl, cb );
        
        Thread ct = new Thread( c, "Consumer" );

        Set<ProducerRunnable> producers = new LinkedHashSet<ProducerRunnable>();
        
        for( int i=0 ; i < threads ; i++ ) {

            ProducerRunnable r = new ProducerRunnable( i, count, q,  cb );

            producers.add( r );
            
            Thread rt = new Thread( r, "Producer" + i );
            rt.start();
        }

        ct.start();

        long consumed;
        long tmpConsumed;
        
        do {
            consumed = c.getConsumed();

            Utils.delay( 1000 );

            tmpConsumed = c.getConsumed();
            
            _log.info( "Waited another sec .... before wait consumed=" + consumed + " of expected " + total + 
                       ", after wait consumed=" + tmpConsumed + ", latchOutstanding=" + cdl.getCount() );
            
        } while ( consumed != total );

        try {
            cdl.await( 30, TimeUnit.SECONDS );
        } catch( InterruptedException e ) {
            // ignore
        }
        
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
        
        c.setFinished();
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
