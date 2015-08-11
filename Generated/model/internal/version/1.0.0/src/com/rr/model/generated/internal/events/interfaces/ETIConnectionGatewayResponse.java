/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.type.ETISessionMode;
import com.rr.model.generated.internal.type.ETIEnv;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;

public interface ETIConnectionGatewayResponse extends BaseETIResponse, Message {

   // Getters and Setters
    public int getRequestTime();

    public int getSendingTime();

    public int getMsgSeqNum();

    public int getGatewayID();

    public int getGatewaySubID();

    public int getSecGatewayID();

    public int getSecGatewaySubID();

    public ETISessionMode getSessionMode();

    public ETIEnv getTradSesMode();

    @Override
    public void dump( ReusableString out );

}
