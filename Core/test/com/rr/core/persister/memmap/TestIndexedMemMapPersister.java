/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister.memmap;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.persister.PersistentReplayListener;
import com.rr.core.persister.Persister;
import com.rr.core.persister.PersisterException;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.ThreadPriority;

public class TestIndexedMemMapPersister extends BaseTestMemMapPersister {

    private final static ZString _indexFileNameBase = new ViewString( "./tmp/TestMemMapPersister" );
    private       static int     _indexIdx          = 0;
    
    private IndexMMPersister _indexPersister = null;

    private synchronized static int nextIdx() {
        return ++_indexIdx;
    }

    @Override
    public void setUp() {
        try {
            super.setUp();
            
            ReusableString fileName = new ReusableString( _indexFileNameBase );
            fileName.append( nextIdx() ).append( ".idx" );
            FileUtils.rm( fileName.toString() );
            _indexPersister = new IndexMMPersister( _persister, new ViewString("TestIdxPersist"), fileName, 1000000, ThreadPriority.Other );
            _indexPersister.setLogTimes( false );
            _indexPersister.open();
            
            _readWrap.setBuffer( _readBuf, 0 );
            
        } catch( Exception e ) {
            assertFalse( "FAIL: " + e.getMessage(), true );
        }
    }
    
    @Override
    public void tearDown() {
        _indexPersister.shutdown();
        
        super.tearDown();
    }
    
    public void testIndexCalcs() {
    
        check( IndexMMPersister.ENTRIES_PER_PAGE - 1, 0, (IndexMMPersister.ENTRIES_PER_PAGE - 1) * IndexMMPersister.RECORD_SIZE );
        check( IndexMMPersister.ENTRIES_PER_PAGE,     1, 0 );
        check( IndexMMPersister.ENTRIES_PER_PAGE + 1, 1, IndexMMPersister.RECORD_SIZE );
    }
    
    private void check( int seqNum, int expPage, int expOffset ) {
        assertEquals( expPage,   seqNum >>> IndexMMPersister.SHIFTS_FOR_SEQ_NUM_TO_PAGE );
        assertEquals( expOffset, (seqNum & IndexMMPersister.MASK_RECS_IN_PAGE) << IndexMMPersister.SHIFTS_PER_RECORD );
    }

    public void testSimple() {

        int    numRecs = 1000;
        long   pkey    = -1;
        long[] pkeys   = new long[ numRecs ];
        
        for ( int i=0 ; i < numRecs ; ++i ) {
            pkey = writeLine( i );
            
            pkeys[ i ] = pkey;
        }
        
        for ( int i=0 ; i < numRecs ; ++i ) {
            readAndVerifyLineByIndex( i, pkeys[i] );
        }
    }
    
    public void testInterleavedFlagMark() throws PersisterException {

        int    numRecs = 1000;
        long   pkey    = -1;
        long[] pkeys   = new long[ numRecs ];
        
        for ( int i=0 ; i < numRecs ; ++i ) {
            pkey = writeLine( i );
            
            pkeys[ i ] = pkey;
            
            if ( i > 0 ) {
                _persister.setUpperFlags( pkeys[i-1], (byte)16 );
            }
        }
        
        for ( int i=0 ; i < numRecs ; ++i ) {
            readAndVerifyLineByIndex( i, pkeys[i] );
        }
    }
    
    private void readAndVerifyLineByIndex( int idx, long key ) {
        
        try {
            int bytes = _indexPersister.readFromIndex( idx, _readBuf, 0 );
            
            _readWrap.setLength( bytes );
            
            formLine( idx );
            
            assertEquals( _genBuf, _readWrap );
            
            // now check can read using key
            
            super.readAndVerifyLine( idx, key );
            
        } catch( Exception e ) {
            assertTrue( "Failed to read row " + idx + " : " + e.getMessage(), false );
        }
    }

    public void testReplayAndPostReplayAppending() throws PersisterException {
        int    numRecs       = 1000;
        int    runTwoTotal   = numRecs * 2;
        int    runThreeTotal = numRecs * 3;
        long[] pkeys         = new long[ runThreeTotal ];
        
        writeRecs( 0, numRecs, pkeys );

        checkRecovery( numRecs );
        
        writeRecs( numRecs, runTwoTotal, pkeys );

        verify( 0, runTwoTotal, pkeys );

        checkRecovery( runTwoTotal );

        writeRecs( runTwoTotal, runThreeTotal, pkeys );

        checkRecovery( runThreeTotal );

        verify( 0, runThreeTotal, pkeys );
    }

    public void testReplaySet1() throws PersisterException {
        doTestRecs( 1, 2, 3, 4, 5 );
    }

    public void testReplaySet2() throws PersisterException {
        doTestRecs( 1, 2, 3, 5, 7, 11 );
    }

    public void testReplaySet3() throws PersisterException {
        doTestRecs( 1, 2, 3, 5, 7, 11 );
    }

    public void testReplaySet4() throws PersisterException {
        doTestRecs( 3, 5, 7, 11, 12, 13 );
    }

    private void doTestRecs( int ... seqNums ) throws PersisterException {
        for( int seqNum : seqNums ) {
            writeLine( seqNum );
        }

        checkRecovery( seqNums );
    }

    private void checkRecovery( final int[] seqNums ) throws PersisterException {
        _count = 0; 
        _startedRecovery = false;
        _completedRecovery = false;
        getPersister().close();
        getPersister().open();
        getPersister().replay( new PersistentReplayListener() {
                
                @Override
                public void started() {
                    _startedRecovery = true;
                    assertEquals( 0,  _count );
                }
                
                @Override
                public void message( Persister p, long key, byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, short flags ) {
                    assertTrue( false );
                }
                
                @Override
                public void message( Persister p, long key, byte[] buf, int offset, int len, short flags ) {
                    int seqNum = seqNums[ _count++ ];

                    formLine( seqNum );

                    assertEquals( 0, offset );
                    _readWrap.setBuffer( buf, len );
                    
                    assertEquals( _genBuf, _readWrap );

                    assertEquals( 0, flags );   // this test didnt set any flags
                    
                    verify( p, key, seqNum );
                }
                
                @Override
                public void failed() { assertTrue( false ); }
                @Override
                public void completed() { _completedRecovery = true; }
            });

        assertTrue( _startedRecovery );
        assertTrue( _completedRecovery );
        assertEquals( seqNums.length, _count );
    }

    // frow file to 4 GB file .. shouldnt run out of memory 
    public void testUnMap() {
        int    numRecs       = 1000000;
        
        writeRecs( 0, numRecs, null );
    }
    
    @Override
    protected long writeLine( int seqNum ) {
        try {
            formLine( seqNum );
            return _indexPersister.persistIdxAndRec( seqNum, _genBuf.getBytes(), 0, _genBuf.length() );
        } catch( PersisterException e ) {
            assertTrue( "Failed to persist row " + seqNum, false );
        }
        
        return -1;
    }

    @Override
    protected Persister getPersister() {
        return _indexPersister;
    }

    @Override
    protected void verify( Persister p, long key, int index ) {
        assertTrue( _indexPersister.verifyIndex( key, index ) );
    }

    @Override
    protected ViewString getFileNameBase() {
        return new ViewString( "./tmp/TestIndexMemMapPersister" );
    }
}
