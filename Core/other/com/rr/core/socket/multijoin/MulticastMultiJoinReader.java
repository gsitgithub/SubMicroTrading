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
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.os.SocketFactory;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.socket.LiteMulticastSocket;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;

// test using selector for reading from channel

public class MulticastMultiJoinReader {

    String               _mcastGroupAddressIn1     = "224.0.26.1";
    String               _mcastGroupAddressIn2     = "224.0.27.1";

    static String        _nicIP;

    static boolean       _spin;
    static boolean       _disableLookback;
    static boolean       _debug = false;
    static int           _qos;
    static int           _msgSize;

    public static void main( String args[] ) {
        
        StatsMgr.setStats( new TestStats() );

        LoggerFactory.setDebug( false );
        LoggerFactory.initLogging( "./logs/mcastDump", 100000000 );        

        LoggerFactory.setMinFlushPeriodSecs( 30 );
        
        Map<String,String> p = new HashMap<String,String>();
        
        try {
            DummyAppProperties.testInit( p );
        } catch( Exception e ) {
            Assert.fail( e.getMessage() );
        }
        
        if ( args.length != 8 ) {
            System.out.println( "Args : msgsToSend  NIC  cpuMask  spin{1|0}  disableLoopback{1|0}  debug{1|0}  qos  msgSize" );
            System.exit(-1);
        }

        int msgsToSend = Integer.parseInt( args[0] );
        _nicIP = args[1];
        int mask       = Integer.parseInt( args[2] );
        _spin = Integer.parseInt( args[3] ) == 1;
        _disableLookback = Integer.parseInt( args[4] ) == 1; 
        _debug = Integer.parseInt( args[5] ) == 1; 
        _qos = Integer.parseInt( args[6] );
        _msgSize = Integer.parseInt( args[7] );

        MulticastMultiJoinReader ps = new MulticastMultiJoinReader();
        ps.doRun( 1, msgsToSend, 10001, mask );
    }
    
    
    
    private class Consumer implements Runnable {

        private       LiteMulticastSocket  _inSocket = null;
        private final ByteBuffer           _inBuf  = ByteBuffer.allocateDirect( 8192 );
        private final ByteBuffer           _outBuf = ByteBuffer.allocateDirect( 8192 );
        @SuppressWarnings( "unused" )
        private       String               _id;
        private final int                  _consume;
        private final int                  _port;
        private final CyclicBarrier        _cb;
        private final int                  _mask;

        private volatile int               _in;

        public Consumer( String id, int consume, int port, CyclicBarrier cb, int mask ) {
            _id = id;
            _consume = consume;
            _port = port;
            _cb = cb;
            _mask = mask;
        }

        @Override
        public void run() {
            try {
                _cb.await();

                ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.HIGHEST, _mask ); 
                
                inConnect();
                
                consume( _consume );
                
                inClose();
            } catch( Exception e ) {
                Assert.fail( e.getMessage() );
            }
        }
        
        public void inConnect() throws IOException {

            SocketConfig sc = new SocketConfig();
            sc.setServer( false );
            sc.setPort( _port );
            sc.setDisableLoopback( _disableLookback );
            sc.setQOS( _qos );
            sc.setTTL( 1 );
            sc.setNic( new ViewString(_nicIP) );
            ZString[] mcastAddrs = { new ViewString(_mcastGroupAddressIn1), new ViewString(_mcastGroupAddressIn2) };
            sc.setMulticastGroups( mcastAddrs );
            
            _inSocket = SocketFactory.instance().createMulticastSocket( sc, _inBuf, _outBuf );
            _inSocket.configureBlocking( _spin == false );
        }

        public void inClose() {
            try { if ( _inSocket != null )  _inSocket.close();  } catch( Exception e ) { /* NADA */ }
        }
        
        void consume( int max ) throws Exception {
            _in = 0;

            Logger log = LoggerFactory.create( MulticastMultiJoinReader.class );
            
            ReusableString tbuf  = new ReusableString();
            ReusableString tbuf2 = new ReusableString();
            
            while( _in < max ) {
                getMessage( _msgSize );
                _inBuf.flip();
                
                int bytes = _inBuf.limit();
                
                tbuf.reset();
                tbuf.ensureCapacity( bytes );
                _inBuf.get( tbuf.getBytes(), 0, bytes );
                tbuf.setLength( bytes );
        
                int template = tbuf.getByte( 6 ) & 0xFF;
                
                if ( template > 128 ) {
                    template -= 128;
                } else {
                    template = (0xFF & (tbuf.getByte( 6 )) << 7) + 
                               (0xFF & (tbuf.getByte( 7 )) - 128);
                }
                
                tbuf2.reset();
                tbuf2.append( "line=" ).append( _in ).append( ", [" ).append( template ).append( "], Read bytes=" ).append( bytes );
                tbuf2.append( ", data=" );
                tbuf2.appendHEX( tbuf.getBytes(), 0, bytes );
                
                log.info( tbuf2 );
                
                ++_in;
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
    
    private void doRun( int numConsumers, int sendPerProducer, int port, int mask ) {
        int totalConsume = numConsumers * sendPerProducer;
        
        Consumer[] consumers = new Consumer[ numConsumers ];
        Thread[] tCons = new Thread[ numConsumers ];

        CyclicBarrier cb = new CyclicBarrier( numConsumers );
        
        for( int i=0; i < numConsumers ; ++i ) {
            consumers[i] = new Consumer( "C"+i, totalConsume, port+i, cb, mask );
            tCons[i] = new Thread( consumers[i], "CONSUMER" + i );
        }
        
        for( int i=0; i < numConsumers ; ++i ) {
            tCons[i].start();
        }
        
        for( int i=0; i < numConsumers ; ++i ) {
            try {
                tCons[i].join();
            } catch( InterruptedException e ) {
                // dont care
            }
        }
    }
}
