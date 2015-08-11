/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.RecoveryCancelRequestImpl;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;

public class TestNewOrderAckUnknownOrder extends BaseProcessorTestCase {

    public void testNOS() {
        
        ZString mktOrderId = new ViewString( "ORD000001" );
        ZString execId     = new ViewString( "EXE50001" );
        
        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.12, mktOrderId, execId );
        _proc.handle( mack );

        Order order = _proc.getOrder( mack.getClOrdId() );

        assertNull( order );
        
        checkQueueSize( 0, 1, 1 );
        
        MarketNewOrderAckImpl hubAck  = (MarketNewOrderAckImpl) getMessage( _hubQ, MarketNewOrderAckImpl.class );
        RecoveryCancelRequestImpl mfc = (RecoveryCancelRequestImpl) getMessage( _downQ, RecoveryCancelRequestImpl.class );
        
        assertSame( mack, hubAck );
        assertEquals( OrdStatus.UnseenOrder, hubAck.getOrdStatus() );
        
        checkMarketForceCancel( mack, mfc );
    }

    public void testNOSNoCancelDownstream() {
        
        ZString mktOrderId = new ViewString( "ORD000002" );
        ZString execId     = new ViewString( "EXE50002" );
        
        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000002", 100, 25.12, mktOrderId, execId );

        _procCfg.setForceCancelUnknownExexId( false );
        _proc.handle( mack );

        Order order = _proc.getOrder( mack.getClOrdId() );

        assertNull( order );
        
        checkQueueSize( 0, 0, 1 );
        
        MarketNewOrderAckImpl hubAck  = (MarketNewOrderAckImpl) getMessage( _hubQ, MarketNewOrderAckImpl.class );
        
        assertSame( mack, hubAck );
        assertEquals( OrdStatus.UnseenOrder, hubAck.getOrdStatus() );
    }

    private void checkMarketForceCancel( NewOrderAck mack, CancelRequest fcan ) {
        
        assertEquals( mack.getClOrdId(),          fcan.getOrigClOrdId() );
        assertEquals( mack.getSide(),             fcan.getSide() );
        assertEquals( mack.getOrderId(),          fcan.getOrderId() );
        
        ReusableString cl = new ReusableString( EventBuilderImpl.FORCE_CANCEL_PREFIX );
        cl.append( mack.getClOrdId() );
        
        assertEquals( cl,                        fcan.getClOrdId() );
    }
}
