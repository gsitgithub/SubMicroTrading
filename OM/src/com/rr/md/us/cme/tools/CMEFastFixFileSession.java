/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.tools;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.codec.binary.fastfix.FastFixEncoder;
import com.rr.core.lang.ZString;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionDispatcher;
import com.rr.core.session.MultiSessionReceiver;
import com.rr.core.session.file.FileSessionConfig;
import com.rr.core.session.file.NonBlockingFileSession;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;


public class CMEFastFixFileSession extends NonBlockingFileSession {

    public CMEFastFixFileSession( String                    name, 
                                  MessageRouter             inboundRouter, 
                                  FileSessionConfig         config, 
                                  MultiSessionDispatcher    dispatcher,
                                  MultiSessionReceiver      receiver, 
                                  Encoder                   encoder, 
                                  Decoder                   decoder, 
                                  Decoder                   recoveryDecoder ) {
        
        super( name, inboundRouter, config, dispatcher, receiver, encoder, decoder, recoveryDecoder );
    }

    @Override
    public void logInboundError( Exception e ) {
        _logInErrMsg.copy( getComponentId() ).append( " lastSeqNum=" ).append( ((CMEFastFixDecoder)_decoder).getLastSeqNum() );
        _logInErrMsg.append( ' ' ).append( e.getMessage() );
        _log.error( ERR_IN_MSG, _logInErrMsg, e );
        ((FastFixDecoder)_decoder).logLastMsg();
    }

    @Override
    public void logInboundDecodingError( RuntimeDecodingException e ) {
        logInboundError( e );
    }

    @Override
    protected final void logInEvent( ZString event ) {
        if ( _logEvents ) {
            ((FastFixDecoder)_decoder).logLastMsg();
        }
    }
    
    @Override
    protected final void logOutEvent( ZString event ) {
        if ( _logEvents ) {
            ((FastFixEncoder)_encoder).logLastMsg();
        }
    }
}
