/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme;

import java.util.ArrayList;
import java.util.List;

import com.rr.core.dispatch.DirectDispatcherNonThreadSafe;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.Constants;
import com.rr.core.model.Book;
import com.rr.md.book.l2.BaseL2BookTst;
import com.rr.md.book.l2.L2BookListener;
import com.rr.md.book.l2.L2LongIdBookFactory;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;
import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.MDUpdateAction;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;


public class TestMKtDataController extends BaseL2BookTst {

    public static final class TestListener implements L2BookListener<CMEBookAdapter> {
        private final List<Book> _changedBooks = new ArrayList<Book>();
        private final String _id = "test";
        
        public TestListener() {
        }

        @Override
        public void changed( CMEBookAdapter book ) {
            _changedBooks.add( book); 
        }

        public List<Book> getOverlaydBooks() {
            return _changedBooks;
        }

        @Override
        public void clear() {
            _changedBooks.clear();
        }

        @Override
        public String id() {
            return _id;
        }
    }
    
    protected CMEMarketDataController   _ctlr;
    protected TestListener              _mktDataListener = new TestListener();

    @Override
    public void setUp() throws Exception { 
        super.setUp();
        
        makeMKtDataController( false );
    }

    protected void makeMKtDataController( boolean enqueueOnGap ) {
        MessageDispatcher dispatcher = new DirectDispatcherNonThreadSafe();
        L2LongIdBookFactory<CMEBookAdapter> bookFactory = new L2LongIdBookFactory<CMEBookAdapter>( CMEBookAdapter.class, false, new DummyInstrumentLocator(), 5 );
        
        _ctlr = new CMEMarketDataController( "TestController", "2", dispatcher, bookFactory, _mktDataListener, enqueueOnGap );
        _ctlr.threadedInit();
        _ctlr.setOverrideSubscribeSet( true );
    }
    
    public void testSnapshot() {
        MDSnapshotFullRefreshImpl event = getSnapEvent( 10000, 100000, 12345 );
        addMDEntry( event, 0, 110, 15.15, MDEntryType.Bid );
        addMDEntry( event, 1, 210, 15.0,  MDEntryType.Bid );
        addMDEntry( event, 2, 310, 14.75, MDEntryType.Bid );
        addMDEntry( event, 3, 410, 14.50, MDEntryType.Bid );
        addMDEntry( event, 4, 510, 14.25, MDEntryType.Bid );
        addMDEntry( event, 0, 100, 15.25, MDEntryType.Offer );
        addMDEntry( event, 1, 200, 15.5,  MDEntryType.Offer );
        addMDEntry( event, 2, 300, 15.75, MDEntryType.Offer );
        addMDEntry( event, 3, 400, 16.0,  MDEntryType.Offer );
        addMDEntry( event, 4, 500, 16.25, MDEntryType.Offer );

        _ctlr.handle( event );

        double[][] results = { 
                               { 110, 15.15, 100, 15.25  }, 
                               { 210, 15.0 , 200, 15.5  },
                               { 310, 14.75, 300, 15.75 }, 
                               { 410, 14.5,  400, 16.0 }, 
                               { 510, 14.25, 500, 16.25 } 
                             };

        List<Book> changed = _mktDataListener.getOverlaydBooks();
        
        assertEquals( 1, changed.size() );
        
        verify( (CMEBookAdapter) changed.get( 0 ),  results );
    }

