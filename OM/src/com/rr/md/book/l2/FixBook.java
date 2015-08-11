/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l2;

import com.rr.core.model.Book;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.interfaces.MDEntry;
import com.rr.model.generated.internal.events.interfaces.MDSnapshotFullRefresh;


/**
 * In FastFix a single event may be applicable to multiple books
 * 
 * This means the event processing needs to be pushed up a level
 * 
 *
 * @author Richard Rose
 */
public interface FixBook extends Book {

    /**
     * apply SINGLE incremental update
     * 
     * @param entry
     * @return true if book updated or trade occurred
     */
    public boolean applyIncrementalEntry( int eventSeqNum, MDEntry entry );

    /**
     * Apply the snapshot event
     * Replay any enqueued incremental updates checking the bookSeqNum (T83)
     * 
     * @param msg
     * 
     * @return true if event applied and book ok
     */
    public boolean applySnapshot( MDSnapshotFullRefresh msg, EventRecycler entryRecycler );
    
    public void clear();

    public int getTotalTradeVol();

    public double getTotalTraded();

    public int getLastTradeQty();

    public double getLastTradePrice();
}
