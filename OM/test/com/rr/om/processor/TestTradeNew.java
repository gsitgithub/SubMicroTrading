/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.ClientProfile;
import com.rr.model.generated.internal.events.impl.ClientCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.ClientReplacedImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeNewImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.client.OMClientProfile;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;


public class TestTradeNew extends BaseProcessorTestCase {
    
    public void testPartialOnOpenState() {

        
       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

       // synth mkt ack
        MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
        
        _mktClOrdId.copy( mack.getClOrdId() );
        
        _proc.handle( mack );
        clearQueues();
       
       // MACK is now recycled !
        
       // order now open send partial fill and get client fill
        int    lastQty = 10;
        double lastPx  = 24.0;
        
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );

        Order order = _proc.getOrder( cnos.getClOrdId() );

        assertSame( _proc.getStateOpen(),       order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
        
        checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.PartiallyFilled );
    }
    
    public void testPartialDuplicate() {

        // send NOS to mkt
         ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
         _proc.handle( cnos );
         clearQueues();

        // synth mkt ack
         MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
         
         _mktClOrdId.copy( mack.getClOrdId() );
         
         _proc.handle( mack );
         clearQueues();
        
        // MACK is now recycled !
         
        // order now open send partial fill and get client fill
         int    lastQty = 10;
         double lastPx  = 24.0;
         
         TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 1, 0, 0 );
         ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );

         Order order = _proc.getOrder( cnos.getClOrdId() );

         assertSame( _proc.getStateOpen(),       order.getState() );
         assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
         
         checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.PartiallyFilled );

         mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 0, 0, 0 ); // DUPLICATE
    }
         
    public void testPartialOnPendingNewState() {
    
       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

       // order is PENDING send partial fill and get client synth ack and client fill
        int    lastQty = 10;
        double lastPx  = 24.0;
        
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 2, 0, 0 );
        ClientNewOrderAckImpl cack  = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );
        ClientTradeNewImpl    cfill = (ClientTradeNewImpl)    getMessage( _upQ, ClientTradeNewImpl.class );

        checkClientAck( cnos, cack, _mktOrderId, _ackExecId );
        
        Order order = _proc.getOrder( cnos.getClOrdId() );

        assertSame( _proc.getStateOpen(),        order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
        
        checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.PartiallyFilled );
    }
    
    public void testPartialFillOnTerminalSentClient() {
       
       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

        Order order = _proc.getOrder( cnos.getClOrdId() );

       // synth mkt ack
        MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
        _proc.handle( mack );
        clearQueues();
        
       // SEND CANCEL --------------------------------------------------------------------------------------
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _cancelClOrdId, cnos.getClOrdId(), _upMsgHandler );
        _proc.handle( ccan );
        clearQueues();
         
       // SYNTH MARKET CANCELLED -----------------------------------------------------------------------------
        Cancelled mcxl = FixTestUtils.getCancelled( _msgBuf, _decoder, _cancelClOrdId, cnos.getOrigClOrdId(), 100, 25.12, _mktOrderId, _cancelExecId );
        _proc.handle( mcxl );
        clearQueues();
         
        assertSame( _proc.getStateCompleted(), order.getState() );
         
       // order is in terminal state 
         
        assertEquals( true, order.getClientProfile().isSendClientLateFills() );
         
       // send partial late fill
        int    lastQty = 10;
        double lastPx  = 24.0;
         
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );

        assertSame( _proc.getStateCompleted(),   order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
         
        checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.Canceled );
    }
    
    public void testFullFillOnTerminalSentClient() {

       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

        Order order = _proc.getOrder( cnos.getClOrdId() );

       // synth mkt ack
        MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
        _proc.handle( mack );
        clearQueues();
        
       // SEND CANCEL --------------------------------------------------------------------------------------
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _cancelClOrdId, cnos.getClOrdId(), _upMsgHandler );
        _proc.handle( ccan );
        clearQueues();
         
       // SYNTH MARKET CANCELLED -----------------------------------------------------------------------------
        Cancelled mcxl = FixTestUtils.getCancelled( _msgBuf, _decoder, _cancelClOrdId, cnos.getOrigClOrdId(), 100, 25.12, _mktOrderId, _cancelExecId );
        _proc.handle( mcxl );
        clearQueues();
         
        assertSame( _proc.getStateCompleted(), order.getState() );
         
       // order is in terminal state 
         
        assertEquals( true, order.getClientProfile().isSendClientLateFills() );
         
       // send partial late fill
        int    lastQty = cnos.getOrderQty();
        double lastPx  = 24.0;
         
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );

        assertSame( _proc.getStateCompleted(),   order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
         
        checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.Filled );
    }
    
    public void testPartialFillOnTerminalNotSentClient() {
        
       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        boolean wasClientLateFills = ((OMClientProfile)cnos.getClient()).setSendClientLateFills( false );

        try {
            _proc.handle( cnos );
            clearQueues();
    
            Order order = _proc.getOrder( cnos.getClOrdId() );
    
           // synth mkt ack
            MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
            _proc.handle( mack );
            clearQueues();
             
           // SEND CANCEL --------------------------------------------------------------------------------------
            ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _cancelClOrdId, cnos.getClOrdId(), _upMsgHandler );
            _proc.handle( ccan );
            clearQueues();
              
           // SYNTH MARKET CANCELLED -----------------------------------------------------------------------------
            Cancelled mcxl = FixTestUtils.getCancelled( _msgBuf, _decoder, _cancelClOrdId, cnos.getOrigClOrdId(), 100, 25.12, _mktOrderId, _cancelExecId );
            _proc.handle( mcxl );
            clearQueues();
              
            assertSame( _proc.getStateCompleted(), order.getState() );
              
           // order is in terminal state 
              
            assertEquals( false, order.getClientProfile().isSendClientLateFills() );
              
           // send partial late fill
            int    lastQty = 10;
            double lastPx  = 24.0;
              
            TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
            _proc.handle( mfill );
            checkQueueSize( 0, 0, 1 );
            ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _hubQ, ClientTradeNewImpl.class );
    
            assertSame( _proc.getStateCompleted(),   order.getState() );
            assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
              
            checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.Canceled );
        } finally  {
            ((OMClientProfile)cnos.getClient()).setSendClientLateFills( wasClientLateFills );
        }
    }
    
    public void testFullFillOnOpenState() {

        // send NOS to mkt
         ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
         _proc.handle( cnos );
         clearQueues();

         Order order = _proc.getOrder( cnos.getClOrdId() );

        // synth mkt ack
         MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
         _proc.handle( mack );
         clearQueues();
         
         assertSame( _proc.getStateOpen(), order.getState() );
          
        // send partial late fill
         int    lastQty = cnos.getOrderQty();
         double lastPx  = 24.0;
          
         TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 1, 0, 0 );
         ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );

         assertSame( _proc.getStateCompleted(),   order.getState() );
         assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
          
         checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.Filled );
    }
    
    public void testFullFillOnPendingNewState() {
        
       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

       // order is PENDING send partial fill and get client synth ack and client fill
        int    lastQty = cnos.getOrderQty();
        double lastPx  = 24.0;
        
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 2, 0, 0 );
        ClientNewOrderAckImpl cack  = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );
        ClientTradeNewImpl    cfill = (ClientTradeNewImpl)    getMessage( _upQ, ClientTradeNewImpl.class );

        checkClientAck( cnos, cack, _mktOrderId, _ackExecId );
        
        Order order = _proc.getOrder( cnos.getClOrdId() );

        assertSame( _proc.getStateCompleted(),   order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
        
        checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.Filled );
    }
    
    @SuppressWarnings( "boxing" )
    public void testFillInPendingAmend() {
        
        int    lastQty   = 0;
        double lastPx    = 0.0;
        int    cumQty    = 0;
        double totTraded = 0.0;
        
       // SEND NOS
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.0, _upMsgHandler );
        _proc.handle( nos );
        
        Order order = _proc.getOrder( nos.getClOrdId() );

        clearQueues();

        ZString mktOrderId = new ViewString("ORD000001");
        _orderId.setValue( mktOrderId );
        
       // SYNTH MARKET ACK
        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.0, mktOrderId, new ViewString("EXE50001") );

        _mktOrigClOrdId.copy( mack.getClOrdId() );
        
        _proc.handle( mack );
        checkQueueSize( 1, 0, 0 );
        
        // MACK now recycled

       // GET THE CLIENT ACK -------------------------------------------------------------------------------
        @SuppressWarnings( "unused" )
        ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );

        assertEquals( nos.getClOrdId(),     order.getClientClOrdIdChain() );
        assertSame(   _proc.getStateOpen(),  order.getState() );
        
        clearQueues();
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001B" );
        
        ClientCancelReplaceRequestImpl ccrr = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        
        _proc.handle( ccrr );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
        assertSame( _proc.getStatePendingReplace(), order.getState() );

        checkMarketReplace( ccrr, mcrr, _mktOrigClOrdId, _orderId );
        
        clearQueues();

       // FILL in PendingReplace
       // was 100 * 25 = 2500, fillVal = 120, totVal = 2495 
        lastPx = 24.0 ; lastQty = 5 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PendingReplace, order, lastQty, lastPx, cumQty, totTraded, _proc.getStatePendingReplace());
        
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50002" );
        
        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr.getClOrdId(), mcrr.getOrigClOrdId(), 90, 23.0, _orderId, _execId );
        _proc.handle( mrep );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        checkClientReplaced( ccrr, crep, mktOrderId, _execId, 5, 24.0 * 5 );

        //  check version and order state
        assertSame( _proc.getStateOpen(),            order.getState() );
        assertSame( order.getLastAckedVerion(),      order.getPendingVersion() );
        assertEquals( _clOrdId,                      order.getClientClOrdIdChain() );
        assertEquals( mcrr.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.PartiallyFilled,     order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(),         nos.getClient() );
        assertEquals( 90,                               client.getTotalOrderQty() );
        assertEquals( 23.0 * (90-cumQty) + totTraded,   client.getTotalOrderValueUSD() );
    }

    
    @SuppressWarnings( "boxing" )
    public void testFullFillOnPendingReplace() {
        
        int    lastQty   = 0;
        double lastPx    = 0.0;
        int    cumQty    = 0;
        double totTraded = 0.0;
        
       // SEND NOS
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.0, _upMsgHandler );
        _proc.handle( nos );
        
        Order order = _proc.getOrder( nos.getClOrdId() );

        clearQueues();

        ZString mktOrderId = new ViewString("ORD000001");
        _orderId.setValue( mktOrderId );
        
       // SYNTH MARKET ACK
        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.0, mktOrderId, new ViewString("EXE50001") );

        _mktOrigClOrdId.copy( mack.getClOrdId() );
        
        _proc.handle( mack );
        checkQueueSize( 1, 0, 0 );
        
        // MACK now recycled

       // GET THE CLIENT ACK -------------------------------------------------------------------------------
        @SuppressWarnings( "unused" )
        ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );

        assertEquals( nos.getClOrdId(),     order.getClientClOrdIdChain() );
        assertSame(   _proc.getStateOpen(),  order.getState() );
        
        clearQueues();
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001B" );
        
        ClientCancelReplaceRequestImpl ccrr = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        
        _proc.handle( ccrr );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
        assertSame( _proc.getStatePendingReplace(), order.getState() );

        checkMarketReplace( ccrr, mcrr, _mktOrigClOrdId, _orderId );
        
        clearQueues();

       // FILLED in PendingReplace
       // was 100 * 25 = 2500, fillVal = 120, totVal = 2495 
        lastPx = 24.0 ; lastQty = 100 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PendingReplace, order, lastQty, lastPx, cumQty, totTraded, _proc.getStatePendingReplace());

        //  check version and order state
        assertSame(    _proc.getStatePendingReplace(),  order.getState() );
        assertNotSame( order.getLastAckedVerion(),      order.getPendingVersion() );
        assertEquals(  _clOrdId,                        order.getClientClOrdIdChain() );
        assertEquals(  mcrr.getClOrdId(),               order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals(  OrdStatus.Filled,                order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(),         nos.getClient() );
        assertEquals( 100,                              client.getTotalOrderQty() );
        assertEquals( totTraded,                        client.getTotalOrderValueUSD() );
        
        // SYNTH MARKET CANCEL REJECTED -----------------------------------------------------------------------------
        MarketCancelRejectImpl mcxl = FixTestUtils.getMarketCancelReject( _msgBuf, 
                                                                          _decoder, 
                                                                          mcrr.getClOrdId(), 
                                                                          mcrr.getOrigClOrdId(), 
                                                                          mcrr.getOrderId(), 
                                                                          REP_REJECT, 
                                                                          CxlRejReason.Other, 
                                                                          CxlRejResponseTo.CancelReplace,
                                                                          OrdStatus.Filled );
        _proc.handle( mcxl );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE CLIENT CANCEL REJECT -------------------------------------------------------------------------
        ClientCancelRejectImpl ccxl = (ClientCancelRejectImpl) getMessage( _upQ, ClientCancelRejectImpl.class );
        
        checkClientCancelReject( ccrr, ccxl, ccrr.getClOrdId(), ccrr.getOrigClOrdId(), mktOrderId, REP_REJECT,  
                                 CxlRejReason.Other, CxlRejResponseTo.CancelReplace, OrdStatus.Filled );

        //  check version and order state
        assertSame( _proc.getStateCompleted(),    order.getState() );
        assertSame( order.getLastAckedVerion(),   order.getPendingVersion() );
        assertEquals( ccrr.getClOrdId(),          order.getClientClOrdIdChain() );
        assertEquals( OrdStatus.Filled,           order.getLastAckedVerion().getOrdStatus() );
        
        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(),   nos.getClient() );
        assertEquals( 100,                        client.getTotalOrderQty() );
        assertEquals( totTraded,                  client.getTotalOrderValueUSD() );
    }

    
    public void testPartialFillOnPendingCancel() {

       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

        Order order = _proc.getOrder( cnos.getClOrdId() );

       // synth mkt ack
        MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
        _proc.handle( mack );
        clearQueues();
         
       // SEND CANCEL --------------------------------------------------------------------------------------
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _cancelClOrdId, cnos.getClOrdId(), _upMsgHandler );
        _proc.handle( ccan );
        clearQueues();
          
        assertSame( _proc.getStatePendingCancel(), order.getState() );
          
       // order is PENDING CANCEL 
          
        assertEquals( true, order.getClientProfile().isSendClientLateFills() );
          
       // send partial late fill
        int    lastQty = 10;
        double lastPx  = 24.0;
         
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );

        assertSame(    _proc.getStatePendingCancel(), order.getState() );
        assertNotSame( order.getLastAckedVerion(),   order.getPendingVersion() );
          
        checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.PendingCancel );
    }
    
    @SuppressWarnings( "boxing" )
    public void testFullFillOnPendingCancel() {

       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

        Order order = _proc.getOrder( cnos.getClOrdId() );

       // synth mkt ack
        MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
        _proc.handle( mack );
        clearQueues();
          
       // SEND CANCEL --------------------------------------------------------------------------------------
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _cancelClOrdId, cnos.getClOrdId(), _upMsgHandler );
        _proc.handle( ccan );
        
        MarketCancelRequestImpl mcan = (MarketCancelRequestImpl) getMessage( _downQ, MarketCancelRequestImpl.class ); 
        clearQueues();
           
        assertSame( _proc.getStatePendingCancel(), order.getState() );
           
       // order is PENDING CANCEL 
           
        assertEquals( true, order.getClientProfile().isSendClientLateFills() );
           
       // send partial late fill
        
        int    lastQty = cnos.getOrderQty();
        double lastPx  = 24.0;
          
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );

        assertSame(    _proc.getStatePendingCancel(), order.getState() );
        assertNotSame( order.getLastAckedVerion(),    order.getPendingVersion() );
           
        checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.PendingCancel );
        
        // SYNTH MARKET CANCEL REJECTED -----------------------------------------------------------------------------
        MarketCancelRejectImpl mcxl = FixTestUtils.getMarketCancelReject( _msgBuf, 
                                                                          _decoder, 
                                                                          mcan.getClOrdId(), 
                                                                          mcan.getOrigClOrdId(), 
                                                                          mcan.getOrderId(), 
                                                                          CAN_REJECT, 
                                                                          CxlRejReason.Other, 
                                                                          CxlRejResponseTo.CancelReplace, // WRONG: SHOULD BE FIXED BY BUILDER
                                                                          OrdStatus.Filled );
        _proc.handle( mcxl );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE CLIENT CANCEL REJECT -------------------------------------------------------------------------
        ClientCancelRejectImpl ccxl = (ClientCancelRejectImpl) getMessage( _upQ, ClientCancelRejectImpl.class );
        
        checkClientCancelReject( cnos, ccxl, _cancelClOrdId, cnos.getClOrdId(), mcan.getOrderId(), CAN_REJECT,  
                                 CxlRejReason.Other, CxlRejResponseTo.CancelRequest, OrdStatus.Filled );

        //  check version and order state
        assertSame( _proc.getStateCompleted(),    order.getState() );
        assertSame( order.getLastAckedVerion(),   order.getPendingVersion() );
        assertEquals( ccxl.getClOrdId(),          order.getClientClOrdIdChain() );
        assertEquals( OrdStatus.Filled,           order.getLastAckedVerion().getOrdStatus() );
        
        ClientProfile client = order.getClientProfile();
        
        // check client limits are zero ascumqty was zero
        assertSame(   client,                     cnos.getClient() );
        assertEquals( 100,                        client.getTotalOrderQty() );
        assertEquals( 100 * 24.0,                 client.getTotalOrderValueUSD() );
        
    }
    
    public void testThreeFillsToFullyFilled() {

        // send NOS to mkt
         ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
         _proc.handle( cnos );
         clearQueues();

        // synth mkt ack
         MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
         _proc.handle( mack );
         clearQueues();
        
        // MACK is now recycled !
         
         Order order = _proc.getOrder( cnos.getClOrdId() );

        // order now open send partial fill and get client fill
         int    lastQty1 = 10;
         double lastPx1  = 24.0;
         double tot1     = lastPx1 * lastQty1;
         
         TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos, lastQty1, lastPx1, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 1, 0, 0 );
         ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );
         clearQueues();

         assertSame( _proc.getStateOpen(),        order.getState() );
         assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
         checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, lastQty1, lastPx1, lastQty1, tot1, OrdStatus.PartiallyFilled );
         
         int    lastQty2 = 15;
         double lastPx2  = 23.0;
         double tot2     = lastPx2 * lastQty2;
         _fillExecId.setValue( "EXE00002" );
         
         mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty2, lastPx2, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 1, 0, 0 );
         cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );
         clearQueues();

         assertSame( _proc.getStateOpen(),        order.getState() );
         assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
         checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, 
                        lastQty2, lastPx2, 
                        lastQty1 + lastQty2, 
                        tot1 + tot2, 
                        OrdStatus.PartiallyFilled );
         
         
         int    lastQty3 = cnos.getOrderQty() - (lastQty1+lastQty2);
         double lastPx3  = 22.0;
         double tot3     = lastPx3 * lastQty3;
         _fillExecId.setValue( "EXE00003" );
         
         mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty3, lastPx3, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 1, 0, 0 );
         cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );
         clearQueues();

         assertSame( _proc.getStateCompleted(),   order.getState() );
         assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
         checkTradeNew( order, cnos, cfill, _mktOrderId, _fillExecId, 
                        lastQty3, lastPx3, 
                        lastQty1 + lastQty2 + lastQty3, 
                        tot1 + tot2 + tot3, 
                        OrdStatus.Filled );
    }
}
