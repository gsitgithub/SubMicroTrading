/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.thread;

import com.rr.core.utils.Utils;

/**
 * slow version of NonBlockingWorkerMultiplexor useful for low end PC's
 */
public final class SlowNonBlockingWorkerMultiplexor extends BaseNonBlockingWorkerMultiplexor {

    private static final int THREAD_THROTTLE_MS = 1;

    private static final long THROTTLE_BATCH = 1000;
    
    private int _delay = THREAD_THROTTLE_MS;

    private long _cnt;

    public SlowNonBlockingWorkerMultiplexor( String id, ControlThread ctl ) {
        super( id, ctl );
    }

    @Override
    public void execute() throws Exception {

        super.execute();
        
        if ( ++_cnt % THROTTLE_BATCH == 0 ) {
            Utils.delay( _delay );
        }
    }
}
