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
 Code to identify reason for cancel rejection
*/

import java.util.Map;
import java.util.HashMap;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ViewString;
import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum CxlRejReason implements MultiByteLookup {

    TooLateToCancel( TypeIds.CXLREJREASON_TOOLATETOCANCEL, "0" ),
    UnknownOrder( TypeIds.CXLREJREASON_UNKNOWNORDER, "1" ),
    BrokerOption( TypeIds.CXLREJREASON_BROKEROPTION, "2" ),
    AlreadyPending( TypeIds.CXLREJREASON_ALREADYPENDING, "3" ),
    UnableProcMassCancel( TypeIds.CXLREJREASON_UNABLEPROCMASSCANCEL, "4" ),
    OrigOrdModeTimeMismatch( TypeIds.CXLREJREASON_ORIGORDMODETIMEMISMATCH, "5" ),
    DuplicateClOrdId( TypeIds.CXLREJREASON_DUPLICATECLORDID, "6" ),
    PriceExceedsCurrentPrice( TypeIds.CXLREJREASON_PRICEEXCEEDSCURRENTPRICE, "7" ),
    PriceExceedsCurrentPriceBand( TypeIds.CXLREJREASON_PRICEEXCEEDSCURRENTPRICEBAND, "8" ),
    InvalidPriceIncrement( TypeIds.CXLREJREASON_INVALIDPRICEINCREMENT, "18" ),
    CMEOrderNotInBook( TypeIds.CXLREJREASON_CMEORDERNOTINBOOK, "2045" ),
    MarketClosed( TypeIds.CXLREJREASON_MARKETCLOSED, "1003" ),
    Other( TypeIds.CXLREJREASON_OTHER, "99" ),
    Unknown( TypeIds.CXLREJREASON_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 4; }

    private final byte[] _val;
    private final int _id;

    CxlRejReason( int id, String val ) {
        _val = val.getBytes();
        _id = id;
    }
    private static final ViewString _lookup = new ViewString();
    private static Map<ViewString,CxlRejReason> _map = new HashMap<ViewString,CxlRejReason>();

    static {
        for ( CxlRejReason en : CxlRejReason.values() ) {
             byte[] val = en.getVal();
            ViewString zVal = new ViewString( val );
            _map.put( zVal, en );
        }
    }

    public static CxlRejReason getVal( final byte[] buf, final int offset, final int len ) {
        _lookup.setValue( buf, offset, len );
        CxlRejReason val = _map.get( _lookup );
        if ( val == null ) throw new RuntimeDecodingException( "Unsupported value of " + _lookup + " for CxlRejReason" );
        return val;
    }

    public static CxlRejReason getVal( final ViewString key ) {
        CxlRejReason val = _map.get( key );
        if ( val == null ) throw new RuntimeDecodingException( "Unsupported value of " + key + " for CxlRejReason" );
        return val;
    }

    @Override
    public final byte[] getVal() {
        return _val;
    }

    public final int getID() {
        return _id;
    }

}
