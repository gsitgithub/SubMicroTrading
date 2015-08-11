/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.core.model.Book;
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

public final class BookClearImpl implements BaseITCH, BookClearWrite, Reusable<BookClearImpl> {

   // Attrs

    private          BookClearImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _nanosecond = Constants.UNSET_INT;
    private long _orderId = Constants.UNSET_LONG;
    private int _msgSeqNum = Constants.UNSET_INT;

    private Book _book;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getNanosecond() { return _nanosecond; }
    @Override public final void setNanosecond( int val ) { _nanosecond = val; }

    @Override public final long getOrderId() { return _orderId; }
    @Override public final void setOrderId( long val ) { _orderId = val; }

    @Override public final Book getBook() { return _book; }
    @Override public final void setBook( Book val ) { _book = val; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _nanosecond = Constants.UNSET_INT;
        _orderId = Constants.UNSET_LONG;
        _book = null;
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.BookClear;
    }

    @Override
    public final BookClearImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( BookClearImpl nxt ) {
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
        out.append( "BookClearImpl" ).append( ' ' );
        out.append( ", nanosecond=" ).append( getNanosecond() );
        out.append( ", orderId=" ).append( getOrderId() );
        out.append( ", book=" );
        if ( getBook() != null ) getBook().id( out );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
