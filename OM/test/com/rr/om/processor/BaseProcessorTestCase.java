/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import org.junit.Assert;

import com.rr.core.codec.FixDecoder;
import com.rr.core.collections.MessageQueue;
import com.rr.core.collections.NonBlockingSyncQueue;
import com.rr.core.collections.SimpleMessageQueue;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Currency;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.ModelVersion;
import com.rr.model.generated.internal.events.impl.ClientAlertLimitBreachImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.ClientRejectedImpl;
import com.rr.model.generated.internal.events.impl.ClientReplacedImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeCancelImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeCorrectImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeNewImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.TradeBase;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.internal.type.ExecType;
import com.rr.om.client.OMClientProfile;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.order.collections.OrderMap;
import com.rr.om.processor.states.OrderState;
import com.rr.om.registry.FullTradeRegistry;
import com.rr.om.registry.SimpleTradeRegistry;
import com.rr.om.registry.TradeRegistry;
import com.rr.om.router.OrderRouter;
import com.rr.om.router.SingleDestRouter;
import com.rr.om.validate.EmeaDmaValidator;
import com.rr.om.validate.EventValidator;
import com.rr.om.warmup.FixTestUtils;

public abstract class BaseProcessorTestCase extends BaseTestCase {

    private static final ZString BASE_EXEC_ID = new ViewString( "FEXE000" );

    protected static final ZString CAN_REJECT = new ViewString( "Unable to cancel" );
    protected static final ZString REP_REJECT = new ViewString( "Unable to replace" );


    static final class TestEventProcConfig implements EventProcConfig {

        private boolean _forceCancelUnknownExexId;
        
        public TestEventProcConfig( boolean forceCancelUnknownExexId ) {
            _forceCancelUnknownExexId = forceCancelUnknownExexId;
        }

        @Override
        public boolean isForceCancelUnknownExexId() {
            return _forceCancelUnknownExexId;
        }
        
        public void setForceCancelUnknownExexId( boolean forceCancelUnknownExexId ) {
            _forceCancelUnknownExexId = forceCancelUnknownExexId;
        }
    }
    
    protected MessageHandler _upMsgHandler   = getTestUpStreamHandler();
    protected MessageHandler _downMsgHandler = getTestDownStreamHandler();
    protected MessageHandler _hubHandler     = getTestHubHandler();

    protected MessageQueue   _downQ = new NonBlockingSyncQueue();
    protected MessageQueue   _upQ   = new NonBlockingSyncQueue();
    protected MessageQueue   _hubQ  = new NonBlockingSyncQueue();

    protected FixDecoder        _decoder        = createDecoder();
    protected ReusableString    _msgBuf         = new ReusableString();
    protected ReusableString    _clOrdId        = new ReusableString( "TST0000001" );
    protected ReusableString    _origClOrdId    = new ReusableString( "TST0000001" );
    protected ReusableString    _mktOrigClOrdId = new ReusableString();
    protected ReusableString    _execId         = new ReusableString();
    protected ReusableString    _refExecId      = new ReusableString();
    protected ReusableString    _orderId        = new ReusableString();
    protected ReusableString    _cancelClOrdId  = new ReusableString( "TST000000B" );
    protected ReusableString    _mktClOrdId     = new ReusableString( "TST0000001" );
    protected ReusableString    _cancelExecId   = new ReusableString( "EXE5000B" );
    protected ReusableString    _mktOrderId     = new ReusableString( "ORD000001" );
    protected ReusableString    _fillExecId     = new ReusableString( "EXE50002" );
    protected ReusableString    _ackExecId      = new ReusableString( "ACKTST0000001" );

    
    protected final  TestEventProcConfig _procCfg = new TestEventProcConfig( true );
    
    protected final  EventProcessorImpl _proc     = getProcesssor( 4, isFullRegistryRequired() );
    protected final  OrderMap           _orderMap = _proc.getInternalMap();

    
    private       int            _fillIdx    = 1;                    // fill couter for use in generating id
    
