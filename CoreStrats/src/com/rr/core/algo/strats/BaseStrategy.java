/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.strats;

import java.util.ArrayList;
import java.util.List;

import com.rr.core.algo.base.StrategyDefinition;
import com.rr.core.algo.strats.BaseAlgo.StratOrdStates;
import com.rr.core.collections.SMTHashMap;
import com.rr.core.collections.SMTMap;
import com.rr.core.component.SMTStartContext;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.book.UnsafeBookCache;
import com.rr.core.model.book.UnsafeL1Book;
import com.rr.core.pool.PoolFactory;
import com.rr.core.pool.Recycler;
import com.rr.core.session.NullThrottler;
import com.rr.core.session.Session;
import com.rr.core.session.Throttler;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.Utils;
import com.rr.md.book.BookSourceManager;
import com.rr.md.book.BookSubscriptionMgr;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.RecoveryCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.StratInstrumentStateImpl;
import com.rr.model.generated.internal.events.impl.StrategyStateImpl;
import com.rr.model.generated.internal.events.interfaces.StrategyStateWrite;
import com.rr.om.router.OrderRouter;

/**
 * @WARNING each instance of a strategy must only have one thread invoking the doWork method else recycling errors will occur
 * 
 * control thread multiplexor invokes doWorkUnit() to processs outstanding events, as events are processed market events are enqueued 
 * in the postEventProcessing method the pending downstream events are "committed" and sent via order router
 * if any events were sent downstream then snap the state of the strategy and send to hub 
 * 
 * This minimises impact of this state logging to after the business event is fully processed
 */

public abstract class BaseStrategy<T extends Book> implements Strategy<T> {
    private static final Logger              _sharedLogger = LoggerFactory.create( BaseStrategy.class );

    private static final double              DEFAULT_PNL_CUTOUT_THRESHOLD = -10000;

    private       Logger                     _log = _sharedLogger;
    
    protected final ReusableString           _logMsg = new ReusableString();
    
    private boolean                          _tradingAllowed = false;
    
    @SuppressWarnings( "unchecked" )
    private StratBookAdapter<T>[]            _mdHandlers = new StratBookAdapter[0];
    @SuppressWarnings( "unchecked" )
    private ExchangeHandler<T>[]             _exchangeHandlers = new ExchangeHandler[0];

    
    private String                           _id;
    private StratRunState                    _runState = StratRunState.Initial;
    private List<StatusChanged>              _listeners = new ArrayList<StatusChanged>(); // only changed during bootstrap
    private EventRecycler                    _recycler; // the recycler for the thread
    private Message                          _firstDownEvent = null;
    private Message                          _lastDownEvent  = null;

    private Message                          _firstHubEvent = null;
    private Message                          _lastHubEvent  = null;

    private int                              _exchangeEventQueueSize = 128;
    private int                              _bookLevels = 10;
    private boolean                          _trace = false;
    private ZString                          _account = new ViewString( "DEFAULTACC" );
    private int                              _minOrderQty = 1;
    private int                              _maxOrderQty = 1;
    private int                              _maxSlices   = Integer.MAX_VALUE;
    private double                           _pnlCutoffThreshold = DEFAULT_PNL_CUTOUT_THRESHOLD;

    private Algo<?>                          _algo;        // owning algo
    private StrategyDefinition               _stratDef;    // configured details for this strategy
    private OrderRouter                      _orderRouter; // shared order router

    private Session                          _hubSession;  // hub session

    /**
     * strategy instrument state
     */
    @SuppressWarnings( "unchecked" )
    private StratInstrumentStateWrapper<T>[] _instState = new StratInstrumentStateWrapper[0];

    /**
     * strategy summary state
     */
    protected final StrategyStateWrite       _summaryState = new StrategyStateImpl();
    
    /**
     * members that shared across all strategies (stateless)
     */
    private StratOrdStates                   _stratOrdStates;
    
    /**
     * members that need to be shared in same thread via TLC
     */
    private PoolFactory<RecoveryNewOrderSingleImpl> _nosPool;
    private PoolFactory<RecoveryCancelRequestImpl>  _cancelPool;
    private PoolFactory<StrategyStateImpl>          _strategyStatePool;
    private PoolFactory<StratInstrumentStateImpl>   _stratInstStatePool;

