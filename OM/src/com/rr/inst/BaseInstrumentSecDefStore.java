/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.rr.core.collections.LongHashMap;
import com.rr.core.collections.LongMap;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ZString;
import com.rr.core.model.Currency;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.events.impl.SecDefLegImpl;
import com.rr.model.generated.internal.events.impl.SecurityAltIDImpl;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.model.generated.internal.events.impl.SecurityStatusImpl;
import com.rr.model.generated.internal.events.recycle.SecurityDefinitionRecycler;
import com.rr.model.generated.internal.events.recycle.SecurityStatusRecycler;
import com.rr.model.generated.internal.type.SecurityTradingStatus;
import com.rr.om.dummy.warmup.DummyInstrument;

/**
 * thread safe instrument store
 * 
 * will BLOCK on updates ... for intraday updates of instruments use more concurrent version
 *
 * @author Richard Rose
 */
public abstract class BaseInstrumentSecDefStore implements InstrumentStore {

    final Map<ZString,InstrumentSecurityDefWrapper> _ricToInstMap;
    final Map<ZString,InstrumentSecurityDefWrapper> _secDescToInstMap;

    class Indexes {
        private final LongMap<InstrumentSecurityDefWrapper>     _idToMap;
        private final Map<ZString,InstrumentSecurityDefWrapper> _symToInstMap;
        private final Map<ZString,InstrumentSecurityDefWrapper> _exchangeSymToInstMap;

        public Indexes( int preSize ) {
            _idToMap              = new LongHashMap<InstrumentSecurityDefWrapper>( preSize, 0.75f );
            _symToInstMap         = new HashMap<ZString,InstrumentSecurityDefWrapper>( preSize, 0.75f );
            _exchangeSymToInstMap = new HashMap<ZString,InstrumentSecurityDefWrapper>( preSize, 0.75f );
        }

        public Map<ZString,InstrumentSecurityDefWrapper> getSymbolIndex() {
            return _symToInstMap;
        }
        
        public Map<ZString,InstrumentSecurityDefWrapper> getIndex( SecurityIDSource src ) {
            if ( src == null ) {
                return _exchangeSymToInstMap;
            }
            
            switch( src ) {
            case RIC:
                return _ricToInstMap;
            case ExchangeSymbol:
                return _exchangeSymToInstMap;
            case SEDOL:
            case CUSIP:
            case ISIN:
            case Belgian:
            case Bloomberg:
            case CTA:
            case ClearingHouse:
            case Common:
            case Dutch:
            case ISDA_FPML:
            case ISO_CcyCode:
            case ISO_CountryCode:
            case OptionsPRA:
            case QUIK:
            case Sicovam:
            case Unknown:
            case Valoren:
            case Wertpapier:
            default:
                break;
            }
            
            return null;
        }

        public LongMap<InstrumentSecurityDefWrapper> getIdToMap() {
            return _idToMap;
        }

        public Map<ZString, InstrumentSecurityDefWrapper> getSecurityDescIndex() {
            return _secDescToInstMap;
        }
    }
    
    private SecurityDefinitionRecycler _securityDefinitionRecycler;
    private SecurityStatusRecycler     _securityStatusRecycler;

    private final int       _preSize;
    private final String    _id;
    
