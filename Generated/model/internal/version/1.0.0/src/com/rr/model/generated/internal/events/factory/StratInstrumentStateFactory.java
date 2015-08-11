/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.factory;

import com.rr.model.generated.internal.events.impl.StratInstrumentStateImpl;
import com.rr.core.pool.PoolFactory;
import com.rr.core.pool.SuperPool;

public class StratInstrumentStateFactory implements PoolFactory<StratInstrumentStateImpl> {

    private SuperPool<StratInstrumentStateImpl> _superPool;

    private StratInstrumentStateImpl _root;

    public StratInstrumentStateFactory(  SuperPool<StratInstrumentStateImpl> superPool ) {
        _superPool = superPool;
        _root = _superPool.getChain();
    }


    @Override public StratInstrumentStateImpl get() {
        if ( _root == null ) {
            _root = _superPool.getChain();
        }
        StratInstrumentStateImpl obj = _root;
        _root = _root.getNext();
        obj.setNext( null );
        return obj;
    }
}
