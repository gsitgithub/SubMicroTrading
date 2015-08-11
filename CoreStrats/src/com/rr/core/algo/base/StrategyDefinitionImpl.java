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


public class StrategyDefinitionImpl implements StrategyDefinition {

    private final String                                _strategyId;
    private final String                                _algoId;
    private final String                                _requestedPipeLineId;
    private final Class<? extends Algo<? extends Book>> _algoClass;
    private final List<Instrument>                      _insts;
    private final Map<String, String>                   _props;

    public StrategyDefinitionImpl( String strategyId, 
                                   String algoId, 
                                   String pipeLineId, 
                                   Class<? extends Algo<?>> aClass, 
                                   List<Instrument> insts, 
                                   Map<String, String> props ) {

        _strategyId = strategyId;
        _algoId = algoId;
        _requestedPipeLineId = pipeLineId;
        _algoClass = aClass;
        _insts = insts;
        _props = props;
    }

    @Override
    public String getStrategyId() {
        return _strategyId;
    }

    @Override
    public String getAlgoId() {
        return _algoId;
    }

    @Override
    public String getRequestedPipeLineId() {
        return _requestedPipeLineId;
    }

    @Override
    public Class<? extends Algo<? extends Book>> getAlgoClass() {
        return _algoClass;
    }

    @Override
    public List<Instrument> getInsts() {
        return _insts;
    }

    @Override
    public Map<String, String> getProps() {
        return _props;
    }
}