    protected EventProcessorImpl getProcesssor( int expectedOrders, boolean isFullReg ) {
        return getProcesssor( expectedOrders, isFullReg, true );
    }
    
    protected EventProcessorImpl getProcesssor( int expectedOrders, boolean isFullReg, boolean isForceCancelUnknownExecId ) {
        
        ModelVersion      version       = new ModelVersion( (byte)'1', (byte)'0' );
        EventValidator    validator     = new EmeaDmaValidator( Integer.MAX_VALUE );
        EventBuilder      builder       = new EventBuilderImpl();
        OrderRouter       router        = new SingleDestRouter( _downMsgHandler );
        MessageDispatcher dispatcher    = new DirectDispatcher();
        TradeRegistry     tradeReg      = (isFullReg) ? new FullTradeRegistry(2) : new SimpleTradeRegistry(2);

        _procCfg.setForceCancelUnknownExexId( isForceCancelUnknownExecId );
        
        EventProcessorImpl t = new EventProcessorImpl( _procCfg, null, version, expectedOrders, validator, builder, dispatcher, _hubHandler, tradeReg );
        t.setProcessorRouter( router );
        
        dispatcher.start();
        
        return t;
    }

    protected FixDecoder createDecoder() {
        return FixTestUtils.getDecoder44();
    }

    protected boolean isFullRegistryRequired() {
        return true;
    }

    protected EventProcessorImpl getConcProcesssor( int expectedOrders, boolean isFullReg ) {
        
        ModelVersion      version       = new ModelVersion( (byte)'1', (byte)'0' );
        EventValidator    validator     = new EmeaDmaValidator( Integer.MAX_VALUE );
        EventBuilder      builder       = new EventBuilderImpl();
        OrderRouter   router        = new SingleDestRouter( _downMsgHandler );
        MessageDispatcher dispatcher    = new DirectDispatcher();
        TradeRegistry     tradeReg      = (isFullReg) ? new FullTradeRegistry(2) : new SimpleTradeRegistry(2);
        
        EventProcessorImpl t = new EventProcessorImpl( version, expectedOrders, validator, builder, dispatcher, _hubHandler, tradeReg );
        t.setProcessorRouter( router );
        
        dispatcher.start();
        
        return t;
    }

    protected EventProcessorImpl getDirectProcesssor( int expectedOrders, boolean isFullReg ) {
        
        _downQ = new SimpleMessageQueue();
        _upQ   = new SimpleMessageQueue();
        _hubQ  = new SimpleMessageQueue();

        ModelVersion      version       = new ModelVersion( (byte)'1', (byte)'0' );
        EventValidator    validator     = new EmeaDmaValidator( Integer.MAX_VALUE );
        EventBuilder      builder       = new EventBuilderImpl();
        OrderRouter   router        = new SingleDestRouter( _downMsgHandler );
        MessageDispatcher dispatcher    = new DirectDispatcher();
        TradeRegistry     tradeReg      = (isFullReg) ? new FullTradeRegistry(2) : new SimpleTradeRegistry(2);
        
        EventProcessorImpl t = new EventProcessorImpl( version, expectedOrders, validator, builder, dispatcher, _hubHandler, tradeReg );
        t.setProcessorRouter( router );
        
        dispatcher.start();
        
        return t;
    }

    protected void clearQueues() {
        _downQ.clear();
        _upQ.clear();
    }

    protected Message getMessage( MessageQueue q, Class<? extends Message> expClass ) {
        Message m = q.poll();
        
        assertNotNull( m );
        
        assertTrue( expClass.isAssignableFrom( m.getClass() ) );
        
        return m;
    }

    protected synchronized void checkQueueSize( int expUpQ, int expDownQ, int expHubQ ) {
        assertEquals( expUpQ,   _upQ.size() );
        assertEquals( expDownQ, _downQ.size() );
        assertEquals( expHubQ,  _hubQ.size() );
    }
                                   
