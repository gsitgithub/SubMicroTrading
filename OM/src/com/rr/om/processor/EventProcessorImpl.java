/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.core.codec.BaseReject;
import com.rr.core.collections.EventQueue;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.factories.ReusableStringFactory;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Currency;
import com.rr.core.model.Instrument;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.ModelVersion;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.recycler.ReusableStringRecycler;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.core.ModelReusableTypes;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.ClientCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.interfaces.Alert;
import com.rr.model.generated.internal.events.interfaces.BaseOrderRequest;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.DoneForDay;
import com.rr.model.generated.internal.events.interfaces.Expired;
import com.rr.model.generated.internal.events.interfaces.MarketTradeCancelWrite;
import com.rr.model.generated.internal.events.interfaces.MarketTradeCorrectWrite;
import com.rr.model.generated.internal.events.interfaces.MarketTradeNewWrite;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.OrderStatus;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.Restated;
import com.rr.model.generated.internal.events.interfaces.Stopped;
import com.rr.model.generated.internal.events.interfaces.Suspended;
import com.rr.model.generated.internal.events.interfaces.TradeBase;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.Side;
import com.rr.om.Strings;
import com.rr.om.client.OMClientProfile;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.model.fix.FixTags;
import com.rr.om.order.Order;
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.order.collections.OrderMap;
import com.rr.om.order.collections.SegmentOrderMap;
import com.rr.om.processor.states.OpenState;
import com.rr.om.processor.states.OrderState;
import com.rr.om.processor.states.PendingCancelReplaceState;
import com.rr.om.processor.states.PendingCancelState;
import com.rr.om.processor.states.PendingNewState;
import com.rr.om.processor.states.StateException;
import com.rr.om.processor.states.TerminalState;
import com.rr.om.registry.TradeRegistry;
import com.rr.om.router.OrderRouter;
import com.rr.om.utils.FixUtils;
import com.rr.om.validate.EventValidator;

public class EventProcessorImpl implements EventProcessor {

    private static final Logger       _log = LoggerFactory.create( EventProcessorImpl.class );

    private static final byte[]       IGNORE_POS_DUP       = "Ignoring pos dup,clOrdId=".getBytes();
    private static final ZString      UNABLE_TO_ROUTE      = new ViewString( "No route available" );
    private static final ZString      UNKNOWN_ORDER        = new ViewString( "UnknownOrder" );
    private static final ZString      UNABLE_SEND_EXEC_HUB = new ViewString( "No Hub available, execId=" );
    private static final ZString      UNABLE_TO_PROCESS    = new ViewString( "HUB" ); // HUB must process
    private static final ZString      TAG_MSG_TYPE         = new ViewString( "" + FixTags.MsgType );
    private static final ZString      TAG_CLORDID          = new ViewString( "" + FixTags.ClOrdID );
    private static final ZString      TAG_ORIG_CLORDID     = new ViewString( "" + FixTags.OrigClOrdID );
    private static final ZString      DUPLICATE            = new ViewString( "Duplicate " );
    private static final ZString      FAILED_DECODE        = new ViewString( "Try to process failed decode event : " );
    
    private static final ErrorCode ERR_MISSING_HANDLER      = new ErrorCode( "EVP100", "No Handler Unable to dispatch Event : " );
    private static final ErrorCode ERR_HANDLE_FAIL          = new ErrorCode( "EVP200", "Exception processing event " );
    private static final ErrorCode ERR_NO_ROUTE             = new ErrorCode( "EVP300", "Unable to find route for clOrdId=");
    private static final ErrorCode ERR_REJ_UNKNOWN_ORDER    = new ErrorCode( "EVP400", "CancelReject Unable to find order");
    private static final ErrorCode ERR_NOHUB                = new ErrorCode( "EVP500", "Uable to dispatch as no hub");
    private static final ErrorCode ERR_UNEXPECTED_EXEC      = new ErrorCode( "EVP600", "Exec mismatch, no matching order ");
    
    
    private final ModelVersion              _version;
    private final int                       _expectedOrders;
    private final OrderMap                  _orderMap;

    private final EventValidator            _validator;

    private final EventQueue                _upStreamQ;
    private final EventQueue                _downStreamQ;

    private final EventBuilder              _eventBuilder;
    
    private final byte[]                    _encodeBuf    = new byte[ SizeConstants.DEFAULT_VIEW_NOS_BUFFER ];
    private final Standard44Encoder         _errorEncoder = new Standard44Encoder( (byte)'4', (byte)'4', _encodeBuf );

    private final OrderState                _pendingNewState;
    private final OrderState                _terminalState;
    private final OrderState                _openState;
    private final OrderState                _pendingCancel;
    private final OrderState                _pendingReplace;

    private       OrderRouter           _router;    // cant be final as processor must be created before sessions

    private final MessageDispatcher         _dispatcher;

    private       ReusableStringFactory     _reusableStringFactory;
    private       ReusableStringRecycler    _reusableStringRecycler;

    private       OrderFactory              _orderFactory = null;
    private       OrderRecycler             _orderRecycler;

