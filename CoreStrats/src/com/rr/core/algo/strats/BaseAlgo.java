/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.strats;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.algo.strats.ordstate.StratOrdOpenState;
import com.rr.core.algo.strats.ordstate.StratOrdPendingCancelReplaceState;
import com.rr.core.algo.strats.ordstate.StratOrdPendingCancelState;
import com.rr.core.algo.strats.ordstate.StratOrdPendingNewState;
import com.rr.core.algo.strats.ordstate.StratOrdTerminalState;
import com.rr.core.algo.strats.ordstate.StratOrderState;
import com.rr.core.component.SMTStartContext;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.thread.NonBlockingWorker.StatusChanged;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.internal.events.interfaces.StrategyState;

/**
 * @WARNING each instance of a strategy must only have one thread invoking the doWork method else recycling errors will occur
 * 
 * Algo must keep track of its strategies
 * Propogates control invocations to its strategy instances
 * 
 * @TODO keep algo level aggregate stats against all strat instances
 */

public abstract class BaseAlgo<T extends Book> implements Algo<T> {

    static final Logger _log = LoggerFactory.create( BaseAlgo.class );

    private static final String CR = "\n\n";
    
    private static final String ALGO_HDR = CR + "======================================================================================" + CR;
    private static final String ALGO_TRL = CR + "______________________________________________________________________________________" + CR;

    private static final String STRAT_HDR = CR + "******************************************************************" + CR;
    private static final String STRAT_TRL = CR + "------------------------------------------------------------------" + CR;

    public static final class StratOrdStates {
        public final StratOrderState  _pendingNewState;
        public final StratOrderState  _terminalState;
        public final StratOrderState  _openState;
        public final StratOrderState  _pendingCancel;
        public final StratOrderState  _pendingReplace;
        
        public StratOrdStates( StratOrderState pendingNewState, 
                               StratOrderState terminalState, 
                               StratOrderState openState, 
                               StratOrderState pendingCancel,
                               StratOrderState pendingReplace ) {
            super();
            _pendingNewState = pendingNewState;
            _terminalState = terminalState;
            _openState = openState;
            _pendingCancel = pendingCancel;
            _pendingReplace = pendingReplace;
        }
    }
    
    protected class StratStatusWatcher implements StatusChanged {
        
        private Strategy<?> _strat;

        public StratStatusWatcher( Strategy<?> strat ) {
            _strat = strat;
        }

        @Override
        public void isActive( boolean isActive ) {
            _log.info( "Algo " + getComponentId() + " strategy " + _strat.getComponentId() + " " + ((isActive) ?  "ACTIVE" : "DOWN") );
        }
    }
    
    private final String                                        _id;

    private final ConcurrentHashMap<String,Strategy<?>>         _strategyMap    = new ConcurrentHashMap<String,Strategy<?>>();

    private       Class<? extends Strategy<? extends Book>>     _stratClass;
    private final LinkedList<Strategy<?>>                       _linkedInstances = new LinkedList<Strategy<?>>();
    private       Strategy<?>[]                                 _instances;
    
    private final StratOrdStates                                _stratOrdStates; // state machine for exec processing
    
    
    /**
     * @param id component id
     * @param instIdList comma delimited list of books to subscribe too
     */
    public BaseAlgo( String id ) {
        _id = id;
        
        _stratOrdStates = new StratOrdStates( createPendingNewState(),
                                              createTerminalState(),
                                              createOpenState(),
                                              createPendingCancelState(),
                                              createPendingCancelReplaceState() );
    }

    @Override
    public void startWork() {
        logAlgoSectionHdr( "startWork" );
        
        for( Strategy<?> strat : _strategyMap.values() ) {
            logStratSectionHdr( "startWork", strat );

            strat.startWork();

            logStratSectionTrailer( "startWork", strat );
        }

        logAlgoSectionTrailer( "startWork" );
    }

