/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import com.rr.core.lang.ZString;
import com.rr.core.session.socket.SessionControllerConfig;
import com.rr.model.generated.internal.type.ETIEnv;
import com.rr.model.generated.internal.type.ETISessionMode;

public interface ETIConfig extends SessionControllerConfig {

    public final static String VERSION = "0.9";
    
    public static final int DEFAULT_THROTTLE_MSGS = 1024;       // rounded down to closest power of 2
    public static final int DEFAULT_THROTTLE_PERIOD_MS = 1000;

    // for connection gateway login
    public int              getUserId();
    public ZString          getPassword();
    public ETIEnv           getEnv();
    public ETISessionMode   getETISessionMode();

    // session logon
    public int              getPartyIDSessionID();
    public ZString          getETIVersion();
    public ZString          getSessionLogonPassword();
    public ZString          getAppSystemName();

    public ZString          getTraderPassword();

    public long             getLocationId();
    
    public boolean          isForceTradingServerLocalhost();
    
    /**
     * methods for emulating exchange
     */
    
    public int              getEmulationTestHost();
    public int              getEmulationTestPort();

}
