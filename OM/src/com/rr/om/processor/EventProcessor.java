/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.core.lang.Stopable;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.model.ModelVersion;
import com.rr.model.generated.internal.events.interfaces.Alert;
import com.rr.model.generated.internal.events.interfaces.BaseOrderRequest;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.TradeBase;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.processor.states.OrderState;
import com.rr.om.processor.states.StateException;
import com.rr.om.registry.TradeRegistry;
import com.rr.om.router.OrderRouter;
import com.rr.om.validate.EventValidator;

public interface EventProcessor extends EventHandlers, Stopable {

    /**
     * shutdown the processor
     */
    @Override
    public void stop();

    /**
     * set the processors downstream router
     * 
     * @param router 
     */
    public void setProcessorRouter( OrderRouter router );
    
    /**
     * initialise the event processor, creates pools and state machine
     * 
     * @NOTE MUST BE CALLED ON THREAD THE PROCESSOR RUNS WITHIN
     */
    @Override
    public void threadedInit();

    public ModelVersion getEventModelVersion();

    public void sendAlertChain( Alert alerts );

    /**
     * create a reject message and enqueue for sending back to client for a NOS
     * 
     * @param nos
     * @param rejectReason
     * @param reason
     * @param status
     */
    public void sendReject( NewOrderSingle nos, ZString rejectReason, OrdRejReason reason, OrdStatus status );

    /**
     * create a client cancel reject to send back to client
     * 
     * @param msg
     * @param rejectReason
     * @param reason
     * @param status
     */
    public void sendCancelReject( CancelRequest msg, ZString rejectReason, CxlRejReason reason, OrdStatus status );
    
    /**
     * create a client cancel replace reject to send back to client
     * 
     * @NOTE doesnt hold refernce to msg so that can be recycled
     * 
     * @param msg
     * @param rejectReason
     * @param reason
     * @param status
     */
    public void sendCancelReplaceReject( CancelReplaceRequest msg, ZString rejectReason, CxlRejReason reason, OrdStatus status );

    public EventBuilder getEventBuilder();

    public void routeMessageDownstream( Order order, NewOrderSingle newMsg ) throws StateException;


    /**
     * State Machine Accessors
     */

    public OrderState getStatePendingNew();
    public OrderState getStateOpen();
    public OrderState getStateCompleted();
    public OrderState getStatePendingCancel();
    public OrderState getStatePendingReplace();

    /**
     * process the message now
     * 
     * @NOTE only to be invoked by the processor itself OR its dispatcher
     * @param msg
     */
    @Override
    public void handleNow( Message msg );

    /**
     * get instance of order version from factory
     * 
     * @param cancelRequest
     * @param lastAcc 
     * @return
     */
    public OrderVersion getNewVersion( CancelRequest cancelRequest, OrderVersion lastAcc );

    /**
     * get instance of order version from factory
     * 
     * @param cancelRequest
     * @param lastAcc 
     * @return
     */
    public OrderVersion getNewVersion( CancelReplaceRequest repRequest, OrderVersion lastAcc );

    /**
     * recycle methods
     */
    public void freeMessage( Message msg );

    public void freeVersion( OrderVersion ver );
 
    /**
     * recycle the order, its version, its version base requests and the clOrdId chains
     * 
     * @NOTE if a map has some refs to the clOrdIds these will be void and UNSAFE
     * 
     * @param value
     */
    public void freeOrder( Order value );

    /**
     * register marketClOrdId with the order and send message downstream
     * 
     * @param msg
     */
    public void sendDownStream( BaseOrderRequest req, Order order );

    /**
     * @return the trade registry
     */
    public TradeRegistry getTradeRegistry();

    /**
     * @return the processors event validator
     */
    public EventValidator getValidator();

    /**
     * @return number of order NOS/AMEND/CANCEL requests
     */
    public int size();
    
    /**
     * EXPENSIVE OPERATION, ONLY FOR USE FOR PROBLEM SOLVING OR END OF DAY
     */
    public void logStats();
    
    /**
     * METHODS INTENDED FOR PROTECTED ACCESS
     */
    
    public void enqueueUpStream( Message msg );

    public void enqueueDownStream( Message msg );

    /**
     * messages which cant be dealt with must be sent to the HUB
     * 
     * messages are not enqueued within the processor 
     * 
     * @param msg
     */
    public void sendTradeHub( TradeBase msg );
}
