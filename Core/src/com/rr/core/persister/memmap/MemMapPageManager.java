/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister.memmap;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.rr.core.java.JavaSpecific;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.persister.PersisterException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;

/**
 * uses a background thread to get the nextPage whenever it becomes null
 *
 * ONLY FOR USE BY OWNING Persister is NOT THREADSAFE FOR ANY OTHER USE ie dont share instance among persisters
 * 
 * popPage may be used by a thread other than the owning persister thread  
 * eg off the FixInbound thread to fetch messages from theoutboound persister which need to be sent as gap fill
 */
public class MemMapPageManager {
    
    private static final Logger _log = LoggerFactory.create( MemMapPageManager.class );
    private static final ZString SYNC_LOCK_DELAY_WARN = new ViewString( "MemMapPageMgr SYNC getPage delay us=" );
    private static final ZString PAGE_SWITCH_DELAY_WARN = new ViewString( "MemMapPageMgr PRELOADED getPage delay us=" );

    static final ErrorCode PERSIST_ERR = new ErrorCode( "MMBA100", "MemMap map error " );

    private class BackgroundAllocator extends Thread {

        private List<Page> _freeList = new ArrayList<Page>();
        
        private volatile int     _nextPageToFetch = -1;
        private          boolean _stopped         = false;

        private final ThreadPriority _priority;

        public BackgroundAllocator( String name, ThreadPriority priority ) {
            super( name );
            _priority = priority;
        }

        public void unmapNextPageAndGetNewNext( int nextPageToFetch ) {
            
            if ( _nextPage.getPageNo() != nextPageToFetch && _nextPage.getPageNo() >= 0 ) {
                Page page = new Page();
                move( page, _nextPage );
                _freeList.add( page );
            }
            
            _nextPageToFetch = nextPageToFetch;
        }
        
        @Override
        public void run() {

            Logger log = LoggerFactory.create( this.getClass() );

            ThreadUtils.setPriority( this, _priority );
            
            while( !_stopped ) {

                synchronized( _threadSync ) {
                    try { _threadSync.wait( Constants.LOW_PRI_LOOP_WAIT_MS ); } catch( InterruptedException e ) { /* */}
                }
                
                int count = 0;

                synchronized( _threadSync ) {
                    if ( _nextPageToFetch >= 0 && _nextPageToFetch != _nextPage.getPageNo() && !_stopped ) {
                        try {
                            _nextPage.reset();
                            map( _nextPage, _nextPageToFetch, false );  // dont log time mapping in background thread 
                        } catch( PersisterException e ) {
                            log.error( PERSIST_ERR, getName(), e );
                            _nextPageToFetch = -1;
                        }
                    }
                    count = _freeList.size();
                }
                
                for( int idx=count-1 ; idx >=0 ; idx-- ) {
                    synchronized( _threadSync ) {
                        Page page = _freeList.remove( idx );
                        
                        unmap( page, false );
                    }
                }
            }
        }

        public void finish() {
            _stopped = true;
        }
    }
    
    private final       int         _pageSize;
    private             FileChannel _channel;
    private             boolean     _logMapOps      = true;
    private             ZString     _name;
    
    private             Page        _lastPage       = new Page();
    private             Page        _curPage        = new Page();
                        Page        _nextPage       = new Page();   // all access to _nextPage MUST be syncd against threadSync
    private             Page        _tmpPage        = new Page();   

    private             Page        _floatingPage   = new Page();   // only used by PopPage

            final       ZString     _threadSync;
    
    private final       BackgroundAllocator _backAllocator;
    private             boolean             _trace = false;
    private final       ReusableString      _msg   = new ReusableString();
    
    public MemMapPageManager( ZString name, int pageSize, ThreadPriority priority ) {
        _pageSize = pageSize;
        _name     = name;
        
        _threadSync = new ViewString( _name );
        
        _backAllocator = new BackgroundAllocator( "MemMapAllocator" + _name, priority );
        
        _backAllocator.setDaemon( true );
        
        _backAllocator.start();
    }

    /**
     * init the memmap
     * 
     * @param channel
     */
    public void setChannel( FileChannel channel ) {
        _channel = channel;
    }

    public Page getPage( int newPageNum ) throws PersisterException {
        
        if ( newPageNum == _curPage.getPageNo() )  return _curPage;
        if ( newPageNum == _lastPage.getPageNo() ) return _lastPage;

        if ( newPageNum == _nextPage.getPageNo() ) { // moving from current to next which is already ready
            // unmap last page
            
            long startMove = Utils.nanoTime();
            
            synchronized( _threadSync ) {
                move( _tmpPage,  _lastPage );
                move( _lastPage, _curPage );
                move( _curPage,  _nextPage );
                move( _nextPage, _tmpPage );
                
                _backAllocator.unmapNextPageAndGetNewNext( newPageNum +1 );
                
                _threadSync.notifyAll();
            }
            
            logPageSwitchDelay( startMove );
            
            return _curPage;
        }
    
        // even if floatingPage is the page thats required remap it to save double checking in unmap
        
        // ok need a page thats not read read 

        move( _tmpPage,  _lastPage );
        move( _lastPage, _curPage );
        map( _curPage, newPageNum, _logMapOps );

        long start =  Utils.nanoTime();
        synchronized( _threadSync ) {
            move( _nextPage, _tmpPage );
            _backAllocator.unmapNextPageAndGetNewNext( newPageNum +1 );
            
            _threadSync.notifyAll();
        }
        
        logSyncGetDelay( start );
        
        return _curPage;
    }

