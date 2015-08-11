/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium;

import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.session.socket.SessionStateException;
import com.rr.om.emea.exchange.millenium.recovery.MilleniumRecoveryController;
import com.rr.om.session.state.SessionController;
import com.rr.om.session.state.SessionStateFactory;
import com.rr.om.session.state.StatefulSessionFactory;

/**
 * based on native trading gateway issue 7.0 Dec 2010
 */

public class MilleniumController extends SessionController<MilleniumSessionFactory> {
    
    private   static final Logger _log = LoggerFactory.create( MilleniumController.class );

    protected static final int   MAX_SEQ_NUM      = 256;
    protected        final int[] _lastAppIdSeqNum = new int[ MAX_SEQ_NUM ];
    
    private MilleniumRecoveryController _recoveryController = null;
    
    public MilleniumController( SeqNumSession session, MilleniumSocketConfig config ) {
        super( session, new MilleniumStateFactory( config ), new MilleniumSessionFactory( config ) ); 
    }

    protected MilleniumController( SeqNumSession session, SessionStateFactory stateFactory, StatefulSessionFactory msgFactory ) {
        super( session, stateFactory, (MilleniumSessionFactory) msgFactory ); // only invoked from the recovery controller
    }

    @Override
    public final void reset() {
        super.reset();
        
        for( int i=0 ; i < MAX_SEQ_NUM ; i++ ) {
            _lastAppIdSeqNum[  i ] = -1;
        }
    }

    public void setRecoveryController( MilleniumRecoveryController recoveryController ) {
        _recoveryController = recoveryController;
    }
    
    public final void handle( final Message msg, final byte bAppId ) throws SessionStateException {
    
        final int appId = 0xFF & bAppId;
        
        if ( appId >= 0 ) {
            storeMaxSeqNum( msg, appId );
        }
        
        handle( msg );
    }

    public final void recoverContext( Message msg, boolean inBound, byte bAppId ) {

        final int appId = 0xFF & bAppId;
        
        if ( inBound && appId >= 0 ) {
            storeMaxSeqNum( msg, appId );
        }
    }

    @Override
    public final void recoverContext( Message msg, boolean inBound ) {
        // not used, the context appId requires special override
    }

    @Override
    public final void outboundError() {
        // nothing
    }

    public final void sendLogonReplyNow( ZString msg, int rejectCode ) {
        Message logon = _sessionFactory.getLogOut( msg, rejectCode, null, 0, 0 );
        send( logon, true );
    }

    public final void sendLogonNow() {
        Message logon = _sessionFactory.getSessionLogOn( 0, 0, 0 );
        send( logon, true );
    }

    public void initiateRecovery() {
        if ( _recoveryController != null ) {
            _log.info( "MilleniumController.initiateRecovery()" );            
            _recoveryController.checkMaxSeqNums( _lastAppIdSeqNum );
            _recoveryController.getSession().connect();
        }
    }
    
    @Override
    public void stop() {
        super.stop();
        if ( _recoveryController != null ) {
            _log.info( "MilleniumController.stop() invoked, stop the recovery controller and its session" );            
            _recoveryController.stop();
            _recoveryController.getSession().stop();
        }
    }
    
    // checkMaxSeqNums is used to integrate the trading session max seq nums 
    // with the recovery session seqNums ... really belongs in the recoveryController
    protected final void checkMaxSeqNums( int[] lastAppIdSeqNum ) {
        for( int i=0 ; i < MAX_SEQ_NUM ; i++ ) {
            if ( lastAppIdSeqNum[i] > _lastAppIdSeqNum[i] ) {
                _lastAppIdSeqNum[i] = lastAppIdSeqNum[i];
            }
        }
    }

    @Override
    protected final void onLogout() {
        if ( _recoveryController != null ) {
            _recoveryController.getSession().disconnect( false );
        }
    }

    private void storeMaxSeqNum( final Message msg, final int appId ) {
        final int curMax = _lastAppIdSeqNum[ appId ];
        final int seqNum = msg.getMsgSeqNum();
        if ( seqNum > curMax ) {
            _lastAppIdSeqNum[ appId ] = seqNum;
        }
    }
}