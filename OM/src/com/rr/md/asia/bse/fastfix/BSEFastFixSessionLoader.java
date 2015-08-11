/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.asia.bse.fastfix;

import java.util.BitSet;

import com.rr.core.component.SMTComponent;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.SessionDirection;
import com.rr.core.utils.SMTException;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.md.asia.bse.fastfix.reader.BSEFastFixDecoder;
import com.rr.md.fastfix.FastSocketConfig;
import com.rr.om.loaders.BaseSessionLoader;


public class BSEFastFixSessionLoader extends BaseSessionLoader {

    private FastSocketConfig     _sessionConfig;
    private MessageRouter        _inboundRouter;
    private String               _subChannelOnMask;

    private MultiSessionThreadedReceiver    _inboundDispatcher;
    
    private String      _templateFile;
    
    private boolean _logIntermediateFix        = true;

    
    public BSEFastFixSessionLoader() {
        super();
        
        _sessionDirection = SessionDirection.Upstream;

        _dummyPersister = true;
    }
    
    @Override
    public SMTComponent create( String id ) throws SMTException {

        try {
            prep();
            
            _sessionConfig.validate();
            
            BitSet bs = null;
            
            if ( _subChannelOnMask != null && ! _subChannelOnMask.equals( "-1" ) ) {
                bs = new BitSet();

                String[] bitsOn = _subChannelOnMask.split( "," );
                
                for( String b : bitsOn ) {
                    int flag = Integer.parseInt( b.trim() );
                    
                    bs.set( flag );
                }
            }
            
            setMulticastGroups( _sessionConfig );
            
            BSEFastFixDecoder            decoder  = new BSEFastFixDecoder( id, _templateFile, bs, _trace, _logIntermediateFix );
            BSENonBlockingFastFixSession sess     = new BSENonBlockingFastFixSession( id, _inboundRouter, _sessionConfig, _inboundDispatcher, decoder );

            sess.setLogStats( _logStats );
            sess.setLogEvents( _logEvents );
            sess.setLogPojos( _logPojoEvents );
            
            if ( _disableNanoStats ) {
                decoder.setNanoStats( false );
                sess.setLogStats( false );
            } else {
                decoder.setNanoStats( true );
            }

            postSessionCreate( null, sess );
            
            return sess;
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Unable to create BSEFastFixSession id=" + id, e );
        }
    }
}
