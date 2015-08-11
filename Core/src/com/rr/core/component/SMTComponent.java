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
 * Represents a component as defined in config
 * 
 * Initially these components will be created using custom loaders or refelection
 * Later may move to Spring ... reason for not doing so now is simplly time.
 * Spring is great when it works and terrible when it doesnt and is a general time sink  
 * 
 * SMTComponent must have a constructor with first element a string for the componentId
 * 
 * The simplest component contract 
 */

public interface SMTComponent {

    /**
     * @return unique identifier of component as used in references to components in component wiring
     */
    public String getComponentId();
}
