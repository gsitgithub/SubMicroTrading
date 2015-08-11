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
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.interfaces.Alert;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.Expired;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.client.OMClientProfile;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.processor.EventProcessor;
import com.rr.om.validate.EventValidator;

public class OpenState extends BaseOrderState {

    public OpenState( EventProcessor proc ) {
        super( proc );
    }
    
    @Override
    public boolean isPending() { return false; }
    
    @Override
    public void handleCancelRequest( Order order, CancelRequest cancelRequest ) throws StateException {
        
        final OrderVersion lastAcc    = order.getLastAckedVerion();
        final OrderRequest lastAccSrc = (OrderRequest) lastAcc.getBaseOrderRequest();
        
        // COPY OVER FIELDS NOT DECODED FROM LAST ACCEPTED REQUEST
        ClientCancelRequestImpl req = (ClientCancelRequestImpl) cancelRequest;
        req.setInstrument( lastAccSrc.getInstrument() );
        req.setSide(       lastAccSrc.getSide() );
        
        OrderVersion ver = _proc.getNewVersion( cancelRequest, lastAcc );
        
        order.setPendingVersion( ver );
        ver.setOrdStatus( OrdStatus.PendingCancel );
        ver.setOrderQty( lastAcc.getOrderQty() );
            
        order.setState( _proc.getStatePendingCancel() );
            
        CancelRequest mcan = _eventBuilder.createMarketCancelRequest( order, cancelRequest );
        mcan.setMessageHandler( order.getDownstreamHandler() );
        
        _proc.sendDownStream( mcan, order );
    }

    @Override
    public void handleCancelReplaceRequest( Order order, CancelReplaceRequest replaceRequest ) throws StateException {

        final OrderVersion lastAcc    = order.getLastAckedVerion();
        final OrderVersion ver        = _proc.getNewVersion( replaceRequest, lastAcc );
        
        final OMClientProfile client  = order.getClientProfile();
        
        // Apply client limits
        try {
            final Alert alerts = client.handleAmendGetAlerts( order, replaceRequest );

            if ( alerts != null ) {
                _proc.sendAlertChain( alerts );
            }
        } catch( Exception e ) {
            
            OrdStatus status = order.getPendingVersion().getOrdStatus();
            
            if ( e instanceof ValidationStateException ){
                ValidationStateException v = (ValidationStateException)e;
                _proc.sendCancelReplaceReject( replaceRequest, v.getValidationError(), CxlRejReason.Other, status );
                v.recycle();
            } else {
                ReusableString err = TLC.instance().pop();
                err.setValue( e.getMessage() );
                _proc.sendCancelReplaceReject( replaceRequest, err, CxlRejReason.Other, status );
                TLC.instance().pushback( err );
            }

            return; // still in OpenState
        }

        ver.setOrdStatus( OrdStatus.PendingReplace );
        ver.setOrderQty( replaceRequest.getOrderQty() );
        
        // must set pending version B4 call event builder
        order.setPendingVersion( ver );
            
        EventValidator v = _proc.getValidator();
        
        if ( ! v.validate( replaceRequest, order ) ) {
            
            _proc.sendCancelReplaceReject( replaceRequest, v.getRejectReason(), v.getReplaceRejectReason(), lastAcc.getOrdStatus() );
            _proc.freeMessage( replaceRequest );
            _proc.freeVersion( ver );
            order.setPendingVersion( lastAcc );
            
            return;
        }
        
        order.setState( _proc.getStatePendingReplace() );
            
        CancelReplaceRequest mrep = _eventBuilder.createMarketCancelReplaceRequest( order, replaceRequest );
        mrep.setMessageHandler( order.getDownstreamHandler() );
        
        _proc.sendDownStream( mrep, order );
    }

    
    @Override
    public void handleTradeNew( Order order, TradeNew msg ) throws StateException {

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
        final OrderVersion version = order.getLastAckedVerion();
        version.setOrdStatus( OrdStatus.Canceled );
        
        // now process cancelled
        order.setState( _proc.getStateCompleted() );
        
        OMClientProfile client = order.getClientProfile();
        client.handleCancelled( order, cancelled );

        Cancelled clientCancelled = _eventBuilder.createClientCanceled( order, cancelled );
        _proc.enqueueUpStream( clientCancelled );

        _proc.freeMessage( cancelled );
    }
    
    @Override
    public void handleExpired( Order order, Expired expired ) throws StateException {
        
        // syth an ack 
        final OrderVersion version = order.getLastAckedVerion();
        version.setOrdStatus( OrdStatus.Expired );
        
        // now process cancelled
        order.setState( _proc.getStateCompleted() );
        
        OMClientProfile client = order.getClientProfile();
        client.handleExpired( order, expired );

        Expired clientCancelled = _eventBuilder.createClientExpired( order, expired );
        _proc.enqueueUpStream( clientCancelled );

        _proc.freeMessage( expired );
    }
    
    /**
     * some exchanges may reject after ACK'ing an order
     */
    @Override
    public void handleRejected( Order order, Rejected msg ) throws StateException {
        final OrderVersion version = order.getLastAckedVerion();
        version.setOrdStatus( OrdStatus.Rejected );

        // order already has mkt orderId from the ack
        OMClientProfile client = order.getClientProfile();
        client.handleRejected( order, msg );
        
        final Rejected rej = _eventBuilder.createClientRejected( order, msg );

        _proc.enqueueUpStream( rej );

        _proc.freeMessage( msg );

        order.setState( _proc.getStateCompleted() );
    }
}
