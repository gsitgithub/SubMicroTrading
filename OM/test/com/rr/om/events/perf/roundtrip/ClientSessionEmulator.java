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
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.recycle.ClientNewOrderAckRecycler;
import com.rr.om.processor.EventProcessor;
import com.rr.om.warmup.FixTestUtils;

/**
 * emulate a client session, has an in thread which generates NOS
 * and an out thread which encodes the client ACK
 * measures time from CNOS decoded to ACK encoded
 *
 * @author Richard Rose
 */
public class ClientSessionEmulator implements MessageHandler {

    private static class ClientNOSGenerator extends Thread {

        private final EventProcessor _proc;
        private int _count;
        private final int _producerIdx;
        private int _errs = 0;
        private int _sent;
        private ReusableString _buffer = new ReusableString(256);
        private final int _producerDelayMS;
        private long[] _stats;
        private MessageHandler _emulator;

        public ClientNOSGenerator( int            producerIdx, 
                                   int            count, 
                                   EventProcessor proc, 
                                   int            producerDelayMS,
                                   MessageHandler emulator ) {
            
            super( "CLIENT_IN_" + producerIdx );
            
            _proc            = proc;
            _count           = count;
            _sent            = 0;
            _producerIdx     = producerIdx;
            _producerDelayMS = producerDelayMS;
            _stats           = new long[_count];
            _emulator        = emulator;
        }

        @Override
        public void run() {
            
            Standard44Decoder decoder = FixTestUtils.getDecoder44();

            ReusableString key = new ReusableString(20);
            
            for ( int i=0 ; i < _count ; ++i ) {
                Thread.yield();
                decoder.setReceived( Utils.nanoTime() );
                mkKey( key, true, i, _producerIdx );
                key.append( _producerIdx );
                Message msg = FixTestUtils.getClientNOS( _buffer, decoder, key, 1, 1, _emulator );
                if ( msg != null ) {
                    _proc.handle( msg );
                    ++_sent;
                } else {
                    ++_errs;
                }
                if ( _producerDelayMS != 0 ) {
                    Utils.delay( _producerDelayMS );
                }
            }
        }

        public long[] stats() {
            return _stats;
        }
        
        public long sent() {
            return _sent;
        }
        
        public long getErrs() {
            return _errs;
        }
    }
    
    private static class ClientSenderEmulator extends Thread {

        private static final long MAX_CONSUME_WAIT_MS = 1000;
        
        private int _tot;
        private MessageQueue _q;
        private volatile boolean _finished = false;
        private int _count = 0;
        private long _totQty = 0;
        private int[] _stats;

        private ClientNewOrderAckRecycler ackRecycler = SuperpoolManager.instance().getRecycler( ClientNewOrderAckRecycler.class, 
                                                                                                 ClientNewOrderAckImpl.class );
        

        public ClientSenderEmulator( int sessionNum, int count, MessageQueue q ) {
            super( "CLIENT_OUT_" + sessionNum );
            
            _tot = count;
            _q = q;
            _stats = new int[_tot];
        }

        @Override
        public void run() {
            long last = System.currentTimeMillis();
            long now;
            
            ClientNewOrderAckImpl ack;

            final byte[] buf = new byte[512];
            Standard44Encoder encoder = new Standard44Encoder( (byte)'4', (byte)'4', buf );
            encoder.setNanoStats( false );
            
            Message m = null;

            boolean poll = Runtime.getRuntime().availableProcessors() > 3;

            while( _count < _tot ) {
                
                do {
                    if ( poll ) {
                        m = _q.poll();
                    } else {
                        m  = _q.next();
                    }
            
                    now = System.currentTimeMillis();
                    
                    if ( now - last > MAX_CONSUME_WAIT_MS ) {
                        _finished = true;
                    }
                    
                } while( m == null && !_finished );
                
                if ( m != null ) {
         
                    ack = (ClientNewOrderAckImpl) m;
                    encoder.encode( ack );                   
                    long nowNano = Utils.nanoTime();  
                    long nosLat  = ack.getOrderSent() - ack.getOrderReceived();
                    long ackLat  = nowNano            - ack.getAckReceived();
                    
                    _stats[_count] = (int) (nosLat + ackLat);
                    
                    _totQty += ack.getOrderQty();

                    ++_count;
                    
                    ackRecycler.recycle( ack );
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
    
    static void mkKey( ReusableString key, boolean isClient, int i, int producerIdx ) {
        key.reset();
        
        key.append( (isClient) ? 'C' : 'M' );
        key.append( "SOMEKEY" );
        key.append( producerIdx );
        key.append( 1000000+i );
    }
    
    private final MessageQueue         _outboundQ;
    private final ClientNOSGenerator   _clientInEmulation;
    private final ClientSenderEmulator _clientOutEmulation;
    private final String               _name;
    
    public ClientSessionEmulator( MessageQueue outboundQ, int producerIdx, int count, EventProcessor proc, int producerDelayMS  ) {
        
        _name = "CLIENT" + producerIdx;
        
        _outboundQ = outboundQ;
        
        _clientInEmulation = new ClientNOSGenerator( producerIdx, count, proc, producerDelayMS, this );
        _clientInEmulation.setDaemon( true );
        
        _clientOutEmulation = new ClientSenderEmulator( producerIdx, count, _outboundQ );
        _clientOutEmulation.setDaemon( false );
    }
    
    public void start() {
        _clientOutEmulation.start();
        _clientInEmulation.start();
    }
    
    @Override
    public void handle( Message msg ) {
        _outboundQ.add( msg );
    }

    public int[] getRoundTripTimes(){
        return _clientOutEmulation.getTimes();
    }
    
    public boolean finished() {
        return _clientOutEmulation.finished();
    }

    public int getConsumed() {
        return _clientOutEmulation.getConsumed();
    }

    public long[] getInboundStats() {
        return _clientInEmulation.stats();
    }
    
    public long sent() {
        return _clientInEmulation.sent();
    }
    
    public long getErrs() {
        return _clientInEmulation.getErrs();
    }

    @Override
    public String getComponentId() {
        return _name;
    }

    @Override
    public void handleNow( Message msg ) {
        _outboundQ.add( msg );
    }

    @Override
    public void threadedInit() {
        // nothing
    }

    @Override
    public boolean canHandle() {
        return true;
    }
}
