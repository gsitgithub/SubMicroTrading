/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.type.ETIEnv;
import com.rr.model.generated.internal.type.ETISessionMode;

public class ETISocketConfig extends SocketConfig implements ETIConfig {

    private boolean         _isGatewaySession                   = false;

    private boolean         _disconnectOnMissedHB           = true;
    private int             _heartBeatIntSecs               = 30;
    private boolean         _isRecoverFromLoginSeqNumTooLow = false; 
    private int             _userId;
    private ZString         _password                       = new ViewString( "" );
    private ReusableString  _etiVersion                     = new ReusableString( "1.0" );
    private ZString         _sessionLogonPassword           = new ViewString( "" );
    private ZString         _appSystemName                  = new ViewString( "SMT" );
    
    private ETIEnv          _env                            = ETIEnv.Simulation;
    private ETISessionMode  _etiSessionMode                 = ETISessionMode.HF;

    private long            _locationId                     = 0;
    private int             _partyIDSessionID;
    private int             _emulationTestPort              = 24001;    // @TODO REMOVE HARD CODED PORT
    private int             _emulationTestHost              = (127 << 24) + 1;

    private ZString         _traderPassword                 = new ViewString("");
    private ZString         _uniqueClientCode               = new ReusableString( "" );

    private int             _expectedRequests               = 10000; // used to presize map of seqNum to mktClOrdId

    private boolean         _forceTradingServerLocalhost    = false;


    public ETISocketConfig() {
        super( EventRecycler.class );
    }
    
    public ETISocketConfig( String id ) {
        super( id );
    }

    public ETISocketConfig( boolean                          disconnectOnMissedHB,
                            Class<? extends MessageRecycler> recycler,
                            boolean                          isServer, 
                            ZString                          host, 
                            ZString                          adapter, 
                            int                              port,
                            int                              userId,
                            ZString                          password,
                            boolean                          isGwySession,
                            ETIEnv                           etiEnv,
                            ETISessionMode                   etiSessionMode ) {
        
        super( recycler, isServer, host, adapter, port );

        _disconnectOnMissedHB       = disconnectOnMissedHB;
        _userId                     = userId;
        _password                   = password;
        _isGatewaySession           = isGwySession;
        _env                        = etiEnv;
        _etiSessionMode             = etiSessionMode;
    }

    public ETISocketConfig( Class<? extends MessageRecycler> recycler,
                            boolean                          isServer, 
                            ZString                          host, 
                            ZString                          adapter, 
                            int                              port,
                            int                              userId,
                            ZString                          password,
                            boolean                          isGwySession,
                            ETIEnv                           etiEnv,
                            ETISessionMode                   etiSessionMode ) {

        this( true, recycler, isServer, host, adapter, port, userId, password,
              isGwySession, etiEnv, etiSessionMode );
    }

    @Override
    public String info() {
        return super.info() + ", useriI=" + _userId + ", recoverFromLowSeqNum=" + 
               _isRecoverFromLoginSeqNumTooLow + ", isGwySess=" + _isGatewaySession;
    }
    
    @Override
    public boolean isDisconnectOnMissedHB() {
        return _disconnectOnMissedHB;
    }

    @Override
    public int getHeartBeatIntSecs() {
        return _heartBeatIntSecs;
    }
    
    @Override
    public void setHeartBeatIntSecs( int heartBeatIntSecs ) {
        _heartBeatIntSecs = heartBeatIntSecs;
    }

    /**
     * if other side send an nextSeqNum less than expected in log on message then can optionally truncate down automatically
     * 
     * THIS IS NOT USUALLY ADVISABLE WITH EXCHANGE BUT MAYBE SO FOR CLIENTS
     * 
     * @return true if should truncate down expected seq num from other side
     */
    @Override
    public boolean isRecoverFromLoginSeqNumTooLow() {
        return _isRecoverFromLoginSeqNumTooLow;
    }

