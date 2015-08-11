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

public abstract class BaseL1Book extends BaseFixedSizeBook {

    private final BookEntryImpl _bid = new BookEntryImpl();
    private final BookEntryImpl _ask = new BookEntryImpl();

    public BaseL1Book( Instrument instrument ) { 
        super( instrument, 1 );
    }

    @Override
    public final void snap( final ApiMutatableBook dest ) {
        if ( _numLevels > 0 ) {
            dest.setLevel( 0, _bid.getQty(), _bid.getPrice(), _bid.isDirty(), _ask.getQty(), _ask.getPrice(), _ask.isDirty() );
        }
        dest.setNumLevels( _numLevels );
    }
    
    @Override
    public final boolean getLevel( final int lvl, final DoubleSidedBookEntry dest ) {
        if ( lvl > _numLevels ) {
            return false;
        }
        dest.set( _bid, _ask );
        return true;
    }
    

    @Override
    public final void clear() {
        _ask.clear();
        _bid.clear();
    }

    @Override
    public final void setLevel( final int lvl, final int buyQty, final double buyPrice, final boolean buyIsDirty, final int sellQty, final double sellPrice, final boolean sellIsDirty ) {
        if ( lvl == 0 ) {
            ++_ticks;

            _bid.set( buyQty, buyPrice, buyIsDirty );
            _ask.set( sellQty, sellPrice, sellIsDirty );
        }
    }

    @Override
    public final void setLevel( final int lvl, final BookEntryImpl bid, final BookEntryImpl ask ) {
        if ( lvl == 0 ) {
            ++_ticks;

            _bid.set( bid );
            _ask.set( ask );
        }
    }
    
    @Override
    public final void setNumLevels( final int lvl ) {
        _numLevels = lvl;
    }

    @Override
    public final int getActiveLevels() {
        return _numLevels;
    }

    @Override
    public final void dump( final ReusableString dest ) {
        dest.append( "Book " ).append( _instrument.getSecurityDesc() ).append( " " ).append( _instrument.getLongSymbol() ).append( " [L0] " );
        dest.append( (_bid.isDirty()) ? "D " : "  " );
        dest.append( _bid.getQty() ).append( " x " ).append( _bid.getPrice() ).append( "  :  " );
        dest.append( _ask.getPrice() ).append( " x " ).append( _ask.getQty() );
        dest.append( (_ask.isDirty()) ? " D" : "  " );
    }
    
    @Override
    public final boolean getBidEntry( final int lvl, final BookLevelEntry dest ) {
        if ( lvl == 0 ) {
            dest.set( _bid.getQty(), _bid.getPrice() );
            return true;
        }
        return false; 
    }

    @Override
    public final boolean getAskEntry( final int lvl, final BookLevelEntry dest ) {
        if ( lvl == 0 ) {
            dest.set( _ask.getQty(), _ask.getPrice() );
            return true;
        }
        return false; 
    }

    @Override
    public final void setBid( final int lvl, final BookLevelEntry entry ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _bid.set( entry.getQty(), entry.getPrice() );
        }
    }

    @Override
    public final void insertBid( final int lvl, final BookLevelEntry entry ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _bid.set( entry.getQty(), entry.getPrice() );
        }
    }

    @Override
    public final void deleteBid( final int lvl ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _bid.clear();
        }
    }

    @Override
    public final void setAsk( final int lvl, final BookLevelEntry entry ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _ask.set( entry.getQty(), entry.getPrice() );
        }
    }

    @Override
    public final void insertAsk( final int lvl, final BookLevelEntry entry ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _ask.set( entry.getQty(), entry.getPrice() );
        }
    }

    @Override
    public final void deleteAsk( final int lvl ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _ask.clear();
        }
    }
    
    @Override
    public final boolean isValidBBO() {
        return( _bid.isValid() && _ask.isValid() );   
    }

    @Override
    public final void setBidQty( final int lvl, final int qty ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _bid.setQty( qty );
        }
    }

    @Override
    public final void setBidPrice( final int lvl, final double px ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _bid.setPrice( px );
        }
    }

    @Override
    public final void setAskQty( final int lvl, final int qty ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _ask.setQty( qty );
        }
    }

    @Override
    public final void setAskPrice( final int lvl, final double px ) {
        if ( lvl == 0 ) {
            ++_ticks;
            _ask.setPrice( px );
        }
    }

    @Override
    public final void deleteThruBid( final int lvl ) {
        if ( lvl == 0 ) {
            _bid.clear();
        }
    }

    @Override
    public final void deleteFromBid( final int lvl ) {
        if ( lvl == 0 ) {
            _bid.clear();
        }
    }

    @Override
    public final void deleteThruAsk( final int lvl ) {
        if ( lvl == 0 ) {
            _ask.clear();
        }
    }

    @Override
    public final void deleteFromAsk( final int lvl ) {
        if ( lvl == 0 ) {
            _ask.clear();
        }
    }

    @Override
    public final void setDirty( final boolean isDirty ) {
        _bid.setDirty( isDirty );
        _ask.setDirty( isDirty );
    }

    @Override
    public final void setBidDirty( final boolean isDirty ) {
        _bid.setDirty( isDirty );
    }

    @Override
    public final void setAskDirty( final boolean isDirty ) {
        _ask.setDirty( isDirty );
    }
}
