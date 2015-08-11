/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.MessageQueue;
import com.rr.core.component.SMTStartContext;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.Constants;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.utils.ThreadPriority;

/**
 * a pausable dispatcher customised for session awareness
 */
public final class SessionThreadedDispatcher implements MessageDispatcher {

    private final    MessageQueue                  _queue;
    private final    MessageQueue                  _syncQueue;
    private final    InternalThreadedDispatcher    _threadDispatcher;
    private volatile boolean                       _started = false;
    private final    String                        _id;

    public SessionThreadedDispatcher( String id, MessageQueue queue, ThreadPriority threadPriority ) {
        this( id, queue, threadPriority, Constants.LOW_PRI_LOOP_WAIT_MS );
    }
    
    public SessionThreadedDispatcher( String id, MessageQueue queue, ThreadPriority threadPriority, int maxSleep ) {
        super();
        _queue            = queue;
        _syncQueue        = new BlockingSyncQueue();
        _id               = id;

        String dispatchName = "DISPATCH_" + id;
        _threadDispatcher = new InternalThreadedDispatcher( dispatchName, threadPriority, queue, _syncQueue, maxSleep );
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public synchronized void start() {
        if ( !_started ) {
            _threadDispatcher.setDaemon( true );
            _threadDispatcher.start();
            _started = true;
        }
    }
    
    @Override
    public void setHandler( MessageHandler handler ) {
        
        Session sess = (Session) handler;
        
        _threadDispatcher.setSession( sess );
    }

    @Override
    public void dispatch( Message msg ) {
        _queue.add( msg );
    }

    @Override
    public void dispatchForSync( Message msg ) {
        _syncQueue.add( msg );
    }

    @Override
    public void setStopping() {
        _threadDispatcher.setFinished();
    }
    
    @Override
    public void handlerStatusChange( MessageHandler handler, boolean connected ) {
        _threadDispatcher.connected( connected );
    }

    @Override
    public boolean canQueue() {
        return true;
    }

    @Override
    public String info() {
        return "SessionThreadedDispatcher( " + _threadDispatcher.getName() + ", queue=" + _queue.getClass().getSimpleName() + ")";
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
