/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.EncryptMethod;
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

public final class LogonImpl implements SessionHeader, LogonWrite, Reusable<LogonImpl> {

   // Attrs

    private          LogonImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private final ReusableString _senderCompId = new ReusableString( SizeType.COMPID_LENGTH.getSize() );
    private final ReusableString _senderSubId = new ReusableString( SizeType.COMPID_LENGTH.getSize() );
    private final ReusableString _targetCompId = new ReusableString( SizeType.COMPID_LENGTH.getSize() );
    private final ReusableString _targetSubId = new ReusableString( SizeType.COMPID_LENGTH.getSize() );
    private final ReusableString _onBehalfOfId = new ReusableString( SizeType.COMPID_LENGTH.getSize() );
    private int _heartBtInt = Constants.UNSET_INT;
    private int _rawDataLen = Constants.UNSET_INT;
    private final ReusableString _rawData = new ReusableString( SizeType.USERNAME_LENGTH.getSize() );
    private boolean _resetSeqNumFlag = false;
    private int _nextExpectedMsgSeqNum = Constants.UNSET_INT;
    private int _msgSeqNum = Constants.UNSET_INT;
    private int _sendingTime = Constants.UNSET_INT;

    private EncryptMethod _encryptMethod;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getSenderCompId() { return _senderCompId; }

    @Override public final void setSenderCompId( byte[] buf, int offset, int len ) { _senderCompId.setValue( buf, offset, len ); }
    @Override public final ReusableString getSenderCompIdForUpdate() { return _senderCompId; }

    @Override public final ViewString getSenderSubId() { return _senderSubId; }

    @Override public final void setSenderSubId( byte[] buf, int offset, int len ) { _senderSubId.setValue( buf, offset, len ); }
    @Override public final ReusableString getSenderSubIdForUpdate() { return _senderSubId; }

    @Override public final ViewString getTargetCompId() { return _targetCompId; }

    @Override public final void setTargetCompId( byte[] buf, int offset, int len ) { _targetCompId.setValue( buf, offset, len ); }
    @Override public final ReusableString getTargetCompIdForUpdate() { return _targetCompId; }

    @Override public final ViewString getTargetSubId() { return _targetSubId; }

    @Override public final void setTargetSubId( byte[] buf, int offset, int len ) { _targetSubId.setValue( buf, offset, len ); }
    @Override public final ReusableString getTargetSubIdForUpdate() { return _targetSubId; }

    @Override public final ViewString getOnBehalfOfId() { return _onBehalfOfId; }

    @Override public final void setOnBehalfOfId( byte[] buf, int offset, int len ) { _onBehalfOfId.setValue( buf, offset, len ); }
    @Override public final ReusableString getOnBehalfOfIdForUpdate() { return _onBehalfOfId; }

    @Override public final EncryptMethod getEncryptMethod() { return _encryptMethod; }
    @Override public final void setEncryptMethod( EncryptMethod val ) { _encryptMethod = val; }

    @Override public final int getHeartBtInt() { return _heartBtInt; }
    @Override public final void setHeartBtInt( int val ) { _heartBtInt = val; }

    @Override public final int getRawDataLen() { return _rawDataLen; }
    @Override public final void setRawDataLen( int val ) { _rawDataLen = val; }

    @Override public final ViewString getRawData() { return _rawData; }

    @Override public final void setRawData( byte[] buf, int offset, int len ) { _rawData.setValue( buf, offset, len ); }
    @Override public final ReusableString getRawDataForUpdate() { return _rawData; }

    @Override public final boolean getResetSeqNumFlag() { return _resetSeqNumFlag; }
    @Override public final void setResetSeqNumFlag( boolean val ) { _resetSeqNumFlag = val; }

    @Override public final int getNextExpectedMsgSeqNum() { return _nextExpectedMsgSeqNum; }
    @Override public final void setNextExpectedMsgSeqNum( int val ) { _nextExpectedMsgSeqNum = val; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }

    @Override public final int getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( int val ) { _sendingTime = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _senderCompId.reset();
        _senderSubId.reset();
        _targetCompId.reset();
        _targetSubId.reset();
        _onBehalfOfId.reset();
        _encryptMethod = null;
        _heartBtInt = Constants.UNSET_INT;
        _rawDataLen = Constants.UNSET_INT;
        _rawData.reset();
        _resetSeqNumFlag = false;
        _nextExpectedMsgSeqNum = Constants.UNSET_INT;
        _msgSeqNum = Constants.UNSET_INT;
        _sendingTime = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.Logon;
    }

    @Override
    public final LogonImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( LogonImpl nxt ) {
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
        out.append( "LogonImpl" ).append( ' ' );
        out.append( ", senderCompId=" ).append( getSenderCompId() );
        out.append( ", senderSubId=" ).append( getSenderSubId() );
        out.append( ", targetCompId=" ).append( getTargetCompId() );
        out.append( ", targetSubId=" ).append( getTargetSubId() );
        out.append( ", onBehalfOfId=" ).append( getOnBehalfOfId() );
        out.append( ", encryptMethod=" ).append( getEncryptMethod() );
        out.append( ", heartBtInt=" ).append( getHeartBtInt() );
        out.append( ", rawDataLen=" ).append( getRawDataLen() );
        out.append( ", rawData=" ).append( getRawData() );
        out.append( ", resetSeqNumFlag=" ).append( getResetSeqNumFlag() );
        out.append( ", nextExpectedMsgSeqNum=" ).append( getNextExpectedMsgSeqNum() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
        out.append( ", sendingTime=" ).append( getSendingTime() );
    }

}
