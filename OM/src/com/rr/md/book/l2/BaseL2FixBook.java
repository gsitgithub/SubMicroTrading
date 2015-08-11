/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l2;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.BookContext;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.MsgFlag;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.model.book.BookEntryImpl;
import com.rr.core.model.book.BookLevelEntry;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.md.book.MutableFixBook;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.MDSnapEntryImpl;
import com.rr.model.generated.internal.events.interfaces.MDEntry;
import com.rr.model.generated.internal.events.interfaces.MDSnapshotFullRefresh;


/**
 * Wrapper which applies fix events to mutable book
 * 
 * Has a queue of pending incremental events which will be used during gap recovery
 * 
 * Implements Message so it can be used with existing MessageDispatchers
 * 
 * Must only be written to on single thread, if using threadsafe underlying book then can snap on multiple reader threads
 * 
 * Fix Book level starts at 1 not 0
 * 
 * All locking of the book against reading and writing must happen at a higher level to avoid costly extra synchronisation
 * 
 * @TODO refactor Message into Dispatchable interface
 */
public abstract class BaseL2FixBook implements MutableFixBook {

    protected static final Logger _log = LoggerFactory.create( BaseL2FixBook.class );

    protected static final int LOG_THRESHOLD = 100;

    protected final ApiMutatableBook  _book;
    
    // use of tmpEntry means cannot have multiple threads updating same book
    protected final BookEntryImpl _tmpEntry = new BookEntryImpl();

    protected int    _totalTradeVol;
    protected double _totalTraded;

    protected int    _lastTradeQty;
    protected double _lastTradePrice;

    protected final ReusableString  _errMsg = new ReusableString();

    private volatile Message      _nextMessage    = null;

    private int _tickCount;

    public BaseL2FixBook( ApiMutatableBook book ) {
        _book = book;
    }

    @Override
    public void id( ReusableString out ) {
        _book.id( out );
    }

    @Override
    public Instrument getInstrument() {
        return _book.getInstrument();
    }

    @Override
    public boolean isLockable() {
        return _book.isLockable();
    }

    @Override
    public int getMaxLevels() {
        return _book.getMaxLevels();
    }

    @Override
    public int getActiveLevels() {
        return _book.getActiveLevels();
    }

    @Override
    public boolean getLevel( int lvl, DoubleSidedBookEntry dest ) {
        return _book.getLevel( lvl, dest );
    }

    @Override
    public void snap( ApiMutatableBook dest ) {
        _book.snap( dest );
        dest.setLastTickInNanos( getLastTickInNanos() );
        dest.setLastTickId( getLastTickId() );
    }

    @Override
    public void dump( ReusableString dest ) {
        dest.append( "L2FixBook bookSeqNum=" ).append( getLastTickId() ).append( " " );
        _book.dump( dest );
    }

    @Override
    public String toString() {
        return _book.toString();
    }
    
    @Override
    public void clear() {
        _book.clear();
        _book.setLastTickInNanos( 0 );
    }

    @Override
    public boolean applySnapshot( MDSnapshotFullRefresh msg, EventRecycler entryRecycler ) {

        boolean ok;
        
        ++_tickCount;

        setSeqNums( msg.getLastMsgSeqNumProcessed(), msg.getRptSeq() );
        MDSnapEntryImpl next = (MDSnapEntryImpl) msg.getMDEntries();
        
        while( next != null ) {
            applySnapEntry( next );

            next = next.getNext();
        }
        
        _book.setLastTickInNanos( msg.getReceived() );
        
        ok = replayQueued( msg.getLastMsgSeqNumProcessed(), msg.getRptSeq(), entryRecycler );
        
        return ok;
    }

    protected abstract void setSeqNums( int msgSeqNum, int rptSeq );

    @Override
    public boolean applyIncrementalEntry( final int eventSeqNum, final MDEntry entry ) {
        
        ++_tickCount;

        setSeqNums( eventSeqNum, entry.getRepeatSeq() );
        
        switch( entry.getMdEntryType() ) {
        case Bid:
            applyBid( entry );
            return true;
        case Offer:
            applyAsk( entry );
            return true;
        case Trade:
            applyTrade( entry );
            return true;
        case EmptyBook:
            clear();
            break;
        case AuctionClearingPrice: case ClosingPrice: case CompositeUnderlyingPrice: case EarlyPrices: case FixingPrice:
        case Imbalance: case IndexValue: case MarginRate: case MidPrice: case OpenInterest: case OpeningPrice:
        case PriorSettlePrice: case SessionHighBid: case SessionLowOffer: case SettleHighPrice: case SettleLowPrice:
        case SettlementPrice: case SimulatedBuy: case SimulatedSellPrice: case TradeVolume: case TradingSessionHighPrice:
        case TradingSessionLowPrice: case TradingSessionVWAPPrice: case Unknown:
        default:
            break;
        }
        
        return false;
    }

