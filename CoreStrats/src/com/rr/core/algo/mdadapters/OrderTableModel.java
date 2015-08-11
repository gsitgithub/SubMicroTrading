/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mdadapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Currency;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;

@SuppressWarnings( { "synthetic-access", "serial" } )

public class OrderTableModel extends AbstractTableModel implements MessageHandler {

    private enum OrderColumnNames {
        Symbol, SecurityId, ReqType, ClOrdId, Side, Price, Qty, OrdType, TIF, Ccy, OrderId, CumQty, AvePx, OrdStatus
    }

    private enum RequestType { New, Amend, Cancel }
    
    static final class MiniOrderRequest implements Comparable<MiniOrderRequest> {
        
        private final RequestType _reqType;
        private final ReusableString  _clOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
        private final Instrument _instrument;

        private double _price;
        private int _orderQty;

        private OrdType _ordType;
        private TimeInForce _timeInForce;
        private Currency _currency;
        private Side _side;
        
        private final ReusableString  _orderId = new ReusableString( SizeType.ORDERID_LENGTH.getSize() );
        private OrdStatus _ordStatus;
        private double _avePx;
        private int _cumQty;

        MiniOrderRequest( NewOrderSingle nos ) {
            _ordStatus = OrdStatus.PendingNew;
            _reqType = RequestType.New;
            _clOrdId.copy( nos.getClOrdId() );
            _price = Utils.nullCheck( nos.getPrice() );
            _orderQty = Utils.nullCheck( nos.getOrderQty() );
            _ordType = nos.getOrdType();
            _timeInForce = nos.getTimeInForce();
            _instrument = nos.getInstrument();
            _currency = nos.getCurrency();
            _side = nos.getSide();
        }

        MiniOrderRequest( CancelReplaceRequest amend ) {
            _reqType = RequestType.Amend;
            _ordStatus = OrdStatus.PendingReplace;
            _clOrdId.copy( amend.getClOrdId() );
            _orderId.copy( amend.getOrderId() );
            _price = Utils.nullCheck( amend.getPrice() );
            _orderQty = Utils.nullCheck( amend.getOrderQty() );
            _ordType = amend.getOrdType();
            _timeInForce = amend.getTimeInForce();
            _instrument = amend.getInstrument();
            _currency = amend.getCurrency();
            _side = amend.getSide();
        }

        MiniOrderRequest( CancelRequest cxl ) {
            _ordStatus = OrdStatus.PendingCancel;
            _reqType = RequestType.Cancel;
            _clOrdId.copy( cxl.getClOrdId() );
            _orderId.copy( cxl.getOrderId() );
            _instrument = cxl.getInstrument();
        }

        @Override
        public int compareTo( MiniOrderRequest o ) {
            return _clOrdId.compareTo( o._clOrdId );
        }

        public final RequestType getReqType()           { return _reqType; }
        public final ReusableString getClOrdId()        { return _clOrdId; }
        public final Instrument getInstrument()         { return _instrument; }
        public final double getPrice()                  { return _price; }
        public final int getOrderQty()                  { return _orderQty; }
        public final OrdType getOrdType()               { return _ordType; }
        public final TimeInForce getTimeInForce()       { return _timeInForce; }
        public final Currency getCurrency()             { return _currency; }
        public final Side getSide()                     { return _side; }
        public final ReusableString getOrderId()        { return _orderId; }
        public final OrdStatus getOrdStatus()           { return _ordStatus; }
        public final double getAvePx()                  { return _avePx; }
        public final int getCumQty()                    { return _cumQty; }
    }
    
    private final List<MiniOrderRequest> _orders = Collections.synchronizedList( new ArrayList<MiniOrderRequest>() );
    private final Map<ZString, MiniOrderRequest> _clOrdId2Request = Collections.synchronizedMap( new HashMap<ZString, MiniOrderRequest>() );

    private final OrderColumnNames[] _cols      = OrderColumnNames.values();
    private final int           _numColumns = _cols.length;

    private final String _id;
    private final ExecTableModel _execModel;
    