    private MessageHandler getTestUpStreamHandler() {
        return( new MessageHandler() {
                @Override
                public void handle( Message msg ) { _upQ.add( msg ); }

                @Override
                public void handleNow( Message msg ) { _upQ.add( msg ); }

                @Override
                public void threadedInit() { /* nothing */ }

                @Override
                public boolean canHandle() { return true; }

                @Override
                public String getComponentId() { return null; }
            } );
    }

    private MessageHandler getTestDownStreamHandler() {
        return( new MessageHandler() {
                @Override
                public void handle( Message msg ) { _downQ.add( msg ); }

                @Override
                public void handleNow( Message msg ) { _downQ.add( msg ); }

                @Override
                public void threadedInit() { /* nothing */ }

                @Override
                public boolean canHandle() { return true; }

                @Override
                public String getComponentId() { return null; }
            } );
    }

    private MessageHandler getTestHubHandler() {
        return( new MessageHandler() {
                @Override
                public void handle( Message msg ) { _hubQ.add( msg ); }

                @Override
                public void handleNow( Message msg ) { _hubQ.add( msg ); }

                @Override
                public void threadedInit() { /* nothing */ }
                
                @Override
                public boolean canHandle() { return true; }

                @Override
                public String getComponentId() { return null; }
            } );
    }
    
