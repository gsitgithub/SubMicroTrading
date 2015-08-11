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
import com.rr.core.algo.strats.Strategy.StratBookAdapter;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.core.model.book.DoubleSidedBookEntryImpl;
import com.rr.model.generated.internal.events.impl.StratInstrumentStateImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.Restated;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.MultiLegReportingType;
import com.rr.model.generated.internal.type.Side;

/**
 * wrapper for strategy instrument state
 * book will be async updated by BookController
 * bookSnap will only be read/written to by single multiplexor thread .. ideally should be shared by all strats requiring book on same thread
 * 
 * @NOTE only suitable for single open order at any time
 */
public final class StratInstrumentStateWrapper<T extends Book> {

    private final StratInstrumentStateImpl          _stratInstState;
    private final DoubleSidedBookEntry              _bboSnap = new DoubleSidedBookEntryImpl(); // worker var

    private       T                                 _book;
    private       ApiMutatableBook                  _bookSnap;
    
    private       ExchangeHandler<T>                _exchangeHandler;
    private       StratBookAdapter<T>               _bookHandler;
    
    private final ReusableString                    _lastClOrdId = new ReusableString(20);
    
    private final int                               _instIdx;
    
    private       long                              _sliceStartTimeMS;             // time of last NOS
    private       long                              _conflateSnaps;         // number of book snaps avoided ie conflated
    
    private       int                               _openOrderCount;        // count of open orders on market
    private       double                            _targetSlicePrice;
    private       double                            _origSlicePrice;
    
    private       int                               _targetSliceQty;
    private       int                               _targetSliceCumQty;
    private       Side                              _targetSliceSide;
    private       int                               _totalFlattenedQty;
    private       double                            _sliceExecutionValue;
    
    private       boolean                           _flattening;
    
    public StratInstrumentStateWrapper( StratInstrumentStateImpl stratInstState, int instIdx ) {
        super();
        
        _stratInstState = stratInstState;
        _instIdx = instIdx;
    }

    /**
     * @return last snapshot ... be aware only contains SUBSET of non book fields MISSING entries like tradeVol, exchangeTS ... use active book for that
     */
    public Book getLastSnapshot() {
        return _bookSnap;
    }
    
    public Book snap() {
        final long latestNanoTS = _book.getLastTickInNanos(); // we have hit a read barrier so dont worry about locking to read the id
        final long latestSeqNum = _book.getLastTickId();
        
        if ( latestNanoTS != _bookSnap.getLastTickInNanos() || latestNanoTS == Constants.UNSET_LONG || latestSeqNum != _bookSnap.getLastTickId() ) {
            try {
                _book.grabLock();
                
                _book.snap( _bookSnap );
                
            } finally {
                _book.releaseLock();
            }
        } else {
            ++_conflateSnaps;
        }
        
        return _bookSnap;
    }

    public long getConflateSnaps() {
        return _conflateSnaps;
    }

    public Instrument getInstrument() {
        return _stratInstState.getInstrument();
    }

    public T getBook() {
        return _book;
    }
    
    public double getTargetSliceOrderPrice() {
        return _targetSlicePrice;
    }
    
    /**
     * @param book
     * @param snapBook threadsafe copy used to snap the live book .. .can be shared by other strats on same thread
     */
    public void setBook( T book, ApiMutatableBook snapBook ) {
        _book = book;
        _bookSnap = snapBook;  
    }

    public ExchangeHandler<T> getExchangeHandler() {
        return _exchangeHandler;
    }
    
    public StratBookAdapter<T> getBookHandler() {
        return _bookHandler;
    }

    @Override
    public String toString() {
        return _stratInstState.getInstrument().getSecurityDesc().toString();
    }
    
    public void setExchangeHandler( ExchangeHandler<T> exchangeHandler ) {
        _exchangeHandler = exchangeHandler;
    }

    public void setBookHandler( StratBookAdapter<T> bookHandler ) {
        _bookHandler = bookHandler;
    }

    public StratInstrumentStateImpl getStratInstState() {
        return _stratInstState;
    }

    public DoubleSidedBookEntry getBBOSnapEntry() {
        return _bboSnap;
    }

