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
 Designates the capacity of the firm placing the order
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum OrderCapacity implements SingleByteLookup {

    Proprietary( TypeIds.ORDERCAPACITY_PROPRIETARY, "G" ),
    Individual( TypeIds.ORDERCAPACITY_INDIVIDUAL, "I" ),
    Principal( TypeIds.ORDERCAPACITY_PRINCIPAL, "P" ),
    RisklessPrincipal( TypeIds.ORDERCAPACITY_RISKLESSPRINCIPAL, "R" ),
    AgentForOtherMember( TypeIds.ORDERCAPACITY_AGENTFOROTHERMEMBER, "W" ),
    Unknown( TypeIds.ORDERCAPACITY_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;

    OrderCapacity( int id, String val ) {
        _val = val.getBytes()[0];
        _id = id;
    }
    private static final int _indexOffset = 63;
    private static final OrderCapacity[] _entries = new OrderCapacity[25];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( OrderCapacity en : OrderCapacity.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static OrderCapacity getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for OrderCapacity" );
        }
        OrderCapacity eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for OrderCapacity" );
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
