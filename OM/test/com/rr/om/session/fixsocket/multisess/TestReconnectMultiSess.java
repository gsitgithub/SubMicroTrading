/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.fixsocket.multisess;

import java.io.IOException;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.utils.FileException;
import com.rr.core.utils.Utils;
import com.rr.om.warmup.WarmupMultiFixSocketSession;

public class TestReconnectMultiSess extends BaseTestCase {

    private WarmupMultiFixSocketSession _sess;
    private Session                     _clientSess;
    private Session                     _omToExSess;
    private Session                     _omToClientSess;
    private Session                     _exSess;
    
    static {
        LoggerFactory.setDebug( false );
        LoggerFactory.initLogging( "./logs/TestReconnect.log", 1000000 );        
    }
    
    static final Logger _log  = LoggerFactory.create( TestReconnectMultiSess.class );
    
    public void testDropExchange() throws Exception {
        try {
            configureSessions( "A", false );
            
            _sess.getDispatcher().start();
            _sess.getReciever().start();  // THIS WILL RUN CONNECT ON REGISTERED SESSIONS
            
            waitLogonExchange();
            
            assertTrue( _exSess.isLoggedIn() );

            _log.info( "Force disconnect on exSess" );
            _exSess.disconnect( true );
            
            waitLogonExchange();
            assertTrue( _exSess.isLoggedIn() );
            
        } finally {

            try {
                _sess.close();
            } catch( Exception e2 ) {
                _log.warn( "Exception cleaning up " + getName() + " : " + e2.getMessage() + " (" + e2.getClass().getSimpleName() + ")" );
            }
            
            LoggerFactory.flush();
        }
    }

    public void testDropOMtoExchange() throws Exception {
        try {
            configureSessions( "B", false );
            
            _sess.getDispatcher().start();
            _sess.getReciever().start();  // THIS WILL RUN CONNECT ON REGISTERED SESSIONS
            
            waitLogonExchange();
            
            assertTrue( _exSess.isLoggedIn() );

            _log.info( "Force disconnect on exSess" );
            _omToExSess.disconnect( true );
            
            waitLogonExchange();
            assertTrue( _omToExSess.isLoggedIn() );
            
        } finally {

            try {
                _sess.close();
            } catch( Exception e2 ) {
                _log.warn( "Exception cleaning up " + getName() + " : " + e2.getMessage() + " (" + e2.getClass().getSimpleName() + ")" );
            }
            
            LoggerFactory.flush();
        }
    }

    public void testReconnectAll() throws Exception {
        try {
            configureSessions( "C", true );

            _sess.getDispatcher().start();
            _sess.getReciever().start();  // THIS WILL RUN CONNECT ON REGISTERED SESSIONS
            
            waitLogonExchange();
            waitLogonClients();
            
            assertTrue( _omToExSess.isLoggedIn() );
            assertTrue( _exSess.isLoggedIn() );
            assertTrue( _clientSess.isLoggedIn() );
            assertTrue( _omToClientSess.isLoggedIn() );

            _exSess.disconnect( true );
            waitLogonExchange();
            assertTrue( _exSess.isLoggedIn() );
            assertTrue( _omToExSess.isLoggedIn() );
            
            _clientSess.disconnect( true );
            waitLogonClients();
            assertTrue( _clientSess.isLoggedIn() );
            assertTrue( _omToClientSess.isLoggedIn() );
            
            _clientSess.disconnect( true );
            waitLogonClients();
            assertTrue( _clientSess.isLoggedIn() );
            assertTrue( _omToClientSess.isLoggedIn() );

        } finally {

            try {
                _sess.close();
            } catch( Exception e2 ) {
                _log.warn( "Exception cleaning up " + getName() + " : " + e2.getMessage() + " (" + e2.getClass().getSimpleName() + ")" );
            }
            
            LoggerFactory.flush();
            Utils.delay( 1000 ); 
        }
    }

    private void configureSessions( String idPostfix, boolean initClient ) throws SessionException, FileException, PersisterException, IOException {
        _sess = WarmupMultiFixSocketSession.create( "testReconnect" + idPostfix, 0, false, 1000 );
        
        _sess.setMaxRunTime( 20000 );
        _sess.setEventLogging( true );
        _sess.init( initClient );
        _sess.recover();
        
        _clientSess = _sess.getClientSession();
        _omToExSess = _sess.getOMtoExSession();
        _omToClientSess = _sess.getOMtoClientSession();
        _exSess = _sess.getExchangeSession();
    }
    
    public void waitLogonExchange() {
        for( int i=0; i < 10 ; i++ ) {
            Utils.delay( 1000 );
            synchronized( this ) {
                if ( _omToExSess.isLoggedIn() && _exSess.isLoggedIn() ) {
                    break;
                }
            }
        }
    }

    public void waitLogonClients() {
        for( int i=0; i < 10 ; i++ ) {
            Utils.delay( 1000 );
            synchronized( this ) {
                if ( _omToClientSess.isLoggedIn() && _clientSess.isLoggedIn() ) {
                    break;
                }
            }
        }
    }
}
