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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.SessionException;
import com.rr.core.utils.FileException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelledImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelledImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.om.dummy.warmup.ClientStatsManager;
import com.rr.om.order.collections.HashEntry;
import com.rr.om.recovery.DummyRecoveryController;
import com.rr.om.warmup.FixSimConstants;
import com.rr.om.warmup.FixTestUtils;
import com.rr.om.warmup.WarmupFixSocketSession;
import com.rr.om.warmup.sim.BaseFixSimProcess;
import com.rr.om.warmup.sim.ClientSimSender;
import com.rr.om.warmup.sim.FixSimParams;
import com.rr.om.warmup.sim.WarmClientReplyHandler;

public class TstFixClientSimulator extends BaseFixSimProcess {

            static final Logger      _log     = LoggerFactory.console( TstFixClientSimulator.class );
    private static final ErrorCode   FAILED   = new ErrorCode( "TFC100", "Exception in main" );
    private static final ErrorCode   WARM_ERR = new ErrorCode( "TFC200", "Warmup Exception in main" );
    
    private FixSocketSession         _clientSession;
    private List<byte[]>             _templateRequests = new ArrayList<byte[]>();
    
            ClientStatsManager       _statsMgr; 
            
    private WarmClientReplyHandler   _clientReplyHandler;

    private ClientSimSender          _clientSimSender;
    
    public static void main( String[] args ) {
        
        LoggerFactory.setDebug( true );
        StatsMgr.setStats( new TestStats() );
        FixSimParams params = getProcessedParams( args ); 

        ThreadUtils.init( params.getCpuMasksFile(), true );
        
        ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.ClientSimulatorOut );
        
        if ( params.getWarmupCount() > 0 ) {
            try {
                WarmupFixSocketSession sess = WarmupFixSocketSession.create( "TstFixClient", args );
                sess.warmup();
            } catch( Throwable t ) {
                _log.error( WARM_ERR, "Error in warmup", t );
            }
        }
        
        TstFixClientSimulator tcs = new TstFixClientSimulator( params );
        