    public void testIgnoreOldSnapshot() {
        MDSnapshotFullRefreshImpl event = getSnapEvent( 10000, 100000, 12345 );
        addMDEntry( event, 0, 100, 15.25, MDEntryType.Offer );
        addMDEntry( event, 1, 200, 15.5,  MDEntryType.Offer );
        addMDEntry( event, 2, 300, 15.75, MDEntryType.Offer );
        addMDEntry( event, 3, 400, 16.0,  MDEntryType.Offer );
        addMDEntry( event, 4, 500, 16.25, MDEntryType.Offer );
        addMDEntry( event, 0, 110, 15.15, MDEntryType.Bid );
        addMDEntry( event, 1, 210, 15.0,  MDEntryType.Bid );
        addMDEntry( event, 2, 310, 14.75, MDEntryType.Bid );
        addMDEntry( event, 3, 410, 14.50, MDEntryType.Bid );
        addMDEntry( event, 4, 510, 14.25, MDEntryType.Bid );
        _ctlr.handle( event );

        List<Book> changed = _mktDataListener.getOverlaydBooks();
        assertEquals( 1, changed.size() );

        event = getSnapEvent( 10000, 99999, 12345 );
        addMDEntry( event, 0, 100, 15.55, MDEntryType.Offer );
        _ctlr.handle( event );
        
        double[][] results = { 
                               { 110, 15.15, 100, 15.25 }, 
                               { 210, 15.0,  200, 15.5  },
                               { 310, 14.75, 300, 15.75 }, 
                               { 410, 14.5,  400, 16.0  }, 
                               { 510, 14.25, 500, 16.25 } 
                             };

        assertEquals( 1, changed.size() );
        
        verify( (CMEBookAdapter) changed.get( 0 ),  results );
    }

    public void testIgnoreDupBookSeqNum() {
        MDSnapshotFullRefreshImpl event = getSnapEvent( 10000, 100000, 12345 );
        addMDEntry( event, 0, 100, 15.25, MDEntryType.Offer );
        addMDEntry( event, 1, 200, 15.5,  MDEntryType.Offer );
        addMDEntry( event, 2, 300, 15.75, MDEntryType.Offer );
        addMDEntry( event, 3, 400, 16.0,  MDEntryType.Offer );
        addMDEntry( event, 4, 500, 16.25, MDEntryType.Offer );
        addMDEntry( event, 0, 110, 15.15, MDEntryType.Bid );
        addMDEntry( event, 1, 210, 15.0,  MDEntryType.Bid );
        addMDEntry( event, 2, 310, 14.75, MDEntryType.Bid );
        addMDEntry( event, 3, 410, 14.50, MDEntryType.Bid );
        addMDEntry( event, 4, 510, 14.25, MDEntryType.Bid );
        _ctlr.handle( event );

        List<Book> changed = _mktDataListener.getOverlaydBooks();
        assertEquals( 1, changed.size() );

        event = getSnapEvent( 10000, 100000, 12345 );
        addMDEntry( event, 0, 100, 15.55, MDEntryType.Offer );
        _ctlr.handle( event );
        
        double[][] results = { 
                               { 110, 15.15, 100, 15.25 }, 
                               { 210, 15.0,  200, 15.5  },
                               { 310, 14.75, 300, 15.75 }, 
                               { 410, 14.5,  400, 16.0  }, 
                               { 510, 14.25, 500, 16.25 } 
                             };

        assertEquals( 1, changed.size() );
        
        verify( (CMEBookAdapter) changed.get( 0 ),  results );
    }

