/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.strats;

import java.util.concurrent.atomic.AtomicBoolean;

import com.rr.core.algo.strats.Strategy.StratBookAdapter;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;


public class L2BookHandler<T extends Book> implements StratBookAdapter<T> {

    private static final Logger _log = LoggerFactory.create( L2BookHandler.class );
    
    private final AtomicBoolean         _changed = new AtomicBoolean( false );
    
    private final String                            _id;
    private final BaseL2BookStrategy                _base;
    private final StratInstrumentStateWrapper<T>    _state;
    private final boolean                           _trace;
    private final ReusableString                    _traceMsg = new ReusableString();

    /**
     * _exh the exchange handler to be passed to the ExchangeRouter with orders for this book, ensures correct handler invoked by exchange replies
     */
    private       T                  _book;

    public L2BookHandler( String id, int idx, BaseL2BookStrategy baseL2BookStrategy, StratInstrumentStateWrapper<T> state, boolean trace ) {
        _id = id;
        _base = baseL2BookStrategy;
        _state = state;
        _trace = trace;
    }

    @Override
    public String id() {
        return _id;
    }

    @Override
    public void clear() {
        _changed.set( false );
    }

    @Override
    public T getBook() {
        return _book;
    }

    @Override
    public StratInstrumentStateWrapper<T> getStratInstState() {
        return _state;
    }

    @Override
    public void changed( T book ) {
        _changed.set( true );
    }

    @Override
    public void doWorkUnit() {
        if ( _changed.get() ) {
            processBookChange();
        }
    }

    private void processBookChange() {
        if ( _trace ) {
            _traceMsg.copy( _base.getComponentId() );
            _traceMsg.append( " BookChanged " );
            _book.dump( _traceMsg );
            
            _log.info( _traceMsg );
        }
        
        _base.bookChanged( _state );
        
        _changed.set( false );
    }

    @Override
    public void setBook( T book ) {
        _book = book;
    }
}
