/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import com.rr.core.codec.FixDecoder;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.recovery.RecoverySessionContext;
import com.rr.model.generated.internal.events.impl.ClientCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelledImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.ClientReplacedImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeNewImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.RecoveryTradeCancelImpl;
import com.rr.model.generated.internal.events.impl.RecoveryTradeCorrectImpl;
import com.rr.model.generated.internal.events.impl.RecoveryTradeNewImpl;
import com.rr.model.generated.internal.events.interfaces.BaseOrderRequest;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.TradeBase;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.order.collections.OrderMap;
import com.rr.om.warmup.FixTestUtils;

/**
 * common code for HFT recovery
 * 
 * note tests follow form of
 * 
 * a) generate events simulating inputs from client and exchange, events generated are passed through processor to generate
 *    outbound events
 * b) input and output events are persisted in the dummy up/down sessions using in memory persistor
 *    this is necessary to allow events to be regenerated
 * c) events are regenerated and replayed into the recovery controller
 * d) reconcile is invoked
 * e) events are regenerated again (as previous ones recycled by controller) and used to verify up and downstream results from reconciliation
 *    
 * Recovery doesnt care about object reuse and GC is completely acceptable, inbound events are recycled to prevent the decoder's pools being
 * exhausted.
 */
public abstract class BaseHFTRecoveryTst extends BaseRecoveryTst {
    
    protected static final double OK_DELTA = 0.0000005;

    
    protected HighFreqSimpleRecoveryController _ctl;
    protected OrderMap                         _orders;

    protected FixDecoder                       _recoveryDecoder = FixTestUtils.getRecoveryDecoder44();
    
    protected DummyRecoverySession             _client1;
    protected DummyRecoverySession             _exchange1;

    /**
     * PKeys represents persistent keys for single order flow, ie event recieved from upstream, sent downstream and its reply back
     *
     * eg clientNOS, marketNOS, marketACK, clientACK
     */
    protected static class PKeys {
        protected long                             _cInKey, _mOutKey, _mInKey, _cOutKey;
    }

    @Override
    protected void setUp() throws Exception {
        int expOrders = 1;
        int totSessions = 2;

        _ctl = new HighFreqSimpleRecoveryController( expOrders, totSessions, _proc );
        _orders = _proc.getInternalMap();
        _client1 = DummyRecoverySession.create( "Client1", true, _proc );
        _exchange1 = DummyRecoverySession.create( "Exchange1", false, _proc );
    }

    protected void replay( RecoverySessionContext ctx, long key ) {
        Message msg = ctx.regenerate( key );
        if ( ctx.isInbound() ) {
            _ctl.processInbound( ctx, key, msg, (short) 0 );
        } else {
            _ctl.processOutbound( ctx, key, msg, (short) 0 );
        }
    }

    protected void setupReplacedOrder( String clOrdId, String repClOrdId, int qty, double px, int newQty, double newPx, PKeys pk1, PKeys pk2 ) {
        setupNOSandACK( clOrdId, qty, px, pk1 );
        setupCanRepReqAndReplaced( clOrdId, repClOrdId, newQty, newPx, pk2 );
    }

    protected void setupCancelledOrder( String clOrdId, String repClOrdId, int qty, double px, int newQty, double newPx, PKeys pk1, PKeys pk2 ) {
        setupNOSandACK( clOrdId, qty, px, pk1 );
        setupCanReqAndCancelled( clOrdId, repClOrdId, qty, px, pk2 );
    }

    protected void setupNOSandACK( String clOrdId, int qty, double px, PKeys pk ) {
        setupNOS( clOrdId, qty, px, pk );
        setupACK( clOrdId, qty, px, pk );
    }

