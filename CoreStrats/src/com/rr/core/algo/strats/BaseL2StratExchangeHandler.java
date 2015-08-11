/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.strats;

import com.rr.core.algo.strats.Strategy.ExchangeHandler;
import com.rr.core.algo.strats.ordstate.BaseStratOrdState;
import com.rr.core.collections.MessageQueue;
import com.rr.core.collections.RingBufferMsgQueue1C1P;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.session.Session;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.impl.RecoveryCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.CommonExchangeHeader;
import com.rr.model.generated.internal.events.interfaces.DoneForDay;
import com.rr.model.generated.internal.events.interfaces.Expired;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.Restated;
import com.rr.model.generated.internal.events.interfaces.Stopped;
import com.rr.model.generated.internal.events.interfaces.Suspended;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;


public class BaseL2StratExchangeHandler<T extends Book> implements ExchangeHandler<T> {

    private static final Logger                  _log = LoggerFactory.create( BaseL2StratExchangeHandler.class );

    private static final ErrorCode               UNKNOWN_ORDER = new ErrorCode( "SEH100", "Exec if for Unknown order" );
    
    private final String                         _id;
    private final int                            _idx;
    private final BaseStrategy<T>                _strategy;
    private final StratInstrumentStateWrapper<T> _statInst;
    private final MessageQueue                   _queue;
    private final boolean                        _trace;
    private final int                            _queueCapacity;
    private       T                              _book = null;
    private       int                            _currQSize;
    private       int                            _nosCnt;

    /**
     * current order for this book/exchange handler
     * when more than one active order used, the previous current order is pushed into map
     */
    private       StratOrder                     _curOrder;
    
    /**
     * count of the number of order entries for this book/exchange handler in the order map
     * used to avoid map lookups if not necessary
     */
    private       int                            _ordersInMap = 0;
    
    /**
     * logMsg only to be used on doWork control thread
     */
    private final ReusableString                _traceMsg = new ReusableString();

    /**
     * exWarnMsg only to be used on the exchange thread callback !
     */
    private final ReusableString                _exWarnMsg = new ReusableString();


    public BaseL2StratExchangeHandler( String                           id,
                                       int                              idx, 
                                       BaseStrategy<T>                  baseL2FixedBookStrategy, 
                                       StratInstrumentStateWrapper<T>   statInst,
                                       int                              queueSize,
                                       boolean                          trace ) {
        _id         = id;
        _idx        = idx;
        _strategy   = baseL2FixedBookStrategy;
        _statInst   = statInst;
        _queue      = new RingBufferMsgQueue1C1P( queueSize );
        _trace      = trace;
        
        _queueCapacity = queueSize;
    }

    @Override
    public final NewOrderSingle makeNOS( ZString exDest, OrdType ordType, TimeInForce tif, int qty, double price, Side side, long tickNanoTS, long tickId ) {
        final RecoveryNewOrderSingleImpl nos = _strategy.getNosPoolFactory().get();
        
        final Instrument inst = _statInst.getInstrument();
        
        nos.setInstrument( inst );

        nos.setSide( side );
        nos.getAccountForUpdate().setValue( _strategy.getAccount() );
        nos.setOrdType( ordType );
        nos.setHandlInst( HandlInst.AutoExecPrivate );
        nos.setOrderReceived( tickNanoTS );
        nos.setOrderQty( qty );
        nos.setPrice( price );
        nos.setTimeInForce( tif );

        final long intInstId = inst.getLongSymbol();
        nos.getClOrdIdForUpdate().append( intInstId ).append( '_' ).append( tickId ).append( '_' ).append( ++_nosCnt );
        
        nos.getSymbolForUpdate().append( inst.getExchangeSymbol() );
        nos.getExDestForUpdate().copy( exDest );
        
        final Session sess = (Session) _strategy.getOrderRouter().getRoute( nos, this );
        nos.setMessageHandler( sess );
        
        /**
         * create new order, register in map with appropriate state
         * could optimise out the map and use a single order state but it will have edge risks
         */
        StratOrder order = getEmptyOrder();
        order.set( nos.getClOrdId(), side, qty, price );
        order.setState( _strategy.getStratOrdStates()._pendingNewState );
        storeOrder( order );
        
        return nos;
    }

    private StratOrder getEmptyOrder() {
        if ( _curOrder != null ) {
            if ( _curOrder.hasPerformedTerminalWork() ) {
                // current order has been reset so can be reused
                
                _curOrder.reset();
                
                return _curOrder;
            }
        }
        
        return _strategy.getStratOrderPoolFactory().get();
    }

