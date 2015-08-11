/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.loaders;

import com.rr.core.codec.CodecFactory;
import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.model.generated.fix.codec.CodecFactoryPopulator;
import com.rr.om.exchange.CodecLoader;
import com.rr.om.warmup.sim.WarmupUtils;


public class CodecFactoryLoader implements SMTSingleComponentLoader {

    @Override
    public SMTComponent create( String id ) {
        
        CodecFactory codecFactory = new CodecFactory( id );
        CodecFactoryPopulator pop = new CodecLoader();
        pop.register( codecFactory );
        
        WarmupUtils.setCodecFactory( codecFactory );
        
        return codecFactory;
    }
}
