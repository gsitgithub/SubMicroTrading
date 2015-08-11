/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.registry;

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.om.order.Order;
import com.rr.om.order.OrderReusableType;

public class TradeCorrectWrapperImpl implements TradeCorrectWrapper, Reusable<TradeCorrectWrapperImpl> {

    private final ReusableString   _execId       = new ReusableString( SizeConstants.DEFAULT_EXECID_LENGTH );
    private final ReusableString   _clientExecId = new ReusableString( SizeConstants.DEFAULT_EXECID_LENGTH );
    private final ReusableString   _execRefId    = new ReusableString( SizeConstants.DEFAULT_EXECID_LENGTH );
    private       Order            _order;
    private       int              _hash;
    private       int              _qty;
    private       double           _px;
    private       TradeWrapper     _next;

    @Override
    public final int hashCode() {
        if ( _hash == 0 ) {
            _hash = TradeWrapperImpl.hashCode( _order, _execId ); 
        }
        
        return _hash;
    }

    @Override
    public boolean equals( TradeWrapper other ) {
        return TradeWrapperImpl.equals( this, other );
    }
    
    @Override
    public void reset() {
        _execId.reset();
        _execRefId.reset();
        _clientExecId.reset();
        _hash  = 0;
        _order = null;
        _qty   = 0;
        _px    = 0.0;
        _next  = null;
    }

    public void set( Order order, ZString execId, ZString execRefId, int qty, double px ) {
        _order = order;
        _px  = px;
        _qty = qty;
        _execId.setValue( execId );
        _execRefId.setValue( execRefId );
    }
    
    @Override
    public final ZString getExecId() {
        return _execId;
    }

    @Override
    public final ZString getExecRefId() {
        return _execRefId;
    }

    @Override
    public int getQty() {
        return _qty;
    }
    
    @Override
    public double getPrice(){
        return _px;
    }

    @Override
    public TradeCorrectWrapperImpl getNext() {
        return (TradeCorrectWrapperImpl) _next;
    }

    @Override
    public void setNext( TradeCorrectWrapperImpl nxt ) {
        _next = nxt;
    }

    @Override
    public ReusableType getReusableType() {
        return OrderReusableType.TradeCorrectWrapper;
    }
    
    @Override
    public TradeWrapper getNextWrapper(){
        return _next;
    }

    /**
     * used by the TradeWrapperSet so it can treat trades/cancels/corrects together
     */
    @Override
    public void setNextWrapper( TradeWrapper next ) {
        _next = next;
    }

    @Override
    public ZString getClientExecId() {
        return _clientExecId;
    }

    @Override
    public void setClientExecId( ZString execId ) {
        _clientExecId.setValue( execId );
    }

    @Override
    public Order getOrder() {
        return _order;
    }
}
