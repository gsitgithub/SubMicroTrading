/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.LiquidityInd;
import com.rr.model.generated.internal.type.MultiLegReportingType;
import com.rr.core.model.Currency;
import com.rr.core.model.SecurityIDSource;
import com.rr.model.internal.type.ExecType;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.AssignableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.Constants;
import com.rr.core.model.MsgFlag;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.Reusable;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.model.internal.type.*;
import com.rr.model.generated.internal.core.ModelReusableTypes;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.*;

@SuppressWarnings( "unused" )

public final class MarketTradeCancelImpl implements TradeBase, MarketTradeCancelWrite, Reusable<MarketTradeCancelImpl> {

   // Attrs

    private          MarketTradeCancelImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private final ReusableString _execRefID = new ReusableString( SizeType.EXECID_LENGTH.getSize() );
    private int _lastQty = Constants.UNSET_INT;
    private double _lastPx = Constants.UNSET_DOUBLE;
    private final ReusableString _lastMkt = new ReusableString( SizeType.LASTMKT_LENGTH.getSize() );
    private final ReusableString _text = new ReusableString( SizeType.TRADE_TEXT_LENGTH.getSize() );
    private final ReusableString _securityDesc = new ReusableString( SizeType.INST_SEC_DESC_LENGTH.getSize() );
    private final ReusableString _execId = new ReusableString( SizeType.EXECID_LENGTH.getSize() );
    private final ReusableString _clOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
    private final ReusableString _orderId = new ReusableString( SizeType.ORDERID_LENGTH.getSize() );
    private int _leavesQty = Constants.UNSET_INT;
    private int _cumQty = Constants.UNSET_INT;
    private double _avgPx = Constants.UNSET_DOUBLE;
    private int _msgSeqNum = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;

    private LiquidityInd _liquidityInd;
    private MultiLegReportingType _multiLegReportingType;
    private ExecType _execType;
    private OrdStatus _ordStatus;
    private Side _side;
    private OrderCapacity _mktCapacity;

    private OrderRequest  _srcEvent;
    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getExecRefID() { return _execRefID; }

    @Override public final void setExecRefID( byte[] buf, int offset, int len ) { _execRefID.setValue( buf, offset, len ); }
    @Override public final ReusableString getExecRefIDForUpdate() { return _execRefID; }

    @Override public final int getLastQty() { return _lastQty; }
    @Override public final void setLastQty( int val ) { _lastQty = val; }

    @Override public final double getLastPx() { return _lastPx; }
    @Override public final void setLastPx( double val ) { _lastPx = val; }

    @Override public final LiquidityInd getLiquidityInd() { return _liquidityInd; }
    @Override public final void setLiquidityInd( LiquidityInd val ) { _liquidityInd = val; }

    @Override public final MultiLegReportingType getMultiLegReportingType() { return _multiLegReportingType; }
    @Override public final void setMultiLegReportingType( MultiLegReportingType val ) { _multiLegReportingType = val; }

    @Override public final ViewString getLastMkt() { return _lastMkt; }

    @Override public final void setLastMkt( byte[] buf, int offset, int len ) { _lastMkt.setValue( buf, offset, len ); }
    @Override public final ReusableString getLastMktForUpdate() { return _lastMkt; }

    @Override public final ViewString getText() { return _text; }

    @Override public final void setText( byte[] buf, int offset, int len ) { _text.setValue( buf, offset, len ); }
    @Override public final ReusableString getTextForUpdate() { return _text; }

    @Override public final ViewString getSecurityDesc() { return _securityDesc; }

    @Override public final void setSecurityDesc( byte[] buf, int offset, int len ) { _securityDesc.setValue( buf, offset, len ); }
    @Override public final ReusableString getSecurityDescForUpdate() { return _securityDesc; }

    @Override public final ViewString getExecId() { return _execId; }

    @Override public final void setExecId( byte[] buf, int offset, int len ) { _execId.setValue( buf, offset, len ); }
    @Override public final ReusableString getExecIdForUpdate() { return _execId; }

    @Override public final ViewString getClOrdId() { return _clOrdId; }

    @Override public final void setClOrdId( byte[] buf, int offset, int len ) { _clOrdId.setValue( buf, offset, len ); }
    @Override public final ReusableString getClOrdIdForUpdate() { return _clOrdId; }

    @Override public ViewString getSecurityId() { throw new IllegalFieldAccess( "Getter for securityId event TradeCancel is a delegate field from order request base" ); }


    @Override public ViewString getSymbol() { throw new IllegalFieldAccess( "Getter for symbol event TradeCancel is a delegate field from order request base" ); }


    @Override public Currency getCurrency() { throw new IllegalFieldAccess( "Getter for currency event TradeCancel is a delegate field from order request base" ); }


    @Override public SecurityIDSource getSecurityIDSource() { throw new IllegalFieldAccess( "Getter for securityIDSource event TradeCancel is a delegate field from order request base" ); }


