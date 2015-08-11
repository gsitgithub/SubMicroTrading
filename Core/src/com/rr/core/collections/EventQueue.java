/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import com.rr.core.model.Message;

/**
 * generally processor will send only one message up or downstream so optimise for that
 *
 * @author Richard Rose
 */
public class EventQueue {

    private Message _first;

    private final MessageHead _head = new MessageHead();
    
    private Message     _tail = _head;
    
    public boolean add( Message msg ) {
        if ( _first == null ){
            _first = msg;
        } else {
            _tail.attachQueue( msg );
            _tail = msg;
        }
        
        return true;
    }
    
    public Message get() {
        if ( _first != null ) { 
            Message tmp = _first;
            _first = null;
            return tmp;
        }  else if ( _head == _tail ) {
            return null;
        } else {
            Message tmp  = _head.getNextQueueEntry();
            Message next = tmp.getNextQueueEntry();
            _head.attachQueue( next );
            
            if ( next == null ) {
                _tail = _head;
            }
            
            return tmp;
        }
    }

    public void clear() {
        // dont recycle, somethings wrong to be clearing the queue, dont assume ownershipof instances in Q
        _head.detachQueue();
        _tail =_head;
    }

    public Message next() {
        return get();
    }

    public Message poll() {
        return get();
    }

    public int size() {

        int count=0;
        
        if ( _first != null ) {
            ++count;
        }
        
        if ( _head == _tail ) return count;
        
        Message m = _head.getNextQueueEntry();
        
        while( m != null ) {
            ++count;
            
            m  = m.getNextQueueEntry();
        }
        
        return count;
    }
}
