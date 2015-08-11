/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import com.rr.core.lang.HasReusableType;
import com.rr.core.lang.ReusableString;


public interface Message extends HasReusableType {

    /**
     * a message can belong to at most one queue ... tho this is NOT checked
     * 
     * @param nextNode - the next node in the queue
     */
    public void attachQueue( Message nextNode );
    
    /**
     * 
     * sets internal nextMessage queue ref to null
     */
    public void detachQueue();

    /**
     * 
     * @return the next message value from attachQueue
     */
    public Message getNextQueueEntry();
    
    /**
     * @param handler to associate with this message
     */
    public void setMessageHandler( MessageHandler handler );
    
    /**
     * @return message handler associated with Message
     */
    public MessageHandler getMessageHandler();
    
    public int getMsgSeqNum();

    public void setMsgSeqNum( int seqNum );

    public void setFlag( MsgFlag flag, boolean isOn );

    public boolean isFlagSet( MsgFlag flag );

    /**
     * dump the message to the supplied reusable string
     */
    public void dump( ReusableString out ); 
}
