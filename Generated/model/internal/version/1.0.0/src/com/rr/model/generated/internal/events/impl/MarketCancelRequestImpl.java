/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.core.model.Instrument;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Currency;
import com.rr.core.model.SecurityIDSource;
import com.rr.model.generated.internal.type.Side;
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

public final class MarketCancelRequestImpl implements BaseOrderRequest, MarketCancelRequestUpdate, Reusable<MarketCancelRequestImpl> {

   // Attrs

    private          MarketCancelRequestImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private final ReusableString _clOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
    private final ReusableString _origClOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
    private final ReusableString _orderId = new ReusableString( SizeType.ORDERID_LENGTH.getSize() );
    private final ReusableString _srcLinkId = new ReusableString( SizeType.SRC_LINKID_LENGTH.getSize() );
    private int _sendingTime = Constants.UNSET_INT;
    private int _msgSeqNum = Constants.UNSET_INT;

    private Currency _currency;

    private OrderRequest  _srcEvent;
    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getAccount() { return _srcEvent.getAccount(); }


    @Override public final ViewString getClOrdId() { return _clOrdId; }

    @Override public final void setClOrdId( byte[] buf, int offset, int len ) { _clOrdId.setValue( buf, offset, len ); }
    @Override public final ReusableString getClOrdIdForUpdate() { return _clOrdId; }

    @Override public final ViewString getOrigClOrdId() { return _origClOrdId; }

    @Override public final void setOrigClOrdId( byte[] buf, int offset, int len ) { _origClOrdId.setValue( buf, offset, len ); }
    @Override public final ReusableString getOrigClOrdIdForUpdate() { return _origClOrdId; }

    @Override public final ViewString getOrderId() { return _orderId; }

    @Override public final void setOrderId( byte[] buf, int offset, int len ) { _orderId.setValue( buf, offset, len ); }
    @Override public final ReusableString getOrderIdForUpdate() { return _orderId; }

    @Override public final ViewString getSrcLinkId() { return _srcLinkId; }

    @Override public final void setSrcLinkId( byte[] buf, int offset, int len ) { _srcLinkId.setValue( buf, offset, len ); }
    @Override public final ReusableString getSrcLinkIdForUpdate() { return _srcLinkId; }

    @Override public final Instrument getInstrument() { return _srcEvent.getInstrument(); }

    @Override public final ClientProfile getClient() { return _srcEvent.getClient(); }

    @Override public final ViewString getSecurityId() { return _srcEvent.getSecurityId(); }


    @Override public final ViewString getSymbol() { return _srcEvent.getSymbol(); }


    @Override public final Currency getCurrency() { return _currency; }
    @Override public final void setCurrency( Currency val ) { _currency = val; }

    @Override public final SecurityIDSource getSecurityIDSource() { return _srcEvent.getSecurityIDSource(); }

    @Override public final int getTransactTime() { return _srcEvent.getTransactTime(); }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }

    @Override public final Side getSide() { return _srcEvent.getSide(); }

    @Override public final ViewString getOnBehalfOfId() { return _srcEvent.getOnBehalfOfId(); }


    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

    @Override public final void setSrcEvent( OrderRequest srcEvent ) { _srcEvent = srcEvent; }
    @Override public final OrderRequest getSrcEvent() { return _srcEvent; }


   // Reusable Contract

    @Override
    public final void reset() {
        _clOrdId.reset();
        _origClOrdId.reset();
        _orderId.reset();
        _srcLinkId.reset();
        _currency = null;
        _sendingTime = Constants.UNSET_INT;
        _msgSeqNum = Constants.UNSET_INT;
        _srcEvent = null;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.MarketCancelRequest;
    }

    @Override
    public final MarketCancelRequestImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( MarketCancelRequestImpl nxt ) {
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
        out.append( "MarketCancelRequestImpl" ).append( ' ' );
        out.append( ", account=" ).append( getAccount() );
        out.append( ", clOrdId=" ).append( getClOrdId() );
        out.append( ", origClOrdId=" ).append( getOrigClOrdId() );
        out.append( ", orderId=" ).append( getOrderId() );
        out.append( ", srcLinkId=" ).append( getSrcLinkId() );
        out.append( ", instrument=" );
        if ( getInstrument() != null ) getInstrument().id( out );
        out.append( ", client=" );
        if ( getClient() != null ) getClient().id( out );
        out.append( ", securityId=" ).append( getSecurityId() );
        out.append( ", symbol=" ).append( getSymbol() );
        out.append( ", currency=" );
        if ( getCurrency() != null ) getCurrency().id( out );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", transactTime=" ).append( getTransactTime() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
        out.append( ", side=" ).append( getSide() );
        out.append( ", onBehalfOfId=" ).append( getOnBehalfOfId() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