    private       OrderVersionFactory       _versionFactory;
    private       OrderVersionRecycler      _versionRecycler;

    private       EventRecycler             _eventRecycler;

    private final MessageHandler            _hub;   // message handler for sending alerts to the HUB session
    private final TradeRegistry             _tradeRegistry; 
    private final EventProcConfig           _config;

    private       ReusableString            _errMsg = new ReusableString(100);
    
    private final String                    _name = "EventProcessor";

    public EventProcessorImpl( ModelVersion      version, 
                               int               expectedOrders, 
                               EventValidator    validator, 
                               EventBuilder      builder, 
                               MessageDispatcher dispatcher,
                               MessageHandler    hub,
                               TradeRegistry     tradeRegistry ) {

        this( null, 
              version,
              expectedOrders,
              validator,
              builder,
              dispatcher,
              hub,
              tradeRegistry );
    }
    
    public EventProcessorImpl( OrderMap          orderMap,
                               ModelVersion      version, 
                               int               expectedOrders, 
                               EventValidator    validator, 
                               EventBuilder      builder, 
                               MessageDispatcher dispatcher,
                               MessageHandler    hub,
                               TradeRegistry     tradeRegistry ) {
        this( EventProcConfigImpl.DEFAULT,
              orderMap, 
              version,
              expectedOrders,
              validator,
              builder,
              dispatcher,
              hub,
              tradeRegistry );
    }
    
    public EventProcessorImpl( EventProcConfig   config,
                               OrderMap          orderMap,
                               ModelVersion      version, 
                               int               expectedOrders, 
                               EventValidator    validator, 
                               EventBuilder      builder, 
                               MessageDispatcher dispatcher,
                               MessageHandler    hub,
                               TradeRegistry     tradeRegistry ) {
        super();
        
        _config          = config;
        _version         = version;
        _expectedOrders  = expectedOrders;
        _orderMap        = (orderMap==null) ? createOrderMap( _expectedOrders ) : orderMap;
        _validator       = validator;
        _eventBuilder    = builder;
        _tradeRegistry   = tradeRegistry;
        
        _hub             = hub;
        
        _pendingNewState = createPendingNewState();
        _openState       = createOpenState();
        _terminalState   = createTerminalState();
        _pendingCancel   = createPendingCancelState();
        _pendingReplace  = createPendingCancelReplaceState();

        _dispatcher      = dispatcher;

        _dispatcher.setHandler( this );

        _upStreamQ       = createEventQueue();
        _downStreamQ     = createEventQueue();
    }

    @Override
    public void logStats() {
        ReusableString buf = new ReusableString();
        
        _orderMap.logStats( buf );
        
        _log.info( buf );
    }

    /**
     * only exposed for population by recovery, dont add to interface
     * 
     * @return the internal order map
     */
    public OrderMap getInternalMap() {
        return _orderMap;
    }
    
    public EventProcConfig getConfig() {
        return _config;
    }

    protected EventQueue createEventQueue() {
        return new EventQueue();
    }

    protected OrderState createPendingNewState() {
        return new PendingNewState( this );
    }

    protected OrderState createOpenState() {
        return new OpenState( this );
    }

    protected OrderState createTerminalState() {
        return new TerminalState( this );
    }

    protected OrderState createPendingCancelState() {
        return new PendingCancelState( this );
    }

    protected OrderState createPendingCancelReplaceState() {
        return new PendingCancelReplaceState( this );
    }

    @Override
    public void setProcessorRouter( OrderRouter router ) {
        _router = router;
    }

    protected OrderMap createOrderMap( int expectedOrders ) {
        return new SegmentOrderMap( expectedOrders, 0.75f, 1 ); // 1 segment fastest
    }

    /**
     * MUST BE CALLED WITHIN THE CONTEXT OF THE DISPATCH THREAD 
     */
    @Override
    public void threadedInit() {
        if ( _orderFactory == null ) {
        
            SuperpoolManager sp = SuperpoolManager.instance();
            
            _reusableStringFactory  = sp.getFactory(  ReusableStringFactory.class,   ReusableString.class );
            _reusableStringRecycler = sp.getRecycler( ReusableStringRecycler.class,  ReusableString.class );
    
            _orderFactory    = sp.getFactory(  OrderFactory.class,   OrderImpl.class );
            _orderRecycler   = sp.getRecycler( OrderRecycler.class,  OrderImpl.class );
    
            _versionFactory  = sp.getFactory(  OrderVersionFactory.class,   OrderVersion.class );
            _versionRecycler = sp.getRecycler( OrderVersionRecycler.class,  OrderVersion.class );
    
            _eventRecycler   = new EventRecycler();
            
            _eventBuilder.initPools();
            
            _orderMap.setRecycleProcessor( this );
        }
    }
    
    @Override public final OrderState getStatePendingNew()      { return _pendingNewState; }
    @Override public final OrderState getStateCompleted()       { return _terminalState; }
    @Override public final OrderState getStateOpen()            { return _openState; }
    @Override public final OrderState getStatePendingCancel()   { return _pendingCancel; }
    @Override public final OrderState getStatePendingReplace()  { return _pendingReplace; }

