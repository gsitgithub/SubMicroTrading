/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ViewString;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.exchange.loader.XMLExchangeLoader;
import com.rr.om.model.id.DailyLongIDGenerator;

public abstract class BaseOMTestCase extends BaseTestCase {

    private static boolean _loadedExchanges = false;

    public static synchronized void loadExchanges() {
        if ( !_loadedExchanges ) {
            _loadedExchanges = true;
            int idNumPrefix = 90;
            // ENX = 1100000000000000000
            DailyLongIDGenerator numIdGen = new DailyLongIDGenerator( idNumPrefix, 19 ); // to fit ENX numeric ID format
            ExchangeManager.instance().register( numIdGen );
            if ( ExchangeManager.instance().getByMIC( new ViewString("XCME") ) == null ) {
                XMLExchangeLoader loader = new XMLExchangeLoader( "./config/testExchange.xml" );
                loader.load();
            }
        }
    }
}