        try {
            tcs.init();
            
            tcs.run();
        } catch( Exception e ) {
            
            _log.error( FAILED, "", e );
        }
    }
    
    private static FixSimParams getProcessedParams( String[] args ) {
        FixSimParams params = new FixSimParams( "TstFixClientSim", false, true, "C" );
        params.enableClientParams();
        
        params.setDownHost(         "localhost" );
        params.setDownSenderCompId( FixSimConstants.DEFAULT_CLIENT_SIM_ID );
        params.setDownTargetCompId( FixSimConstants.DEFAULT_OM_UP_ID );
        params.setDownPort(         FixSimConstants.DEFAULT_OM_CLIENT_PORT );
        params.setFileName(         FixSimConstants.DEFAULT_CLIENT_DATA_FILE );

        params.procArgs( args );
        
        _log.info( "CONNECT PARAMS : OM host=" + params.getDownHost() + ", port=" + params.getDownPort() );
        
        return params;
    }

    @Override
    protected void init() throws SessionException, FileException, PersisterException, IOException {
        
        super.init();
        
        presize();
        
        int expOrders = _params.getNumOrders();
        
        _statsMgr = new ClientStatsManager( expOrders );
        
        DateFormat utcFormat = new SimpleDateFormat( "yyyyMMdd" );
        String today = utcFormat.format( new Date() );
        FixTestUtils.setDateStr( today );
        
        _clientReplyHandler = new WarmClientReplyHandler( _statsMgr );
        _clientSession      = createClientSession( expOrders, _clientReplyHandler );

        // start the sockets
        
        _clientSession.init();
    }
     
    private void presize() {
        int orders              = _params.getNumOrders();
        int recycledMax         = Math.min( _params.getNumOrders(), 20000 ); // allowing 20000 per second, assume in second get time to recycle
        
        int chainSize           = 1000;
        int orderChains         = orders / chainSize;
        int recycledEventChains = recycledMax / chainSize;
        int extraAlloc          = 50;
        
        
        presize( ClientNewOrderSingleImpl.class, recycledEventChains, chainSize, extraAlloc );
        presize( MarketNewOrderSingleImpl.class, recycledEventChains, chainSize, extraAlloc );
        presize( ClientNewOrderAckImpl.class,    recycledEventChains, chainSize, extraAlloc );
        presize( MarketNewOrderAckImpl.class,    recycledEventChains, chainSize, extraAlloc );
        presize( ClientCancelRequestImpl.class,  recycledEventChains, chainSize, extraAlloc );
        presize( MarketCancelRequestImpl.class,  recycledEventChains, chainSize, extraAlloc );
        presize( ClientCancelledImpl.class,      recycledEventChains, chainSize, extraAlloc );
        presize( MarketCancelledImpl.class,      recycledEventChains, chainSize, extraAlloc );

        presize( ReusableString.class,           orderChains, chainSize, extraAlloc );
        presize( HashEntry.class,                orderChains, chainSize, extraAlloc );
    }

    private void run() throws IOException {
        
        RecoveryController rct = new DummyRecoveryController();
        
        _clientSession.recover( rct );
        
        _clientSession.waitForRecoveryToComplete();
        
        Utils.invokeGC();
        
        _clientSession.connect();
        
        loadTradesFromFile( _params.getFileName(), _templateRequests );

        FixSocketSession[] client = { _clientSession };
        _clientSimSender = new ClientSimSender( _templateRequests, client, _statsMgr, _params.getIdPrefix() );

        Utils.delay( 200 ); // wait for any inflight recovery messages
        
        _clientReplyHandler.reset();

        _clientSimSender.dispatchEvents( _params.getNumOrders(), _params.getBatchSize(), _params.getDelayMicros() );
        
        waitForReplies();

        _clientSession.stop();
        
        _statsMgr.logStats();
    }

    private void waitForReplies() {
        // check received fix message
        
        _log.info( "AWAITING REPLIES - ctrl-C to stop program, send=" + _clientSimSender.getSent() + 
                   ", replies=" + _clientReplyHandler.getStatsBasedReplies() );

        int cnt=0;
        while( _clientReplyHandler.getStatsBasedReplies() < _clientSimSender.getSent() ) {
            
            Utils.delay( 1000 );
            
            if ( ++cnt % 10 == 0 ) {
                _log.info( "AWAITING REPLIES [" + cnt + "] - ctrl-C to stop program, send=" + _clientSimSender.getSent() + 
                           ", replies=" + _clientReplyHandler.getStatsBasedReplies() );
            }
        }
    }

    public TstFixClientSimulator( FixSimParams params ) {
        super( params );
    }

    private FixSocketSession createClientSession( int expOrders, MessageHandler inHandler ) throws SessionException, PersisterException {
        String              name              = "TCLIENT";
        MessageRouter       inboundRouter     = new PassThruRouter( inHandler ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = FixTestUtils.getEncoder44( outBuf, logHdrOut ); 
        FixDecoder          decoder           = FixTestUtils.getDecoder44();
        Decoder             recoveryDecoder   = FixTestUtils.getRecoveryDecoder44();
        ThreadPriority      receiverPriority  = ThreadPriority.ClientSimulatorIn;
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
        
        MessageDispatcher dispatcher = new DirectDispatcher(); // want a direct dispatcher for stats logging

        socketConfig.setLocalPort( _params.getDownLocalPort() );
        socketConfig.setInboundPersister(  createInboundPersister( "CL_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( "CL_OUT", ThreadPriority.Other ) ); 
        
        setSocketPerfOptions( socketConfig );
        
        FixSocketSession sess = new FixSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                      recoveryDecoder, receiverPriority );

        dispatcher.setHandler( sess );
        
        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        }
        
        return sess;
    }
}
