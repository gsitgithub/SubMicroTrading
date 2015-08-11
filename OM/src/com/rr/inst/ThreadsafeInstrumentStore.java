/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.rr.core.lang.ZString;
import com.rr.core.model.Currency;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.model.SecurityIDSource;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.model.generated.internal.events.impl.SecurityStatusImpl;

/**
 * thread safe instrument store
 * 
 * provides spin locking for thread safety to a supplied instrument store
 * 
 * @WARN Locking is NON re-entrant
 * 
 * will BLOCK on updates ... for intraday updates of instruments use more concurrent version
 */

public final class ThreadsafeInstrumentStore implements InstrumentStore {

    private final AtomicBoolean   _lock = new AtomicBoolean( false );
    private final InstrumentStore _store;

    public ThreadsafeInstrumentStore( InstrumentStore store ) {
        _store = store;
    }
    
    @Override
    public String getComponentId() {
        return _store.getComponentId(); // as this is a wrapper return id of the wrapped component, only one of which should be in comp. registry
    }
    
    @Override
    public Instrument getInstrument( ZString securityId, SecurityIDSource securityIDSource, ZString exDest, ZString securityExchange, Currency currency ) {
        try {
            grabLock();

            return _store.getInstrument( securityId, securityIDSource, exDest, securityExchange, currency );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public Instrument getInstrumentByRIC( ZString ric ) {
        try {
            grabLock();

            return _store.getInstrumentByRIC( ric );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public Instrument getInstrumentBySecurityDesc( ZString securityDescription ) {
        try {
            grabLock();

            return _store.getInstrumentBySecurityDesc( securityDescription );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public Instrument getInstrumentBySymbol( ZString symbol, ZString exDest, ZString securityExchange, Currency clientCcy ) {
        try {
            grabLock();

            return _store.getInstrumentBySymbol( symbol, exDest, securityExchange, clientCcy );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public Instrument getInstrumentByID( ZString rec, long instrumentId ) {
        try {
            grabLock();

            return _store.getInstrumentByID( rec, instrumentId );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public Instrument getDummyInstrument() {
        return _store.getDummyInstrument();
    }

    @Override
    public boolean allowIntradayAddition() {
        return true;
    }

    @Override
    public void add( SecurityDefinitionImpl def, ZString rec ) {
        try {
            grabLock();

            _store.add( def, rec );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public boolean updateStatus( SecurityStatusImpl status ) {
        try {
            grabLock();

            return _store.updateStatus( status );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public void getInstruments( Set<Instrument> instruments, Exchange ex ) {
        try {
            grabLock();

            _store.getInstruments( instruments, ex );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public void remove( SecurityDefinitionImpl def, ZString rec ) {
        try {
            grabLock();

            _store.remove( def, rec );
            
        } finally {
            releaseLock();
        }
    }

    private void grabLock() {
        while( !_lock.compareAndSet( false, true ) ) {
            // spin
        }
    }

    private void releaseLock() {
        _lock.set( false );
    }
}
