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
import com.rr.core.model.Currency;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Percentiles;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.internal.events.factory.MarketNewOrderSingleFactory;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.recycle.MarketNewOrderSingleRecycler;
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.order.collections.HashEntry;
import com.rr.om.processor.BaseProcessorTestCase;
import com.rr.om.processor.EventProcessorImpl;
import com.rr.om.warmup.FixTestUtils;

public class PerfTestCodecSingleThread extends BaseProcessorTestCase {

    private int[] _toMKt;

    public static void main( String[] arg ) throws Exception {
        int count = Integer.parseInt( arg[0] );
        
        PerfTestCodecSingleThread t = new PerfTestCodecSingleThread();
        
        ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.Processor );
        
        for( int i=0 ; i < 3 ; i++ ) {
            System.out.println( "TEST CODEC " + i + " ====================================================================" );
            t.setUp();
            t.doRunOrderMap( count );
            t.tearDown();
        }
    }
    
    public void testCodec() {
        
        doRunOrderMap( 100  );
        doRunOrderMap( 10000 );
        doRunOrderMap( 10000 );
    }

    private void doRunOrderMap( int total ) {
        presizePool( total );
        
        doTestCodec( "HASHMAP", total, getDirectProcesssor( total, false ) );
    }

    private void doTestCodec( String desc, int total, EventProcessorImpl proc ) {
        _toMKt     = new int[ total ];
        
        Standard44Decoder decoder = FixTestUtils.getDecoder44();
        ReusableString decodeBuffer = new ReusableString(250);

        ReusableString key = new ReusableString(20);

        SuperpoolManager spm = SuperpoolManager.instance();
        MarketNewOrderSingleRecycler nosRecycler = spm.getRecycler( MarketNewOrderSingleRecycler.class, MarketNewOrderSingleImpl.class );

        final byte[] bufMKt    = new byte[250];
        final byte[] bufClient = new byte[250];
        Standard44Encoder mktEncoder    = new Standard44Encoder( (byte)'4', (byte)'4', bufMKt );
        Standard44Encoder clientEncoder = new Standard44Encoder( (byte)'4', (byte)'4', bufClient );
        
        clientEncoder.setNanoStats( false );
        mktEncoder.setNanoStats( false );
        
        SuperpoolManager.instance().resetPoolStats();
        
        MarketNewOrderSingleFactory marketNOSFactory = SuperpoolManager.instance().getFactory( MarketNewOrderSingleFactory.class, 
                                                                                               MarketNewOrderSingleImpl.class );

        Utils.invokeGC();
        
        for ( int i=0 ; i < total ; ++i ) {

            decoder.setReceived( Utils.nanoTime() );
            mkKey( key, true, i );

            ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( decodeBuffer, decoder, key, 1, 1, _upMsgHandler );
            
            MarketNewOrderSingleImpl mnos = getMarketNOS( cnos, marketNOSFactory );
        
            mktEncoder.encode( mnos );                   
            long nowNano = Utils.nanoTime();
            mnos.getSrcEvent().setOrderSent( nowNano );
            
            int delay = (int) (nowNano - mnos.getOrderReceived());
            _toMKt[i] = delay;
            
            nosRecycler.recycle( mnos );
        }
        
        logStats( desc + ": TO_MARKET", _toMKt, total );
        
        logPoolStats();
    }

    private MarketNewOrderSingleImpl getMarketNOS( ClientNewOrderSingleImpl clientNos, MarketNewOrderSingleFactory marketNOSFactory ) {
        final MarketNewOrderSingleImpl mnos = marketNOSFactory.get();
        
        mnos.setSrcEvent( clientNos );
        mnos.setOrderQty( clientNos.getOrderQty() );

        double price = clientNos.getPrice();
        
        final Instrument inst            = clientNos.getInstrument();
        final Currency   tradingCurrency = inst.getCurrency();
        
        mnos.setPrice( price );
        mnos.setCurrency( tradingCurrency );
        
        final Exchange   exchange = inst.getExchange();
        exchange.generateMarketClOrdId( mnos.getClOrdIdForUpdate(), clientNos.getClOrdId() );
        
        return mnos;
    }

    private void logStats( String comment, int[] stats, long total ) {
        Percentiles p = new Percentiles( stats );

        System.out.println( "[" + comment + "]  ROUND TRIP NanoSecond stats " + " count=" + total + 
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
