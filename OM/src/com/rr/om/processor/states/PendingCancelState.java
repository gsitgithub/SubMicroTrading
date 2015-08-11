/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor.states;

import com.rr.core.model.Message;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.client.OMClientProfile;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.processor.EventProcessor;

public class PendingCancelState extends BaseOrderState {

    public PendingCancelState( EventProcessor proc ) {
        super( proc );
    }

    @Override
    public boolean isPending() { return true; }
    
    @Override
    public void handleCancelled( Order order, Cancelled cancelled ) throws StateException {

        OMClientProfile client = order.getClientProfile();
        
        OrderVersion ver = order.getPendingVersion();
        
        client.handleCancelled( order, cancelled );

        order.setState( _proc.getStateCompleted() );
            
        OrderVersion lastAcc = order.getLastAckedVerion();
        lastAcc.setOrdStatus( OrdStatus.Canceled ); // do this B4 call builder

        Cancelled clientCancelled = _eventBuilder.createClientCanceled( order, cancelled );

        // now change lastAccepted to be the cancelled version ... keep the fully populated req + cum totals

        _proc.freeMessage( cancelled );
        _proc.freeMessage( ver.getBaseOrderRequest() ); // can free the underlying cancel req as its not used in events
        _proc.freeVersion( ver );
        
        order.setPendingVersion( lastAcc );
        
        _proc.enqueueUpStream( clientCancelled );
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
                OMClientProfile client = order.getClientProfile();
                client.handleCancelReject( order, mktReject );

                lastAcc.setOrdStatus( exchangeOrdStatus  ); 
                order.setState( _proc.getStateCompleted() );

            } else {
                order.setState( _proc.getStateOpen() );
            }
        }

        clientMsg = _eventBuilder.createClientCancelReject( order, mktReject );
            
        // now change lastAccepted to be the cancelled version ... keep the fully populated req + cum totals

        _proc.freeMessage( mktReject );
        _proc.freeMessage( pending.getBaseOrderRequest() ); // can free the underlying cancel req as its not used in events
        _proc.freeVersion( pending );
        
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

        clientMsg = _eventBuilder.getClientCancelReject( order, mktReject, false );
            
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
}
