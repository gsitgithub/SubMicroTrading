/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.thread;

import java.util.List;


public interface PipeLineable {

    /**
     * @return list of the pipeLineIds or null if not assigned
     */
    public List<String> getPipeLineIds();
    
    /**
     * @param pipeLineId
     * @return true if the the supplied pipeLineId is associated
     */
    public boolean hasPipeLineId( String pipeLineId );
}
