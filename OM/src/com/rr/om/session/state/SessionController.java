/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.state;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.NullMessage;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.session.socket.SessionControllerConfig;
import com.rr.core.session.socket.SessionStateException;
import com.rr.core.tasks.ScheduledEvent;
import com.rr.core.tasks.Scheduler;
import com.rr.model.generated.internal.events.factory.HeartbeatFactory;
import com.rr.model.generated.internal.events.impl.HeartbeatImpl;

public abstract class SessionController<T_SESSION_FACTORY extends StatefulSessionFactory> {

    protected static final ErrorCode ERR_MISSED_HB = new ErrorCode( "SCT100", "Missed heartbeats" );

    private final Logger _log = LoggerFactory.create( SessionController.class );

    private final class HeartbeatActor implements Scheduler.Callback {

        private final SuperPool<HeartbeatImpl> _heartbeatPool    = SuperpoolManager.instance().getSuperPool( HeartbeatImpl.class );
        private final HeartbeatFactory         _heartbeatFactory = new HeartbeatFactory( _heartbeatPool );

        private final ZString _name;
        
        public HeartbeatActor( String name ){
            _name = new ViewString( name );
        }
        
        @Override
        public void event( final ScheduledEvent event ) {

            if ( isLoggedOut() ) { 
            
                Scheduler.instance().cancelIndividual( ScheduledEvent.Heartbeat, this );
                
                return;
            }
            // logged in or syncing
            
            final HeartbeatImpl hb = _heartbeatFactory.get();
            hb.setMessageHandler( _session );

            if ( isLoggedIn() ) { 
                _session.handle( hb );
            } else{
                _session.handleForSync( hb );
            }
        }

        @Override
        public ZString getName() {
            return _name;
        }
    }
    
    private final class MissingHeartbeatActor implements Scheduler.Callback {

        private final ZString _name;
        
        public MissingHeartbeatActor( String name ){
            _name = new ViewString( name );
        }
        
        @Override
        public void event( final ScheduledEvent event ) {

            if ( isLoggedOut() ) { 
            
                Scheduler.instance().cancelIndividual( ScheduledEvent.Heartbeat, this );
                
                return;
            }
            // logged in or syncing
            checkMissingHeartbeat();
        }

        @Override
        public ZString getName() {
            return _name;
        }
    }
    
    protected final SeqNumSession            _session; 
    protected final SessionControllerConfig  _controllerConfig;
    protected final T_SESSION_FACTORY        _sessionFactory;
       
    private SessionState _loggedOutState;
    private SessionState _loggedInState;
    private SessionState _synchroniseState;

    private SessionState _state;
    
    private          long _heartbeatBreakTimeMS = Long.MAX_VALUE;
    private          long _lastRecvMsgMS        = 0;
    private volatile long _lastRecvHeartBeatMS  = Long.MAX_VALUE; // volatile as read from a timer thread

    private final    HeartbeatActor        _heartbeatActor;
    private final    MissingHeartbeatActor _missHBActor;

    public SessionController( SeqNumSession session, SessionStateFactory stateFactory, T_SESSION_FACTORY msgFactory ) {
        _controllerConfig  = session.getStateConfig();
        _session           = session;

        _sessionFactory    = msgFactory;
        
        _loggedOutState   = stateFactory.createLoggedOutState( session, this );
        _loggedInState    = stateFactory.createLoggedOnState( session, this );
        _synchroniseState = stateFactory.createSynchroniseState( session, this );

        _heartbeatActor = new HeartbeatActor( "HB_" + session.getComponentId().toString() );
        _missHBActor    = new MissingHeartbeatActor( "MHB_" + session.getComponentId().toString() );
        
        _state          = _loggedOutState; 
    }

    public void reset() {
        _log.info( "SessionController.reset " + _session.getComponentId() );
        
        _lastRecvHeartBeatMS  = Integer.MAX_VALUE;
        _heartbeatBreakTimeMS = Integer.MAX_VALUE;
    }

    public abstract void recoverContext( Message msg, boolean inBound );
    public abstract void outboundError();
    
    
    public void setHeartbeatReceived() {
        _lastRecvHeartBeatMS = System.currentTimeMillis();
    }
    
