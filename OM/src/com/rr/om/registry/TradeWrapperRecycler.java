/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.registry;

import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.om.order.OrderReusableType;

public class TradeWrapperRecycler {

    private final Recycler<TradeWrapperImpl>        _tradeWrapperRecycler;
    private final Recycler<TradeCorrectWrapperImpl> _tradeCorrectWrapperRecycler;
    
    public TradeWrapperRecycler() {
    
        SuperPool<TradeWrapperImpl>        spt = SuperpoolManager.instance().getSuperPool( TradeWrapperImpl.class );
        SuperPool<TradeCorrectWrapperImpl> spc = SuperpoolManager.instance().getSuperPool( TradeCorrectWrapperImpl.class );
        
        _tradeWrapperRecycler = spt.getRecycleFactory();
        _tradeCorrectWrapperRecycler = spc.getRecycleFactory();   
    }
    
    public void recycle( TradeWrapper w ) {
        if ( w.getReusableType() == OrderReusableType.TradeWrapper ) {
            _tradeWrapperRecycler.recycle( (TradeWrapperImpl) w );
        } else{
            _tradeCorrectWrapperRecycler.recycle( (TradeCorrectWrapperImpl) w );            
        }
    }
}
