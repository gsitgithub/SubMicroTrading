/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import com.rr.core.lang.ReusableString;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.model.book.BookLevelEntry;
import com.rr.core.model.book.DoubleSidedBookEntry;

/**
 * Book interface
 * 
 * @NOTE book is used in maps, so the equals and hashCode methods are java default
 *       do NOT implement to compare prices in two books .... create new compare methods for that
 */
public interface Book {

    public void id( ReusableString out );

    public Instrument getInstrument();
    
    /**
     * @return true if book supports locking
     */
    public boolean isLockable();
    
    /**
     * grab the lock, if the underlying implementation supports it will spin until get lock
     * 
     * @WARNING lock is non - renentrant !
     * 
     * always ensure releaseLock is used within a finally block to free the lock !
     */
    public void grabLock();
    public void releaseLock();

    /**
     * @return maxiumum number of levels in book
     */
    public int getMaxLevels();

    public int getActiveLevels();
    
    /**
     * get both entries for a particular level
     *   
     * @param lvl (0+)
     * @param dest
     * @return  false if requested level not available
     */
    public boolean getLevel( int lvl, DoubleSidedBookEntry dest );
    
    /**
     * get the requested bid entry
     * 
     * @param lvl (0+)
     * @param dest
     * @return true if level supported by book, false if not
     */
    public boolean getBidEntry( int lvl, BookLevelEntry dest );    

    /**
     * get the requested ask entry
     * 
     * @param lvl (0+)
     * @param dest
     * @return true if level supported by book, false if not
     */
    public boolean getAskEntry( int lvl, BookLevelEntry dest );    
    
    /**
     * write copy of book to destination upto max levels as supported in the destination book
     * 
     * if source book has 20 levels but dest book only supports 5 then only top 5 copied
     *
     * if book is threadsafe may spinlock against mutating thread
     * 
     * only copies the book, lastTickId and lastTickInNanos 
     * 
     * @param dest
     */
    public void snap( ApiMutatableBook dest );
    
    public void dump( ReusableString dest );

    /**
     * @return true if bid and ask populated, and prices not crossed, ie ask > bid 
     */
    public boolean isValidBBO();
    
    /**
     * @return number of tick updates (snapshot counts as 1 only)
     */
    public int getTickCount();

    public int getLastTickId();
    
    /**
     * @return event id causing last update
     */
    public int getMsgSeqNum();
    
    /**
     * @return nanoTime from after reading packet off socket (assuming logStats set on Session)
     *         or 0 if another event caused book change ... eg setting book to dirty
     */
    public long getLastTickInNanos();

    /**
     * @return timestamp as set by the exchange
     */
    public long getLastExchangeTickTime();
    
    public BookContext getContext();

    /**
     * allow association of a context object with the book
     * 
     * only one context block allowed per book
     * 
     * @param context new context block
     * @return previous context block
     */
    public BookContext setContext( BookContext context );

    /**
     * mark book as potentially out of date
     */
    public void setDirty( boolean isDirty );
}
