/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.exchange;

import com.rr.core.lang.ZString;
import com.rr.core.model.ExchangeBook;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;

// @NOTE only generates one sided fill
// @TODO implement proper book and generate fills for both sides as appropriate

public class DummyOrderBook implements ExchangeBook {

    public DummyOrderBook() {
        //
    }
    
    public TradeNew add( final ZString mktOrdId, final int orderQty, final double price, final OrdType ordType, final Side side ) {
        return null;
    }

    public TradeNew amend( ZString marketOrderId, int newQty, int origQty, int fillQty, double newPrice, double origPrice, OrdType ordType, Side side ) {
        return null;
    }

    public void remove( ZString marketOrderId, int openQty, double price, OrdType ordType, Side side ) {
        //
    }
}
