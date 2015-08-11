/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import java.nio.ByteBuffer;

import org.apache.log4j.BasicConfigurator;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ZString;

public class ApacheLogger implements com.rr.core.log.Logger {

    static {
        BasicConfigurator.configure();
    }

    private org.apache.log4j.Logger _log;
    
    private final byte[]     _tmpData = new byte[8192];
    private final ByteBuffer _tmpBuf  = ByteBuffer.wrap( _tmpData );

    public static ApacheLogger getApacheLogger( Class<?> aClass ) {
        return new ApacheLogger( org.apache.log4j.Logger.getLogger( aClass ) );
    }

    public ApacheLogger( org.apache.log4j.Logger logger ) {
        _log = logger;
    }
    
    @Override
    public void error( ErrorCode code, String msg ) {
        _log.error( code.getError() + " " + msg );
    }

    @Override
    public void error( ErrorCode code, ZString msg ) {
        _log.error( code.getError() + " " + msg.toString() );
    }

    @Override
    public void info( ZString msg ) {
        _log.info( msg );
    }

    @Override
    public void warn( ZString msg ) {
        _log.warn( msg );
    }

    @Override
    public void warn( String msg ) {
        _log.warn( msg );
    }

    @Override
    public void errorHuge( ErrorCode code, String msg ) {
        _log.error( code.getError() + msg );
    }

    @Override
    public void errorLarge( ErrorCode code, String msg ) {
        _log.error( code.getError() + msg );
    }

    @Override
    public void errorLarge( ErrorCode code, ZString msg ) {
        _log.error( code.getError() + msg.toString() );
    }

    @Override
    public void infoHuge( ZString msg ) {
        _log.info( msg );
    }

    @Override
    public void infoLarge( ZString msg ) {
        _log.info( msg );
    }

    @Override
    public void infoLargeAsHex( ZString msg, int hexStartOffset ) {
        LogEventLarge e = new LogEventLarge();

        e.set( Level.info, msg, hexStartOffset );

        String s = null;
        
        synchronized( ApacheLogger.class ) {
            _tmpBuf.clear();
            _tmpBuf.put( (byte)0x0A );
            e.encode( _tmpBuf );
            _tmpBuf.put( (byte)0x0A );
            s = new String( _tmpData, 0, _tmpBuf.limit() );
            _log.info( s );
        }
    }
    
    @Override
    public void warnHuge( String msg ) {
        _log.warn( msg );
    }

    @Override
    public void warnLarge( String msg ) {
        _log.warn( msg );
    }

    @Override
    public void error( ErrorCode code, ZString msg, Throwable t ) {
        _log.error( code.getError() + " " + msg.toString(), t );
    }

    @Override
    public void error( ErrorCode code, String msg, Throwable t ) {
        _log.error( code.getError() + msg, t );
    }

    @Override
    public void infoHuge( byte[] buf, int offset, int len ) {
        _log.info( new String(buf,offset,len) );
    }

    @Override
    public void infoHuge( ByteBuffer buf ) {
        _log.info( buf.asCharBuffer().toString() );
    }

    @Override
    public void infoLarge( byte[] buf, int offset, int len ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void info( String msg ) {
        _log.info( msg );
    }
}
