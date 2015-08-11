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

public class TradeWrapperImpl implements TradeWrapper, Reusable<TradeWrapperImpl> {

    private final ReusableString   _execId       = new ReusableString( SizeConstants.DEFAULT_EXECID_LENGTH );
    private final ReusableString   _clientExecId = new ReusableString( SizeConstants.DEFAULT_EXECID_LENGTH );
    private       Order            _order;
    private       int              _hash;
    private       int              _qty;
    private       double           _px;
    private       TradeWrapper     _next;

    public static int hashCode( Order order, ZString execId ) {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((execId == null) ? 0 : execId.hashCode());
        result = prime * result + ((order == null) ? 0 : order.hashCode());
        return result;
    }

    public static boolean equals( TradeWrapper twA, TradeWrapper other ) {
        if ( twA == other )
            return true;
        if ( other == null )
            return false;
        if ( twA.getExecId() == null ) {
            if ( other.getExecId() != null )
                return false;
        } else if ( !twA.getExecId().equals( other.getExecId() ) )
            return false;
        final Order ordA = twA.getOrder();
        if ( ordA == null ) {
            if ( other.getOrder() != null )
                return false;
        } else if ( ordA != other.getOrder() )
            return false;
        return true;
    }

    @Override
    public void reset() {
        _hash = 0;
        _execId.reset();
        _clientExecId.reset();
        _order = null;
        _qty = 0;
        _px  = 0.0;
    }

    @Override
    public final int hashCode() {
        if ( _hash == 0 ) {
            _hash = hashCode( _order, _execId ); 
        }
        
        return _hash;
    }

    @Override
    public boolean equals( TradeWrapper other ) {
        return equals( this, other );
    }
    
    public void set( Order order, ZString execId, int qty, double px ) {
        _px  = px;
        _qty = qty;
        _execId.setValue( execId );
        _order = order;
    }
    
    @Override
    public final ZString getExecId() {
        return _execId;
    }
    
    @Override
    public int     getQty() {
        return _qty;
    }
    
    @Override
    public double  getPrice(){
        return _px;
    }

    /**
     * in superpool next will always be a tradewrapper
     */
    @Override
    public TradeWrapperImpl getNext() {
        return (TradeWrapperImpl) _next;
    }

    @Override
    public void setNext( TradeWrapperImpl nxt ) {
        _next = nxt;
    }

    @Override
    public ReusableType getReusableType() {
        return OrderReusableType.TradeWrapper;
    }
    
    @Override
    public TradeWrapper getNextWrapper(){
        return _next;
    }

    /**
     * in TradeWrapperSet next could be a cancel !
     */
    @Override
    public void setNextWrapper( TradeWrapper next ) {
        _next =  next;
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
