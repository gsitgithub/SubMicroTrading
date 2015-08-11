/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import java.util.Collection;

import com.rr.core.java.FieldOffsetDict;
import com.rr.core.java.JavaSpecific;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.MsgFlag;

/**
 * Queue optimised for use with Message, only at most ever one consumer from the queue 
 * Multiple possible producers on different threads entering Messages to the queue
 *
 * @NOTE Message a message must only ever be put on one queue at a time .. this is NOT checked
 * 
 * @NOTE optimisations made based on single thread consuming from queue
 * 
 * Only a subset of the queue methods are implemented
 */
public final class ConcLinkedMsgQueueSingle implements MessageQueue {

    private static final Logger    _log = LoggerFactory.console( ConcLinkedMsgQueueSingle.class );
    
    private static final ErrorCode UNABLE_UPDATE_PREV_TAIL = new ErrorCode( "CLM100", "ConcLinkedMsgQueueSingle.add() " + 
                                                                             "Unable to link new tail to  prev node" );

    private static final ErrorCode UNABLE_RESTORE_HEAD     = new ErrorCode( "CLM200", "ConcLinkedMsgQueueSingle.poll() " + 
                                                                            "Unable to restore head link to prev value" );

    private volatile boolean _loggedAnError = false;
    
    static class MessageHead implements Message {

        volatile Message _nextMessage = null;

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
            return CollectionTypes.ConcurrentLinkedQueueHead;
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
        @Override public void           dump( ReusableString logInbound )   { /* not needed */ }
    }
    
    /**
     * Pointer to header node, initialized to a dummy node.  The first actual node is at head.getNext().
     * The header node itself never changes
     */
    private final MessageHead _head = new MessageHead();

    /** Pointer to last node on list **/
    private transient volatile Message _tail = _head;

    private final long _tailOffset = getTailOffset();
    
    private FieldOffsetDict _nextMessageOffset = new FieldOffsetDict( Message.class, "_nextMessage" );

    private final String    _id;

    public ConcLinkedMsgQueueSingle() {
        this( null );
    }
    
    public ConcLinkedMsgQueueSingle( String id ) {
        _id = id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    private boolean casTail( Message cmp, Message val ) {
        return JavaSpecific.instance().compareAndSwapObject( this, _tailOffset, cmp, val );
    }

    private long getTailOffset() {
        long offset = JavaSpecific.instance().getOffset( ConcLinkedMsgQueueSingle.class, "_tail", true );
        
        if ( offset < 0 ) {
            throw new RuntimeException( "ConcLinkedMsgQueueSingle cant find offset of field _tail" );
        }
        
        return offset;
    }

    private boolean casSetNextEntry( Message entry, Message cmp, Message val ) {
        long offset = _nextMessageOffset.getOffset( entry, true );
        return JavaSpecific.instance().compareAndSwapObject( entry, offset, cmp, val );
    }

    /**
     * Inserts the specified element at the tail of this queue.
     *
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean add( Message entry ) {
        if ( entry == null ) return true;
        
        entry.attachQueue( null );
        
        for (;;) {
            Message tail = _tail;
            final Message nextToTail = tail.getNextQueueEntry();
            if ( tail == _tail ) {
                if ( nextToTail == null ) {
                    // move tail pointer to new entry
                    if ( casTail( tail, entry ) ) {
                        // relink previous node next ptr to new tail
                        if ( casSetNextEntry( tail, null, entry ) == false ) {
                            // shouldnt be possible 
                            logError( UNABLE_UPDATE_PREV_TAIL );
                        }
                        return true;
                    }
                } else { 
                    // another thread has entered something to tail of queue, spin waiting for tail ptr to be updated
                }
            }
        }
    }

    private void logError( ErrorCode code ) {
        if ( _loggedAnError == false ) {       // avoid spinning filling disk with errors
            _log.error( code, (ZString)null );
            _loggedAnError = true;
        }
    }

    /**
     * only ONE thread is allowed to CONSUME !
     */
    
    @Override
    public Message poll() {
        Message h;
        Message t;
        Message tmp;
        
        for (;;) {
            h = _head; // head element itself never changes
            t = _tail;

            if ( t == h ) {
                return null; // nothing in queue
            }
            
            final Message first = h.getNextQueueEntry();

            if ( t == first ) {
                // only one entry in list, move tail to head then return first

                tmp = t.getNextQueueEntry();
                
                if ( tmp == null ) { 
                    if ( casSetNextEntry( _head, first, null ) ) {
                        if ( casTail( t, h ) ) {
                            first.detachQueue();
                            return first;
                        } 
                        // problem now we NULL'd the head next but since then, a new node was added .. restore and cont
                        if ( casSetNextEntry( _head, null, first ) ==  false ) {
                            // should never happen
                            logError( UNABLE_RESTORE_HEAD );
                        }
                    }
                }
            } else if ( first != null ) {
                tmp  = first.getNextQueueEntry();
                // tmp is the third node, if tmp is null then tail should equal the second node, if not a node is midway thru being added
                // in which case circle round and wait for add to complete
                if ( tmp != null ) {
                     if ( casSetNextEntry( _head, first, tmp ) ) {
                        // ok dumy head now points to the next AFTER first ... first is now detached from queue
                        first.detachQueue();
                        return first;
                     }
                }
            }
        }
    }

    /**
     * Returns <tt>true</tt> if this queue contains no elements.
     *
     * @return <tt>true</tt> if this queue contains no elements
     */
    @Override
    public boolean isEmpty() {
        return _head.getNextQueueEntry() == null;
    }

    /**
     * Returns the number of elements in this queue.  If this queue
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * <p>Beware that, unlike in most collections, this method is
     * <em>NOT</em> a constant-time operation. Because of the
     * asynchronous nature of these queues, determining the current
     * number of elements requires an O(n) traversal.
     *
     * @return the number of elements in this queue
     */
    @Override
    public int size() {
        
        int count = 0;

        for ( Message p = _head.getNextQueueEntry(); p != null; p = p.getNextQueueEntry() ) {
            // Collections.size() spec says to max out
            if (++count == Integer.MAX_VALUE)
                break;
        }
        
        return count;
    }

    Message first() {
        return _head.getNextQueueEntry();
    }

    @Override
    public void clear() {
        while ( poll() != null ) {
            // nada
        }
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
