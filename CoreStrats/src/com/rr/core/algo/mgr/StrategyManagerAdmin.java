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

import com.rr.core.admin.AdminReply;
import com.rr.core.admin.AdminTableReply;
import com.rr.core.algo.strats.Strategy;
import com.rr.core.algo.strats.Strategy.StratRunState;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Instrument;

public class StrategyManagerAdmin implements StrategyManagerAdminMBean {

    private static final Logger   _log     = LoggerFactory.create( StrategyManagerAdmin.class );
    private static final String[] _columns = { "StratName", "Instruments", "runState", "tradingAllowed", "algoEventSeqNum", "lastEventInst", "lastTickId", "P&L", "details" };

    private static int _nextInstance = 1;
    
    private final StrategyManager _stratMgr;
    private final String _name;

    private static int nextId() {
        return _nextInstance++;
    }

    public StrategyManagerAdmin( StrategyManager sessionManager ) {
        _stratMgr = sessionManager;
        _name = "StrategyManagerAdmin" + nextId();
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String fullPauseStrategy( String strategyId ) {
        Strategy<?> strat = _stratMgr.getStrategy( strategyId );
        
        _log.info( "StrategyManagerAdmin.fullPauseStrategy " + strategyId );
        
        if ( strat == null ) return "Unable to find strategy " + strategyId;
        
        try {
            strat.setRunState( StratRunState.FullyPaused );

            return "fullyPauseStrategy " + strategyId + " NOW " + strat.getRunState();

        } catch( Exception e ) {
            return "Exception " + e.getMessage();
        }
    }

    @Override
    public String semiPauseStrategy( String strategyId ) {
        Strategy<?> strat = _stratMgr.getStrategy( strategyId );
        
        _log.info( "StrategyManagerAdmin.semiPauseStrategy " + strategyId );
        
        if ( strat == null ) return "Unable to find strategy " + strategyId;
        
        try {
            strat.setRunState( StratRunState.SemiPaused );

            return "semiPauseStrategy " + strategyId + " NOW " + strat.getRunState();

        } catch( Exception e ) {
            return "Exception " + e.getMessage();
        }
    }

    @Override
    public String resetCatchUpStateForStrategy( String strategyId ) {
        Strategy<?> strat = _stratMgr.getStrategy( strategyId );
        
        _log.info( "StrategyManagerAdmin.resetCatchUpStateForStrategy " + strategyId );
        
        if ( strat == null ) return "Unable to find strategy " + strategyId;
        
        try {
            strat.forceSliceReset();

            ReusableString s = TLC.instance().getString();
            strat.dumpInstDetails( s );

            return "resetCatchUpStateForStrategy " + strategyId + " NOW " + s;

        } catch( Exception e ) {
            return "Exception " + e.getMessage();
        }
    }
    
    @Override
    public String resumeStrategy( String strategyId ) {
        Strategy<?> strat = _stratMgr.getStrategy( strategyId );
        
        _log.info( "StrategyManagerAdmin.resumeStrategy " + strategyId );
        
        if ( strat == null ) return "Unable to find strategy " + strategyId;
        
        try {
            strat.setRunState( StratRunState.Running );

            return "resumeStrategy " + strategyId + " NOW " + strat.getRunState();

        } catch( Exception e ) {
            return "Exception " + e.getMessage();
        }
    }

    @Override
    public String fullPauseAll() {
        Set<Strategy<?>> strats = _stratMgr.getStrategies();
        int cnt=0;
        
        for( Strategy<?> strat : strats ) {
            _log.info( "StrategyManagerAdmin.fullPauseAll ... pausing " + strat.getComponentId() );

            if ( strat.setRunState( StratRunState.FullyPaused ) != StratRunState.FullyPaused ) {
                ++cnt;
            }
        }
        
        return "fullyPauseAll paused " + cnt + " strategies";
    }

    @Override
    public String semiPauseAll() {
        Set<Strategy<?>> strats = _stratMgr.getStrategies();
        int cnt=0;
        
        for( Strategy<?> strat : strats ) {
            _log.info( "StrategyManagerAdmin.semiPauseAll ... pausing " + strat.getComponentId() );

            if ( strat.setRunState( StratRunState.SemiPaused ) != StratRunState.SemiPaused ) {
                ++cnt;
            }
        }
        
        return "semiPauseAll paused " + cnt + " strategies";
    }

    @Override
    public String resumeAll() {
        Set<Strategy<?>> strats = _stratMgr.getStrategies();
        int cnt=0;
        
        for( Strategy<?> strat : strats ) {
            _log.info( "StrategyManagerAdmin.resumeAll ... resuming " + strat.getComponentId() );

            if ( strat.setRunState( StratRunState.Running ) != StratRunState.Running ) {
                ++cnt;
            }
        }
        
        return "resumeAll ran " + cnt + " strategies";
    }
    
    @Override
    public String listAllStrategies() {
        AdminReply reply = new AdminTableReply( _columns );
        Set<Strategy<?>> strats = _stratMgr.getStrategies();
        for( Strategy<?> strat : strats ) {
            list( reply, strat );
        }
        return reply.end();
    }

    private void list( AdminReply reply, Strategy<?> strat ) {
        if ( strat != null ) {
            reply.add( strat.getComponentId() );
            reply.add( getInstrumentStr( strat ) );
            reply.add( strat.getRunState().toString() );
            reply.add( strat.isTradingAllowed() );
            reply.add( strat.getStrategyState().getAlgoEventSeqNum() );
            reply.add( strat.getStrategyState().getLastEventInst() );
            reply.add( strat.getStrategyState().getLastTickId() );
            reply.add( strat.getStrategyState().getPnl() );
            
            ReusableString s = TLC.instance().getString();
            strat.dumpInstDetails( s );
            reply.add( s );
            TLC.instance().recycle( s );
        }
    }

    private String getInstrumentStr( Strategy<?> strat ) {
        StringBuilder secDes = new StringBuilder();
        
        for( Instrument inst : strat.getStrategyDefinition().getInsts() ) {
            if ( secDes.length() > 0 ) {
                secDes.append( "," );
            }
            secDes.append( inst.getSecurityDesc() );
        }
        
        return secDes.toString();
    }
}
