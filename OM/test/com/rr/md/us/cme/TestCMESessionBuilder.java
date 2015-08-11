/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme;

import java.util.List;

import com.rr.algo.t1.DummyExchangeSession;
import com.rr.core.codec.binary.fastfix.NonBlockingFastFixSocketSession;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ViewString;
import com.rr.core.model.MessageHandler;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.thread.ControlThread;
import com.rr.core.thread.SingleElementControlThread;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ThreadPriority;
import com.rr.md.fastfix.FastSocketConfig;
import com.rr.md.us.cme.builder.CMERoundRobinSessionBuilder;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.om.session.SessionManager;


public class TestCMESessionBuilder extends BaseTestCase {

    private CMEConfig _cfg;
    
    @Override
    public void setUp() {
        XMLCMEConfigLoader loader = new XMLCMEConfigLoader( "./data/cme/config.xml" );
        _cfg = loader.load();
    }
    
    public void testNoHandlers() {
        
        try {
            SessionManager sessMgr = new SessionManager( "testSM" ); 
            MessageHandler[] handlers = new MessageHandler[0];
            MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 1 );
            FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
            CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7,10", sessMgr, handlers, multiplexReceivers, socketConfig, null );
            
            b.create();
            
            fail( "Should of thrown exception" );
            
        } catch( SMTRuntimeException e ) {
            // expected
        }
    }

    private MultiSessionThreadedReceiver[] makeMultiplexReceivers( int tot ) {
        MultiSessionThreadedReceiver[] h = new MultiSessionThreadedReceiver[ tot ];
        
        for( int i=0 ; i < tot ; i++ ) {
            ControlThread t = new SingleElementControlThread( "CTL" + i, ThreadPriority.Other );
            h[i] = new MultiSessionThreadedReceiver( "MREC" + i, t );
        }
        
        return h;
    }

    private MessageHandler[] makeHandlers( int tot ) {
        
        MessageHandler[] h = new MessageHandler[ tot ];
        
        for( int i=0 ; i < tot ; i++ ) {
            h[i] = new DummyExchangeSession();
        }
        
        return h;
    }

    public void testInvalidChannel() {
        
        try {
            SessionManager sessMgr = new SessionManager( "testSM" ); 
            MessageHandler[] handlers = makeHandlers( 1 );
            MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 1 );
            FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
            CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "9999999", sessMgr, handlers, multiplexReceivers, socketConfig, null );

            b.create();
            
            fail( "Should of thrown exception" );
            
        } catch( SMTRuntimeException e ) {
            // expected
        }
    }

    public void testOneChannels() {
        
        SessionManager sessMgr = new SessionManager( "testSM" ); 
        MessageHandler[] handlers = makeHandlers( 1 );
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 1 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7", sessMgr, handlers, multiplexReceivers, socketConfig, null );

        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        assertEquals( 3, sessions.size() );
        assertEquals( 3, multiplexReceivers[0].getNumSessions() );
    }

    public void testTwoChannels() {
        
        SessionManager sessMgr = new SessionManager( "testSM" ); 
        MessageHandler[] handlers = makeHandlers( 1 );
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 1 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7, 10", sessMgr, handlers, multiplexReceivers, socketConfig, null );

        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        assertEquals( 6, sessions.size() );
        assertEquals( 6, multiplexReceivers[0].getNumSessions() );
    }

    public void testFiveChannels() {
        
        SessionManager sessMgr = new SessionManager( "testSM" ); 
        MessageHandler[] handlers = makeHandlers( 1 );
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 1 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7, 10, 9, 13, 121", sessMgr, handlers, multiplexReceivers, socketConfig, null );

        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        assertEquals( 15, sessions.size() );
        assertEquals( 15, multiplexReceivers[0].getNumSessions() );
    }

    public void testTwoHandlers() {
        SessionManager sessMgr = new SessionManager( "testSM" ); 
        MessageHandler[] handlers = makeHandlers( 2 );
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 1 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7, 10, 9, 13, 121", sessMgr, handlers, multiplexReceivers, socketConfig, null );

        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        assertEquals( 15, sessions.size() );
        assertEquals( 15, multiplexReceivers[0].getNumSessions() );
    }

    public void testFourHandlers() {
        SessionManager sessMgr = new SessionManager( "testSM" ); 
        MessageHandler[] handlers = makeHandlers( 4 );
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 1 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7, 10, 9, 13, 121", sessMgr, handlers, multiplexReceivers, socketConfig, null );

        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        assertEquals( 15, sessions.size() );
        assertEquals( 15, multiplexReceivers[0].getNumSessions() );
    }

    public void testTwoMultiplexReceivers() {
        SessionManager sessMgr = new SessionManager( "testSM" ); 
        MessageHandler[] handlers = makeHandlers( 1 );
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 2 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7, 10, 9, 13, 121", sessMgr, handlers, multiplexReceivers, socketConfig, null );

        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        assertEquals( 15, sessions.size() );
        assertEquals( 9, multiplexReceivers[0].getNumSessions() );
        assertEquals( 6, multiplexReceivers[1].getNumSessions() );
    }

    public void testThreeMultiplexReceivers() {
        SessionManager sessMgr = new SessionManager( "testSM" ); 
        MessageHandler[] handlers = makeHandlers( 1 );
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 3 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7, 10, 9, 13, 121", sessMgr, handlers, multiplexReceivers, socketConfig, null );

        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        assertEquals( 15, sessions.size() );
        assertEquals( 6, multiplexReceivers[0].getNumSessions() );
        assertEquals( 6, multiplexReceivers[1].getNumSessions() );
        assertEquals( 3, multiplexReceivers[2].getNumSessions() );
    }

    public void testThreeMultiplexReceiversAndHandlers() {
        SessionManager sessMgr = new SessionManager( "testSM" ); 
        MessageHandler[] handlers = makeHandlers( 3 );
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( 3 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMETestSessBldr", _cfg, "7, 10, 9, 13, 121", sessMgr, handlers, multiplexReceivers, socketConfig, null );

        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        assertEquals( 15, sessions.size() );
        assertEquals( 6, multiplexReceivers[0].getNumSessions() );
        assertEquals( 6, multiplexReceivers[1].getNumSessions() );
        assertEquals( 3, multiplexReceivers[2].getNumSessions() );
    }
}
