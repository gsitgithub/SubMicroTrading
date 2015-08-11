/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.DoneForDay;
import com.rr.model.generated.internal.events.interfaces.Expired;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.OrderStatus;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.Restated;
import com.rr.model.generated.internal.events.interfaces.Stopped;
import com.rr.model.generated.internal.events.interfaces.Suspended;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.om.processor.states.StateException;

public interface OrderHandler {

    public void handleNewOrderSingle(       final NewOrderSingle        msg ) throws StateException;
    public void handleNewOrderAck(          final NewOrderAck           msg ) throws StateException;
    public void handleTradeNew(             final TradeNew              msg ) throws StateException;
    public void handleCancelReplaceRequest( final CancelReplaceRequest  msg ) throws StateException;
    public void handleCancelRequest(        final CancelRequest         msg ) throws StateException;
    public void handleCancelReject(         final CancelReject          msg ) throws StateException;
    public void handleVagueReject(          final VagueOrderReject      msg ) throws StateException;
    public void handleRejected(             final Rejected              msg ) throws StateException;
    public void handleCancelled(            final Cancelled             msg ) throws StateException;
    public void handleReplaced(             final Replaced              msg ) throws StateException;
    public void handleDoneForDay(           final DoneForDay            msg ) throws StateException;
    public void handleStopped(              final Stopped               msg ) throws StateException;
    public void handleExpired(              final Expired               msg ) throws StateException;
    public void handleSuspended(            final Suspended             msg ) throws StateException;
    public void handleRestated(             final Restated              msg ) throws StateException;
    public void handleTradeCorrect(         final TradeCorrect          msg ) throws StateException;
    public void handleTradeCancel(          final TradeCancel           msg ) throws StateException;
    public void handleOrderStatus(          final OrderStatus           msg ) throws StateException;
}
