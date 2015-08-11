/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup;


public interface FixSimConstants {

    public String   DEFAULT_OM_UP_ID            = "SMC01";
    public String   DEFAULT_CLIENT_SIM_ID       = "CLT01";
    public int      DEFAULT_OM_CLIENT_PORT      = 14802;
    public String   DEFAULT_OM_DOWN_ID          = "SME01";
    public String   DEFAULT_EXCHANGE_SIM_ID     = "EXE1";
    public int      DEFAULT_OM_EXCHANGE_PORT    = 14812;
    public String   DEFAULT_HUB_HOST            = "localhost";
    public int      DEFAULT_HUB_PORT            = 14250;
    public String   DEFAULT_CLIENT_DATA_FILE    = "./data/fixClientSimOrders.txt";
    public String   DEFAULT_HUB_BRIDGE_ID       = "HUB01";
    public String   DEFAULT_OM_HUB_ID           = "SMH01";
}
