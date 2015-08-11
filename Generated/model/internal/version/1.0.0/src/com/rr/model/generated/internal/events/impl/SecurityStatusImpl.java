/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.core.model.SecurityIDSource;
import com.rr.model.generated.internal.type.SecurityTradingStatus;
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

public final class SecurityStatusImpl implements BaseMDResponse, SecurityStatusWrite, Reusable<SecurityStatusImpl> {

   // Attrs

    private          SecurityStatusImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private long _securityID = Constants.UNSET_LONG;
    private int _TradeDate = Constants.UNSET_INT;
    private double _highPx = Constants.UNSET_DOUBLE;
    private double _lowPx = Constants.UNSET_DOUBLE;
    private int _haltReason = Constants.UNSET_INT;
    private int _SecurityTradingEvent = Constants.UNSET_INT;
    private final ReusableString _symbol = new ReusableString( SizeType.SYMBOL_LENGTH.getSize() );
    private final ReusableString _securityExchange = new ReusableString( SizeType.SECURITYEXCH_LENGTH.getSize() );
    private int _msgSeqNum = Constants.UNSET_INT;

    private SecurityIDSource _securityIDSource = SecurityIDSource.ExchangeSymbol;
    private SecurityTradingStatus _securityTradingStatus;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final SecurityIDSource getSecurityIDSource() { return _securityIDSource; }
    @Override public final void setSecurityIDSource( SecurityIDSource val ) { _securityIDSource = val; }

    @Override public final long getSecurityID() { return _securityID; }
    @Override public final void setSecurityID( long val ) { _securityID = val; }

    @Override public final int getTradeDate() { return _TradeDate; }
    @Override public final void setTradeDate( int val ) { _TradeDate = val; }

    @Override public final double getHighPx() { return _highPx; }
    @Override public final void setHighPx( double val ) { _highPx = val; }

    @Override public final double getLowPx() { return _lowPx; }
    @Override public final void setLowPx( double val ) { _lowPx = val; }

    @Override public final SecurityTradingStatus getSecurityTradingStatus() { return _securityTradingStatus; }
    @Override public final void setSecurityTradingStatus( SecurityTradingStatus val ) { _securityTradingStatus = val; }

    @Override public final int getHaltReason() { return _haltReason; }
    @Override public final void setHaltReason( int val ) { _haltReason = val; }

    @Override public final int getSecurityTradingEvent() { return _SecurityTradingEvent; }
    @Override public final void setSecurityTradingEvent( int val ) { _SecurityTradingEvent = val; }

    @Override public final ViewString getSymbol() { return _symbol; }

    @Override public final void setSymbol( byte[] buf, int offset, int len ) { _symbol.setValue( buf, offset, len ); }
    @Override public final ReusableString getSymbolForUpdate() { return _symbol; }

    @Override public final ViewString getSecurityExchange() { return _securityExchange; }

    @Override public final void setSecurityExchange( byte[] buf, int offset, int len ) { _securityExchange.setValue( buf, offset, len ); }
    @Override public final ReusableString getSecurityExchangeForUpdate() { return _securityExchange; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _securityIDSource = SecurityIDSource.ExchangeSymbol;
        _securityID = Constants.UNSET_LONG;
        _TradeDate = Constants.UNSET_INT;
        _highPx = Constants.UNSET_DOUBLE;
        _lowPx = Constants.UNSET_DOUBLE;
        _securityTradingStatus = null;
        _haltReason = Constants.UNSET_INT;
        _SecurityTradingEvent = Constants.UNSET_INT;
        _symbol.reset();
        _securityExchange.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.SecurityStatus;
    }

    @Override
    public final SecurityStatusImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( SecurityStatusImpl nxt ) {
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
        out.append( "SecurityStatusImpl" ).append( ' ' );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", securityID=" ).append( getSecurityID() );
        out.append( ", TradeDate=" ).append( getTradeDate() );
        out.append( ", highPx=" ).append( getHighPx() );
        out.append( ", lowPx=" ).append( getLowPx() );
        out.append( ", securityTradingStatus=" ).append( getSecurityTradingStatus() );
        out.append( ", haltReason=" ).append( getHaltReason() );
        out.append( ", SecurityTradingEvent=" ).append( getSecurityTradingEvent() );
        out.append( ", symbol=" ).append( getSymbol() );
        out.append( ", securityExchange=" ).append( getSecurityExchange() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
