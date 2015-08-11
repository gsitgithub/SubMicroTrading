/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.MessageQueue;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.dispatch.ThreadedDispatcher;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.ModelVersion;
import com.rr.core.persister.PersisterException;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.properties.AppProps;
import com.rr.core.properties.PropertyGroup;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.DummyRouter;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.SeperateGatewaySession;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.session.socket.SocketSession;
import com.rr.core.utils.FileException;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.mds.client.MDSConsumer;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.main.OMProps.Tags;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.order.collections.OrderMap;
import com.rr.om.order.collections.SegmentOrderMap;
import com.rr.om.order.collections.SimpleOrderMap;
import com.rr.om.processor.EventProcConfigImpl;
import com.rr.om.processor.EventProcessorImpl;
import com.rr.om.recovery.HighFreqSimpleRecoveryController;
import com.rr.om.registry.FullTradeRegistry;
import com.rr.om.registry.TradeRegistry;
import com.rr.om.router.ExchangeRouter;
import com.rr.om.router.OrderRouter;
import com.rr.om.router.RoundRobinRouter;
import com.rr.om.router.SingleDestRouter;
import com.rr.om.session.SessionManager;
import com.rr.om.validate.EmeaDmaValidator;
import com.rr.om.validate.EventValidator;
import com.rr.om.warmup.FixTestUtils;

/**
 * SubMicroTrading collapsed main loader
 */
public class SMTMain extends BaseSMTMain {

    private static final Logger    _console         = LoggerFactory.console( SMTMain.class );
    private static final ErrorCode FAILED           = new ErrorCode( "SMT100", "Exception in main" );
    private static final int       MAX_AGE          = 10000;
    private static final int       EXPECTED_TRADES  = 100000;
    
    private final Logger              _log;
    private final MDSConsumer         _mdsClient;
    private       EventProcessorImpl  _proc;
    private       int                 _expOrders    = 1000000;
    
    public static void main( String[] args ) {
        
        try {
            prepare( args, ThreadPriority.Main );
            
            SMTMain smt = new SMTMain();
            smt.init();
            smt.warmup();
            smt.run();
            
            _console.info( "Completed" );
            
        } catch( Exception e ) {
            
            _console.error( FAILED, "", e );
        }
    }

    SMTMain() {
        super();
        _log       = LoggerFactory.create( SMTMain.class );
        _mdsClient = new MDSConsumer();
    }
    
    @Override
    protected Logger log() {
        return _log;
    }

    @Override
    protected void init() throws SessionException, FileException, PersisterException {
        
        _log.info( "SMTMain.init()" );
        
        super.init();
        
        _expOrders = AppProps.instance().getIntProperty( OMProps.EXPECTED_ORDERS, false, 1010000 );        

        presize( _expOrders );

        DateFormat utcFormat = new SimpleDateFormat( "yyyyMMdd" );
        
        String today = utcFormat.format( new Date() );
        FixTestUtils.setDateStr( today );
        
        SessionManager sessMgr = getSessionManager();
        Session hub = createHub();
        _proc   = getProcesssor( hub );
        String[] downSessionNames = AppProps.instance().getNodes( "session.down." );
        for( String sessName : downSessionNames ) {
            if ( ! sessName.equals( "default") ) {
                createSession( sessName, null, true, sessMgr, _proc, hub, "session.down." );
            }
        }
        String[] upSessionNames = AppProps.instance().getNodes( "session.up." );
        for( String sessName : upSessionNames ) {
            if ( ! sessName.equals( "default") && ! sessName.startsWith( "test" ) ) {
                ClientProfile client = loadClientProfile( sessName );
                createSession( sessName, client, false, sessMgr, _proc, hub, "session.up." );
            }
        }

        OrderRouter router;
        Session[] downStream = sessMgr.getDownStreamSessions();
        if ( downStream.length == 1 ) {
            router = new SingleDestRouter( downStream[0] );
        } else {
            String routerType = AppProps.instance().getProperty( OMProps.PROC_ROUTER, false, "RoundRobin" );
            
            if ( "RoundRobin".equalsIgnoreCase( routerType ) ) {
                router = new RoundRobinRouter( downStream );
            } else if ( "ExchangeRouter".equalsIgnoreCase( routerType ) ) {
                router = new ExchangeRouter( downStream, sessMgr );
            } else {
                throw new SMTRuntimeException( "SMTMain only proc routers RoundRobin and ExchangeRouter supported" );
            }
        }

        _proc.setProcessorRouter( router );
        
        // start the sockets

        int mdsPort = AppProps.instance().getIntProperty( "session.mds.port", false, MDSConsumer.DEFAULT_MDS_PORT );
        _mdsClient.init( new DummyInstrumentLocator(), mdsPort );
        
        for( Session downSess : downStream ) {
            if ( downSess instanceof SeperateGatewaySession ) { // currently only ETI
                ((SeperateGatewaySession) downSess).getGatewaySession().init();
            }
            downSess.init();
        }

        Session[] upStream = sessMgr.getUpStreamSessions();
        for( Session upSess : upStream ) {
            upSess.init();
        }
    }

