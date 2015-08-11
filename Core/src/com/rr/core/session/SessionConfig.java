/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.component.SMTComponent;
import com.rr.core.properties.DynamicConfig;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.utils.SMTRuntimeException;

public class SessionConfig implements DynamicConfig, SMTComponent {

    private String                           _id;

    private Class<? extends MessageRecycler> _recycler;
    private boolean                          _isMarkConfirmationEnabled = false;
    private SessionDirection                 _direction;
    private Class<? extends Throttler>       _throttlerClass = ThrottleWithExceptions.class;
    private int                              _maxMsgsPerSecond = 0;                             // default no limit
    private int                              _maxResendRequestSize = 0;                         // default no batching for resend requests

    public SessionConfig() {
        super();
    }
    
    public SessionConfig( String id ) {
        _id = id;
    }
    
    public SessionConfig( Class<? extends MessageRecycler> recycler ) {
        super();
        _recycler                  = recycler;
    }

    public boolean isMarkConfirmationEnabled() {
        return _isMarkConfirmationEnabled;
    }

    public Class<? extends MessageRecycler> getRecycler() {
        return _recycler;
    }

    public void setMarkConfirmationEnabled( boolean isMarkConfirmationEnabled ) {
        _isMarkConfirmationEnabled = isMarkConfirmationEnabled;
    }

    public void setRecycler( Class<? extends MessageRecycler> recycler ) {
        _recycler = recycler;
    }

    @Override
    public void validate() throws SMTRuntimeException {
        if ( _recycler == null ) throw new SMTRuntimeException( "SessionConfig missing recycler" );
    }

    public boolean isOpenUTC( long currentTimeMillis ) {
        return true;
    }

    @Override
    public String info() {
        return ((_direction != null) ? _direction.toString() : "") +
               ", maxMsgsPerSecond=" + _maxMsgsPerSecond;
    }

    public SessionDirection getDirection() {
        return _direction;
    }

    public void setDirection( SessionDirection direction ) {
        _direction = direction;
    }

    public Class<? extends Throttler> getThrottlerClass() {
        return _throttlerClass;
    }

    public int getMaxMsgsPerSecond() {
        return _maxMsgsPerSecond;
    }

    public int getMaxResendRequestSize() {
        return _maxResendRequestSize;
    }

    public void setMaxMsgsPerSecond( int maxMsgsPerSecond ) {
        _maxMsgsPerSecond = maxMsgsPerSecond;
    }

    public void setMaxResendRequestSize( int maxResendRequestSize ) {
        _maxResendRequestSize = maxResendRequestSize;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}