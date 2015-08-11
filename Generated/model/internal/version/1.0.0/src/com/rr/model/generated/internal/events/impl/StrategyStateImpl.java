/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.events.interfaces.StratInstrumentState;
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

public final class StrategyStateImpl implements SessionHeader, StrategyStateWrite, Reusable<StrategyStateImpl> {

   // Attrs

    private          StrategyStateImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private final ReusableString _algoId = new ReusableString( SizeType.ALGO_ID_LEN.getSize() );
    private int _timestamp = Constants.UNSET_INT;
    private int _algoEventSeqNum = Constants.UNSET_INT;
    private long _lastTickId = Constants.UNSET_LONG;
    private double _pnl = 0;
    private int _lastEventInst = Constants.UNSET_INT;
    private int _noInstEntries = Constants.UNSET_INT;
    private int _msgSeqNum = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;

    private StratInstrumentState _instState;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getAlgoId() { return _algoId; }

    @Override public final void setAlgoId( byte[] buf, int offset, int len ) { _algoId.setValue( buf, offset, len ); }
    @Override public final ReusableString getAlgoIdForUpdate() { return _algoId; }

    @Override public final int getTimestamp() { return _timestamp; }
    @Override public final void setTimestamp( int val ) { _timestamp = val; }

    @Override public final int getAlgoEventSeqNum() { return _algoEventSeqNum; }
    @Override public final void setAlgoEventSeqNum( int val ) { _algoEventSeqNum = val; }

    @Override public final long getLastTickId() { return _lastTickId; }
    @Override public final void setLastTickId( long val ) { _lastTickId = val; }

    @Override public final double getPnl() { return _pnl; }
    @Override public final void setPnl( double val ) { _pnl = val; }

    @Override public final int getLastEventInst() { return _lastEventInst; }
    @Override public final void setLastEventInst( int val ) { _lastEventInst = val; }

    @Override public final int getNoInstEntries() { return _noInstEntries; }
    @Override public final void setNoInstEntries( int val ) { _noInstEntries = val; }

    @Override public final StratInstrumentState getInstState() { return _instState; }
    @Override public final void setInstState( StratInstrumentState val ) { _instState = val; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _algoId.reset();
        _timestamp = Constants.UNSET_INT;
        _algoEventSeqNum = Constants.UNSET_INT;
        _lastTickId = Constants.UNSET_LONG;
        _pnl = 0;
        _lastEventInst = Constants.UNSET_INT;
        _noInstEntries = Constants.UNSET_INT;
        _instState = null;
        _msgSeqNum = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.StrategyState;
    }

    @Override
    public final StrategyStateImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( StrategyStateImpl nxt ) {
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
        out.append( "StrategyStateImpl" ).append( ' ' );
        out.append( ", algoId=" ).append( getAlgoId() );
        out.append( ", timestamp=" ).append( getTimestamp() );
        out.append( ", algoEventSeqNum=" ).append( getAlgoEventSeqNum() );
        out.append( ", lastTickId=" ).append( getLastTickId() );
        out.append( ", pnl=" ).append( getPnl() );
        out.append( ", lastEventInst=" ).append( getLastEventInst() );
        out.append( ", noInstEntries=" ).append( getNoInstEntries() );

        StratInstrumentStateImpl tPtrinstState = (StratInstrumentStateImpl) getInstState();
        int tIdxinstState=0;

        while( tPtrinstState != null ) {
            out.append( " {#" ).append( ++tIdxinstState ).append( "} " );
            tPtrinstState.dump( out );
            tPtrinstState = tPtrinstState.getNext();
        }

        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
    }

}
