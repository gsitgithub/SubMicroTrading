/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import com.rr.core.lang.TLC;


public class LoggerFactory {

    private static boolean _debug = false; // in debug mode all loggers are console
    private static boolean _init  = false;
    
    private static LogEventFileAppender     _fileAppender   = null; 
    private static AsyncAppender    _asyncAppender  = null;
    private static boolean          _running        = false; 
    
    /**
     * @param aClass
     * @return a console logger .. suitable for use before pooling / app is initialised
     */
    public static synchronized com.rr.core.log.Logger console( Class<?> aClass ) {
        
        return ApacheLogger.getApacheLogger( aClass );
    }

    public static synchronized void initLogging( String fileName, long maxFileSize ) {
        
        if ( !_init ) {
            _fileAppender  = new LogEventFileAppender( fileName, maxFileSize );
            _fileAppender.init();

            _asyncAppender = new AsyncAppender( _fileAppender, "AsyncAppenderR", true );
            _asyncAppender.init();

            _init = true;
        }
        
        if ( !_running ) {

            _asyncAppender.open();

            _running = true;
        }
    }
    
    /**
     * create a thread safe logger
     * 
     * @param aClass
     * @return
     */
    public static synchronized com.rr.core.log.Logger createSync( Class<?> aClass ) {
        
        if ( _debug ) return console( aClass );
        
        if ( !_init ) throw new RuntimeException( "LoggerFactory() must initialise before creating non console loggers" );
        
        return new com.rr.core.log.LogDelegator( _asyncAppender );
    }

    /**
     * should be invoked withing context of required control thread, eg within run method or threadedInit call
     * 
     * @param aClass
     * @return instance of logger for specified class, obtained using current threads TLC 
     */
    public static synchronized com.rr.core.log.Logger getThreadLocal( Class<?> aClass ) {
        
        if ( _debug ) return console( aClass );
        
        if ( !_init ) throw new RuntimeException( "LoggerFactory() must initialise before creating non console loggers" );
        
        Class<?>[] pClass = { Appender.class }; 
        Object[]   pArgs  = { _asyncAppender };
        
        String className = aClass.getSimpleName();
        String id = "Logger" + className.substring( className.lastIndexOf( '.' ) + 1 );
        
        Logger l = TLC.instance().getInstanceOf( id, LogDelegator.class, pClass, pArgs );
        
        return l;
    }

    public static synchronized com.rr.core.log.Logger create( Class<?> aClass ) {
        
        if ( _debug ) return console( aClass );
        
        if ( !_init ) throw new RuntimeException( "LoggerFactory() must initialise before creating non console loggers" );
        
        return new com.rr.core.log.LogDelegator( _asyncAppender );
    }

    public static void setDebug( boolean isDebug ) {
        _debug = isDebug;
    }

    public static void setMinFlushPeriodSecs( int secs ) {
        if ( _asyncAppender != null ) {
            _asyncAppender.setMinFlushIntervalMS( secs * 1000 );
        }
    }
    
    public static void flush() {
        if ( _fileAppender != null ) _fileAppender.flush();
    }
}
