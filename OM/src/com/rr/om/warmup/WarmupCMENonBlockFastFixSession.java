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
import java.util.ArrayList;
import java.util.List;

import com.rr.algo.ExchangeContainerAdapter;
import com.rr.algo.t1.T1Algo;
import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.MessageQueue;
import com.rr.core.collections.RingBufferMsgQueueSingleConsumer;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.DirectDispatcherNonThreadSafe;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.dispatch.ThreadedDispatcher;
import com.rr.core.dummy.warmup.DummyMessageHandler;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.Book;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.DummyRouter;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.SessionThreadedDispatcher;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.session.socket.SocketSession;
import com.rr.core.thread.DualElementControlThread;
import com.rr.core.utils.FileException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.core.warmup.JITWarmup;
import com.rr.core.warmup.WarmupRegistry;
import com.rr.inst.InstrumentStore;
import com.rr.md.book.l2.L2BookDispatchAdapter;
import com.rr.md.book.l2.L2LongIdBookFactory;
import com.rr.md.fastfix.FastSocketConfig;
import com.rr.md.us.cme.CMEBookAdapter;
import com.rr.md.us.cme.CMEFastFixSession;
import com.rr.md.us.cme.CMEMarketDataController;
import com.rr.md.us.cme.CMENonBlockingFastFixSession;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;
import com.rr.md.us.cme.writer.CMEFastFixEncoder;
import com.rr.model.generated.fix.codec.CMEEncoder;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.om.main.BaseSMTMain;
import com.rr.om.router.OrderRouter;
import com.rr.om.router.SingleDestRouter;
import com.rr.om.session.fixsocket.FixSocketConfig;
import com.rr.om.session.fixsocket.FixSocketSession;
import com.rr.om.warmup.sim.FixSimParams;
import com.rr.om.warmup.sim.SimCMEFastFixSender;
import com.rr.om.warmup.sim.SimClient;
import com.rr.om.warmup.sim.WarmClientReplyHandler;
import com.rr.om.warmup.sim.WarmupExchangeSimAdapter;
import com.rr.om.warmup.sim.WarmupUtils;

/**
 * @NOTE is get multicast dropped packets it possible the decoder will have the wrong value in the stateful templateId reader this will generate errors
 *
 */
public class WarmupCMENonBlockFastFixSession extends AbstractWarmupSession implements JITWarmup {

    private static final String DEFAULT_TEMPLATES = "./data/cme/sampleMD.dat";

    public static WarmupCMENonBlockFastFixSession create( String appName, String[] args, InstrumentStore locator ) {
        
        FixSimParams params = getProcessedParams( appName, args );

        WarmupCMENonBlockFastFixSession  wfss = new WarmupCMENonBlockFastFixSession( params, locator );
        
        return wfss;
    }

    public static WarmupCMENonBlockFastFixSession create( String appName, int portOffset, boolean spinLocks, int count, InstrumentStore locator ) {
        
        FixSimParams params = AbstractWarmupSession.createParams( appName, portOffset, spinLocks, count );
        
        WarmupCMENonBlockFastFixSession  wfss = new WarmupCMENonBlockFastFixSession( params, locator );
        
        return wfss;
    }

    private final List<byte[]>              _templateRequests = new ArrayList<byte[]>();
    private       String                    _templateDataFile;
    private       CMEMarketDataController   _ctlr;

    T1Algo _t1;
    private ExchangeContainerAdapter _exchangeInboundHandler;
    private String _mcastGroups = "225.0.0.1";
    
    private final InstrumentStore _instLocator;
    
    private         MultiSessionThreadedDispatcher  _T1dispatcher;
    private         MultiSessionThreadedReceiver    _T1reciever;
    private         DualElementControlThread _ct;
    
    public WarmupCMENonBlockFastFixSession( String appName, String args[], InstrumentStore locator ) {
        super( getProcessedParams( appName, args ), CodecId.CME );
        _instLocator = locator;
        commonInit();
    }

    public WarmupCMENonBlockFastFixSession( FixSimParams params, InstrumentStore locator ) {
        super( params, CodecId.CME );
        _instLocator = locator;

        commonInit();
    }

