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
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
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

public class PerfTestOrderMapSingleThread extends BaseProcessorTestCase {

    private int[] _roundTrip;
    private int[] _toMKt;
    private int[] _omNOS; 
    private int[] _omACK;

    private ViewString _baseOrderId = new ReusableString( "ORDID" );
    private ViewString _baseExecId  = new ReusableString( "EXECID" );

    private EventProcessorImpl _localProc;

    public static void main( String[] arg ) throws Exception {
        int count = Integer.parseInt( arg[0] );
        
        PerfTestOrderMapSingleThread t = new PerfTestOrderMapSingleThread();
        
        ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.Processor );
        
        for( int i=0 ; i < 3 ; i++ ) {
            System.out.println( "TEST RoundTrip with OM " + i + " ====================================================================" );
            t.setUp();
            t.doRunOrderMap( count, 0, 0 );
            t.tearDown();
        }
    }
    
    public void testAckWithUniqueExecId() {
        
        doRunOrderMap( 100, 2, 5 );
        doRunOrderMap( 10000, 0, 0 );
        doRunOrderMap( 10000, 0, 1 );
    }

    private void doRunOrderMap( int total, int genDelayMS, int ackDelayMS ) {
        presizePool( total );
        
        doTest( "HASHMAP", total, genDelayMS, ackDelayMS, getDirectProcesssor( total, false ) );
    }

    private void doTest( String desc, int total, int genDelayMS, int ackDelayMS, EventProcessorImpl proc ) {
        _roundTrip = new int[ total ];
        _toMKt     = new int[ total ];
        _omNOS     = new int[ total ];
        _omACK     = new int[ total ];
        
        _localProc = proc;
        
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
        
        SuperpoolManager.instance().resetPoolStats();

        Utils.invokeGC();
        
        long preNOS, postNOS;
        long preACK, postACK;
        
        for ( int i=0 ; i < total ; ++i ) {

            if ( genDelayMS > 0 ) {
                Utils.delay( genDelayMS );
            }
            
            decoder.setReceived( Utils.nanoTime() );
            mkKey( key, true, i );

            ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( decodeBuffer, decoder, key, 1, 1, _upMsgHandler );
            preNOS = Utils.nanoTime();
            _localProc.handle( cnos );
            postNOS = Utils.nanoTime();
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

            preACK = Utils.nanoTime();
            _localProc.handle( mack );
            postACK = Utils.nanoTime();
            ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) _upQ.poll();
        
            clientEncoder.encode( cack );                   
            nowNano = Utils.nanoTime();
            
            long nosLat     = cack.getOrderSent() - cack.getOrderReceived();
            long ackLat     = nowNano             - cack.getAckReceived();
            long nosProcLat = postNOS             - preNOS;
            long ackProcLat = postACK             - preACK;
            
            _roundTrip[i] = (int) (nosLat + ackLat);
            _omNOS[i] = (int)nosProcLat;
            _omACK[i] = (int)ackProcLat;
            
            ackRecycler.recycle( cack );
            
            clearQueues();
        }
        
        logStats( desc + ": TO_MARKET", _toMKt,     total, genDelayMS, ackDelayMS );
        logStats( desc + ": OM_NOS",    _omNOS,     total, genDelayMS, ackDelayMS );
        logStats( desc + ": OM_ACK",    _omACK,     total, genDelayMS, ackDelayMS );
        logStats( desc + ": ROUNDTRIP", _roundTrip, total, genDelayMS, ackDelayMS );
        
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
        int chainSize = 1000;
        int numChains = (count / chainSize) + 2; 
        int extra     = 50;
        
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