    @SuppressWarnings( "boxing" )
    protected void checkOrder( OMClientProfile client, int qty, double price, String alertMsg, boolean expAlert, boolean reject ) {
        
        _proc.clear();
        
        long   origClientQty = client.getTotalOrderQty();
        double origTotVal    = client.getTotalOrderValueUSD();
        
        ClientNewOrderSingleImpl nos = FixTestUtils.getClientNOS( _decoder, "TST0000001", qty, price, _upMsgHandler );
        nos.setClient( client );
        
        _proc.handle( nos );
        
        Order order = _proc.getOrder( nos.getClOrdId() );
        
        if ( reject ) {
            checkQueueSize( 1, 0, 0 );
            ClientRejectedImpl rejected = (ClientRejectedImpl) getMessage( _upQ,  ClientRejectedImpl.class );
            Assert.assertTrue( rejected.getText().toString(), rejected.getText().equals( alertMsg ) );
        
            return;
        }
        
        if ( expAlert ) {
            checkQueueSize( 0, 1, 1 );

            ClientAlertLimitBreachImpl alert = (ClientAlertLimitBreachImpl) getMessage( _hubQ,  ClientAlertLimitBreachImpl.class );
            checkAlert( nos, alert, alertMsg );
            
        } else {
            checkQueueSize( 0, 1, 0 );
        }
        
        assertNotNull( order );
        
        assertSame( _proc.getStatePendingNew(),  order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
        
        OrderVersion ver = order.getLastAckedVerion();

        assertNotNull( ver );
        
        assertSame(   nos,                      ver.getBaseOrderRequest() );
        assertEquals( nos.getOrderQty(),        ver.getLeavesQty() );
        assertEquals( 0.0,                      ver.getAvgPx() );
        assertEquals( 0,                        ver.getCumQty() );
        assertEquals( OrderCapacity.Principal,  ver.getMarketCapacity() );
        
        double totVal = origTotVal;
        long   totQty = origClientQty;
        
        if ( origTotVal + nos.getPrice() * nos.getOrderQty() * nos.getCurrency().toUSDFactor() <= client.getMaxTotalOrderValueUSD() ) {
            totVal += (nos.getPrice() * nos.getOrderQty() * nos.getCurrency().toUSDFactor());
            totQty += nos.getOrderQty();
        }
        
        assertSame( order.getClientProfile(), nos.getClient() );
        assertEquals( totQty,                 client.getTotalOrderQty() );
        assertEquals( totVal,                 client.getTotalOrderValueUSD() );
        
        MarketNewOrderSingleImpl   mnos  = (MarketNewOrderSingleImpl)   getMessage( _downQ, MarketNewOrderSingleImpl.class );
        checkMarketNOS( nos, mnos );
    }

    protected static void checkAlert( ClientNewOrderSingleImpl nos, ClientAlertLimitBreachImpl alert, String text ) {
        
        // check those that should be same as src NOS
        Assert.assertSame(   nos,                       alert.getSrcEvent() );
        Assert.assertEquals( nos.getClOrdId(),          alert.getClOrdId() );
        Assert.assertEquals( nos.getOrderQty(),         alert.getOrderQty() );
        Assert.assertEquals( nos.getOnBehalfOfId(),     alert.getOnBehalfOfId() );
        Assert.assertEquals( nos.getSecurityId(),       alert.getSecurityId() );
        Assert.assertEquals( nos.getSecurityIDSource(), alert.getSecurityIDSource() );
        Assert.assertEquals( nos.getSide(),             alert.getSide() );
        Assert.assertEquals( nos.getSymbol(),           alert.getSymbol() );

        // check this fields which can be overriden
        Assert.assertSame(   nos.getCurrency(),         alert.getCurrency() );
        Assert.assertEquals( nos.getPrice(),            alert.getPrice(), Constants.WEIGHT  );
        
        // check those fields which should be different
        Assert.assertFalse( alert.getPossDupFlag() );

        Assert.assertTrue( alert.getText().toString(), alert.getText().equals( text ) );
    }
    
    public TradeNew sendFILL( EventProcessor proc, 
                              ReusableString msgBuf, 
                              FixDecoder     decoder, 
                              OrdStatus      expOrdStatus,
                              Order          order, 
                              int            lastQty, 
                              double         lastPx, 
                              int            cumQty, 
                              double         totTraded,
                              OrderState     expState ) {
        
        // order now open send partial fill and get client fill
        
        OrderVersion lastAcc    = order.getLastAckedVerion();
        OrderRequest src        = (OrderRequest) lastAcc.getBaseOrderRequest();
        ZString      mktOrderId = lastAcc.getMarketOrderId();
        ZString      mktClOrdId = lastAcc.getMarketClOrdId();
        
        _fillExecId.setValue( BASE_EXEC_ID );
        _fillExecId.append( _fillIdx++ );
        
        TradeNew mfill = FixTestUtils.getMarketTradeNew( msgBuf, decoder, mktOrderId, mktClOrdId, src,  lastQty, lastPx, _fillExecId );
        proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );

        assertSame( expState,                   order.getState() );
        
        if ( expState == proc.getStatePendingCancel() || expState == proc.getStatePendingReplace() ) {
            assertNotSame( order.getLastAckedVerion(), order.getPendingVersion() );
        } else {
            assertSame( order.getLastAckedVerion(), order.getPendingVersion() );
        }
        
        checkTradeNew( order, src, cfill, mktOrderId, _fillExecId, lastQty, lastPx, cumQty, totTraded, expOrdStatus );
        
        return cfill;
    }
    
    public static void checkTradeNew( Order                    order,
                                      OrderRequest             csrc, // nos or amend 
                                      ClientTradeNewImpl       fill, 
                                      ZString                  mktOrderId, 
                                      ZString                  mktExecId,
                                      int                      lastQty,
                                      double                   lastPx,
                                      int                      cumQty,
                                      double                   totTraded,
                                      OrdStatus                ordStatus ) {

        Assert.assertSame( csrc, fill.getSrcEvent() );

        checkTrade( ExecType.Trade, order, csrc, fill, mktOrderId, mktExecId, lastQty, lastPx, cumQty, totTraded, ordStatus );
    }

    public static void checkTradeCancel( Order                    order,
                                         OrderRequest             csrc, // nos or amend 
                                         ClientTradeCancelImpl    ccan, 
                                         ZString                  mktOrderId, 
                                         ZString                  mktExecId,
                                         int                      lastQty,
                                         double                   lastPx,
                                         int                      cumQty,
                                         double                   totTraded,
                                         OrdStatus                ordStatus ) {

        Assert.assertSame( csrc, ccan.getSrcEvent() );

        checkTrade( ExecType.TradeCancel, order, csrc, ccan, mktOrderId, mktExecId, lastQty, lastPx, cumQty, totTraded, ordStatus );
    }

