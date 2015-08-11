/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup;

import java.util.TimeZone;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.Exchange;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.utils.FileException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.core.warmup.JITWarmup;
import com.rr.model.generated.codec.UTPEuronextCashDecoder;
import com.rr.model.generated.codec.UTPEuronextCashEncoder;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.emea.exchange.utp.UTPSocketConfig;
import com.rr.om.emea.exchange.utp.UTPSocketSession;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.warmup.sim.FixSimParams;
import com.rr.om.warmup.sim.WarmupExchangeSimAdapter;
import com.rr.om.warmup.sim.WarmupUtils;

public class WarmupUTPSocketSession extends AbstractWarmupSession implements JITWarmup {

    public static WarmupUTPSocketSession create( String appName, int portOffset, boolean spinLocks, int count ) {
        
        FixSimParams params = createParams( appName, portOffset, spinLocks, count );
        
        WarmupUTPSocketSession  wfss = new WarmupUTPSocketSession( params );
        
        return wfss;
    }

    public WarmupUTPSocketSession( String appName, String args[] ) {
        super( getProcessedParams( appName, args ), CodecId.Standard44 );
    }

    public WarmupUTPSocketSession( FixSimParams params ) {
        super( params, CodecId.Standard44 );
    }

    @Override
    public String getName() {
        return "UTPSocketSession";
    }
    
    @Override
    protected Session createOmToExchangeSession( MessageHandler proc, Session hub ) throws SessionException, FileException, PersisterException {
        String            name              = "WARM_OMUTP";
        MessageRouter     inboundRouter     = new PassThruRouter( proc ); 
        int               logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder           encoder           = getEncoder( outBuf, logHdrOut ); 
        Decoder           decoder           = getDecoder();
        Decoder           recoveryDecoder   = getDecoder();

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 

        ZString             userName          = new ViewString( "" );
        UTPSocketConfig     socketConfig      = new UTPSocketConfig( EventRecycler.class, 
                                                                     false, 
                                                                     new ViewString( _params.getDownHost() ),
                                                                     null,
                                                                     _params.getDownPort(),
                                                                     userName );
        
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );
        setSocketPerfOptions( socketConfig );

        socketConfig.setInboundPersister(  createInboundPersister( name + "_EXU1_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXU1_OUT", ThreadPriority.Other ) ); 
        
        MessageDispatcher dispatcher = getSessionDispatcher( "WARM_OM2EX_UTPDISPATCHER", ThreadPriority.Other ); 

        UTPSocketSession sess = new UTPSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
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
        String              name              = "WARM_UTPSIM";
        MessageRouter       inboundRouter     = new PassThruRouter( exSimAdapter ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder             encoder           = getEncoder( outBuf, logHdrOut ); 
        Decoder             decoder           = getDecoder();
        Decoder             recoveryDecoder   = getDecoder();
        ThreadPriority      receiverPriority  = ThreadPriority.Other;
        ZString             userName          = new ViewString( "" );

        UTPSocketConfig     socketConfig      = new UTPSocketConfig( EventRecycler.class, 
                                                                     true, 
                                                                     new ViewString( _params.getDownHost() ), 
                                                                     null,
                                                                     _params.getDownPort(),
                                                                     userName );
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );
        
        MessageDispatcher dispatcher = getSessionDispatcher( "WARMUP_UTPSIM_DISPATCHER", ThreadPriority.Other ); 

        socketConfig.setInboundPersister(  createInboundPersister( name + "_EXUTPSIM_IN", ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( name + "_EXUTPSIM_OUT", ThreadPriority.Other ) ); 
        
        setSocketPerfOptions( socketConfig );

        UTPSocketSession sess = new UTPSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
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

    @Override
    protected Decoder getDecoder( CodecId id ) {
        Decoder decoder = WarmupUtils.getDecoder( id );
        Exchange enp = ExchangeManager.instance().getByMIC( new ViewString("XPAR") );
        decoder.setInstrumentLocator( new DummyInstrumentLocator( enp ) );
        return decoder;
    }
    
    private Decoder getDecoder() {
        UTPEuronextCashDecoder decoder = new UTPEuronextCashDecoder();
        decoder.setClientProfile( WarmupUtils.getWarmupClient() );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setLocalTimezone( TimeZone.getTimeZone( "CET" ) );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        return decoder;
    }

    private Encoder getEncoder( byte[] outBuf, int offset ) {
        return new UTPEuronextCashEncoder( outBuf, offset );
    }
}
