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
import com.rr.core.lang.Stopable;
import com.rr.core.model.Book;
import com.rr.core.model.BookListener;
import com.rr.core.model.book.SingleOwnerListenerContext;
import com.rr.md.book.l2.L2BookListener;

/**
 * listens to changes in book and notifies the registered listeners in the books context block
 */
public final class L2BookChangeNotifier<T extends Book> implements L2BookListener<T>, Stopable, SMTComponent {

    private       long              _changeCount;
    private       long              _notifyCount;
    
    private final String            _id;
    
    public L2BookChangeNotifier() {
        this( "anonymous");
    }

    public L2BookChangeNotifier( String id ) {
        super();
        _id = id;
    }

    @Override
    public void changed( final T book ) {
        
        @SuppressWarnings( "unchecked" )
        final SingleOwnerListenerContext<T> ctx = (SingleOwnerListenerContext<T>) book.getContext();
        
        if ( ctx != null ) {
            final BookListener<T>[] listeners = ctx.getListeners();
    
            int numListeners = listeners.length;
            
            for ( int i=0 ; i < numListeners ; i++ ) {
                final BookListener<T> listener = listeners[i];
                
                listener.changed( book );
                ++_notifyCount;
            }
    
            ++_changeCount;
        }
    }

    @Override
    public void stop() {
        // nothing
    }

    @Override
    public void clear() {
        _changeCount = 0;
        _notifyCount = 0;
    }

    @Override
    public String id() {
        return _id;
    }

    public long getChangeCount() {
        return _changeCount;
    }
    
    public long getNotifyCount() {
        return _notifyCount;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}

