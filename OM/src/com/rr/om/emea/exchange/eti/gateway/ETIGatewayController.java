/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.gateway;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.model.generated.internal.events.interfaces.ETIConnectionGatewayRequest;
import com.rr.model.generated.internal.events.interfaces.ETIConnectionGatewayResponse;
import com.rr.om.emea.exchange.eti.trading.ETIController;
import com.rr.om.emea.exchange.eti.trading.ETISessionFactory;
import com.rr.om.emea.exchange.eti.trading.ETISocketConfig;

public final class ETIGatewayController extends ETIController {
    
    private static final Logger  _log = LoggerFactory.create( ETIGatewayController.class );
    
    private Session _tradingSession;

    public ETIGatewayController( SeqNumSession session, ETISocketConfig config ) {
        super( session, new ETIGatewayStateFactory( config ), new ETISessionFactory( config ) ); 
    }

    public void setTradingSession( Session tradingSession ) {
        _tradingSession = tradingSession;
    }
    
    public Session getTradingSession() {
        return _tradingSession;
    }
    
    public void initiateTradingSession( ETIConnectionGatewayResponse rep ) {
        if ( ! _tradingSession.isConnected() ) {
            ETISocketConfig sc = (ETISocketConfig) _tradingSession.getConfig();

            if ( sc.isForceTradingServerLocalhost() ) {
                _log.warn( "FORCING TRADING SERVER TO LOCALHOST" );

                sc.setHostname( new ViewString("127.0.0.1") );
            } else {
                ReusableString newIP = new ReusableString(15);
                
                intIPtoFullAddr( newIP, rep.getGatewayID() );
                sc.setHostname( newIP );
            }
            
            sc.setPort( rep.getGatewaySubID() );
    
            ReusableString newSecIP = new ReusableString(15);
            
            intIPtoFullAddr( newSecIP, rep.getSecGatewayID() );
            sc.setSecHostname( newSecIP );
            sc.setSecPort( rep.getSecGatewaySubID() );
            
            _tradingSession.connect();
        }
    }

    public boolean isTradingSessionConnected() {
        return _tradingSession.isConnected();
    }
    
    public final void sendConnectionGatewayRequest() {
        ETIConnectionGatewayRequest request = ((ETISessionFactory) _sessionFactory).getConnectionGatewayRequest();
        send( request, true );
    }

    public void sendConnectionGatewayResponse() {
        ETIConnectionGatewayResponse response = ((ETISessionFactory) _sessionFactory).getConnectionGatewayResponse();
        _log.info( "Sent connection gateway response, start the exchange trading exchange simulator session" );
        send( response, true );
    }

    public static void intIPtoFullAddr( ReusableString ipDest, int ipAsInt ) {
        int n1 = (ipAsInt >> 0)  & 0xFF;
        int n2 = (ipAsInt >> 8)  & 0xFF;
        int n3 = (ipAsInt >> 16) & 0xFF;
        int n4 = (ipAsInt >> 24) & 0xFF;
                
        ipDest.append( n4 );
        ipDest.append( '.' );
        ipDest.append( n3 );
        ipDest.append( '.' );
        ipDest.append( n2 );
        ipDest.append( '.' );
        ipDest.append( n1 );
    }
}