/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.recycler.MessageRecycler;

public class TradingSessionConfig extends SessionConfig {

    private boolean _isCancelOnDisconnect;
    private boolean _isGapFillAlllowed;

    public TradingSessionConfig() {
        super();
    }
    
    public TradingSessionConfig( String id ) {
        super( id );
    }

    public TradingSessionConfig( Class<? extends MessageRecycler> recycler ) {
        super( recycler );
    }

    public void setCancelOnDisconnect( boolean isCancelOnDisconnect ) {
        _isCancelOnDisconnect = isCancelOnDisconnect;
    }

    public boolean isCancelOnDisconnect() {
        return _isCancelOnDisconnect;
    }
    
    public boolean isGapFillAllowed() {
        return _isGapFillAlllowed;
    }

    public void setGapFillAllowed( boolean canGapFill ) {
        _isGapFillAlllowed = canGapFill;
    }
}
