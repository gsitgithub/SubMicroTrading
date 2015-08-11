/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import com.rr.core.component.SMTComponent;
import com.rr.core.model.Message;

/**
 * queue implementation as specifically required to pass Messages from multiple producers
 * to single consumer.
 *
 * @NOTE MUST BE THREADSAFE
 *
 * @author Richard Rose
 */
public interface MessageQueue extends SMTComponent {
    
    /**
     * @return either single Message OR the chain of all entries in queue in order of insertion, or null if queue empty
     */
    public Message poll();

    /**
     * @return chain of all entries in queue in order of insertion, block if queue empty
     */
    public Message next();

    /**
     * @param msg - the message to add to the end of the queue, note the next pointer will be nulled
     * @return true if successful as requiried by Queue interface
     */
    public boolean add( Message e );

    /**
     * @return true if the queue is empty
     */
    public boolean isEmpty();
    
    /**
     * @return size of the Q ... iterates over q counting elems 
     */
    public int size();

    /**
     * clear any messages in the q
     */
    public void clear();
}
