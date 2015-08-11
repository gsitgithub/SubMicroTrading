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
import com.rr.core.lang.ErrorCode;
import com.rr.model.generated.internal.events.interfaces.Restated;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeNew;


public final class StratOrdTerminalState extends BaseStratOrdState {

    private static final ErrorCode LATE_FILL            = new ErrorCode( "SOT100", "Late Fill" );
    private static final ErrorCode LATE_TRADE_CANCEL    = new ErrorCode( "SOT200", "Late Trade Cancel" );

    @Override
    public void handleRestated( StratOrder order, StratInstrumentStateWrapper<?> instStat, Restated msg ) {
        
        if ( ! msg.getOrdStatus().getIsTerminal() ) {
            // order is open on exchange cancel it
            
            instStat.getExchangeHandler().enqueueCancel( msg.getSide(), msg.getClOrdId(), msg.getOrderId(), null );
            
            instStat.updateStratInstState( msg );
            
            changeState( order, instStat.getStrategy().getStratOrdStates()._pendingCancel );
        }
    }
    
    @Override
    public void handleTradeNew( StratOrder order, StratInstrumentStateWrapper<?> instStat, TradeNew msg ) {

        logError( LATE_FILL, order, instStat, msg );

        super.handleTradeNew( order, instStat, null );
    }
    
    @Override
    public void handleTradeCancel( StratOrder order, StratInstrumentStateWrapper<?> instStat, TradeCancel msg ) {
        logError( LATE_TRADE_CANCEL, order, instStat, msg );
        
        super.handleTradeCancel( order, instStat, msg );
    }
}