    public void setSliceStart( final long timeMS ) {
        _sliceStartTimeMS = timeMS;
    }
    
    public void setTargetSlicePrice( final double px ) {
        _targetSlicePrice = px;
    }
    
    public void updateStratInstStateBBO() {
        _stratInstState.setAskPx( _bboSnap.getAskPx() );
        _stratInstState.setBidPx( _bboSnap.getBidPx() );
    }
    
    public void updateStratInstState( final NewOrderSingle nos ) {
        
        _lastClOrdId.copy( nos.getClOrdId() );
        
        final int nosQty = nos.getOrderQty();
        
        if ( nos.getSide().getIsBuySide() ) {
            _stratInstState.setTotalLongOrders( _stratInstState.getTotalLongOrders() + 1 );
            _stratInstState.setTotLongContractsOpen( _stratInstState.getTotLongContractsOpen() + nosQty );
            _stratInstState.setLastDecidedPosition( nosQty );
        } else {
            _stratInstState.setTotalShortOrders( _stratInstState.getTotalLongOrders() + 1 );
            _stratInstState.setTotShortContractsOpen( _stratInstState.getTotLongContractsOpen() + nosQty );
            _stratInstState.setLastDecidedPosition( -(nosQty) );
        }

        _stratInstState.setLastTickId( _bookSnap.getLastTickId() );
        updateStratInstStateBBO();
        
        ++_openOrderCount;
    }

    public ZString getLastClOrdId() {
        return _lastClOrdId;
    }
    
    public boolean isActiveOnMarket() {
        return _openOrderCount > 0;
    }
    
    public long getSliceStartTimeMS() {
        return _sliceStartTimeMS;
    }
    
    /**
     * snap the state of the strategy instrument state as is with the last book and BBO snap taken
     * 
     * @param stratState
     * @param stateInstSnapshot
     */
    public void snapStratInstState( final StratInstrumentStateImpl stateInstSnapshot ) {
        
        stateInstSnapshot.setTotalLongOrders( _stratInstState.getTotalLongOrders() );
        stateInstSnapshot.setTotLongContractsOpen( _stratInstState.getTotLongContractsOpen() );

        stateInstSnapshot.setTotalShortOrders( _stratInstState.getTotalShortOrders() );
        stateInstSnapshot.setTotShortContractsOpen( _stratInstState.getTotShortContractsOpen() );

        stateInstSnapshot.setTotLongContractsExecuted( _stratInstState.getTotLongContractsExecuted() );
        stateInstSnapshot.setTotShortContractsExecuted( _stratInstState.getTotShortContractsExecuted() );
        stateInstSnapshot.setTotLongValueExecuted( _stratInstState.getTotLongValueExecuted() );
        stateInstSnapshot.setTotShortValueExecuted( _stratInstState.getTotShortValueExecuted() );
        stateInstSnapshot.setLastDecidedPosition( _stratInstState.getLastDecidedPosition() );
        stateInstSnapshot.setUnwindPnl( _stratInstState.getUnwindPnl() );

        stateInstSnapshot.setLastTickId( _stratInstState.getLastTickId() );
        stateInstSnapshot.setAskPx( _stratInstState.getAskPx() );
        stateInstSnapshot.setBidPx( _stratInstState.getBidPx() );
    }

    public void orderTerminal( final StratOrder order ) {
        final int orderQty = order.getQty();
        final int openAdjust = order.getCumQty() - orderQty; // unfilled amount
        
        if ( order.getSide().getIsBuySide() ) {
            _stratInstState.setTotLongContractsOpen( _stratInstState.getTotLongContractsOpen() + openAdjust );
        } else {
            _stratInstState.setTotShortContractsOpen( _stratInstState.getTotShortContractsOpen() + openAdjust );
        }
        
        if ( _openOrderCount == 1 ) {
            getStrategy().legSliceCompleted();
        }

        if ( --_openOrderCount < 0 ) { // could happen with PosDup's
            _openOrderCount = 0;
        }
        
    }

    public BaseStrategy<T> getStrategy() {
        return _exchangeHandler.getStrategy();
    }

