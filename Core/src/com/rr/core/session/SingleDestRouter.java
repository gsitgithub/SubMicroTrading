/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;

public class SingleDestRouter implements MessageRouter {

    private final String _name = "SingleDestRouter";

    private final MessageHandler _delegate;
    
    public SingleDestRouter( MessageHandler delegate ) {
        super();
        
        _delegate = delegate;
    }

    @Override
    public final void handle( final Message msg ) {
        _delegate.handle( msg );
    }

    @Override
    public void handleNow( Message msg ) {
        _delegate.handle( msg );
    }

    @Override
    public void threadedInit() {
        // nothing
    }

    @Override
    public boolean canHandle() {
        return _delegate.canHandle();
    }

    @Override
    public String getComponentId() {
        return _name;
    }
}
