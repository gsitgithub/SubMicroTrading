/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.events.interfaces.SecMassStatGrp;
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

public final class MassInstrumentStateChangeImpl implements SessionHeader, MassInstrumentStateChangeWrite, Reusable<MassInstrumentStateChangeImpl> {

   // Attrs

    private          MassInstrumentStateChangeImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _marketSegmentID = Constants.UNSET_INT;
    private int _instrumentScopeProductComplex = Constants.UNSET_INT;
    private int _securityMassTradingStatus = Constants.UNSET_INT;
    private int _transactTime = Constants.UNSET_INT;
    private int _numRelatedSym = Constants.UNSET_INT;
    private int _msgSeqNum = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;

    private SecMassStatGrp _instState;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getMarketSegmentID() { return _marketSegmentID; }
    @Override public final void setMarketSegmentID( int val ) { _marketSegmentID = val; }

    @Override public final int getInstrumentScopeProductComplex() { return _instrumentScopeProductComplex; }
    @Override public final void setInstrumentScopeProductComplex( int val ) { _instrumentScopeProductComplex = val; }

    @Override public final int getSecurityMassTradingStatus() { return _securityMassTradingStatus; }
    @Override public final void setSecurityMassTradingStatus( int val ) { _securityMassTradingStatus = val; }

    @Override public final int getTransactTime() { return _transactTime; }
    @Override public final void setTransactTime( int val ) { _transactTime = val; }

    @Override public final int getNumRelatedSym() { return _numRelatedSym; }
    @Override public final void setNumRelatedSym( int val ) { _numRelatedSym = val; }

    @Override public final SecMassStatGrp getInstState() { return _instState; }
    @Override public final void setInstState( SecMassStatGrp val ) { _instState = val; }

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
        _instrumentScopeProductComplex = Constants.UNSET_INT;
        _securityMassTradingStatus = Constants.UNSET_INT;
        _transactTime = Constants.UNSET_INT;
        _numRelatedSym = Constants.UNSET_INT;
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
        return ModelReusableTypes.MassInstrumentStateChange;
    }

    @Override
    public final MassInstrumentStateChangeImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( MassInstrumentStateChangeImpl nxt ) {
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
        out.append( "MassInstrumentStateChangeImpl" ).append( ' ' );
        out.append( ", marketSegmentID=" ).append( getMarketSegmentID() );
        out.append( ", instrumentScopeProductComplex=" ).append( getInstrumentScopeProductComplex() );
        out.append( ", securityMassTradingStatus=" ).append( getSecurityMassTradingStatus() );
        out.append( ", transactTime=" ).append( getTransactTime() );
        out.append( ", numRelatedSym=" ).append( getNumRelatedSym() );

        SecMassStatGrpImpl tPtrinstState = (SecMassStatGrpImpl) getInstState();
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
