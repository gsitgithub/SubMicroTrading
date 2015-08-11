/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.TradingSessionID;
import com.rr.model.generated.internal.type.TradingSessionSubID;
import com.rr.model.generated.internal.type.TradSesStatus;
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

public final class TradingSessionStatusImpl implements SessionHeader, TradingSessionStatusWrite, Reusable<TradingSessionStatusImpl> {

   // Attrs

    private          TradingSessionStatusImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _marketSegmentID = Constants.UNSET_INT;
    private int _transactTime = Constants.UNSET_INT;
    private int _msgSeqNum = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;

    private TradingSessionID _tradingSessionID;
    private TradingSessionSubID _tradingSessionSubID;
    private TradSesStatus _tradSesStatus;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getMarketSegmentID() { return _marketSegmentID; }
    @Override public final void setMarketSegmentID( int val ) { _marketSegmentID = val; }

    @Override public final TradingSessionID getTradingSessionID() { return _tradingSessionID; }
    @Override public final void setTradingSessionID( TradingSessionID val ) { _tradingSessionID = val; }

    @Override public final TradingSessionSubID getTradingSessionSubID() { return _tradingSessionSubID; }
    @Override public final void setTradingSessionSubID( TradingSessionSubID val ) { _tradingSessionSubID = val; }

    @Override public final TradSesStatus getTradSesStatus() { return _tradSesStatus; }
    @Override public final void setTradSesStatus( TradSesStatus val ) { _tradSesStatus = val; }

    @Override public final int getTransactTime() { return _transactTime; }
    @Override public final void setTransactTime( int val ) { _transactTime = val; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _marketSegmentID = Constants.UNSET_INT;
        _tradingSessionID = null;
        _tradingSessionSubID = null;
        _tradSesStatus = null;
        _transactTime = Constants.UNSET_INT;
        _msgSeqNum = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.TradingSessionStatus;
    }

    @Override
    public final TradingSessionStatusImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( TradingSessionStatusImpl nxt ) {
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
        out.append( "TradingSessionStatusImpl" ).append( ' ' );
        out.append( ", marketSegmentID=" ).append( getMarketSegmentID() );
        out.append( ", tradingSessionID=" ).append( getTradingSessionID() );
        out.append( ", tradingSessionSubID=" ).append( getTradingSessionSubID() );
        out.append( ", tradSesStatus=" ).append( getTradSesStatus() );
        out.append( ", transactTime=" ).append( getTransactTime() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
    }

}
