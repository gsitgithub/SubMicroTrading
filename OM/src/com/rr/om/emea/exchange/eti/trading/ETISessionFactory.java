/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.events.factory.HeartbeatFactory;
import com.rr.model.generated.internal.events.impl.ETIConnectionGatewayRequestImpl;
import com.rr.model.generated.internal.events.impl.ETIConnectionGatewayResponseImpl;
import com.rr.model.generated.internal.events.impl.ETIRetransmitOrderEventsImpl;
import com.rr.model.generated.internal.events.impl.ETISessionLogonRequestImpl;
import com.rr.model.generated.internal.events.impl.ETISessionLogonResponseImpl;
import com.rr.model.generated.internal.events.impl.ETISessionLogoutNotificationImpl;
import com.rr.model.generated.internal.events.impl.ETIUserLogonRequestImpl;
import com.rr.model.generated.internal.events.impl.ETIUserLogonResponseImpl;
import com.rr.model.generated.internal.events.impl.HeartbeatImpl;
import com.rr.model.generated.internal.events.interfaces.ETIConnectionGatewayRequest;
import com.rr.model.generated.internal.events.interfaces.ETIConnectionGatewayResponse;
import com.rr.model.generated.internal.events.interfaces.ETIRetransmitOrderEvents;
import com.rr.model.generated.internal.type.ETIEnv;
import com.rr.model.generated.internal.type.ETIEurexDataStream;
import com.rr.model.generated.internal.type.ETIOrderProcessingType;
import com.rr.om.session.state.StatefulSessionFactory;

public class ETISessionFactory implements StatefulSessionFactory {

    private static final boolean ETI_ORDER_ROUTING = true;

    private final SuperPool<HeartbeatImpl> _heartbeatPool = SuperpoolManager.instance().getSuperPool( HeartbeatImpl.class );
    private final HeartbeatFactory _heartbeatFactory = new HeartbeatFactory( _heartbeatPool );

    private final ETIConfig _config;

    public ETISessionFactory( ETIConfig cfg ) {
        _config = cfg;
    }
    
    @Override
    public Message getHeartbeat( ZString testReqID ) {
        
        HeartbeatImpl hb = _heartbeatFactory.get();
        hb.getTestReqIDForUpdate().setValue( testReqID );
        
        return hb;
    }

    @Override
    public Message getLogOut( ZString pwdExpiry, int code, Message logon, int nextOutSeqNum, int nextExpectedInSeqNum ) {
        
        ETISessionLogoutNotificationImpl logoutNotif = new ETISessionLogoutNotificationImpl();
        
        logoutNotif.getReasonForUpdate().append( "Code " ).append( code );
        
        return logoutNotif;
    }

    @Override
    public Message getSessionLogOn( int heartBtInt, int nextOutSeqNum, int nextExpInSeqNum ) {
        ETISessionLogonRequestImpl logon = new ETISessionLogonRequestImpl();
        
        logon.setHeartBtIntMS( _config.getHeartBeatIntSecs() * 1000 );
        logon.setPartyIDSessionID( _config.getPartyIDSessionID() );
        logon.getDefaultCstmApplVerIDForUpdate().copy( _config.getETIVersion() );
        logon.getPasswordForUpdate().copy( _config.getSessionLogonPassword() );
        logon.setApplUsageOrders( ETIOrderProcessingType.Automated );
        logon.setApplUsageQuotes( ETIOrderProcessingType.Automated );
        logon.setOrderRoutingIndicator( ETI_ORDER_ROUTING );
        logon.getApplicationSystemNameForUpdate().copy( _config.getAppSystemName() );
        logon.getApplicationSystemVerForUpdate().append( ETIConfig.VERSION );
        logon.getApplicationSystemVendorForUpdate().copy( "SMT"  );
        
        return logon;
    }

    @Override public Message getResendRequest( int beginSeqNum, int endSeqNum )         { return null; }
    @Override public Message createGapFillMessage( int beginSeqNo, int uptoSeqNum )     { return null; }

    public ETIConnectionGatewayRequest getConnectionGatewayRequest() {
        ETIConnectionGatewayRequestImpl req = new ETIConnectionGatewayRequestImpl();
        
        req.setPartyIDSessionID( _config.getPartyIDSessionID() );
        req.getPasswordForUpdate().copy( _config.getPassword() );
        return req;
    }

    public ETIConnectionGatewayResponse getConnectionGatewayResponse() {
        ETIConnectionGatewayResponseImpl req = new ETIConnectionGatewayResponseImpl();
        req.setTradSesMode( _config.getEnv() );
        req.setSessionMode( _config.getETISessionMode() );
        req.setGatewayID( _config.getEmulationTestHost() );                    
        req.setGatewaySubID( _config.getEmulationTestPort() );
        return req;
    }

    /**
     * getSessionLogonReply - only used by the exchange emulator
     */
    public Message getSessionLogonReply() {
        ETISessionLogonResponseImpl rep = new ETISessionLogonResponseImpl();
        
        rep.setThrottleTimeIntervalMS( ETIConfig.DEFAULT_THROTTLE_PERIOD_MS );
        rep.setHeartBtIntMS( _config.getHeartBeatIntSecs() * 1000 );
        rep.setThrottleNoMsgs( ETIConfig.DEFAULT_THROTTLE_MSGS );
        rep.setThrottleDisconnectLimit( ETIConfig.DEFAULT_THROTTLE_MSGS );
        rep.setTradSesMode( ETIEnv.Simulation );

        return rep;
    }

    /**
     * getUserLogonReply - only used by the exchange emulator
     */
    public Message getUserLogOnReply() {
        ETIUserLogonResponseImpl rep = new ETIUserLogonResponseImpl();
        
        return rep;
    }

    public Message getUserLogOn() {
        ETIUserLogonRequestImpl logon = new ETIUserLogonRequestImpl();

        logon.setUserName( _config.getUserId() );
        logon.getPasswordForUpdate().copy( _config.getTraderPassword() );
        
        return logon;
    }

    public ETIRetransmitOrderEvents createRetransmitOrderEventsRequest( ETIEurexDataStream stream, short partitionId, ApplMsgID lastApplMsgID ) {
        ETIRetransmitOrderEventsImpl req = new ETIRetransmitOrderEventsImpl();
        
        req.setRefApplID( stream );
        req.setPartitionID( partitionId );
        lastApplMsgID.toBytes( req.getApplBegMsgIDForUpdate() );
        
        return req;
    }

    @Override
    public Message createForceSeqNumResetMessage( int nextMsgSeqNoOut ) {
        return null;
    }
}
