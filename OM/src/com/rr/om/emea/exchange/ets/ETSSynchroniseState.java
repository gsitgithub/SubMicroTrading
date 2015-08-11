/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.ets;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.session.Session;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.session.socket.SessionStateException;
import com.rr.om.session.state.SessionState;

public class ETSSynchroniseState implements SessionState {

    private static final Logger  _log           = LoggerFactory.create( ETSSynchroniseState.class );
    private static final ZString BAD_SESS_STATE = new ViewString( "Invalid session state, UTP doesnt support synchronise state" );
    
    private final Session        _session;

    private final ReusableString _logMsg     = new ReusableString(100);
    private final ReusableString _logMsgBase;
    
    public ETSSynchroniseState( SeqNumSession session, ETSController sessionController ) {
        _session          = session;
        _logMsgBase       = new ReusableString( "[ETSSynchroniseState-" + _session.getComponentId() + "] " );
    }
    
    @Override
    public void handle( Message msg ) throws SessionStateException {
        // UTP doesnt have a sync session, shouldnt be possible to get here
        
        _logMsg.copy( _logMsgBase ).append( BAD_SESS_STATE ).append( msg.getReusableType().toString() );
        
        _session.inboundRecycle( msg );

        throw new SessionStateException( _logMsg.toString() );
    }

    @Override
    public void connected() {
        _log.warn( "Unexpected connected event when in synchronise mode" );
    }
}
