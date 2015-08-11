/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.fixsocket;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.SessionException;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.om.session.AbstractNonBlockingSocketSession;

/**
 * on disconnect its possible to loose the buffered messages, its up to Fix or Exchange protocol to re-request
 */
public class NonBlockingFixSocketSession extends AbstractNonBlockingSocketSession<FixController,FixSocketConfig> {

    private static final int INITIAL_READ_BYTES = 30;

    public NonBlockingFixSocketSession( String                          name, 
                                        MessageRouter                   inboundRouter, 
                                        FixSocketConfig                 fixConfig, 
                                        MultiSessionThreadedDispatcher  dispatcher,
                                        MultiSessionThreadedReceiver    receiver,
                                        FixEncoder                      encoder,
                                        FixDecoder                      decoder, 
                                        Decoder                         recoveryDecoder,
                                        MessageQueue                    dispatchQueue ) throws SessionException, PersisterException {
        
        super( name, inboundRouter, fixConfig, dispatcher, receiver, encoder, decoder, recoveryDecoder, INITIAL_READ_BYTES, dispatchQueue );
        
        encoder.setSenderCompId( fixConfig.getSenderCompId() );
        encoder.setSenderSubId(  fixConfig.getSenderSubId() );
        encoder.setTargetCompId( fixConfig.getTargetCompId() );
        encoder.setTargetSubId(  fixConfig.getTargetSubId() );
        
        decoder.setSenderCompId( fixConfig.getSenderCompId() );
        decoder.setSenderSubId(  fixConfig.getSenderSubId() );
        decoder.setTargetCompId( fixConfig.getTargetCompId() );
        decoder.setTargetSubId(  fixConfig.getTargetSubId() );
    }

    @Override
    public int getLastSeqNumProcessed() {
        return _controller.getNextExpectedInSeqNo() - 1;  
    }
    
    @Override
    protected final FixController createSessionContoller() {
        return new FixController( this, _config );
    }
    
    @Override
    protected final int setOutSeqNum( final Message msg ) {
        final int nextOut = _controller.getAndIncNextOutSeqNum();
        msg.setMsgSeqNum( nextOut );
        return nextOut;
    }
    
    @Override
    public final boolean isSessionMessage( Message msg ) {
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_HEARTBEAT:
        case EventIds.ID_TESTREQUEST:
        case EventIds.ID_RESENDREQUEST:
        case EventIds.ID_SESSIONREJECT:
        case EventIds.ID_SEQUENCERESET:
        case EventIds.ID_LOGOUT:
        case EventIds.ID_LOGON:
            return true;
        }
        return false;
    }

    @Override
    protected final void logInEvent( ZString event ) {
        if ( _logEvents ) {
            _log.infoLarge( event );
        }
    }
    
    @Override
    protected final void logOutEvent( ZString event ) {
        if ( _logEvents ) {
            _log.infoLarge( event );
        }
    }
}
