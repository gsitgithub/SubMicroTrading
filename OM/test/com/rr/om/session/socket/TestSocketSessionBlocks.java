/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.socket;

import java.util.ArrayList;
import java.util.List;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.NullMessage;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.ConnectionListener;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.session.socket.SocketSession;
import com.rr.core.utils.Percentiles;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.om.processor.BaseProcessorTestCase;
import com.rr.om.warmup.FixTestUtils;

/**
 * @TODO refactor tests to use threads with CyclicBarrier / CountdownLatch .. and put check back in for non nio socket block
 */
public class TestSocketSessionBlocks extends BaseProcessorTestCase {

    private static final Logger _log    = LoggerFactory.console( TestSocketSessionBlocks.class );
    
    private final byte[] _buf1 = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
    private final byte[] _buf2 = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
    
    volatile boolean _clientConnected = false;
    volatile boolean _serverConnected = false;

    static class ServerHandler implements MessageHandler {

        public List<Message> _list = new ArrayList<Message>();
        
        @Override public void threadedInit() { /* nada */ }

        @Override
        public void handle( Message msg ) {
            handleNow( msg );
        }

        @Override
        public void handleNow( Message msg ) {
            Utils.delay( 10 );
            synchronized( _list ) {
                if ( msg != null && msg.getClass() != NullMessage.class ) {
                    _list.add( msg );
                }
            }
        }

        @Override public boolean canHandle() { return true; }

        @Override
        public String getComponentId() {
            return null;
        }
    }
    
    private ServerHandler _server = new ServerHandler();

    public void testNIODelayedWritesOnFullSocket() throws PersisterException {
        doSend( true );
    }
    
    public void doSend( boolean nio ) throws PersisterException {
        SocketSession client = createClient( nio );
        SocketSession server = createServer( nio );
        
        try {
            sendOrders( client, server );
        } finally {
            client.stop();
            server.stop();
        }

        _log.info( "Finished" );
    }

    private void sendOrders( SocketSession client, SocketSession server ) throws PersisterException {
        // start the sockets
        
        server.init();
        client.init();
        
        server.connect();
        client.connect();
        
        // check connected state at both ends
        long maxWait = 5000;
        
        long startConnect = System.currentTimeMillis();
        
        while( !_clientConnected || !_serverConnected ) {
        
            Utils.delay( 100 );
            
            if ( System.currentTimeMillis() - startConnect > maxWait ) {
                assertTrue( "Failed to connect", false );
            }
        }
        
        int numMsgsFromClient = 1000;
        int[] times = new int[ numMsgsFromClient ];

        _log.info( "Send " + numMsgsFromClient );

        for( int i=0 ; i < numMsgsFromClient ; ++i ) {
            // send fix messages
            
            ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _decoder, "TST000000" + i, 100 + i, 25.12, _upMsgHandler );
            
            long start = Utils.nanoTime();
            client.handle( cnos );
            long end = Utils.nanoTime();
            
            times[ i ] = (int) ((end - start) / 1000);
        }

        _log.info( "Sent All, delayed=" + client.getDelayedWriteCount() );

        assertTrue( client.getDelayedWriteCount() > 0 );
        
        
        // check received fix message
        
        boolean found = false;
        
        long last = System.currentTimeMillis();
        int  lastCount = 0;
        
        while( ! found ) {

            int count; 

            synchronized( _server._list ) { 
                
                count = _server._list.size(); 
            }
                    
            if ( count == numMsgsFromClient ) {
                found = true;
            } else if ( count == lastCount ) {
                if ( System.currentTimeMillis() - last > maxWait ) {
                    assertTrue( "Failed to find required msgs, found=" + count, false );
                }

                Utils.delay( 100 );
                
            } else {
                last = System.currentTimeMillis();
                lastCount = count;
            }
        }
        
        Percentiles p = new Percentiles( times );
        
        _log.info( "MicroSecond stats "      + " count=" + times.length + 
                            ", med=" + p.median()     + ", ave=" + p.getAverage() + 
                            ", min=" + p.getMinimum() + ", max=" + p.getMaximum() + 
                            "\n"                      +
                            ", p99=" + p.calc( 99 )   + ", p95=" + p.calc( 95 )   + 
                            ", p90=" + p.calc( 90 )   + ", p80=" + p.calc( 80 )   + 
                            ", p70=" + p.calc( 70 )   + ", p50=" + p.calc( 50 )   + "\n" );
    }

    private SocketSession createClient( boolean nio ) {
        String            name              = "TCLIENT";
        MessageRouter     inboundRouter     = new PassThruRouter( _proc ); 
        int               logHdrOut         = SocketSession.getDataOffset( name.toString(), false );
        Encoder           encoder           = FixTestUtils.getEncoder44( _buf1, logHdrOut ); 
        Decoder           decoder           = _decoder;
        Decoder           recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();
        ThreadPriority    receiverPriority  = ThreadPriority.Other; 
        SocketConfig      socketConfig      = new SocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 9901 ); 
        
        MessageDispatcher dispatcher        = new DirectDispatcher(); 

        socketConfig.setUseNIO( nio );
        socketConfig.setSoDelayMS( 0 );

        SocketSession sess = new SocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, recoveryDecoder, receiverPriority );

        dispatcher.setHandler( sess );
        
        sess.registerConnectionListener( new ConnectionListener(){
                                                    @Override
                                                    public void connected( Session session ) { _clientConnected = true; }

                                                    @Override
                                                    public void disconnected( Session session ) { _clientConnected = false; }
                                              } );

        return sess;
    }

    private SocketSession createServer( boolean nio ) {
        String            name              = "TSERVER3";
        MessageRouter     inboundRouter     = new PassThruRouter( _server ); 
        int               logHdrOut         = SocketSession.getDataOffset( name.toString(), false );
        Encoder           encoder           = FixTestUtils.getEncoder44( _buf2, logHdrOut ); 
        Decoder           decoder           = FixTestUtils.getDecoder44();
        Decoder           recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 
        SocketConfig      socketConfig      = new SocketConfig( EventRecycler.class, true, new ViewString("localhost"), null, 9901 ); 
        
        socketConfig.setUseNIO( nio );
        socketConfig.setSoDelayMS( 0 );
        
        MessageDispatcher dispatcher        = new DirectDispatcher(); 

        SocketSession sess = new SocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, recoveryDecoder, receiverPriority );
  
        dispatcher.setHandler( sess );

        sess.registerConnectionListener( new ConnectionListener(){
                                                    @Override
                                                    public void connected( Session session ) { _serverConnected = true; }
                                        
                                                    @Override
                                                    public void disconnected( Session session ) { _serverConnected = false; }
                                              } );
        
        return sess;
    }
}
