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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import org.junit.Assert;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.Dictionaries;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandReaderCopy;
import com.rr.core.collections.IntHashMap;
import com.rr.core.collections.IntMap;
import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.os.SocketFactory;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.socket.LiteMulticastSocket;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.md.fastfix.XMLFastFixTemplateLoader;
import com.rr.md.fastfix.meta.MetaTemplate;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.reader.FastFixToFixReader;
import com.rr.md.fastfix.template.FastFixTemplateClassRegister;
import com.rr.md.fastfix.template.TemplateClassRegister;

// test using base components directly

public class CMEFastFixSoftSampleClient {

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
        
        // p.put( CoreProps.HACKED_LINUX_SOCKETS, "true" );
        
        try {
            DummyAppProperties.testInit( p );
        } catch( Exception e ) {
            Assert.fail( e.getMessage() );
        }
        
        if ( args.length != 7 ) {
            System.out.println( "Args : msgsToRead  NIC  cpuMask  spin{1|0}  disableLoopback{1|0}  debug{1|0}  qos" );
            System.exit(-1);
        }

        int msgsToSend = Integer.parseInt( args[0] );
        _nicIP = args[1];
        int mask       = Integer.parseInt( args[2] );
        _spin = Integer.parseInt( args[3] ) == 1;
        _disableLookback = Integer.parseInt( args[4] ) == 1; 
        _debug = Integer.parseInt( args[5] ) == 1; 
        _qos = Integer.parseInt( args[6] );

        LoggerFactory.initLogging( "./logs/CMEFastFixSampleClient.log", 100000000 );        
        
        TemplateClassRegister reg = new FastFixTemplateClassRegister();
        XMLFastFixTemplateLoader l = new XMLFastFixTemplateLoader( "data/cme/templates.xml" );
        l.load( reg, meta );
        
        String[] mcastAddrs = {
            "224.000.026.001:10001",
            "224.000.026.019:11001",
            "224.000.026.037:12001",
            "224.000.027.001:10001",
            "224.000.027.019:11001",
            "224.000.027.037:12001" };
        
        CMEFastFixSoftSampleClient ps = new CMEFastFixSoftSampleClient();
        ps.doRun( 1, msgsToSend, mcastAddrs, mask );
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
        
        private final Dictionaries              _dictionaries = new Dictionaries();
        private final IntMap<FastFixToFixReader>_readers = new IntHashMap<FastFixToFixReader>( 200, 0.75f );

        final Logger log = LoggerFactory.create( CMEFastFixSoftSampleClient.class );

        final ReusableString errMsg = new ReusableString();
        
        long startMS = 0;
        long count = 0;
        long packets = 0;
        long skips = 0;
        
        final FastFixDecodeBuilder decoder = new FastFixDecodeBuilder();
        final PresenceMapReader pMap = new PresenceMapReader();
        final ReusableString destFixMsg = new ReusableString();
        
        final UIntMandReaderCopy templateIdReader = new UIntMandReaderCopy( "TemplateId", 0, 0 );


        long endMS;
        long duration;
        double durationSEC;
        int runIdx; 
        
        private byte[] _inBytes;

        int numMsgs;
        int dups = 0;
        
        public Consumer( String id, int consume, CyclicBarrier cb, int mask, ZString[] grps, int port ) {
            _id = id;
            _consume = consume;
            _cb = cb;
            _mask = mask;
            _inBytes = new byte[ 8192 ];
            _multicastGroups = grps;
            _port = port;
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
                    runIdx = i;
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
            sc.setTTL( 1 );
            sc.setNic( new ViewString(_nicIP) );
            sc.setMulticastGroups( _multicastGroups );
            sc.setMulticast( true );
            
            _inSocket = SocketFactory.instance().createMulticastSocket( sc, _inBuf, _outBuf );
            _inSocket.configureBlocking( _spin == false );
        }

        public void inClose() {
            try { if ( _inSocket != null )  _inSocket.close();  } catch( Exception e ) { /* NADA */ }
        }
        
