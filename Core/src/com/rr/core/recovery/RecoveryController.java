/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.recovery;

import com.rr.core.component.SMTComponent;
import com.rr.core.model.Message;
import com.rr.core.session.Session;

/**
 * recovery controller, if recovery fails should have config switch to allow force override if problem
 * 
 * Note that recovery is highly concurrent with each session potentially having two threads one for inbound and one for outbound messages
 * 
 * Each session has two RecoverySessionContext's one for inbound, one for outbound messages
 */
public interface RecoveryController extends SMTComponent {
    
    /**
     * start must be invoked once before sessions played into the controller
     */
    public void start();
    
    /**
     * inform recovery controller starting to process incoming messages
     *  
     * @param name of session
     * @return RecoverySessionContext a subcontext for the sessions inbound messages
     */
    public RecoverySessionContext startedInbound( Session sess );

    public void completedInbound( RecoverySessionContext ctx );

    public void failedInbound( RecoverySessionContext ctx );

    /**
     * @param ctx the session sub context
     * @param persistKey a key which can be used later to reread the message from session persistence .. required for two phase recovery
     * @param msg the recovered message
     */
    public void processInbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags );

    /**
     * inform recovery controller starting to process outgoing messages
     *  
     * @param name of session
     * @return RecoverySessionContext a subcontext for the sessions outbound messages
     */
    public RecoverySessionContext startedOutbound( Session sess );

    public void completedOutbound( RecoverySessionContext ctx );

    public void failedOutbound( RecoverySessionContext ctx );

    /**
     * @param ctx the session sub context
     * @param persistKey a key which can be used later to reread the message from session persistence .. required for two phase recovery
     * @param msg the recovered message
     */
    public void processOutbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags );


    /**
     * reconcile should only be invoked once after all sessions finished replaying records into the controller
     * 
     * note it is expected that there should not be many queued events as even in systems trading millions of orders actual live orders
     * will be in thousands and actions from reconcile will only apply to live orders
     * 
     * reconcile generates events to be sent upstream and downstrea,
     * 
     * @NOTE YOU MUST INVOKE commit TO SUBMIT / QUEUE THE EVENTS WITHIN THE APPROPRIATE SESSION 
     */
    public void reconcile();
    
    /**
     * send all the enqueued messages from the reconcile
     */
    public void commit();
}
