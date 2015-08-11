/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium.lse;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.model.generated.internal.type.TypeIds;
import com.rr.om.Strings;
import com.rr.om.exchange.BaseExchangeValidator;


/**
 * may be used on multiple threads so dont maintain state in mem vars
 */

// TODO push common into base class

public class LSEMessageValidator extends BaseExchangeValidator {

    private static final ZString INVALID_ORD_QTY         = new ViewString( "Order qty must be greater than minQty of " );

    private final int _minQty = 10; // min qty on LSE

    @Override
    public final void validate( final NewOrderSingle msg, final ReusableString err, final long now ) {
        validateOpen( msg.getInstrument(), now, err );
        validateTIF( msg.getTimeInForce(), err );
        validateOrderType( msg.getOrdType(), err );
        validateMinQty( msg.getOrderQty(), err );
    }

    @Override
    public void validate( CancelReplaceRequest msg, ReusableString err, long now ) {
        validateOpen( msg.getInstrument(), now, err );
        validateTIF( msg.getTimeInForce(), err );
        validateOrderType( msg.getOrdType(), err );
        validateMinQty( msg.getOrderQty(), err );
    }
    
    private void validateMinQty( int orderQty, ReusableString err ) {
        if ( orderQty < _minQty ) delim( err ).append( INVALID_ORD_QTY ).append( _minQty ).append( Strings.QUANTITY ).append( orderQty );
    }

    private void validateOrderType( OrdType ordType, ReusableString err ) {
        switch( ordType.getID() ) {
        case TypeIds.ORDTYPE_LIMIT:
        case TypeIds.ORDTYPE_STOP:
        case TypeIds.ORDTYPE_STOPLIMIT:
        case TypeIds.ORDTYPE_WITHORWITHOUT:
        case TypeIds.ORDTYPE_LIMITORBETTER:
        case TypeIds.ORDTYPE_LIMITWITHORWITHOUT:
        case TypeIds.ORDTYPE_ONBASIS:
        case TypeIds.ORDTYPE_PREVQUOTED:
        case TypeIds.ORDTYPE_PREVINDICATED:
        case TypeIds.ORDTYPE_FOREXSWAP:
        case TypeIds.ORDTYPE_FUNARI:
        case TypeIds.ORDTYPE_MARKETIFTOUCHED:
        case TypeIds.ORDTYPE_MKTWITHLEFTOVERASLIMIT:
        case TypeIds.ORDTYPE_PREVFUNDVALPOINT:
        case TypeIds.ORDTYPE_NEXTFUNDVALPOINT:
            break;
        case TypeIds.ORDTYPE_MARKET:
        case TypeIds.ORDTYPE_PEGGED:
        case TypeIds.ORDTYPE_UNKNOWN:
            addErrorUnsupported( ordType, err );
            return;
        }
    }

    private void validateTIF( TimeInForce tif, ReusableString err  ) {
        if ( tif == null )
            return;
        switch( tif.getID() ) {
        case TypeIds.TIMEINFORCE_DAY:
        case TypeIds.TIMEINFORCE_IMMEDIATEORCANCEL:
        case TypeIds.TIMEINFORCE_FILLORKILL:
            break;
        case TypeIds.TIMEINFORCE_GOODTILLCANCEL:
        case TypeIds.TIMEINFORCE_ATTHEOPENING:
        case TypeIds.TIMEINFORCE_GOODTILLCROSSING:
        case TypeIds.TIMEINFORCE_GOODTILLDATE:
        case TypeIds.TIMEINFORCE_ATTHECLOSE:
        case TypeIds.TIMEINFORCE_UNKNOWN:
            addErrorUnsupported( tif, err );
            return;
        }
    }
}
