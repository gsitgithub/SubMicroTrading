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
import com.rr.core.component.SMTComponent;
import com.rr.core.component.builder.OptionalReference;
import com.rr.core.model.ClientProfile;
import com.rr.core.session.MessageRouter;
import com.rr.core.utils.SMTException;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.om.client.DummyClientProfile;
import com.rr.om.session.fixsocket.FixSocketConfig;
import com.rr.om.session.jmx.JMXSession;


public class JMXSessionLoader extends BaseSessionLoader {

    @OptionalReference
    private MessageRouter   _inboundRouter;

    @OptionalReference
    private ClientProfile   _clientProfile = new DummyClientProfile();

    @Override
    public SMTComponent create( String id ) throws SMTException {

        try {
            String sessName = id;
            
            Decoder    decoder       = getRecoveryDecoder( _codecId, _clientProfile, true );
            
            FixSocketConfig  config = new FixSocketConfig();
            config.setFixVersion( _codecId.getFixVersion() );
            config.setCodecId( _codecId );
            
            JMXSession sess = new JMXSession( sessName, config, _inboundRouter, decoder, _sessionManager );

            postSessionCreate( null, sess );
            
            return sess;
            
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Unable to create MultiFixSession id=" + id, e );
        }
    }
    

}
