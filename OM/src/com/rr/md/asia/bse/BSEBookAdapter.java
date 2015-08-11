/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.asia.bse;

import com.rr.core.lang.Constants;
import com.rr.core.lang.ErrorCode;
import com.rr.core.model.Instrument;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.model.book.UnsafeL1Book;
import com.rr.md.book.l2.BaseL2FixBook;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.interfaces.MDEntry;
import com.rr.om.dummy.warmup.DummyInstrument;


/**
 * BSE FastFix market data event book wrapper
 *  
 * Wrapper which adapts BSE fix events to mutable book
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
public final class BSEBookAdapter extends BaseL2FixBook {

    public static final BSEBookAdapter DUMMY = makeDummy(DummyInstrument.DUMMY);           // DUMMY instance to avoid null checks and repeated calls to book lookups for unknown insts

    private static final ErrorCode ERR_MIXED_UPDATE = new ErrorCode( "BSEB10", "Mismatched MDincRefresh entries during replay " );
    
    private int                 _lastEventSeqNum;

    private MDIncRefreshImpl    _rootEnqueue;
    private MDIncRefreshImpl    _lastEnqueued;

    public BSEBookAdapter( ApiMutatableBook book ) {
        super( book );
    }

    @Override
    public int getLastTickId() {
        return _lastEventSeqNum;
    }

    @Override 
    public int getMsgSeqNum() { 
        return _lastEventSeqNum; 
    }
    
    @Override 
    public void setMsgSeqNum( final int seqNum ) { 
        _lastEventSeqNum = seqNum; 
    }
    
    /**
     * setLastTickId ... only used when L2FixBook passed into snap call
     */
    @Override
    public void setLastTickId( int tickId ) {
        _lastEventSeqNum = tickId;
    }

    public void enqueue( MDIncRefreshImpl msg ) {
        if ( _rootEnqueue == null ) {
            _rootEnqueue = _lastEnqueued = msg;
        } else  {
            _lastEnqueued.setNext( msg );
            _lastEnqueued = msg;
        }
    }

    private static BSEBookAdapter makeDummy( Instrument inst ) {
        BSEBookAdapter book = new BSEBookAdapter( new UnsafeL1Book( inst ) );
        
        book.setMsgSeqNum( Integer.MAX_VALUE-1 );
        
        book.setLastTickId( 0 ); 
        
        return book;
    }

    @Override
    protected void setSeqNums( int msgSeqNum, int rptSeq ) {
        _lastEventSeqNum = msgSeqNum;
    }
    
    @Override
    protected final void applyBid( final MDEntry entry ) {
        final int lvl = entry.getMdPriceLevel() - 1;

        switch( entry.getMdUpdateAction() ) {
        case New: {
                _tmpEntry.set( entry.getMdEntrySize(), entry.getMdEntryPx() );
                _book.insertBid( lvl, _tmpEntry );
            }
            break;
        case Change: { // should be used ONLY for qty change, but CME use for price setting as well
                final double px = entry.getMdEntryPx();
                if ( px != Constants.UNSET_DOUBLE ) {
                    _tmpEntry.set( entry.getMdEntrySize(), px );
                    _book.setBid( lvl, _tmpEntry );
                } else {
                    _book.setBidQty( lvl, entry.getMdEntrySize() );
                }
            }
            break;
        case Delete: {
                _book.deleteBid( lvl );
            }
            break;
        case DeleteThru: 
            _book.deleteThruBid( lvl );
            break;
        case DeleteFrom: 
            _book.deleteFromBid( lvl );
            break;
        case Overlay: {  
                final int qty = entry.getMdEntrySize();
                if ( qty != Constants.UNSET_INT ) {
                    _tmpEntry.set( qty, entry.getMdEntryPx() );
                    _book.setBid( lvl, _tmpEntry );
                } else {
                    _book.setBidPrice( lvl, entry.getMdEntryPx() );
                }
            }
            break;
        case Unknown:
            break;
        }
    }

    @Override
    protected final void applyAsk( final MDEntry entry ) {
        final int lvl = entry.getMdPriceLevel() - 1;

        switch( entry.getMdUpdateAction() ) {
        case New: {
                _tmpEntry.set( entry.getMdEntrySize(), entry.getMdEntryPx() );
                _book.insertAsk( lvl, _tmpEntry );
            }
            break;
        case Change: {
                final double px = entry.getMdEntryPx();
                if ( px != Constants.UNSET_DOUBLE ) {
                    _tmpEntry.set( entry.getMdEntrySize(), px );
                    _book.setAsk( lvl, _tmpEntry );
                } else {
                    _book.setAskQty( lvl, entry.getMdEntrySize() );
                }
            }
            break;
        case Delete: {
                _book.deleteAsk( lvl );
            }
            break;
        case DeleteThru:
            _book.deleteThruAsk( lvl );
            break;
        case DeleteFrom:
            _book.deleteFromAsk( lvl );
            break;
        case Overlay: {
                final int qty = entry.getMdEntrySize();
                if ( qty != Constants.UNSET_INT ) {
                    _tmpEntry.set( qty, entry.getMdEntryPx() );
                    _book.setAsk( lvl, _tmpEntry );
                } else {
                    _book.setAskPrice( lvl, entry.getMdEntryPx() );
                }
            }
            break;
        case Unknown:
            break;
        }
    }
    
    @Override
    protected final void applyTrade( MDEntry entry ) {
        switch( entry.getMdUpdateAction() ) {
        case New: 
        case Change: 
            _lastTradeQty   = entry.getMdEntrySize();
            _lastTradePrice = entry.getMdEntryPx();
            
            _totalTradeVol += _lastTradeQty;
            _totalTraded   += (_lastTradeQty * _lastTradePrice);
            break;
        case Delete: 
            final int    tradeQty   = entry.getMdEntrySize();
            final double tradePrice = entry.getMdEntryPx();
            
            _totalTradeVol -= tradeQty;
            _totalTraded   -= (tradeQty * tradePrice);
            break;
        case DeleteThru:
        case DeleteFrom:
        case Overlay: 
        case Unknown:
            break;
        }
    }

    /**
     * @param snapSeq the seqNum of the snap event
     * @param i 
     * 
     * @return true if events applied ok with NO gap
     */
    @Override
    protected final boolean replayQueued( int lastSeqNumProcessed, int snapSeq, EventRecycler entryRecycler ) {
        
        if ( _rootEnqueue == null ) return true;
            
        MDIncRefreshImpl nxt = _rootEnqueue;
        MDIncRefreshImpl tmp = null;
        
        int nextExpSeq = snapSeq + 1;
        
        boolean ok = true;

        int cnt = 0;
        
        while( nxt != null ) {
        
            ++cnt;
            final int seqNum = nxt.getMsgSeqNum();
            
            tmp = nxt.getNext();
            nxt.setNext( null );
            
            if ( nxt.getMsgSeqNum() < snapSeq ) {
                // ignore old update
            } else if ( seqNum == nextExpSeq ) {
                applyMdIncRefresh( _lastEventSeqNum, nxt );
            
                ++nextExpSeq;
                
            } else {
                _errMsg.reset();
                id( _errMsg );
                // tag 83 not tag 34
                _errMsg.append( " Gap detected in REPLAY, bookSeqNo=" ).append( snapSeq ).append( ", expSeqNo=" ).append( nextExpSeq ).append( ", got=" ).append( seqNum );

                _log.warn( _errMsg );
                
                ok = false;
            }
            
            entryRecycler.recycle( nxt );
            
            nxt = tmp;
        }
        
        if ( cnt > LOG_THRESHOLD ) {
            _errMsg.reset();
            id( _errMsg );
            _errMsg.append( " large replay , bookSeqNo=" ).append( snapSeq ).append( ", expSeqNo=" ).append( nextExpSeq ).append( ", replayed" ).append( cnt );

            _log.warn( _errMsg );
        }
        
        _rootEnqueue = _lastEnqueued = null;
        
        return ok;
    }

    private void applyMdIncRefresh( int lastEventSeqNum, MDIncRefreshImpl msg ) {
        MDEntryImpl nextEntry    = (MDEntryImpl) msg.getMDEntries();
        
        boolean bookChanged = false;
        
        while( nextEntry != null ) {
            if ( nextEntry.getSecurityIDSource() != SecurityIDSource.ExchangeSymbol ) {
                continue;
            }

            if ( nextEntry.getSecurityID() != getInstrument().getLongSymbol() ) {
                _errMsg.copy( "MDIncRefresh msgSeqNum=" ).append( msg.getMsgSeqNum() ).
                        append( ", book instId ").append( getInstrument().getLongSymbol() ).
                        append( " mismatches against securityId of ").append( nextEntry.getSecurityID() ).
                        append( " event : " );
                
                nextEntry.dump( _errMsg );
                
                _log.error( ERR_MIXED_UPDATE, _errMsg );

                return;
            }
            
            bookChanged = applyIncrementalEntry( msg.getMsgSeqNum(), nextEntry );

            nextEntry = nextEntry.getNext();
        }

        if ( bookChanged ) {
            setLastTickInNanos( msg.getReceived() );
            setLastExchangeTickTime( msg.getSendingTime() );
        }
    }
}
