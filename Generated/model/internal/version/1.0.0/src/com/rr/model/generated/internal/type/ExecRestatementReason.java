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
 Code to identify reason for an Execution Report (8) message sent with ExecType (150) =Restated or used when communicating an unsolicited cancel.
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum ExecRestatementReason implements TwoByteLookup {

    GTCorporateAction( TypeIds.EXECRESTATEMENTREASON_GTCORPORATEACTION, "0" ),
    GTRenewalOrRestatement( TypeIds.EXECRESTATEMENTREASON_GTRENEWALORRESTATEMENT, "1" ),
    VerbalChange( TypeIds.EXECRESTATEMENTREASON_VERBALCHANGE, "2" ),
    RepricingOfOrder( TypeIds.EXECRESTATEMENTREASON_REPRICINGOFORDER, "3" ),
    BrokerOption( TypeIds.EXECRESTATEMENTREASON_BROKEROPTION, "4" ),
    PartialDeclineOfOrderQty( TypeIds.EXECRESTATEMENTREASON_PARTIALDECLINEOFORDERQTY, "5" ),
    CancelOnTradingHalt( TypeIds.EXECRESTATEMENTREASON_CANCELONTRADINGHALT, "6" ),
    CancelOnSystemFailure( TypeIds.EXECRESTATEMENTREASON_CANCELONSYSTEMFAILURE, "7" ),
    MarketOption( TypeIds.EXECRESTATEMENTREASON_MARKETOPTION, "8" ),
    CanceledNotBest( TypeIds.EXECRESTATEMENTREASON_CANCELEDNOTBEST, "9" ),
    WarehouseRecap( TypeIds.EXECRESTATEMENTREASON_WAREHOUSERECAP, "10" ),
    Other( TypeIds.EXECRESTATEMENTREASON_OTHER, "99" ),
    Unknown( TypeIds.EXECRESTATEMENTREASON_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 2; }

    private final byte[] _val;
    private final int _id;

    ExecRestatementReason( int id, String val ) {
        _val = val.getBytes();
        _id = id;
    }
    private static ExecRestatementReason[] _entries = new ExecRestatementReason[256*256];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) 
            { _entries[i] = Unknown; }

        for ( ExecRestatementReason en : ExecRestatementReason.values() ) {
             if ( en == Unknown ) continue;
            byte[] val = en.getVal();
            int key = val[0] << 8;
            if ( val.length == 2 ) key += val[1];
            _entries[ key ] = en;
        }
    }

    public static ExecRestatementReason getVal( byte[] val, int offset, int len ) {
        int key = val[offset++] << 8;
        if ( len == 2 ) key += val[offset];
        ExecRestatementReason eval;
        eval = _entries[ key ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + ((key>0xFF) ? ("" + (char)(key>>8) + (char)(key&0xFF)) : ("" + (char)(key&0xFF))) + " for ExecRestatementReason" );
        return eval;
    }

    public static ExecRestatementReason getVal( byte[] val ) {
        int offset = 0;
        int key = val[offset++] << 8;
        if ( val.length == 2 ) key += val[offset];
        ExecRestatementReason eval;
        eval = _entries[ key ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + ((key>0xFF) ? ("" + (char)(key>>8) + (char)(key&0xFF)) : ("" + (char)(key&0xFF))) + " for ExecRestatementReason" );
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
