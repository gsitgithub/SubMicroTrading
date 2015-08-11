/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SessionStateException;
import com.rr.model.generated.codec.MilleniumLSEDecoder;
import com.rr.om.session.AbstractNonBlockingSocketSession;

public class MilleniumNonBlockingSocketSession extends AbstractNonBlockingSocketSession<MilleniumController,MilleniumSocketConfig> {

    private static final int INITIAL_READ_BYTES = 4;

    private final ReusableString msgContext = new ReusableString();
    
    public MilleniumNonBlockingSocketSession( String                          name, 
                                              MessageRouter                   inboundRouter, 
                                              MilleniumSocketConfig           millConfig, 
                                              MultiSessionThreadedDispatcher  dispatcher,
                                              MultiSessionThreadedReceiver    receiver,
                                              Encoder                         encoder,
                                              Decoder                         decoder, 
                                              Decoder                         recoveryDecoder,
                                              MessageQueue                    dispatchQueue ) throws SessionException, PersisterException {
        
        super( name, inboundRouter, millConfig, dispatcher, receiver, encoder, decoder, recoveryDecoder, INITIAL_READ_BYTES, dispatchQueue );
    }

    @Override public int getLastSeqNumProcessed() { return 0; }
    
    @Override
    public final boolean isSessionMessage( Message msg ) {
        return MilleniumCommonSessionUtils.isSessionMessage( msg );
    }
    
    @Override
    protected final MilleniumController createSessionContoller() {
        return MilleniumCommonSessionUtils.createSessionController( this, _config );        
    }
    
    @Override
    protected final int setOutSeqNum( final Message msg ) {
        return 0;
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
        _controller.handle( msg, ((MilleniumLSEDecoder)_decoder).getAppId() );
    }
    
    @Override
    protected synchronized Message recoveryDecode( byte[] buf, int offset, int len, boolean inBound ) {
        return MilleniumCommonSessionUtils.recoveryDecode( _controller, (MilleniumLSEDecoder) _recoveryDecoder, buf, offset, len, inBound );
    }
    
    /**
     * specialisation to persist linkId as millenium doesnt have anywhere the srcLinkId can be stored for mktNOS
     */
    @Override
    protected long persistOutRec( int nextOut, final int encodedLen, final int startEncodeIdx, Message msg ) throws PersisterException {
        MilleniumCommonSessionUtils.getContextForOutPersist( msg, msgContext );
        return getOutboundPersister().persistIdxAndRec( nextOut, _outBuffer, startEncodeIdx, encodedLen, msgContext.getBytes(), 0, msgContext.length() );
    }
    
    /**
     * for Millenium the only message with extra context is the mkt NOS which stores the srcLinkId
     */
    @Override
    protected Message recoveryDecodeWithContext( byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, boolean inBound ) {
        final Message msg = recoveryDecode( buf, offset, len, inBound );
        MilleniumCommonSessionUtils.enrichRecoveredContext( msg, opt, optOffset, optLen );
        return msg;
    }
    
    @Override
    protected void errorDumpMsg( ReusableString logInMsg, RuntimeDecodingException rde ) {
        _logInMsg.append( ' ' ).appendHEX( rde.getFixMsg() );
    }
}
