/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.fixsocket.multisess;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.Utils;

public abstract class BaseTestFixMultiSocketSessions extends BaseTestCase {

    void doTestMultiSessionSocket( String id, int ordersPerClient, int numClients ) {
        
        LoggerFactory.setDebug( false );
        LoggerFactory.initLogging( "./logs/TstFixMultiSocketSession" + id + ".log", 1000000 * numClients );        
        
        try {
            MultiFixSocketSessionEmul sess = MultiFixSocketSessionEmul.create( id, ordersPerClient, numClients );
            
            sess.setMaxRunTime( 10000 + 10000 * numClients );
            sess.setEventLogging( true );
            sess.warmup();

            LoggerFactory.flush();
            
            Utils.delay( 1000 ); 
            
            sess.logDetails();
            
            assertEquals( ordersPerClient * numClients, sess.getSent() );
            assertEquals( ordersPerClient * numClients, sess.getReceived() );
            assertEquals( ordersPerClient * numClients, sess.getExchangeSent() );
            assertEquals( sess.getSent(), sess.getReceived() );
            
        } catch( Exception e ) {

            fail( e.getMessage() );
        }
    }
}

