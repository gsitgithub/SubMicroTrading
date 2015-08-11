/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme;

import java.io.IOException;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.codec.binary.fastfix.FastFixSocketSession;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;
import com.rr.md.channel.MarketDataChannel;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;
import com.rr.md.us.cme.writer.CMEFastFixEncoder;
import com.rr.model.generated.internal.events.factory.MsgSeqNumGapFactory;
import com.rr.model.generated.internal.events.impl.MsgSeqNumGapImpl;

/**
 * @WARN CHANGES TO THIS CLASS SHOULD BE CHECKED AGAINST CMENonBlockingFastFixSession
 */

public final class CMEFastFixSession extends FastFixSocketSession implements MarketDataChannel<Integer> {

    private final SuperPool<MsgSeqNumGapImpl> _gapPool    = SuperpoolManager.instance().getSuperPool( MsgSeqNumGapImpl.class );
    private final MsgSeqNumGapFactory         _gapFactory = new MsgSeqNumGapFactory( _gapPool );
    private       Integer[]                   _channelKeys;
    
    private       int                         _lastSeqNum;
    private       int                         _dups;
    private       int                         _gaps;

    public CMEFastFixSession(  String            name, 
                               MessageRouter     inboundRouter, 
                               SocketConfig      socketConfig, 
                               MessageDispatcher dispatcher, 
                               Encoder           encoder, 
                               Decoder           decoder,
                               ThreadPriority    receiverPriority ) {
        
        super( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, receiverPriority );
    }
    
    /**
     * only for use by simulator
     */
    public void handleNow( Message msg, int templateId, byte subChannel ) throws IOException {
        ((CMEFastFixEncoder)_encoder).encode( msg, templateId, subChannel );

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
    public void logInboundDecodingError( RuntimeDecodingException e ) {
        _logInErrMsg.copy( getComponentId() ).append( " lastSeqNum=" ).append( ((CMEFastFixDecoder)_decoder).getLastSeqNum() );
        _logInErrMsg.append( ' ' ).append( e.getMessage() );
        _log.error( ERR_IN_MSG, _logInErrMsg, e );
        ((FastFixDecoder)_decoder).logLastMsg();
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
    protected void persistIntegrityCheck( boolean inbound, long key, Message msg ) {
        // nothing
    }

    @Override
    protected void finalLog( ReusableString msg ) {
        super.finalLog( msg );
        msg.append( ", dups=" + _dups + ", gaps=" + _gaps );
    }
    
    @Override
    protected final void invokeController( Message msg ) {
        final int seqNum = msg.getMsgSeqNum();
        final int nextExpSeqNum = _lastSeqNum + 1;

        if ( seqNum == nextExpSeqNum || _lastSeqNum == 0 ) {
            _lastSeqNum = seqNum;
        } else  if ( seqNum > nextExpSeqNum ) {
            
            ++_gaps;

            _logInErrMsg.reset();
            _logInErrMsg.append( "Gap detected " ).append( getComponentId() ).append( " last=" ).append( _lastSeqNum ).append( ", gapSeq=" ).append( seqNum );
            _log.info( _logInErrMsg );
            
            dispatchMsgGap( 0, _lastSeqNum, seqNum );
            
            _lastSeqNum = seqNum;
        } else if ( seqNum == _lastSeqNum ) { // DUP 
        
            ++_dups;
            
            inboundRecycle( msg );
            
            return;
        }

        dispatchInbound( msg );
    }
    
    @Override
    protected void dispatchMsgGap( int channelId, int lastSeqNum, int seqNum ) {
        MsgSeqNumGapImpl gap = _gapFactory.get();
        
        gap.setChannelId( channelId );
        gap.setMsgSeqNum( seqNum );
        gap.setPrevSeqNum( lastSeqNum );
        
        dispatchInbound( gap );
    }
}
