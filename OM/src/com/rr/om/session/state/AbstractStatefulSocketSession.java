/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.state;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.rr.core.codec.BaseReject;
import com.rr.core.codec.Decoder;
import com.rr.core.codec.Decoder.ResyncCode;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.persister.IndexPersister;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.BadMessageSize;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.socket.AbstractSocketSession;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.session.socket.SessionControllerConfig;
import com.rr.core.session.socket.SessionStateException;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;

public abstract class AbstractStatefulSocketSession<T_CONTROLLER extends SessionController<?>, 
                                                    T_CFG        extends SocketConfig & SessionControllerConfig>  
                extends AbstractSocketSession implements SeqNumSession {

    protected static final ZString  FAILED_TO_RESYNC       = new ViewString( "Invalid header, failed to skip to next valid header " );
    private   static final ZString  FAILED_TO_RETRIEVE     = new ViewString( "Failed to reconstitute sent message seqNo=" );

    protected final Logger   _log = LoggerFactory.create( AbstractStatefulSocketSession.class );

    private   final    int               _outBufStartMsgIdx;

    protected final    T_CONTROLLER      _controller;
    protected final    T_CFG             _config;
    protected          int               _inMsgLen;                                   // length of current inbound message
    private            int               _inMsgSeqNo;

    private            boolean           _assignSeqNumAndPersist = true;

    protected final    byte[]            _tmpBuffer             = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
    protected final    ByteBuffer        _tmpByteBuffer         = ByteBuffer.wrap( _tmpBuffer );

    private   final    IndexPersister    _inIdxPersister;     // avoid cast cost per write
    private   final    IndexPersister    _outIdxPersister;
    private            long              _persistedOutKey;    // persisted key for current out message

    protected final    int               _initialBytesToRead;



    public AbstractStatefulSocketSession( String            name, 
                                          MessageRouter     inboundRouter, 
                                          T_CFG             fixConfig, 
                                          MessageDispatcher dispatcher, 
                                          Encoder           encoder, 
                                          Decoder           decoder,
                                          Decoder           recoveryDecoder, 
                                          ThreadPriority    receiverPriority,
                                          int               initialBytesToRead ) throws SessionException, PersisterException {
        
        super( name, inboundRouter, fixConfig, dispatcher, encoder, decoder, recoveryDecoder, receiverPriority );
        
        _initialBytesToRead = initialBytesToRead;
        _outBufStartMsgIdx = _outHdrLen;
        
        _config = fixConfig;
        
        if ( ! (_inPersister instanceof IndexPersister) ) {
            throw new SessionException( "Stateful inbound Persister must be instance of IndexPersister" );
        }
        
        _inIdxPersister = (IndexPersister)_inPersister;

        if ( ! (_outPersister instanceof IndexPersister) ) {
            throw new SessionException( "Stateful outbound Persister must be instance of IndexPersister" );
        }

        _outIdxPersister = (IndexPersister)_outPersister;

        _controller  = createSessionContoller();
        
        _inIdxPersister.open();
        _outIdxPersister.open();
    }

    @Override
    public synchronized void stop() {
        if (  isStopping() ) return;

        super.stop();
        _controller.stop();
    }
    
    protected abstract T_CONTROLLER createSessionContoller();

    @Override
    public String info() {
        ReusableString m = TLC.instance().getString();

        m.append( super.info() ).append( " " );
        
        if ( _controller != null ) m.append( _controller.info() );

        if ( _socketChannel != null ) _socketChannel.info( m );
        
        String s = m.toString();

        TLC.instance().recycle( m );
        
        return s;
    }
    
    @Override
    public final T_CFG getStateConfig() {
        return _config;
    }
    
    @Override
    public void processNextInbound() throws Exception {

        final int preBuffered = prepareForReadMessage();
        
        int bytesRead = initialChannelRead( preBuffered, _initialBytesToRead );
        if ( bytesRead == 0 ) {
            _inPreBuffered = preBuffered;
            return;
        }

        _inMsgLen = _decoder.parseHeader( _inBuffer, _inHdrLen, bytesRead );  // bytesRead is at least INITIAL_READ_BYTES
        
        if ( _inMsgLen < 0 ) {
            bytesRead = resyncToNextHeader( bytesRead );
        }

        final int hdrLenPlusMsgLen  = _inHdrLen + _inMsgLen;
        final int remainingMsgBytes = _inMsgLen - bytesRead;
        
        if ( remainingMsgBytes > 0 ) { // in general remainingBytes will be zero as client will disable Nagle
            bytesRead = readFixedExpectedBytes( bytesRead, _inMsgLen );
        }

        processReadMessage( bytesRead, hdrLenPlusMsgLen );
    }

    // persistence will be invoked by the controller/fixEngine
    @Override
    public final void persistLastInboundMesssage() {
        if ( _inMsgSeqNo  > 0 ) {
            try {
                _inIdxPersister.persistIdxAndRec( _inMsgSeqNo, _inBuffer, _inHdrLen, _inMsgLen );
            } catch( Exception e ) {
                _logInErrMsg.setValue( e.getMessage() );
                _logInErrMsg.append( ", inMsgSeqNo=" ).append( _inMsgSeqNo ).append( ", offset=" ).append( _inHdrLen )
                            .append( ", len=" ).append( _inMsgLen );
                
                _inIdxPersister.log( _logOutMsg ).append( ' ' );
                
                _log.error( ERR_PERSIST_IN, _logInErrMsg, e );
            }
        }
    }
    
    /**
     * @return the fix controller, only for use by admin commands
     */
    public final T_CONTROLLER getController() {
        return _controller;
    }
    @Override
    public boolean isLoggedIn() {
        return _controller.isLoggedIn();
    }
    
    @Override
    public final void handleForSync( Message msg ) {
        _outboundDispatcher.dispatchForSync( msg );
    }
    
    @Override
    public final boolean discardOnDisconnect( Message msg ) {     // discard SESSION messages
        return isSessionMessage( msg );
    }
    

    @Override
    public final boolean canHandle() {
        return ! _controller.isLoggedOut();
    }
    
    public static int getDataOffset( String name, boolean isInbound ) {
        return AbstractSession.getLogHdrLen( name, isInbound );
    }

    @Override
    public final void gapFillInbound( int fromSeqNum, int uptoSeqNum ) throws SessionStateException {
        
        boolean ok = _inIdxPersister.addIndexEntries( fromSeqNum, uptoSeqNum );
        
        if ( !ok ) {
            throw new SessionStateException( "SequenceReset failure: nextExpInSeqNum=" + fromSeqNum + ", NewInSeqNum=" + uptoSeqNum );
        }
    }

    @Override
    public final void truncateInboundIndexDown( int fromSeqNum, int toSeqNum ) throws SessionStateException {
        
        _log.warn( "FixSocketSession.truncateInboundIndexDown() erasing index entries from=" + fromSeqNum + " to=" + toSeqNum );
        
        boolean ok = _inIdxPersister.removeIndexEntries( fromSeqNum, toSeqNum );
        
        if ( !ok ) {
            throw new SessionStateException( "Failed to truncate inbound index entries: fromSeqNum=" + fromSeqNum + ", downtoSeqNum=" + toSeqNum );
        }
    }

    @Override
    public final void truncateOutboundIndexDown( int fromSeqNum, int toSeqNum ) throws SessionStateException {
        
        _log.warn( "FixSocketSession.truncateOutboundIndexDown() erasing index entries from=" + fromSeqNum + " to=" + toSeqNum );
        
        boolean ok = _outIdxPersister.removeIndexEntries( fromSeqNum, toSeqNum );
        
        if ( !ok ) {
            throw new SessionStateException( "Failed to truncate outbound index entries: fromSeqNum=" + fromSeqNum + ", downtoSeqNum=" + toSeqNum );
        }
    }

    @Override
    public final void gapExtendOutbound( int fromSeqNum, int toSeqNum ) {
        ((IndexPersister)_outPersister).addIndexEntries( fromSeqNum, toSeqNum );
    }

    /**
     * @NOTE MUST ONLY BE CALLED IN THE RECEIVERS THREAD OF CONTROL
     */
    @Override
    public final Message retrieve( int curMsgSeqNo ) {
        Message msg = null;
        
        try {
            int numBytes = ((IndexPersister)_outPersister).readFromIndex( curMsgSeqNo, _inBuffer, _inHdrLen );

            if ( numBytes > 0 ) {
                
                _recoveryDecoder.parseHeader( _inBuffer, _inHdrLen, numBytes );
                
                msg = _recoveryDecoder.postHeaderDecode();
                
                if ( msg instanceof BaseReject<?> )  {
                    BaseReject<?> rej = (BaseReject<?>)msg;
                    
                    _logInMsg.copy( FAILED_TO_RETRIEVE ).append( curMsgSeqNo ).append( ' ' ).append( rej.getMessage() );
                    
                    Throwable t = rej.getThrowable();
                    if ( t instanceof RuntimeDecodingException ) {
                        RuntimeDecodingException rde = (RuntimeDecodingException) t;
                        errorDumpMsg( _logInMsg, rde );
                    }
                    _log.warn( _logInMsg );
                    
                    return null;
                }
            }
            
        } catch( PersisterException e ) {
            _logInMsg.copy( FAILED_TO_RETRIEVE ).append( curMsgSeqNo ).append( e.getMessage() );
            _log.warn( _logInMsg );
        }
        
        return msg;
    }

    protected void errorDumpMsg( ReusableString logInMsg, RuntimeDecodingException rde ) {
        _logInMsg.append( ' ' ).append( rde.getFixMsg() );
//        _logInMsg.append( ' ' ).appendHEX( rde.getFixMsg() );
    }

    protected final void processReadMessage( final int bytesRead, final int hdrLenPlusMsgLen ) throws SessionStateException {
        // time starts from when we have read the full message off the socket
        if ( _logStats ) _decoder.setReceived( Utils.nanoTime() );

        _inByteBuffer.position( _inHdrLen );
        
        if ( _stopping )  return;

        _inLogBuf.setLength( hdrLenPlusMsgLen );

        final int extraBytes = bytesRead - _inMsgLen;
        _inPreBuffered = extraBytes;                    // set preBuffered here incase decoder throws exception

        logInEvent( _inLogBuf );

        Message msg = _decoder.postHeaderDecode();
        
        shiftInBufferLeft( extraBytes, hdrLenPlusMsgLen );

        if ( msg != null ) {
            _inMsgSeqNo = msg.getMsgSeqNum();
    
            invokeController( msg );
        }
    }

    protected void invokeController( Message msg ) throws SessionStateException {
        _controller.handle( msg );
    }

    /**
     * needs to be syncronised OR have a seperate decoder for in/out reccovery
     */
    @Override
    protected synchronized Message recoveryDecode( byte[] buf, int offset, int len, boolean inBound ) {
        
        _recoveryDecoder.parseHeader( buf, offset, len );
        Message msg = _recoveryDecoder.postHeaderDecode();

        _controller.recoverContext( msg, inBound );
        
        return msg;
    }

    @Override
    protected final void setOutboundRecoveryFinished( boolean finished ) {
        super.setOutboundRecoveryFinished( finished );
        
        if ( finished ) _log.info( getComponentId() + " OUTBOUND recovery finished " + _controller.logOutboundRecoveryFinished() );
    }

    @Override
    protected final void setInboundRecoveryFinished( boolean finished ) {
        super.setInboundRecoveryFinished( finished );

        if ( finished ) _log.info( getComponentId() + " INBOUND recovery finished nextExpectedInSeqNum=" + _controller.logInboundRecoveryFinished() );
    }
    
    
    @Override
    protected Message recoveryDecodeWithContext( byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, boolean inBound ) {
        return recoveryDecode( buf, offset, len, inBound );
    }
    
    @Override
    protected void sendNow( Message msg ) throws IOException {

        final int encodedLen     = encodeForWrite( msg );
              int startEncodeIdx = _encoder.getOffset(); 

        if ( encodedLen > 0 ) {

            final int totLimit = encodedLen + startEncodeIdx;
                
            _outByteBuffer.clear();
            _outByteBuffer.limit( totLimit );
            _outByteBuffer.position( startEncodeIdx );
            
            blockingWriteSocket();
            
            if ( _stopping ) return;

            postSocketWriteActions( msg, startEncodeIdx, totLimit );
        }
    }

    /**
     * @param msg
     * @return bytes encoded, 0 for no message to send
     */
    protected final int encodeForWrite( final Message msg ) {
        // is Replay or its a message we previously tried but failed to send
        
        _assignSeqNumAndPersist = (msg.getMsgSeqNum() <= 0);
        
        int nextOut;
        
        if ( _assignSeqNumAndPersist ) {
            nextOut = setOutSeqNum( msg );
        } else {
            nextOut = msg.getMsgSeqNum();
            
            if  ( nextOut < 0 ) {
                nextOut = 0;
                msg.setMsgSeqNum( 0 );
            }
        }

        _encoder.encode( msg );
        
        final int encodedLen     = _encoder.getLength();
        final int startEncodeIdx = _encoder.getOffset(); 

        if ( encodedLen > 0 ) {
            _persistedOutKey = -1;
            
            if ( _assignSeqNumAndPersist ) {
                if ( nextOut > 0 ) {
                    try {
                        _persistedOutKey = persistOutRec( nextOut, encodedLen, startEncodeIdx, msg );
                    } catch( Exception e ) {
                        _logOutMsg.setValue( e.getMessage() );
                        _logOutMsg.append( ", outMsgSeqNo=" ).append( nextOut ).append( ", offset=" ).append( startEncodeIdx )
                                    .append( ", len=" ).append( encodedLen );
                        
                        _outIdxPersister.log( _logOutMsg ).append( ' ' );
                        
                        _log.error( ERR_PERSIST_OUT, _logOutMsg, e );
                    }
                }
            }
        }
        
        return encodedLen;
    }

    protected long persistOutRec( int nextOut, final int encodedLen, final int startEncodeIdx, Message msg ) throws PersisterException {
        return _outIdxPersister.persistIdxAndRec( nextOut, _outBuffer, startEncodeIdx, encodedLen );
    }

    protected IndexPersister getOutboundPersister() {
        return _outIdxPersister;
    }
    
    protected abstract int setOutSeqNum( final Message msg );
    
    protected void postSocketWriteActions( final Message msg, int startEncodeIdx, final int totLimit ) {
        if ( _assignSeqNumAndPersist && _markConfirmationEnabled ) {
            try {
                _outPersister.setUpperFlags( _persistedOutKey, Session.PERSIST_FLAG_CONFIRM_SENT );
            } catch( Exception e ) {
                _log.error( ERR_PERSIST_MKR, "", e );
            }
        }
        
        while( --startEncodeIdx >= _outBufStartMsgIdx ) {
            _outBuffer[ startEncodeIdx ] = ' ';
        }

        _outLogBuf.setLength( totLimit );
        
        if ( _logStats ) {
            _encoder.addStats( _outLogBuf, msg, getLastSent() );
        }
        
        logOutEvent( _outLogBuf );
    }
        
    @Override
    protected void handleOutboundError( IOException e, Message msg ) {
        
        if ( _assignSeqNumAndPersist ) {
            _controller.outboundError();
        }
        
        super.handleOutboundError( e, msg );
    }

    @Override
    protected final void sendChain( Message msg, boolean canRecycle ) {
        if ( _assignSeqNumAndPersist && _chainSession != null && _chainSession.isConnected() ) {
            _chainSession.handle( msg );
        } else if ( canRecycle ) {
            outboundRecycle( msg );
        }
    }
    
    @Override
    protected void disconnected() {
        super.disconnected();
        _controller.changeState( _controller.getStateLoggedOut() );
    }

    @Override
    protected final void connected() {
        super.connected();
        _controller.connected();
    }

    @Override
    protected final void persistIntegrityCheck( final boolean inbound, final long key, final Message msg ) {
        final IndexPersister p = (inbound) ? _inIdxPersister : _outIdxPersister;
        
        if ( msg != null ) {
            final int seqNum = msg.getMsgSeqNum();
            
            if ( seqNum > 0 ) {
                p.verifyIndex( key, seqNum );
            }
        }
    }
    
    /**
     * resync stream IF a header can be found in current buffered data
     * If find a partial buffer at end of the stream, then shift left and read more
     * 
     * @param bytesRead
     * @return bytesRead
     * @throws Exception 
     */
    private final int resyncToNextHeader( int bytesRead ) throws Exception {
        
        ResyncCode code = _decoder.resync( _inBuffer, _inHdrLen, bytesRead );

        final int skippedBytes = _decoder.getSkipBytes();
        
        bytesRead -= skippedBytes;
        
        shiftInBufferLeft( bytesRead, _inHdrLen+skippedBytes );
        
        if ( code == ResyncCode.FOUND_PARTIAL_HEADER_NEED_MORE_DATA ) {
            bytesRead = readFixedExpectedBytes( bytesRead, _initialBytesToRead ); // now should have enough for header
        }
        
        _inMsgLen = _decoder.parseHeader( _inBuffer, _inHdrLen, bytesRead );  // bytesRead is at least INITIAL_READ_BYTES
        
        if ( _inMsgLen < 0 ) {
            _logInMsg.copy( FAILED_TO_RESYNC ).append( _inBuffer, _inHdrLen, _initialBytesToRead );
            _log.info( _logInMsg );
            throw new BadMessageSize( _logInMsg.toString() );
        }
        
        return bytesRead;
    }
}