    @Override
    public EventBuilder getEventBuilder() {
        return _eventBuilder;
    }

    @Override
    public final void handle( Message msg ) {

        if ( msg != null ) {
            _dispatcher.dispatch( msg );
        }
    }
    
    @Override
    public final void handleNow( Message msg ) {

        try {
            switch( msg.getReusableType().getSubId() ) {
            case EventIds.ID_NEWORDERSINGLE:
                handleNewOrderSingle( (NewOrderSingle) msg );
                break;
            case EventIds.ID_NEWORDERACK:
                handleNewOrderAck( (NewOrderAck) msg );
                break;
            case EventIds.ID_TRADENEW:
                handleTradeNew( (TradeNew) msg );
                break;
            case EventIds.ID_CANCELREPLACEREQUEST:
                handleCancelReplaceRequest( (CancelReplaceRequest) msg );
                break;
            case EventIds.ID_CANCELREQUEST:
                handleCancelRequest( (CancelRequest) msg );
                break;
            case EventIds.ID_CANCELREJECT:
                handleCancelReject( (CancelReject) msg );
                break;
            case EventIds.ID_REJECTED:
                handleRejected( (Rejected) msg );
                break;
            case EventIds.ID_CANCELLED:
                handleCancelled( (Cancelled) msg );
                break;
            case EventIds.ID_REPLACED:
                handleReplaced( (Replaced) msg );
                break;
            case EventIds.ID_DONEFORDAY:
                handleDoneForDay( (DoneForDay) msg );
                break;
            case EventIds.ID_STOPPED:
                handleStopped( (Stopped) msg );
                break;
            case EventIds.ID_EXPIRED:
                handleExpired( (Expired) msg );
                break;
            case EventIds.ID_SUSPENDED:
                handleSuspended( (Suspended) msg );
                break;
            case EventIds.ID_RESTATED:
                handleRestated( (Restated) msg );
                break;
            case EventIds.ID_TRADECORRECT:
                handleTradeCorrect( (TradeCorrect) msg );
                break;
            case EventIds.ID_TRADECANCEL:
                handleTradeCancel( (TradeCancel) msg );
                break;
            case EventIds.ID_ORDERSTATUS:
                handleOrderStatus( (OrderStatus) msg );
                break;
            case EventIds.ID_VAGUEORDERREJECT:
                handleVagueReject( (VagueOrderReject) msg );
                break;
            case EventIds.ID_FORCECANCEL: // @TODO implement support for admin based force cancel events
                break;
            default:
                if ( msg instanceof BaseReject<?> ) {
                    processDecodeFailure( (BaseReject<?>) msg );
                }
            }
            
            flush( _downStreamQ );
            flush( _upStreamQ );
            
        } catch( Exception e ) {
            
            _log.error( ERR_HANDLE_FAIL, e.getMessage(), e );
            
            _downStreamQ.clear();
            _upStreamQ.clear();
        }
    }

    /**
     * process a decode failure from the decoder
     * 
     * processed here to get the correct order details like status
     * 
     * @param msg
     */
    private void processDecodeFailure( BaseReject<?> msg ) {

        _errMsg.copy( FAILED_DECODE );
        msg.dump( _errMsg );
        _log.warn( _errMsg );

        if ( msg.getNumFields() == 0 ) { // probably just a disconnect
            return;
        }
        
        ZString type   = msg.getFixField( TAG_MSG_TYPE );
        byte[] bType   = type.getBytes();
        byte   msgType = bType[0];

        Message reject = null;
        
        if ( msgType == 'D' ) {
            ViewString   clOrdId     = msg.getFixField( TAG_CLORDID );
            ViewString   origClOrdId = msg.getFixField( TAG_ORIG_CLORDID );
            OrdStatus status         = findOrdStatus( clOrdId, origClOrdId );

            reject = _eventBuilder.getNOSReject( clOrdId, status, msg.getMessage(), msg );
            
        } else if ( msgType == 'F' ) {
            ViewString   clOrdId     = msg.getFixField( TAG_CLORDID );
            ViewString   origClOrdId = msg.getFixField( TAG_ORIG_CLORDID );
            OrdStatus status         = findOrdStatus( clOrdId, origClOrdId );
            
            reject = _eventBuilder.getCancelReject( clOrdId, origClOrdId, msg.getMessage(), CxlRejResponseTo.CancelRequest, status );

        } else if ( msgType == 'G' ) {
            ViewString   clOrdId     = msg.getFixField( TAG_CLORDID );
            ViewString   origClOrdId = msg.getFixField( TAG_ORIG_CLORDID );
            OrdStatus status         = findOrdStatus( clOrdId, origClOrdId );
            
            reject = _eventBuilder.getCancelReject( clOrdId, origClOrdId, msg.getMessage(), CxlRejResponseTo.CancelReplace, status );
        }
        
        if ( reject == null ) { // couldnt get enough info for execReject
            reject = _eventBuilder.createSessionReject( msg.getMessage(), msg );
        }
        
        if ( reject != null ) {
            reject.setMessageHandler( msg.getMessageHandler() );
            enqueueUpStream( reject );
        }
    }

