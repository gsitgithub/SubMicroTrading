/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.asia.bse;

import com.rr.core.collections.IntToIntHashMap;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.model.BookListener;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.utils.Utils;
import com.rr.inst.InstrumentStore;
import com.rr.md.book.FixBookLongKeyFactory;
import com.rr.md.book.MktDataController;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;

/**
 * BSE specialisation
 * 
 * Note the array and prodGrpMap could be replaced by an IntHolder in the BookContext .... set in the subscribe method
 */
public final class BSEMarketDataController extends MktDataController<BSEBookAdapter> {

    private static final long ENQUEUE_DURATION_WARN_NANOS_ = 1000;
    
    private int     _secGrpId;
    private int     _curProdSeqNum;
    
    private final int[]              _secGrpSeqNums;            // sequence number for products 0..31   CDX ... 1 to 797 EqD
    private final MDIncRefreshImpl[] _prodGrpPendingChain;
    private final int[]              _chainSize;
    private final int                _prodSeqNumArraySize;
    private final IntToIntHashMap    _prodGrpMap;
    
    private boolean _debug = false;

    private boolean _setDirtyFlag = false;


    public BSEMarketDataController( String                                  id, 
                                    String                                  rec, 
                                    MessageDispatcher                       inboundDispatcher, 
                                    FixBookLongKeyFactory<BSEBookAdapter>   bookFactory,
                                    BookListener<BSEBookAdapter>            mktDataListener, 
                                    InstrumentStore                         instrumentStore, 
                                    boolean                                 enqueueIncUpdatesOnGap,
                                    int                                     maxProdGrpArraySize ) {
        
        super( id, rec, inboundDispatcher, bookFactory, mktDataListener, instrumentStore, enqueueIncUpdatesOnGap );

        _secGrpSeqNums       = new int[ maxProdGrpArraySize ]; // sequence number for products 0..31
        _prodGrpPendingChain = new MDIncRefreshImpl[ maxProdGrpArraySize ];
        _chainSize           = new int[ maxProdGrpArraySize ];
        
        _prodGrpMap          = new IntToIntHashMap( 128, 0.75f );
        
        _prodSeqNumArraySize     = maxProdGrpArraySize;
    }

    public final boolean isDebug() {
        return _debug;
    }

    public final void setDebug( boolean debug ) {
        _debug = debug;
    }

    @Override
    protected final void handleSnapshot( final MDSnapshotFullRefreshImpl msg ) {
        
        if ( msg.getSecurityIDSource() != SecurityIDSource.ExchangeSymbol ) {
            return;
        }
        
        if ( isSubscribed( msg.getSecurityID() ) ) {
            
            processBookChange( msg.getMessageHandler(), msg.getSecurityID() );

            if ( msg.getLastMsgSeqNumProcessed() > _lastBook.getMsgSeqNum() && _secGrpId != 0 ) {
                boolean updated = false;
                
                try {
                    _lastBook.grabLock();
                    
                    if ( _lastBook.applySnapshot( msg, _eventRecycler ) ) {
                        updated = true;
                    }
                    
                    //recoverFromSnapshot();
                    
                } finally {
                    _lastBook.releaseLock();
                }
                
                if ( msg.getLastMsgSeqNumProcessed() > _curProdSeqNum ) {
                    _curProdSeqNum = msg.getLastMsgSeqNumProcessed();
                }
                
                if ( updated ) {
                    
//                    REPLAY ANY PENDING INC UPDATES, RECYCLING ANY OLD
//                    ISSUE IS MdIncRefresh CAN AFFECT MULTIPLE BOOKS !! AND THOSE BOOK SNAPS MAY NOT HAVE ARRIVED YET
//                    SO ONLY REPLAY THE SubElement to the BOOK and RECYCLE THAT ... RECYCLE WHOLE MESSAGE IF ALL SUBELEMS FOR SNAP BOOK
//                    
//                    MAYBE REMOVE SNAPSHOT CODE FROM BOOKS ?
//                                                           
//                    ADD UPPER BOUND TO NUMBER OF PACKETS TO BE HELD PENDING RECOVERY
                                                           
                    
                    notifyUpdate( _lastBook );      // book updated
                }
            } else {
                // ignore old snapshot
            }
        }
    }

