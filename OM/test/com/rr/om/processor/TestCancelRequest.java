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
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelledImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.RecoveryCancelRejectImpl;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.internal.type.ExecType;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;

public class TestCancelRequest extends BaseProcessorTestCase {

    @SuppressWarnings( "boxing" )
    public void testCancelReq() {
        
        ReusableString    clOrdId     = new ReusableString();
        ReusableString    mktClOrdId  = new ReusableString();
        ReusableString    execId      = new ReusableString();
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
        
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, clOrdId, nos.getClOrdId(), _upMsgHandler );
        
        _proc.handle( ccan );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        MarketCancelRequestImpl mcan = (MarketCancelRequestImpl) getMessage( _downQ, MarketCancelRequestImpl.class );
        
        assertSame( _proc.getStatePendingCancel(),        order.getState() );

        checkMarketCancel( nos, mcan, clOrdId, nos.getClOrdId(), mktClOrdId, orderId );
        
        clearQueues();
        
       // SYNTH MARKET CANCELLED -----------------------------------------------------------------------------
        execId.setValue( "EXE50002" );
        
        Cancelled mcxl = FixTestUtils.getCancelled( _msgBuf, _decoder, mcan.getClOrdId(), mcan.getOrigClOrdId(), 100, 25.12, orderId, execId );
        _proc.handle( mcxl );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientCancelledImpl ccxl = (ClientCancelledImpl) getMessage( _upQ, ClientCancelledImpl.class );
        
        checkClientCancelled( nos, ccxl, clOrdId, nos.getClOrdId(), mktOrderId, execId );

