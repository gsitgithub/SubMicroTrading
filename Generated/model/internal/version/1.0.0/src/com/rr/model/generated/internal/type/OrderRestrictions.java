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
 Restrictions associated with an order. If more than one restriction is applicable to an order, this field can contain multiple instructions separated by space
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum OrderRestrictions implements SingleByteLookup {

    ProgramTrade( TypeIds.ORDERRESTRICTIONS_PROGRAMTRADE, "1" ),
    IndexArbitrage( TypeIds.ORDERRESTRICTIONS_INDEXARBITRAGE, "2" ),
    NonIndexArbitrage( TypeIds.ORDERRESTRICTIONS_NONINDEXARBITRAGE, "3" ),
    CompetingMarketMaker( TypeIds.ORDERRESTRICTIONS_COMPETINGMARKETMAKER, "4" ),
    SpecialistInSecurity( TypeIds.ORDERRESTRICTIONS_SPECIALISTINSECURITY, "5" ),
    SpecialistInUnderlyingSec( TypeIds.ORDERRESTRICTIONS_SPECIALISTINUNDERLYINGSEC, "6" ),
    ForeignEntity( TypeIds.ORDERRESTRICTIONS_FOREIGNENTITY, "7" ),
    ExtMarketParticipant( TypeIds.ORDERRESTRICTIONS_EXTMARKETPARTICIPANT, "8" ),
    ExtInterConnectMktLink( TypeIds.ORDERRESTRICTIONS_EXTINTERCONNECTMKTLINK, "9" ),
    RisklessArbitrage( TypeIds.ORDERRESTRICTIONS_RISKLESSARBITRAGE, "A" ),
    Unknown( TypeIds.ORDERRESTRICTIONS_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;

    OrderRestrictions( int id, String val ) {
        _val = val.getBytes()[0];
        _id = id;
    }
    private static final int _indexOffset = 49;
    private static final OrderRestrictions[] _entries = new OrderRestrictions[17];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( OrderRestrictions en : OrderRestrictions.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static OrderRestrictions getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for OrderRestrictions" );
        }
        OrderRestrictions eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for OrderRestrictions" );
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
