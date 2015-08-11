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
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.dispatch.ThreadedDispatcher;
import com.rr.core.dummy.warmup.DummyMessageHandler;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ModelVersion;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Percentiles;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.om.events.perf.roundtrip.ClientSessionEmulator;
import com.rr.om.events.perf.roundtrip.ExchangeSessionEmulator;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.processor.EventProcessorImpl;
import com.rr.om.registry.SimpleTradeRegistry;
import com.rr.om.router.OrderRouter;
import com.rr.om.router.SingleDestRouter;
import com.rr.om.validate.EmeaDmaValidator;
import com.rr.om.validate.EventValidator;

public class PerfTestRoundTripMultiThreaded extends BaseTestCase {

    private static final Logger _log = LoggerFactory.create( PerfTestRoundTripMultiThreaded.class );
    
    public void testThreaded() throws InstantiationException, IllegalAccessException {
     
        doTestThreaded(   1000,  1,   10,  5 );
        Utils.delay( 1000 );
        doTestThreaded(  10000,  1,    2,  1 );

        //        doTestAckDelay( 0 );
//        doTestAckDelay( 1 );
//        doTestAckDelay( 4 );
//        doTestAckDelay( 10 );
        
//        doTestThreaded(    1000,  1,   0, ackDelay );
//        doTestThreaded(    1000,  4,  10, ackDelay );
//        doTestThreaded(     100,  4, 100, ackDelay );
//        doTestThreaded(     100,  4, 100, ackDelay );
//        doTestThreaded(     100,  8, 100, ackDelay );
//        doTestThreaded(      50,  4, 500, ackDelay );
//        doTestThreaded(      50,  8, 500, ackDelay );
//        doTestThreaded( 5000000,  1,   0, ackDelay  );
//        doTestThreaded(  200000,  1,   0, ackDelay  );
//        doTestThreaded(  100000,  2,   0, ackDelay  );
//        doTestThreaded(  100000,  8,   0, ackDelay  );
//        doTestThreaded(   25000, 32,   0, ackDelay  );
    }

