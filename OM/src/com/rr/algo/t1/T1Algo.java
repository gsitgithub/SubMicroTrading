/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.algo.t1;

import com.rr.algo.SimpleAlgo;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.Message;
import com.rr.core.model.book.BookEntryImpl;
import com.rr.core.model.book.BookLevelEntry;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.Session;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.factory.RecoveryNewOrderSingleFactory;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.om.router.OrderRouter;


/**
 * Original Implementation of T1 handler .. cross the spread every X ticks
 *
 * MessageHandler could be EventProcesor or a direct exchange session
 */
public final class T1Algo implements SimpleAlgo {

    private static final Logger                 _log = LoggerFactory.create( T1Algo.class );

    private static final ZString                CME       = new ViewString( "XMCE" );
    private static final ZString                T1ACCOUNT = new ViewString( "T1SMT" );
    
    private final EventRecycler                 _exchangeEventRecycler;
    private final RecoveryNewOrderSingleFactory _nosFactory;
    private final int                           _nosMod;
    private final int                           _pow2Mask;
    private final BookLevelEntry                _tmpDest = new BookEntryImpl();
    private final OrderRouter                   _router;
    private final ReusableString                _debugMsg = new ReusableString();

    private       boolean                       _debug = false;

    private final String                        _id;

    public T1Algo( int nosMod, OrderRouter router ) {
        this(  null, nosMod, router );
    }

    public T1Algo( String id, int nosMod, OrderRouter router ) {
        _id = id;

        _exchangeEventRecycler = new EventRecycler();
        
        SuperPool<RecoveryNewOrderSingleImpl> _nosPool = SuperpoolManager.instance().getSuperPool( RecoveryNewOrderSingleImpl.class );
        _nosFactory = new RecoveryNewOrderSingleFactory( _nosPool );
        
        _nosMod = nosMod;
        
        _pow2Mask = ( isPowerOfTwo( _nosMod ) ) ? (_nosMod-1) : 0;
        
       _router = router;
    }
    
    @Override
    public String getComponentId() {
        return _id;
    }

    public int getNOSMod() {
        return _nosMod;
    }
    
    private boolean isPowerOfTwo( int val ) {
        int nxtPow2 = 1;
        
        while( nxtPow2 < val ) {
            nxtPow2 <<= 1;
        }
        
        return val == nxtPow2;
    }

    @Override
    public void changed( final Book book ) {
        if ( _debug ) {
            _debugMsg.reset();
            book.dump( _debugMsg );
        
            _log.info( _debugMsg );
        }

        if ( book.isValidBBO() ) {
            // we have top of book ... should check not crossed and not zero
            
            final int bookTickCount = book.getTickCount();
            
            if ( _pow2Mask > 0 ) { // mod is a power of two
                
                if ( (bookTickCount & _pow2Mask) == 0 ) {
                    sendOrder( book );
                }
            } else if ( ( bookTickCount % _nosMod) == 0 ) { // mod isnt power of 2 so have to use MOD 
                sendOrder( book );
            }
        }
    }

    private void sendOrder( final Book book ) {
        // cross the spread to trade
        book.getAskEntry( 0, _tmpDest );
        
        final RecoveryNewOrderSingleImpl nos = _nosFactory.get();

        nos.setSide( Side.Buy );
        nos.getAccountForUpdate().setValue( T1ACCOUNT );
        nos.setOrdType( OrdType.Limit );
        nos.setHandlInst( HandlInst.AutoExecPrivate );
        nos.setOrderReceived( book.getLastTickInNanos() );
        nos.setInstrument( book.getInstrument() );
        nos.setOrderQty( _tmpDest.getQty() );
        nos.setPrice( _tmpDest.getPrice() );

        final long longInstId = book.getInstrument().getLongSymbol();
        nos.getClOrdIdForUpdate().append( longInstId ).append( '_' ).append( book.getLastTickId() ).append( ' ' ).append( book.getLastExchangeTickTime() );
        
        nos.getSymbolForUpdate().append( book.getInstrument().getExchangeSymbol() );
        nos.getExDestForUpdate().copy( CME );
        
        final Session sess = (Session) _router.getRoute( nos, null );
        nos.setMessageHandler( sess );
        
        sess.handle( nos );
    } 

    @Override
    public void handleMarketEvent( final Message mktMsg ) {
        _exchangeEventRecycler.recycle( mktMsg );
    }

    public boolean isDebug() {
        return _debug;
    }

    public void setDebug( boolean debug ) {
        _debug = debug;
    }
}
