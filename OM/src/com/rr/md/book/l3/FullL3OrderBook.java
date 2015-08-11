/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l3;

import java.util.concurrent.atomic.AtomicBoolean;

import com.rr.core.collections.LongHashMap;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.model.book.AbstractBook;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.model.book.BookLevelEntry;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.core.model.book.EventMutatableBook;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.impl.BookAddOrderImpl;
import com.rr.model.generated.internal.events.impl.BookDeleteOrderImpl;
import com.rr.model.generated.internal.events.impl.BookModifyOrderImpl;

/**
 * full order book implementation ... uses book events to maintain a book
 * 
 * Mutation must only occur in one thread which must protect against another thread invoking snap
 * 
 * Assumption is only one reader ... if more then change the locking logic so readers dont block each other
 *
 * May be MANY books loaded against several threads which is why the pools need to be aligned to the thread not the book
 * 
 * Assumption is that 99% of updates will not be dups and as such code is optimised to assumption data is good 
 */

public final class FullL3OrderBook extends AbstractBook implements EventMutatableBook {

    private static final Logger _log = LoggerFactory.create( FullL3OrderBook.class );

    private static final int MIN_ORDERS = 16;

    private final LongBookPoolMgr<OrderBookEntry> _poolMgr;
    
    private final LongHashMap<OrderBookEntry> _orderEntryMap;
    
    private       FullBookLevelEntry _bestLevelBuy   = new FullBookLevelEntry( 0.0 );
    private final FullBookLevelEntry _worstLevelBuy  = _bestLevelBuy;
    
    private       FullBookLevelEntry _bestLevelSell  = new FullBookLevelEntry( Double.MAX_VALUE );
    private final FullBookLevelEntry _worstLevelSell = _bestLevelSell;

    private final ReusableString _msg = new ReusableString();

    private final AtomicBoolean  _lock = new AtomicBoolean( false );
    
    private int   _buyLevels;
    private int   _sellLevels;


    public FullL3OrderBook( Instrument                      instrument, 
                            int                             presizeOrders, 
                            LongBookPoolMgr<OrderBookEntry> poolMgr ) {

        super( instrument );
        
        if ( presizeOrders < MIN_ORDERS ) presizeOrders = MIN_ORDERS;
        
        _poolMgr    = poolMgr;
        _orderEntryMap   = new LongHashMap<OrderBookEntry>( presizeOrders, 
                                                            0.75f, 
                                                            poolMgr.getEntryFactory(), 
                                                            poolMgr.getEntryRecycler() );
    }

    @Override
    public void snap( ApiMutatableBook dest ) {
        int maxLevels = dest.getMaxLevels();
        
        /**
         * qty adjustment is sync'd, but insertion of new levels in book is not
         * so skip any levels where qty is zero
         */
        FullBookLevelEntry nxtBuy  = _bestLevelBuy;
        FullBookLevelEntry nxtSell = _bestLevelSell;
        
        int lvl = 0;
        try {
            grabLock();
            
            while ( lvl < maxLevels ) {
                if ( nxtBuy == _worstLevelBuy )   break;
                if ( nxtSell == _worstLevelSell ) break;
                
                dest.setLevel( lvl, nxtBuy.getQty(), nxtBuy.getPrice(), false, nxtSell.getQty(), nxtSell.getPrice(), false );

                nxtBuy = nxtBuy.getNext();
                nxtSell = nxtSell.getNext();
                
                ++lvl;
            }
            
            if ( lvl == maxLevels ) return;

            if ( nxtBuy != _worstLevelBuy ) {
                while ( lvl < maxLevels && nxtBuy != _worstLevelBuy ) {
                    
                    dest.setLevel( lvl, nxtBuy.getQty(), nxtBuy.getPrice(), false, 0, 0.0d, true );

                    nxtBuy = nxtBuy.getNext();
                    
                    ++lvl;
                }
            } else {
                while ( lvl < maxLevels && nxtSell != _worstLevelSell ) {
                    
                    dest.setLevel( lvl, 0, 0.0d, true, nxtSell.getQty(), nxtSell.getPrice(), false );
    
                    nxtSell = nxtSell.getNext();
                    
                    ++lvl;
                }
            }

            dest.setNumLevels( lvl );
            
        } finally {
            releaseLock();
        }
    }

