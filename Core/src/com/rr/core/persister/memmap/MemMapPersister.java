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
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import com.rr.core.lang.Constants;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.persister.PersistentReplayListener;
import com.rr.core.persister.PersisterException;
import com.rr.core.utils.ThreadPriority;

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
 * record OR the two byte end of page marker ! Thus the bits used in the end of page marker cannot be used by the app
 * 
 * @NOTE only one thread can be writing and one thread reading 
 * @NOTE the memmap manager maintains FOUR pages in memory ! SO for a session thats EIGHT !
 */
public class MemMapPersister extends BaseMemMapPersister {
    
    private static enum ReplayCode { EndOfPage, SkippedBad, LastRecord, ReplayedRec }

    private static final ErrorCode BAD_MAIN                 = new ErrorCode( "MMP100", "Bad main record " );
    private static final ErrorCode BAD_OPT                  = new ErrorCode( "MMP200", "Bad optional record " );
    private static final ErrorCode BAD_END_REC_MARKER       = new ErrorCode( "MMP300", "Bad end of record marker" );
    private static final ErrorCode FAIL_REPLAY              = new ErrorCode( "MMP400", "Replay listener threw exception" );
    
    private static final short     END_REC_MARKER             = 0x0E0F;
    private static final int       FLAGS_PLUS_LEN_PLUS_MARKER = 6;         // 2 byte flags, 2 byte rec len, 2 byte end mkr
    private static final short     PAGE_TRAILER               = 0x0302;    // bits used 
    private static final short     PAGE_TRAILER_SIZE          = 2;
    private static final int       OPT_LEN                    = 1;         // 1 byte len for optional context
    public static final short      DISALLOWED_FLAG_MASK       = ~PAGE_TRAILER; // bits used by end page marker cant be used in flags

    private static final int       MAX_REC_LEN = Constants.MAX_BUF_LEN - (PAGE_TRAILER_SIZE + FLAGS_PLUS_LEN_PLUS_MARKER + OPT_LEN);

    private static final ZString   MAIN_LEN_TOO_BIG       = new ViewString( "bad main rec len=" );
    private static final ZString   POS                    = new ViewString( ", pos=" );
    private static final ZString   LAST_GOOD              = new ViewString( ", lastGoodPos=" );
    private static final ZString   BAD_OPT_LEN            = new ViewString( "bad optional len=" );
    private static final ZString   FOUND                  = new ViewString( "found=" );
    private static final ZString   RESYSNC_PAGE_TRAILER   = new ViewString( "Resyncd persistence with page trailer" );
    private static final ZString   RESYSNC_REC_TRAILER    = new ViewString( "Resyncd persistence with record trailer" );
    private static final ZString   RECNO                  = new ViewString( ", recNo=" );
    
    private         int                   _maxPageOffset;     // _pageSize - TAIL_MKR_SIZE
    private         byte[]                _replayBuf    = new byte[ Constants.MAX_BUF_LEN ]; 
    private         byte[]                _replayOptBuf = new byte[ 128 ];

    private         PersistentReplayListener _replayCallback = null;
    private         int                      _offsetAfterLastReplayMessage; 
    
    public MemMapPersister( ZString name, ZString fname, long filePreSize, int pageSize, ThreadPriority priority ) {
        super( name, fname, filePreSize, pageSize, 32L, priority );
        
        _maxPageOffset = _pageSize - PAGE_TRAILER_SIZE;
        
        _log.info( "MemMapPersister " + name + ", maxRecSize=" + MAX_REC_LEN + ", pageSize=" + _pageSize + ", initFileSize=" + filePreSize );
    }

