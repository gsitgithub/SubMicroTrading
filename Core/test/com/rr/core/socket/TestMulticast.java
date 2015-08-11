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
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CyclicBarrier;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.utils.Utils;

// test using selector for reading from channel

public class TestMulticast extends BaseTestCase {

    private static final int     MSG_SIZE               = 16;  // 8 bytes for long pingId, 8 bytes for nanoSent
    
    String               _mcastGroupAddress     = "224.0.0.255";
    int                  _msDelay;
    
    private class Consumer implements Runnable {

        private       MulticastSocket      _inSocket              = null;
        private       String               _id;
        private final int                  _consume;
        private final byte[]               _inBuf = new byte[1024];
        private final ByteBuffer           _inBB = ByteBuffer.wrap( _inBuf );
        private final int                  _port;
        private final CyclicBarrier        _cb;

        private volatile int               _in;
        private       InetAddress          _multicastAddr;
        private       DatagramPacket       _packet;

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
            
            _inSocket = new MulticastSocket( _port );
            
            _inSocket.setReuseAddress( true );
            
            _inSocket.setTrafficClass( 0x08 );

            _multicastAddr = InetAddress.getByName( _mcastGroupAddress );
            
            InetSocketAddress group = new InetSocketAddress( _mcastGroupAddress, _port );
            
            _packet = new DatagramPacket( _inBuf, _inBuf.length, group );
            
            // _inSocket.connect( group );

            // join the multicast group
            _inSocket.joinGroup( _multicastAddr );
       
            _inSocket.setTimeToLive( 3 );
            
            // if running on same host loopback must be enabled
            _inSocket.setLoopbackMode( false );
            
            _inSocket.setBroadcast( true );

            System.out.println( "Init " + _id );
        }

        public void inClose() {
            try { if ( _inSocket != null )  _inSocket.close();  } catch( Exception e ) { /* NADA */ }
        }
        
        public int getIn() { return _in; }

        void consume( int max ) throws Exception {
            _in = 0;
            
            while( _in < max ) {
                int read = getMessage( MSG_SIZE );
                
                if ( read > 0 ) {
                    long now = Utils.nanoTime();
                    
                    long id   = _inBB.getLong();
                    long sent = _inBB.getLong();
                    
                    long delay = Math.abs( (sent - now) / 1000 );
                    
                    System.out.println( "Consumer " + _id + ", Read id=" + id + ", usecs=" + delay );
                    
                    ++_in;
                }
            }
        }

        private int getMessage( int msgSize ) throws Exception {

            while( true ) {
                
                _inSocket.receive( _packet );
        
                if ( _packet.getLength() > 0 ) {
                    _inBB.position( 0 );
                    _inBB.limit( _packet.getLength() );
                    break;
                }
                
                Thread.sleep(1);
            }
            
            return _packet.getLength();
        }
    }

    private class Producer implements Runnable {
        
        private final String               _id;
        private       MulticastSocket      _outSocket             = null; 
        private final int                  _send;
        private final int                  _port;
        private       CyclicBarrier        _cb;
        private       byte[]               _outBuf = new byte[1024];
        private       ByteBuffer           _outBB = ByteBuffer.wrap( _outBuf );
        private       DatagramPacket       _packet;
        private       InetAddress          _multicastAddr;

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

            _packet.setLength( _outBB.limit() );
            _outSocket.send( _packet );
        }

        public void outConnect() throws IOException {
            _outSocket = new MulticastSocket();
            
            _outSocket.setReuseAddress( true );

            _outSocket.setTrafficClass( 0x08 );

            _multicastAddr = InetAddress.getByName( _mcastGroupAddress );
            
            InetSocketAddress group = new InetSocketAddress( _mcastGroupAddress, _port );
            
            _packet = new DatagramPacket( _outBuf, _outBuf.length, null, 0 );
            
            _outSocket.connect( group );
            
            // join the multicast group
            _outSocket.joinGroup( _multicastAddr );
       
            _outSocket.setTimeToLive( 3 );
            
            // if running on same host loopback must be enabled
            _outSocket.setLoopbackMode( false );
            
            _outSocket.setBroadcast( true );

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

            _outBB.clear();
            _outBB.putLong( idx );
            _outBB.putLong( nanoTime );
            
            _outBB.flip();
            
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
