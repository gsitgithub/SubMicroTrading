/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.rr.core.codec.BinaryDecoder;
import com.rr.core.codec.CodecFactory;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.fix.codec.CodecFactoryPopulator;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.om.exchange.CodecLoader;

public class DecodeBinaryMessageFile {

    private static Logger _log;
    
    public static void main( String args[] ) throws IOException {
        
        LoggerFactory.setDebug( true );
        StatsMgr.setStats( new TestStats() );
        
        _log = LoggerFactory.create( DecodeBinaryMessageFile.class );
        
        if ( args.length != 2 ) {
            System.err.println( "Usage DecodeBinaryMessageFile <codecId> <fileName>" );
            System.exit( -1 );
        }
        
        String idStr = args[0];
        String fileName = args[1];
        
        CodecId codecId;
        
        try {
            codecId = Enum.valueOf( CodecId.class, idStr );
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Bad CodecId " + idStr + " not in " + CodecId.class.getCanonicalName() );
        }
        
        CodecFactory codecFactory = new CodecFactory();
        CodecFactoryPopulator pop = new CodecLoader();
        pop.register( codecFactory );

        BinaryDecoder decoder = (BinaryDecoder) codecFactory.getDecoder( codecId );

        decoder.setDebug( true );
        
        File file = new File( fileName );

        byte[] inBuf = new byte[8192];
        
        FileInputStream stream = new FileInputStream( file );
        
        try {
            int bytesRead;
            int offset = 0;
            while( (bytesRead = stream.read( inBuf, offset, inBuf.length )) != -1 ) {
                int leftOverBytes = procBytes( decoder, bytesRead, inBuf );

                if ( leftOverBytes < 0 ) {
                    break;
                }

                if ( leftOverBytes > 0 ) {
                    int copyFrom = bytesRead - leftOverBytes;
                    System.arraycopy( inBuf, 0, inBuf, copyFrom, leftOverBytes ); // dont need zero out bytes out on the right
                }

                offset = leftOverBytes;
            }
        } finally {
            FileUtils.close( stream );
        }
        
        System.out.println( "FINISHED" );
    }

    private static int procBytes( BinaryDecoder decoder, int bytesRead, byte[] inBuf ) {
        int offset = 0;
        
        int remainingBytes = bytesRead - offset;
        
        ReusableString dump = new ReusableString();
        
        while( remainingBytes > 0 ) {
            
            int expLen = decoder.parseHeader( inBuf, offset, bytesRead );
            
            if ( expLen <= 0 ) {
                return -1;
            }
            
            if ( expLen > remainingBytes ) {
                return remainingBytes;
            }
            
            Message msg = decoder.postHeaderDecode();

            msg.dump( dump );
            
            _log.info( dump );
            
            offset += expLen;
            remainingBytes = bytesRead - offset;
        }
        
        return 0;
    }
}
