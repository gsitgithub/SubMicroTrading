/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common;

import com.rr.core.lang.HasReusableType;
import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.recycler.MessageRecycler;
import com.rr.mds.common.events.Subscribe;
import com.rr.mds.common.events.TradingRangeUpdate;
import com.rr.mds.common.events.TradingRangeUpdateRecycler;

public class MDSEventRecycler implements MessageRecycler {

    private TradingRangeUpdateRecycler  _tradingRangeUpdateRecycler;
    private Recycler<Subscribe>         _subscribeRecycler;

    public MDSEventRecycler() {
        SuperpoolManager sp = SuperpoolManager.instance();
        _tradingRangeUpdateRecycler = sp.getRecycler( TradingRangeUpdateRecycler.class, TradingRangeUpdate.class );
        _subscribeRecycler          = sp.getRecycler( Subscribe.class );
    }
    
    @Override
    public void recycle( HasReusableType msg ) {
        switch( msg.getReusableType().getSubId() ) {
        case MDSReusableTypeConstants.SUB_ID_SUBSCRIBE:
            _subscribeRecycler.recycle( (Subscribe) msg );
            break;
        case MDSReusableTypeConstants.SUB_ID_TRADING_BAND_UPDATE:
            _tradingRangeUpdateRecycler.recycle( (TradingRangeUpdate) msg );
            break;
        case MDSReusableTypeConstants.SUB_ID_FX_SNAPSHOT:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_ACTIVE_BBO:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_ACTIVE_DEPTH:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_SNAPSHOT_BBO:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_SNAPSHOT_DEPTH:
            break;
        }
    }
    
    public void recycle( TradingRangeUpdate msg ) {
        _tradingRangeUpdateRecycler.recycle( msg );
    }
}
