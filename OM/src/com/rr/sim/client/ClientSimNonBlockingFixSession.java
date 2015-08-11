/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.sim.client;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.collections.MessageQueue;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.SessionException;
import com.rr.om.session.fixsocket.FixSocketConfig;
import com.rr.om.session.fixsocket.NonBlockingFixSocketSession;

public class ClientSimNonBlockingFixSession extends NonBlockingFixSocketSession {

    public interface EventListener {
        public void sent( Message msg, long sent );
    }
    
    private EventListener _listener = null;
    
    public ClientSimNonBlockingFixSession( String                           name, 
                                           MessageRouter                    inboundRouter, 
                                           FixSocketConfig                  fixConfig, 
                                           MultiSessionThreadedDispatcher   dispatcher,
                                           MultiSessionThreadedReceiver     receiver, 
                                           FixEncoder                       encoder, 
                                           FixDecoder                       decoder, 
                                           Decoder                          recoveryDecoder,
                                           MessageQueue                     dispatchQueue ) throws SessionException,
                                                                                                                                                   PersisterException {
        super( name, inboundRouter, fixConfig, dispatcher, receiver, encoder, decoder, recoveryDecoder, dispatchQueue );
    }
    
    public EventListener getListener() {
        return _listener;
    }

    public void setListener( EventListener listener ) {
        _listener = listener;
    }

    @Override
    protected void postSocketWriteActions( final Message msg, int startEncodeIdx, final int totLimit ) {
        super.postSocketWriteActions( msg, startEncodeIdx, totLimit );

        if ( _listener != null ) {
            _listener.sent( msg, getLastSent() );
        }
    }
    
}
