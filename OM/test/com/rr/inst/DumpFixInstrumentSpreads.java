/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.model.LegInstrument;
import com.rr.core.utils.FileException;
import com.rr.inst.BaseInstrumentSecDefStore.Indexes;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.om.exchange.ExchangeManager;


public class DumpFixInstrumentSpreads extends BaseTestCase {

    private static final Logger       _log = LoggerFactory.create( DumpFixInstrumentSpreads.class );
    private int _calenderSpread;
    private int _otherSpread;

    public void testLoadCMECheckSpreads() throws FileException {
        Exchange cme = ExchangeManager.instance().getByREC( new ViewString("2") );
        SingleExchangeInstrumentStore instStore = new SingleExchangeInstrumentStore( cme, 1000 );
        FixInstrumentLoader loader = new FixInstrumentLoader( instStore );
        loader.loadFromFile( "../../data/secdef.dat", new ViewString("2") );
//        loader.loadFromFile( "data/cme/algo_secdef.dat", new ViewString("2") );

        Indexes ind = instStore.getExchangeMap( null, 0 );
        
        Collection<InstrumentSecurityDefWrapper> insts = ind.getSymbolIndex().values();

        int spreadCnt = 1;
        ReusableString msg = new ReusableString();

        /**
         * for each applId #spreads
         */
        
        Map<Integer,Integer> applIdToSpreadMap = new HashMap<Integer,Integer>();
        Map<Integer,Set<Integer>> spreadSegmentIdToDepends = new HashMap<Integer,Set<Integer>>();
        Map<Integer,Set<Instrument>> segmentInst = new HashMap<Integer,Set<Instrument>>();
        Map<Instrument,Integer> instSpreadCount = new HashMap<Instrument, Integer>();
        
        boolean logAnyway = true;

        int bothLegsBuy = 0;
        int bothLegsSell = 0;
        int leg1Buyleg2Sell = 0;
        int leg1Sellleg2Buy = 0;
        
        for( InstrumentSecurityDefWrapper inst : insts ) {
            if ( inst.getNumLegs() == 2 ) {
                msg.copy( "Spread #" ).append( spreadCnt ).append( " " ).append( inst.getSecurityDesc() ).append( ", segment=" ).append( inst.getIntSegment() ).append( "     " );
                
                incApplIdMap( applIdToSpreadMap, inst.getIntSegment() );

                addInstrumentToSegment( segmentInst, inst.getIntSegment(), inst );
                
                boolean legsPresent = true;
                boolean sameApplId = true;

                if ( inst.getLeg( 0 ).getLegSide().getIsBuySide() && inst.getLeg( 1 ).getLegSide().getIsBuySide() ) {
                    ++bothLegsBuy;
                }
                
                if ( (inst.getLeg( 0 ).getLegSide().getIsBuySide() == false) && (false == inst.getLeg( 1 ).getLegSide().getIsBuySide()) ) {
                    ++bothLegsSell;
                }
                
                if ( inst.getLeg( 0 ).getLegSide().getIsBuySide() == true && inst.getLeg( 1 ).getLegSide().getIsBuySide() == false ) {
                    ++leg1Buyleg2Sell;
                }
                
                if ( inst.getLeg( 0 ).getLegSide().getIsBuySide() == false && inst.getLeg( 1 ).getLegSide().getIsBuySide() == true ) {
                    ++leg1Sellleg2Buy;
                }
                
                for ( int i=0 ; i < inst.getNumLegs() ; i++ ) {
                    LegInstrument leg = inst.getLeg( i );

                    InstrumentSecurityDefWrapper legInst = (InstrumentSecurityDefWrapper)leg.getInstrument();

                    addInstrumentSpreadCount( instSpreadCount, legInst );
 
                    SecurityDefinitionImpl legSecDef = legInst.getSecDef();

                    addInstrumentToSegment( segmentInst, inst.getIntSegment(), legInst );
                    addSpreadSegmentSet( spreadSegmentIdToDepends, inst.getIntSegment(), legInst.getIntSegment() );

                    if ( legSecDef == null ) {
                        msg.append( " [leg" ).append( i+1 )
                        .append( " id=" ).append( legInst.getLongSymbol() )
                        .append( ", side=" ).append( leg.getLegSide().toString() )
                        .append( ", legInstLoaded=" ).append( 'N' )
                        .append( ", segment=" ).append( legInst.getIntSegment() )
                        .append(  "] " );
                        
                        legsPresent = false;
                        
                    } else {
                        ZString secDesc = legInst.getSecurityDesc();
                        if ( secDesc.length() == 0 ) secDesc = legSecDef.getSecurityDesc();
    
                        msg.append( " [leg" ).append( i+1 )
                           .append( " secDes=" ).append( secDesc )
                           .append( ", side=" ).append( leg.getLegSide().toString() )
                           .append( ", legInstLoaded=" ).append( 'Y' )
                           .append( ", segment=" ).append( legInst.getIntSegment() )
                           .append(  "] " );
                    }
                    
                    if ( legInst.getIntSegment() != inst.getIntSegment() ) {
                        sameApplId = false;
                    }
                }

                msg.append( ", priceRation=" ).append( inst.getSecurityDefinition().getPriceRatio() );
                
                if ( sameApplId == false ) {
                    msg.append( "  MULTI_SEGMENT" );
                }
                
                if ( logAnyway || legsPresent ) {
                    ++spreadCnt;

if ( inst.getLeg( 0 ).getLegSide().getIsBuySide() == true && inst.getLeg( 1 ).getLegSide().getIsBuySide() == false ) {
    
    if ( inst.getSecurityDefinition().getPriceRatio() == Constants.UNSET_DOUBLE ) {

String leg1SecDesc = inst.getLegSecurityDesc( 0, new ReusableString() ).toString(); 
String leg2SecDesc = inst.getLegSecurityDesc( 1, new ReusableString() ).toString(); 

String part1 = leg1SecDesc.substring( 0, leg1SecDesc.length() - 2 );
String part2 = leg2SecDesc.substring( 0, leg2SecDesc.length() - 2 );

        if ( part1.equals( part2 ) ) {
            _log.info( "CALENDAR SPREAD : " + msg );
            ++_calenderSpread;
        } else {
            _log.info( "OTHER    SPREAD : " + msg );
            ++_otherSpread;
        }
    }
}
                    
                }
            }
        }
/*        
        for( Map.Entry<Integer, Integer> e : applIdToSpreadMap.entrySet() ) {
            _log.info( "ApplId " + e.getKey() + " = " + e.getValue() + " spreads" );
        }

        for( Map.Entry<Integer, Set<Integer>> e : spreadSegmentIdToDepends.entrySet() ) {
            msg.copy( "Spread segment Id " + e.getKey() + " depends" );
            Set<Integer> s = e.getValue();
            boolean first = true;
            int totalInsts = segmentInst.get( e.getKey() ).size();
            for( Integer i : s ) {
                if ( first ) {
                    first = false;
                } else {
                    msg.append( "," );
                }
                int segSize = segmentInst.get( i ).size();
                msg.append( " " + i + " (" + segSize + ")");
                if ( i != e.getKey() ) {
                    totalInsts += segSize;
                }
            }
            msg.append( ", totalInsts=" + totalInsts );
            _log.info( msg );
        }

        int threshold = 20;
        
        for( Map.Entry<Instrument, Integer> e : instSpreadCount.entrySet() ) {
            if ( e.getValue().intValue() > threshold ) {
                _log.info( "Inst " + e.getKey().getSecurityDesc() + " on segment " + e.getKey().getIntSegment() + ", in " + e.getValue() + " spreads" );
            }
        }
  */      
        _log.info( "LegsBothBuy #" + bothLegsBuy + ", LegsBothSell=" + bothLegsSell + ", leg1BuyLeg2Sell=" + leg1Buyleg2Sell + ", leg1SellLeg2Buy=" + leg1Sellleg2Buy +
                   ", calSpread=" + _calenderSpread  + ", otherSpread=" + _otherSpread );
    }

