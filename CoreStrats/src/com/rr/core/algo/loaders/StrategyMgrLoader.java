/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rr.core.algo.base.StrategyDefinition;
import com.rr.core.algo.mgr.StrategyDefFileParser;
import com.rr.core.algo.mgr.StrategyManager;
import com.rr.core.algo.mgr.StrategyManagerImpl;
import com.rr.core.algo.strats.Algo;
import com.rr.core.algo.strats.Strategy;
import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.session.Session;
import com.rr.core.session.ThrottleWithExceptions;
import com.rr.core.session.Throttler;
import com.rr.core.thread.BaseNonBlockingWorkerMultiplexor;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.SMTException;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.hub.AsyncLogSession;
import com.rr.om.router.OrderRouter;

/**
 * StrategyMgrLoader
 * 
 * creates the StrategyManager
 * creates the Algo "singleton" instance (each Algo has 1 instance which all strategys of the Algo refer too)
 * creates the Strategy instances, where a strategy is a running instance of an Algo for specified instruments and specified parameters
 *
 * @author Richard Rose
 */

public class StrategyMgrLoader implements SMTSingleComponentLoader {
    
    private static final Logger _log = LoggerFactory.create( StrategyMgrLoader.class );

    private Map<String,String> _algoClassNames;
    private Map<String,String> _stratClassNames;
    private Session            _hubSession = new AsyncLogSession( "HubSession" );
    private OrderRouter        _exchangeRouter;
    private String             _strategyDefFile;
    private int                _bookLevels = 1;
    private InstrumentLocator  _instrumentLocator;
    private int                _throttleRateMsgsPerSecond = 0;
    private SMTComponent[]     _multiplexors = new SMTComponent[0];
    private ZString            _account = new ViewString("DEF");
    
    private boolean            _tradingAllowed = true;
    private boolean            _trace = false;
    private int                _maxOrderQty = 1;
    private int                _maxSlices = Integer.MAX_VALUE;
    private double             _pnlCutoffThreshold = Constants.UNSET_DOUBLE;

    private Map<String, BaseNonBlockingWorkerMultiplexor> _pipeToStratMultiplexor = new HashMap<String,BaseNonBlockingWorkerMultiplexor>();

    private Map<BaseNonBlockingWorkerMultiplexor, Throttler> _throttlers = new HashMap<BaseNonBlockingWorkerMultiplexor, Throttler>();

    @Override
    public SMTComponent create( String id ) throws SMTException {
        
        List<StrategyDefinition> strats = parseStartegyFile();

        StrategyManager mgr = new StrategyManagerImpl( id );

        makeStratMultiplexorMap();
        
        for( StrategyDefinition def : strats ) {
            createStrategy( mgr, def );
        }
        
        return mgr;
    }

    private void makeStratMultiplexorMap() {
        if ( _multiplexors.length == 0 ) {
            throw new SMTRuntimeException( "Missing property multiplexors in StrategyMgrLoader" );
        }

        for( SMTComponent c : _multiplexors ) {
            if ( c instanceof BaseNonBlockingWorkerMultiplexor ) {
                BaseNonBlockingWorkerMultiplexor m = (BaseNonBlockingWorkerMultiplexor)c;
                
                List<String> pipes = m.getPipeLineIds();
                
                for( String pipe : pipes ) {
                    BaseNonBlockingWorkerMultiplexor existing = _pipeToStratMultiplexor.get( pipe );
                    
                    if ( existing != null ) {
                        throw new SMTRuntimeException( "StrategyMgrLoader, duplicate pipeId on multiplexor " + c.getComponentId() + 
                                                       " and " + existing.getComponentId() );
                    }
                    
                    _pipeToStratMultiplexor.put( pipe, m );
                }
                
            }  else{
                throw new SMTRuntimeException( c.getComponentId() + " - Expected strategy multiplexor to be instanceof BaseNonBlockingWorkerMultiplexor not " + c.getClass().getSimpleName() );
            }
        }
    }

