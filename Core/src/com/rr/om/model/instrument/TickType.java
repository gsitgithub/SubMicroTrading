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
import com.rr.core.lang.ZString;

public interface TickType {

    /**
     * @param price
     * @return true if the price is valid
     */
    public boolean isValid( double price );

    /**
     * when price is not valid APPEND log details to the supplied buffer
     * 
     * @param price
     */
    public void writeError( double price, ReusableString err );
    
    /**
     * @return true if the tick type is setup properly aand can verify prices
     */
    public boolean canVerifyPrice();

    /**
     * @return id/name of the tick type
     */
    public ZString getId();
}
