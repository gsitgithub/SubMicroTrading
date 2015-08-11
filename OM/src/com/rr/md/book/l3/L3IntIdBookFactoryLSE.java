/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l3;

import com.rr.core.book.BookFactory;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.model.InstrumentLocator;

// @TODO refactor out the hardcoded LSE ref

public class L3IntIdBookFactoryLSE implements BookFactory<Integer> {

    private static final int DEFAULT_PRESIZE_ORDERS = 100;
    
    private final int _presizeOrders; // @TODO persist orders per symbol basis

    private static final ThreadLocal<LongBookPoolMgr<OrderBookEntry>>  _poolMgrLocal = new ThreadLocal<LongBookPoolMgr<OrderBookEntry>>() {
        @Override
        public LongBookPoolMgr<OrderBookEntry> initialValue() {
            return new LongBookPoolMgr<OrderBookEntry>();
        }
    };

    private static       InstrumentLocator    _instrumentLocator;
    private static final ViewString           _lseREC = new ViewString( "L" );

    
    public static void setInstrumentLocator( InstrumentLocator locator ) {
        _instrumentLocator = locator;
    }
    
    public L3IntIdBookFactoryLSE() {
        this( DEFAULT_PRESIZE_ORDERS );
    }

    public L3IntIdBookFactoryLSE( int presizeOrders ) {
        _presizeOrders = presizeOrders;
    }

    @Override
    public Book create( Integer key ) {
        Instrument inst = _instrumentLocator.getInstrumentByID( _lseREC, key.longValue() );

        return new FullL3OrderBook( inst, _presizeOrders, _poolMgrLocal.get() );
    }

    @Override
    public Book create( Integer key, int maxLevels ) {
        return create( key );
    }
}