    /**
     * @throws IOException 
     * @NOTE never persist a record across a page boundary
     */
    @Override
    public long persist( byte[] fromBuffer, int offset, int length ) throws PersisterException {

        if ( length > MAX_REC_LEN ) throw new PersisterException( "Cant persist len=" + length + ",max=" + Constants.MAX_BUF_LEN );

        final int totRecLen = length + FLAGS_PLUS_LEN_PLUS_MARKER;
        
        checkRoomForWrite( totRecLen );
        
        final long key = _curPageNoKeyMask | _curPageOffset;

        //  <2byte lenA><2byte flags><dataA><2byte Marker>

        _curPage.putShort( (short)length );
        _curPage.putShort( (short)0 );
        _curPage.put(      fromBuffer, offset, length );
        _curPage.putShort( END_REC_MARKER );

        _curPageOffset += totRecLen;
        
        return key;
    }

    @Override
    public long persist( byte[] fromBuffer, int offset, int length, byte[] optional, int optOffset, int optLen ) throws PersisterException {
        final int totRecLen = length + FLAGS_PLUS_LEN_PLUS_MARKER + optLen + OPT_LEN;

        if ( totRecLen > MAX_REC_LEN ) throw new PersisterException( "Cant persist len=" + length + ",max=" + Constants.MAX_BUF_LEN );
        
        checkRoomForWrite( totRecLen );
        
        final long key = _curPageNoKeyMask | _curPageOffset;

        //  <2byte lenA><2byte flags><dataA><1 byte lenB><data B><2byte Marker>

        byte upper = UPPER_FLAG_OPT_CONTEXT;
        byte lower = 0;
        
        final byte blen = (byte)(0xFF & optLen);
        
        _curPage.putShort( (short)length );
        _curPage.put( upper );
        _curPage.put( lower );
        _curPage.put(      fromBuffer, offset, length );
        _curPage.put( blen );
        _curPage.put(      optional, optOffset, blen );
        _curPage.putShort( END_REC_MARKER );

        _curPageOffset += totRecLen;
        
        return key;
    }

    private void checkRoomForWrite( int totRecLen ) throws PersisterException {

        if ( _curPageOffset + totRecLen > _maxPageOffset ) {
            
            _curPage.putShort( PAGE_TRAILER );

            getPage( _curPageNo + 1 );
        }
    }
    
    @Override
    public int read( long key, byte[] outBuffer, int offset ) throws PersisterException {
        
        return read( key, outBuffer, offset, null );
    }

    private int fetch( MappedByteBuffer mm, int recOffset, byte[] outBuffer, int offset, ByteBuffer optionalContext ) throws PersisterException {

        mm.position( recOffset );
        
        short len   = mm.getShort();
        short flags = mm.getShort();
        
        if ( len > 0 ) {
            final int bufferLen = outBuffer.length - offset;

            if ( len <= bufferLen ) {
                mm.get( outBuffer, offset, len );
            } else {
                throw new PersisterException( "Free Buffer of " + bufferLen + " too small for record of " + len + " bytes" );
            }
        }
 
        if ( optionalContext != null ) {
            optionalContext.clear(); // clear even if no extra present to stop values being carried over erroneously
        }
        
        if ( (flags & FLAG_OPT_CONTEXT) > 0 ) {
            byte optLen = mm.get();
            if ( optLen != 0 ) {
                if ( optLen >=0 && optLen < 128 && (mm.position() + optLen <= _maxPageOffset) )  {
                    
                    if ( optionalContext != null ) {
                        if ( optionalContext.limit() < optLen ) {
                            throw new PersisterException( "Optional Buffer of " + optionalContext.limit() 
                                                          + " too small for record of " + optLen + " bytes" );
                        }
                        mm.get( _replayOptBuf, 0, optLen );
                        optionalContext.put( _replayOptBuf, 0, optLen );
                    } else {
                        int newPos = mm.position() + optLen;
                        mm.position( newPos );
                    }
                } else {
                    _logBuf.copy( BAD_OPT_LEN );
                    _logBuf.append( (0xFF&optLen) ).append( POS ).append( mm.position() );
                    
                    throw new PersisterException( _logBuf.toString() );
                }
            }
        }

        short marker = mm.getShort();

        if ( marker != END_REC_MARKER ) {
            _logBuf.copy( BAD_END_REC_MARKER.getError() );
            _logBuf.append( FOUND ).append( marker ).append( POS ).append( mm.position() );
            throw new PersisterException( _logBuf.toString() );
        }

        return len;
    }

