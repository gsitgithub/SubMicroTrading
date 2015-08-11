/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.component.SMTStartContext;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.thread.ControlThread;
import com.rr.core.thread.ExecutableElement;
import com.rr.core.thread.PipeLineable;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;

/**
 * single receiver thread used to service all registered sessions
 * 
 * @TODO add reconnection throttling and out of hours handling
 */
public class MultiSessionThreadedReceiver implements MultiSessionReceiver, ExecutableElement, PipeLineable {
    
            static final    Logger               _rlog  = LoggerFactory.create( MultiSessionThreadedReceiver.class );
            
    private                 AtomicBoolean        _stopping = new AtomicBoolean(false);
    private                 List<String>         _pipeLineIds = new ArrayList<String>();
    private                 NonBlockingSession[] _sessions = new NonBlockingSession[0];
    private                 int                  _nextSession;
    private        final    String               _id;
    private                 NonBlockingSession   _curSess = null;
                            boolean              _allDisconnected = true; // note not volatile
                   final    ControlThread        _ctlr;
                   final    Map<Session,SocketConnector> _connectors = new ConcurrentHashMap<Session,SocketConnector>( 64 );


    private final class SocketConnector extends Thread {
        private final NonBlockingSession _session;

        SocketConnector( final NonBlockingSession session ) {
            super( "SocketConnector-" + session.getComponentId() );
            setDaemon( true );
            _session            = session;
        }

        @Override
        public void run() {
            ThreadUtils.setPriority( this, ThreadPriority.Other );
            _rlog.info( "MultiSession ConnectorThread STARTED : " + _session.getComponentId() );
            
            try {
                long start = System.currentTimeMillis();
                
                _session.internalConnect(); 

                long duration = System.currentTimeMillis() - start;
                
                _rlog.info( "MultiSession ConnectorThread END " + _session.getComponentId() + ", status=" + _session.getSessionState().toString() + 
                            ", duration=" + duration );
                
                if ( _session.isConnected() ) _allDisconnected = false;
                
                _ctlr.statusChange(); // Mem barrier

            } catch( Exception e ) { // shouldnt be possible
                _rlog.info( "MultiSession ConnectorThread ERROR : " + _session.getComponentId() + " " + e.getMessage() );
            } finally {
                synchronized( _connectors ) {
                    _connectors.remove( _session );
                }
            }
        }

        public NonBlockingSession  getSession() {
            return _session;
        }
    }

    public MultiSessionThreadedReceiver( String receiverId, ControlThread ctlr ) {
        this( receiverId, ctlr, null );
    }
    
    public MultiSessionThreadedReceiver( String receiverId, ControlThread ctlr, String pipeLineIds ) {
        _id = receiverId;
        _ctlr = ctlr;
        
        _ctlr.register( this );
        
        setPipeIdList( pipeLineIds );
    }

    public ControlThread getControlThread() {
        return _ctlr;
    }
    
    @Override
    public String info() {
        return "MultiSessionThreadedReceiver( " + _id + " )";
    }
    
    @Override
    public synchronized void addSession( final NonBlockingSession session ) {
        NonBlockingSession[] newSessions = new NonBlockingSession[ _sessions.length + 1 ];
        
        int idx=0;
        while( idx < _sessions.length ) {
            newSessions[idx] = _sessions[idx];
            ++idx;
        }
        
        newSessions[ idx ] = session;
        
        _sessions = newSessions;
        
        /**
         * manual disconnect calls will not trigger a disconnectedException
         * for test code to work, trap disconnect events and kick off reconnect if open hours
         */
        session.registerConnectionListener( new ConnectionListener() {
                                            @SuppressWarnings( "synthetic-access" )
                                            @Override
                                            public void disconnected( Session disconnectedSession ) {
                                                nonBlockingConnect( (NonBlockingSession)disconnectedSession );
                                            }
                                            
                                            @Override
                                            public void connected( Session connectedSession ) {
                                                // dont care
                                            }
                                        });
    }

    @Override
    public int getNumSessions() {
        return _sessions.length;
    }
    
    @Override
    public void threadedInit() {
        for( int i=0 ; i < _sessions.length ; ++i ) {
            nonBlockingConnect( _sessions[ i ] );
        }
    }

    @Override
    public synchronized void start() {
        _ctlr.start();
    }
    
    @Override
    public synchronized void setStopping( boolean stopping ) {
        // request to stop ignored as its propogated from session which doesnt own the control thread
    }