    protected void setupNOS( String clOrdId, int qty, double px, PKeys pk ) {
        clearQueues();
        // SEND NOS
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, clOrdId, qty, px, _upMsgHandler );
        pk._cInKey = persist( _client1, nos, true );
        _proc.handle( nos );
        MarketNewOrderSingleImpl mnos = (MarketNewOrderSingleImpl) getMessage( _downQ, MarketNewOrderSingleImpl.class );
        pk._mOutKey = persist( _exchange1, mnos, false );
        clearQueues();
    }

    protected void setupACK( String clOrdId, int qty, double px, PKeys pk ) {
        // SYNTH MARKET ACK
        ZString mktOrderId = new ViewString("MO"+ clOrdId);
        NewOrderAck mack = FixTestUtils.getMarketACK( _recoveryDecoder, clOrdId, qty, px, mktOrderId, new ViewString( "ACK" + clOrdId ) );
        pk._mInKey = persist( _exchange1, mack, true );
        _proc.handle( mack ); // MACK now recycled
        checkQueueSize( 1, 0, 0 );
        ClientNewOrderAckImpl cack = (ClientNewOrderAckImpl) getMessage( _upQ, ClientNewOrderAckImpl.class );
        pk._cOutKey = persist( _client1, cack, false );
        clearQueues();
    }
    
    protected void setupTrade( int qty, double px, int cumQty, double avePx, String execId, PKeys keySRC, PKeys pkTrade, OrdStatus status ) {
        ReusableString buf = new ReusableString();
        OrderRequest req = (OrderRequest) regen( _exchange1, false, keySRC._mOutKey );
        _execId.copy( execId );
        RecoveryTradeNewImpl mtrade = (RecoveryTradeNewImpl) FixTestUtils.getMarketTradeNew( buf, _recoveryDecoder, _mktOrderId, req.getClOrdId(), 
                                                                                             req, qty, px, _execId );
        mtrade.setOrdStatus( status );
        mtrade.setCumQty( cumQty );
        mtrade.setAvgPx( avePx );
        mtrade.setLeavesQty( req.getOrderQty() - cumQty );
        if ( status.getIsTerminal() || mtrade.getLeavesQty() < 0 ) {
            mtrade.setLeavesQty( 0 );
        }
        pkTrade._mInKey = persist( _exchange1, mtrade, true );
        _proc.handle( mtrade ); 
        checkQueueSize( 1, 0, 0 );
        TradeNew cfill = (TradeNew) getMessage( _upQ, ClientTradeNewImpl.class );
        pkTrade._cOutKey = persist( _client1, cfill, false );
        clearQueues();
    }

    protected void setupTradeCorrect( String refExecId, 
                                      int       qty, 
                                      double    px, 
                                      int       cumQty, 
                                      double    avePx, 
                                      String    execId, 
                                      PKeys     keySRC, 
                                      PKeys     pkTrade, 
                                      OrdStatus status ) {
        
        ReusableString buf = new ReusableString();
        OrderRequest req = (OrderRequest) regen( _exchange1, false, keySRC._mOutKey );
        _execId.copy( execId );
        _refExecId.copy( refExecId );
        
        RecoveryTradeCorrectImpl mtrade = (RecoveryTradeCorrectImpl) 
                                            FixTestUtils.getMarketTradeCorrect( buf, _recoveryDecoder, _mktOrderId, req.getClOrdId(), 
                                                                                req, qty, px, _execId, _refExecId, status );
        mtrade.setOrdStatus( status );
        mtrade.setCumQty( cumQty );
        mtrade.setAvgPx( avePx );
        mtrade.setLeavesQty( req.getOrderQty() - cumQty );
        if ( status.getIsTerminal() || mtrade.getLeavesQty() < 0 ) {
            mtrade.setLeavesQty( 0 );
        }
        pkTrade._mInKey = persist( _exchange1, mtrade, true );
        _proc.handle( mtrade ); 
        checkQueueSize( 1, 0, 0 );
        TradeBase cfill = (TradeBase) getMessage( _upQ, TradeBase.class );
        pkTrade._cOutKey = persist( _client1, cfill, false );
        clearQueues();
    }

    protected void setupTradeCancel( String refExecId, 
                                     int       qty, 
                                     double    px, 
                                     int       cumQty, 
                                     double    avePx, 
                                     String    execId, 
                                     PKeys     keySRC, 
                                     PKeys     pkTrade, 
                                     OrdStatus status ) {
        
        ReusableString buf = new ReusableString();
        OrderRequest req = (OrderRequest) regen( _exchange1, false, keySRC._mOutKey );
        _execId.copy( execId );
        _refExecId.copy( refExecId );
        
        RecoveryTradeCancelImpl mtrade = (RecoveryTradeCancelImpl) 
                                            FixTestUtils.getMarketTradeCancel( buf, _recoveryDecoder, _mktOrderId, req.getClOrdId(), 
                                                                               req, qty, px, _execId, _refExecId, status );
        mtrade.setOrdStatus( status );
        mtrade.setCumQty( cumQty );
        mtrade.setAvgPx( avePx );
        mtrade.setLeavesQty( req.getOrderQty() - cumQty );
        if ( status.getIsTerminal() || mtrade.getLeavesQty() < 0 ) {
            mtrade.setLeavesQty( 0 );
        }
        pkTrade._mInKey = persist( _exchange1, mtrade, true );
        _proc.handle( mtrade ); 
        checkQueueSize( 1, 0, 0 );
        TradeBase cfill = (TradeBase) getMessage( _upQ, TradeBase.class );
        pkTrade._cOutKey = persist( _client1, cfill, false );
        clearQueues();
    }

    protected void setupCanRepReqAndReplaced( String clOrdId, String repClOrdId, int newQty, double newPx, PKeys pk ) {
        setupReplaceRequested( repClOrdId, clOrdId, newQty, newPx, pk );
        setupReplaced( repClOrdId, clOrdId, newQty, newPx, pk );
    }

    protected void setupReplaceRequested( String clOrdId, String origClOrdId, int newQty, double newPx, PKeys pk ) {
        // SEND CLIENT REPLACE REQUEST
        _clOrdId.setValue( clOrdId );
        _origClOrdId.setValue( origClOrdId );
        ClientCancelReplaceRequestImpl ccrr = FixTestUtils.getClientCancelReplaceRequest( _msgBuf, _decoder, _clOrdId, _origClOrdId, newQty, newPx,
                                                                                          _upMsgHandler );
        pk._cInKey = persist( _client1, ccrr, true );
        _proc.handle( ccrr );
        MarketCancelReplaceRequestImpl mcrr = (MarketCancelReplaceRequestImpl) getMessage( _downQ, MarketCancelReplaceRequestImpl.class );
        pk._mOutKey = persist( _exchange1, mcrr, false );
        clearQueues();
    }

    protected void setupReplaced( String clOrdId, String origClOrdId, int newQty, double newPx, PKeys pk ) {
        // SYNTH MARKET REPLACED -----------------------------------------------------------------------------
        _execId.setValue( "EX_RPD_" + clOrdId );
        _clOrdId.setValue( clOrdId );
        _origClOrdId.setValue( origClOrdId );

        Replaced mrep = FixTestUtils.getMarketReplaced( _msgBuf, _recoveryDecoder, _clOrdId, _origClOrdId, newQty, newPx, _orderId, _execId);
        pk._mInKey = persist( _exchange1, mrep, true );
        _proc.handle( mrep ); // MCXL now recycled, also CCAN recycled
        ClientReplacedImpl crep = (ClientReplacedImpl) getMessage( _upQ, ClientReplacedImpl.class );
        pk._cOutKey = persist( _client1, crep, false );
        clearQueues();
    }

    protected void setupCanReqAndCancelled( String clOrdId, String repClOrdId, int qty, double px, PKeys pk ) {
        setupCancelRequested( repClOrdId, clOrdId, pk );
        setupCancelled( repClOrdId, clOrdId, qty, px, pk );
    }

    protected void setupCancelRequested( String clOrdId, String origClOrdId, PKeys pk ) {
        _clOrdId.setValue( clOrdId );
        _origClOrdId.setValue( origClOrdId );
        ClientCancelRequestImpl ccrr = FixTestUtils.getClientCancelRequest( _msgBuf, _decoder, _clOrdId, _origClOrdId, _upMsgHandler );
        pk._cInKey = persist( _client1, ccrr, true );
        _proc.handle( ccrr );
        MarketCancelRequestImpl mcrr = (MarketCancelRequestImpl) getMessage( _downQ, MarketCancelRequestImpl.class );
        pk._mOutKey = persist( _exchange1, mcrr, false );
        clearQueues();
    }

    protected void setupCancelled( String clOrdId, String origClOrdId, int qty, double px, PKeys pk ) {
        _execId.setValue( "EX_RPD_" + clOrdId );
        _clOrdId.setValue( clOrdId );
        _origClOrdId.setValue( origClOrdId );

        Cancelled mrep = FixTestUtils.getCancelled( _msgBuf, _recoveryDecoder, _clOrdId, _origClOrdId, qty, px, _orderId, _execId);
        pk._mInKey = persist( _exchange1, mrep, true );
        _proc.handle( mrep ); // MCXL now recycled, also CCAN recycled
        ClientCancelledImpl crep = (ClientCancelledImpl) getMessage( _upQ, ClientCancelledImpl.class );
        pk._cOutKey = persist( _client1, crep, false );
        clearQueues();
    }

    protected void checkCancelReject( BaseOrderRequest req, CancelReject rej, OrdStatus expStatus ) {
        assertSame( expStatus, rej.getOrdStatus() );
        assertEquals( req.getClOrdId(),     rej.getClOrdId() );
        assertEquals( req.getOrigClOrdId(), rej.getOrigClOrdId() );

        if ( req instanceof CancelReplaceRequest ) {
            assertSame( CxlRejResponseTo.CancelReplace, rej.getCxlRejResponseTo() );
        } else {
            assertSame( CxlRejResponseTo.CancelRequest, rej.getCxlRejResponseTo() );
        }
    }

    protected void checkRequestReply( OrderRequest req, CommonExecRpt rej, OrdStatus expStatus, ExecType expType, int cumQty, double totalTraded ) {
        assertSame( expStatus, rej.getOrdStatus() );
        assertSame( expType, rej.getExecType() );

        checkExec( req, rej, cumQty, totalTraded );
    }

    private void checkExec( OrderRequest expReq, CommonExecRpt exec, int cumQty, double totalTraded ) {
        assertEquals( cumQty, exec.getCumQty() );
        assertEquals( (cumQty==0) ? 0 : (totalTraded/cumQty), exec.getAvgPx(), OK_DELTA );

        assertEquals( expReq.getClOrdId(),        exec.getClOrdId() );
        assertEquals( expReq.getOrderQty(),       exec.getOrderQty() );
        assertEquals( expReq.getPrice(),          exec.getPrice(),   OK_DELTA );
        assertEquals( expReq.getOrderCapacity(),  exec.getMktCapacity() );
        assertEquals( expReq.getSide(),           exec.getSide() );
        assertEquals( expReq.getSymbol(),         exec.getSymbol() );
    }
    
    protected void checkExec( CommonExecRpt    execToCheck, 
                              OrderRequest     baseRequest, 
                              CommonExecRpt    lastSentExec, 
                              OrdStatus        expStatus, 
                              ExecType         expType ) {

        checkExec( execToCheck, baseRequest, lastSentExec, expStatus, expType, lastSentExec.getCumQty(), lastSentExec.getAvgPx() );
    }
    
    protected void checkExec( CommonExecRpt    execToCheck, 
                              OrderRequest     baseRequest, 
                              CommonExecRpt    lastSentExec, 
                              OrdStatus        expStatus, 
                              ExecType         expType,
                              int              expCumQty,
                              double           expAvePx ) {
        
        assertSame( expStatus, execToCheck.getOrdStatus() );
        assertSame( expType, execToCheck.getExecType() );
        
        if ( expStatus.getIsTerminal() ) {
            assertEquals( 0, execToCheck.getLeavesQty() );
        } else {
            int leavesQty = baseRequest.getOrderQty() - expCumQty;
            
            assertEquals( leavesQty, execToCheck.getLeavesQty() );
        }
        
        assertEquals( baseRequest.getClOrdId(),      execToCheck.getClOrdId() );
        assertEquals( expCumQty,                     execToCheck.getCumQty() );
        assertEquals( expAvePx,                      execToCheck.getAvgPx(), OK_DELTA );
        assertEquals( baseRequest.getSymbol(),       execToCheck.getSymbol() );
        assertEquals( lastSentExec.getMktCapacity(), execToCheck.getMktCapacity() );
        assertEquals( baseRequest.getSide(),         execToCheck.getSide() );
        assertEquals( lastSentExec.getOrderId(),     execToCheck.getOrderId() );

        assertEquals( baseRequest.getOrderQty(),     execToCheck.getOrderQty() );
        assertEquals( baseRequest.getPrice(),        execToCheck.getPrice(),   OK_DELTA );
    }
    
    protected void checkTrade( TradeBase        execToCheck, 
                               OrderRequest     baseRequest, 
                               CommonExecRpt    lastSentExec, 
                               OrdStatus        expStatus, 
                               ExecType         expType,
                               int              expLastQty,
                               int              expCumQty,
                               double           expLastPx,
                               double           expAvePx ) {
        
        checkExec( execToCheck, baseRequest, lastSentExec, expStatus, expType, expCumQty, expAvePx );
        
        assertEquals( expLastQty, execToCheck.getLastQty() );
        assertEquals( expLastPx,  execToCheck.getLastPx(),    OK_DELTA );
    }
    
    protected void checkCancel( CommonExecRpt lastExec, CancelRequest mktCanReq ) {
        EventBuilderImpl.makeForceCancelClOrdId( _clOrdId, lastExec.getClOrdId() );
        
        assertEquals( lastExec.getClOrdId(), mktCanReq.getOrigClOrdId() );
        assertEquals( lastExec.getOrderId(), mktCanReq.getOrderId() );
        assertEquals( _clOrdId, mktCanReq.getClOrdId() );
        assertTrue( !_clOrdId.equals(mktCanReq.getOrigClOrdId()) );
    }
}
