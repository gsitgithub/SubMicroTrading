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
import com.rr.model.generated.internal.events.impl.ClientCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.ClientReplacedImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeCancelImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeNewImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.client.OMClientProfile;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;


public class TestTradeCancel extends BaseProcessorTestCase {
    
    public void testCancelPartialOnOpenState() {
        
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
        
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos, lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        getMessage( _upQ, ClientTradeNewImpl.class );

        _execId.setValue( "CANCEL001" );
        TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, 
                                                              _execId,  _fillExecId, OrdStatus.New );
        _proc.handle( mcan );
        checkQueueSize( 1, 0, 0 );
        ClientTradeCancelImpl ccan = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );

        
        Order order = _proc.getOrder( cnos.getClOrdId() );

        assertSame( _proc.getStateOpen(),       order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
        
        checkTradeCancel( order, cnos, ccan, _mktOrderId, _execId, lastQty, lastPx, 0, lastPx * 0, OrdStatus.New );
    }
    
    public void testCancelMissingQtyAndPx() {
        
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
         
         TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos, lastQty, lastPx, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 1, 0, 0 );
         getMessage( _upQ, ClientTradeNewImpl.class );

         _execId.setValue( "CANCEL001" );
         TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  4, 5.5, 
                                                               _execId,  _fillExecId, OrdStatus.New );
         _proc.handle( mcan );
         checkQueueSize( 1, 0, 0 );
         ClientTradeCancelImpl ccan = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );

         Order order = _proc.getOrder( cnos.getClOrdId() );

         assertSame( _proc.getStateOpen(),       order.getState() );
         assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
         
         checkTradeCancel( order, cnos, ccan, _mktOrderId, _execId, lastQty, lastPx, 0, lastPx * 0, OrdStatus.New );
    }
     
    public void testTradeCancelDupOnOpenState() {
        
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
         
         TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos, lastQty, lastPx, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 1, 0, 0 );
         getMessage( _upQ, ClientTradeNewImpl.class );

         _execId.setValue( "CANCEL001" );
         TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, 
                                                               _execId,  _fillExecId, OrdStatus.New );
         _proc.handle( mcan );
         checkQueueSize( 1, 0, 0 );
         ClientTradeCancelImpl ccan = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );

         _execId.setValue( "CANCEL001" );
         TradeCancel mcan2 = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, 
                                                                _execId,  _fillExecId, OrdStatus.New );
         _proc.handle( mcan2 );
         checkQueueSize( 0, 0, 0 ); // dup ignored
         
         Order order = _proc.getOrder( cnos.getClOrdId() );

         assertSame( _proc.getStateOpen(),       order.getState() );
         assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
         
         checkTradeCancel( order, cnos, ccan, _mktOrderId, _execId, lastQty, lastPx, 0, lastPx * 0, OrdStatus.New );
    }
     
    public void testCancelPartialFillOnCancelledSentClient() {
       
       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

        Order order = _proc.getOrder( cnos.getClOrdId() );

       // synth mkt ack
        MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
        _proc.handle( mack );
        clearQueues();

       // send partial fill
        int    lastQty = 10;
        double lastPx  = 24.0;
         
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        getMessage( _upQ, ClientTradeNewImpl.class );

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

        _execId.setValue( "CANCEL001" );
        TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, 
                                                              _execId,  _fillExecId, OrdStatus.Canceled );
        _proc.handle( mcan );
        checkQueueSize( 1, 0, 0 );
        ClientTradeCancelImpl ctcan = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );
        
        checkTradeCancel( order, cnos, ctcan, _mktOrderId, _execId, lastQty, lastPx, 0, lastPx * 0, OrdStatus.Canceled );
    }
    
    public void testFullFillBustToOpen() {
        
        // send NOS to mkt
         ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
         _proc.handle( cnos );
         clearQueues();

         Order order = _proc.getOrder( cnos.getClOrdId() );

        // synth mkt ack
         MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
         _proc.handle( mack );
         clearQueues();

        // send partial fill
         int    lastQty = 100;
         double lastPx  = 24.0;
          
         TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
         _proc.handle( mfill );
         checkQueueSize( 1, 0, 0 );
         getMessage( _upQ, ClientTradeNewImpl.class );

         assertSame( _proc.getStateCompleted(), order.getState() );
          
        // order is in terminal state 
          
         assertEquals( true, order.getClientProfile().isSendClientLateFills() );

         _execId.setValue( "CANCEL001" );
         TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, 
                                                               _execId,  _fillExecId, OrdStatus.New );
         _proc.handle( mcan );
         checkQueueSize( 1, 0, 0 );
         ClientTradeCancelImpl ctcan = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );
         
         checkTradeCancel( order, cnos, ctcan, _mktOrderId, _execId, lastQty, lastPx, 0, lastPx * 0, OrdStatus.New );
    }
     
    public void testFullFillBustToPartiallyFilled() {
        
        // send NOS to mkt
         ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
         _proc.handle( cnos );
         clearQueues();

         Order order = _proc.getOrder( cnos.getClOrdId() );

        // synth mkt ack
         MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
         _proc.handle( mack );
         clearQueues();

         int    lastQty   = 0;
         double lastPx    = 0.0;
         int    cumQty    = 0;
         double totTraded = 0.0;

         TradeNew t1, t2;
         
        // send partial fill
         
         lastPx = 24.0 ; lastQty = 5 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
         t1 = sendFILL( _proc, _msgBuf, _decoder, OrdStatus.PartiallyFilled, order, lastQty, lastPx, cumQty, totTraded, _proc.getStateOpen() );
         ReusableString fillExec1 = new ReusableString( _fillExecId );
         
         lastPx = 24.0 ; lastQty = 95 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
         t2 = sendFILL( _proc, _msgBuf, _decoder, OrdStatus.Filled, order, lastQty, lastPx, cumQty, totTraded, _proc.getStateCompleted() );
         ReusableString fillExec2 = new ReusableString( _fillExecId );

         assertSame( _proc.getStateCompleted(), order.getState() );
          
        // order is in terminal state 
          
         assertEquals( true, order.getClientProfile().isSendClientLateFills() );

        // bust t1 to partiallyFilled -> note px and qty NOT supplied only the ref
         _execId.setValue( "CANCEL001" );
         TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos, 0, 0, 
                                                               _execId,  fillExec1, OrdStatus.PartiallyFilled );
         _proc.handle( mcan );
         checkQueueSize( 1, 0, 0 );
         ClientTradeCancelImpl ctcan = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );
         
         checkTradeCancel( order, cnos, ctcan, _mktOrderId, _execId, t1.getLastQty(), t1.getLastPx(), 
                           t2.getLastQty(), t2.getLastPx() * t2.getLastQty(), OrdStatus.PartiallyFilled );
         
        // bust t2 top new

         _execId.setValue( "CANCEL002" );
         TradeCancel mcan2 = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos, 0, 0, 
                                                                _execId, fillExec2, OrdStatus.New );
         _proc.handle( mcan2 );
         checkQueueSize( 1, 0, 0 );
         ClientTradeCancelImpl ctcan2 = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );
         
         checkTradeCancel( order, cnos, ctcan2, _mktOrderId, _execId, t2.getLastQty(), t2.getLastPx(), 0, 0.0, OrdStatus.New );
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
             
            // send partial late fill
            int    lastQty = 10;
            double lastPx  = 24.0;
              
            TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
            _proc.handle( mfill );
            checkQueueSize( 1, 0, 0 );
            getMessage( _upQ, ClientTradeNewImpl.class );
    
           // SEND CANCEL --------------------------------------------------------------------------------------
            ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _cancelClOrdId, cnos.getClOrdId(), _upMsgHandler );
            _proc.handle( ccan );
            clearQueues();
              
           // SYNTH MARKET CANCELLED -----------------------------------------------------------------------------
            Cancelled mcxl = FixTestUtils.getCancelled( _msgBuf, _decoder, _cancelClOrdId, cnos.getOrigClOrdId(), 100, 25.12, _mktOrderId, _cancelExecId );
            _proc.handle( mcxl );
            clearQueues();
              
            assertSame( _proc.getStateCompleted(), order.getState() );
              
           // order is in terminal state BUST GOES TO HUB
              
            assertEquals( false, order.getClientProfile().isSendClientLateFills() );

            _execId.setValue( "CANCEL001" );
            TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos, 0, 0, 
                                                                  _execId,  _fillExecId, OrdStatus.Canceled );
            _proc.handle( mcan );
            checkQueueSize( 0, 0, 1 );
            ClientTradeCancelImpl ctcan = (ClientTradeCancelImpl) getMessage( _hubQ, ClientTradeCancelImpl.class );
            
            checkTradeCancel( order, cnos, ctcan, _mktOrderId, _execId, lastQty, lastPx, 0, 0.0, OrdStatus.Canceled );
            
            assertSame( _proc.getStateCompleted(),   order.getState() );
            assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
              
        } finally  {
            ((OMClientProfile)cnos.getClient()).setSendClientLateFills( wasClientLateFills );
        }
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

        // FILL 
        // was 100 * 25 = 2500, fillVal = 120, totVal = 2495 
         lastPx = 24.0 ; lastQty = 5 ; cumQty += lastQty ; totTraded += (lastQty * lastPx);
         sendFILL(_proc, _msgBuf, _decoder, OrdStatus.PartiallyFilled, order, lastQty, lastPx, cumQty, totTraded, _proc.getStateOpen());
         
        
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

       // SYNTH TRADE CANCEL IN PENDING REPLACE -----------------------------------------------------------------------------

        // this simulates exchange sending PartialFil BUST to New - ie replace not yet processed at exchange
        TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, nos, 0, 0, 
                                                              _execId,  _fillExecId, OrdStatus.New );
        _proc.handle( mcan );
        checkQueueSize( 1, 0, 0 );
        ClientTradeCancelImpl ctcan = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );
       
        // cant rely on ord status from exchange need manage ... here state is PendingReplace
        checkTradeCancel( order, nos, ctcan, _mktOrderId, _execId, lastQty, lastPx, 0, 0.0, OrdStatus.PendingReplace );
       
       // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EXE50002" );
        
        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _decoder, mcrr.getClOrdId(), mcrr.getOrigClOrdId(), 90, 23.0, _orderId, _execId );
        _proc.handle( mrep );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        
        checkClientReplaced( ccrr, crep, mktOrderId, _execId, 0, 0.0 );

        //  check version and order state
        assertSame( _proc.getStateOpen(),            order.getState() );
        assertSame( order.getLastAckedVerion(),      order.getPendingVersion() );
        assertEquals( _clOrdId,                      order.getClientClOrdIdChain() );
        assertEquals( mcrr.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.New,                 order.getLastAckedVerion().getOrdStatus() ); // fix 44 doesnt use ord status replaced
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero as cumqty was zero
        assertSame(   order.getClientProfile(),         nos.getClient() );
        assertEquals( 90,                               client.getTotalOrderQty() );
        assertEquals( 23.0 * 90,                        client.getTotalOrderValueUSD() );
    }

    
    public void testTradeCancelOnPendingCancel() {

       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getClientNOS( _msgBuf, _decoder, _clOrdId, 100, 25.12, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

        Order order = _proc.getOrder( cnos.getClOrdId() );

       // synth mkt ack
        MarketNewOrderAckImpl mack = FixTestUtils.getMarketACK( _msgBuf, _decoder, _mktClOrdId, 100, 25.12, _mktOrderId, _execId );
        _proc.handle( mack );
        clearQueues();

       // send partial fill
        int    lastQty = 10;
        double lastPx  = 24.0;
         
        TradeNew mfill = FixTestUtils.getMarketTradeNew( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos,  lastQty, lastPx, _fillExecId );
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        getMessage( _upQ, ClientTradeNewImpl.class );
         
       // SEND CANCEL --------------------------------------------------------------------------------------
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _cancelClOrdId, cnos.getClOrdId(), _upMsgHandler );
        _proc.handle( ccan );
        clearQueues();
          
        assertSame( _proc.getStatePendingCancel(), order.getState() );
          
       // order is PENDING CANCEL 
          
        assertEquals( true, order.getClientProfile().isSendClientLateFills() );

        assertSame(    _proc.getStatePendingCancel(), order.getState() );
        assertNotSame( order.getLastAckedVerion(),   order.getPendingVersion() );

        // this simulates exchange sending PartialFil BUST to New - ie cancel not yet processed at exchange
        TradeCancel mcan = FixTestUtils.getMarketTradeCancel( _msgBuf, _decoder, _mktOrderId, _mktClOrdId, cnos, 0, 0, 
                                                              _execId,  _fillExecId, OrdStatus.New );
        _proc.handle( mcan );
        checkQueueSize( 1, 0, 0 );
        ClientTradeCancelImpl ctcan = (ClientTradeCancelImpl) getMessage( _upQ, ClientTradeCancelImpl.class );
       
        // cant rely on ord status from exchange need manage ... here state is PendingCancel
        checkTradeCancel( order, cnos, ctcan, _mktOrderId, _execId, lastQty, lastPx, 0, 0.0, OrdStatus.PendingCancel );
    }
}
