/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.socket;

import com.rr.core.model.Message;
import com.rr.core.session.Session;

public interface SeqNumSession extends Session {

    /**
     * @return config with state details
     */
    public SessionControllerConfig getStateConfig();
    
    /**
     * create gap fill messsages UPTO but not including the gapFillToNewSeqNum
     * 
     * @param origExpInSeqNum
     * @param gapFillToNewSeqNum
     * @throws SessionStateException
     */
    public void gapFillInbound( int origExpInSeqNum, int gapFillToNewSeqNum ) throws SessionStateException;

    /**
     * extend the persistence index from nextOutSeqNo to newSeqNum
     * 
     * @param nextOutSeqNo
     * @param newSeqNum
     */
    public void gapExtendOutbound( int nextOutSeqNo, int newSeqNum );

    /**
     * retrieve previously sent requested message from persistence store and decode it
     * 
     * @param curMsgSeqNo
     * 
     * @return decoded message or NULL if unable to obtain
     */
    public Message retrieve( int curMsgSeqNo );

    /**
     * @param recMsg
     * @return return true if recMsg is a session message
     */
    public boolean isSessionMessage( Message recMsg );

    /**
     * if client looses messages they have sent us, allow index to be truncated down
     * 
     * @param fromSeqNum
     * @param toSeqNum
     * @throws SessionStateException if unable to comply
     */
    public void truncateInboundIndexDown( int fromSeqNum, int toSeqNum ) throws SessionStateException;
    
    
    /**
     * allow admin override to set index to be truncated down
     *
     * @NOTE ONLY FOR USE IN ADMINS
     * 
     * @param fromSeqNum
     * @param toSeqNum
     * @throws SessionStateException if unable to comply
     */
    public void truncateOutboundIndexDown( int fromSeqNum, int toSeqNum ) throws SessionStateException;

    /**
     * @return seqNum of last message processed
     */
    public int getLastSeqNumProcessed();
}
