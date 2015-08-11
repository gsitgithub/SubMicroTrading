/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import java.nio.ByteBuffer;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.MsgFlag;

public abstract class BaseLogEvent<T> implements Reusable<T>, LogEvent {

    public static final byte[]   TRUNCATED = "[TRUNCATED]".getBytes();

    protected static final int MAGIC_SPARE = 4;
    private   static final int MAX_LEN     = 16384;
    
    private            T            _next;                    // used for pooling
    protected volatile Message      _nextMessage    = null;   // used for concurrent Q
    
    protected       Level           _level;
    protected       long            _time;
    protected final ReusableString  _buf;

    
    public BaseLogEvent() {
        _buf = new ReusableString( getExpectedMaxEventSize() );
    }
    
    public BaseLogEvent( String str ) {
        _buf   = new ReusableString( str );
        _time  = System.currentTimeMillis();
        _level = Level.info;
    }
    
    @Override
    public ZString getMessage() {
        return _buf;
    }

    @Override
    public void reset() {
        _level = Level.info;
        _time  = 0;
        _buf.reset();
        _next  = null;
    }

    protected abstract int getExpectedMaxEventSize();

    @Override
    public void encode( ByteBuffer buf ) {
        TimeZoneCalculator.instance().utcFullTimeToLocal( buf, _time );
        
        int bufCapacity = (buf.capacity() - buf.position()) - MAGIC_SPARE;
        
        final byte[] levelHdr = _level.getLogHdr();
        
        final int msgLen = levelHdr.length + _buf.length();
        
        if ( msgLen > bufCapacity ) {
            int copyBytes = bufCapacity - TRUNCATED.length - levelHdr.length;
            buf.put( levelHdr, 0, levelHdr.length );
            buf.put( _buf.getBytes(), 0, copyBytes );
            buf.put( TRUNCATED, 0, TRUNCATED.length );
        } else {
            buf.put( levelHdr, 0, levelHdr.length );
            buf.put( _buf.getBytes(), 0, _buf.length() );
        }
    }

    @Override
    public Level getLevel() {
        return _level;
    }

    @Override
    public long getTime() {
        return _time;
    }

    @Override
    public int length() {
        return _buf.length();
    }

    @Override
    public T getNext() {
        return _next;
    }

    @Override
    public void setNext( T nxt ) {
        _next = nxt;
    }
    
    @Override
    public void set( Level lvl, byte[] bytes, int offset, int length ) {
        _time  = System.currentTimeMillis();
        _level = lvl;
        _buf.setValue( bytes, offset, length );
    }

    @Override
    public void set( Level lvl, byte[] bytes, int offset, int length, byte[] other, int offsetOther, int otherLength ) {
        _time  = System.currentTimeMillis();
        _level = lvl;
        _buf.setValue( bytes, offset, length );
        _buf.append( ' ' ).append( other, offsetOther, otherLength );
    }

    @Override
    public void set( Level lvl, ByteBuffer buf ) {
        _time  = System.currentTimeMillis();
        _level = lvl;
        
        final int offset = buf.position();
              int len    = buf.limit() - offset;
        
        _buf.reset();
        
        if ( len > MAX_LEN ) {
            len = MAX_LEN;
            buf.limit( offset + len );
            _buf.append( buf );
            _buf.append( TRUNCATED, 0, TRUNCATED.length );
        } else {
            _buf.append( buf );
        }
    }

    @Override
    public void set( Level lvl, String msg ) {
        _time  = System.currentTimeMillis();
        _level = lvl;
        _buf.setValue( msg );
    }

    @Override
    public void setError( ErrorCode code, String msg ) {
        _time  = System.currentTimeMillis();
        _level = Level.ERROR;
        _buf.copy( code.getError() ).append( ' ' ).append( msg );
    }

    @Override
    public void setError( ErrorCode code, String msg, Throwable t ) {
        _time  = System.currentTimeMillis();
        _level = Level.ERROR;
        _buf.copy( code.getError() ).append( ' ' ).append( msg );
        ExceptionTrace.format( _buf, t );
    }

    @Override
    public void setError( ErrorCode code, byte[] bytes, int offset, int length ) {
        _time  = System.currentTimeMillis();
        _level = Level.ERROR;
        _buf.copy( code.getError() ).append( ' ' ).append( bytes, offset, length );
    }

    @Override
    public void setError( ErrorCode code, byte[] bytes, int offset, int length, byte[] other, int offsetOther, int otherLength ) {
        _time  = System.currentTimeMillis();
        _level = Level.ERROR;
        _buf.copy( code.getError() ).append( ' ' ).append( bytes, offset, length ).append( ' ' ).append( other, offsetOther, otherLength );
    }

    /**
     * methods to comply with Message interface ... which is used to allow use of concurrent Q 
     * which generates no GC until for example ArrayBlockingQueue
     */

    @Override public final void             detachQueue()                               { _nextMessage = null; }
    @Override public final Message          getNextQueueEntry()                         { return _nextMessage; }
    @Override public final void             attachQueue( Message nxt )                  { _nextMessage = nxt; }
    
    @Override public final MessageHandler   getMessageHandler()                         { return null; }
    @Override public final void             setMessageHandler( MessageHandler handler ) { /* nothing */ }
    @Override public final int              getMsgSeqNum()                              { return 0; }
    @Override public final void             setMsgSeqNum( int seqNum )                  { /* nothing */ }
    @Override public final void             setFlag( MsgFlag flag, boolean on )         { /* not needed */ }
    @Override public final boolean          isFlagSet( MsgFlag flag )                   { return false; }
    @Override public final void             dump( ReusableString logInbound )           { /* not needed */ }
}
