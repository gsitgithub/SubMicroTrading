/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.core.lang.ReusableString;

public interface MarketCancelRejectWrite extends CommonExchangeHeader, CancelReject {

   // Getters and Setters
    public void setOrderId( byte[] buf, int offset, int len );
    public ReusableString getOrderIdForUpdate();

    public void setClOrdId( byte[] buf, int offset, int len );
    public ReusableString getClOrdIdForUpdate();

    public void setOrigClOrdId( byte[] buf, int offset, int len );
    public ReusableString getOrigClOrdIdForUpdate();

    public void setCxlRejReason( CxlRejReason val );

    public void setCxlRejResponseTo( CxlRejResponseTo val );

    public void setOrdStatus( OrdStatus val );

    public void setText( byte[] buf, int offset, int len );
    public ReusableString getTextForUpdate();

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

    public void setSendingTime( int val );

}