    @Override
    public int read( long key, byte[] outBuffer, int offset, ByteBuffer optionalContext ) throws PersisterException {
        int recPage   = (int)(key >>> 32);
        int recOffset = (int)(key & 0xFFFFFFFF);
        
        int bytes;

        // dont use current page as thats only for writing
        Page page = _pageManager.popPage( recPage );

        MappedByteBuffer mm = page.getMappedByteBuf();
        
        bytes = fetch( mm, recOffset, outBuffer, offset, optionalContext );
     
        return bytes;
    }

    @Override
    
    
    public void replay( PersistentReplayListener listener ) throws PersisterException {

        _offsetAfterLastReplayMessage = 0;
        _replayCallback = listener;
        
        _replayCallback.started();
        
        try {
            while( ! replayNextPage() ) {
                getPage( _curPageNo + 1 );
            }
        } catch( PersisterException e ) {
            
            _replayCallback.failed();

            throw e;
        }

        _replayCallback.completed();
        
        _curPage.position( _offsetAfterLastReplayMessage );
        _curPageOffset = _offsetAfterLastReplayMessage;
    }

    // @return true if processed last  record
    private boolean replayNextPage() {

        int lastGoodPos;
        
        int recNum = 0;
        
        //  <2byte lenA><2byte flags><dataA><1 byte lenB><data B><2byte Marker>

        for( ; ; ) {
        
            lastGoodPos = _curPage.position();
            
            short s = _curPage.getShort();
            
            if ( s == PAGE_TRAILER ) { // page trailer need to go next page
                _offsetAfterLastReplayMessage = _curPage.position();
                break;
            } else if ( s == 0 ) {  // NO MORE RECS
                _offsetAfterLastReplayMessage = lastGoodPos;
                return true;
            } else {

                ++recNum;
                
                ReplayCode code = replayNextRec( s, lastGoodPos, recNum );

                _offsetAfterLastReplayMessage = _curPage.position();

                switch( code ) {
                case EndOfPage:
                    return false;
                case LastRecord:
                    return true;
                case ReplayedRec:
                case SkippedBad:
                    break;
                }
            }
        }
        
        return false;
    }

    private ReplayCode replayNextRec( short lenMain, int lastGoodPos, int recNum ) {
        
        final long key = _curPageNoKeyMask | lastGoodPos;
        
        short flags = _curPage.getShort();

        // get the main record buf
        if ( lenMain > Constants.MAX_BUF_LEN || (lastGoodPos + lenMain + FLAGS_PLUS_LEN_PLUS_MARKER > _maxPageOffset ) ) { // ERROR
            _logBuf.copy( MAIN_LEN_TOO_BIG );
            _logBuf.append( lenMain ).append( POS ).append( _curPage.position() ).append( LAST_GOOD ).append( lastGoodPos );
            _log.error( BAD_MAIN, _logBuf );

            return handleReplayError( lastGoodPos );
        } 
        
        _curPage.get( _replayBuf, 0, lenMain );
        
        byte optLen = 0;
        
        if ( (flags & FLAG_OPT_CONTEXT) > 0 ) {
            optLen = _curPage.get();
            if ( optLen != 0 ) {
                if ( optLen >=0 && optLen < 128 && (_curPage.position() + optLen <= _maxPageOffset) )  {
                    _curPage.get( _replayOptBuf, 0, optLen );
                } else {
                    _logBuf.copy( BAD_OPT_LEN );
                    _logBuf.append( (0xFF&optLen) ).append( POS ).append( _curPage.position() ).append( LAST_GOOD ).append( lastGoodPos );
                    _log.error( BAD_OPT, _logBuf );
                    
                    return handleReplayError( lastGoodPos );
                }
            }
        }

        short marker = _curPage.getShort();

        if ( marker != END_REC_MARKER ) {
            _logBuf.copy( FOUND );
            _logBuf.append( marker ).append( POS ).append( _curPage.position() ).append( LAST_GOOD ).append( lastGoodPos );
            _log.error( BAD_END_REC_MARKER, _logBuf );
        }

        try {
            if ( optLen > 0 ) {
                _replayCallback.message( this, key, _replayBuf, 0, lenMain, _replayOptBuf, 0, optLen, flags );
            } else {
                _replayCallback.message( this, key, _replayBuf, 0, lenMain, flags );
            }
        } catch( Exception e ) {
            _logBuf.copy( _name ).append( RECNO ).append( recNum ).append( POS );
            _logBuf.append( _curPage.position() ).append( LAST_GOOD ).append( lastGoodPos );
            
            _log.error( FAIL_REPLAY, _logBuf, e );
        }
        
        return ReplayCode.ReplayedRec;
    }
    
