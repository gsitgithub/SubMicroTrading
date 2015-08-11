/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.type.SecurityTradingStatus;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.SecurityType;
import com.rr.model.generated.internal.events.interfaces.SecDefEvents;
import com.rr.model.generated.internal.type.SecurityUpdateAction;
import com.rr.model.generated.internal.events.interfaces.SecDefLeg;
import com.rr.model.generated.internal.events.interfaces.SecurityAltID;
import com.rr.core.model.Currency;
import com.rr.model.generated.internal.events.interfaces.SDFeedType;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;

public interface SecurityDefinition extends BaseMDResponse, Message {

   // Getters and Setters
    public int getTotNumReports();

    public SecurityTradingStatus getSecurityTradingStatus();

    public SecurityIDSource getSecurityIDSource();

    public SecurityType getSecurityType();

    public long getSecurityID();

    public ViewString getSymbol();

    public int getNoEvents();

    public SecDefEvents getEvents();

    public SecurityUpdateAction getSecurityUpdateAction();

    public int getNoLegs();

    public SecDefLeg getLegs();

    public double getTradingReferencePrice();

    public double getHighLimitPx();

    public double getLowLimitPx();

    public double getMinPriceIncrement();

    public double getMinPriceIncrementAmount();

    public ViewString getSecurityGroup();

    public ViewString getSecurityDesc();

    public ViewString getCFICode();

    public ViewString getUnderlyingProduct();

    public ViewString getSecurityExchange();

    public int getNoSecurityAltID();

    public SecurityAltID getSecurityAltIDs();

    public double getStrikePrice();

    public Currency getStrikeCurrency();

    public Currency getCurrency();

    public Currency getSettlCurrency();

    public long getMinTradeVol();

    public long getMaxTradeVol();

    public int getNoSDFeedTypes();

    public SDFeedType getSDFeedTypes();

    public long getMaturityMonthYear();

    public long getLastUpdateTime();

    public ViewString getApplID();

    public double getDisplayFactor();

    public double getPriceRatio();

    public int getContractMultiplierType();

    public int getContractMultiplier();

    public int getOpenInterestQty();

    public int getTradingReferenceDate();

    public int getMinQty();

    public double getPricePrecision();

    @Override
    public void dump( ReusableString out );

}
