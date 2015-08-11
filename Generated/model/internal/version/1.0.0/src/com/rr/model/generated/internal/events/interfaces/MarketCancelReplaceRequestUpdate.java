/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.core.model.Currency;
import com.rr.core.lang.ReusableString;

public interface MarketCancelReplaceRequestUpdate extends OrderRequest, CancelReplaceRequest {

   // Getters and Setters
    public void setClOrdId( byte[] buf, int offset, int len );
    public ReusableString getClOrdIdForUpdate();

    public void setOrigClOrdId( byte[] buf, int offset, int len );
    public ReusableString getOrigClOrdIdForUpdate();

    public void setOrderId( byte[] buf, int offset, int len );
    public ReusableString getOrderIdForUpdate();

    public void setPrice( double val );

    public void setOrderQty( int val );

    public void setOrderCapacity( OrderCapacity val );

    public void setOrderSent( long val );

    public void setCurrency( Currency val );

    public void setSendingTime( int val );

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );


    public void setSrcEvent( OrderRequest request );
    public OrderRequest getSrcEvent();

}
