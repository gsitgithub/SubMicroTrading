/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.type.EncryptMethod;
import com.rr.core.lang.ReusableString;

public interface LogonWrite extends SessionHeader, Logon {

   // Getters and Setters
    public void setSenderCompId( byte[] buf, int offset, int len );
    public ReusableString getSenderCompIdForUpdate();

    public void setSenderSubId( byte[] buf, int offset, int len );
    public ReusableString getSenderSubIdForUpdate();

    public void setTargetCompId( byte[] buf, int offset, int len );
    public ReusableString getTargetCompIdForUpdate();

    public void setTargetSubId( byte[] buf, int offset, int len );
    public ReusableString getTargetSubIdForUpdate();

    public void setOnBehalfOfId( byte[] buf, int offset, int len );
    public ReusableString getOnBehalfOfIdForUpdate();

    public void setEncryptMethod( EncryptMethod val );

    public void setHeartBtInt( int val );

    public void setRawDataLen( int val );

    public void setRawData( byte[] buf, int offset, int len );
    public ReusableString getRawDataForUpdate();

    public void setResetSeqNumFlag( boolean val );

    public void setNextExpectedMsgSeqNum( int val );

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

    public void setSendingTime( int val );

}
