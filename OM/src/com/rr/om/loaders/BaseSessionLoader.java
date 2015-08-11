/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.loaders;

import java.util.TimeZone;

import com.rr.core.codec.BinaryDecoder;
import com.rr.core.codec.BinaryEncoder;
import com.rr.core.codec.CodecFactory;
import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Exchange;
import com.rr.core.persister.DummyIndexPersister;
import com.rr.core.persister.IndexPersister;
import com.rr.core.persister.Persister;
import com.rr.core.persister.memmap.IndexMMPersister;
import com.rr.core.persister.memmap.MemMapPersister;
import com.rr.core.session.Session;
import com.rr.core.session.SessionDirection;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.FileException;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.inst.InstrumentStore;
import com.rr.model.generated.fix.codec.CMEEncoder;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.om.emea.exchange.millenium.SequentialPersister;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.session.SessionManager;


/**
 * remember properties HAVE to have the SAME name as config entries to be autowired !
 *
 */
public abstract class BaseSessionLoader implements SMTSingleComponentLoader {
   
    private static final Logger         _console             = LoggerFactory.console( BaseSessionLoader.class );
    private static       ViewString     _persistFileNameBase = new ViewString( "./persist/daily" );
    
    protected boolean _logEvents        = true;
    protected boolean _logPojoEvents    = true;
    protected boolean _logStats         = true;
    protected boolean _trace            = false; 
    

    protected boolean _disableNanoStats = false;

    protected CodecId             _codecId = CodecId.Standard44;
    protected ExchangeManager     _exchangeManager;
    protected SessionManager      _sessionManager;
    protected SessionDirection    _sessionDirection = SessionDirection.Downstream;
    protected String              _multicastGroups;

    protected String              _rec; // exchange REC
    protected boolean             _dummyPersister = false;

    private int                   _persistDatPageSize;
    private long                  _persistIdxPreSize;
    private long                  _persistDatPreSize;
    private int                   _expectedOrders = 1024 * 1024;
    private boolean               _forceRemovePersistence = false;
    private ThreadPriority        _persistThreadPriority = ThreadPriority.MemMapAllocator;

    private final byte[]          _dateStrBytes;
    private final String          _dateStr;

    private TimeZone              _timeZone;
    private Exchange              _ex;
    

    /**
     * mem vars to be set via reflection .. should be auto set
     */
    private CodecFactory        _codecFactory;
    private InstrumentStore     _instrumentStore;
    
    
    public BaseSessionLoader() {
        // @TODO move TimeZone stuff into new component
        
        _dateStrBytes = TimeZoneCalculator.instance().getDateLocal();
        _dateStr      = new String( _dateStrBytes );
    }
    
    protected void prep() {
        _ex = ExchangeManager.instance().getByREC( new ViewString(_rec) ); 
        _timeZone = _ex.getTimeZone();
    }
    
    private void calcSizes() {
        long estDataSize = 256l * _expectedOrders * 8;
        
        int defPageSize = 10000000;
        long estPageSize = 4096l + Math.abs( estDataSize / 10);

       // dont overwrite values that may have been in config
        
        if ( _persistDatPageSize == 0 ) _persistDatPageSize = (estPageSize < defPageSize) ? (int)estPageSize : defPageSize;
        if ( _persistIdxPreSize == 0 )  _persistIdxPreSize  = 1024l * 1024l + (_expectedOrders*8*16);
        if ( _persistDatPreSize == 0 )  _persistDatPreSize  = 1024l * 1024l + estDataSize;
    }
    
    protected IndexPersister createInboundPersister( String id ) throws FileException {

        calcSizes();
        
        if ( _dummyPersister ) {
            _console.info( "Using dummy inbound persister for " + id );
            return new DummyIndexPersister();
        }

        ReusableString fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/in/" ).append( id ).append( ".dat" );
        if ( _forceRemovePersistence ) FileUtils.rm( fileName.toString() );
        MemMapPersister persister = new MemMapPersister( new ViewString( id ), 
                                                         fileName, 
                                                         _persistDatPreSize,
                                                         _persistDatPageSize, 
                                                         _persistThreadPriority );
        
        fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/in/" ).append( id ).append( ".idx" );
        if ( _forceRemovePersistence ) FileUtils.rm( fileName.toString() );
        IndexPersister indexPersister = new IndexMMPersister( persister, 
                                                              new ViewString( "IDX_" + id ), 
                                                              fileName, 
                                                              _persistIdxPreSize,
                                                              _persistThreadPriority );
        
        return indexPersister;
    }

