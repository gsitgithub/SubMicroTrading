/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.ETIEnv;
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

public final class ETISessionLogonResponseImpl implements BaseETIResponse, ETISessionLogonResponseWrite, Reusable<ETISessionLogonResponseImpl> {

   // Attrs

    private          ETISessionLogonResponseImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _requestTime = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;
    private long _throttleTimeIntervalMS = Constants.UNSET_LONG;
    private int _throttleNoMsgs = Constants.UNSET_INT;
    private int _throttleDisconnectLimit = Constants.UNSET_INT;
    private int _heartBtIntMS = Constants.UNSET_INT;
    private int _sessionInstanceID = Constants.UNSET_INT;
    private final ReusableString _defaultCstmApplVerID = new ReusableString( SizeType.ETI_INTERFACE_VERSION_LENGTH.getSize() );
    private int _msgSeqNum = Constants.UNSET_INT;

    private ETIEnv _tradSesMode;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getRequestTime() { return _requestTime; }
    @Override public final void setRequestTime( int val ) { _requestTime = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }

    @Override public final long getThrottleTimeIntervalMS() { return _throttleTimeIntervalMS; }
    @Override public final void setThrottleTimeIntervalMS( long val ) { _throttleTimeIntervalMS = val; }

    @Override public final int getThrottleNoMsgs() { return _throttleNoMsgs; }
    @Override public final void setThrottleNoMsgs( int val ) { _throttleNoMsgs = val; }

    @Override public final int getThrottleDisconnectLimit() { return _throttleDisconnectLimit; }
    @Override public final void setThrottleDisconnectLimit( int val ) { _throttleDisconnectLimit = val; }

    @Override public final int getHeartBtIntMS() { return _heartBtIntMS; }
    @Override public final void setHeartBtIntMS( int val ) { _heartBtIntMS = val; }

    @Override public final int getSessionInstanceID() { return _sessionInstanceID; }
    @Override public final void setSessionInstanceID( int val ) { _sessionInstanceID = val; }

    @Override public final ETIEnv getTradSesMode() { return _tradSesMode; }
    @Override public final void setTradSesMode( ETIEnv val ) { _tradSesMode = val; }

    @Override public final ViewString getDefaultCstmApplVerID() { return _defaultCstmApplVerID; }

    @Override public final void setDefaultCstmApplVerID( byte[] buf, int offset, int len ) { _defaultCstmApplVerID.setValue( buf, offset, len ); }
    @Override public final ReusableString getDefaultCstmApplVerIDForUpdate() { return _defaultCstmApplVerID; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _requestTime = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _throttleTimeIntervalMS = Constants.UNSET_LONG;
        _throttleNoMsgs = Constants.UNSET_INT;
        _throttleDisconnectLimit = Constants.UNSET_INT;
        _heartBtIntMS = Constants.UNSET_INT;
        _sessionInstanceID = Constants.UNSET_INT;
        _tradSesMode = null;
        _defaultCstmApplVerID.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.ETISessionLogonResponse;
    }

    @Override
    public final ETISessionLogonResponseImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( ETISessionLogonResponseImpl nxt ) {
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
        out.append( "ETISessionLogonResponseImpl" ).append( ' ' );
        out.append( ", requestTime=" ).append( getRequestTime() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
        out.append( ", throttleTimeIntervalMS=" ).append( getThrottleTimeIntervalMS() );
        out.append( ", throttleNoMsgs=" ).append( getThrottleNoMsgs() );
        out.append( ", throttleDisconnectLimit=" ).append( getThrottleDisconnectLimit() );
        out.append( ", heartBtIntMS=" ).append( getHeartBtIntMS() );
        out.append( ", sessionInstanceID=" ).append( getSessionInstanceID() );
        out.append( ", tradSesMode=" ).append( getTradSesMode() );
        out.append( ", defaultCstmApplVerID=" ).append( getDefaultCstmApplVerID() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
