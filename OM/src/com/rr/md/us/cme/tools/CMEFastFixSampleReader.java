/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.tools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;

import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.os.SocketFactory;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.socket.LiteMulticastSocket;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.md.fastfix.XMLFastFixTemplateLoader;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.template.FastFixTemplateClassRegister;
import com.rr.md.fastfix.template.TemplateClassRegister;
import com.rr.md.us.cme.WarmupCMECodec;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.recycle.MDIncRefreshRecycler;

/**
 * Sample Hand Coded template decoder
 * 
 * uses CMEFastFixReader
 * 

    $ tcpreplay --listnics
    Available network interfaces:
    Alias   Name    Description
    %0      \Device\NPF_{5B4E6144-C734-4047-AB8C-48A6BFAC81E0}
            MS LoopBack Driver
    %1      \Device\NPF_{94649F6E-31E4-48B1-8D1B-E5A8EB54A8BA}
            Microsoft
    
    # note the %1 is not the loopback adapter .... IF ALL FAILS TRY UNINSTALL THEN INSTALL FROM DEVICE MANAGER
    tcpreplay --intf1=%1 --oneatatime cme.ab.channel7-3Apr2012.8.30am.pcap
    
    # tcpreplay --intf1=%1 --topspeed cme.ab.channel7-3Apr2012.8.30am.pcap
 *
 */
public class CMEFastFixSampleReader {

    static final ErrorCode ERR_FF1 = new ErrorCode( "CMEFF01", "CME Error" );

    static String        _nicIP;

    static boolean       _spin;
    static boolean       _disableLookback;
    static boolean       _debug = false;
    static int           _qos;

    static final MetaTemplates meta = new MetaTemplates();
    
    public static void main( String args[] ) {
        
        LoggerFactory.setDebug( false );
        StatsMgr.setStats( new TestStats() );
        Map<String,String> p = new HashMap<String,String>();
        
        try {
            DummyAppProperties.testInit( p );
        } catch( Exception e ) {
            Assert.fail( e.getMessage() );
        }
        
        if ( args.length != 9 ) {
            System.out.println( "Args : msgsToRead  NIC       cpuMask  spin{1|0}  disableLoopback{1|0}  debug{1|0}  qos  poolSize  logStatsMod" );
            System.out.println( "eg     10000000    127.0.0.1 0        0          0                     1           2    100        100000" );
            System.exit(-1);
        }

        int msgsToSend = Integer.parseInt( args[0] );
        _nicIP = args[1];
        int mask       = Integer.parseInt( args[2] );
        _spin = Integer.parseInt( args[3] ) == 1;
        _disableLookback = Integer.parseInt( args[4] ) == 1; 
        _debug = Integer.parseInt( args[5] ) == 1; 
        _qos = Integer.parseInt( args[6] );

        int poolSize = Integer.parseInt( args[7] );
        int logStatsMod = Integer.parseInt( args[8] );

        LoggerFactory.initLogging( "./logs/CMEFastFixSampleReader.log", 1000000000 );        
        
        TemplateClassRegister reg = new FastFixTemplateClassRegister();
        XMLFastFixTemplateLoader l = new XMLFastFixTemplateLoader( "data/cme/templates.xml" );
        l.load( reg, meta );
        
        final Logger _log = LoggerFactory.create( CMEFastFixSampleReader.class );

        WarmupCMECodec warmup = new WarmupCMECodec( 50000 );
        try {
            warmup.warmup();
        } catch( Exception e ) {
            _log.warn( "Error in warmup " + e.getMessage() );
        }
        
//        String[] mcastAddrs = {
//            "224.000.026.001:10001",
//            "224.000.027.001:10001",
//            "224.000.026.019:11001",
//            "224.000.027.019:11001",
//            "224.000.026.037:12001",
//            "224.000.027.037:12001" 
//            };

        List<ZString[]> addrs = new ArrayList<ZString[]>();
        
//        addrs.add( new ZString[] { new ViewString("224.000.026.001") } );
        
        addrs.add( new ZString[] { new ViewString("224.000.026.001"), new ViewString("224.000.027.001") } );

        List<Integer> ports = new ArrayList<Integer>();

        ports.add( new Integer(10001) );
        
        CMEFastFixSampleReader ps = new CMEFastFixSampleReader();
        ps.doRun( 1, msgsToSend, ports, addrs, mask, poolSize, logStatsMod );
    }
    
    
    
    private class Consumer implements Runnable {

        private       LiteMulticastSocket  _inSocket = null;
        private final ByteBuffer           _inBuf  = ByteBuffer.allocateDirect( 8192 );
        private final ByteBuffer           _outBuf = ByteBuffer.allocateDirect( 8192 );
        private       String               _id;
        private final int                  _consume;
        private final CyclicBarrier        _cb;
        private final int                  _mask;
        private final ZString[]            _multicastGroups;
        private final int                  _port;
        
        private SuperPool<MDIncRefreshImpl> _mdIncSP      = SuperpoolManager.instance().getSuperPool( MDIncRefreshImpl.class );
        private final MDIncRefreshRecycler  _mdIncRecyler ;
        
        final Logger _log = LoggerFactory.create( CMEFastFixSampleReader.class );

        final ReusableString _errMsg = new ReusableString();
        
        long _startMS = 0;
        long _count = 0;
        long _packets = 0;
        long _gaps = 0;
        long _skips = 0;
        long _dups = 0;
        
        final CMEFastFixDecoder _decoder;

