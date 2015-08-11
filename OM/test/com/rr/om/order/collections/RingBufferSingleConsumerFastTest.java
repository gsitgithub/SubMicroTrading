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
import com.rr.core.collections.RingBufferMsgQueueSingleConsumer;


public class RingBufferSingleConsumerFastTest extends BaseQueueTst {

    @Override
    protected MessageQueue getNewQueue( int presize ) {
        return new RingBufferMsgQueueSingleConsumer( presize );
    }
    
    @Override
    public void testMixedThreadedE() {
        // due to producer spin takes far too long
        doTestMixedThreaded( 1000, 16 );
    }

    @Override
    public void testThreaded2500_32() {
        // due to producer spin takes far too long
        doTestThreaded( 1000, 32 );
    }
}