    public void testRecoveryFromGap() {
        int secId = 12345;
        
        List<Book> changed = _mktDataListener.getOverlaydBooks();

        MDSnapshotFullRefreshImpl snapEvent = getSnapEvent( 10000, 999, secId );
        addMDEntry( snapEvent, 0, 100, 15.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 1, 200, 15.5,  MDEntryType.Offer );
        addMDEntry( snapEvent, 2, 300, 15.75, MDEntryType.Offer );
        addMDEntry( snapEvent, 3, 400, 16.0,  MDEntryType.Offer );
        addMDEntry( snapEvent, 4, 500, 16.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 0, 110, 15.15, MDEntryType.Bid );
        addMDEntry( snapEvent, 1, 210, 15.0,  MDEntryType.Bid );
        addMDEntry( snapEvent, 2, 310, 14.75, MDEntryType.Bid );
        addMDEntry( snapEvent, 3, 410, 14.50, MDEntryType.Bid );
        addMDEntry( snapEvent, 4, 510, 14.25, MDEntryType.Bid );
        _ctlr.handle( snapEvent );
        assertEquals( 1, changed.size() );
        CMEBookAdapter book = (CMEBookAdapter) changed.get( 0 );
        assertTrue( book.isValidBBO() );
        double[][] results = { 
                              { 110, 15.15, 100, 15.25 }, 
                              { 210, 15.0,  200, 15.5  },
                              { 310, 14.75, 300, 15.75 }, 
                              { 410, 14.5,  400, 16.0  }, 
                              { 510, 14.25, 500, 16.25 } 
                            };
        verify( (CMEBookAdapter) changed.get( 0 ),  results );
        changed.clear();

        // GAP - queue event
        MDIncRefreshImpl incEvent = getBaseEvent( 10001 );
        addMDEntry( secId, incEvent, 1, MDUpdateAction.Overlay, 90,  15.35, MDEntryType.Offer, 1010 );
        addMDEntry( secId, incEvent, 4, MDUpdateAction.Overlay, 550, 16.55, MDEntryType.Offer, 1011 );
        _ctlr.handle( incEvent );
        assertEquals( 1, changed.size() );
        assertFalse( book.isValidBBO() );
        changed.clear();

        incEvent = getBaseEvent( 10005 );
        addMDEntry( secId, incEvent, 0, MDUpdateAction.Overlay, 80,   15.25, MDEntryType.Offer, 1012 );
        addMDEntry( secId, incEvent, 2, MDUpdateAction.Overlay, 190,  15.45, MDEntryType.Offer, 1013 );
        addMDEntry( secId, incEvent, 3, MDUpdateAction.Overlay, 290,  15.55, MDEntryType.Offer, 1014 );

        addMDEntry( secId, incEvent, 0, MDUpdateAction.Overlay, 95,   15.19, MDEntryType.Bid, 1015 );
        addMDEntry( secId, incEvent, 1, MDUpdateAction.Overlay, 195,  15.18, MDEntryType.Bid, 1016 );
        addMDEntry( secId, incEvent, 2, MDUpdateAction.Overlay, 295,  15.17, MDEntryType.Bid, 1017 );
        addMDEntry( secId, incEvent, 3, MDUpdateAction.Overlay, 395,  15.16, MDEntryType.Bid, 1018 );
        addMDEntry( secId, incEvent, 4, MDUpdateAction.Overlay, 495,  15.15, MDEntryType.Bid, 1019 );
 
        _ctlr.handle( incEvent );
        assertEquals( 1, changed.size() );
        assertTrue( book.isValidBBO() );
        double[][] results2 = { 
                              {  95, 15.19,  80, 15.25 }, 
                              { 195, 15.18,  90, 15.35 },
                              { 295, 15.17, 190, 15.45 }, 
                              { 395, 15.16, 290, 15.55 }, 
                              { 495, 15.15, 550, 16.55 } 
                              }; 
        
        verify( (CMEBookAdapter) changed.get( 0 ),  results2 );
        changed.clear();
    }
    
