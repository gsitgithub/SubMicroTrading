/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;


/**
 * allows tracking and reservation of outstanding orders aligned against book bid/ask qty
 * 
 * provides envelope to allow dynamic runtime implementation change based on number of thread using reserver
 *
 * @author Richard Rose
 */
public interface BookReserver {
    public void setMinResetDelayNANOS( long nanos );
    
    public void attachReserveWorkerThread( Thread t );
    
    /**
     * The timestamp is used to reset grab incase of any weird edge cases which fail to reset the current reserver
     * By default the reserve is considered stale after minResetDelayNanos;
     * 
     * @param requestedQty - qty want to reserve
     * @param currentBidQtyFromBook - current qty avail at top book
     * @param timeNanos - timestamp of tick update
     * @return quantity successfully reserved or zero if not
     */
    public int grabQty( int requestedQty, int currentQtyFromBook, long timeNanos );

    public void completed( int orderQty );

    public void reset();

    public int  getReserved();

    public int getAttachedWorkerThreads();
}
