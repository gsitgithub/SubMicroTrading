/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.main;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rr.core.collections.MessageQueue;
import com.rr.core.dispatch.DirectDispatcherNonThreadSafe;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.log.LogEventLarge;
import com.rr.core.log.LogEventSmall;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.PersisterException;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.properties.AppProps;
import com.rr.core.properties.PropertyGroup;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.SeperateGatewaySession;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.utils.FileException;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.core.warmup.WarmupRegistry;
import com.rr.md.book.l2.L2BookDispatchAdapter;
import com.rr.md.book.l2.L2LongIdBookFactory;
import com.rr.md.us.cme.CMEBookAdapter;
import com.rr.md.us.cme.CMEMarketDataController;
import com.rr.md.us.cme.WarmupCMECodec;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.om.main.OMProps.Tags;
import com.rr.om.recovery.DummyRecoveryController;
import com.rr.om.session.SessionManager;
import com.rr.om.warmup.FixTestUtils;

/**
 * SubMicroTrading collapsed main loader
 */
public class MDDump extends BaseSMTMain {

    private static final Logger    _console         = LoggerFactory.console( MDDump.class );
    private static final ErrorCode FAILED           = new ErrorCode( "MDD100", "Exception in main" );
    
                  final Logger              _log;
    private       CMEMarketDataController   _proc;
    private       int                       _expOrders    = 1000000;
    private       MessageDispatcher         _inboundDispatcher;
    
    public static void main( String[] args ) {
        
        try {
            prepare( args, ThreadPriority.Main );
            
            MDDump smt = new MDDump();
            smt.init();
            
            smt.warmup();

            smt.run();
            
            _console.info( "Completed" );
            
        } catch( Exception e ) {
            
            _console.error( FAILED, "", e );
        }
    }

    MDDump() {
        super();
        _log       = LoggerFactory.create( MDDump.class );
    }
    
    @Override
    protected Logger log() {
        return _log;
    }

    @Override
    protected void init() throws SessionException, FileException, PersisterException {
        
        _log.info( "SMTMain.init()" );
        
        super.init();
        
        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        WarmupCMECodec warmupCMECodec = new WarmupCMECodec( warmupCount );
        WarmupRegistry.instance().register( warmupCMECodec );

        _expOrders = AppProps.instance().getIntProperty( OMProps.EXPECTED_ORDERS, false, 1010000 );        
        presize( _expOrders );

        DateFormat utcFormat = new SimpleDateFormat( "yyyyMMdd" );
        
        String today = utcFormat.format( new Date() );
        FixTestUtils.setDateStr( today );
        
        SessionManager sessMgr = getSessionManager();
        Session hub = null;
        _proc   = getProcesssor( hub );

        String[] upSessionNames = AppProps.instance().getNodes( "session.up." );
        for( String sessName : upSessionNames ) {
            if ( ! sessName.equals( "default") ) {
                ClientProfile client = null;
                if ( ! sessName.startsWith( "test" ) ) {
                    client = loadClientProfile( sessName );
                }
                createSession( sessName, client, false, sessMgr, _proc, hub, "session.up." );
            }
        }

        // could load balance market data events across number of processors
        MessageHandler[] handlers = { _proc };
        
        createCMEFastFixSessionsViaBuilder( handlers );
        
        Session[] upStream = sessMgr.getUpStreamSessions();
        for( Session upSess : upStream ) {
            upSess.init();
        }

        _inboundDispatcher.start();
    }

    @Override
    protected void presize( int expOrders ) {
        super.presize( expOrders );
        
        int recycledMax         = Math.min( expOrders, 50000 ); // allowing 100000 per second, assume in second get time to recycle
        
        int chainSize           = SizeConstants.DEFAULT_CHAIN_SIZE;
        int recycledEventChains = (recycledMax / chainSize) + 100;
        int extraAlloc          = 50;

        presize( MDEntryImpl.class, recycledEventChains, chainSize, extraAlloc );
        presize( MDIncRefreshImpl.class, recycledEventChains, chainSize, extraAlloc );
        presize( RecoveryNewOrderSingleImpl.class, recycledEventChains, chainSize, extraAlloc );

        presize( LogEventSmall.class,  200, 100, 50 );
        presize( LogEventLarge.class,  10000, 100, 100 );
    }
    
