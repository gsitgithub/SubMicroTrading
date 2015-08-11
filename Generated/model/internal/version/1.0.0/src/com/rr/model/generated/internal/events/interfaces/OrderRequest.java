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
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;

public interface OrderRequest extends BaseOrderRequest, Message {

   // Getters and Setters
    public ViewString getAccount();

    public ViewString getText();

    public ViewString getExDest();

    public ViewString getSecurityExchange();

    public double getPrice();

    public int getOrderQty();

    public ExecInst getExecInst();

    public HandlInst getHandlInst();

    public OrderCapacity getOrderCapacity();

    public OrdType getOrdType();

    public SecurityType getSecurityType();

    public SecurityIDSource getSecurityIDSource();

    public TimeInForce getTimeInForce();

    public BookingType getBookingType();

    public long getOrderReceived();

    public void setOrderSent( long val );
    public long getOrderSent();

    @Override
    public void dump( ReusableString out );

}
