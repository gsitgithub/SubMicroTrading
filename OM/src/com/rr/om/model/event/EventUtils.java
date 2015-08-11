/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.event;

import com.rr.core.model.Currency;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;

public final class EventUtils {

    public static double convertForMajorMinor( OrderRequest src, double price ) {
        
        final Currency   clientCurrency  = src.getCurrency();
        final Currency   tradingCurrency = src.getInstrument().getCurrency();
        
        if ( tradingCurrency != clientCurrency ) {
        
            price = clientCurrency.majorMinorConvert( tradingCurrency, price );
        }
        
        return price;
    }
}