        long _endMS;
        long _duration;
        double _durationSEC;
        int _runIdx; 
        
        private byte[] _inBytes;

        private int _logStatsMod;
        
        public Consumer( String id, int consume, CyclicBarrier cb, int mask, ZString[] grps, int port, int poolSize, int logStatsMod ) {
            _id = id;
            _consume = consume;
            _cb = cb;
            _mask = mask;
            _inBytes = new byte[ 8192 ];
            _multicastGroups = grps;
            _port = port;
            _decoder = new CMEFastFixDecoder( "CMETstReader"+id, "data/cme/templates.xml", -1, true );
            _mdIncRecyler = new MDIncRefreshRecycler( poolSize, _mdIncSP );
            _logStatsMod = logStatsMod;
        }

        @Override
        public void run() {
            try {
                _cb.await();

                ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.HIGHEST, _mask ); 
    
                ShutdownManager.instance().register( new Callback(){
                    @Override
                    public void shuttingDown() {
                        logStats();
                    }} );
                
                inConnect();
                
                for( int i=1 ; i <= 5 ; i++ ) {
                    _runIdx = i;
                    consume( _consume );
                }
                
                inClose();
                
            } catch( Exception e ) {
                e.printStackTrace();
                Assert.fail( e.getMessage() );
            }
        }
        
        public void inConnect() throws IOException {

            SocketConfig sc = new SocketConfig();
            sc.setServer( false );
            sc.setPort( _port );
            sc.setDisableLoopback( _disableLookback );
            sc.setQOS( _qos );
            sc.setUseNIO( true );
            sc.setTTL( 1 );
            sc.setNic( new ViewString(_nicIP) );
            sc.setMulticast( true );
            sc.setMulticastGroups( _multicastGroups );
            
            _inSocket = SocketFactory.instance().createMulticastSocket( sc, _inBuf, _outBuf );
            _inSocket.configureBlocking( _spin == false );
        }

        public void inClose() {
            try { if ( _inSocket != null )  _inSocket.close();  } catch( Exception e ) { /* NADA */ }
        }
        
        void consume( int max ) throws Exception {
            
            int hdrSeqNum = -1;
            int tmpSeqNum = -1;
            
            while( _packets < max ) {
                getMessage( 8, true );
                
                // start time from start of first message read
                if ( _startMS == 0 ) _startMS = System.currentTimeMillis();
                
                int numBytes = _inBuf.position();
                _inBuf.flip();
                _inBuf.get(_inBytes, 0, numBytes );

                try {
                    Message dec = _decoder.decode( _inBytes, 0, numBytes );

                    if ( dec == null ) {
                        ++_skips;
                        continue;
                    }

                    _decoder.logLastMsg();
                    
                    // application handles gap processing
                    
                    tmpSeqNum = dec.getMsgSeqNum();
                    
                    if ( tmpSeqNum != (hdrSeqNum+1) && hdrSeqNum != -1 && tmpSeqNum > hdrSeqNum ) {
                        _errMsg.reset();
                        _errMsg.append( 'C' ).append( _id ).append( "  GAP detected lastSeqNum=" );
                        _errMsg.append( hdrSeqNum ).append( " received=" ).append( tmpSeqNum ).append( " gap of " ).append( tmpSeqNum-hdrSeqNum );
                        _log.info( _errMsg );
                        ++_gaps;
                    } 
                    
                    if ( tmpSeqNum > hdrSeqNum ) {
                        hdrSeqNum = tmpSeqNum;
                    }
                    
                    ++_packets;
   
                    if ( dec.getClass() == MDIncRefreshImpl.class ) {
                        _mdIncRecyler.recycle( (MDIncRefreshImpl) dec );
                    }
                    
                } catch( Exception e ) {
                    
                    _decoder.logError( _log, _errMsg, e );

                    ++_skips;
                }
                
                if ( _debug ) {
                    if ( _packets % _logStatsMod == 0 ) {
                        logStats();
                    }
                }
            }

            _endMS = System.currentTimeMillis();
            _duration = (_endMS - _startMS);
            _durationSEC = _duration / 1000.0;
        }

        void logStats() {
            _log.info( "STATS: Run " + _runIdx + ", duration=" + _duration + ", packets=" + _packets + ", subMsgs=" + _count + 
                                ", msgRate=" + (long)(_count/_durationSEC) + 
                                ", packetRate=" + (long)(_packets/_durationSEC) + 
                                ", skips=" + _skips + ", dups=" + _dups + ", gaps=" + _gaps );
            
            _decoder.logStats();
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
    
    private void doRun( int numConsumers, int sendPerProducer, List<Integer> ports, List<ZString[]> ipAddrs, int mask, int poolSize, int logStatsMod ) {
        int totalConsume = numConsumers * sendPerProducer;
        
        Consumer[] consumers = new Consumer[ numConsumers ];
        Thread[] tCons = new Thread[ numConsumers ];

        CyclicBarrier cb = new CyclicBarrier( numConsumers );
        
        for( int i=0; i < numConsumers ; ++i ) {
            int port = ports.get( i ).intValue();
            
            ZString[] grps = ipAddrs.get(  i  );
            consumers[i] = new Consumer( "C"+i, totalConsume, cb, mask, grps, port, poolSize, logStatsMod );
            tCons[i] = new Thread( consumers[i], "CONSUMER" + i );
        }
        
        System.gc();
        System.out.println( "STARTING" );
        
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
