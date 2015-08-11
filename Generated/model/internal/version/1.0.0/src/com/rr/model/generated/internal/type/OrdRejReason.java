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
 Code to identify reason for order rejection
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum OrdRejReason implements TwoByteLookup {

    UnknownSymbol( TypeIds.ORDREJREASON_UNKNOWNSYMBOL, "1" ),
    ExchangeClosed( TypeIds.ORDREJREASON_EXCHANGECLOSED, "2" ),
    OrderExceedsLimit( TypeIds.ORDREJREASON_ORDEREXCEEDSLIMIT, "3" ),
    TooLateToEnter( TypeIds.ORDREJREASON_TOOLATETOENTER, "4" ),
    UnknownOrder( TypeIds.ORDREJREASON_UNKNOWNORDER, "5" ),
    DuplicateOrder( TypeIds.ORDREJREASON_DUPLICATEORDER, "6" ),
    DupOfVerbalOrder( TypeIds.ORDREJREASON_DUPOFVERBALORDER, "7" ),
    StaleOrder( TypeIds.ORDREJREASON_STALEORDER, "8" ),
    TradeAlongReq( TypeIds.ORDREJREASON_TRADEALONGREQ, "9" ),
    InvalidInvestorId( TypeIds.ORDREJREASON_INVALIDINVESTORID, "10" ),
    UnsupOrdCharacteristic( TypeIds.ORDREJREASON_UNSUPORDCHARACTERISTIC, "11" ),
    SurveillenceOption( TypeIds.ORDREJREASON_SURVEILLENCEOPTION, "12" ),
    IncorrectQuantity( TypeIds.ORDREJREASON_INCORRECTQUANTITY, "13" ),
    IncorrectAllocQty( TypeIds.ORDREJREASON_INCORRECTALLOCQTY, "14" ),
    UnknownAccounts( TypeIds.ORDREJREASON_UNKNOWNACCOUNTS, "15" ),
    Other( TypeIds.ORDREJREASON_OTHER, "99" ),
    Unknown( TypeIds.ORDREJREASON_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 2; }

    private final byte[] _val;
    private final int _id;

    OrdRejReason( int id, String val ) {
        _val = val.getBytes();
        _id = id;
    }
    private static OrdRejReason[] _entries = new OrdRejReason[256*256];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) 
            { _entries[i] = Unknown; }

        for ( OrdRejReason en : OrdRejReason.values() ) {
             if ( en == Unknown ) continue;
            byte[] val = en.getVal();
            int key = val[0] << 8;
            if ( val.length == 2 ) key += val[1];
            _entries[ key ] = en;
        }
    }

    public static OrdRejReason getVal( byte[] val, int offset, int len ) {
        int key = val[offset++] << 8;
        if ( len == 2 ) key += val[offset];
        OrdRejReason eval;
        eval = _entries[ key ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + ((key>0xFF) ? ("" + (char)(key>>8) + (char)(key&0xFF)) : ("" + (char)(key&0xFF))) + " for OrdRejReason" );
        return eval;
    }

    public static OrdRejReason getVal( byte[] val ) {
        int offset = 0;
        int key = val[offset++] << 8;
        if ( val.length == 2 ) key += val[offset];
        OrdRejReason eval;
        eval = _entries[ key ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + ((key>0xFF) ? ("" + (char)(key>>8) + (char)(key&0xFF)) : ("" + (char)(key&0xFF))) + " for OrdRejReason" );
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
