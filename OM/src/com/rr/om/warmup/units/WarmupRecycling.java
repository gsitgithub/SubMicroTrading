/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.units;

import com.rr.core.factories.ReusableStringFactory;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.pool.SuperPool;
import com.rr.core.recycler.ReusableStringRecycler;
import com.rr.core.warmup.JITWarmup;

public class WarmupRecycling implements JITWarmup {

    private int _warmupCount;
    
    public WarmupRecycling( int warmupCount ) {
        _warmupCount = warmupCount;
    }
    
    @Override
    public String getName() {
        return "Recycling";
    }

    @Override
    public void warmup() throws InstantiationException, IllegalAccessException {
        int chains   = 2;
        int poolSize = 2;
        
        SuperPool<ReusableString>   sp       = new SuperPool<ReusableString>( ReusableString.class, chains, poolSize, poolSize );
        ReusableStringFactory       factory  = new ReusableStringFactory( sp );
        ReusableStringRecycler      recycler = new ReusableStringRecycler( sp.getChainSize(), sp );

        @SuppressWarnings( "unused" )
        ReusableType t;
        
        int cnt = poolSize * chains * _warmupCount;
        
        for ( int i=0 ; i < cnt ; i++ ) {
            ReusableString l = factory.get();
            t = l.getReusableType();
            recycler.recycle( l );
        }
        
        sp.deleteAll();
    }
    
}
