/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.ModelVersion;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.processor.states.OrderState;
import com.rr.om.processor.states.SyncOpenState;
import com.rr.om.processor.states.SyncPendingCancelReplaceState;
import com.rr.om.processor.states.SyncPendingCancelState;
import com.rr.om.processor.states.SyncPendingNewState;
import com.rr.om.processor.states.SyncTerminalState;
import com.rr.om.registry.TradeRegistry;
import com.rr.om.validate.EventValidator;

public class SyncEventProcessorImpl extends EventProcessorImpl {

    public SyncEventProcessorImpl( ModelVersion         version, 
                                   int                  expectedOrders, 
                                   EventValidator       validator, 
                                   EventBuilder         builder, 
                                   MessageDispatcher    dispatcher,
                                   MessageHandler       hub, 
                                   TradeRegistry        tradeRegistry ) {
        
        super( version, expectedOrders, validator, builder, dispatcher, hub, tradeRegistry );
    }

    @Override
    protected OrderState createPendingNewState() {
        return new SyncPendingNewState( this );
    }

    @Override
    protected OrderState createOpenState() {
        return new SyncOpenState( this );
    }

    @Override
    protected OrderState createTerminalState() {
        return new SyncTerminalState( this );
    }

    @Override
    protected OrderState createPendingCancelState() {
        return new SyncPendingCancelState( this );
    }

    @Override
    protected OrderState createPendingCancelReplaceState() {
        return new SyncPendingCancelReplaceState( this );
    }
}
