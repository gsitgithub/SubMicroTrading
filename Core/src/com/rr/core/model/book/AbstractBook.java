/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

import com.rr.core.lang.ReusableString;
import com.rr.core.model.BookContext;
import com.rr.core.model.Instrument;

/**
 * for use when only read and write to book on same thread
 *
 * @author Richard Rose
 */
public abstract class AbstractBook implements MutableBook {

    protected final Instrument _instrument;

    protected int _ticks;

    protected int _seqNum;

    private long _lastTickInNanos;
    private long _lastExchangeTickTime;

    private int _msgSeqNum = 0;

    private BookContext _context;


    public AbstractBook( Instrument instrument ) { 
        _instrument = instrument;
    }

    @Override
    public void id( final ReusableString out ) {
        out.append( getClass().getSimpleName() ).append( " : " );
        _instrument.id( out );
    }

    @Override
    public final Instrument getInstrument() {
          return _instrument;
    }

    @Override
    public final String toString() {
        ReusableString buf = new ReusableString();
        dump( buf );
        return buf.toString();
    }

    @Override
    public abstract void setDirty( final boolean isDirty );

    @Override
    public final int getTickCount() {
        return _ticks;
    }

    @Override
    public final int getLastTickId() {
        return _seqNum;
    }

    @Override
    public final long getLastTickInNanos() {
        return _lastTickInNanos;
    }

    @Override
    public final void setLastTickInNanos( long nanoTS ) {
        _lastTickInNanos = nanoTS;
    }
    
    @Override
    public final void setLastTickId( int tickId ) {
        _seqNum = tickId;
    }
    
    @Override
    public final void setLastExchangeTickTime( long exchangeSentTime ) {
        _lastExchangeTickTime = exchangeSentTime;
    }

    @Override
    public final long getLastExchangeTickTime() {
        return _lastExchangeTickTime;
    }
    
    @Override
    public int getMsgSeqNum() {
        return _msgSeqNum;
    }

    public void setMsgSeqNum( final int msgSeqNum ) {
        _msgSeqNum = msgSeqNum;
    }
    
    @Override
    public BookContext setContext( BookContext context ) {
        BookContext prev = _context;
        _context = context;
        return prev;
    }

    @Override
    public BookContext getContext() {
        return _context;
    }
}
