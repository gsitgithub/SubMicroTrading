/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.socket.multijoin;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;

import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.LoggerFactory;
import com.rr.core.os.SocketFactory;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.socket.LiteMulticastSocket;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;

// test using selector for reading from channel

public class MulticastWriter1 {

    String               _mcastGroupAddressOut    = "224.0.26.1";

    static String        _nicIP;
    static boolean       _spin;
    static boolean       _disableLookback;
    static boolean       _debug = false;
    static int           _qos;
    static int           _msgSize;
    
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
        
        if ( args.length != 9 ) {
            System.out.println( "Args : msgsToSend  delayUSEC  NIC  cpuMask   spin{1|0}  disableLoopback{1|0} debug{1|0} qos msgSize" );
            System.exit(-1);
        }

        int msgsToSend = Integer.parseInt( args[0] );
        int usecDelay  = Integer.parseInt( args[1] );
        _nicIP = args[2];
        int mask       = Integer.parseInt( args[3] );
        _spin = Integer.parseInt( args[4] ) == 1;
        _disableLookback = Integer.parseInt( args[5] ) == 1; 
        _debug = Integer.parseInt( args[6] ) == 1; 
        _qos = Integer.parseInt( args[7] );
        _msgSize = Integer.parseInt( args[8] );

        MulticastWriter1 ps = new MulticastWriter1();
        ps.doRun( 1, msgsToSend, 10001, mask, usecDelay );
        ps.doRun( 1, msgsToSend, 10001, mask, usecDelay );
        ps.doRun( 1, msgsToSend, 10001, mask, usecDelay );
        ps.doRun( 1, msgsToSend, 10001, mask, usecDelay );
        ps.doRun( 1, msgsToSend, 10001, mask, usecDelay );
    }
    

    private class Producer implements Runnable {
        
        private final String               _id;
        private final ByteBuffer           _inBuf  = ByteBuffer.allocateDirect( _msgSize );
        private final ByteBuffer           _outBuf = ByteBuffer.allocateDirect( _msgSize );
        private       LiteMulticastSocket  _outSocket = null; 
        private final int                  _send;
        private final int                  _port;
        private       CyclicBarrier        _cb;
        private final int                  _mask;
        private final int                  _delayUSEC;

        private volatile int               _out;

        public Producer( String id, int send, int port, CyclicBarrier cb, int mask, int delayUSEC ) {
            _id = id;
            _send = send;
            _port = port;
            _cb = cb;
            _mask = mask;
            _delayUSEC = delayUSEC;
        }

        @Override
        public void run() {
            try {
                _cb.await();

                ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.HIGHEST, _mask );
                
                outConnect();
                
                produce( _send );
                
                outClose();
            } catch( Exception e ) {
                Assert.fail( e.getMessage() );
            }
        }
        
        protected final void writeSocket() throws IOException {
            
            _outSocket.write();
        }

        public void outConnect() throws IOException {
            SocketConfig sc = new SocketConfig();
            sc.setServer( true );
            sc.setPort( _port );
            sc.setDisableLoopback( _disableLookback );
            sc.setQOS( _qos );
            sc.setTTL( 1 );
            sc.setNic( new ViewString(_nicIP) );
            ZString[] mcastAddrs = { new ViewString(_mcastGroupAddressOut) };
            sc.setMulticastGroups( mcastAddrs );
            
            _outSocket  = SocketFactory.instance().createMulticastSocket( sc, _inBuf, _outBuf ); 
            _outSocket.configureBlocking( _spin == false );
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

                if ( _debug ) System.out.println( _id + " Sent id=" + _out + ", time=" + ts );

                if ( _delayUSEC > 0 ) {
                    Utils.delayMicros( _delayUSEC );
                }
            }
        }
        
        private void send( long idx, long nanoTime ) throws IOException {

            _outBuf.clear();
            _outBuf.putLong( idx );
            _outBuf.putLong( nanoTime );
            _outBuf.position( _msgSize );
            _outBuf.flip();
            
            writeSocket();
        }
    }
    
    private void doRun( int numProducers, int sendPerProducer, int port, int mask, int delayUSEC ) {
        
        CyclicBarrier cb = new CyclicBarrier( numProducers );
        
        Producer[] producers = new Producer[ numProducers ];
        Thread[] tProd = new Thread[ numProducers ];
        
        for( int i=0; i < numProducers ; ++i ) {
            producers[i] = new Producer( "P"+i, sendPerProducer, port+i, cb, mask, delayUSEC );
            tProd[i] = new Thread( producers[i], "PRODUCER" + i );
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
    }
}
