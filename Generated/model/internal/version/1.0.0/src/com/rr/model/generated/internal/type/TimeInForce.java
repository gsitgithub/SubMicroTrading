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
 Specifies how long the order remains in effect. Absence of this field is interpreted as DAY. NOTE not applicable to CIV Orders
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum TimeInForce implements SingleByteLookup {

    Day( TypeIds.TIMEINFORCE_DAY, "0" ),
    GoodTillCancel( TypeIds.TIMEINFORCE_GOODTILLCANCEL, "1" ),
    AtTheOpening( TypeIds.TIMEINFORCE_ATTHEOPENING, "2" ),
    ImmediateOrCancel( TypeIds.TIMEINFORCE_IMMEDIATEORCANCEL, "3" ),
    FillOrKill( TypeIds.TIMEINFORCE_FILLORKILL, "4" ),
    GoodTillCrossing( TypeIds.TIMEINFORCE_GOODTILLCROSSING, "5" ),
    GoodTillDate( TypeIds.TIMEINFORCE_GOODTILLDATE, "6" ),
    AtTheClose( TypeIds.TIMEINFORCE_ATTHECLOSE, "7" ),
    Unknown( TypeIds.TIMEINFORCE_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;

    TimeInForce( int id, String val ) {
        _val = val.getBytes()[0];
        _id = id;
    }
    private static final int _indexOffset = 48;
    private static final TimeInForce[] _entries = new TimeInForce[16];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( TimeInForce en : TimeInForce.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static TimeInForce getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for TimeInForce" );
        }
        TimeInForce eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for TimeInForce" );
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
