/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.model.Message;

public interface Throttler {

    public void setThrottleNoMsgs( int throttleNoMsgs );

    public void setThrottleTimeIntervalMS( long throttleTimeIntervalMS );

    public void setDisconnectLimit( int disconnectLimit );

    /**
     * @return true if passed throttle check, false if exceeded limit
     */
    public void checkThrottle( Message msg ) throws ThrottleException;

    /**
     * optional throttle check method
     * 
     * will throw runtime exception if method not supported by implementation
     * 
     * @return true if throttled .... can be used as alternative to checkThrottle
     */
    public boolean throttled( long now );
}