    @Override
    public void grabLock() {
        while( !_lock.compareAndSet( false, true ) ) {
            // spin
        }
    }

    @Override
    public void releaseLock() {
        _lock.set( false );
    }

    @Override
    public boolean apply( Message event ) {
        boolean changed = false;
        
        while( event != null ) {
            final Message nxt = event.getNextQueueEntry();
            
            changed |= applyEvent( event );
            
            event = nxt;
        }
        
        return changed;
    }
    
    private boolean applyEvent( Message event ) {
        _seqNum = event.getMsgSeqNum();
        
        switch( event.getReusableType().getSubId() ) {
        case EventIds.ID_BOOKADDORDER:
            ++_ticks;
            return applyAdd( (BookAddOrderImpl) event );
        case EventIds.ID_BOOKMODIFYORDER:
            ++_ticks;
            return applyModify( (BookModifyOrderImpl) event );
        case EventIds.ID_BOOKDELETEORDER:
            ++_ticks;
            return applyDelete( (BookDeleteOrderImpl) event );
        case EventIds.ID_BOOKCLEAR:
            return applyClear();
        }
        
        return false;
    }

    private boolean applyClear() {
        boolean hasData = _orderEntryMap.size() > 0;

        clear();
        
        return hasData;
    }

    private boolean applyDelete( BookDeleteOrderImpl event ) {

        OrderBookEntry entry = _orderEntryMap.get( event.getOrderId() );

        if ( entry == null ) {
            _msg.setValue( "Delete for entry not in book " );
            event.dump( _msg );
            _log.warn( _msg );
            _poolMgr.recycle( event );
            return false;
        }
        
        FullBookLevelEntry newEntry = entry.getBookEntry(); 
                
        try {
            grabLock();
            
            removeFromBook( entry );
            entry.setBookEntry( newEntry );
        } finally {
            releaseLock();
            _poolMgr.recycle( event );
        }

        return true;
    }

    private boolean  applyModify( BookModifyOrderImpl event ) {
                
        OrderBookEntry entry = _orderEntryMap.get( event.getOrderId() );

        if ( entry == null ) {
            _msg.setValue( "Modification for entry not in book " );
            event.dump( _msg );
            _log.warn( _msg );
            _poolMgr.recycle( event );
            return false;
        }
        
        // has price changed
        
        double newPrice = event.getPrice();
        int newQty = event.getOrderQty();

        boolean priceChange = checkPriceChange( entry, newPrice );
        boolean qtyChange   = checkQtyChange( entry, newQty );

        if ( !qtyChange && !priceChange ) {
            _poolMgr.recycle( event );
            return false;
        }
        
        FullBookLevelEntry newEntry = entry.getBookEntry(); 
                
        if ( priceChange ) {
            if ( entry.isBuySide() ) {
                newEntry = getLevelBuy( newPrice );
            } else {
                newEntry = getLevelSell( newPrice );
            }
        }
        
        try {
            grabLock();
            
            if ( priceChange ) {
                removeFromBook( entry );

                entry.setBookEntry( newEntry );
            } else {
                newEntry.removeQty( entry.getQty() );
            }
            
            entry.setQty( newQty );
            newEntry.addQty( newQty );
        } finally {
            releaseLock();
            _poolMgr.recycle( event );
        }

        return true;
    }

    private boolean applyAdd( BookAddOrderImpl event ) {

        if ( event.getSide().getIsBuySide() ) {
            return applyAddBuy( event );
        } 
        return applyAddSell( event );
    }

