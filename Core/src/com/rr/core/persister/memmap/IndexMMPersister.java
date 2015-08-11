/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister.memmap;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.persister.IndexPersister;
import com.rr.core.persister.PersistentReplayListener;
import com.rr.core.persister.PersisterException;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.core.utils.ThreadPriority;

/*
 * index mem map persister, doesnt use header / trailer markers
 * 
 * simple page size is power of 2, and can store exact number of 16 byte records which hold the key of the actual record in
 * the delegate persister and the seq num.
 * 
 * @NOTE only one thread can be writing and one thread reading 
 */
public class IndexMMPersister extends BaseMemMapPersister implements IndexPersister {

            static final int SHIFTS_FOR_INDEX_PAGE_MASK = 21;               // will give page size of 2MB
            static final int SHIFTS_PER_RECORD          = 4;                // as record size is 16
            static final int SHIFTS_FOR_SEQ_NUM_TO_PAGE = SHIFTS_FOR_INDEX_PAGE_MASK  - SHIFTS_PER_RECORD;
    private static final int PAGE_SIZE                  = 1 << SHIFTS_FOR_INDEX_PAGE_MASK;
            static final int RECORD_SIZE                = 16;               // <8 byte recAddr><4 byte seqNum><4 byte not used>
            static final int ENTRIES_PER_PAGE           = PAGE_SIZE >> SHIFTS_PER_RECORD;   
            static final int MASK_RECS_IN_PAGE          = ENTRIES_PER_PAGE - 1;       // as shifts per page is 20
    
    private static final ErrorCode ERR_FAIL_ADD_INDEX_ENTRIES = new ErrorCode( "IMP100", "Failed to add dummy index entries for range" );
    private static final ErrorCode ERR_FAIL_RM_INDEX_ENTRIES  = new ErrorCode( "IMP200", "Failed to remove index entries for range" );
            static final ErrorCode SHUT_ERR                   = new ErrorCode( "IMP300", "Exception in shutdown" );
    private static final ErrorCode ERR_FAIL_VERIFY_INDEX      = new ErrorCode( "IMP400", "Failed to reinstate index entry" );

    private static final ZString BAD_INDEX              = new ViewString( "BadIndex missing key to data in data file for seqNum=" );
    private static final ZString MISMATCH_SEQNUM        = new ViewString( "Persisted appSeqNum had unexpected value, persisted=" );
    private static final ZString EXPECTED               = new ViewString( ", expected=" );
    private static final ZString FROM                   = new ViewString( ", from=" );
    private static final ZString TO                     = new ViewString( ", to=" );
    private static final ZString KEY                    = new ViewString( ", key=" );
    private static final ZString SEQ_NUM                = new ViewString( ", seqNum=" );
    private static final ZString INDEX_PAGE             = new ViewString( ", indexPage=" );

    private static final ZString RESET_INDEX            = new ViewString( "Replay : fixing index mismatch, appSeqNum=" );
    private static final ZString IDX_PAGE               = new ViewString( ", idxPage=" );
    private static final ZString IDX_OFFSET             = new ViewString( ", idxOffset=" );
    private static final ZString EXP_KEY                = new ViewString( ", expKey=" );
    private static final ZString CHK_KEY                = new ViewString( ", chkKey=" );
    private static final ZString CHK_SEQ_NUM            = new ViewString( ", chkSeqNum=" );
    
    
    private final MemMapPersister _recordPersister;         // the recordPersister is the delegate that persistes the actual records
    
    public IndexMMPersister( MemMapPersister recordPersister, ZString name, ZString fname, long filePreSize, int pageSize, ThreadPriority priority ) {
        super( name, fname, filePreSize, pageSize, SHIFTS_FOR_INDEX_PAGE_MASK, priority );
        
        _log.info( "IndexMMPersister " + name + ", preSize=" + filePreSize + 
                   ", recSize=" + RECORD_SIZE + 
                   ", entriesPage=" + ENTRIES_PER_PAGE + ", pageSize=" + PAGE_SIZE );
        
        _recordPersister = recordPersister;
        
        registerShutdownFlush();
    }

    private void registerShutdownFlush() {
        ShutdownManager.instance().register( new Callback(){

            @Override
            public void shuttingDown() {
                
                try {
                    ReusableString msg = new ReusableString();
                    _log.info( msg );
                    close();
                } catch( Throwable t ) {
                    _log.error( SHUT_ERR, "Unexpected exception in IndexMMPersister shutdown: " + t.getMessage(), t );
                }
                
            }} );
    }

    public IndexMMPersister( MemMapPersister recordPersister, ZString name, ZString fname, long filePreSize, ThreadPriority priority ) {
        super( name, fname, filePreSize, PAGE_SIZE, SHIFTS_FOR_INDEX_PAGE_MASK, priority );
        
        _log.info( "IndexMMPersister " + name + ", preSize=" + filePreSize + 
                   ", recSize=" + RECORD_SIZE + 
                   ", entriesPage=" + ENTRIES_PER_PAGE + ", pageSize=" + PAGE_SIZE );
        
        _recordPersister = recordPersister;
    }

