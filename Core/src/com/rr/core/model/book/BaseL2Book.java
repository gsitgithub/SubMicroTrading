/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

import com.rr.core.lang.ReusableString;
import com.rr.core.model.Instrument;

/**
 * common L2 book code
 */
public abstract class BaseL2Book extends BaseFixedSizeBook {

    private final BookEntryImpl[] _bids;
    private final BookEntryImpl[] _asks;
    private final BookEntryImpl[] _tmp;
    
    private final int             _maxIdx;


    public BaseL2Book( Instrument instrument, int maxLevels ) {
        super( instrument, maxLevels );
    
        _bids = new BookEntryImpl[ maxLevels ];
        _asks = new BookEntryImpl[ maxLevels ];
        _tmp  = new BookEntryImpl[ maxLevels ]; // required for deleteThru op
        
        for ( int l=0 ; l < maxLevels ; l++ ) {
            _asks[l] = new BookEntryImpl();
            _bids[l] = new BookEntryImpl();
            
            _tmp[l]  = null;
        }
        
        _maxIdx    = maxLevels - 1;
    }

    @Override
    public final void snap( final ApiMutatableBook dest ) {
        if ( _numLevels > 0 ) {
            int lvl = 0;
            
            while( lvl < _numLevels ) {
                final BookEntryImpl bid = _bids[lvl];
                final BookEntryImpl ask = _asks[lvl];
                dest.setLevel( lvl, bid, ask );
                ++lvl;
            }
            
        }
        dest.setNumLevels( _numLevels );
    }

    @Override
    public final boolean getLevel( final int lvl, final DoubleSidedBookEntry dest ) {
        if ( lvl < 0 || lvl > _maxIdx ) return false;
        
        final BookEntryImpl bid = _bids[lvl];
        final BookEntryImpl ask = _asks[lvl];
        
        dest.set( bid, ask );
        
        return true;
    }
    
    @Override
    public final void clear() {
        clear( _asks );
        clear( _bids );
    }

    @Override
    public final void setLevel( final int lvl, final BookEntryImpl newBid, final BookEntryImpl newAsk ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        final BookEntryImpl bid = _bids[lvl];
        final BookEntryImpl ask = _asks[lvl];
        
        ++_ticks;
        
        bid.set( newBid );
        ask.set( newAsk );
    }
    
    @Override
    public final void setLevel( final int lvl, 
                                final int bidQty, final double bidPrice, final boolean bidIsDirty, 
                                final int askQty, final double askPrice, final boolean askIsDirty ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        final BookEntryImpl bid = _bids[lvl];
        final BookEntryImpl ask = _asks[lvl];
        
        ++_ticks;
        
        bid.set( bidQty,  bidPrice, bidIsDirty );
        ask.set( askQty, askPrice, askIsDirty );
    }

    @Override
    public final void setNumLevels( final int lvl ) {
        // N/A .. fixed size
    }

    @Override
    public final int getActiveLevels() {
        return _numLevels;
    }

    @Override
    public final void dump( final ReusableString dest ) {
        dest.append( "Book " ).append( _instrument.getSecurityDesc() ).append( " " ).append( _instrument.getLongSymbol() ).append( "\n" );
        for( int l=0 ; l < _numLevels ; ++l ) {
            final BookEntryImpl bid = _bids[l];
            final BookEntryImpl ask = _asks[l];

            dest.append( "[L" ).append( l ).append( "]  ");
            dest.append( (bid.isDirty()) ? "D " : "  " );
            dest.append( bid.getQty() ).append( " x " ).append( bid.getPrice() ).append( "  :  " );
            dest.append( ask.getPrice() ).append( " x " ).append( ask.getQty() );
            dest.append( (ask.isDirty()) ? " D" : "  " );
            dest.append( "\n" );
        }
    }
    
    @Override
    public final boolean getBidEntry( final int lvl, final BookLevelEntry dest ) {
        if ( lvl < 0 || lvl > _maxIdx ) return false;
        
        final BookEntryImpl entry = _bids[lvl];
        
        dest.set( entry.getQty(), entry.getPrice() );

        return true;
    }

    @Override
    public final boolean getAskEntry( final int lvl, final BookLevelEntry dest ) {
        if ( lvl < 0 || lvl > _maxIdx ) return false;
        
        final BookEntryImpl entry = _asks[lvl];
        
        dest.set( entry.getQty(), entry.getPrice() );

        return true;
    }

    @Override
    public final void setBid( final int lvl, final BookLevelEntry entry ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        final BookEntryImpl bid = _bids[lvl];
        
        ++_ticks;

        bid.set( entry.getQty(), entry.getPrice() );
    }

    @Override
    public final void insertBid( final int lvl, final BookLevelEntry entry ) {
        insert( _bids, lvl );
        setBid( lvl, entry );
    }

    @Override
    public final void deleteBid( final int lvl ) {
        delete( _bids, lvl );
    }

