/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.units;

import com.rr.core.factories.LogEventHugeFactory;
import com.rr.core.factories.LogEventLargeFactory;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ViewString;
import com.rr.core.log.AsyncAppender;
import com.rr.core.log.LogEventFileAppender;
import com.rr.core.log.LogDelegator;
import com.rr.core.log.LogEventHuge;
import com.rr.core.log.LogEventLarge;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.recycler.LogEventHugeRecycler;
import com.rr.core.recycler.LogEventLargeRecycler;
import com.rr.core.utils.FileUtils;
import com.rr.core.warmup.JITWarmup;
import com.rr.model.generated.internal.events.factory.ClientAlertLimitBreachFactory;
import com.rr.model.generated.internal.events.impl.ClientAlertLimitBreachImpl;

public class WarmupLogger implements JITWarmup {

    private static final ErrorCode TEST_ERR             = new ErrorCode( "WRM999", "Dummy" );
    
    private int _warmupCount;
    
    public WarmupLogger( int warmupCount ) {
        _warmupCount = warmupCount;
    }
    
    @Override
    public String getName() {
        return "LoggerAndFileAppender";
    }

    @Override
    public void warmup() throws Exception {
        LogEventLargeFactory logEventLargePool = warmLoggerEvents();

        String tstFile = warmFileDelete();
        warmFileAppender( logEventLargePool, tstFile );
        // rmForce AFTER warmFileAppender
        FileUtils.rmForceRecurse( FileUtils.getDirName( tstFile ), false );
    }

    private LogEventLargeFactory warmLoggerEvents() {
        // LOGGER
        SuperPool<LogEventLarge>    spLargeEvent        = SuperpoolManager.instance().getSuperPool( LogEventLarge.class );
        LogEventLargeFactory        logEventLargePool   = new LogEventLargeFactory( spLargeEvent );
        LogEventLargeRecycler       recycleLargePool    = new LogEventLargeRecycler( spLargeEvent.getChainSize(), spLargeEvent );

        SuperPool<LogEventHuge>     spHugeEvent         = SuperpoolManager.instance().getSuperPool( LogEventHuge.class );
        LogEventHugeFactory         logEventHugePool    = new LogEventHugeFactory( spHugeEvent );
        LogEventHugeRecycler        recycleHugePool     = new LogEventHugeRecycler( spHugeEvent.getChainSize(), spHugeEvent );

        @SuppressWarnings( "unused" )
        ReusableType t;
        
        for ( int i=0 ; i < _warmupCount ; i++ ) {
            LogEventLarge l = logEventLargePool.get();
            t = l.getReusableType();
            recycleLargePool.recycle( l );
            
            LogEventHuge h = logEventHugePool.get();
            t = h.getReusableType();
            recycleHugePool.recycle( h );
        }
        
        return logEventLargePool;
    }

    private void warmFileAppender( LogEventLargeFactory logEventLargePool, String tstFile ) {
        //            4184   b   com.rr.core.session.AbstractSession::inboundRecycle (11 bytes)
        //             10% !b   com.rr.core.session.SessionThreadedDispatcher$Dispatcher::run @ 20 (200 bytes)
        //            4186   b   com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl::setMessageHandler (6 bytes)
        //             11% !b   com.rr.core.session.SessionThreadedDispatcher$Dispatcher::run @ 35 (200 bytes)
        //            4187   b   java.io.FileDescriptor::valid (14 bytes)
        //            4188   b   java.io.FileOutputStream::close (19 bytes)
        //            ---   n   java.io.FileOutputStream::close0
        //            4189 s!b   com.rr.core.log.FileAppender::open (125 bytes)
        //            4190  !b   com.rr.core.utils.FileUtils::formRollableFileName (98 bytes)
        //            4191   b   java.io.FileOutputStream::<init> (89 bytes)
        //            4192  !b   java.io.FileOutputStream::getChannel (41 bytes)
        //            4193 s!b   com.rr.core.log.FileAppender::close (69 bytes)
        
        LogEventFileAppender fileApp = new LogEventFileAppender( tstFile, 10000000, false );
        fileApp.init();
        AsyncAppender asyncApp = new AsyncAppender( fileApp, "WarmupAsyncLogger" );
        asyncApp.init();

        LogDelegator log = new LogDelegator( asyncApp );
        ClientAlertLimitBreachFactory alertFactory = SuperpoolManager.instance().getFactory( ClientAlertLimitBreachFactory.class, 
                                                                                             ClientAlertLimitBreachImpl.class );
         
        Exception e        = new Exception( "DummyEx" );
        String    dummyErr = "DummyErr";
         
        ViewString zstr = new ViewString( "abcdefghijklmnopqrstiuvwxyzabcdefghijklmnopqrstiuvwxyzabcdefghijklmnopqrstiuvwxyz" +
                                          "abcdefghijklmnopqrstiuvwxyzabcdefghijklmnopqrstiuvwxyzabcdefghijklmnopqrstiuvwxyz" );

        asyncApp.open();

        for( int j=0 ; j < _warmupCount ; ++j ) {
            log.infoLarge( zstr );
            log.infoLargeAsHex( zstr, 15 );
            
            ClientAlertLimitBreachImpl alert = alertFactory.get();
            ReusableString text = alert.getTextForUpdate();
            text.setValue( "Error message" );
            
            log.error( TEST_ERR, alert.getText() );
            log.error( TEST_ERR, alert.getText(), e );
            log.error( TEST_ERR, dummyErr );
            log.error( TEST_ERR, dummyErr, e );
        }

        asyncApp.forceClose();
    }

    private String warmFileDelete() {
        // FILE
        String tstFile = "./tmp/warmLog/warmTst.tmp";
        FileUtils.rmForceRecurse( FileUtils.getDirName( tstFile ), true );
        return tstFile;
    }
}
