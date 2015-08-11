/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.type.ExecInst;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.core.model.SecurityType;
import com.rr.core.model.SecurityIDSource;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.model.generated.internal.type.BookingType;
import com.rr.core.model.Instrument;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Currency;
import com.rr.model.generated.internal.type.Side;
import com.rr.core.lang.AssignableString;

public interface ClientNewOrderSingleWrite extends OrderRequest, NewOrderSingle {

   // Getters and Setters
    public AssignableString getClOrdIdForUpdate();

    public AssignableString getAccountForUpdate();

    public AssignableString getTextForUpdate();

    public AssignableString getExDestForUpdate();

    public AssignableString getSecurityExchangeForUpdate();

    public void setPrice( double val );

    public void setOrderQty( int val );

    public void setExecInst( ExecInst val );

    public void setHandlInst( HandlInst val );

    public void setOrderCapacity( OrderCapacity val );

    public void setOrdType( OrdType val );

    public void setSecurityType( SecurityType val );

    public void setSecurityIDSource( SecurityIDSource val );

    public void setTimeInForce( TimeInForce val );

    public void setBookingType( BookingType val );

    public void setOrderReceived( long val );

    public void setOrderSent( long val );

    public void setInstrument( Instrument val );

    public void setClient( ClientProfile val );

    public AssignableString getOrigClOrdIdForUpdate();

    public AssignableString getSecurityIdForUpdate();

    public AssignableString getSymbolForUpdate();

    public void setCurrency( Currency val );

    public void setTransactTime( int val );

    public void setSendingTime( int val );

    public void setSide( Side val );

    public AssignableString getSrcLinkIdForUpdate();

    public AssignableString getOnBehalfOfIdForUpdate();

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

}
