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
import com.rr.md.asia.bse.BSEBookAdapter;
import com.rr.md.asia.bse.BSEMarketDataController;
import com.rr.md.book.l2.L2BookListener;
import com.rr.md.book.l2.L2LongIdBookFactory;


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
 * @author Richard Rose
 */
public class BSEMarketDataControllerLoader implements SMTSingleComponentLoader {

    private static final Logger _log = LoggerFactory.console( BSEMarketDataControllerLoader.class );
    
    private int                 _bookLevels = 10;
    private int                 _maxProdGrpArraySize = 32; 
    private boolean             _enqueueIncUpdatesOnGap = false;
    private boolean             _allowIntradaySecurityUpdates = false;
    private String              _subscriptionFile = null;
    private String              _rec = null;
    private String              _pipeIdList = null;
    private boolean             _overrideSubscribeSet = false;
    private boolean             _disableDirtyAllBooksOnPacketGap = false;
    private boolean             _ignoreDirtyOnGap = false;
    private boolean             _debug = false;
    private int                 _maxEnqueueIncUpdatesOnGap = 10; 

    private MessageDispatcher   _inboundDispatcher = null;
    private InstrumentStore     _instrumentStore    = null;
    
    private SMTComponent        _bookListener = new L2BookChangeNotifier<BSEBookAdapter>();
    
    @Override
    public SMTComponent create( String id ) {

        _log.info( "BSEMarketDataController " + id + " has dispatcher " + _inboundDispatcher.info() + ", bookLevels=" + _bookLevels );

        L2LongIdBookFactory<BSEBookAdapter> bookFactory = new L2LongIdBookFactory<BSEBookAdapter>( BSEBookAdapter.class, true, _instrumentStore, _bookLevels );
        
        @SuppressWarnings( "unchecked" )
        L2BookListener<BSEBookAdapter> bookListener = (L2BookListener<BSEBookAdapter>) _bookListener;
        
        BSEMarketDataController mktCtl = new BSEMarketDataController( id,
                                                                      _rec,
                                                                      _inboundDispatcher, 
                                                                      bookFactory, 
                                                                      bookListener, 
                                                                      _instrumentStore, 
                                                                      _enqueueIncUpdatesOnGap,
                                                                      _maxProdGrpArraySize );

        mktCtl.setDebug( _debug );
        mktCtl.setAllowIntradaySecurityUpdates( _allowIntradaySecurityUpdates );
        mktCtl.setPipeIdList( _pipeIdList );
        mktCtl.setOverrideSubscribeSet( _overrideSubscribeSet );
        mktCtl.setDisableDirtyAllBooksOnPacketGap( _disableDirtyAllBooksOnPacketGap );
        mktCtl.setIgnoreDirtyOnGap( _ignoreDirtyOnGap );
        mktCtl.setMaxEnqueueIncUpdatesOnGap( _maxEnqueueIncUpdatesOnGap );
        
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
