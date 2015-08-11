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
 Code to identify reason for a session-level Reject (3) message.
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum SessionRejectReason implements TwoByteLookup {

    InvalidTagNumber( TypeIds.SESSIONREJECTREASON_INVALIDTAGNUMBER, "0" ),
    RequiredTagMissing( TypeIds.SESSIONREJECTREASON_REQUIREDTAGMISSING, "1" ),
    TagNotForMsgType( TypeIds.SESSIONREJECTREASON_TAGNOTFORMSGTYPE, "2" ),
    UndefinedTag( TypeIds.SESSIONREJECTREASON_UNDEFINEDTAG, "3" ),
    TagNoValue( TypeIds.SESSIONREJECTREASON_TAGNOVALUE, "4" ),
    ValueIncorrect( TypeIds.SESSIONREJECTREASON_VALUEINCORRECT, "5" ),
    IncorrectFormat( TypeIds.SESSIONREJECTREASON_INCORRECTFORMAT, "6" ),
    DecryptProblem( TypeIds.SESSIONREJECTREASON_DECRYPTPROBLEM, "7" ),
    SignatureProblem( TypeIds.SESSIONREJECTREASON_SIGNATUREPROBLEM, "8" ),
    CompIDProblem( TypeIds.SESSIONREJECTREASON_COMPIDPROBLEM, "9" ),
    SendTimeAccuracy( TypeIds.SESSIONREJECTREASON_SENDTIMEACCURACY, "10" ),
    InvalidMsgType( TypeIds.SESSIONREJECTREASON_INVALIDMSGTYPE, "11" ),
    XMLValidError( TypeIds.SESSIONREJECTREASON_XMLVALIDERROR, "12" ),
    TagTooManyTimes( TypeIds.SESSIONREJECTREASON_TAGTOOMANYTIMES, "13" ),
    TagOutOfOrder( TypeIds.SESSIONREJECTREASON_TAGOUTOFORDER, "14" ),
    RepGroupOutOfOrder( TypeIds.SESSIONREJECTREASON_REPGROUPOUTOFORDER, "15" ),
    BadNumInGrp( TypeIds.SESSIONREJECTREASON_BADNUMINGRP, "16" ),
    SOHInData( TypeIds.SESSIONREJECTREASON_SOHINDATA, "17" ),
    Other( TypeIds.SESSIONREJECTREASON_OTHER, "99" ),
    ThrottleLimitExceeded( TypeIds.SESSIONREJECTREASON_THROTTLELIMITEXCEEDED, "20" ),
    ExposureLimitExceeded( TypeIds.SESSIONREJECTREASON_EXPOSURELIMITEXCEEDED, "21" ),
    ServiceTemporarilyNotAvailable( TypeIds.SESSIONREJECTREASON_SERVICETEMPORARILYNOTAVAILABLE, "22" ),
    ServiceNotAvailable( TypeIds.SESSIONREJECTREASON_SERVICENOTAVAILABLE, "23" ),
    ResultOfTransactionUnknown( TypeIds.SESSIONREJECTREASON_RESULTOFTRANSACTIONUNKNOWN, "24" ),
    OutboundConversionError( TypeIds.SESSIONREJECTREASON_OUTBOUNDCONVERSIONERROR, "25" ),
    InternalTechnicalError( TypeIds.SESSIONREJECTREASON_INTERNALTECHNICALERROR, "26" ),
    OrderNotFound( TypeIds.SESSIONREJECTREASON_ORDERNOTFOUND, "27" ),
    PriceNotReasonable( TypeIds.SESSIONREJECTREASON_PRICENOTREASONABLE, "28" ),
    Unknown( TypeIds.SESSIONREJECTREASON_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 2; }

    private final byte[] _val;
    private final int _id;

    SessionRejectReason( int id, String val ) {
        _val = val.getBytes();
        _id = id;
    }
    private static SessionRejectReason[] _entries = new SessionRejectReason[256*256];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) 
            { _entries[i] = Unknown; }

        for ( SessionRejectReason en : SessionRejectReason.values() ) {
             if ( en == Unknown ) continue;
            byte[] val = en.getVal();
            int key = val[0] << 8;
            if ( val.length == 2 ) key += val[1];
            _entries[ key ] = en;
        }
    }

    public static SessionRejectReason getVal( byte[] val, int offset, int len ) {
        int key = val[offset++] << 8;
        if ( len == 2 ) key += val[offset];
        SessionRejectReason eval;
        eval = _entries[ key ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + ((key>0xFF) ? ("" + (char)(key>>8) + (char)(key&0xFF)) : ("" + (char)(key&0xFF))) + " for SessionRejectReason" );
        return eval;
    }

    public static SessionRejectReason getVal( byte[] val ) {
        int offset = 0;
        int key = val[offset++] << 8;
        if ( val.length == 2 ) key += val[offset];
        SessionRejectReason eval;
        eval = _entries[ key ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + ((key>0xFF) ? ("" + (char)(key>>8) + (char)(key&0xFF)) : ("" + (char)(key&0xFF))) + " for SessionRejectReason" );
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
