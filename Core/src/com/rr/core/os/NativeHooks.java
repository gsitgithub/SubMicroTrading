/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.os;

public interface NativeHooks {
    
    public static final int MAX_PRIORITY = 10;

    /**
     * set priority 1 to 10 with 10 highest
     * @param priority
     */
    public void setPriority( Thread thread, int mask, int priority );

    public void sleep( int ms );

    public void sleepMicros( int micros );

    public void setProcessMaxPriority();

    public long nanoTimeRDTSC();

    public long nanoTimeMonotonicRaw();
}