    @Override
    public void setRecoverFromLoginSeqNumTooLow( boolean isRecoverFromLoginSeqNumTooLow ) {
        _isRecoverFromLoginSeqNumTooLow = isRecoverFromLoginSeqNumTooLow;
    }

    public boolean isGatewaySession() {
        return _isGatewaySession;
    }

    public void setGatewaySession( boolean isGwySession ) {
        _isGatewaySession = isGwySession;
    }

    @Override
    public int getUserId() {
        return _userId;
    }

    @Override
    public ZString getPassword() {
        return _password;
    }

    public void setPassword( ZString password ) {
        _password = password;
    }

    public void setDisconnectOnMissedHB( boolean disconnectOnMissedHB ) {
        _disconnectOnMissedHB = disconnectOnMissedHB;
    }

    public void setUserId( int userId) {
        _userId = userId;
    }

    @Override
    public void validate() throws SMTRuntimeException {
        super.validate();
        
        if ( _userId == 0 )                              throw new SMTRuntimeException( "ETISocketConfig missing userId" );
        if ( _env == ETIEnv.Unknown )                    throw new SMTRuntimeException( "ETISocketConfig missing etiEnv" );
        if ( _etiSessionMode == ETISessionMode.Unknown ) throw new SMTRuntimeException( "ETISocketConfig missing etiMode" );
        
        if ( isGatewaySession() && _partyIDSessionID == 0 ) throw new SMTRuntimeException( "ETISocketConfig missing partyIDSessionID" );
    }

    @Override
    public final long getLocationId() {
        return _locationId;
    }
    
    public final void setLocationId( long locationId ) {
        _locationId = locationId;
    }

    @Override
    public int getMaxSeqNum() {
        return 0;
    }

    @Override
    public ETIEnv getEnv() {
        return _env;
    }

    public void setEnv( ETIEnv etiEnv ) {
        _env = etiEnv;
    }

    @Override
    public ETISessionMode getETISessionMode() {
        return _etiSessionMode;
    }

    public void setEtiSessionMode( ETISessionMode etiSessionMode ) {
        _etiSessionMode = etiSessionMode;
    }

    @Override
    public int getPartyIDSessionID() {
        return _partyIDSessionID;
    }

    public void setPartyIDSessionID( int partyIDSessionID ) {
        _partyIDSessionID = partyIDSessionID;
    }

    @Override
    public int getEmulationTestHost() {
        return _emulationTestHost;
    }

    @Override
    public int getEmulationTestPort() {
        return _emulationTestPort;
    }

    @Override
    public ZString getETIVersion() {
        return _etiVersion;
    }

    @Override
    public ZString getSessionLogonPassword() {
        return _sessionLogonPassword;
    }

    @Override
    public ZString getAppSystemName() {
        return _appSystemName;
    }

    public void setSessionLogonPassword( ZString sessionLogonPassword ) {
        _sessionLogonPassword = sessionLogonPassword;
    }

    public void setAppSystemName( ZString appSystemName ) {
        _appSystemName = appSystemName;
    }

    public void setEmulationTestPort( int port ) {
        _emulationTestPort = port;
    }

    @Override
    public ZString getTraderPassword() {
        return _traderPassword;
    }

    public void setTraderPassword( ZString traderPassword ) {
        _traderPassword = traderPassword;
    }

    public int getExpectedRequests() {
        return _expectedRequests;
    }

    public void setExpectedRequests( int expectedRequests ) {
        _expectedRequests = expectedRequests;
    }

    public void setETIVersion( String etiVersion ) {
        _etiVersion.copy( etiVersion );
    }

    @Override
    public final boolean isForceTradingServerLocalhost() {
        return _forceTradingServerLocalhost;
    }

    public final void setForceTradingServerLocalhost( boolean forceTradingServerLocalhost ) {
        _forceTradingServerLocalhost = forceTradingServerLocalhost;
    }

    public ZString getUniqueClientCode() {
        return _uniqueClientCode;
    }

    public void setUniqueClientCode( ZString uniqueClientCode ) {
        _uniqueClientCode = uniqueClientCode;
    }
}
