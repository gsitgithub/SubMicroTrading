/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.book;

import com.rr.core.collections.IntHashMap;
import com.rr.core.model.Book;

/**
 * Non thread safe BookFactory using int as key
 *
 * @author Richard Rose
 */
public final class MapIntBookFactory implements IntBookFactory {

    private final IntHashMap<Book>     _map;
    private final BookFactory<Integer> _bookFactory; 
    
    public MapIntBookFactory( int numSymbols, BookFactory<Integer> bookFactory ) {
        _map = new IntHashMap<Book>( numSymbols, 0.75f );
        _bookFactory = bookFactory;
    }

    @Override
    public Book find( int id ) {
        Book book = _map.get( id );
        
        if ( book == null ) {
            book = _bookFactory.create( new Integer(id) );
            
            _map.put( id, book );
        }
        
        return book;
    }
    
}
