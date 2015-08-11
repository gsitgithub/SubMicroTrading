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
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;

public class HFTRecoveryTradeCancelTest extends BaseHFTRecoveryTst {

    private PKeys pkNOS1   = new PKeys();
    private PKeys pkTrade1 = new PKeys();
    private PKeys pkTrade2 = new PKeys();
    private PKeys pkTrade3 = new PKeys();
    private PKeys pkTrade4 = new PKeys();

    private final int    qty1 = 100;
    private final double px1  = 25.25;
    
    private final int    fillQty1    = 10,    fillQty2  = 15,     fillQty3 = (qty1 - fillQty1 - fillQty2);
    private final double fillPx1     = 25.15, fillPx2 = 25.175,   fillPx3 = 24.9;

    private final int    cumQty1 = fillQty1;
    private final double avePx1  = fillPx1;
    
    private final int    cumQty2 = fillQty1 + fillQty2;
    private final double avePx2  = (fillQty1 * fillPx1 + fillQty2 * fillPx2) / (fillQty1 + fillQty2);
    
    private final int    cumQty3 = fillQty1 + fillQty2 + fillQty3;
    private final double avePx3  = (fillQty1 * fillPx1 + fillQty2 * fillPx2 + fillQty3 * fillPx3) / (fillQty1 + fillQty2 + fillQty3);
    
    
    public void testCancelFullFillToPartial() {

        setupNOSandACK( "C0000001A", qty1, px1, pkNOS1 );
        setupTrade( fillQty1, fillPx1, cumQty1, avePx1, "EX_TR_1", pkNOS1, pkTrade1, OrdStatus.PartiallyFilled );
        setupTrade( fillQty2, fillPx2, cumQty2, avePx2, "EX_TR_2", pkNOS1, pkTrade2, OrdStatus.PartiallyFilled );
        setupTrade( fillQty3, fillPx3, cumQty3, avePx3, "EX_TR_3", pkNOS1, pkTrade3, OrdStatus.Filled );
        setupTradeCancel( "EX_TR_3", fillQty3, fillPx3, cumQty2, avePx2, "EX_TR_4", pkNOS1, pkTrade4, OrdStatus.PartiallyFilled );

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

        replay( exchangeIn,  pkTrade1._mInKey );
        replay( clientOut,   pkTrade1._cOutKey );

        replay( exchangeIn,  pkTrade2._mInKey );
        replay( clientOut,   pkTrade2._cOutKey );

        replay( exchangeIn,  pkTrade3._mInKey );
        replay( clientOut,   pkTrade3._cOutKey );

        replay( exchangeIn,  pkTrade4._mInKey );
        replay( clientOut,   pkTrade4._cOutKey );

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        Cancelled    clientUnsolCancelled = (Cancelled) upChain;
        OrderRequest clientReq            = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep            = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled, cumQty2, avePx2 );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }
    
    public void testCancelFullFillToNew() {

        setupNOSandACK( "C0000001A", qty1, px1, pkNOS1 );
        setupTrade( fillQty1, fillPx1, cumQty1, avePx1, "EX_TR_1", pkNOS1, pkTrade1, OrdStatus.PartiallyFilled );
        setupTradeCancel( "EX_TR_1", fillQty1, fillPx1, 0, 0, "EX_TR_2", pkNOS1, pkTrade2, OrdStatus.New );

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

        replay( exchangeIn,  pkTrade1._mInKey );
        replay( clientOut,   pkTrade1._cOutKey );

        replay( exchangeIn,  pkTrade2._mInKey );
        replay( clientOut,   pkTrade2._cOutKey );

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        Cancelled    clientUnsolCancelled = (Cancelled) upChain;
        OrderRequest clientReq            = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep            = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled, 0, 0 );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }
    
    public void testCancelFullFillToPartialMissingClient() {

        setupNOSandACK( "C0000001A", qty1, px1, pkNOS1 );
        setupTrade( fillQty1, fillPx1, cumQty1, avePx1, "EX_TR_1", pkNOS1, pkTrade1, OrdStatus.PartiallyFilled );
        setupTrade( fillQty2, fillPx2, cumQty2, avePx2, "EX_TR_2", pkNOS1, pkTrade2, OrdStatus.PartiallyFilled );
        setupTrade( fillQty3, fillPx3, cumQty3, avePx3, "EX_TR_3", pkNOS1, pkTrade3, OrdStatus.Filled );
        setupTradeCancel( "EX_TR_3", fillQty3, fillPx3, cumQty2, avePx2, "EX_TR_4", pkNOS1, pkTrade4, OrdStatus.PartiallyFilled );

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

        replay( exchangeIn,  pkTrade1._mInKey );
        replay( clientOut,   pkTrade1._cOutKey );

        replay( exchangeIn,  pkTrade2._mInKey );
        replay( clientOut,   pkTrade2._cOutKey );

        replay( exchangeIn,  pkTrade3._mInKey );
        replay( clientOut,   pkTrade3._cOutKey );

        replay( exchangeIn,  pkTrade4._mInKey );
        // missing client

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        OrderRequest clientReq = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );

        _refExecId.copy( "EX_TR_3" );
        TradeCancel clientTradeCancel   = (TradeCancel) upChain;
        checkTrade( clientTradeCancel, clientReq, clientRep, OrdStatus.PartiallyFilled, ExecType.TradeCancel, fillQty3, cumQty2, fillPx3, avePx2 );
        assertEquals( _refExecId, clientTradeCancel.getExecRefID() );
        
        Cancelled clientUnsolCancelled = (Cancelled) upChain.getNextQueueEntry();
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled, cumQty2, avePx2 );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }
    
    public void testCancelFullFillToNewMissingClient() {

        setupNOSandACK( "C0000001A", qty1, px1, pkNOS1 );
        setupTrade( fillQty1, fillPx1, cumQty1, avePx1, "EX_TR_1", pkNOS1, pkTrade1, OrdStatus.PartiallyFilled );
        setupTradeCancel( "EX_TR_1", fillQty1, fillPx1, 0, 0, "EX_TR_2", pkNOS1, pkTrade2, OrdStatus.New );

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

        replay( exchangeIn,  pkTrade1._mInKey );
        replay( clientOut,   pkTrade1._cOutKey );

        replay( exchangeIn,  pkTrade2._mInKey );
        // missing client
        
        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        OrderRequest clientReq = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );

        _refExecId.copy( "EX_TR_1" );
        TradeCancel clientTradeCancel   = (TradeCancel) upChain;
        checkTrade( clientTradeCancel, clientReq, clientRep, OrdStatus.New, ExecType.TradeCancel, fillQty1, 0, fillPx1, 0 );
        assertEquals( _refExecId, clientTradeCancel.getExecRefID() );
        
        Cancelled clientUnsolCancelled = (Cancelled) upChain.getNextQueueEntry();
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled, 0, 0 );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }
}
