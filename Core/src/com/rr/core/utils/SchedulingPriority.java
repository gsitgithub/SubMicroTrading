/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

public interface SchedulingPriority {

    public static final int UNKNOWN_MASK = -1;
    
    public enum CPU {
        ANY, MAIN, SECONDARY, THIRD, FOURTH;
    }
    
    public enum CoreThread {
        ANY, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT;
    }
    
    public int        getPriority();

    public CPU        getCPU();

    public CoreThread getCoreThread();

    /**
     * 
     * set the mask (from config file  cpumasks.cfg)
     * @param mask
     */
    public void setMask( int mask );

    public int getMask();

    /**
     * allow the priority to be set from config
     * 
     * @param priority
     */
    public void setPriority( int priority );
}
