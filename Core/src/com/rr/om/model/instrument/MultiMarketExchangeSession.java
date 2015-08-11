/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.instrument;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;

public class MultiMarketExchangeSession extends SingleExchangeSession {

    private final Map<ZString,ExchangeSession> _sessions;

    public MultiMarketExchangeSession( ZString  id,
                                       Calendar openCal, 
                                       Calendar startContinuous, 
                                       Calendar endContinuous, 
                                       Calendar halfDayCal, 
                                       Calendar endCal,
                                       Auction  openAuction, 
                                       Auction  intraDayAuction, 
                                       Auction  closeAuction,
                                       Map<ZString,ExchangeSession> sessions ) {
        
        super( id, openCal, startContinuous, endContinuous, halfDayCal, endCal, openAuction, intraDayAuction, closeAuction );
        
        _sessions = (sessions!=null) ? sessions : new HashMap<ZString,ExchangeSession>();
    }


    @Override
    public ExchangeSession getExchangeSession( ZString marketSegment ) {
        
        if ( marketSegment == null ) return this;
        
        ExchangeSession sess = _sessions.get( marketSegment );
        
        return (sess == null) ? this : sess;
    }
    
    @Override
    public ReusableString toString( ReusableString buf ) {
        buf.append( "MultiMarketSession defaultSess {" );
        super.toString( buf );
        buf.append( "}\n      segment sessions\n" );
        Set<ExchangeSession> _vals = new HashSet<ExchangeSession>( _sessions.values() );
        for ( ExchangeSession sess : _vals ) {
            buf.append( "        segmentSession {" );
            sess.toString( buf );
            buf.append( "}" );
        }

        buf.append( "\n    segmentToSessionIDMap\n" );

        for ( Map.Entry<ZString,ExchangeSession> entry : _sessions.entrySet() ) {
            ZString         segment = entry.getKey();
            ExchangeSession sess    = entry.getValue();
            buf.append( "        segment " ).append( segment ).append( " to " ).append( sess.getId() ).append( "\n" );
        }
        
        return buf;
    }
}
