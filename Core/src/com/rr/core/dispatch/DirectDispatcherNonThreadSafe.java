/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.dispatch;

import com.rr.core.component.SMTStartContext;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;

public class DirectDispatcherNonThreadSafe implements MessageDispatcher {

    private          MessageHandler _handler;
    private volatile boolean        _started = false;

    private final    String         _id;

    public DirectDispatcherNonThreadSafe() {
        this( null );
    }
    
    public DirectDispatcherNonThreadSafe( String id ) {
        _id = id;
    }
    
    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
    public synchronized void start() {
        if ( !_started ) {
            _handler.threadedInit();
            _started = true;
        }
    }

    @Override
    public void setHandler( MessageHandler handler ) {
        _handler = handler;
    }

    @Override
    public void dispatch( Message msg ) {
        _handler.handleNow( msg );
    }

    @Override
    public void setStopping() {
        // nothing
    }

    @Override
    public void handlerStatusChange( MessageHandler handler, boolean isOk ) {
        // nothing
    }

    @Override
    public boolean canQueue() {
        return false;               // CANT ENQUEUE
    }

    @Override
    public String info() {
        return "DirectDispatcherNonThreadSafe";
    }

    @Override
    public void dispatchForSync( Message msg ) {
        _handler.handleNow( msg );
    }

    @Override
    public void startWork() {
        start();
    }

    @Override
    public void stopWork() {
        setStopping();
    }

    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }

    @Override
    public void prepare() {
        // nothing
    }
}
