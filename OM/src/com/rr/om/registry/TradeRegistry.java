/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.registry;

import com.rr.core.lang.ViewString;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.om.order.Order;

/**
 * trade registry tracks execIds so it can detect dup trades and stop them being sengt to client
 * 
 * most exchanges dont support trade correct or cancels, those that do will need to track every trade qty/px and execId
 * 
 * @NOTE some exchanges dont provide unique execId, so for those need scope with the order
 */

public interface TradeRegistry {

    /**
     * register the trade
     * @param msg 
     * @return true if stored ok, false if DUPLICATE execId
     */
    public boolean register( Order order, TradeNew msg );

    /**
     * register the trade cancel
     * @param msg 
     * @return true if stored ok, false if DUPLICATE execId
     */
    public boolean register( Order order, TradeCancel msg );
    
    /**
     * register the trade correct
     * @param msg 
     * @return true if stored ok, false if DUPLICATE execId
     */
    public boolean register( Order order, TradeCorrect msg );
    
    /**
     * @param execId
     * @return true if the execId is registered
     */
    public boolean contains( Order order, ViewString execId );

    /**
     * clear the set recyling any entries
     */
    public void clear();

    /**
     * @return number of entries in registry
     */
    public int size();

    /**
     * @return true if this registry maintains all trade details
     */
    public boolean hasTradeDetails();
    
    /**
     * @param execId
     * @return TradeWrapper for the execId IF the registry supports  trade storage
     * 
     * @NOTE dont hang onto or change the wrapper instance which is the object directly from set
     */
    public TradeWrapper get( Order order, ViewString execId );

    /**
     * @param execId
     * @return true if the execId is registered and the registry has its details
     */
    public boolean hasDetails( Order order, ViewString execRefID );
}
