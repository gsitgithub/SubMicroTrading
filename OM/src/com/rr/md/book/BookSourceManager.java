/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.component.SMTComponent;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.md.channel.MarketDataChannelBuilder;

public class BookSourceManager<T extends Book> implements SMTComponent {

    private final Set<BookSource<T>>                            _srcs = new LinkedHashSet<BookSource<T>>();
    private final String                                        _id;
    private final Map<String,BookSource<T>>                     _pipeLineIdToBookSrc = new ConcurrentHashMap<String,BookSource<T>>();
    private final Map<BookSource<T>, SimpleSubscriberMgr<T>>    _subMgr = new ConcurrentHashMap<BookSource<T>, SimpleSubscriberMgr<T>>();
    
    private       MarketDataChannelBuilder<Integer>             _mdSessionBuilder = null;  
    
    public BookSourceManager( String id ) {
        this( id, null );
    }
    
    public BookSourceManager( String id, BookSource<T>[] srcs ) {
        _id = id;

        if ( srcs != null ) {
            for( BookSource<T> src : srcs ) {
                add( src );
            }
        }
    }

    public synchronized void add( BookSource<T> src ) {
        _srcs.add( src );

        List<String> pipeLineIds = src.getPipeLineIds();
        
        for( String pipeId : pipeLineIds ) {
            if ( _pipeLineIdToBookSrc.containsKey( pipeId ) ) {
                throw new SMTRuntimeException( "Error duplicate pipeLineId " + pipeId + " used in bookSrc=" + src.getComponentId() +
                                               " and " + _pipeLineIdToBookSrc.get( pipeId ).getComponentId() ); 
            }
            
            _pipeLineIdToBookSrc.put( pipeId, src );
        }
        
        SimpleSubscriberMgr<T> subMgr = new SimpleSubscriberMgr<T>( "SubMgr" + src.getComponentId(), src );
        
        _subMgr.put( src, subMgr );
    }
    
    public synchronized BookSource<T> findSource( Instrument inst ) {
        BookSource<T> foundSrc = null;
        
        for( BookSource<T> src : _srcs ) {
            if ( src.supports( inst ) ) {
                foundSrc = src;
                break;
            }
        }
        
        return foundSrc;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    public BookSubscriptionMgr<T> findSubscriptionManager( Instrument inst, String pipeLineId ) {
        
        if ( pipeLineId == null || pipeLineId.length() == 0 ) {
            throw new SMTRuntimeException( "Missing pipelineId " + inst.getSecurityDesc() + " with pipeLineId=" + pipeLineId );
        }
        
        BookSource<T> src = _pipeLineIdToBookSrc.get( pipeLineId );
        
        if ( src == null ) {
            throw new SMTRuntimeException( "Unable to find book source for " + inst.getSecurityDesc() + " with pipeLineId=" + pipeLineId );
        }
        
        BookSubscriptionMgr<T> mgr = _subMgr.get( src );
        
        if ( mgr == null ) {
            throw new SMTRuntimeException( "Unable to find subscription manager for " + inst.getSecurityDesc() + " with pipeLineId=" + pipeLineId );
        }
        
        int channel = inst.getIntSegment();

        if ( channel == 0 ) {
            throw new SMTRuntimeException( "Instrument missing instrument channel/intSegment for " + inst.getSecurityDesc() + " with pipeLineId=" + pipeLineId );
        }
        
        ensureBookSourceHasMarketDataSession( src, channel, pipeLineId );
        
        return mgr;
    }

    public void setMdSessionBuilder( MarketDataChannelBuilder<Integer> mdSessionBuilder ) {
        _mdSessionBuilder = mdSessionBuilder;
    }

    @SuppressWarnings( "boxing" )
    private void ensureBookSourceHasMarketDataSession( BookSource<T> src, int channel, String pipeLineId ) {
        if ( _mdSessionBuilder != null ) {
            _mdSessionBuilder.register( channel, pipeLineId, src );
        }
    }
}