    private void commonInit() {
        if ( _params.getClientDataFile() == null ) {
            _params.setClientDataFile( DEFAULT_TEMPLATES );
        }
        
        _templateDataFile = _params.getClientDataFile();
    }

    @Override
    public String getName() {
        return "WarmCMEFastFixSession";
    }

    @Override
    public void warmup() throws Exception {
//        warmSession();        
        super.warmup();
    }
    
    @Override
    public void connect() {
        super.connect();
        _T1dispatcher.start();
        _T1reciever.start();  // THIS WILL RUN CONNECT ON OM-CLIENT SESSIONS
    }

    @Override
    public void close() {
        super.close();
        _T1dispatcher.stop();
        _T1reciever.stop();
        _ct.setStopping( true );
        _ct.statusChange();
    }
    
    @Override
    protected void init() throws SessionException, FileException, PersisterException, IOException {
        _ct = new DualElementControlThread( "WarmupMultiSessControlThread", ThreadPriority.Other );

        _T1reciever       = new MultiSessionThreadedReceiver( "MultiSessReceiver", _ct );
        _T1dispatcher     = new MultiSessionThreadedDispatcher( "MultiSessDispatcher", _ct ); 
        
        super.init();
    }
    
    @Override
    protected SimClient createSimSender() throws IOException {
        BaseSMTMain.loadSampleData( _templateDataFile, _templateRequests, 30 );

        return new SimCMEFastFixSender( _templateRequests, false, (CMEFastFixSession) _clientSess );
    }
    
    @Override
    protected void run() {

        try {
            int expectedOrders = _params.getNumOrders() / _t1.getNOSMod();

            _log.info( "CMEMultiFastFix Warmup expected number of events " + expectedOrders );

            recover();
            connect();

            int idx=0;
            while( idx < 20 && ! isReadyToTrade() ) {
                Utils.delay( 100 );
            }

            sendClientMessages();

            _log.info( "CMEMultiFastFix Warmup waiting for expected number of events " );

            long start = System.currentTimeMillis();
            
            // first wait for client replay handler for client to receive
            while( _exSimAdapter.getRequests() < expectedOrders ) {
                
                Utils.delay( 200 );
                
                long timeMS = System.currentTimeMillis() - start;
                
                if ( timeMS > _maxRunTime ) {
                    _log.info( "Hit max wait time for warmup complete" );
                    
                    break;
                }
            }
            
            _log.info( "CMEMultiFastFix Warmup replies=" + _exSimAdapter.getRequests() );

            _log.info( "CMEMultiFastFix WarmupFixSocketSession COMPLETED  : testRequests=" + _params.getWarmupCount() );

        } catch( IOException e ) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    @Override
    protected void postCreateInit() {
        OrderRouter router = new SingleDestRouter( _omToExSess );
        _t1 = new T1Algo( 2, router );
        if ( _params.isDebug() ) {
            _t1.setDebug( true );
        }
        _exchangeInboundHandler.setContainer( _t1 ); 
    }

    @Override
    protected MessageHandler getProcesssor( Session hub ) {
        
        _exchangeInboundHandler = new ExchangeContainerAdapter();
        
        MessageDispatcher inboundDispatcher;
        
        if ( _params.isOptimiseForThroughPut() ) {
            
            MessageQueue queue = new ConcLinkedMsgQueueSingle();
            inboundDispatcher = new ThreadedDispatcher( "WarmupMultiMktDataDispatcher", queue, ThreadPriority.Other );
            
        } else if ( _params.isOptimiseForLatency() ) { 
            // no dispatcher faster for straight latency .. fine providing only one client
            // tho should really lock the order in processor
            
            inboundDispatcher = new DirectDispatcher();
            
        } else {
            MessageQueue queue = new BlockingSyncQueue();
            inboundDispatcher = new ThreadedDispatcher( "WarmupMultiMktDataDispatcher", queue, ThreadPriority.Other );
        }

        L2LongIdBookFactory<CMEBookAdapter> bookFactory = new L2LongIdBookFactory<CMEBookAdapter>( CMEBookAdapter.class, false, _instLocator, 10 );
        
        MessageDispatcher algoDispatcher = new DirectDispatcherNonThreadSafe();

        algoDispatcher.setHandler( new MessageHandler() {
            
            @Override
            public void handle( Message event ) {
                handleNow( event );
            }

            @Override public void handleNow( Message event ) {
                Book book = (Book) event;
                getAlgo().changed( book );
            }

            @Override public String    getComponentId() { return null; }
            @Override public void       threadedInit()  { /* nothing */ }
            @Override public boolean    canHandle()     { return true; }
        });
        
        L2BookDispatchAdapter<CMEBookAdapter> asyncListener = new L2BookDispatchAdapter<CMEBookAdapter>( algoDispatcher );
        
        _ctlr = new CMEMarketDataController( "TestController", "2", inboundDispatcher, bookFactory, asyncListener, _instLocator, false );
        
        _ctlr.setOverrideSubscribeSet( true );

        algoDispatcher.start();
        inboundDispatcher.start();
        
        return _ctlr;
    }

    @Override
    protected Session createClientSession( WarmClientReplyHandler unused ) throws SessionException, PersisterException {

        String           name               = "WARMM_CME_NB_FASTFIX_CLIENTSIM";
        MessageRouter     inboundRouter     = new PassThruRouter( new DummyMessageHandler() ); 

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 

        MessageDispatcher dispatcher = getSessionDispatcher( "WARMM_CME_NB_CLTSIM_DISPATCHER", ThreadPriority.Other ); 

        int port = _params.getUpPort();
        ZString[] grps = BaseSMTMain.formMulticastGroups( _mcastGroups );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, true, grps[0], null, port );
        
        setSocketPerfOptions( socketConfig );

        socketConfig.setDisableLoopback( false );
        socketConfig.setQOS( 2 );
        socketConfig.setTTL( 0 );
        socketConfig.setNic( new ViewString("127.0.0.1") );
        socketConfig.setMulticast( true );
        socketConfig.setMulticastGroups( grps );

        socketConfig.validate();
        
        long                subChannelMask    = -1;

        
        CMEFastFixEncoder   encoder           = new CMEFastFixEncoder( "CMEMultiClientWriter"+name, "data/cme/templates.xml", _params.isDebug() );
        Decoder             decoder           = new CMEFastFixDecoder( "CMEMultiClientReader"+name, "data/cme/templates.xml", subChannelMask, _params.isDebug() );
        
        CMEFastFixSession sess = new CMEFastFixSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, receiverPriority );

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

