/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.sim;

import java.util.concurrent.atomic.AtomicInteger;

import com.rr.core.codec.BaseReject;
import com.rr.core.collections.MessageQueue;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.warmup.ExchangeSim;

public final class WarmupExchangeSimAdapter implements MessageHandler {

    private static final Logger       _log              = LoggerFactory.console( WarmupExchangeSimAdapter.class );
    private static final ErrorCode    ERR_HANDLE_FAIL   = new ErrorCode( "WES100", "Exception processing event " );
    private static final ZString      FAILED_DECODE     = new ViewString( "(WES) Try to process failed decode event : " );

            static final ZString      TAG_MSG_TYPE      = new ViewString( "35" );
            static final ZString      TAG_CLORDID       = new ViewString( "11" );
            static final ZString      TAG_ORIG_CLORDID  = new ViewString( "41" );


    private final String              _name             = "WarmExSimAd";
    private final EventRecycler       _eventRecycler    = new EventRecycler();
    
    private final MessageQueue        _queue;
    private final MessageDispatcher   _simDispatcher;
    private final EventBuilder        _eventBuilder     = new EventBuilderImpl(); // @ NOTE only use under handleNow thread SHARED ExchangeSim

    private final ExchangeSim         _exchangeSim;
    private final ReusableString      _errMsg           = new ReusableString(100);

    private volatile AtomicInteger    _sent             = new AtomicInteger();
    private int _requests;


    public WarmupExchangeSimAdapter( MessageDispatcher dispatcher, int expectedOrders, MessageQueue queue ) {
        _queue             = queue;
        _simDispatcher     = dispatcher;
        
        _simDispatcher.setHandler( this );
        _simDispatcher.start();
        
        _eventBuilder.initPools();

        _exchangeSim       = new ExchangeSim( expectedOrders, _eventBuilder );
    }
    
    public int getRequests() {
        return _requests;
    }

    @Override
    public void handle( final Message msg ) {
        
        if ( msg.getMessageHandler() == null ) {
            final ReusableString errMsg = TLC.instance().getString();
            errMsg.setValue( "Error msg has no source handler : " );
            msg.dump( errMsg );
            _log.info( errMsg );
            TLC.instance().recycle( errMsg );
        }
        _queue.add( msg );
    }

    @Override
    public void handleNow( final Message msg ) {
        
        Message reply = null;
        MessageHandler sess = null;

        try {
            sess = msg.getMessageHandler();
            
            switch( msg.getReusableType().getSubId() ) {
            case EventIds.ID_NEWORDERSINGLE:
                reply = handleNewOrderSingle( (NewOrderSingle) msg );
                // NOS used by reply msg - dont recycle
                break;
            case EventIds.ID_CANCELREPLACEREQUEST:
                reply = handleCancelReplaceRequest( (CancelReplaceRequest) msg );
                // amend used by reply msg - dont recyle
                break;
            case EventIds.ID_CANCELREQUEST:
                reply = handleCancelRequest( (CancelRequest) msg );
                _eventRecycler.recycle( msg );
                break;    
            default:
                if ( msg instanceof BaseReject<?> ) {
                    reply = processDecodeFailure( (BaseReject<?>) msg );
                }
            }
            
        } catch( Exception e ) {
            
            _log.error( ERR_HANDLE_FAIL, e.getMessage(), e );
        }
        
        handleReply( msg, reply, sess );
    }

    private void handleReply( final Message msg, Message reply, final MessageHandler sess ) {
        if ( reply != null ) {
            
            while( reply != null ) {
                final Message tmp = reply.getNextQueueEntry();
                
                reply.detachQueue();
                reply.setMessageHandler( sess );
                _sent.incrementAndGet();
                
                if ( sess == null ) {
                    _log.warn( "WarmupExchangeSimAdapter: message has no messageHandler (should be src session)  type=" + msg.getClass().getSimpleName() );
                } else {
                    sess.handle( reply );
                }
                
                reply = tmp;
            }
        }
    }

    private Message handleCancelRequest( final CancelRequest msg ) {
        ++_requests;
        return  _exchangeSim.applyCancelRequest( msg );
    }

    private Message handleCancelReplaceRequest( final CancelReplaceRequest msg ) {
        ++_requests;
        return _exchangeSim.applyCancelReplaceRequest( msg );
    }

    private Message handleNewOrderSingle( final NewOrderSingle msg ) {
        ++_requests;
        return _exchangeSim.applyNewOrderSingle( msg );
    }

    private Message processDecodeFailure( BaseReject<?> msg ) {

        _errMsg.copy( FAILED_DECODE );
        msg.dump( _errMsg );
        _log.warn( _errMsg );

        if ( msg.getNumFields() == 0 ) { // probably just a disconnect
            return null;
        }
        
        ZString type   = msg.getFixField( TAG_MSG_TYPE );
        byte[] bType   = type.getBytes();
        byte   msgType = bType[0];

        Message reject;
        
        if ( msgType == 'D' ) {
            ViewString   clOrdId     = msg.getFixField( TAG_CLORDID );
            OrdStatus status         = OrdStatus.Rejected;

            reject = _eventBuilder.getNOSReject( clOrdId, status, msg.getMessage(), msg );
            
        } else if ( msgType == 'F' ) {
            ViewString   clOrdId     = msg.getFixField( TAG_CLORDID );
            ViewString   origClOrdId = msg.getFixField( TAG_ORIG_CLORDID );
            OrdStatus status         = OrdStatus.Rejected;
            
            reject = _eventBuilder.getCancelReject( clOrdId, origClOrdId, msg.getMessage(), CxlRejResponseTo.CancelRequest, status );

        } else if ( msgType == 'G' ) {
            ViewString   clOrdId     = msg.getFixField( TAG_CLORDID );
            ViewString   origClOrdId = msg.getFixField( TAG_ORIG_CLORDID );
            OrdStatus status         = OrdStatus.Rejected;
            
            reject = _eventBuilder.getCancelReject( clOrdId, origClOrdId, msg.getMessage(), CxlRejResponseTo.CancelReplace, status );
            
        } else {
            reject = _eventBuilder.createSessionReject( msg.getMessage(), msg );
        }
        
        reject.setMessageHandler( msg.getMessageHandler() );
        
        return reject;
    }
    
    
    @Override public boolean canHandle() { return true; }
    
    @Override public void threadedInit() { 
        /* nothing */
    }

    public void stop() {
        _simDispatcher.setStopping();
    }
    
    public int getSent() {
        return _sent.get();
    }

    public void reset() {
        _sent.set( 0 );
    }

    @Override
    public String getComponentId() {
        return _name;
    }

    public void setLogOrderInTS( boolean logOrderInTS ) {
        _exchangeSim.setLogOrderInTS( logOrderInTS );
    }
}
