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
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SessionStateException;
import com.rr.core.utils.ThreadPriority;
import com.rr.model.generated.codec.MilleniumLSEDecoder;
import com.rr.om.session.state.AbstractStatefulSocketSession;

// @TODO detangle index based seqnum persistence from base class into persist helper .. not relevant for millenium

public class MilleniumSocketSession extends AbstractStatefulSocketSession<MilleniumController,MilleniumSocketConfig> {

    private static final int            INITIAL_READ_BYTES = 4;

    private final ReusableString _msgContext = new ReusableString();
    
    public MilleniumSocketSession( String                   name, 
                                   MessageRouter            inboundRouter, 
                                   MilleniumSocketConfig    millConfig, 
                                   MessageDispatcher        dispatcher, 
                                   Encoder                  encoder, 
                                   Decoder                  decoder,
                                   Decoder                  recoveryDecoder, 
                                   ThreadPriority           receiverPriority ) throws SessionException, PersisterException {
        
        super( name, inboundRouter, millConfig, dispatcher, encoder, decoder, recoveryDecoder, receiverPriority, INITIAL_READ_BYTES );
        
        if ( _inPersister.getClass() != SequentialPersister.class ) {
            throw new SessionException( "inbound Persister must be of type SequentialPersister" );
        }
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
    
    // @NOTE ideally decoder should support enrichment of a generic context type, for Millenium this would hold the appId 
    
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
        MilleniumCommonSessionUtils.getContextForOutPersist( msg, _msgContext );
        return getOutboundPersister().persistIdxAndRec( nextOut, _outBuffer, startEncodeIdx, encodedLen, _msgContext.getBytes(), 0, _msgContext.length() );
    }
    
    @Override
    protected Message recoveryDecodeWithContext( byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, boolean inBound ) {
        final Message msg = recoveryDecode( buf, offset, len, inBound );
        MilleniumCommonSessionUtils.enrichRecoveredContext( msg, opt, optOffset, optLen );
        return msg;
    }
}