    private void doTestThreaded( int count, int threads, int prodDelayMS, int ackDelayMS ) throws InstantiationException, IllegalAccessException {

        if ( Runtime.getRuntime().availableProcessors() > 3 ) {
            int total = count * threads;

            _log.info( "\n===========================================================================\n" );
            
            _log.info( "\nCONCURRENT LINKED QUEUE tot=" + total );
            long dougQ = perf( "SUN", count, threads, total, JavaConcMessageQueue.class, ackDelayMS, prodDelayMS );

            _log.info( "\nNEW LINKED QUEUE tot=" + total );
            long concQ = perf( "RRCAS", count, threads, total, ConcLinkedMsgQueueSingle.class, ackDelayMS, prodDelayMS );

            _log.info( "\nNEW SYNC LINKED QUEUE tot=" + total );
            long multQ = perf( "RRSYNC", count, threads, total, NonBlockingSyncQueue.class, ackDelayMS, prodDelayMS );

            _log.info( "\nNEW BLOCK LINKED QUEUE tot=" + total );
            long blockQ = perf( "RRBLOCK", count, threads, total, BlockingSyncQueue.class, ackDelayMS, prodDelayMS );

            _log.info( "RRCASQ   delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + concQ / 1000 + " secs" );
            _log.info( "SUNQ     delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + dougQ / 1000 + " secs" );
            _log.info( "RRSYNC   delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + multQ / 1000 + " secs" );
            _log.info( "RRBLOCK  delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + blockQ / 1000 + " secs" );
            
        } else {
            int total = count * threads;

            _log.info( "\n===========================================================================\n" );
            
            _log.info( "\nNEW BLOCK LINKED QUEUE tot=" + total );
            long blockQ = perf( "RRBLOCK", count, threads, total, BlockingSyncQueue.class, ackDelayMS, prodDelayMS );

            _log.info( "RRBLOCK  delay=" + prodDelayMS + ", totCnt=" + total + ", over threads=" + threads + ", time=" + blockQ / 1000 + " secs" );
        }
    }

    private long perf( String                        comment, 
                       int                           count, 
                       int                           threads, 
                       int                           total, 
                       Class<? extends MessageQueue> classQ, 
                       int                           ackDelayMS,
                       int                           producerDelayMS ) throws InstantiationException, IllegalAccessException {
        
        MessageQueue exSessQ = classQ.newInstance();
        ExchangeSessionEmulator exSess = new ExchangeSessionEmulator( count, exSessQ, ackDelayMS );
        
        ModelVersion    version       = new ModelVersion( (byte)'1', (byte)'0' );
        EventValidator  validator     = new EmeaDmaValidator( Integer.MAX_VALUE );
        EventBuilder    builder       = new EventBuilderImpl();
        OrderRouter router        = new SingleDestRouter( exSess );
        
        MessageDispatcher dispatcher = new ThreadedDispatcher( "PROC1", classQ.newInstance(), ThreadPriority.Processor );
        
        // if test more than 1024 fills then change presize of trade registry
        EventProcessorImpl proc  = new EventProcessorImpl( version, 
                                                           count*2, 
                                                           validator, 
                                                           builder, 
                                                           dispatcher, 
                                                           new DummyMessageHandler(),
                                                           new SimpleTradeRegistry(1024) );
        
        try {
            return doPerfRun( comment, count, threads, total, classQ, producerDelayMS, exSess, router, dispatcher, proc );
        } finally {
            exSess.finished();
            proc.stop();
            dispatcher.setStopping();
        }
    }

    private long doPerfRun( String                          comment, 
                            int                             count, 
                            int                             threads, 
                            int                             total, 
                            Class<? extends MessageQueue>   classQ, 
                            int                             producerDelayMS,
                            ExchangeSessionEmulator         exSess, 
                            OrderRouter                 router, 
                            MessageDispatcher               dispatcher, 
                            EventProcessorImpl              proc ) throws InstantiationException, IllegalAccessException {
        proc.setProcessorRouter( router );
        exSess.setProcessor( proc );
        
        dispatcher.start();
        
        Set<ClientSessionEmulator> producers = new LinkedHashSet<ClientSessionEmulator>();

        presizePool( count );

        for( int i=0 ; i < threads ; i++ ) {
            ClientSessionEmulator r = new ClientSessionEmulator( classQ.newInstance(), i, count, proc, producerDelayMS );

            producers.add( r );
        }

        Utils.invokeGC();
        
        long start = System.currentTimeMillis();
        
        for( ClientSessionEmulator prod : producers ) {
            prod.start();
        }

        // wait until all sent
        
        long totalSent;
        long consumed;
        long errs;

        do {
            Utils.delay( 1000 );

            totalSent = countSent( producers );
            errs      = countErrs( producers );
            
            // _log.info( "Waited another sec .... produced =" + totalSent + " of expected " + total + ", errs=" + errs );

        } while ( totalSent + errs < total );
        
        assertEquals( 0, errs );
        assertEquals( total, totalSent );

        long tmpConsumed;
        
        do {
            consumed = countConsumed( producers );

            Utils.delay( 2000 );
            // two waits incase GC occurs during delay
            Utils.delay( 2000 );

            tmpConsumed = countConsumed( producers );
            
            // _log.info( "Waited another sec .... before wait consumed=" + consumed + " of expected " + total + 
            //           ", after wait consumed=" + tmpConsumed );
            
        } while ( consumed != tmpConsumed );

        assertEquals( total, consumed );
        
        long end = System.currentTimeMillis();

        for( ClientSessionEmulator pr : producers ){
            
            String consumer = pr.getComponentId();
            
            int[] stats = pr.getRoundTripTimes();
            
            Percentiles p = new Percentiles( stats );
            
            System.out.println( "[" + comment + ":" + consumer + "]  ROUND TRIP NanoSecond stats " + " count=" + total + ", delay=" + producerDelayMS +
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
        }
        
        logPoolStats();
        
        return end - start;
    }

    private void logPoolStats() {
        SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class ).logStats();
        SuperpoolManager.instance().getSuperPool( MarketNewOrderSingleImpl.class ).logStats();
        SuperpoolManager.instance().getSuperPool( MarketNewOrderAckImpl.class ).logStats();
        SuperpoolManager.instance().getSuperPool( ClientNewOrderAckImpl.class ).logStats();
    }

    private void presizePool( int count ) {
        int chainSize = 100;
        int numChains = (count / chainSize) + 1; 
        int extra     = 50;
        
        SuperpoolManager.instance().getSuperPool( OrderImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( OrderVersion.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( MarketNewOrderSingleImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( ClientNewOrderAckImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( MarketNewOrderAckImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( ReusableString.class ).init( numChains, chainSize, extra );
    }

    private long countSent( Set<ClientSessionEmulator> producers ) {
        long cnt = 0;
        
        for( ClientSessionEmulator pr : producers ){
        
            cnt += pr.sent();
        }
        
        return cnt;
    }

    private long countConsumed( Set<ClientSessionEmulator> producers ) {
        long cnt = 0;
        
        for( ClientSessionEmulator pr : producers ){
        
            cnt += pr.getConsumed();
        }
        
        return cnt;
    }

    private long countErrs( Set<ClientSessionEmulator> producers ) {
        long cnt = 0;
        
        for( ClientSessionEmulator pr : producers ){
        
            cnt += pr.getErrs();
        }
        
        return cnt;
    }
}
