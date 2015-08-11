/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rr.core.admin.AdminAgent;
import com.rr.core.collections.LongHashMap;
import com.rr.core.collections.LongHashSet;
import com.rr.core.collections.LongMap;
import com.rr.core.component.SMTStartContext;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.Stopable;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.BookListener;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.utils.FileUtils;
import com.rr.inst.InstrumentStore;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;
import com.rr.model.generated.internal.events.impl.MsgSeqNumGapImpl;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.model.generated.internal.events.impl.SecurityStatusImpl;
import com.rr.model.generated.internal.type.SecurityUpdateAction;

/**
 * CME Market Data Controller
 * 
 * threadsafe handler
 * 
 * has an inboundDispatcher to allow POSSIBLE async processing of the event generating thread (depending which dispatcher used) 
 * 
 * handleNow (and below) must only be invoked by dispatch thread
 * 
 * book owned by this controller and only mutated by this controller, hence all writing must be locked to protect against snapping of book while its updated 
 *
 * long securityId subscription market data controller
 * 
 * All subscriptions loaded from file are considered expected/supported instruments
 * 
 * THIS IS RELIANT ON CORRECT CONFIG AND PATCHING TOGETHER OF SESSION TO MKTDATACONTROLLER's
 */

public abstract class MktDataController<T extends MutableFixBook> implements Stopable, BookSource<T>  {

    protected static final Logger       _log = LoggerFactory.create( MktDataController.class );

    private static final ErrorCode MKTERR1 = new ErrorCode( "MDC001", "MODEL CHANGED " );
    
    private final MessageDispatcher _inboundDispatcher;
    private       String            _id;
    private       boolean           _running = false;
    protected     EventRecycler     _eventRecycler;

    // error message ONLY for use on the dispatch callback
    protected final ReusableString _errMsg = new ReusableString();

    private Map<T, MessageHandler>      _books            = new HashMap<T, MessageHandler>( 128 );
    private LongMap<T>                  _activeBooksById  = new LongHashMap<T>( 128, 0.75f );
    private LongMap<T>                  _pendingBooksById = new LongHashMap<T>( 128, 0.75f );

    private   long                      _lastSecurityId;
    protected T                         _lastBook;
    
    private FixBookLongKeyFactory<T>    _bookFactory;
    
    protected boolean                   _lastBookChanged;

    private final boolean               _enqueueIncUpdatesOnGap;
    
    private final BookListener<T>       _mktDataListener;

    private final InstrumentStore       _instrumentStore;

    protected final ZString             _srcREC;

    private boolean _allowIntradaySecurityUpdates = false;

    // subscriptions
    private final LongHashSet    _subs    = new LongHashSet( 128, 0.75f );
    private final LongHashSet    _ignored = new LongHashSet( 128, 0.75f );

    private final Set<ZString>   _secDefs = new HashSet<ZString>();
    private final Set<ZString>   _pending = new HashSet<ZString>();

    protected final ReusableString _logMsg  = new ReusableString();
    
    private       List<String>   _pipeLineIds = new ArrayList<String>();

    // override the isSubscribed check to allow all updates
    private       boolean        _overrideSubscribeSet = false;
    private       boolean        _disableDirtyAllBooksOnPacketGap = false;
    private       boolean        _ignoreDirtyOnGap = false;
    private       int            _maxEnqueueIncUpdatesOnGap = 10;

    public MktDataController( String                    id,
                              String                    rec,
                              MessageDispatcher         inboundDispatcher, 
                              FixBookLongKeyFactory<T>  bookFactory, 
                              BookListener<T>           mktDataListener,
                              boolean                   enqueueIncUpdatesOnGap ) {
        
        this( id, rec, inboundDispatcher, bookFactory, mktDataListener, null, enqueueIncUpdatesOnGap );
    }