    @Override
    public long persistIdxAndRec( int appSeqNum, byte[] inBuffer, int offset, int length ) throws PersisterException {
        
        long key = _recordPersister.persist( inBuffer, offset, length );
        
        int  indexPage = appSeqNum >>> SHIFTS_FOR_SEQ_NUM_TO_PAGE;
        int  recOffset = (appSeqNum & MASK_RECS_IN_PAGE) << SHIFTS_PER_RECORD;
        
        // _log.info( "written seqNum=" + appSeqNum + ", idxPage=" + indexPage + ", idxOffset=" + recOffset + ", recKey=" + key );
        
        if ( indexPage != _curPageNo ) {

            getPage( indexPage );
        }
        
        _curPage.position( recOffset );
        _curPage.putLong( key );
        _curPage.putInt( appSeqNum );
        
        return key;
    }

    @Override
    public long persistIdxAndRec( int appSeqNum, byte[] inBuffer, int offset, int length, byte[] optional, int optOffset, int optLen ) throws PersisterException {
        long key = _recordPersister.persist( inBuffer, offset, length, optional, optOffset, optLen );
        
        int  indexPage = appSeqNum >>> SHIFTS_FOR_SEQ_NUM_TO_PAGE;
        int  recOffset = (appSeqNum & MASK_RECS_IN_PAGE) << SHIFTS_PER_RECORD;
        
        if ( indexPage != _curPageNo ) {

            getPage( indexPage );
        }
        
        _curPage.position( recOffset );
        _curPage.putLong( key );
        _curPage.putInt( appSeqNum );
        
        return key;
    }

    @Override
    public int readFromIndex( int appSeqNum, byte[] outBuffer, int offset ) throws PersisterException {
        int  indexPage = appSeqNum >>> SHIFTS_FOR_SEQ_NUM_TO_PAGE;
        int  recOffset = (appSeqNum & MASK_RECS_IN_PAGE) << SHIFTS_PER_RECORD;

        int  bytes       = -1;
        long key;
        long checkAppNum;            
        
        Page page = _pageManager.popPage( indexPage );

        MappedByteBuffer mm = page.getMappedByteBuf();

        mm.position( recOffset );
        
        key         = mm.getLong();
        checkAppNum = mm.getInt();

        // _log.info( "read seqNum=" + appSeqNum + ", idxPage=" + indexPage + ", idxOffset=" + recOffset + ", recKey=" + key + ", checkSeqNum=" + checkAppNum );
        
        if ( checkAppNum == appSeqNum ) {
            if ( key != -1 ) {
                bytes = _recordPersister.read( key, outBuffer, offset );
            } else {
                _logBuf.copy( BAD_INDEX ).append( appSeqNum );
                _log.warn( _logBuf );
            }
        } else {
            _logBuf.copy( MISMATCH_SEQNUM ).append( checkAppNum ).append( EXPECTED ).append( appSeqNum );
            _log.warn( _logBuf );
        }

        return bytes;
    }

    @Override
    public int readFromIndex( int appSeqNum, byte[] outBuffer, int offset, ByteBuffer optionalContext ) throws PersisterException {
        int  indexPage = appSeqNum >>> SHIFTS_FOR_SEQ_NUM_TO_PAGE;
        int  recOffset = (appSeqNum & MASK_RECS_IN_PAGE) << SHIFTS_PER_RECORD;

        int  bytes       = -1;
        long key;
        long checkAppNum;            
        
        Page page = _pageManager.popPage( indexPage );

        MappedByteBuffer mm = page.getMappedByteBuf();

        mm.position( recOffset );
        
        key         = mm.getLong();
        checkAppNum = mm.getInt();
        
        if ( checkAppNum == appSeqNum ) {
            
            if ( key == -1 ) return 0;
            
            bytes = _recordPersister.read( key, outBuffer, offset, optionalContext );
        } else {
            _logBuf.copy( MISMATCH_SEQNUM ).append( checkAppNum ).append( EXPECTED ).append( appSeqNum );
            _log.warn( _logBuf );
        }

        return bytes;
    }

    @Override
    public long persist( byte[] inBuffer, int offset, int length ) throws PersisterException {
        return _recordPersister.persist( inBuffer, offset, length );
    }

    @Override
    public long persist( byte[] inBuffer, int offset, int length, byte[] optional, int optOffset, int optLen ) throws PersisterException {
        return _recordPersister.persist( inBuffer, offset, length, optional, optOffset, optLen );
    }

    @Override
    public int read( long key, byte[] outBuffer, int offset ) throws PersisterException {
        return _recordPersister.read( key, outBuffer, offset );
    }

    @Override
    public int read( long key, byte[] outBuffer, int offset, ByteBuffer optionalContext ) throws PersisterException {
        return _recordPersister.read( key, outBuffer, offset, optionalContext );
    }

    /**
     * replay from the record persister not the index file
     */
    @Override
    public void replay( final PersistentReplayListener listener ) throws PersisterException {
        _recordPersister.replay( listener );
    }