    @Override public final ViewString getOrderId() { return _orderId; }

    @Override public final void setOrderId( byte[] buf, int offset, int len ) { _orderId.setValue( buf, offset, len ); }
    @Override public final ReusableString getOrderIdForUpdate() { return _orderId; }

    @Override public final ExecType getExecType() { return _execType; }
    @Override public final void setExecType( ExecType val ) { _execType = val; }

    @Override public final OrdStatus getOrdStatus() { return _ordStatus; }
    @Override public final void setOrdStatus( OrdStatus val ) { _ordStatus = val; }

    @Override public final int getLeavesQty() { return _leavesQty; }
    @Override public final void setLeavesQty( int val ) { _leavesQty = val; }

    @Override public final int getCumQty() { return _cumQty; }
    @Override public final void setCumQty( int val ) { _cumQty = val; }

    @Override public final double getAvgPx() { return _avgPx; }
    @Override public final void setAvgPx( double val ) { _avgPx = val; }

    @Override public int getOrderQty() { throw new IllegalFieldAccess( "Getter for orderQty event TradeCancel is a delegate field from order request base" ); }


    @Override public double getPrice() { throw new IllegalFieldAccess( "Getter for price event TradeCancel is a delegate field from order request base" ); }


    @Override public final Side getSide() { return _side; }
    @Override public final void setSide( Side val ) { _side = val; }

    @Override public final OrderCapacity getMktCapacity() { return _mktCapacity; }
    @Override public final void setMktCapacity( OrderCapacity val ) { _mktCapacity = val; }

    @Override public ViewString getOnBehalfOfId() { throw new IllegalFieldAccess( "Getter for onBehalfOfId event TradeCancel is a delegate field from order request base" ); }


    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _execRefID.reset();
        _lastQty = Constants.UNSET_INT;
        _lastPx = Constants.UNSET_DOUBLE;
        _liquidityInd = null;
        _multiLegReportingType = null;
        _lastMkt.reset();
        _text.reset();
        _securityDesc.reset();
        _execId.reset();
        _clOrdId.reset();
        _orderId.reset();
        _execType = null;
        _ordStatus = null;
        _leavesQty = Constants.UNSET_INT;
        _cumQty = Constants.UNSET_INT;
        _avgPx = Constants.UNSET_DOUBLE;
        _side = null;
        _mktCapacity = null;
        _msgSeqNum = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.MarketTradeCancel;
    }

    @Override
    public final MarketTradeCancelImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( MarketTradeCancelImpl nxt ) {
        _next = nxt;
    }

    @Override
    public final void detachQueue() {
        _nextMessage = null;
    }

    @Override
    public final Message getNextQueueEntry() {
        return _nextMessage;
    }

    @Override
    public final void attachQueue( Message nxt ) {
        _nextMessage = nxt;
    }

    @Override
    public final MessageHandler getMessageHandler() {
        return _messageHandler;
    }

    @Override
    public final void setMessageHandler( MessageHandler handler ) {
        _messageHandler = handler;
    }


   // Helper methods
    @Override
    public void setFlag( MsgFlag flag, boolean isOn ) {
        _flags = (byte) MsgFlag.setFlag( _flags, flag, isOn );
    }

    @Override
    public boolean isFlagSet( MsgFlag flag ) {
        return MsgFlag.isOn( _flags, flag );
    }

    @Override
    public String toString() {
        ReusableString buf = new ReusableString();
        dump( buf );
        return buf.toString();
    }

    @Override
    public final void dump( ReusableString out ) {
        out.append( "MarketTradeCancelImpl" ).append( ' ' );
        out.append( ", execRefID=" ).append( getExecRefID() );
        out.append( ", lastQty=" ).append( getLastQty() );
        out.append( ", lastPx=" ).append( getLastPx() );
        out.append( ", liquidityInd=" ).append( getLiquidityInd() );
        out.append( ", multiLegReportingType=" ).append( getMultiLegReportingType() );
        out.append( ", lastMkt=" ).append( getLastMkt() );
        out.append( ", text=" ).append( getText() );
        out.append( ", securityDesc=" ).append( getSecurityDesc() );
        out.append( ", execId=" ).append( getExecId() );
        out.append( ", clOrdId=" ).append( getClOrdId() );
        out.append( ", orderId=" ).append( getOrderId() );
        out.append( ", execType=" );
        if ( getExecType() != null ) getExecType().id( out );
        out.append( ", ordStatus=" ).append( getOrdStatus() );
        out.append( ", leavesQty=" ).append( getLeavesQty() );
        out.append( ", cumQty=" ).append( getCumQty() );
        out.append( ", avgPx=" ).append( getAvgPx() );
        out.append( ", side=" ).append( getSide() );
        out.append( ", mktCapacity=" ).append( getMktCapacity() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
    }

}
