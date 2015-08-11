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
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;

public class HFTRecoveryTradeCorrectTest extends BaseHFTRecoveryTst {

    private PKeys pkNOS1   = new PKeys();
    private PKeys pkTrade1 = new PKeys();
    private PKeys pkTrade2 = new PKeys();

    private final int    qty1 = 100;
    private final double px1  = 25.25;
    
    private final int    fillQty1    = 10;
    private final double fillPx1     = 25.15;

    private final int    cumQty1 = fillQty1;
    private final double avePx1  = fillPx1;
    
    public void testCorrectPartialFillToFullFill() {

        setupNOSandACK( "C0000001A", qty1, px1, pkNOS1 );
        setupTrade( 9, 25.0125, 9, 25.0125, "EX_TR_1", pkNOS1, pkTrade1, OrdStatus.PartiallyFilled );
        setupTradeCorrect( "EX_TR_1", qty1, px1, qty1, px1, "EX_TR_2", pkNOS1, pkTrade2, OrdStatus.Filled );

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
        
        assertNull( upChain );
        assertNull( downChain );
    }

    public void testCorrectFullFillToPartial() {

        setupNOSandACK( "C0000001A", qty1, px1, pkNOS1 );
        setupTrade( qty1, px1, qty1, px1, "EX_TR_1", pkNOS1, pkTrade1, OrdStatus.Filled );
        setupTradeCorrect( "EX_TR_1", fillQty1, fillPx1, cumQty1, avePx1, "EX_TR_2", pkNOS1, pkTrade2, OrdStatus.PartiallyFilled );

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
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled, cumQty1, avePx1 );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }
    
    public void testCorrectFullFillToPartialMissingClient() {

        setupNOSandACK( "C0000001A", qty1, px1, pkNOS1 );
        setupTrade( qty1, px1, qty1, px1, "EX_TR_1", pkNOS1, pkTrade1, OrdStatus.Filled );
        setupTradeCorrect( "EX_TR_1", fillQty1, fillPx1, cumQty1, avePx1, "EX_TR_2", pkNOS1, pkTrade2, OrdStatus.PartiallyFilled );

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
        // missing client correct 

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNotNull( downChain );

        OrderRequest clientReq = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );

        _refExecId.copy( "EX_TR_1" );
        TradeCorrect clientTradeCorrect   = (TradeCorrect) upChain;
        checkTrade( clientTradeCorrect, clientReq, clientRep, OrdStatus.PartiallyFilled, ExecType.TradeCorrect, fillQty1, cumQty1, fillPx1, avePx1 );
        assertEquals( _refExecId, clientTradeCorrect.getExecRefID() );
        
        Cancelled clientUnsolCancelled = (Cancelled) upChain.getNextQueueEntry();
        checkExec( clientUnsolCancelled, clientReq, clientRep, OrdStatus.Canceled, ExecType.Canceled, cumQty1, avePx1 );
        
        CancelRequest mktCanReq = (CancelRequest) downChain;
        NewOrderAck mktAck = (NewOrderAck) exchangeIn.regenerate( pkNOS1._mInKey );
        checkCancel( mktAck, mktCanReq );
    }

    public void testCorrectPartialToFullFillMissingClient() {

        setupNOSandACK( "C0000001A", qty1, px1, pkNOS1 );
        setupTrade( fillQty1, fillPx1, cumQty1, avePx1, "EX_TR_1", pkNOS1, pkTrade1, OrdStatus.PartiallyFilled );
        setupTradeCorrect( "EX_TR_1", qty1, px1, qty1, px1, "EX_TR_2", pkNOS1, pkTrade2, OrdStatus.Filled );

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
        // missing client correct 

        _ctl.reconcile();

        assertEquals( 1, _orders.size() );
        
        Message upChain = _ctl.getUpstreamChain();
        Message downChain = _ctl.getDownChain();
        
        assertNotNull( upChain );
        assertNull( downChain );

        OrderRequest clientReq = (OrderRequest) clientIn.regenerate( pkNOS1._cInKey );
        NewOrderAck  clientRep = (NewOrderAck) clientOut.regenerate( pkNOS1._cOutKey );

        _refExecId.copy( "EX_TR_1" );
        TradeCorrect clientTradeCorrect   = (TradeCorrect) upChain;
        checkTrade( clientTradeCorrect, clientReq, clientRep, OrdStatus.Filled, ExecType.TradeCorrect, qty1, qty1, px1, px1 );
        assertEquals( _refExecId, clientTradeCorrect.getExecRefID() );
        
        assertNull( upChain.getNextQueueEntry() );
    }
}
