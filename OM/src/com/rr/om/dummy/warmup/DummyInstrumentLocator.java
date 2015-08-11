/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.dummy.warmup;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.rr.core.collections.LongHashMap;
import com.rr.core.collections.LongMap;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Currency;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.model.SecurityIDSource;
import com.rr.inst.InstrumentStore;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.model.generated.internal.events.impl.SecurityStatusImpl;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.model.instrument.InstrumentWrite;

// DO NOT USE IN PROD
public final class DummyInstrumentLocator implements InstrumentStore {

//    public static DummyInstrumentLocator _instance = new DummyInstrumentLocator();
//
//    public static DummyInstrumentLocator instance() { return _instance; }
    
    private final Exchange _overrideExchange;
    
    public DummyInstrumentLocator() {
        this( null, null );
    }
    
    public DummyInstrumentLocator( String id ) {
        this( id, null );
    }

    public DummyInstrumentLocator( Exchange ex ) {
        this( null, ex );
    }
    
    public DummyInstrumentLocator( String id, Exchange ex ) {
        _overrideExchange = ex;
        
        _id = (id == null) ? "DummyInstLoader" : id;
        
        storeInstrument( DummyInstrument.DUMMY );
    }

    private final Map<ViewString,DummyInstrument> _instruments     = new HashMap<ViewString,DummyInstrument>( 1024 );
    private final LongMap<DummyInstrument>        _instrumentsById = new LongHashMap<DummyInstrument>( 1024, 0.75f );
    private final String _id;
    
    @Override
    public InstrumentWrite getInstrument( ZString       securityId, 
                                          SecurityIDSource securityIDSource, 
                                          ZString       exDest, 
                                          ZString       securityExchange, 
                                          Currency         currency ) {
        
        DummyInstrument inst = _instruments.get( securityId );
        
        if ( inst != null ) return inst;

        if ( securityIDSource == SecurityIDSource.RIC || securityIDSource == SecurityIDSource.SEDOL ) {
            return addInst( securityId, exDest, securityExchange, currency );
        }

        if ( securityIDSource == SecurityIDSource.ExchangeSymbol ) {
            return addInst( securityId, exDest, securityExchange, currency );
        }
        
        throw new RuntimeException( "DummyInstrumentLocator doesnt support securityIdSource=" + securityIDSource );
    }

    private void storeInstrument( DummyInstrument inst ) {
        _instruments.put( inst.getRIC(), inst );
        _instrumentsById.put( inst.getLongSymbol(), inst );
    }

    @Override
    public Instrument getInstrumentByRIC( ZString ric ) {
        DummyInstrument inst = _instruments.get( ric );

        if ( inst == null ) {
            inst = addInst( ric );
        }
        
        return inst;
    }

    @Override
    public Instrument getInstrumentByID( ZString rec, long instrumentId ) {
        DummyInstrument inst = _instrumentsById.get( instrumentId );

        if ( inst == null ) {
            inst = addInst( rec, instrumentId );
        }
        
        return inst;
    }

    @Override
    public Instrument getInstrumentBySecurityDesc( ZString securityDescription ) {
        DummyInstrument inst = _instruments.get( securityDescription );

        if ( inst == null ) {
            inst = addInst( securityDescription );
        }
        
        return inst;
    }

    @Override
    public Instrument getInstrumentBySymbol( ZString symbol, ZString exDest, ZString securityExchange, Currency currency ) {
        DummyInstrument inst = _instruments.get( symbol );

        if ( inst == null ) {
            inst = addInst( symbol, exDest, securityExchange, currency );
        }
        
        return inst;
    }

    private DummyInstrument addInst( ZString rec, long instrumentId ) {
        DummyInstrument inst;
        Exchange exchange = ExchangeManager.instance().getByREC( rec );
        
        if ( exchange == null ) {
            exchange = DummyExchangeManager.instance().get( new ReusableString(rec) );
        }

        ReusableString ric = new ReusableString( 8 );
        ric.append( instrumentId ).append( '.' ).append( rec );
        
        Currency ccy = Currency.GBP;
        
        if ( rec.equals( "2" ) ) {
            ccy = Currency.USD;
        }
        
        inst = new DummyInstrument( ric, exchange, ccy, false, (int)instrumentId );
        
        storeInstrument( inst );
        return inst;
    }

    private DummyInstrument addInst( ZString securityId, ZString exDest, ZString securityExchange, Currency currency ) {
        DummyInstrument inst;
        ZString ex = (exDest.length() > 0) ? exDest : securityExchange;

        Exchange exchange;
        
        if ( _overrideExchange != null ) {
            exchange = _overrideExchange;
        } else {
            exchange = ExchangeManager.instance().getByREC( ex );
        }
        
        if ( exchange == null ) {
            exchange = DummyExchangeManager.instance().get( ex );
        }

        if ( currency == null ) {
            currency = Currency.GBP;
        }
        
        inst = new DummyInstrument( securityId, exchange, currency );
        
        storeInstrument( inst );
        
        return inst;
    }

    private DummyInstrument addInst( ZString ric ) {
        DummyInstrument inst;
        String ex = "L";
        
        int idx = ric.indexOf( '.' );
        
        if ( idx > -1 )  {
            ex = ric.toString().substring( idx+1 );
        }
        
        Exchange exchange = DummyExchangeManager.instance().get( new ReusableString(ex) );

        inst = new DummyInstrument( ric, exchange, Currency.GBP );
        
        storeInstrument( inst );
        return inst;
    }

    @Override
    public boolean allowIntradayAddition() {
        return true;
    }

    @Override
    public void add( SecurityDefinitionImpl def, ZString rec ) {
        // nothing
    }

    @Override
    public boolean updateStatus( SecurityStatusImpl status ) {
        return false;
    }
    
    @Override
    public Instrument getDummyInstrument() {
        return DummyInstrument.DUMMY;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void getInstruments( Set<Instrument> instruments, Exchange ex ) {
        instruments.addAll( _instruments.values() );
    }

    @Override
    public void remove( SecurityDefinitionImpl def, ZString rec ) {
        // nothing
    }
}
