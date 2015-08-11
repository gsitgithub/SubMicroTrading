/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf.roundtrip;

import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.recycle.MarketNewOrderSingleRecycler;
import com.rr.om.processor.EventProcessor;
import com.rr.om.processor.EventProcessorImpl;
import com.rr.om.warmup.FixTestUtils;

public class ExchangeSessionEmulator implements MessageHandler {

    private static class ExchangeSessionThread extends Thread {

        private static final long MAX_CONSUME_WAIT_MS = 1000;
        
        private int _tot;
        private MessageQueue _sessQ;
        private volatile boolean _finished = false;
        private int _count = 0;
        private long _totQty = 0;
        private long[] _stats;

        private int _ackDelayMS;

        private EventProcessor _proc;

        private ViewString _baseOrderId = new ReusableString( "ORDID" );
        private ViewString _baseExecId  = new ReusableString( "EXECID" );
        
        public ExchangeSessionThread( int tot, MessageQueue sessQ, int ackDelayMS ) {
            
            super( "EXSESS" );
            
            _tot        = tot;
            _sessQ      = sessQ;
            _ackDelayMS = ackDelayMS;
            _stats      = new long[_tot];
        }

        @Override
        public void run() {
            long last = System.currentTimeMillis();
            long now;

            Standard44Decoder decoder = FixTestUtils.getDecoder44();
            
            SuperpoolManager spm = SuperpoolManager.instance();
            MarketNewOrderSingleRecycler nosRecycler = spm.getRecycler( MarketNewOrderSingleRecycler.class, MarketNewOrderSingleImpl.class );

            MarketNewOrderSingleImpl nos;
            MarketNewOrderAckImpl    ack;
            
            ReusableString buffer  = new ReusableString(150);
            ReusableString orderId = new ReusableString( SizeConstants.DEFAULT_MARKETORDERID_LENGTH );
            ReusableString execId  = new ReusableString( SizeConstants.DEFAULT_EXECID_LENGTH );

            final byte[] buf = new byte[512];
            Standard44Encoder encoder = new Standard44Encoder( (byte)'4', (byte)'4', buf );
            encoder.setNanoStats( false );
            
            Message m = null;
            
            boolean poll = Runtime.getRuntime().availableProcessors() > 3;

            while( _count < _tot ) {
                
                do {
                    if ( poll ) {
                        m = _sessQ.poll();
                    } else {
                        m  = _sessQ.next();
                    }
            
                    now = System.currentTimeMillis();
                    
                    if ( now - last > MAX_CONSUME_WAIT_MS ) {
                        _finished = true;
                    }
                    
                } while( m == null && !_finished );
                
                if ( m != null ) {
         
                    nos = (MarketNewOrderSingleImpl) m;
                    encoder.encode( nos );                   
                    long nowNano = Utils.nanoTime();
                    nos.getSrcEvent().setOrderSent( nowNano );
                    
                    long delay = nowNano - nos.getOrderReceived();
                    _stats[_count] = delay;
                    
                    _totQty += nos.getOrderQty();

                    ++_count;
            
                    orderId.copy( _baseOrderId );
                    orderId.append( _count );
                    execId.copy( _baseExecId );
                    execId.append( _count );
                    
                    if ( _ackDelayMS > 0 ) {
                        Utils.delay( _ackDelayMS );
                    }
                    
                    ack = FixTestUtils.getMarketACK( buffer, decoder, nos.getClOrdId(), nos.getOrderQty(),  nos.getPrice(), orderId, execId ); 

                    _proc.handle( ack );
                    
                    nosRecycler.recycle( nos );
                }
                
                last = now; 
            }
            
            _finished = true;
        }

        public long[] getTimes(){
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

        public void setProc( EventProcessorImpl proc ) {
            _proc = proc;
        }
    }

    private final MessageQueue          _outQ;
    private final ExchangeSessionThread _exSimulator;
    private final String                _name = "ExSimEmul";
    
    public ExchangeSessionEmulator( int tot, MessageQueue sessQ, int ackDelayMS ) {
        _exSimulator = new ExchangeSessionThread( tot, sessQ, ackDelayMS );
        _exSimulator.setDaemon( true );
        _outQ = sessQ;
        
        _exSimulator.start();
    }
    
    @Override
    public void handle( Message msg ) {
        _outQ.add( msg );
    }
    
    public long[] getTimes(){
        return _exSimulator.getTimes();
    }
    
    public boolean finished() {
        return _exSimulator.finished();
    }

    public int getConsumed() {
        return _exSimulator.getConsumed();
    }

    public void setProcessor( EventProcessorImpl proc ) {
        _exSimulator.setProc( proc );
    }

    @Override
    public void handleNow( Message msg ) {
        _outQ.add( msg );
    }

    @Override
    public void threadedInit() {
        // nothing
    }

    @Override
    public boolean canHandle() {
        return true;
    }

    @Override
    public String getComponentId() {
        return _name;
    }
}
