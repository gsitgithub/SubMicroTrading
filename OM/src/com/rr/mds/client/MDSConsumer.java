/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.rr.core.codec.Encoder;
import com.rr.core.dispatch.DirectDispatcherNonThreadSafe;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Instrument;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.DummyPersister;
import com.rr.core.persister.PersisterException;
import com.rr.core.pool.PoolFactory;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.ConnectionListener;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.session.socket.SocketSession;
import com.rr.core.utils.ThreadPriority;
import com.rr.mds.common.FXToUSDFromFile;
import com.rr.mds.common.MDSDecoder;
import com.rr.mds.common.MDSEncoder;
import com.rr.mds.common.MDSEventRecycler;
import com.rr.mds.common.MDSReusableType;
import com.rr.mds.common.MDSReusableTypeConstants;
import com.rr.mds.common.events.Subscribe;
import com.rr.mds.common.events.TradingRangeUpdate;

/**
 * MDS consumer/client layer 
 * 
 * @NOTE for market data critical events get the decoder to invoke the service directly
 *
 */
public class MDSConsumer {

    private static final Logger                 _log                = LoggerFactory.create( MDSConsumer.class );
    public  static final int                    DEFAULT_MDS_PORT    = 14200;
    public  static final ZString                DEFAULT_PRESUB_FILE = new ViewString( "./data/mdssubs.txt" );
    private static final ErrorCode              CANT_READ_FILE      = new ErrorCode( "MDC100", "Unable to read the presub file" );

                         InstrumentLocator      _instLocator;
                   final MessageRecycler        _inboundRecycler    = new MDSEventRecycler();
                         PoolFactory<Subscribe> _subscribePool;
    private              SocketSession          _sess;
    private        final Set<ReusableString>    _rics               = new HashSet<ReusableString>(8192);

    
    class MDSInboundServiceRouter implements MessageHandler {

        private final String _name = "MDSIn";
        
        @Override public void threadedInit() { /* nothing */ }
        @Override public boolean canHandle() { return true; }

        @Override
        public void handle( Message msg ) { // dispatcher will invoke handleNow
            handleNow( msg );                   
        }

        @Override
        public void handleNow( Message msg ) {
            switch( msg.getReusableType().getSubId() ) {
            case MDSReusableTypeConstants.SUB_ID_TRADING_BAND_UPDATE:
                TradingRangeUpdate upd = (TradingRangeUpdate)msg;
                
                Instrument inst = _instLocator.getInstrumentByRIC( upd.getRIC() );
                
                if ( inst != null ) {
                    upd.setTradingRange( inst.getValidTradingRange() );
                }

                _inboundRecycler.recycle( upd );
                break;
            case MDSReusableTypeConstants.SUB_ID_FX_SNAPSHOT:
            case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_ACTIVE_BBO:
            case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_ACTIVE_DEPTH:
            case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_SNAPSHOT_BBO:
            case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_SNAPSHOT_DEPTH:
            default:
                _inboundRecycler.recycle( msg );
                break;
            }
        }
        @Override
        public String getComponentId() {
            return _name;
        }
    }
    
    public void init( InstrumentLocator il ) {
        init( il, FXToUSDFromFile.DEFAULT_FX_TO_USD_FILE, DEFAULT_MDS_PORT, DEFAULT_PRESUB_FILE );
    }

    public void init( InstrumentLocator il, int mdsPort ) {
        init( il, FXToUSDFromFile.DEFAULT_FX_TO_USD_FILE, mdsPort, DEFAULT_PRESUB_FILE );
    }

    public void init( InstrumentLocator il, ZString fxUSDFile, int mdsPort, ZString presubFile ) {
    
        _log.info( "MDSConsumer : listening to connection from MDSServer on port " + mdsPort );

        SuperpoolManager sp = SuperpoolManager.instance();
        _subscribePool = sp.getPoolFactory( Subscribe.class );
        
        _instLocator = il;
        
        FXToUSDFromFile.load( fxUSDFile ); 
        
        loadRICS( presubFile );
        
        runServer( mdsPort );
    }