    public void testChangeOp() {
        int secId = 12345;
        
        List<Book> changed = _mktDataListener.getOverlaydBooks();

        MDSnapshotFullRefreshImpl snapEvent = getSnapEvent( 10000, 999, secId );
        addMDEntry( snapEvent, 0, 100, 15.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 1, 200, 15.5,  MDEntryType.Offer );
        addMDEntry( snapEvent, 2, 300, 15.75, MDEntryType.Offer );
        addMDEntry( snapEvent, 3, 400, 16.0,  MDEntryType.Offer );
        addMDEntry( snapEvent, 4, 500, 16.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 0, 110, 15.15, MDEntryType.Bid );
        addMDEntry( snapEvent, 1, 210, 15.0,  MDEntryType.Bid );
        addMDEntry( snapEvent, 2, 310, 14.75, MDEntryType.Bid );
        addMDEntry( snapEvent, 3, 410, 14.50, MDEntryType.Bid );
        addMDEntry( snapEvent, 4, 510, 14.25, MDEntryType.Bid );
        _ctlr.handle( snapEvent );
        assertEquals( 1, changed.size() );
        CMEBookAdapter book = (CMEBookAdapter) changed.get( 0 );
        assertTrue( book.isValidBBO() );
        double[][] results = { 
                              { 110, 15.15, 100, 15.25 }, 
                              { 210, 15.0,  200, 15.5  },
                              { 310, 14.75, 300, 15.75 }, 
                              { 410, 14.5,  400, 16.0  }, 
                              { 510, 14.25, 500, 16.25 } 
                            };
        verify( (CMEBookAdapter) changed.get( 0 ),  results );
        changed.clear();

        // GAP - queue event
        MDIncRefreshImpl incEvent = getBaseEvent( 10001 );
        addMDEntry( secId, incEvent, 1, MDUpdateAction.Change,  90,  Constants.UNSET_DOUBLE,  MDEntryType.Offer, 1010 );
        addMDEntry( secId, incEvent, 4, MDUpdateAction.Change,  95,  16.55, MDEntryType.Offer, 1011 ); 
        _ctlr.handle( incEvent );
        assertEquals( 1, changed.size() );
        assertFalse( book.isValidBBO() );
        changed.clear();

        incEvent = getBaseEvent( 10005 );
        addMDEntry( secId, incEvent, 0, MDUpdateAction.Overlay, 80,   15.25, MDEntryType.Offer, 1012 );
        addMDEntry( secId, incEvent, 2, MDUpdateAction.Overlay, 190,  15.45, MDEntryType.Offer, 1013 );
        addMDEntry( secId, incEvent, 3, MDUpdateAction.Overlay, 290,  15.55, MDEntryType.Offer, 1014 );

        addMDEntry( secId, incEvent, 0, MDUpdateAction.Overlay, 95,   15.19, MDEntryType.Bid, 1015 );
        addMDEntry( secId, incEvent, 1, MDUpdateAction.Overlay, 195,  15.18, MDEntryType.Bid, 1016 );
        addMDEntry( secId, incEvent, 2, MDUpdateAction.Overlay, 295,  15.17, MDEntryType.Bid, 1017 );
        addMDEntry( secId, incEvent, 3, MDUpdateAction.Overlay, 395,  15.16, MDEntryType.Bid, 1018 );
        addMDEntry( secId, incEvent, 4, MDUpdateAction.Overlay, 495,  15.15, MDEntryType.Bid, 1019 );
 
        _ctlr.handle( incEvent );
        assertEquals( 1, changed.size() );
        assertTrue( book.isValidBBO() );
        double[][] results2 = { 
                              {  95, 15.19,  80, 15.25 }, 
                              { 195, 15.18,  90, 15.5 },
                              { 295, 15.17, 190, 15.45 }, 
                              { 395, 15.16, 290, 15.55 }, 
                              { 495, 15.15,  95, 16.55 } 
                              }; 
        
        verify( (CMEBookAdapter) changed.get( 0 ),  results2 );
        changed.clear();
    }
    
    public void testEnqueueWhenDirty() {
        int secId = 12345;
        
        List<Book> changed = _mktDataListener.getOverlaydBooks();

        MDIncRefreshImpl incEvent = getBaseEvent( 10000 );
        addMDEntry( secId, incEvent, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Offer, 1000 );
        addMDEntry( secId, incEvent, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Offer, 1001 );
        addMDEntry( secId, incEvent, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Offer, 1002 );
        addMDEntry( secId, incEvent, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Offer, 1003 );
        addMDEntry( secId, incEvent, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Offer, 1004 );
        _ctlr.handle( incEvent );
        assertEquals( 1, changed.size() );
        changed.clear();

        MDSnapshotFullRefreshImpl snapEvent = getSnapEvent( 10001, 1005, secId );
        addMDEntry( snapEvent, 0, 100, 15.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 1, 200, 15.5,  MDEntryType.Offer );
        addMDEntry( snapEvent, 2, 300, 15.75, MDEntryType.Offer );
        addMDEntry( snapEvent, 3, 400, 16.0,  MDEntryType.Offer );
        addMDEntry( snapEvent, 4, 500, 16.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 0, 110, 15.15, MDEntryType.Bid );
        addMDEntry( snapEvent, 1, 210, 15.0,  MDEntryType.Bid );
        addMDEntry( snapEvent, 2, 310, 14.75, MDEntryType.Bid );
        addMDEntry( snapEvent, 3, 410, 14.50, MDEntryType.Bid );
        addMDEntry( snapEvent, 4, 510, 14.25, MDEntryType.Bid );
        _ctlr.handle( snapEvent );
        assertEquals( 1, changed.size() );

        double[][] results = { 
                               { 110, 15.15, 100, 15.25 }, 
                               { 210, 15.0,  200, 15.5  },
                               { 310, 14.75, 300, 15.75 }, 
                               { 410, 14.5,  400, 16.0  }, 
                               { 510, 14.25, 500, 16.25 } 
                             };

        assertEquals( 1, changed.size() );
        
        verify( (CMEBookAdapter) changed.get( 0 ),  results );
    }
    