        void consume( int max ) throws Exception {
            
            int hdrSeqNum = -1;
            int tmpSeqNum = -1;
            
            while( packets < max ) {
                getMessage( 8, true );
                
                // start time from start of first message read
                if ( startMS == 0 ) startMS = System.currentTimeMillis();
                
                int numBytes = _inBuf.position();
                _inBuf.flip();
                _inBuf.get(_inBytes, 0, numBytes );

                destFixMsg.reset();
                
                int templateId = 0;

                try {
                    decoder.start( _inBytes, 0, numBytes );

                    // 4 byte sequence , 1 byte channel
                    
                    tmpSeqNum = decoder.decodeSeqNum();
                    byte subchannel  = decoder.decodeChannel();
                    
                    if ( tmpSeqNum <= hdrSeqNum ) {
                        ++dups;
                        continue;
                    }
                    
                    if ( tmpSeqNum != (hdrSeqNum+1) && hdrSeqNum != -1 ) {
                        gap( tmpSeqNum, hdrSeqNum );
                    }
                    
                    hdrSeqNum = tmpSeqNum;
                    
                    pMap.readMap( decoder );
                    
                    templateId = templateIdReader.read( decoder, pMap ); // its only two bytes but int decoder is fine

                    destFixMsg.append( 'C' ).append( _id );
                    destFixMsg.append( ", [" ).append( hdrSeqNum ).append( "] [s#" ).append( (int)subchannel ).append( "] [t#" ).append( templateId ).append( "] ");
                    
                    FastFixToFixReader reader = getReader( templateId );

                    if ( reader != null ) {
                        reader.reset();
   
                        reader.read( decoder, pMap, destFixMsg );
                        
                        ++packets;
   
                        log.info( destFixMsg );
                        
                        count += numMsgs;
                    } else {
                        destFixMsg.append( " SKIPPING templateId=" ).append( templateId );
                        log.info( destFixMsg );
                        
                        ++skips;
                    }
                } catch( RuntimeDecodingException e ) {
                    destFixMsg.append( "EXCEPTION SKIPPING templateId=" ).append( templateId ).append( " : ").append( e.getMessage() );
                    log.error( ERR_FF1, destFixMsg, e );
                    log.infoLargeAsHex( e.getFixMsg(), 0 );

                    ++skips;
                } catch( Exception e ) {
                    destFixMsg.append( "EXCEPTION SKIPPING templateId=" ).append( templateId ).append( " : ").append( e.getMessage() );
                    log.error( ERR_FF1, destFixMsg, e );
                    decoder.dump( errMsg );
                    log.info( errMsg );

                    ++skips;
                }
            }

            endMS = System.currentTimeMillis();
            duration = (endMS - startMS);
            durationSEC = duration / 1000.0;
        }

        void logStats() {
            log.info( "Run " + runIdx + ", duration=" + duration + ", packets=" + packets + ", subMsgs=" + count + 
                                ", msgRate=" + (long)(count/durationSEC) + 
                                ", packetRate=" + (long)(packets/durationSEC) + 
                                ", skips=" + skips + ", dups=" + dups );
        }

        private void gap( int newSeqNum, int lastSeqNum ) {
            errMsg.reset();
            errMsg.append( "GAP DETECTED lastSeqNum=" ).append( lastSeqNum ).append( ", newSeqNum=" ).append( newSeqNum );
            
            log.warn( errMsg );
        }

        private FastFixToFixReader getReader( int templateId ) {
            MetaTemplate mt = meta.getTemplate( templateId );
            if ( mt == null ) return null;
            
            FastFixToFixReader reader = _readers.get( templateId );
            
            if ( reader == null ) {
                reader = new FastFixToFixReader( mt, "T" + templateId, templateId, (byte) '|' );
                
               _readers.put( templateId, reader );
               
               reader.init( _dictionaries.getMsgTypeDictComponentFactory( mt.getDictionaryId() ) );
            }
            
            return reader;
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
        
        // p.put( CoreProps.HACKED_LINUX_SOCKETS, "true" );
        
        try {
            DummyAppProperties.testInit( p );
        } catch( Exception e ) {
            Assert.fail( e.getMessage() );
        }
    }
    
    private void doRun( int numConsumers, int sendPerProducer, String[] mcastAddrs, int mask ) {
        int totalConsume = numConsumers * sendPerProducer;
        
        Consumer[] consumers = new Consumer[ numConsumers ];
        Thread[] tCons = new Thread[ numConsumers ];

        CyclicBarrier cb = new CyclicBarrier( numConsumers );
        
        for( int i=0; i < numConsumers ; ++i ) {
            String mcastAddr= mcastAddrs[i];
            String[] mcastParts = mcastAddr.split( ":" );
            int port = Integer.parseInt( mcastParts[1] );
            ZString[] grps = new ViewString[1];
            grps[0] = new ViewString( mcastParts[0] );
            consumers[i] = new Consumer( "C"+i, totalConsume, cb, mask, grps, port );
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
