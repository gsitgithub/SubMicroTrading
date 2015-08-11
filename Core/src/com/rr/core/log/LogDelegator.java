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

import com.rr.core.factories.LogEventHugeFactory;
import com.rr.core.factories.LogEventLargeFactory;
import com.rr.core.factories.LogEventSmallFactory;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;

/**
 * a thread safe log delegator
 * 
 * pay the overhead of the ThreadLocal map lookup as otherwise each LogDelegator would need its own LogEvent pool
 * which would be somewhat inefficient memory wise
 * 
 * @author Richard Rose
 */
public class LogDelegator implements Logger {

    private static final ViewString NULL_MSG = new ViewString( "" );

    private final Appender _appender;

    private static ThreadLocal<ReusableString> _stackTrace = new ThreadLocal<ReusableString>() {
        @Override
        public ReusableString initialValue() {
            ReusableString str = new ReusableString( 1024 );
            return str;
        }
    };
    
    private static ThreadLocal<LogEventSmallFactory> _localEventSmallPool = new ThreadLocal<LogEventSmallFactory>() {
        @Override
        public LogEventSmallFactory initialValue() {
            SuperPool<LogEventSmall> sp = SuperpoolManager.instance().getSuperPool( LogEventSmall.class );
            LogEventSmallFactory pool = new LogEventSmallFactory( sp );
            return pool;
        }
    };
    
    private static ThreadLocal<LogEventLargeFactory> _localEventLargePool = new ThreadLocal<LogEventLargeFactory>() {
        @Override
        public LogEventLargeFactory initialValue() {
            SuperPool<LogEventLarge> sp = SuperpoolManager.instance().getSuperPool( LogEventLarge.class );
            LogEventLargeFactory pool = new LogEventLargeFactory( sp );
            return pool;
        }
    };
    
    private static ThreadLocal<LogEventHugeFactory> _localEventHugePool = new ThreadLocal<LogEventHugeFactory>() {
        @Override
        public LogEventHugeFactory initialValue() {
            SuperPool<LogEventHuge> sp = SuperpoolManager.instance().getSuperPool( LogEventHuge.class );
            LogEventHugeFactory pool = new LogEventHugeFactory( sp );
            return pool;
        }
    };
    
    public LogDelegator( Appender appender ) {
        _appender = appender;
    }

    @Override
    public void error( ErrorCode code, String msg ) {

        LogEvent e = _localEventHugePool.get().get();

        e.setError( code, msg );

        _appender.handle( e );
        
    }

    @Override
    public void error( ErrorCode code, ZString msg ) {

        LogEvent e = _localEventHugePool.get().get();

        populateErrorEvent( e, code, msg );         

        _appender.handle( e );
        
    }

    @Override
    public void error( ErrorCode code, String msg, Throwable t ) {
        LogEvent e = _localEventHugePool.get().get();

        populateThrowableEvent( e, code, msg, t );

        _appender.handle( e );
    }

    @Override
    public void error( ErrorCode code, ZString msg, Throwable t ) {
        LogEvent e = _localEventHugePool.get().get();

        populateThrowableEvent( e, code, msg, t );

        _appender.handle( e );
    }

    @Override
    public void errorHuge( ErrorCode code, String msg ) {
        LogEvent e = _localEventHugePool.get().get();

        populateErrorEvent( e, code, msg );

        _appender.handle( e );
    }

    @Override
    public void errorLarge( ErrorCode code, String msg ) {
        LogEvent e = _localEventLargePool.get().get();

        populateErrorEvent( e, code, msg );

        _appender.handle( e );
    }

    @Override
    public void errorLarge( ErrorCode code, ZString msg ) {
        LogEvent e = _localEventLargePool.get().get();

        populateErrorEvent( e, code, msg );

        _appender.handle( e );
    }

    @Override
    public void info( ZString msg ) {
        LogEvent e = _localEventSmallPool.get().get();

        populateEvent( e, Level.info, msg );

        _appender.handle( e );
    }

    @Override
    public void infoHuge( ZString msg ) {
        LogEvent e = _localEventHugePool.get().get();

        populateEvent( e, Level.info, msg );

        _appender.handle( e );
    }

