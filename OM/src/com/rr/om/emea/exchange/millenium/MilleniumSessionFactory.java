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
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.events.factory.HeartbeatFactory;
import com.rr.model.generated.internal.events.factory.MilleniumLogonFactory;
import com.rr.model.generated.internal.events.factory.MilleniumLogonReplyFactory;
import com.rr.model.generated.internal.events.factory.MilleniumMissedMessageRequestFactory;
import com.rr.model.generated.internal.events.impl.HeartbeatImpl;
import com.rr.model.generated.internal.events.impl.MilleniumLogonImpl;
import com.rr.model.generated.internal.events.impl.MilleniumLogonReplyImpl;
import com.rr.model.generated.internal.events.impl.MilleniumMissedMessageRequestImpl;
import com.rr.om.session.state.StatefulSessionFactory;

public class MilleniumSessionFactory implements StatefulSessionFactory {

    private final SuperPool<HeartbeatImpl> _heartbeatPool = SuperpoolManager.instance().getSuperPool( HeartbeatImpl.class );
    private final HeartbeatFactory _heartbeatFactory = new HeartbeatFactory( _heartbeatPool );

    private final SuperPool<MilleniumLogonImpl> _logonPool = SuperpoolManager.instance().getSuperPool( MilleniumLogonImpl.class );
    private final MilleniumLogonFactory _logonFactory = new MilleniumLogonFactory( _logonPool );

    private final SuperPool<MilleniumLogonReplyImpl> _logoutMilleniumPool = SuperpoolManager.instance().getSuperPool( MilleniumLogonReplyImpl.class );
    private final MilleniumLogonReplyFactory _logoutFactory = new MilleniumLogonReplyFactory( _logoutMilleniumPool );

    private final SuperPool<MilleniumMissedMessageRequestImpl> _missedMessageRequestPool = SuperpoolManager.instance().getSuperPool( MilleniumMissedMessageRequestImpl.class );
    private final MilleniumMissedMessageRequestFactory _missedMessageRequestFactory = new MilleniumMissedMessageRequestFactory( _missedMessageRequestPool );

    @SuppressWarnings( "unused" )
    private final MilleniumConfig _config;

    public MilleniumSessionFactory( MilleniumConfig MilleniumConfig ) {
        _config = MilleniumConfig;
    }
    
    @Override
    public Message getHeartbeat( ZString testReqID ) {
        
        HeartbeatImpl hb = _heartbeatFactory.get();
        hb.getTestReqIDForUpdate().setValue( testReqID );
        
        return hb;
    }

    @Override
    public Message getLogOut( ZString pwdExpiry, int code, Message logon, int nextOutSeqNum, int nextExpectedInSeqNum ) {
        
        MilleniumLogonReplyImpl logReply = _logoutFactory.get();
        
        logReply.setRejectCode( code );
        
        if ( pwdExpiry != null ) {
            logReply.getPwdExpiryDayCountForUpdate().setValue( pwdExpiry );
        }

        return logReply;
    }

    @Override
    public Message getSessionLogOn( int heartBtInt, int nextOutSeqNum, int nextExpectedInSeqNum ) {
        MilleniumLogonImpl logon = _logonFactory.get();
        
        logon.setLastMsgSeqNum( (nextExpectedInSeqNum>0) ? (nextExpectedInSeqNum-1) : 0 );

        return logon;
    }

    public Message getRerequest( int appId, int lastMsgSeqNum ) {
        MilleniumMissedMessageRequestImpl msg = _missedMessageRequestFactory.get();

        msg.setAppId( (byte) appId );
        msg.setLastMsgSeqNum( lastMsgSeqNum );

        return msg;
    }

    @Override public Message getResendRequest( int beginSeqNum, int endSeqNum )         { return null; }
    @Override public Message createGapFillMessage( int beginSeqNo, int uptoSeqNum )     { return null; }

    @Override
    public Message createForceSeqNumResetMessage( int nextMsgSeqNoOut ) {
        return null;
    }
}
