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
import java.util.TimeZone;

import com.rr.core.codec.Decoder;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.Exchange;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.DummyIndexPersister;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.utils.FileException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.core.warmup.JITWarmup;
import com.rr.model.generated.codec.ETIEurexHFTDecoder;
import com.rr.model.generated.codec.ETIEurexHFTEncoder;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.type.ETIEnv;
import com.rr.model.generated.internal.type.ETISessionMode;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.emea.exchange.eti.gateway.ETIGatewayController;
import com.rr.om.emea.exchange.eti.trading.ETISocketConfig;
import com.rr.om.emea.exchange.eti.trading.ETISocketSession;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.recovery.DummyRecoveryController;
import com.rr.om.warmup.sim.FixSimParams;
import com.rr.om.warmup.sim.WarmupExchangeSimAdapter;
import com.rr.om.warmup.sim.WarmupUtils;

public class WarmupETIEurexSocketSession extends AbstractWarmupSession implements JITWarmup {

    public static boolean TRACE = false;
    
    public static WarmupETIEurexSocketSession create( String appName, int portOffset, boolean spinLocks, int count ) {
        
        FixSimParams params = createParams( appName, portOffset, spinLocks, count );

        if ( TRACE ) {
            // ONLY FOR DEBUG
            
            params.setLogPojoEvents( true );
            params.setDisableEventLogging( false );
            params.setDebug( true );
        }

        WarmupETIEurexSocketSession  wfss = new WarmupETIEurexSocketSession( params );
        
        return wfss;
    }

    private ETISocketSession _connOMtoExchangeGwySess;
    private ETISocketSession _exchangeTradeSess;

    public WarmupETIEurexSocketSession( String appName, String args[] ) {
        super( getProcessedParams( appName, args ), CodecId.Standard44 );
    }

    public WarmupETIEurexSocketSession( FixSimParams params ) {
        super( params, CodecId.Standard44 ); // client will use standard44
    }

    @Override
    public String getName() {
        return "ETIEurexSocketSessionWarmup";
    }
    
    @Override
    protected void init() throws SessionException, FileException, PersisterException, IOException {
        super.init();
        
        _connOMtoExchangeGwySess.init();
        _exchangeTradeSess.init();
    }
    
    @Override
    protected Session createOmToExchangeSession( MessageHandler proc, Session hub ) throws SessionException, FileException, PersisterException {
        
        ETISocketSession tradingSess = getAnOMExSession( proc, hub, "WARM_OMETIEurex",    _params.getDownPort(),   false );
        _connOMtoExchangeGwySess     = getAnOMExSession( proc, hub, "WARM_OMETIEurexGwy", _params.getDownPort()+1, true );
        
        ((ETIGatewayController) _connOMtoExchangeGwySess.getController()).setTradingSession( tradingSess );
        
        return tradingSess;
    }

    @Override
    protected void recover() {

        // EX
        RecoveryController exRct = new DummyRecoveryController();

        _connOMtoExchangeGwySess.recover( exRct );
        _connOMtoExchangeGwySess.waitForRecoveryToComplete();

        _exchangeTradeSess.recover( exRct );
        _exchangeTradeSess.waitForRecoveryToComplete();

        super.recover();
    }

    @Override
    protected void connect() {
        _exchangeTradeSess.connect();
        _exSess.connect();
        _omToClientSess.connect();
        _connOMtoExchangeGwySess.connect();
        _clientSess.connect();

        // dont connect the omToEx session yet 
        // _omToExSess.connect();
    }

    @Override
    protected void close() {
        _exchangeTradeSess.stop();
        _connOMtoExchangeGwySess.stop();
        super.close();
    }