    @Override
    public void stop() {
        if ( _stopping.compareAndSet( false, true ) ) {
            _ctlr.setStopping( true );
            
            Collection<SocketConnector> connSet = _connectors.values();
            SocketConnector[] connectors = connSet.toArray( new SocketConnector[ connSet.size() ] );
            
            for( SocketConnector conn : connectors ) {
                conn.getSession().stop();
                
                try {
                    conn.interrupt();
                } catch( Exception e ) {
                    // dont care
                }
            }
        }
    }
    
    @Override
    public synchronized boolean isStarted() {
        return _ctlr.isStarted();
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void execute() throws Exception {

        _curSess = _sessions[ _nextSession ];
        
        if ( ++_nextSession >= _sessions.length ) _nextSession = 0;
        
        if ( _curSess.getSessionState() == Session.State.Connected ) {
            _curSess.processNextInbound();
        }
    }

    @Override
    public void handleExecutionException( Exception ex ) {

        if ( ex instanceof SessionException ) {
            if ( _curSess != null ) {
                _curSess.logInboundError( ex );
                _curSess.disconnect( true );
            }
        } else if ( ex instanceof DisconnectedException ) {
            if ( _curSess != null ) {
                if ( ! _ctlr.isStopping() ) _curSess.logDisconnected( ex );
                _curSess.disconnect( true );
            }
        } else if ( ex instanceof IOException ) {
            if ( _curSess != null ) {
                if ( ! _ctlr.isStopping() ) _curSess.logInboundError( ex );
                _curSess.disconnect( true );
            }
        } else if ( ex instanceof RuntimeDecodingException ) {
            if ( _curSess != null ) {
                _curSess.logInboundDecodingError( (RuntimeDecodingException) ex );
            }
        } else if ( ex instanceof RuntimeEncodingException ) {
            if ( _curSess != null ) {
                _curSess.logOutboundEncodingError( (RuntimeEncodingException) ex );
            }
        } else {
            if ( _curSess != null ) {
                _curSess.logInboundError( ex );          // not a socket error dont drop socket
            }
        }
        
        updateDisconnectedStatus();
        
        nonBlockingConnect( _curSess );
    }

    private void updateDisconnectedStatus() {
        int                nextSession = 0;
        NonBlockingSession sess        = null;
        
        while( nextSession < _sessions.length ) {                    
            sess = _sessions[ nextSession ];

            if ( sess.getSessionState() == Session.State.Connected ) {
                _allDisconnected = false;
                return;
            }
            
            ++nextSession;
        }

        _allDisconnected = true;
    }

    @Override
    public void notReady() {
        int                nextSession = 0;
        NonBlockingSession sess        = null;
        
        while( nextSession < _sessions.length ) {                    
            sess = _sessions[ nextSession++ ];
            
            if ( sess.getSessionState() == Session.State.Connected ) {
                _allDisconnected = false;
            } else {
                if ( _connectors.get( sess ) == null ) {
                    nonBlockingConnect( sess );
                }
            }
        }
    }
    
    @Override
    public boolean checkReady() {
        return ( _allDisconnected == false );
    }

    public void setPipeIdList( String pipeIdList ) {
        List<String> pipeLineIds = new ArrayList<String>();
        
        if ( pipeIdList != null ) {
            String[] parts = pipeIdList.split( "," );
            
            for( String part : parts ) {
                part = part.trim();
                
                if ( part.length() > 0 ) {
                    pipeLineIds.add( part );
                }
            }
        }
        
        _pipeLineIds = pipeLineIds;
    }
    
    @Override
    public boolean hasPipeLineId( String pipeLineId ) {
        return _pipeLineIds.contains( pipeLineId );
    }
    
    @Override
    public List<String> getPipeLineIds() {
        return _pipeLineIds;
    }
    
    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }

    @Override
    public void prepare() {
        // nothing
    }

    @Override
    public void startWork() {
        start();
    }

    @Override
    public void stopWork() {
        stop();
    }
    
    private void nonBlockingConnect( NonBlockingSession session ) {
        if ( session != null ) {
            if ( session.getSessionState() == Session.State.Disconnected ) {
                if ( session.getConfig().isOpenUTC( System.currentTimeMillis() ) && ! session.isStopping() && ! session.isPaused() ) {
                    synchronized( _connectors ) {
                        if ( _connectors.get( session ) == null ) {
                            _rlog.info( "nonBlockingConnect connect thread started for : " + session.getComponentId() );
                            SocketConnector conn = new SocketConnector( session );
                            _connectors.put( session, conn );
                            conn.start();
                        } else {
                            _rlog.info( "nonBlockingConnect connect request ignored as already queued : " + session.getComponentId() );
                        }
                    }
                }
            }
        }
    }
}

