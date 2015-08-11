/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;

public interface Encoder {
    /**
     * encode the message to the registered buffer
     * 
     * @NOTE always use getOffset() and getLength() for EACH message when extracting the data from the buffer
     * the offset will not necessarily be 0 due to optimisations for header processing which can adjust the offset per message
     * 
     * @param msg
     */
    public void encode( Message msg );
    
    public int getOffset();
    
    public int getLength();

    /**
     * invoked when a message cannot be sent downstream
     * 
     * @param msg       the outbound message that cannot be sent
     * @param errMsg    an error messsage to be encorporated into action message
     * @return null     if no action required, OR a reject  message that should be sent upstream
     */
    public Message unableToSend( Message msg, ZString errMsg );

    /**
     * @return get the underlying byte array, used  to avoid extra memcpy in logging
     */
    public byte[] getBytes();

    /**
     * @param calc - time zone calculator to use
     */
    public void setTimeZoneCalculator( TimeZoneCalculator calc );

    /**
     * @param nanoTiming if true nano stat collection is enabled
     */
    public void setNanoStats( boolean nanoTiming );
    
    /**
     * append statistics for message to buffer
     * 
     * @param outByteBuffer
     * @param msg
     * @param time
     */
    public void addStats( ReusableString outBuf, Message msg, long time );
}
