/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import com.rr.core.collections.MessageHead;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.model.Message;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.om.warmup.FixTestUtils;

/**
 * test the performance difference in volatile next pointer vs array for use in processor Q
 */

public class PerfTestVolatileNextPointer extends BaseTestCase {

    protected Standard44Decoder _decoder        = FixTestUtils.getDecoder44();
    
    @SuppressWarnings( "unused" )
    private int _dontOpt = 0;

    public void testNext() {
    
        int runs = 5;
        int iterations = 100000000;
        
        doRun( runs, iterations );
    }

    private void doRun( int runs, int iterations ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long link  = queue( iterations );
            long array = array( iterations );
            
            System.out.println( "Run " + idx + " processor Q, array=" + array + ", queue=" + link );
        }
    }

    private long queue( int iterations ) {
        ClientNewOrderSingleImpl o1 = FixTestUtils.getClientNOS( _decoder, "CL00000000001", 100, 1000.0 );

        MessageHead head = new MessageHead();
        Message     tail = head;
        Message     tmp;
        Message     tmp2 = o1;
        
        long startTime = System.currentTimeMillis();

        for( int j=0 ; j < iterations ; j++ ) {
            tail.attachQueue( o1 );
            tail = o1;
        
            for( tmp=head.getNextQueueEntry() ; tmp != null ; tmp = tmp.getNextQueueEntry() ) {

                tmp2 = tmp;
            }
            
            head.attachQueue( null );
            tail = head;
        }
        
        long endTime = System.currentTimeMillis();

        _dontOpt = ((ClientNewOrderSingleImpl)tmp2).getOrderQty();
        
        return endTime - startTime;
    }

    private long array( int iterations ) {
        ClientNewOrderSingleImpl o1 = FixTestUtils.getClientNOS( _decoder, "CL00000000001", 100, 1000.0 );

        Message[] msg = new Message[4];
        
        long startTime = System.currentTimeMillis();

        int size =0;
        int i;
        Message     tmp2 = o1;
        
        for( int j=0 ; j < iterations ; j++ ) {
            msg[0] = o1;
            ++size;
        
            for( i=0 ; i< size ; ++i ) {

                tmp2 = msg[i];
            }
            size = 0;
        }
        
        long endTime = System.currentTimeMillis();

        _dontOpt = ((ClientNewOrderSingleImpl)tmp2).getOrderQty();
        
        return endTime - startTime;
    }

}
