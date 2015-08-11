/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import com.rr.core.lang.ZString;
import com.rr.core.model.InstrumentLocator;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.model.generated.internal.events.impl.SecurityStatusImpl;

public interface InstrumentStore extends InstrumentLocator {

    /**
     * @return if store can handle intraday addition in threadsafe manner
     */
    public boolean allowIntradayAddition();
    
    /**
     * add/replace the security in the store
     * 
     * method takes ownership of the SecurityDefination,so should store/recycle as appropriate
     * @param rec 
     */
    public void add( SecurityDefinitionImpl def, ZString rec );

    /**
     * remove the security in the store
     * 
     * method takes ownership of the SecurityDefination,so should store/recycle as appropriate
     * @param rec 
     */
    public void remove( SecurityDefinitionImpl def, ZString rec );

    /**
     * method takes ownership of the SecurityStatusImpl,so should store/recycle as appropriate
     */
    public boolean updateStatus( SecurityStatusImpl status );
}
