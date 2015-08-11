/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.strats.ordstate;

import com.rr.core.algo.strats.StratInstrumentStateWrapper;
import com.rr.core.algo.strats.StratOrder;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.DoneForDay;
import com.rr.model.generated.internal.events.interfaces.Expired;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
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


public interface StratOrderState {

    public void handleNewOrderAck( StratOrder order, StratInstrumentStateWrapper<?> instStat, NewOrderAck msg );

    public void handleTradeNew( StratOrder order, StratInstrumentStateWrapper<?> instStat, TradeNew msg );

    public void handleCancelReject( StratOrder order, StratInstrumentStateWrapper<?> instStat, CancelReject msg );

    public void handleRejected( StratOrder order, StratInstrumentStateWrapper<?> instStat, Rejected msg );

    public void handleCancelled( StratOrder order, StratInstrumentStateWrapper<?> instStat, Cancelled msg );

    public void handleReplaced( StratOrder order, StratInstrumentStateWrapper<?> instStat, Replaced msg );

    public void handleDoneForDay( StratOrder order, StratInstrumentStateWrapper<?> instStat, DoneForDay msg );

    public void handleStopped( StratOrder order, StratInstrumentStateWrapper<?> instStat, Stopped msg );

    public void handleExpired( StratOrder order, StratInstrumentStateWrapper<?> instStat, Expired msg );

    public void handleSuspended( StratOrder order, StratInstrumentStateWrapper<?> instStat, Suspended msg );

    public void handleRestated( StratOrder order, StratInstrumentStateWrapper<?> instStat, Restated msg );

    public void handleTradeCorrect( StratOrder order, StratInstrumentStateWrapper<?> instStat, TradeCorrect msg );

    public void handleTradeCancel( StratOrder order, StratInstrumentStateWrapper<?> instStat, TradeCancel msg );

    public void handleOrderStatus( StratOrder order, StratInstrumentStateWrapper<?> instStat, OrderStatus msg );

    public void handleVagueReject( StratOrder order, StratInstrumentStateWrapper<?> instStat, VagueOrderReject msg );

}
