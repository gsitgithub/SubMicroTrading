/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix;

import com.rr.core.collections.MessageQueue;
import com.rr.core.model.Message;

/**
 * @WARNING USE WITH CAUTION ITEMS ADDED NOT RECYCLED SO WILL CONTRIB GC
 */
public class DummyQueue implements MessageQueue {

    private final String    _id;

    public DummyQueue() {
        this( null );
    }
    
    public DummyQueue( String id ) {
        _id = id;
    }
    
    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
    public Message poll() {
        return null;
    }

    @Override
    public Message next() {
        return null;
    }

    @Override
    public boolean add( Message e ) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
        // nothing
    }
}
