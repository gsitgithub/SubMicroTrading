/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.component;

import com.rr.core.utils.SMTException;




/**
 * helper component used for more complex glueing of components
 * 
 * loader responsible for instantiating one 
 */
public interface SMTSingleComponentLoader extends SMTComponentLoader {

    /**
     * create instances for 1 or more components
     * @param id 
     *  
     * @param props - application properties configuration used by loader
     * @return 1 or more component instances
     * @throws SMTException if exception creating component 
     */
    public SMTComponent create( String id ) throws SMTException;
}
