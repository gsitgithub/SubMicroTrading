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

public class BlockingSyncQueue implements MessageQueue {

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
            return CollectionTypes.BlockSyncQueueHead;
        }

        @Override
        public Message getNextQueueEntry() {
            return _nextMessage;
        }

        @Override public MessageHandler getMessageHandler()                 { return null; }
        @Override public void           setMessageHandler                   ( MessageHandler handler ) { /* not needed */ }
        @Override public int            getMsgSeqNum()                      { return 0; }
        @Override public void           setMsgSeqNum( int seqNum )          { /* not needed */ }
        @Override public void           dump( ReusableString logInbound )   { /* not needed */ }
        @Override public void           setFlag( MsgFlag flag, boolean on ) { /* not needed */ }
        @Override public boolean        isFlagSet( MsgFlag flag )           { return false; }
    }
    
    /**
     * Pointer to header node, initialized to a dummy node.  The first actual node is at head.getNext().
     * The header node itself never changes
     */
    private final Message   _head = new MessageHead();
    private       Message   _tail = _head;
    private final String    _id;

    public BlockingSyncQueue() {
        this( null );
    }
    
    public BlockingSyncQueue( String id ) {
        _id = id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
    public boolean add( Message e ) {
        synchronized( _head ) {
            boolean signal = (_head == _tail);
            
            _tail.attachQueue( e );
            _tail = e;
            
            if ( signal ) _head.notifyAll();
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
            if ( _tail == _head ) return null;
            
            t = _head.getNextQueueEntry();
            
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
        Message t = null;
        
        do {
            
            synchronized( _head ) {
                
                if ( _tail == _head ) {
                    try {
                        _head.wait();
                    } catch( InterruptedException e ) {
                        // dont care
                    }
                }
                
                t = _head.getNextQueueEntry();

                if ( t != null ) {
                    if ( _tail != t ) {
                        _head.attachQueue( t.getNextQueueEntry() );
                    } else {
                        _head.attachQueue( null );
                        _tail = _head;
                    }
                }
            }

        } while( t == null );
        
        t.attachQueue( null );
        
        return t;
    }
}
