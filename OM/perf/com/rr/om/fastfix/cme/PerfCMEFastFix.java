/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.fastfix.cme;

import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.inst.FixInstrumentLoader;
import com.rr.inst.MultiExchangeInstrumentStore;
import com.rr.inst.InstrumentStore;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.om.order.collections.HashEntry;
import com.rr.om.warmup.FixSimConstants;
import com.rr.om.warmup.WarmupCMEFastFixSession;
import com.rr.om.warmup.WarmupFixSocketSession;
import com.rr.om.warmup.sim.BaseFixSimProcess;
import com.rr.om.warmup.sim.FixSimParams;

public class PerfCMEFastFix {

            static final Logger      _log     = LoggerFactory.console( PerfCMEFastFix.class );
    private static final ErrorCode   FAILED   = new ErrorCode( "TFC100", "Exception in main" );
    private static final ErrorCode   WARM_ERR = new ErrorCode( "TFC200", "Warmup Exception in main" );

    private static FixSimParams _params;

    private WarmupCMEFastFixSession _sess;

    public static void main( String[] args ) {
        
        LoggerFactory.setDebug( true );
        StatsMgr.setStats( new TestStats() );
        _params = getProcessedParams( args ); 

        ThreadUtils.init( _params.getCpuMasksFile(), true );
        
        ThreadUtils.setPriority( Thread.currentThread(), ThreadPriority.ClientSimulatorOut );
        
        if ( _params.getWarmupCount() > 0 ) {
            try {
                WarmupFixSocketSession sess = WarmupFixSocketSession.create( "TstFixClient", args );
                sess.warmup();
            } catch( Throwable t ) {
                _log.error( WARM_ERR, "Error in warmup", t );
            }
        }
        
        try {
            MultiExchangeInstrumentStore store = new MultiExchangeInstrumentStore( 1000 );
            FixInstrumentLoader loader = new FixInstrumentLoader( store );
            loader.loadFromFile( "./data/cme/secdef.t1.dat", new ViewString("2") );
            
            PerfCMEFastFix tcs = new PerfCMEFastFix( _params, store );
            
            tcs.init();
            
            tcs.run();
        } catch( Exception e ) {
            
            _log.error( FAILED, "", e );
        }
    }
    
    private static FixSimParams getProcessedParams( String[] args ) {
        FixSimParams params = new FixSimParams( "TstFixClientSim", true, true, "X" );
        params.enableClientParams();
     
        params.setRemovePersistence( true );
        
        params.setDownHost(         "localhost" );
        params.setDownSenderCompId( FixSimConstants.DEFAULT_CLIENT_SIM_ID );
        params.setDownTargetCompId( FixSimConstants.DEFAULT_OM_UP_ID );
        params.setDownPort(         FixSimConstants.DEFAULT_OM_CLIENT_PORT );
        params.setFileName(         FixSimConstants.DEFAULT_CLIENT_DATA_FILE );
        params.setUpHost( "224.0.0.249" );
        params.setUpPort( 30005 );

        params.procArgs( args );
        
        _log.info( "CONNECT PARAMS : OM host=" + params.getDownHost() + ", port=" + params.getDownPort() );
        
        return params;
    }

    protected void init() {
        presize();
        
        _sess.setMaxRunTime( 60000 );
    }
     
    private void presize() {
        int orders              = _params.getNumOrders();
        int recycledMax         = Math.min( _params.getNumOrders(), 20000 ); // allowing 20000 per second, assume in second get time to recycle
        
        int chainSize           = 1000;
        int orderChains         = orders / chainSize;
        int recycledEventChains = recycledMax / chainSize;
        int extraAlloc          = 50;
        
        BaseFixSimProcess.presize( MDIncRefreshImpl.class, recycledEventChains, chainSize, extraAlloc );
        BaseFixSimProcess.presize( MDEntryImpl.class, recycledEventChains, chainSize, extraAlloc );
        BaseFixSimProcess.presize( RecoveryNewOrderSingleImpl.class,    recycledEventChains, chainSize, extraAlloc );
        BaseFixSimProcess.presize( MarketNewOrderAckImpl.class,    recycledEventChains, chainSize, extraAlloc );

        BaseFixSimProcess.presize( ReusableString.class,           orderChains, chainSize, extraAlloc );
        BaseFixSimProcess.presize( HashEntry.class,                orderChains, chainSize, extraAlloc );
    }

    private void run() throws Exception {
        _sess.warmup();
    }

    public PerfCMEFastFix( FixSimParams params, InstrumentStore locator ) {
        _sess = new WarmupCMEFastFixSession( params, locator );
    }
}
