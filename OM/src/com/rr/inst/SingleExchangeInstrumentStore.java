/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import com.rr.core.lang.ZString;
import com.rr.core.model.Exchange;

/**
 * NON thread safe instrument store
 *
 * assumes only single destination, so dont need segregate instruments by exchange
 */
public final class SingleExchangeInstrumentStore extends BaseInstrumentSecDefStore {

    private final Indexes  _indices;
    private final Exchange _exchange;
    
    public SingleExchangeInstrumentStore( Exchange exchange, int preSize ) {
        this( "SingleExchangeInstrumentStore", exchange, preSize );
    }
    
    public SingleExchangeInstrumentStore( String id, Exchange exchange, int preSize ) {
        super( id, preSize );
        
        _exchange = exchange;
        
        _indices = new Indexes( preSize );
    }

    @Override
    protected Indexes getExchangeMap( Exchange ex, int preSize ) {
        return _indices;
    }

    @Override
    protected Exchange getExchange( ZString rec, ZString altRec ) {
        return _exchange;
    }
}
