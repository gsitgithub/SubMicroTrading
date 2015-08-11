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
import com.rr.model.generated.internal.events.impl.MarketCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.RecoveryCancelRejectImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;

public class TestCancelReplaceRequest extends BaseProcessorTestCase {

    @SuppressWarnings( "boxing" )
    public void testSimpleAmend() {
        
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
        
        assertEquals( ccrr.getClOrdId(), ccrr.getSrcLinkId() );

        _proc.handle( ccrr );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
        assertSame( _proc.getStatePendingReplace(), order.getState() );

        checkMarketReplace( ccrr, mcrr, _mktOrigClOrdId, _orderId );
        
        clearQueues();
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50002" );
        
        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr.getClOrdId(), mcrr.getOrigClOrdId(), 90, 23.0, _orderId, _execId );
        _proc.handle( mrep );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        checkClientReplaced( ccrr, crep, mktOrderId, _execId, 0, 0 );

        //  check version and order state
        assertSame( _proc.getStateOpen(),             order.getState() );
        assertSame( order.getLastAckedVerion(),      order.getPendingVersion() );
        assertEquals( _clOrdId,                       order.getClientClOrdIdChain() );
        assertEquals( mcrr.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.New,                 order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(), nos.getClient() );
        assertEquals( 90,                       client.getTotalOrderQty() );
        assertEquals( 23.0 * 90,                client.getTotalOrderValueUSD() );
    }

    @SuppressWarnings( "boxing" )
    public void testMultiAmend() {
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

       // GET THE CLIENT ACK -------------------------------------------------------------------------------
        getMessage( _upQ, ClientNewOrderAckImpl.class );
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001B" );
        ClientCancelReplaceRequestImpl ccrr = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        _proc.handle( ccrr );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50002" );
        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr.getClOrdId(), mcrr.getOrigClOrdId(), 90, 23.0, _orderId, _execId );
        _mktOrigClOrdId.copy( mrep.getClOrdId() );
        
        _proc.handle( mrep );
        
        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        /**
         * SECOND CANCEL REPLACE
         */
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001C" );
        ClientCancelReplaceRequestImpl ccrr2 = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, crep.getClOrdId(), 
                                                                                          80, 20.0,_upMsgHandler );
        _proc.handle( ccrr2 );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr2 = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
        assertSame( _proc.getStatePendingReplace(), order.getState() );

        checkMarketReplace( ccrr2, mcrr2, _mktOrigClOrdId, _orderId );
        
        clearQueues();
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50003" );
        
        Replaced mrep2 = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr2.getClOrdId(), mcrr2.getOrigClOrdId(), 80, 20.0, _orderId, _execId );
        _proc.handle( mrep2 );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep2 = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        checkClientReplaced( ccrr2, crep2, mktOrderId, _execId, 0, 0 );

        //  check version and order state
        assertSame( _proc.getStateOpen(),             order.getState() );
        assertSame( order.getLastAckedVerion(),       order.getPendingVersion() );
        assertEquals( _clOrdId,                       order.getClientClOrdIdChain() );
        assertEquals( mcrr2.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.New,                  order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(), nos.getClient() );
        assertEquals( 80,                       client.getTotalOrderQty() );
        assertEquals( 20.0 * 80,                client.getTotalOrderValueUSD() );
    }

    @SuppressWarnings( "boxing" )
    public void testMultiAmendDownWithFills() {

        int    lastQty   = 0;
        double lastPx    = 0.0;
        int    cumQty    = 0;
        double totTraded = 0.0;
        
        _clOrdId.setValue( "TST0000001" );
        
        // SEND NOS
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.0, _upMsgHandler );

        Order order = _proc.getOrder( nos.getClOrdId() );
        assertNull( order );
        
        _proc.handle( nos );
        order = _proc.getOrder( nos.getClOrdId() );
        clearQueues();
        ZString mktOrderId = new ViewString("ORD000001");
        _orderId.setValue( mktOrderId );

        // SYNTH MARKET ACK
        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.0, mktOrderId, new ViewString("EXE50001") );
        _mktOrigClOrdId.copy( mack.getClOrdId() );
        _proc.handle( mack );

        // GET THE CLIENT ACK -------------------------------------------------------------------------------
        getMessage( _upQ, ClientNewOrderAckImpl.class );

        // FIRST FILL
        lastPx = 24.0 ; lastQty = 5 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PartiallyFilled, order, lastQty, lastPx, cumQty, totTraded, _proc.getStateOpen() );
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001B" );
        ClientCancelReplaceRequestImpl ccrr = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        _proc.handle( ccrr );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50002" );
        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr.getClOrdId(), mcrr.getOrigClOrdId(), 90, 23.0, _orderId, _execId );
        _mktOrigClOrdId.copy( mrep.getClOrdId() );
        
        _proc.handle( mrep );
        
        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );

        // SECOND FILL
        lastPx = 23.0 ; lastQty = 10 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PartiallyFilled, order, lastQty, lastPx, cumQty, totTraded, _proc.getStateOpen() );
        
        /**
         * SECOND CANCEL REPLACE
         */
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001C" );
        ClientCancelReplaceRequestImpl ccrr2 = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, crep.getClOrdId(), 
                                                                                          80, 20.0,_upMsgHandler );
        _proc.handle( ccrr2 );
        checkQueueSize( 0, 1, 0 );

        // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr2 = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
        assertSame( _proc.getStatePendingReplace(), order.getState() );

        checkMarketReplace( ccrr2, mcrr2, _mktOrigClOrdId, _orderId );
        
        clearQueues();

        // THIRD FILL BEFORE REPLACED 
        lastPx = 18.0 ; lastQty = 5 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PendingReplace, order, lastQty, lastPx, cumQty, totTraded, _proc.getStatePendingReplace());
        
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50003" );
        
        Replaced mrep2 = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr2.getClOrdId(), mcrr2.getOrigClOrdId(), 80, 20.0, _orderId, _execId );
        _proc.handle( mrep2 );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep2 = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        checkClientReplaced( ccrr2, crep2, mktOrderId, _execId, cumQty, totTraded );

        //  check version and order state
        assertSame( _proc.getStateOpen(),             order.getState() );
        assertSame( order.getLastAckedVerion(),       order.getPendingVersion() );
        assertEquals( _clOrdId,                       order.getClientClOrdIdChain() );
        assertEquals( mcrr2.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.PartiallyFilled,      order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(),         nos.getClient() );
        assertEquals( 80,                               client.getTotalOrderQty() );
        assertEquals( totTraded + (80-cumQty) * 20.0,   client.getTotalOrderValueUSD() );
    }

    /**
     * tests that after a cancel reject the order can be properly amended and filled
     */
    @SuppressWarnings( "boxing" )
    public void testAmendAfterCancelReject() {
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

        // GET THE CLIENT ACK -------------------------------------------------------------------------------
        @SuppressWarnings( "unused" )
        ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );
        clearQueues();
        
        // SEND CANCEL --------------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001B" );
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), _upMsgHandler );
        _proc.handle( ccan );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        MarketCancelRequestImpl mcan = (MarketCancelRequestImpl) getMessage( _downQ, MarketCancelRequestImpl.class );
        clearQueues();
        
       // SYNTH MARKET CANCEL REJECTED -----------------------------------------------------------------------------
        MarketCancelRejectImpl mcxl = FixTestUtils.getMarketCancelReject( _msgBuf, 
                                                                          _decoder, 
                                                                          mcan.getClOrdId(), 
                                                                          mcan.getOrigClOrdId(), 
                                                                          mcan.getOrderId(), 
                                                                          CAN_REJECT, 
                                                                          CxlRejReason.Other, 
                                                                          CxlRejResponseTo.CancelRequest,
                                                                          OrdStatus.New );
        _proc.handle( mcxl );
        // MCXL now recycled, also CCAN recycled
        checkQueueSize( 1, 0, 0 );
        // GET THE CLIENT CANCEL REJECT -------------------------------------------------------------------------
        ClientCancelRejectImpl ccxl = (ClientCancelRejectImpl) getMessage( _upQ, ClientCancelRejectImpl.class );
        
        checkClientCancelReject( nos, ccxl, _clOrdId, nos.getClOrdId(), mktOrderId, CAN_REJECT,  
                                 CxlRejReason.Other, CxlRejResponseTo.CancelRequest, OrdStatus.New );

        //  check version and order state
        assertSame( _proc.getStateOpen(),         order.getState() );
        assertSame( order.getLastAckedVerion(),   order.getPendingVersion() );
        assertEquals( _clOrdId,                   order.getClientClOrdIdChain() );
        assertEquals( mcan.getClOrdId(),          order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.New,              order.getLastAckedVerion().getOrdStatus() );
        
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001C" );
        ClientCancelReplaceRequestImpl ccrr = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        _proc.handle( ccrr );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50002" );
        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr.getClOrdId(), mcrr.getOrigClOrdId(), 90, 23.0, _orderId, _execId );
        _mktOrigClOrdId.copy( mrep.getClOrdId() );
        
        _proc.handle( mrep );
        
        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        /**
         * SECOND CANCEL REPLACE
         */
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001D" );
        ClientCancelReplaceRequestImpl ccrr2 = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, crep.getClOrdId(), 
                                                                                          80, 20.0,_upMsgHandler );
        _proc.handle( ccrr2 );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr2 = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
        assertSame( _proc.getStatePendingReplace(), order.getState() );

        checkMarketReplace( ccrr2, mcrr2, _mktOrigClOrdId, _orderId );
        
        clearQueues();
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50003" );
        
        Replaced mrep2 = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr2.getClOrdId(), mcrr2.getOrigClOrdId(), 80, 20.0, _orderId, _execId );
        _proc.handle( mrep2 );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep2 = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        checkClientReplaced( ccrr2, crep2, mktOrderId, _execId, 0, 0 );

        //  check version and order state
        assertSame( _proc.getStateOpen(),             order.getState() );
        assertSame( order.getLastAckedVerion(),       order.getPendingVersion() );
        assertEquals( _clOrdId,                       order.getClientClOrdIdChain() );
        assertEquals( mcrr2.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.New,                  order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(), nos.getClient() );
        assertEquals( 80,                       client.getTotalOrderQty() );
        assertEquals( 20.0 * 80,                client.getTotalOrderValueUSD() );
    }
    
    @SuppressWarnings( "boxing" )
    public void testAmendMsgDup() {
        
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

        // send the first DUP while in pending replace
        ClientCancelReplaceRequestImpl cdup1 = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        cdup1.setPossDupFlag( true );
        _proc.handle( cdup1 );
        checkQueueSize( 0, 0, 0 );
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50002" );
        
        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr.getClOrdId(), mcrr.getOrigClOrdId(), 90, 23.0, _orderId, _execId );
        _proc.handle( mrep );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        checkClientReplaced( ccrr, crep, mktOrderId, _execId, 0, 0 );

        //  check version and order state
        assertSame( _proc.getStateOpen(),             order.getState() );
        assertSame( order.getLastAckedVerion(),      order.getPendingVersion() );
        assertEquals( _clOrdId,                       order.getClientClOrdIdChain() );
        assertEquals( mcrr.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.New,                 order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(), nos.getClient() );
        assertEquals( 90,                       client.getTotalOrderQty() );
        assertEquals( 23.0 * 90,                client.getTotalOrderValueUSD() );
        
       // send the SECOND DUP while in pending replace
        ClientCancelReplaceRequestImpl cdup2 = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        // dont set the posDup so should get dup reject
        _proc.handle( cdup2 );
        checkQueueSize( 1, 0, 0 );
        RecoveryCancelRejectImpl crej = (RecoveryCancelRejectImpl) getMessage( _upQ, RecoveryCancelRejectImpl.class );
        assertEquals( CxlRejReason.DuplicateClOrdId, crej.getCxlRejReason() );
        
        //  check version and order state unchanged
        assertSame( _proc.getStateOpen(),             order.getState() );
        assertSame( order.getLastAckedVerion(),      order.getPendingVersion() );
        assertEquals( _clOrdId,                       order.getClientClOrdIdChain() );
        assertEquals( mcrr.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.New,                 order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(), nos.getClient() );
        assertEquals( 90,                       client.getTotalOrderQty() );
        assertEquals( 23.0 * 90,                client.getTotalOrderValueUSD() );
    }

    public void testAmendRejectAsNotOpenState() {

        int    lastQty   = 0;
        double lastPx    = 0.0;
        int    cumQty    = 0;
        double totTraded = 0.0;
        
        _clOrdId.setValue( "TST0000001" );
        
        // SEND NOS
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.0, _upMsgHandler );

        _proc.handle( nos );
        clearQueues();

        Order order = _proc.getOrder( nos.getClOrdId() );

        // SEND CLIENT REPLACE WHICH WILL FAIL AS PENDINGNEW -----------------------------------------------------------------
        _clOrdId.setValue( "TST000001X" );
        ClientCancelReplaceRequestImpl cbad1 = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                           90, 23.0,_upMsgHandler );
        _proc.handle( cbad1 );
        checkQueueSize( 1, 0, 0 );
        getMessage( _upQ, RecoveryCancelRejectImpl.class );
        clearQueues();
        
        // SYNTH MARKET ACK
        ZString mktOrderId = new ViewString("ORD000001");
        _orderId.setValue( mktOrderId );
        NewOrderAck mack = FixTestUtils.getMarketACK( _decoder, "TST0000001", 100, 25.0, mktOrderId, new ViewString("EXE50001") );
        _mktOrigClOrdId.copy( mack.getClOrdId() );
        _proc.handle( mack );

        // GET THE CLIENT ACK -------------------------------------------------------------------------------
        getMessage( _upQ, ClientNewOrderAckImpl.class );

        // FIRST FILL
        lastPx = 24.0 ; lastQty = 5 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PartiallyFilled, order, lastQty, lastPx, cumQty, totTraded, _proc.getStateOpen() );
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001B" );
        ClientCancelReplaceRequestImpl ccrr = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        _proc.handle( ccrr );
        
       // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50002" );
        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr.getClOrdId(), mcrr.getOrigClOrdId(), 90, 23.0, _orderId, _execId );
        _mktOrigClOrdId.copy( mrep.getClOrdId() );
        _proc.handle( mrep );
        
        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );

        // SECOND FILL
        lastPx = 23.0 ; lastQty = 10 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PartiallyFilled, order, lastQty, lastPx, cumQty, totTraded, _proc.getStateOpen() );
        
        /**
         * SECOND CANCEL REPLACE
         */
        
        // SEND CLIENT REPLACE ------------------------------------------------------------------------------
        _clOrdId.setValue( "TST000001C" );
        ClientCancelReplaceRequestImpl ccrr2 = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, crep.getClOrdId(), 
                                                                                          80, 20.0,_upMsgHandler );
        _proc.handle( ccrr2 );
        checkQueueSize( 0, 1, 0 );

        // GET THE MARKEY REPLACE REQ -------------------------------------------------------------------------
        MarketCancelReplaceRequestImpl mcrr2 = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        clearQueues();

        // THIRD FILL BEFORE REPLACED 
        lastPx = 18.0 ; lastQty = 5 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PendingReplace, order, lastQty, lastPx, cumQty, totTraded, _proc.getStatePendingReplace());
        
        
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50003" );
        
        Replaced mrep2 = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr2.getClOrdId(), mcrr2.getOrigClOrdId(), 80, 20.0, _orderId, _execId );
        _proc.handle( mrep2 );
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        getMessage( _upQ, ClientReplacedImpl.class );

        // THIRD FILL BEFORE REPLACED 
        lastPx = 18.0 ; lastQty = 80 - cumQty ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
        sendFILL(_proc, _msgBuf, _decoder, OrdStatus.Filled, order, lastQty, lastPx, cumQty, totTraded, _proc.getStateCompleted());
        
        // SEND CLIENT REPLACE WHICH WILL FAIL AS FILLED -----------------------------------------------------------------
        _clOrdId.setValue( "TST000001Y" );
        ClientCancelReplaceRequestImpl cbad2 = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, nos.getClOrdId(), 
                                                                                          90, 23.0,_upMsgHandler );
        _proc.handle( cbad2 );
        checkQueueSize( 1, 0, 0 );
        RecoveryCancelRejectImpl crej2 = (RecoveryCancelRejectImpl) getMessage( _upQ, RecoveryCancelRejectImpl.class );
        assertEquals( CxlRejReason.Other, crej2.getCxlRejReason() );
        assertEquals( OrdStatus.Filled,   crej2.getOrdStatus() );
        clearQueues();
    }
}
