/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import java.util.ArrayList;
import java.util.List;

import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;


public class PackedMessage implements Message {

    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private          int            _seqNum;
    private          int            _flags;
    private          List<Message>  _list = new ArrayList<Message>();

    @Override
    public void attachQueue( Message nextNode ) {
        _nextMessage = nextNode;
    }

    @Override
    public void detachQueue() {
        _nextMessage = null;
    }

    @Override
    public MessageHandler getMessageHandler() {
        return _messageHandler;
    }

    @Override
    public Message getNextQueueEntry() {
        return _nextMessage;
    }

    @Override
    public void setMessageHandler( MessageHandler handler ) {
        _messageHandler = handler;
    }

    @Override
    public ReusableType getReusableType() {
        return CoreReusableType.PackedMessage;
    }

    @Override public int            getMsgSeqNum()                      { return _seqNum; }
    @Override public void           setMsgSeqNum( int seqNum )          { _seqNum = seqNum; }
    @Override public void           dump( ReusableString out )          { /* not needed */ }

    public void reset() {
        _list.clear();
    }

    public void addMessage( Message msg ) {
        _list.add( msg );
    }
    
    public List<Message> getMessageList() {
        return _list;
    }
    
    @Override
    public void setFlag( MsgFlag flag, boolean isOn ) {
        _flags = (byte) MsgFlag.setFlag( _flags, flag, isOn );
    }

    @Override
    public boolean isFlagSet( MsgFlag flag ) {
        return MsgFlag.isOn( _flags, flag );
    }
}