    /**
     * handle the INcrementalRefresh : RECYCLE if not enqueued in book
     */
    @Override
    protected final void handleIncrementalRefresh( final MDIncRefreshImpl msg ) {

        MDEntryImpl nextEntry    = (MDEntryImpl) msg.getMDEntries();
        
        if ( isSubscribed( msg, nextEntry) ) {
            if ( nextEntry.getSecurityIDSource() == SecurityIDSource.ExchangeSymbol ) {

                long curSecId = 0;

                while( nextEntry != null ) {
                    curSecId = nextEntry.getSecurityID();

                    processBookChange( msg.getMessageHandler(), curSecId );

                    if ( _secGrpId != 0  ) break;
                    
                    if ( _debug ) {
                        _logMsg.copy( "MDINC SKIP entry : [" ).append( msg.getMsgSeqNum() )
                            .append( " / start " ).append( "] ")
                            .append( ", secGrpId=" ).append( _secGrpId )
                            .append( " book=" ).append( curSecId )
                            .append( " " ).append( _lastBook.getInstrument().getSecurityDesc() )
                            ;
                        
                        _log.info( _logMsg );
                    }
                    
                    nextEntry = nextEntry.getNext();
                }

                if ( _secGrpId != 0  ) {
                    if ( checkApplyEntry( msg ) ) {         
                        
                        doApplyIncrementalUpdate( msg, nextEntry, curSecId, true );
    
                    } else if ( shouldQueueEntry( msg ) ) {  
                        
                        enqueueUpdate( msg );
                        
                        return; // DONT RECYCLE MESSAGE
                    }
                }
            }
        }

        checkForBookDispatch();

        getEventRecycler().recycle( msg );
    }

    private void doApplyIncrementalUpdate( final MDIncRefreshImpl msg, MDEntryImpl nextEntry, long curSecId, boolean isDirty ) {
        try {
            _curProdSeqNum = msg.getMsgSeqNum();

            lock();

            if ( _setDirtyFlag ) _lastBook.setDirty( isDirty );

            int entry = 0;
            int startGrp = _secGrpId;
            
            while( nextEntry != null ) {
                if ( nextEntry.getSecurityIDSource() == SecurityIDSource.ExchangeSymbol ) {
                    
                    final long nextSecId = nextEntry.getSecurityID();
                    
                    if ( nextSecId != curSecId ) {
                        stampChangedBook( msg );

                        unlock();
                        
                        processBookChange( msg.getMessageHandler(), nextSecId );

                        lock();
                        
                        if ( _setDirtyFlag ) _lastBook.setDirty( isDirty );

                        curSecId = nextSecId;
                        
                        if ( startGrp != _secGrpId && _secGrpId != 0 ) {
                            _logMsg.copy( "WARNING: increment update has multiple productGroups, msgSeqNum=[" ).append( msg.getMsgSeqNum() )
                                    .append( " / " ).append( ++entry ).append( "] ")
                                    .append( ", firstSecGrpId=" ).append( startGrp )
                                    .append( ", secGrpId=" ).append( _secGrpId )
                                    .append( " book=" ).append( curSecId )
                                    .append( " " ).append( _lastBook.getInstrument().getSecurityDesc() )
                                    ;
                        
                            _log.info( _logMsg );
                        }
                    }

                    if ( _secGrpId != 0 ) {
                        if ( _debug ) {
                            _logMsg.copy( "MDINC entry : [" ).append( msg.getMsgSeqNum() )
                                .append( " / " ).append( ++entry ).append( "] ")
                                .append( ", secGrpId=" ).append( _secGrpId )
                                .append( " book=" ).append( curSecId )
                                .append( " " ).append( _lastBook.getInstrument().getSecurityDesc() )
                                ;
                            
                            _log.info( _logMsg );
                        }

                        _lastBookChanged |= _lastBook.applyIncrementalEntry( msg.getMsgSeqNum(), nextEntry );
                    } else {
                        if ( _debug ) {
                            _logMsg.copy( "MDINC SKIP entry : [" ).append( msg.getMsgSeqNum() )
                                .append( " / " ).append( ++entry ).append( "] ")
                                .append( ", secGrpId=" ).append( _secGrpId )
                                .append( " book=" ).append( curSecId )
                                .append( " " ).append( _lastBook.getInstrument().getSecurityDesc() )
                                ;
                            
                            _log.info( _logMsg );
                        }
                    }
                }

                nextEntry = nextEntry.getNext();
            }
            
            stampChangedBook( msg );

        } finally {
            unlock();
        }
    }

