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
 Indicator to identify whether this fill was a result of a liquidity provider providing or liquidity taker taking the liquidity. Applicable only for OrdStatus (39) of Partial or Filled
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum LiquidityInd implements SingleByteLookup {

    AddedLiquidity( TypeIds.LIQUIDITYIND_ADDEDLIQUIDITY, "1" ),
    RemovedLiquidity( TypeIds.LIQUIDITYIND_REMOVEDLIQUIDITY, "2" ),
    LiquidityRoutedOut( TypeIds.LIQUIDITYIND_LIQUIDITYROUTEDOUT, "3" ),
    Auction( TypeIds.LIQUIDITYIND_AUCTION, "4" ),
    Unknown( TypeIds.LIQUIDITYIND_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;

    LiquidityInd( int id, String val ) {
        _val = val.getBytes()[0];
        _id = id;
    }
    private static final int _indexOffset = 49;
    private static final LiquidityInd[] _entries = new LiquidityInd[15];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( LiquidityInd en : LiquidityInd.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static LiquidityInd getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for LiquidityInd" );
        }
        LiquidityInd eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for LiquidityInd" );
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
