/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import java.util.LinkedHashMap;
import java.util.Map;

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.model.MsgFlag;
import com.rr.core.model.MessageHandler;
import com.rr.core.utils.NumberFormatUtils;

public abstract class BaseReject<T> implements Message, Reusable<T> {

    private static final int DEFAULT_REJECT_FIELD_ENTRIES = 32;

    private static final ZString SEQ_NUM = new ViewString( "34" );
    private static final ZString SRC     = new ViewString( "Src=" );
    private static final ZString DELIM   = new ViewString( ", " );
    
    private Map<ReusableString,ReusableString> _map = new LinkedHashMap<ReusableString,ReusableString>( DEFAULT_REJECT_FIELD_ENTRIES );

    private          Throwable      _throwable;
    private          int            _maxLen;
    private          T              _next;
    
    private volatile Message        _nextMessage;    // must be volatile for CAS queues
    private          MessageHandler _messageHandler;
    
    private          int            _seqNum = 0;
    private          byte           _flags = 0;
    
    public BaseReject( byte[] fixMsg, int offset, int maxIdx, Throwable t ) {
        _throwable = t;
        _maxLen = maxIdx - offset;

        DecoderUtils.populate( _map, fixMsg, offset, maxIdx );
        
        ReusableString seqNum = _map.get( SEQ_NUM );
        
        if ( seqNum != null ) {
            int iSeqNum = NumberFormatUtils.toInteger( seqNum );
            
            if ( iSeqNum > 0 ) _seqNum = iSeqNum;
        }
    }
    
    @Override
    public void reset()  {
        _throwable = null;
        _maxLen = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
        
        TLC.instance().recycleStringMap( _map );
    }

    public Throwable getThrowable() {
        return _throwable;
    }
    
    public String getMessage() {
        return (_throwable == null) ? null : _throwable.getMessage();
    }

    public int getNumFields() {
        return _map.size();
    }
    
    public int getMaxLen() {
        return _maxLen;
    }

    @Override
    public T getNext() {
        return _next;
    }

    @Override
    public void setNext( T nxt ) {
        _next = nxt;
    }

    public ReusableString getFixField( ZString key ) {
        return _map.get( key );
    }

    @Override
    public void attachQueue( Message nextNode ) {
        _nextMessage = nextNode;
    }

    @Override
    public void detachQueue() {
        _nextMessage = null;
    }
    
    @Override
    public Message getNextQueueEntry() {
        return _nextMessage;
    }
    
    @Override
    public MessageHandler getMessageHandler() {
        return _messageHandler;
    }

    @Override
    public void setMessageHandler( MessageHandler handler ) {
        _messageHandler = handler;
    }
    
    @Override
    public int getMsgSeqNum() {
        return _seqNum;
    }

    @Override
    public void setMsgSeqNum( int seqNum ) {
        _seqNum = seqNum;
    }

    @Override
    public void setFlag( MsgFlag flag, boolean isOn ) {
        _flags = (byte) MsgFlag.setFlag( _flags, flag, isOn );
    }

    @Override
    public boolean isFlagSet( MsgFlag flag ) {
        return MsgFlag.isOn( _flags, flag );
    }

    @Override
    public void dump( ReusableString out ) {
        if ( _messageHandler != null ) {
            out.append( SRC );
            out.append( _messageHandler.getComponentId() );
            out.append( DELIM );
        }
        out.append( getMessage() );
        for( Map.Entry<ReusableString, ReusableString> entry : _map.entrySet() ) {
            out.append( DELIM );
            out.append( entry.getKey() ).append( '=' ).append( entry.getValue() );
        }
    }
    
    @Override
    public String toString() {
        ReusableString tmp = new ReusableString();
        dump(tmp);
        return tmp.toString();
    }
}
