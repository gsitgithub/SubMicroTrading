/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.base;

import java.util.HashSet;
import java.util.Set;

import com.rr.core.properties.PropertyTags;
import com.rr.om.main.OMProps;

/**
 * only the final tag of a property is validated, eg property xxx.yyy.ZZZ, only ZZZ must be a member of Tags
 * this is because the set of complete property names is dynamic and can vary across instances
 * 
 * Note Tags ARE inherited from base class from validation perspective
 * 
 * point of this class is to avoid incorrect program behaviour because properties are spelled incorrectly
 */

public class StratProps extends OMProps {
    
    public enum Tags implements PropertyTags.Tag {
        account,
        addInstrumentSessions,
        addSnapshotSessions,
        addIncrementalSessions,
        algoClassNames,
        algoMgr,
        allowedChannels,
        arbThresh,
        bookSrcMgr,
        enableEventPojoLogging,
        hubSession,
        maxSlices,
        maxInstSpreadAssociation,
        maxStrategyInstances,
        mdSessionBuilder,
        multiplexors,
        pipeIdList,
        pipeToChannels,
        pnlCutoffThreshold,
        reconciler,
        stratClassNames,
        strategyDefFile,
        templatesFile,
        throttleRateMsgsPerSecond,
        tradingAllowed,
        maxProdGrpArraySize,
        
        // arb strats
        excludeNonCalSpreads,
        allowBuyLeg1SellLeg2,
        allowSellLeg1BuyLeg2;
    }

    private static final Set<String> _set = new HashSet<String>();

    static {
        for ( Tags p : Tags.values() ) {
             _set.add( p.toString().toLowerCase() );
        }
    }

    private static StratProps _instance = new StratProps();
    
    public static StratProps instance() { return _instance; }
    
    @Override
    public String getSetName() {
        return "StratProps";
    }

    @Override
    public boolean isValidTag( String tag ) {
        if ( tag == null ) return false;
        
        if ( _set.contains( tag.toLowerCase() )  ) {
            return true;
        }
            
        return super.isValidTag( tag );
    }
    
    @Override
    public Tag lookup( String tag ){
        Tag val = null;
        
        try {
            val = Tags.valueOf( tag );
        } catch( Exception e ) {
            // ignore
        }
        
        if ( val == null ) {
            val = super.lookup( tag );
        }
        
        return val;
    }

    protected StratProps() {
        // protected
    }
}
