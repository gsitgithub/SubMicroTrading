/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mgr;

import java.util.Set;

import com.rr.core.algo.strats.Algo;
import com.rr.core.algo.strats.Strategy;
import com.rr.core.component.SMTControllableComponent;


public interface StrategyManager extends SMTControllableComponent {

    /**
     * @return the identified Algo or null if not known 
     */
    public Algo<?> getAlgo( String algoId );

    /**
     * @return the identified strategy or null if not known 
     */
    public Strategy<?> getStrategy( String strategyId );

    /**
     * registers the algo against its componentId
     */
    public void registerAlgo( Algo<?> algo );

    /**
     * register a running instance of an algo ... registration against its componentId
     */
    public void registerStrategy( Strategy<?> strat );

    /**
     * @return THE set of registered strategies (NOT a copy)
     */
    public Set<Strategy<?>> getStrategies();
}