    protected Persister createOutboundSequentialPersister( String id ) throws FileException {

        ReusableString fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/out/" ).append( id ).append( ".dat" );
        if ( _forceRemovePersistence ) FileUtils.rm( fileName.toString() );
        SequentialPersister persister = new SequentialPersister( new ViewString( id ), 
                                                                 fileName, 
                                                                 _persistDatPreSize,
                                                                 _persistDatPageSize, 
                                                                 _persistThreadPriority );
        
        return persister;
    }

    protected IndexPersister createOutboundPersister( String id ) throws FileException {

        calcSizes();

        if ( _dummyPersister ) {
            _console.info( "Using dummy outbound persister for " + id );
            return new DummyIndexPersister();
        }

        ReusableString fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/out/" ).append( id ).append( ".dat" );
        if ( _forceRemovePersistence ) FileUtils.rm( fileName.toString() );
        MemMapPersister persister = new MemMapPersister( new ViewString( id ), 
                                                         fileName, 
                                                         _persistDatPreSize,
                                                         _persistDatPageSize, 
                                                         _persistThreadPriority );
        
        fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/out/" ).append( id ).append( ".idx" );
        if ( _forceRemovePersistence ) FileUtils.rm( fileName.toString() );
        IndexPersister indexPersister = new IndexMMPersister( persister, 
                                                              new ViewString( "IDX_" + id ), 
                                                              fileName, 
                                                              _persistIdxPreSize,
                                                              _persistThreadPriority );
        
        return indexPersister;
    }
    

    protected Decoder getDecoder( CodecId id, ClientProfile client, boolean debug ) {
        Decoder decoder = _codecFactory.getDecoder( id );
        decoder.setClientProfile( client );
        decoder.setInstrumentLocator( _instrumentStore );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setLocalTimezone( _timeZone );
        calc.setDate( _dateStr );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        if ( decoder instanceof BinaryDecoder ) {
            ((BinaryDecoder)decoder).setDebug( debug );
        }
        return decoder;
    }

    protected Decoder getRecoveryDecoder( CodecId id, ClientProfile client, boolean debug ) {
        Decoder decoder = _codecFactory.getRecoveryDecoder( id );
        decoder.setClientProfile( client );
        decoder.setInstrumentLocator( _instrumentStore );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setLocalTimezone( _timeZone );
        calc.setDate( _dateStr );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        if ( decoder instanceof BinaryDecoder ) {
            ((BinaryDecoder)decoder).setDebug( debug );
        }
        return decoder;
    }

    protected Encoder getEncoder( CodecId id, byte[] buf, int offset, boolean debug ) {
        Encoder encoder = _codecFactory.getEncoder( id, buf, offset );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setLocalTimezone( _timeZone );
        calc.setDate( _dateStr );
        encoder.setTimeZoneCalculator( calc );
        if ( encoder instanceof BinaryEncoder ) {
            ((BinaryEncoder)encoder).setDebug( debug );
        }
        return encoder;
    }
    
    protected void postSessionCreate( Encoder encoder, Session sess ) {
        
        if ( _codecId == CodecId.CME && encoder != null ) {
            ((CMEEncoder)encoder).setSession( (SeqNumSession) sess );
        }
        
        if ( _sessionDirection == SessionDirection.DropCopy ) {
            _sessionManager.setHub( sess );
        } else {
            boolean isDown = _sessionDirection == SessionDirection.Downstream;
            
            _sessionManager.add( sess, isDown );
            
            if ( isDown ) {
                registerExchangeSession( _sessionManager, sess );
            }
        }
    }
    
    protected void registerExchangeSession( SessionManager sessMgr, Session sess ) {
        if ( _rec != null ) {
            Exchange e = ExchangeManager.instance().getByREC( new ViewString(_rec) );
            
            if ( e == null ) {
                throw new SMTRuntimeException( "Session " + sess.getComponentId() + " exchangeREC=" + _rec + " exchange not registered with manager");
            }
            
            sessMgr.associateExchange( sess, e );
        }
    }

    protected void setMulticastGroups( SocketConfig socketCfg ) {
        if ( _multicastGroups != null ) {
            String[] parts = _multicastGroups.split( "," );
            ZString[] grps = new ZString[ parts.length ];
            for( int i=0 ; i < parts.length ; i++ ) {
                grps[ i ] = new ViewString( parts[i].trim() );
            }
            socketCfg.setMulticastGroups( grps );
        }
    }
}
