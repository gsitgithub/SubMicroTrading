/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.ets;

import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.events.factory.HeartbeatFactory;
import com.rr.model.generated.internal.events.factory.UTPLogonFactory;
import com.rr.model.generated.internal.events.factory.UTPLogonRejectFactory;
import com.rr.model.generated.internal.events.impl.HeartbeatImpl;
import com.rr.model.generated.internal.events.impl.UTPLogonImpl;
import com.rr.model.generated.internal.events.impl.UTPLogonRejectImpl;
import com.rr.model.generated.internal.type.UTPRejCode;
import com.rr.om.session.state.StatefulSessionFactory;

public class ETSSessionFactory implements StatefulSessionFactory {

    private final SuperPool<HeartbeatImpl> _heartbeatPool = SuperpoolManager.instance().getSuperPool( HeartbeatImpl.class );
    private final HeartbeatFactory _heartbeatFactory = new HeartbeatFactory( _heartbeatPool );

    private final SuperPool<UTPLogonImpl> _logonPool = SuperpoolManager.instance().getSuperPool( UTPLogonImpl.class );
    private final UTPLogonFactory _logonFactory = new UTPLogonFactory( _logonPool );

    private final SuperPool<UTPLogonRejectImpl> _logoutPool = SuperpoolManager.instance().getSuperPool( UTPLogonRejectImpl.class );
    private final UTPLogonRejectFactory _logoutFactory = new UTPLogonRejectFactory( _logoutPool );

    @SuppressWarnings( "unused" )
    private final ETSConfig _config;

    public ETSSessionFactory( ETSConfig utpConfig ) {
        _config = utpConfig;
    }
    
    @Override
    public Message getHeartbeat( ZString testReqID ) {
        
        HeartbeatImpl hb = _heartbeatFactory.get();
        hb.getTestReqIDForUpdate().setValue( testReqID );
        
        return hb;
    }

    @Override
    public Message getLogOut( ZString logMsg, int code, Message logon, int nextOutSeqNum, int nextExpectedInSeqNum ) {
        
        UTPLogonRejectImpl logout = _logoutFactory.get();
        
        logout.getRejectTextForUpdate().setValue( logMsg );
        logout.setLastMsgSeqNumRcvd( (nextExpectedInSeqNum>0) ? (nextExpectedInSeqNum-1) : 0 );
        logout.setLastMsgSeqNumSent( (nextOutSeqNum>0)        ? (nextOutSeqNum - 1)      : 0 );
        logout.setRejectCode( UTPRejCode.InvalidSequenceNumber );

        return logout;
    }

    @Override
    public Message getSessionLogOn( int heartBtInt, int nextOutSeqNum, int nextExpectedInSeqNum ) {
        UTPLogonImpl logon = _logonFactory.get();
        
        logon.setLastMsgSeqNum( (nextExpectedInSeqNum>0) ? (nextExpectedInSeqNum-1) : 0 );

        return logon;
    }

    @Override public Message getResendRequest( int beginSeqNum, int endSeqNum )         { return null; }
    @Override public Message createGapFillMessage( int beginSeqNo, int uptoSeqNum )     { return null; }

    @Override
    public Message createForceSeqNumResetMessage( int nextMsgSeqNoOut ) {
        return null;
    }
}
