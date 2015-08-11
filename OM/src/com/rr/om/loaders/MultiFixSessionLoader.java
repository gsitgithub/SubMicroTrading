/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.loaders;

import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.collections.MessageQueue;
import com.rr.core.component.SMTComponent;
import com.rr.core.component.builder.OptionalReference;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.ClientProfile;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.utils.SMTException;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.om.session.fixsocket.FixSocketConfig;
import com.rr.om.session.fixsocket.NonBlockingFixSocketSession;


public class MultiFixSessionLoader extends BaseSessionLoader {

    private FixSocketConfig      _sessionConfig;
    private ClientProfile        _clientProfile;
    private MessageRouter        _inboundRouter;

    private MultiSessionThreadedDispatcher  _outboundDispatcher;
    private MultiSessionThreadedReceiver    _inboundDispatcher;
    
    private MessageQueue                    _queue;
    
    @OptionalReference
    private Session                         _hubSession; 

    @Override
    public SMTComponent create( String id ) throws SMTException {

        try {
            String sessName = id;
            
            _sessionConfig.setFixVersion( _codecId.getFixVersion() );
            _sessionConfig.setCodecId( _codecId );
            
            _sessionConfig.validate();
            
            int                 logHdrOut         = AbstractSession.getDataOffset( id, false );
            byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
            FixEncoder          encoder           = (FixEncoder) getEncoder( _codecId, outBuf, logHdrOut, _trace ); 
            FixDecoder          decoder           = (FixDecoder) getDecoder( _codecId, _clientProfile, _trace );
            FixDecoder          recoveryDecoder   = (FixDecoder) getRecoveryDecoder( _codecId, _clientProfile, false );

            decoder.setValidateChecksum( true );
            recoveryDecoder.setValidateChecksum( true );
            
            _sessionConfig.setInboundPersister(  createInboundPersister(  sessName + "_" + _sessionDirection + "_IN" ) ); 
            _sessionConfig.setOutboundPersister( createOutboundPersister( sessName + "_" + _sessionDirection + "_OUT" ) ); 
            
            NonBlockingFixSocketSession sess;
            
            sess = createNonBlockingFixSession( _sessionConfig, _inboundRouter, encoder, decoder, recoveryDecoder, sessName, _outboundDispatcher, _inboundDispatcher, _queue );

            sess.setChainSession( _hubSession );
            _outboundDispatcher.addSession( sess );

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

            postSessionCreate( encoder, sess );
            
            return sess;
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Unable to create MultiFixSession id=" + id, e );
        }
    }
    
    protected NonBlockingFixSocketSession createNonBlockingFixSession( FixSocketConfig                socketConfig, 
                                                                       MessageRouter                  inboundRouter, 
                                                                       FixEncoder                     encoder,
                                                                       FixDecoder                     decoder, 
                                                                       FixDecoder                     recoveryDecoder, 
                                                                       String                         name,
                                                                       MultiSessionThreadedDispatcher dispatcher, 
                                                                       MultiSessionThreadedReceiver   receiver,
                                                                       MessageQueue                   dispatchQueue ) throws SessionException, PersisterException {
        
        NonBlockingFixSocketSession sess = new NonBlockingFixSocketSession( name, 
                                                                            inboundRouter, 
                                                                            socketConfig, 
                                                                            dispatcher,
                                                                            receiver,
                                                                            encoder, 
                                                                            decoder, 
                                                                            recoveryDecoder,
                                                                            dispatchQueue );
        return sess;
    }
}