    public static void checkTradeCorrect( Order                    order,
                                          OrderRequest             csrc, // nos or amend 
                                          ClientTradeCorrectImpl   ccor, 
                                          ZString                  mktOrderId, 
                                          ZString                  mktExecId,
                                          int                      lastQty,
                                          double                   lastPx,
                                          int                      cumQty,
                                          double                   totTraded,
                                          OrdStatus                ordStatus ) {

        Assert.assertSame( csrc, ccor.getSrcEvent() );

        checkTrade( ExecType.TradeCorrect, order, csrc, ccor, mktOrderId, mktExecId, lastQty, lastPx, cumQty, totTraded, ordStatus );
    }

    @SuppressWarnings( "boxing" )
    public static void checkTrade( ExecType                 tradeType,
                                   Order                    order,
                                   OrderRequest             csrc, // nos or amend 
                                   TradeBase                trade, 
                                   ZString                  mktOrderId, 
                                   ZString                  mktExecId,
                                   int                      lastQty,
                                   double                   lastPx,
                                   int                      cumQty,
                                   double                   totTraded,
                                   OrdStatus                ordStatus ) {

        OrderVersion  lastAcc   = order.getLastAckedVerion();
        double        totVal    = ((csrc.getPrice() * (csrc.getOrderQty() - cumQty)) + totTraded) * csrc.getCurrency().toUSDFactor();
        
        ClientProfile client = order.getClientProfile();
        
        Assert.assertSame( client, csrc.getClient() );
        
        if ( ordStatus.getIsTerminal() ) {
            Assert.assertEquals( cumQty,                      client.getTotalOrderQty() );
            Assert.assertEquals( totTraded,                   client.getTotalOrderValueUSD(), Constants.WEIGHT );
            Assert.assertEquals( 0,                           lastAcc.getLeavesQty() );
            Assert.assertEquals( 0,                           trade.getLeavesQty() );
        } else {
            Assert.assertEquals( csrc.getOrderQty() - cumQty, trade.getLeavesQty() );
            Assert.assertEquals( csrc.getOrderQty(),          client.getTotalOrderQty() );
            Assert.assertEquals( totVal,                      client.getTotalOrderValueUSD(), Constants.WEIGHT );
            Assert.assertEquals( csrc.getOrderQty() - cumQty, lastAcc.getLeavesQty() );
        }
        
        double avePx = (cumQty>0) ? totTraded / cumQty : 0.0;
        
        // check version
        Assert.assertSame(   csrc,                        lastAcc.getBaseOrderRequest() );
        Assert.assertEquals( avePx,                       lastAcc.getAvgPx(), Constants.WEIGHT );
        Assert.assertEquals( cumQty,                      lastAcc.getCumQty() );
        Assert.assertEquals( totTraded,                   lastAcc.getTotalTraded(), Constants.WEIGHT );
        Assert.assertEquals( OrderCapacity.Principal,     lastAcc.getMarketCapacity() );
        
        // check those that should be same as src NOS
        Assert.assertEquals( csrc.getClOrdId(),           trade.getClOrdId() );
        Assert.assertEquals( csrc.getOnBehalfOfId(),      trade.getOnBehalfOfId() );
        Assert.assertEquals( csrc.getSecurityId(),        trade.getSecurityId() );
        Assert.assertEquals( csrc.getSecurityIDSource(),  trade.getSecurityIDSource() );
        Assert.assertEquals( csrc.getSide(),              trade.getSide() );
        Assert.assertEquals( csrc.getSymbol(),            trade.getSymbol() );

        Assert.assertSame( csrc.getInstrument().getCurrency(), trade.getCurrency() );

        // check those fields come from market
        Assert.assertEquals( mktOrderId,                  trade.getOrderId() );
        Assert.assertEquals( mktExecId,                   trade.getExecId() );

        Assert.assertEquals( tradeType,                   trade.getExecType() );
        
        Assert.assertEquals( ordStatus,                   trade.getOrdStatus() );
        
        // check those fields that are set by processor
        Assert.assertEquals( order.getLastAckedVerion().getMarketPrice(), trade.getPrice(), Constants.WEIGHT  );
        
        Assert.assertEquals( lastPx,                      trade.getLastPx(), Constants.WEIGHT  );
        Assert.assertEquals( lastQty,                     trade.getLastQty() );
        Assert.assertEquals( cumQty,                      trade.getCumQty() );
        Assert.assertEquals( avePx,                       trade.getAvgPx(), Constants.WEIGHT  );
        Assert.assertEquals( OrderCapacity.Principal,     trade.getMktCapacity() );
        
        // check those fields which should be different
        Assert.assertEquals( false,                       trade.getPossDupFlag() );
    }

