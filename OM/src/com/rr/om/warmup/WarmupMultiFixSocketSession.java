/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.MessageQueue;
import com.rr.core.collections.RingBufferMsgQueueSingleConsumer;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.dispatch.ThreadedDispatcher;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ModelVersion;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Receiver;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.thread.ControlThread;
import com.rr.core.thread.DualElementControlThread;
import com.rr.core.utils.FileException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.core.warmup.JITWarmup;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.om.dummy.warmup.ClientStatsManager;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.processor.EventProcessor;
import com.rr.om.processor.EventProcessorImpl;
import com.rr.om.recovery.DummyRecoveryController;
import com.rr.om.registry.FullTradeRegistry;
import com.rr.om.registry.TradeRegistry;
import com.rr.om.router.OrderRouter;
import com.rr.om.router.SingleDestRouter;
import com.rr.om.session.fixsocket.FixSocketConfig;
import com.rr.om.session.fixsocket.NonBlockingFixSocketSession;
import com.rr.om.validate.EmeaDmaValidator;
import com.rr.om.validate.EventValidator;
import com.rr.om.warmup.sim.BaseFixSimProcess;
import com.rr.om.warmup.sim.ClientSimSender;
import com.rr.om.warmup.sim.FixSimParams;
import com.rr.om.warmup.sim.WarmClientReplyHandler;
import com.rr.om.warmup.sim.WarmupExchangeSimAdapter;
import com.rr.om.warmup.sim.WarmupUtils;
import com.rr.sim.client.ClientSimNonBlockingFixSession;

public class WarmupMultiFixSocketSession extends BaseFixSimProcess implements JITWarmup {

    private static final Logger    _log          = LoggerFactory.console( WarmupMultiFixSocketSession.class );

    private static final long MAX_TIME_WAIT_MS         = 15000;
    
    public static final String   DEFAULT_OM_UP_ID            = "W_SMTC1";
    public static final String   DEFAULT_CLIENT_SIM_ID       = "W_TCLT1";
    public static final int      DEFAULT_OM_CLIENT_PORT      = 14101;
    public static final String   DEFAULT_OM_DOWN_ID          = "W_SMTE1";
    public static final String   DEFAULT_EXCHANGE_SIM_ID     = "W_TEXE1";
    public static final int      DEFAULT_OM_EXCHANGE_PORT    = 14102;
    public static final String   HUB_HOST                    = "127.0.0.1";
    public static final int      HUB_PORT                    = 14155;
           static final int      TOT_ORDERS                  = 3000;

    
    private final   CodecId                         _codecId;

    private         ControlThread                   _ct;

    // client sim side
    private         ClientSimNonBlockingFixSession  _clientSess;
    private         ClientSimSender                 _clientSimSender;
    private         WarmClientReplyHandler          _clientReplyHandler;
    private         ClientStatsManager              _statsMgr;
    private         long                            _maxRunTime = MAX_TIME_WAIT_MS;
    

    // OM side
    private         NonBlockingFixSocketSession    _omToExSess;
    private         NonBlockingFixSocketSession    _omToClientSess;
    private         EventProcessorImpl             _proc;
    private         MultiSessionThreadedDispatcher _dispatcher;
    private         MultiSessionThreadedReceiver   _reciever;
    

    // exchange sim side
    private         NonBlockingFixSocketSession     _exSess;
    private         WarmupExchangeSimAdapter        _exSimAdapter;

    public static WarmupMultiFixSocketSession create( String appName, int portOffset, boolean spinLocks, int count ) {
        return create( appName, portOffset, spinLocks, count, CodecId.Standard44 );
    }
    
    public static WarmupMultiFixSocketSession create( String appName, int portOffset, boolean spinLocks, int count, CodecId codecId ) {

        FixSimParams params;
        
        if ( spinLocks ) {
            String[] wargs = { "-u", "-F", "" + portOffset, "-W", "" + count }; 
            params = getProcessedParams( appName, wargs );
        } else {
            String[] wargs = { "-F", "" + portOffset,  "-W", "" + count };
            params = getProcessedParams( appName, wargs );
        }
        
        WarmupMultiFixSocketSession wfss = new WarmupMultiFixSocketSession( params, codecId );
        
        return wfss;
    }

