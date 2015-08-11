/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.codec.binary.sbe.DummySBEEncoder;
import com.rr.core.codec.binary.sbe.NonBlockingSBESocketSession;
import com.rr.core.codec.binary.sbe.SBEDecoder;
import com.rr.core.codec.binary.sbe.SBEEncoder;
import com.rr.core.codec.binary.sbe.SBEPacketHeader;
import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.DummyMultiSessionDispatcher;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionDispatcher;
import com.rr.core.session.MultiSessionReceiver;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.Utils;
import com.rr.md.channel.MarketDataChannel;
import com.rr.md.fastfix.DummyQueue;
import com.rr.md.fastfix.FastSocketConfig;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;
import com.rr.model.generated.internal.events.factory.MsgSeqNumGapFactory;
import com.rr.model.generated.internal.events.impl.MsgSeqNumGapImpl;


/**
 * @WARN CHANGES TO THIS CLASS SHOULD BE CHECKED AGAINST CMEFastFixSession
 * 
 * CME implementation of the SBE session
 */

public final class CMENonBlockingSBESession extends NonBlockingSBESocketSession implements MarketDataChannel<Integer> {

    private static final int MIN_BYTES = 30;

    private final SuperPool<MsgSeqNumGapImpl> _gapPool     = SuperpoolManager.instance().getSuperPool( MsgSeqNumGapImpl.class );
    private final MsgSeqNumGapFactory         _gapFactory  = new MsgSeqNumGapFactory( _gapPool );
    
    private       Integer[]                   _channelKeys = new Integer[0];

    private       int _inMsgLen;                                   // length of current inbound message
    
    private       int _lastSeqNum;
    private       int _dups;
    private       int _gaps;
    
    private final int                         _initialBytesToRead;
    
    private final SBEDecoder                  _scopedDecoder;
    private final SBEPacketHeader             _packetHeader = new SBEPacketHeader();


    
    public CMENonBlockingSBESession( String                      name, 
                                     MessageRouter               inboundRouter, 
                                     SocketConfig                config, 
                                     MultiSessionDispatcher      dispatcher,
                                     MultiSessionReceiver        receiver,
                                     SBEEncoder                  encoder,
                                     SBEDecoder                  decoder, 
                                     MessageQueue                dispatchQueue ) {
        
        super( name, inboundRouter, config, dispatcher, receiver, encoder, decoder, dispatchQueue );
        
        _initialBytesToRead = MIN_BYTES;
        _scopedDecoder = decoder;
    }

    /**
     * CMENonBlockingFastFixSession for use by builder, uses dummies for encoding, just does decoding (as normal)
     */
    public CMENonBlockingSBESession( String                         name, 
                                     MessageRouter                  inboundRouter, 
                                     FastSocketConfig               config, 
                                     MultiSessionThreadedReceiver   multiplexReceiver,
                                     SBEDecoder                     decoder ) {

        super( name, 
               inboundRouter, 
               config, 
               new DummyMultiSessionDispatcher(), 
               multiplexReceiver, 
               new DummySBEEncoder(), 
               decoder, 
               new DummyQueue() );

        _initialBytesToRead = MIN_BYTES;
        _scopedDecoder = decoder;
    }