    private ReplayCode handleReplayError( int lastGoodPos ) {
        short res = consumeToMarker( lastGoodPos );  // skip forward to end of rec
        if ( res == PAGE_TRAILER )   return ReplayCode.EndOfPage;
        if ( res == END_REC_MARKER ) return ReplayCode.SkippedBad;

        // unable to resync truncate from last good point
        
        _curPage.position( lastGoodPos );
        _curPage.putShort( (short)0 );
        
        return ReplayCode.LastRecord;
    }

    private short consumeToMarker( int lastGoodPos ) {
        
        int last = 0xFF & _curPage.get();   
        int cur;
        int curMarker;
        
        while( _curPage.position() < _curPage.limit() ) {
            cur = 0xFF & _curPage.get();
            
            curMarker = (last << 8) + cur;
            
            if ( curMarker == PAGE_TRAILER ) {
                _logBuf.copy( RESYSNC_PAGE_TRAILER );
                _logBuf.append( POS ).append( _curPage.position() ).append( LAST_GOOD ).append( lastGoodPos );
                _log.info(_logBuf );
                
                return PAGE_TRAILER;
            }
            if ( curMarker == END_REC_MARKER ) {
                _logBuf.copy( RESYSNC_REC_TRAILER );
                _logBuf.append( POS ).append( _curPage.position() ).append( LAST_GOOD ).append( lastGoodPos );
                _log.info(_logBuf );

                return END_REC_MARKER;
            }
            
            last = cur;
        }
        
        return 0;   // unable to sync
    }

    @Override
    public void setLowerFlags( long key, byte flags ) throws PersisterException {
        int recPage   = (int)(key >>> 32);
        int recOffset = (int)(key & 0xFFFFFFFF);
        
        if ( recPage == _curPageNo ) {
            int prevPos = _curPageOffset;
            
            setFlags( _curPage, recOffset+3, flags );

            _curPage.position( prevPos );       // restore position
        } else {

            Page page = _pageManager.getPage( recPage );
    
            MappedByteBuffer mm = page.getMappedByteBuf();
            
            setFlags( mm, recOffset+3, flags );
        }
    }

    @Override
    public void setUpperFlags( long key, byte flags ) throws PersisterException {

        int recPage   = (int)(key >>> 32);
        int recOffset = (int)(key & 0xFFFFFFFF);
        
        if ( recPage == _curPageNo ) {
            int prevPos = _curPageOffset;
            
            setFlags( _curPage, recOffset+2, flags );

            _curPage.position( prevPos );       // restore position
        } else {

            Page page = _pageManager.getPage( recPage );
    
            MappedByteBuffer mm = page.getMappedByteBuf();
            
            setFlags( mm, recOffset+2, flags );
        }
    }

    private void setFlags( MappedByteBuffer mm, int recOffset, byte flags ) {
        mm.position( recOffset );
        
        byte existingFlags = mm.get();

        flags |= existingFlags;
        
        mm.position( recOffset );
        
        mm.put( flags );
    }
}