    public WarmupMultiFixSocketSession( FixSimParams params ) {
        this( params, CodecId.Standard44 );
    }
    
    public WarmupMultiFixSocketSession( FixSimParams params, CodecId codecId ) {
        super( params );
        
        _codecId = codecId;
    }

    @Override
    public String getName() {
        return "MultiFixSocketSession";
    }
    
    @Override
    public void warmup() throws Exception {

        init();
        run();

        // need to do this as sim sender will be in async mode
        
        // 5919   b   com.rr.om.warmup.sim.ClientSimSender::recordSent (20 bytes)
        ClientSimNonBlockingFixSession[] clients = { _clientSess };
        
        ClientStatsManager statsMgr        = new ClientStatsManager( _params.getWarmupCount() );
        ClientSimSender    clientSimSender = new ClientSimSender( WarmupUtils.getTemplateRequests(), clients, statsMgr, "WRMUP" );
        
        ViewString v = new ViewString( "DUMMYREC" );
        for( int i=0 ; i < _params.getWarmupCount() ; ++i ) {
            clientSimSender.recordSent( v, i );
        }
    }

    public int getReceived() {
        return _clientReplyHandler.getReceived();
    }
    
    public int getSent() {
        return _clientSimSender.getSent();
    }
        
    public void setMaxRunTime( long maxRunTime ) {
        _maxRunTime = maxRunTime;
    }

    public void setEventLogging( boolean on ) {
        _params.setDisableEventLogging( !on );
    }
    
    @Override
    public void init() throws SessionException, FileException, PersisterException, IOException {
        init( true );
    }
    
    public void init( boolean initClient ) throws SessionException, FileException, PersisterException, IOException {
        
        super.init();

        // no super pool presizing

        _statsMgr           = new ClientStatsManager( _params.getWarmupCount() );
        _clientReplyHandler = new WarmClientReplyHandler( _statsMgr );

        MessageQueue queue = getQueue( "WarmupExchangeSimProcessorQueue" );
        MessageDispatcher dispatcher = new ThreadedDispatcher( "WarmupExchangeSimAdapter", queue, ThreadPriority.Other );
        _exSimAdapter   = new WarmupExchangeSimAdapter( dispatcher, _params.getWarmupCount(), queue );            
        
        Session hub     = null;

        _ct = new DualElementControlThread( "WarmupMultiSessControlThread", ThreadPriority.Other );

        _reciever       = new MultiSessionThreadedReceiver( "MultiSessReceiver", _ct );
        _dispatcher     = new MultiSessionThreadedDispatcher( "MultiSessDispatcher", _ct ); 
        _proc           = getProcesssor( hub );
        _exSess         = createExchangeSession( _proc, _exSimAdapter, _dispatcher, _reciever );
        _omToExSess     = createOmToExchangeSession( _proc, hub, _dispatcher, _reciever );
        
        if ( initClient ) {
            _omToClientSess = createOmToClientSession( _proc, hub, _dispatcher, _reciever );
            _clientSess     = createClientSession( _proc, _clientReplyHandler, _dispatcher, _reciever );
        }

        OrderRouter    router = new SingleDestRouter( _omToExSess );

        _proc.setProcessorRouter( router );
        
        // init the sockets
        
        _exSess.init();
        _omToExSess.init();

        if ( initClient ) {
            _omToClientSess.init();
            _clientSess.init();
            
            SeqNumSession[] client = { _clientSess };
            _clientSimSender = new ClientSimSender( WarmupUtils.getTemplateRequests(), client, _statsMgr, "WARMUP" );
        }
    }
       
    public ClientSimSender getClientSimSender() {
        return _clientSimSender;
    }
    
    private void run() {

        try {
            recover();
            connect();

            sendClientMessages();
            waitForMessagesProcessed();

            _log.info( "WarmupMultiFixSocketSession COMPLETED  : testRequests=" + _params.getWarmupCount() );

        } finally {
            close();
        }

        _statsMgr.logStats();
    }
    
