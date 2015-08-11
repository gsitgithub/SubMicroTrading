/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.type.ETIOrderProcessingType;
import com.rr.core.lang.ReusableString;

public interface ETISessionLogonRequestWrite extends BaseETIRequest, ETISessionLogonRequest {

   // Getters and Setters
    public void setHeartBtIntMS( int val );

    public void setPartyIDSessionID( int val );

    public void setDefaultCstmApplVerID( byte[] buf, int offset, int len );
    public ReusableString getDefaultCstmApplVerIDForUpdate();

    public void setPassword( byte[] buf, int offset, int len );
    public ReusableString getPasswordForUpdate();

    public void setApplUsageOrders( ETIOrderProcessingType val );

    public void setApplUsageQuotes( ETIOrderProcessingType val );

    public void setOrderRoutingIndicator( boolean val );

    public void setApplicationSystemName( byte[] buf, int offset, int len );
    public ReusableString getApplicationSystemNameForUpdate();

    public void setApplicationSystemVer( byte[] buf, int offset, int len );
    public ReusableString getApplicationSystemVerForUpdate();

    public void setApplicationSystemVendor( byte[] buf, int offset, int len );
    public ReusableString getApplicationSystemVendorForUpdate();

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

}
