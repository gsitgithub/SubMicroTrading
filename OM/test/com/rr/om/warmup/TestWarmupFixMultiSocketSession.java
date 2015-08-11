/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.Utils;

public class TestWarmupFixMultiSocketSession extends BaseTestCase {

    public void testWarmupFixMultiSessionSocket() {
        
        LoggerFactory.setDebug( false );
        LoggerFactory.initLogging( "./logs/TstWarmupFixSocketMultiSession.log", 1000000 );        
        
        try {
            WarmupMultiFixSocketSession sess = WarmupMultiFixSocketSession.create( "testWFMSS", 0, false, 1000  );
            
            sess.setMaxRunTime( 20000 );
            sess.setEventLogging( true );
            sess.warmup();

            LoggerFactory.flush();
            
            Utils.delay( 1000 ); 
            
            assertEquals( WarmupMultiFixSocketSession.TOT_ORDERS, sess.getSent() );
            assertEquals( sess.getSent(), sess.getReceived() );
            
        } catch( Exception e ) {

            fail( e.getMessage() );
        }
    }
}

