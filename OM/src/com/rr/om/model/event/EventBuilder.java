/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.event;

import com.rr.core.codec.BaseReject;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.Expired;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.SessionReject;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.Side;
import com.rr.om.order.Order;
import com.rr.om.registry.TradeWrapper;

/**
 * responsible for managing event pools and the population of events
 * 
 * @author Richard Rose
 */
public interface EventBuilder {

    /**
     * to be invoked by the thread owning the  event builder instance
     */
    public void initPools();
    
    public Rejected synthNOSRejected( NewOrderSingle nos, ZString rejectReason, OrdRejReason reason, OrdStatus status );

    public CancelReject getCancelReject( ZString          clOrdId, 
                                         ZString          origClOrdId, 
                                         ZString          orderId, 
                                         ZString          rejectReason, 
                                         CxlRejReason     reason,
                                         CxlRejResponseTo msgTypeRejected, 
                                         OrdStatus        status );


    /**
     * generate a rejected from exchange or from downstream unable to dispatch
     * 
     * @param order
     * @param msg
     * @return
     */
    public Rejected createClientRejected( Order order, Rejected msg );
    
    /**
     * create a cancel reject message to be back to the client, must be response to cancel request
     * @param order
     * @param mktReject
     * @return
     */
    public CancelReject createClientCancelReject( Order order, CancelReject mktReject );

    /**
     * create a market new order single from the order
     * 
     * enrich market order
     * 
     * populate version with any overrides that need to be retained
     * 
     * @param order
     * @param nos 
     * @return
     */
    public NewOrderSingle createMarketNewOrderSingle( Order order, NewOrderSingle nos );

    /**
     * create a client new order ack based on the market ack
     * 
     * @param order
     * @param msg   market ack or null if synthesizing an ack
     * @return
     */
    public NewOrderAck createClientNewOrderAck( Order order, NewOrderAck msg );

    /**
     * create a force cancel request for an UNKNOWN order to go to exchange
     * 
     * @param clOrdId
     * @param side
     * @param orderId
     * @param srcLinkId - linkId for parent/upstream order
     * @return
     */
    public CancelRequest createForceMarketCancel( ViewString clOrdId, Side side, ViewString orderId, ViewString srcLinkId );

    /**
     * create a market cancel request
     * @param order
     * @param cancelRequest
     * @return
     */
    public CancelRequest createMarketCancelRequest( Order order, CancelRequest clientCancelRequest );

    /**
     * create a client cancel
     * 
     * @param order
     * @param cancelled market cancelled message
     * @return
     */
    public Cancelled createClientCanceled( Order order, Cancelled cancelled );

    /**
     * create a client fill
     * 
     * @param order
     * @param msg
     * @param tradeWrapper the trade registration entry
     * @return
     */
    public TradeNew createClientTradeNew( Order order, TradeNew msg, TradeWrapper tradeWrapper );

    /**
     * create a market cancel replace request
     * 
     * @param order
     * @param replaceRequest
     * @return
     */
    public CancelReplaceRequest createMarketCancelReplaceRequest( Order order, CancelReplaceRequest replaceRequest );

    /**
     * create a client side replaced
     * @param order
     * @param replaced
     * @return
     */
    public Replaced createClientReplaced( Order order, Replaced replaced );
    
    /**
     * create a client side cancel replace reject based on the pending version
     * 
     * @param order
     * @param mktReject
     * @return
     */
    public CancelReject createClientCancelReplaceReject( Order order, CancelReject mktReject );

    /**
     * create a client trade cancel
     * 
     * @param order
     * @param msg
     * @param cancelTradeDetails 
     * @param origTradeDetails 
     * @return
     */
    public TradeCancel createClientTradeCancel( Order order, TradeCancel msg, TradeWrapper origTradeDetails, TradeWrapper cancelTradeDetails );

    /**
     * create a client trade correct
     * 
     * @param order
     * @param msg
     * @return
     */
    public TradeCorrect createClientTradeCorrect( Order order, TradeCorrect msg, TradeWrapper origTrade, TradeWrapper correctWrapper );

    /**
     * create a session reject from a decode exception ... strictly speaking should be an execRpt 150=8, not session reject
     */
    public SessionReject createSessionReject( String err, BaseReject<?> msg );

    /**
     * create an amend/cancel reject from a decode exception
     */
    public CancelReject getCancelReject( ZString          clOrdId, 
                                         ZString          origClOrdId, 
                                         String           message, 
                                         CxlRejResponseTo cancelrequest, 
                                         OrdStatus        status );

    /**
     * create a client reject for a NOS
     * 
     * @param clOrdId
     * @param status
     * @param message
     * @param msg
     * @return
     */
    public Message getNOSReject( ViewString clOrdId, OrdStatus status, String message, BaseReject<?> msg );

    /**
     * create a client cancel reject for Amend or Cancel
     * @param order
     * @param mktReject
     * @param isAmend
     * @return
     */
    public CancelReject getClientCancelReject( Order order, VagueOrderReject mktReject, boolean isAmend );

    public Expired createClientExpired( Order order, Expired expired );
}
