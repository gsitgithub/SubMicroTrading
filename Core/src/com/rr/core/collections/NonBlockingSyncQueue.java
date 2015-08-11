/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.MsgFlag;

public class NonBlockingSyncQueue implements MessageQueue {

    static class MessageHead implements Message {

        Message _nextMessage = null;

        @Override
        public void attachQueue( Message nextNode ) {
            _nextMessage = nextNode;
        }

        @Override
        public void detachQueue() {
            _nextMessage = null;
        }

        @Override
        public ReusableType getReusableType() {
            return CollectionTypes.NonBlockSyncQueueHead;
        }

        @Override
        public Message getNextQueueEntry() {
            return _nextMessage;
        }
        
        @Override public MessageHandler getMessageHandler()                 { return null; }
        @Override public void           setMessageHandler                   ( MessageHandler handler ) { /* not needed */ }
        @Override public int            getMsgSeqNum()                      { return 0; }
        @Override public void           setMsgSeqNum( int seqNum )          { /* not needed */ }
        @Override public void           setFlag( MsgFlag flag, boolean on ) { /* not needed */ }
        @Override public boolean        isFlagSet( MsgFlag flag )           { return false; }
        @Override public void           dump( ReusableString out )          { /* not needed */ }
    }
    
    /**
     * Pointer to header node, initialized to a dummy node.  The first actual node is at head.getNext().
     * The header node itself never changes
     */
    private final Message _head = new MessageHead();
    private Message _tail = _head;
    
    private final String    _id;

    public NonBlockingSyncQueue() {
        this( null );
    }
    
    public NonBlockingSyncQueue( String id ) {
        _id = id;
    }
    
    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
    public boolean add( Message e ) {
        e.attachQueue( null );
        
        synchronized( _head ) {
            _tail.attachQueue( e );
            _tail = e;
        }
        
        return true;
    }

    /**
     * @return chain of Messages in order of insertion
     * 
     * @NOTE for some reason this fails in PerfTestQueueTest
     */
    public Message getChain() {
        Message t;
        
        synchronized( _head ) {
            t = _head.getNextQueueEntry();
            _head.attachQueue( null );
            _tail = _head;
        }
        
        return t;
    }

    @Override
    public Message poll() {
        Message t;
        
        synchronized( _head ) {
            t = _head.getNextQueueEntry();
            
            if ( _tail == _head ) return null;
            
            if ( _tail != t ) {
                _head.attachQueue( t.getNextQueueEntry() );
            } else {
                _head.attachQueue( null );
                _tail = _head;
            }
        }

        t.attachQueue( null );
        
        return t;
    }

    @Override
    public void clear() {
        poll();
    }

    @Override
    public boolean isEmpty() {
        synchronized( _head ) {
            return( _head.getNextQueueEntry() == null );
        }
    }

    @Override
    public int size() {
        int cnt = 0;
        
        Message m;
        
        synchronized( _head ) {
            m = _head.getNextQueueEntry();
        }

        while( m != null ) {
            ++cnt;
            m = m.getNextQueueEntry();
        }
        
        return cnt;
    }

    @Override
    public Message next() {
        Message m;
        
        do {
            m = poll();
        } while( m == null );
        
        return m;
    }
}
