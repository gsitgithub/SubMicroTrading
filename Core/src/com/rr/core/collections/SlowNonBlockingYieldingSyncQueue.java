/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import com.rr.core.model.Message;
import com.rr.core.utils.ThreadUtils;

/**
 * Queue which can be used on PC to stop multi sessions spinning and grinding box to halt
 */
public class SlowNonBlockingYieldingSyncQueue extends NonBlockingSyncQueue {

    private static final long MOD_CNT = 1000;
    
    private int     _delayMS    = 50;
    private long    _cnt        = 0;

    public SlowNonBlockingYieldingSyncQueue() {
        super();
    }

    public SlowNonBlockingYieldingSyncQueue( String id ) {
        super( id );
    }

    @Override
    public Message poll() {
        Message t = super.poll();

        if ( t == null ) {
            if ( (_cnt++ % MOD_CNT) == 0 ) {
                ThreadUtils.pause( _delayMS );
            }
        }
        
        return t;
    }
}
