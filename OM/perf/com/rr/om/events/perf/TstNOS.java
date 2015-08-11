/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import java.util.Currency;

import javax.sound.midi.Instrument;

import com.rr.core.lang.ZString;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.SecurityType;
import com.rr.model.generated.internal.type.BookingType;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.generated.internal.type.PositionEffect;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.om.client.OMClientProfile;

public interface TstNOS {

    // common to msgs
    
    int getMsgSeqNo();

    boolean getPossDup();
    
    
    // common to all fix events
    
    SecurityIDSource getIDSource();

    ZString getSecurityID();

    ZString getSenderCompID();

    ZString getSenderSubID();

    ZString getOnBehalfOfID();

    ZString getSymbol();

    ZString getTargetCompID();

    ZString getTargetSubID();
    
    // NOS fields
    
    Currency getCurrency();

    long getOrderQty();

    ZString getClOrdID();
    
    ZString getAccount();

    Currency getOriginalCurrency();

    long getEffectiveTime();

    ZString getExDestination();
    
    ZString getSecurityExch();

    long getExpireTime();

    HandlInst getHandlingInstruction();

    Instrument getInstrument();

    OrderCapacity getOrderCapacity();

    OrdType getOrdType();

    double getPrice();

    Side getSide();

    ZString getText();

    TimeInForce getTimeInForce();

    long getTransactTime();

    OMClientProfile getClientProfile();

    void setTmTransmit( long value );

    long getTmTransmit();

    long getTmReceive();

    ZString getSecClOrdID();

    long getMaxFloor();

    SecurityType getSecurityType();

    BookingType getBookingType();
    
    PositionEffect getPositionEffect();
    
    
}
