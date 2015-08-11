/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix.cme;

import java.io.IOException;
import java.util.List;

import com.rr.core.model.Message;
import com.rr.om.warmup.sim.SimCMEFastFixSender;

/**
 * a specialisation of SimCMEFastFixSender but doesnt send generated messages over socket
 */
public final class SimFastFixMDGenWithMsgHandler extends SimCMEFastFixSender {

    private final Listener _listener;

    public interface Listener {
        public void onEvent( Message msg, int templateId );
    }
    
    public SimFastFixMDGenWithMsgHandler( List<byte[]> templateRequests, boolean nanoTiming, Listener l ) {
        super( templateRequests, nanoTiming, null );
        
        _listener = l;
    }

    @Override
    protected void doSend( int templateId, Message msg, long now ) throws IOException {
        _listener.onEvent( msg, templateId );
    }
}