    private OrdStatus findOrdStatus( ViewString clOrdId, ViewString origClOrdId ) {
        
        if ( clOrdId != null ) {
            final Order clOrdIdOrder     = _orderMap.get( clOrdId );
    
            if ( clOrdIdOrder != null ) return clOrdIdOrder.getPendingVersion().getOrdStatus();
        }
        
        if ( origClOrdId != null ) {
            final Order origClOrdIdOrder = _orderMap.get( origClOrdId );
    
            if ( origClOrdIdOrder  != null ) return origClOrdIdOrder .getPendingVersion().getOrdStatus();
        }

        return OrdStatus.UnseenOrder;
    }

    private void flush( final EventQueue q ) {

        Message m = q.get();
        Message msgToDispatch;
        
        while( m != null ) {
            
            msgToDispatch = m;
            m = q.get(); // get next BEFORE we add element to handler, as handler may put event on new queue

            MessageHandler h = msgToDispatch.getMessageHandler();
            
            if ( h != null ) {
                h.handle( msgToDispatch );
            } else {
                logMissingHandler( msgToDispatch );
            }
        }
    }

    private void logMissingHandler( Message msg  ) {
        final ReusableString tmpText = TLC.instance().pop();

        try {
            _errorEncoder.encode( msg );
            
            if ( msg instanceof BaseOrderRequest ) {
                orderToText( ((BaseOrderRequest)msg).getClOrdId(), tmpText );
            } else if ( msg instanceof CommonExecRpt ){
                orderToText( ((CommonExecRpt)msg).getClOrdId(), tmpText );
            }
            
            tmpText.append( _encodeBuf, _errorEncoder.getOffset(), _errorEncoder.getLength() );
        } catch( Exception e ) {
            tmpText.append( "  Failed to encode event for logging" );
        }
        
        _log.errorLarge( ERR_MISSING_HANDLER, tmpText );
        
        TLC.instance().pushback( tmpText );
        
        _eventRecycler.recycle( msg );
    }

    private void orderToText( ViewString clOrdId, ReusableString tmpText ) {
        
        Order order = _orderMap.get( clOrdId );
        
        if ( order == null ) {
            tmpText.append( Strings.NO_SUCH_ORDER ).append( clOrdId );
        } else {
     
            order.appendDetails( tmpText );
        }
    }

    @Override
    public final void handleNewOrderSingle( NewOrderSingle nos ) throws StateException {

        // assume order is not a dup
        final OrderImpl order = createOrder( nos );

        if ( ! _validator.validate( nos, order ) ) {
            
            sendReject( nos, _validator.getRejectReason(), _validator.getOrdRejectReason(), OrdStatus.Rejected );
            
            // dont recycle nos as its attached to reject

            _versionRecycler.recycle( order.getLastAckedVerion() );
            _orderRecycler.recycle( order );
            
            return;
        }
        
        final ReusableString clOrdId = _reusableStringFactory.get();
        clOrdId.setValue( nos.getClOrdId() );
        
        if ( _orderMap.putIfKeyAbsent( clOrdId, order ) )  {
        
            // order is NOT a dup

            order.registerClientClOrdId( clOrdId );
            
            order.setState( getStatePendingNew() ).handleNewOrderSingle( order, nos );
            
        } else  {
            
            if ( nos.getPossDupFlag() ) {

                processPosDup( nos );
                
                _eventRecycler.recycle( nos );
            
            } else {
                // now reject the NOS

                _errMsg.copy( DUPLICATE );
                order.appendDetails( _errMsg );
                
                // dont recycle msg its attached to the Reject
                
                sendReject( nos, _errMsg, OrdRejReason.DuplicateOrder, OrdStatus.Rejected );
            }

            _versionRecycler.recycle( order.getLastAckedVerion() );
            _orderRecycler.recycle( order );
        }
    }
    
