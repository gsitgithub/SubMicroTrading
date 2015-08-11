/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.hub.bridge;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.MessageQueue;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.dispatch.ThreadedDispatcher;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.DummyRouter;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.session.socket.SocketSession;
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
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.order.collections.HashEntry;
import com.rr.om.recovery.DummyRecoveryController;
import com.rr.om.warmup.FixSimConstants;
import com.rr.om.warmup.FixTestUtils;
import com.rr.om.warmup.sim.BaseFixSimProcess;
import com.rr.om.warmup.sim.FixSimParams;

public class TstHubBridge extends BaseFixSimProcess {

            static final Logger      _log    = LoggerFactory.console( TstHubBridge.class );
    private static final ErrorCode   FAILED  = new ErrorCode( "THB100", "Exception in main" );
    
    private HubBridge                _hubBridge;
    private SocketSession            _omSession;
            
    
    public static void main( String[] args ) {
        
        LoggerFactory.setDebug( true );
        StatsMgr.setStats( new TestStats() );

        FixSimParams params = getProcessedParams( args ); 

        ThreadUtils.init( params.getCpuMasksFile(), true );
        
        ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.Other );
        
        TstHubBridge tes = new TstHubBridge( params );
        
        try {
            tes.init();
            
            Utils.invokeGC();
            
            tes.run();
        } catch( Exception e ) {
            
            _log.error( FAILED, "", e );
        }
    }
    
    private static FixSimParams getProcessedParams( String[] args ) {
        FixSimParams params = new FixSimParams( "TstHubBridge", true, false, "H" );
        
        params.setUpHost(         "localhost" );
        params.setUpSenderCompId( FixSimConstants.DEFAULT_HUB_BRIDGE_ID );
        params.setUpTargetCompId( FixSimConstants.DEFAULT_OM_HUB_ID );
        params.setUpPort(         FixSimConstants.DEFAULT_HUB_PORT );

        params.procArgs( args );
        
        _log.info( "CONNECT PARAMS : host=" + params.getUpHost() + ", port=" + params.getUpPort() );
        
        return params;
    }

    @Override
    protected void init() throws SessionException, FileException, PersisterException, IOException {
        
        super.init();
        
        presize();
        
        int expOrders = _params.getNumOrders();
        
        DateFormat utcFormat = new SimpleDateFormat( "yyyyMMdd" );
        
        String today = utcFormat.format( new Date() );
        FixTestUtils.setDateStr( today );
        
        _hubBridge    = new HubBridgeImpl( expOrders );            
        _omSession = createHubSession( expOrders, _hubBridge );

        // start the sockets
        
        _omSession.init();
    }
     
    private void presize() {
        int orders              = _params.getNumOrders();
        int recycledMax         = Math.min( _params.getNumOrders(), 20000 ); // allowing 20000 per second, assume in second get time to recycle
        
        int chainSize           = 1000;
        int orderChains         = orders / chainSize;
        int recycledEventChains = recycledMax / chainSize;
        int extraAlloc          = 50;
        
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
    }
    
    private void run() {
        
        RecoveryController rct = new DummyRecoveryController();
        
        _omSession.recover( rct );
        
        _omSession.waitForRecoveryToComplete();
        
        _omSession.connect();
        
        // check received fix message
    
        _log.info( "ENTERING MAIN LOOP - ctrl-C to stop program" );
        
        while( true ) {
            
            Utils.delay( 200 );
            
        }
    }

    public TstHubBridge( FixSimParams params ) {
        super( params );
    }

    private SocketSession createHubSession( int expOrders, HubBridge hubBridge ) {
        String            name              = "THUBBRIDGE";
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
        MessageQueue queue = new BlockingSyncQueue();
        dispatcher = new ThreadedDispatcher( "HUB_DISPATCHER", queue, ThreadPriority.Other );

        SocketSession sess = new SocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                recoveryDecoder, receiverPriority );
  
        dispatcher.setHandler( sess );

        return sess;
    }
}
