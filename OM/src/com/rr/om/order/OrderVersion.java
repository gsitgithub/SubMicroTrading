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
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.model.generated.internal.events.interfaces.BaseOrderRequest;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.generated.internal.type.TypeIds;

/**
 * represents the order at a point in time
 * 
 * either pending an ack from exchange or last acked 
 * 
 *                   In an execution report the OrdStatus is used to convey the current state of the order. If an order simultaneously exists in more than one order state, the value
                  with highest precedence is the value that is reported in the OrdStatus field. The order statuses are as follows (in highest to lowest precedence):
               
                     11 - Pending Cancel
                     10 - Pending Replace
                      9 - Done for Day    (Order not, or partially, filled; no further executions forthcoming for the trading day)
                      8 - Calculated      (Order has been completed for the day (either filled or done for day). 
                      7 - Filled          (Order completely filled, no remaining quantity)
                      6 - Stopped         (Order has been stopped at the exchange. Used when guranteeing or protecting a price and quantity)
                      5 - Suspended       (Order has been placed in suspended state at the request of the client.)
                      4 - Canceled        (Canceled order with or without executions)
                      4 - Expired         (Order has been canceled in broker's system due to time in force instructions.)
                      3 - Partially Filled (Outstanding order with executions and remaining quantity)
                      2 - New              (Outstanding order with no executions)
                      2 - Rejected         (Order has been rejected by sell-side (broker, exchange, ECN). 
                                            NOTE: An order can be rejected subsequent to order acknowledgment,
                                            i.e. an order can pass from New to Rejected status.
                      2 - Pending New       (Order has been received by sell-side's (broker, exchange, ECN) system 
                                            but not yet accepted for execution. An execution message
                                            with this status will only be sent in response to a Status Request message.)
                      1 - Accepted for bidding  (Order has been received and is being evaluated for pricing. 
                                                It is anticipated that this status will only be used with the
                                                "Disclosed" BidType List Order Trading model.)
                                                
                                                
                                                Note in fix 4.4 ord status replaced is NOT used (4.2 encoder will need handle this)

 */
public class OrderVersion implements Reusable<OrderVersion> {

    /**
     * order request is the request associated with this version .. can be NOS, AMEND, CANC
     * 
     * common base class is missing many fields, for NOS/AMEND must cast appropriately
     */
    private BaseOrderRequest _orderRequest;
    
    private OrderVersion     _next;
    private OrdStatus        _status;
    private OrderCapacity    _marketCapacity;

    private double           _marketPrice;
    private double           _avgPx;
    private double           _totalTraded;
    private int              _cumQty;
    private int              _orderQty;

    private ZString          _marketClOrdId;
    private ReusableString   _marketOrderId = new ReusableString( SizeConstants.DEFAULT_MARKETORDERID_LENGTH );

    /**
     * @NOTE cast with CARE !!
     * 
     * @return
     */
    public final BaseOrderRequest getBaseOrderRequest() {
        return _orderRequest;
    }

    public final double getTotalTraded() {
        return _totalTraded;
    }

    public final int getOrderQty() {
        return _orderQty;
    }

    public final int getLeavesQty() {

        if ( _status.getIsTerminal() ) return 0;
        
        final int qty = _orderQty - _cumQty;
        
        return( (qty < 0) ? 0 : qty );
    }

    public final int getCumQty() {
        return _cumQty;
    }
    
    @Override
    public final void reset() {
        _marketOrderId.reset();
        _marketClOrdId  = null;
        _orderRequest   = null;
        _next           = null;
        _status         = null;
        _cumQty         = 0;
        _avgPx          = 0;
        _orderRequest   = null;
        _marketCapacity = null;
        _orderQty       = 0;
        _totalTraded    = 0.0;
    }

    @Override
    public final OrderVersion getNext() {
        return _next;
    }

    @Override
    public final void setNext( final OrderVersion nxt ) {
        _next = nxt;
    }

    @Override
    public final ReusableType getReusableType() {
        return OrderReusableType.OrderVersion;
    }

    public final void setBaseOrderRequest( final BaseOrderRequest event ) {
        _orderRequest = event;
    }

    public final void setOrdStatus( final OrdStatus status ) {
        _status = status;
    }
    
    public final OrdStatus getOrdStatus() {
        return _status;
    }

    public OrderCapacity getMarketCapacity() {
        return _marketCapacity;
    }

    public void setMarketCapacity( OrderCapacity marketCapacity ) {
        _marketCapacity = marketCapacity;
    }

    public double getMarketPrice() {
        return _marketPrice;
    }

    /**
     * @param mktPx the price for trading ie adjusted for major minor
     */
    public void setMarketPrice( double mktPx ) {
        _marketPrice = mktPx;
    }

    public double getAvgPx() {
        return _avgPx;
    }

    public void setAvgPx( double avgPx ) {
        _avgPx = avgPx;
    }

    public void setCumQty( int cumQty ) {
        _cumQty = cumQty;
    }
    
