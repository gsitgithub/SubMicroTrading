/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.type;


/**
 Security Trading Status
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum SecurityTradingStatus implements TwoByteLookup {

    OpeningDelay( TypeIds.SECURITYTRADINGSTATUS_OPENINGDELAY, "1" ),
    TradingHalt( TypeIds.SECURITYTRADINGSTATUS_TRADINGHALT, "2" ),
    Resume( TypeIds.SECURITYTRADINGSTATUS_RESUME, "3" ),
    NoOpenNoResume( TypeIds.SECURITYTRADINGSTATUS_NOOPENNORESUME, "4" ),
    PriceIndication( TypeIds.SECURITYTRADINGSTATUS_PRICEINDICATION, "5" ),
    TradingRangeIndication( TypeIds.SECURITYTRADINGSTATUS_TRADINGRANGEINDICATION, "6" ),
    MarketImbalanceBuy( TypeIds.SECURITYTRADINGSTATUS_MARKETIMBALANCEBUY, "7" ),
    MarketImbalanceSell( TypeIds.SECURITYTRADINGSTATUS_MARKETIMBALANCESELL, "8" ),
    MarketOnCloseImbalanceBuy( TypeIds.SECURITYTRADINGSTATUS_MARKETONCLOSEIMBALANCEBUY, "9" ),
    MarketOnCloseImbalanceSell( TypeIds.SECURITYTRADINGSTATUS_MARKETONCLOSEIMBALANCESELL, "10" ),
    Unassigned( TypeIds.SECURITYTRADINGSTATUS_UNASSIGNED, "11" ),
    NoMarketImbalance( TypeIds.SECURITYTRADINGSTATUS_NOMARKETIMBALANCE, "12" ),
    NoMarketOnCloseImbalance( TypeIds.SECURITYTRADINGSTATUS_NOMARKETONCLOSEIMBALANCE, "13" ),
    ITSPreOpening( TypeIds.SECURITYTRADINGSTATUS_ITSPREOPENING, "14" ),
    NewPriceIndication( TypeIds.SECURITYTRADINGSTATUS_NEWPRICEINDICATION, "15" ),
    TradeDisseminationTime( TypeIds.SECURITYTRADINGSTATUS_TRADEDISSEMINATIONTIME, "16" ),
    StartOfTradingSession( TypeIds.SECURITYTRADINGSTATUS_STARTOFTRADINGSESSION, "17" ),
    EndOfSessionTradingUnavailable( TypeIds.SECURITYTRADINGSTATUS_ENDOFSESSIONTRADINGUNAVAILABLE, "18" ),
    NotTradedOnThisMarket( TypeIds.SECURITYTRADINGSTATUS_NOTTRADEDONTHISMARKET, "19" ),
    Invalid( TypeIds.SECURITYTRADINGSTATUS_INVALID, "20" ),
    PreOpen( TypeIds.SECURITYTRADINGSTATUS_PREOPEN, "21" ),
    OpeningRotation( TypeIds.SECURITYTRADINGSTATUS_OPENINGROTATION, "22" ),
    FastMarket( TypeIds.SECURITYTRADINGSTATUS_FASTMARKET, "23" ),
    PreCross( TypeIds.SECURITYTRADINGSTATUS_PRECROSS, "24" ),
    Cross( TypeIds.SECURITYTRADINGSTATUS_CROSS, "25" ),
    Unknown( TypeIds.SECURITYTRADINGSTATUS_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 2; }

    private final byte[] _val;
    private final int _id;

    SecurityTradingStatus( int id, String val ) {
        _val = val.getBytes();
        _id = id;
    }
    private static SecurityTradingStatus[] _entries = new SecurityTradingStatus[256*256];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) 
            { _entries[i] = Unknown; }

        for ( SecurityTradingStatus en : SecurityTradingStatus.values() ) {
             if ( en == Unknown ) continue;
            byte[] val = en.getVal();
            int key = val[0] << 8;
            if ( val.length == 2 ) key += val[1];
            _entries[ key ] = en;
        }
    }

    public static SecurityTradingStatus getVal( byte[] val, int offset, int len ) {
        int key = val[offset++] << 8;
        if ( len == 2 ) key += val[offset];
        SecurityTradingStatus eval;
        eval = _entries[ key ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + ((key>0xFF) ? ("" + (char)(key>>8) + (char)(key&0xFF)) : ("" + (char)(key&0xFF))) + " for SecurityTradingStatus" );
        return eval;
    }

    public static SecurityTradingStatus getVal( byte[] val ) {
        int offset = 0;
        int key = val[offset++] << 8;
        if ( val.length == 2 ) key += val[offset];
        SecurityTradingStatus eval;
        eval = _entries[ key ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + ((key>0xFF) ? ("" + (char)(key>>8) + (char)(key&0xFF)) : ("" + (char)(key&0xFF))) + " for SecurityTradingStatus" );
        return eval;
    }

    @Override
    public final byte[] getVal() {
        return _val;
    }

    public final int getID() {
        return _id;
    }

}
