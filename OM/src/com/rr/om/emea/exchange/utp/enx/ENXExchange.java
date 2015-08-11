/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.utp.enx;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.om.exchange.BaseExchangeImpl;
import com.rr.om.model.id.IDGenerator;
import com.rr.om.model.instrument.ExchangeSession;

public class ENXExchange extends BaseExchangeImpl {

    private IDGenerator _idGen;

    public ENXExchange( ZString         micCode, 
                        ZString         rec, 
                        TimeZone        timezone, 
                        ExchangeSession sess,
                        IDGenerator     idGen,           // used for mktClOrdId and clientExecId
                        Calendar        eodExpireEvents, 
                        List<Long>      halfDays ) {
        
        super( new ENXEnricher(), 
               new ENXMessageValidator(), 
               micCode, 
               rec, 
               timezone,
               false,
               true,
               true,
               false,
               idGen,
               sess,
               false,
               true,
               eodExpireEvents,
               halfDays );
        
        if ( idGen == null ) throw new RuntimeException( "ENXExchange requires an IDGenerator for numeric marketClOrdId" );
        
        _idGen = idGen;
    }

    @Override
    public void generateMarketClOrdId( ReusableString dest, ZString clientClOrdId ) {
        _idGen.genID( dest );
    }
}
