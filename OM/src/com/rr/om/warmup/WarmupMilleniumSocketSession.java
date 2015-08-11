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
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.Exchange;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.Persister;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.utils.FileException;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.core.warmup.JITWarmup;
import com.rr.model.generated.codec.MilleniumLSEDecoder;
import com.rr.model.generated.codec.MilleniumLSEEncoder;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.emea.exchange.millenium.MilleniumSocketConfig;
import com.rr.om.emea.exchange.millenium.MilleniumSocketSession;
import com.rr.om.emea.exchange.millenium.SequentialPersister;
import com.rr.om.emea.exchange.millenium.recovery.MilleniumRecoveryController;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.warmup.sim.FixSimParams;
import com.rr.om.warmup.sim.WarmupExchangeSimAdapter;
import com.rr.om.warmup.sim.WarmupUtils;

public class WarmupMilleniumSocketSession extends AbstractWarmupSession implements JITWarmup {

    public static WarmupMilleniumSocketSession create( String appName, int portOffset, boolean spinLocks, int count ) {
        
        FixSimParams params = createParams( appName, portOffset, spinLocks, count );
        
        WarmupMilleniumSocketSession  wfss = new WarmupMilleniumSocketSession( params );
        
        return wfss;
    }

    public WarmupMilleniumSocketSession( String appName, String args[] ) {
        super( getProcessedParams( appName, args ), CodecId.Standard44 );
    }

    public WarmupMilleniumSocketSession( FixSimParams params ) {
        super( params, CodecId.Standard44 ); // client will be Standard44
    }

    @Override
    public String getName() {
        return "MilleniumSocketSession";
    }
    
    @Override
    protected Session createOmToExchangeSession( MessageHandler proc, Session hub ) throws SessionException, FileException, PersisterException {
        
        MilleniumSocketSession sess    = getAnOMExSession( proc, hub, "WARM_OMMillenium",    _params.getDownPort(),   false );
        MilleniumSocketSession recSess = getAnOMExSession( proc, hub, "WARM_OMMilleniumRec", _params.getDownPort()+1, true );
        
        sess.getController().setRecoveryController( (MilleniumRecoveryController) recSess.getController() );
        
        return sess;
    }

    private MilleniumSocketSession getAnOMExSession( MessageHandler proc, Session hub, String sesName, int port, boolean isRecoverySess ) throws FileException, SessionException, PersisterException {
        String            name              = sesName;
        MessageRouter     inboundRouter     = new PassThruRouter( proc ); 
        int               logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]            outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder           encoder           = getEncoder( outBuf, logHdrOut ); 
        Decoder           decoder           = getDecoder();
        Decoder           recoveryDecoder   = getDecoder();

        ThreadPriority    receiverPriority  = ThreadPriority.Other; 

        ZString               userName      = new ViewString( "testUser" );
        ZString               pwd           = new ViewString( "testPwd" );
        MilleniumSocketConfig socketConfig  = new MilleniumSocketConfig( EventRecycler.class, false, new ViewString( _params.getDownHost() ),
                                                                         null, port, userName, pwd, null, isRecoverySess );
        
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverySession( isRecoverySess );
        setSocketPerfOptions( socketConfig );

        socketConfig.setInboundPersister(  createMilleniumPersister( name + "_EXU1_IN",  "in",  ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createMilleniumPersister( name + "_EXU1_OUT", "out", ThreadPriority.Other ) ); 
        
        MessageDispatcher dispatcher = getSessionDispatcher( name + "Dispatcher", ThreadPriority.Other ); 

        MilleniumSocketSession sess = new MilleniumSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
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
        
        return sess;
    }
    
    @Override
    protected Session createExchangeSession( WarmupExchangeSimAdapter exSimAdapter ) throws FileException, SessionException, PersisterException {
        MilleniumSocketSession sess    = getAnExSession( exSimAdapter, "WARM_MilleniumSim",    _params.getDownPort(),   false );
        MilleniumSocketSession recSess = getAnExSession( exSimAdapter, "WARM_MilleniumSimRec", _params.getDownPort()+1, true );
        
        sess.getController().setRecoveryController( (MilleniumRecoveryController) recSess.getController() );
        
        return sess;
    }

    private MilleniumSocketSession getAnExSession( WarmupExchangeSimAdapter exSimAdapter, String sesName, int port, boolean isRecoverySess ) throws FileException, SessionException, PersisterException {
        String              name              = sesName;
        MessageRouter       inboundRouter     = new PassThruRouter( exSimAdapter ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder             encoder           = getEncoder( outBuf, logHdrOut ); 
        Decoder             decoder           = getDecoder();
        Decoder             recoveryDecoder   = getDecoder();
        ThreadPriority      receiverPriority  = ThreadPriority.Other;
        
        ZString               userName      = new ViewString( "testUser" );
        ZString               pwd           = new ViewString( "testPwd" );
        MilleniumSocketConfig socketConfig  = new MilleniumSocketConfig( EventRecycler.class, true, new ViewString( _params.getDownHost() ),
                                                                         null, port, userName, pwd, null, isRecoverySess );

        socketConfig.setDisconnectOnMissedHB( false );
        
        MessageDispatcher dispatcher = getSessionDispatcher( sesName + "DISPATCHER", ThreadPriority.Other ); 

        socketConfig.setInboundPersister(  createMilleniumPersister( name + "_EXMilleniumSIM_IN",  "in",  ThreadPriority.Other ) ); 
        socketConfig.setOutboundPersister( createMilleniumPersister( name + "_EXMilleniumSIM_OUT", "out", ThreadPriority.Other ) ); 
        
        setSocketPerfOptions( socketConfig );

        MilleniumSocketSession sess = new MilleniumSocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
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
        
        return sess;
    }
    
    private Persister createMilleniumPersister( String id, String direction, ThreadPriority priority ) throws FileException {
        ReusableString fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/" ).append( direction ).append( "/" ).append( id ).append( ".dat" );
        if ( _params.isRemovePersistence() ) FileUtils.rm( fileName.toString() );
        SequentialPersister persister = new SequentialPersister( new ViewString( id ), 
                                                               fileName, 
                                                               _params.getPersistDatPreSize(),
                                                               _params.getPersistDatPageSize(), 
                                                               priority );
        return persister;
    }

    @Override
    protected Decoder getDecoder( CodecId id ) {
        Decoder decoder = WarmupUtils.getDecoder( id );
        Exchange enp = ExchangeManager.instance().getByMIC( new ViewString("XPAR") );
        decoder.setInstrumentLocator( new DummyInstrumentLocator( enp ) );
        return decoder;
    }
    
    private Decoder getDecoder() {
        MilleniumLSEDecoder decoder = new MilleniumLSEDecoder();
        decoder.setClientProfile( WarmupUtils.getWarmupClient() );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setLocalTimezone( TimeZone.getTimeZone( "GMT" ) );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        return decoder;
    }

    private Encoder getEncoder( byte[] outBuf, int offset ) {
        return new MilleniumLSEEncoder( outBuf, offset );
    }
}
