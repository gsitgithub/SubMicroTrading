/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

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

public final class UTPTradingSessionStatusImpl implements BaseUTP, UTPTradingSessionStatusWrite, Reusable<UTPTradingSessionStatusImpl> {

   // Attrs

    private          UTPTradingSessionStatusImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _mktPhaseChgTime = Constants.UNSET_INT;
    private final ReusableString _instClassId = new ReusableString( SizeType.UTP_INST_CLASS_LEN.getSize() );
    private final ReusableString _instClassStatus = new ReusableString( SizeType.UTP_INST_CLASS_STATUS_LEN.getSize() );
    private boolean _orderEntryAllowed = false;
    private final ReusableString _tradingSessionId = new ReusableString( SizeType.UTP_TRADING_SESSION_ID_LEN.getSize() );
    private int _msgSeqNum = Constants.UNSET_INT;


    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getMktPhaseChgTime() { return _mktPhaseChgTime; }
    @Override public final void setMktPhaseChgTime( int val ) { _mktPhaseChgTime = val; }

    @Override public final ViewString getInstClassId() { return _instClassId; }

    @Override public final void setInstClassId( byte[] buf, int offset, int len ) { _instClassId.setValue( buf, offset, len ); }
    @Override public final ReusableString getInstClassIdForUpdate() { return _instClassId; }

    @Override public final ViewString getInstClassStatus() { return _instClassStatus; }

    @Override public final void setInstClassStatus( byte[] buf, int offset, int len ) { _instClassStatus.setValue( buf, offset, len ); }
    @Override public final ReusableString getInstClassStatusForUpdate() { return _instClassStatus; }

    @Override public final boolean getOrderEntryAllowed() { return _orderEntryAllowed; }
    @Override public final void setOrderEntryAllowed( boolean val ) { _orderEntryAllowed = val; }

    @Override public final ViewString getTradingSessionId() { return _tradingSessionId; }

    @Override public final void setTradingSessionId( byte[] buf, int offset, int len ) { _tradingSessionId.setValue( buf, offset, len ); }
    @Override public final ReusableString getTradingSessionIdForUpdate() { return _tradingSessionId; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _mktPhaseChgTime = Constants.UNSET_INT;
        _instClassId.reset();
        _instClassStatus.reset();
        _orderEntryAllowed = false;
        _tradingSessionId.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.UTPTradingSessionStatus;
    }

    @Override
    public final UTPTradingSessionStatusImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( UTPTradingSessionStatusImpl nxt ) {
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
        out.append( "UTPTradingSessionStatusImpl" ).append( ' ' );
        out.append( ", mktPhaseChgTime=" ).append( getMktPhaseChgTime() );
        out.append( ", instClassId=" ).append( getInstClassId() );
        out.append( ", instClassStatus=" ).append( getInstClassStatus() );
        out.append( ", orderEntryAllowed=" ).append( getOrderEntryAllowed() );
        out.append( ", tradingSessionId=" ).append( getTradingSessionId() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
