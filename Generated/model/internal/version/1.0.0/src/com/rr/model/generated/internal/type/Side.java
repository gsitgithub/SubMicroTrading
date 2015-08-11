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
 Side of order
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum Side implements SingleByteLookup, com.rr.core.model.BasicSide {

    Buy( TypeIds.SIDE_BUY, "1", true ),
    Sell( TypeIds.SIDE_SELL, "2", false ),
    BuyMinus( TypeIds.SIDE_BUYMINUS, "3", true ),
    SellPlus( TypeIds.SIDE_SELLPLUS, "4", false ),
    SellShort( TypeIds.SIDE_SELLSHORT, "5", false ),
    SellShortExempt( TypeIds.SIDE_SELLSHORTEXEMPT, "6", false ),
    Unknown( TypeIds.SIDE_UNKNOWN, "?", false );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;
    private boolean _isBuySide = false;

    Side( int id, String val,  boolean isBuySide ) {
        _val = val.getBytes()[0];
        _id = id;
        _isBuySide = isBuySide;
    }
    private static final int _indexOffset = 49;
    private static final Side[] _entries = new Side[15];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( Side en : Side.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static Side getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for Side" );
        }
        Side eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for Side" );
        return eval;
    }

    @Override
    public final byte getVal() {
        return _val;
    }

    public final int getID() {
        return _id;
    }

    public boolean getIsBuySide() { return _isBuySide; }
    public void setIsBuySide( boolean val ) { _isBuySide = val; }
}
