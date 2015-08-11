/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session;

import java.io.IOException;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Decoder.ResyncCode;
import com.rr.core.codec.Encoder;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.MessageQueue;
import com.rr.core.model.Message;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.BadMessageSize;
import com.rr.core.session.DisconnectedException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.NonBlockingSession;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.SessionControllerConfig;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.om.session.state.AbstractStatefulSocketSession;
import com.rr.om.session.state.SessionController;

/**
 * on disconnect its possible to loose the buffered messages, its up to Fix or Exchange protocol to re-request
 */
public abstract class AbstractNonBlockingSocketSession<T_CONTROLLER extends SessionController<?>, 
                                                       T_CFG extends SocketConfig & SessionControllerConfig>
                extends AbstractStatefulSocketSession<T_CONTROLLER,T_CFG>
                implements NonBlockingSession {

    private int          _outStandingWriteBytes = 0;  // bytes left of current message to write out
    private Message      _partialSentMsg;
    
    private final MessageQueue _sendQueue;
    private final MessageQueue _sendSyncQueue = new BlockingSyncQueue();

    private long _writes = 0;
    private long _startWrites = 0;
    
    public AbstractNonBlockingSocketSession( String                          name, 
                                             MessageRouter                   inboundRouter, 
                                             T_CFG                           config, 
                                             MultiSessionThreadedDispatcher  dispatcher,
                                             MultiSessionThreadedReceiver    receiver,
                                             Encoder                         encoder,
                                             Decoder                         decoder, 
                                             Decoder                         recoveryDecoder,
                                             int                             initialBytesToRead,
                                             MessageQueue                    dispatchQueue ) throws SessionException, PersisterException {
        
        // note pass Other as receiver priority to super as NonBlocking sessions do NOT own the receive thread
        super( name, inboundRouter, config, dispatcher, encoder, decoder, recoveryDecoder, ThreadPriority.Other, initialBytesToRead );
        
        _sendQueue = dispatchQueue;
        
        if ( ! config.isNIO() ) {
            _log.warn( "Session " + name + " override and enable NIO which is required for non blocking" );
            config.setUseNIO( true );
        }
        
        attachReceiver( receiver );
        receiver.addSession( this );
    }

    @Override
    public MessageQueue getSendQueue() {
        return _sendQueue;
    }
    
    @Override
    public MessageQueue getSendSyncQueue() {
        return _sendSyncQueue;
    }
    
    @Override
    public synchronized final void connect() {
        _log.info( "AbstractNonBlockingSocketSession: " + getComponentId() + " connect ignored, connection will be started via MultiSessionThreadedReceiver" );
    }
    
    @Override
    public final void processNextInbound() throws Exception {

        final int preBuffered = prepareForReadMessage();
        
        int bytesRead = nonBlockingRead( preBuffered, _initialBytesToRead );
        if ( bytesRead < _initialBytesToRead ) {
            _inPreBuffered = bytesRead;
            return;
        }

        _inMsgLen = _decoder.parseHeader( _inBuffer, _inHdrLen, bytesRead );  // bytesRead is at least INITIAL_READ_BYTES
        
        if ( _inMsgLen < 0 ) {
            bytesRead = nonBlockingResyncToNextHeader( bytesRead );
            
            if ( bytesRead < _initialBytesToRead ) {
                _inPreBuffered = bytesRead;
                return;
            }
        }

        final int hdrLenPlusMsgLen  = _inHdrLen + _inMsgLen;
        final int remainingMsgBytes = _inMsgLen - bytesRead;
        
        if ( remainingMsgBytes > 0 ) { // in general remainingBytes will be zero as client will disable Nagle
            bytesRead = nonBlockingRead( bytesRead, _inMsgLen );
        }

        if ( bytesRead >= _inMsgLen ) {
            processReadMessage( bytesRead, hdrLenPlusMsgLen );
        } else {
            // @NOTE could store the inHdrLen and avoid reparsing header but this adds extra overhead to normal flow which should be avoided 
            _inPreBuffered = bytesRead;
        }
    }

    @Override
    protected void postSend( Message msg ) {
        // if message has been fully sent then can send to echo session now
        // otherwise echo must be done on retryCompleteWrite
        
        if ( _partialSentMsg == null ) {
            sendChain( msg, true );
        }
    }
    
    @Override
    protected final void sendNow( Message msg ) throws IOException {

        final int encodedLen     = encodeForWrite( msg );
              int startEncodeIdx = _encoder.getOffset(); 

        if ( encodedLen > 0 ) {

            final int totLimit = encodedLen + startEncodeIdx;
                
            _outByteBuffer.clear();
            _outByteBuffer.limit( totLimit );
            _outByteBuffer.position( startEncodeIdx );

            _outNativeByteBuffer.clear();
            _outNativeByteBuffer.put( _outByteBuffer );
            _outNativeByteBuffer.flip();

            if ( _logStats ) lastSent( Utils.nanoTime() );

            _startWrites  = _writes;
            
            tryWriteRemaining( msg, startEncodeIdx, totLimit ); 
        }
    }

    @Override
    public final boolean isMsgPendingWrite() {
        return _outStandingWriteBytes > 0;
    }

    @Override
    public final void retryCompleteWrite() throws IOException {
        final int startEncodeIdx = _encoder.getOffset(); 
        final int encodedLen     = _encoder.getLength();
        final int totLimit       = encodedLen + startEncodeIdx;
        
        if ( _partialSentMsg != null ) {
            final Message echoMsg = _partialSentMsg;
            
            tryWriteRemaining( _partialSentMsg, startEncodeIdx, totLimit );

            if ( _partialSentMsg == null ) {
                sendChain( echoMsg, true );
            }
        }
    }
    
    @Override
    protected final void handleOutboundError( IOException e, Message msg ) {
        super.handleOutboundError( e, msg );
        clearPartialMsg();
    }

    @Override
    protected final void disconnected() {
        super.disconnected();
        _inPreBuffered = 0;
        clearPartialMsg();
    }
    
    /**
     * non blocking socket write returns remaining bytes to be written
     * @return
     * @throws IOException
     */
    private int nonBlockingWriteSocket() throws IOException {
        
        ++_writes ;
        if ( _socketChannel.write() == 0 ) {
            if ( _delayedWrites++ == 0 ) {
                _log.warn( getComponentId() + " Delayed Write : possible slow consumer" );
            }
        }
        
        return( _outNativeByteBuffer.remaining() );
    }

    private int nonBlockingRead( final int preBuffered, int requiredBytes ) throws Exception {
        final int maxBytes = _inByteBuffer.remaining();
        int totalRead = preBuffered;
        int curRead;
        
        if ( requiredBytes - preBuffered <= 0 ) return preBuffered;

        _inNativeByteBuffer.position( 0 );
        _inNativeByteBuffer.limit( maxBytes );

        curRead = _socketChannel.read();

        if ( curRead == -1 ) throw new DisconnectedException( "Detected socket disconnect" );

        // spurious wakeup
        
        if ( curRead <= 0 ) {
            return totalRead;
        }
        
        totalRead += curRead;
        
        _inNativeByteBuffer.flip();
        
        // @TODO bypass the soft ref creation in IOUtil.read for heap based buffers
        _inByteBuffer.put( _inNativeByteBuffer );

        return totalRead;
    }
    
    private int nonBlockingResyncToNextHeader( int bytesRead ) throws Exception {
        
        ResyncCode code = _decoder.resync( _inBuffer, _inHdrLen, bytesRead );

        final int skippedBytes = _decoder.getSkipBytes();
        
        bytesRead -= skippedBytes;
        
        shiftInBufferLeft( bytesRead, _inHdrLen+skippedBytes );
        
        if ( code == ResyncCode.FOUND_PARTIAL_HEADER_NEED_MORE_DATA ) {
            bytesRead = nonBlockingRead( bytesRead, _initialBytesToRead ); // now should have enough for header
        }
        
        if ( bytesRead >= _initialBytesToRead ) {
            _inMsgLen = _decoder.parseHeader( _inBuffer, _inHdrLen, bytesRead );  // bytesRead is at least INITIAL_READ_BYTES
            
            if ( _inMsgLen < 0 ) {
                _logInMsg.copy( FAILED_TO_RESYNC ).append( _inBuffer, _inHdrLen, _initialBytesToRead );
                _log.info( _logInMsg );
                throw new BadMessageSize( _logInMsg.toString() );
            }
        }
        
        return bytesRead;
    }
    
    private void tryWriteRemaining( Message msg, int startEncodeIdx, final int totLimit ) throws IOException {
        _outStandingWriteBytes = nonBlockingWriteSocket();
        
        if ( _stopping ) return;

        if ( _outStandingWriteBytes <= 0 ) {
            final long sendStart = getLastSent();
            if ( sendStart > 0 ) {
                final long duration = Math.abs( Utils.nanoTime() - sendStart );
                
                if ( duration > _socketConfig.getLogDelayedWriteNanos() ) {
                    
                    long writesForThisMsg = _writes - _startWrites;
                    
                    _outMessage.reset();
                    _outMessage.append( getComponentId() );
                    _outMessage.append( DELAYED_WRITE ).append( duration / 1000 ).append( MICROS );
                    _outMessage.append( NUM_WRITES ).append( writesForThisMsg );
                    _log.warn( _outMessage );
                }
            }
            
            postSocketWriteActions( msg, startEncodeIdx, totLimit );
            clearPartialMsg();
        } else {
            _partialSentMsg = msg;
        }
    }

    private void clearPartialMsg() {
        _outStandingWriteBytes = 0;
        _partialSentMsg = null;
    }
}
