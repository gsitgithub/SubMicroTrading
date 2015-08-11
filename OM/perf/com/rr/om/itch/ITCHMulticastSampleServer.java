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

import com.rr.codec.emea.exchange.millenium.ITCHEncodeBuilderImpl;
import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Level;
import com.rr.core.log.LogEventLarge;
import com.rr.core.log.LoggerFactory;
import com.rr.core.os.SocketFactory;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.socket.LiteMulticastSocket;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;

// test using selector for reading from channel

public class ITCHMulticastSampleServer {

    String               _mcastGroupAddressOut    = "226.1.1.1";
    String               _mcastGroupAddressIn     = "226.1.1.2";

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
        _msgSize = Math.max( 50, Integer.parseInt( args[8] ) );

        ITCHMulticastSampleServer ps = new ITCHMulticastSampleServer();
        ps.doRun( 1, msgsToSend, 14880, 14981, mask, usecDelay );
        ps.doRun( 1, msgsToSend, 14880, 14981, mask, usecDelay );
        ps.doRun( 1, msgsToSend, 14880, 14981, mask, usecDelay );
        ps.doRun( 1, msgsToSend, 14880, 14981, mask, usecDelay );
        ps.doRun( 1, msgsToSend, 14880, 14981, mask, usecDelay );
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

        private final ITCHEncodeBuilderImpl _encoder; 
        private       int                   _out;
        private final byte[] _buf;
        private final int _msgsPerBuf;
        private final long _startMicros;
        private final byte _mktDataGrp = 'Z';

        private LogEventLarge logEvent =  new LogEventLarge();
        private ByteBuffer logBuf = ByteBuffer.allocate( 8192 );

        public Producer( String id, int send, int port, CyclicBarrier cb, int mask, int delayUSEC ) {
            _id = id;
            _send = send;
            _port = port;
            _cb = cb;
            _mask = mask;
            _delayUSEC = delayUSEC;
            
            _startMicros = Utils.nanoTime() / 1000;  
            _buf = new byte[_msgSize];
            _msgsPerBuf = Math.max(1,(_msgSize - 8) / 40);
            _encoder = new ITCHEncodeBuilderImpl( _buf, 0, new ReusableString("0") );
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
            sc.setPort( _port );
            sc.setServer( false );
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
            
            long startMS = System.currentTimeMillis();
            long count = 0;
            
            while( _out < max ) {
                long ts = Utils.nanoTime();
                send( _out, ts );

                count += _msgsPerBuf;
                ++_out;

                if ( _debug ) System.out.println( _id + " Sent id=" + _out + ", time=" + ts );

                if ( _delayUSEC > 0 ) {
                    Utils.delayMicros( _delayUSEC );
                }
            }

            long endMS = System.currentTimeMillis();
            long duration = (endMS - startMS);
            double durationSEC = duration / 1000.0;
            
            System.out.println( "Duration=" + duration + ", packets=" + _out + ", subMsgs=" + count + 
                                ", msgRate=" + (long)(count/durationSEC) + 
                                ", packetRate=" + (long)(_out/durationSEC) ); 
            
        }
        
        private void send( int idx, long nanoTime ) throws IOException {

            _encoder.clear();
            int nowMicros = (int) Math.abs( (Utils.nanoTime() / 1000) - _startMicros );
            
            _encoder.startMultiMsg( _msgsPerBuf );
            _encoder.encodeByte( _mktDataGrp  );
            
            _encoder.encodeInt( idx ); // seqNum

            long id = (idx << 16);
            
            byte side = 'S';
            double price = 100.005; 
            byte msgType = 'A';
            for( int i=0 ; i < _msgsPerBuf ; i++ ) {
                _encoder.nextMessage( msgType );
                _encoder.encodeInt( nowMicros );
                _encoder.encodeLong( id++ );        // orderId
                _encoder.encodeByte( side );
                _encoder.encodeInt( i + 100 );      // qty
                _encoder.encodeInt( idx & 0xFF );   // instID
                _encoder.encodeFiller( 2 );
                _encoder.encodePrice( price );
                _encoder.encodeFiller( 1 );
                
                side = (byte)((side == 'S') ? 'B' : 'S');
            }

            _encoder.end();

            _outBuf.clear();
            _outBuf.put( _encoder.getBuffer(), _encoder.getOffset(), _encoder.getLength() );
            _outBuf.flip();
            
            if ( _debug ) {
                logEvent.set( Level.info, new ViewString(_encoder.getBuffer(), _encoder.getOffset(), _encoder.getLength()), 0 );
                logBuf.clear();
                logEvent.encode( logBuf );
                System.out.println( "PACKET " + idx + ", size=" + _encoder.getLength() );
                System.out.println( new String(logBuf.array(), 0, logBuf.position()) );
            }
            
            writeSocket();
        }
    }
    
    private void doRun( int numProducers, int sendPerProducer, int port, int echoPort, int mask, int delayUSEC ) {
        
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
