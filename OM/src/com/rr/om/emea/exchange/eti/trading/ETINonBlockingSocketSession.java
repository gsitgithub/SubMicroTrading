/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import com.rr.codec.emea.exchange.eti.ETIDecodeContext;
import com.rr.codec.emea.exchange.eti.ETIDecoder;
import com.rr.codec.emea.exchange.eti.ETIEncoder;
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
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SessionStateException;
import com.rr.om.session.AbstractNonBlockingSocketSession;

public class ETINonBlockingSocketSession extends AbstractNonBlockingSocketSession<ETIController,ETISocketConfig> implements ETISession {

    private static final int INITIAL_READ_BYTES = 8;

    private final ETIDecodeContext _lastDecodeContext;

    private Session _gwySess;
    
    public ETINonBlockingSocketSession( String                          name, 
                                        MessageRouter                   inboundRouter, 
                                        ETISocketConfig                 etiConfig, 
                                        MultiSessionThreadedDispatcher  dispatcher,
                                        MultiSessionThreadedReceiver    receiver,
                                        Encoder                         encoder,
                                        Decoder                         decoder, 
                                        Decoder                         recoveryDecoder,
                                        MessageQueue                    dispatchQueue ) throws SessionException, PersisterException {
        
        super( name, inboundRouter, etiConfig, dispatcher, receiver, encoder, decoder, recoveryDecoder, INITIAL_READ_BYTES, dispatchQueue );
        
        _lastDecodeContext = new ETIDecodeContext( etiConfig.getExpectedRequests() );

        ((ETIEncoder)encoder).setSenderSubID( etiConfig.getUserId() );
        ((ETIEncoder)encoder).setLocationId( etiConfig.getLocationId() );
        ((ETIEncoder)encoder).setUniqueClientCode( etiConfig.getUniqueClientCode() );

        
        if ( etiConfig.isServer() ) { // emulating exchange
            ((ETIEncoder)encoder).setExchangeEmulationOn();
            ((ETIDecoder)decoder).setExchangeEmulationOn();
        }
    }

    @Override
    public final boolean isSessionMessage( Message msg ) {
        return ETICommonSessionUtils.isSessionMessage( msg );
    }

    @Override
    public ETIDecodeContext getDecodeContext() {
        return _lastDecodeContext;
    }
    
    public void setGatewaySession( Session gwySess ) {
        _gwySess = gwySess;
    }
    
    @Override
    public Session getGatewaySession() {
        return (_gwySess != null) ? _gwySess : this;
    }

    @Override public int getLastSeqNumProcessed() { return 0; }
    
    @Override
    protected final ETIController createSessionContoller() {
        return ETICommonSessionUtils.createSessionController( this, _config );        
    }
    
    @Override
    protected final int setOutSeqNum( final Message msg ) {
        return ETICommonSessionUtils.setOutSeqNum( _controller, _lastDecodeContext, msg );
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
        ((ETIDecoder)_decoder).getLastContext( _lastDecodeContext );
        _controller.handle( msg );
    }
    
    @Override
    protected synchronized Message recoveryDecode( byte[] buf, int offset, int len, boolean inBound ) {
        return ETICommonSessionUtils.recoveryDecode( _controller, (ETIDecoder) _recoveryDecoder, _lastDecodeContext, buf, offset, len, inBound );
    }
    
    /**
     * specialisation to persist linkId as ETI doesnt have anywhere to store it, the srcLinkId is stored for mktNOS
     */
    @Override
    protected long persistOutRec( int nextOut, final int encodedLen, final int startEncodeIdx, Message msg ) throws PersisterException {
        ReusableString linkId = _lastDecodeContext.getSrcLinkIdForUpdate();
        ETICommonSessionUtils.getContextForOutPersist( msg, linkId );
        return getOutboundPersister().persistIdxAndRec( nextOut, _outBuffer, startEncodeIdx, encodedLen, linkId.getBytes(), 0, linkId.length() );
    }
    
    @Override
    protected Message recoveryDecodeWithContext( byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, boolean inBound ) {
        final Message msg = recoveryDecode( buf, offset, len, inBound );
        ETICommonSessionUtils.enrichRecoveredContext( msg, opt, optOffset, optLen );
        return msg;
    }

    @Override
    protected void errorDumpMsg( ReusableString logInMsg, RuntimeDecodingException rde ) {
        _logInMsg.append( ' ' ).appendHEX( rde.getFixMsg() );
    }
}
