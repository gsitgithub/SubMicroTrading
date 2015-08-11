/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.thread;

import com.rr.core.component.SMTComponent;
import com.rr.core.utils.ThreadPriority;

/**
 * ControlThread is a long running (generally daemon) thread, load balances the thread across the registered executable streams
 * For best performance the control thread should spin and not wait/notify
 * Note implememtations can restrict the number of allowed ExecutableElements / streams
 */
public interface ControlThread extends SMTComponent {
    
    public void register( ExecutableElement ex );
    
    public void start();

    public void setStopping( boolean stopping );

    public void setPriority( ThreadPriority priority );

    public boolean isStarted();
    
    public boolean isStopping();

    /**
     * allows executable elements to notify control thread of a change of state, control thread can then wake if it was sleeping 
     */
    public void statusChange();
}
