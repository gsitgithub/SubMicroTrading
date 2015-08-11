/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.loader;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Exchange;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.exchange.loader.XMLExchangeLoader;
import com.rr.om.model.id.DailyLongIDGenerator;
import com.rr.om.model.instrument.ExchangeSession;

public class TestXMLExchangeLoader extends BaseTestCase {

    public void testXMLExchangeLoader() {
        int idNumPrefix = 90;
        // ENX = 1100000000000000000
        DailyLongIDGenerator numIdGen = new DailyLongIDGenerator( idNumPrefix, 19 ); // to fit ENX numeric ID format
        ExchangeManager.instance().register( numIdGen );
        
        ZString zLon = new ViewString( "XLON" );
        XMLExchangeLoader loader = new XMLExchangeLoader( "./config/testExchange.xml" );
        ExchangeManager.instance().clear();
        assertNull( ExchangeManager.instance().getByMIC( zLon ) );
        loader.load();

        Exchange lon = ExchangeManager.instance().getByMIC( zLon );
        assertNotNull( lon );
        
        ExchangeSession iobSess = lon.getExchangeSession( new ViewString( "IOB" ) );
        assertNotNull( iobSess );
        
        ExchangeSession defaultSess = lon.getSession();
        assertNotNull( defaultSess );
        
        assertNotSame( iobSess, defaultSess );
        
    }
}
