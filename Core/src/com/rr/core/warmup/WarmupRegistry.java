/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.warmup;

import java.util.LinkedHashMap;
import java.util.Map;

import com.rr.core.lang.ErrorCode;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.properties.AppProps;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.Utils;

public class WarmupRegistry {

    private static final Logger    _log     = LoggerFactory.create( WarmupRegistry.class );
    private static final ErrorCode WARM_ERR = new ErrorCode( "WRR100", "Exception during warmup" );

    private static final WarmupRegistry        _instance = new WarmupRegistry();
    private        final Map<String,JITWarmup> _warmers  = new LinkedHashMap<String,JITWarmup>();

    private int _warmupCount = 3000;

    public static final WarmupRegistry instance()    { return _instance; }
    
    public void register( JITWarmup warmer ) {
        
        String name = warmer.getName();
        
        if ( name == null ) throw new SMTRuntimeException( "JITWarmup has null name" );
        
        if ( _warmers.put( warmer.getName(), warmer ) != null ) {
            _log.info( "WarmupRegistry : overriding warmer name=" + name );
        }
    }
    
    public void warmAll() {
        
        if ( _warmupCount > 0 ) {
            _log.info( "Warmup Starting" );
            
            for( JITWarmup warmer : _warmers.values() ){
                _log.info( "Warmup (A) " + warmer.getName() );
                long start = System.currentTimeMillis();
                try {
                    warmer.warmup();
                } catch( Exception e ) {
                    _log.error( WARM_ERR, "", e );
                }
                long end = System.currentTimeMillis();
                _log.info( "Warmup (A) " + warmer.getName() + " total " + (end-start)/1000 + " secs" );
            }
            
            AppProps props = AppProps.instance();
            int delaySECS = props.getIntProperty( "warmup.delay", false, 0 );
            _log.info( "FORCED SLEEP TO DECAY RECOMPILE TIMERS for " + delaySECS + " SECS" );
            Utils.delay( delaySECS*1000 );
            
            for( JITWarmup warmer : _warmers.values() ){
                _log.info( "Warmup (B) " + warmer.getName() );
                long start = System.currentTimeMillis();
                try {
                    warmer.warmup();
                } catch( Exception e ) {
                    _log.error( WARM_ERR, "", e );
                }
                long end = System.currentTimeMillis();
                _log.info( "Warmup (B) " + warmer.getName() + " total " + (end-start)/1000 + " secs" );
            }
            
            _log.info( "Warmup Finished ... clearing warmup list" );
        } else {
            _log.info( "WarmupRegistry : warmup DISABLED" );
        }
        
        _warmers.clear();
    }

    public void setWarmupCount( int warmupCount ) {
        _warmupCount = warmupCount;
    }

    public int getWarmupCount() {
        return _warmupCount;
    }
}
