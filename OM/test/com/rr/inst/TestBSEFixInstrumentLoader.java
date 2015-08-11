/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import com.rr.core.lang.ViewString;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.utils.FileException;
import com.rr.om.BaseOMTestCase;
import com.rr.om.exchange.ExchangeManager;


public class TestBSEFixInstrumentLoader extends BaseOMTestCase {

    static {
        loadExchanges();
    }
    
    public void testLoadOne() throws FileException {
        Exchange e = ExchangeManager.instance().getByREC( new ViewString("2") );
        SingleExchangeInstrumentStore instStore = new SingleExchangeInstrumentStore( e, 1000 );
        FixInstrumentLoader loader = new FixInstrumentLoader( instStore );
        loader.loadFromFile( "../data/bse/common/cdx/sim/bseInst.cdx.sim.dat", e.getRecCode() );

        Instrument inst = instStore.getInstrumentByID( new ViewString("2"),  1001388 );
        assertNotNull( inst );
        assertEquals( 1001388, inst.getLongSymbol() );
        assertEquals( 5, inst.getSecurityGroupId() );
        
        inst = instStore.getInstrumentByID( new ViewString("2"),  72057606922829885L );
        assertNotNull( inst );
        assertEquals( 72057606922829885L, inst.getLongSymbol() );
        assertEquals( 3, inst.getSecurityGroupId() );
        
    }
}
