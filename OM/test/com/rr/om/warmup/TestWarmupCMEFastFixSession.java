/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup;

import java.util.Properties;

import com.rr.core.log.LoggerFactory;
import com.rr.core.properties.AppProps;
import com.rr.core.utils.Utils;
import com.rr.om.BaseOMTestCase;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.main.OMProps;
import com.rr.om.warmup.sim.FixSimParams;

public class TestWarmupCMEFastFixSession extends BaseOMTestCase {

    public void testWarmupCMEFastFixSession() throws Exception {

        LoggerFactory.setDebug( true );
        LoggerFactory.initLogging( "./logs/TstWarmupCMEFastFixSession.log", 1000000 );        
        AppProps.instance().init( null, OMProps.instance() );
        
        try {
            int orders      = get( "ORDERS",    3000 );
            int delayMicros = get( "DELAY",     100 );
            int batchSize   = get( "BATCH",     1);
            int maxTime     = get( "MAX_TIME",  30000 );
            
            WarmupCMEFastFixSession sess = WarmupCMEFastFixSession.create( "Test", 0, false, orders, new DummyInstrumentLocator() );
            
            FixSimParams p = sess.getParams();

            p.setBatchSize( batchSize );
            p.setDelayMicros( delayMicros );
            p.setDisableNanoStats( false );
            p.setDisableEventLogging( false );
            p.setUpHost( "224.0.0.250" );
            p.setUpPort( 30001 );
            p.setDebug( false );
            
            sess.setMaxRunTime( maxTime );
            sess.setEventLogging( true );
            sess.warmup();

            LoggerFactory.flush();
            
            Utils.delay( 1000 ); 
            
            assertEquals( 3000, sess.getSent() ); 
            assertEquals( 1514, sess.getOrdersReceived() );
            
        } catch( Exception e ) {

            fail( e.getMessage() );
        }
    }

    private int get( String key, int def ) {
        int val = def;
        
        Properties p = System.getProperties();
        
        String sVal = p.getProperty( key );
        
        if ( sVal != null ) {
            try {
                val = Integer.parseInt( sVal );
            } catch( NumberFormatException e ) {
                //
            }
        }
        
        return val;
    }
}

