/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.ExecInst;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.core.model.SecurityType;
import com.rr.core.model.SecurityIDSource;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.model.generated.internal.type.BookingType;
import com.rr.core.model.Instrument;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Currency;
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

public final class ClientCancelReplaceRequestImpl implements OrderRequest, ClientCancelReplaceRequestWrite, Reusable<ClientCancelReplaceRequestImpl> {

   // Attrs

    private final ReusableString _buf = new ReusableString( SizeType.VIEW_NOS_BUFFER.getSize() );
    private          ClientCancelReplaceRequestImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private final ReusableString _clOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
    private final ReusableString _origClOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
    private final ViewString _exDest = new ViewString( _buf );
    private final ViewString _securityExchange = new ViewString( _buf );
    private final ReusableString _orderId = new ReusableString( SizeType.ORDERID_LENGTH.getSize() );
    private final ViewString _account = new ViewString( _buf );
    private final ViewString _text = new ViewString( _buf );
    private double _price = Constants.UNSET_DOUBLE;
    private int _orderQty = Constants.UNSET_INT;
    private long _orderReceived = Constants.UNSET_LONG;
    private long _orderSent = Constants.UNSET_LONG;
    private final ViewString _securityId = new ViewString( _buf );
    private final ViewString _symbol = new ViewString( _buf );
    private int _transactTime = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;
    private final ViewString _srcLinkId = new ViewString( _buf );
    private final ViewString _onBehalfOfId = new ViewString( _buf );
    private int _msgSeqNum = Constants.UNSET_INT;

    private ExecInst _execInst;
    private HandlInst _handlInst;
    private OrderCapacity _orderCapacity;
    private OrdType _ordType;
    private SecurityType _securityType;
    private SecurityIDSource _securityIDSource;
    private TimeInForce _timeInForce;
    private BookingType _bookingType;
    private Instrument _instrument;
    private ClientProfile _client;
    private Currency _currency;
    private Side _side;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getClOrdId() { return _clOrdId; }

    @Override public final void setClOrdId( byte[] buf, int offset, int len ) { _clOrdId.setValue( buf, offset, len ); }
    @Override public final ReusableString getClOrdIdForUpdate() { return _clOrdId; }

    @Override public final ViewString getOrigClOrdId() { return _origClOrdId; }

    @Override public final void setOrigClOrdId( byte[] buf, int offset, int len ) { _origClOrdId.setValue( buf, offset, len ); }
    @Override public final ReusableString getOrigClOrdIdForUpdate() { return _origClOrdId; }

    @Override public final ViewString getExDest() { return _exDest; }

              public final void setExDest( int offset, int len ) { _exDest.setValue( offset, len ); }
    @Override public final AssignableString getExDestForUpdate() { return _exDest; }

    @Override public final ViewString getSecurityExchange() { return _securityExchange; }

              public final void setSecurityExchange( int offset, int len ) { _securityExchange.setValue( offset, len ); }
    @Override public final AssignableString getSecurityExchangeForUpdate() { return _securityExchange; }

    @Override public final ViewString getOrderId() { return _orderId; }

    @Override public final void setOrderId( byte[] buf, int offset, int len ) { _orderId.setValue( buf, offset, len ); }
    @Override public final ReusableString getOrderIdForUpdate() { return _orderId; }

    @Override public final ViewString getAccount() { return _account; }

              public final void setAccount( int offset, int len ) { _account.setValue( offset, len ); }
    @Override public final AssignableString getAccountForUpdate() { return _account; }

    @Override public final ViewString getText() { return _text; }

              public final void setText( int offset, int len ) { _text.setValue( offset, len ); }
    @Override public final AssignableString getTextForUpdate() { return _text; }

    @Override public final double getPrice() { return _price; }
    @Override public final void setPrice( double val ) { _price = val; }

    @Override public final int getOrderQty() { return _orderQty; }
    @Override public final void setOrderQty( int val ) { _orderQty = val; }

    @Override public final ExecInst getExecInst() { return _execInst; }
    @Override public final void setExecInst( ExecInst val ) { _execInst = val; }

    @Override public final HandlInst getHandlInst() { return _handlInst; }
    @Override public final void setHandlInst( HandlInst val ) { _handlInst = val; }

    @Override public final OrderCapacity getOrderCapacity() { return _orderCapacity; }
    @Override public final void setOrderCapacity( OrderCapacity val ) { _orderCapacity = val; }

    @Override public final OrdType getOrdType() { return _ordType; }
    @Override public final void setOrdType( OrdType val ) { _ordType = val; }

    @Override public final SecurityType getSecurityType() { return _securityType; }
    @Override public final void setSecurityType( SecurityType val ) { _securityType = val; }

    @Override public final SecurityIDSource getSecurityIDSource() { return _securityIDSource; }
    @Override public final void setSecurityIDSource( SecurityIDSource val ) { _securityIDSource = val; }

    @Override public final TimeInForce getTimeInForce() { return _timeInForce; }
    @Override public final void setTimeInForce( TimeInForce val ) { _timeInForce = val; }

    @Override public final BookingType getBookingType() { return _bookingType; }
    @Override public final void setBookingType( BookingType val ) { _bookingType = val; }

