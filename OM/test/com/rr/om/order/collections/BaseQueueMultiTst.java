/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order.collections;

import com.rr.core.collections.MessageQueue;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.om.warmup.FixTestUtils;

// Base test for queues that return the whole chain on poll

// currently NO implementations support this !!

public abstract class BaseQueueMultiTst extends BaseQueueTst {

    @Override
    public void testAdd() {
        
        MessageQueue q = getNewQueue(2048);
        
        int numOrders = 1000;
        
        ZString mktOrdId = new ViewString( "ORDID" );
        ZString execId   = new ViewString( "EXEID" );
        
        Standard44Decoder decoder = FixTestUtils.getDecoder44();
        
        for ( int i=0 ; i < numOrders ; ++i ) {
            Message msg = FixTestUtils.getClientNOS( _decoder, mkKey( true, i ), i, i ); 
            Message ack = FixTestUtils.getMarketACK( decoder, mkKey( false, i ), i, i, mktOrdId, execId ); 
            
            q.add( msg );
            q.add( ack );
        }
        
        assertEquals( numOrders*2, q.size() );

        Message nos = q.poll();

        for ( int i=0 ; i < numOrders ; ++i ) {
            String ckey = mkKey( true, i );
            String mkey = mkKey( false, i );

            Message ack = nos.getNextQueueEntry();
            
            assertSame( ClientNewOrderSingleImpl.class, nos.getClass() );
            assertSame( MarketNewOrderAckImpl.class,    ack.getClass() );

            ClientNewOrderSingleImpl cnos = (ClientNewOrderSingleImpl) nos;
            MarketNewOrderAckImpl    mack = (MarketNewOrderAckImpl)    ack;
            
            assertEquals( ckey, cnos.getClOrdId().toString() );
            assertEquals( mkey, mack.getClOrdId().toString() );
            assertEquals( i, cnos.getOrderQty() );

            nos = ack.getNextQueueEntry();
        }
        
        assertNull( q.poll() );
    }
}