    /**
     * apply the fill to the strategy instrument state
     * @param msg
     */
    public void updateStratInstState( final TradeNew msg ) {
        final double lastPx       = msg.getLastPx();
        final int    lastQty      = msg.getLastQty();
        final int    contractMult = _stratInstState.getInstrument().getContractMultiplier();
        final double value        = lastPx * lastQty * contractMult;
        
        if ( msg.getMultiLegReportingType() != MultiLegReportingType.LegOfSpread ) {
    
            if ( msg.getSide().getIsBuySide() ) {
                _stratInstState.setTotLongContractsExecuted( _stratInstState.getTotLongContractsExecuted() + lastQty );
                _stratInstState.setTotLongValueExecuted( _stratInstState.getTotLongValueExecuted() + value );
                _stratInstState.setTotLongContractsOpen( _stratInstState.getTotLongContractsOpen() - lastQty );
            } else {
                _stratInstState.setTotShortContractsExecuted( _stratInstState.getTotShortContractsExecuted() + lastQty );
                _stratInstState.setTotShortValueExecuted( _stratInstState.getTotShortValueExecuted() + value );
                _stratInstState.setTotShortContractsOpen( _stratInstState.getTotShortContractsOpen() - lastQty );
            }
            
            getStrategy().getStrategyState().setLastEventInst( _instIdx );

            _targetSliceCumQty   += lastQty;

            if ( !_flattening ) {
                _sliceExecutionValue += value;
            } else {                                    // flattening / unwind
                _sliceExecutionValue -= value;

                double pnl; 

                if ( msg.getSide().getIsBuySide() ) { // unwinding a sell, eg qty=2, soldAt 100, boughtBackAt 105, pnl = 100-105 * 2 = -10 (loss)
                    pnl = lastQty * (_origSlicePrice - lastPx) * contractMult; 
                    _stratInstState.setTotShortContractsUnwound( _stratInstState.getTotShortContractsUnwound() + lastQty );
                } else {                              // unwinding a buy, eg qty=2, boughtAt 100, sellAt 95, pnl = 95-100 * 2 = -10 (loss)
                    pnl = lastQty * (lastPx - _origSlicePrice) * contractMult; 
                    _stratInstState.setTotLongContractsUnwound( _stratInstState.getTotLongContractsUnwound() + lastQty );
                }

                _stratInstState.setUnwindPnl( _stratInstState.getUnwindPnl() + pnl );

                _totalFlattenedQty += _targetSliceCumQty;
            }
        }
    }

    /**
     * terminal order has been reopened .. cancel will be sent but in mean time contracts open must be adjusted
     * 
     * @param msg
     */
    public void updateStratInstState( final Restated msg ) {
        final int openAdjust = msg.getLeavesQty();
        
        if ( msg.getSide().getIsBuySide() ) {
            _stratInstState.setTotLongContractsOpen( _stratInstState.getTotLongContractsOpen() + openAdjust );
        } else {
            _stratInstState.setTotLongContractsOpen( _stratInstState.getTotLongContractsOpen() + openAdjust );
        }

        getStrategy().getStrategyState().setLastEventInst( _instIdx );
    }

