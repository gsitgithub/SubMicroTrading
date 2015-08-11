/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mgr;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.admin.AdminAgent;
import com.rr.core.algo.strats.Algo;
import com.rr.core.algo.strats.Strategy;
import com.rr.core.component.SMTStartContext;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.SMTRuntimeException;

/**
 * Manages strategies, expects Algo instances to propogate control invocations
 *
 * @author Richard Rose
 */
public class StrategyManagerImpl implements StrategyManager {

    private static final Logger       _log = LoggerFactory.create( StrategyManagerImpl.class );
    
    private final String                                  _id;
    private final ConcurrentHashMap<String,Algo<?>>       _algoMap        = new ConcurrentHashMap<String,Algo<?>>();
    private final ConcurrentHashMap<String,Strategy<?>>   _strategyMap    = new ConcurrentHashMap<String,Strategy<?>>();
    private final Set<Strategy<?>>                        _strategies     = Collections.synchronizedSet( new LinkedHashSet<Strategy<?>>() );
    
    public StrategyManagerImpl( String id ) {
        _id = id;
        StrategyManagerAdmin sma = new StrategyManagerAdmin( this );
        AdminAgent.register( sma );
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void startWork() {
        _log.info( "StrategyManagerImpl.startWork() start" );
        
        for( Algo<?> algo : _algoMap.values() ) {
            _log.info( "StrategyManagerImpl.startWork() start algo " + algo.getComponentId() );

            algo.startWork();
        }

        _log.info( "StrategyManagerImpl.startWork() end" );
    }

    @Override
    public void stopWork() {
        _log.info( "StrategyManagerImpl.stopWork() start" );
        
        for( Algo<?> algo : _algoMap.values() ) {
            _log.info( "StrategyManagerImpl.stopWork() stop algo " + algo.getComponentId() );

            algo.stopWork();
        }

        _log.info( "StrategyManagerImpl.stopWork() end" );
    }

    @Override
    public void init( SMTStartContext ctx ) {
        _log.info( "StrategyManagerImpl.init() start" );
        
        for( Algo<?> algo : _algoMap.values() ) {
            _log.info( "StrategyManagerImpl.init() init algo " + algo.getComponentId() );

            algo.init( ctx );
        }

        _log.info( "StrategyManagerImpl.init() end" );
    }

    @Override
    public void prepare() {
        _log.info( "StrategyManagerImpl.prepare() start" );
        
        for( Algo<?> algo : _algoMap.values() ) {
            _log.info( "StrategyManagerImpl.prepare() prepare algo " + algo.getComponentId() );

            algo.prepare();
        }

        _log.info( "StrategyManagerImpl.prepare() end" );
    }

    @Override
    public Algo<?> getAlgo( String algoId ) {
        return _algoMap.get( algoId );
    }

    @Override
    public Strategy<?> getStrategy( String strategyId ) {
        return _strategyMap.get( strategyId );
    }

    @Override
    public Set<Strategy<?>> getStrategies() {
        return _strategies;
    }

    @Override
    public void registerAlgo( Algo<?> algo ) {
        if ( _algoMap.putIfAbsent( algo.getComponentId(), algo ) != null ) {
            throw new SMTRuntimeException( "Duplicate registration of algo " + algo.getComponentId() );
        }
    }

    @Override
    public void registerStrategy( Strategy<?> strat ) {
        if ( _strategyMap.putIfAbsent( strat.getComponentId(), strat ) != null ) {
            throw new SMTRuntimeException( "Duplicate registration of strategy " + strat.getComponentId() );
        }
        _strategies.add( strat );        
    }
}
