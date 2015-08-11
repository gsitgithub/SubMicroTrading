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
import com.rr.core.model.ClientProfile;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.internal.events.impl.ClientCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketVagueOrderRejectImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;

public class TestVagueCancelReject extends BaseProcessorTestCase {

    @SuppressWarnings( "boxing" )
    public void testCancelRejectOrdStateNew() {
        
        ReusableString    clOrdId     = new ReusableString();
        ReusableString    mktClOrdId  = new ReusableString();
        ReusableString    orderId     = new ReusableString();
        
       // SEND NOS
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, _upMsgHandler );
        _proc.handle( nos );
        
        Order order = _proc.getOrder( nos.getClOrdId() );

        clearQueues();

        ZString mktOrderId = new ViewString("ORD000001");
        orderId.setValue( mktOrderId );
        
       // SYNTH MARKET ACK
        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.12, mktOrderId, new ViewString("EXE50001") );

        mktClOrdId.copy( mack.getClOrdId() );
        
        _proc.handle( mack );
        checkQueueSize( 1, 0, 0 );
        
        // MACK now recycled
        mack = null;

       // GET THE CLIENT ACK -------------------------------------------------------------------------------
        @SuppressWarnings( "unused" )
        ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );

        assertEquals( nos.getClOrdId(),     order.getClientClOrdIdChain() );
        assertSame(   _proc.getStateOpen(),  order.getState() );
        
        clearQueues();
        
        // SEND CANCEL --------------------------------------------------------------------------------------
        clOrdId.setValue( "TST000001B" );
        
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, clOrdId, nos.getClOrdId(), _upMsgHandler );
        
        _proc.handle( ccan );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        MarketCancelRequestImpl mcan = (MarketCancelRequestImpl) getMessage( _downQ, MarketCancelRequestImpl.class );
        
        assertSame( _proc.getStatePendingCancel(),        order.getState() );

        clearQueues();
        
       // SYNTH MARKET CANCEL REJECTED -----------------------------------------------------------------------------
        MarketVagueOrderRejectImpl mcxl = FixTestUtils.getMarketVagueReject( mcan.getClOrdId(), CAN_REJECT, false );
        _proc.handle( mcxl );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE CLIENT CANCEL REJECT -------------------------------------------------------------------------
        ClientCancelRejectImpl ccxl = (ClientCancelRejectImpl) getMessage( _upQ, ClientCancelRejectImpl.class );
        
        checkClientCancelReject( nos, ccxl, clOrdId, nos.getClOrdId(), mktOrderId, CAN_REJECT,  
                                 CxlRejReason.Other, CxlRejResponseTo.CancelRequest, OrdStatus.New );

        //  check version and order state
        assertSame( _proc.getStateOpen(),         order.getState() );
        assertSame( order.getLastAckedVerion(),   order.getPendingVersion() );
        assertEquals( clOrdId,                    order.getClientClOrdIdChain() );
        assertEquals( mcan.getClOrdId(),          order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.New,              order.getLastAckedVerion().getOrdStatus() );
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(),   nos.getClient() );
        assertEquals( 100,                        client.getTotalOrderQty() );
        assertEquals( 2512.0,                     client.getTotalOrderValueUSD() );
        
    }

    @SuppressWarnings( "boxing" )
    public void testCancelRejectOrdStateExpired() {
        
        ReusableString    msgBuf      = new ReusableString();
        Standard44Decoder decoder     = FixTestUtils.getDecoder44();
        ReusableString    clOrdId     = new ReusableString();
        ReusableString    mktClOrdId  = new ReusableString();
        ReusableString    orderId     = new ReusableString();
        
       // SEND NOS
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, _upMsgHandler );
        _proc.handle( nos );
        
        Order order = _proc.getOrder( nos.getClOrdId() );

        clearQueues();

        ZString mktOrderId = new ViewString("ORD000001");
        orderId.setValue( mktOrderId );
        
       // SYNTH MARKET ACK
        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.12, mktOrderId, new ViewString("EXE50001") );

        mktClOrdId.copy( mack.getClOrdId() );
        
        _proc.handle( mack );
        checkQueueSize( 1, 0, 0 );
        
        // MACK now recycled

       // GET THE CLIENT ACK -------------------------------------------------------------------------------
        @SuppressWarnings( "unused" )
        ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );

        assertEquals( nos.getClOrdId(),     order.getClientClOrdIdChain() );
        assertSame(   _proc.getStateOpen(),  order.getState() );
        
        clearQueues();
        
        // SEND CANCEL --------------------------------------------------------------------------------------
        clOrdId.setValue( "TST000001B" );
        
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( msgBuf, decoder, clOrdId, nos.getClOrdId(), _upMsgHandler );
        
        _proc.handle( ccan );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        MarketCancelRequestImpl mcan = (MarketCancelRequestImpl) getMessage( _downQ, MarketCancelRequestImpl.class );
        
        assertSame( _proc.getStatePendingCancel(),        order.getState() );

        clearQueues();
        
       // SYNTH MARKET CANCEL REJECTED -----------------------------------------------------------------------------
        MarketVagueOrderRejectImpl mcxl = FixTestUtils.getMarketVagueReject( mcan.getClOrdId(), CAN_REJECT, true );
        _proc.handle( mcxl );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE CLIENT CANCEL REJECT -------------------------------------------------------------------------
        ClientCancelRejectImpl ccxl = (ClientCancelRejectImpl) getMessage( _upQ, ClientCancelRejectImpl.class );
        
        checkClientCancelReject( nos, ccxl, clOrdId, nos.getClOrdId(), mktOrderId, CAN_REJECT,  
                                 CxlRejReason.Other, CxlRejResponseTo.CancelRequest, OrdStatus.Stopped );

        //  check version and order state
        assertSame( _proc.getStateCompleted(),    order.getState() );
        assertSame( order.getLastAckedVerion(),   order.getPendingVersion() );
        assertEquals( clOrdId,                    order.getClientClOrdIdChain() );
        assertEquals( mcan.getClOrdId(),          order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.Stopped,          order.getLastAckedVerion().getOrdStatus() );
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(), nos.getClient() );
        assertEquals( 0,                        client.getTotalOrderQty() );
        assertEquals( 0.0,                      client.getTotalOrderValueUSD() );
    }
}
