/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.registry;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.model.generated.internal.events.impl.MarketTradeNewImpl;
import com.rr.model.generated.internal.events.interfaces.MarketTradeNewWrite;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.om.registry.FullTradeRegistry;
import com.rr.om.registry.TradeWrapper;

public class TestFullTradeRegistry extends BaseTestCase {

    public void testRegistryTrade() {
        
        FullTradeRegistry set = new FullTradeRegistry( 2 );

        MarketTradeNewWrite trade = new MarketTradeNewImpl();
        
        assertTrue(  set.register( null, setTrade( trade, "AAAA00001", 10, 121.2 ) ) );
        assertFalse( set.register( null, setTrade( trade, "AAAA00001", 10, 121.2 ) ) );
        assertFalse( set.register( null, setTrade( trade, "AAAA00001", 20, 121.2 ) ) );
        assertEquals( 1, set.size() );
        assertTrue(  set.contains( null, new ReusableString( "AAAA00001" ) ) );

        int max  = 16384;
        
        for ( int i=2 ; i < max ; ++ i ) {
            assertTrue(   set.register( null, setTrade( trade, "AAAA00001"+i, i, 121.2 ) )  );
            assertEquals( i, set.size() );
        }
        
        for ( int i=2 ; i < max ; ++ i ) {
            ReusableString execId = new ReusableString( "AAAA00001" + i ) ;
            assertTrue( set.contains( null, execId ) );
            
            TradeWrapper wrapper = set.get( null, execId );
            
            assertNotNull( wrapper );
            
            assertEquals( i, wrapper.getQty() );
        }

        assertFalse(  set.contains( null, new ReusableString( "AAAA00001" + (max+2) ) ) );

        set.clear();
        assertEquals( 0, set.size() );
    }

    private TradeNew setTrade( MarketTradeNewWrite trade, String execId, int qty, double px ) {
        trade.getExecIdForUpdate().setValue( execId.getBytes() );
        trade.setLastQty( qty );
        trade.setLastPx( px );
        return trade;
    }
}
