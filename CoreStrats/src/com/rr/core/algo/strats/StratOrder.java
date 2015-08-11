/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.strats;

import com.rr.core.algo.base.StratReusableTypes;
import com.rr.core.algo.strats.ordstate.StratOrderState;
import com.rr.core.lang.Constants;
import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ZString;
import com.rr.core.utils.StringUtils;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.type.Side;


public final class StratOrder implements Reusable<StratOrder> {

    private StratOrder      _next;
    private StratOrderState _state;
    private Side            _side;

    private int     _qty       = 0;
    private double  _price     = Constants.UNSET_DOUBLE;
    private int     _lastQty   = 0;
    private double  _lastPrice = Constants.UNSET_DOUBLE;
    private int     _cumQty    = 0;
    private double  _avePrice  = Constants.UNSET_DOUBLE;

    /**
     * identifies if this order has been placed in map 
     * if false then can optimise out map access
     */
    private boolean _inMap                    = false;
    private boolean _hasPerformedTerminalWork = false; // GUARD - used to avoid multiple terminal asjustments to positions  
    
    private final ReusableString _clOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
    private final ReusableString _orderId = new ReusableString( SizeType.ORDERID_LENGTH.getSize() );

    @Override
    public void reset() {
        _clOrdId.reset();
        _orderId.reset();

        _inMap                    = false;
        _hasPerformedTerminalWork = false;

        _next       = null;
        _state      = null;
        _side       = null;
        
        _qty        = 0;
        _price      = Constants.UNSET_DOUBLE;
        _lastQty    = 0;
        _lastPrice  = Constants.UNSET_DOUBLE;
        _cumQty     = 0;
        _avePrice   = Constants.UNSET_DOUBLE;
    }

    /**
     * @param clOrdId - client order id, must copy it not keep reference
     * @param side
     * @param qty
     * @param price
     */
    public void set( ZString clOrdId, Side side, int qty, double price ) {
        _side = side;
        _qty = qty;
        _price = price;
        _clOrdId.copy( clOrdId );
    }

    public void fill( int lastQty, double lastPx, int cumQty, double avePx ) {
        _lastQty    = lastQty;
        _lastPrice  = lastPx;
        _cumQty     = cumQty;
        _avePrice   = avePx;
    }
    
    @Override
    public StratOrder getNext() {
        return _next;
    }

    @Override
    public void setNext( StratOrder next ) {
        _next = next;
    }

    @Override
    public ReusableType getReusableType() {
        return StratReusableTypes.StratOrder;
    }

    public void setState( StratOrderState state ) {
        _state = state;
    }
    
    public StratOrderState getState() {
        return _state;
    }

    public Side getSide() {
        return _side;
    }

    public int getQty() {
        return _qty;
    }
    
    public double getPrice() {
        return _price;
    }

    public int getLastQty() {
        return _lastQty;
    }

    public double getLastPrice() {
        return _lastPrice;
    }

    public int getCumQty() {
        return _cumQty;
    }

    public double getAvePrice() {
        return _avePrice;
    }

    public ReusableString getClOrdId() {
        return _clOrdId;
    }

    public ReusableString getOrderId() {
        return _orderId;
    }
    
    public void setOrderId( ZString orderId ) {
        _orderId.copy( orderId );
    }

    public boolean isInMap() {
        return _inMap;
    }
    
    public void setInMap( boolean inMap ) {
        _inMap = inMap;
    }

    public boolean hasPerformedTerminalWork() {
        return _hasPerformedTerminalWork;
    }

    public void setHasPerformedTerminalWork( boolean hasPerformedTerminalWork ) {
        _hasPerformedTerminalWork = hasPerformedTerminalWork;
    }

    public void dump( ReusableString dest ) {
        dest.append( "stratOrder clOrdId=" ).append( _clOrdId ).
             append( ", state=" ).append( ((_state==null) ? "<null>" : StringUtils.className(_state.getClass()))).
             append( ", orderId=" ).append( _orderId ).
             append( ", qty=" ).append( _qty ).
             append( ", price=" ).append( _price ).
             append( ", lastQty=" ).append( _lastQty ).
             append( ", lastPx=" ).append( _lastPrice ).
             append( ", cumQty=" ).append( _cumQty ).
             append( ", avePx=" ).append( _avePrice ).
             append( ", inMap=" ).append( _inMap ).
             append( ", hasTerminated=" ).append( _hasPerformedTerminalWork );
        
    }
    
    @Override
    public String toString() {
        ReusableString s = TLC.instance().getString();
        dump( s );
        
        String ret = s.toString();
        
        TLC.instance().recycle( s );
        
        return ret;
    }
}
