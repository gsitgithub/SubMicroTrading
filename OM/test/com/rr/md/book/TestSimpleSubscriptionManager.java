/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rr.core.component.SMTStartContext;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Book;
import com.rr.core.model.BookListener;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.model.book.SingleOwnerListenerContext;
import com.rr.core.model.book.UnsafeL2Book;
import com.rr.md.us.cme.CMEBookAdapter;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;


public class TestSimpleSubscriptionManager extends BaseTestCase {

    protected DummyInstrumentLocator _loc = new DummyInstrumentLocator();

    protected Instrument getInst( String securityDescription ) {
        return _loc.getInstrumentBySecurityDesc( new ViewString(securityDescription) );
    }

    private final class DummyBookSrc implements BookSource<CMEBookAdapter> {

        Map<Instrument,CMEBookAdapter> _books = new HashMap<Instrument,CMEBookAdapter>();
        
        public DummyBookSrc() {
            // for test
        }

        @Override
        public CMEBookAdapter subscribe( Instrument inst ) {
            CMEBookAdapter book = _books.get( inst );
            if ( book == null ) {
                book = new CMEBookAdapter( new UnsafeL2Book( inst, 0 ));
                
                _books.put( inst, book );
            }
            
            return book;
        }

        @Override public boolean supports( Instrument inst ) { return true; }
        @Override public String getComponentId() { return null; }
        @Override public boolean hasPipeLineId( String pipeLineId ) { return false; }
        @Override public List<String> getPipeLineIds() { return null; }
        @Override public void threadedInit() { /* */ }
        @Override public void handle( Message msg ) { /* */ }
        @Override public void handleNow( Message msg ) { /* */ }
        @Override public boolean canHandle() { return true; }
        @Override public void startWork() { /* nothing */        }
        @Override public void stopWork() { /* nothing */ }
        @Override public void init( SMTStartContext ctx ) { /* nothing */ }
        @Override public void prepare() { /* nothing */ }
    }
    
    private static class Listener implements BookListener<CMEBookAdapter> {

        public List<Book> _changes = new ArrayList<Book>();

        public Listener() {
            // for test
        }

        @Override
        public String id() {
            return null;
        }

        @Override
        public void changed( CMEBookAdapter book ) {
            _changes.add( book );
        }

        @Override
        public void clear() {
            _changes.clear();
        }
    }
    
