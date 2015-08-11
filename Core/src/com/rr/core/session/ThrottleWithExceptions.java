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

/**
 * A simple throttler with no concept of which messages can and cant be rejected
 * 
 * NOT threadsafe, should only be used by the dispatch thread
 * 
 * Each dispatcher should own its own throttler
 * 
 * Very simple, uses fact that default value in timestamp array will be zero 
 */
public class ThrottleWithExceptions implements Throttler {

    private static final int DEFAULT_SIZE = 1000;
    
    private long    _timestamps[];
    private long    _timePeriodMS = 1000;
    private int     _throttleNoMsgs;
    private int     _idx    = -1;
    private String  _errMsg = "";
    
    public ThrottleWithExceptions() {
        size( DEFAULT_SIZE );
    }
    
    @Override
    public void setThrottleNoMsgs( int throttleNoMsgs ) {
        size( throttleNoMsgs );
    }

    @Override
    public void setThrottleTimeIntervalMS( long throttleTimeIntervalMS ) {
        _timePeriodMS = throttleTimeIntervalMS;
        setErrorMsg();
    }

    @Override
    public void setDisconnectLimit( int disconnectLimit ) {
        // not used 
    }

    @Override
    public void checkThrottle( Message msg ) throws ThrottleException {
        
        long time = System.currentTimeMillis();

        int nextIdx = _idx + 1;
        
        if ( nextIdx == _timestamps.length ) {
            nextIdx = 0;
        }

        long oldestTime = _timestamps[ nextIdx ];
        
        long period = Math.abs( time - oldestTime );

        if ( period < _timePeriodMS ) {
            throw new ThrottleException( _errMsg );
        }
        
        _timestamps[ nextIdx ] = time;
        _idx = nextIdx;
    }

    private void size( int throttleNoMsgs ) {
        _throttleNoMsgs = throttleNoMsgs;
        
        _timestamps = new long[ _throttleNoMsgs ];
  
        setErrorMsg();
    }

    private void setErrorMsg() {
        _errMsg = "Exceeded throttle rate of " + _throttleNoMsgs + " messages per " + _timePeriodMS + " ms";
    }

    @Override
    public boolean throttled( long now ) {
        int nextIdx = _idx + 1;
        
        if ( nextIdx == _timestamps.length ) {
            nextIdx = 0;
        }

        long oldestTime = _timestamps[ nextIdx ];
        
        long period = Math.abs( now - oldestTime );

        if ( period < _timePeriodMS ) {
            return true;
        }
        
        _timestamps[ nextIdx ] = now;
        _idx = nextIdx;
        
        return false;
    }
}
