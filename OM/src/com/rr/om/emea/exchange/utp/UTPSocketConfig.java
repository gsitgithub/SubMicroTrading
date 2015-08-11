/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.utp;

import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.internal.events.factory.EventRecycler;

public class UTPSocketConfig extends SocketConfig implements UTPConfig {

    private boolean     _disconnectOnMissedHB           = true;
    private int         _heartBeatIntSecs               = 60;
    private boolean     _isRecoverFromLoginSeqNumTooLow = false; 
    private ZString     _userName                       = new ViewString( "" );
    
    public UTPSocketConfig() {
        super( EventRecycler.class );
    }
    
    public UTPSocketConfig( boolean                          disconnectOnMissedHB,
                            Class<? extends MessageRecycler> recycler,
                            boolean                          isServer, 
                            ZString                          host, 
                            ZString                          adapter, 
                            int                              port,
                            ZString                          userName ) {
        
        super( recycler, isServer, host, adapter, port );

        _disconnectOnMissedHB       = disconnectOnMissedHB;
        _userName                   = userName;
    }

    public UTPSocketConfig( Class<? extends MessageRecycler> recycler,
                            boolean                          isServer, 
                            ZString                          host, 
                            ZString                          adapter, 
                            int                              port,
                            ZString                          userName ) {
        
        super( recycler, isServer, host, adapter, port );

        _disconnectOnMissedHB       = true;
        _userName                   = userName;
    }

    @Override
    public String info() {
        return super.info() + ", userName=" + _userName + ", recoverFromLowSeqNum=" + _isRecoverFromLoginSeqNumTooLow;
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

    @Override
    public ZString getUserName() {
        return _userName;
    }

    public void setDisconnectOnMissedHB( boolean disconnectOnMissedHB ) {
        _disconnectOnMissedHB = disconnectOnMissedHB;
    }

    public void setUserName( ZString userName ) {
        _userName = userName;
    }

    @Override
    public void validate() throws SMTRuntimeException {
        super.validate();
        if ( isUnset( _userName ) ) throw new SMTRuntimeException( "UTPSocketConfig missing userName" );
    }

    private boolean isUnset( ZString val ) {
        return val == null || val.toString().length() == 0;
    }

    @Override
    public int getMaxSeqNum() {
        return 0;
    }
}
