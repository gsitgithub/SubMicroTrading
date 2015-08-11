/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Percentiles;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.recycle.ClientNewOrderAckRecycler;
import com.rr.model.generated.internal.events.recycle.MarketNewOrderSingleRecycler;
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.order.collections.HashEntry;
import com.rr.om.processor.BaseProcessorTestCase;
import com.rr.om.processor.EventProcessorImpl;
import com.rr.om.warmup.FixTestUtils;

public class PerfTestRoundTrip extends BaseProcessorTestCase {

    private int[] _roundTrip;
    private int[] _toMKt;

    private ViewString _baseOrderId = new ReusableString( "ORDID" );
    private ViewString _baseExecId  = new ReusableString( "EXECID" );

    private EventProcessorImpl _localProc;

    public void testAckWithUniqueExecId() {
        
        doRun( 20000, 0, 0 );
        doRunNoStats( 20000 );
        
        doRun( 10000, 0, 0 );
        doRunNoStats( 10000 );

        doRun( 10000, 0, 0 );

//        doRun( 100, 2, 5 );
//        doRun( 1000, 2, 5 );
//        doRun( 10000, 0, 1 );
    }

    private void doRun( int total, int genDelayMS, int ackDelayMS ) {
        presizePool( total );
        
        _roundTrip = new int[ total ];
        _toMKt     = new int[ total ];
        
        _localProc = getProcesssor( total, false );
        
        Standard44Decoder decoder = FixTestUtils.getDecoder44();
        ReusableString decodeBuffer = new ReusableString(250);

        ReusableString key = new ReusableString(20);

        SuperpoolManager spm = SuperpoolManager.instance();
        MarketNewOrderSingleRecycler nosRecycler = spm.getRecycler( MarketNewOrderSingleRecycler.class, MarketNewOrderSingleImpl.class );
        ClientNewOrderAckRecycler    ackRecycler = spm.getRecycler( ClientNewOrderAckRecycler.class,    ClientNewOrderAckImpl.class );

        MarketNewOrderAckImpl    mack;
        
        ReusableString mkDecBuf  = new ReusableString(250);
        ReusableString orderId = new ReusableString( SizeConstants.DEFAULT_MARKETORDERID_LENGTH );
        ReusableString execId  = new ReusableString( SizeConstants.DEFAULT_EXECID_LENGTH );

        final byte[] bufMKt    = new byte[250];
        final byte[] bufClient = new byte[250];
        Standard44Encoder mktEncoder    = new Standard44Encoder( (byte)'4', (byte)'4', bufMKt );
        Standard44Encoder clientEncoder = new Standard44Encoder( (byte)'4', (byte)'4', bufClient );
        
        clientEncoder.setNanoStats( false );
        mktEncoder.setNanoStats( false );
        
        Utils.invokeGC();
        
        for ( int i=0 ; i < total ; ++i ) {

            if ( genDelayMS > 0 ) {
                Utils.delay( genDelayMS );
            }
            
            decoder.setReceived( Utils.nanoTime() );
            mkKey( key, true, i );

            ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( decodeBuffer, decoder, key, 1, 1, _upMsgHandler );
            _localProc.handle( cnos );
            MarketNewOrderSingleImpl mnos = (MarketNewOrderSingleImpl) _downQ.poll();
        
            mktEncoder.encode( mnos );                   
            long nowNano = Utils.nanoTime();
            mnos.getSrcEvent().setOrderSent( nowNano );
            
            int delay = (int) (nowNano - mnos.getOrderReceived());
            _toMKt[i] = delay;
            
            orderId.copy( _baseOrderId );
            orderId.append( i );
            execId.copy( _baseExecId );
            execId.append( i );
            
            if ( ackDelayMS > 0 ) {
                Utils.delay( ackDelayMS );
            }
            
            clearQueues();
            
            mack = FixTestUtils.getMarketACK( mkDecBuf, decoder, mnos.getClOrdId(), mnos.getOrderQty(),  mnos.getPrice(), orderId, execId ); 
            nosRecycler.recycle( mnos );

            _localProc.handle( mack );
            ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) _upQ.poll();
        
            clientEncoder.encode( cack );                   
            nowNano = Utils.nanoTime();  
            long nosLat  = cack.getOrderSent() - cack.getOrderReceived();
            long ackLat  = nowNano            - cack.getAckReceived();
            
            _roundTrip[i] = (int) (nosLat + ackLat);
            
            ackRecycler.recycle( cack );
            
            clearQueues();
        }
        
        logStats( "TO_MARKET", _toMKt,     total, genDelayMS, ackDelayMS );
        logStats( "ROUNDTRIP", _roundTrip, total, genDelayMS, ackDelayMS );
        
