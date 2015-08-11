/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.newmain;

import com.rr.core.component.SMTStartContext;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.properties.AppProps;
import com.rr.om.client.ClientProfileManager;
import com.rr.om.exchange.ExchangeManager;

/**
 * Context block passed by Bootstrap to initialise method 
 */
public class SMTContext implements SMTStartContext {
    
    private final String                _id;

    private       AppProps              _appProps;
    private       ExchangeManager       _exchangeManager;
    private       InstrumentLocator     _instrumentLocator;
    private       ClientProfileManager  _clientProfileManager;
    private       WarmupControl         _warmupControl;
    
    public SMTContext( String id ) {
        _id = id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    public ExchangeManager getExchangeManager() {
        return _exchangeManager;
    }
    
    public ClientProfileManager getClientProfileManager() {
        return _clientProfileManager;
    }
    
    public InstrumentLocator getInstrumentLocator() {
        return _instrumentLocator;
    }

    public void setInstrumentLocator( InstrumentLocator instrumentLocator ) {
        this._instrumentLocator = instrumentLocator;
    }

    public WarmupControl getWarmupControl() {
        return _warmupControl;
    }

    public AppProps getAppProps() {
        return _appProps;
    }

    public void setAppProps( AppProps appProps ) {
        _appProps = appProps;
    }
}
