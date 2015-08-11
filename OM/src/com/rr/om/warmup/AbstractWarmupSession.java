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
import com.rr.core.lang.Stopable;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.ModelVersion;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.DummyRouter;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.SessionThreadedDispatcher;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.session.socket.SocketSession;
import com.rr.core.utils.FileException;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
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
import com.rr.om.session.fixsocket.FixSocketSession;
import com.rr.om.validate.EmeaDmaValidator;
import com.rr.om.validate.EventValidator;
import com.rr.om.warmup.sim.BaseFixSimProcess;
import com.rr.om.warmup.sim.ClientSimSender;
import com.rr.om.warmup.sim.FixSimParams;
import com.rr.om.warmup.sim.SimClient;
import com.rr.om.warmup.sim.WarmClientReplyHandler;
import com.rr.om.warmup.sim.WarmupExchangeSimAdapter;
import com.rr.om.warmup.sim.WarmupUtils;

public abstract class AbstractWarmupSession extends BaseFixSimProcess {

    protected static final Logger    _log          = LoggerFactory.create( AbstractWarmupSession.class );

    private static final long MAX_TIME_WAIT_MS         = 10000;
    
    public static String   DEFAULT_OM_UP_ID            = "WARMUP_SMTC1";
    public static String   DEFAULT_CLIENT_SIM_ID       = "WARMUP_TCLT1";
    public static int      DEFAULT_OM_CLIENT_PORT      = 14001;
    public static String   DEFAULT_OM_DOWN_ID          = "WARMUP_SMTE1";
    public static String   DEFAULT_EXCHANGE_SIM_ID     = "WARMUP_TEXE1";
    public static int      DEFAULT_OM_EXCHANGE_PORT    = 14011;         // +1 must be free as well for recovery port
    public static String   HUB_HOST                    = "127.0.0.1";
    public static int      HUB_PORT                    = 14055;
    
    // client sim side
    protected       Session                  _clientSess;
    private         SimClient                _clientSimSender;
    private         WarmClientReplyHandler   _clientReplyHandler;
    private         ClientStatsManager       _statsMgr;
    protected       long                     _maxRunTime = MAX_TIME_WAIT_MS;
    

    // OM side
    protected       Session                  _omToExSess;
    protected       Session                  _omToClientSess;
    private         MessageHandler           _proc;
    

    // exchange sim side
    protected       Session                  _exSess;
    protected       WarmupExchangeSimAdapter _exSimAdapter;

    protected       CodecId                  _codecId;


    public AbstractWarmupSession( FixSimParams params, CodecId id ) {
        super( params );
        
        _codecId = id;
    }

    public static FixSimParams createParams( String appName, int portOffset, boolean spinLocks, int count ) {
        if ( spinLocks ) {
            String[] wargs = { "-u", "-F", "" + portOffset, "-W", "" + count }; 
            return getProcessedParams( appName, wargs );
        } 
        
        String[] wargs = { "-F", "" + portOffset,  "-W", "" + count };
        return getProcessedParams( appName, wargs );
    }

