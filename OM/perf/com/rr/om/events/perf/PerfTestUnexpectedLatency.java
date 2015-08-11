/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;

/**
 * Why is the first caall 20% slower than the second
 
        switch( msg.getEventIdWithinCategory() ) {


        switch( msg.getReusableType().getIdWithinCategory() ) {

    This perf test doesnt correlate the findings !!
 *
 */
public class PerfTestUnexpectedLatency extends BaseTestCase {
    
    private final static Logger _log = LoggerFactory.console( PerfTestUnexpectedLatency.class );
    
    @SuppressWarnings( "unused" )
    public void test() {

        Message nos = new MarketNewOrderSingleImpl();
        Message ack = new ClientNewOrderAckImpl();

        long t1 = testEventIdDirect(   1000000, nos, ack );
        long t2 = testEventIdIndirect( 1000000, nos, ack );

        long timeByRS   = testEventIdIndirect( 1000000, nos, ack );
        long timeDirect = testEventIdDirect(   1000000, nos, ack );
        
        _log.info( "typeId via interface                  =" + timeDirect );
        _log.info( "typeId via reusable type and final var=" + timeByRS );
    }

    private long testEventIdDirect( long tot, Message m, Message ack ) {
        
        long x=0;
        
        long start = Utils.nanoTime();
        
        for ( long i=0 ; i < tot ; i++ ) {
        
            x = doDirect( m, x );
            x = doDirect( ack, x );
        }

        long end = Utils.nanoTime();

        return end - start;
    }

    private long doDirect( Message m, long x ) {
        switch( m.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERSINGLE:
            x += 1;
            break;
        case EventIds.ID_NEWORDERACK:
            x += 2;
            break;
        case EventIds.ID_TRADENEW:
            x += 3;
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            x += 4;
            break;
        case EventIds.ID_CANCELREQUEST:
            x += 5;
            break;
        case EventIds.ID_CANCELREJECT:
            x += 6;
            break;
        case EventIds.ID_REJECTED:
            x += 7;
            break;
        case EventIds.ID_CANCELLED:
            x += 8;
            break;
        case EventIds.ID_REPLACED:
            x += 9;
            break;
        case EventIds.ID_DONEFORDAY:
            x += 10;
            break;
        case EventIds.ID_STOPPED:
            x += 11;
            break;
        case EventIds.ID_EXPIRED:
            x += 12;
            break;
        case EventIds.ID_SUSPENDED:
            x += 13;
            break;
        case EventIds.ID_RESTATED:
            x += 14;
            break;
        case EventIds.ID_TRADECORRECT:
            x += 15;
            break;
        case EventIds.ID_TRADECANCEL:
            x += 16;
            break;
        case EventIds.ID_ORDERSTATUS:
            x += 17;
            break;
        }
        
        for( int j=0 ;j < 1000 ; ++j ) {
            //delay
        }
        return x;
    }
    
    private long testEventIdIndirect( long tot, Message m, Message ack ) {
        
        long x=0;
        
        long start = Utils.nanoTime();
        
        for ( long i=0 ; i < tot ; i++ ) {
        
            x = doIndirect( m, x );
            x = doIndirect( ack, x );
        }

        long end = Utils.nanoTime();

        return end - start;
    }

    private long doIndirect( Message m, long x ) {
        switch( m.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERSINGLE:
            x += 1;
            break;
        case EventIds.ID_NEWORDERACK:
            x += 2;
            break;
        case EventIds.ID_TRADENEW:
            x += 3;
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            x += 4;
            break;
        case EventIds.ID_CANCELREQUEST:
            x += 5;
            break;
        case EventIds.ID_CANCELREJECT:
            x += 6;
            break;
        case EventIds.ID_REJECTED:
            x += 7;
            break;
        case EventIds.ID_CANCELLED:
            x += 8;
            break;
        case EventIds.ID_REPLACED:
            x += 9;
            break;
        case EventIds.ID_DONEFORDAY:
            x += 10;
            break;
        case EventIds.ID_STOPPED:
            x += 11;
            break;
        case EventIds.ID_EXPIRED:
            x += 12;
            break;
        case EventIds.ID_SUSPENDED:
            x += 13;
            break;
        case EventIds.ID_RESTATED:
            x += 14;
            break;
        case EventIds.ID_TRADECORRECT:
            x += 15;
            break;
        case EventIds.ID_TRADECANCEL:
            x += 16;
            break;
        case EventIds.ID_ORDERSTATUS:
            x += 17;
            break;
        }
        for( int j=0 ;j < 1000 ; ++j ) {
            //delay
        }
        return x;
    }
    
}
