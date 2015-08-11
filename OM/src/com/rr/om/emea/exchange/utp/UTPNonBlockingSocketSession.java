/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.utp;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SessionStateException;
import com.rr.om.session.AbstractNonBlockingSocketSession;

/**
 * on disconnect its possible to loose the buffered messages, its up to Fix or Exchange protocol to re-request
 */
public class UTPNonBlockingSocketSession extends AbstractNonBlockingSocketSession<UTPController,UTPSocketConfig> {

    private static final int INITIAL_READ_BYTES = 8;

    public UTPNonBlockingSocketSession( String                          name, 
                                        MessageRouter                   inboundRouter, 
                                        UTPSocketConfig                 fixConfig, 
                                        MultiSessionThreadedDispatcher  dispatcher,
                                        MultiSessionThreadedReceiver    receiver,
                                        Encoder                         encoder,
                                        Decoder                         decoder, 
                                        Decoder                         recoveryDecoder,
                                        MessageQueue                    dispatchQueue ) throws SessionException, PersisterException {
        
        super( name, inboundRouter, fixConfig, dispatcher, receiver, encoder, decoder, recoveryDecoder, INITIAL_READ_BYTES, dispatchQueue );
    }

    @Override public int getLastSeqNumProcessed() { return 0; }
    
    @Override
    protected final UTPController createSessionContoller() {
        return new UTPController( this, _config );
    }
    
    @Override
    protected final int setOutSeqNum( final Message msg ) {
        return UTPCommonSessionUtils.setOutSeqNum( _controller, msg );
    }
    
    @Override
    public final boolean isSessionMessage( Message msg ) {
        return UTPCommonSessionUtils.isSessionMessage( msg );
    }
    
    @Override
    protected final void logInEvent( ZString event ) {
        if ( _logEvents ) {
            _log.infoLargeAsHex( event, _inHdrLen );
        }
    }
    
    @Override
    protected final void logOutEvent( ZString event ) {
        if ( _logEvents ) {
            _log.infoLargeAsHex( event, _outHdrLen );
        }
    }
    
    @Override
    protected void postSocketWriteActions( final Message msg, int startEncodeIdx, final int totLimit ) {
        super.postSocketWriteActions( msg, startEncodeIdx, totLimit );
        logOutEventPojo( msg );
    }
    
    @Override
    protected void invokeController( Message msg ) throws SessionStateException {
        logInEventPojo( msg );                          // must log event BEFORE controller called
        super.invokeController( msg );
    }
}
