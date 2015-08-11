/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

import com.rr.core.lang.ReusableString;

public interface DoubleSidedBookEntry {

    public double getBidPx();

    public int getBidQty();

    public double getAskPx();

    public int getAskQty();

    public void set( int buyQty, double buyPrice, boolean buyIsDirty, int sellQty, double sellPrice, boolean sellIsDirty );

    public void set( BookLevelEntry bid, BookLevelEntry ask );
    
    /**
     * reset values back to zero
     */
    public void reset();

    public void dump( ReusableString dest );

    /**
     * @return true if bid and ask not dirty and bid < ask
     */
    public boolean isValid();

    public boolean isBuyIsDirty();

    public boolean isSellIsDirty();
}
