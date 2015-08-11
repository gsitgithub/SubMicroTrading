/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common.events;

import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.MsgFlag;

public abstract class BaseMDSEvent implements Message {

    protected volatile Message _nextMessage    = null;

    @Override
    public void attachQueue( Message nextNode ) {
        _nextMessage = nextNode;
    }

    @Override
    public void detachQueue() {
        _nextMessage = null;
    }

    @Override
    public Message getNextQueueEntry() {
        return _nextMessage;
    }

    @Override public void           setMessageHandler( MessageHandler handler )     { /* nothing */ }
    @Override public MessageHandler getMessageHandler()                             { return null; }
    @Override public int            getMsgSeqNum()                                  { return 0; }
    @Override public void           setMsgSeqNum( int seqNum )                      { /* nothing */ }
    @Override public void           setFlag( MsgFlag flag, boolean on )             { /* not needed */ }
    @Override public boolean        isFlagSet( MsgFlag flag )                       { return false; }
}
