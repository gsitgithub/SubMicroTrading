/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.socket;

import com.rr.core.model.FixVersion;

public interface FixControllerConfig extends SessionControllerConfig {

    public boolean      isUseNewFix44GapFillProtocol();
    public FixVersion   getFixVersion();
    
    /**
     * safest behaviour is disconnect on gap, otherwise send a gap fill request
     */
    public boolean      isDisconnectOnSeqGap();     
}
