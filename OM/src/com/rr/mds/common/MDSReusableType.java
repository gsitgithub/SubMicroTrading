/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common;

import com.rr.core.lang.ReusableCategory;
import com.rr.core.lang.ReusableCategoryEnum;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ReusableTypeIDFactory;


/**
 * represents a reusable type of object with a unique code identifier
 * 
 * enum not used as its not extensible
 *
 * @author Richard Rose
 */
public enum MDSReusableType implements ReusableType, MDSReusableTypeConstants {

    Subscribe(               ReusableCategoryEnum.MDS, FULL_ID_SUBSCRIBE,                   SUB_ID_SUBSCRIBE ), 
    TradingBandUpdate(       ReusableCategoryEnum.MDS, FULL_ID_TRADING_BAND_UPDATE,         SUB_ID_TRADING_BAND_UPDATE ), 
    FXSnapshot(              ReusableCategoryEnum.MDS, FULL_ID_FX_SNAPSHOT,                 SUB_ID_FX_SNAPSHOT ), 
    MarketDataActiveBBO(     ReusableCategoryEnum.MDS, FULL_ID_MARKET_DATA_ACTIVE_BBO,      SUB_ID_MARKET_DATA_ACTIVE_BBO ), 
    MarketDataActiveDepth(   ReusableCategoryEnum.MDS, FULL_ID_MARKET_DATA_ACTIVE_DEPTH,    SUB_ID_MARKET_DATA_ACTIVE_DEPTH ), 
    MarketDataSnapshotBBO(   ReusableCategoryEnum.MDS, FULL_ID_MARKET_DATA_SNAPSHOT_BBO,    SUB_ID_MARKET_DATA_SNAPSHOT_BBO),
    MarketDataSnapshotDepth( ReusableCategoryEnum.MDS, FULL_ID_MARKET_DATA_SNAPSHOT_DEPTH,  SUB_ID_MARKET_DATA_SNAPSHOT_DEPTH);
    
    private final int              _eventId;
    private final int              _id;
    private final ReusableCategory _cat;

    private MDSReusableType( ReusableCategory cat, int catId, int eventId ) {
        _cat     = cat;
        _id      = ReusableTypeIDFactory.setID( cat, catId );
        _eventId = eventId;
    }

    @Override
    public int getSubId() {
        return _eventId;
    }

    @Override
    public int getId() {
        return _id;
    }

    @Override
    public ReusableCategory getReusableCategory() {
        return _cat;
    }
}