    public BaseInstrumentSecDefStore( String id, int preSize ) {
        _id = id;
        _preSize = preSize;

        _ricToInstMap         = new HashMap<ZString,InstrumentSecurityDefWrapper>( preSize, 0.75f );
        _secDescToInstMap     = new HashMap<ZString,InstrumentSecurityDefWrapper>( preSize, 0.75f );
                
        SuperpoolManager sp         = SuperpoolManager.instance();
        _securityDefinitionRecycler = sp.getRecycler( SecurityDefinitionRecycler.class, SecurityDefinitionImpl.class );
        _securityStatusRecycler     = sp.getRecycler( SecurityStatusRecycler.class, SecurityStatusImpl.class );
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    public void addExchange( Exchange ex, int preSize ) {
        getExchangeMap( ex, preSize );
    }

    @Override
    public Instrument getInstrumentBySecurityDesc( ZString securityDescription ) {
        return _secDescToInstMap.get( securityDescription );
    }
    
    @Override
    public void remove( SecurityDefinitionImpl def, ZString rec ) {
        long secId = def.getSecurityID();
        
        Exchange ex = getExchange( rec, null ); 
        
        Indexes indexes = getExchangeMap( ex, _preSize );

        LongMap<InstrumentSecurityDefWrapper> idToInstMap = indexes.getIdToMap();
        
        InstrumentSecurityDefWrapper inst = idToInstMap.get( secId );
        
        if ( inst != null ) {
            removeInstFromSupplementaryIndexes( inst, indexes ); // BEFORE KEYS RECYCLED
            SecurityDefinitionImpl oldDef = inst.getSecurityDefinition();
            if ( oldDef != null ) _securityDefinitionRecycler.recycle( oldDef );
            inst.setSecurityDefinition( def );
        }
    }
    
    @Override
    public void add( SecurityDefinitionImpl def, ZString rec ) {

        long secId = def.getSecurityID();
        
        Exchange ex = getExchange( rec, null ); 
        
        Indexes indexes = getExchangeMap( ex, _preSize );

        LongMap<InstrumentSecurityDefWrapper> idToInstMap = indexes.getIdToMap();
        
        InstrumentSecurityDefWrapper inst = idToInstMap.get( secId );
        
        if ( inst == null ) {
            int legCnt = def.getNoLegs();

            if ( legCnt == Constants.UNSET_INT ) legCnt = 0;
            
            inst = new InstrumentSecurityDefWrapper( ex, def, legCnt );
            idToInstMap.put( secId, inst );
            
            if ( legCnt > 0 ) {
                SecDefLegImpl curLeg = (SecDefLegImpl) def.getLegs();
    
                int legIdx = 0;
                
                while( curLeg != null ) {
                    InstrumentSecurityDefWrapper legInst = idToInstMap.get( curLeg.getLegSecurityID() );

                    if ( legInst == null ) { // create placeholder instrument wrapper, assume secDef will come later
                        
                        legInst = new InstrumentSecurityDefWrapper( ex, null, 0 );

                        legInst.setPlaceHolderDefinition( curLeg );

                        idToInstMap.put( curLeg.getLegSecurityID(), legInst );
                    }

                    curLeg.setInstrument( legInst );
                    
                    inst.setLeg( legIdx++, curLeg );
                    
                    SecDefLegImpl prevLeg = curLeg;
                    curLeg = curLeg.getNext();
                    prevLeg.setNext( null );
                }
                
                def.setLegs( null ); // POTENTIAL GC ON MASS INST UPDATE
            }
            
        } else {
            removeInstFromSupplementaryIndexes( inst, indexes ); // BEFORE KEYS RECYCLED
            SecurityDefinitionImpl oldDef = inst.getSecurityDefinition();
            if ( oldDef != null ) _securityDefinitionRecycler.recycle( oldDef );
            inst.setSecurityDefinition( def );
        }

        addInstToSupplementaryIndexes( inst, indexes );
    }

    private void removeInstFromSupplementaryIndexes( InstrumentSecurityDefWrapper inst, Indexes indexes ) {
        SecurityDefinitionImpl def = inst.getSecurityDefinition();
        
        if ( def != null ) {
            SecurityAltIDImpl securityAltIDs = (SecurityAltIDImpl) def.getSecurityAltIDs();
            
            while( securityAltIDs != null ) {
                
                Map<ZString, InstrumentSecurityDefWrapper> indexMap = indexes.getIndex( securityAltIDs.getSecurityAltIDSource() );
                
                ZString id = securityAltIDs.getSecurityAltID();
                
                if ( id.length() > 0 ) {
                    indexMap.remove( securityAltIDs.getSecurityAltID() );
                }
                
                securityAltIDs = securityAltIDs.getNext();
            }
        }
        
        indexes.getSymbolIndex().remove( inst.getExchangeSymbol() );
    }

    private void addInstToSupplementaryIndexes( InstrumentSecurityDefWrapper inst, Indexes indexes ) {
        SecurityDefinitionImpl def = inst.getSecurityDefinition();
        
        SecurityAltIDImpl securityAltIDs = (SecurityAltIDImpl) def.getSecurityAltIDs();
        
        while( securityAltIDs != null ) {
            
            Map<ZString, InstrumentSecurityDefWrapper> indexMap = indexes.getIndex( securityAltIDs.getSecurityAltIDSource() );
            
            ZString id = securityAltIDs.getSecurityAltID();
            
            if ( id.length() > 0 ) {
                indexMap.put( securityAltIDs.getSecurityAltID(), inst );
            }
            
            securityAltIDs = securityAltIDs.getNext();
        }

        indexes.getIndex( SecurityIDSource.ExchangeSymbol ).put( inst.getExchangeSymbol(), inst );

        indexes.getSymbolIndex().put( inst.getExchangeSymbol(), inst );

        indexes.getSecurityDescIndex().put( inst.getSecurityDefinition().getSecurityDesc(), inst );
    }

    @Override
    public boolean updateStatus( SecurityStatusImpl status ) {
        Exchange ex = getExchange( status.getSecurityExchange(), null );
        
        Indexes indexes = getExchangeMap( ex, _preSize );

        LongMap<InstrumentSecurityDefWrapper> idToInstMap = indexes.getIdToMap();
        
        InstrumentSecurityDefWrapper inst = idToInstMap.get( status.getSecurityID() );
        
        boolean changed = false;
        
        if ( inst != null ) {
            SecurityTradingStatus curSt  = inst.getSecurityTradingStatus();
            SecurityTradingStatus newSt =status.getSecurityTradingStatus();
            
            if ( curSt != newSt ) {
                inst.setSecurityTradingStatus( newSt );
                changed = true;
            }
        }
        
        _securityStatusRecycler.recycle( status );
        
        return changed;
    }
    
    @Override
    public Instrument getInstrument( ZString securityId, SecurityIDSource securityIDSource, ZString exDest, ZString securityExchange, Currency currency ) {

        Exchange ex = getExchange( exDest, securityExchange ); 

        Indexes indexes = getExchangeMap( ex, _preSize );

        if ( indexes != null ) {
            Map<ZString, InstrumentSecurityDefWrapper> secMap = indexes.getIndex( securityIDSource );
            
            return secMap.get( securityId );
        }
            
        return null;
    }

    @Override
    public Instrument getInstrumentByRIC( ZString ric ) {
        return _ricToInstMap.get( ric );
    }

    @Override
    public Instrument getInstrumentBySymbol( ZString symbol, ZString rec, ZString securityExchange, Currency clientCcy ) {

        Exchange ex = getExchange( rec, securityExchange ); 

        Indexes indexes = getExchangeMap( ex, _preSize );

        if ( indexes != null ) {
            Map<ZString, InstrumentSecurityDefWrapper> symMap = indexes.getSymbolIndex();
            
            Instrument inst = symMap.get( symbol );
            
            if ( inst == null ) {
                inst = indexes.getSecurityDescIndex().get( symbol );
            }
            
            return inst;
        }
            
        return null;
    }

    @Override
    public Instrument getInstrumentByID( ZString rec, long instrumentId ) {

        if ( instrumentId == Instrument.DUMMY_INSTRUMENT_ID ) return DummyInstrument.DUMMY;
        
        Exchange ex = getExchange( rec, null );

        Indexes indexes = getExchangeMap( ex, _preSize );

        if ( indexes != null ) {
            LongMap<InstrumentSecurityDefWrapper> idToInstMap = indexes.getIdToMap();
            
            return idToInstMap.get( instrumentId );
        }
        
        return null;
    }
    
    @Override
    public Instrument getDummyInstrument() {
        return DummyInstrument.DUMMY;
    }

    @Override
    public boolean allowIntradayAddition() {
        return false;
    }

    @Override
    public void getInstruments( Set<Instrument> instruments, Exchange ex ) {

        Indexes indexes = getExchangeMap( ex, _preSize );
        
        Collection<InstrumentSecurityDefWrapper> insts = indexes.getSymbolIndex().values();

        instruments.addAll( insts );
    }
    
    protected abstract Indexes getExchangeMap( Exchange ex, int preSize );
    protected abstract Exchange getExchange( ZString rec, ZString altRec );
}
