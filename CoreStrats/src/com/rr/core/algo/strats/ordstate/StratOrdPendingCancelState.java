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
import com.rr.model.generated.internal.events.interfaces.Stopped;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;


public final class StratOrdPendingCancelState extends BaseStratOrdState {

    @Override
    public void handleCancelReject( StratOrder order, StratInstrumentStateWrapper<?> instStat, CancelReject msg ) {
        if ( ! msg.getOrdStatus().getIsTerminal() ) {
            changeState( order, instStat.getStrategy().getStratOrdStates()._openState );
        }
    }

    @Override
    public void handleCancelled( StratOrder order, StratInstrumentStateWrapper<?> instStat, Cancelled msg ) {
        // terminal handling catered for in exchange handler
    }

    @Override
    public void handleDoneForDay( StratOrder order, StratInstrumentStateWrapper<?> instStat, DoneForDay msg ) {
        // terminal handling catered for in exchange handler
    }

    @Override
    public void handleStopped( StratOrder order, StratInstrumentStateWrapper<?> instStat, Stopped msg ) {
        // terminal handling catered for in exchange handler
    }

    @Override
    public void handleExpired( StratOrder order, StratInstrumentStateWrapper<?> instStat, Expired msg ) {
        // terminal handling catered for in exchange handler
    }

    @Override
    public void handleVagueReject( StratOrder order, StratInstrumentStateWrapper<?> instStat, VagueOrderReject msg ) {
        // terminal handling catered for in exchange handler
    }
}