    public void testEnqueueWhenGap() {
        
        makeMKtDataController( true ); // FORCE ENQUEUE INC UPDATES PENDING SNAPSHOT

        int secId = 12345;
        
        List<Book> changed = _mktDataListener.getOverlaydBooks();

        MDSnapshotFullRefreshImpl snapEvent = getSnapEvent( 10000, 999, secId );
        addMDEntry( snapEvent, 0, 100, 15.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 1, 200, 15.5,  MDEntryType.Offer );
        addMDEntry( snapEvent, 2, 300, 15.75, MDEntryType.Offer );
        addMDEntry( snapEvent, 3, 400, 16.0,  MDEntryType.Offer );
        addMDEntry( snapEvent, 4, 500, 16.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 0, 110, 15.15, MDEntryType.Bid );
        addMDEntry( snapEvent, 1, 210, 15.0,  MDEntryType.Bid );
        addMDEntry( snapEvent, 2, 310, 14.75, MDEntryType.Bid );
        addMDEntry( snapEvent, 3, 410, 14.50, MDEntryType.Bid );
        addMDEntry( snapEvent, 4, 510, 14.25, MDEntryType.Bid );
        _ctlr.handle( snapEvent );
        assertTrue( ((CMEBookAdapter) changed.get( 0 )).isValidBBO() );
        assertEquals( 1, changed.size() );
        double[][] results = { 
                              { 110, 15.15, 100, 15.25 }, 
                              { 210, 15.0,  200, 15.5  },
                              { 310, 14.75, 300, 15.75 }, 
                              { 410, 14.5,  400, 16.0  }, 
                              { 510, 14.25, 500, 16.25 } 
                            };
        verify( (CMEBookAdapter) changed.get( 0 ),  results );
        changed.clear();

        // GAP - queue event
        MDIncRefreshImpl incEvent = getBaseEvent( 10001 );
        addMDEntry( secId, incEvent, 1, MDUpdateAction.Overlay, 90,  15.35, MDEntryType.Offer, 1010 );
        addMDEntry( secId, incEvent, 4, MDUpdateAction.Overlay, 550, 16.75, MDEntryType.Offer, 1011 );
        _ctlr.handle( incEvent );
        assertEquals( 1, changed.size() );
        assertFalse( ((CMEBookAdapter) changed.get( 0 )).isValidBBO() );
        changed.clear();

        snapEvent = getSnapEvent( 10010, 1009, secId );
        addMDEntry( snapEvent, 0, 100, 15.20, MDEntryType.Offer );
        addMDEntry( snapEvent, 1, 200, 15.5,  MDEntryType.Offer );
        addMDEntry( snapEvent, 2, 300, 15.75, MDEntryType.Offer );
        addMDEntry( snapEvent, 3, 400, 16.0,  MDEntryType.Offer );
        addMDEntry( snapEvent, 4, 500, 16.25, MDEntryType.Offer );
        addMDEntry( snapEvent, 0, 110, 15.1, MDEntryType.Bid );
        addMDEntry( snapEvent, 1, 210, 15.0,  MDEntryType.Bid );
        addMDEntry( snapEvent, 2, 310, 14.75, MDEntryType.Bid );
        addMDEntry( snapEvent, 3, 410, 14.50, MDEntryType.Bid );
        addMDEntry( snapEvent, 4, 510, 14.25, MDEntryType.Bid );
        _ctlr.handle( snapEvent );
        assertEquals( 1, changed.size() );
        assertTrue( ((CMEBookAdapter) changed.get( 0 )).isValidBBO() );
        double[][] results2 = { 
                              { 110, 15.1,  100, 15.20 }, 
                              { 210, 15.0,   90, 15.35 },
                              { 310, 14.75, 300, 15.75 }, 
                              { 410, 14.50, 400, 16.0 }, 
                              { 510, 14.25, 550, 16.75 } 
                              }; 
        
        assertEquals( 1, changed.size() );
        verify( (CMEBookAdapter) changed.get( 0 ),  results2 );
        changed.clear();
    }
}
