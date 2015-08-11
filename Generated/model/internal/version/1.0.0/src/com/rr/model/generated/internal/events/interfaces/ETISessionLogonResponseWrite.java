/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.type.ETIEnv;
import com.rr.core.lang.ReusableString;

public interface ETISessionLogonResponseWrite extends BaseETIResponse, ETISessionLogonResponse {

   // Getters and Setters
    public void setRequestTime( int val );

    public void setSendingTime( int val );

    public void setThrottleTimeIntervalMS( long val );

    public void setThrottleNoMsgs( int val );

    public void setThrottleDisconnectLimit( int val );

    public void setHeartBtIntMS( int val );

    public void setSessionInstanceID( int val );

    public void setTradSesMode( ETIEnv val );

    public void setDefaultCstmApplVerID( byte[] buf, int offset, int len );
    public ReusableString getDefaultCstmApplVerIDForUpdate();

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

}
