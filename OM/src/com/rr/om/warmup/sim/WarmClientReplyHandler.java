/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.sim;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.om.dummy.warmup.ClientStatsManager;

public class WarmClientReplyHandler implements MessageHandler {
    
    private static final Logger     _log            = LoggerFactory.console( WarmClientReplyHandler.class );

    private final String            _name = "WarmClientReply";
    
    private EventRecycler           _eventRecycler  = new EventRecycler();
    private ClientStatsManager      _statsMgr;
    private volatile int            _received       = 0;

    private volatile int            _statBasedReplies = 0;
    
    public WarmClientReplyHandler( ClientStatsManager statsMgr ) {
        _statsMgr = statsMgr;
    }
                           
    @Override
    public void handle( Message msg ) {
        handleNow( msg );
    }

    @Override
    public void handleNow( Message msg ) {
        boolean statsAdded = false;
        
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERACK:
            NewOrderAck ack = (NewOrderAck) msg;
            if ( _statsMgr.replyRecieved( ack.getClOrdId(), ack.getAckReceived() ) ) statsAdded = true;
            else {
                _log.info( "WarmClientReplyHandler: Stats cant find entry for exec " + 
                           msg.getReusableType().toString() + ", clOrdId=" + ack.getClOrdId() );
            }
            break;
        case EventIds.ID_REPLACED:
        case EventIds.ID_CANCELLED:
            CommonExecRpt exec  = (CommonExecRpt) msg;
            if ( _statsMgr.replyRecieved( exec.getClOrdId(), Utils.nanoTime() ) ) statsAdded = true;
            else {
                _log.info( "WarmClientReplyHandler: Stats cant find entry for exec " + 
                           msg.getReusableType().toString() + ", clOrdId=" + exec.getClOrdId() );
            }
            break;
        case EventIds.ID_REJECTED:              
            Rejected rej  = (Rejected) msg;
            if ( _statsMgr.replyRecieved( rej.getClOrdId(), Utils.nanoTime() ) ) statsAdded = true;
            else {
                // in non blocking sessions, if downstream session is disconnected the order will be rejected and the 
                // sent callback is NOT invoked so the order will not be registered in the statsMgr
                
                _log.info( "WarmClientReplyHandler: Stats cant find entry for exec " + 
                           msg.getReusableType().toString() + ", clOrdId=" + rej.getClOrdId() + " : " + rej.getText() );
            }
            break;
        case EventIds.ID_CANCELREJECT:
            CancelReject crej  = (CancelReject) msg;
            if ( _statsMgr.replyRecieved( crej.getClOrdId(), Utils.nanoTime() ) ) statsAdded = true;
            else {
                _log.info( "WarmClientReplyHandler: Stats cant find entry for exec " + 
                           msg.getReusableType().toString() + ", clOrdId=" + crej.getClOrdId() );
            }
            break;
        case EventIds.ID_TRADENEW:
            break;
        default:
            _log.info( "WarmClientReplyHandler: Unexpected reply " + msg.getReusableType().toString() );
            break;
        }
        
        if ( statsAdded ) {
            synchronized( this ) {
                ++_statBasedReplies;
                ++_received;
            }
        }

        _eventRecycler.recycle( msg );
    }

    public synchronized int getReceived() {
        return _received;
    }

    public synchronized int getStatsBasedReplies() {
        return _statBasedReplies;
    }

    public void reset() {
        _received         = 0;
        _statBasedReplies = 0;
    }

    @Override public boolean canHandle() { return true; }
    @Override public void threadedInit() { /* nothing */ }

    @Override
    public String getComponentId() {
        return _name;
    }
}

