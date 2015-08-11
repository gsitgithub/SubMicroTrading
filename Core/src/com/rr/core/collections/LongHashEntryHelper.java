/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperpoolManager;

/**
 * non thread safe helper to create/recycle HashEntries
 * 
 * dont use static factory/recycler
 */
public final class LongHashEntryHelper<T> {
    
    private final LongHashEntryFactory<T>    _entryFactory;
    private final Recycler<LongHashEntry<T>> _entryRecycler;

    @SuppressWarnings( "unchecked" )
    public LongHashEntryHelper() {
        _entryFactory  = SuperpoolManager.instance().getFactory( LongHashEntryFactory.class, LongHashEntry.class );
        _entryRecycler = SuperpoolManager.instance().getRecycler( LongHashEntry.class );
    }
    
    public LongHashEntryHelper( LongHashEntryFactory<T> entryFactory, Recycler<LongHashEntry<T>> entryRecycler ) {
        _entryFactory  = entryFactory;
        _entryRecycler = entryRecycler;
    }

    @SuppressWarnings( "unchecked" )
    LongHashEntry<T>[] newArray( int size ) {
        return new LongHashEntry[size];
    }

    LongHashEntry<T> newEntry( long key, LongHashEntry<T> next, T value ) {
        LongHashEntry<T> entry = _entryFactory.get();
        entry.set( key, next, value );
        return entry;
    }

    void recycleEntry( LongHashEntry<T> entry ) {
        if ( entry != null ) {

            LongHashEntry<T> tmp;

            while( entry != null ) {

                tmp = entry;
                entry = entry.getNext();

                _entryRecycler.recycle( tmp );
            }
        }
    }


}
