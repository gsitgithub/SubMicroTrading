/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.units;

import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.warmup.JITWarmup;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.om.router.RoundRobinRouter;

public class WarmupRouters implements JITWarmup {

    private int _warmupCount;
    
    public WarmupRouters( int warmupCount ) {
        _warmupCount = warmupCount;
    }
    
    @Override
    public String getName() {
        return "Routers";
    }

    @Override
    public void warmup() throws Exception {
        warmRoundRobinRouter();
    }

    @SuppressWarnings( "unused" )
    private void warmRoundRobinRouter() {
        MessageHandler m = new MessageHandler() {
                    public int _last;
                    @Override public void threadedInit()        { /* nothing */ }
                    @Override public void handle( Message msg ) { /* nothing */ }
                    @Override public boolean canHandle()        { return true;  }
                    @Override public String getComponentId()    { return null; }
                    @Override public void handleNow( Message msg ) {
                        _last = msg.getMsgSeqNum();
                    }
                };
        MessageHandler[] handlers = { m };
        RoundRobinRouter r = new RoundRobinRouter( handlers );
        ClientNewOrderSingleImpl t = new ClientNewOrderSingleImpl();
        MessageHandler routed;
        for( int i=0 ; i < _warmupCount ; ++i ) {
            routed = r.getRoute( t, null );
        }
    }
}