    public void updateStratInstState( final TradeCancel msg ) {
        final double lastPx  = msg.getLastPx();
        final int    lastQty = msg.getLastQty();
        final int    contractMult = _stratInstState.getInstrument().getContractMultiplier();
        final double value        = lastPx * lastQty * contractMult;
        
        if ( msg.getMultiLegReportingType() != MultiLegReportingType.LegOfSpread ) {
            if ( msg.getSide().getIsBuySide() ) {
                _stratInstState.setTotLongContractsExecuted( _stratInstState.getTotLongContractsExecuted() - lastQty );
                _stratInstState.setTotLongValueExecuted( _stratInstState.getTotLongValueExecuted() - value );
                _stratInstState.setTotLongContractsOpen( _stratInstState.getTotLongContractsOpen() + lastQty );
            } else {
                _stratInstState.setTotShortContractsExecuted( _stratInstState.getTotShortContractsExecuted() - lastQty );
                _stratInstState.setTotShortValueExecuted( _stratInstState.getTotShortValueExecuted() - value  );
                _stratInstState.setTotShortContractsOpen( _stratInstState.getTotLongContractsOpen() + lastQty );
            }
            
            getStrategy().getStrategyState().setLastEventInst( _instIdx );
    
            if ( !_flattening ) {
                _sliceExecutionValue -= value;
                _targetSliceCumQty   -= lastQty;
            } else {                                    // flattening / unwind
                _sliceExecutionValue += value;
                _targetSliceCumQty   += lastQty;
    
                if ( msg.getSide().getIsBuySide() ) { // CANCEL unwinding a sell, eg qty=2, soldAt 100, boughtBackAt 105, pnl = 100-105 * 2 = -10 (loss)
                    double pnl = lastQty * (_targetSlicePrice - lastPx) * contractMult; 
                    _stratInstState.setTotShortContractsUnwound( _stratInstState.getTotShortContractsUnwound() - lastQty );
                    _stratInstState.setUnwindPnl( _stratInstState.getUnwindPnl() - pnl );
                    
                } else {                              // CANCEL unwinding a buy, eg qty=2, boughtAt 100, sellAt 95, pnl = 95-100 * 2 = -10 (loss)
                    double pnl = lastQty * (lastPx - _targetSlicePrice) * contractMult; 
                    _stratInstState.setTotLongContractsUnwound( _stratInstState.getTotLongContractsUnwound() - lastQty );
                    _stratInstState.setUnwindPnl( _stratInstState.getUnwindPnl() - pnl );
                }
            }
        }
    }
    
    public int getInstIdx() {
        return _instIdx;
    }

    public void setSnapBook( final ApiMutatableBook snapBook ) {
        _bookSnap = snapBook;
    }

    public int getTargetSliceQty() {
        return _targetSliceQty;
    }
    
    public int getTargetSliceCumQty() {
        return _targetSliceCumQty;
    }

    public void setTargetSliceQty( final int targetSliceQty ) {
        _targetSliceQty = targetSliceQty;
    }

    public void setTargetSliceCumQty( final int targetSliceCumQty ) {
        _targetSliceCumQty = targetSliceCumQty;
    }
    
    /**
     * @return the outstanding qty for current slice for this instrument
     */
    public int getSliceUnfilledQty() {
        return _targetSliceQty - _targetSliceCumQty;
    }

    public void dumpInstDetails( final ReusableString s ) {
        s.append( ", " ).append( _stratInstState.getInstrument().getSecurityDesc() ).
          append( " sliceRem=" ).append( getSliceUnfilledQty() ).
          append( ", sliceSide=" ).append( _targetSliceSide ).
          append( ", flattenedQty=" ).append( _totalFlattenedQty ).
          append( ", openOrders=" ).append( _openOrderCount ).
          append( ", longExecuted=" ).append( _stratInstState.getTotLongValueExecuted() ).
          append( ", shortExecuted=" ).append( _stratInstState.getTotShortValueExecuted() );
    }

    public void setTargetSliceSide( final Side side ) {
        _targetSliceSide = side;
    }
    
    public Side getTargetSliceSide() {
        return _targetSliceSide;
    }

    public double getSliceExecutionValue() {
        return _sliceExecutionValue;
    }
    
    public void newSlice( final long time, final int availQty, final double price, final Side side ) {
        setSliceStart( time );
        setTargetSliceQty( availQty );
        setTargetSlicePrice( price );
        setTargetSliceSide( side );
        setTargetSliceCumQty( 0 );
        _sliceExecutionValue = 0;
        _flattening = false;
        _origSlicePrice = price;
    }

    public boolean isTargetSliceFilled() {
        return _targetSliceCumQty == _targetSliceQty;
    }
    
    public void prepInitialFlatten( final int qty, final double price, final Side side ) {
        _flattening = true;
        
        prepFlatten( qty, price, side );
    }

    public void prepFlatten( final int qty, final double price, final Side side ) {
        setTargetSliceSide( side );
        setTargetSliceQty( qty );
        setTargetSlicePrice( price );
        setTargetSliceCumQty( 0 );
    }

    public boolean isFlattening() {
        return _flattening;
    }
    
    public void setFlattening( boolean flattening ) {
        _flattening = flattening;
    }

    public boolean isFlattened() {
        return getSliceUnfilledQty() == 0 || !_flattening;
    }

    public void abortUnfilledSlice() {
        setTargetSliceQty( 0 );
    }
}
