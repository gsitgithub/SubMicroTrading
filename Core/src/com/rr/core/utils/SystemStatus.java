/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import java.util.Map;

import com.rr.core.component.builder.SMTPropertyComponentBuilder;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.ExceptionTrace;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.properties.AppProps;
import com.rr.core.tasks.ScheduledEvent;
import com.rr.core.tasks.Scheduler;
import com.rr.core.utils.ShutdownManager.Callback;

public class SystemStatus {

    private static final Logger       _log = LoggerFactory.create( SystemStatus.class );

    private static SystemStatus         _instance = new SystemStatus();

    private SMTPropertyComponentBuilder _builder;

    public static SystemStatus instance() {
        return _instance;
    }

    public void initialised( SMTPropertyComponentBuilder smtPropertyComponentBuilder ) {

        _builder = smtPropertyComponentBuilder;

        if ( isUnitTst() )
            return; // unit test

        ShutdownManager.instance().register( new Callback() {

            @Override
            public void shuttingDown() {
                logStatus( "STOP" );
            }
        } );

        Scheduler.instance().registerForGroupEvent( ScheduledEvent.EndOfDay, new Scheduler.Callback() {

            private ZString _id = new ViewString( "SystemStatus.EndOfDay" );

            @Override
            public ZString getName() {
                return _id;
            }

            @Override
            public void event( ScheduledEvent event ) {
                logStatus( "DATE_ROLL" );
            }
        } );
    }

    private boolean isUnitTst() {
        if ( AppProps.instance().getFile() == null )
            return true;

        Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
        for ( Thread t : stackTraceMap.keySet() ) {
            if ( "main".equals( t.getName() ) ) {
                StackTraceElement[] mainStackTrace = stackTraceMap.get( t );
                for ( StackTraceElement element : mainStackTrace ) {
                    if ( element.toString().contains( "Test" ) ) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
	private String mkSubject() {
        String app = AppProps.instance().getFile();

        return app + ":SystemStatus";
    }

    private String mkHdr() {
        ReusableString h = new ReusableString( "System initialised\n" );

        h.append( AppProps.instance().toString() );

        h.append( "\n\nSTACK TRACE\n\n" );

        ExceptionTrace.dumpStackTrace( h );

        h.append( "\n\n" );

        return h.toString();
    }

    public void logStatus( String cmt ) {
        // String subject = mkSubject() + " " + cmt;
        String msg = mkHdr() + "\n\n";

        if ( _builder != null ) {
            msg += _builder.toString();
        }

        // String[] to = { p1b };
        // EmailProxy.instance().sendMail( to, subject, msg );
        
        _log.info( msg );
    }
}
