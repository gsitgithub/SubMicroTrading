/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order;

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.model.Exchange;
import com.rr.core.model.MessageHandler;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.om.Strings;
import com.rr.om.client.OMClientProfile;
import com.rr.om.processor.states.OrderState;

/**
 * dont add a hashCode/equals using clOrdId as this CHANGES over time
 * keep the native implementations which allow scoping of execIds by order instance
 *
 * @author Richard Rose
 */
public final class OrderImpl implements Order, Reusable<OrderImpl>  {

    private OrderImpl       _next;
    private OrderVersion    _lastAckedVersion;
    private OrderVersion    _pendingVersion;
    private OrderState      _state;
    private OMClientProfile _clientProfile;

    private MessageHandler  _stickyDownstreamHandler;
    
    private ReusableString  _clientClOrdIdChain;
    private ReusableString  _marketClOrdIdChain;

    @Override
    public void reset() {
        _clientClOrdIdChain = null;
        _marketClOrdIdChain = null;
        _next               = null;
        _lastAckedVersion   = null;
        _pendingVersion     = null;
        _state              = null;
        _clientProfile      = null;
        _stickyDownstreamHandler = null;
    }

    @Override
    public final OrderVersion getLastAckedVerion() {
        return _lastAckedVersion;
    }

    @Override
    public final OrderVersion getPendingVersion() {
        return _pendingVersion;
    }

    @Override
    public ReusableType getReusableType() {
        return OrderReusableType.Order;
    }

    @Override
    public OrderImpl getNext() {
        return _next;
    }

    @Override
    public void setNext( OrderImpl nxt ) {
        _next = nxt;
    }

    @Override
    public void appendDetails( ReusableString buf ) {

        OrderRequest req = (OrderRequest) _lastAckedVersion.getBaseOrderRequest();
        
        buf.append( ' ' );
        buf.append( Strings.CL_ORD_ID ).append( req.getClOrdId()                   ).append( Strings.DELIM );
        buf.append( Strings.SYMBOL    ).append( req.getSymbol()                    ).append( Strings.DELIM );
        buf.append( Strings.QUANTITY  ).append( req.getOrderQty()                  ).append( Strings.DELIM );
        buf.append( Strings.PRICE     ).append( req.getPrice()                     ).append( Strings.DELIM );

        if ( _state != null ) buf.append( Strings.STATE ).append( _state.getName() );
    }
    
    @Override
    public OrderState setState( OrderState state ) {

        if ( _state != null )  {
            _state.onExit( this );
        }
        
        _state = state;
        
        _state.onEnter( this );
        
        return state;
    }
    
    @Override 
    public OrderState getState() {
        return _state;
    }

    @Override
    public void setLastAckedVerion( OrderVersion ver ) {
        _lastAckedVersion = ver;
    }

    @Override
    public void setPendingVersion( OrderVersion ver ) {
        _pendingVersion = ver;
    }

    public void setClientProfile( OMClientProfile clientProfile ) {
        _clientProfile = clientProfile;
    }
    
    @Override
    public OMClientProfile getClientProfile() {
        return _clientProfile;
    }

    @Override
    public Exchange getExchange() {
        return _lastAckedVersion.getBaseOrderRequest().getInstrument().getExchange();
    }

    @Override
    public MessageHandler getDownstreamHandler() {
        return _stickyDownstreamHandler;
    }

    @Override
    public void setDownstreamHandler( MessageHandler stickyDownstreamHandler ) {
        _stickyDownstreamHandler = stickyDownstreamHandler;
    }

    @Override
    public ReusableString getClientClOrdIdChain() {
        return _clientClOrdIdChain;
    }

    @Override
    public ReusableString getMarketClOrdIdChain() {
        return _marketClOrdIdChain;
    }

    @Override
    public void registerClientClOrdId( ReusableString clientClOrdId ) {

        clientClOrdId.setNext( _clientClOrdIdChain );
        _clientClOrdIdChain = clientClOrdId;
    }

    @Override
    public void registerMarketClOrdId( ReusableString marketClOrdId ) {
        marketClOrdId.setNext( _marketClOrdIdChain );
        _marketClOrdIdChain = marketClOrdId;
    }
}