    @SuppressWarnings( "boxing" )
    private void addInstrumentSpreadCount( Map<Instrument, Integer> instSpreadCount, InstrumentSecurityDefWrapper inst ) {
        Integer old = instSpreadCount.get( inst );
        if ( old == null ) {
            instSpreadCount.put( inst, 1 );
        } else {
            instSpreadCount.put( inst, old.intValue() + 1 );
        }
    }

    @SuppressWarnings( "boxing" )
    private void addInstrumentToSegment( Map<Integer, Set<Instrument>> segmentInst, int instSegment, Instrument inst ) {
        Set<Instrument> set = segmentInst.get( instSegment );
        if ( set == null ) {
            set = new HashSet<Instrument>();
            segmentInst.put( instSegment, set );
        }
        set.add( inst );
    }

    @SuppressWarnings( "boxing" )
    private void incApplIdMap( Map<Integer, Integer> applIdToSpreadMap, int intSegment ) {
        Integer old = applIdToSpreadMap.get( intSegment );
        if ( old == null ) {
            applIdToSpreadMap.put( intSegment, 1 );
        } else {
            applIdToSpreadMap.put( intSegment, old.intValue() + 1 );
        }
    }

    @SuppressWarnings( "boxing" )
    private void addSpreadSegmentSet( Map<Integer, Set<Integer>> spreadSegmentIdToDepends, int instSegment, int legSegment ) {
        Set<Integer> set = spreadSegmentIdToDepends.get( instSegment );
        if ( set == null ) {
            set = new HashSet<Integer>();
            spreadSegmentIdToDepends.put( instSegment, set );
        }
        set.add( legSegment );
    }
}
