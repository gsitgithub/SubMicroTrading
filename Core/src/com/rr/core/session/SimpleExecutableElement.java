/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import java.util.concurrent.atomic.AtomicBoolean;

import com.rr.core.collections.MessageQueue;
import com.rr.core.component.SMTStartContext;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.NullMessage;
import com.rr.core.thread.ControlThread;
import com.rr.core.thread.ExecutableElement;
import com.rr.core.utils.SMTRuntimeException;


/**
 * simple wrapper for ExecutableElement to a single message handler
 * 
 * does NOT own spinning control thread, that is shared and which round robins sessions 
 */
public final class SimpleExecutableElement implements MessageDispatcher, ExecutableElement {

    private static final Logger   _log = LoggerFactory.create( SimpleExecutableElement.class );

    private final    ControlThread     _ctl;

    private final    MessageQueue      _queue;
    private          MessageHandler    _handler;

    
    private          Message           _curMsg   = null;

    private          AtomicBoolean     _stopping = new AtomicBoolean(false);
    
    private final    String            _id;

    
    // array index assigned to session to save map lookup

    public SimpleExecutableElement( String id, ControlThread ctl, MessageQueue queue ) {
        _ctl   = ctl;
        _id    = id;
        _queue = queue;
        
        ctl.register( this );
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
    public void threadedInit() {
        _handler.threadedInit();
    }

    @Override
    public void notReady() {
        // NADA
    }
    
    @Override
    public void execute() throws Exception {
        if ( _handler.canHandle() ) {
            _curMsg = _queue.poll();                      
            if ( _curMsg != null && _curMsg.getReusableType() != CoreReusableType.NullMessage ) {
                _handler.handleNow( _curMsg );
            }
        }
    }

    @Override
    public void handleExecutionException( Exception ex ) {
        
        if ( _curMsg != null ) {
            _log.warn( "SimpleExecutableElement " + getComponentId() + ", msgSeqNum=" + _curMsg.getMsgSeqNum() + 
                       ", sess=" + _handler.getComponentId() + " exception " + ex.getMessage() );
        }
        
        // some problem, possibly disconnect, poke controller to wake up anything waiting on controller passive lock
        _ctl.statusChange(); // Mem barrier
    }

    @Override
    public boolean checkReady() {
        return ( _handler.canHandle() );
    }

    @Override
    public synchronized void start() {
        _ctl.start();
    }
    
    @Override
    public void setHandler( MessageHandler handler ) {
        if ( _handler != null ) throw new SMTRuntimeException( "SingleExecutableElement MISCONFIGURATION : handler is already set" );
        
        _handler = handler;
    }

    @Override
    public void dispatch( final Message msg ) {
        if ( msg != null ) {
            _queue.add( msg );
        }
    }

    @Override
    public void setStopping() {
        _queue.add( new NullMessage() ); // wake up queue
    }
    
    @Override
    public void stop() {
        if ( _stopping.compareAndSet( false, true ) ) {
            _ctl.setStopping( true );
            
            _queue.add( new NullMessage() ); // wake up queue
        }
    }
    
    @Override
    public void handlerStatusChange( MessageHandler handler, boolean connected ) {
        _ctl.statusChange();
    }

    @Override
    public boolean canQueue() {
        return true;
    }
    
    @Override
    public String info() {
        return "SimpleExecutableElement( " + _id + " )";
    }

    @Override
    public void dispatchForSync( Message msg ) {
        throw new SMTRuntimeException( "SingleExecutableElement : dispatchForSync not allowed" );
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
        start();
    }

    @Override
    public void stopWork() {
        stop();
    }
}
