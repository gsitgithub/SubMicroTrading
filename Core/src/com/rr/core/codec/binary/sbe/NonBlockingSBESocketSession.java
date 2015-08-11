/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.sbe;

import java.io.IOException;

import com.rr.core.codec.DummyDecoder;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.session.DisconnectedException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionDispatcher;
import com.rr.core.session.MultiSessionReceiver;
import com.rr.core.session.NonBlockingSession;
import com.rr.core.session.socket.AbstractSocketSession;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;

/**
 * a multicast socket session implementation for SBE
 * 
 *  @WARN CHANGES TO THIS CLASS SHOULD BE CHECKED AGAINST FastFixSocketSession
 */
public abstract class NonBlockingSBESocketSession extends AbstractSocketSession implements NonBlockingSession {

    protected static final Logger _log = LoggerFactory.create( NonBlockingSBESocketSession.class );
    
    protected static final ZString  FAILED_TO_RESYNC       = new ViewString( "Invalid mcast header, failed to skip to next valid header " );
    
    private int          _outStandingWriteBytes = 0;  // bytes left of current message to write out
    private Message      _partialSentMsg;

    private final MessageQueue _sendQueue;
    private final MessageQueue _sendSyncQueue = new BlockingSyncQueue();

    private SBEPacketHeader _outPktHdr;
    
    public NonBlockingSBESocketSession(  String                      name, 
                                         MessageRouter               inboundRouter, 
                                         SocketConfig                config, 
                                         MultiSessionDispatcher      dispatcher,
                                         MultiSessionReceiver        receiver,
                                         SBEEncoder                  encoder,
                                         SBEDecoder                  decoder, 
                                         MessageQueue                dispatchQueue ) {
        
        // note pass Other as receiver priority to super as NonBlocking sessions do NOT own the receive thread
        super( name, inboundRouter, config, dispatcher, encoder, decoder, new DummyDecoder(), ThreadPriority.Other );
        
        _sendQueue = dispatchQueue;
        
        if ( ! config.isNIO() ) {
            _log.warn( "Session " + name + " override and enable NIO which is required for non blocking" );
            config.setUseNIO( true );
        }
        
        attachReceiver( receiver );
        receiver.addSession( this );
    }

    @Override
    public final MessageQueue getSendQueue() {
        return _sendQueue;
    }
    
    @Override
    public final MessageQueue getSendSyncQueue() {
        return _sendSyncQueue;
    }
    
    @Override
    public synchronized final void connect() {
        _log.info( "NonBlockingSBESocketSession: " + getComponentId() + " connect ignored, connection will be started via MultiSessionThreadedReceiver" );
    }
    
    /**
     * @param channelId
     * @param lastSeqNum
     * @param seqNum      upper bound of gap or 0 if book seq num should be reset to 0 ... eg failover at exchange
     */
    protected abstract void dispatchMsgGap( int channelId, int lastSeqNum, int seqNum );

    @Override
    protected final void postSend( Message msg ) {
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
        
        do {
            if ( _socketChannel.write() == 0 ) {
                if ( _delayedWrites++ == 0 ) {
                    _log.warn( getComponentId() + " Delayed Write : possible slow consumer" );
                }
                break; // DONT BLOCK ON FAILED WRITE
            }
        } while( _outNativeByteBuffer.hasRemaining() && ! _stopping ); // allow write to be split into multiple calls
        
        return( _outNativeByteBuffer.remaining() );
    }

    protected final int nonBlockingRead( final int preBuffered, int requiredBytes ) throws Exception {
        final int maxBytes = _inByteBuffer.remaining();
        int totalRead = preBuffered;
        int curRead;
        
        if ( requiredBytes - preBuffered <= 0 ) return preBuffered;

        _inNativeByteBuffer.position( 0 );
        _inNativeByteBuffer.limit( maxBytes );

        curRead = _socketChannel.read();

        if ( curRead == -1 ) throw new DisconnectedException( "Detected socket disconnect" );

        // spurious wakeup
        
        if ( curRead == 0 ) {
            return totalRead;
        }
        
        totalRead += curRead;
        
        _inNativeByteBuffer.flip();
        
        // @TODO bypass the soft ref creation in IOUtil.read for heap based buffers
        _inByteBuffer.put( _inNativeByteBuffer );

        return totalRead;
    }
    
    private void tryWriteRemaining( Message msg, int startEncodeIdx, final int totLimit ) throws IOException {
        _outStandingWriteBytes = nonBlockingWriteSocket();
        
        if ( _stopping ) return;

        if ( _outStandingWriteBytes <= 0 ) {
            final long sendStart = getLastSent();
            if ( sendStart > 0 ) {
                final long duration = Math.abs( Utils.nanoTime() - sendStart );
                
                if ( duration > _socketConfig.getLogDelayedWriteNanos() ) {
                    _outMessage.reset();
                    _outMessage.append( getComponentId() );
                    _outMessage.append( DELAYED_WRITE ).append( duration / 1000 ).append( MICROS );
                    _log.info( _outMessage );
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

    private final int encodeForWrite( final Message msg ) {

        // dont bother setting nano timestamp
        ++_outPktHdr._packetSeqNum;

        ((SBEEncoder)_encoder).encodeStartPacket( _outPktHdr );
        _encoder.encode( msg );
        
        return _encoder.getLength();
    }

    protected final void postSocketWriteActions( final Message msg, int startEncodeIdx, final int totLimit ) {
        
        logOutEvent( null );
        logOutEventPojo( msg );
    }

    @Override
    public final void handleForSync( Message msg ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public final boolean discardOnDisconnect( Message msg ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public final void persistLastInboundMesssage() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public final boolean canHandle() {
        return isConnected();
    }

    @Override
    protected final void sendChain( final Message msg, final boolean canRecycle ) {
        if ( _chainSession != null && _chainSession.isConnected() ) {
            _chainSession.handle( msg );
        } else if ( canRecycle ) {
            outboundRecycle( msg );
        }
    }

    @Override
    protected final void logInEvent( ZString event ) {
        if ( _logEvents ) {
            ((SBEDecoder)_decoder).logLastMsg();
        }
    }
    
    @Override
    protected final void logOutEvent( ZString event ) {
        if ( _logEvents ) {
            ((SBEEncoder)_encoder).logLastMsg();
        }
    }

    @Override
    protected final Message recoveryDecode( byte[] buf, int offset, int len, boolean inBound ) {
        return null;
    }

    @Override
    protected final Message recoveryDecodeWithContext( byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, boolean inBound ) {
        return null;
    }
    
    @Override
    public void logInboundError( Exception e ) {
        super.logInboundError( e );
        ((SBEDecoder)_decoder).logLastMsg();
    }
}