    private void sendClientMessages() {
        _proc.clear();

        _clientReplyHandler.reset();
        _clientSimSender.reset();
        _statsMgr.reset();
        
        int totalOrders = _params.getWarmupCount();
        CountDownLatch cdl = new CountDownLatch( totalOrders );
        _clientSimSender.setCountDownLatch( cdl );
        
        _clientSimSender.dispatchEvents( totalOrders, _params.getBatchSize(), _params.getDelayMicros() );

        try {
            cdl.await( 30, TimeUnit.SECONDS );
        } catch( InterruptedException e ) {
            // dont care
        }
    }

    private void waitForMessagesProcessed() {

        _log.info( "Warmup waiting for expected number of events " + _clientSimSender.getSent() );

        long start = System.currentTimeMillis();
        
        // first wait for client replay handler for client to receive
        while( _clientReplyHandler.getReceived() < _clientSimSender.getSent() ) {
            
            Utils.delay( 200 );
            
            long timeMS = System.currentTimeMillis() - start;
            
            if ( timeMS > _maxRunTime ) {
                _log.info( "Hit max wait time " + _maxRunTime + "ms for warmup complete" );
                
                break;
            }
        }

        Utils.delay( 200 );
        
        _log.info( "Warmup replies=" + _clientReplyHandler.getReceived() + ", statsSeenClOrdId=" + _clientReplyHandler.getStatsBasedReplies() );
    }

    public void recover() {

        // OM
        RecoveryController omRct = new DummyRecoveryController();
        _omToExSess.recover( omRct );
        _omToExSess.waitForRecoveryToComplete();
        if ( _omToClientSess != null ) {
            _omToClientSess.recover( omRct );
            _omToClientSess.waitForRecoveryToComplete();
        }

        // EX
        RecoveryController exRct = new DummyRecoveryController();
        _exSess.recover( exRct );
        _exSess.waitForRecoveryToComplete();

        // CLIENT
        if ( _clientSess != null ) {
            RecoveryController clientRct = new DummyRecoveryController();
            _clientSess.recover( clientRct );
            _clientSess.waitForRecoveryToComplete();
        }
    }

    public void connect() {
        _exSess.connect();
        _omToExSess.connect();
        _dispatcher.start();
        _reciever.start();  // THIS WILL RUN CONNECT ON OM-CLIENT SESSIONS
        _clientSess.connect();
    }

    public void close() {
        _proc.stop();
        if ( _clientSess != null ) _clientSess.stop();
        if ( _omToClientSess != null ) _omToClientSess.stop();
        _omToExSess.stop();
        _exSess.stop();
        _exSimAdapter.stop();
        _dispatcher.stop();
        _reciever.stop();
        _ct.setStopping( true );
        _ct.statusChange();
    }

    private static FixSimParams getProcessedParams( String appName, String[] args ) {
        FixSimParams params = new FixSimParams( "WarmupMFS" + appName, true, true, "S" );
        
        params.enableClientParams();
        
        params.procArgs( args );
        
        params.setRemovePersistence( true );
        
        params.setWarmupCount( TOT_ORDERS );
        params.setNumOrders( TOT_ORDERS );
        params.setDelayMicros( 100 );
        
        params.setUpHost(           "127.0.0.1" );
        params.setUpSenderCompId(   DEFAULT_OM_UP_ID );
        params.setUpTargetCompId(   DEFAULT_CLIENT_SIM_ID );
        params.setUpPort(           DEFAULT_OM_CLIENT_PORT + params.getWarmupPortOffset() );

        params.setDownHost(         "127.0.0.1" );
        params.setDownSenderCompId( DEFAULT_OM_DOWN_ID );
        params.setDownTargetCompId( DEFAULT_EXCHANGE_SIM_ID );
        params.setDownPort(         DEFAULT_OM_EXCHANGE_PORT + params.getWarmupPortOffset() );

        params.setNumOrders( params.getWarmupCount() );
        params.setPersistDatPageSize( 4096 );
        params.setPersistDatPreSize( 256 * params.getWarmupCount() + 1024 );
        params.setPersistIdxPreSize( 16 * params.getWarmupCount() + 1024 );

        params.setDisableEventLogging( true );
        
        _log.info( "WARMUP PARAMS : numOrder="         + params.getWarmupCount() +
                                 ", delayMS="          + params.getDelayMicros()  + 
                                 ", warmClientHost="   + params.getUpHost()   + 
                                 ", warmClientPort="   + params.getUpPort()   +
                                 ", warmExchangeHost=" + params.getDownHost() +
                                 ", warmExchagePort="  + params.getDownPort() +
                                 ", disableLogEvents=" + params.isDisableEventLogging() );
        
        return params;
    }