    private void run() {
        
        Session[] downStream = getSessionManager().getDownStreamSessions();
        Session[] upStream   = getSessionManager().getUpStreamSessions();

        RecoveryController rct = new DummyRecoveryController();
        
        rct.start();
        
        for( Session upSess : upStream ) {
            upSess.recover( rct );
        }
        
        for( Session downSess : downStream ) {
            downSess.recover( rct );
        }
        
        for( Session upSess : upStream ) {
            upSess.waitForRecoveryToComplete();
        }
        
        for( Session downSess : downStream ) {
            downSess.waitForRecoveryToComplete();
        }
        
        rct.reconcile();
        rct.commit();
        
        Utils.invokeGC();
        SuperpoolManager.instance().resetPoolStats();

        commonSessionConnect();
        
        for( Session downSess : downStream ) {
            if ( downSess instanceof SeperateGatewaySession ) {
                ((SeperateGatewaySession) downSess).getGatewaySession().connect();
            } else {
                downSess.connect();
            }
        }
        
        for( Session upSess : upStream ) {
            upSess.connect();
        }
        
        // check received fix message
    
        _console.info( "ENTERING MAIN LOOP - ctrl-C to stop program" );

        System.out.flush();
        System.err.flush();
        
        _log.info( "ENTERING MAIN LOOP - ctrl-C to stop program" );
        
        while( true ) {
            try {
                Utils.delay( 1000 );
            } catch( Throwable t ) {
                System.out.println( t.getMessage() );
            }
        }
    }

    private CMEMarketDataController getProcesssor( Session hub ) throws SessionException {
        
        PropertyGroup procGroup      = new PropertyGroup( "proc.", null, null );

        int bookLevels    = procGroup.getIntProperty( Tags.bookLevels,       false, 10 );
        
        // enqueue incremental updates pending next snapshot event
        boolean enqueueIncTicksOnGap = procGroup.getBoolProperty( Tags.enqueueIncTicksOnGap, false, false );
        String rec = procGroup.getProperty( Tags.REC, false, "2" ); // CME

        MessageQueue queue = getQueue( procGroup, "Processor" );
        
        _inboundDispatcher = getProcessorDispatcher( procGroup, queue, "ProcessorDispatcher", ThreadPriority.Processor ); 
        
        _log.info( "PROCESSOR Using " + _inboundDispatcher.getClass().getSimpleName() + " with " + queue.getClass().getSimpleName() + " for Processsor" );

        L2LongIdBookFactory<CMEBookAdapter> bookFactory = new L2LongIdBookFactory<CMEBookAdapter>( CMEBookAdapter.class, false, _instrumentStore, bookLevels );
        
        MessageDispatcher algoDispatcher = new DirectDispatcherNonThreadSafe();

        algoDispatcher.setHandler( new MessageHandler() {
            
            private final ReusableString _debugMsg = new ReusableString();
            
            @Override
            public void handle( Message event ) {
                handleNow( event );
            }

            @Override public void handleNow( Message event ) {
                Book book = (Book) event;
                _debugMsg.reset();
                book.dump( _debugMsg );
            
                _log.info( _debugMsg );
            }

            @Override public String    getComponentId() { return null; }
            @Override public void       threadedInit()  { /* nothing */ }
            @Override public boolean    canHandle()     { return true; }
        });
        
        L2BookDispatchAdapter<CMEBookAdapter> asyncListener = new L2BookDispatchAdapter<CMEBookAdapter>( algoDispatcher );
        
        String subscriptionFile = procGroup.getProperty( Tags.subscriptionFile, false, null );
        
        CMEMarketDataController p;
        
        if ( subscriptionFile != null ) {
            CMEMarketDataController sc = new CMEMarketDataController( "TestController", 
                                                                      rec,
                                                                      _inboundDispatcher, 
                                                                      bookFactory, 
                                                                      asyncListener, 
                                                                      _instrumentStore, 
                                                                      enqueueIncTicksOnGap );

            try {
                sc.addSubscriptions( subscriptionFile );
            } catch( IOException e ) {
                throw new SessionException( "Error subscribing to file " + subscriptionFile, e );
            }
            
            p = sc;
            
        } else {
            p = new CMEMarketDataController( "TestController", 
                                       rec,
                                       _inboundDispatcher, 
                                       bookFactory, 
                                       asyncListener, 
                                       _instrumentStore, 
                                       enqueueIncTicksOnGap );
        }
        
        algoDispatcher.start();

        ReflectUtils.setProperty( p, "_overrideSubscribeSet", "true" );

        return p;
    }
}