    private void run() {
        
        Session[] downStream = getSessionManager().getDownStreamSessions();
        Session[] upStream   = getSessionManager().getUpStreamSessions();

        RecoveryController rct = new HighFreqSimpleRecoveryController( _expOrders, downStream.length + upStream.length, _proc );
        
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

    private EventProcessorImpl getProcesssor( Session hub ) {
        
        PropertyGroup procGroup      = new PropertyGroup( "proc.", null, null );
        PropertyGroup validatorGroup = new PropertyGroup( "proc.validate.", null, null );

        int     maxAgeMS                 = validatorGroup.getIntProperty( OMProps.Tags.maxAgeMS,  false, MAX_AGE );
        int     expTrades                = validatorGroup.getIntProperty( OMProps.Tags.expTrades, false, EXPECTED_TRADES );
        boolean forceCancelUnknownExexId = validatorGroup.getBoolProperty( OMProps.Tags.forceCancelUnknownExexId, false, false );
        
        ModelVersion      version       = new ModelVersion( (byte)'1', (byte)'0' );
        EventValidator    validator     = new EmeaDmaValidator( maxAgeMS );
        EventBuilder      builder       = new EventBuilderImpl();
        TradeRegistry     tradeReg      = new FullTradeRegistry( expTrades );

        MessageDispatcher dispatcher;
        
        MessageQueue queue = getQueue( procGroup, "Processor" );
        
        dispatcher  = getProcessorDispatcher( procGroup, queue, "ProcessorDispatcher", ThreadPriority.Processor ); 
        
        _log.info( "PROCESSOR Using " + dispatcher.getClass().getSimpleName() + " with " + queue.getClass().getSimpleName() + " for Processsor" );
        
        OrderMap orderMap = getOrderMap( procGroup, _expOrders );
        
        EventProcConfigImpl config= new EventProcConfigImpl( forceCancelUnknownExexId );
        final EventProcessorImpl p = new EventProcessorImpl( config, orderMap, version, _expOrders, validator, builder, dispatcher, hub, tradeReg );

        ShutdownManager.instance().register( new Callback() {
                                                    @Override
                                                    public void shuttingDown() {
                                                        p.logStats();
                                                    }} );

        dispatcher.start();
        
        return p;
    }

    private OrderMap getOrderMap( PropertyGroup procGroup, int expOrders ) {
        String mapType    = procGroup.getProperty( Tags.mapType, false, "SegmentOrderMap" );
        float  loadFactor = procGroup.getFloatProperty( Tags.loadFactor, false, 0.75f );

        if ( loadFactor > 1.0 || loadFactor < 0.1 ) {
            _log.warn( "Override bad load factor from " + loadFactor + " to 0.75f" );
            loadFactor = 0.75f;
        }

        int initCap  = (int)(expOrders * (1.05 + (1.0 - loadFactor)));
        
        OrderMap map = null;
        if ( mapType.equalsIgnoreCase( "SegmentOrderMap" ) ) {
            int segments = procGroup.getIntProperty( Tags.segments, false, 1 );
            map = new SegmentOrderMap( initCap, loadFactor, segments );
        } else  if ( mapType.equalsIgnoreCase( "SimpleOrderMap" ) ) {
            map = new SimpleOrderMap( initCap, loadFactor );
        } else{
            throw new SMTRuntimeException( "OrderMap type " + mapType + " not supported" );
        }

        return map;
    }

    private Session createHub() {

        String        propertyGroup = "session.hub";   // overrides defaults
        String        sessName      = "HUB";
        PropertyGroup propGroup     = new PropertyGroup( propertyGroup, null, null );
        boolean       enabled       = propGroup.getBoolProperty( Tags.enabled, false, false );
        
        Session hub = null;
        
        if ( enabled ) {
            SocketConfig  socketConfig = new SocketConfig( EventRecycler.class );
            propGroup.reflectSet( OMProps.instance(), socketConfig );
            socketConfig.validate();
            
            socketConfig.setSoDelayMS( 0 );
            socketConfig.setUseNIO( false );
            socketConfig.setTcpNoDelay( false );
            
            MessageQueue      queue         = new BlockingSyncQueue();
            MessageDispatcher dispatcher    = new ThreadedDispatcher( "HUB_DISPATCHER", queue, ThreadPriority.Other );
            MessageRouter     inboundRouter = new DummyRouter();

            int     logHdrOut       = AbstractSession.getDataOffset( sessName, false );
            byte[]  outBuf          = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
            Encoder encoder         = _codecFactory.getEncoder( CodecId.Standard44, outBuf, logHdrOut );
            Decoder decoder         = _codecFactory.getDecoder( CodecId.Standard44 );
            Decoder recoveryDecoder = _codecFactory.getRecoveryDecoder( CodecId.Standard44 );
            
            SocketSession sess = new SocketSession( sessName, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                    recoveryDecoder, ThreadPriority.Other );
      
            dispatcher.setHandler( sess );

            getSessionManager().setHub( hub );
        }

        return hub;
    }
}