    public static void checkClientCancelReject( OrderRequest             creq, 
                                                ClientCancelRejectImpl   ccxl, 
                                                ZString                  canClientClOrdId, 
                                                ZString                  origClOrdId,
                                                ZString                  mktOrderId,
                                                ZString                  text,
                                                CxlRejReason             canRejReason, 
                                                CxlRejResponseTo         responseTo, 
                                                OrdStatus                ordStatus ) {  
        
        Assert.assertSame(   creq,                       ccxl.getSrcEvent() );

        // check those that should be same as src NOS
        Assert.assertEquals( creq.getOnBehalfOfId(),     ccxl.getOnBehalfOfId() );

        // check those fields come from market
        Assert.assertTrue( ccxl.getOrderId().equals( mktOrderId ) );
        
        Assert.assertEquals( ordStatus,                  ccxl.getOrdStatus() );
        Assert.assertEquals( canRejReason,               ccxl.getCxlRejReason() );
        Assert.assertEquals( responseTo,                 ccxl.getCxlRejResponseTo() );
        Assert.assertEquals( ordStatus,                  ccxl.getOrdStatus() );
        Assert.assertEquals( text,                       ccxl.getText() );
        
        // check those fields that are set by processor
        Assert.assertEquals( origClOrdId,                ccxl.getOrigClOrdId() );
        Assert.assertEquals( canClientClOrdId,           ccxl.getClOrdId() );
        
        // check those fields which should be different
        Assert.assertFalse( ccxl.getPossDupFlag() );
    }

    @SuppressWarnings( "boxing" )
    public static void checkMarketNOS( ClientNewOrderSingleImpl nos, MarketNewOrderSingleImpl mnos ) {
        
        Currency instCcy = nos.getInstrument().getCurrency();
        
        // check those that should be same as src NOS
        Assert.assertEquals( nos.getClOrdId(),          mnos.getClOrdId() );
        Assert.assertEquals( nos.getOrderQty(),         mnos.getOrderQty() );
        Assert.assertEquals( nos.getAccount(),          mnos.getAccount() );
        Assert.assertEquals( nos.getExecInst(),         mnos.getExecInst() );
        Assert.assertEquals( nos.getHandlInst(),        mnos.getHandlInst() );
        Assert.assertEquals( nos.getExDest(),           mnos.getExDest() );
        Assert.assertEquals( nos.getOnBehalfOfId(),     mnos.getOnBehalfOfId() );
        Assert.assertEquals( nos.getOrdType(),          mnos.getOrdType() );
        Assert.assertEquals( nos.getOrigClOrdId(),      mnos.getOrigClOrdId() );
        Assert.assertEquals( nos.getSecurityExchange(), mnos.getSecurityExchange() );
        Assert.assertEquals( nos.getSecurityId(),       mnos.getSecurityId() );
        Assert.assertEquals( nos.getSecurityIDSource(), mnos.getSecurityIDSource() );
        Assert.assertEquals( nos.getSecurityType(),     mnos.getSecurityType() );
        Assert.assertEquals( nos.getSide(),             mnos.getSide() );
        Assert.assertEquals( nos.getSymbol(),           mnos.getSymbol() );
        Assert.assertEquals( nos.getTimeInForce(),      mnos.getTimeInForce() );
        Assert.assertEquals( nos.getText(),             mnos.getText() );

        // check thise fields which can be overriden
        Assert.assertSame(   instCcy,                   mnos.getCurrency() );
        Assert.assertEquals( nos.getPrice(),            mnos.getPrice(), Constants.WEIGHT  );
        Assert.assertEquals( OrderCapacity.Principal,   mnos.getOrderCapacity() );
        
        // check those fields which should be different
        Assert.assertEquals( false,                     mnos.getPossDupFlag() );
    }

