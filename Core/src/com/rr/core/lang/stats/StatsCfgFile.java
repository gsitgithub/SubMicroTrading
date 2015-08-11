/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.rr.core.lang.RTStartupException;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;

/**
 * Repository for statistics, to be populated on startup as soon as possible and before any use of pooling or ZString etc 
 *
 * @author Richard Rose
 */
public class StatsCfgFile implements Stats {

    private static final Logger _log = LoggerFactory.console( StatsCfgFile.class );
    
    private static final String DEFAULT_STATS_CFG_FILE = "./config/stats.cfg";
    
    private final Map<SizeType,Integer> _stats = new HashMap<SizeType,Integer>( 128 );

    private String _file = DEFAULT_STATS_CFG_FILE;

    public StatsCfgFile() {
        this( DEFAULT_STATS_CFG_FILE );
    }
    
    public StatsCfgFile( String cfgFile ) {
        _file = (cfgFile != null) ? cfgFile : DEFAULT_STATS_CFG_FILE;
    }

    /**
     * @param id    
     * @return
     * @throws RTStartupException if id doesnt have entry
     */
    @Override
    public int find( SizeType id ) {

        Integer val = _stats.get( id );
        
        if ( val == null ) {
            _log.info( "StatsCfgFile : no config entry for " + id + " use default " + id.getSize() );
            
            return id.getSize();
        }
        
        return val.intValue();
    }

    @Override
    public void set( SizeType id, int val ) {
        _stats.put( id, new Integer(val) );
    }
    
    // TODO pass a Config element into initialise
    @Override
    public void initialise() {
        
        Properties p = new Properties();
        
        try {
            BufferedReader rdr = new BufferedReader( new InputStreamReader( new FileInputStream( _file ) ) );
            
            p.load( rdr );
            
            rdr.close();
            
        } catch( IOException e ) {
            throw new RTStartupException( "StatsCfgFile : Unable to load stats properties from " + _file + ", error=" + e.getMessage() );
        }
        
        for ( Map.Entry<Object,Object> entry : p.entrySet() ) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            
            SizeType s = Enum.valueOf( SizeType.class, key );
            
            if ( s == null ) {
                throw new RTStartupException( "StatsCfgFile : " + key + " is not a valid entry, entries must be members of " + SizeType.class.getSimpleName() );
            }
            
            Integer iVal;
            
            try {
                iVal = Integer.valueOf( value );
            } catch( NumberFormatException e ) {
                throw new RTStartupException( "StatsCfgFile : " + key + " entry of " + value + " is not a valid number" );
            }
            
            _stats.put(  s, iVal );
        }
    }
    
    /**
     * persist stats
     */
    @Override
    public void store() {

        Properties p = new Properties();
        for ( Map.Entry<SizeType,Integer> entry : _stats.entrySet() ) {
            SizeType key   = entry.getKey();
            Integer  value = entry.getValue();
            if ( value != null ) {
                p.put( key.toString(), value.toString() );
            }
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( _file ) ) );
            
            p.store( writer, null );
            
        } catch( IOException e ) {
            throw new RTStartupException( "StatsCfgFile : Unable to load stats properties from " + _file + ", error=" + e.getMessage() );
        } finally {
            try {
                if ( writer != null ) writer.close();
            } catch( IOException e ) {
                // TODO console log error closing file 
            }
        }
    }
    
    @Override
    public void reload() {
        initialise();        
    }

    void setFile( String fileName ) {
        _file = fileName;
    }
}
