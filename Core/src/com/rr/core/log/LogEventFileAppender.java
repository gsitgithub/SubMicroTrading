/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.rr.core.lang.CoreErrorCode;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.recycler.LogEventHugeRecycler;
import com.rr.core.recycler.LogEventLargeRecycler;
import com.rr.core.recycler.LogEventSmallRecycler;
import com.rr.core.utils.FileUtils;

public final class LogEventFileAppender implements Appender {
    
    private static final Logger _console = LoggerFactory.console( LogEventFileAppender.class );

    
    private static final ZString FILE_APPENDER_ALREADY_CLOSED = new ViewString( "FileAppender already closed" );
    private static final long    MIN_FILE_SIZE                = 10000000;
    private static final int     BUF_SIZE                     = 1024 * 1024;
    private static final int     SAFETY_BYTES                 = 256;    // leave space for extra formatting around the event

    private String           _baseFileName;
    private FileChannel      _channel;
    
    private boolean          _init         = false;

    private File             _file;
    
    private LogEventHugeRecycler  _recycleHugePool;
    private LogEventLargeRecycler _recycleLargePool;
    private LogEventSmallRecycler _recycleSmallPool;

    private int              _rollNumber  = 0;

    private FileOutputStream _stream;

    private final ByteBuffer _directBlockBuf;
    
    private long             _fileSize = 0;
    private final long       _maxFileSize;


    public LogEventFileAppender( String fileName, long maxFileSize ) {
        this( fileName, maxFileSize, true );
    }
    
    public LogEventFileAppender( String fileName, long maxFileSize, boolean enforceMinFileSize ) {
        _baseFileName = fileName;
        
        int blockSize = BUF_SIZE;
            
        if ( maxFileSize < MIN_FILE_SIZE  ) {
            if ( enforceMinFileSize ) {
                maxFileSize = MIN_FILE_SIZE;
            } else {
                blockSize = (int) maxFileSize;
            }
        }
        
        _directBlockBuf = ByteBuffer.allocateDirect( blockSize );
        
        _maxFileSize = maxFileSize;
    }

    @Override
    public synchronized void close() {

        if ( _channel != null ) {
            flush();

            try {
                if ( _stream != null && _stream.getFD().valid() )  _stream.close();
            } catch( Throwable t ) { /* NADA */ }

            try {
                _channel.close();
            } catch( Exception e ) { /* NADA */ }

            _channel = null;
            _stream = null;
            _file = null;
        }
    }
    
    @Override
    public synchronized void init() {

        SuperPool<LogEventSmall> spSmall = SuperpoolManager.instance().getSuperPool( LogEventSmall.class );
        SuperPool<LogEventLarge> spLarge = SuperpoolManager.instance().getSuperPool( LogEventLarge.class );
        SuperPool<LogEventHuge>  spHuge  = SuperpoolManager.instance().getSuperPool( LogEventHuge.class );

        _recycleSmallPool = new LogEventSmallRecycler( spSmall.getChainSize(), spSmall );
        _recycleLargePool = new LogEventLargeRecycler( spLarge.getChainSize(), spLarge );
        _recycleHugePool  = new LogEventHugeRecycler( spHuge.getChainSize(), spHuge );
        
        _init = true;
    }


    @Override
    public void handle( LogEvent curEvent ) {
        int startPos = _directBlockBuf.position();

        try {
            doLog( curEvent );

        } catch( Exception e ) {
            
            _console.info( "Failed to append log event -> will flush and retry" +
                           ", startPos=" + startPos + 
                           ", curPos=" + _directBlockBuf.position() + 
                           ", limit=" + _directBlockBuf.limit() +
                           ", capacity=" + _directBlockBuf.capacity() +
                           ", eventSize=" + curEvent.length() +
                           ", eventClass=" + curEvent.getClass().getSimpleName() +
                           ", errMsg=" + e.getMessage() );
            
            try {
                _directBlockBuf.position( startPos );
                blockWriteToDisk(); 
                doLog( curEvent );
                
            } catch( Exception e2 ) {
                _console.error( CoreErrorCode.ERR_RETRY, e.getMessage(), e );
            }
        }
    }
    
    public void doLog( LogEvent curEvent ) {

        if ( (curEvent.length() + _directBlockBuf.position() + SAFETY_BYTES) >= _directBlockBuf.capacity() ) {
            blockWriteToDisk();
        }

        curEvent.encode( _directBlockBuf );

        _directBlockBuf.put( (byte)0x0A );
    
        ReusableType type = curEvent.getReusableType();

        if ( type == CoreReusableType.LogEventSmall ) {
            _recycleSmallPool.recycle( (LogEventSmall)curEvent );
        } else if ( type == CoreReusableType.LogEventLarge ) {
            _recycleLargePool.recycle( (LogEventLarge)curEvent );
        } else {
            _recycleHugePool.recycle( (LogEventHuge)curEvent );
        }
    }

    @Override
    public synchronized void open() {

        if ( !_init ) {
            throw new RuntimeException( "FileAppender.open must invoke init first" );
        }

        if ( _channel != null ) {
            close();
        }

        String fileName = FileUtils.formRollableFileName( _baseFileName, ++_rollNumber, ".log" );
        
        try {
            FileUtils.mkDirIfNeeded( fileName );
            
            _file = new File( fileName );

            _stream = new FileOutputStream( _file, true );
        } catch( Exception e ) {
            throw new RuntimeException( "FileAppender.open error creating log file " + fileName, e );
        }

        _channel = _stream.getChannel();
    }

    private int blockWriteToDisk() {

        int eventSize = 0;

        try {
            _directBlockBuf.flip();
            eventSize = _directBlockBuf.limit();

            if ( eventSize > 0 ) {
                if ( _channel != null ) {
                    do { 
                        _channel.write( _directBlockBuf );
                    } while (_directBlockBuf.hasRemaining());
                } else {
                    _console.info( FILE_APPENDER_ALREADY_CLOSED );
                    _console.infoHuge( _directBlockBuf );
                }
            }
            
            _fileSize += eventSize;

            _directBlockBuf.clear();

            if ( _fileSize > _maxFileSize ) {
                synchronized( this ) {

                    close();
                    open();

                    _fileSize = 0;
                }
            }
            
        } catch( Exception e ) {
            _console.warn( "FileAppender: error to log : " + e.getMessage() );
            _console.infoHuge( _directBlockBuf );

            _directBlockBuf.clear();
        }

        return eventSize;
    }

    @Override
    public synchronized void flush() {

        if ( _directBlockBuf.position() > 0 ) {
            blockWriteToDisk();
        }

        if ( _channel != null ) {
            try {
                _channel.force( false );
            } catch( Exception e ) {
                _console.warn( "FileAppender : Failed to flush logfile :" + e.getMessage() );
            }
        }
    }
}