    @Override
    public void infoHuge( byte[] buf, int offset, int len ) {
        LogEvent e = _localEventHugePool.get().get();

        e.set( Level.info, buf, offset, len );

        _appender.handle( e );
    }

    @Override
    public void infoLargeAsHex( ZString msg, int hexStartOffset ) {
        LogEventLarge e = _localEventLargePool.get().get();

        e.set( Level.info, msg, hexStartOffset );

        _appender.handle( e );
    }
    
    @Override
    public void infoHuge( ByteBuffer buf ) {
        LogEvent e = _localEventHugePool.get().get();

        e.set( Level.info, buf );

        _appender.handle( e );
    }

    @Override
    public void infoLarge( ZString msg ) {
        LogEvent e = _localEventLargePool.get().get();

        populateEvent( e, Level.info, msg );

        _appender.handle( e );
    }

    @Override
    public void infoLarge( byte[] buf, int offset, int len ) {
        LogEvent e = _localEventLargePool.get().get();

        e.set( Level.info, buf, offset, len );

        _appender.handle( e );
    }

    @Override
    public void warn( String msg ) {
        LogEvent e = _localEventSmallPool.get().get();

        populateEvent( e, Level.Warn, msg );

        _appender.handle( e );
    }

    @Override
    public void warn( ZString msg ) {
        LogEvent e = _localEventSmallPool.get().get();

        populateEvent( e, Level.Warn, msg );

        _appender.handle( e );
    }

    @Override
    public void warnHuge( String msg ) {
        LogEvent e = _localEventHugePool.get().get();

        populateEvent( e, Level.Warn, msg );

        _appender.handle( e );
    }

    @Override
    public void warnLarge( String msg ) {
        LogEvent e = _localEventLargePool.get().get();

        populateEvent( e, Level.Warn, msg );

        _appender.handle( e );
    }
    
    private void populateEvent( LogEvent e, Level lvl, ZString msg ) {

        if ( msg == null) {
            e.set( lvl, NULL_MSG.getBytes(), 0, NULL_MSG.length() );
        } else {
            e.set( lvl, msg.getBytes(), msg.getOffset(), msg.length() );
        }
    }

    private void populateEvent( LogEvent e, Level lvl, String msg ) {

        if ( msg == null) {
            e.set( lvl, NULL_MSG.getBytes(), 0, NULL_MSG.length() );
        } else {
            e.set( lvl, msg );
        }
    }

    private void populateErrorEvent( LogEvent e, ErrorCode code, String msg ) {

        if ( msg == null) {
            e.setError( code, NULL_MSG.getBytes(), 0, NULL_MSG.length() );
        } else {
            e.setError( code, msg.getBytes(), 0, msg.length() );
        }
    }

    private void populateErrorEvent( LogEvent e, ErrorCode code, ZString msg ) {

        if ( msg == null) {
            e.setError( code, NULL_MSG.getBytes(), 0, NULL_MSG.length() );
        } else {
            e.setError( code, msg.getBytes(), msg.getOffset(), msg.length() );
        }
    }

    private void populateThrowableEvent( LogEvent e, ErrorCode code, ZString msg, Throwable t ) {

        ReusableString stackTrace = _stackTrace.get();

        stackTrace.reset();
        
        ExceptionTrace.format( stackTrace, t );

        if ( msg == null) {
            e.setError( code, stackTrace.getBytes(), 0, stackTrace.length() );
        } else {
            e.setError( code, msg.getBytes(), msg.getOffset(), msg.length(), stackTrace.getBytes(), 0, stackTrace.length() );
        }
    }

    private void populateThrowableEvent( LogEvent e, ErrorCode code, String msg, Throwable t ) {

        ReusableString stackTrace = _stackTrace.get();

        stackTrace.reset();
        
        ExceptionTrace.format( stackTrace, t );

        if ( msg == null) {
            e.setError( code, stackTrace.getBytes(), 0, stackTrace.length() );
        } else {
            e.setError( code, msg.getBytes(), 0, msg.length(), stackTrace.getBytes(), 0, stackTrace.length() );
        }
    }

    @Override
    public void info( String msg ) {
        LogEvent e = _localEventSmallPool.get().get();

        populateEvent( e, Level.info, msg );

        _appender.handle( e );
    }
}
