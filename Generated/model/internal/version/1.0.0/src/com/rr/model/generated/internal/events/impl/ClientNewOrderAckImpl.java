/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

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

public final class ClientNewOrderAckImpl implements CommonExecRpt, ClientNewOrderAckUpdate, Reusable<ClientNewOrderAckImpl> {

   // Attrs

    private          ClientNewOrderAckImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private long _ackReceived = Constants.UNSET_LONG;
    private final ReusableString _execId = new ReusableString( SizeType.EXECID_LENGTH.getSize() );
    private final ReusableString _orderId = new ReusableString( SizeType.ORDERID_LENGTH.getSize() );
    private int _leavesQty = Constants.UNSET_INT;
    private int _cumQty = Constants.UNSET_INT;
    private double _avgPx = Constants.UNSET_DOUBLE;
    private int _msgSeqNum = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;

    private ExecType _execType;
    private OrdStatus _ordStatus;
    private OrderCapacity _mktCapacity;

    private OrderRequest  _srcEvent;
    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final long getAckReceived() { return _ackReceived; }
    @Override public final void setAckReceived( long val ) { _ackReceived = val; }

    @Override public final long getOrderReceived() { return _srcEvent.getOrderReceived(); }

    @Override public final long getOrderSent() { return _srcEvent.getOrderSent(); }

    @Override public final ViewString getExecId() { return _execId; }

    @Override public final void setExecId( byte[] buf, int offset, int len ) { _execId.setValue( buf, offset, len ); }
    @Override public final ReusableString getExecIdForUpdate() { return _execId; }

    @Override public final ViewString getClOrdId() { return _srcEvent.getClOrdId(); }


    @Override public final ViewString getSecurityId() { return _srcEvent.getSecurityId(); }


    @Override public final ViewString getSymbol() { return _srcEvent.getSymbol(); }


    @Override public final Currency getCurrency() { return _srcEvent.getCurrency(); }

    @Override public final SecurityIDSource getSecurityIDSource() { return _srcEvent.getSecurityIDSource(); }

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

    @Override public final int getOrderQty() { return _srcEvent.getOrderQty(); }

    @Override public final double getPrice() { return _srcEvent.getPrice(); }

    @Override public final Side getSide() { return _srcEvent.getSide(); }

    @Override public final OrderCapacity getMktCapacity() { return _mktCapacity; }
    @Override public final void setMktCapacity( OrderCapacity val ) { _mktCapacity = val; }

    @Override public final ViewString getOnBehalfOfId() { return _srcEvent.getOnBehalfOfId(); }


    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

    @Override public final void setSrcEvent( OrderRequest srcEvent ) { _srcEvent = srcEvent; }
    @Override public final OrderRequest getSrcEvent() { return _srcEvent; }


   // Reusable Contract

    @Override
    public final void reset() {
        _ackReceived = Constants.UNSET_LONG;
        _execId.reset();
        _orderId.reset();
        _execType = null;
        _ordStatus = null;
        _leavesQty = Constants.UNSET_INT;
        _cumQty = Constants.UNSET_INT;
        _avgPx = Constants.UNSET_DOUBLE;
        _mktCapacity = null;
        _msgSeqNum = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _srcEvent = null;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.ClientNewOrderAck;
    }

    @Override
    public final ClientNewOrderAckImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( ClientNewOrderAckImpl nxt ) {
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
        out.append( "ClientNewOrderAckImpl" ).append( ' ' );
        out.append( ", ackReceived=" ).append( getAckReceived() );
        out.append( ", orderReceived=" ).append( getOrderReceived() );
        out.append( ", orderSent=" ).append( getOrderSent() );
        out.append( ", execId=" ).append( getExecId() );
        out.append( ", clOrdId=" ).append( getClOrdId() );
        out.append( ", securityId=" ).append( getSecurityId() );
        out.append( ", symbol=" ).append( getSymbol() );
        out.append( ", currency=" );
        if ( getCurrency() != null ) getCurrency().id( out );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", orderId=" ).append( getOrderId() );
        out.append( ", execType=" );
        if ( getExecType() != null ) getExecType().id( out );
        out.append( ", ordStatus=" ).append( getOrdStatus() );
        out.append( ", leavesQty=" ).append( getLeavesQty() );
        out.append( ", cumQty=" ).append( getCumQty() );
        out.append( ", avgPx=" ).append( getAvgPx() );
        out.append( ", orderQty=" ).append( getOrderQty() );
        out.append( ", price=" ).append( getPrice() );
        out.append( ", side=" ).append( getSide() );
        out.append( ", mktCapacity=" ).append( getMktCapacity() );
        out.append( ", onBehalfOfId=" ).append( getOnBehalfOfId() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
    }

}
