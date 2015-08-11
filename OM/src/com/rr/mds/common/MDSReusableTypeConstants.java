/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common;

import com.rr.core.lang.ReusableCategoryEnum;

public interface MDSReusableTypeConstants {

    public static int FULL_ID_SUBSCRIBE                     = ReusableCategoryEnum.MDS.getBaseId() + 1; 
    public static int FULL_ID_TRADING_BAND_UPDATE           = ReusableCategoryEnum.MDS.getBaseId() + 2; 
    public static int FULL_ID_FX_SNAPSHOT                   = ReusableCategoryEnum.MDS.getBaseId() + 3;
    public static int FULL_ID_MARKET_DATA_ACTIVE_BBO        = ReusableCategoryEnum.MDS.getBaseId() + 4;
    public static int FULL_ID_MARKET_DATA_ACTIVE_DEPTH      = ReusableCategoryEnum.MDS.getBaseId() + 5;
    public static int FULL_ID_MARKET_DATA_SNAPSHOT_BBO      = ReusableCategoryEnum.MDS.getBaseId() + 6;
    public static int FULL_ID_MARKET_DATA_SNAPSHOT_DEPTH    = ReusableCategoryEnum.MDS.getBaseId() + 7;

    public static int SUB_ID_SUBSCRIBE                      = 1;
    public static int SUB_ID_TRADING_BAND_UPDATE            = 2;
    public static int SUB_ID_FX_SNAPSHOT                    = 3;
    public static int SUB_ID_MARKET_DATA_ACTIVE_BBO         = 4;
    public static int SUB_ID_MARKET_DATA_ACTIVE_DEPTH       = 5;
    public static int SUB_ID_MARKET_DATA_SNAPSHOT_BBO       = 6;
    public static int SUB_ID_MARKET_DATA_SNAPSHOT_DEPTH     = 7;
}
