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
import com.rr.core.lang.ReusableString;

public interface SecurityDefinitionUpdateWrite extends SecurityDefinition, SecurityDefinitionUpdate {

   // Getters and Setters
    public void setTotNumReports( int val );

    public void setSecurityTradingStatus( SecurityTradingStatus val );

    public void setSecurityIDSource( SecurityIDSource val );

    public void setSecurityType( SecurityType val );

    public void setSecurityID( long val );

    public void setSymbol( byte[] buf, int offset, int len );
    public ReusableString getSymbolForUpdate();

    public void setNoEvents( int val );

    public void setEvents( SecDefEvents val );

    public void setSecurityUpdateAction( SecurityUpdateAction val );

    public void setNoLegs( int val );

    public void setLegs( SecDefLeg val );

    public void setTradingReferencePrice( double val );

    public void setHighLimitPx( double val );

    public void setLowLimitPx( double val );

    public void setMinPriceIncrement( double val );

    public void setMinPriceIncrementAmount( double val );

    public void setSecurityGroup( byte[] buf, int offset, int len );
    public ReusableString getSecurityGroupForUpdate();

    public void setSecurityDesc( byte[] buf, int offset, int len );
    public ReusableString getSecurityDescForUpdate();

    public void setCFICode( byte[] buf, int offset, int len );
    public ReusableString getCFICodeForUpdate();

    public void setUnderlyingProduct( byte[] buf, int offset, int len );
    public ReusableString getUnderlyingProductForUpdate();

    public void setSecurityExchange( byte[] buf, int offset, int len );
    public ReusableString getSecurityExchangeForUpdate();

    public void setNoSecurityAltID( int val );

    public void setSecurityAltIDs( SecurityAltID val );

    public void setStrikePrice( double val );

    public void setStrikeCurrency( Currency val );

    public void setCurrency( Currency val );

    public void setSettlCurrency( Currency val );

    public void setMinTradeVol( long val );

    public void setMaxTradeVol( long val );

    public void setNoSDFeedTypes( int val );

    public void setSDFeedTypes( SDFeedType val );

    public void setMaturityMonthYear( long val );

    public void setLastUpdateTime( long val );

    public void setApplID( byte[] buf, int offset, int len );
    public ReusableString getApplIDForUpdate();

    public void setDisplayFactor( double val );

    public void setPriceRatio( double val );

    public void setContractMultiplierType( int val );

    public void setContractMultiplier( int val );

    public void setOpenInterestQty( int val );

    public void setTradingReferenceDate( int val );

    public void setMinQty( int val );

    public void setPricePrecision( double val );

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

}
