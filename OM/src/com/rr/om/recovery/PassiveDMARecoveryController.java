/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.lang.ErrorCode;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.recovery.RecoverySessionContext;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.Session;
import com.rr.model.generated.internal.events.factory.EventRecycler;

/**
 * A standard OM recovery controller
 * 
 * Each session has two event streams one for inbound events, one for outbound events
 * Within a stream each event is guarenteed to be in time order (ie order received / sent)
 * 
 * Each session replays its inbound and outbound streams from persistence into the controller concurrently
 * Each session is replayed into the controller concurrently
 * 
 * Standard OM controller behaviour
 * 
 * 1) open orders remain open, ie client orders not auto cancelled
 * 2) orders received but not sent to exchange will be cancel rejected back to client
 * 3) fills from exchange not sent to client will be sent to client
 * 4) events received but not sent to hub are sent to hub 
 * 5) events sent but not sent to hub are sent to hub
 * 6) processors order map and trade registry should look as if the processes hadnt been restarted  
 * 7) and pending orders force cancelled (downstream and upstream - up requires reject then cancelled)
 *    this is to avoid edge case conditions
 */

public class PassiveDMARecoveryController implements RecoveryController {

    private static final Logger _log = LoggerFactory.create( PassiveDMARecoveryController.class );

    private static final ErrorCode ERR_INBOUND_REPLY  = new ErrorCode( "REC100", "FAILED replay of inbound messages" );
    private static final ErrorCode ERR_OUTBOUND_REPLY = new ErrorCode( "REC110", "FAILED replay of outbound messages" );
    
    private final MessageRecycler _inboundRecycler  = new EventRecycler();
    private final MessageRecycler _outboundRecycler = new EventRecycler();

    private final ConcurrentHashMap<Session,RecoverySessionContext> _inSessCtx  = new ConcurrentHashMap<Session,RecoverySessionContext>();
    private final ConcurrentHashMap<Session,RecoverySessionContext> _outSessCtx = new ConcurrentHashMap<Session,RecoverySessionContext>();

    private final String _id;

    public PassiveDMARecoveryController() {
        this( "PassiveDMARecoveryController" );
    }
    
    public PassiveDMARecoveryController( String id ) {
        _id = id;
    }

    @Override
    public void start() {
        _log.info( "Starting recovery process" );
    }

    @Override
    public RecoverySessionContext startedInbound( Session sess ) {
        RecoverySessionContextImpl ctx = new RecoverySessionContextImpl(sess, true);
        _inSessCtx.put( sess, ctx );
        return ctx;
    }

    @Override
    public void completedInbound( RecoverySessionContext ctx ) {
        _log.info( "Completed replay of inbound messages from " + ctx.getSession().getComponentId() );
    }

    @Override
    public void failedInbound( RecoverySessionContext ctx ) {
        _log.error( ERR_INBOUND_REPLY, "from " + ctx.getSession().getComponentId() );
    }

    @Override
    public void processInbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags ) {
        synchronized( _inboundRecycler ) {
            while( msg != null ) {
                _inboundRecycler.recycle( msg );
                msg = msg.getNextQueueEntry();
            }
        }
    }

    @Override
    public RecoverySessionContext startedOutbound( Session sess ) {
        RecoverySessionContextImpl ctx = new RecoverySessionContextImpl(sess, true);
        _outSessCtx.put( sess, ctx );
        return ctx;
    }

    @Override
    public void completedOutbound( RecoverySessionContext ctx ) {
        _log.info( "Completed replay of outbound messages from " + ctx.getSession().getComponentId() );
    }
    
    @Override
    public void failedOutbound( RecoverySessionContext ctx ) {
        _log.error( ERR_OUTBOUND_REPLY, "from " + ctx.getSession().getComponentId() );
    }
    
    @Override
    public void processOutbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags ) {
        synchronized( _outboundRecycler ) {
            while( msg != null ) {
                _outboundRecycler.recycle( msg );
                msg = msg.getNextQueueEntry();
            }
        }
    }
    
    @Override
    public void reconcile() {
        throw new RuntimeException( "NOT YET IMPLEMENTED" );
    }

    @Override
    public void commit() {
        throw new RuntimeException( "NOT YET IMPLEMENTED" );
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}
