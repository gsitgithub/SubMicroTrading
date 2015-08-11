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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;

import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.LoggerFactory;
import com.rr.core.os.SocketFactory;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.Percentiles;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;

// test using selector for reading from channel

public class TCPPingServer {

    static   boolean     _debug = false;
    static   int         _qos;
    static   int         _msgSize;
    static   String      _nicIP;
             LiteSocket  _socket = null;
    final    ByteBuffer  _inBuf  = ByteBuffer.allocateDirect( _msgSize );
    final    ByteBuffer  _outBuf = ByteBuffer.allocateDirect( _msgSize );
    final    int         _port = 4321;
    
    volatile boolean     _isConnected;
    
    // args count, delayUSEC, NIC  
    public static void main( String args[] ) {
        
        LoggerFactory.setDebug( true );
        StatsMgr.setStats( new TestStats() );
        Map<String,String> p = new HashMap<String,String>();
        
        try {
            DummyAppProperties.testInit( p );
        } catch( Exception e ) {
            Assert.fail( e.getMessage() );
        }
        
        if ( args.length != 7 ) {
            System.out.println( "Args : msgsToSend  delayUSEC  cpuMask   debug{1|0} qos msgSize  NIC" );
            System.exit(-1);
        }

        int msgsToSend = Integer.parseInt( args[0] );
        int usecDelay  = Integer.parseInt( args[1] );
        int mask       = Integer.parseInt( args[2] );
        _debug = Integer.parseInt( args[3] ) == 1; 
        _qos = Integer.parseInt( args[4] );
        _msgSize = Integer.parseInt( args[5] );
        _nicIP = args[6];

        try {
            TCPPingServer ps = new TCPPingServer();
            ps.connect();
            ps.doRun( msgsToSend, mask, usecDelay );
            ps.doRun( msgsToSend, mask, usecDelay );
            ps.doRun( msgsToSend, mask, usecDelay );
            ps.doRun( msgsToSend, mask, usecDelay );
            ps.doRun( msgsToSend, mask, usecDelay );
            ps.close();
        } catch( Exception e ) {
            System.out.println( "ERROR "  + e.getMessage() );
            e.printStackTrace();
        }
    }
    
    public void connect() throws IOException {
        SocketConfig sc = new SocketConfig();
        sc.setPort( _port );
        sc.setQOS( _qos );
        sc.setTTL( 1 );
        sc.setNic( new ViewString(_nicIP) );
        sc.setUseNIO( true );
        sc.setSoDelayMS( 0 );
        
        LiteServerSocket serverSocket = SocketFactory.instance().createServerSocket( sc, _inBuf, _outBuf );
        SocketAddress addr = new InetSocketAddress( _nicIP, _port );
        
        serverSocket.configureBlocking( true );
        serverSocket.bind( addr);
        _socket = serverSocket.accept();

        _socket.configureBlocking( false );
        while( !_socket.finishConnect() ) {
            Utils.delay( 200 );     // Spin until connection is established
        }
        
        _socket.setTcpNoDelay( true );
        _socket.setKeepAlive( false );
        
//        _socket.setSoLinger( true, soLinger );
//        _socket.setSoTimeout( soTimeout );
        
        _isConnected = true;
        
        System.out.println( "Connected " );
    }
    
    public void close() {
        try { if ( _socket != null ) _socket.close(); } catch( Exception e ) { /* NADA */ }
    }
    
    

    private class Producer implements Runnable {
        
        private final int                  _send;
        private       CyclicBarrier        _cb;
        private final Consumer             _consumer;
        private final int                  _mask;
        private final int                  _delayUSEC;

        private volatile int               _out;

        public Producer( int send, CyclicBarrier cb, Consumer consumer, int mask, int delayUSEC ) {
            _send = send;
            _cb = cb;
            _consumer = consumer;
            _mask = mask;
            _delayUSEC = delayUSEC;
        }

        @Override
        public void run() {
            try {
                _cb.await();

                ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.HIGHEST, _mask );
                
                produce( _send );
                
            } catch( Exception e ) {
                Assert.fail( e.getMessage() );
            }
        }
        
        protected final void writeSocket() throws IOException {
            
            _socket.write();
        }

        void produce( int max ) throws Exception {
            _out = 0;
            
            int[] dur = new int[ max ];
            
            while( _out < max ) {
                long ts = Utils.nanoTime();
                send( _out, ts );
                long sendNanos = Math.abs( Utils.nanoTime() - ts );
                
                dur[_out] = (int)sendNanos;
                
                ++_out;

                if ( _debug ) System.out.println( " Sent id=" + _out + ", time=" + ts +  ", sendNanos=" + sendNanos);

                _consumer.waitEcho();

                if ( _delayUSEC > 0 ) {
                    Utils.delayMicros( _delayUSEC );
                }
            }
            
            Percentiles p = new Percentiles( dur );
            ReusableString s = new ReusableString();
            
            p.logStats( s );
            
            System.out.println( "TimeToSend  " + s.toString() );
        }
        
        private void send( int idx, long nanoTime ) throws IOException {

            _outBuf.clear();
            _outBuf.putInt( idx );
            _outBuf.putLong( nanoTime );
            _outBuf.position( _msgSize );
            _outBuf.flip();
            
            writeSocket();
        }

        public void logStats() {
            _consumer.logStats();
        }
    }
    
    private class Consumer {

        private volatile int               _in;
        private          int[]             _times;

        public Consumer( int cnt ) {
            _times = new int[cnt]; 
        }

        public void waitEcho() throws Exception {
            
            getMessage( _msgSize );
            _inBuf.flip();
            
            while( _inBuf.hasRemaining() ) {

                long now = Utils.nanoTime();
                int  startPos = _inBuf.position();
                
                int id    = _inBuf.getInt();
                long sent = _inBuf.getLong();
                
                int delay = (int)Math.abs( (sent - now) / 1000 );

                if ( _in < _times.length ) _times[_in] = delay;
                
                if ( _debug ) System.out.println( "Consumer, Read id=" + id + ", usecs=" + delay );
                
                ++_in;
                
                _inBuf.position( startPos + _msgSize ); 
            }
        }

        public void logStats() {
            Percentiles p = new Percentiles( _times );
            ReusableString buf = new ReusableString();
            p.logStats( buf );
            System.out.println( "RoundTrip " + buf.toString() );
        }
        
        private void getMessage( int msgSize ) throws Exception {
            _inBuf.clear();
            
            while( true ) {
                
                _socket.read();
        
                if ( _inBuf.position() >= msgSize ) {
                    break;
                }
            }
        }
    }
    
    private void doRun( int sendPerProducer, int mask, int delayUSEC ) {
        
        CyclicBarrier cb = new CyclicBarrier( 1 );
        
        Producer producer;
        Thread   tProd;
        
        Consumer consumer = new Consumer( sendPerProducer );
        producer = new Producer( sendPerProducer, cb, consumer, mask, delayUSEC );
        tProd = new Thread( producer, "PRODUCER" );
        
        tProd.start();
        
        try {
            tProd.join();
        } catch( InterruptedException e ) {
            // dont care
        }

        producer.logStats();
    }
}