    private boolean checkPriceChange( OrderBookEntry entry, double newPrice ) {
        if ( newPrice != Constants.UNSET_DOUBLE ) {
            double delta = Math.abs( newPrice - entry.getPrice() );

            if ( delta > Constants.WEIGHT ) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean checkQtyChange( OrderBookEntry entry, int newQty ) {
        if ( newQty != Constants.UNSET_INT ) {
            int delta = newQty - entry.getQty();

            return delta != 0;
        }
        
        return false;
    }
    
    private boolean applyAddBuy( BookAddOrderImpl event ) {

        final double price = event.getPrice();
        final int orderQty = event.getOrderQty();
                
        if ( price <= 0 || orderQty <= 0 ) return false;

        OrderBookEntry newOBE = _poolMgr.getOrderBookEntryFactory().get();
        
        OrderBookEntry old = _orderEntryMap.put( event.getOrderId(), newOBE );
        
        FullBookLevelEntry l = getLevelBuy( price );

        newOBE.setBookEntry( l );
        
        try {
            grabLock();
            
            if ( old != null ) {
                removeFromBook( old );
            }
            
            newOBE.setQty( orderQty );
            newOBE.setBuySide( true );
            l.addQty( orderQty );
        } finally {
            releaseLock();
        }

        recycle( old );
        
        _poolMgr.recycle( event );
        
        return true;
    }
    
    private boolean applyAddSell( BookAddOrderImpl event ) {

        final double price = event.getPrice();
        final int orderQty = event.getOrderQty();
                
        if ( price <= 0 || orderQty <= 0 ) return true;

        OrderBookEntry newOBE = _poolMgr.getOrderBookEntryFactory().get();
        
        OrderBookEntry old = _orderEntryMap.put( event.getOrderId(), newOBE );
        
        FullBookLevelEntry l = getLevelSell( price );

        newOBE.setBookEntry( l );
        
        try {
            grabLock();
            
            if ( old != null ) {
                removeFromBook( old );
            }
            
            newOBE.setQty( orderQty );
            newOBE.setBuySide( false );
            l.addQty( orderQty );
        } finally {
            releaseLock();
        }

        recycle( old );
        _poolMgr.recycle( event );
        
        return true;
    }
    
    private void recycle( OrderBookEntry old ) {
        if ( old != null ) {
            _poolMgr.getOrderBookEntryRecycler().recycle( old );
        }
    }

    private void removeFromBook( OrderBookEntry old ) {
        FullBookLevelEntry ble = old.getBookEntry();
        
        if ( ble != null ) {
            ble.removeQty( old.getQty() );
            
            if ( ble.getQty() == 0 ) {
                removeBookEntry( ble, old.isBuySide() );
            }
            
            old.setQty( 0 );
        }
    }

    private void removeBookEntry( FullBookLevelEntry ble, boolean isBuySide ) {
        
        final FullBookLevelEntry nxt = ble.getNext();
        final FullBookLevelEntry prv = ble.getPrev();
        
        if ( prv != null ) {
            prv.setNext( nxt );
        } else {
            // must be the head of chain

            if ( nxt == null ) {
                return; // cant delete the tail node (which is also the only node in chain)
            }

            if ( isBuySide ) {
                _bestLevelBuy = nxt;
            } else {
                _bestLevelSell = nxt;
            }
        }
        
        if ( nxt != null ) {
            nxt.setPrev( prv );
        }

        if ( isBuySide ) {
            --_buyLevels;
        } else {
            --_sellLevels;
        }
        
        _poolMgr.getBookLevelRecycler().recycle( ble );
    }

    private FullBookLevelEntry getLevelBuy( double price ) {
        FullBookLevelEntry l = _bestLevelBuy;
        
        while( price < l.getPrice() ) {
            final FullBookLevelEntry nxt = l.getNext();
            
            if ( nxt == null ) break;
            
            l = nxt;
        }
        
        if ( Math.abs(price - l.getPrice()) > Constants.WEIGHT ) {

            final FullBookLevelEntry newL = _poolMgr.getBookLevelEntryFactory().get();
            final FullBookLevelEntry prev = l.getPrev();
            
            newL.setPrice( price );
            newL.setPrev( prev );
            newL.setNext( l );

            l.setPrev( newL );

            if ( prev == null ) {
                _bestLevelBuy = newL;
            } else {
                prev.setNext( newL );
            }
            
            l = newL;

            ++_buyLevels;
        }
        
        return l;
    }
    
    private FullBookLevelEntry getLevelSell( double price ) {
        FullBookLevelEntry l = _bestLevelSell;
        
        while( price > l.getPrice() ) {
            l = l.getNext();
        }
        
        if ( Math.abs(price - l.getPrice()) > Constants.WEIGHT ) {

            final FullBookLevelEntry newL = _poolMgr.getBookLevelEntryFactory().get();
            final FullBookLevelEntry prev = l.getPrev();
            
            newL.setPrice( price );
            newL.setPrev( prev );
            newL.setNext( l );

            l.setPrev( newL );

            if ( prev == null ) {
                _bestLevelSell = newL;
            } else {
                prev.setNext( newL );
            }

            l = newL;
            
            ++_sellLevels;
        }
        
        return l;
    }

    @Override
    public void clear() {
        try {
            grabLock();
            
            _orderEntryMap.clear();
            
            for ( FullBookLevelEntry e = _bestLevelBuy ; e != _worstLevelBuy ; e = e.getNext() ) {
                _poolMgr.getBookLevelRecycler().recycle( e );
            }
            
            for ( FullBookLevelEntry e = _bestLevelSell ; e != _worstLevelSell ; e = e.getNext() ) {
                _poolMgr.getBookLevelRecycler().recycle( e );
            }
        } finally {
            releaseLock();
        }
    }

    @Override
    public int getMaxLevels() {
        return (_buyLevels>_sellLevels) ? _buyLevels : _sellLevels;
    }

    @Override
    public int getActiveLevels() {
        return (_buyLevels>_sellLevels) ? _buyLevels : _sellLevels;
    }

    @Override
    public boolean isLockable() {
        return true;
    }

    @Override
    public boolean getLevel( int lvl, DoubleSidedBookEntry dest ) {
        throw new RuntimeException( "getLevel not implemented yet, use snap" );
    }

    @Override
    public void dump( ReusableString dest ) {
        int maxLevels = getMaxLevels();
        
        dest.append( "Book " ).append( _instrument.getRIC() ).append( "\n" );
        
        /**
         * qty adjustment is sync'd, but insertion of new levels in book is not
         * so skip any levels where qty is zero
         */
        FullBookLevelEntry nxtBuy  = _bestLevelBuy;
        FullBookLevelEntry nxtSell = _bestLevelSell;
        
        int lvl = 0;
        while ( lvl < maxLevels ) {
            if ( nxtBuy == _worstLevelBuy )   break;
            if ( nxtSell == _worstLevelSell ) break;
            
            dest.append( "[L" ).append( lvl ).append( "]  ");
            dest.append( nxtBuy.getQty() ).append( " x " ).append( nxtBuy.getPrice() ).append( "  :  " );
            dest.append( nxtSell.getPrice() ).append( " x " ).append( nxtSell.getQty() ).append( "\n" );

            nxtBuy = nxtBuy.getNext();
            nxtSell = nxtSell.getNext();
            
            ++lvl;
        }
        
        if ( lvl == maxLevels ) return;

        if ( nxtBuy != _worstLevelBuy ) {
            while ( lvl < _buyLevels && nxtBuy != _worstLevelBuy ) {
                
                dest.append( "[L" ).append( lvl ).append( "]  ");
                dest.append( nxtBuy.getQty() ).append( " x " ).append( nxtBuy.getPrice() ).append( "  :  " );
                dest.append( "0.0 x 0\n" );

                nxtBuy = nxtBuy.getNext();
                
                ++lvl;
            }
            
            return;
        }

        while ( lvl < _sellLevels && nxtSell != _worstLevelSell ) {
            
            dest.append( "[L" ).append( lvl ).append( "]  ");
            dest.append( "0 x 0.0  :  " );
            dest.append( nxtBuy.getQty() ).append( " x " ).append( nxtBuy.getPrice() ).append( "\n" );

            nxtSell = nxtSell.getNext();
            
            ++lvl;
        }
    }
    
    @Override
    public boolean getBidEntry( int lvl, BookLevelEntry dest ) {
        throw new SMTRuntimeException( "getBidEntry not implemented yet, use snap" );
    }

    @Override
    public boolean getAskEntry( int lvl, BookLevelEntry dest ) {
        throw new SMTRuntimeException( "getAskEntry not implemented yet, use snap" );
    }

    @Override
    public void setDirty( final boolean isDirty ) {
        throw new SMTRuntimeException( "setDirty not implemented yet, use snap" );
    }

    @Override
    public boolean isValidBBO() {
        throw new SMTRuntimeException( "isValidBBO not yet implemented, use snap" );
    }
}