    private ETISocketSession getAnOMExSession( MessageHandler proc, Session hub, String sesName, int port, boolean isConnGwySess ) throws SessionException, PersisterException {
        String              name              = sesName;
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        ETIEurexHFTEncoder  encoder           = getEncoder( outBuf, logHdrOut ); 
        ETIEurexHFTDecoder  decoder           = getDecoder();
        ETIEurexHFTDecoder  recoveryDecoder   = getDecoder();

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 

        int                   userName      = 1234;
        ZString               pwd           = new ViewString( "testPwd" );

        ETISocketConfig socketConfig        = new ETISocketConfig( EventRecycler.class, false, new ViewString( _params.getDownHost() ),
                                                                   null, port, userName, pwd, isConnGwySess, ETIEnv.Simulation, ETISessionMode.HF );
        
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setGatewaySession( isConnGwySess );
        setSocketPerfOptions( socketConfig );

        if ( isConnGwySess ) {
            socketConfig.setInboundPersister(  new DummyIndexPersister() ); 
            socketConfig.setOutboundPersister( new DummyIndexPersister() ); 
        } else {
            socketConfig.setInboundPersister(  createInboundPersister( name + "_EXU1_IN", ThreadPriority.Other ) ); 
            socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXU1_OUT", ThreadPriority.Other ) ); 
        }
        
        MessageDispatcher dispatcher = getSessionDispatcher( name + "Dispatcher", ThreadPriority.Other ); 

        ETISocketSession sess = new ETISocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
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
        
        sess.setLogPojos( _params.isLogPojoEvents() );

        if ( _params.isDebug() ) {
            encoder.setDebug( true );
            decoder.setDebug( true );
        }
        
        return sess;
    }
    
    @Override
    protected Session createExchangeSession( WarmupExchangeSimAdapter exSimAdapter ) throws FileException, SessionException, PersisterException {
        _exchangeTradeSess        = getAnExSession( exSimAdapter, "WARM_ETIEurexSim",    _params.getDownPort(),   false );
        ETISocketSession gwySess  = getAnExSession( exSimAdapter, "WARM_ETIEurexSimGwy", _params.getDownPort()+1, true );

        gwySess.getStateConfig().setEmulationTestPort( _params.getDownPort() );
        
        ((ETIGatewayController) gwySess.getController()).setTradingSession( _exchangeTradeSess );
        
        
        
        return gwySess;
    }

    private ETISocketSession getAnExSession( WarmupExchangeSimAdapter exSimAdapter, String sesName, int port, boolean isConnGwySess ) throws SessionException, PersisterException {
        String              name              = sesName;
        MessageRouter       inboundRouter     = new PassThruRouter( exSimAdapter ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        ETIEurexHFTEncoder  encoder           = getEncoder( outBuf, logHdrOut ); 
        ETIEurexHFTDecoder  decoder           = getDecoder();
        ETIEurexHFTDecoder  recoveryDecoder   = getDecoder();
        ThreadPriority      receiverPriority  = ThreadPriority.Other;
        
        int                 userName          = 1234;
        ZString             pwd               = new ViewString( "testPwd" );
        ETISocketConfig     socketConfig      = new ETISocketConfig( EventRecycler.class, true, new ViewString( _params.getDownHost() ),
                                                                         null, port, userName, pwd, isConnGwySess, ETIEnv.Simulation, ETISessionMode.HF );

        socketConfig.setDisconnectOnMissedHB( false );
        
        if ( isConnGwySess ) {
            socketConfig.setInboundPersister(  new DummyIndexPersister() ); 
            socketConfig.setOutboundPersister( new DummyIndexPersister() ); 
        } else {
            socketConfig.setInboundPersister(  createInboundPersister( name + "_EXETIEurexSIM_IN", ThreadPriority.Other ) ); 
            socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXETIEurexSIM_OUT", ThreadPriority.Other ) ); 
        }
        
        setSocketPerfOptions( socketConfig );

        MessageDispatcher dispatcher = getSessionDispatcher( sesName + "DISPATCHER", ThreadPriority.Other ); 

        ETISocketSession sess = new ETISocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
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
        
        sess.setLogPojos( _params.isLogPojoEvents() );

        if ( _params.isDebug() ) {
            encoder.setDebug( true );
            decoder.setDebug( true );
            
            // need reset these as debug will have created new builder instance
            encoder.setExchangeEmulationOn();
            decoder.setExchangeEmulationOn();
        }
        
        return sess;
    }
    
    @Override
    protected Decoder getDecoder( CodecId codecId ) {
        Decoder decoder = WarmupUtils.getDecoder( codecId );
        Exchange enp = ExchangeManager.instance().getByMIC( new ViewString("XPAR") );
        decoder.setInstrumentLocator( new DummyInstrumentLocator( enp ) );
        return decoder;
    }
    
    private ETIEurexHFTDecoder getDecoder() {
        ETIEurexHFTDecoder decoder = new ETIEurexHFTDecoder();
        decoder.setClientProfile( WarmupUtils.getWarmupClient() );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setLocalTimezone( TimeZone.getTimeZone( "GMT" ) );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        return decoder;
    }

    private ETIEurexHFTEncoder getEncoder( byte[] outBuf, int offset ) {
        return new ETIEurexHFTEncoder( outBuf, offset );
    }
}
