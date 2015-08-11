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
 Market Data Update Entry Type
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum MDEntryType implements SingleByteLookup {

    Bid( TypeIds.MDENTRYTYPE_BID, "0" ),
    Offer( TypeIds.MDENTRYTYPE_OFFER, "1" ),
    Trade( TypeIds.MDENTRYTYPE_TRADE, "2" ),
    IndexValue( TypeIds.MDENTRYTYPE_INDEXVALUE, "3" ),
    OpeningPrice( TypeIds.MDENTRYTYPE_OPENINGPRICE, "4" ),
    ClosingPrice( TypeIds.MDENTRYTYPE_CLOSINGPRICE, "5" ),
    SettlementPrice( TypeIds.MDENTRYTYPE_SETTLEMENTPRICE, "6" ),
    TradingSessionHighPrice( TypeIds.MDENTRYTYPE_TRADINGSESSIONHIGHPRICE, "7" ),
    TradingSessionLowPrice( TypeIds.MDENTRYTYPE_TRADINGSESSIONLOWPRICE, "8" ),
    TradingSessionVWAPPrice( TypeIds.MDENTRYTYPE_TRADINGSESSIONVWAPPRICE, "9" ),
    Imbalance( TypeIds.MDENTRYTYPE_IMBALANCE, "A" ),
    TradeVolume( TypeIds.MDENTRYTYPE_TRADEVOLUME, "B" ),
    OpenInterest( TypeIds.MDENTRYTYPE_OPENINTEREST, "C" ),
    CompositeUnderlyingPrice( TypeIds.MDENTRYTYPE_COMPOSITEUNDERLYINGPRICE, "D" ),
    SimulatedSellPrice( TypeIds.MDENTRYTYPE_SIMULATEDSELLPRICE, "E" ),
    SimulatedBuy( TypeIds.MDENTRYTYPE_SIMULATEDBUY, "F" ),
    MarginRate( TypeIds.MDENTRYTYPE_MARGINRATE, "G" ),
    MidPrice( TypeIds.MDENTRYTYPE_MIDPRICE, "H" ),
    EmptyBook( TypeIds.MDENTRYTYPE_EMPTYBOOK, "J" ),
    SettleHighPrice( TypeIds.MDENTRYTYPE_SETTLEHIGHPRICE, "K" ),
    SettleLowPrice( TypeIds.MDENTRYTYPE_SETTLELOWPRICE, "L" ),
    PriorSettlePrice( TypeIds.MDENTRYTYPE_PRIORSETTLEPRICE, "M" ),
    SessionHighBid( TypeIds.MDENTRYTYPE_SESSIONHIGHBID, "N" ),
    SessionLowOffer( TypeIds.MDENTRYTYPE_SESSIONLOWOFFER, "O" ),
    EarlyPrices( TypeIds.MDENTRYTYPE_EARLYPRICES, "P" ),
    AuctionClearingPrice( TypeIds.MDENTRYTYPE_AUCTIONCLEARINGPRICE, "Q" ),
    FixingPrice( TypeIds.MDENTRYTYPE_FIXINGPRICE, "W" ),
    Unknown( TypeIds.MDENTRYTYPE_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;

    MDEntryType( int id, String val ) {
        _val = val.getBytes()[0];
        _id = id;
    }
    private static final int _indexOffset = 48;
    private static final MDEntryType[] _entries = new MDEntryType[40];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( MDEntryType en : MDEntryType.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static MDEntryType getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for MDEntryType" );
        }
        MDEntryType eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for MDEntryType" );
        return eval;
    }

    @Override
    public final byte getVal() {
        return _val;
    }

    public final int getID() {
        return _id;
    }

}
