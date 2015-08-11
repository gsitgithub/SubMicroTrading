/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ZLock;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;

public final class AsyncAppender implements Appender {

    private static final long             FORCE_FLUSH_DELAY_MS = 6000;
    private static final int              PAUSE_CHECK_INTERVAL = 1000;
    private static final long             ASYNC_BACK_MAX_WAIT  = 500;
    private static final int              DELAY_ON_EMPTY_MS    = 10;

    private com.rr.core.log.Logger        _console             = LoggerFactory.console( AsyncAppender.class );

    private final    Appender             _delegate;

    private final    Thread               _logger;
    private volatile boolean              _paused              = false;
    private final    PausedLock           _pauseLock           = new PausedLock();

    private final    MessageQueue         _queue;

    private volatile boolean              _systemStopping      = false;
    private          long                 _eventCount          = 0;
    private          int                  _minFlushIntervalMS  = 0;
    private          boolean              _logDepth            = false;

    static class PausedLock implements ZLock {
        // use specific class so can spot contention in profiler
    }

    public AsyncAppender( Appender delegate, String id ) {
        this( delegate, id, false );
    }
    
    public AsyncAppender( Appender delegate, String id, boolean registerShutdownHook ) {

        _delegate = delegate;

        _queue = new ConcLinkedMsgQueueSingle();

        if ( registerShutdownHook ) {
            ShutdownManager.instance().registerLogger( new Callback() {
    
                @Override
                public void shuttingDown() {
                    ThreadUtils.pause( 1000 ); // give other threads chance to log shutdown
                    setSystemStopping( true );
                    close();
                }
            } );
        }

        _logger = new Thread( new Runnable() {
                                    @Override
                                    public void run() {
                                        dispatchLoop();
                                    }
                                }, id );

        _logger.setDaemon( true );

        _logger.start();
    }

    @Override
    public synchronized void close() {
        setPaused( true );

        if ( _logger != null ) {

            LogEventSmall event = new LogEventSmall( "AsyncAppender.close" );
            _queue.add( event );

            boolean done = false;

            do {
                synchronized( _pauseLock ) {
                    if ( _queue.size() > 0 ) {
                        try {
                            _pauseLock.wait( ASYNC_BACK_MAX_WAIT );
                            
                            if ( ! _logger.isAlive() ) {
                                done = true;
                            }
                        } catch( InterruptedException e ) { /* dont care */ }
                    } else {
                        done = true;
                    }
                }
            } while( !done );

            // at this point the background Loop is no longer processing events so its safe to instruct flush on delegate

            _delegate.flush();

        }

        LogEvent event;

        while( _queue.size() > 0 ) {
            event = (LogEvent)_queue.poll();

            if ( event != null ) {
                _console.info( event.getMessage() );
            } else {
                break; // shouldnt happen
            }
        }
    }

    @Override
    public void flush() {
        // not supported
    }

    @Override
    public void handle( LogEvent event ) {
        _queue.add( event );
    }

    @Override
    public synchronized void open() {

        setPaused( false );
    }

    @Override
    public void init() {
        // nothing
    }

    public int getMinFlushIntervalMS() {
        return _minFlushIntervalMS;
    }

    public void setMinFlushIntervalMS( int minFlushInterval ) {
        _minFlushIntervalMS = minFlushInterval;
    }

    public void forceClose() {
        _systemStopping = true;
        synchronized( _pauseLock ) {
            _pauseLock.notifyAll();
        }
    }
    
    private boolean isPaused() {
        return _paused;
    }

    private boolean isSystemStopping() {
        return _systemStopping;
    }

    private synchronized void setPaused( boolean isPaused ) {
        _paused = isPaused;
    }

    void dispatchLoop() {
        ThreadUtils.setPriority( _logger, ThreadPriority.BackgroundLogger );

        _delegate.open();

        LogEvent event = new LogEventSmall( "AsyncAppender.LoggerThread - " + Thread.currentThread().getName() );
        _delegate.handle( event );

        long lastEvent = System.currentTimeMillis();
        long lastFlush = System.currentTimeMillis();

        while( !isSystemStopping() ) {
            while( !isPaused() && !isSystemStopping() ) {

                event = (LogEvent) _queue.poll();
                
                long now = System.currentTimeMillis();
                long delay = now - lastEvent;

                if ( event != null ) {
                    _delegate.handle( event );
                    
                    ++_eventCount;
                    if ( _logDepth && (_eventCount & 0x3FF) == 0 ) {
                        ReusableString s = TLC.instance().pop();
                        s.setValue( "AsyncLogDepth : cnt=" );
                        s.append( _eventCount ).append( ", depth=" ).append( _queue.size() );
                        _console.info( s );
                        
                        TLC.instance().pushback( s );
                    }
                    
                    lastEvent = now;
                    
                    if ( _minFlushIntervalMS > 0 && (now - lastFlush) > _minFlushIntervalMS  ) {
                        _delegate.flush();
                        lastFlush = now;
                    }
                } else {
                    
                    if ( delay > FORCE_FLUSH_DELAY_MS ) {
                        _delegate.flush();
                        lastEvent = now;
                        lastFlush = now;
                    }
                    
                    Utils.delay( DELAY_ON_EMPTY_MS );
                }
            }

            event = new LogEventSmall( "AsyncAppender background thread paused, flushing events left in queue" );
            _delegate.handle( event );

            do {
                event = (LogEvent) _queue.poll();

                if ( event != null ) {
                    _delegate.handle( event );
                }

            } while( event != null );

            event = new LogEventSmall( "AsyncAppender flushed" );
            _delegate.handle( event );
            _delegate.flush();

            synchronized( _pauseLock ) {
                _pauseLock.notifyAll();
            }

            while( isPaused() && !isSystemStopping() ) {
                ThreadUtils.pause( PAUSE_CHECK_INTERVAL );
            }

            if ( !isSystemStopping() ) {
                event = new LogEventSmall( "AsyncAppender background thread unpaused" );
                _delegate.handle( event );
            }
        }
        
        _delegate.close();
        
        _console.info( Thread.currentThread().getName() + " LOG DISPATCH LOOP TERMINATING" );
    }

    synchronized void setSystemStopping( boolean isStopping ) {
        _systemStopping = isStopping;
    }
}