    @Override
    protected Session createOmToClientSession( MessageHandler proc, Session hub ) throws SessionException, PersisterException {

        String            name              = "WARMM_CME_NB_OM_FASTFIX_IN";
        MessageRouter     inboundRouter     = new PassThruRouter( proc ); 

        int port = _params.getUpPort();
        ZString[] grps = BaseSMTMain.formMulticastGroups( _mcastGroups );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, grps[0], null, port );
        
        setSocketPerfOptions( socketConfig );

        socketConfig.setDisableLoopback( false );
        socketConfig.setQOS( 2 );
        socketConfig.setTTL( 0 );
        socketConfig.setNic( new ViewString("127.0.0.1") );
        socketConfig.setMulticast( true );
        socketConfig.setMulticastGroups( grps );
        socketConfig.setUseNIO( true );

        socketConfig.validate();
        
        long                subChannelMask    = -1;

        CMEFastFixEncoder   encoder           = new CMEFastFixEncoder( "CMEMultiOM2ClientWriter"+name, "data/cme/templates.xml", _params.isDebug() );
        FastFixDecoder      decoder           = new CMEFastFixDecoder( "CMEMultiOM2ClientReader"+name, "data/cme/templates.xml", subChannelMask, _params.isDebug() );
        
        CMENonBlockingFastFixSession sess = new CMENonBlockingFastFixSession( name, inboundRouter, socketConfig, _T1dispatcher, _T1reciever, encoder, decoder, 
                                                                              new RingBufferMsgQueueSingleConsumer( 1024 )  );

        _T1dispatcher.addSession( sess );

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
    
