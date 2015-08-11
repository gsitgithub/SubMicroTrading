/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.recovery;

import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;
import com.rr.core.persister.Persister;
import com.rr.core.session.Session;

/**
 * RecoverSessionContext - each session will have two, inbound/outbound processed on different threads
 */
public interface RecoverySessionContext {

    /**
     * recovery context is either inbound (for received messages) or outbound (for sent messages)
     */
    public boolean isInbound();

    /**
     * @return the session associated with this recovery context
     */
    public Session getSession();

    /**
     * @return buffer to use for generating a warning message
     */
    public ReusableString getWarnMessage();

    /**
     * @return true if the session is configured with a chain session, used for detecting messages that didnt propogate to chain
     */
    public boolean hasChainSession();

    /**
     * @return true if the session will mark a message as sent in persistent flags
     */
    public boolean persistFlagConfirmSentEnabled();

    /**
     * @param persister used to reread events from persistence file during recovery 
     */
    public void setPersister( Persister persister );

    /**
     * @return the persister associated with this context
     */
    public Persister getPersister();

    /**
     * regenerate the requested event from the registered persister
     * 
     * @NOTE MUST ONLY USE WHEN 100% SURE NO CONFLICTING CROSS THREAD USE ie DURING RECONCILIATION
     * 
     * @param persistKey
     * @return
     */
    public Message regenerate( long persistKey );
}
