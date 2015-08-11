/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.factories.ReusableStringFactory;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.factory.ClientCancelledFactory;
import com.rr.model.generated.internal.events.factory.ClientNewOrderAckFactory;
import com.rr.model.generated.internal.events.factory.ClientReplacedFactory;
import com.rr.model.generated.internal.events.impl.ClientCancelledImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientReplacedImpl;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.ClientCancelReplaceRequestWrite;
import com.rr.model.generated.internal.events.interfaces.MarketTradeNewWrite;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.internal.type.ExecType;
import com.rr.om.book.sim.SimpleOrderBook;
import com.rr.om.client.OMClientProfile;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.order.Order;
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.order.collections.OrderMap;
import com.rr.om.order.collections.SegmentOrderMap;
import com.rr.om.processor.OrderFactory;
import com.rr.om.processor.OrderVersionFactory;

public final class ExchangeSim {

    private static final Logger                 _log                  = LoggerFactory.create( ExchangeSim.class );
    
    private static final ZString                EXECID_PREFIX         = new ViewString( "90" );
    private static final ZString                DUP_ORDER             = new ViewString( "Duplicate order request" );
    private static final String                 DUP_ORDER_STR         = DUP_ORDER.toString();
    private static final ZString                UNKNOWN_ORDER         = new ViewString( "Original order not found" );
    private static final ZString                ORDID_BASE            = new ViewString( "999" );

    private final OrderMap                      _orderMap;
    private       Map<Instrument,SimpleOrderBook> _orderBookMap         = new HashMap<Instrument,SimpleOrderBook>(1024);
    private final Map<ZString,Order>            _mktOrderIdMap;

    private       ReusableStringFactory         _reusableStringFactory;

    private       OrderFactory                  _orderFactory = null;

    private       OrderVersionFactory           _versionFactory;
    
    private       ClientNewOrderAckFactory      _clientNewOrderAckFactory;
    private       ClientCancelledFactory        _clientCancelledFactory;
    private       ClientReplacedFactory         _clientReplacedFactory;
    
    private final EventBuilder                  _eventBuilder; // @NOTE only use in apply method to avoid threading issues

    private       int                           _execId   = 1000000;
    private       ReusableString                _orderId  = new ReusableString();
    private       int                           _ordIdIdx = 1000000;
    private       boolean                       _logOrderInTS;
    private final ReusableString                _logMsg = new ReusableString();
    
    public ExchangeSim( int expectedOrders, EventBuilder eventBuilder ) {
        SuperpoolManager sp = SuperpoolManager.instance();
        
        _eventBuilder  = eventBuilder;
        
        _orderMap      = new SegmentOrderMap( expectedOrders, 0.75f, 1 );
        _mktOrderIdMap = new HashMap<ZString,Order>( expectedOrders );
        
        _clientNewOrderAckFactory   = sp.getFactory( ClientNewOrderAckFactory.class, ClientNewOrderAckImpl.class );
        _clientCancelledFactory     = sp.getFactory( ClientCancelledFactory.class,   ClientCancelledImpl.class );
        _clientReplacedFactory      = sp.getFactory( ClientReplacedFactory.class,    ClientReplacedImpl.class );
        
        _reusableStringFactory      = sp.getFactory(  ReusableStringFactory.class,   ReusableString.class );
        _orderFactory               = sp.getFactory(  OrderFactory.class,   OrderImpl.class );
        _versionFactory             = sp.getFactory(  OrderVersionFactory.class,   OrderVersion.class );
    }

    public boolean isLogOrderInTS() {
        return _logOrderInTS;
    }

    public void setLogOrderInTS( boolean logOrderInTS ) {
        _logOrderInTS = logOrderInTS;
    }

    public Message handle( final Message msg ) {
        Message reply = null;

        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERSINGLE:
            reply = handleNewOrderSingle( (NewOrderSingle) msg );
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            reply = handleCancelReplaceRequest( (CancelReplaceRequest) msg );
            break;
        case EventIds.ID_CANCELREQUEST:
            reply = handleCancelRequest( (CancelRequest) msg );
            break;    
        default:
            break;    
        }
        