    private void createStrategy( StrategyManager mgr, StrategyDefinition def ) {

        String stratId = def.getStrategyId();
        
        Algo<?> algo = getAlgo( mgr, def.getAlgoId(), def.getAlgoClass() );

        if ( mgr.getStrategy( _strategyDefFile ) != null ) {
            throw new SMTRuntimeException( "StrategyMgrLoader duplicate strategy id " + def.getStrategyId() );
        }
        
        Object[]   argVals    = { stratId };
        Class<?>[] argClasses = { String.class };
        
        Strategy<?> strat = ReflectUtils.create( algo.getStrategyClass(), argClasses, argVals );
        
        strat.setAlgo( algo );
        // set default properties from loader
        strat.setOrderRouter( _exchangeRouter );
        strat.setAccount( _account );
        strat.setHubSession( _hubSession );
        strat.setBookLevels( _bookLevels );

        strat.setTrace( _trace );
        strat.setTradingAllowed( _tradingAllowed );
        strat.setMaxOrderQty( _maxOrderQty );
        strat.setMaxSlices( _maxSlices );
        
        if ( _pnlCutoffThreshold != Constants.UNSET_DOUBLE ) strat.setPnlCutoffThreshold( _pnlCutoffThreshold );
        
        // now reflect the values from strategy config
        strat.setStrategyDefinition( def );

        algo.registerStrategy( strat );
        mgr.registerStrategy( strat );
        
        String stratPipe = def.getRequestedPipeLineId();
        
        BaseNonBlockingWorkerMultiplexor m = _pipeToStratMultiplexor.get( stratPipe );
        
        if ( m == null ) {
            throw new SMTRuntimeException( "createStrategy on " + strat.getComponentId() + " failed as its requestedPipe " + stratPipe + 
                                           " doesnt have a matching multiplexor" );
        }
        
        if ( _throttleRateMsgsPerSecond > 0 ) {
            // the throttler is NOT threadsafe so must be aligned to the multiplexor

            Throttler t = getThrottler( m );
            
            strat.setThrottler( t );
        }

        _log.info( "createStrategy " + strat.getComponentId() + " assigned to pipe " + stratPipe + " against multiplexor " + m.getComponentId() );
        
        m.addWorker( strat );
    }

    private Throttler getThrottler( BaseNonBlockingWorkerMultiplexor m ) {
        Throttler t = _throttlers .get( m );
        
        if ( t == null ) {
            t = new ThrottleWithExceptions();
            t.setThrottleNoMsgs( _throttleRateMsgsPerSecond );
            t.setThrottleTimeIntervalMS( 1000 );
            
            _throttlers.put( m, t );
        }
        
        return t;
    }


    private Algo<?> getAlgo( StrategyManager mgr, String algoId, Class<? extends Algo<? extends Book>> algoClass ) {
        Algo<?> algo = mgr.getAlgo( algoId );
        
        if ( algo == null ) {
            algo = createAlgo( algoId, algoClass );
            
            
            mgr.registerAlgo( algo );
        }
        
        return algo;
    }

    private Algo<?> createAlgo( String algoId, Class<? extends Algo<? extends Book>> algoClass ) {
        Object[]   argVals    = { algoId };
        Class<?>[] argClasses = { String.class };
        
        Algo<?> algo = ReflectUtils.create( algoClass, argClasses, argVals );
        
        String stratClassName = _stratClassNames.get( algoId );
        
        if ( stratClassName == null ) {
            throw new SMTRuntimeException( "StrategyMgrLoader algoId " + algoId + " missing strategy className entry in map.stratClass" );
        }
        
        try {
            @SuppressWarnings( "unchecked" )
            Class<? extends Strategy<? extends Book>> stratClass = (Class<? extends Strategy<? extends Book>>) Class.forName( stratClassName );
            
            algo.setStrategyClass( stratClass );
            
        } catch( ClassNotFoundException e ) {
            throw new SMTRuntimeException( "StrategyMgrLoader algoId " + algoId + " unable to get class for className " + stratClassName );
        }
        
        return algo;
    }

    private List<StrategyDefinition> parseStartegyFile() {
        StrategyDefFileParser parser = new StrategyDefFileParser( _instrumentLocator, _algoClassNames );
        
        List<StrategyDefinition> strats = new ArrayList<StrategyDefinition>();
        
        if ( _strategyDefFile == null ) {
            throw new SMTRuntimeException( "StrategyMgrLoader missing property [strategyDefFile]" );
        }
        
        parser.parseStrategyDefFile( strats, _strategyDefFile );

        _log.info( "StrategyMgrLoader strategy file " + _strategyDefFile + " loaded with " + strats.size() + " entries" );
        
        return strats;
    }
}
