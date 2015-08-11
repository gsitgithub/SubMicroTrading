/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.dummy.warmup;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.lang.stats.SizeType;
import com.rr.core.lang.stats.Stats;

public class TestStats implements Stats {

    private static final int DEFAULT_SIZE = 16;
    
    private Map<SizeType,Integer> _map = new HashMap<SizeType,Integer>();

    public TestStats() {
        for ( SizeType key : SizeType.values() ) {
            _map.put( key, new Integer( key.getSize() ) );
        }
        _map.put( SizeType.DEFAULT_VIEW_NOS_BUFFER,    new Integer(400) );
    }
    
    @Override
    public int find( SizeType id ) {
        Integer i = _map.get( id );
        
        return( i == null ? DEFAULT_SIZE : i.intValue() );
    }

    @Override
    public void initialise() {
        // NADA
    }

    @Override
    public void reload() {
        // NADA
    }

    @Override
    public void set( SizeType id, int val ) {
        _map.put( id, new Integer(val) );
    }

    @Override
    public void store() {
        // NADA
    }
}
