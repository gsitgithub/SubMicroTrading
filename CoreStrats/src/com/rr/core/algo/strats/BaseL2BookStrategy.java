/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.strats;

import com.rr.core.model.Book;


public abstract class BaseL2BookStrategy extends BaseStrategy<Book> {

    public BaseL2BookStrategy( String id ) {
        super( id );
    }

    @Override
    protected StratBookAdapter<Book> createBookListener( String id, int idx, StratInstrumentStateWrapper<Book> state ) {
        return new L2BookHandler<Book>( id, idx, this, state, isTrace() );
    }
}