    public OrderTableModel( String id, ExecTableModel execModel ) {
        _id = id;
        _execModel = execModel;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void threadedInit() {
        // nothing
    }

    @Override
    public void handle( Message msg ) {
        handleNow( msg );
    }

    @Override
    public void handleNow( Message msg ) {
        if ( msg instanceof NewOrderSingle ) {
            NewOrderSingle req = (NewOrderSingle) msg;
            
            if ( ! _clOrdId2Request.containsKey( req.getClOrdId() ) ) {
                MiniOrderRequest r = new MiniOrderRequest( req );
                changeOrderModel( r );
            }
        } else if ( msg instanceof CancelReplaceRequest ) {
            CancelReplaceRequest req = (CancelReplaceRequest) msg;
            
            if ( ! _clOrdId2Request.containsKey( req.getClOrdId() ) ) {
                MiniOrderRequest r = new MiniOrderRequest( req );
                changeOrderModel( r );

                MiniOrderRequest rOrig = _clOrdId2Request.get( req.getOrigClOrdId() ); 
                
                if ( rOrig != null ) {
                    ReusableString clOrigOrdId = rOrig._clOrdId;

                    r._clOrdId.setNext( clOrigOrdId );
                }
            }
        } else if ( msg instanceof CancelRequest ) {
            CancelRequest req = (CancelRequest) msg;
            
            if ( ! _clOrdId2Request.containsKey( req.getClOrdId() ) ) {
                MiniOrderRequest r = new MiniOrderRequest( req );
                changeOrderModel( r );

                MiniOrderRequest rOrig = _clOrdId2Request.get( req.getOrigClOrdId() ); 
                
                if ( rOrig != null ) {
                    ReusableString clOrigOrdId = rOrig._clOrdId;

                    r._clOrdId.setNext( clOrigOrdId );
                }
            }
        } else if ( msg instanceof CommonExecRpt ) {

            updateOrder( (CommonExecRpt)msg );
            
            _execModel.handle( msg );
        }
    }

    private void updateOrder( CommonExecRpt rpt ) {
        ZString clOrdId = rpt.getClOrdId();
        ZString orderId = rpt.getOrderId();
        
        MiniOrderRequest ord = null;
        
        if ( _clOrdId2Request.containsKey( clOrdId ) ) {
            ord = _clOrdId2Request.get( clOrdId );
            if ( ! _clOrdId2Request.containsKey( orderId ) ) { // ACK or REJECT
                ord._orderId.copy( orderId );
                
                _clOrdId2Request.put( ord._orderId, ord );
            }

            updateOrderFromExec( ord, rpt );
        } else { // dont have clOrdId
            if ( _clOrdId2Request.containsKey( orderId ) ) { 
                ord = _clOrdId2Request.get( clOrdId );
                
                updateOrderFromExec( ord, rpt );
            }
        }
        
        if ( ord != null ) {
            int row = _orders.indexOf( ord );
            
            if ( row > -1 ) {
                fireTableRowsUpdated( row, row );
            }
        }
    }

    private synchronized void updateOrderFromExec( MiniOrderRequest ord, CommonExecRpt rpt ) {
        ord._ordStatus = rpt.getOrdStatus();
        ord._avePx = Utils.nullCheck( rpt.getAvgPx() );
        ord._cumQty = Utils.nullCheck( rpt.getCumQty() );
    }

    private void changeOrderModel( MiniOrderRequest r ) {
        _clOrdId2Request.put( r._clOrdId, r );
        _orders.add( 0, r );
        
        fireTableRowsInserted( 0, 0 );
    }

    @Override
    public boolean canHandle() {
        return true;
    }

    @Override
    public int getColumnCount() {
        return _numColumns;
    }

    @Override
    public int getRowCount() {
        return _orders.size();
    }

    @Override
    public String getColumnName( int col ) {
        return _cols[col].name();
    }

    @SuppressWarnings( "boxing" )
    @Override
    public Object getValueAt( int row, int col ) {
        final MiniOrderRequest order = _orders.get( row );

        OrderColumnNames c = _cols[col];

        switch( c ) {
        case ClOrdId:
            return order._clOrdId;
        case OrdStatus:
            return order._ordStatus;
        case OrdType:
            return ( order._ordType == null ) ? "" : order._ordType;
        case OrderId:
            return order._orderId;
        case Price:
            return order._price;
        case Qty:
            return order._orderQty;
        case ReqType:
            return order._reqType;
        case SecurityId:
            return order._instrument.getLongSymbol();
        case Ccy:
            return ( order._currency == null ) ? "" : order._currency;
        case Side:
            return ( order._side == null ) ? "" : order._side;
        case AvePx:
            return order._avePx;
        case CumQty:
            return order._cumQty;
        case Symbol:
            return order._instrument.getSecurityDesc();
        case TIF:
            return ( order._timeInForce == null ) ? "" : order._timeInForce;
        default:
            break;
        }

        return null;
    }

    @Override
    public Class<?> getColumnClass( int c ) {
        return getValueAt( 0, c ).getClass();
    }

    @Override
    public boolean isCellEditable( int row, int col ) {
        return false;
    }

    public MiniOrderRequest getOrder( int row ) {
        return _orders.get( row );
    }
}

