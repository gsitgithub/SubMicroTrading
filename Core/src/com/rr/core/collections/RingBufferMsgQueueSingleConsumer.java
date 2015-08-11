/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import java.util.concurrent.atomic.AtomicLong;

import sun.misc.Contended;

import com.rr.core.model.Message;

/**
 * message queue consumer, supports multi producer single consumer
 */
public final class RingBufferMsgQueueSingleConsumer implements MessageQueue {

    private static final long NOT_SET = -1;
    
    private final Message[] _ring;
    private final int       _arrayMask;
    private final int       _size;
    
    private @Contended volatile long       _lastConsumed           = NOT_SET;
    private @Contended volatile long       _lastFullyAdded         = NOT_SET;

    private          long       _unsafeLastConsumed     = NOT_SET; // helps avoid volatile writes
    private          long       _availableToConsume     = NOT_SET;
    private          AtomicLong _lastClaimedByProducer  = new AtomicLong( NOT_SET );
    
    private          long       _unsafeProducerLastSeenConsumed; // helps avoid spinning read barrier
    
    private final    String     _id;

    public RingBufferMsgQueueSingleConsumer( int size ) {
        this( null, size );
    }
    
    public RingBufferMsgQueueSingleConsumer( String id, int size ) {
        
        _id = id;

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < size)
            capacity <<= 1;
        
        _size = capacity;
        _ring = new Message[ capacity ];

        for( int i=0 ; i < capacity ; i++ ) {
            _ring[i] = null;
        }
        
        _arrayMask = capacity - 1;
    }
    
    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
    public int size() {
        return (int) (_lastClaimedByProducer.longValue() - _unsafeLastConsumed);
    }
    
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public void clear() {
        _lastConsumed       = NOT_SET;
        _unsafeLastConsumed = NOT_SET; // helps avoid volatile writes
        _availableToConsume  = NOT_SET;
        _lastClaimedByProducer.set( NOT_SET );
    }
    
    @Override
    public Message next() {
        Message m;
        
        do {
            m = poll();
        } while( m == null );
        
        return m;
    }

    @Override
    public boolean add( Message msg ) {
        final long putSeqNum = waitForNextFreeEntry(); 
        final int entry = getIndex( putSeqNum );
        final long expectedSequence = putSeqNum - 1;
        while( expectedSequence != _lastFullyAdded ) {
            // potential multi producer race, each producer thread will have unique putSeqNum, spin with read barrier, until this thread is next in line
            Thread.yield(); // give the other thread chance to go
        }
        _ring[entry] = msg;
        _lastFullyAdded = putSeqNum; // write barrier
        return true;
    }
    
    @Override
    public Message poll() {
        if ( _unsafeLastConsumed < _availableToConsume ) {  // in batch consume from previous call

            final int idx = getIndex( ++_unsafeLastConsumed ); 
            final Message m = _ring[ idx ]; // grab value before declare it free
            _ring[ idx ] = null;
            
            if ( _unsafeLastConsumed == _availableToConsume ) {
                _lastConsumed = _unsafeLastConsumed;        // write barrier
            }
            
            return m;
        }

        final long lastFullyAdded = _lastFullyAdded;        // read barrier
        if ( _unsafeLastConsumed == lastFullyAdded ) {
            return null;
        }
        
        _availableToConsume = lastFullyAdded;
        final int idx = getIndex( ++_unsafeLastConsumed ); 
        final Message m = _ring[ idx ]; // grab value before declare it free
        _ring[ idx ] = null;

        if ( _unsafeLastConsumed == _availableToConsume ) { // only one entry available
            _lastConsumed = _unsafeLastConsumed;            // write barrier
        }
        
        return m;
    }
    
    private long waitForNextFreeEntry() {
        final long sequenceForPut = _lastClaimedByProducer.incrementAndGet();
        final long wrapPoint = sequenceForPut - _size;

        // note the unsafeProducerLastSeenConsumed means safe a mem read barrier when there was space in the buffer
        // when the wrap point is greater then the spin will occur
        while( wrapPoint > _unsafeProducerLastSeenConsumed && wrapPoint > (_unsafeProducerLastSeenConsumed = _lastConsumed) ) {
            // spin
        }

        return sequenceForPut;
    }

    private int getIndex( final long sequence ) {
        return (int)sequence & _arrayMask;
    }
}
