/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import com.rr.core.model.Message;
import com.rr.core.recovery.RecoverySessionContext;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;

public class HFTRecoveryNOSTest extends BaseHFTRecoveryTst {

    private PKeys pkNOS1 = new PKeys();
    
    public void testJustClientNOS() {
        _ctl.start();
        RecoverySessionContext clientIn = _ctl.startedInbound( _client1 );

        // simulate persisted events
        NewOrderSingle nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", 100, 25.12, null );
        long nosKey = persist( _client1, nos, true );

        _ctl.processInbound( clientIn, nosKey, nos, (short) 0 );

        _ctl.completedInbound( clientIn );
        _ctl.reconcile();

        assertEquals( 1, _orders.size() );

        // NOS was recycled regenerate
        nos = (NewOrderSingle) regen( _client1, true, nosKey );
        Order o1 = _orders.get( nos.getClOrdId() );

        assertTrue( _orders.containsKey( nos.getClOrdId() ) );
        assertNull( o1 );

        // now check upstream messages
        assertNull( _ctl.getDownChain() );
        Message upChain = _ctl.getUpstreamChain();
        assertNotNull( upChain );
        assertNull( upChain.getNextQueueEntry() );
        assertTrue( upChain instanceof Rejected );

        Rejected reject = (Rejected) upChain;
        assertSame( _client1, reject.getMessageHandler() );

        checkRequestReply( nos, reject, OrdStatus.Rejected, ExecType.Rejected, 0, 0.0 );
    }

    public void testNOSInOrder() {

        setupNOSandACK( "C0000001A", 100, 25.25, pkNOS1 );
        
        _orderMap.clear();
        
        // persister's are now populated, play persisted events into reconciler

        _ctl.start();
        
        RecoverySessionContext clientIn    = _ctl.startedInbound( _client1 );
        RecoverySessionContext clientOut   = _ctl.startedOutbound( _client1 );
        RecoverySessionContext exchangeIn  = _ctl.startedInbound( _exchange1 );
        RecoverySessionContext exchangeOut = _ctl.startedOutbound( _exchange1 );

        replay( clientIn,    pkNOS1._cInKey );
        replay( exchangeOut, pkNOS1._mOutKey );
        replay( exchangeIn,  pkNOS1._mInKey );
        replay( clientOut,   pkNOS1._cOutKey );

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        Cancelled    clientUnsolCancelled = (Cancelled) upChain;
        OrderRequest clientReq            = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep            = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }

    /**
     * NOS and ACK recovery test where client NOS is last message replayed to recovery
     */
    public void testNOSOutOfOrderA() {

        setupNOSandACK( "C0000001A", 100, 25.25, pkNOS1 );
        
        _orderMap.clear();
        
        // persister's are now populated, play persisted events into reconciler

        _ctl.start();
        
        RecoverySessionContext clientIn    = _ctl.startedInbound( _client1 );
        RecoverySessionContext clientOut   = _ctl.startedOutbound( _client1 );
        RecoverySessionContext exchangeIn  = _ctl.startedInbound( _exchange1 );
        RecoverySessionContext exchangeOut = _ctl.startedOutbound( _exchange1 );

        replay( exchangeOut, pkNOS1._mOutKey );
        replay( clientOut,   pkNOS1._cOutKey );
        replay( exchangeIn,  pkNOS1._mInKey );
        replay( clientIn,    pkNOS1._cInKey );

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        Cancelled    clientUnsolCancelled = (Cancelled) upChain;
        OrderRequest clientReq            = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep            = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }

    /**
     * NOS and ACK recovery test where exchange ACK is last message replayed to recovery
     */
    public void testNOSOutOfOrderB() {

        setupNOSandACK( "C0000001A", 100, 25.25, pkNOS1 );
        
        _orderMap.clear();
        
        // persister's are now populated, play persisted events into reconciler

        _ctl.start();
        
        RecoverySessionContext clientIn    = _ctl.startedInbound( _client1 );
        RecoverySessionContext clientOut   = _ctl.startedOutbound( _client1 );
        RecoverySessionContext exchangeIn  = _ctl.startedInbound( _exchange1 );
        RecoverySessionContext exchangeOut = _ctl.startedOutbound( _exchange1 );

        replay( exchangeOut, pkNOS1._mOutKey );
        replay( clientOut,   pkNOS1._cOutKey );
        replay( clientIn,    pkNOS1._cInKey );
        replay( exchangeIn,  pkNOS1._mInKey );

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        Cancelled    clientUnsolCancelled = (Cancelled) upChain;
        OrderRequest clientReq            = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep            = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }

    /**
     * NOS from client and sent to exchange but not acked
     */
    public void testNOSNoAck() { 

        setupNOS( "C0000001A", 100, 25.25, pkNOS1 );
        
        _orderMap.clear();
        
        // persister's are now populated, play persisted events into reconciler

        _ctl.start();
        
        RecoverySessionContext clientIn    = _ctl.startedInbound( _client1 );
        RecoverySessionContext exchangeOut = _ctl.startedOutbound( _exchange1 );

        _ctl.startedOutbound( _client1 );
        _ctl.startedInbound( _exchange1 );

        replay( clientIn,    pkNOS1._cInKey );
        replay( exchangeOut, pkNOS1._mOutKey );

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        Rejected reject = (Rejected) upChain;
        assertSame( _client1, reject.getMessageHandler() );

        OrderRequest nos = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        checkRequestReply( nos, reject, OrdStatus.Rejected, ExecType.Rejected, 0, 0.0 );

        CancelRequest mktCanReq = (CancelRequest) downChain;
        OrderRequest mktNos = (OrderRequest) exchangeOut.regenerate( pkNOS1._mOutKey );
        assertEquals( mktNos.getClOrdId(), mktCanReq.getOrigClOrdId() );
        EventBuilderImpl.makeForceCancelClOrdId( _clOrdId, mktNos.getClOrdId() );
        assertEquals( _clOrdId, mktCanReq.getClOrdId() );
    }

    /**
     * test NOS with mktAck but no ACK sent to client
     */
    public void testNOSMissingClientACK() {

        setupNOSandACK( "C0000001A", 100, 25.25, pkNOS1 );
        
        _orderMap.clear();
        
        // persister's are now populated, play persisted events into reconciler

        _ctl.start();
        
        RecoverySessionContext clientIn    = _ctl.startedInbound( _client1 );
        RecoverySessionContext exchangeIn  = _ctl.startedInbound( _exchange1 );
        RecoverySessionContext exchangeOut = _ctl.startedOutbound( _exchange1 );
        _ctl.startedOutbound( _client1 );

        replay( clientIn,    pkNOS1._cInKey );
        replay( exchangeOut, pkNOS1._mOutKey );
        replay( exchangeIn,  pkNOS1._mInKey );

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        Rejected reject = (Rejected) upChain;
        assertSame( _client1, reject.getMessageHandler() );
        OrderRequest nos = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        checkRequestReply( nos, reject, OrdStatus.Rejected, ExecType.Rejected, 0, 0.0 );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }
}
