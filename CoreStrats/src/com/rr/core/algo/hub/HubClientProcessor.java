/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.hub;

import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.model.Message;
import com.rr.hub.HubProcessor;


/**
 * client processor for instructions from HUB
 */
public class HubClientProcessor extends HubProcessor {

    public HubClientProcessor( String id, MessageDispatcher inboundDispatcher ) {
        super( id, inboundDispatcher );
    }

    @Override
    public void handleNow( Message msg ) {
        
        _logMsg.copy( "RECEIVED MESSAGE FROM HUB : " );
        
        msg.dump( _logMsg );
        
        _log.info( _logMsg );
        
        _eventRecycler.recycle( msg );
    }
}
