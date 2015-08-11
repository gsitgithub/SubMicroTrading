/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.asia.bse.fastfix;

import java.util.Arrays;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.codec.binary.fastfix.NonBlockingFastFixSocketSession;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.DummyMultiSessionDispatcher;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.utils.Utils;
import com.rr.md.asia.bse.fastfix.reader.BSEFastFixDecoder;
import com.rr.md.channel.MarketDataChannel;
import com.rr.md.fastfix.DummyFastFixEncoder;
import com.rr.md.fastfix.DummyQueue;
import com.rr.md.fastfix.FastSocketConfig;
import com.rr.model.generated.internal.events.factory.MsgSeqNumGapFactory;
import com.rr.model.generated.internal.events.impl.MsgSeqNumGapImpl;


/**
 * @WARN CHANGES TO THIS CLASS SHOULD BE CHECKED AGAINST CMEFastFixSession
 */

public final class BSENonBlockingFastFixSession extends NonBlockingFastFixSocketSession implements MarketDataChannel<Integer> {

    private static final int MIN_BYTES = 30;

    /**
     * partitionId is a byte (so max 255) and identifies product stream and its CURRENT senderCompId and seqNum
     * 
     * on failover the partitionId is the same, the senderCompId is incremented and seqNum reset to 1
     */

    public static class PartitionSeqNum {
        public PartitionSeqNum( int senderCompId ) {
            _senderCompId = senderCompId;
        }
        public int _senderCompId;
        public int _lastSeqNum;
    }
    
    private final SuperPool<MsgSeqNumGapImpl> _gapPool    = SuperpoolManager.instance().getSuperPool( MsgSeqNumGapImpl.class );
    private final MsgSeqNumGapFactory         _gapFactory = new MsgSeqNumGapFactory( _gapPool );
    private final ReusableString              _logMsg     = new ReusableString();
    
    private       Integer[]                   _channelKeys = new Integer[0];

    private int _inMsgLen;                                   // length of current inbound message
    
    private int _dups;
    private int _gaps;
    
    private final int               _initialBytesToRead;

    private final BSEFastFixDecoder _bseScopedDecoder;
    
    private final BSEPacketHeader   _curPacketHeader;
    
    private       PartitionSeqNum[] _partitionDetails = new PartitionSeqNum[0];
    
