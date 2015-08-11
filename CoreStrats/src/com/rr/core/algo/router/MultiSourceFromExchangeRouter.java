/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.router;

import com.rr.core.factories.ReusableStringFactory;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.MessageRouter;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;

/**
 * used to multiplex between multiple order sources and single exchange destination
 */
public final class MultiSourceFromExchangeRouter implements MessageRouter {

    private static final ErrorCode ERR_UNEXPECTED_EXEC  = new ErrorCode( "MSR100", "Exec mismatch, no matching order ");
    private static final ErrorCode ERR_NOHUB            = new ErrorCode( "MSR200", "Uable to dispatch as no hub");

    private static final ZString   UNABLE_SEND_EXEC_HUB                 = new ViewString( "No Hub available, execId=" );
    private static final ZString   DROP_UNEXPECTED_MSG_FROM_EXCHANGE    = new ViewString( "Dropping unexpected message : " );
    
    private static final Logger       _log = LoggerFactory.create( MultiSourceFromExchangeRouter.class );
    
    private final String            _id;
    private final MessageHandler    _hub;
    
    private       ReusableStringFactory     _reusableStringFactory; // ONLY FOR USE BY EXCHANGE THREAD
    
    private final ReusableString   _mktErrMsg = new ReusableString(100);
    
    private final RouterSharedData _sharedData;
    
    public MultiSourceFromExchangeRouter( String id, MessageHandler hub, RouterSharedData sharedData ) {
        super();
        _id = id;
        _hub = hub;
        _sharedData = sharedData;
        _reusableStringFactory = SuperpoolManager.instance().getFactory(  ReusableStringFactory.class,   ReusableString.class );
    }

    @Override
    public void threadedInit() {
        // nothing
    }

    
    @Override
    public void handle( final Message msg ) {
        handleNow( msg );
    }

    /**
     * handle the market reply
     */
    @Override
    public void handleNow( final Message msg ) {
        boolean sent = false;
    
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERACK:
            sent = handleNewOrderAck( (NewOrderAck) msg );
            if ( ! sent ) {
                warnHub( (CommonExecRpt) msg );
            }
            break;
        case EventIds.ID_TRADENEW:
        case EventIds.ID_CANCELREJECT:
        case EventIds.ID_REJECTED:
        case EventIds.ID_CANCELLED:
        case EventIds.ID_REPLACED:
        case EventIds.ID_DONEFORDAY:
        case EventIds.ID_STOPPED:
        case EventIds.ID_EXPIRED:
        case EventIds.ID_SUSPENDED:
        case EventIds.ID_RESTATED:
        case EventIds.ID_TRADECORRECT:
        case EventIds.ID_TRADECANCEL:
        case EventIds.ID_ORDERSTATUS:
        case EventIds.ID_VAGUEORDERREJECT:
            sent = handleExec( (CommonExecRpt) msg );
            if ( ! sent ) {
                warnHub( (CommonExecRpt) msg );
            }
            break;
        case EventIds.ID_FORCECANCEL:      // FILLER EVENTS
        case EventIds.ID_NEWORDERSINGLE:
        case EventIds.ID_CANCELREPLACEREQUEST:
        case EventIds.ID_CANCELREQUEST:
            break;
        default:
            break;
        }
        
        if ( !sent ) {
            _mktErrMsg.copy( DROP_UNEXPECTED_MSG_FROM_EXCHANGE );
            msg.dump( _mktErrMsg );
            _log.warn( _mktErrMsg );
        }
    }

    private boolean handleExec( CommonExecRpt msg ) {
        
        MessageHandler mh = _sharedData.findHandler( msg.getClOrdId(), msg.getOrderId() );
        
        if ( mh != null ) {
            routeInwards( msg, mh );
            
            return true;            
        }
        
        return false;
    }

    private boolean handleNewOrderAck( final NewOrderAck msg ) {
        final ZString clOrdId = msg.getClOrdId();
        final ZString orderId = msg.getOrderId();
        
        MessageHandler mh = null;
        
        if ( orderId.length() > 0 ) {
            final ReusableString mktOrdIdCopy = _reusableStringFactory.get();
            mktOrdIdCopy.setValue( orderId );

            // @NOTE this is a memory leak ... if need to purge terminal entries then need lookup the clOrdId instance in the map and 
            // link the orderId to the clOrdid via setNext
            // given this is an extra map lookup dont bother for now.
            
            mh = _sharedData.matchUpOrderIdToClOrdId( clOrdId, mktOrdIdCopy );
        } else {
            mh = _sharedData.findHandler( clOrdId );
        }

        if ( mh == null ) return false;
        
        routeInwards( msg, mh );
        
        return true;
    }

    private void routeInwards( Message msg, MessageHandler mh ) {
        // dont hold anything up if handler unable to process
        
        mh.handle( msg );
    }

    @Override
    public boolean canHandle() {
        return true;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    private void warnHub( CommonExecRpt msg ) {

        _mktErrMsg.copy( "clOrdId=" ).append( msg.getClOrdId() ).append( " : " );
        msg.dump( _mktErrMsg );
        _log.error( ERR_UNEXPECTED_EXEC, _mktErrMsg );
        
        if ( _hub != null ) {
            _hub.handle( msg );
        } else {
            _mktErrMsg.copy( UNABLE_SEND_EXEC_HUB ).append( msg.getExecId() );
            _log.error( ERR_NOHUB, _mktErrMsg );
        }
    }
}
