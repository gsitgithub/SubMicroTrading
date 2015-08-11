/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.fixsocket;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.MessageQueue;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.dispatch.ThreadedDispatcher;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.lang.stats.StatsCfgFile;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.LogEventLarge;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ModelVersion;
import com.rr.core.persister.PersisterException;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.DummyRouter;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.session.socket.SocketSession;
import com.rr.core.utils.FileException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;
import com.rr.mds.client.MDSConsumer;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelledImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelledImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.order.collections.HashEntry;
import com.rr.om.processor.EventProcessor;
import com.rr.om.processor.EventProcessorImpl;
import com.rr.om.recovery.DummyRecoveryController;
import com.rr.om.registry.FullTradeRegistry;
import com.rr.om.registry.TradeRegistry;
import com.rr.om.router.OrderRouter;
import com.rr.om.router.SingleDestRouter;
import com.rr.om.validate.EmeaDmaValidator;
import com.rr.om.validate.EventValidator;
import com.rr.om.warmup.FixSimConstants;
import com.rr.om.warmup.FixTestUtils;
import com.rr.om.warmup.sim.BaseFixSimProcess;
import com.rr.om.warmup.sim.FixSimParams;

public class TstFixServer extends BaseFixSimProcess {

    private static final Logger    _log          = LoggerFactory.console( TstFixServer.class );
    private static final ErrorCode FAILED        = new ErrorCode( "TFS100", "Exception in main" );
    private static final int       MAX_AGE       = 10000;
    
    private static       int       _indexIdx     = 0;
    
    protected synchronized static int nextIdx() {
        return ++_indexIdx;
    }

    private         FixSocketSession    _exSess;
    private         FixSocketSession    _client;
    private         EventProcessorImpl  _proc;
    private final   MDSConsumer           _mdsClient;
    
    public static void main( String[] args ) {
        
        StatsMgr.setStats( new StatsCfgFile() );
        StatsMgr.instance().initialise();
        FixSimParams params = getProcessedParams( args ); 
        
        ThreadUtils.init( params.getCpuMasksFile() );
        
        ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.Main );
        
        LoggerFactory.setDebug( false );
        LoggerFactory.initLogging( "./logs/TstFixServer.log", 10000000 );        
        
