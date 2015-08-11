/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.pool;

import com.rr.core.lang.Reusable;

/**
 * pool factory designed, each instance to be used on single thread
 */
public class PoolFactoryImpl<T extends Reusable<T>> implements PoolFactory<T> {

    private SuperPool<T> _superPool;

    private T _root;
    
    PoolFactoryImpl( SuperPool<T> superPool ) {

        _superPool = superPool;

        allocate();
    }
    
    @Override
    public T get() {

        if ( _root == null ) {
            allocate();
        }

        T obj = _root;
        _root = _root.getNext();
        obj.setNext( null );

        return obj;
    }

    private void allocate() {

        _root = _superPool.getChain();
    }
    
}