    @Override
    public final void setAsk( final int lvl, final BookLevelEntry entry ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        final BookEntryImpl ask = _asks[lvl];
        
        ++_ticks;

        ask.set( entry.getQty(), entry.getPrice() );
    }

    @Override
    public final void insertAsk( final int lvl, final BookLevelEntry entry ) {
        insert( _asks, lvl );
        setAsk( lvl, entry );
    }

    @Override
    public final void deleteAsk( final int lvl ) {
        delete( _asks, lvl );
    }

    @Override
    public final void setBidDirty( final boolean isDirty ) {
        dirty( _bids, isDirty ); 
    }

    @Override
    public final void setAskDirty( final boolean isDirty ) {
        dirty( _asks, isDirty ); 
    }
    
    @Override
    public final void setDirty( boolean isDirty ) {
        dirty( _asks, isDirty ); 
        dirty( _bids, isDirty ); 
    }
    
    @Override
    public final boolean isValidBBO() {
        final BookEntryImpl bid = _bids[0];
        final BookEntryImpl ask = _asks[0];
        
        /**
         * the bid can be less than the ask for spread instruments
         * could check inst type and if its not a spread verify, but for now disable
         */
        return( (bid.isValid()) && (ask.isValid()) );   
    }

    @Override
    public final void setAskQty( final int lvl, final int qty ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        final BookEntryImpl ask = _asks[lvl];
        
        ++_ticks;

        ask.setQty( qty );
    }

    @Override
    public final void setBidQty( final int lvl, final int qty ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        final BookEntryImpl bid = _bids[lvl];
        
        ++_ticks;

        bid.setQty( qty );
    }

    @Override
    public final void setAskPrice( final int lvl, final double price ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        final BookEntryImpl ask = _asks[lvl];
        
        ++_ticks;

        ask.setPrice( price );
    }

    @Override
    public final void setBidPrice( final int lvl, final double price ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        final BookEntryImpl bid = _bids[lvl];
        
        ++_ticks;

        bid.setPrice( price );
    }

    @Override
    public final void deleteThruBid( final int lvl ) {
        deleteThru( _bids, lvl );
    }

    @Override
    public final void deleteFromBid( final int lvl ) {
        deleteFrom( _bids, lvl );
    }

    @Override
    public final void deleteThruAsk( final int lvl ) {
        deleteThru( _asks, lvl );
    }

    @Override
    public final void deleteFromAsk( final int lvl ) {
        deleteFrom( _asks, lvl );
    }

    private void insert( final BookEntryImpl[] entries, final int lvl ) {
        if ( lvl < 0 || lvl >= _maxIdx ) return; // note upper bounds check is gtr OR equal too

        final BookEntryImpl last = entries[_maxIdx ];
        for( int l = _maxIdx ; l > lvl ; --l ) {
            entries[ l ] = entries[ l-1 ];
        }
        
        entries[ lvl ] = last;
        
        // dont need reset the inserted entry as it will be set post this operation
    }

    private void delete( final BookEntryImpl[] entries, final int lvl ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        ++_ticks;
        
        final BookEntryImpl removed = entries[lvl];
        for( int l = lvl ; l < _maxIdx ; ++l ) {
            entries[ l ] = entries[ l+1 ];
        }
        
        entries[ _maxIdx ] = removed;
        removed.clear();
    }

    private void deleteThru( final BookEntryImpl[] entries, final int lvl ) {
        if ( lvl < 0 || lvl > _maxIdx ) return;

        ++_ticks;
        
        /**
         * L1 L2 L3 L4 L5 L6 L7 L8 E1 E2
         * 
         * deleteFrom idx=2 to idx=0  .. ie levelsToMove=7
         * 
         * L4 L5 L6 L7 L8 E1 E2 L1 L2 L3 .... L1/L2/L3 cleared
         */
        
        // backup the entries at top of book that are being erased
        for( int l=0 ; l <= lvl ; l++ ) {
            final BookEntryImpl removed = entries[l];
            removed.clear();
            _tmp[l] = removed; 
        }

        // shift up entries
        int destIdx = 0;
        for( int l = lvl+1 ; l <= _maxIdx ; l++ ) {
            entries[ destIdx++ ] = entries[ l ];
        }

        // put back removed entries at bottom of book
        for( int l = 0 ; l <= lvl ; l++ ) {
            entries[ destIdx++ ] = _tmp[ l ];
        }
    }

    private void deleteFrom( final BookEntryImpl[] entries, final int fromLvl ) {
        ++_ticks;
        for( int l=fromLvl ; l <= _maxIdx ; l++ ) {
            entries[ l ].clear(); 
        }
    }

    private void dirty( final BookEntryImpl[] entries, boolean isDirty ) {
        ++_ticks;
        for( int l=0 ; l <= _maxIdx ; l++ ) {
            entries[ l ].setDirty( isDirty ); 
        }
    }

    private void clear( final BookEntryImpl[] entries ) {
        ++_ticks;
        for( int l=0 ; l <= _maxIdx ; l++ ) {
            entries[ l ].clear(); 
        }
    }
}
