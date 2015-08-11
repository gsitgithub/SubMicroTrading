/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.lang.ZString;
import com.rr.core.model.Exchange;
import com.rr.om.exchange.ExchangeManager;

public final class MultiExchangeInstrumentStore extends BaseInstrumentSecDefStore {

    private final Map<Exchange,Indexes> _exchangeMaps = new HashMap<Exchange,Indexes>( 16 );

    public MultiExchangeInstrumentStore( int preSize ) {
        super( "MultiExchangeInstrumentStore", preSize );
    }

    public MultiExchangeInstrumentStore( String id, int preSize ) {
        super( id, preSize );
    }

    @Override
    protected Indexes getExchangeMap( Exchange ex, int preSize ) {
        Indexes exMap = _exchangeMaps.get( ex );
        
        if ( exMap == null ) {
            exMap = new Indexes( preSize );
            _exchangeMaps.put( ex, exMap );
        }
        
        return exMap;
    }

    @Override
    protected Exchange getExchange( ZString rec, ZString altRec ) {
        Exchange ex = ExchangeManager.instance().getByREC( rec );
        
        if ( ex == null && altRec != null ) ex = ExchangeManager.instance().getByREC( altRec );

        if ( ex == null ) {
            throw new RuntimeException( "ExchangeManager doesnt have rec=" + rec + " loaded" );
        }
        
        return ex;
    }
}
