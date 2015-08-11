/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.utils.FileException;
import com.rr.core.utils.FileUtils;
import com.rr.model.generated.fix.codec.MD44Decoder;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.model.generated.internal.events.impl.SecurityStatusImpl;



public class FixInstrumentLoader {

    private static final Logger _log = LoggerFactory.create( FixInstrumentLoader.class );

    private final MD44Decoder     _softDecoder    = new MD44Decoder();
    private final InstrumentStore _store;
    
    public FixInstrumentLoader( InstrumentStore store ) {
        _store = store;
        
        _softDecoder.setValidateChecksum( false );
    }
    
    /**
     * load instruments from file ... invoke before force GC as creates temp strings
     * 
     * @param fileName
     * @param viewString 
     * @throws IOException 
     */
    public void loadFromFile( String fileName, ZString rec ) throws FileException {
        String[] files = fileName.split( "," );
        
        for( String file : files ) {
            doLoadFromFile( file.trim(), rec );
        }
    }
    
    private void doLoadFromFile( String fileName, ZString rec ) throws FileException {
        BufferedReader reader = null;
        
        int minLineLen = 20; 
        
        int count = 0;
        int lines = 0;

        _log.info( "About to start loading instruments from " + fileName + " for REC=" + rec );
        
        long start = System.currentTimeMillis();
        
        try {
            reader = new BufferedReader( new FileReader( fileName ) );
            for ( String line = reader.readLine() ; line != null ; line = reader.readLine() ) {

                ++lines;
                
                if ( line.length() > minLineLen ) {
                    byte[] msg = line.getBytes();

                    if ( decode( msg, 0, msg.length, rec ) ) {
                        ++count;
                    }
                }
            }
        } catch( IOException e ) {
            throw new FileException( "Unable to load instruments from " + fileName, e );
        } finally {
            FileUtils.close( reader );
        }
        
        long time = (System.currentTimeMillis() - start) / 1000;
        
        _log.info( "Loaded " + count + " instruments out of " + lines + ", from " + fileName + " in " + time + " secs" );
    }
    
    private boolean decode( byte[] newFixMsg, int offset, int len, ZString rec ) {

        Message m = _softDecoder.decode( newFixMsg, 0, len );

        switch( m.getReusableType().getSubId() ) {
        case EventIds.ID_SECURITYDEFINITION:
            SecurityDefinitionImpl inc = (SecurityDefinitionImpl) m;
            _store.add( inc, rec );
            return true;
        case EventIds.ID_SECURITYSTATUS:
            SecurityStatusImpl status = (SecurityStatusImpl) m;
            _store.updateStatus( status );
            break;
        default:
            break;
        }

        // dont recycle
        
        return false;
    }
}
