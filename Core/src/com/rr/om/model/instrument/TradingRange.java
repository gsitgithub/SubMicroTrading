/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.instrument;

import com.rr.core.lang.ReusableString;

/**
 * the PT server when it detects a PT movement should order messages to ensure that the band is as wide as possible
 * eg if current is 10 - 12, and new is 11 - 13, then first event should be 13 so 10 - 13, then 11 so 11 - 13
 * its unlikely in aggressive trading this extra band will cause exchange PT failure ... 
 * this is consistent anyway with conflation and reuters even wombat feed
 */

public interface TradingRange {

    /**
     * validate the price must be threadsafe against the PT update thread
     * 
     * @param price     price of the order
     * @param isBuy     is the price for a buy side order
     * @param err       the buffer to APPEND a failed validation message
     * @return
     */
    public boolean valid( double price, boolean isBuySide, ReusableString err );

    /**
     * it is invokers responsibility to verify the prices, this routine doesnt check the values
     * 
     * @param lower
     * @param flags bit flags indicating how threshold was calculated
     */
    public void setMinSell( long tickId, double lower, int flags );
    
    /**
     * set the maximum value to sell
     * @param tickId
     * @param upper
     */
    public void setMaxBuy( long tickId, double upper, int flags );
}
