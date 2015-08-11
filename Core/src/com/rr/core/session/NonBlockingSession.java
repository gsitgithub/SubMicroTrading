/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import java.io.IOException;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.collections.MessageQueue;

public interface NonBlockingSession extends Session {

    public boolean isMsgPendingWrite();

    public void retryCompleteWrite() throws IOException;

    public void logInboundError( Exception e );

    public void logInboundDecodingError( RuntimeDecodingException e );

    public void logOutboundEncodingError( RuntimeEncodingException e );

    public void logDisconnected( Exception ex );
    
    public MessageQueue getSendQueue();
    
    public MessageQueue getSendSyncQueue();
}