    @SuppressWarnings( "boxing" )
    public static void checkMarketReplace( ClientCancelReplaceRequestImpl crep, 
                                           MarketCancelReplaceRequestImpl mrep, 
                                           ReusableString                 mktOrigClOrdId, 
                                           ReusableString                 orderId ) {
        
        Currency instCcy = crep.getInstrument().getCurrency();
        
        // check those that should be same as src NOS
        Assert.assertEquals( crep.getOrderQty(),         mrep.getOrderQty() );
        Assert.assertEquals( crep.getAccount(),          mrep.getAccount() );
        Assert.assertEquals( crep.getExecInst(),         mrep.getExecInst() );
        Assert.assertEquals( crep.getHandlInst(),        mrep.getHandlInst() );
        Assert.assertEquals( crep.getExDest(),           mrep.getExDest() );
        Assert.assertEquals( crep.getOnBehalfOfId(),     mrep.getOnBehalfOfId() );
        Assert.assertEquals( crep.getOrdType(),          mrep.getOrdType() );
        Assert.assertEquals( crep.getSecurityExchange(), mrep.getSecurityExchange() );
        Assert.assertEquals( crep.getSecurityId(),       mrep.getSecurityId() );
        Assert.assertEquals( crep.getSecurityIDSource(), mrep.getSecurityIDSource() );
        Assert.assertEquals( crep.getSecurityType(),     mrep.getSecurityType() );
        Assert.assertEquals( crep.getSide(),             mrep.getSide() );
        Assert.assertEquals( crep.getSymbol(),           mrep.getSymbol() );
        Assert.assertEquals( crep.getTimeInForce(),      mrep.getTimeInForce() );
        Assert.assertEquals( crep.getText(),             mrep.getText() );

        // check thise fields which can be overriden
        Assert.assertSame(   instCcy,                    mrep.getCurrency() );
        Assert.assertEquals( crep.getPrice(),            mrep.getPrice(), Constants.WEIGHT  );
        Assert.assertEquals( OrderCapacity.Principal,    mrep.getOrderCapacity() );
        Assert.assertEquals( mktOrigClOrdId,             mrep.getOrigClOrdId() );
        Assert.assertEquals( crep.getClOrdId(),          mrep.getClOrdId() );
        Assert.assertEquals( orderId,                    mrep.getOrderId() );
        
        // check those fields which should be different
        Assert.assertEquals( false,                     mrep.getPossDupFlag() );
    }

