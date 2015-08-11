/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor.states;

import com.rr.core.lang.ViewString;
import com.rr.core.model.Message;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.client.OMClientProfile;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.processor.EventProcessor;

public class PendingCancelReplaceState extends BaseOrderState {

    public PendingCancelReplaceState( EventProcessor proc ) {
        super( proc );
    }

    @Override
    public boolean isPending() { return true; }
    
    @Override
    public void handleReplaced( Order order, Replaced replaced ) throws StateException {

        OMClientProfile client = order.getClientProfile();
        
        OrderVersion oldVer = order.getLastAckedVerion();
        OrderVersion newVer = order.getPendingVersion();
        
        // invoke client profile replaced before chande versions
        client.handleReplaced( order, replaced );

        newVer.inherit( oldVer ); // copy cumQty etc over
        
        final ViewString newOrderId = replaced.getOrderId();
        if ( newOrderId.length() > 0 ) {
            newVer.setMarketOrderId( newOrderId );
        } else {
            newVer.setMarketOrderId( oldVer.getMarketOrderId() );
        }
        
        if ( newVer.getLeavesQty() > 0 ) {
            order.setState( _proc.getStateOpen() );
            
            newVer.setOrdStatus( oldVer.getOrdStatus() ); // fix 4.4 doesnt use REPLACED
            
        } else {
            order.setState( _proc.getStateCompleted() );
            newVer.setOrdStatus( OrdStatus.Filled ); 
        }
            
        Replaced clientReplaced = _eventBuilder.createClientReplaced( order, replaced );

        // now change lastAccepted to be the replaced version 
        _proc.freeMessage( replaced );
        _proc.freeVersion( oldVer );
        // cant free underlying order request as it may be src for event waiting to be sent out of system
        
        order.setLastAckedVerion( newVer );
        
        _proc.enqueueUpStream( clientReplaced );
    }
    
    @Override
    public void handleCancelReject( Order order, CancelReject mktReject ) throws StateException {
        
        final OrderVersion pending = order.getPendingVersion();
        final OrderVersion lastAcc = order.getLastAckedVerion();
        
        Message clientMsg;

        if ( lastAcc.getLeavesQty() == 0 ) {
            lastAcc.setOrdStatus( OrdStatus.Filled ); // shouldnt be necessary 
            order.setState( _proc.getStateCompleted() );
        } else {
            
            final OrdStatus exchangeOrdStatus = mktReject.getOrdStatus();
            
            if ( exchangeOrdStatus.getIsTerminal() ) {

                lastAcc.setOrdStatus( exchangeOrdStatus  ); 
                order.setState( _proc.getStateCompleted() );

            } else {
                order.setState( _proc.getStateOpen() );
            }
        }

        clientMsg = _eventBuilder.createClientCancelReplaceReject( order, mktReject );
            
        // now change lastAccepted to be the current version ... keep the fully populated req + cum totals

        _proc.freeMessage( mktReject );
        _proc.freeVersion( pending );
        // dont free the underlying event as its the src for the reject 
        
        order.setPendingVersion( lastAcc );
        
        _proc.enqueueUpStream( clientMsg );
    }
    
    @Override
    public void handleVagueReject( Order order, VagueOrderReject mktReject ) throws StateException {
        final OrderVersion pending = order.getPendingVersion();
        final OrderVersion lastAcc = order.getLastAckedVerion();
        
        Message clientMsg;

        if ( lastAcc.getLeavesQty() == 0 ) {
            lastAcc.setOrdStatus( OrdStatus.Filled ); // shouldnt be necessary 
            order.setState( _proc.getStateCompleted() );
        } else if ( mktReject.getIsTerminal() ) {
            OMClientProfile client = order.getClientProfile();
            client.handleVagueReject( order, mktReject );

            order.setState( _proc.getStateCompleted() );
            lastAcc.setOrdStatus( OrdStatus.Stopped ); 
        } else {
            order.setState( _proc.getStateOpen() );
        }

        clientMsg = _eventBuilder.getClientCancelReject( order, mktReject, true );
            
        // now change lastAccepted to be the current version ... keep the fully populated req + cum totals

        _proc.freeMessage( mktReject );
        _proc.freeVersion( pending );
        // dont free the underlying event as its the src for the reject 
        
        order.setPendingVersion( lastAcc );
        
        _proc.enqueueUpStream( clientMsg );
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
        
        // dont move into completed state yet ass expect ack/reject from exchange
    }
    
    @Override
    public void handleTradeCancel( Order order, TradeCancel msg ) throws StateException {
        pendingTradeCancel( order, msg );
    }

    @Override
    public void handleTradeCorrect( Order order, TradeCorrect msg ) throws StateException {
        pendingTradeCorrect( order, msg );
    }
    
    @Override
    public void handleCancelled( Order order, Cancelled cancelled ) throws StateException {
        
        // syth a reject 
        final OrderVersion lastAcc = order.getLastAckedVerion();
        lastAcc.setOrdStatus( OrdStatus.Canceled );
        order.setState( _proc.getStateCompleted() );
        
        final CancelReject reject = _eventBuilder.createClientCancelReplaceReject( order, null );
        
        _proc.freeVersion( order.getPendingVersion() );
        
        // dont free the underlying event as its the src for the reject 
        
        order.setPendingVersion( lastAcc );
        
        _proc.enqueueUpStream( reject );
        
        // now process cancelled
        
        OMClientProfile client = order.getClientProfile();
        client.handleCancelled( order, cancelled );

        Cancelled clientCancelled = _eventBuilder.createClientCanceled( order, cancelled );
        _proc.enqueueUpStream( clientCancelled );

        _proc.freeMessage( cancelled );
    }
}