    @Override
    protected Session createOmToExchangeSession( MessageHandler proc, Session hub ) throws SessionException, FileException, PersisterException {
        String            name              = "WARMM_CME_NB_OM_2_SRV";
        int               logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder        encoder           = new CMEEncoder( _codecId.getFixVersion()._major, _codecId.getFixVersion()._minor, outBuf, logHdrOut ); 
        FixDecoder        decoder           = WarmupUtils.getCMEDecoder();
        Decoder           recoveryDecoder   = WarmupUtils.getRecoveryDecoder( _codecId );

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 

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

        socketConfig.setInboundPersister(  createInboundPersister( name + "_EXC1_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXC1_OUT", ThreadPriority.Other ) ); 
        
        MessageDispatcher dispatcher = getSessionDispatcher( "WARMM_CME_NB_OM2EX_DISPATCHER", ThreadPriority.Other ); 

        FixSocketSession sess = new FixSocketSession( name, _exchangeInboundHandler, socketConfig, dispatcher, encoder, decoder, 
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
    
    @Override
    protected Session createExchangeSession( WarmupExchangeSimAdapter exSimAdapter ) throws FileException, SessionException, PersisterException {
        String              name              = "WARMM_CME_NB_EXSIM";
        MessageRouter       inboundRouter     = new PassThruRouter( exSimAdapter ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = new CMEEncoder( _codecId.getFixVersion()._major, _codecId.getFixVersion()._minor, outBuf, logHdrOut ); 
        FixDecoder          decoder           = WarmupUtils.getCMEDecoder();
        FixDecoder          recoveryDecoder   = (FixDecoder) WarmupUtils.getRecoveryDecoder( _codecId );
        ThreadPriority      receiverPriority  = ThreadPriority.Other;
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
        socketConfig.setHeartBeatIntSecs( _heartbeat );
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );
        
        MessageDispatcher dispatcher = getSessionDispatcher( "WARMUPM_EXSIM_DISPATCHER", ThreadPriority.Other ); 

        socketConfig.setInboundPersister(  createInboundPersister( name + "_EXSIM_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXSIM_OUT", ThreadPriority.Other ) ); 
        
        setSocketPerfOptions( socketConfig );

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
    
    protected void warmSession() {
        String            name              = "WARMM_CME_NBSESS";
        int               logHdrOut         = SocketSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder           encoder           = FixTestUtils.getEncoder44( outBuf, logHdrOut ); 
        Decoder           decoder           = FixTestUtils.getDecoder44();
        Decoder           recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();
        MessageRouter     inboundRouter     = new DummyRouter();

        ThreadPriority    receiverPriority  = ThreadPriority.HubSimulator; 
        SocketConfig      socketConfig      = new SocketConfig( EventRecycler.class, true, new ViewString(FixSimConstants.DEFAULT_HUB_HOST), 
                                                                null, FixSimConstants.DEFAULT_HUB_PORT ); 
        
        socketConfig.setSoDelayMS( 0 );
        socketConfig.setUseNIO( false );
        
        MessageDispatcher dispatcher;
        MessageQueue queue = new ConcLinkedMsgQueueSingle();
        dispatcher = new SessionThreadedDispatcher( "WARMM_CME_NBDISP", queue, ThreadPriority.Other );

        SocketSession sess = new SocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                recoveryDecoder, receiverPriority );
  
        dispatcher.setHandler( sess );
        
        Message m1 = new ClientNewOrderSingleImpl();
        Message m2 = new MarketNewOrderSingleImpl();
        Message m3 = new MarketNewOrderAckImpl();
        Message m4 = new ClientNewOrderAckImpl();

        // dispatcher is not started and session is not connected so no background thread running, ie can manipulate queue directly
        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        for( int i=0 ; i < warmupCount ; i++ ) {
            sess.handle( m1 );
            queue.next();
            sess.handle( m2 );
            queue.next();
            sess.handle( m3 );
            sess.handle( m4 );
            queue.poll();
            queue.poll();
            queue.poll();
            queue.poll();
            sess.handle( m1 );
            sess.handle( m2 );
            sess.handle( m3 );
            sess.handle( m4 );
            queue.poll();
            queue.poll();
            queue.poll();
            queue.poll();
        }
        
        queue.clear();
    }

    T1Algo getAlgo() {
        return _t1;
    }

    public int getOrdersReceived() {
        return _exSimAdapter.getRequests();
    }
}