        try {
            TstFixServer tfs = new TstFixServer( params );
            
            tfs.init();
            
            Utils.invokeGC();
            
            SuperpoolManager.instance().resetPoolStats();

            tfs.run();
            
            _log.info( "Completed" );
            
        } catch( Exception e ) {
            
            _log.error( FAILED, "", e );
        }
    }
        
    @Override
    protected void init() throws SessionException, FileException, PersisterException, IOException {
        
        _log.info( "TstFixServer.init()" );
        
        super.init();
        
        presize();

        _log.info( "CONNECT PARAMS : clientHost="   + _params.getUpHost()   + 
                   ", clientPort="                  + _params.getUpPort()   +
                   ", exchangeHost="                + _params.getDownHost() +
                   ", exchagePort="                 + _params.getDownPort() );

        DateFormat utcFormat = new SimpleDateFormat( "yyyyMMdd" );
        
        String today = utcFormat.format( new Date() );
        FixTestUtils.setDateStr( today );
        
        Session hub = createHub();
        
        _proc   = getProcesssor( hub );
        _exSess = createExchangeFacingSession( _proc, hub );
        _client = createClientFacingSession( _proc, hub );

        OrderRouter    router = new SingleDestRouter( _exSess );

        _proc.setProcessorRouter( router );
        
        // start the sockets

        _mdsClient.init( new DummyInstrumentLocator(), _params.getMDSListenPort() );
        
        _exSess.init();
        _client.init();
    }
        
    private void presize() {
        int orders              = Math.max( _params.getNumOrders(),   20000 );
        int recycledMax         = Math.min( _params.getNumOrders(),   50000 ); // allowing 100000 per second, assume in second get time to recycle
        
        int chainSize           = 100;
        int orderChains         = orders / chainSize ;
        int recycledEventChains = recycledMax / chainSize;
        int logChains           = Math.min( orders, 500 );
        int extraAlloc          = 50;

        _log.info( "Presize based on " + orders + " orders will have orderChains=" + orderChains + ", chainSize=" + chainSize );
        
        presize( ClientNewOrderSingleImpl.class, orderChains,         chainSize, extraAlloc );
        presize( MarketNewOrderSingleImpl.class, recycledEventChains, chainSize, extraAlloc );
        presize( ClientNewOrderAckImpl.class,    recycledEventChains, chainSize, extraAlloc );
        presize( MarketNewOrderAckImpl.class,    recycledEventChains, chainSize, extraAlloc );
        presize( ClientCancelRequestImpl.class,  recycledEventChains, chainSize, extraAlloc );
        presize( MarketCancelRequestImpl.class,  recycledEventChains, chainSize, extraAlloc );
        presize( ClientCancelledImpl.class,      recycledEventChains, chainSize, extraAlloc );
        presize( MarketCancelledImpl.class,      recycledEventChains, chainSize, extraAlloc );

        presize( ReusableString.class,           10 * orderChains, chainSize, extraAlloc );
        presize( HashEntry.class,                2  * orderChains, chainSize, extraAlloc );

        presize( OrderImpl.class,                orderChains, chainSize, extraAlloc );
        presize( OrderVersion.class,             2 * orderChains, chainSize, extraAlloc );

        presize( OrderImpl.class,                orderChains, chainSize, extraAlloc );

        presize( LogEventLarge.class,            logChains, chainSize, 100 );
    }

    private void run() {
        
        RecoveryController rct = new DummyRecoveryController();
        
        _exSess.recover( rct );
        _client.recover( rct );
        
        _exSess.waitForRecoveryToComplete();
        _client.waitForRecoveryToComplete();
        
        _exSess.connect();
        _client.connect();
        
        // check received fix message
    
        _log.info( "ENTERING MAIN LOOP - ctrl-C to stop program" );
        
        while( true ) {
            try {
                Utils.delay( 1000 );
            } catch( Throwable t ) {
                System.out.println( t.getMessage() );
            }
        }
    }

    public TstFixServer( FixSimParams params ) {
        super( params );
        
        _mdsClient = new MDSConsumer();
    }

    private static FixSimParams getProcessedParams( String[] args ) {
        FixSimParams params = new FixSimParams( "TstFixServer", true, true, "S" );
        
        params.setUpHost(         "localhost" );
        params.setUpSenderCompId( FixSimConstants.DEFAULT_OM_UP_ID );
        params.setUpTargetCompId( FixSimConstants.DEFAULT_CLIENT_SIM_ID );
        params.setUpPort(         FixSimConstants.DEFAULT_OM_CLIENT_PORT );

        params.setDownHost(         "localhost" );
        params.setDownSenderCompId( FixSimConstants.DEFAULT_OM_DOWN_ID );
        params.setDownTargetCompId( FixSimConstants.DEFAULT_EXCHANGE_SIM_ID );
        params.setDownPort(         FixSimConstants.DEFAULT_OM_EXCHANGE_PORT );

        params.setHubHost(         "localhost" );
        params.setHubSenderCompId( FixSimConstants.DEFAULT_OM_HUB_ID );
        params.setHubTargetCompId( FixSimConstants.DEFAULT_HUB_BRIDGE_ID );
        params.setHubPort(         FixSimConstants.DEFAULT_HUB_PORT );

        params.procArgs( args );
        
        return params;
    }

    private EventProcessorImpl getProcesssor( Session hub ) {
        
        ModelVersion      version       = new ModelVersion( (byte)'1', (byte)'0' );
        EventValidator    validator     = new EmeaDmaValidator( MAX_AGE );
        EventBuilder      builder       = new EventBuilderImpl();
        TradeRegistry     tradeReg      = new FullTradeRegistry(2);

        MessageDispatcher dispatcher;
        
        if ( _params.isOptimiseForThroughPut() ) {
            
            MessageQueue queue = new ConcLinkedMsgQueueSingle();
            dispatcher = new ThreadedDispatcher( "ProcessorDispatcher", queue, ThreadPriority.Processor );
            
        } else if ( _params.isOptimiseForLatency() ) { 
            // no dispatcher faster for straight latency .. fine providing only one client
            // tho should really lock the order in processor
            
            dispatcher = new DirectDispatcher();
            
        } else {
            MessageQueue queue = new BlockingSyncQueue();
            dispatcher = new ThreadedDispatcher( "ProcessorDispatcher", queue, ThreadPriority.Processor );
        }
        
        EventProcessorImpl t = new EventProcessorImpl( version, _params.getNumOrders(), validator, builder, dispatcher, hub, tradeReg );
        
        dispatcher.start();
        
        return t;
    }

    private FixSocketSession createClientFacingSession( EventProcessor proc, Session hub ) throws SessionException, PersisterException {
        
        String              name              = "TCLIENT";
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = FixTestUtils.getEncoder44( outBuf, logHdrOut ); 
        FixDecoder          decoder           = FixTestUtils.getDecoder44();
        FixDecoder          recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();
        ThreadPriority      receiverPriority  = ThreadPriority.SessionInbound1;
        ZString             senderCompId      = new ViewString( _params.getUpSenderCompId() );
        ZString             senderSubId       = new ViewString( "" );
        ZString             targetCompId      = new ViewString( _params.getUpTargetCompId() ); 
        ZString             targetSubId       = new ViewString( "" );
        ZString             userName          = new ViewString( "" );
        ZString             password          = new ViewString( "" );

        FixSocketConfig     socketConfig      = new FixSocketConfig( EventRecycler.class, 
                                                                     true, 
                                                                     new ViewString( _params.getUpHost() ), 
                                                                     new ViewString( _params.getUpAdapter() ), 
                                                                     _params.getUpPort(),
                                                                     senderCompId, 
                                                                     senderSubId, 
                                                                     targetCompId, 
                                                                     targetSubId,
                                                                     userName,
                                                                     password );
        
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );
        socketConfig.setLocalPort( _params.getUpLocalPort() );
        
        MessageDispatcher dispatcher = getSessionDispatcher( "CLIENT_DISPATCHER", ThreadPriority.SessionOutbound1 ); 

        socketConfig.setInboundPersister(  createInboundPersister( name + "_CLT_IN", ThreadPriority.MemMapAllocator ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_CLT_OUT", ThreadPriority.MemMapAllocator ) ); 
        
        setSocketPerfOptions( socketConfig );

        FixSocketSession sess = new FixSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                      recoveryDecoder, receiverPriority );

        sess.setChainSession( hub );
        dispatcher.setHandler( sess );

        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        } else if ( _params.isDisableNanoStats() ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }
        
        return sess;
    }

    private FixSocketSession createExchangeFacingSession( EventProcessor proc, Session hub ) throws SessionException, PersisterException {
        String            name              = "TSERVER1";
        MessageRouter     inboundRouter     = new PassThruRouter( proc ); 
        int               logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder        encoder           = FixTestUtils.getEncoder44( outBuf, logHdrOut ); 
        FixDecoder        decoder           = FixTestUtils.getDecoder44();
        Decoder           recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();

        ThreadPriority    receiverPriority  = ThreadPriority.SessionInbound2; 

        ZString             senderCompId      = new ViewString( _params.getDownSenderCompId() );
        ZString             senderSubId       = new ViewString( "" );
        ZString             targetCompId      = new ViewString( _params.getDownTargetCompId() ); 
        ZString             targetSubId       = new ViewString( "" );
        ZString             userName          = new ViewString( "" );
        ZString             password          = new ViewString( "" );
        FixSocketConfig     socketConfig      = new FixSocketConfig( EventRecycler.class, 
                                                                     false, 
                                                                     new ViewString( _params.getDownHost() ), 
                                                                     new ViewString( _params.getDownAdapter() ), 
                                                                     _params.getDownPort(),
                                                                     senderCompId, 
                                                                     senderSubId, 
                                                                     targetCompId, 
                                                                     targetSubId,
                                                                     userName,
                                                                     password );
        
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );
        socketConfig.setLocalPort( _params.getDownLocalPort() );
        setSocketPerfOptions( socketConfig );
        
        socketConfig.setInboundPersister(  createInboundPersister( name + "_EXC1_IN", ThreadPriority.MemMapAllocator ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXC1_OUT", ThreadPriority.MemMapAllocator ) ); 
        
        MessageDispatcher dispatcher = getSessionDispatcher( "SRV_DISPATCHER", ThreadPriority.SessionOutbound2 ); 

        FixSocketSession sess = new FixSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                      recoveryDecoder, receiverPriority );
  
        sess.setChainSession( hub );
        dispatcher.setHandler( sess );

        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        } else if ( _params.isDisableNanoStats() ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }
        
        return sess;
    }
    
    private SocketSession createHub() {
        String            name              = "THUB";
        int               logHdrOut         = SocketSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder           encoder           = FixTestUtils.getEncoder44( outBuf, logHdrOut ); 
        Decoder           decoder           = FixTestUtils.getDecoder44();
        Decoder           recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();
        MessageRouter     inboundRouter     = new DummyRouter();

        if ( ! _params.isHubEnabled() ){
            return null;
        }
        
        ThreadPriority    receiverPriority  = ThreadPriority.Other; 
        SocketConfig      socketConfig      = new SocketConfig( EventRecycler.class, 
                                                                false, 
                                                                new ViewString( _params.getHubHost() ), 
                                                                new ViewString( _params.getHubAdapter() ), 
                                                                _params.getHubPort() ); 

        socketConfig.setSoDelayMS( 0 );
        socketConfig.setUseNIO( false );
        socketConfig.setTcpNoDelay( false );
        
        MessageDispatcher dispatcher;
        MessageQueue queue = new BlockingSyncQueue();
        dispatcher = new ThreadedDispatcher( "HUB_DISPATCHER", queue, ThreadPriority.Other );

        SocketSession sess = new SocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                recoveryDecoder, receiverPriority );
  
        dispatcher.setHandler( sess );

        return sess;
    }
}
