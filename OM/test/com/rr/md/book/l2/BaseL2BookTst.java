/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.rr.core.lang.Constants;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Currency;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.core.model.book.DoubleSidedBookEntryImpl;
import com.rr.core.model.book.UnsafeL2Book;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.inst.FixInstrumentLoader;
import com.rr.inst.InstrumentStore;
import com.rr.inst.SingleExchangeInstrumentStore;
import com.rr.md.us.cme.CMEBookAdapter;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapEntryImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;
import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.MDUpdateAction;
import com.rr.om.BaseOMTestCase;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.model.instrument.InstrumentWrite;

public abstract class BaseL2BookTst extends BaseOMTestCase {

    protected static InstrumentWrite _inst = new DummyInstrumentLocator().getInstrument( new ViewString( "BT.TST" ), SecurityIDSource.RIC,
                                                                                         new ViewString( "TST" ), new ViewString( "TST" ), Currency.EUR );

    protected CMEBookAdapter    _book;
    protected EventRecycler     _entryRecycler = TLC.instance().getInstanceOf( EventRecycler.class );
    protected InstrumentStore   _instrumentLocator;

    private Map<Instrument, AtomicInteger> _secToSeqNumMap    = new HashMap<Instrument, AtomicInteger>(16, 0.75f);
    private Map<Instrument, AtomicInteger> _secToBookSeqNumMap = new HashMap<Instrument, AtomicInteger>(16, 0.75f);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        loadExchanges();
        
        Exchange e = ExchangeManager.instance().getByREC( new ViewString("2") );
        
        _instrumentLocator = new SingleExchangeInstrumentStore( e, 1000 );
        
        String instFile = getInstFile();
        FixInstrumentLoader loader = new FixInstrumentLoader( _instrumentLocator );
        loader.loadFromFile( instFile, e.getRecCode() );
        
        ApiMutatableBook base = new UnsafeL2Book( _inst, 5 );