    @Override
    public void setLowerFlags( long persistedKey, byte flags ) throws PersisterException {
        _recordPersister.setLowerFlags( persistedKey, flags );
    }

    @Override
    public void setUpperFlags( long persistedKey, byte flags ) throws PersisterException {
        _recordPersister.setUpperFlags( persistedKey, flags );
    }

    @Override
    public void open() throws PersisterException {
        _recordPersister.open();
        super.open();
    }
    
    @Override
    public void close() {
        super.close();
        _recordPersister.close();
    }
    
    @Override
    public void rollPersistence() throws PersisterException {
        _recordPersister.rollPersistence();
        super.rollPersistence();
    }
    
    @Override
    public void shutdown() {
        _recordPersister.shutdown();

        super.shutdown();
    }
    
    /**
     * add -1 entries
     * 
     * @return true if ok
     */
    @Override
    public boolean addIndexEntries( int fromSeqNum, int toSeqNum ) {
        
        try {
            for( int idx = fromSeqNum ; idx <= toSeqNum ; ++idx ) {
                int  indexPage = idx >>> SHIFTS_FOR_SEQ_NUM_TO_PAGE;
                int  recOffset = (idx & MASK_RECS_IN_PAGE) << SHIFTS_PER_RECORD;
   
                long checkAppNum;            
                
                if ( indexPage != _curPageNo ) {
                    getPage( indexPage );
                }
                
                _curPage.position( recOffset );
                
                _curPage.getLong();
                checkAppNum = _curPage.getInt();            
                
                if ( checkAppNum == idx ) {
                    // nothing to do
                } else {
                    _curPage.position( recOffset );
                    _curPage.putLong( -1 );
                    _curPage.putInt( idx );
                }
            }
        } catch( PersisterException e ) {
            _logBuf.copy( FROM ).append( fromSeqNum ).append( TO ).append( toSeqNum );
            
            _log.error(  ERR_FAIL_ADD_INDEX_ENTRIES, _logBuf );
            return false;
        }

        return true;
    }

    @Override
    public boolean removeIndexEntries( int fromSeqNum, int toSeqNum ) {

        try {
            for( int idx = fromSeqNum ; idx <= toSeqNum ; ++idx ) {
                int  indexPage = idx >>> SHIFTS_FOR_SEQ_NUM_TO_PAGE;
                int  recOffset = (idx & MASK_RECS_IN_PAGE) << SHIFTS_PER_RECORD;
   
                if ( indexPage != _curPageNo ) {
                    getPage( indexPage );
                }
                
                _curPage.position( recOffset );
                _curPage.putLong( -1 );
                _curPage.putInt( idx );
            }
        } catch( PersisterException e ) {
            _logBuf.copy( FROM ).append( fromSeqNum ).append( TO ).append( toSeqNum );
            
            _log.error(  ERR_FAIL_RM_INDEX_ENTRIES, _logBuf );
            return false;
        }

        return true;
    }

    @Override
    public ReusableString log( ReusableString logMsg ) {
        logMsg.append( "IDX[" );
        super.log( logMsg ).append( "], DAT[" );
        _recordPersister.log( logMsg ).append( ']' );
        return logMsg;
    }

    @Override
    public boolean verifyIndex( long key, int appSeqNum ) {
        int  indexPage = appSeqNum >>> SHIFTS_FOR_SEQ_NUM_TO_PAGE;
        int  idxOffset = (appSeqNum & MASK_RECS_IN_PAGE) << SHIFTS_PER_RECORD;

        long checkAppNum;
        
        boolean matched = false;
        
        try {
            Page page = _pageManager.popPage( indexPage );

            MappedByteBuffer mm = page.getMappedByteBuf();

            mm.position( idxOffset );
            
            long checkKey = mm.getLong();
            checkAppNum   = mm.getInt();

//            _log.info( "verify seqNum=" + appSeqNum   + 
//                       ", checkSeqNum=" + checkAppNum + 
//                       ", expKey="      + key         + 
//                       ", checkKey="    + checkKey    + 
//                       ", idxPage="     + indexPage   + 
//                       ", idxOffset="   + idxOffset );
            
            if ( checkAppNum != appSeqNum || key != checkKey ) {
                _logBuf.copy( RESET_INDEX )  .append( appSeqNum )
                       .append( CHK_SEQ_NUM ).append( checkAppNum )
                       .append( EXP_KEY )    .append( key )
                       .append( CHK_KEY )    .append( checkKey )
                       .append( IDX_PAGE )   .append( indexPage )
                       .append( IDX_OFFSET ) .append( idxOffset );
                
                _log.warn( _logBuf );
                
                mm.position( idxOffset );
                _curPage.putLong( key );
                _curPage.putInt( appSeqNum );
            } else {
                matched = true;
            }
        } catch( PersisterException e ) {
            _logBuf.copy( KEY ).append( key ).append( SEQ_NUM ).append( appSeqNum ).append( INDEX_PAGE ).append( indexPage );
            
            _log.error( ERR_FAIL_VERIFY_INDEX, _logBuf );
        }
        
        return matched;
    }
}