    private PoolFactory<StratOrder>                 _stratOrderFactory;
    private Recycler<StratOrder>                    _stratOrderRecycler;

    private SMTMap<ZString, StratOrder>             _map;

    /**
     * expected 1 active order per strat leg
     * strat order map shared across strats in SAME thread
     */
    private int                                     _stratOrderMapSize = 16;

    private Throttler                               _throttler         = new NullThrottler();
    private long                                    _throttleCount;
    private boolean                                 _logThrottled;
    private boolean                                 _sendStateSnapshot;



    /**
     * @param id component id
     */
    public BaseStrategy( String id ) {
        _id = id;
    }
    
    /**
     * perform next unit of work 
     */
    @Override
    public final void doWorkUnit() {
        doAllWork();            
        postEventProcessing();
    }

    public final ZString getAccount() {
        return _account;
    }

    @Override
    public final Algo<?> getAlgo() {
        return _algo;
    }

    public final int getBookLevels() {
        return _bookLevels;
    }
    
    @Override
    public final String getComponentId() {
        return _id;
    }

    @Override
    public final ExchangeHandler<T>[] getExchangeHandlers() {
        return _exchangeHandlers;
    }

    @Override
    public final StratBookAdapter<T>[] getMarketDataHandlers() {
        return _mdHandlers;
    }

    @Override
    public final StrategyDefinition getStrategyDefinition() {
        return _stratDef;
    }
    