    /**
     * BSENonBlockingFastFixSession for use by builder, uses dummies for encoding, just does decoding (as normal)
     */
    public BSENonBlockingFastFixSession( String                         name, 
                                         MessageRouter                  inboundRouter, 
                                         FastSocketConfig               config, 
                                         MultiSessionThreadedReceiver   multiplexReceiver,
                                         BSEFastFixDecoder              decoder ) {

        super( name, 
               inboundRouter, 
               config, 
               new DummyMultiSessionDispatcher(), 
               multiplexReceiver, 
               new DummyFastFixEncoder(), 
               decoder, 
               new DummyQueue() );

        _initialBytesToRead = MIN_BYTES;
        
        _bseScopedDecoder = decoder;

        _curPacketHeader = _bseScopedDecoder.getCurPacketHeader();
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
        _logInErrMsg.copy( getComponentId() ).append( " last packetSeqNum=" ).append( _curPacketHeader._packetSeqNum );
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
    
    /**
     * fast fix messages are encoded and length is unknown
     */
    @Override
    public final void processNextInbound() throws Exception {

        final int preBuffered = prepareForReadMessage();
        
        int bytesRead = nonBlockingRead( preBuffered, _initialBytesToRead );
        
        if ( bytesRead < _initialBytesToRead ) {
            _inPreBuffered = bytesRead;
            return;
        }
        
        // at this point we have the packet and assume will contain whole message as UDP
        if ( _logStats ) _decoder.setReceived( Utils.nanoTime() );

        _inMsgLen = bytesRead;
        
        final int hdrLenPlusMsgLen  = _inHdrLen + _inMsgLen;
        
        _inByteBuffer.position( _inHdrLen );
        
        if ( _stopping )  return;

        _inLogBuf.setLength( hdrLenPlusMsgLen );

        _inPreBuffered = 0;                    // set preBuffered here incase decoder throws exception

        final int maxOffset = bytesRead + _inHdrLen;
              int offset = _inHdrLen;

        if ( _bseScopedDecoder.decodeStartPacket( _inBuffer, offset, maxOffset ) ) {
            
            final PartitionSeqNum curPart = getPartitionDetails( _curPacketHeader );
            final int lastSeqNum = curPart._lastSeqNum;
            final int newSeqNum  = _curPacketHeader._packetSeqNum;

            if ( lastSeqNum == newSeqNum ) {
                ++_dups;
                return;
            }

            if ( curPart._senderCompId != _curPacketHeader._senderCompId ) {
                failoverDetected( curPart );
            }

            final int nextExpSeqNum = lastSeqNum + 1;

            if ( newSeqNum == nextExpSeqNum || lastSeqNum == 0 || newSeqNum == 1 ) {
                curPart._lastSeqNum = newSeqNum;
            } else  if ( newSeqNum > nextExpSeqNum ) {
                
                ++_gaps;

                logGapDetected( lastSeqNum, newSeqNum );
                
                dispatchMsgGap( _curPacketHeader._partitionId, curPart._lastSeqNum, newSeqNum );
                
                curPart._lastSeqNum = newSeqNum;
            }
            
            logInEvent( null );
            
            // now send the messages one at a time
            offset = _bseScopedDecoder.getCurrentOffset();
            
            // have a valid header so process the messages
            Message msg = _bseScopedDecoder.decode( _inBuffer, offset, maxOffset );

            while ( msg != null ) {
                logInEventPojo( msg );

                invokeController( msg );

                offset = _bseScopedDecoder.getCurrentOffset();
                
                msg = _bseScopedDecoder.decode( _inBuffer, offset, maxOffset );
            }

            offset = _bseScopedDecoder.getCurrentOffset();
            int extraBytes = (maxOffset - offset);

            if ( extraBytes > 0 ) {
                shiftInBufferLeft( extraBytes, offset );
                _inPreBuffered = extraBytes;
            }
        } 
    }

    private void logGapDetected( final int lastSeqNum, final int newSeqNum ) {
        _logInErrMsg.reset();
        _logInErrMsg.append( "Packet Gap detected " ).append( getComponentId() ).append( ", partitionId=" ).append( _curPacketHeader._partitionId ).
                     append( " last=" ).append( lastSeqNum ).append( ", gapSeq=" ).append( newSeqNum );
        _log.info( _logInErrMsg );
    }

    private void failoverDetected( final PartitionSeqNum curPart ) {
        _logMsg.copy( "FAILOVER DETECTED " ).append( getComponentId() ).
                append( ", partitionId=" ).append( _curPacketHeader._partitionId ).
                append( ", oldSenderCompId=" ).append( curPart._senderCompId ).
                append( ", newSenderCompId=" ).append( _curPacketHeader._senderCompId ).
                append( ", newPacketSeqNum=" ).append( _curPacketHeader._packetSeqNum );
        
        curPart._senderCompId = _curPacketHeader._senderCompId;
        
        dispatchMsgGap( _curPacketHeader._partitionId, curPart._lastSeqNum, 0 );
    }
    
    private PartitionSeqNum getPartitionDetails( final BSEPacketHeader curPacketHeader ) {
        final int partitionId = curPacketHeader._partitionId;

        if ( partitionId > _partitionDetails.length ) {
            _partitionDetails = Arrays.copyOf( _partitionDetails, partitionId+2 );
        }
        
        PartitionSeqNum details = _partitionDetails[ partitionId ];
        
        if ( details == null ) {
            details = new PartitionSeqNum( curPacketHeader._senderCompId );
            _partitionDetails[ partitionId ] = details;
        }
        
        return details;
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

    private void invokeController( Message msg ) {

        dispatchInbound( msg );
    }
}
