/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import com.rr.core.model.Message;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.recovery.RecoverySessionContext;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.Session;
import com.rr.model.generated.internal.events.factory.EventRecycler;

public class DummyRecoveryController implements RecoveryController {

    private final MessageRecycler _inboundRecycler  = new EventRecycler();
    private final MessageRecycler _outboundRecycler = new EventRecycler();
    private final String _id;
    
    public DummyRecoveryController() {
        this( "DummyRecoveryController" );
    }

    public DummyRecoveryController( String id ) {
        _id = id;
    }

    @Override
    public void start() {
        // nothing
    }

    @Override
    public RecoverySessionContext startedInbound( Session sess ) {
        return new RecoverySessionContextImpl(sess, true);
    }

    @Override
    public void completedInbound( RecoverySessionContext ctx ) {
        // nothing
    }

    @Override
    public void failedInbound( RecoverySessionContext ctx ) {
        // nothing
    }

    @Override
    public void processInbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags ) {
        synchronized( _inboundRecycler ) {
            _inboundRecycler.recycle( msg );
        }
    }

    @Override
    public RecoverySessionContext startedOutbound( Session sess ) {
        return new RecoverySessionContextImpl(sess, false);
    }

    @Override
    public void completedOutbound( RecoverySessionContext ctx ) {
        // nothing
    }
    
    @Override
    public void failedOutbound( RecoverySessionContext ctx ) {
        // nothing
    }
    
    @Override
    public void processOutbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags ) {
        synchronized( _outboundRecycler ) {
            _outboundRecycler.recycle( msg );
        }
    }
    
    @Override
    public void reconcile() {
        // nothing
    }

    @Override
    public void commit() {
        // nothing
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}