    @Override
    public void stopWork() {
        logAlgoSectionHdr( "stopWork" );
        
        for( Strategy<?> strat : _strategyMap.values() ) {
            logStratSectionHdr( "stopWork", strat );

            strat.stop();

            logStratSectionTrailer( "stopWork", strat );
        }

        logAlgoSectionTrailer( "stopWork" );
    }

    @Override
    public void init( SMTStartContext ctx ) {
        logAlgoSectionHdr( "init" );
        
        for( Strategy<?> strat : _strategyMap.values() ) {
            logStratSectionHdr( "init", strat );

            strat.registerListener( new StratStatusWatcher( strat ) );
            strat.setStratOrdStates( _stratOrdStates );
            strat.init( ctx );

            logStratSectionTrailer( "init", strat );
        }

        logAlgoSectionTrailer( "init" );
    }

    @Override
    public void prepare() {
        logAlgoSectionHdr( "prepare" );
        
        for( Strategy<?> strat : _strategyMap.values() ) {
            logStratSectionHdr( "prepare", strat );

            strat.prepare();

            logStratSectionTrailer( "prepare", strat );
        }

        logAlgoSectionTrailer( "prepare" );
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Strategy<T>[] getStrategyInstances() {
        return (Strategy<T>[]) _instances;
    }

    @Override
    public synchronized void registerStrategy( Strategy<?> strat ) {
        if ( _strategyMap.putIfAbsent( strat.getComponentId(), strat ) != null ) {
            throw new SMTRuntimeException( "Duplicate registration of strategy " + strat.getComponentId() + ", in Algo " + _id );
        }
        
        _linkedInstances.add( strat );
        
        _instances = _linkedInstances.toArray( new Strategy<?>[_linkedInstances.size()] ); 
    }

    @Override
    public String toString() {

        double minPL = Double.MAX_VALUE;
        double maxPL = Double.MIN_NORMAL;
        double totalPNL = 0;
        
        long totalEvents = 0;
        
        for( Strategy<?> strat : _strategyMap.values() ) {
            StrategyState state = strat.getStrategyState();
            
            double pnl = state.getPnl();
            
            totalPNL += pnl;
            totalEvents += state.getAlgoEventSeqNum();
            
            if ( pnl < minPL ) minPL = pnl;
            if ( pnl > maxPL ) maxPL = pnl;
        }
        
        return "stratCount=" + _strategyMap.size() + ", totPnl=" + totalPNL + ", minPnL=" + minPL + ", maxPnL=" + maxPL + ", totSlices=" + totalEvents;
    }

    @Override
    public void setStrategyClass( Class<? extends Strategy<? extends Book>> stratClass ) {
        _stratClass = stratClass;
    }

    @Override
    public Class<? extends Strategy<? extends Book>> getStrategyClass() {
        return _stratClass;
    }

    protected StratOrderState createPendingNewState() {
        return new StratOrdPendingNewState();
    }

    protected StratOrderState createOpenState() {
        return new StratOrdOpenState();
    }

    protected StratOrderState createTerminalState() {
        return new StratOrdTerminalState();
    }

    protected StratOrderState createPendingCancelState() {
        return new StratOrdPendingCancelState();
    }

    protected StratOrderState createPendingCancelReplaceState() {
        return new StratOrdPendingCancelReplaceState();
    }

    private void logAlgoSectionHdr( String section ) {
        _log.info( ALGO_HDR + "BaseAlgo." + section + "() algo=" + _id + " begin" + CR );
    }

    private void logAlgoSectionTrailer( String section ) {
        _log.info( CR + "BaseAlgo." + section + "() algo=" + _id + " end" + ALGO_TRL );
    }

    private void logStratSectionHdr( String section, Strategy<?> strat ) {
        _log.info( STRAT_HDR + "BaseAlgo." + section + "() algo=" + _id + ", strat=" + strat.getComponentId()+ " begin" + CR );
    }

    private void logStratSectionTrailer( String section, Strategy<?> strat ) {
        _log.info( CR + "BaseAlgo." + section + "() algo=" + _id + ", strat=" + strat.getComponentId() + " end" + STRAT_TRL );
    }
}