    @SuppressWarnings( "boxing" )
    public static void checkClientAck( ClientNewOrderSingleImpl cnos, ClientNewOrderAckImpl cack, ZString mktOrderId, ZString mktExecId ) {
        
        Assert.assertSame(   cnos,                       cack.getSrcEvent() );

        // check those that should be same as src NOS
        Assert.assertEquals( cnos.getClOrdId(),          cack.getClOrdId() );
        Assert.assertEquals( cnos.getOnBehalfOfId(),     cack.getOnBehalfOfId() );
        Assert.assertEquals( cnos.getSecurityId(),       cack.getSecurityId() );
        Assert.assertEquals( cnos.getSecurityIDSource(), cack.getSecurityIDSource() );
        Assert.assertEquals( cnos.getSide(),             cack.getSide() );
        Assert.assertEquals( cnos.getSymbol(),           cack.getSymbol() );
        Assert.assertEquals( cnos.getOrderReceived(),    cack.getOrderReceived() );
        Assert.assertSame(   cnos.getCurrency(),         cack.getCurrency() );

        // check those fields come from market
        Assert.assertEquals( mktOrderId,                 cack.getOrderId() );
        Assert.assertEquals( mktExecId,                  cack.getExecId() );
        
        Assert.assertEquals( ExecType.New,               cack.getExecType() );
        Assert.assertEquals( OrdStatus.New,              cack.getOrdStatus() );
        
        // check those fields that are set by processor
        Assert.assertEquals( cnos.getPrice(),            cack.getPrice(), Constants.WEIGHT  );
        Assert.assertEquals( cnos.getOrderQty(),         cack.getLeavesQty() );
        Assert.assertEquals( 0,                          cack.getCumQty() );
        Assert.assertEquals( 0.0,                        cack.getAvgPx(), Constants.WEIGHT  );
        Assert.assertEquals( OrderCapacity.Principal,    cack.getMktCapacity() );
        
        // check those fields which should be different
        Assert.assertEquals( false,                      cack.getPossDupFlag() );
    }

    @SuppressWarnings( "boxing" )
    public static void checkClientReplaced( ClientCancelReplaceRequestImpl ccrr, 
                                            ClientReplacedImpl             crep, 
                                            ZString                        mktOrderId, 
                                            ZString                        mktExecId, 
                                            int                            cumQty, 
                                            double                         totTraded ) {
        
        Assert.assertSame(   ccrr,                       crep.getSrcEvent() );

        // check those that should be same as src NOS
        Assert.assertEquals( ccrr.getClOrdId(),          crep.getClOrdId() );
        Assert.assertEquals( ccrr.getOrigClOrdId(),      crep.getOrigClOrdId() );
        Assert.assertEquals( ccrr.getOnBehalfOfId(),     crep.getOnBehalfOfId() );
        Assert.assertEquals( ccrr.getSecurityId(),       crep.getSecurityId() );
        Assert.assertEquals( ccrr.getSecurityIDSource(), crep.getSecurityIDSource() );
        Assert.assertEquals( ccrr.getSide(),             crep.getSide() );
        Assert.assertEquals( ccrr.getSymbol(),           crep.getSymbol() );
        Assert.assertSame(   ccrr.getCurrency(),         crep.getCurrency() );

        // check those fields come from market
        Assert.assertEquals( mktOrderId,                 crep.getOrderId() );
        Assert.assertEquals( mktExecId,                  crep.getExecId() );
        
        Assert.assertEquals( ExecType.Replaced,          crep.getExecType() );
        
        if ( crep.getCumQty() == 0 ) {
            Assert.assertEquals( OrdStatus.New,             crep.getOrdStatus() );
        } else if ( crep.getLeavesQty() == 0 ) {
            Assert.assertEquals( OrdStatus.Filled,          crep.getOrdStatus() );
        } else {
            Assert.assertEquals( OrdStatus.PartiallyFilled, crep.getOrdStatus() );
        }
        
        // check those fields that are set by processor
        Assert.assertEquals( OrderCapacity.Principal,    crep.getMktCapacity() );
        Assert.assertEquals( ccrr.getPrice(),            crep.getPrice(), Constants.WEIGHT  );
        Assert.assertEquals( ccrr.getOrderQty()-cumQty,  crep.getLeavesQty() );
        Assert.assertEquals( cumQty,                     crep.getCumQty() );
        
        if ( cumQty > 0 )
            Assert.assertEquals( totTraded/cumQty,       crep.getAvgPx(), Constants.WEIGHT  );
        else 
            Assert.assertEquals( 0.0,                    crep.getAvgPx(), Constants.WEIGHT  );
        
        // check those fields which should be different
        Assert.assertEquals( false,                      crep.getPossDupFlag() );
    }
}