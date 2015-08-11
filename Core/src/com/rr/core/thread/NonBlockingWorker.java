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



public interface NonBlockingWorker extends SMTComponent {

    public interface StatusChanged {
        public void isActive( boolean isActive );
    }
    
    /**
     * init specific to the message handler which is requied to be 
     * within the thread the handler will run in
     */
    public void threadedInit();
        
    /**
     * @return true if component is active
     */
    public boolean isActive();
    
    /**
     * do a unit of work .... important that work units are fast as possible as will hold up other workers
     * 
     * if no work pending then just return
     */
    public void doWorkUnit();
    
    public void registerListener( StatusChanged callback );

    public void stop();
}
