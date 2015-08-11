/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.dummy.warmup;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.om.model.instrument.Auction;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.model.instrument.ExchangeState;

public class DummyExchangeSession implements ExchangeSession {

    private long _open = 0;
    private final ZString _id = new ViewString( "DummyExSession" );

    @Override
    public ZString getId() {
        return _id;
    }
    
    @Override
    public ExchangeState getExchangeState( long time ) {
        return ExchangeState.Continuous;
    }

    @Override
    public boolean isOpen( long time ) {
        return time > _open ;
    }

    @Override
    public Auction getCloseAuction() {
        return null;
    }

    @Override
    public long getCloseTime() {
        return 0;
    }

    @Override
    public ExchangeSession getExchangeSession( ZString marketSegment ) {
        return this;
    }

    @Override
    public ExchangeState getExchangeState() {
        return ExchangeState.Continuous;
    }

    @Override
    public long getHalfDayCloseTime() {
        return 0;
    }

    @Override
    public Auction getIntradayAuction() {
        return null;
    }

    @Override
    public Auction getOpenAuction() {
        return null;
    }

    @Override
    public long getOpenTime() {
        return 0;
    }

    @Override
    public void setToday() {
        // nothing
    }

    @Override
    public void setHalfDay( boolean isHalfDay ) {
        // nothing
    }

    @Override
    public void setOpen( long openTimeUTC ) {
        _open  = openTimeUTC;
    }

    @Override
    public ReusableString toString( ReusableString buf ) {
        buf.append( "DummySession" );
        return buf;
    }
}
