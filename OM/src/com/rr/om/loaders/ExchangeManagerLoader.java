/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.loaders;

import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.exchange.loader.XMLExchangeLoader;
import com.rr.om.model.id.DailyLongIDGenerator;


public class ExchangeManagerLoader implements SMTSingleComponentLoader {

    private int    _genNumIdPrefix  = 10;
    private String _fileName        = "./config/exchange.xml";
    
    @Override
    public SMTComponent create( String id ) {
        
        // @TODO move off singleton when all code base moved to component loaders
        ExchangeManager mgr = ExchangeManager.instance();
        
        mgr.setId( id );
        
        DailyLongIDGenerator numIdGen = new DailyLongIDGenerator( _genNumIdPrefix, 19 ); // to fit ENX numeric ID format
        mgr.register( numIdGen );
        XMLExchangeLoader loader = new XMLExchangeLoader( _fileName );
        loader.load();
        
        return mgr;
    }
}
