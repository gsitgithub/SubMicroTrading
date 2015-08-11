/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order;

import com.rr.core.lang.ReusableString;
import com.rr.core.model.Exchange;
import com.rr.core.model.MessageHandler;
import com.rr.om.client.OMClientProfile;
import com.rr.om.processor.states.OrderState;

/**
 * Order
 *
 * @NOTE cancels treated specially, when order cancel acked, the cancel version is recycled, we keep the full last accepted version
 * BUT note the clOrdId on this is NOT the one from the cancel so BEWARE  .. will be issue for BUSTS so force BUSTS thru Master process
 */
public interface Order {

    public void registerClientClOrdId( ReusableString clientClOrdId );
    
    public void registerMarketClOrdId( ReusableString marketClOrdId );

    /*
     * the current clOrdId, only one pending message is allowed, so first in chain is either pending or the lastAccepted id 
     */
    public ReusableString getClientClOrdIdChain();

    /**
     * if the mkt clOrdId is same as client then the market clOrdId chain is NULL

     * @return the first link in chain or null
     */
    public ReusableString getMarketClOrdIdChain();

    public OrderVersion getLastAckedVerion();
    
    public OrderVersion getPendingVersion();
    
    /**
     * write details about current order into the reusable buffer
     * should generate NO temp objects
     * 
     * @param buf
     */
    public void appendDetails( ReusableString buf );

    /**
     * set the order state, will invoke onExit from previous state
     * then onEnter for this new state
     * 
     * @param state
     * @return previous state
     */
    public OrderState setState( OrderState state );

    /**
     * @return the current order state
     */
    public OrderState getState();

    /**
     * set the current / last accepted version, will only be a NEW or AMEND event never the part populated cancel
     * 
     * @param ver
     */
    public void setLastAckedVerion( OrderVersion ver );

    /**
     * set a pending version, if same as lastAccepted then order is NOT in pending cancel or pending replace state
     * 
     * @param ver
     */
    public void setPendingVersion( OrderVersion ver );

    public OMClientProfile getClientProfile();

    public Exchange getExchange();

    public MessageHandler getDownstreamHandler();

    public void setDownstreamHandler( MessageHandler stickyDownstreamHandler );
}
