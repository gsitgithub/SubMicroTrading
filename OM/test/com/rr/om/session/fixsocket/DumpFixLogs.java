/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.fixsocket;

import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.persister.PersistentReplayListener;
import com.rr.core.persister.Persister;
import com.rr.core.persister.memmap.MemMapPersister;
import com.rr.core.utils.ThreadPriority;

public class DumpFixLogs {

            static final Logger      _log    = LoggerFactory.console( DumpFixLogs.class );
    private static final ErrorCode   FAILED  = new ErrorCode( "DFL100", "Exception in main" );
    
    
    public static void main( String[] args ) {
        
        LoggerFactory.setDebug( true );
        StatsMgr.setStats( new TestStats() );

        DumpFixLogs tcs = new DumpFixLogs();
        
        try {
            
            tcs.dump( args[0], Long.parseLong( args[1] ), Integer.parseInt( args[2] ) );
            
        } catch( Exception e ) {
            
            _log.error( FAILED, "", e );
        }
    }


    private void dump( String fileName, long persistDatPreSize, int persistDatPageSize ) throws Exception {
        MemMapPersister persister = new MemMapPersister( new ViewString( "Dump" ), 
                                                         new ViewString( fileName ), 
                                                         persistDatPreSize,
                                                         persistDatPageSize, 
                                                         ThreadPriority.Other );
        
        persister.open();
        
        recoverFromFile( persister );
    }

    private void recoverFromFile( MemMapPersister persister ) throws Exception {
        persister.replay( new PersistentReplayListener() {
                
                int            msgNum = 0;
                ReusableString msg    = new ReusableString();
                
                @Override
                public void message( Persister p, long key, byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, short flags ) {
                    message( p, key, buf, offset, len, flags );
                }
                
                @Override
                public void message( Persister p, long key, byte[] buf, int offset, int len, short flags ) {
                    msg.reset();
                    msg.append( "[" ).append( ++msgNum ).append( "] key=[ " + key + "] flags=" ).append( flags).append( " :: " ).append( buf, offset, len );
                    _log.info( msg );
                }
                
                @Override public void started()         { _log.info( "Dump started" ); }
                @Override public void failed()          { _log.info( "Dump FAILED" ); }
                @Override public void completed()       { _log.info( "Dump complete" ); }
            } );
    }
}
