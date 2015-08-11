/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix;

import java.io.IOException;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.DummyDecoder;
import com.rr.core.codec.Encoder;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.socket.AbstractSocketSession;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;

/**
 * a multicast socket session implementation for FastFix
 * 
 * @WARN CHANGES TO THIS CLASS SHOULD BE CHECKED AGAINST NonBlockingFastFixSocketSession
 */
public abstract class FastFixSocketSession extends AbstractSocketSession {

    protected static final Logger _log      = LoggerFactory.create( FastFixSocketSession.class );
    
    private   static final int    MIN_BYTES = 30;
    
    public static int getDataOffset( String name, boolean isInbound ) {
        return AbstractSession.getLogHdrLen( name, isInbound );
    }

    public FastFixSocketSession(  String            name, 
                                  MessageRouter     inboundRouter, 
                                  SocketConfig      socketConfig, 
                                  MessageDispatcher dispatcher, 
                                  Encoder           encoder, 
                                  Decoder           decoder,
                                  ThreadPriority    receiverPriority ) {
        
        super( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, new DummyDecoder(), receiverPriority );
    }

    @Override
    public final void processNextInbound() throws Exception {

        int preBuffered = _inPreBuffered;
        Message msg;

        _inPreBuffered = 0;                                                         // reset the preBuffer .. incase of exception
        final int startPos = _inHdrLen + preBuffered;
        _inByteBuffer.limit( _inByteBuffer.capacity() );
        _inByteBuffer.position( startPos );

        final int bytesRead = initialChannelRead( preBuffered, MIN_BYTES );
        if ( bytesRead == 0 ) {
            _inPreBuffered = preBuffered;
            return;
        }

        // time starts from when we have read the full message off the socket
        if ( _logStats ) _decoder.setReceived( Utils.nanoTime() );
        
        _inByteBuffer.position( _inHdrLen );

        final int maxIdx = _inHdrLen + bytesRead;
        _inByteBuffer.limit( maxIdx );
        
        if ( _stopping )  return;

        _inLogBuf.setLength( maxIdx );

        msg = decode( _inHdrLen, bytesRead );

        final int msgLen = _decoder.getLength();
        int extraBytes = bytesRead;
        
        if ( msg != null ) {
            
            extraBytes = bytesRead - msgLen;
            
            logInEvent( null );
            logInEventPojo( msg );

            // message decoded, shift left any extra bytes before invoke controller as that can throw exception .. avoids extra try-finally
            invokeController( msg );
        } else {
            // no partial messages in UDP ... could also be duplicate in any case discard
            extraBytes = 0;
        }

        if ( extraBytes > 0 ) {
            _inPreBuffered = extraBytes;
            final int nextMsgStart  = _inHdrLen + msgLen;
            shiftInBufferLeft( extraBytes, nextMsgStart );
        }
    }

    @Override
    public final void handleForSync( Message msg ) {
        // 
    }

    @Override
    public final void persistLastInboundMesssage() {
        // N/A
    }
    
    @Override
    public final void logInboundError( Exception e ) {
        super.logInboundError( e );
        ((FastFixDecoder)_decoder).logLastMsg();
    }

    protected abstract void invokeController( Message msg );
    
    protected abstract void dispatchMsgGap( int channelId, int lastSeqNum, int seqNum );

    @Override
    protected final synchronized Message recoveryDecode( byte[] buf, int offset, int len, boolean inBound ) {
        return null;
    }
    
    @Override
    protected final Message recoveryDecodeWithContext( byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, boolean inBound ) {
        return null;
    }    
    
    @Override
    protected final void sendNow( Message msg ) throws IOException {
        _encoder.encode( msg );

        int length = _encoder.getLength();
        
        if ( length > 0 ) {

            final int lastIdx = length + _encoder.getOffset();
                
            _outByteBuffer.clear();
            _outByteBuffer.limit( lastIdx );
            _outByteBuffer.position( _encoder.getOffset() );

            blockingWriteSocket();

            logOutEvent( null );
            logOutEventPojo( msg );
            
            if ( _stopping ) return;
        }
    }

    @Override
    public final boolean discardOnDisconnect( Message msg ) {
        return false;
    }

    @Override
    public final boolean canHandle() {
        return isConnected();
    }

    @Override
    protected final void sendChain( Message msg, boolean canRecycle ) {
        if ( _chainSession != null && _chainSession.isConnected() ) {
            _chainSession.handle( msg );
        } else if ( canRecycle ) {
            outboundRecycle( msg );
        }
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