    // enqueue in asc order as thats order replay requires ... check to see if enqueued update fills gap 
    // @WARNING N+1 function COULD BE ISSUE FOR BIG GAPS
    @SuppressWarnings( "null" )
    private void enqueueUpdate( MDIncRefreshImpl msg ) {
        
        final long startNanos = Utils.nanoTime();
        
        msg.detachQueue();
        
        MDIncRefreshImpl prv  = null;
        MDIncRefreshImpl next = _prodGrpPendingChain[ _secGrpId ];
        
        if ( next == null ) {
            _prodGrpPendingChain[ _secGrpId ] = msg;
            return;
        }

        int diff = 0;

        int nextExpected = _curProdSeqNum+1;
        boolean noGaps = true;
        
        while( next != null ) {
            final int nextMsgSeqNum = next.getMsgSeqNum();
            diff = msg.getMsgSeqNum() - nextMsgSeqNum;

            if ( diff <= 0 ) break;

            if ( nextExpected != nextMsgSeqNum ) {
                noGaps = false;
            }
            
            prv = next;
            next = next.getNext();
            ++nextExpected;
        }

        int curProdSeqNum = _curProdSeqNum;

        if ( diff == 0 ) {
            // duplicate
            getEventRecycler().recycle( msg );
        }

        if ( diff < 0 ) {
            msg.setNext( next );
            if ( prv == null ) {
                _prodGrpPendingChain[ _secGrpId ] = msg;
            } else {
                prv.setNext( msg );
            }
            
            if ( noGaps ) { // check for chain being restored
                while( msg != null ) {
                    final int nextMsgSeqNum = msg.getMsgSeqNum();

                    if ( nextExpected != nextMsgSeqNum ) {
                        noGaps = false;
                        break;
                    }
                    
                    msg = msg.getNext();
                    ++nextExpected;
                }
                
                if ( noGaps ) {
                    recoverGap();
                }
            }
            return;
        }

        if ( diff > 0 ) {
            // got to end of chain, next is null, set prv to msg
            prv.setNext( msg );
    
            if ( noGaps ) {
                recoverGap();
            }
        }

        final long durationNanos = Math.abs(Utils.nanoTime() - startNanos);
        
        if ( noGaps ) {
            int gap = _curProdSeqNum - curProdSeqNum;
            
            _logMsg.copy( "ENQUEUE REPLAY Recovered gap processing took " ).append( durationNanos ).append( " nanos for prodGrp=" ).append( _secGrpId )
                   .append( ", msgSeqNum=" ).append( msg.getMsgSeqNum() )
                   .append( ", gap=" ).append( gap );
                   
            _log.info( _logMsg );
        } else {
            final int chainSize = _chainSize[ _secGrpId ] + 1; 
            _chainSize[ _secGrpId ] = chainSize;

            if ( chainSize > getMaxEnqueueIncUpdatesOnGap() ) {
                
                _logMsg.copy( "ENQUEUE FORCE REPLAY, Exceeded max enqueue size of " ).append( getMaxEnqueueIncUpdatesOnGap() ).append( ", duration " )
                       .append( durationNanos ).append( " nanos for prodGrp=" ).append( _secGrpId )
                       .append( ", curSeqNum=" ).append( _curProdSeqNum )
                       .append( ", newSeqNum=" ).append( msg.getMsgSeqNum() );
                _log.warn( _logMsg );

                recoverGap();
                
            } else {
                if ( durationNanos > ENQUEUE_DURATION_WARN_NANOS_) {
                    _logMsg.copy( "ENQUEUE for gap processing took " ).append( durationNanos ).append( " nanos for prodGrp=" ).append( _secGrpId )
                           .append( ", curSeqNum=" ).append( _curProdSeqNum )
                           .append( ", msgSeqNum=" ).append( msg.getMsgSeqNum() );
                    _log.warn( _logMsg );
                } else {
                    if ( _debug ) {
                        _logMsg.copy( "ENQUEUE due to GAP " ).append( " prodGrp=" ).append( _secGrpId )
                               .append( ", curSeqNum=" ).append( _curProdSeqNum )
                               .append( ", msgSeqNum=" ).append( msg.getMsgSeqNum() );
                        _log.info( _logMsg );
                    }
                }
            }
        }
     }

    /**
     * incremental updates have recovered the GAP
     */
    private void recoverGap() {
        if ( isMarkDirtyEnabled() ) _setDirtyFlag = true; // force mark dirtyflag reset
        
        MDIncRefreshImpl cur = _prodGrpPendingChain[ _secGrpId ];
        MDIncRefreshImpl next = null;

        while( cur != null ) {
            next = cur.getNext();
            cur.detachQueue();
            recover( cur );
            cur = next;
        }

        if ( isMarkDirtyEnabled() ) _setDirtyFlag = false;

        _prodGrpPendingChain[ _secGrpId ] = null;
        _chainSize[ _secGrpId ] = 0;
    }

