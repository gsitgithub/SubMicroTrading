/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.us.cme;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.om.exchange.BaseExchangeImpl;
import com.rr.om.model.id.IDGenerator;
import com.rr.om.model.instrument.ExchangeSession;

public class CMEExchange extends BaseExchangeImpl {

    public CMEExchange( ZString         micCode, 
                        TimeZone        timezone, 
                        ExchangeSession exchangeSession, 
                        IDGenerator     idGen,
                        Calendar        eodExpireEvents, 
                        List<Long>      halfDays ) {
        
        super( new CMEEnricher(), 
               new CMEMessageValidator(), 
               micCode, 
               new ViewString( "2" ), 
               timezone,
               false,
               true,
               true,
               false,
               idGen,
               exchangeSession,
               false,
               true,
               eodExpireEvents,
               halfDays );
    }

    @Override
    public void generateMarketClOrdId( ReusableString dest, ZString clientClOrdId ) {
        dest.setValue( clientClOrdId );
    }
}
