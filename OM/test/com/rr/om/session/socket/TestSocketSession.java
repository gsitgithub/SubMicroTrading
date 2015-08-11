/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.socket;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.ConnectionListener;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.session.socket.SocketSession;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.om.processor.BaseProcessorTestCase;
import com.rr.om.warmup.FixTestUtils;

public class TestSocketSession extends BaseProcessorTestCase {

    
    private final byte[] _buf1 = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
    private final byte[] _buf2 = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
    
    volatile boolean _clientConnected = false;
    volatile boolean _serverConnected = false;

    public void testSendNIOWithClientLocalBind() throws PersisterException {
        doSend( true, 14220, 14221 );
    }
    
    public void testSendNIO() throws PersisterException {
        doSend( true, 14223, 0 );
    }
    
    public void testSendBlockingWithClientLocalBind() throws PersisterException {
        doSend( false, 14226, 14227 );
    }
    
    public void testSendBlocking() throws PersisterException {
        doSend( false, 14228, 0 );
    }
    
    public void doSend( boolean nio, int serverPort, int clientLocalPort ) throws PersisterException {
        SocketSession client = createClient( nio, serverPort, clientLocalPort );
        SocketSession server = createServer( nio, serverPort );
        
        try {
            doActualSend( client, server );
        } finally {
            client.stop();
            server.stop();
        }
    }

    private void doActualSend( SocketSession client, SocketSession server ) throws PersisterException {
        client.setLogEvents( true );
        server.setLogEvents( true );
        
        // start the sockets
        
        server.init();
        client.init();
        
        server.connect();
        client.connect();
        
        // check connected state at both ends
        long maxWait = 2000;
        long start = System.currentTimeMillis();
        
        while( !_clientConnected || !_serverConnected ) {
        
            Utils.delay( 200 );
            
            if ( System.currentTimeMillis() - start > maxWait ) {
                assertTrue( "Failed to connect", false );
            }
        }
        
        int numMsgsFromClient = 5;
        
        for( int i=0 ; i < numMsgsFromClient ; ++i ) {
            // send fix messages
            
            ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _decoder, "TST000000" + i, 100 + i, 25.12, _upMsgHandler );
            
            client.handle( cnos );
        }
        
        // check received fix message
        
        boolean found = false;
        start = System.currentTimeMillis();
        MarketNewOrderSingleImpl mnos = null;
        
        while( ! found ) {
            
            Utils.delay( 200 );
            
            synchronized( _downQ ) { /* force mem barrier  */ }
            
            if ( System.currentTimeMillis() - start > maxWait ) {
                assertTrue( "Failed to find required msgs, found=" + _downQ.size(), false );
            }
            
            if ( _downQ.size() == numMsgsFromClient ) {
                mnos = (MarketNewOrderSingleImpl) getMessage( _downQ, MarketNewOrderSingleImpl.class );
                found = true;
            }
        }
        
        assertNotNull( mnos );
    }

    private SocketSession createClient( boolean nio, int serverPort, int clientLocalPort ) {
        String            name              = "TCLIENT";
        MessageRouter     inboundRouter     = new PassThruRouter( _proc ); 
        int               logHdrOut         = SocketSession.getDataOffset( name.toString(), false );
        Encoder           encoder           = FixTestUtils.getEncoder44( _buf1, logHdrOut ); 
        Decoder           decoder           = _decoder;
        Decoder           recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();
        ThreadPriority    receiverPriority  = ThreadPriority.Other; 
        SocketConfig      socketConfig      = new SocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, serverPort ); 
        
        if ( clientLocalPort != 0 ) {
            socketConfig.setLocalPort( clientLocalPort );
            socketConfig.setNic( new ViewString( "lo" ) );
        }
        
        //MessageDispatcher dispatcher        = new SessionThreadedDispatcher( new BlockingSyncQueue(), "CLIENT_DISPATCHER", ThreadPriority.Other ); 
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

    private SocketSession createServer( boolean nio, int serverPort ) {
        String            name              = "TSERVER2";
        MessageRouter     inboundRouter     = new PassThruRouter( _proc ); 
        int               logHdrOut         = SocketSession.getDataOffset( name.toString(), false );
        Encoder           encoder           = FixTestUtils.getEncoder44( _buf2, logHdrOut ); 
        Decoder           decoder           = FixTestUtils.getDecoder44();
        Decoder           recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 
        SocketConfig      socketConfig      = new SocketConfig( EventRecycler.class, true, new ViewString("localhost"), null, serverPort ); 
        
        socketConfig.setUseNIO( nio );
        socketConfig.setSoDelayMS( 0 );
        
//        MessageDispatcher dispatcher        = new SessionThreadedDispatcher( new BlockingSyncQueue(), "SRV_DISPATCHER", ThreadPriority.Other ); 
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
