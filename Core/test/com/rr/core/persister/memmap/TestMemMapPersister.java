/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister.memmap;

import com.rr.core.lang.ViewString;
import com.rr.core.persister.PersisterException;

public class TestMemMapPersister extends BaseTestMemMapPersister {

    public void testSimple() {

        int    numRecs = 1000;
        long   pkey    = -1;
        long[] pkeys   = new long[ numRecs ];
        
        for ( int i=0 ; i < numRecs ; ++i ) {
            pkey = writeLine( i );
            
            pkeys[ i ] = pkey;
        }
        
        for ( int i=0 ; i < numRecs ; ++i ) {
            readAndVerifyLine( i, pkeys[i] );
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
            readAndVerifyLine( i, pkeys[i] );
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

    // frow file to 4 GB file .. shouldnt run out of memory 
    public void testUnMap() {
        int    numRecs       = 1000000;
        
        writeRecs( 0, numRecs, null );
    }

    @Override
    protected ViewString getFileNameBase() {
        return new ViewString( "./tmp/TestMemMapPersister" );
    }
}
