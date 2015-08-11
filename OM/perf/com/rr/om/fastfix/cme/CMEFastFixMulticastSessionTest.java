/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.fastfix.cme;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.binary.fastfix.FastFixSocketSession;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.ConnectionListener;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.md.us.cme.CMEFastFixSession;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;
import com.rr.md.us.cme.writer.CMEFastFixEncoder;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.om.processor.BaseProcessorTestCase;

public class CMEFastFixMulticastSessionTest extends BaseProcessorTestCase {

    private static class EventHandler implements MessageHandler {

        private final String _name;
        private int _count = 0;
        
        public EventHandler( String name ) {
            super();
            _name = name;
        }

        @Override
        public String getComponentId() {
            return _name;
        }

        @Override
        public void threadedInit() {
            // nothing
        }

        @Override
        public void handle( Message msg ) {
            handleNow( msg );
        }

        @Override
        public synchronized void handleNow( Message msg ) {
            ++_count;
        }

        @Override
        public boolean canHandle() {
            return true;
        }

        public synchronized int count() {
            return _count;
        }
    }
    
    volatile boolean _clientConnected = false;
    volatile boolean _serverConnected = false;

    private ZString[] _grps = { new ViewString( "224.000.026.001" ) };

    public void testSendNIO() throws PersisterException {
        doSend( true, 10001, 10000 );
    }
    
    public void testSendBlocking() throws PersisterException {
        doSend( false, 10001, 10000 );
    }
    
    public void doSend( boolean nio, int serverPort, int count ) throws PersisterException {
        EventHandler eventHandler = new EventHandler( "StoreHandler" );

        FastFixSocketSession server = createServer( nio, serverPort, eventHandler );
        
        try {
            doRead( server, eventHandler, count );
        } finally {
            server.stop();
        }
    }

    private void doRead( FastFixSocketSession server, EventHandler storeHandler, int events ) throws PersisterException {
        server.setLogEvents( true );
        
        // start the sockets
        
        server.init();
        
        server.connect();
        
        // check connected state at both ends
        long maxWait = 30000;
        long start = System.currentTimeMillis();
        
        while( !_clientConnected || !_serverConnected ) {
        
            Utils.delay( 200 );
            
            if ( System.currentTimeMillis() - start > maxWait ) {
                assertTrue( "Failed to connect", false );
            }
        }
        
        // check received fix message
        
        boolean found = false;
        start = System.currentTimeMillis();
        
        while( ! found ) {
            
            Utils.delay( 200 );
            
            if ( System.currentTimeMillis() - start > maxWait ) {
                assertTrue( "Failed to find required msgs, found=" + _downQ.size(), false );
            }
            
            if ( storeHandler.count() == events ) {
                found = true;
            }
        }
    }

    private void setMCastSocketParams( SocketConfig socketConfig ) {
        socketConfig.setDisableLoopback( false );
        socketConfig.setQOS( 2 );
        socketConfig.setTTL( 1 );
        socketConfig.setMulticast( true );
        socketConfig.setMulticastGroups( _grps );
    }

    private FastFixSocketSession createServer( boolean nio, int serverPort, EventHandler store ) {
        String            name              = "TSERVER2";
        MessageRouter     inboundRouter     = new PassThruRouter( store ); 
        CMEFastFixEncoder encoder           = new CMEFastFixEncoder( "CMETstWriter", "data/cme/templates.xml", true );
        Decoder           decoder           = new CMEFastFixDecoder( "TstReader", "data/cme/templates.xml", -1, true );

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 
        SocketConfig      socketConfig      = new SocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, serverPort ); 
        
        socketConfig.setUseNIO( nio );
        socketConfig.setSoDelayMS( 0 );

        setMCastSocketParams( socketConfig );
        
        MessageDispatcher dispatcher        = new DirectDispatcher(); 
//        MessageDispatcher dispatcher        = new ThreadedDispatcher( new BlockingSyncQueue(), "SRV_DISPATCHER", ThreadPriority.Other ); 

        FastFixSocketSession sess = new CMEFastFixSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, receiverPriority );
  
        sess.setLogEvents( true );
        sess.setLogPojos( true );
        
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