    @Override
    public boolean getBidEntry( int lvl, BookLevelEntry dest ) {
        return _book.getBidEntry( lvl, dest );
    }

    @Override
    public boolean getAskEntry( int lvl, BookLevelEntry dest ) {
        return _book.getAskEntry( lvl, dest );
    }

    @Override
    public int getTotalTradeVol() {
        return _totalTradeVol;
    }

    @Override
    public double getTotalTraded() {
        return _totalTraded;
    }
    
    @Override
    public int getLastTradeQty() {
        return _lastTradeQty;
    }

    @Override
    public double getLastTradePrice() {
        return _lastTradePrice;
    }

    @Override
    public void setDirty( boolean isDirty ) {
        _book.setDirty( isDirty );
        _book.setLastTickInNanos( 0 );
    }

    @Override
    public final void detachQueue() {
        _nextMessage = null;
    }

    @Override
    public final Message getNextQueueEntry() {
        return _nextMessage;
    }

    @Override
    public final void attachQueue( Message nxt ) {
        _nextMessage = nxt;
    }

    @Override public ReusableType getReusableType() { return null; }
    @Override public void setMessageHandler( MessageHandler handler ) { /* nothing */ }
    @Override public MessageHandler getMessageHandler() { return null; }
    @Override public void setFlag( MsgFlag flag, boolean isOn ) { /* nothing */ }
    @Override public boolean isFlagSet( MsgFlag flag ) { return false; }

    @Override
    public int getTickCount() {
        return _tickCount;
    }

    @Override
    public long getLastTickInNanos() {
        return _book.getLastTickInNanos();
    }

    @Override
    public void setLastTickInNanos( long nanoTS ) {
        _book.setLastTickInNanos( nanoTS );
    }

    @Override
    public void setLastExchangeTickTime( long exchangeSentTime ) {
        _book.setLastExchangeTickTime( exchangeSentTime );
    }

    @Override
    public long getLastExchangeTickTime() {
        return _book.getLastExchangeTickTime();
    }
    
    @Override
    public void grabLock() {
        _book.grabLock();
    }

    @Override
    public void releaseLock() {
        _book.releaseLock();
    }

    @Override
    public BookContext setContext( BookContext context ) {
        return _book.setContext( context );
    }

    @Override
    public BookContext getContext() {
        return _book.getContext();
    }

    @Override
    public boolean isValidBBO() {
        return _book.isValidBBO();
    }

    private void applySnapEntry( final MDSnapEntryImpl entry ) {

        final int lvl = entry.getMdPriceLevel() - 1;
        
        switch( entry.getMdEntryType() ) {
        case Bid:
            _tmpEntry.set( entry.getMdEntrySize(), entry.getMdEntryPx() );
            _book.setBid( lvl, _tmpEntry );
            break;
        case Offer:
            _tmpEntry.set( entry.getMdEntrySize(), entry.getMdEntryPx() );
            _book.setAsk( lvl, _tmpEntry );
            break;
        case Trade:
            _lastTradeQty   = entry.getMdEntrySize();
            _lastTradePrice = entry.getMdEntryPx();
            
            _totalTradeVol += _lastTradeQty;
            _totalTraded   += (_lastTradeQty * _lastTradePrice);
            break;
        case EmptyBook:
            clear();
            break;
        case AuctionClearingPrice: case ClosingPrice: case CompositeUnderlyingPrice: case EarlyPrices: case FixingPrice:
        case Imbalance: case IndexValue: case MarginRate: case MidPrice: case OpenInterest: case OpeningPrice:
        case PriorSettlePrice: case SessionHighBid: case SessionLowOffer: case SettleHighPrice: case SettleLowPrice:
        case SettlementPrice: case SimulatedBuy: case SimulatedSellPrice: case TradeVolume: case TradingSessionHighPrice:
        case TradingSessionLowPrice: case TradingSessionVWAPPrice: case Unknown:
        default:
            break;
        }
    }

    protected abstract boolean replayQueued( int lastSeqNumProcessed, int snapSeq, EventRecycler entryRecycler );
    
    protected abstract void applyBid( final MDEntry entry );

    protected abstract void applyAsk( final MDEntry entry );
    
    protected abstract void applyTrade( MDEntry entry );
    
}