    @Override
    protected void dispatchMsgGap( int channelId, int lastSeqNum, int seqNum ) {
        MsgSeqNumGapImpl gap = _gapFactory.get();
        
        gap.setChannelId( channelId );
        gap.setMsgSeqNum( seqNum );
        gap.setPrevSeqNum( lastSeqNum );
        
        dispatchInbound( gap );
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
    public Integer[] getChannelKeys() {
        return _channelKeys;
    }

    @Override
    public boolean hasChannelKey( Integer channelKey ) {
        if ( _channelKeys == null ) return false;
        
        final int key = channelKey.intValue();
        
        for( int i=0 ; i < _channelKeys.length ; ++i ) {
            if ( _channelKeys[i].intValue() == key ) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public synchronized void addChannelKey( Integer channelKey ) {
        if ( ! hasChannelKey( channelKey ) ) {
            _channelKeys = Utils.arrayCopyAndAddEntry( _channelKeys, channelKey );
        }
    }
    
    @Override
    public final void processNextInbound() throws Exception {
        final int preBuffered = prepareForReadMessage();
        
        int bytesRead = nonBlockingRead( preBuffered, _initialBytesToRead );
        
        if ( bytesRead < _initialBytesToRead ) {
            _inPreBuffered = bytesRead;
            return;
        }
        
        // at this point we have the packet and assume will contain whole message as UDP
        if ( _logStats ) _scopedDecoder.setReceived( Utils.nanoTime() );

        _inMsgLen = bytesRead;
        
        final int hdrLenPlusMsgLen  = _inHdrLen + _inMsgLen;
        
        _inByteBuffer.position( _inHdrLen );
        
        if ( _stopping )  return;

        _inLogBuf.setLength( hdrLenPlusMsgLen );

        _inPreBuffered = 0;                    // set preBuffered here incase decoder throws exception

        final int maxOffset = bytesRead + _inHdrLen;
              int offset = _inHdrLen;

        _scopedDecoder.decodeStartPacket( _inBuffer, offset, maxOffset, _packetHeader );
            
        final int lastSeqNum = _lastSeqNum;
        final int newSeqNum  = _packetHeader._packetSeqNum;

        if ( lastSeqNum == newSeqNum ) {
            ++_dups;
            return;
        }

        final int nextExpSeqNum = lastSeqNum + 1;

        if ( newSeqNum == nextExpSeqNum || lastSeqNum == 0 || newSeqNum == 1 ) {
            _lastSeqNum = newSeqNum;
        } else  if ( newSeqNum > nextExpSeqNum ) {
            
            ++_gaps;

            logGapDetected( lastSeqNum, newSeqNum );
            
            dispatchMsgGap( getChannelId(), _lastSeqNum, newSeqNum );
            
            _lastSeqNum = newSeqNum;
        }
        
        // now send the messages one at a time
        offset = _scopedDecoder.getCurrentOffset();
        _decoder.parseHeader( _inBuffer, offset, bytesRead );
        
        // @TODO insert partial message carried over from last packet 
        // as well as shifting the bytes over MAY need to increase the maxIdx in the decoder/builer
        
        Message msg = _decoder.postHeaderDecode();

        logInEvent( null );
        
        while ( msg != null ) {
            logInEventPojo( msg );

            invokeController( msg );

            offset = _scopedDecoder.getCurrentOffset();
            
            msg = _decoder.decode( _inBuffer, offset, maxOffset );
        }

        offset = _scopedDecoder.getCurrentOffset();
        int extraBytes = (maxOffset - offset);

        if ( extraBytes > 0 ) {
            _logInErrMsg.copy( getComponentId() ).append( " lastSeqNum=" ).append( ((CMEFastFixDecoder)_decoder).getLastSeqNum() );
            _logInErrMsg.append( " SKIPPING " ).append( extraBytes ).append( " extra bytes at end of packet" );
            _log.warn( _logInErrMsg );
            
// NEED FEEDBACK FROM CME ON HOW TO HANDLE MESSAGES THAT SPAN PACKETS
//                
//                shiftInBufferLeft( extraBytes, offset );
//                _inPreBuffered = extraBytes;
// instead of shifting left copy to temp buffer and insert bytes at TODO marker above            
            
        }
    }
    
    @Override
    protected final void finalLog( ReusableString msg ) {
        super.finalLog( msg );
        msg.append( ", dups=" + _dups + ", gaps=" + _gaps );
    }
    
    @Override
    protected final void persistIntegrityCheck( boolean inbound, long key, Message msg ) {
        // nothing
    }

    private void logGapDetected( final int lastSeqNum, final int newSeqNum ) {
        _logInErrMsg.reset();
        _logInErrMsg.append( "Gap detected " ).append( getComponentId() ).append( ", channelId=" ).append( getChannelId() ).
                     append( " last=" ).append( lastSeqNum ).append( ", gapSeq=" ).append( newSeqNum );
        _log.info( _logInErrMsg );
    }
    
    private void invokeController( Message msg ) {
        dispatchInbound( msg );
    }
    
    private int getChannelId() {
        if ( _channelKeys.length == 1 ) {
            return _channelKeys[0].intValue();
        }
        return 0; // ALL channels
    }
}
