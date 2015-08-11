/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.algo.t1;

import java.util.List;

import com.rr.core.dispatch.DirectDispatcherNonThreadSafe;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.Constants;
import com.rr.core.model.Book;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.md.book.l2.BaseL2BookTst;
import com.rr.md.book.l2.L2BookDispatchAdapter;
import com.rr.md.book.l2.L2LongIdBookFactory;
import com.rr.md.us.cme.CMEBookAdapter;
import com.rr.md.us.cme.CMEMarketDataController;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.MDUpdateAction;
import com.rr.model.generated.internal.type.Side;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.router.OrderRouter;
import com.rr.om.router.SingleDestRouter;


public class T1AlgoTest extends BaseL2BookTst {

    private DummyExchangeSession _downstreamHandler;
            T1Algo      _t1;

    protected CMEMarketDataController _ctlr;

    @Override
    public void setUp() throws Exception { 
        super.setUp();
        
        _downstreamHandler = new DummyExchangeSession();
        
        OrderRouter router = new SingleDestRouter( _downstreamHandler );

        _t1 = new T1Algo( 2, router );

        // inboundDispatcher used to pass src market data events to controller
        MessageDispatcher inboundDispatcher = new DirectDispatcherNonThreadSafe();
        L2LongIdBookFactory<CMEBookAdapter> bookFactory = new L2LongIdBookFactory<CMEBookAdapter>( CMEBookAdapter.class, false, new DummyInstrumentLocator(), 10 );

        // book dispatcher allows async handoff of book change notification
        MessageDispatcher bookEventDispatcher = new DirectDispatcherNonThreadSafe();

        bookEventDispatcher.setHandler( new MessageHandler() {
            
            @Override
            public void handle( Message event ) {
                handleNow( event );
            }

            @Override public void handleNow( Message event ) {
                _t1.changed( (Book) event );
            }

            @Override public String     getComponentId(){ return null; }
            @Override public void       threadedInit()  { /* nothing */ }
            @Override public boolean    canHandle()     { return true; }
        });
        
        L2BookDispatchAdapter<CMEBookAdapter> bookListener = new L2BookDispatchAdapter<CMEBookAdapter>( bookEventDispatcher );

        // market data controller, applies market data events to books generating change events to registered listener
        _ctlr = new CMEMarketDataController( "TestController", "2", inboundDispatcher, bookFactory, bookListener, false );
        _ctlr.threadedInit();
        _ctlr.setOverrideSubscribeSet( true );
    }

    public void testSingleUpdate() {
        int secId = 12345;
        
        List<Message> events = _downstreamHandler.getEvents();

        MDSnapshotFullRefreshImpl snapEvent = getSnapEvent( 10000, 999, secId );
        addMDEntry( snapEvent, 0, 100, 15.10,  MDEntryType.Bid );
        addMDEntry( snapEvent, 1, 200, 15.07,  MDEntryType.Bid );
        addMDEntry( snapEvent, 2, 300, 15.05,  MDEntryType.Bid );
        addMDEntry( snapEvent, 0, 110, 15.2,   MDEntryType.Offer );
        addMDEntry( snapEvent, 1, 210, 15.25,  MDEntryType.Offer );
        addMDEntry( snapEvent, 2, 310, 14.3,   MDEntryType.Offer );

        _ctlr.handle( snapEvent );              // dispatch market data event to MarketDataController
        
        assertEquals( 0, events.size() );       // mod is 2 so first event wont trigger order

        MDIncRefreshImpl incEvent = getBaseEvent( 10001 );
        addMDEntry( secId, incEvent, 0, MDUpdateAction.Change, 90,  15.175, MDEntryType.Offer, 1000 );

        _ctlr.handle( incEvent );
        assertEquals( 1, events.size() );       // order triggered by update
        
        RecoveryNewOrderSingleImpl nos = (RecoveryNewOrderSingleImpl) events.get( 0 );
        assertEquals( 90,   nos.getOrderQty() );
        assertEquals( 15.175, nos.getPrice(), Constants.TICK_WEIGHT );
        assertEquals( Side.Buy, nos.getSide() );
    }
}