    private SimpleSubscriberMgr<CMEBookAdapter> _mgr;
    private DummyBookSrc _src;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        _src = new DummyBookSrc();
        _mgr = new SimpleSubscriberMgr<CMEBookAdapter>( "test", _src );
    }
    
    public void testOneBookOneSub() {
        Instrument secDef1 = getInst( "SECA" );
        
        Listener l1 = new Listener();
        _mgr.subscribe( l1, secDef1 );

        CMEBookAdapter[] books = _mgr.getBookSet( l1 ).toArray( new CMEBookAdapter[0] );
        assertEquals( 1, books.length );
        assertSame( _src._books.get(secDef1), books[0] );
        
        _mgr.changed( books[0] );
        
        assertEquals( 1, l1._changes.size() );
        assertSame( books[0], l1._changes.get(0) );

        @SuppressWarnings( "unchecked" )
        SingleOwnerListenerContext<CMEBookAdapter> ctx =  (SingleOwnerListenerContext<CMEBookAdapter>) l1._changes.get( 0 ).getContext();
        
        BookListener<CMEBookAdapter>[] foundListeners = ctx.getListeners();
        
        assertEquals( 1, foundListeners.length );
        assertSame( l1, foundListeners[0] );
    }

    @SuppressWarnings( "unchecked" )
    public void testThreeBookOneSub() {
        Instrument secDef1 = getInst( "SECA" );
        Instrument secDef2 = getInst( "SECB" );
        Instrument secDef3 = getInst( "SECC" );
        
        Listener l1 = new Listener();
        _mgr.subscribe( l1, secDef1 );
        _mgr.subscribe( l1, secDef2 );
        _mgr.subscribe( l1, secDef3 );

        CMEBookAdapter[] books = _mgr.getBookSet( l1 ).toArray( new CMEBookAdapter[0] );
        assertEquals( 3, books.length );
        assertSame( _src._books.get(secDef1), books[0] );
        assertSame( _src._books.get(secDef2), books[1] );
        assertSame( _src._books.get(secDef3), books[2] );
        
        _mgr.changed( books[0] );
        _mgr.changed( books[2] );
        
        assertEquals( 2, l1._changes.size() );
        assertSame( books[0], l1._changes.get(0) );
        assertSame( books[2], l1._changes.get(1) );

        SingleOwnerListenerContext<CMEBookAdapter>  ctx;
        BookListener<CMEBookAdapter>[]                       foundListeners;

        ctx =  (SingleOwnerListenerContext<CMEBookAdapter>) l1._changes.get( 0 ).getContext();
        foundListeners = ctx.getListeners();
        assertEquals( 1, foundListeners.length );
        assertSame( l1, foundListeners[0] );

        ctx =  (SingleOwnerListenerContext<CMEBookAdapter>) l1._changes.get( 1 ).getContext();
        foundListeners = ctx.getListeners();
        assertEquals( 1, foundListeners.length );
        assertSame( l1, foundListeners[0] );
        
        assertNotSame( l1._changes.get( 0 ).getContext(), l1._changes.get( 1 ).getContext() );
    }

    @SuppressWarnings( "unchecked" )
    public void testThreeBookThreeSub() {
        Instrument secDef1 = getInst( "SECA" );
        Instrument secDef2 = getInst( "SECB" );
        Instrument secDef3 = getInst( "SECC" );
        
        Listener l1 = new Listener();
        Listener l2 = new Listener();
        Listener l3 = new Listener();

        _mgr.subscribe( l1, secDef1 );
        _mgr.subscribe( l2, secDef2 );
        _mgr.subscribe( l3, secDef3 );

        CMEBookAdapter[] l1Books = _mgr.getBookSet( l1 ).toArray( new CMEBookAdapter[0] );
        assertEquals( 1, l1Books.length );
        assertSame( _src._books.get(secDef1), l1Books[0] );

        CMEBookAdapter[] l2Books = _mgr.getBookSet( l2 ).toArray( new CMEBookAdapter[0] );
        assertEquals( 1, l2Books.length );
        assertSame( _src._books.get(secDef2), l2Books[0] );

        CMEBookAdapter[] l3Books = _mgr.getBookSet( l3 ).toArray( new CMEBookAdapter[0] );
        assertEquals( 1, l3Books.length );
        assertSame( _src._books.get(secDef3), l3Books[0] );

        _mgr.changed( l1Books[0] );
        _mgr.changed( l3Books[0] );
        
        assertEquals( 1, l1._changes.size() );
        assertEquals( 0, l2._changes.size() );
        assertEquals( 1, l3._changes.size() );
        assertSame( l1Books[0], l1._changes.get(0) );
        assertSame( l3Books[0], l3._changes.get(0) );

        SingleOwnerListenerContext<CMEBookAdapter>  ctx;
        BookListener<CMEBookAdapter>[]                       foundListeners;

        ctx =  (SingleOwnerListenerContext<CMEBookAdapter>) l1._changes.get( 0 ).getContext();
        foundListeners = ctx.getListeners();
        assertEquals( 1, foundListeners.length );
        assertSame( l1, foundListeners[0] );

        ctx =  (SingleOwnerListenerContext<CMEBookAdapter>) l3._changes.get( 0 ).getContext();
        foundListeners = ctx.getListeners();
        assertEquals( 1, foundListeners.length );
        assertSame( l3, foundListeners[0] );
        
        assertNotSame( l1._changes.get( 0 ).getContext(), l3._changes.get( 0 ).getContext() );
    }

    @SuppressWarnings( "unchecked" )
    public void testMultiSub() {
        Instrument secDef1 = getInst( "SECA" );
        Instrument secDef2 = getInst( "SECB" );
        Instrument secDef3 = getInst( "SECC" );
        
        Listener l1 = new Listener();
        Listener l2 = new Listener();
        Listener l3 = new Listener();

        _mgr.subscribe( l1, secDef1 );
        _mgr.subscribe( l1, secDef3 );
        
        _mgr.subscribe( l2, secDef2 );
        _mgr.subscribe( l2, secDef3 );
        
        _mgr.subscribe( l3, secDef1 );
        _mgr.subscribe( l3, secDef2 );
        _mgr.subscribe( l3, secDef3 );

        CMEBookAdapter[] l1Books = _mgr.getBookSet( l1 ).toArray( new CMEBookAdapter[0] );
        assertEquals( 2, l1Books.length );
        assertSame( _src._books.get(secDef1), l1Books[0] );
        assertSame( _src._books.get(secDef3), l1Books[1] );

        CMEBookAdapter[] l2Books = _mgr.getBookSet( l2 ).toArray( new CMEBookAdapter[0] );
        assertEquals( 2, l2Books.length );
        assertSame( _src._books.get(secDef2), l2Books[0] );
        assertSame( _src._books.get(secDef3), l2Books[1] );

        CMEBookAdapter[] l3Books = _mgr.getBookSet( l3 ).toArray( new CMEBookAdapter[0] );
        assertEquals( 3, l3Books.length );
        assertSame( _src._books.get(secDef1), l3Books[0] );
        assertSame( _src._books.get(secDef2), l3Books[1] );
        assertSame( _src._books.get(secDef3), l3Books[2] );

        _mgr.changed( l3Books[0] );
        _mgr.changed( l3Books[1] );
        _mgr.changed( l3Books[2] );
        
        assertEquals( 2, l1._changes.size() );
        assertEquals( 2, l2._changes.size() );
        assertEquals( 3, l3._changes.size() );
        assertSame( l1Books[0], l1._changes.get(0) );
        assertSame( l1Books[1], l1._changes.get(1) );
        assertSame( l2Books[0], l2._changes.get(0) );
        assertSame( l2Books[1], l2._changes.get(1) );
        assertSame( l3Books[0], l3._changes.get(0) );
        assertSame( l3Books[1], l3._changes.get(1) );
        assertSame( l3Books[2], l3._changes.get(2) );

        SingleOwnerListenerContext<CMEBookAdapter>  ctx;
        BookListener<CMEBookAdapter>[]                       foundListeners;

        ctx =  (SingleOwnerListenerContext<CMEBookAdapter>) l3._changes.get( 0 ).getContext();
        foundListeners = ctx.getListeners();
        assertEquals( 2, foundListeners.length );
        assertSame( l1, foundListeners[0] );
        assertSame( l3, foundListeners[1] );

        ctx =  (SingleOwnerListenerContext<CMEBookAdapter>) l3._changes.get( 1 ).getContext();
        foundListeners = ctx.getListeners();
        assertEquals( 2, foundListeners.length );
        assertSame( l2, foundListeners[0] );
        assertSame( l3, foundListeners[1] );

        ctx =  (SingleOwnerListenerContext<CMEBookAdapter>) l3._changes.get( 2 ).getContext();
        foundListeners = ctx.getListeners();
        assertEquals( 3, foundListeners.length );
        assertSame( l1, foundListeners[0] );
        assertSame( l2, foundListeners[1] );
        assertSame( l3, foundListeners[2] );

        assertSame( l1._changes.get( 0 ).getContext(), l3._changes.get( 0 ).getContext() );
        assertSame( l2._changes.get( 0 ).getContext(), l3._changes.get( 1 ).getContext() );

        assertNotSame( l1._changes.get( 0 ).getContext(), l2._changes.get( 0 ).getContext() );
        assertNotSame( l2._changes.get( 0 ).getContext(), l3._changes.get( 0 ).getContext() );
    }
}
