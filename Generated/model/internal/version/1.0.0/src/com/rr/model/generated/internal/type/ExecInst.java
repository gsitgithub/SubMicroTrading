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
 Instructions for order handling on exchange trading floor. If more than one instruction is applicable to an order, this field can contain multiple instructions separated by space
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum ExecInst implements SingleByteLookup {

    NotHeld( TypeIds.EXECINST_NOTHELD, "1" ),
    Work( TypeIds.EXECINST_WORK, "2" ),
    GoAlong( TypeIds.EXECINST_GOALONG, "3" ),
    OverTheDay( TypeIds.EXECINST_OVERTHEDAY, "4" ),
    Held( TypeIds.EXECINST_HELD, "5" ),
    PaticipateNotInitiate( TypeIds.EXECINST_PATICIPATENOTINITIATE, "6" ),
    StrictScale( TypeIds.EXECINST_STRICTSCALE, "7" ),
    TryToScale( TypeIds.EXECINST_TRYTOSCALE, "8" ),
    StayOnBidSide( TypeIds.EXECINST_STAYONBIDSIDE, "9" ),
    StayOnOfferSide( TypeIds.EXECINST_STAYONOFFERSIDE, "0" ),
    NoCross( TypeIds.EXECINST_NOCROSS, "A" ),
    OkToCross( TypeIds.EXECINST_OKTOCROSS, "B" ),
    CallFirst( TypeIds.EXECINST_CALLFIRST, "C" ),
    PercentageOfVolume( TypeIds.EXECINST_PERCENTAGEOFVOLUME, "D" ),
    DoNotIncrease( TypeIds.EXECINST_DONOTINCREASE, "E" ),
    DoNotReduce( TypeIds.EXECINST_DONOTREDUCE, "F" ),
    AllOrNone( TypeIds.EXECINST_ALLORNONE, "G" ),
    ReinstateOnSysFail( TypeIds.EXECINST_REINSTATEONSYSFAIL, "H" ),
    InstitutesOnly( TypeIds.EXECINST_INSTITUTESONLY, "I" ),
    ReinstateOnTradHalt( TypeIds.EXECINST_REINSTATEONTRADHALT, "J" ),
    CancelOnTradeHalt( TypeIds.EXECINST_CANCELONTRADEHALT, "K" ),
    LastPeg( TypeIds.EXECINST_LASTPEG, "L" ),
    MidPricePeg( TypeIds.EXECINST_MIDPRICEPEG, "M" ),
    NonNegotiable( TypeIds.EXECINST_NONNEGOTIABLE, "N" ),
    OpeningPeg( TypeIds.EXECINST_OPENINGPEG, "O" ),
    MarketPeg( TypeIds.EXECINST_MARKETPEG, "P" ),
    CancelOnSysFail( TypeIds.EXECINST_CANCELONSYSFAIL, "Q" ),
    PrimaryPeg( TypeIds.EXECINST_PRIMARYPEG, "R" ),
    Suspend( TypeIds.EXECINST_SUSPEND, "S" ),
    CustDispInst( TypeIds.EXECINST_CUSTDISPINST, "U" ),
    Netting( TypeIds.EXECINST_NETTING, "V" ),
    PegToVWAP( TypeIds.EXECINST_PEGTOVWAP, "W" ),
    TradeAlong( TypeIds.EXECINST_TRADEALONG, "X" ),
    TryToStop( TypeIds.EXECINST_TRYTOSTOP, "Y" ),
    CancelIfNotBest( TypeIds.EXECINST_CANCELIFNOTBEST, "Z" ),
    TrailingStopPeg( TypeIds.EXECINST_TRAILINGSTOPPEG, "a" ),
    StrictLimit( TypeIds.EXECINST_STRICTLIMIT, "b" ),
    IgnorePriceValidCheck( TypeIds.EXECINST_IGNOREPRICEVALIDCHECK, "c" ),
    PegToLimitPrice( TypeIds.EXECINST_PEGTOLIMITPRICE, "d" ),
    WorkToTgtStrat( TypeIds.EXECINST_WORKTOTGTSTRAT, "e" ),
    Unknown( TypeIds.EXECINST_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;

    ExecInst( int id, String val ) {
        _val = val.getBytes()[0];
        _id = id;
    }
    private static final int _indexOffset = 48;
    private static final ExecInst[] _entries = new ExecInst[54];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( ExecInst en : ExecInst.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static ExecInst getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for ExecInst" );
        }
        ExecInst eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for ExecInst" );
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