    public MktDataController( String                    id, 
                              String                    rec,
                              MessageDispatcher         inboundDispatcher, 
                              FixBookLongKeyFactory<T>  bookFactory, 
                              BookListener<T>           mktDataListener, 
                              InstrumentStore           instrumentStore,
                              boolean                   enqueueIncUpdatesOnGap ) {
        super();
        
        _inboundDispatcher = inboundDispatcher;
        _id                = id;
        _bookFactory       = bookFactory;
        _mktDataListener   = mktDataListener;
        _instrumentStore   = instrumentStore;
        _srcREC            = new ViewString( rec );
        
        _enqueueIncUpdatesOnGap = enqueueIncUpdatesOnGap;

        _inboundDispatcher.setHandler( this );

        if ( Math.abs( EventIds.ID_MSGSEQNUMGAP             - EventIds.ID_MDINCREFRESH)             != 1 || 
             Math.abs( EventIds.ID_MDINCREFRESH             - EventIds.ID_MDSNAPSHOTFULLREFRESH)    != 1 ||
             Math.abs( EventIds.ID_MDSNAPSHOTFULLREFRESH    - EventIds.ID_SECURITYDEFINITION)       != 1 || 
             Math.abs( EventIds.ID_SECURITYDEFINITION       - EventIds.ID_SECURITYDEFINITIONUPDATE) != 1 || 
             Math.abs( EventIds.ID_SECURITYDEFINITIONUPDATE - EventIds.ID_PRODUCTSNAPSHOT)          != 1 ||    
             Math.abs( EventIds.ID_PRODUCTSNAPSHOT          - EventIds.ID_SECURITYSTATUS)           != 1 ) {

            _log.error( MKTERR1, "REGENERATE MODEL AND PUT MDINCREFRESH EVENTS BACK TOGETHER !" );
        }
        
        MktDataControllerAdmin adminBean = new MktDataControllerAdmin( this );
        AdminAgent.register( adminBean );
    }

    // invoked via reflection
    public final void clear() {
        _books.clear();
        _activeBooksById.clear();
        _pendingBooksById.clear();

        _lastSecurityId = 0;
        _lastBook = null;
        _lastBookChanged = false;
        
        _mktDataListener.clear();
    }
    
    @Override
    public final String getComponentId() {
        return _id;
    }

    @Override
    public final void threadedInit() {
        _eventRecycler = new EventRecycler();

        _running = true;
    }

    @Override
    public final void handle( final Message msg ) {
        if ( msg != null ) {
            _inboundDispatcher.dispatch( msg );
        }
    }
    
    public final boolean isAllowIntradaySecurityUpdates() {
        return _allowIntradaySecurityUpdates;
    }
    
    public final void setAllowIntradaySecurityUpdates( boolean allowIntradaySecurityUpdates ) {
        _allowIntradaySecurityUpdates = allowIntradaySecurityUpdates;
    }

    public final boolean isDisableDirtyAllBooksOnPacketGap() {
        return _disableDirtyAllBooksOnPacketGap;
    }
    
    public final void setDisableDirtyAllBooksOnPacketGap( boolean dirtyAllBooksOnChannelOnPacketGap ) {
        _disableDirtyAllBooksOnPacketGap = dirtyAllBooksOnChannelOnPacketGap;
    }

    public final boolean isIgnoreDirtyOnGap() {
        return _ignoreDirtyOnGap;
    }
    
    public final boolean isMarkDirtyEnabled() {
        return !_ignoreDirtyOnGap;
    }
    
    public final void setIgnoreDirtyOnGap( boolean ignoreDirtyOnGap ) {
        _ignoreDirtyOnGap = ignoreDirtyOnGap;
    }

    public final int getMaxEnqueueIncUpdatesOnGap() {
        return _maxEnqueueIncUpdatesOnGap;
    }

    public final void setMaxEnqueueIncUpdatesOnGap( int maxEnqueueIncUpdatesOnGap ) {
        _maxEnqueueIncUpdatesOnGap = maxEnqueueIncUpdatesOnGap;
    }

    @Override
    public final void handleNow( final Message msg ) {

        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_MDINCREFRESH:
            handleIncrementalRefresh( (MDIncRefreshImpl) msg );

            return; // RETURN DONT RECYCLE .. BSE for example may hold onto whole message
            
        case EventIds.ID_MDSNAPSHOTFULLREFRESH:
            handleSnapshot( (MDSnapshotFullRefreshImpl) msg );
            break;
        case EventIds.ID_MSGSEQNUMGAP:
            handleGap( (MsgSeqNumGapImpl) msg );
            break;
        case EventIds.ID_SECURITYDEFINITION:
            if ( _allowIntradaySecurityUpdates ) {
                SecurityDefinitionImpl secDef = (SecurityDefinitionImpl) msg;
                applySecurity( secDef );
                // instrument store recycles
            } else {
                getEventRecycler().recycle( msg ); // DISABLE INTRADAY SECURITYDEF DUE TO LOCKING OVERHEAD
            }
            return;
        case EventIds.ID_SECURITYSTATUS:
            if ( _allowIntradaySecurityUpdates ) {
                statusUpdate( msg );
                // instrument store recycles
            } else {
                getEventRecycler().recycle( msg ); // DISABLE INTRADAY SECURITYDEF DUE TO LOCKING OVERHEAD
            }
            return;
        case EventIds.ID_SECURITYDEFINITIONUPDATE: // @TODO add support for security def update
        case EventIds.ID_PRODUCTSNAPSHOT:
        default:
            // dont care
            break;
        }
        
