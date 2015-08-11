/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium.recovery;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.om.emea.exchange.millenium.MilleniumController;
import com.rr.om.emea.exchange.millenium.MilleniumSessionFactory;
import com.rr.om.emea.exchange.millenium.MilleniumSocketConfig;

public final class MilleniumRecoveryController extends MilleniumController {
    
    private   static final Logger _log = LoggerFactory.create( MilleniumRecoveryController.class );
    
    private int _lastRequestAppId = -1;

    public MilleniumRecoveryController( SeqNumSession session, MilleniumSocketConfig config ) {
        super( session, new MilleniumRecoveryStateFactory( config ), new MilleniumSessionFactory( config ) ); 
    }

    public void sendReplayRequests() {
        _lastRequestAppId = -1;
        nextRetryRequest();
    }

    public void retryLastRerequest() {
        request( _lastRequestAppId );
    }
    
    public void nextRetryRequest() {
        _lastRequestAppId = nextAppId( _lastRequestAppId+1 );
        
        if ( _lastRequestAppId == -1 ) {
            _log.info( "MilleniumRecoveryController: All valid appId's have had messages requested ... disconnecting recovery session" );
            
            _session.disconnect( false );
        }
        
        request( _lastRequestAppId );
    }
    
    private int nextAppId( int nextAppId ) {
        for( int curAppId=nextAppId ; curAppId < MAX_SEQ_NUM ; curAppId++ ) {
            int curSeqNum = _lastAppIdSeqNum[ curAppId ];
            
            if ( curSeqNum > 0 ) {
                return curAppId;
            }
        }
        
        return -1;
    }

    private void request( int curAppId ) {
        if ( curAppId >= 0 ) {
            int curSeqNum = _lastAppIdSeqNum[ curAppId ];
            Message logon = _sessionFactory.getRerequest( curAppId, curSeqNum );
            send( logon, false );
        }
    }
}