    public void warmup() throws Exception {
        
        _log.info( getClass().getSimpleName() +
                                 " WARMUP PARAMS : numOrder=" + _params.getWarmupCount() +
                                 ", delayMS="          + _params.getDelayMicros()  + 
                                 ", warmClientHost="   + _params.getUpHost()   + 
                                 ", warmClientPort="   + _params.getUpPort()   +
                                 ", warmExchangeHost=" + _params.getDownHost() +
                                 ", warmExchagePort="  + _params.getDownPort() );
        
        init();
        run();
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
    protected void init() throws SessionException, FileException, PersisterException, IOException {
        
        super.init();

        // no super pool presizing

        _statsMgr           = new ClientStatsManager( _params.getWarmupCount() );
        _clientReplyHandler = new WarmClientReplyHandler( _statsMgr );

        MessageQueue queue = getQueue( "WarmupExchangeSimProcessorQueue" );
        MessageDispatcher dispatcher = new ThreadedDispatcher( "WarmupExchangeSimAdapter", queue, ThreadPriority.Other );

        _exSimAdapter   = new WarmupExchangeSimAdapter( dispatcher, _params.getWarmupCount(), queue );            
        
        Session hub     = createOmToHub();
        
        _proc           = getProcesssor( hub );
        _clientSess     = createClientSession( _clientReplyHandler );
        _omToExSess     = createOmToExchangeSession( _proc, hub );
        _omToClientSess = createOmToClientSession( _proc, hub );
        _exSess         = createExchangeSession( _exSimAdapter );

        postCreateInit();
        
        // init the sockets
        
        _exSess.init();
        _omToExSess.init();
        _omToClientSess.init();
        _clientSess.init();

        _clientSimSender = createSimSender();
    }

    protected void postCreateInit() {
        OrderRouter    router = new SingleDestRouter( _omToExSess );
        ((EventProcessor)_proc).setProcessorRouter( router );
    }

    /**
     * @throws IOException  
     */
    protected SimClient createSimSender() throws IOException {
        FixSocketSession[] client = { (FixSocketSession) _clientSess };
        return new ClientSimSender( WarmupUtils.getTemplateRequests(), client, _statsMgr, "WARMUP" );
    }
        
    protected abstract Session createOmToExchangeSession( MessageHandler proc, Session hub ) throws SessionException, FileException, PersisterException;
    protected abstract Session createExchangeSession( WarmupExchangeSimAdapter exSimAdapter ) throws FileException, SessionException, PersisterException;

    protected void run() {

        try {
            recover();
            connect();

            int idx=0;
            while( idx < 20 && ! isReadyToTrade() ) {
                Utils.delay( 100 );
            }

            sendClientMessages();
            waitForMessagesProcessed();

            _log.info( "WarmupFixSocketSession COMPLETED  : testRequests=" + _params.getWarmupCount() );

        } catch( IOException e ) {
            e.printStackTrace();
        } finally {
            close();
        }

        _statsMgr.logStats();
    }

    protected boolean isReadyToTrade() {
        return _omToExSess.isLoggedIn() && _clientSess.isConnected();
    }
    
    protected void sendClientMessages() throws IOException {
        ReflectUtils.invoke( "clear", _proc );
        
        _clientReplyHandler.reset();
        _clientSimSender.reset();
        _statsMgr.reset();
        
        _clientSimSender.dispatchEvents( _params.getWarmupCount(), _params.getBatchSize(), _params.getDelayMicros() );
    }

    private void waitForMessagesProcessed() {

        _log.info( "Warmup waiting for expected number of events " + _clientSimSender.getSent() );

        long start = System.currentTimeMillis();
        
        // first wait for client replay handler for client to receive
        while( _clientReplyHandler.getReceived() < _clientSimSender.getSent() ) {
            
            Utils.delay( 200 );
            
            long timeMS = System.currentTimeMillis() - start;
            
            if ( timeMS > _maxRunTime ) {
                _log.info( "Hit max wait time for warmup complete" );
                
                break;
            }
        }
        
        _log.info( "Warmup replies=" + _clientReplyHandler.getReceived() + ", statsSeenClOrdId=" + _clientReplyHandler.getStatsBasedReplies() );
    }

    protected void recover() {

        // EX
        RecoveryController exRct = new DummyRecoveryController();
        _exSess.recover( exRct );
        _exSess.waitForRecoveryToComplete();

        // OM
        RecoveryController omRct = new DummyRecoveryController();
        _omToExSess.recover( omRct );
        _omToClientSess.recover( omRct );
        _omToExSess.waitForRecoveryToComplete();
        _omToClientSess.waitForRecoveryToComplete();

        // CLIENT
        RecoveryController clientRct = new DummyRecoveryController();
        _clientSess.recover( clientRct );
        _clientSess.waitForRecoveryToComplete();
    }

    protected void connect() {
        _exSess.connect();
        _omToExSess.connect();
        _omToClientSess.connect();
        _clientSess.connect();
    }

    protected void close() {
        if ( _proc instanceof Stopable ) {
            ((Stopable)_proc).stop();
        }
        _clientSess.stop();
        _omToClientSess.stop();
        _omToExSess.stop();
        _exSess.stop();
        _exSimAdapter.stop();
    }

    public static FixSimParams getProcessedParams( String appName, String[] args ) {
        FixSimParams params = new FixSimParams( "Warmup" + appName, true, true, "S" );
        
        params.enableClientParams();
        
        params.setWarmupCount( 3000 );
        params.setNumOrders( 3000 );

        params.procArgs( args );
        
        params.setRemovePersistence( true );
        
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
        
        return params;
    }

    protected MessageHandler getProcesssor( Session hub ) {
        
        ModelVersion      version       = new ModelVersion( (byte)'1', (byte)'0' );
        EventValidator    validator     = new EmeaDmaValidator( Integer.MAX_VALUE );
        EventBuilder      builder       = new EventBuilderImpl();
        TradeRegistry     tradeReg      = new FullTradeRegistry(2);

        MessageDispatcher dispatcher;
        
        if ( _params.isOptimiseForThroughPut() ) {
            
            MessageQueue queue = new ConcLinkedMsgQueueSingle();
            dispatcher = new ThreadedDispatcher( "WarmupProcessorDispatcher", queue, ThreadPriority.Other );
            
        } else if ( _params.isOptimiseForLatency() ) { 
            // no dispatcher faster for straight latency .. fine providing only one client
            // tho should really lock the order in processor
            
            dispatcher = new DirectDispatcher();
            
        } else {
            MessageQueue queue = new BlockingSyncQueue();
            dispatcher = new ThreadedDispatcher( "WarmupProcessorDispatcher", queue, ThreadPriority.Other );
        }
        
        EventProcessorImpl t = new EventProcessorImpl( version, _params.getWarmupCount(), validator, builder, dispatcher, hub, tradeReg );
        
        dispatcher.start();
        
        return t;
    }

    protected Session createOmToClientSession( MessageHandler proc, Session hub ) throws SessionException, PersisterException {
        
        String              name              = "WARM_OM2CLT";
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = (FixEncoder) WarmupUtils.getEncoder( _codecId, outBuf, logHdrOut ); 
        FixDecoder          decoder           = (FixDecoder) getDecoder( _codecId );
        FixDecoder          recoveryDecoder   = (FixDecoder) WarmupUtils.getRecoveryDecoder( _codecId );

        ThreadPriority      receiverPriority  = ThreadPriority.Other;
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

        MessageDispatcher dispatcher = getSessionDispatcher( "WARMUP_OM2CLIENT_DISPATCHER", ThreadPriority.Other); 

        socketConfig.setInboundPersister(  createInboundPersister( name + "_CLT_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_CLT_OUT", ThreadPriority.Other ) ); 
        
        setSocketPerfOptions( socketConfig );

        FixSocketSession sess = new FixSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                      recoveryDecoder, receiverPriority );

        sess.setChainSession( hub );
        dispatcher.setHandler( sess );

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

    protected Decoder getDecoder( CodecId codecId ) {
        Decoder decoder = WarmupUtils.getDecoder( codecId );
        return decoder;
    }

    private SocketSession createOmToHub() {
        String            name              = "WARM_OM2HUB";
        int               logHdrOut         = SocketSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder           encoder           = WarmupUtils.getEncoder( _codecId, outBuf, logHdrOut ); 
        Decoder           decoder           = WarmupUtils.getDecoder( _codecId );
        Decoder           recoveryDecoder   = WarmupUtils.getRecoveryDecoder( _codecId );
        MessageRouter     inboundRouter     = new DummyRouter();

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 
        SocketConfig      socketConfig      = new SocketConfig( EventRecycler.class, true, new ViewString(HUB_HOST), null, HUB_PORT ); 
        
        socketConfig.setSoDelayMS( 0 );
        socketConfig.setUseNIO( false );
        
        MessageDispatcher dispatcher        = new SessionThreadedDispatcher( "WARM_OM2HUB_DISPATCHER", new BlockingSyncQueue(), ThreadPriority.Other ); 

        SocketSession sess = new SocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                recoveryDecoder, receiverPriority );
  
        dispatcher.setHandler( sess );

        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        }

        return sess;
    }

    protected Session createClientSession( WarmClientReplyHandler clientReplyHandler ) throws SessionException, PersisterException {

        String            name              = "WARM_CLIENTSIM";
        MessageRouter     inboundRouter     = new PassThruRouter( clientReplyHandler ); 
        int               logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder        encoder           = (FixEncoder) WarmupUtils.getEncoder( _codecId, outBuf, logHdrOut ); 
        FixDecoder        decoder           = (FixDecoder) WarmupUtils.getDecoder( _codecId );
        Decoder           recoveryDecoder   = WarmupUtils.getRecoveryDecoder( _codecId );

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 

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
        
        socketConfig.setHeartBeatIntSecs( _heartbeat );
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );
        setSocketPerfOptions( socketConfig );

        socketConfig.setInboundPersister(  createInboundPersister( name + "_CLTSIM_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_CLTSIM_OUT", ThreadPriority.Other ) ); 
        
        MessageDispatcher dispatcher = getSessionDispatcher( "WARM_CLTSIM_DISPATCHER", ThreadPriority.Other ); 

        FixSocketSession sess = new FixSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                      recoveryDecoder, receiverPriority );
  
        dispatcher.setHandler( sess );

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

    public FixSimParams getParams() {
        return _params;
    }
}