    private EventProcessorImpl getProcesssor( Session hub ) {
        
        ModelVersion      version       = new ModelVersion( (byte)'1', (byte)'0' );
        EventValidator    validator     = new EmeaDmaValidator( Integer.MAX_VALUE );
        EventBuilder      builder       = new EventBuilderImpl();
        TradeRegistry     tradeReg      = new FullTradeRegistry(2);

        MessageDispatcher dispatcher;
        
        if ( _params.isOptimiseForThroughPut() ) {
            
            MessageQueue queue = new ConcLinkedMsgQueueSingle();
            dispatcher = new ThreadedDispatcher( "WarmupMFSProcessorDispatcher", queue, ThreadPriority.Other );
            
        } else if ( _params.isOptimiseForLatency() ) { 
            // no dispatcher faster for straight latency .. fine providing only one client
            // tho should really lock the order in processor
            
            dispatcher = new DirectDispatcher();
            
        } else {
            MessageQueue queue = new BlockingSyncQueue();
            dispatcher = new ThreadedDispatcher( "WarmupMFSProcessorDispatcher", queue, ThreadPriority.Other );
        }
        
        EventProcessorImpl t = new EventProcessorImpl( version, _params.getWarmupCount(), validator, builder, dispatcher, hub, tradeReg );
        
        dispatcher.start();
        
        return t;
    }

    private NonBlockingFixSocketSession createOmToClientSession( EventProcessor                 proc, 
                                                                 Session                        hub,
                                                                 MultiSessionThreadedDispatcher dispatcher, 
                                                                 MultiSessionThreadedReceiver   receiver )
    
                    throws SessionException, PersisterException {
        
        String              name              = "WARMM_OM2CLT";
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = (FixEncoder) WarmupUtils.getEncoder( _codecId, outBuf, logHdrOut ); 
        FixDecoder          decoder           = (FixDecoder) WarmupUtils.getDecoder( _codecId );
        Decoder             recoveryDecoder   = WarmupUtils.getRecoveryDecoder( _codecId );

        ZString             senderCompId      = new ViewString( _params.getUpSenderCompId() );
        ZString             senderSubId       = new ViewString( "" );
        ZString             targetCompId      = new ViewString( _params.getUpTargetCompId() ); 
        ZString             targetSubId       = new ViewString( "" );
        ZString             userName          = new ViewString( "" );
        ZString             password          = new ViewString( "" );

        FixSocketConfig     socketConfig      = new FixSocketConfig( EventRecycler.class, 
                                                                     true, 
                                                                     new ViewString( _params.getUpHost() ), 
                                                                     null,
                                                                     _params.getUpPort(),
                                                                     senderCompId, 
                                                                     senderSubId, 
                                                                     targetCompId, 
                                                                     targetSubId,
                                                                     userName,
                                                                     password );
        socketConfig.setHeartBeatIntSecs( _heartbeat );
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );
        
        socketConfig.setInboundPersister(  createInboundPersister( name + "_CLT_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_CLT_OUT", ThreadPriority.Other ) ); 
        
        setSocketPerfOptions( socketConfig );
        socketConfig.setUseNIO( true );

