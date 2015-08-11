/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.dummy.warmup;

import java.util.Map;
import java.util.Map.Entry;

import com.rr.core.properties.AppProps;
import com.rr.core.properties.CoreProps;
import com.rr.core.properties.PropertyTags;

/**
 * DummyAppProperties - for tests
 */
public class DummyAppProperties extends AppProps {

    private static DummyAppProperties instance = null;
    
    public synchronized static void testInit( Map<String,String> p ) throws Exception {
        instance = new DummyAppProperties();
        
        instance.init( p, CoreProps.instance() );
    }
    
    public void init( Map<String,String> p, PropertyTags validNames ) throws Exception {
        setPropSet( validNames );
        for( Entry<String, String> entry : p.entrySet() ) {
            put( entry.getKey(), entry.getValue() );
        }
        resolveProps();
        setInit();
        
        AppProps.instance().init( this );
    }
}
