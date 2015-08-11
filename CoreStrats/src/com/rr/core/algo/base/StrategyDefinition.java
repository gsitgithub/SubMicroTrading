/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.base;

import java.util.List;
import java.util.Map;

import com.rr.core.algo.strats.Algo;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;


public interface StrategyDefinition {

    public String getStrategyId();

    public String getAlgoId();

    public String getRequestedPipeLineId();

    public Class<? extends Algo<? extends Book>> getAlgoClass();

    public List<Instrument> getInsts();

    public Map<String, String> getProps();

}
