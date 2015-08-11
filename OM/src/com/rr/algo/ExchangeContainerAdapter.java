/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.algo;

import com.rr.core.model.Message;
import com.rr.core.session.MessageRouter;


/**
 * adapts exchange responses to algo Container
 */
public final class ExchangeContainerAdapter implements MessageRouter {

    private SimpleAlgo _container;
    
    public ExchangeContainerAdapter() {
    }

    public ExchangeContainerAdapter( SimpleAlgo container ) {
        _container = container;
    }

    public void setContainer( SimpleAlgo container ) {
        _container = container;
    }

    @Override
    public void handle( Message msg ) {
        handleNow( msg );
    }

    @Override
    public void handleNow( Message msg ) {
        _container.handleMarketEvent( msg );
    }

    @Override
    public boolean canHandle() {
        return true;
    }
    
    @Override public String getComponentId() { return null; }
    @Override public void threadedInit() { /* nothing */  }
}
