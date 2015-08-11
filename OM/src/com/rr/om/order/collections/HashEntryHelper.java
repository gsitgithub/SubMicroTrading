/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order.collections;

import com.rr.core.lang.ViewString;
import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.om.order.Order;
import com.rr.om.processor.EventProcessor;

/**
 * non thread safe helper to create/recycle HashEntries
 */
public final class HashEntryHelper {
    
    private HashEntryFactory    _entryFactory  = SuperpoolManager.instance().getFactory( HashEntryFactory.class, HashEntry.class );
    private Recycler<HashEntry> _entryRecycler = SuperpoolManager.instance().getRecycler( HashEntry.class );

    HashEntryHelper() {
        // dont use static factory/recycler
    }
    
    HashEntry[] newArray( int size ) {
        return new HashEntry[size];
    }

    HashEntry newEntry( ViewString key, int hash, HashEntry next, Order value ) {
        HashEntry entry = _entryFactory.get();
        entry.set( key, hash, next, value );
        return entry;
    }

    void recycleEntry( HashEntry entry, EventProcessor proc ) {
        if ( entry != null ) {

            HashEntry tmp;

            while( entry != null ) {

                // DONT RECYCLE the KEY !!

                tmp = entry;
                entry = entry.getNext();

                if ( proc != null ) {
                    proc.freeOrder( tmp._value ); // note order may have multiple refs, may already have been recycled
                }

                _entryRecycler.recycle( tmp );
            }
        }
    }


}
