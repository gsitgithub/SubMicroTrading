/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CyclicBarrier;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.utils.Utils;

// test using selector for reading from channel

public class TestMulticastSocket extends BaseTestCase {

    private static final int     MSG_SIZE               = 16;  // 8 bytes for long pingId, 8 bytes for nanoSent
    
    String               _mcastGroupAddress     = "224.0.0.255";
    int                  _msDelay;
    
    private class Consumer implements Runnable {

        private       MulticastSocketAdapter _inSocket = null;
        private final ByteBuffer           _inBuf    = ByteBuffer.allocate( MSG_SIZE+1 );
        private final ByteBuffer           _outBuf   = ByteBuffer.allocate( MSG_SIZE+1 );
        private       String               _id;
        private final int                  _consume;
        private final int                  _port;
        private final CyclicBarrier        _cb;

        private volatile int               _in;

        public Consumer( String id, int consume, int port, CyclicBarrier cb ) {
            _id = id;
            _consume = consume;
            _port = port;
            _cb = cb;
        }

        @Override
        public void run() {
            try {
                _cb.await();
                
                inConnect();
                
                consume( _consume );
                
                inClose();
            } catch( Exception e ) {
                fail( e.getMessage() );
            }
        }
        
        public void inConnect() throws IOException {
            
            _inSocket = new MulticastSocketAdapter( _port, false, 0x08, 3, _inBuf, _outBuf );
            _inSocket.configureBlocking( false );
            _inSocket.setTcpNoDelay( true );
            
            _inSocket.joinGroup( _mcastGroupAddress, null );

            System.out.println( "Connected " + _id );
        }

        public void inClose() {
            try { if ( _inSocket != null )  _inSocket.close();  } catch( Exception e ) { /* NADA */ }
        }
        
        public int getIn() { return _in; }

        void consume( int max ) throws Exception {
            _in = 0;
            
            while( _in < max ) {
                int read = getMessage( MSG_SIZE );
                
                if ( read >= MSG_SIZE ) {
                    _inBuf.flip();
                    
                    long now = Utils.nanoTime();
                    
                    long id   = _inBuf.getLong();
                    long sent = _inBuf.getLong();
                    
                    long delay = Math.abs( (sent - now) / 1000 );
                    
                    System.out.println( "Consumer " + _id + ", Read id=" + id + ", usecs=" + delay );
                    
                    ++_in;
                }
            }
        }

        private int getMessage( int msgSize ) throws Exception {
            while( true ) {
                
                _inSocket.read();
        
                if ( _inBuf.position() > 0 ) {
                    break;
                }
                
                Thread.sleep(1);
            }
            
            return _inBuf.position();
        }
    }

    private class Producer implements Runnable {
        
        private final String               _id;
        private final ByteBuffer           _inBuf  = ByteBuffer.allocate( MSG_SIZE+1 );
        private final ByteBuffer           _outBuf = ByteBuffer.allocate( MSG_SIZE+1 );
        private       MulticastSocketAdapter _outSocket = null; 
        private final int                  _send;
        private final int                  _port;
        private       CyclicBarrier        _cb;

        private volatile int               _out;

        public Producer( String id, int send, int port, CyclicBarrier cb ) {
            _id = id;
            _send = send;
            _port = port;
            _cb = cb;
        }

        @Override
        public void run() {
            try {
                _cb.await();
                
                outConnect();
                
                produce( _send );
                
                outClose();
            } catch( Exception e ) {
                fail( e.getMessage() );
            }
        }
        
        protected final void writeSocket() throws IOException {
            
            _outSocket.write();
        }

        public void outConnect() throws IOException {
            _outSocket = new MulticastSocketAdapter( _port, false, 0x08, 3, _inBuf, _outBuf );
            _outSocket.configureBlocking( false );
            _outSocket.setTcpNoDelay( true );
            
            InetSocketAddress group = new InetSocketAddress( _mcastGroupAddress, _port );
            _outSocket.connect( group );

            System.out.println( "Connected " + _id );
        }
        
        public void outClose() {
            try { if ( _outSocket != null ) _outSocket.close(); } catch( Exception e ) { /* NADA */ }
        }
        
        void produce( int max ) throws Exception {
            _out = 0;
            
            while( _out < max ) {
                send( _out, Utils.nanoTime() );
                
                ++_out;

                System.out.println( _id + " Sent id=" + _out );
                
                try{ Thread.sleep( _msDelay ); } catch( Exception e ) {  /* dont care */ }
            }
        }
        
        private void send( long idx, long nanoTime ) throws IOException {

            _outBuf.clear();
            _outBuf.putLong( idx );
            _outBuf.putLong( nanoTime );
            
            _outBuf.flip();
            
            writeSocket();
        }

        public int getOut() {
            return _out;
        }
    }
    
    public void testSimpleMCast() throws Exception {
        doRun( 1, 1, 10, 14880 );
    }
    
    private void doRun( int numConsumers, int numProducers, int sendPerProducer, int port ) {
        int totalConsume = numProducers * sendPerProducer;
        
        Consumer[] consumers = new Consumer[ numConsumers ];
        Thread[] tCons = new Thread[ numConsumers ];

        CyclicBarrier cb = new CyclicBarrier( numProducers + numConsumers );
        
        for( int i=0; i < numConsumers ; ++i ) {
            consumers[i] = new Consumer( "C"+i, totalConsume, port, cb );
            tCons[i] = new Thread( consumers[i], "CONSUMER" + i );
        }
        
        Producer[] producers = new Producer[ numProducers ];
        Thread[] tProd = new Thread[ numProducers ];
        
        for( int i=0; i < numProducers ; ++i ) {
            producers[i] = new Producer( "P"+i, sendPerProducer, port, cb );
            tProd[i] = new Thread( producers[i], "PRODUCER" + i );
        }
        
        _msDelay = 1;
        
        for( int i=0; i < numConsumers ; ++i ) {
            tCons[i].start();
        }
        
        for( int i=0; i < numProducers ; ++i ) {
            tProd[i].start();
        }
        
        for( int i=0; i < numProducers ; ++i ) {
            try {
                tProd[i].join();
            } catch( InterruptedException e ) {
                // dont care
            }
        }
        
        for( int i=0; i < numConsumers ; ++i ) {
            try {
                tCons[i].join( totalConsume / 10000 + 1000 );
            } catch( InterruptedException e ) {
                // dont care
            }
        }
        
        for( int i=0; i < numConsumers ; ++i ) {
            assertTrue( consumers[i].getIn() > totalConsume / 2 );
        }
        
        for( int i=0; i < numProducers ; ++i ) {
            assertTrue( producers[i].getOut() > sendPerProducer / 2 );
        }
    }
}