    @Override public final long getOrderReceived() { return _orderReceived; }
    @Override public final void setOrderReceived( long val ) { _orderReceived = val; }

    @Override public final long getOrderSent() { return _orderSent; }
    @Override public final void setOrderSent( long val ) { _orderSent = val; }

    @Override public final Instrument getInstrument() { return _instrument; }
    @Override public final void setInstrument( Instrument val ) { _instrument = val; }

    @Override public final ClientProfile getClient() { return _client; }
    @Override public final void setClient( ClientProfile val ) { _client = val; }

    @Override public final ViewString getSecurityId() { return _securityId; }

              public final void setSecurityId( int offset, int len ) { _securityId.setValue( offset, len ); }
    @Override public final AssignableString getSecurityIdForUpdate() { return _securityId; }

    @Override public final ViewString getSymbol() { return _symbol; }

              public final void setSymbol( int offset, int len ) { _symbol.setValue( offset, len ); }
    @Override public final AssignableString getSymbolForUpdate() { return _symbol; }

    @Override public final Currency getCurrency() { return _currency; }
    @Override public final void setCurrency( Currency val ) { _currency = val; }

    @Override public final int getTransactTime() { return _transactTime; }
    @Override public final void setTransactTime( int val ) { _transactTime = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }

    @Override public final Side getSide() { return _side; }
    @Override public final void setSide( Side val ) { _side = val; }

    @Override public final ViewString getSrcLinkId() { return _srcLinkId; }

              public final void setSrcLinkId( int offset, int len ) { _srcLinkId.setValue( offset, len ); }
    @Override public final AssignableString getSrcLinkIdForUpdate() { return _srcLinkId; }

    @Override public final ViewString getOnBehalfOfId() { return _onBehalfOfId; }

              public final void setOnBehalfOfId( int offset, int len ) { _onBehalfOfId.setValue( offset, len ); }
    @Override public final AssignableString getOnBehalfOfIdForUpdate() { return _onBehalfOfId; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }
    public final ViewString getViewBuf() { return _buf; }
    public final void setViewBuf( byte[] buf, int offset, int len ) { 
        _buf.setValue( buf, offset, len );
    }

   // Reusable Contract

    @Override
    public final void reset() {
        _clOrdId.reset();
        _origClOrdId.reset();
        _exDest.reset();
        _securityExchange.reset();
        _orderId.reset();
        _account.reset();
        _text.reset();
        _price = Constants.UNSET_DOUBLE;
        _orderQty = Constants.UNSET_INT;
        _execInst = null;
        _handlInst = null;
        _orderCapacity = null;
        _ordType = null;
        _securityType = null;
        _securityIDSource = null;
        _timeInForce = null;
        _bookingType = null;
        _orderReceived = Constants.UNSET_LONG;
        _orderSent = Constants.UNSET_LONG;
        _instrument = null;
        _client = null;
        _securityId.reset();
        _symbol.reset();
        _currency = null;
        _transactTime = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _side = null;
        _srcLinkId.reset();
        _onBehalfOfId.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.ClientCancelReplaceRequest;
    }

    @Override
    public final ClientCancelReplaceRequestImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( ClientCancelReplaceRequestImpl nxt ) {
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
        out.append( "ClientCancelReplaceRequestImpl" ).append( ' ' );
        out.append( ", clOrdId=" ).append( getClOrdId() );
        out.append( ", origClOrdId=" ).append( getOrigClOrdId() );
        out.append( ", exDest=" ).append( getExDest() );
        out.append( ", securityExchange=" ).append( getSecurityExchange() );
        out.append( ", orderId=" ).append( getOrderId() );
        out.append( ", account=" ).append( getAccount() );
        out.append( ", text=" ).append( getText() );
        out.append( ", price=" ).append( getPrice() );
        out.append( ", orderQty=" ).append( getOrderQty() );
        out.append( ", execInst=" ).append( getExecInst() );
        out.append( ", handlInst=" ).append( getHandlInst() );
        out.append( ", orderCapacity=" ).append( getOrderCapacity() );
        out.append( ", ordType=" ).append( getOrdType() );
        out.append( ", securityType=" );
        if ( getSecurityType() != null ) getSecurityType().id( out );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", timeInForce=" ).append( getTimeInForce() );
        out.append( ", bookingType=" ).append( getBookingType() );
        out.append( ", orderReceived=" ).append( getOrderReceived() );
        out.append( ", orderSent=" ).append( getOrderSent() );
        out.append( ", instrument=" );
        if ( getInstrument() != null ) getInstrument().id( out );
        out.append( ", client=" );
        if ( getClient() != null ) getClient().id( out );
        out.append( ", securityId=" ).append( getSecurityId() );
        out.append( ", symbol=" ).append( getSymbol() );
        out.append( ", currency=" );
        if ( getCurrency() != null ) getCurrency().id( out );
        out.append( ", transactTime=" ).append( getTransactTime() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
        out.append( ", side=" ).append( getSide() );
        out.append( ", srcLinkId=" ).append( getSrcLinkId() );
        out.append( ", onBehalfOfId=" ).append( getOnBehalfOfId() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
