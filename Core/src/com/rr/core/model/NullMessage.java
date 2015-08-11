/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;

public class NullMessage implements Message {

    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;

    public NullMessage() {
    }

    public NullMessage( MessageHandler src ) {
        _messageHandler = src;
    }

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
        return CoreReusableType.NullMessage;
    }

    @Override public int            getMsgSeqNum()                      { return 0; }
    @Override public void           setMsgSeqNum( int seqNum )          { /* not needed */ }
    @Override public void           setFlag( MsgFlag flag, boolean on ) { /* not needed */ }
    @Override public boolean        isFlagSet( MsgFlag flag )           { return false; }
    @Override public void           dump( ReusableString out )          { /* not needed */ }
}