        NonBlockingFixSocketSession sess = new NonBlockingFixSocketSession( name, 
                                                                            inboundRouter, 
                                                                            socketConfig, 
                                                                            dispatcher,
                                                                            receiver,
                                                                            encoder, 
                                                                            decoder, 
                                                                            recoveryDecoder,
                                                                            new RingBufferMsgQueueSingleConsumer( 1024 ) );

        sess.setChainSession( hub );
        dispatcher.addSession( sess );

        if ( _params.isDisableNanoStats() ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }
        
        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        }
        
        return sess;
    }

    private NonBlockingFixSocketSession createOmToExchangeSession( EventProcessor                 proc, 
                                                                   Session                        hub,
                                                                   MultiSessionThreadedDispatcher dispatcher, 
                                                                   MultiSessionThreadedReceiver   receiver ) 
                throws SessionException, PersisterException {
        
        String            name              = "WARMM_OM_2_SRV";
        MessageRouter     inboundRouter     = new PassThruRouter( proc ); 
        int               logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder        encoder           = (FixEncoder) WarmupUtils.getEncoder( _codecId, outBuf, logHdrOut ); 
        FixDecoder        decoder           = (FixDecoder) WarmupUtils.getDecoder( _codecId );
        Decoder           recoveryDecoder   = WarmupUtils.getRecoveryDecoder( _codecId );

        ZString             senderCompId      = new ViewString( _params.getDownSenderCompId() );
        ZString             senderSubId       = new ViewString( "" );
        ZString             targetCompId      = new ViewString( _params.getDownTargetCompId() ); 
        ZString             targetSubId       = new ViewString( "" );
        ZString             userName          = new ViewString( "" );
        ZString             password          = new ViewString( "" );
        FixSocketConfig     socketConfig      = new FixSocketConfig( EventRecycler.class, 
                                                                     false, 
                                                                     new ViewString( _params.getDownHost() ),
                                                                     null,
                                                                     _params.getDownPort(),
                                                                     senderCompId, 
                                                                     senderSubId, 
                                                                     targetCompId, 
                                                                     targetSubId,
                                                                     userName,
                                                                     password );
        
        socketConfig.setHeartBeatIntSecs( _heartbeat );
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );

        setSocketPerfOptions( socketConfig );
        socketConfig.setUseNIO( true );

        socketConfig.setInboundPersister(  createInboundPersister( name + "_EXC1_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXC1_OUT", ThreadPriority.Other ) ); 
        
        NonBlockingFixSocketSession sess = new NonBlockingFixSocketSession( name, 
                                                                            inboundRouter, 
                                                                            socketConfig, 
                                                                            dispatcher,
                                                                            receiver,
                                                                            encoder, 
                                                                            decoder, 
                                                                            recoveryDecoder,
                                                                            new RingBufferMsgQueueSingleConsumer(1024) );

        sess.setChainSession( hub );
        dispatcher.addSession( sess );
        
        if ( _params.isDisableNanoStats() ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }

        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        }
        
        return sess;
    }
    
    private NonBlockingFixSocketSession createExchangeSession( EventProcessorImpl              proc, 
                                                               WarmupExchangeSimAdapter        exSimAdapter,
                                                               MultiSessionThreadedDispatcher  dispatcher, 
                                                               MultiSessionThreadedReceiver    receiver ) throws SessionException, PersisterException {
        
        String              name              = "WARMM_EXSIM";
        MessageRouter       inboundRouter     = new PassThruRouter( exSimAdapter ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = (FixEncoder) WarmupUtils.getEncoder( _codecId, outBuf, logHdrOut ); 
        FixDecoder          decoder           = (FixDecoder) WarmupUtils.getDecoder( _codecId );
        Decoder             recoveryDecoder   = WarmupUtils.getRecoveryDecoder( _codecId );
        ZString             senderCompId      = new ViewString( _params.getDownTargetCompId() );
        ZString             senderSubId       = new ViewString( "" );
        ZString             targetCompId      = new ViewString( _params.getDownSenderCompId() ); // perspective of params is OM in this case 
        ZString             targetSubId       = new ViewString( "" );
        ZString             userName          = new ViewString( "" );
        ZString             password          = new ViewString( "" );

        FixSocketConfig     socketConfig      = new FixSocketConfig( EventRecycler.class, 
                                                                     true, 
                                                                     new ViewString( _params.getDownHost() ), 
                                                                     null,
                                                                     _params.getDownPort(),
                                                                     senderCompId, 
                                                                     senderSubId, 
                                                                     targetCompId, 
                                                                     targetSubId,
                                                                     userName,
                                                                     password );
        
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );
        
        socketConfig.setInboundPersister(  createInboundPersister( name + "_EXSIM_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXSIM_OUT", ThreadPriority.Other ) ); 
        
        setSocketPerfOptions( socketConfig );

        NonBlockingFixSocketSession sess = new NonBlockingFixSocketSession( name, 
                                                                            inboundRouter, 
                                                                            socketConfig, 
                                                                            dispatcher,
                                                                            receiver,
                                                                            encoder, 
                                                                            decoder, 
                                                                            recoveryDecoder,
                                                                            new RingBufferMsgQueueSingleConsumer( 1024 ) );

        dispatcher.addSession( sess );

        encoder.setNanoStats( false );
        decoder.setNanoStats( false );
        sess.setLogStats( false );
        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        }
        
        return sess;
    }

    private ClientSimNonBlockingFixSession createClientSession( EventProcessorImpl             proc, 
                                                                WarmClientReplyHandler         clientReplyHandler,
                                                                MultiSessionThreadedDispatcher dispatcher, 
                                                                MultiSessionThreadedReceiver   receiver )
    
                    throws SessionException, PersisterException {

        String            name              = "WARMM_CLIENTSIM";
        MessageRouter     inboundRouter     = new PassThruRouter( clientReplyHandler ); 
        int               logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder        encoder           = (FixEncoder) WarmupUtils.getEncoder( _codecId, outBuf, logHdrOut ); 
        FixDecoder        decoder           = (FixDecoder) WarmupUtils.getDecoder( _codecId );
        Decoder           recoveryDecoder   = WarmupUtils.getRecoveryDecoder( _codecId );

        ZString             senderCompId      = new ViewString( _params.getUpTargetCompId() );
        ZString             senderSubId       = new ViewString( "" );
        ZString             targetCompId      = new ViewString( _params.getUpSenderCompId() ); 
        ZString             targetSubId       = new ViewString( "" );
        ZString             userName          = new ViewString( "" );
        ZString             password          = new ViewString( "" );
        FixSocketConfig     socketConfig      = new FixSocketConfig( EventRecycler.class, 
                                                                     false, 
                                                                     new ViewString( _params.getUpHost() ),
                                                                     null,
                                                                     _params.getUpPort(),
                                                                     senderCompId, 
                                                                     senderSubId, 
                                                                     targetCompId, 
                                                                     targetSubId,
                                                                     userName,
                                                                     password );
        
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );

        setSocketPerfOptions( socketConfig );

        socketConfig.setInboundPersister(  createInboundPersister( name + "_CLTSIM_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_CLTSIM_OUT", ThreadPriority.Other ) ); 
        
        ClientSimNonBlockingFixSession sess = new ClientSimNonBlockingFixSession( name, 
                                                                                  inboundRouter, 
                                                                                  socketConfig, 
                                                                                  dispatcher,
                                                                                  receiver,
                                                                                  encoder, 
                                                                                  decoder, 
                                                                                  recoveryDecoder,
                                                                                  new RingBufferMsgQueueSingleConsumer( 8192 ) );

        dispatcher.addSession( sess );
  
        if ( _params.isDisableNanoStats() ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }

        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        }
        
        return sess;
    }

    public Session getClientSession() {
        return _clientSess;
    }


    public Session getOMtoExSession() {
        return _omToExSess;
    }


    public Session getOMtoClientSession() {
        return _omToClientSess;
    }


    public Session getExchangeSession() {
        return _exSess;
    }


    public MessageDispatcher getDispatcher() {
        return _dispatcher;
    }

    public Receiver getReciever() {
        return _reciever;
    }
}