    private void recover( MDIncRefreshImpl msg ) {
        MDEntryImpl nextEntry    = (MDEntryImpl) msg.getMDEntries();

        long curSecId = nextEntry.getSecurityID();

        processBookChange( msg.getMessageHandler(), curSecId );

        doApplyIncrementalUpdate( msg, nextEntry, curSecId, false );

        checkForBookDispatch();
        
        getEventRecycler().recycle( msg );
    }

    @Override
    protected final void processBookChange( final MessageHandler src, final long securityID ) {
        if ( securityID == getLastSecurityId() ) {
            return;
        }

        if ( _debug ) {
            _logMsg.copy( "changeBook prevBook=" ).append( getLastSecurityId() ).append( ", secGrp=" ).append( _secGrpId ).append( ", curSecNum=" ).append( _curProdSeqNum );
            _log.info( _logMsg );
        }

        storeLatestProdSeqNumBeforeBookChange();
        
        super.processBookChange( src, securityID );
        
        _secGrpId = _lastBook.getInstrument().getSecurityGroupId();

        getProdSeqNumForNewBook();
        
        if ( _debug ) {
            _logMsg.copy( "changeBook newBook=" ).append( securityID ).append( ", secGrp=" ).append( _secGrpId ).append( ", curSecNum=" ).append( _curProdSeqNum );
            _log.info( _logMsg );
        }
    }

    private void getProdSeqNumForNewBook() {
        if ( _secGrpId < _prodSeqNumArraySize ) {
            _curProdSeqNum = _secGrpSeqNums[ _secGrpId ];
        } else {
            _curProdSeqNum = _prodGrpMap.get( _secGrpId );
        }
    }

    private void storeLatestProdSeqNumBeforeBookChange() {
        if ( _curProdSeqNum > 0 ) {
            if ( _secGrpId < _prodSeqNumArraySize ) {
                _secGrpSeqNums[ _secGrpId ] = _curProdSeqNum;
            } else {
                _prodGrpMap.put( _secGrpId, _curProdSeqNum );
            }
        }
    }

    private void stampChangedBook( final MDIncRefreshImpl msg ) {
        if ( _lastBookChanged ) {
            _lastBook.setLastTickInNanos( msg.getReceived() );
            _lastBook.setLastExchangeTickTime( msg.getSendingTime() );
        }
    }

    private void lock() {
        _lastBook.grabLock();
    }

    private void unlock() {
        _lastBook.releaseLock();
    }

    private boolean checkApplyEntry( final MDIncRefreshImpl msg ) {
        if ( ! isMarkDirtyEnabled() ) _setDirtyFlag = false;
        final int productSeqNum = msg.getMsgSeqNum();
        
        if ( (_curProdSeqNum+1) == productSeqNum || _curProdSeqNum == 0 ) {
            return true;
        }
       
        if ( isEnqueueIncUpdatesOnGap() ) {
            return false; // all inc updates enqueued for next snapshot
        }
        
        // if detect gap mark book as dirty and still apply update as long as not old
        
        if ( productSeqNum <= _curProdSeqNum ) {
            return false;  // dont process old seqnum
        }
        
        if ( isMarkDirtyEnabled() ) _setDirtyFlag = true;
        _lastBookChanged = true;                // propogate state change
        
        _errMsg.copy( "Force Update and Mark Dirty, Gap detected on " ).append( _lastBook.getInstrument().getSecurityDesc() ).append( ", bookId=" ).append( _lastBook.getInstrument().getLongSymbol() );
        _errMsg.append( ", lastSeqNum=" ).append( _curProdSeqNum ).append( ", gapSeqNum=" ).append( productSeqNum );
        _errMsg.append( ", gapOf=" ).append( productSeqNum-_curProdSeqNum );
        
        _log.warn( _errMsg );
        
        return true;
    }

    private boolean shouldQueueEntry( final MDIncRefreshImpl msg ) {
        final int bookSeqNum      = _lastBook.getMsgSeqNum();
        final int eventBookSeqNum = msg.getMsgSeqNum();
        
        if ( eventBookSeqNum <= bookSeqNum ) {
            return false;  // OLD dont enqueue
        }
        
        setLastBookDirty();
        _lastBookChanged = true;                // propogate state change
        
        _errMsg.copy( "Holdup update as Gap detected, lastBookId=" ).append( _lastBook.getInstrument().getLongSymbol() );
        _errMsg.append( ", msgSeqNum=" ).append( msg.getMsgSeqNum());
        _errMsg.append( ", lastSeqNum=" ).append( bookSeqNum ).append( ", gapSeqNum=" ).append( eventBookSeqNum );
        _errMsg.append( ", gapOf=" ).append( eventBookSeqNum-bookSeqNum );
        
        _log.warn( _errMsg );
        
        return true;
    }

}
