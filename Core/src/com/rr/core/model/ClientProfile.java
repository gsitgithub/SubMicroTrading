/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import com.rr.core.component.SMTComponentWithPostConstructHook;
import com.rr.core.lang.ReusableString;


public interface ClientProfile extends SMTComponentWithPostConstructHook {

    public int  DEFAULT_LOW_THRESHOLD   = 60;
    public int  DEFAULT_MED_THRESHOLD   = 80;
    public int  DEFAULT_HIGH_THRESHOLD  = 90;
    public long DEFAULT_MAX_TOTAL_QTY   = Long.MAX_VALUE;
    public long DEFAULT_MAX_TOTAL_VAL   = Long.MAX_VALUE;
    public long DEFAULT_MAX_SINGLE_VAL  = Long.MAX_VALUE;
    public int  DEFAULT_MAX_SINGLE_QTY  = Integer.MAX_VALUE;
    
    public long   getTotalOrderQty();
    public double getTotalOrderValueUSD();
    public long   getTotalQty();

    public double getMaxSingleOrderValueUSD();
    public int    getMaxSingleOrderQty();
    public double getMaxTotalOrderValueUSD();
    public long   getMaxTotalQty();

    public void setThresholds( int lowThresholdPercent, int medThresholdPercent, int highThresholdPercent );
    public void setMaxTotalQty( long maxTotalQty );
    
    public void setMaxTotalOrderValueUSD( double maxTotalValueUSD );
    public void setMaxSingleOrderValueUSD( double maxSingleOrderValueUSD );
    public void setMaxSingleOrderQty( int maxSingleOrderQty );
    public void id( ReusableString out );
}
