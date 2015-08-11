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

public final class RecyclerImpl<T extends Reusable<T>> implements Recycler<T> {

    private final SuperPool<T> _superPool;
    private final T            _root;
    private final Class<T>     _poolClass;
    private final int          _recycleSize;
    private       int          _count = 0;

    public RecyclerImpl( Class<T> poolClass, int recycleSize, SuperPool<T> superPool ) {
        _poolClass   = poolClass;
        _superPool   = superPool;
        _recycleSize = recycleSize;
        
        try {
            _root    = _poolClass.newInstance();
            
        } catch( Exception e ) {

            throw new RuntimePoolingException( "Unable to create recycle root for " + poolClass.getSimpleName() + " : " + e.getMessage(), e );
        }
    }

    @Override
    public void recycle( final T obj ) {

        if ( obj == null ) return;
        
        if ( obj.getNext() == null ) {
            obj.reset();
    
            obj.setNext( _root.getNext() );
            _root.setNext( obj );
            
            if ( ++_count == _recycleSize ) {
                _superPool.returnChain( _root.getNext() );
                _root.setNext( null );
                _count = 0;
            }
        }
    }
}
