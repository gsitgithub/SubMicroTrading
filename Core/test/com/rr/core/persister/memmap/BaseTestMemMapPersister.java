/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister.memmap;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.persister.PersistentReplayListener;
import com.rr.core.persister.Persister;
import com.rr.core.persister.PersisterException;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.ThreadPriority;

public abstract class BaseTestMemMapPersister extends BaseTestCase {

    private       static int     _idx          = 0;
    
    protected MemMapPersister _persister = null;

    protected ReusableString  _base1    = new ReusableString( "ABCDEFGHIJKLMNOPQRSTUVWXYZ" );
    protected ReusableString  _base2    = new ReusableString( "abcdefghijklmnopqrstuvwzyz" );
              ReusableString  _genBuf   = new ReusableString();

    protected byte[]          _readBuf  = new byte[ Constants.MAX_BUF_LEN ];
              ReusableString  _readWrap = new ReusableString();

    protected boolean       _startedRecovery   = false;
    protected boolean       _completedRecovery = false;
    protected int           _count             = 0;
    
    private synchronized static int nextIdx() {
        return ++_idx;
    }

    @Override
    public void setUp() {
        try {
            ReusableString fileName = new ReusableString( getFileNameBase() );
            fileName.append( nextIdx() ).append( ".dat" );
            FileUtils.rmIgnoreError( fileName.toString() );
            _persister = new MemMapPersister( new ViewString("TestPersist"), fileName, 1000000, 2048, ThreadPriority.Other );
            _persister.setLogTimes( false );
            _persister.open();
            
            _readWrap.setBuffer( _readBuf, 0 );
            
        } catch( Exception e ) {
            e.printStackTrace();
            assertFalse( "FAIL: " + e.getClass().getName() + " : " + e.getMessage(), true );
        }
    }
    
    protected abstract ViewString getFileNameBase();

    @Override
    public void tearDown() {
        _persister.shutdown();
    }
    
    protected Persister getPersister() {
        return _persister;
    }
    
    protected void readAndVerifyLine( int idx, long key ) {
        
        try {
            int bytes = getPersister().read( key, _readBuf, 0 );
            
            _readWrap.setLength( bytes );
            
            formLine( idx );
            
            assertEquals( _genBuf, _readWrap );
        } catch( Exception e ) {
            assertTrue( "Failed to read row " + idx, false );
        }
    }

    protected long writeLine( int i ) {
        try {
            formLine( i );
            return getPersister().persist( _genBuf.getBytes(), 0, _genBuf.length() );
        } catch( PersisterException e ) {
            assertTrue( "Failed to persist row " + i, false );
        }
        
        return -1;
    }

    void formLine( int i ) {
        _genBuf.copy( _base1 );
        _genBuf.append( i );
        _genBuf.append( _base2 );
    }

    protected void verify( int startIdx, int total, long[] pkeys ) {
        _readWrap.setBuffer( _readBuf, 0 );

        for ( int i=startIdx ; i < total ; ++i ) {
            readAndVerifyLine( i, pkeys[i] );
        }
    }

    protected void writeRecs( int startIdx, int total, long[] pkeys ) {
        long   pkey    = -1;
        for ( int i=startIdx ; i < total ; ++i ) {
            pkey = writeLine( i );
            
            if ( pkeys != null && i < pkeys.length ) pkeys[ i ] = pkey;
        }
    }

    protected void checkRecovery( int expRecs ) throws PersisterException {
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
                    int seqNum = _count++;
                    
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
        assertEquals( expRecs, _count );
    }

    protected void verify( Persister p, long key, int index ) {
        // nothing
    }
}
