/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.SessionRejectReason;
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

public final class SessionRejectImpl implements SessionHeader, SessionRejectWrite, Reusable<SessionRejectImpl> {

   // Attrs

    private          SessionRejectImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _refSeqNum = Constants.UNSET_INT;
    private int _refTagID = Constants.UNSET_INT;
    private final ReusableString _refMsgType = new ReusableString( SizeType.TAG_LEN.getSize() );
    private final ReusableString _text = new ReusableString( SizeType.TEXT_LENGTH.getSize() );
    private int _msgSeqNum = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;

    private SessionRejectReason _sessionRejectReason;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getRefSeqNum() { return _refSeqNum; }
    @Override public final void setRefSeqNum( int val ) { _refSeqNum = val; }

    @Override public final int getRefTagID() { return _refTagID; }
    @Override public final void setRefTagID( int val ) { _refTagID = val; }

    @Override public final ViewString getRefMsgType() { return _refMsgType; }

    @Override public final void setRefMsgType( byte[] buf, int offset, int len ) { _refMsgType.setValue( buf, offset, len ); }
    @Override public final ReusableString getRefMsgTypeForUpdate() { return _refMsgType; }

    @Override public final SessionRejectReason getSessionRejectReason() { return _sessionRejectReason; }
    @Override public final void setSessionRejectReason( SessionRejectReason val ) { _sessionRejectReason = val; }

    @Override public final ViewString getText() { return _text; }

    @Override public final void setText( byte[] buf, int offset, int len ) { _text.setValue( buf, offset, len ); }
    @Override public final ReusableString getTextForUpdate() { return _text; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _refSeqNum = Constants.UNSET_INT;
        _refTagID = Constants.UNSET_INT;
        _refMsgType.reset();
        _sessionRejectReason = null;
        _text.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.SessionReject;
    }

    @Override
    public final SessionRejectImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( SessionRejectImpl nxt ) {
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
        out.append( "SessionRejectImpl" ).append( ' ' );
        out.append( ", refSeqNum=" ).append( getRefSeqNum() );
        out.append( ", refTagID=" ).append( getRefTagID() );
        out.append( ", refMsgType=" ).append( getRefMsgType() );
        out.append( ", sessionRejectReason=" ).append( getSessionRejectReason() );
        out.append( ", text=" ).append( getText() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
    }

}
