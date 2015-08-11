/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.router;

import com.rr.core.model.MessageHandler;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;

public final class SingleDestRouter implements OrderRouter {

    private final MessageHandler   _dest;
    private final MessageHandler[] _all;
    private final String           _id;
    
    public SingleDestRouter( final MessageHandler dest ) {
        this( null, dest );
    }
    
    public SingleDestRouter( String id, final MessageHandler dest ) {
        super();
        _dest = dest;
        _all  = new MessageHandler[] { dest };
        _id = id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
    public final MessageHandler getRoute( final NewOrderSingle nos, final MessageHandler replyHandler ) {
        return _dest;
    }

    @Override
    public final MessageHandler[] getAllRoutes() {
        return _all;
    }
}
