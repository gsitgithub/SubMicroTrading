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
import com.rr.core.algo.strats.Strategy.ExchangeHandler;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
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

/**
 * stateless strategy order state handler for PendingNew state
 *
 * DONT PUT ANY STATE IN HERE, USE the StratOrder order, StratInstrumentStateWrapper for strategy instance vars like log buffer / trace
 */
public class BaseStratOrdState implements StratOrderState {

    private static final ErrorCode ERR_UNSUPPORTED_EVENT = new ErrorCode( "BSO100", "NOT FULL OMS, UNSUPPORTED EVENT, MANUAL CHECK REQUIRED" );

    @Override
    public void handleNewOrderAck( StratOrder order, StratInstrumentStateWrapper<?> instStat, NewOrderAck msg ) {
        logUnexpected( order, instStat, msg );
    }

    @Override
    public void handleTradeNew( StratOrder order, StratInstrumentStateWrapper<?> instStat, TradeNew msg ) {
        instStat.updateStratInstState( msg );
    }

    @Override
    public void handleCancelReject( StratOrder order, StratInstrumentStateWrapper<?> instStat, CancelReject msg ) {
        logUnexpected( order, instStat, msg );
    }

    @Override
    public void handleRejected( StratOrder order, StratInstrumentStateWrapper<?> instStat, Rejected msg ) {
        // standard terminal handling will adjust as necessary
    }

    @Override
    public void handleCancelled( StratOrder order, StratInstrumentStateWrapper<?> instStat, Cancelled msg ) {
        // standard terminal handling will adjust as necessary
    }

    @Override
    public void handleReplaced( StratOrder order, StratInstrumentStateWrapper<?> instStat, Replaced msg ) {
        logUnexpected( order, instStat, msg );
    }

    @Override
    public void handleDoneForDay( StratOrder order, StratInstrumentStateWrapper<?> instStat, DoneForDay msg ) {
        logUnexpected( order, instStat, msg );
    }

    @Override
    public void handleStopped( StratOrder order, StratInstrumentStateWrapper<?> instStat, Stopped msg ) {
        logUnexpected( order, instStat, msg );
    }

    @Override
    public void handleExpired( StratOrder order, StratInstrumentStateWrapper<?> instStat, Expired msg ) {
        logUnexpected( order, instStat, msg );
    }

    @Override
    public void handleSuspended( StratOrder order, StratInstrumentStateWrapper<?> instStat, Suspended msg ) {
        logUnexpected( order, instStat, msg );
    }

    @Override
    public void handleRestated( StratOrder order, StratInstrumentStateWrapper<?> instStat, Restated msg ) {
        logUnexpected( order, instStat, msg );
    }

    @Override
    public void handleTradeCorrect( StratOrder order, StratInstrumentStateWrapper<?> instStat, TradeCorrect msg ) {
        logError( ERR_UNSUPPORTED_EVENT, order, instStat, msg );
    }

    @Override
    public void handleTradeCancel( StratOrder order, StratInstrumentStateWrapper<?> instStat, TradeCancel msg ) {
        instStat.updateStratInstState( msg );
    }

    @Override
    public void handleOrderStatus( StratOrder order, StratInstrumentStateWrapper<?> instStat, OrderStatus msg ) {
        // dont care
    }

    @Override
    public void handleVagueReject( StratOrder order, StratInstrumentStateWrapper<?> instStat, VagueOrderReject msg ) {
        logUnexpected( order, instStat, msg );
    }

    public static final void traceLogEvent( StratOrder order, StratInstrumentStateWrapper<?> instStat, CommonExecRpt msg ) {
        final ExchangeHandler<?> exHdl = instStat.getExchangeHandler();
        
        final boolean isPosDup = msg.getPossDupFlag();
        
        if ( exHdl.isTrace() || isPosDup ) {
            final ReusableString traceMsg = exHdl.getTraceMsg();
            traceMsg.copy( exHdl.getComponentId() );
            traceMsg.append( (isPosDup) ? " POS DUP MarketEvent " : " MarketEvent " );
            if ( order != null ) {
                traceMsg.append( " order=" );
                order.dump( traceMsg );
            }
            traceMsg.append( " : " );
            msg.dump( traceMsg );
            traceMsg.append( ", qSize=" ).append( exHdl.getCurrQSize() );
            
            exHdl.getStrategy().getLogger().info( traceMsg );
        }
    }

    protected final void logError( ErrorCode err, StratOrder order, StratInstrumentStateWrapper<?> instStat, Message msg ) {
        final ExchangeHandler<?> exHdl = instStat.getExchangeHandler();
        final ReusableString traceMsg = exHdl.getTraceMsg();
        traceMsg.copy( exHdl.getComponentId() );
        traceMsg.append( " : " );
        traceMsg.append( " order=" );
        order.dump( traceMsg );
        traceMsg.append( " : " );
        msg.dump( traceMsg );
        traceMsg.append( ", qSize=" ).append( exHdl.getCurrQSize() );
        
        exHdl.getStrategy().getLogger().error( err, traceMsg );
    }

    protected final void logUnexpected( StratOrder order, StratInstrumentStateWrapper<?> instStat, Message msg ) {
        final ExchangeHandler<?> exHdl = instStat.getExchangeHandler();
        final ReusableString traceMsg = exHdl.getTraceMsg();
        traceMsg.copy( exHdl.getComponentId() );
        traceMsg.append( " UNEXPECTED MarketEvent " );
        traceMsg.append( " order=" );
        order.dump( traceMsg );
        traceMsg.append( " : " );
        msg.dump( traceMsg );
        traceMsg.append( ", qSize=" ).append( exHdl.getCurrQSize() );
        
        exHdl.getStrategy().getLogger().info( traceMsg );
    }

    protected void changeState( StratOrder order, StratOrderState newState ) {
        order.setState( newState );
    }
}
