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
import com.rr.core.pool.PoolFactory;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.om.order.Order;

/**
 * fully track trades so can apply cancel and corrects correctly
 */

public final class FullTradeRegistry implements TradeRegistry {

    private final TradeWrapperSet                      _tradeSet;
    
    private final PoolFactory<TradeWrapperImpl>        _tradeWrapperFactory;
    private final PoolFactory<TradeCorrectWrapperImpl> _tradeCorrectWrapperFactory;
    
    public FullTradeRegistry( int capacity ) {
        _tradeSet = new TradeWrapperSet( capacity );
        
        SuperPool<TradeWrapperImpl>        spt = SuperpoolManager.instance().getSuperPool( TradeWrapperImpl.class );
        SuperPool<TradeCorrectWrapperImpl> spc = SuperpoolManager.instance().getSuperPool( TradeCorrectWrapperImpl.class );
        
        _tradeWrapperFactory        = spt.getPoolFactory();
        _tradeCorrectWrapperFactory = spc.getPoolFactory();   
    }
    
    /**
     * @param execId an id copy that the trade registry will own
     * @param msg 
     * @return true if stored ok, false if DUPLICATE
     */
    @Override
    public boolean register( final Order order, final TradeNew msg ) {
        final TradeWrapperImpl w = _tradeWrapperFactory.get();
        
        w.set( order, msg.getExecId(), msg.getLastQty(), msg.getLastPx() );
        
        // wrapper is immediately recycled IF its a dup
        
        return _tradeSet.put( w );
    }

    /**
     * @param execId
     * @return true if the execId is registered
     */
    @Override
    public boolean contains( final Order order, final ViewString execId ) {
        return _tradeSet.contains( order, execId );
    }
    
    @Override
    public void clear() {
        _tradeSet.clear();
    }

    @Override
    public int size() {
        return _tradeSet.size();
    }

    @Override
    public TradeWrapper get( final Order order, final ViewString execId ) {
        return _tradeSet.get( order, execId );
    }

    @Override
    public boolean hasTradeDetails() {
        return true;
    }

    @Override
    public boolean hasDetails( final Order order, final ViewString execId ) {
        return _tradeSet.contains( order, execId );
    }

    @Override
    public boolean register( final Order order, final TradeCancel msg ) {
        final TradeCorrectWrapperImpl w = _tradeCorrectWrapperFactory.get();
        
        w.set( order, msg.getExecId(), msg.getExecRefID(), msg.getLastQty(), msg.getLastPx() );
        
        // wrapper is immediately recycled IF its a dup
        
        return _tradeSet.put( w );
    }

    @Override
    public boolean register( final Order order, final TradeCorrect msg ) {
        final TradeCorrectWrapperImpl w = _tradeCorrectWrapperFactory.get();
        
        w.set( order, msg.getExecId(), msg.getExecRefID(), msg.getLastQty(), msg.getLastPx() );
        
        // wrapper is immediately recycled IF its a dup
        
        return _tradeSet.put( w );
    }
}
