/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.router;

import com.rr.core.lang.TLC;
import com.rr.core.lang.ZString;
import com.rr.core.model.MessageHandler;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.om.router.OrderRouter;

/**
 * used to multiplex between multiple order sources and single exchange destination
 */
public final class MultiSourceToSingleExchangeRouter implements OrderRouter {

    private final String            _id;
    private final MessageHandler[]  _all;
    private final MessageHandler    _dest;
    
    private final RouterSharedData  _sharedData;
    
    
    public MultiSourceToSingleExchangeRouter( String id, final MessageHandler dest, RouterSharedData sharedData ) {
        super();
        _dest = dest;
        _all  = new MessageHandler[] { dest };
        _id = id;
        _sharedData = sharedData;
    }

    /**
     * used by sources to determine session for   
     */
    @Override
    public final MessageHandler getRoute( final NewOrderSingle nos, final MessageHandler replyHandler ) {

        ZString copyClOrdId = TLC.safeCopy( nos.getClOrdId() );
        
        _sharedData.storeReturnRoute( copyClOrdId, replyHandler );
        
        return _dest;
    }

    @Override
    public final MessageHandler[] getAllRoutes() {
        return _all;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}
