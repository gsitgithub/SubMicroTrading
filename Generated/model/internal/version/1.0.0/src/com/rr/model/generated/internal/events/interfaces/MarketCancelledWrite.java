/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.internal.type.ExecType;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.core.lang.ReusableString;

public interface MarketCancelledWrite extends CommonExecRpt, Cancelled {

   // Getters and Setters
    public void setClOrdId( byte[] buf, int offset, int len );
    public ReusableString getClOrdIdForUpdate();

    public void setOrigClOrdId( byte[] buf, int offset, int len );
    public ReusableString getOrigClOrdIdForUpdate();

    public void setExecId( byte[] buf, int offset, int len );
    public ReusableString getExecIdForUpdate();

    public void setOrderId( byte[] buf, int offset, int len );
    public ReusableString getOrderIdForUpdate();

    public void setExecType( ExecType val );

    public void setOrdStatus( OrdStatus val );

    public void setLeavesQty( int val );

    public void setCumQty( int val );

    public void setAvgPx( double val );

    public void setSide( Side val );

    public void setMktCapacity( OrderCapacity val );

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

    public void setSendingTime( int val );

}
