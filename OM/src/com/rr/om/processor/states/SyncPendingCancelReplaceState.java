/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor.states;

import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.om.order.Order;
import com.rr.om.processor.EventProcessor;

public class SyncPendingCancelReplaceState extends PendingCancelReplaceState {

    public SyncPendingCancelReplaceState( EventProcessor proc ) {
        super( proc );
    }

    @Override
    public boolean isPending() { return true; }
    
    @Override
    public void handleReplaced( Order order, Replaced replaced ) throws StateException {
        synchronized( order ) {
            super.handleReplaced( order, replaced );
        }
    }
    
    @Override
    public void handleCancelReject( Order order, CancelReject mktReject ) throws StateException {
        synchronized( order ) {
            super.handleCancelReject( order, mktReject );
        }
    }
    
    @Override
    public void handleTradeNew( Order order, TradeNew msg ) throws StateException {
        synchronized( order ) {
            super.handleTradeNew( order, msg );
        }
    }
    
    @Override
    public void handleTradeCancel( Order order, TradeCancel msg ) throws StateException {
        synchronized( order ) {
            super.handleTradeCancel( order, msg );
        }
    }

    @Override
    public void handleTradeCorrect( Order order, TradeCorrect msg ) throws StateException {
        synchronized( order ) {
            super.handleTradeCorrect( order, msg );
        }
    }
    
    @Override
    public void handleCancelled( Order order, Cancelled cancelled ) throws StateException {
        synchronized( order ) {
            super.handleCancelled( order, cancelled );
        }
    }
}
