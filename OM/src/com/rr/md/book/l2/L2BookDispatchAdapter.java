/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l2;

import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.Stopable;
import com.rr.md.book.MutableFixBook;

/**
 * Adapts book change notifications to a message dispatcher
 * 
 * Note use of getNextQueueEntry to check if book already queued and if so then conflate
 *
 * @author Richard Rose
 */
public class L2BookDispatchAdapter<T extends MutableFixBook> implements L2BookListener<T>, Stopable {

    private final MessageDispatcher _dispatcher;
    private       int               _conflateCount;
    private final String            _id;
    
    public L2BookDispatchAdapter( MessageDispatcher dispatcher ) {
        this( "anonymous", dispatcher );
    }

    public L2BookDispatchAdapter( String id, MessageDispatcher dispatcher ) {
        super();
        _dispatcher = dispatcher;
        _id = id;
    }

    @Override
    public void changed( T book ) {
        if ( book.getNextQueueEntry() == null ) {
            _dispatcher.dispatch( book );
        } else {
            ++_conflateCount;
        }
    }
    
    public int getConflateCount() {
        return _conflateCount;
    }

    @Override
    public void stop() {
        _dispatcher.setStopping();
    }

    @Override
    public void clear() {
        _conflateCount = 0;
    }

    @Override
    public String id() {
        return _id;
    }
}