    public void setOrderQty( int qty ) {
        _orderQty = qty;
    }

    public ZString getMarketClOrdId() {
        return _marketClOrdId;
    }

    public void setMarketClOrdId( ZString marketClOrdId ) {
        _marketClOrdId = marketClOrdId;
    }

    public final void setMarketOrderId( final ZString marketOrderId ) {
        _marketOrderId.setValue( marketOrderId );
    }
    
    public final ZString getMarketOrderId() {
        return _marketOrderId;
    }

    public void applyFill( TradeNew msg ) {
        final int    qty   = msg.getLastQty();
        final double price = msg.getLastPx();
        
        _cumQty      += qty;
        _totalTraded += (qty * price);
        _avgPx        = _totalTraded / _cumQty;

        final int outstanding = _orderQty - _cumQty;
        
        if ( outstanding == 0 ) {
            // careful not to override order precedence
            switch( _status.getID() ) {
            case TypeIds.ORDSTATUS_NEW:
            case TypeIds.ORDSTATUS_PARTIALLYFILLED:
            case TypeIds.ORDSTATUS_FILLED:
            case TypeIds.ORDSTATUS_CANCELED:
            case TypeIds.ORDSTATUS_REPLACED:
            case TypeIds.ORDSTATUS_STOPPED:
            case TypeIds.ORDSTATUS_REJECTED:
            case TypeIds.ORDSTATUS_SUSPENDED:
            case TypeIds.ORDSTATUS_PENDINGNEW:
            case TypeIds.ORDSTATUS_EXPIRED:
            case TypeIds.ORDSTATUS_RESTATED:
            case TypeIds.ORDSTATUS_UNSEENORDER:
                setOrdStatus( OrdStatus.Filled );
                break;
            case TypeIds.ORDSTATUS_PENDINGCANCEL:
            case TypeIds.ORDSTATUS_PENDINGREPLACE:
            case TypeIds.ORDSTATUS_DONEFORDAY:
            case TypeIds.ORDSTATUS_CALCULATED:
                break;
            }
        } else {
            switch( _status.getID() ) {
            case TypeIds.ORDSTATUS_NEW:
            case TypeIds.ORDSTATUS_REJECTED:
            case TypeIds.ORDSTATUS_PENDINGNEW:
                setOrdStatus( OrdStatus.PartiallyFilled );
                break;
            case TypeIds.ORDSTATUS_PARTIALLYFILLED:
            case TypeIds.ORDSTATUS_FILLED:
            case TypeIds.ORDSTATUS_DONEFORDAY:
            case TypeIds.ORDSTATUS_CANCELED:
            case TypeIds.ORDSTATUS_REPLACED:
            case TypeIds.ORDSTATUS_PENDINGCANCEL:
            case TypeIds.ORDSTATUS_STOPPED:
            case TypeIds.ORDSTATUS_SUSPENDED:
            case TypeIds.ORDSTATUS_CALCULATED:
            case TypeIds.ORDSTATUS_EXPIRED:
            case TypeIds.ORDSTATUS_RESTATED:
            case TypeIds.ORDSTATUS_PENDINGREPLACE:
            case TypeIds.ORDSTATUS_UNSEENORDER:
                break;
            }
        }
    }

    public void setTotalTraded( double totalTraded ) {
        _totalTraded = totalTraded;
    }

    /**
     * invoked when a version becomes the new lastAccepted
     * inherits appropriate values from previous version
     * 
     * @param oldVer
     */
    public void inherit( OrderVersion oldVer ) {
        _avgPx       = oldVer._avgPx;
        _cumQty      = oldVer._cumQty;
        _totalTraded = oldVer._totalTraded;
    }

    public void applyTradeCancel( TradeCancel msg ) {
        final int    qty   = msg.getLastQty();
        final double price = msg.getLastPx();
        
        _cumQty      -= qty;
        _totalTraded -= (qty * price);
        
        if ( _cumQty < 0 )       _cumQty = 0;
        if ( _totalTraded  < 0 ) _totalTraded = 0;
        
        if ( _cumQty == 0 ) {
            _avgPx       = 0.0;
            _totalTraded = 0.0;
        } else {
            _avgPx       = _totalTraded / _cumQty;
        }
    }

    public void applyTradeCorrect( TradeCorrect msg ) {
        final int    repQty    = msg.getLastQty();                     
        final int    origQty   = msg.getOrigQty();                 
        final double repPx     = msg.getLastPx();                     
        final double origPx    = msg.getOrigPx();                 
        final int    qtyChange = repQty - origQty;
        
        _cumQty      += qtyChange;
        _totalTraded += ((repQty*repPx) - (origQty * origPx));
        
        if ( _cumQty < 0 )       _cumQty = 0;
        if ( _totalTraded  < 0 ) _totalTraded = 0;
        
        if ( _cumQty == 0 ) {
            _avgPx       = 0.0;
            _totalTraded = 0.0;
        } else {
            _avgPx       = _totalTraded / _cumQty;
        }
    }
}