    private void logSyncGetDelay( long start ) {
        long durationMicros = Math.abs(Utils.nanoTime() - start) >> 10;
        
        if ( durationMicros > 10 ) {
            _msg.copy( SYNC_LOCK_DELAY_WARN );
            _msg.append( durationMicros );
            _msg.append( " page=" );
            _msg.append( _curPage.getPageNo() );
            _msg.append( " name=" );
            _msg.append( _name );
            _log.warn( _msg );
        } else {
            if ( _logMapOps && durationMicros > 3 ) {
                _msg.copy( "MemMapPageMgr SYNC getPage delay us=" );
                _msg.append( durationMicros );
                _msg.append( " page=" );
                _msg.append( _curPage.getPageNo() );
                _msg.append( " name=" );
                _msg.append( _name );
                _log.info( _msg );
            }
        }
    }

    private void logPageSwitchDelay( long start ) {
        long durationMicros = Math.abs(Utils.nanoTime() - start) >> 10;
        
        if ( durationMicros > 4 ) {
            _msg.copy( PAGE_SWITCH_DELAY_WARN );
            _msg.append( durationMicros );
            _msg.append( " page=" );
            _msg.append( _curPage.getPageNo() );
            _msg.append( " name=" );
            _msg.append( _name );
            _log.warn( _msg );
        } else {
            if ( _logMapOps && durationMicros > 2 ) {
                _msg.copy( "MemMapPageMgr PRELOADED getPage delay us=" );
                _msg.append( durationMicros );
                _msg.append( " page=" );
                _msg.append( _curPage.getPageNo() );
                _msg.append( " name=" );
                _msg.append( _name );
                _log.info( _msg );
            }
        }
    }

    void move( Page toPage, Page fromPage ) {

        toPage.setMappedByteBuf( fromPage.getMappedByteBuf() );
        toPage.setPageNo( fromPage.getPageNo() );
        
        fromPage.reset();
    }

    /**
     * blocking call dont return until all unmapped (dont unmap nextPageas thats in free list in mapper thread)
     */
    public void unmapAll() {
        synchronized( _threadSync ) {
            _backAllocator.unmapNextPageAndGetNewNext( -1 );
        }
        unmap( _lastPage,     _logMapOps );
        unmap( _curPage,      _logMapOps );
        unmap( _floatingPage, _logMapOps );
    }

    void unmap( Page page, boolean logUnmap ){

        MappedByteBuffer buf = page.getMappedByteBuf();
        
        if ( buf != null ) {
            final int  pageNum = page.getPageNo();
            final long start   = Utils.nanoTime();

            if ( _trace  ) _log.info( "Unmap REQUEST page (-1 = background unmap) " + pageNum + ", logUnmap=" + logUnmap );

            page.reset();
            JavaSpecific.instance().unmap( _name, buf );
            
            if ( logUnmap ) {
                final long end = Utils.nanoTime();
                
                _log.info( "Unmap for page " + pageNum + ", mapper=" + _name + " : " + ((end-start) >> 10) + " usec" );
            }
        }
    }

    /*
     * get a page but dont unmap the current page, lastPage or nextPage
     * 
     * popPage only used for off main thread access eg fix resendRequest
     */
    public Page popPage( int recPage ) throws PersisterException {
        
        if ( recPage == _floatingPage.getPageNo() ) return _floatingPage;

        map( _floatingPage, recPage, _logMapOps );
        
        return _floatingPage;
    }

    void map( Page page, int pageToFetch, boolean logTime ) throws PersisterException {

        if ( pageToFetch < 0 ) {
            throw new PersisterException( "MemMapPageManager cant read page " + pageToFetch );
        }
            
        if ( _trace  ) _log.info( "MAP REQUEST page " + pageToFetch + ", logUnmap=" + logTime );

        long offset = pageToFetch * _pageSize;

        unmap( page, logTime );
        
        try {
            long start = 0, end = 0;
            
            if ( logTime ) start = Utils.nanoTime();
            
            MappedByteBuffer buffer = _channel.map( FileChannel.MapMode.READ_WRITE, offset, _pageSize );

            page.setPageNo( pageToFetch );
            page.setMappedByteBuf( buffer );
            
            if ( logTime ) {
                end = Utils.nanoTime();

                _log.info( "Map for page " + page.getPageNo() + ", mapper=" + _name + ", size=" + _pageSize + 
                           " : " + ((end-start) >>> 10) + " usec" );
            }
            
        } catch( IOException e ) {
            throw new PersisterException( "Failed to map page " + pageToFetch + " in persister " + _name + 
                                          ", size=" + _pageSize + ", offset=" + offset +
                                          " : " + e.getMessage(), e );
        }
    }

    public void shutdown() {
        synchronized( _threadSync ) {
            _backAllocator.finish();
            
            _threadSync.notifyAll();
        }
    }

    public void setLogTimes( boolean logTimes ) {
        _logMapOps = logTimes;
    }
}
