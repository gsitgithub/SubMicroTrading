/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mdadapters;

import com.rr.core.component.SMTComponent;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.Stopable;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.md.book.l2.L2BookListener;

/**
 * listens to changes in book and logs them
 */
public final class DebugBookChangeConsumer implements L2BookListener<Book>, Stopable, SMTComponent {

    private static final Logger     _log = LoggerFactory.create( DebugBookChangeConsumer.class );

    private final ReusableString    _debugMsg = new ReusableString();
    
    private final String            _id;

    private       long              _changes;
    
    public DebugBookChangeConsumer() {
        this( "anonymous");
    }

    public DebugBookChangeConsumer( String id ) {
        super();
        _id = id;
    }

    @Override
    public void changed( final Book book ) {
        ++_changes;
        _debugMsg.reset();
        book.dump( _debugMsg );
        _log.info( _debugMsg );
    }

    @Override
    public void stop() {
        // nothing
    }

    @Override
    public void clear() {
        // nothing
    }

    @Override
    public String id() {
        return _id;
    }

    public long getChangeCount() {
        return _changes;
    }
    
    public long getNotifyCount() {
        return 0;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}