    private void storeOrder( StratOrder order ) {

        if ( _curOrder != null && ! _curOrder.isInMap() ) {
            // push current active order into map
            
            _curOrder.setInMap( true );

            final ReusableString clOrdId = _curOrder.getClOrdId();
            
            _strategy.getOrderMap().put( clOrdId, _curOrder );
            
            mapOrderIdToOrder( _curOrder );
            
            ++_ordersInMap;
        }
        
        _curOrder = order;
    }

    private void mapOrderIdToOrder( StratOrder order ) {
        if ( _curOrder.isInMap() ) {
            final ReusableString orderId = _curOrder.getOrderId();
            
            if ( orderId.length() > 0 ) {
                _strategy.getOrderMap().put( orderId, order );
    
                // link the orderId to the clOrdId .. so when order terminal event comes in can remove
                // clOrdId  link from map
                
                final ReusableString clOrdId = _curOrder.getClOrdId();
                
                orderId.setNext( clOrdId );
            }
        }
    }

    @Override
    public void enqueueCancel( Side side, ZString clOrdId, ZString orderId, MessageHandler dest ) {
        final RecoveryCancelRequestImpl cancel = _strategy.getCancelPoolFactory().get();
        
        final Instrument inst = _statInst.getInstrument();
        
        cancel.setInstrument( inst );

        cancel.setSide( Side.Buy );
        cancel.getAccountForUpdate().setValue( _strategy.getAccount() );

        cancel.getOrigClOrdIdForUpdate().copy( clOrdId );
        cancel.getClOrdIdForUpdate().copy( clOrdId ).append( "-CXL" );

        if ( orderId == null ) {
            StratOrder order = findOrder( clOrdId );
            
            if ( order != null ) orderId = order.getOrderId();
        }

        cancel.getOrderIdForUpdate().copy( orderId );
        
        cancel.getSymbolForUpdate().append( inst.getExchangeSymbol() );
        
        if ( dest == null ) {
            dest = _strategy.getOrderRouter().getAllRoutes()[ 0 ];
        }
        
        cancel.setMessageHandler( dest );
        
        _strategy.enqueueForDownDispatch( cancel );
    }

    @Override
    public T getBook() {
        return _book;
    }

    @Override
    public BaseStrategy<T> getStrategy() {
        return _strategy;
    }

    @Override 
    public int getIndex() {
        return _idx;
    }
    
    @Override
    public StratInstrumentStateWrapper<T> getStratInstState() {
        return _statInst;
    }

    @Override
    public void threadedInit() {
        /**
         * DO NOT USE THIS WILL NOT BE CALLED AS THIS STRATEGY HAS THE REAL THREADED INIT
         * THIS HANDLER OPERATES UNDER THE STRATEGY THREAD
         */
    }

    @Override
    public ReusableString getTraceMsg() {
        return _traceMsg;
    }

    @Override
    public void handle( Message msg ) {
        final int qSize = _queue.size();
        
        if ( qSize+1 == _queueCapacity ) {
            _exWarnMsg.copy( "ABOUT TO BLOCK EXCHANGE THREAD AS QFULL for " );
            _exWarnMsg.append( _id ).append( ", queueSize=" + qSize );
            _log.warn( "" );
        }

        _queue.add( msg );
    }

    /**
     * not intended to be used as queue is consumed via multiplexor control thread using doWorkUnit()
     */
    @Override
    public void handleNow( Message msg ) {
        applyEvent( msg );
    }

