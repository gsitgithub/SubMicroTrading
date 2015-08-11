/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.router;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Exchange;
import com.rr.core.model.MessageHandler;
import com.rr.core.session.Session;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.om.session.SessionManager;

/**
 * simple exchange router based on instruments exchange, only supports one session per exchange
 */
public final class ExchangeRouter implements OrderRouter {

    private static final Logger _log = LoggerFactory.create( ExchangeRouter.class );
    
    private final Session[] _sessionByExchangeIdx; // array may be sparse
    private final Session[] _origDownSess;         // pointer to original downstream array DO NOT MODIFY 

    private final String    _id;

    public ExchangeRouter( Session[] downStream, SessionManager sessMgr ) {
        this( null, downStream, sessMgr );
    }
    
    public ExchangeRouter( String id, Session[] downStream, SessionManager sessMgr ) {
        int maxIdx = -1;
        
        _id = id;
        
        _origDownSess = downStream;
        
        for( Session s : downStream ) {
            Exchange e = sessMgr.getExchange( s );
            
            if ( e == null ) {
                throw new SMTRuntimeException( "Session " + s.getComponentId() + " doesnt have configured REC so cant find exchange" );
            }
            
            if ( e.getId() > maxIdx ) maxIdx =  e.getId();
        }
        
        _sessionByExchangeIdx = new Session[maxIdx+1];
        
        for ( int i=0 ; i <= maxIdx ; i++ ) {
            _sessionByExchangeIdx[i] = null;
        }

        for( Session s : downStream ) {
            Exchange e = sessMgr.getExchange( s );
            int idx = e.getId();
            
            _log.info( "ExchangeRouter associate session " + s.getComponentId() + " with exchange " + e.getRecCode() + ", and exchangeIdx=" + idx );
            
            _sessionByExchangeIdx[idx] = s;
        }
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public final MessageHandler getRoute( final NewOrderSingle nos, final MessageHandler replyHandler ) {
        final int exId = nos.getInstrument().getExchange().getId();
        
        return _sessionByExchangeIdx[ exId ];
    }

    @Override
    public final MessageHandler[] getAllRoutes() {
        return _origDownSess;
    }
}