        //  check version and order state
        assertSame( _proc.getStateCompleted(),        order.getState() );
        assertSame( _proc.getStateCompleted(),        order.getState() );
        assertSame( order.getLastAckedVerion(),      order.getPendingVersion() );
        assertEquals( clOrdId,                       order.getClientClOrdIdChain() );
        assertEquals( mcan.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.Canceled,            order.getLastAckedVerion().getOrdStatus() );
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(), nos.getClient() );
        assertEquals( 0,                        client.getTotalOrderQty() );
        assertEquals( 0.0,                      client.getTotalOrderValueUSD() );
        
    }

    @SuppressWarnings( "boxing" )
    public void testCancelReqDupMsg() {
        
        ReusableString    clOrdId     = new ReusableString();
        ReusableString    mktClOrdId  = new ReusableString();
        ReusableString    execId      = new ReusableString();
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
        
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, clOrdId, nos.getClOrdId(), _upMsgHandler );
        
        _proc.handle( ccan );
        checkQueueSize( 0, 1, 0 );
        
       // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        MarketCancelRequestImpl mcan = (MarketCancelRequestImpl) getMessage( _downQ, MarketCancelRequestImpl.class );
        
        assertSame( _proc.getStatePendingCancel(),        order.getState() );

        checkMarketCancel( nos, mcan, clOrdId, nos.getClOrdId(), mktClOrdId, orderId );
        
        clearQueues();

        ClientCancelRequestImpl cdup = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, clOrdId, nos.getClOrdId(), _upMsgHandler );
        cdup.setPossDupFlag( true );
        _proc.handle( cdup );
        checkQueueSize( 0, 0, 0 );
        
       // SYNTH MARKET CANCELLED -----------------------------------------------------------------------------
        execId.setValue( "EXE50002" );
        
        Cancelled mcxl = FixTestUtils.getCancelled( _msgBuf, _decoder, mcan.getClOrdId(), mcan.getOrigClOrdId(), 100, 25.12, orderId, execId );
        _proc.handle( mcxl );
        
        // MCXL now recycled, also CCAN recycled
        
        checkQueueSize( 1, 0, 0 );

        // GET THE MARKEY CANCEL REQ -------------------------------------------------------------------------
        ClientCancelledImpl ccxl = (ClientCancelledImpl) getMessage( _upQ, ClientCancelledImpl.class );
        
        checkClientCancelled( nos, ccxl, clOrdId, nos.getClOrdId(), mktOrderId, execId );

        //  check version and order state
        assertSame( _proc.getStateCompleted(),        order.getState() );
        assertSame( _proc.getStateCompleted(),        order.getState() );
        assertSame( order.getLastAckedVerion(),      order.getPendingVersion() );
        assertEquals( clOrdId,                       order.getClientClOrdIdChain() );
        assertEquals( mcan.getClOrdId(),             order.getClientClOrdIdChain() ); // as exchange is not using its own ids
        assertEquals( OrdStatus.Canceled,            order.getLastAckedVerion().getOrdStatus() );
        
        ClientProfile client = order.getClientProfile();
        assertNotNull( client );

        // check client limits are zero ascumqty was zero
        assertSame(   order.getClientProfile(), nos.getClient() );
        assertEquals( 0,                        client.getTotalOrderQty() );
        assertEquals( 0.0,                      client.getTotalOrderValueUSD() );
        
        ClientCancelRequestImpl cPostDup = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, clOrdId, nos.getClOrdId(), _upMsgHandler );
        // dont set the posDup so should get dup reject
        _proc.handle( cPostDup );
        checkQueueSize( 1, 0, 0 );
        RecoveryCancelRejectImpl crej = (RecoveryCancelRejectImpl) getMessage( _upQ, RecoveryCancelRejectImpl.class );
        assertEquals( CxlRejReason.DuplicateClOrdId, crej.getCxlRejReason() );
        assertEquals( OrdStatus.Canceled,            crej.getOrdStatus() );
    }

    public void testCancelRejectAsInPendingState() {
        
        ReusableString    clOrdId     = new ReusableString();
        
       // SEND NOS
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, _upMsgHandler );
        _proc.handle( nos );
        clearQueues();

        // SEND CANCEL --------------------------------------------------------------------------------------
        clOrdId.setValue( "TST000001B" );
        
        ClientCancelRequestImpl ccan = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, clOrdId, nos.getClOrdId(), _upMsgHandler );
        _proc.handle( ccan );
        checkQueueSize( 1, 0, 0 );
        RecoveryCancelRejectImpl crej = (RecoveryCancelRejectImpl) getMessage( _upQ, RecoveryCancelRejectImpl.class );
        assertEquals( CxlRejReason.AlreadyPending, crej.getCxlRejReason() );
        assertEquals( OrdStatus.PendingNew,        crej.getOrdStatus() );
    }
    
    private void checkMarketCancel( ClientNewOrderSingleImpl cnos, 
                                    MarketCancelRequestImpl  mcan, 
                                    ReusableString           canClientClOrdId, 
                                    ViewString               origClOrdId,
                                    ViewString               mktClOrdId,
                                    ReusableString           orderId ) {
        
        assertSame(   cnos,                       mcan.getSrcEvent() );

        // check those that should be same as src NOS
        assertEquals( cnos.getOnBehalfOfId(),     mcan.getOnBehalfOfId() );
        assertEquals( cnos.getSecurityId(),       mcan.getSecurityId() );
        assertEquals( cnos.getSecurityIDSource(), mcan.getSecurityIDSource() );
        assertEquals( cnos.getSide(),             mcan.getSide() );
        assertEquals( cnos.getSymbol(),           mcan.getSymbol() );

        // check those fields come from market
        assertEquals( orderId,                    mcan.getOrderId() );
        
        // check those fields that are set by processor
        assertEquals( mktClOrdId,                 mcan.getOrigClOrdId() );
        assertEquals( canClientClOrdId,           mcan.getClOrdId() );
        
        // check those fields which should be different
        assertEquals( false,                      mcan.getPossDupFlag() );
    }
    
    @SuppressWarnings( "boxing" )
    private void checkClientCancelled( ClientNewOrderSingleImpl cnos, 
                                       ClientCancelledImpl      ccxl, 
                                       ZString                  canClientClOrdId, 
                                       ZString                  origClOrdId,
                                       ZString                  mktOrderId,  
                                       ZString                  mktExecId ) {
        
        assertSame(   cnos,                       ccxl.getSrcEvent() );

        // check those that should be same as src NOS
        assertEquals( cnos.getOnBehalfOfId(),     ccxl.getOnBehalfOfId() );
        assertEquals( cnos.getSecurityId(),       ccxl.getSecurityId() );
        assertEquals( cnos.getSecurityIDSource(), ccxl.getSecurityIDSource() );
        assertEquals( cnos.getSide(),             ccxl.getSide() );
        assertEquals( cnos.getSymbol(),           ccxl.getSymbol() );
        assertSame(   cnos.getCurrency(),         ccxl.getCurrency() );

        // check those fields come from market
        assertTrue( ccxl.getOrderId().equals( mktOrderId ) );
        assertTrue( ccxl.getExecId().equals( mktExecId ) );
        
        assertEquals( ExecType.Canceled,          ccxl.getExecType() );
        assertEquals( OrdStatus.Canceled,         ccxl.getOrdStatus() );
        
        // check those fields that are set by processor
        assertEquals( origClOrdId,                ccxl.getOrigClOrdId() );
        assertEquals( canClientClOrdId,           ccxl.getClOrdId() );
        assertEquals( cnos.getPrice(),            ccxl.getPrice() );
        assertEquals( 0,                          ccxl.getLeavesQty() );
        assertEquals( 0,                          ccxl.getCumQty() );
        assertEquals( 0.0,                        ccxl.getAvgPx() );
        assertEquals( OrderCapacity.Principal,    ccxl.getMktCapacity() );
        
        // check those fields which should be different
        assertEquals( false,                      ccxl.getPossDupFlag() );
    }
}