        logPoolStats();
    }

    private void doRunNoStats( int total ) {
        presizePool( total );
        
        _localProc = getProcesssor( total, false );
        
        Standard44Decoder decoder = FixTestUtils.getDecoder44();
        ReusableString decodeBuffer = new ReusableString(250);

        ReusableString key = new ReusableString(20);

        SuperpoolManager spm = SuperpoolManager.instance();
        MarketNewOrderSingleRecycler nosRecycler = spm.getRecycler( MarketNewOrderSingleRecycler.class, MarketNewOrderSingleImpl.class );
        ClientNewOrderAckRecycler    ackRecycler = spm.getRecycler( ClientNewOrderAckRecycler.class,    ClientNewOrderAckImpl.class );

        MarketNewOrderAckImpl    mack;
        
        ReusableString mkDecBuf  = new ReusableString(250);
        ReusableString orderId = new ReusableString( SizeConstants.DEFAULT_MARKETORDERID_LENGTH );
        ReusableString execId  = new ReusableString( SizeConstants.DEFAULT_EXECID_LENGTH );

        final byte[] bufMKt    = new byte[250];
        final byte[] bufClient = new byte[250];
        Standard44Encoder mktEncoder    = new Standard44Encoder( (byte)'4', (byte)'4', bufMKt );
        Standard44Encoder clientEncoder = new Standard44Encoder( (byte)'4', (byte)'4', bufClient );
        
        clientEncoder.setNanoStats( false );
        mktEncoder.setNanoStats( false );
        
        Utils.invokeGC();

        long start = Utils.nanoTime();
        
        for ( int i=0 ; i < total ; ++i ) {

            decoder.setReceived( Utils.nanoTime() );
            mkKey( key, true, i );

            ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( decodeBuffer, decoder, key, 1, 1, _upMsgHandler );
            _localProc.handle( cnos );
            MarketNewOrderSingleImpl mnos = (MarketNewOrderSingleImpl) _downQ.poll();
        
            mktEncoder.encode( mnos );                   
            
            orderId.copy( _baseOrderId );
            orderId.append( i );
            execId.copy( _baseExecId );
            execId.append( i );
            
            clearQueues();
            
            mack = FixTestUtils.getMarketACK( mkDecBuf, decoder, mnos.getClOrdId(), mnos.getOrderQty(),  mnos.getPrice(), orderId, execId ); 
            nosRecycler.recycle( mnos );

            _localProc.handle( mack );
            ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) _upQ.poll();
        
            clientEncoder.encode( cack );                   
            
            ackRecycler.recycle( cack );
            
            clearQueues();
        }

        long end = Utils.nanoTime();
        
        System.out.println( "NO STATS, TOTAL TIME=" + (end - start) + ", ave=" + (end-start) / total );
        
        logPoolStats();
    }

    private void logStats( String comment, int[] stats, long total, long genDelayMS, long ackDelayMS ) {
        Percentiles p = new Percentiles( stats );

        System.out.println( "[" + comment + "]  ROUND TRIP NanoSecond stats " + " count=" + total + ", genDelayMS=" + genDelayMS +
                            ", acckDelayMS=" + ackDelayMS +
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

    private void logPoolStats() {
        SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class ).logStats();
        SuperpoolManager.instance().getSuperPool( MarketNewOrderSingleImpl.class ).logStats();
        SuperpoolManager.instance().getSuperPool( MarketNewOrderAckImpl.class ).logStats();
        SuperpoolManager.instance().getSuperPool( ClientNewOrderAckImpl.class ).logStats();
        SuperpoolManager.instance().getSuperPool( ReusableString.class ).logStats();
        SuperpoolManager.instance().getSuperPool( OrderImpl.class ).logStats();
        SuperpoolManager.instance().getSuperPool( OrderVersion.class ).logStats();
    }
        

    private static void mkKey( ReusableString key, boolean isClient, int i ) {
        key.reset();
        
        key.append( (isClient) ? 'C' : 'M' );
        key.append( "SOMEKEY" );
        key.append( 1000000+i );
    }
           
    private void presizePool( int count ) {
        int chainSize = 100;
        int numChains = (count / chainSize) + 3; 
        int extra     = 50;

        if ( numChains < 15 ) numChains = 15;
        
        SuperpoolManager.instance().getSuperPool( HashEntry.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( OrderImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( OrderVersion.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( MarketNewOrderSingleImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( ClientNewOrderAckImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( MarketNewOrderAckImpl.class ).init( numChains, chainSize, extra );
        SuperpoolManager.instance().getSuperPool( ReusableString.class ).init( numChains, chainSize, extra );
    }
}
