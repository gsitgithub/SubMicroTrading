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
import java.util.ArrayList;
import java.util.List;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.utils.Utils;

public class TestLogging extends BaseTestCase {

    private static final long       MAX_WAIT       = 1000;

    private static final ZString    SMALL_ZSTRING  = new ViewString( "xxxabcdefg".getBytes(), 3, 5 );
    private static final String     SMALL_STRING    = "abcde";
    private static final String     WARN_ZSTRING   = "WARN abcde";
    private static final String     WARN_STRING    = "WARN abcdefghijklmnopqrstuvwxyz";
    private static final ErrorCode  ERR_CODE       = new ErrorCode( "TST101", "TEST MESSAGE" );
    private static final ZString    LARGE_ZSTRING  = new ReusableString( makeLarge() );
    private static final String     LARGE_STRING   = makeLarge();
    private static final String     HUGE_STRING    = makeHuge();
    private static final ZString    HUGE_ZSTRING   = new ReusableString( makeHuge() );
    private static final ByteBuffer BYTE_BUFFER    = ByteBuffer.wrap( HUGE_STRING.getBytes(), 0, HUGE_STRING.length() );

    static class DummyLogDelegate implements Appender {

        private long _closed  = 0;
        private long _flushed = 0;
        private long _init    = 0;
        private long _open    = 0;
        
        private List<LogEvent> _sent = new ArrayList<LogEvent>();

        @Override public void close() { _closed  = Utils.nanoTime(); }
        @Override public void flush() { _flushed = Utils.nanoTime(); }
        @Override public void init()  { _init    = Utils.nanoTime(); }
        @Override public void open()  { _open    = Utils.nanoTime(); }

        @Override
        public void handle( LogEvent e ) {
            synchronized( _sent ) {
                _sent.add( e );

                _sent.notifyAll();
            }
        }
        
        public long           getClosed()  { return _closed; }
        public long           getFlushed() { return _flushed; }
        public long           getInit()    { return _init; }
        public long           getOpen()    { return _open; }
        public List<LogEvent> getSent()    { return _sent; }
    }
    
    public void testAsync() {

        DummyLogDelegate delegate = new DummyLogDelegate();

        AsyncAppender asyncAppender = new AsyncAppender( delegate, "TestAsyncLoggr" );
        asyncAppender.init();
        
        LogDelegator delegator = new LogDelegator( asyncAppender );
        
        Throwable t;
        
        try {
            throw new Exception( "DUMMY" );
        } catch( Exception e ) {
            t = e;
        }
        
        Utils.delay( 200 );
        delegate.getSent().clear();
        
        delegator.info( SMALL_STRING );
        checkString( getEvent(delegate), SMALL_STRING, Level.info );
        delegator.info( SMALL_ZSTRING );
        checkString( getEvent(delegate), SMALL_ZSTRING.toString(), Level.info );
        delegator.info( "\nLINE2\nLINE3\n\nLINE5\n" );
        checkString( getEvent(delegate), "\nLINE2\nLINE3\n\nLINE5\n", Level.info );
        
        @SuppressWarnings( "unused" )
        LogEvent event;                     // @TODO check each event combination
       
        delegator.warn( WARN_ZSTRING );
        event = getEvent(delegate);
        delegator.warn( WARN_STRING );
        event = getEvent(delegate);
        delegator.error( ERR_CODE, WARN_ZSTRING );
        event = getEvent(delegate);
        delegator.error( ERR_CODE, WARN_ZSTRING, t );
        event = getEvent(delegate);
        delegator.error( ERR_CODE, WARN_STRING );
        event = getEvent(delegate);
        delegator.error( ERR_CODE, WARN_STRING, t );
        event = getEvent(delegate);

        // logging messages upto 512 bytes
        delegator.infoLarge( LARGE_ZSTRING );
        checkString( getEvent(delegate), LARGE_ZSTRING.toString(), Level.info );
        delegator.infoLarge( LARGE_STRING.getBytes(), 10, LARGE_STRING.length() - 10 );
        checkString( getEvent(delegate), LARGE_ZSTRING.toString().substring(10), Level.info );
        delegator.warnLarge( LARGE_STRING );
        event = getEvent(delegate);
        delegator.errorLarge( ERR_CODE, WARN_STRING );
        event = getEvent(delegate);
        delegator.errorLarge( ERR_CODE, WARN_ZSTRING );
        event = getEvent(delegate);

        // logging messages over 512 bytes
        delegator.infoHuge( HUGE_ZSTRING );
        checkString( getEvent(delegate), HUGE_ZSTRING.toString(), Level.info );
        delegator.infoHuge( HUGE_ZSTRING.getBytes() , 10, HUGE_ZSTRING.length() - 10 );
        checkString( getEvent(delegate), HUGE_ZSTRING.toString().substring(10), Level.info );
        BYTE_BUFFER.position( 0 );
        delegator.infoHuge( BYTE_BUFFER );
        BYTE_BUFFER.position( 0 );
        checkString( getEvent(delegate), new String( BYTE_BUFFER.array(), 0, BYTE_BUFFER.limit() ), Level.info );

        delegator.warnHuge( HUGE_ZSTRING.toString() );
        event = getEvent(delegate);
        delegator.errorHuge( ERR_CODE, WARN_STRING );
        event = getEvent(delegate);
    }

    private void checkString( LogEvent event, String exp, Level expLevel ) {

        assertEquals( exp, event.getMessage().toString() );
        assertEquals( expLevel, event.getLevel() );
    }

    private LogEvent getEvent( DummyLogDelegate delegate ) {
        
        long start = System.currentTimeMillis();
        long delay = 0;
        
        List<LogEvent> list = delegate.getSent();
        
        do {

            synchronized( list ) {
                int entries = list.size();
                
                if ( entries == 0) {
                    
                    Utils.delay( list, 100 );
                    
                } else {
                    LogEvent event = list.remove( entries-1 );
                    
                    list.clear();
                    
                    return event;
                }
            }
        
            delay = System.currentTimeMillis() - start;
            
        } while ( delay < MAX_WAIT );
        
        assertTrue( delay < MAX_WAIT );

        return null;
    }

    private static String makeLarge() {

        String a = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String b = a + "1" + a + "2" + a + "3" + a + "4"; 
        
        return b;
    }

    private static String makeHuge() {

        String a = "abcdefghijklmnopqrstuvwxyz1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String b = a + "1" + a + "2" + a + "3" + a + "4"; 
        String c = b + "X" + b + "Y" + b + "Z";  
        
        return c;
    }

}