        return reply;
    }
    
    private Message handleCancelRequest( final CancelRequest msg ) {
        return applyCancelRequest( msg );
    }

    private Message handleCancelReplaceRequest( final CancelReplaceRequest msg ) {
        return applyCancelReplaceRequest( msg );
    }

    private Message handleNewOrderSingle( final NewOrderSingle msg ) {
        return applyNewOrderSingle( msg );
    }

    public Message applyCancelRequest( final CancelRequest msg ) {
        Order order = _orderMap.get( msg.getClOrdId() );
        
        Message reply = null;
        
        if ( order == null ) {
            order = _orderMap.get(  msg.getOrigClOrdId() );
            
            if ( order != null ) {
                final OrderVersion version = order.getLastAckedVerion();
                
                if ( version.getOrdStatus().getIsTerminal() ) {
                    
                    reply = _eventBuilder.getCancelReject( msg.getClOrdId(), 
                                                                 msg.getOrigClOrdId(), 
                                                                 version.getMarketOrderId(), 
                                                                 DUP_ORDER, 
                                                                 CxlRejReason.Other,
                                                                 CxlRejResponseTo.CancelRequest, 
                                                                 version.getOrdStatus() );
                } else {
                    final ClientCancelledImpl ccld = _clientCancelledFactory.get();
                    
                    double origPx  = version.getMarketPrice(); 
                    
                    OrderRequest nosAmendReq = (OrderRequest) version.getBaseOrderRequest();
                    
                    ccld.getClOrdIdForUpdate().setValue( msg.getClOrdId() );
                    ccld.getOrderIdForUpdate().setValue( version.getMarketOrderId() );
                    ccld.getOrigClOrdIdForUpdate().setValue( msg.getClOrdId() );
                    
                    SimpleOrderBook book = getBook( nosAmendReq.getInstrument() );
                    
                    book.remove( version.getMarketOrderId(), version.getLeavesQty(), origPx, nosAmendReq.getOrdType(), nosAmendReq.getSide() );
                    
                    ccld.setSrcEvent( nosAmendReq );
                    ccld.setAvgPx( version.getAvgPx() );
                    ccld.setCumQty( version.getCumQty() );
                    ccld.setLeavesQty( version.getLeavesQty() );
            
                    ccld.setMessageHandler( msg.getMessageHandler() );
                    ccld.getOrderIdForUpdate().setValue( version.getMarketOrderId() );
                    ccld.setMktCapacity( nosAmendReq.getOrderCapacity() );
            
                    ccld.setExecType( ExecType.Canceled );
            
                    final ReusableString synthAckExecId = ccld.getExecIdForUpdate();
                    synthAckExecId.setValue( EXECID_PREFIX );
                    synthAckExecId.append( msg.getClOrdId() ).append( ++_execId );
                    
                    version.setOrdStatus( OrdStatus.Canceled );
                    ccld.setOrdStatus( OrdStatus.Canceled );

                    reply = ccld;
                    
                    _orderMap.put( _reusableStringFactory.get().copy( msg.getClOrdId() ), order );
                    _mktOrderIdMap.put( _reusableStringFactory.get().copy( ccld.getOrderId() ), order );
                }
            } else {
                final OrdStatus status = OrdStatus.Rejected;
                
                reply = _eventBuilder.getCancelReject( msg.getClOrdId(), 
                                                             msg.getOrigClOrdId(), 
                                                             null, 
                                                             UNKNOWN_ORDER, 
                                                             CxlRejReason.UnknownOrder,
                                                             CxlRejResponseTo.CancelRequest, 
                                                             status );
            }
            
        } else { 
            final OrdStatus    status  = OrdStatus.Rejected;
            final OrderVersion version = order.getLastAckedVerion();
            
            reply = _eventBuilder.getCancelReject( msg.getClOrdId(), 
                                                         msg.getOrigClOrdId(), 
                                                         version.getMarketOrderId(), 
                                                         DUP_ORDER, 
                                                         CxlRejReason.DuplicateClOrdId,
                                                         CxlRejResponseTo.CancelRequest, 
                                                         status );
        }
        
        return reply;
    }

    public Message applyCancelReplaceRequest( CancelReplaceRequest msg ) {

        Order order = _orderMap.get( msg.getClOrdId() );
        
        Message reply = null;
        
        if ( order == null ) {
            order = _orderMap.get(  msg.getOrigClOrdId() );
            
            if ( order != null ) {
                final OrderVersion version = order.getLastAckedVerion();
                
                if ( version.getOrdStatus().getIsTerminal() ) {
                    
                    reply = _eventBuilder.getCancelReject( msg.getClOrdId(), 
                                                                 msg.getOrigClOrdId(), 
                                                                 version.getMarketOrderId(), 
                                                                 DUP_ORDER, 
                                                                 CxlRejReason.Other,
                                                                 CxlRejResponseTo.CancelReplace, 
                                                                 version.getOrdStatus() );
                } else {
                    final ClientReplacedImpl crpd = _clientReplacedFactory.get();
                    
                    double origPx  = version.getMarketPrice(); 
                    int    origQty = version.getOrderQty();
                    
                    enrichVersion( msg, version );
                    version.setOrdStatus( OrdStatus.Replaced );

                    ((ClientCancelReplaceRequestWrite)msg).setInstrument( version.getBaseOrderRequest().getInstrument() );
                    
                    TradeNew trades = amendBook( version.getMarketOrderId(), 
                                                 msg.getInstrument(),
                                                 msg.getOrderQty(),
                                                 msg.getPrice(),
                                                 version.getCumQty(),
                                                 origQty,
                                                 origPx,
                                                 msg.getOrdType(), 
                                                 msg.getSide() );
                    
                    enrich( order, msg.getClOrdId(), trades );
                    
                    version.setOrdStatus( calcOrdStatus( order ) );

                    crpd.attachQueue( trades );
                    
                    crpd.setSrcEvent( msg );
                    crpd.setAvgPx( version.getAvgPx() );
                    crpd.setCumQty( version.getCumQty() );
                    crpd.setLeavesQty( version.getLeavesQty() );
                    crpd.setOrdStatus( version.getOrdStatus() );
            
                    crpd.setMessageHandler( msg.getMessageHandler() );
                    crpd.getOrderIdForUpdate().setValue( version.getMarketOrderId() );
                    crpd.setMktCapacity( msg.getOrderCapacity() );
            
                    crpd.setExecType( ExecType.Replaced );
            
                    final ReusableString synthAckExecId = crpd.getExecIdForUpdate();
                    synthAckExecId.setValue( EXECID_PREFIX );
                    synthAckExecId.append( msg.getClOrdId() ).append( ++_execId );
                    
                    reply = crpd;
                    
                    _orderMap.put( _reusableStringFactory.get().copy( msg.getClOrdId() ), order );
                    _mktOrderIdMap.put( _reusableStringFactory.get().copy( crpd.getOrderId() ), order );
                }
            } else {
                final OrdStatus status = OrdStatus.Rejected;
                
                reply = _eventBuilder.getCancelReject( msg.getClOrdId(), 
                                                             msg.getOrigClOrdId(), 
                                                             null, 
                                                             UNKNOWN_ORDER, 
                                                             CxlRejReason.UnknownOrder,
                                                             CxlRejResponseTo.CancelReplace, 
                                                             status );
            }
            
        } else { 
            final OrdStatus status = OrdStatus.Rejected;
            
            reply = _eventBuilder.getCancelReject( msg.getClOrdId(), 
                                                         msg.getOrigClOrdId(), 
                                                         order.getLastAckedVerion().getMarketOrderId(), 
                                                         DUP_ORDER, 
                                                         CxlRejReason.DuplicateClOrdId,
                                                         CxlRejResponseTo.CancelReplace, 
                                                         status );
        }
        
        return reply;
    }

    public final Message applyNewOrderSingle( final NewOrderSingle src ) {
        
        final ViewString clOrdId = src.getClOrdId(); 

        if ( _orderMap.containsKey( clOrdId ) == false ) {
            return processNewOrder( src );
        }
        
        return makeNOSReject( clOrdId );
    }

    private Message makeNOSReject( final ViewString clOrdId ) {
        return _eventBuilder.getNOSReject( clOrdId, OrdStatus.Rejected, DUP_ORDER_STR, null );
    }

    private final ClientNewOrderAckImpl processNewOrder( final NewOrderSingle src ) {
        final OrderImpl  order   = createOrder( src );
        final TradeNew   trades  = addToBook( src, order );
        
        return registerVersionAndAck( src, order, trades );
    }

    private ClientNewOrderAckImpl registerVersionAndAck( final NewOrderSingle src, final OrderImpl order, final TradeNew trades ) {
        final ViewString clOrdId = src.getClOrdId(); 

        enrich( order, clOrdId, trades );

        final OrderVersion          version = registerOrderVersion( src, order );
        final ClientNewOrderAckImpl cack    = makeReplyNewAck( src, order, trades, version );

        registerAckOrderId( order, cack );
        
        return cack;
    }

    private OrderVersion registerOrderVersion( final NewOrderSingle src, final Order order ) {
        final OrderVersion version = order.getLastAckedVerion();
        version.setBaseOrderRequest( src );
        version.setOrdStatus( calcOrdStatus( order ) );
        return version;
    }

    private void registerAckOrderId( Order order, final ClientNewOrderAckImpl cack ) {
        final ReusableString copyOrderId = _reusableStringFactory.get();
        copyOrderId.copy( cack.getOrderId() );
        _mktOrderIdMap.put( copyOrderId, order );
    }

    private ClientNewOrderAckImpl makeReplyNewAck( final NewOrderSingle src, Order order, final TradeNew trades, final OrderVersion version ) {
        final ClientNewOrderAckImpl cack = _clientNewOrderAckFactory.get();
        cack.attachQueue( trades );
        
        cack.setSrcEvent( src );
        cack.setAvgPx( version.getAvgPx() );
        cack.setCumQty( version.getCumQty() );
        cack.setLeavesQty( version.getLeavesQty() );
        cack.setOrdStatus( version.getOrdStatus() );
   
        cack.setMessageHandler( src.getMessageHandler() );
        cack.getOrderIdForUpdate().setValue( version.getMarketOrderId() );
        cack.setMktCapacity( src.getOrderCapacity() );
   
        cack.setExecType( ExecType.New );
        
        src.setOrderSent( src.getOrderReceived() );
        cack.setAckReceived( src.getOrderReceived() );
   
        final ReusableString synthAckExecId = cack.getExecIdForUpdate();
        synthAckExecId.setValue( EXECID_PREFIX );
        synthAckExecId.append( src.getClOrdId() ).append( ++_execId );
        return cack;
    }

    private OrdStatus calcOrdStatus( final Order order ) {
        final OrderVersion v = order.getLastAckedVerion();
        
        if ( v.getCumQty() == 0 )    return OrdStatus.New;
        if ( v.getLeavesQty() == 0 ) return OrdStatus.Filled;

        return OrdStatus.PartiallyFilled;
    }

    private void enrich( final Order order, final ZString clOrdId, final TradeNew trades ) {

        if ( trades == null ) return;
        
        TradeNew curTrade = trades;
        
        final OrderVersion ver = order.getLastAckedVerion();
        
        int    cumQty    = ver.getCumQty(); 
        double total     = ver.getAvgPx() * cumQty;
        double avPx      = total / cumQty;
        int    ordQty    = ver.getOrderQty();
        int    leavesQty = ordQty - cumQty;

        while( curTrade != null ) {
            MarketTradeNewWrite trade = (MarketTradeNewWrite) curTrade;
            
            trade.getClOrdIdForUpdate().copy( clOrdId );

            int    lastQty = trade.getLastQty();
            double lastPx  = trade.getLastPx();
            
            total  += (lastPx * lastQty);
            cumQty += lastQty; 
            avPx    = total / cumQty;

            leavesQty = ver.getOrderQty() - cumQty;
            if ( leavesQty <= 0 ) leavesQty = 0;
            
            trade.setAvgPx( avPx );
            trade.setCumQty( cumQty );
            trade.setLeavesQty( leavesQty );
            
            curTrade = (TradeNew) curTrade.getNextQueueEntry();
        }

        ver.setAvgPx( avPx );
        ver.setCumQty( cumQty );
    }

    private TradeNew addToBook( final NewOrderSingle src, final Order order ) { 
        
        final ZString    mktOrdId   = order.getLastAckedVerion().getMarketOrderId();
        final Instrument instrument = src.getInstrument();
        final int        orderQty   = src.getOrderQty();
        final double     price      = src.getPrice(); 
        final OrdType    ordType    = src.getOrdType();
        final Side       side       = src.getSide();
        
        final SimpleOrderBook book = getBook( instrument );
        
        return book.add( mktOrdId, orderQty, price, ordType, side );
    }

    private TradeNew amendBook( final ZString    marketOrderId, 
                                final Instrument instrument,
                                final int        newQty,
                                final double     newPrice, 
                                final int        cumQty,
                                final int        origQty, 
                                final double     origPrice, 
                                final OrdType    ordType, 
                                final Side       side ) {
        
        final SimpleOrderBook book = getBook( instrument );
        
        return book.amend( marketOrderId, newQty, origQty, cumQty, newPrice, origPrice, ordType, side );
    }

    private SimpleOrderBook getBook( final Instrument instrument ) {
        SimpleOrderBook book = _orderBookMap.get( instrument );
     
        if ( book == null ) {
            book = new SimpleOrderBook();
            
            _orderBookMap.put( instrument, book );
        }
        
        return book;
    }

    private OrderImpl createOrder( final NewOrderSingle msg ) {
        final OMClientProfile clientProf = (OMClientProfile) msg.getClient();
        final OrderImpl order = mkOrder( msg, clientProf );
        
        final ViewString clOrdId = msg.getClOrdId();
        
        registerOrderInMap( order, clOrdId );
        
        if ( _logOrderInTS ) {
            _logMsg.reset();
            _logMsg.append( "NOS_clOrdId " ).append( clOrdId ).append( ' ' ).append( (msg.getOrderReceived()>>10) );
            _log.info( _logMsg );
        }
        
        return order;
    }
    
    private OrderImpl mkOrder( final NewOrderSingle msg, final OMClientProfile clientProf ) {
        final OrderImpl    order  = _orderFactory.get();
        final OrderVersion ver    = _versionFactory.get();

        ver.setMarketOrderId( nextOrderId() );
        enrichVersion( msg, ver );
        ver.setOrdStatus( OrdStatus.New );
        
        order.setLastAckedVerion( ver );
        order.setPendingVersion( ver );
        
        order.setClientProfile( clientProf );
        return order;
    }
    
    private void registerOrderInMap( final OrderImpl order, final ViewString clOrdId ) {
        final ReusableString copyId = _reusableStringFactory.get();
        copyId.copy( clOrdId );
        _orderMap.put( copyId, order );
    }

    private ZString nextOrderId() {
        _orderId.copy( ORDID_BASE ).append( ++_ordIdIdx );
        
        return _orderId;
    }

    private void enrichVersion( OrderRequest msg, OrderVersion ver ) {
        double price = msg.getPrice();

        ver.setBaseOrderRequest( msg );
        ver.setOrderQty( msg.getOrderQty() );
        ver.setMarketPrice( price );
    }
}
