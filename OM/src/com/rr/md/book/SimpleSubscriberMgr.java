/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.model.Book;
import com.rr.core.model.BookListener;
import com.rr.core.model.Instrument;
import com.rr.core.model.book.SingleOwnerListenerContext;
import com.rr.core.utils.SMTRuntimeException;


/**
 * allow multiple subscriptions to same book
 * 
 * each SimpleSubscriberMgr can have 1 BookSource
 * 
 * when book changes invoke each registered callback
 * 
 * Uses the Context on a book to store the subscribers to avoid an extra map lookup
 */
public final class SimpleSubscriberMgr<T extends Book> implements BookSubscriptionMgr<T> {

    private final String        _id;
    private final BookSource<T> _src;
    
    /**
     * retain map of subscriber to books for cancellation
     */
    private final Map<BookListener<T>, LinkedHashSet<T>> _subscribersBooks = new ConcurrentHashMap<BookListener<T>, LinkedHashSet<T>>();

    public SimpleSubscriberMgr( String id, BookSource<T> src ) {
        super();
        _id = id;
        _src = src;
    }

    @Override
    public String id() {
        return _id;
    }

    @Override
    public synchronized T subscribe( BookListener<T> callback, Instrument inst ) {
        T book =  _src.subscribe( inst );
        
        if ( book == null ) {
            return null;
        }
        
        Set<T> books = getBookSet( callback );
        books.add( book );
        
        @SuppressWarnings( "unchecked" )
        SingleOwnerListenerContext<T> ctx = (SingleOwnerListenerContext<T>) book.getContext();
        
        if ( ctx == null ) {
            ctx = new SingleOwnerListenerContext<T>( this );
            book.setContext( ctx );
        }
        
        if ( ctx.getOwner() != this ) {
            /**
             * cannot have a book shared across multiple subscribers (bookSrc's) as the subscription chain could cause
             * threading errors with multiple subscribers invoking the same listener concurrently
             * shouldnt be possible as the bookSrc owns the book 
             */
            throw new SMTRuntimeException( "ERROR MORE BOOK SUBSCRIBED TO ACROSS MULTIPLE SUBSCRIBERS, " + 
                                           "secDesc=" + inst.getSecurityDesc() + ", ric=" + inst.getRIC() );
        }
        
        ctx.addListener( callback );
        
        return book;
    }

    @Override
    public void clear() {
        for( Map.Entry<BookListener<T>, LinkedHashSet<T>> entry : _subscribersBooks.entrySet() ) {
            Set<T> books = entry.getValue();
            
            for( T book : books ) {
                @SuppressWarnings( "unchecked" )
                SingleOwnerListenerContext<T> ctx = (SingleOwnerListenerContext<T>) book.getContext();
                if ( ctx != null ) {
                    ctx.clear();
                }
                book.setContext( null );
            }
        }
        _subscribersBooks.clear();
    }

    @Override
    public void changed( T book ) {
        @SuppressWarnings( "unchecked" )
        SingleOwnerListenerContext<T> ctx = (SingleOwnerListenerContext<T>) book.getContext();
        if ( ctx != null ) {
            final BookListener<T>[]  listener = ctx._listeners;
            final int size = listener.length;
            
            for( int i = 0 ;  i < size ; i++ ) {
                listener[i].changed( book );
            }
        }
    }

    LinkedHashSet<T> getBookSet( BookListener<T> callback ) {
        LinkedHashSet<T> books = _subscribersBooks.get( callback );
        if ( books == null ) {
            books = new LinkedHashSet<T>();
            _subscribersBooks.put( callback, books );
        }
        return books;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}
