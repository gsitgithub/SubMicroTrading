/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.thread;

import com.rr.core.component.SMTControllableComponent;

public interface ExecutableElement extends SMTControllableComponent {

    /**
     * invoked within the thread of control from the owning ControlThread
     * 
     * occurs AFTER GC so be aware of object creation
     */
    public void threadedInit();
    
    /**
     * run execution unit (assumes element is ready)
     * 
     * if execution unit is no longer ready it should throw an exception to force control thread to use ready before next execute invoke
     * @throws Exception 
     */
    public void execute() throws Exception;
    
    /**
     * handle caught exception thrown during execute, MUST be called on exception before execute is invoked again
     *  
     * @param ex
     */
    public void handleExecutionException( Exception ex );

    /**
     * @return true if executor has work it can do
     */
    public boolean checkReady();
    
    /**
     * invoked when element not ready (as opposed to execute)
     * 
     * @NOTE must never wait/sleep
     */
    public void notReady();

    /**
     * stop the executable element freeing any resources, should only be invoked once
     */
    public void stop();

    public String info();
}
