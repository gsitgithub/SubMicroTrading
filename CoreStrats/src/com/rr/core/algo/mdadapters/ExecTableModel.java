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

import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Currency;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.TradeBase;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.internal.type.ExecType;

@SuppressWarnings( { "synthetic-access", "serial" } )

public class ExecTableModel extends AbstractTableModel implements MessageHandler {

    private enum OrderColumnNames {
        Symbol, ClOrdId, ExecType, ExecId, Side, Qty, Price, LastQty, LastPx, CumQty, AvePx, LeavesQty, Ccy, OrderId, OrdStatus
    }

    static final class MiniExec implements Comparable<MiniExec> {
        
        private final ExecType _execType;
        private final ReusableString  _clOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
        private final ReusableString  _symbol = new ReusableString( SizeType.SYMBOL_LENGTH.getSize() );

        private double _price;
        private int _orderQty;

        private Currency _currency;
        private Side _side;
        
        private final ReusableString  _orderId = new ReusableString( SizeType.ORDERID_LENGTH.getSize() );
        private final ReusableString  _execId = new ReusableString( SizeType.EXECID_LENGTH.getSize() );
        private OrdStatus _ordStatus;
        private double _lastPx;
        private int _lastQty;
        private double _avePx;
        private int _cumQty;
        private int _leavesQty;

        MiniExec( CommonExecRpt exec ) {
            _ordStatus = exec.getOrdStatus();
            _execType = exec.getExecType();
            _clOrdId.copy( exec.getClOrdId() );
            _symbol.copy( exec.getSymbol() );
            _execId.copy( exec.getExecId() );
            _orderId.copy( exec.getOrderId() );
            _price = Utils.nullCheck( exec.getPrice() );
            _orderQty = Utils.nullCheck( exec.getOrderQty() );
            _currency = exec.getCurrency();
            _side = exec.getSide();
            _avePx = Utils.nullCheck( exec.getAvgPx() );
            _cumQty = Utils.nullCheck( exec.getCumQty() );
            _leavesQty = exec.getLeavesQty();
            
            if ( exec instanceof TradeBase ) {
                TradeBase b = (TradeBase) exec;
                
                _lastQty = b.getLastQty();
                _lastPx = b.getLastPx();
            }

            if ( _leavesQty == Constants.UNSET_INT ) {
                _leavesQty = _orderQty - _cumQty;
            }
        }

        @Override
        public int compareTo( MiniExec o ) {
            return _clOrdId.compareTo( o._clOrdId );
        }

        public final ReusableString getClOrdId()        { return _clOrdId; }
        public final double getPrice()                  { return _price; }
        public final int getOrderQty()                  { return _orderQty; }
        public final Currency getCurrency()             { return _currency; }
        public final Side getSide()                     { return _side; }
        public final ReusableString getOrderId()        { return _orderId; }
        public final OrdStatus getOrdStatus()           { return _ordStatus; }
        public final double getAvePx()                  { return _avePx; }
        public final int getCumQty()                    { return _cumQty; }
        public final ExecType getExecType()             { return _execType; }
        public final ReusableString getSymbol()         { return _symbol; }
        public final ReusableString getExecId()         { return _execId; }
    }
    
    private final List<MiniExec> _execs = Collections.synchronizedList( new ArrayList<MiniExec>() );
    private final Map<String, MiniExec> _idToExec = new HashMap<String, MiniExec>();

    private final OrderColumnNames[] _cols      = OrderColumnNames.values();
    private final int           _numColumns = _cols.length;

    private final String _id;
    
    public ExecTableModel( String id ) {
        _id = id;
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
        if ( msg instanceof CommonExecRpt ) {
            CommonExecRpt req = (CommonExecRpt) msg;
            
            String key = makeKey(req);
            
            if ( ! _idToExec.containsKey( key ) ) {
                MiniExec r = new MiniExec( req );
                changeExecModel( key, r );
            }
        }
    }

    private String makeKey( CommonExecRpt req ) {
        return req.getClOrdId().toString() + req.getExecId().toString();
    }

    private void changeExecModel( String key, MiniExec r ) {
        _idToExec.put( key, r );
        _execs.add( 0, r );
        
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
        return _idToExec.size();
    }

    @Override
    public String getColumnName( int col ) {
        return _cols[col].name();
    }

    @SuppressWarnings( "boxing" )
    @Override
    public Object getValueAt( int row, int col ) {
        final MiniExec order = _execs.get( row );

        OrderColumnNames c = _cols[col];

        switch( c ) {
        case ClOrdId:
            return order._clOrdId;
        case OrdStatus:
            return order._ordStatus;
        case OrderId:
            return order._orderId;
        case Price:
            return order._price;
        case Qty:
            return order._orderQty;
        case Ccy:
            return ( order._currency == null ) ? "" : order._currency;
        case Side:
            return ( order._side == null ) ? "" : order._side;
        case AvePx:
            return order._avePx;
        case CumQty:
            return order._cumQty;
        case Symbol:
            return order._symbol;
        case ExecId:
            return order._execId;
        case ExecType:
            return order._execType;
        case LastPx:
            return order._lastPx;
        case LastQty:
            return order._lastQty;
        case LeavesQty:
            return order._leavesQty;
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

    public MiniExec getOrder( int row ) {
        return _execs.get( row );
    }
}

