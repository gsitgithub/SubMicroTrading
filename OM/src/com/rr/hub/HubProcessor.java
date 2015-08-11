/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.hub;

import com.rr.core.component.SMTControllableComponent;
import com.rr.core.component.SMTStartContext;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.Stopable;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.model.generated.internal.events.factory.EventRecycler;


public class HubProcessor implements MessageHandler, SMTControllableComponent, Stopable {

    protected static final Logger     _log = LoggerFactory.create( HubProcessor.class );
    
    private   final String            _componentId;
    private   final MessageDispatcher _inboundDispatcher;
    private         boolean           _running = false;

    protected       EventRecycler     _eventRecycler;
    protected final ReusableString    _logMsg = new ReusableString();

    public HubProcessor( String id, MessageDispatcher inboundDispatcher ) {
        _componentId = id;
        _inboundDispatcher = inboundDispatcher;
        
        _inboundDispatcher.setHandler( this );
    }
    
    @Override
    public String getComponentId() {
        return _componentId;
    }

    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }

    @Override
    public void prepare() {
        // nothing
    }

    @Override
    public void startWork() {
        // nothing
    }

    @Override
    public void stopWork() {
        _inboundDispatcher.setStopping();
    }

    @Override
    public void threadedInit() {
        _eventRecycler = new EventRecycler();
        _running = true;
    }

    @Override
    public void handle( Message msg ) {
        if ( msg != null ) {
            _inboundDispatcher.dispatch( msg );
        }
    }

    @Override
    public void handleNow( Message msg ) {
        _logMsg.copy( "HUB RECEIVED : " );
        
        msg.dump( _logMsg );
        
        _log.info( _logMsg );
        
        _eventRecycler.recycle( msg );
    }

    @Override
    public final boolean canHandle() {
        return _running;
    }

    @Override
    public final void stop() {
        stopWork();
    }
}
