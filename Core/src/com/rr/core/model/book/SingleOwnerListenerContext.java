/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

import com.rr.core.model.Book;
import com.rr.core.model.BookContext;
import com.rr.core.model.BookListener;
import com.rr.core.model.BookReserver;
import com.rr.core.utils.Utils;

/**
 * book context, used to register book specific listeners
 * 
 * has a single owner, so designed for by single book consumer
 *
 * @author Richard Rose
 * @param <T>
 */
public final class SingleOwnerListenerContext<T extends Book> implements BookContext {

    private final Object _owner;
    
    // dont sync read access
    public BookListener<T>[] _listeners;

    private BookReserver _bidReserver = new SafeBookReserver();
    private BookReserver _askReserver = new SafeBookReserver();

    @SuppressWarnings( "unchecked" )
    public SingleOwnerListenerContext( Object owner ) {
        _owner = owner;
        _listeners = new BookListener[ 0 ];
    }

    public Object getOwner() {
        return _owner;
    }
    
    public void addListener( BookListener<T> listener ) {
        _listeners = Utils.arrayCopyAndAddEntry( _listeners, listener );
    }
    
    public BookListener<T>[] getListeners() {
        return _listeners;
    }

    @SuppressWarnings( "unchecked" )
    public void clear() {
        _listeners = new BookListener[ 0 ];
    }

    @Override
    public BookReserver getBidBookReserver() {
        return _bidReserver;
    }

    @Override
    public BookReserver getAskBookReserver() {
        return _askReserver;
    }
}
