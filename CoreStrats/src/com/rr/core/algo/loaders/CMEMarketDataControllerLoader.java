/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.loaders;

import java.io.IOException;

import com.rr.core.algo.mdadapters.L2BookChangeNotifier;
import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.inst.InstrumentStore;
import com.rr.md.book.l2.L2BookListener;
import com.rr.md.book.l2.L2LongIdBookFactory;
import com.rr.md.us.cme.CMEBookAdapter;
import com.rr.md.us.cme.CMEMarketDataController;


/**
 * loader for MarketDataController
 * 
 * Many market data sessions may write to the inbound dispatcher queue
 * only one MarketDataController must be used to consume from the inbound dispatcher queue 
 * Many market data controllers may write to the algo dispatcher queue
 * Only one Algo instance must be used to consume from the algo dispatcher queue
 * 
 * ie relationships can be
 * 
 * MarketDataSession  --> many to 1 --> MarketDataController --> many to 1 --> Algo
 * MarketDataSession  --> many to 1 --> MarketDataController --> 1 to 1 --> Algo
 *
 * where 1 to 1 is required the dispatcher should be DirectDispatcherNonThreadSafe() 
 * 
 * CME uses rptSeqNum on individual entries in the MdIncRefresh repeating group as book seq number
 * This is used for recovery where the individual entries can apply to DIFFERENT products are used by the CME book
 * 
 * @author Richard Rose
 */
public class CMEMarketDataControllerLoader implements SMTSingleComponentLoader {

    private static final Logger _log = LoggerFactory.console( CMEMarketDataControllerLoader.class );
    
    private int                 _bookLevels = 10;
    private boolean             _enqueueIncTicksOnGap = false;
    private boolean             _allowIntradaySecurityUpdates = false;
    private String              _subscriptionFile = null;
    private String              _rec = null;
    private String              _pipeIdList = null;
    private boolean             _overrideSubscribeSet = false;

    private MessageDispatcher   _inboundDispatcher = null;
    private InstrumentStore     _instrumentStore    = null;
    
    private SMTComponent _bookListener = new L2BookChangeNotifier<CMEBookAdapter>();
    
    @Override
    public SMTComponent create( String id ) {

        _log.info( "CMEMarketDataController " + id + " has dispatcher " + _inboundDispatcher.info() + ", bookLevels=" + _bookLevels );

        L2LongIdBookFactory<CMEBookAdapter> bookFactory = new L2LongIdBookFactory<CMEBookAdapter>( CMEBookAdapter.class, true, _instrumentStore, _bookLevels );

        @SuppressWarnings( "unchecked" )
        L2BookListener<CMEBookAdapter> bookListener = (L2BookListener<CMEBookAdapter>) _bookListener;
        
        CMEMarketDataController mktCtl = new CMEMarketDataController( id,
                                                                      _rec,
                                                                      _inboundDispatcher, 
                                                                      bookFactory, 
                                                                      bookListener, 
                                                                      _instrumentStore, 
                                                                      _enqueueIncTicksOnGap );

        mktCtl.setAllowIntradaySecurityUpdates( _allowIntradaySecurityUpdates );
        mktCtl.setPipeIdList( _pipeIdList );
        mktCtl.setOverrideSubscribeSet( _overrideSubscribeSet );
        
        try {
            if ( _subscriptionFile != null ) {
                mktCtl.addSubscriptions( _subscriptionFile );
            }
        } catch( IOException e ) {
            throw new SMTRuntimeException( "Error subscribing to file " + _subscriptionFile, e );
        }

        return mktCtl;
    }
}
