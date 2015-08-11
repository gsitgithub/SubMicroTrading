/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.itch;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;

import com.rr.codec.emea.exchange.millenium.ITCHDecodeBuilderImpl;
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

// test using selector for reading from channel

public class ITCHMulticastSampleClient {

    String               _mcastGroupAddressIn     = "226.1.1.1";
    String               _mcastGroupAddressOut    = "226.1.1.2";

    static String        _nicIP;

    static boolean       _spin;
    static boolean       _disableLookback;
    static boolean       _debug = false;
    static int           _qos;
    static int           _msgSize;

    public static void main( String args[] ) {
        
        LoggerFactory.setDebug( true );
        StatsMgr.setStats( new TestStats() );
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
        _msgSize = Math.max( 50, Integer.parseInt( args[7] ) );

        ITCHMulticastSampleClient ps = new ITCHMulticastSampleClient();
        ps.doRun( 1, msgsToSend, 14880, 14981, mask );
    }
    
    
    
    private class Consumer implements Runnable {

        private       LiteMulticastSocket  _inSocket = null;
        private final ByteBuffer           _inBuf  = ByteBuffer.allocateDirect( _msgSize );
        private final ByteBuffer           _outBuf = ByteBuffer.allocateDirect( _msgSize );
        private       String               _id;
        private final int                  _consume;
        private final int                  _port;
        private final CyclicBarrier        _cb;
        private final int                  _mask;

        private ITCHDecodeBuilderImpl      _decoder;
        private byte[] _inBytes;

        int numMsgs;
        byte mktDataGrp;
        int seqNum;
        byte msgType;
        byte subMsgLen;
        int nowMicros;
        long orderId;        
        byte side;
        int qty;      
        int instId;   
        double price;
        
        public Consumer( String id, int consume, int port, CyclicBarrier cb, int mask ) {
            _id = id;
            _consume = consume;
            _port = port;
            _cb = cb;
            _mask = mask;
            _decoder = new ITCHDecodeBuilderImpl();
            _inBytes = new byte[ _msgSize ];
        }

        @Override
        public void run() {
            try {
                _cb.await();

                ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.HIGHEST, _mask ); 
                
                inConnect();
                
                for( int i=1 ; i <= 5 ; i++ ) {
                    consume( i, _consume );
                }
                
                inClose();
                
                dontOptimise();
                
            } catch( Exception e ) {
                Assert.fail( e.getMessage() );
            }
        }
        
        private void dontOptimise() {
            double v = numMsgs + mktDataGrp + seqNum + msgType + subMsgLen +  nowMicros + orderId + side + qty + instId + price;
            System.out.println("DontOpimise " + v );
        }

        public void inConnect() throws IOException {

            SocketConfig sc = new SocketConfig();
            sc.setServer( false );
            sc.setPort( _port );
            sc.setDisableLoopback( _disableLookback );
            sc.setQOS( _qos );
            sc.setTTL( 1 );
            sc.setNic( new ViewString(_nicIP) );
            ZString[] mcastAddrs = { new ViewString(_mcastGroupAddressIn) };
            sc.setMulticastGroups( mcastAddrs );
            
            _inSocket = SocketFactory.instance().createMulticastSocket( sc, _inBuf, _outBuf );
            _inSocket.configureBlocking( _spin == false );
            
            System.out.println( "Connected " + _id );
        }

        public void inClose() {
            try { if ( _inSocket != null )  _inSocket.close();  } catch( Exception e ) { /* NADA */ }
        }
        
        void consume( int runIdx, int max ) throws Exception {
            
            long startMS = 0;
            long count = 0;
            long packets = 0;
            long skips = 0;
            
            while( packets < max ) {
                getMessage( 8, true );
                
                // start time from start of first message read
                if ( startMS == 0 ) startMS = System.currentTimeMillis();
                
                int numBytes = _inBuf.position();
                _inBuf.flip();
                _inBuf.get(_inBytes, 0, numBytes );
                _decoder.start( _inBytes, 0, numBytes );

                int msgLen = _decoder.decodeShort();
                
                if ( numBytes < msgLen ) {
                    System.out.println( "SKIP INCOMPLETE LINE expected " + msgLen + " got " + numBytes );

                    ++skips;
                    
                    continue;
                } 
                
                ++packets;
                
                numMsgs = _decoder.decodeByte();
                mktDataGrp = _decoder.decodeByte();
                seqNum  = _decoder.decodeInt();
                
                for( int i=0  ; i < numMsgs ; i++ ) {
                    subMsgLen = _decoder.decodeByte();
                    msgType = _decoder.decodeByte();
                    
                    nowMicros = _decoder.decodeInt();
                    orderId = _decoder.decodeLong();        
                    side = _decoder.decodeByte();
                    qty = _decoder.decodeInt();      
                    instId = _decoder.decodeInt();   
                    _decoder.skip( 2 );
                    price = _decoder.decodePrice();
                    _decoder.skip( 1 );
                }
                
                count += numMsgs;
            }

            long endMS = System.currentTimeMillis();
            long duration = (endMS - startMS);
            double durationSEC = duration / 1000.0;
            
            System.out.println( "Run " + runIdx + ", duration=" + duration + ", packets=" + packets + ", subMsgs=" + count + 
                                ", msgRate=" + (long)(count/durationSEC) + 
                                ", packetRate=" + (long)(packets/durationSEC) + 
                                ", skips=" + skips );
        }

        private void getMessage( int msgSize, boolean clear ) throws Exception {
            if ( clear) _inBuf.clear();
            
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
    
    private void doRun( int numConsumers, int sendPerProducer, int port, int echoPort, int mask ) {
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
