/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.file;

import java.io.IOException;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.MessageQueue;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionDispatcher;
import com.rr.core.session.MultiSessionReceiver;
import com.rr.core.session.NonBlockingSession;

public class NonBlockingFileSession extends BaseFileSession implements NonBlockingSession {

    private       MessageQueue          _queue = new ConcLinkedMsgQueueSingle();
    
    public NonBlockingFileSession( String                   name, 
                                   MessageRouter            inboundRouter, 
                                   FileSessionConfig        config, 
                                   MultiSessionDispatcher   dispatcher,
                                   MultiSessionReceiver     receiver,
                                   Encoder                  encoder,
                                   Decoder                  decoder, 
                                   Decoder                  recoveryDecoder ) {
        
        super( name, inboundRouter, config, dispatcher, encoder, decoder, recoveryDecoder );
        
        attachReceiver( receiver );
        receiver.addSession( this );
    }

    @Override public boolean isMsgPendingWrite() { return false; }
    @Override public void retryCompleteWrite() throws IOException { /* nothing */ }
    
    @Override
    public void logOutboundEncodingError( RuntimeEncodingException e ) {
        _logOutErrMsg.copy( getComponentId() ).append( ' ' ).append( e.getMessage() ).append( ":: " );
        _log.error( ERR_OUT_MSG, _logOutErrMsg, e );
    }

    @Override
    public MessageQueue getSendQueue() {
        return _queue;
    }

    @Override
    public MessageQueue getSendSyncQueue() {
        return _queue;
    }
}
