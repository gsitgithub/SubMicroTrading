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
import com.rr.model.generated.internal.events.interfaces.BaseOrderRequest;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;

public class HFTRecoveryCancelTest extends BaseHFTRecoveryTst {

    private PKeys pkNOS1 = new PKeys();
    private PKeys pkCAN1 = new PKeys();
    
    public void testCancelledOrder() {

        setupCancelledOrder( "C0000001A", "C0000001B", 100, 25.25, 90, 26.75, pkNOS1, pkCAN1 );
        
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

        replay( clientIn,    pkCAN1._cInKey );
        replay( exchangeOut, pkCAN1._mOutKey );
        replay( exchangeIn,  pkCAN1._mInKey );
        replay( clientOut,   pkCAN1._cOutKey );

        _ctl.reconcile();

        assertEquals( 2, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNull( upChain );
        assertNull( downChain );
    }

    public void testCancelledReplayOutOfOrder() {

        setupCancelledOrder( "C0000001A", "C0000001B", 100, 25.25, 90, 26.75, pkNOS1, pkCAN1 );

        _orderMap.clear();

        // persister's are now populated, play persisted events into reconciler

        _ctl.start();
        RecoverySessionContext clientIn    = _ctl.startedInbound( _client1 );
        RecoverySessionContext clientOut   = _ctl.startedOutbound( _client1 );
        RecoverySessionContext exchangeIn  = _ctl.startedInbound( _exchange1 );
        RecoverySessionContext exchangeOut = _ctl.startedOutbound( _exchange1 );

        // only guarantee on replay is events within stream are in order
        replay( exchangeIn,  pkNOS1._mInKey );
        replay( exchangeIn,  pkCAN1._mInKey );

        replay( clientOut,   pkNOS1._cOutKey );
        replay( clientOut,   pkCAN1._cOutKey );

        replay( exchangeOut, pkNOS1._mOutKey );
        replay( exchangeOut, pkCAN1._mOutKey );

        replay( clientIn,    pkNOS1._cInKey );
        replay( clientIn,    pkCAN1._cInKey );

        _ctl.reconcile();

        assertEquals( 2, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNull( upChain );
        assertNull( downChain );
    }

    public void testCancelReceivedNotSentToMKt() {

        setupCancelledOrder( "C0000001A", "C0000001B", 100, 25.25, 90, 26.75, pkNOS1, pkCAN1 );
        
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

        replay( clientIn,    pkCAN1._cInKey );

        _ctl.reconcile();

        assertEquals( 2, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        CancelReject reject = (CancelReject) upChain;
        assertSame( _client1, reject.getMessageHandler() );

        BaseOrderRequest creq = (BaseOrderRequest) clientIn.regenerate( pkCAN1._cInKey );
        checkCancelReject( creq, reject, OrdStatus.Canceled );

        assertNotNull( reject.getNextQueueEntry() );
        
        Cancelled      clientUnsolCancelled = (Cancelled) reject.getNextQueueEntry();
        NewOrderSingle clientReq            = (NewOrderSingle) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck    clientRep            = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktRep = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktRep, mktCanReq );
    }

    public void testCancelReqNotAcked() {

        setupCancelledOrder( "C0000001A", "C0000001B", 100, 25.25, 90, 26.75, pkNOS1, pkCAN1 );
        
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

        replay( clientIn,    pkCAN1._cInKey );
        replay( exchangeOut, pkCAN1._mOutKey );

        _ctl.reconcile();

        assertEquals( 2, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        CancelReject reject = (CancelReject) upChain;
        assertSame( _client1, reject.getMessageHandler() );

        BaseOrderRequest creq = (BaseOrderRequest) clientIn.regenerate( pkCAN1._cInKey );
        checkCancelReject( creq, reject, OrdStatus.Canceled );

        assertNotNull( reject.getNextQueueEntry() );
        
        Cancelled      clientUnsolCancelled = (Cancelled) reject.getNextQueueEntry();
        NewOrderSingle clientReq            = (NewOrderSingle) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck    clientRep            = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktRep = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktRep, mktCanReq );
    }

    public void testCancelledButNotToClient() {

        setupCancelledOrder( "C0000001A", "C0000001B", 100, 25.25, 90, 26.75, pkNOS1, pkCAN1 );
        
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

        replay( clientIn,    pkCAN1._cInKey );
        replay( exchangeOut, pkCAN1._mOutKey );
        replay( exchangeIn,  pkCAN1._mInKey );
        // cancelled not propagated to client
        
        _ctl.reconcile();

        assertEquals( 2, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNull( downChain );

        CancelReject reject = (CancelReject) upChain;
        assertSame( _client1, reject.getMessageHandler() );

        BaseOrderRequest creq = (BaseOrderRequest) clientIn.regenerate( pkCAN1._cInKey );
        checkCancelReject( creq, reject, OrdStatus.Canceled );

        assertNotNull( reject.getNextQueueEntry() );
        
        Cancelled      clientUnsolCancelled = (Cancelled) reject.getNextQueueEntry();
        NewOrderSingle clientReq            = (NewOrderSingle) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck    clientRep            = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled );
    }
}
