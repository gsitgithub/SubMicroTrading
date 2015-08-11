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
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;

import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.os.SocketFactory;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;

// test using selector for reading from channel

public class MulticastPingConcServer extends BaseTestCase {

    private static final int     MSG_SIZE               = 16;  // 8 bytes for long pingId, 8 bytes for nanoSent
    
    String               _mcastGroupAddressOut    = "226.1.1.1";
    String               _mcastGroupAddressIn     = "226.1.1.2";

    int                  _msDelay = 1;

    static String        _nicIP = getNIC();

    private class Producer implements Runnable {
        
        private final String               _id;
        private final ByteBuffer           _inBuf  = ByteBuffer.allocateDirect( MSG_SIZE+1 );
        private final ByteBuffer           _outBuf = ByteBuffer.allocateDirect( MSG_SIZE+1 );
        private       LiteMulticastSocket  _outSocket = null; 
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

                ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.HIGHEST, 128 );
                
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
            SocketConfig sc = new SocketConfig();
            sc.setPort( _port );
            sc.setServer( true );
            sc.setDisableLoopback( false );
            sc.setQOS( 0x08 );
            sc.setTTL( 3 );
            sc.setNic( new ViewString(_nicIP) );
            ZString[] mcastAddrs = { new ViewString(_mcastGroupAddressOut) };
            sc.setMulticastGroups( mcastAddrs );
            
            _outSocket  = SocketFactory.instance().createMulticastSocket( sc, _inBuf, _outBuf ); 
            _outSocket.configureBlocking( false );
            _outSocket.setTcpNoDelay( true );
        }
        
        public void outClose() {
            try { if ( _outSocket != null ) _outSocket.close(); } catch( Exception e ) { /* NADA */ }
        }
        
        void produce( int max ) throws Exception {
            _out = 0;
            
            while( _out < max ) {
                long ts = Utils.nanoTime();
                send( _out, ts );
                
                ++_out;

                System.out.println( _id + " Sent id=" + _out + ", time=" + ts );
                
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
    
    private class Consumer implements Runnable {

        private       LiteMulticastSocket  _inSocket = null;
        private final ByteBuffer           _inBuf  = ByteBuffer.allocateDirect( MSG_SIZE+1 );
        private final ByteBuffer           _outBuf = ByteBuffer.allocateDirect( MSG_SIZE+1 );
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

                ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.HIGHEST, 256 );
                
                inConnect();
                
                consume( _consume );
                
                inClose();
            } catch( Exception e ) {
                fail( e.getMessage() );
            }
        }
        
        public void inConnect() throws IOException {

            SocketConfig sc = new SocketConfig();
            sc.setPort( _port );
            sc.setServer( false );
            sc.setDisableLoopback( false );
            sc.setQOS( 0x08 );
            sc.setTTL( 1 );
            sc.setNic( new ViewString(_nicIP) );
            ZString[] mcastAddrs = { new ViewString(_mcastGroupAddressIn) };
            sc.setMulticastGroups( mcastAddrs );
            
            _inSocket = SocketFactory.instance().createMulticastSocket( sc, _inBuf, _outBuf );
            _inSocket.configureBlocking( false );
            _inSocket.setTcpNoDelay( true );
        }

        public void inClose() {
            try { if ( _inSocket != null )  _inSocket.close();  } catch( Exception e ) { /* NADA */ }
        }
        
        public int getIn() { return _in; }

        void consume( int max ) throws Exception {
            _in = 0;
            
            while( _in < max ) {
                getMessage( MSG_SIZE );
                _inBuf.flip();
                
                while( _inBuf.hasRemaining() ) {

                    long now = Utils.nanoTime();
                    
                    long id   = _inBuf.getLong();
                    long sent = _inBuf.getLong();
                    
                    long delay = Math.abs( (sent - now) / 1000 );
                    
                    System.out.println( "Consumer " + _id + ", Read id=" + id + ", usecs=" + delay );
                    
                    ++_in;
                }
            }
        }

        private void getMessage( int msgSize ) throws Exception {
            _inBuf.clear();
            
            while( true ) {
                
                _inSocket.read();
        
                if ( _inBuf.position() >= msgSize ) {
                    break;
                }
            }
        }
    }
    
    {
        Map<String,String> p = new HashMap<String,String>();
        
        try {
            DummyAppProperties.testInit( p );
        } catch( Exception e ) {
            Assert.fail( e.getMessage() );
        }
    }
    
    public void testSimpleMCast() throws Exception {
        doRun( 1, 1000, 14880, 14981 );
    }
    
    private static String getNIC() {
        String nic = System.getProperty( "NIC" );
        
        if ( nic == null ) {
            nic = "127.0.0.1";
        }
        
        System.out.println( "NIC=" + nic );
        
        return nic;
    }

    private void doRun( int numProducers, int sendPerProducer, int port, int echoPort ) {
        
        int totalConsume = numProducers * sendPerProducer;
        
        Consumer[] consumers = new Consumer[ numProducers ];
        Thread[] tCons = new Thread[ numProducers ];

        CyclicBarrier cb = new CyclicBarrier( numProducers * 2 );
        
        for( int i=0; i < numProducers ; ++i ) {
            consumers[i] = new Consumer( "C"+i, totalConsume, echoPort+i, cb );
            tCons[i] = new Thread( consumers[i], "CONSUMER" + i );
        }
        
        Producer[] producers = new Producer[ numProducers ];
        Thread[] tProd = new Thread[ numProducers ];
        
        for( int i=0; i < numProducers ; ++i ) {
            producers[i] = new Producer( "P"+i, sendPerProducer, port+i, cb );
            tProd[i] = new Thread( producers[i], "PRODUCER" + i );
        }
        
        _msDelay = 1;
        
        for( int i=0; i < numProducers ; ++i ) {
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
        
        for( int i=0; i < numProducers ; ++i ) {
            try {
                tCons[i].join();
            } catch( InterruptedException e ) {
                // dont care
            }
        }
        
        for( int i=0; i < numProducers ; ++i ) {
            assertEquals( totalConsume, consumers[i].getIn() );
        }

        for( int i=0; i < numProducers ; ++i ) {
            assertEquals( sendPerProducer, producers[i].getOut() );
        }
    }
}
