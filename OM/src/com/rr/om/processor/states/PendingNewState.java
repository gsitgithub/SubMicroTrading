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
import com.rr.model.generated.internal.events.interfaces.Alert;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.ClientRejectedUpdate;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.client.OMClientProfile;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.processor.EventProcessor;

public class PendingNewState extends BaseOrderState {

    private static final ZString CLIENT_PROF_EXCEPTION = new ViewString( "Exception in client validation " );

    public PendingNewState( EventProcessor proc ) {
        super( proc );
    }

    @Override
    public boolean isPending() { return true; }
    
    @Override
    public void handleNewOrderSingle( Order order, NewOrderSingle cnos ) throws StateException {

        final OMClientProfile client = order.getClientProfile();
        
        final  OrderVersion ver = order.getPendingVersion();
        
        // Apply client limits
        try {
            final Alert alerts = client.handleNOSGetAlerts( order, cnos );

            if ( alerts != null ) {
                _proc.sendAlertChain( alerts );
            }
        } catch( Exception e ) {
            
            ver.setOrdStatus( OrdStatus.Rejected );
            
            ReusableString err = TLC.instance().pop();

            if ( e instanceof ValidationStateException ){
                
                ValidationStateException v = (ValidationStateException)e;
                
                ZString errMsg = v.getValidationError();
                
                err.copy( errMsg );
                
                _proc.sendReject( cnos, err, OrdRejReason.OrderExceedsLimit, OrdStatus.Rejected );
                
                v.recycle();
                
            } else {
                err.copy( CLIENT_PROF_EXCEPTION );
                err.append( e.getClass().getSimpleName() ).append( ' ' ).append( e.getMessage() );
                
                _proc.sendReject( cnos, err, OrdRejReason.OrderExceedsLimit, OrdStatus.Rejected );
            }

            TLC.instance().pushback( err );

            order.setState( _proc.getStateCompleted() );
            
            return;
        }
        
        ver.setOrdStatus( OrdStatus.PendingNew );
        final NewOrderSingle mktNOS = _eventBuilder.createMarketNewOrderSingle( order, cnos );
        _proc.routeMessageDownstream( order, mktNOS );
    }
    
    @Override
    public void handleRejected( Order order, Rejected msg ) throws StateException {
        final OrderVersion version = order.getPendingVersion();
        version.setOrdStatus( OrdStatus.Rejected );
        
        final ZString marketOrderId = msg.getOrderId();
        version.setMarketOrderId( marketOrderId );

        OMClientProfile client = order.getClientProfile();
        client.handleRejected( order, msg );
        
        final Rejected rej = _eventBuilder.createClientRejected( order, msg );

        _proc.enqueueUpStream( rej );

        _proc.freeMessage( msg );

        order.setState( _proc.getStateCompleted() );
    }

    @Override
    public void handleVagueReject( Order order, VagueOrderReject msg ) throws StateException {
        final OrderVersion version = order.getPendingVersion();
        version.setOrdStatus( OrdStatus.Rejected );
        
        OMClientProfile client = order.getClientProfile();
        client.handleVagueReject( order, msg );
        
        final ClientRejectedUpdate rej = (ClientRejectedUpdate) _eventBuilder.createClientRejected( order, null );
        rej.getTextForUpdate().copy( msg.getText() );

        _proc.enqueueUpStream( rej );

        _proc.freeMessage( msg );

        order.setState( _proc.getStateCompleted() );
    }
    
    @Override
    public void handleNewOrderAck( Order order, NewOrderAck msg ) throws StateException {

        final OrderVersion version = order.getPendingVersion();
        version.setOrdStatus( OrdStatus.New );
        
        final ZString marketOrderId = msg.getOrderId();
        version.setMarketOrderId( marketOrderId );
        
        final NewOrderAck ack = _eventBuilder.createClientNewOrderAck( order, msg );

        _proc.enqueueUpStream( ack );

        _proc.freeMessage( msg );

        order.setState( _proc.getStateOpen() );
    }

    @Override
    public void handleTradeNew( Order order, TradeNew msg ) throws StateException {
        
       // syth an ack 

        final OrderVersion version = order.getPendingVersion();
        version.setOrdStatus( OrdStatus.New );
        
        final ZString marketOrderId = msg.getOrderId();
        version.setMarketOrderId( marketOrderId );
        
        final NewOrderAck ack = _eventBuilder.createClientNewOrderAck( order, null );

        _proc.enqueueUpStream( ack );

        order.setState( _proc.getStateOpen() );
        
       // now handle the trade
        
        TradeNew fill = commonTradeNew( order, msg );

        _proc.enqueueUpStream( fill );

        _proc.freeMessage( msg );

        if ( order.getLastAckedVerion().getLeavesQty() <= 0 ) {
            order.setState( _proc.getStateCompleted() );
        }
    }

    @Override
    public void handleCancelled( Order order, Cancelled cancelled ) throws StateException {
        
        // syth an ack 
        final OrderVersion version = order.getPendingVersion();
        version.setOrdStatus( OrdStatus.Canceled );
        
        final ZString marketOrderId = cancelled.getOrderId();
        version.setMarketOrderId( marketOrderId );
        
        final NewOrderAck ack = _eventBuilder.createClientNewOrderAck( order, null );

        _proc.enqueueUpStream( ack );

        // now process cancelled
        order.setState( _proc.getStateCompleted() );
        
        OMClientProfile client = order.getClientProfile();
        client.handleCancelled( order, cancelled );

        Cancelled clientCancelled = _eventBuilder.createClientCanceled( order, cancelled );
        _proc.enqueueUpStream( clientCancelled );

        _proc.freeMessage( cancelled );
    }
}
