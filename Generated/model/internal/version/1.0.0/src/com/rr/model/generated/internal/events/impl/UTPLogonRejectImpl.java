/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.UTPRejCode;
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

public final class UTPLogonRejectImpl implements BaseUTP, UTPLogonRejectWrite, Reusable<UTPLogonRejectImpl> {

   // Attrs

    private          UTPLogonRejectImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _lastMsgSeqNumRcvd = Constants.UNSET_INT;
    private int _lastMsgSeqNumSent = Constants.UNSET_INT;
    private final ReusableString _rejectText = new ReusableString( SizeType.UTP_REJECT_TEXT_LENGTH.getSize() );
    private int _msgSeqNum = Constants.UNSET_INT;

    private UTPRejCode _rejectCode;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getLastMsgSeqNumRcvd() { return _lastMsgSeqNumRcvd; }
    @Override public final void setLastMsgSeqNumRcvd( int val ) { _lastMsgSeqNumRcvd = val; }

    @Override public final int getLastMsgSeqNumSent() { return _lastMsgSeqNumSent; }
    @Override public final void setLastMsgSeqNumSent( int val ) { _lastMsgSeqNumSent = val; }

    @Override public final UTPRejCode getRejectCode() { return _rejectCode; }
    @Override public final void setRejectCode( UTPRejCode val ) { _rejectCode = val; }

    @Override public final ViewString getRejectText() { return _rejectText; }

    @Override public final void setRejectText( byte[] buf, int offset, int len ) { _rejectText.setValue( buf, offset, len ); }
    @Override public final ReusableString getRejectTextForUpdate() { return _rejectText; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _lastMsgSeqNumRcvd = Constants.UNSET_INT;
        _lastMsgSeqNumSent = Constants.UNSET_INT;
        _rejectCode = null;
        _rejectText.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.UTPLogonReject;
    }

    @Override
    public final UTPLogonRejectImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( UTPLogonRejectImpl nxt ) {
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
        out.append( "UTPLogonRejectImpl" ).append( ' ' );
        out.append( ", lastMsgSeqNumRcvd=" ).append( getLastMsgSeqNumRcvd() );
        out.append( ", lastMsgSeqNumSent=" ).append( getLastMsgSeqNumSent() );
        out.append( ", rejectCode=" ).append( getRejectCode() );
        out.append( ", rejectText=" ).append( getRejectText() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
