/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils.lock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * for use as an INTERNAL lock instead of synchronized when spinning should continue until lock has been grabbed
 * 
 * use with great care or process is at risk of deadlock !
 * 
 * always use with try .. finally : unlock
 * 
 * @NOTE NOT reentrant ! 
 */
public class UnfairSpinningNonReentractLock implements Lock {

    private final AtomicBoolean  _lock = new AtomicBoolean( false );
    
    @Override
    public void lock() {
        while( !_lock.compareAndSet( false, true ) ) {
            // spin
        }
    }

    @Override
    public void release() {
        _lock.set( false );
    }
}