    public boolean isLoggedIn() {
        return _state == _loggedInState;
    }

    public boolean isLoggedOut() {
        return _state == _loggedOutState;
    }

    public void handle( Message msg ) throws SessionStateException {
        _lastRecvMsgMS = System.currentTimeMillis();
        _state.handle( msg );
    }

    public void enqueueHeartbeat( ZString testReqID ) {
        Message hb = _sessionFactory.getHeartbeat( testReqID );
        hb.setMessageHandler( _session );
        _session.handle( hb );
    }

    public void persistPosDupMsg( int newSeqNum ) {
        _session.persistLastInboundMesssage();
    }

    public SessionControllerConfig getControllerConfig() {
        return _controllerConfig;
    }

    public boolean isServer() {
        return _session.getStateConfig().isServer();
    }

    public SessionState getStateSynchronise() {
        return _synchroniseState;
    }

    public SessionState getStateLoggedIn() {
        return _loggedInState;
    }

    public SessionState getStateLoggedOut() {
        return _loggedOutState;
    }

    public synchronized void changeState( SessionState toState ) {
        
        if ( toState != _state ) {
            
            _log.info( "SessionController change state from " + _state.getClass().getSimpleName() + " to " + toState.getClass().getSimpleName() + 
                       " for " + _session.getComponentId() );
            
            if ( toState == _loggedOutState ) {
                onLogout();
            }
            
            _state = toState;

            _log.info( _session.getComponentId() + " " + info() );
            
            // WAKE UP QUEUES
            
            _session.handle( new NullMessage(_session) );
            _session.handleForSync( new NullMessage(_session) );
        }
    }

    protected void onLogout() {
        // for specialisation hooks
    }

    protected final void send( Message msg, boolean now ) {
        if ( now ) {
            _session.handleNow( msg );
        } else {
            msg.setMessageHandler( _session );
            if ( isLoggedIn() ) {
                _session.handle( msg );
            } else {
                _session.handleForSync( msg );
            }
        }
    }

    public void startHeartbeatTimer( int heartBtIntSecs ) {
        
        _heartbeatBreakTimeMS = heartBtIntSecs * 3000; // upto 3 missed HB fine
        
        _lastRecvHeartBeatMS = System.currentTimeMillis();
        
        long nextFireMS     = (heartBtIntSecs * 1000);
        
        Scheduler.instance().registerIndividualRepeating( ScheduledEvent.Heartbeat, _heartbeatActor, nextFireMS,     nextFireMS );
        Scheduler.instance().registerIndividualRepeating( ScheduledEvent.Heartbeat, _missHBActor,    nextFireMS+100, nextFireMS+100 );
    }

    void checkMissingHeartbeat() {
        final long nowMS = System.currentTimeMillis();
        final long ageHeartbeatMS = nowMS - _lastRecvHeartBeatMS;
        final long ageLastMsgMS   = nowMS - _lastRecvMsgMS;
        
        // only disconnect if missed HBs and not getting active messages
        if ( ageHeartbeatMS > _heartbeatBreakTimeMS && ageLastMsgMS > _heartbeatBreakTimeMS ) {
            if ( _controllerConfig.isDisconnectOnMissedHB() ) {
                _log.error( ERR_MISSED_HB, _session.getComponentId() );
                
                Message lo = formLogoutMessage();
                lo.setMessageHandler( _session );
                
                _session.handle( lo );
                
                _session.disconnect( true );
                
            } else {
                _log.error( ERR_MISSED_HB, _session.getComponentId() );
            }
        }
    }

    protected Message formLogoutMessage() {
        Message lo = _sessionFactory.getLogOut( ERR_MISSED_HB.getError(), 0, null, 0, 0 );
        return lo;
    }

    public void connected() {
        _state.connected();
    }
    
    public void stop() {
        // forced stop, close any extra resources
    }

    public String info() {
        return ", state=" + _state.getClass().getSimpleName();
    }
    
    public String logOutboundRecoveryFinished() {
        return "";
    }

    public String logInboundRecoveryFinished() {
        return "";
    }

    String getState() {
        return _state.getClass().getSimpleName();
    }
    
    protected final Session getSession() {
        return _session;
    }
}