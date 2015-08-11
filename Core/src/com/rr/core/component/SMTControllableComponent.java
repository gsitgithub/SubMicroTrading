/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.component;


/**
 * Represents a controllable component as defined in config
 *
 * can be started/ stopped 
 * has initialise method
 * 
 * SMTComponent must have a constructor with first element a string for the componentId
 */

public interface SMTControllableComponent extends SMTInitialisableComponent {

    /**
     * startWork - invoked by bootstrat after all components have had following invoked
     *             legacy code uses start() method in some cases that needs to be moved to prepare in others startWork
     *             this method is named differently to make it simpler to identify legacy code and move it
     *  
     *  init - used to validate properties, makes no assumptions on state of other components
     *  
     *  prepare - invoked after all components have been instantiated and had init invoked
     *            used for second chance to get links to referenced components
     *            assumes no other components have been prepared
     *            
     *  method should return and not simply grab the main thread.
     */
    public void startWork();
    
    public void stopWork();
}