    @Override
    public final StrategyStateWrite getStrategyState() {
        return _summaryState;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public void init( SMTStartContext ctx ) {
        
        validateProps();
        BookSourceManager<T> bookMgr = (BookSourceManager<T>) ((StratContext)ctx).getBookSrcMgr();
        
        int instIdx = 0;
        
        for ( Instrument inst : _stratDef.getInsts() ) {

            // GC is after prepare so dont worry about allocs
            // instrument state for strategy
            StratInstrumentStateImpl instState = new StratInstrumentStateImpl();
            instState.setInstrument( inst );
            
            StratInstrumentStateWrapper<T> instStateWrapper = new StratInstrumentStateWrapper<T>( instState, instIdx++ );
            addInstState( instStateWrapper );

            ExchangeHandler<T> exh = createExchangeHandler( getComponentId() + "_EXH_" + inst, _exchangeHandlers.length, instStateWrapper );
            addExchangeDataHandlerToList( exh );

            BookSubscriptionMgr<T> src = bookMgr.findSubscriptionManager( inst, _stratDef.getRequestedPipeLineId() );
            
            StratBookAdapter<T> mdh = createBookListener( getComponentId() + "_MDH_" + inst, _mdHandlers.length, instStateWrapper );
            addMarketDataHandlerToList( mdh );
            
            _log.info( getComponentId()             + 
                       " subscribing to "           + inst.getSecurityDesc() + 
                       ", via BookSubscriptionMgr " + src.getComponentId() + 
                       ", pipeLineId="              + _stratDef.getRequestedPipeLineId() );
            
            T book = src.subscribe( mdh, inst );
            
            // set the book and a TEMP snap book, pending threaded init for the real shared book
            instStateWrapper.setBook( book, new UnsafeL1Book( inst ) ); 
            mdh.setBook( book );
            exh.setBook( book );
            
            instStateWrapper.setBookHandler( mdh );
            instStateWrapper.setExchangeHandler( exh );
        }
        
        _summaryState.setNoInstEntries( instIdx );
    }

    @Override
    public String toString() {
        return _summaryState.toString();
    }

    /**
     * strictly speaking should use a sync lock to get latest value
     * but this is invoked from multiplexor and it has already had a read/write barrier against work queue
     * 
     * @return true if strategy is in run state
     */
    @Override
    public final boolean isActive() {
        return( getRunState().isActiveWorkerState() );
    }

    public final boolean isTrace() {
        return _trace;
    }
    
    @Override
    public final void setTrace( boolean trace ) {
        _trace = trace;
    }

    /**
     * invoked after market data or exchange handlers have updated strategy state
     */
    public abstract void postEventProcessing();

    @Override
    public void prepare() {
        // nothing 
    }
    
    /**
     * ONLY FOR INVOCATION BY THE CONTROL THREAD THAT OWNS THIS STRATEGY
     */
    public final void recycle( Message msg ) {
        _recycler.recycle( msg );
    }

    @Override
    public final void registerListener( StatusChanged callback ) {
        if ( ! _listeners.contains( callback ) ) {
            _listeners.add( callback );
        }
    }

    @Override
    public void setAccount( ZString account ) {
        _account = account;
    }

    @Override
    public final void setAlgo( Algo<?> algo ) {
        _algo = algo;
    }

    @Override
    public final void setBookLevels( int bookLevels ) {
        _bookLevels = bookLevels;
    }
    
    @Override
    public final void setHubSession( Session hubSession ) {
        _hubSession = hubSession;
    }
    
    /**
     * set the router component that sits between strategy and exchange
     *  
     * @param exchangeRouter
     */
    @Override
    public final void setOrderRouter( OrderRouter exchangeRouter ) {
        _orderRouter = exchangeRouter;
    }

    @Override
    public final void setStrategyDefinition( StrategyDefinition def ) {
        _stratDef = def;
        
        ReflectUtils.setProperties( this, def.getProps() );
    }

    @Override
    public final void startWork() {
        setRunState( StratRunState.Running );
        for( int i=0 ; i < _listeners.size() ; i++ ) {
            _listeners.get(i).isActive( true );
        }
    }
    
    @Override
    public final void stop() {
        stopWork();
    }

    @Override
    public final void stopWork() {
        setRunState( StratRunState.FullyPaused );
        for( int i=0 ; i < _listeners.size() ; i++ ) {
            _listeners.get(i).isActive( false );
        }
    }
    
    @SuppressWarnings( "unchecked" )
    @Override
    public void threadedInit() {
        _logMsg.copy( "threadedInit " ).append( getComponentId() );
        
        _log.info( _logMsg );
        
        _recycler   = TLC.instance().getInstanceOf( EventRecycler.class );
        _nosPool    = TLC.instance().getPoolFactory( RecoveryNewOrderSingleImpl.class );
        _cancelPool = TLC.instance().getPoolFactory( RecoveryCancelRequestImpl.class );
        
        _strategyStatePool  = TLC.instance().getPoolFactory( StrategyStateImpl.class );
        _stratInstStatePool = TLC.instance().getPoolFactory( StratInstrumentStateImpl.class );

        _stratOrderFactory  = TLC.instance().getPoolFactory( StratOrder.class );
        _stratOrderRecycler = TLC.instance().getPoolRecycler( StratOrder.class );

        _log = LoggerFactory.getThreadLocal( this.getClass() );

        Class<?>[] pClass = { int.class }; 
        @SuppressWarnings( "boxing" )
        Object[]   pArgs  = { _stratOrderMapSize };
        
        _map = TLC.instance().getInstanceOf( _id, SMTHashMap.class, pClass, pArgs );

        UnsafeBookCache bookCache = TLC.instance().getInstanceOf( UnsafeBookCache.class );

        /**
         * all strats on same thread can share the snap book, this will massively reduce contention
         */
        for( int i=0 ; i < _instState.length ; i++ ) {
            final StratInstrumentStateWrapper<T> isw = _instState[ i ];
            
            isw.setSnapBook( bookCache.get( isw.getInstrument(), _bookLevels ) );
            
            isw.getBook().getContext().getBidBookReserver().attachReserveWorkerThread( Thread.currentThread() );
            isw.getBook().getContext().getAskBookReserver().attachReserveWorkerThread( Thread.currentThread() );
        }
    }

    @Override
    public final void setStratOrdStates( StratOrdStates stratOrdStates ) {
        _stratOrdStates = stratOrdStates;
    }
    
    @Override
    public final StratOrdStates getStratOrdStates() {
        return _stratOrdStates;
    }

    @Override
    public final StratRunState getRunState() {
        /**
         * not synchronised on purpose as dont want another read barrier hit PER strat in multiplexor
         */
        return _runState;
    }
    
    @Override
    public final synchronized StratRunState setRunState( StratRunState state ) {
        StratRunState old = _runState;
        _runState = state;
        return old;
    }
    
    /**
     * @return logger for logging messages in current control thread
     */
    @Override
    public final Logger getLogger() {
        return _log;
    }
    
    @Override
    public final void setThrottler( Throttler t ) {
        _throttler = t;
    }
    
    @Override
    public final boolean isTradingAllowed() {
        return _tradingAllowed;
    }

    @Override
    public final void setTradingAllowed( boolean tradingAllowed ) {
        _tradingAllowed = tradingAllowed;
    }

    @Override
    public final void setMaxOrderQty( int maxOrderQty ) {
        _maxOrderQty = maxOrderQty;
    }

    @Override
    public final void setMinOrderQty( int minOrderQty ) {
        _minOrderQty = minOrderQty;
    }

    @Override
    public final void setMaxSlices( int maxSlices ) {
        _maxSlices = maxSlices;
    }

    @Override
    public final void setPnlCutoffThreshold( double pnlCutoffThreshold ) {
        _pnlCutoffThreshold = pnlCutoffThreshold;
    }

    public final SMTMap<ZString,StratOrder> getOrderMap() {
        return _map;
    }

    public final StratInstrumentStateWrapper<T>[] getInstState() {
        return _instState;
    }

    public final int getMaxSlices() {
        return _maxSlices;
    }

    public final double getPnlCutoffThreshold() {
        return _pnlCutoffThreshold;
    }

    public final int getMaxOrderQty() {
        return _maxOrderQty;
    }

    public final int getMinOrderQty() {
        return _minOrderQty;
    }

    protected final void dispatchQueued() {
        if ( sendEnqueuedDownstream() > 0 ) {
            /**
             * avoid latency impact on strategy processing by snapping the strategy state and enqueing to hub AFTER sending messages to exchange
             */
            setSendStateSnapshotToHub();
        }
        generateStateSnapshot();
        sendEnqueuedHub();
    }

    protected final void setSendStateSnapshotToHub() {
        _sendStateSnapshot = true;
    }

    /**
     * create a handler to handle book change notifications
     * as each book has its own handler saves lookup on book
     * @param id
     * @param idx
     * @param state
     * @return
     */
    protected abstract StratBookAdapter<T> createBookListener( String id, int idx, StratInstrumentStateWrapper<T> state  );

    protected ExchangeHandler<T> createExchangeHandler( String id, int idx, StratInstrumentStateWrapper<T> state ) {
        return new BaseL2StratExchangeHandler<T>( id, idx, this, state, _exchangeEventQueueSize, _trace );
    }
    
    protected final boolean mustThrottle( final long time ) {
        final boolean throttled = _throttler.throttled( time );
        
        if ( throttled ) {
            ++_throttleCount;
            
            if ( _logThrottled ) {
                _logMsg.copy( "Strategy " ).append( getComponentId() ).append( " hit throttle limit so didnt try and trade, throttleCount=" ).append( _throttleCount );
            
                _log.info( _logMsg );
                
                _logThrottled = true;
            }
        } else {
            _logThrottled = false;
        }
        
        return throttled;
    }
    
    @Override
    public final void enqueueForDownDispatch( Message msg ) {
        if ( _firstDownEvent == null ) {
            _firstDownEvent = _lastDownEvent = msg;
        } else {
            _lastDownEvent.attachQueue( msg );
            _lastDownEvent = msg;
        }
    }
    
    protected final void enqueueForHubDispatch( Message msg ) {
        if ( _firstHubEvent == null ) {
            _firstHubEvent = _lastHubEvent = msg;
        } else {
            _lastHubEvent.attachQueue( msg );
            _lastHubEvent = msg;
        }
    }
    
    protected final PoolFactory<StrategyStateImpl> getStrategyStatePoolFactory() {
        return _strategyStatePool;
    }

    protected final Recycler<StratOrder> getStratOrderRecycler() {
        return _stratOrderRecycler;
    }
    
    protected final PoolFactory<StratInstrumentStateImpl> getStratInstrumentStatePoolFactory() {
        return _stratInstStatePool;
    }

    protected final PoolFactory<StratOrder> getStratOrderPoolFactory() {
        return _stratOrderFactory;
    }

    /**
     * check all required properties have been set
     */
    protected void validateProps() {
        //
    }
    
    protected final void snapStratOveriewState( StrategyStateImpl stratState ) {
        stratState.getAlgoIdForUpdate().copy( getComponentId() );
        stratState.setTimestamp( TimeZoneCalculator.instance().getTimeUTC( System.currentTimeMillis() ) );
        stratState.setAlgoEventSeqNum( _summaryState.getAlgoEventSeqNum() );
        stratState.setPnl( _summaryState.getPnl() );
        stratState.setLastEventInst( _summaryState.getLastEventInst() );
        stratState.setLastTickId( _summaryState.getLastTickId() );
    }

    PoolFactory<RecoveryCancelRequestImpl> getCancelPoolFactory() {
        return _cancelPool;
    }

    /**
     * exchange handlers need access to the pool factories
     * @return
     */
    PoolFactory<RecoveryNewOrderSingleImpl> getNosPoolFactory() {
        return _nosPool;
    }
    
    OrderRouter getOrderRouter() {
        return _orderRouter;
    }

    private void addExchangeDataHandlerToList( ExchangeHandler<T> exh ) {
        _exchangeHandlers = Utils.arrayCopyAndAddEntry( _exchangeHandlers, exh );
    }

    private void addInstState( StratInstrumentStateWrapper<T> instState ) {
        _instState = Utils.arrayCopyAndAddEntry( _instState, instState );
    }

    private void addMarketDataHandlerToList( StratBookAdapter<T> mdh ) {
        _mdHandlers = Utils.arrayCopyAndAddEntry( _mdHandlers, mdh );
    }

    private void doAllWork() {
        for( int i=0 ; i < _mdHandlers.length ; i++ ) {
            _exchangeHandlers[i].doWorkUnit();
            _mdHandlers[i].doWorkUnit();
        }
    }

    private void generateStateSnapshot() {
        
        if ( _sendStateSnapshot ) {
            final StrategyStateImpl stratState = getStrategyStatePoolFactory().get();
            
            StratInstrumentStateImpl lastInstSnap = null;
            
            for( int i=0 ; i < _instState.length ; i++ ) {
                final StratInstrumentStateWrapper<T> stratInstStateWrapper = _instState[ i ];
                final StratInstrumentStateImpl stratInstStateSnapshot = getStratInstrumentStatePoolFactory().get();
                
                stratInstStateWrapper.snapStratInstState( stratInstStateSnapshot );
    
                if ( lastInstSnap == null ) {
                    stratState.setInstState( stratInstStateSnapshot );
                } else {
                    lastInstSnap.setNext( stratInstStateSnapshot );
                }
                
                lastInstSnap = stratInstStateSnapshot;
            }
            
            stratState.setNoInstEntries( _instState.length );
    
            snapStratOveriewState( stratState );
            
            enqueueForHubDispatch( stratState );
            
            _sendStateSnapshot = false;
        }
    }

    private int sendEnqueuedDownstream() {
        
        if ( _firstDownEvent == null ) return 0;
        
        int msgs = 0;
        
        Message m = _firstDownEvent;
        Message msgToDispatch;
        
        while( m != null ) {
            
            msgToDispatch = m;
            m = m.getNextQueueEntry(); 
            msgToDispatch.detachQueue();

            MessageHandler h = msgToDispatch.getMessageHandler();
            
            if ( h != null ) {
                if ( _tradingAllowed ) {
                    h.handle( msgToDispatch );
                } else{
                    _logMsg.copy( getComponentId() ).append( " BaseStrategy SEND DISABLED, recycle : " );
                    msgToDispatch.dump( _logMsg );
                    _log.info( _logMsg );
                    recycle( msgToDispatch );
                }
                ++msgs;
            } else {
                _logMsg.copy( getComponentId() ).append( " BaseStrategy Missing downstream handler for " );
                m.dump( _logMsg );
                _log.warn( _logMsg );
            }
        }
        
        _firstDownEvent = _lastDownEvent = null;
        
        return msgs;
    }

    private void sendEnqueuedHub() {
        
        if ( _firstHubEvent == null ) return;

        Message m = _firstHubEvent;
        Message msgToDispatch;
        
        while( m != null ) {
            
            msgToDispatch = m;
            m = m.getNextQueueEntry(); 
            msgToDispatch.detachQueue();

            if ( _hubSession != null ) {
                _hubSession.handle( msgToDispatch );
            } else {
                recycle( m );
            }
        }
        
        _firstHubEvent = _lastHubEvent = null;
    }
}
