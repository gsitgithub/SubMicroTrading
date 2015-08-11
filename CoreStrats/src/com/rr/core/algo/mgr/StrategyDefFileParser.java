/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mgr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.rr.core.algo.base.StrategyDefinition;
import com.rr.core.algo.base.StrategyDefinitionImpl;
import com.rr.core.algo.strats.Algo;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Instrument;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.SMTRuntimeException;

public class StrategyDefFileParser {

    private final InstrumentLocator             _instLocator;
    private final Map<String, String>           _algoClassNames;
    private final Map<String, AtomicInteger>    _ids = new HashMap<String, AtomicInteger>();

    public StrategyDefFileParser( InstrumentLocator instrumentLocator, Map<String, String> algoClassNames ) {
        _instLocator = instrumentLocator;
        _algoClassNames = algoClassNames;
    }

    /**
     * Parse the supplied strategy definition file into StrategyDefinition instances
     * 
     * @param defs the list to APPEND the strategy definition instances into ... does NOT clear list first
     *  
     *  file format is :-
     *  
        # algoId | pipelineId | securityDescriptionList | parameter1 , ... , parameterN
        # parameters applied using reflection

        CALARB | P1 | ESH4,ESM4,ESH4-ESM4 | ARB_THRESH=50
        CALARB | P2 | 6EH4,6EU4,6EH4-6EU4 | ARB_THRESH=5
     * @param instrumentLocator 
     * @param algoClassNames 
        
     * @throws IOException 
     */
    public void parseStrategyDefFile( List<StrategyDefinition> defs, String fileName ) {
        List<String> lines = new ArrayList<String>();
        
        try {
            FileUtils.read( lines, fileName, true, true );
        } catch( IOException e ) {
            throw new SMTRuntimeException( "Failed to read strategy definition file " + fileName + " : " + e.getMessage(), e );
        }

        int i=0;
        
        for( String line : lines ) {
            StrategyDefinition sd = parseStratDef( line, ++i, fileName );
            
            if ( sd != null ) {
                defs.add( sd );
            }
        }
    }

    @SuppressWarnings( "unchecked" )
    private StrategyDefinition parseStratDef( String line, int lineNo, String fileName ) {
        if ( line.length() == 0 ) return null;
        
        String[] parts = line.split( "\\|" );

        if ( parts.length != 4 ) {
            throw new SMTRuntimeException( "StrategyDefFileParser error in " + fileName + ", on line " + lineNo + 
                                           ", expected four fields, not " + parts.length );
        }
        
        String algoId           = parts[0].trim();
        String pipeLineId       = parts[1].trim();
        String securityDescList = parts[2].trim();
        String args             = parts[3].trim();

        String algoClassName = _algoClassNames.get( algoId );
        
        if ( algoClassName == null ) {
            throw new SMTRuntimeException( "StrategyDefFileParser error in " + fileName + ", on line " + lineNo + 
                                           ", algoId " + algoId + " not in algo map ... check map.algos.* entries" );
        }
        
        Class<? extends Algo<?>> aClass = null;
        
        try {
            aClass = (Class<? extends Algo<?>>) Class.forName( algoClassName );
        } catch( ClassNotFoundException e ) {
            throw new SMTRuntimeException( "StrategyDefFileParser error in " + fileName + ", on line " + lineNo + 
                                           ", unable to find class for algoId " + algoId + ", className=" + algoClassName );
        }
        
        List<Instrument> insts = new ArrayList<Instrument>();
        
        for( String secDesc : securityDescList.split( "," ) ) {
            secDesc = secDesc.trim();
            
            Instrument inst = _instLocator.getInstrumentBySecurityDesc( new ViewString(secDesc) );
            
            if ( inst == null ) {
                throw new SMTRuntimeException( "StrategyDefFileParser invalid securityDesc [" + secDesc + "] in " + fileName + ", on line " + lineNo ); 
            }
            
            insts.add( inst );
        }
        
        Map<String,String> props = new LinkedHashMap<String,String>();
        
        for( String arg : args.split( "," ) ) {
            String[] argParts = arg.split( "=" );
            
            if ( argParts.length != 2 ) {
                throw new SMTRuntimeException( "StrategyDefFileParser error in " + fileName + ", on line " + lineNo + 
                                               ", invalid property [" + arg + "] expected key=value " );
            }
            
            props.put( argParts[0].trim(), argParts[1].trim() );
        }
        
        String stratId = algoId + "_"  + nextStratInstanceId( algoId );
        
        StrategyDefinition def = new StrategyDefinitionImpl( stratId, algoId, pipeLineId, aClass, insts, props );
        
        return def;
    }

    private int nextStratInstanceId( String algoId ) {
        AtomicInteger intWrapper = _ids.get( algoId );
        
        if ( intWrapper == null ) {
            intWrapper = new AtomicInteger(0);
            
            _ids.put( algoId, intWrapper );
        }
        
        return intWrapper.incrementAndGet();
    }
}