    @Override
    public final void handleNewOrderAck( NewOrderAck msg ) throws StateException {
        
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
            
            order.getState().handleNewOrderAck( order, msg );
            
        } else {
            // forward to hub so it has knowledge of unexpected execRpt
            handleUnknownExecToHub( msg );
        }
    }

    private void handleUnknownExecToHub( CommonExecRpt msg ) {
        final ReusableType type = msg.getReusableType();

        _errMsg.copy( "clOrdId=" ).append( msg.getClOrdId() ).append( " : ");
        msg.dump( _errMsg );
        _log.error( ERR_UNEXPECTED_EXEC, _errMsg );
        
        final  OrdStatus forceStatus = OrdStatus.UnseenOrder;
        final boolean   openOnMkt   = FixUtils.overrideOrdStatus( msg, type, forceStatus );

        if ( openOnMkt && _config.isForceCancelUnknownExexId() ) {
            // force cancel as not safe to leave in mkt unexpected order
            
            MessageHandler h = msg.getMessageHandler();
            
            if ( h != this && h != null ) {
                _errMsg.copy( "Force Cancel unknown execRpt, clOrdId=" ).append( msg.getClOrdId() ).append( " : ");
                msg.dump( _errMsg );
                
                _log.warn( _errMsg );
                
                forceCancelToMKt( h, msg.getClOrdId(), msg.getSide(), msg.getOrderId() );
                
            } else { // dont know src of message, send to all destinations
        
                MessageHandler[] allRoutes = _router.getAllRoutes();
                
                for( MessageHandler route : allRoutes ) {
                    forceCancelToMKt( route, msg.getClOrdId(), msg.getSide(), msg.getOrderId() );
                }
            }
        }
        
        if ( _hub != null ) {
            _hub.handle( msg );
        } else {
            _errMsg.copy( UNABLE_SEND_EXEC_HUB ).append( msg.getExecId() );
            _log.error( ERR_NOHUB, _errMsg );
        }
    }

    private void forceCancelToMKt( MessageHandler h, ViewString clOrdId, Side side, ViewString orderId ) {
        final CancelRequest mktCancel = _eventBuilder.createForceMarketCancel( clOrdId, side, orderId, null );
        
        mktCancel.setMessageHandler( h );

        enqueueDownStream( mktCancel );
    }

    private void processPosDup( BaseOrderRequest event ) {
        ReusableString tmpText = TLC.instance().pop();

        tmpText.setValue( IGNORE_POS_DUP );
        tmpText.append( event.getClOrdId() );
        tmpText.append( Strings.EVENT_TYPE );
        tmpText.append( event.getReusableType().toString() ); // no temp obj in this toString
        
        _log.infoLarge( tmpText );
        
        TLC.instance().pushback( tmpText );
    }

    @Override
    public final void sendReject( NewOrderSingle nos, ZString rejectReason, OrdRejReason reason, OrdStatus status ) {

        Rejected reject = _eventBuilder.synthNOSRejected( nos, rejectReason, reason, status );
        reject.setMessageHandler( nos.getMessageHandler() );
        
        enqueueUpStream( reject );
    }

    @Override
    public final void sendCancelReject( CancelRequest msg, ZString rejectReason, CxlRejReason reason, OrdStatus status ) {

        CancelReject reject = _eventBuilder.getCancelReject( msg.getClOrdId(), 
                                                                   msg.getOrigClOrdId(), 
                                                                   UNKNOWN_ORDER, 
                                                                   rejectReason, 
                                                                   reason,
                                                                   CxlRejResponseTo.CancelRequest, 
                                                                   status );

        reject.setMessageHandler( msg.getMessageHandler() );
        enqueueUpStream( reject );
    }
    
    @Override
    public final void sendCancelReplaceReject( CancelReplaceRequest msg, ZString rejectReason, CxlRejReason reason, OrdStatus status ) {

        CancelReject reject = _eventBuilder.getCancelReject( msg.getClOrdId(), 
                                                                   msg.getOrigClOrdId(), 
                                                                   UNKNOWN_ORDER, 
                                                                   rejectReason, 
                                                                   reason,
                                                                   CxlRejResponseTo.CancelReplace, 
                                                                   status );

        reject.setMessageHandler( msg.getMessageHandler() );
        enqueueUpStream( reject );
    }
    
    @Override
    public final void enqueueUpStream( Message msg ) {
        
        _upStreamQ.add( msg );
    }

    @Override
    public void sendDownStream( BaseOrderRequest msg, Order order ) {

        registerMarketReq( msg, order );

        enqueueDownStream( msg );
    }

    private void registerMarketReq( BaseOrderRequest mktMsg, Order order ) {
        final ViewString mktClOrdId = mktMsg.getClOrdId();
        final ViewString clOrdId    = order.getClientClOrdIdChain();

        if ( ! clOrdId.equals( mktClOrdId ) ) {
            
            final ReusableString mktClOrdIdCopy = _reusableStringFactory.get();
            mktClOrdIdCopy.setValue( mktClOrdId );
            
            _orderMap.put( mktClOrdIdCopy, order );
            
            order.registerMarketClOrdId( mktClOrdIdCopy );
            
            order.getPendingVersion().setMarketClOrdId( mktClOrdIdCopy );
            
        } else{
            
            // dont register mktClOrdId list as null will indicate same as client side
            
            order.getPendingVersion().setMarketClOrdId( clOrdId );
        }
    }
    
    @Override
    public final void enqueueDownStream( Message msg ) {
        
        _downStreamQ.add( msg );
    }

    @Override
    public final void sendTradeHub( TradeBase msg ) {
        
        // 99% will be trade so dont worry cost of triple condition check
        
        if ( msg.getReusableType() == ModelReusableTypes.MarketTradeNew ) {
            
            ((MarketTradeNewWrite)msg).getTextForUpdate().setValue( UNABLE_TO_PROCESS );
            
        } else if ( msg.getReusableType() == ModelReusableTypes.MarketTradeCancel ) {
            
            ((MarketTradeCancelWrite)msg).getTextForUpdate().setValue( UNABLE_TO_PROCESS );
            
        } else if ( msg.getReusableType() == ModelReusableTypes.MarketTradeCorrect ) {
            
            ((MarketTradeCorrectWrite)msg).getTextForUpdate().setValue( UNABLE_TO_PROCESS );
        }

        if ( _hub != null ) {
            _hub.handle( msg );
        } else {
            _errMsg.copy( UNABLE_SEND_EXEC_HUB ).append( msg.getExecId() );
            
            _log.error( ERR_NOHUB, _errMsg );
        }
    }

    private OrderImpl createOrder( NewOrderSingle msg ) {
        OrderImpl order = _orderFactory.get();

        OrderVersion ver = _versionFactory.get();
        ver.setBaseOrderRequest( msg );
        ver.setOrderQty( msg.getOrderQty() );
        
        double price = msg.getPrice();
        Currency ccy = msg.getCurrency();
        
        final Instrument inst            = msg.getInstrument();
        final Currency   tradingCurrency = inst.getCurrency();
        
        if ( tradingCurrency != ccy ) {
            price = tradingCurrency.majorMinorConvert( ccy, price );
        }
        
        ver.setMarketPrice( price );
        
        order.setLastAckedVerion( ver );
        order.setPendingVersion( ver );
        
        order.setClientProfile( (OMClientProfile) msg.getClient() );
        
        return order;
    }

    @Override
    public void freeOrder( Order order ) {
        
        if ( order != null && order.getLastAckedVerion() != null ) {
            
            // ok not already recycled
            
            OrderVersion lastAcked = order.getLastAckedVerion();
            OrderVersion pending   = order.getPendingVersion();
            
            _eventRecycler.recycle( lastAcked.getBaseOrderRequest() );
            _versionRecycler.recycle( lastAcked );
            
            if  ( lastAcked != pending ) {
                _eventRecycler.recycle( pending.getBaseOrderRequest() );
                _versionRecycler.recycle( pending );
            }
            
            ReusableString tmp;
            ReusableString clOrdId = order.getClientClOrdIdChain();
            
            while( clOrdId != null ) {
                tmp = clOrdId;
                clOrdId = clOrdId.getNext();
                _reusableStringRecycler.recycle( tmp );
            }

            ReusableString mkOrdId = order.getMarketClOrdIdChain();
            
            while( mkOrdId != null ) {
                tmp = mkOrdId;
                mkOrdId = mkOrdId.getNext();
                _reusableStringRecycler.recycle( tmp );
            }
            
            _orderRecycler.recycle( (OrderImpl) order );
        }
    }

    @Override
    public ModelVersion getEventModelVersion() {
        return _version;
    }

    @Override
    public final void handleCancelReject( CancelReject msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
            
            order.getState().handleCancelReject( order, msg );
            
        } else {
            final ReusableString tmpText = TLC.instance().pop();

            tmpText.append( Strings.CL_ORD_ID ).append( msg.getClOrdId() );
            
            _log.errorLarge( ERR_REJ_UNKNOWN_ORDER, tmpText );
            
            TLC.instance().pushback( tmpText );
            
            _eventRecycler.recycle( msg );
        }
    }

    @Override
    public final void handleVagueReject( VagueOrderReject msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
            
            order.getState().handleVagueReject( order, msg );
            
        } else {
            final ReusableString tmpText = TLC.instance().pop();

            tmpText.append( Strings.CL_ORD_ID ).append( msg.getClOrdId() );
            
            _log.errorLarge( ERR_REJ_UNKNOWN_ORDER, tmpText );
            
            TLC.instance().pushback( tmpText );
            
            _eventRecycler.recycle( msg );
        }
    }

    @Override
    public final void handleCancelReplaceRequest( CancelReplaceRequest msg ) throws StateException {
        final ViewString origClOrdId   = msg.getOrigClOrdId();
        final Order      existingOrder = _orderMap.get( origClOrdId );
        
        if ( existingOrder == null )  { // reject the unknown clOrdId
            sendCancelReplaceReject( msg, null, CxlRejReason.UnknownOrder, OrdStatus.Rejected );
            _eventRecycler.recycle( msg);
        } else  {
            final ReusableString clOrdId = _reusableStringFactory.get();
            clOrdId.setValue( msg.getClOrdId() );

            if ( _orderMap.putIfKeyAbsent( clOrdId, existingOrder ) ) { // first time for this clOrdId

                existingOrder.registerClientClOrdId( clOrdId ); // register the new clOrdId
                
                final OrderVersion lastAcc    = existingOrder.getLastAckedVerion();
                final OrderRequest lastAccSrc = (OrderRequest) lastAcc.getBaseOrderRequest();
                
                // COPY OVER FIELDS NOT DECODED FROM LAST ACCEPTED REQUEST
                ClientCancelReplaceRequestImpl req = (ClientCancelReplaceRequestImpl) msg;
                req.setInstrument( lastAccSrc.getInstrument() );
                req.setSide(       lastAccSrc.getSide() );
                
                Currency clientCcy = req.getCurrency();
                
                if ( clientCcy == null ) {
                    req.setCurrency( lastAccSrc.getCurrency() );
                }
                
                existingOrder.getState().handleCancelReplaceRequest( existingOrder, msg );
            } else {
                if ( msg.getPossDupFlag() ) {
                    processPosDup( msg );
                    _eventRecycler.recycle( msg);
                } else { // reject the unknown clOrdId
                    sendCancelReplaceReject( msg, DUPLICATE, CxlRejReason.DuplicateClOrdId, existingOrder.getPendingVersion().getOrdStatus() );
                    _eventRecycler.recycle( msg);
                }
            }
        }
    }

    @Override
    public final void handleCancelRequest( CancelRequest msg ) throws StateException {

        final ViewString origClOrdId   = msg.getOrigClOrdId();
        final Order      existingOrder = _orderMap.get( origClOrdId );
        
        if ( existingOrder == null )  { // order is UNKNOWN
            if ( msg.getPossDupFlag() ) {
                processPosDup( msg );
                _eventRecycler.recycle( msg );
            } else {
                sendCancelReject( msg, null, CxlRejReason.UnknownOrder, OrdStatus.Rejected );
                _eventRecycler.recycle( msg );
            }
        } else  {
            final ReusableString clOrdId = _reusableStringFactory.get();
            clOrdId.setValue( msg.getClOrdId() );

            if ( _orderMap.putIfKeyAbsent( clOrdId, existingOrder ) ) { // ok first time for the clOrdId
                // register the new clOrdId
                existingOrder.registerClientClOrdId( clOrdId );
                existingOrder.getState().handleCancelRequest( existingOrder, msg );
            } else {
                if ( msg.getPossDupFlag() ) {
                    processPosDup( msg );
                    _eventRecycler.recycle( msg );
                } else {
                    sendCancelReject( msg, null, CxlRejReason.DuplicateClOrdId, existingOrder.getPendingVersion().getOrdStatus() );
                    _eventRecycler.recycle( msg );
                }
            }
        }
    }

    @Override
    public final void handleCancelled( Cancelled msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
            
            order.getState().handleCancelled( order, msg );
            
        } else {
            // forward to hub so it has knowledge of unexpected execRpt
            handleUnknownExecToHub( msg );
        }
    }

    @Override
    public final void handleDoneForDay( DoneForDay msg ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public final void handleExpired( Expired msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
            
            order.getState().handleExpired( order, msg );
            
        } else {
            handleUnknownExecToHub( msg );
        }
    }

    @Override
    public final void handleOrderStatus( OrderStatus msg ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public final void handleRejected( Rejected msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
            
            order.getState().handleRejected( order, msg );
            
        } else {
            handleUnknownExecToHub( msg );
        }
    }

    @Override
    public final void handleReplaced( Replaced msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
            
            order.getState().handleReplaced( order, msg );
            
        } else {
            handleUnknownExecToHub( msg );
        }
    }

    @Override
    public final void handleRestated( Restated msg ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public final void handleStopped( Stopped msg ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public final void handleSuspended( Suspended msg ) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public final void handleTradeCancel( TradeCancel msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
        
            // we could avoid the map lookup as need to get the actual orig trade wraapper
            // in the state machine, but as this is rare event dont worry 
            
            if ( _tradeRegistry.hasDetails( order, msg.getExecRefID() ) ) { // we know about the original trade
                if ( _tradeRegistry.register( order, msg ) == true )  {
             
                    order.getState().handleTradeCancel( order, msg );
                    
                } else { // duplicate trade
    
                    processPosDup( msg );
                }
            } else { 
                
                if ( _tradeRegistry.contains( order, msg.getExecRefID() ) ) { // cant process cancels as not configured with trade detail registry
                    sendTradeHub( msg );
                } else { // unknown ref send to hub
                    handleUnknownExecToHub( msg );
                }
            }
            
        } else {
            // forward to hub so it has knowledge of unexpected execRpt
            handleUnknownExecToHub( msg );
        }
    }

    @Override
    public final void handleTradeCorrect( TradeCorrect msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
        
            // we could avoid the map lookup as need to get the actual orig trade wraapper
            // in the state machine, but as this is rare event dont worry 
            
            if ( _tradeRegistry.hasDetails( order, msg.getExecRefID() ) ) { // we know about the original trade
                if ( _tradeRegistry.register( order, msg ) == true )  {
             
                    order.getState().handleTradeCorrect( order, msg );
                    
                } else { // duplicate trade
    
                    processPosDup( msg );
                }
            } else { 
                
                if ( _tradeRegistry.contains( order, msg.getExecRefID() ) ) { // cant process cancels as not configured with trade detail registry
                    sendTradeHub( msg );
                } else {                            // unknown ref send to hub
                    handleUnknownExecToHub( msg );
                }
            }
            
        } else {
            // forward to hub so it has knowledge of unexpected execRpt
            handleUnknownExecToHub( msg );
        }
    }

    @Override
    public final void handleTradeNew( TradeNew msg ) throws StateException {
        final Order order = _orderMap.get( msg.getClOrdId() );
        
        if ( order != null ) {
            if ( _tradeRegistry.register( order, msg ) == true )  {
                
                order.getState().handleTradeNew( order, msg );
                
            } else { // duplicate trade

                processPosDup( msg );
            }
            
        } else {
            // forward to hub so it has knowledge of unexpected execRpt
            handleUnknownExecToHub( msg );
        }
    }

    @Override
    public void sendAlertChain( Alert alerts ) {

        Message tmp;
        Message curr = alerts;
        
        while( curr != null ) {
            tmp = curr;
            
            curr = curr.getNextQueueEntry();
            
            tmp.detachQueue();
            
            if ( _hub != null ) {
                _hub.handle( tmp );
            } else {
                _log.error( ERR_NOHUB, ((Alert)tmp).getText() );
            }
        }
    }

    @Override
    public void routeMessageDownstream( final Order order, final NewOrderSingle nos ) throws StateException {
        
        MessageHandler handler = _router.getRoute( nos, this );
        
        if ( handler == null ) {
            _log.error( ERR_NO_ROUTE, nos.getClOrdId() );

            Rejected reject = _eventBuilder.synthNOSRejected( nos, UNABLE_TO_ROUTE, OrdRejReason.Other, OrdStatus.Rejected );
            
            handleRejected( reject );
        } else {
            nos.setMessageHandler( handler );
            
            // all future downstream messages will now be sticky to this handler
            
            order.setDownstreamHandler( handler );

            registerMarketReq( nos, order );
            
            enqueueDownStream( nos );
        }
    }

    /**
     * @return the number of order request entries
     */
    @Override
    public int size() {
        return _orderMap.size();
    }
    
    /**
     * accessor for use in TESTING 
     * 
     * @param clOrdId
     * @return
     */
    Order getOrder( ViewString clOrdId ) {
        return _orderMap.get( clOrdId );
    }

    @Override
    public OrderVersion getNewVersion( CancelReplaceRequest msg, OrderVersion lastAcc ) {
        OrderVersion ver = _versionFactory.get();
        ver.setBaseOrderRequest( msg );
        double price = msg.getPrice();
        Currency ccy = msg.getCurrency();
        
        final Instrument inst            = msg.getInstrument();
        final Currency   tradingCurrency = inst.getCurrency();
        
        if ( tradingCurrency != ccy ) {
            price = tradingCurrency.majorMinorConvert( ccy, price );
        }
        
        ver.setMarketPrice( price );
        return ver;
    }

    @Override
    public OrderVersion getNewVersion( CancelRequest msg, OrderVersion lastAcc ) {
        OrderVersion ver = _versionFactory.get();
        ver.setBaseOrderRequest( msg );
        double price = lastAcc.getMarketPrice();
        ver.setMarketPrice( price );
        return ver;
    }

    @Override
    public void freeMessage( Message msg ) {
        _eventRecycler.recycle( msg );
    }

    @Override
    public void freeVersion( OrderVersion ver ) {
        _versionRecycler.recycle( ver );
    }

    private void processPosDup( TradeBase event ) {
        ReusableString tmpText = TLC.instance().pop();

        ReusableType type = event.getReusableType();
        
        tmpText.setValue( IGNORE_POS_DUP );
        tmpText.append( event.getClOrdId() );
        tmpText.append( Strings.EVENT_TYPE );
        tmpText.append( type.toString() ); // enum so no temp obj in this toString
        tmpText.append( Strings.EXEC_ID ).append( event.getExecId() );

        if ( type == ModelReusableTypes.MarketTradeCorrect ) {
            tmpText.append( Strings.REF_EXEC_ID ).append( ((TradeCorrect)event).getExecRefID() );
        } else  if ( type == ModelReusableTypes.MarketTradeCancel ) {
            tmpText.append( Strings.REF_EXEC_ID ).append( ((TradeCancel)event).getExecRefID() );
        }
        
        _log.infoLarge( tmpText );
        
        TLC.instance().pushback( tmpText );
    }

    @Override
    public TradeRegistry getTradeRegistry() {
        return _tradeRegistry;
    }

    public void clear() {

        _log.info( "Clearing the order map" );

        try {
            _orderMap.clear();
        } catch( Exception e ) {
            _log.warn( "Error clearing order map " + e.getMessage() );
        }
        
        try {
            _tradeRegistry.clear();
        } catch( Exception e ) {
            _log.warn( "Error clearing trade registry " + e.getMessage() );
        }

        _log.info( "Clear complete" );
    }

    @Override
    public final EventValidator getValidator() {
        return _validator;
    }

    @Override
    public boolean canHandle() {
        return true;
    }

    @Override
    public void stop() {
        _dispatcher.setStopping();
    }

    @Override
    public String getComponentId() {
        return _name;
    }
}
