/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;



/**
 * Book can be threadsafe or non threadsafe
 *
 * @author Richard Rose
 */
public interface ApiMutatableBook extends MutableBook {

    /**
     * set both levels in the book
     * 
     * @param lvl
     * @param buyQty
     * @param buyPrice
     * @param sellQty
     * @param sellPrice
     */
    public void setLevel( int lvl, int bidQty, double bidPrice, boolean bidIsDirty, int askQty, double askPrice, boolean askIsDirty );
    public void setLevel( int lvl, BookEntryImpl bid, BookEntryImpl ask );

    public void setNumLevels( int lvl );
    
    public void setBid( int lvl, BookLevelEntry entry );
    public void setBidQty( int lvl, int qty );
    public void setBidPrice( int lvl, double px );
    public void insertBid( int lvl, BookLevelEntry entry );
    public void deleteBid( int lvl );
    public void setBidDirty( boolean isDirty );

    public void setAsk( int lvl, BookLevelEntry entry );
    public void setAskQty( int lvl, int qty );
    public void setAskPrice( int lvl, double px );
    public void insertAsk( int lvl, BookLevelEntry entry );
    public void deleteAsk( int lvl );
    public void setAskDirty( boolean isDirty );

    /**
     * fix 5.0.sp2 .. delete all BID entries from lvl to MAX
     */
    public void deleteThruBid( int lvl );

    /**
     * fix 5.0.sp2 .. delete BID entries from TOP down to specified lvl
     */
    public void deleteFromBid( int lvl );

    /**
     * fix 5.0.sp2 .. delete all ASK entries from lvl to MAX
     * @param lvl 
     */
    public void deleteThruAsk( int lvl );

    /**
     * fix 5.0.sp2 .. delete ASK entries from TOP down to specified lvl 
     */
    public void deleteFromAsk( int lvl );
}
