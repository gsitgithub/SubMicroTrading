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
import com.rr.model.generated.internal.type.Side;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
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

public final class RecoveryAlertLimitBreachImpl implements Alert, MarketAlertLimitBreachWrite, Reusable<RecoveryAlertLimitBreachImpl> {

   // Attrs

    private          RecoveryAlertLimitBreachImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private final ReusableString  _clOrdId = new ReusableString( SizeType.CLORDID_LENGTH.getSize() );
    private final ReusableString  _securityId = new ReusableString( SizeType.SECURITYID_LENGTH.getSize() );
    private final ReusableString  _symbol = new ReusableString( SizeType.SYMBOL_LENGTH.getSize() );
    private final ReusableString _text = new ReusableString( SizeType.TEXT_LENGTH.getSize() );
    private int _orderQty = Constants.UNSET_INT;
    private double _price = Constants.UNSET_DOUBLE;
    private final ReusableString  _onBehalfOfId = new ReusableString( SizeType.COMPID_LENGTH.getSize() );
    private int _msgSeqNum = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;

    private Currency _currency;
    private SecurityIDSource _securityIDSource;
    private Side _side;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getClOrdId() { return _clOrdId; }

    public final void setClOrdId( byte[] buf, int offset, int len ) { _clOrdId.setValue( buf, offset, len ); }
    public final ReusableString getClOrdIdForUpdate() { return _clOrdId; }

    @Override public final ViewString getSecurityId() { return _securityId; }

    public final void setSecurityId( byte[] buf, int offset, int len ) { _securityId.setValue( buf, offset, len ); }
    public final ReusableString getSecurityIdForUpdate() { return _securityId; }

    @Override public final ViewString getSymbol() { return _symbol; }

    public final void setSymbol( byte[] buf, int offset, int len ) { _symbol.setValue( buf, offset, len ); }
    public final ReusableString getSymbolForUpdate() { return _symbol; }

    @Override public final Currency getCurrency() { return _currency; }
    public final void setCurrency( Currency val ) { _currency = val; }

    @Override public final SecurityIDSource getSecurityIDSource() { return _securityIDSource; }
    public final void setSecurityIDSource( SecurityIDSource val ) { _securityIDSource = val; }

    @Override public final ViewString getText() { return _text; }

    public final void setText( byte[] buf, int offset, int len ) { _text.setValue( buf, offset, len ); }
    public final ReusableString getTextForUpdate() { return _text; }

    @Override public final int getOrderQty() { return _orderQty; }
    public final void setOrderQty( int val ) { _orderQty = val; }

    @Override public final double getPrice() { return _price; }
    public final void setPrice( double val ) { _price = val; }

    @Override public final Side getSide() { return _side; }
    public final void setSide( Side val ) { _side = val; }

    @Override public final ViewString getOnBehalfOfId() { return _onBehalfOfId; }

    public final void setOnBehalfOfId( byte[] buf, int offset, int len ) { _onBehalfOfId.setValue( buf, offset, len ); }
    public final ReusableString getOnBehalfOfIdForUpdate() { return _onBehalfOfId; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    public final void setSendingTime( int val ) { _sendingTime = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _clOrdId.reset();
        _securityId.reset();
        _symbol.reset();
        _currency = null;
        _securityIDSource = null;
        _text.reset();
        _orderQty = Constants.UNSET_INT;
        _price = Constants.UNSET_DOUBLE;
        _side = null;
        _onBehalfOfId.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.RecoveryAlertLimitBreach;
    }

    @Override
    public final RecoveryAlertLimitBreachImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( RecoveryAlertLimitBreachImpl nxt ) {
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
        out.append( "RecoveryAlertLimitBreachImpl" ).append( ' ' );
        out.append( ", clOrdId=" ).append( getClOrdId() );
        out.append( ", securityId=" ).append( getSecurityId() );
        out.append( ", symbol=" ).append( getSymbol() );
        out.append( ", currency=" );
        if ( getCurrency() != null ) getCurrency().id( out );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", text=" ).append( getText() );
        out.append( ", orderQty=" ).append( getOrderQty() );
        out.append( ", price=" ).append( getPrice() );
        out.append( ", side=" ).append( getSide() );
        out.append( ", onBehalfOfId=" ).append( getOnBehalfOfId() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
    }

}