    @Override
    public boolean canHandle() {
        return true;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void doWorkUnit() {
        if ( _trace ) {
            _currQSize = _queue.size();
        }
        
        Message m  = _queue.poll();
        
        while( m != null ) {
            applyEvent( m );
            
            if ( _trace ) {
                _currQSize = _queue.size();
            }
            
            m = _queue.poll();
        }
    }

    @Override
    public int getCurrQSize() {
        return _currQSize;
    }

    @Override
    public void setBook( T book ) {
        _book = book;
    }

    @Override
    public boolean isTrace() {
        return _trace;
    }
    
    private void applyEvent( Message msg ) {

        StratOrder order = null; 
        
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERACK: 
        {
            NewOrderAck exec = (NewOrderAck) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleNewOrderAck( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_TRADENEW:
        {
            TradeNew exec = (TradeNew) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            BaseStratOrdState.traceLogEvent( order, _statInst, exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.fill( exec.getLastQty(), exec.getLastPx(), exec.getCumQty(), exec.getAvgPx() );
                order.getState().handleTradeNew( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_CANCELREJECT:
        {
            CancelReject exec = (CancelReject) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleCancelReject( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_REJECTED:
        {
            Rejected exec = (Rejected) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            BaseStratOrdState.traceLogEvent( order, _statInst, exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleRejected( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_CANCELLED:
        {
            Cancelled exec = (Cancelled) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleCancelled( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_REPLACED:
        {
            Replaced exec = (Replaced) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleReplaced( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_DONEFORDAY:
        {
            DoneForDay exec = (DoneForDay) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleDoneForDay( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_STOPPED:
        {
            Stopped exec = (Stopped) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleStopped( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_EXPIRED:
        {
            Expired exec = (Expired) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleExpired( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_SUSPENDED:
        {
            Suspended exec = (Suspended) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleSuspended( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_RESTATED:
        {
            Restated exec = (Restated) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            BaseStratOrdState.traceLogEvent( order, _statInst, exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleRestated( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_TRADECORRECT:
        {
            TradeCorrect exec = (TradeCorrect) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            BaseStratOrdState.traceLogEvent( order, _statInst, exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.fill( exec.getLastQty(), exec.getLastPx(), exec.getCumQty(), exec.getAvgPx() );
                order.getState().handleTradeCorrect( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_TRADECANCEL:
        {
            TradeCancel exec = (TradeCancel) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            BaseStratOrdState.traceLogEvent( order, _statInst, exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.fill( exec.getLastQty(), exec.getLastPx(), exec.getCumQty(), exec.getAvgPx() );
                order.getState().handleTradeCancel( order, _statInst, exec );
                checkTerminalAction( order, exec.getOrdStatus() );
            }
            break;
        }
        case EventIds.ID_VAGUEORDERREJECT:
        {
            VagueOrderReject exec = (VagueOrderReject) msg;
            order = findOrder( exec.getClOrdId(), exec.getOrderId(), exec );
            if ( order != null ) {
                linkOrderId( order, exec.getOrderId() );
                order.getState().handleVagueReject( order, _statInst, exec );
                checkTerminalAction( order, OrdStatus.Rejected );
            }
            break;
        }
        case EventIds.ID_FORCECANCEL: // @TODO implement support for admin based force cancel events
            break;
        case EventIds.ID_ORDERSTATUS:
        case EventIds.ID_NEWORDERSINGLE:
        case EventIds.ID_CANCELREPLACEREQUEST:
        case EventIds.ID_CANCELREQUEST:
            break;
        default:
            break;
        }

        // CANNOT REFERENCE ORDER ANYMORE AS MAY OF BEEN RECYCLED
        
        _strategy.enqueueForHubDispatch( msg );
    }

    private void checkTerminalAction( StratOrder order, OrdStatus ordStatus ) {
        if ( ordStatus.getIsTerminal() && ! order.hasPerformedTerminalWork() ) {
            
            _statInst.orderTerminal( order );
            
            if ( order.isInMap() ) {
                _strategy.getOrderMap().remove( order.getClOrdId() );
                
                if ( order != _curOrder ) {
                    _strategy.getStratOrderRecycler().recycle( order );
                    
                    return;
                }
            }

            order.setState( _strategy.getStratOrdStates()._terminalState );
            order.setHasPerformedTerminalWork( true );
            
            _strategy.setMarketOrderTerminalEvent( _statInst );
        }
    }

    private void linkOrderId( StratOrder ord, ZString orderId ) {
        if ( ord != null ) {
            ord.setOrderId( orderId );
            
            mapOrderIdToOrder( ord );
        }
    }

    private StratOrder findOrder( ZString clOrdId ) {
        if ( _curOrder != null ) {
            if ( clOrdId.length() > 0 ) {
                if ( _curOrder.getClOrdId().equals( clOrdId ) ) {
                    return _curOrder;
                }
            }
        }
        
        StratOrder ord = _strategy.getOrderMap().get( clOrdId );
        
        if ( ord == null ) {
            _exWarnMsg.copy( "Unable to find order " ).append( clOrdId );

            _log.error( UNKNOWN_ORDER, _exWarnMsg );
        }
        
        return ord;
    }

    private StratOrder findOrder( ZString clOrdId, ZString orderId, CommonExchangeHeader exec ) {
        if ( _curOrder != null ) {
            if ( clOrdId.length() > 0 ) {
                if ( _curOrder.getClOrdId().equals( clOrdId ) ) {
                    return _curOrder;
                }
            }
            if ( orderId.length() > 0 ) {
                if ( _curOrder.getOrderId().equals( orderId ) ) {
                    return _curOrder;
                }
            }
        }
        
        StratOrder ord = _strategy.getOrderMap().get( clOrdId );
        
        if ( ord == null ) {
            ord = _strategy.getOrderMap().get( orderId );
        }
        
        if ( ord == null ) {
            _exWarnMsg.copy( "Unable to process execution report for unknown order" );

            exec.dump( _exWarnMsg );
            
            _log.error( UNKNOWN_ORDER, _exWarnMsg );
        }
        
        return ord;
    }

}
