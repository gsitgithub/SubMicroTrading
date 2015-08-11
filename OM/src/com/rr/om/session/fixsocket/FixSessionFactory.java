/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.fixsocket;

import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.events.factory.HeartbeatFactory;
import com.rr.model.generated.internal.events.factory.LogonFactory;
import com.rr.model.generated.internal.events.factory.LogoutFactory;
import com.rr.model.generated.internal.events.factory.ResendRequestFactory;
import com.rr.model.generated.internal.events.factory.SequenceResetFactory;
import com.rr.model.generated.internal.events.impl.HeartbeatImpl;
import com.rr.model.generated.internal.events.impl.LogonImpl;
import com.rr.model.generated.internal.events.impl.LogoutImpl;
import com.rr.model.generated.internal.events.impl.ResendRequestImpl;
import com.rr.model.generated.internal.events.impl.SequenceResetImpl;
import com.rr.model.generated.internal.events.interfaces.Logon;
import com.rr.model.generated.internal.type.EncryptMethod;
import com.rr.om.session.state.StatefulSessionFactory;

public class FixSessionFactory implements StatefulSessionFactory {

    private final SuperPool<HeartbeatImpl> _heartbeatPool = SuperpoolManager.instance().getSuperPool( HeartbeatImpl.class );
    private final HeartbeatFactory _heartbeatFactory = new HeartbeatFactory( _heartbeatPool );

    private final SuperPool<LogonImpl> _logonPool = SuperpoolManager.instance().getSuperPool( LogonImpl.class );
    private final LogonFactory _logonFactory = new LogonFactory( _logonPool );

    private final SuperPool<LogoutImpl> _logoutPool = SuperpoolManager.instance().getSuperPool( LogoutImpl.class );
    private final LogoutFactory _logoutFactory = new LogoutFactory( _logoutPool );

    private final SuperPool<ResendRequestImpl> _resendRequestPool = SuperpoolManager.instance().getSuperPool( ResendRequestImpl.class );
    private final ResendRequestFactory _resendRequestFactory = new ResendRequestFactory( _resendRequestPool );

    private final SuperPool<SequenceResetImpl> _sequenceResetPool = SuperpoolManager.instance().getSuperPool( SequenceResetImpl.class );
    private final SequenceResetFactory _sequenceResetFactory = new SequenceResetFactory( _sequenceResetPool );

    private final FixConfig _config;

    public FixSessionFactory( FixConfig fixConfig ) {
        _config = fixConfig;
    }
    
    @Override
    public Message getHeartbeat( ZString testReqID ) {
        
        HeartbeatImpl hb = _heartbeatFactory.get();
        
        hb.getTestReqIDForUpdate().setValue( testReqID );
        
        return hb;
    }

    /**
     * dont use compIDs from encoder, use the ones supplied in the login message to avoid
     * unwanted snooping
     * 
     * @param logMsg
     * @param msg
     * @return
     */
    @Override
    public Message getLogOut( ZString logMsg, int code, Message logon, int nextOutSeqNum, int nextExpectedInSeqNum ) {
        
        Logon msg = (Logon)logon;
        
        LogoutImpl logout = _logoutFactory.get();
        logout.getTextForUpdate().setValue( logMsg );
        
        if ( msg != null ) {
            logout.getSenderCompIdForUpdate().setValue( msg.getTargetCompId() );
            logout.getSenderSubIdForUpdate().setValue(  msg.getTargetSubId() );
            logout.getTargetCompIdForUpdate().setValue( msg.getSenderCompId() );
            logout.getTargetSubIdForUpdate().setValue(  msg.getSenderSubId() );
        } else {
            logout.getSenderCompIdForUpdate().setValue( _config.getSenderCompId() );
            logout.getSenderSubIdForUpdate().setValue(  _config.getSenderSubId() );
            logout.getTargetCompIdForUpdate().setValue( _config.getTargetCompId() );
            logout.getTargetSubIdForUpdate().setValue(  _config.getTargetSubId() );
        }
        
        return logout;
    }

    @Override
    public Message getSessionLogOn( int heartBtInt, int nextOutSeqNum, int nextExpInSeqNum ) {
        LogonImpl logon = _logonFactory.get();
        
        logon.setHeartBtInt( heartBtInt );

        logon.getSenderCompIdForUpdate().setValue( _config.getSenderCompId() );
        logon.getSenderSubIdForUpdate().setValue(  _config.getSenderSubId() );
        logon.getTargetCompIdForUpdate().setValue( _config.getTargetCompId() );
        logon.getTargetSubIdForUpdate().setValue(  _config.getTargetSubId() );
        
        ZString rawData = _config.getRawData();
        
        if ( rawData.length() > 0 ) {
            logon.getRawDataForUpdate().setValue( rawData );
            logon.setRawDataLen( rawData.length() );
        }
        
        ZString encryptMethod = _config.getEncryptMethod();
        
        if ( encryptMethod.length() > 0 ) {
            EncryptMethod em = EncryptMethod.getVal( encryptMethod.getByte( 0 ) );
            if ( em != EncryptMethod.Unknown ) {
                logon.setEncryptMethod( em );
            }
        }
        
        if ( _config.isUseNewFix44GapFillProtocol() ) { 
            logon.setNextExpectedMsgSeqNum( nextExpInSeqNum );
        }
        
        return logon;
    }

    @Override
    public Message getResendRequest( int beginSeqNum, int endSeqNum ) {
        
        ResendRequestImpl req = _resendRequestFactory.get();
        
        req.setBeginSeqNo( beginSeqNum );
        
        if ( endSeqNum == 0 ) {
            req.setEndSeqNo( _config.getFixVersion().getMaxSeqNum() );
        } else {
            req.setEndSeqNo( endSeqNum );
        }
        
        return req;
    }

    @Override
    public Message createGapFillMessage( int beginSeqNo, int uptoSeqNum ) {

        SequenceResetImpl req = _sequenceResetFactory.get();
        
        req.setMsgSeqNum( beginSeqNo );
        req.setNewSeqNo( uptoSeqNum );
        req.setGapFillFlag( true );
        req.setPossDupFlag( true );
        
        return req;
    }
    
    @Override
    public Message createForceSeqNumResetMessage( int nextMsgSeqNoOut ) {

        SequenceResetImpl req = new SequenceResetImpl(); // dont use the factory as this can be invoked on multiple threads
        
        req.setMsgSeqNum( 0 );
        req.setNewSeqNo( nextMsgSeqNoOut );
        req.setGapFillFlag( false );
        req.setPossDupFlag( false );
        
        return req;
    }
    
}
