/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister.memmap;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.persister.Persister;
import com.rr.core.persister.PersisterException;
import com.rr.core.tasks.ScheduledEvent;
import com.rr.core.tasks.Scheduler;
import com.rr.core.utils.FileException;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ShutdownManager.Callback;

/**
 * each persister operates on a single file
 * 
 * long key is composed of an int pageNUmber and an int offset within the page
 * this caters for a page size upto 4GB !
 * 
 * in general page size will be 10MB ... tho this needs some experimentation
 * need three pages mapped
 *      previousPage       so when markConf called we should have page mapped
 *      curPage            current page for writing new records too
 *      nextFreePage       so dont have to wait, when curPage full go straight to next free page
 *      
 * Page management is via MemMapPageHelper 
 * 
 * If the persistMarker is invoked on a page not in memory then the helper will retrieve a temp page
 * The helper will allow a list of 3 small temp pages of 1MB each
 * 
 *   <2byte lenA><2byte flags><dataA><1 byte lenB><data B><2byte Marker>
 * 
 *   if dataB exists the context flag is set
 * 
 * IMPORTANT, when reading from start to finish, reading two bytes at start of record can be either the 2 byte flag of a new 
 * record OR the two byte end of page marker ! This the bits used in the end of page marker cannot be used by the app
 * 
 * @NOTE the memmap manager maintains FOUR pages in memory ! SO for a session thats EIGHT !
 */
public abstract class BaseMemMapPersister implements Persister {

            static final Logger    _log                     = LoggerFactory.create( BaseMemMapPersister.class );

            static final ErrorCode DATE_ROLL_FAIL           = new ErrorCode( "MMP500", "Error during date roll" );

    public static final  byte      UPPER_FLAG_OPT_CONTEXT   = 1 << 6;                       // the persisted record has optional context
    public static final  short     FLAG_OPT_CONTEXT         = UPPER_FLAG_OPT_CONTEXT << 8;  // byte flag as an short

    private static final long      MIN_FILE_SIZE            = 1024 * 1024;
    private static final int       MIN_PAGE_SIZE            = 1024;


              final ReusableString        _name  = new ReusableString();
    private   final ReusableString        _fname = new ReusableString();
    private   final long                  _filePreSize;
    protected final int                   _pageSize;
    private         RandomAccessFile      _file;
    private         FileChannel           _channel;
    protected final MemMapPageManager     _pageManager;

    protected       MappedByteBuffer      _curPage;
    protected       int                   _curPageNo;
    protected       int                   _curPageOffset;     // offset to start of next record in page
    protected       long                  _curPageNoKeyMask;

    protected       ReusableString        _logBuf       = new ReusableString( 100 );

    private         long                  _bitShiftForPageMask;
    
    public BaseMemMapPersister( ZString name, ZString fname, long filePreSize, int pageSize, long bitShiftForPageMask, ThreadPriority priority ) {
        super();

        _name.copy(  name );
        _fname.copy( fname );
        if ( filePreSize < MIN_FILE_SIZE ) filePreSize = MIN_FILE_SIZE;
        if ( pageSize < MIN_PAGE_SIZE )    pageSize    = MIN_PAGE_SIZE;
        

        _log.info( "MemMapPersister " + name + ", filePreSize=" + filePreSize + ", pageSize=" + pageSize );
        
        _filePreSize   = filePreSize;
        
        _pageSize      = pageSize;
        _pageManager   = new MemMapPageManager( _name, _pageSize, priority );
    
        _bitShiftForPageMask = bitShiftForPageMask;
        
        ShutdownManager.instance().register( new Callback() {
                                                    @Override
                                                    public void shuttingDown() {
                                                        shutdown();
                                                    }} );
        
        Scheduler.instance().registerForGroupEvent( ScheduledEvent.EndOfDay, 
                                                    
                                       new Scheduler.Callback() {
                                               private final ZString _cbName = new ViewString( _name + "DateRoll" );
                                    
                                               @Override
                                               public ZString getName() {
                                                   return _cbName;
                                               }
                                               
                                               @Override
                                               public void event( ScheduledEvent event ) {
                                                   try {
                                                        rollPersistence();
                                                    } catch( PersisterException e ) {
                                                        _log.error( DATE_ROLL_FAIL, "", e );
                                                        
                                                        ShutdownManager.instance().shutdown( -1 );
                                                    }
                                                   }
                                               } );
    }

    @Override
    public void open() throws PersisterException {
        String fname = _fname.toString();
        
        try {
            FileUtils.mkDirIfNeeded( fname );
            
            _file = new RandomAccessFile( fname, "rw" );
            
            if ( _file.length() == 0 ) {
                _file.setLength( _filePreSize );
            }
            
            _channel = _file.getChannel();

            _pageManager.setChannel( _channel );
            
            getPage( 0 );
            
        } catch( FileException e ) {
            throw new PersisterException( "MemMapPersister " + _name + " failed to mkdir for file " + fname, e );
        } catch( IOException e ) {
            throw new PersisterException( "MemMapPersister " + _name + " failed to memmap file " + fname, e );
        }
    }

    @Override
    public void close() {
        _pageManager.unmapAll();
        _pageManager.shutdown();
        
        _curPage            = null;
        _curPageNo          = 0;
        _curPageNoKeyMask   = 0;
        _curPageOffset      = 0;

        close( _channel );
        _channel = null;
        close( _file );
        _file = null;
    }
    
    @Override
    public void rollPersistence() throws PersisterException {
        close();
        FileUtils.backup( _fname.toString() );
        open();
    }
    
    protected final void getPage( int newPageNum ) throws PersisterException {
        Page page           = _pageManager.getPage( newPageNum );
        
        _curPage            = page.getMappedByteBuf();
        _curPageNo          = page.getPageNo();
        
        if ( _curPageNo != newPageNum ) {
            throw new PersisterException( "Failed to obtain page " + newPageNum + ", pageMgr returnd " + _curPageNo );
        }
        
        _curPageNoKeyMask   = (long)_curPageNo << _bitShiftForPageMask;
        _curPageOffset = 0;      
    }

    private final void close( Closeable obj ) {
        if ( obj == null ) return;
        try { obj.close(); } catch( Exception e ) { /* dont care */ }
    }

    public void shutdown() {
        close();
        _pageManager.shutdown();
    }

    public final void setLogTimes( boolean logTimes ) {
        _pageManager.setLogTimes( logTimes );
    }

    @Override
    public ReusableString log( ReusableString logMsg ) {
        logMsg.append( ", curPageNo=" ).append( _curPageNo ).append( ", curPageOffset=" ).append( _curPageOffset );
        
        return logMsg;
    }
}