        getEventRecycler().recycle( msg );
    }

    @Override
    public final boolean canHandle() {
        return _running;
    }

    @Override
    public final void stop() {
        _inboundDispatcher.setStopping();
        if ( _mktDataListener instanceof Stopable ) {
            ((Stopable)_mktDataListener).stop();
        }
    }
    
    @Override
    public final boolean supports( Instrument inst ) {
        return _secDefs.contains( inst.getSecurityDesc() );
    }

    /**
     * @param securityDescription fix tag 107 - security description eg  QCZ5-QCN7 
     */
    @Override
    public synchronized T subscribe( final Instrument instr ) {
        
        final long intKey = instr.getLongSymbol();
        
        _logMsg.copy( "MktDataController: " + getComponentId() + " subscribe to instrument " ).
                      append( "secDes=" ).append( instr.getSecurityDesc() ).
                      append( ", ric=" ).append( instr.getRIC() ).
                      append( " has intKey=" ).append( intKey );
        
        _log.info( _logMsg );
        
        _subs.add( intKey );
        _secDefs.add( instr.getSecurityDesc() );
       
        return getBookForSubscription( intKey ); 
    }
    
    /**
     * read in subscriptions from file, with one securityDescription or int symbol id per line
     * 
     * @param subscriptionFile
     * @throws IOException
     */
    public synchronized void addSubscriptions( String subscriptionFile ) throws IOException {
        BufferedReader rdr = new BufferedReader( new InputStreamReader( new FileInputStream( subscriptionFile ) ) );

        _log.info( "MktDataController.addSubscriptions  file " + subscriptionFile );
        
        try {
            ReusableString secDes = new ReusableString();
            
            int count = 0;
            int fail = 0;
                    
            String line;
            while( (line = rdr.readLine()) != null ) {
                line = line.trim();
                if ( line.length() > 0 && !line.startsWith( "#" ) ) {
                    ++count;
                    
                    try {
                        int intKey = Integer.parseInt( line );

                        subscribe( intKey );
                        
                    } catch( NumberFormatException e ) { // assume its security description

                        secDes.copy( line );

                        final Instrument instr = getInstrumentStore().getInstrumentBySecurityDesc( secDes );
                        
                        if ( instr == null ) {
                            _logMsg.copy( "MktDataController: Unable to subscribe to instrument secDes=" ).append( secDes );
                            _log.info( _logMsg );

                            _pending.add( new ReusableString(secDes) );
                            
                            ++fail;
                        } else if ( subscribe( instr ) == null ) {
                            ++fail;
                        }
                    }
                }
            }

            _log.info( "MktDataController.addSubscriptions  entries=" + count + ", failed=" + fail );
            
        } finally {
            FileUtils.close( rdr );
        }
    }

    public final void setPipeIdList( String pipeIdList ) {
        List<String> pipeLineIds = new ArrayList<String>();
        
        if ( pipeIdList != null ) {
            String[] parts = pipeIdList.split( "," );
            
            for( String part : parts ) {
                part = part.trim();
                
                if ( part.length() > 0 ) {
                    pipeLineIds.add( part );
                }
            }
        }
        
        _pipeLineIds = pipeLineIds;
    }
    
    @Override
    public final boolean hasPipeLineId( String pipeLineId ) {
        return _pipeLineIds.contains( pipeLineId );
    }
    
    @Override
    public final List<String> getPipeLineIds() {
        return _pipeLineIds;
    }

    @Override
    public void startWork() {
        // nothing
    }

    @Override
    public void stopWork() {
        stop();
    }

    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }

    @Override
    public void prepare() {
        // nothing
    }
    
    public final boolean isOverrideSubscribeSet() {
        return _overrideSubscribeSet;
    }
    
    /**
     * ignore subscription list and process all updates
     * 
     * @param overrideSubscribeSet
     */
    public final void setOverrideSubscribeSet( boolean overrideSubscribeSet ) {
        _overrideSubscribeSet = overrideSubscribeSet;
    }

    protected final InstrumentStore getInstrumentStore() {
        return _instrumentStore;
    }

    protected final boolean isSubscribed( final MDIncRefreshImpl msg, final MDEntryImpl nextEntry ) {

        if ( nextEntry == null ) return false;

        return isSubscribed( nextEntry.getSecurityID() );
    }

    private void logIgnored( final long key ) {
        if ( _ignored.add( key ) ) {
            _logMsg.copy( "MktDataController: Ignoring market data event for instrument int key " ).append( key );
            _log.info( _logMsg );
        }
    }

    private void statusUpdate( final Message msg ) {
        final SecurityStatusImpl secStatus = (SecurityStatusImpl) msg;
        if ( _instrumentStore != null && _instrumentStore.updateStatus( secStatus ) ) {
            if ( secStatus.getSecurityIDSource() != SecurityIDSource.ExchangeSymbol ) {
                return;
            }
            
            processBookChange( msg.getMessageHandler(), secStatus.getSecurityID() );
            _lastBookChanged = true;
        }
    }

    private void handleGap( final MsgSeqNumGapImpl msg ) {
        gapDetected( msg.getMessageHandler(), msg.getChannelId(), msg.getPrevSeqNum(), msg.getMsgSeqNum() );
    }

    protected abstract void handleSnapshot( final MDSnapshotFullRefreshImpl msg );

    protected final boolean isSubscribed( final long securityId ) {
        
        if ( _overrideSubscribeSet ) {
            return true;
        }
        
        if ( _subs.contains( securityId ) ) {
            return true;
        }
     
        logIgnored( securityId );
        
        return false;
    }

    protected abstract void handleIncrementalRefresh( final MDIncRefreshImpl msg );
    
    protected void notifyUpdate( final T book ) {
        _mktDataListener.changed( book );
    }

    protected final void setLastBookDirty() {
        if ( isMarkDirtyEnabled() ) {
            try {
                _lastBook.grabLock();
                
                _lastBook.setDirty( true );
            } finally {
                _lastBook.releaseLock();
            }
        }
    }

    protected void processBookChange( final MessageHandler src, final long securityID ) {
        if ( securityID == getLastSecurityId() ) {
            return;
        }
        
        T book = getBook( src, securityID );
        
        checkForBookDispatch();
        
        _lastSecurityId = securityID;
        _lastBook = book;
    }

    private T getBookForSubscription( final long securityID ) {
        T book = _activeBooksById.get( securityID );
        
        if ( book == null ) {
            book = _pendingBooksById.get( securityID );
            if ( book == null ) {
                book = _bookFactory.create( securityID, _srcREC );
                _pendingBooksById.put( securityID, book );
            }
        }
        
        return book;
    }

    private T getBook( final MessageHandler src, final long securityID ) {
        T book = _activeBooksById.get( securityID );
        
        if ( book == null ) {
            book = _pendingBooksById.get( securityID );
            if ( book == null ) {
                book = _bookFactory.create( securityID, _srcREC );
            }
            _activeBooksById.put( securityID, book );
            _books.put( book, src );
        }
        
        return book;
    }

    protected final void checkForBookDispatch() {
        if ( _lastBookChanged ) {
            notifyUpdate( _lastBook );      // about to change book and flag indicating items of interest so dispatch update
            _lastBookChanged = false;
        }
    }

    protected final long getLastSecurityId() {
        return _lastSecurityId;
    }

    private void gapDetected( final MessageHandler src, final int channelId, final int lastSeqNum, final int seqNum ) {
        if ( lastSeqNum == 0 ) {
            return; // first message, no real gap
        }
        
        if ( _disableDirtyAllBooksOnPacketGap == false ) {
            _errMsg.copy( "Packet Gap detected, mark books as dirty against src=" );
            _errMsg.append( src.getComponentId() ).append( ", channelId=" ).append( channelId ).
                    append( ", lastSeqNum=" ).append( lastSeqNum ).append( ", gapSeqNum=" ).append( seqNum );
            _errMsg.append( ", gap=" ).append( seqNum - lastSeqNum );
            
            _log.warn( _errMsg );
    
            markAllDirty( src, channelId, seqNum );
            
        } else {
            _errMsg.copy( "Packet Gap detected, IGNORING, against src=" );
            _errMsg.append( src.getComponentId() ).append( ", channelId=" ).append( channelId ).
                    append( ", lastSeqNum=" ).append( lastSeqNum ).append( ", gapSeqNum=" ).append( seqNum );
            _errMsg.append( ", gap=" ).append( seqNum - lastSeqNum );
            
            _log.warn( _errMsg );
        }
    }

    private void markAllDirty( final MessageHandler src, final int channelId, final int seqNum ) {
        
        for( Map.Entry<T, MessageHandler> entry : _books.entrySet() ) {

            final MessageHandler bookHandler = entry.getValue();
            if ( bookHandler == src ) {
                
                final T book = entry.getKey();

                if ( channelId > 0 ) {
                    // can filter by channelId
                    
                    if ( book.getInstrument().getIntSegment() != channelId ) {
                        continue;
                    }
                }
                
                try {
                    book.grabLock();
                    
                    book.setDirty( true );
                    
                    if ( seqNum == 0 ) {
                        book.setMsgSeqNum( 0 );
                        book.setLastTickId( 0 );
                    }
                } finally {
                    book.releaseLock();
                }
            }
        }
    }
    
    private void applySecurity( final SecurityDefinitionImpl secDef ) {

        if ( ! isSubscribed( secDef.getSecurityID() ) ) {
            return;
        }

        if ( secDef.getSecurityUpdateAction() == SecurityUpdateAction.Delete ) {
            _instrumentStore.remove( secDef, secDef.getSecurityExchange() );
        } else {
            _instrumentStore.add( secDef, secDef.getSecurityExchange() );
        }
        
        ZString secDes = secDef.getSecurityDesc();
        
        if ( _pending.contains( secDes ) ) {
            _logMsg.copy( "MktDataController: " + getComponentId() + " dynamically added instrument which had pending subscribe to instrument " ).
                    append( "secDes=" ).append( secDes );

            _log.info( _logMsg );

            final Instrument instr = getInstrumentStore().getInstrumentBySecurityDesc( secDes );
            
            if ( instr != null ) {
                if ( subscribe( instr ) != null ) {
                    _pending.remove( secDes );
                }
            }
        }
    }
    
    private boolean subscribe( final int intKey ) {
        _subs.add( intKey );
        
        final Instrument instr = getInstrumentStore().getInstrumentByID( _srcREC , intKey );
        ZString secDef = instr.getSecurityDesc();
        
        if ( secDef != null && secDef.length() > 0 ) {
            _logMsg.copy( "MktDataController: " + getComponentId() + " subscribing to instrument intKey=" ).append( intKey );
            
            _secDefs.add( secDef );
            
            _logMsg.append( ", secDesc=" ).append( secDef );

            _log.info( _logMsg );
            
            return true;
        } 

        _logMsg.copy( "MktDataController: " + getComponentId() + " UNKNOWN INSTRUMENT intKey=" ).append( intKey );

        return false;
    }

    public final boolean isEnqueueIncUpdatesOnGap() {
        return _enqueueIncUpdatesOnGap;
    }

    protected final EventRecycler getEventRecycler() {
        return _eventRecycler;
    }

    /**
     * Admin Command Control : TO BE USED MANUALLY ONLY DUE TO GC AND POSSIBLE THREAD CONTENTION
     */

    void clearAllBooks() {
        _log.info( "clearAllBooks invoked" );

        for( MutableFixBook book : _books.keySet() ) {
            book.clear();
        }
    }

    boolean clearBook( String securityDesc ) {
        _log.info( "clearBook invoked " + securityDesc );

        for( MutableFixBook book : _books.keySet() ) {
            if ( book.getInstrument().getSecurityDesc().equals( securityDesc ) ) {
                book.clear();
                
                return true;
            }
        }
        
        return false;
    }

    boolean getBook( String securityDesc, ApiMutatableBook dest ) {
        for( Book book : _books.keySet() ) {
            if ( book.getInstrument().getSecurityDesc().equals( securityDesc ) ) {
                book.snap( dest );
                
                return true;
            }
        }

        return false;
    }

    Iterator<T> getBookIterator() {
        return _books.keySet().iterator();
    }
}
