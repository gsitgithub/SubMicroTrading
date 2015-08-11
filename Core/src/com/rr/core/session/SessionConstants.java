/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.lang.ErrorCode;

public interface SessionConstants {
    public static final int    CONNECT_WAIT_DELAY_MS           = 5000;              // wait for 5secs then check if reconnected
    public static final int    DEFAULT_MAX_DELAY_MS            = 1000 * 60 * 15;    // max reconnect delay 15min

    public static final int    DEFAULT_MAX_CONNECT_ATTEMPTS    = 10;

    public static final ErrorCode ERR_MAX_ATT_EXCEEDED         = new ErrorCode( "SEC100", "Exceeded max connect attempts, pausing session which will " +
                                                                                          "require manual restart : " );
    public static final ErrorCode ERR_OPEN_SOCK                = new ErrorCode( "SEC200", "Error opening socket: " );
}