        _book = new CMEBookAdapter( base );
    }

    protected String getInstFile() {
        return "./data/cme/algo_secdef.dat";
    }

    protected void applyEvent( CMEBookAdapter book, MDIncRefreshImpl event ) {
        
        MDEntryImpl next = (MDEntryImpl) event.getMDEntries();
        
        while( next != null ) {
            book.applyIncrementalEntry( event.getMsgSeqNum(), next );
            
            next = next.getNext();
        }
    }

    protected void applyEvent( CMEBookAdapter book, MDSnapshotFullRefreshImpl event ) {
        
        book.applySnapshot( event, _entryRecycler );
    }

    protected void addMDEntry( MDSnapshotFullRefreshImpl event, int lvl, int qty, double px, MDEntryType type ) {
        MDSnapEntryImpl entry = makeEntry( event, lvl, qty, px, type );
        MDSnapEntryImpl root = (MDSnapEntryImpl) event.getMDEntries();

        if ( root == null ) {
            event.setMDEntries( entry );
        } else {
            while( root.getNext() != null ) {
                root = root.getNext();
            }

            root.setNext( entry );
        }

        event.setNoMDEntries( event.getNoMDEntries() + 1 );
    }

    protected void add( MDIncRefreshImpl event, int lvl, MDUpdateAction action, int qty, double px, MDEntryType type, int bookSeqNum ) {
        addMDEntry( 0, event, lvl, action, qty, px, type, bookSeqNum );
    }

    protected void addMDEntry( Instrument inst, MDIncRefreshImpl event, int lvl, MDUpdateAction action, int qty, double px, MDEntryType type ) {
        addMDEntry( inst.getLongSymbol(), event, lvl, action, qty, px, type, nextBookSeqNum( inst ) );
    }

    protected void addMDEntry( long secId, MDIncRefreshImpl event, int lvl, MDUpdateAction action, int qty, double px, MDEntryType type, int bookSeqNum ) {
        MDEntryImpl entry = makeEntry( secId, event, lvl, action, qty, px, type, bookSeqNum );
        MDEntryImpl root = (MDEntryImpl) event.getMDEntries();

        if ( root == null ) {
            event.setMDEntries( entry );
        } else {
            while( root.getNext() != null ) {
                root = root.getNext();
            }

            root.setNext( entry );
        }

        event.setNoMDEntries( event.getNoMDEntries() + 1 );
    }

    protected MDSnapEntryImpl makeEntry( MDSnapshotFullRefreshImpl event, int lvl, int qty, double px, MDEntryType type ) {
        MDSnapEntryImpl entry = new MDSnapEntryImpl();
        entry.setMdEntryPx( px );
        entry.setMdEntrySize( qty );
        entry.setMdPriceLevel( lvl+1 );
        entry.setMdEntryType( type );
        return entry;
    }

    protected MDEntryImpl makeEntry( long securityId, MDIncRefreshImpl event, int lvl, MDUpdateAction action, int qty, double px, MDEntryType type, int bookSeqNum ) {
        MDEntryImpl entry = makeEntry( event, lvl, action, qty, px, type, bookSeqNum );
        
        entry.setSecurityID( securityId );
        entry.setSecurityIDSource( SecurityIDSource.ExchangeSymbol );

        return entry;
    }

    protected MDEntryImpl makeEntry( MDIncRefreshImpl event, int lvl, MDUpdateAction action, int qty, double px, MDEntryType type, int bookSeqNum ) {
        MDEntryImpl entry = new MDEntryImpl();
        entry.setMdEntryPx( px );
        entry.setMdEntrySize( qty );
        entry.setMdUpdateAction( action );
        entry.setMdPriceLevel( lvl+1 );
        entry.setMdEntryType( type );
        entry.setRepeatSeq( bookSeqNum );
        return entry;
    }

    protected MDSnapshotFullRefreshImpl getSnapEvent( int seqNum, int repSet, int securityId ) {
        MDSnapshotFullRefreshImpl event = getSnapEvent( seqNum, repSet );
        event.setSecurityID( securityId );
        event.setSecurityIDSource( SecurityIDSource.ExchangeSymbol );
        return event;
    }

    protected MDSnapshotFullRefreshImpl getSnapEvent( Instrument inst ) {
        int seqNum = nextSeqNum( inst ); 
        int repSet = nextBookSeqNum( inst );
        
        MDSnapshotFullRefreshImpl event = getSnapEvent( seqNum, repSet );
        event.setSecurityID( inst.getLongSymbol() );
        event.setSecurityIDSource( SecurityIDSource.ExchangeSymbol );
        event.setNoMDEntries( 0 );
        return event;
    }

    private int nextSeqNum( Instrument inst ) {
        AtomicInteger ia = _secToSeqNumMap.get( inst );
        
        if ( ia == null ) {
            ia = new AtomicInteger(100);
            
            _secToSeqNumMap.put( inst, ia );
        }
        
        return ia.incrementAndGet();
    }
    
    private int nextBookSeqNum( Instrument inst ) {
        AtomicInteger ia = _secToBookSeqNumMap.get( inst );
        
        if ( ia == null ) {
            ia = new AtomicInteger(1000);
            
            _secToBookSeqNumMap.put( inst, ia );
        }
        
        return ia.incrementAndGet();
    }
    
    protected MDSnapshotFullRefreshImpl getSnapEvent( int seqNum, int repSet ) {
        MDSnapshotFullRefreshImpl event = new MDSnapshotFullRefreshImpl();
        event.setNoMDEntries( 0 );
        event.setMsgSeqNum( seqNum );
        event.setRptSeq( repSet );
        return event;
    }

    protected MDIncRefreshImpl getBaseEvent( int seqNum ) {
        MDIncRefreshImpl event = new MDIncRefreshImpl();
        event.setNoMDEntries( 0 );
        event.setMsgSeqNum( seqNum );
        return event;
    }

    protected MDIncRefreshImpl getBaseEvent( Instrument inst ) {
        MDIncRefreshImpl event = new MDIncRefreshImpl();
        event.setNoMDEntries( 0 );
        event.setMsgSeqNum( nextSeqNum( inst ) );
        return event;
    }

    protected void verify( CMEBookAdapter verBook, double[][] results ) {
        UnsafeL2Book snappedBook = new UnsafeL2Book( _inst, 10 );
        verBook.snap( snappedBook );

        int numLevels = results.length;

        assertTrue( verBook.getActiveLevels() >= numLevels );

        DoubleSidedBookEntry entry = new DoubleSidedBookEntryImpl();

        for ( int l = 0 ; l < numLevels ; l++ ) {
            boolean ok = snappedBook.getLevel( l, entry );

            assertTrue( ok );
            assertTrue( results[l].length == 4 );

            assertEquals( results[l][0], entry.getBidQty(), Constants.TICK_WEIGHT );
            assertEquals( results[l][1], entry.getBidPx(),  Constants.TICK_WEIGHT );
            assertEquals( results[l][2], entry.getAskQty(), Constants.TICK_WEIGHT );
            assertEquals( results[l][3], entry.getAskPx(),  Constants.TICK_WEIGHT );
        }
    }
    
    protected List<Instrument> createInsts( String instList ) {
        List<Instrument> insts = new ArrayList<Instrument>();
        
        String[] instSecDesc = instList.split( "," );
        
        for( String secDes : instSecDesc ) {
            Instrument inst = _instrumentLocator.getInstrumentBySecurityDesc( new ViewString(secDes.trim()) );
            
            if ( inst == null ) {
                throw new SMTRuntimeException( "Failed to locate instrument [" + secDes + "]" );
            }
                
            insts.add( inst );
        }
        
        return insts;
    }
}
