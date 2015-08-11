/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l3;

import com.rr.core.collections.LongHashEntry;
import com.rr.core.collections.LongHashEntryFactory;
import com.rr.core.pool.PoolFactory;
import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.events.impl.BookAddOrderImpl;
import com.rr.model.generated.internal.events.impl.BookDeleteOrderImpl;
import com.rr.model.generated.internal.events.impl.BookModifyOrderImpl;

public final class LongBookPoolMgr<T> {

    private final PoolFactory<OrderBookEntry> _orderBookEntryFactory;
    private final PoolFactory<FullBookLevelEntry> _bookLevelEntryFactory;
    
    private final Recycler<OrderBookEntry>        _orderBookEntryRecycler;
    private final Recycler<FullBookLevelEntry>    _bookLevelRecycler;
    private final Recycler<BookAddOrderImpl>      _orderBookAddRecycler;
    private final Recycler<BookModifyOrderImpl>   _orderBookModifyRecycler;
    private final Recycler<BookDeleteOrderImpl>   _orderBookDeleteRecycler;
    
    
    private final LongHashEntryFactory<T>     _entryFactory;
    private final Recycler<LongHashEntry<T>>  _entryRecycler; 
    
    @SuppressWarnings( "unchecked" )
    public LongBookPoolMgr() {
        _orderBookEntryFactory  = SuperpoolManager.instance().getPoolFactory( OrderBookEntry.class );
        _orderBookEntryRecycler = SuperpoolManager.instance().getRecycler( OrderBookEntry.class );

        _bookLevelEntryFactory  = SuperpoolManager.instance().getPoolFactory( FullBookLevelEntry.class );
        _bookLevelRecycler      = SuperpoolManager.instance().getRecycler( FullBookLevelEntry.class );

        _entryFactory           = SuperpoolManager.instance().getFactory( LongHashEntryFactory.class, LongHashEntry.class );
        _entryRecycler          = SuperpoolManager.instance().getRecycler( LongHashEntry.class );

        _orderBookAddRecycler    = SuperpoolManager.instance().getRecycler( BookAddOrderImpl.class );
        _orderBookModifyRecycler = SuperpoolManager.instance().getRecycler( BookModifyOrderImpl.class );
        _orderBookDeleteRecycler = SuperpoolManager.instance().getRecycler( BookDeleteOrderImpl.class );
    }

    public PoolFactory<OrderBookEntry> getOrderBookEntryFactory() {
        return _orderBookEntryFactory;
    }

    public PoolFactory<FullBookLevelEntry> getBookLevelEntryFactory() {
        return _bookLevelEntryFactory;
    }

    public Recycler<OrderBookEntry> getOrderBookEntryRecycler() {
        return _orderBookEntryRecycler;
    }

    public Recycler<FullBookLevelEntry> getBookLevelRecycler() {
        return _bookLevelRecycler;
    }

    public LongHashEntryFactory<T> getEntryFactory() {
        return _entryFactory;
    }

    public Recycler<LongHashEntry<T>> getEntryRecycler() {
        return _entryRecycler;
    }

    public void recycle( BookDeleteOrderImpl event ) {
        _orderBookDeleteRecycler.recycle( event );
    }

    public void recycle( BookModifyOrderImpl event ) {
        _orderBookModifyRecycler.recycle( event );
    }

    public void recycle( BookAddOrderImpl event ) {
        _orderBookAddRecycler.recycle( event );
    }
}
