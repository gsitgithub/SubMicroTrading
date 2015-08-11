/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.strats;

import com.rr.core.component.SMTControllableComponent;
import com.rr.core.model.Book;

/**
 * Algo must have a single constructor taking componentId as parameter for instantiation by reflection
 * 
 * Running instances of the algo (aka Strategies) must be kept track of
 * 
 * Allows all running instances of an algo to be controlled via the Algo instance
 */

public interface Algo<T extends Book> extends SMTControllableComponent {

    public Strategy<T>[] getStrategyInstances();

    /**
     * register running strategy instance of this algo
     *  
     * @param strat
     */
    public void registerStrategy( Strategy<?> strat );

    /**
     * set the class that strategy instances will use
     */
    public void setStrategyClass( Class<? extends Strategy<? extends Book>> stratClass );

    /**
     * 
     * @return
     */
    public Class<? extends Strategy<? extends Book>> getStrategyClass();
}
