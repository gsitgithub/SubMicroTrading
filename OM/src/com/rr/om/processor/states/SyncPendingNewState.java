/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor.states;

import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.om.order.Order;
import com.rr.om.processor.EventProcessor;

public class SyncPendingNewState extends PendingNewState {

    public SyncPendingNewState( EventProcessor proc ) {
        super( proc );
    }

    @Override
    public boolean isPending() { return true; }
    
    @Override
    public void handleNewOrderSingle( Order order, NewOrderSingle cnos ) throws StateException {
        synchronized( order ) {
            super.handleNewOrderSingle( order, cnos );
        }
    }
    
    @Override
    public void handleRejected( Order order, Rejected msg ) throws StateException {
        synchronized( order ) {
            super.handleRejected( order, msg );
        }
    }

    @Override
    public void handleNewOrderAck( Order order, NewOrderAck msg ) throws StateException {
        synchronized( order ) {
            super.handleNewOrderAck( order, msg );
        }
    }

    @Override
    public void handleTradeNew( Order order, TradeNew msg ) throws StateException {
        synchronized( order ) {
            super.handleTradeNew( order, msg );
        }
    }

    @Override
    public void handleCancelled( Order order, Cancelled cancelled ) throws StateException {
        synchronized( order ) {
            super.handleCancelled( order, cancelled );
        }
    }
}
