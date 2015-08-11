/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.ETIEurexDataStream;
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

public final class ETIRetransmitOrderEventsImpl implements BaseETIRequest, ETIRetransmitOrderEventsWrite, Reusable<ETIRetransmitOrderEventsImpl> {

   // Attrs

    private          ETIRetransmitOrderEventsImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _subscriptionScope = Constants.UNSET_INT;
    private short _partitionID = Constants.UNSET_SHORT;
    private final ReusableString _applBegMsgID = new ReusableString( SizeType.ETI_APP_MSG_ID_LENGTH.getSize() );
    private final ReusableString _applEndMsgID = new ReusableString( SizeType.ETI_APP_MSG_ID_LENGTH.getSize() );
    private int _msgSeqNum = Constants.UNSET_INT;

    private ETIEurexDataStream _refApplID;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getSubscriptionScope() { return _subscriptionScope; }
    @Override public final void setSubscriptionScope( int val ) { _subscriptionScope = val; }

    @Override public final short getPartitionID() { return _partitionID; }
    @Override public final void setPartitionID( short val ) { _partitionID = val; }

    @Override public final ETIEurexDataStream getRefApplID() { return _refApplID; }
    @Override public final void setRefApplID( ETIEurexDataStream val ) { _refApplID = val; }

    @Override public final ViewString getApplBegMsgID() { return _applBegMsgID; }

    @Override public final void setApplBegMsgID( byte[] buf, int offset, int len ) { _applBegMsgID.setValue( buf, offset, len ); }
    @Override public final ReusableString getApplBegMsgIDForUpdate() { return _applBegMsgID; }

    @Override public final ViewString getApplEndMsgID() { return _applEndMsgID; }

    @Override public final void setApplEndMsgID( byte[] buf, int offset, int len ) { _applEndMsgID.setValue( buf, offset, len ); }
    @Override public final ReusableString getApplEndMsgIDForUpdate() { return _applEndMsgID; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _subscriptionScope = Constants.UNSET_INT;
        _partitionID = Constants.UNSET_SHORT;
        _refApplID = null;
        _applBegMsgID.reset();
        _applEndMsgID.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.ETIRetransmitOrderEvents;
    }

    @Override
    public final ETIRetransmitOrderEventsImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( ETIRetransmitOrderEventsImpl nxt ) {
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
        out.append( "ETIRetransmitOrderEventsImpl" ).append( ' ' );
        out.append( ", subscriptionScope=" ).append( getSubscriptionScope() );
        out.append( ", partitionID=" ).append( getPartitionID() );
        out.append( ", refApplID=" ).append( getRefApplID() );
        out.append( ", applBegMsgID=" ).append( getApplBegMsgID() );
        out.append( ", applEndMsgID=" ).append( getApplEndMsgID() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
