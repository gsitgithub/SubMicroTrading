/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.loaders;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.component.SMTComponent;
import com.rr.core.component.builder.OptionalReference;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.DummyIndexPersister;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.SessionThreadedDispatcher;
import com.rr.core.utils.FileException;
import com.rr.core.utils.LoggingMessageHandler;
import com.rr.core.utils.SMTException;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ThreadPriority;
import com.rr.model.generated.codec.ETIEurexHFTDecoder;
import com.rr.model.generated.codec.ETIEurexHFTEncoder;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.om.emea.exchange.eti.gateway.ETIGatewayController;
import com.rr.om.emea.exchange.eti.trading.ETISocketConfig;
import com.rr.om.emea.exchange.eti.trading.ETISocketSession;


public class EurexHFTSessionLoader extends BaseSessionLoader {

    private ETISocketConfig             _sessionConfig;
    private ETISocketConfig             _gwyConfig;
    
    private ClientProfile               _clientProfile;
    private MessageRouter               _inboundRouter;

    private SessionThreadedDispatcher   _outboundDispatcher;
    
    private ThreadPriority              _inThreadPriority = ThreadPriority.Other;

    private String                      _etiVersion = "1.0";
    
    @OptionalReference
    private Session                     _hubSession; 

    @Override
    public SMTComponent create( String id ) throws SMTException {

        try {
            String sessName = id;
            
            _sessionConfig.validate();
            
            _sessionConfig.setETIVersion( _etiVersion );
            
            int                 logHdrOut         = AbstractSession.getDataOffset( id, false );
            byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
            ETIEurexHFTEncoder  encoder           = (ETIEurexHFTEncoder) getEncoder( _codecId, outBuf, logHdrOut, _trace ); 
            ETIEurexHFTDecoder  decoder           = (ETIEurexHFTDecoder) getDecoder( _codecId, _clientProfile, _trace );
            ETIEurexHFTDecoder  recoveryDecoder   = (ETIEurexHFTDecoder) getRecoveryDecoder( _codecId, _clientProfile, false );

            ETISocketSession sess;
            
            sess = createETISession( _codecId, sessName, encoder, decoder, recoveryDecoder );

            sess.setChainSession( _hubSession );

            sess.setLogStats( _logStats );
            sess.setLogEvents( _logEvents );
            sess.setLogPojos( _logPojoEvents );
            
            if ( _disableNanoStats ) {
                encoder.setNanoStats( false );
                decoder.setNanoStats( false );
                sess.setLogStats( false );
            } else {
                encoder.setNanoStats( true );
                decoder.setNanoStats( true );
            }
            
            if ( _trace ) {
                encoder.setDebug( true );
                decoder.setDebug( true );
            }

            postSessionCreate( encoder, sess );
            
            return sess;
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Unable to create MultiFixSession id=" + id, e );
        }
    }
    
    protected ETISocketSession createETISession( CodecId codecId,
                                                 String  sessName, 
                                                 Encoder encoder, 
                                                 Decoder decoder, 
                                                 Decoder recoveryDecoder ) throws FileException, SessionException, PersisterException {

        _sessionConfig.setInboundPersister(  createOutboundSequentialPersister(  sessName + "_" + _sessionDirection + "_IN" ) ); 
        _sessionConfig.setOutboundPersister( createOutboundSequentialPersister( sessName + "_" + _sessionDirection + "_OUT" ) ); 
        
        _sessionConfig.setGatewaySession( false );
        
        // setup for gateway session
        String gwySessName = sessName + "Gateway";
        _gwyConfig.setGatewaySession( true );

        _sessionConfig.setPort( _sessionConfig.getEmulationTestPort() );         
        
        int                 recLogHdrOut         = AbstractSession.getDataOffset( gwySessName, false );
        byte[]              recOutBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder             recEncoder           = getEncoder( codecId, recOutBuf, recLogHdrOut, _trace ); 
        Decoder             recDecoder           = getDecoder( codecId, _clientProfile, _trace );
        Decoder             recRecoveryDecoder   = getRecoveryDecoder( codecId, _clientProfile, false );
        ThreadPriority      lowPriority          = ThreadPriority.Other;

        _gwyConfig.setInboundPersister( new DummyIndexPersister() ); 
        _gwyConfig.setOutboundPersister( new DummyIndexPersister() );
        
        MessageDispatcher gwySendDispatcher = new SessionThreadedDispatcher( sessName + "Gateway", new BlockingSyncQueue(), ThreadPriority.Other );
        
        MessageRouter       recInboundRouter     = new PassThruRouter( getRecoveryMessageHandler( sessName) ); 
        
        // create reccovery session
        ETISocketSession gwySess = new ETISocketSession( gwySessName, recInboundRouter, _gwyConfig, gwySendDispatcher, recEncoder, recDecoder, recRecoveryDecoder, lowPriority );

        // create reccovery session
        ETISocketSession tradingSess = new ETISocketSession( sessName, _inboundRouter, _sessionConfig, _outboundDispatcher, encoder, decoder, recoveryDecoder, _inThreadPriority );

        ((ETIGatewayController)gwySess.getController()).setTradingSession( tradingSess );
        
        tradingSess.setChainSession( _hubSession );
        _outboundDispatcher.setHandler( tradingSess );
        
        gwySess.setChainSession( _hubSession );
        gwySendDispatcher.setHandler( gwySess );

        tradingSess.setLogPojos( _logPojoEvents );
        gwySess.setLogPojos( _logPojoEvents );
        
        gwySess.setLogStats( false );
        
        tradingSess.setGatewaySession( gwySess );
        
        return tradingSess;
    }

    private MessageHandler getRecoveryMessageHandler( String sessName ) {
        return new LoggingMessageHandler( "Recovery" + sessName );
    }

}
