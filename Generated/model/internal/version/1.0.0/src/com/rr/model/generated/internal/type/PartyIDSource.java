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
 Identifies class or source of the PartyID (448) value. Required if PartyID (448) is specified. Note: applicable values depend upon PartyRole (452) specified
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum PartyIDSource implements SingleByteLookup {

    BIC( TypeIds.PARTYIDSOURCE_BIC, "B" ),
    MktParticipantId( TypeIds.PARTYIDSOURCE_MKTPARTICIPANTID, "C" ),
    CustomCode( TypeIds.PARTYIDSOURCE_CUSTOMCODE, "D" ),
    ISOCountryCode( TypeIds.PARTYIDSOURCE_ISOCOUNTRYCODE, "E" ),
    SettlementEntityLoc( TypeIds.PARTYIDSOURCE_SETTLEMENTENTITYLOC, "F" ),
    MIC( TypeIds.PARTYIDSOURCE_MIC, "G" ),
    CSD( TypeIds.PARTYIDSOURCE_CSD, "H" ),
    KoreanInvestorId( TypeIds.PARTYIDSOURCE_KOREANINVESTORID, "1" ),
    TaiQFII( TypeIds.PARTYIDSOURCE_TAIQFII, "2" ),
    TaiTradingAccount( TypeIds.PARTYIDSOURCE_TAITRADINGACCOUNT, "3" ),
    MalayCentralDepNum( TypeIds.PARTYIDSOURCE_MALAYCENTRALDEPNUM, "4" ),
    ChineseBShare( TypeIds.PARTYIDSOURCE_CHINESEBSHARE, "5" ),
    UKNationalInsNum( TypeIds.PARTYIDSOURCE_UKNATIONALINSNUM, "6" ),
    USSocialSecNum( TypeIds.PARTYIDSOURCE_USSOCIALSECNUM, "7" ),
    USEmpIdNum( TypeIds.PARTYIDSOURCE_USEMPIDNUM, "8" ),
    AustralianBizNum( TypeIds.PARTYIDSOURCE_AUSTRALIANBIZNUM, "9" ),
    AustralianTaxNum( TypeIds.PARTYIDSOURCE_AUSTRALIANTAXNUM, "A" ),
    DirectedBrokerCore( TypeIds.PARTYIDSOURCE_DIRECTEDBROKERCORE, "I" ),
    Unknown( TypeIds.PARTYIDSOURCE_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;

    PartyIDSource( int id, String val ) {
        _val = val.getBytes()[0];
        _id = id;
    }
    private static final int _indexOffset = 49;
    private static final PartyIDSource[] _entries = new PartyIDSource[25];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( PartyIDSource en : PartyIDSource.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static PartyIDSource getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for PartyIDSource" );
        }
        PartyIDSource eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for PartyIDSource" );
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
