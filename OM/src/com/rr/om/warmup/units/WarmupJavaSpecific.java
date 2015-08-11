/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.units;

import com.rr.core.warmup.JITWarmup;

public class WarmupJavaSpecific implements JITWarmup {

    public static int _stopOpt;
    
    private int _warmupCount;

    public WarmupJavaSpecific( int warmupCount ) {
        _warmupCount = warmupCount;
    }

    @Override
    public String getName() {
        return "JavaSpecific";
    }

    @Override
    public void warmup() throws Exception {
        warmClass();
    }
    
    private void warmClass() {
        Class<WarmupJavaSpecific> c = WarmupJavaSpecific.class;
        
        java.lang.reflect.Method[] m;
        
        // 1250   b   java.lang.Class::clearCachesOnClassRedefinition (70 bytes)
        
        for( int i=0 ; i < _warmupCount ; i++ ) {
            m = c.getDeclaredMethods();
            _stopOpt += m.length;
        }
    }
}
