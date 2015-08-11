/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.validate;

import com.rr.om.client.OMClientProfile;
import com.rr.om.processor.BaseProcessorTestCase;
import com.rr.om.warmup.FixTestUtils;

public class TestNewOrderSingleClientValidation extends BaseProcessorTestCase {

    public void testAllThresholdWarnings() {
        String alertMsg = "Low total order value limit breached , client=TestClient, , session=unknown, " +
                          "orderValueUSD=6512.0, prevTotalValueUSD=100.0, newTotalValueUSD=6612.0, thresholdUSD=6000.0";
        
        OMClientProfile client =(OMClientProfile) FixTestUtils.getTestClient();
        client.setThresholds( 60, 80, 90 );
        client.setMaxTotalOrderValueUSD( 10000 );

        checkOrder( client, 1, 100.0, alertMsg, false, false );
        
        checkOrder( client, 100, 65.12, alertMsg, true, false );
        
        // check next order doesnt generate alert
        checkOrder( client, 1, 100.0, alertMsg, false, false );
        
        // now breach medium threshold
        alertMsg = "Medium total order value limit breached , client=TestClient, , session=unknown, orderValueUSD=2000.0, " +
                   "prevTotalValueUSD=6712.0, newTotalValueUSD=8712.0, thresholdUSD=8000.0";

        checkOrder( client, 20, 100.0, alertMsg, true, false );
        
        // check next order doesnt generate alert
        checkOrder( client, 1, 100.0, alertMsg, false, false );
        
        // now breach high threshold
        alertMsg = "High total order value limit breached , client=TestClient, , session=unknown, orderValueUSD=1000.0, " +
                   "prevTotalValueUSD=8812.0, newTotalValueUSD=9812.0, thresholdUSD=9000.0";

        checkOrder( client, 10, 100.0, alertMsg, true, false );

        // check next order doesnt generate alert
        checkOrder( client, 1, 100.0, alertMsg, false, false );
        
        alertMsg = "Maximum total order value exceeded , clOrdId=TST0000001, val=10012, max=10000";
        // check next order makes reject
        checkOrder( client, 1, 100.0, alertMsg, false, true );
    }

    public void testQtyRejects() {
        String alertMsg = "";
     
        OMClientProfile client =(OMClientProfile) FixTestUtils.getTestClient();
        client.setThresholds( 60, 80, 90 );
        client.setMaxSingleOrderValueUSD( 1000 );
        client.setMaxSingleOrderQty( 100 );
        client.setMaxTotalQty( 500 );
        client.setMaxTotalOrderValueUSD( 10000 );

        checkOrder( client, 1, 100.0, alertMsg, false, false );
        checkOrder( client, 100, 1.0, alertMsg, false, false );

        // break single order value limit
        alertMsg = "Maximum single order value exceeded , clOrdId=TST0000001, val=10100, max=1000";
        checkOrder( client, 101, 100.0, alertMsg, false, true );

        // check after reject next processed fine
        checkOrder( client, 1, 100.0, alertMsg, false, false );

        // break total qty limit
        alertMsg = "Maximum single order quantity exceeded , clOrdId=TST0000001, val=388, max=100";
        checkOrder( client, 388, 2.0, alertMsg, false, true );

        // check after reject next processed fine
        checkOrder( client, 10, 100.0, alertMsg, false, false );

        // break single order limit
        alertMsg = "Maximum single order quantity exceeded , clOrdId=TST0000001, val=101, max=100";
        checkOrder( client, 101, 2.0, alertMsg, false, true );

        // check after reject next processed fine
        checkOrder( client, 10, 100.0, alertMsg, false, false );
    }

}
