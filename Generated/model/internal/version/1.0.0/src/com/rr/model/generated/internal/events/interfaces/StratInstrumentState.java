/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.core.model.Instrument;
import com.rr.core.lang.ReusableString;
import com.rr.model.internal.type.SubEvent;

public interface StratInstrumentState extends SubEvent {

   // Getters and Setters
    public Instrument getInstrument();

    public long getLastTickId();

    public int getTotLongContractsExecuted();

    public int getTotShortContractsExecuted();

    public int getTotLongContractsOpen();

    public int getTotShortContractsOpen();

    public double getTotLongValueExecuted();

    public double getTotShortValueExecuted();

    public int getTotalLongOrders();

    public int getTotalShortOrders();

    public int getTotLongContractsUnwound();

    public int getTotShortContractsUnwound();

    public double getBidPx();

    public double getAskPx();

    public double getLastDecidedPosition();

    public double getUnwindPnl();

    @Override
    public void dump( ReusableString out );

    public void setInstrument( Instrument val );

    public void setLastTickId( long val );

    public void setTotLongContractsExecuted( int val );

    public void setTotShortContractsExecuted( int val );

    public void setTotLongContractsOpen( int val );

    public void setTotShortContractsOpen( int val );

    public void setTotLongValueExecuted( double val );

    public void setTotShortValueExecuted( double val );

    public void setTotalLongOrders( int val );

    public void setTotalShortOrders( int val );

    public void setTotLongContractsUnwound( int val );

    public void setTotShortContractsUnwound( int val );

    public void setBidPx( double val );

    public void setAskPx( double val );

    public void setLastDecidedPosition( double val );

    public void setUnwindPnl( double val );

}