    void presub() {

        int count      = 0;

        synchronized( _rics ) {
            
            ReusableString ricChain = null;
            ReusableString tmpRIC;
            
            for( ReusableString ric : _rics ) {
    
                tmpRIC = TLC.instance().getString();
                tmpRIC.append( ric );
                
                tmpRIC.setNext( ricChain );
                ricChain = tmpRIC;
                
                ++count;
    
                if ( count % Subscribe.MAX_RIC_IN_SUB == 0) {
                    subscribe( ricChain );
                    TLC.instance().recycleChain( ricChain );
                    ricChain = null;
                }
            }
    
            if ( ricChain != null ) {
                subscribe( ricChain );
                TLC.instance().recycleChain( ricChain );
            }
        }
        
        _log.info( "Presubscribed count=" + count );
    }

    public void addRIC( ZString ric ) {
        if ( ! _rics.contains( ric ) ) {
            _rics.add( TLC.safeCopy( ric ) );
        }
    }
    
    private void loadRICS( ZString fileName ) {
        if ( fileName == null ) fileName = DEFAULT_PRESUB_FILE;
        
        File file = new File( fileName.toString() );
        
        if ( ! file.canRead() ) {
            _log.error( CANT_READ_FILE, fileName );
            
            throw new RuntimeException( "Cant read from presub file " + fileName );
        }
        
        try {
            BufferedReader input =  new BufferedReader( new FileReader(file) );

            try {
                int count      = 0;
                int badInst    = 0;
                int spaceLines = 0;
                
                String line = null; 
                
                ReusableString tmpRIC;
                
                while( ( line = input.readLine()) != null ){
                    if ( line.charAt( 0 ) == '#' ) continue;

                    line = line.trim();
                    
                    if ( line.indexOf( ' ' ) > 0 ) {
                        ++spaceLines;
                        continue; 
                    }
                    
                    tmpRIC = TLC.instance().getString();
                    tmpRIC.append( line );
                    
                    Instrument inst = _instLocator.getInstrumentByRIC( tmpRIC );
                    
                    if ( inst != null ) {

                        _rics.add( tmpRIC );
                        
                        ++count;

                    } else {
                        ++badInst;
                        TLC.instance().recycle( tmpRIC );
                    }
                }

                _log.info( "RICsub ric file has count=" + count + ", badInst=" + badInst + ", badSpaceLines=" + spaceLines );
            }
            finally {
                input.close();
            }
        }
        catch( IOException e ) {
            _log.warn( "Unable to read file " + fileName + " : " + e.getMessage() );
            throw new RuntimeException( e );
        }
        
    }

    private void subscribe( ReusableString ricChain ) {
        subscribeTradingRangeUpdates( ricChain );
    }

    public synchronized void subscribeTradingRangeUpdates( ReusableString ricChain ) {
        Subscribe sub = _subscribePool.get();
        
        sub.setType( MDSReusableType.TradingBandUpdate );
        sub.addRICChain( ricChain );
        
        _sess.handle( sub );
    }
    
    private void runServer( int mdsPort ) {

        String              name              = "MDS_CLIENT";
        MessageHandler      mdsRouter         = new MDSInboundServiceRouter();
        MessageRouter       inboundRouter     = new PassThruRouter( mdsRouter ); 
        Encoder             encoder           = new MDSEncoder( new byte[Constants.MAX_BUF_LEN], 0 );
        MDSDecoder          decoder           = new MDSDecoder();
        ThreadPriority      receiverPriority  = ThreadPriority.PriceTolerance;
        SocketConfig        socketConfig      = new SocketConfig( MDSEventRecycler.class, 
                                                                  true, 
                                                                  new ViewString( "127.0.0.1" ), 
                                                                  null,
                                                                  mdsPort );
        
        decoder.init( _instLocator );
        
        MessageDispatcher dispatcher = new DirectDispatcherNonThreadSafe();

        socketConfig.setInboundPersister(  new DummyPersister() ); 
        socketConfig.setOutboundPersister( new DummyPersister() ); 
        
        socketConfig.setSoDelayMS( 0 );

        // for OM the MD feed is not time critical so disable NIO and TCP_NO_DELAY
        socketConfig.setUseNIO( false );        
        socketConfig.setTcpNoDelay( false );    

        _sess = new SocketSession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, null, receiverPriority );

        dispatcher.setHandler( _sess );

        try {
            _sess.init();
        } catch( PersisterException e ) {
            _log.warn( "Session init error : " + e.getMessage() );  // should not be possible as using dummy persister
        }

        _sess.registerConnectionListener( new ConnectionListener() {

            @Override
            public void connected( Session session ) {
                presub();
            }

            @Override
            public void disconnected( Session session ) {
                // TODO Auto-generated method stub
                
            }} );
        
        _sess.connect();
        
    }
}
