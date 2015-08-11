/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor.states;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.DoneForDay;
import com.rr.model.generated.internal.events.interfaces.Expired;
import com.rr.model.generated.internal.events.interfaces.MarketTradeCancelWrite;
import com.rr.model.generated.internal.events.interfaces.MarketTradeCorrectWrite;
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
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.MultiLegReportingType;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.processor.EventProcessor;
import com.rr.om.registry.TradeWrapper;

public abstract class BaseOrderState implements OrderState {

    private static final Logger       _log = LoggerFactory.create( BaseOrderState.class );

    private static final ViewString   NOT_OPEN = new ViewString( "Order is not in open state" );

    private   final ZString           _name;
    protected final EventProcessor    _proc;
    protected final EventBuilder      _eventBuilder;


    public BaseOrderState( EventProcessor proc ) {
        _name = Utils.getClassName( this );
        _proc = proc;
        
        _eventBuilder = proc.getEventBuilder();
    }
    
    @Override
    public ZString getName() {
        return _name;
    }

    @Override
    public void handleCancelReject( Order order, CancelReject msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleCancelReplaceRequest( Order order, CancelReplaceRequest msg ) throws StateException {
        
        CxlRejReason reason = order.getState().isPending() ? CxlRejReason.AlreadyPending : CxlRejReason.Other;
        
        _proc.sendCancelReplaceReject( msg, NOT_OPEN, reason, order.getPendingVersion().getOrdStatus() );
        _proc.freeMessage( msg);
    }

    @Override
    public void handleCancelRequest( Order order, CancelRequest msg ) throws StateException {
        _proc.sendCancelReject( msg, NOT_OPEN, CxlRejReason.AlreadyPending, order.getPendingVersion().getOrdStatus() );
        _proc.freeMessage( msg);
    }

    @Override
    public void handleCancelled( Order order, Cancelled msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    /**
     * VagueReject handler, must only be used when exchange send a reject without clOrdId / current exchange state
     */
    @Override
    public void handleVagueReject( Order order, VagueOrderReject msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleDoneForDay( Order order, DoneForDay msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleExpired( Order order, Expired msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleNewOrderAck( Order order, NewOrderAck msg ) throws StateException {
        // assume already sythd an ack ... get mktOrderId if currently blank
        
        final  OrderVersion ver             = order.getLastAckedVerion();
        final  ZString      existingOrderId = ver.getMarketOrderId();
        
        if ( existingOrderId.length() == 0 ) {
            final ZString marketOrderId = msg.getOrderId();
            ver.setMarketOrderId( marketOrderId );
        }
    }

    @Override
    public void handleNewOrderSingle( Order order, NewOrderSingle msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleOrderStatus( Order order, OrderStatus msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleRejected( Order order, Rejected msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleReplaced( Order order, Replaced msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleRestated( Order order, Restated msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleStopped( Order order, Stopped msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleSuspended( Order order, Suspended msg ) throws StateException {
        throwUnhandledEvent( order, msg );
    }

    @Override
    public void handleTradeCancel( Order order, TradeCancel msg ) throws StateException {

        final TradeWrapper origTradeDetails   = _proc.getTradeRegistry().get( order, msg.getExecRefID() );
        final TradeWrapper cancelTradeDetails = _proc.getTradeRegistry().get( order, msg.getExecId() );
        
        final OrderVersion lastAcc = order.getLastAckedVerion();
        
        final MarketTradeCancelWrite cancel = (MarketTradeCancelWrite)msg;
        
        // many exchanges cannot provide the correct qty and price so enrich message with real values
        cancel.setLastQty( origTradeDetails.getQty() );
        cancel.setLastPx(  origTradeDetails.getPrice() );
        
        // adjust client profile BEFORE apply fill to version
        order.getClientProfile().handleTradeCancel( order, msg );
        lastAcc.applyTradeCancel( msg );

        if ( lastAcc.getOrdStatus() == OrdStatus.PartiallyFilled && lastAcc.getCumQty() == 0 ) {
            lastAcc.setOrdStatus( OrdStatus.New );
        }
        
        final TradeCancel canc = _eventBuilder.createClientTradeCancel( order, msg, origTradeDetails, cancelTradeDetails );

        if ( order.getState() == _proc.getStateCompleted() && order.getClientProfile().isSendClientLateFills() == false ) {
            _proc.sendTradeHub( canc );
            
        } else {
            _proc.enqueueUpStream( canc );
        }

        _proc.freeMessage( msg );
    }

    @Override
    public void handleTradeCorrect( Order order, TradeCorrect msg ) throws StateException {
        final TradeWrapper origTradeDetails   = _proc.getTradeRegistry().get( order, msg.getExecRefID() );
        final TradeWrapper cancelTradeDetails = _proc.getTradeRegistry().get( order, msg.getExecId() );
        
        final OrderVersion lastAcc = order.getLastAckedVerion();
        
        final MarketTradeCorrectWrite correct = (MarketTradeCorrectWrite)msg;
        
        // add orig values
        correct.setOrigQty( origTradeDetails.getQty() );
        correct.setOrigPx(  origTradeDetails.getPrice() );
        
        // adjust client profile BEFORE apply fill to version
        order.getClientProfile().handleTradeCorrect( order, msg );
        lastAcc.applyTradeCorrect( msg );

        if ( lastAcc.getOrdStatus() == OrdStatus.PartiallyFilled && lastAcc.getCumQty() == 0 ) {
            lastAcc.setOrdStatus( OrdStatus.New );
        }
        
        final TradeCorrect canc = _eventBuilder.createClientTradeCorrect( order, msg, origTradeDetails, cancelTradeDetails );

        if ( order.getState() == _proc.getStateCompleted() && order.getClientProfile().isSendClientLateFills() == false ) {
            _proc.sendTradeHub( canc );
            
        } else {
            _proc.enqueueUpStream( canc );
        }

        _proc.freeMessage( msg );
    }

    @Override
    public void handleTradeNew( Order order, TradeNew msg ) throws StateException {

        TradeNew fill = commonTradeNew( order, msg );
        
        if ( order.getState() == _proc.getStateCompleted() && order.getClientProfile().isSendClientLateFills() == false ) {
            _proc.sendTradeHub( fill );
            
        } else {
            _proc.enqueueUpStream( fill );
        }

        _proc.freeMessage( msg );

        if ( order.getLastAckedVerion().getLeavesQty() <= 0 ) {
            order.setState( _proc.getStateCompleted() );
        }
    }
    
    protected TradeNew commonTradeNew( Order order, TradeNew msg ) throws StateException {
        final OrderVersion lastAcc = order.getLastAckedVerion();
        
        if ( msg.getMultiLegReportingType() != MultiLegReportingType.LegOfSpread ) {
            // adjust client profile BEFORE apply fill to version
            order.getClientProfile().handleTradeNew( order, msg );
            lastAcc.applyFill( msg );
        }

        TradeWrapper tradeWrapper = _proc.getTradeRegistry().get( order, msg.getExecId() );

        TradeNew fill = _eventBuilder.createClientTradeNew( order, msg, tradeWrapper );

        return fill;
    }

    @Override
    public void onEnter( Order order ) {
        // nothing by default
    }

    @Override
    public void onExit( Order order ) {
        // nothing by default
    }

    protected StateException throwUnhandledEvent( Order order, Message msg ) throws StateException {
        
        ReusableString s = TLC.instance().pop();
        
        try {
            s.append( "Event " ).append(  msg.getClass().getSimpleName() );
            s.append( " not supported for current state=" ).append( _name );
            s.append( ", order=" );
            
            order.appendDetails( s );
            
            throw new StateException( s.toString() );
        } finally {
            TLC.instance().pushback( s );
        }
    }
    

    protected void ignoreEvent( Order order, CommonExecRpt exec ) {
        ReusableString s = TLC.instance().pop();
        
        s.append( "Event " ).append( exec.getClass().getSimpleName() );
        s.append( " not supported for current state=" ).append( _name );
        s.append( ", execId=" ).append( exec.getExecId() );
        s.append( ", clOrdId=" ).append( exec.getClOrdId() );
        s.append( ", orderId=" ).append( exec.getOrderId() );
            
        _log.info( s );
        
        TLC.instance().pushback( s );
    }

    protected void ignoreEvent( Order order, CancelReject msg ) {
        ReusableString s = TLC.instance().pop();
        
        s.append( "Event CancelReject" );
        s.append( " not supported for current state=" ).append( _name );
        s.append( ", clOrdId=" ).append( msg.getClOrdId() );
        s.append( ", orderId=" ).append( msg.getOrderId() );
            
        _log.info( s );
        
        TLC.instance().pushback( s );
    }
    

    protected void pendingTradeCancel( Order order, TradeCancel msg ) throws StateException {
        final TradeWrapper origTradeDetails   = _proc.getTradeRegistry().get( order, msg.getExecRefID() );
        final TradeWrapper cancelTradeDetails = _proc.getTradeRegistry().get( order, msg.getExecId() );
        
        final OrderVersion           lastAcc = order.getLastAckedVerion();
        final MarketTradeCancelWrite cancel  = (MarketTradeCancelWrite)msg;
        
        // many exchanges cannot provide the correct qty and price so enrich message with real values
        cancel.setLastQty( origTradeDetails.getQty() );
        cancel.setLastPx(  origTradeDetails.getPrice() );
        
        // adjust client profile BEFORE apply fill to version
        order.getClientProfile().handleTradeCancel( order, msg );
        lastAcc.applyTradeCancel( msg );

        if ( lastAcc.getOrdStatus() == OrdStatus.PartiallyFilled && lastAcc.getCumQty() == 0 ) {
            lastAcc.setOrdStatus( OrdStatus.New );
        }

        final TradeCancel canc = _eventBuilder.createClientTradeCancel( order, msg, origTradeDetails, cancelTradeDetails );

        _proc.enqueueUpStream( canc );
        _proc.freeMessage( msg );
    }
    
    protected void pendingTradeCorrect( Order order, TradeCorrect msg ) throws StateException {
        final TradeWrapper origTradeDetails   = _proc.getTradeRegistry().get( order, msg.getExecRefID() );
        final TradeWrapper cancelTradeDetails = _proc.getTradeRegistry().get( order, msg.getExecId() );
        
        final OrderVersion lastAcc = order.getLastAckedVerion();
        
        final MarketTradeCorrectWrite correct = (MarketTradeCorrectWrite)msg;
        
        // many exchanges cannot provide the correct qty and price so enrich message with real values
        correct.setOrigQty( origTradeDetails.getQty() );
        correct.setOrigPx(  origTradeDetails.getPrice() );
        
        // adjust client profile BEFORE apply fill to version
        order.getClientProfile().handleTradeCorrect( order, msg );
        lastAcc.applyTradeCorrect( msg );

        if ( lastAcc.getOrdStatus() == OrdStatus.PartiallyFilled && lastAcc.getCumQty() == 0 ) {
            lastAcc.setOrdStatus( OrdStatus.New );
        }

        final TradeCorrect canc = _eventBuilder.createClientTradeCorrect( order, msg, origTradeDetails, cancelTradeDetails );

        _proc.enqueueUpStream( canc );
        _proc.freeMessage( msg );
    }
}
