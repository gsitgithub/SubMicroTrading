/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium;

import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.internal.events.factory.EventRecycler;

public class MilleniumSocketConfig extends SocketConfig implements MilleniumConfig {

    private ZString     _recoveryHostname               = null;
    private int         _recoveryPort                   = 0;
    
    private boolean     _disconnectOnMissedHB           = true;
    private int         _heartBeatIntSecs               = 3;
    private boolean     _isRecoverFromLoginSeqNumTooLow = false; 
    private ZString     _userName                       = new ViewString( "" );
    private ZString     _password                       = new ViewString( "" );
    private ZString     _newPassword                    = new ViewString( "" );
    private boolean     _isRecoverySession              = false;
    
    public MilleniumSocketConfig() {
        super( EventRecycler.class );
    }
    
    public MilleniumSocketConfig( boolean                          disconnectOnMissedHB,
                                  Class<? extends MessageRecycler> recycler,
                                  boolean                          isServer, 
                                  ZString                          host, 
                                  ZString                          adapter, 
                                  int                              port,
                                  ZString                          userName,
                                  ZString                          password,
                                  ZString                          newPassword,
                                  boolean                          isRecoverySession ) {
        
        super( recycler, isServer, host, adapter, port );

        _disconnectOnMissedHB       = disconnectOnMissedHB;
        _userName                   = userName;
        _password                   = password;
        _newPassword                = newPassword;
        _isRecoverySession          = isRecoverySession;
    }

    public MilleniumSocketConfig( Class<? extends MessageRecycler> recycler,
                                  boolean                          isServer, 
                                  ZString                          host, 
                                  ZString                          adapter, 
                                  int                              port,
                                  ZString                          userName,
                                  ZString                          password,
                                  ZString                          newPassword,
                                  boolean                          isRecoverySession ) {
        
        super( recycler, isServer, host, adapter, port );

        _disconnectOnMissedHB       = true;
        _userName                   = userName;
        _password                   = password;
        _newPassword                = newPassword;
        _isRecoverySession          = isRecoverySession;
    }

    @Override
    public String info() {
        return super.info() + ", userName=" + _userName + ", recoverFromLowSeqNum=" + 
               _isRecoverFromLoginSeqNumTooLow + ", isRecoverySess=" + _isRecoverySession;
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

    public boolean isRecoverySession() {
        return _isRecoverySession;
    }

    public void setRecoverySession( boolean isRecoverySession ) {
        _isRecoverySession = isRecoverySession;
    }

    @Override
    public ZString getUserName() {
        return _userName;
    }

    @Override
    public ZString getPassword() {
        return _password;
    }

    @Override
    public ZString getNewPassword() {
        return _newPassword;
    }

    public void setPassword( ZString password ) {
        _password = password;
    }

    public void setNewPassword( ZString newPassword ) {
        _newPassword = newPassword;
    }

    public void setDisconnectOnMissedHB( boolean disconnectOnMissedHB ) {
        _disconnectOnMissedHB = disconnectOnMissedHB;
    }

    public void setUserName( ZString userName ) {
        _userName = userName;
    }

    public ZString getRecoveryHostname() {
        return _recoveryHostname;
    }

    public int getRecoveryPort() {
        return _recoveryPort;
    }

    public void setRecoveryHostname( ZString recoveryHostname ) {
        _recoveryHostname = recoveryHostname;
    }

    public void setRecoveryPort( int recoveryPort ) {
        _recoveryPort = recoveryPort;
    }

    @Override
    public void validate() throws SMTRuntimeException {
        super.validate();
        
        if ( isUnset( _userName ) )         throw new SMTRuntimeException( "MillleniumSocketConfig missing userName" );
        if ( isUnset( _recoveryHostname ) ) throw new SMTRuntimeException( "MillleniumSocketConfig missing recoveryHostname" );
        if ( _recoveryPort == 0 )           throw new SMTRuntimeException( "MillleniumSocketConfig missing recoveryPort" );
    }

    private boolean isUnset( ZString val ) {
        return val == null || val.toString().length() == 0;
    }

    @Override
    public int getMaxSeqNum() {
        return 0;
    }
}
