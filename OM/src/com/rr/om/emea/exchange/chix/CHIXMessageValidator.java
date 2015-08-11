/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.chix;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.ExecInst;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.model.generated.internal.type.TypeIds;
import com.rr.om.Strings;
import com.rr.om.exchange.BaseExchangeValidator;


/**
 * may be used on multiple threads so dont maintain state in mem vars
 */

// TODO push common into base class

public class CHIXMessageValidator extends BaseExchangeValidator {

    private static final ZString INVALID_ORD_QTY         = new ViewString( "Order qty must be greater than minQty of " );

    private final int _minQty = 1; // min qty on CHIX

    @Override
    public final void validate( final NewOrderSingle msg, final ReusableString err, final long now ) {
        validateOpen( msg.getInstrument(), now, err );
        validateTIF( msg.getTimeInForce(), err );
        validateOrderType( msg.getOrdType(), err );
        validateMinQty( msg.getOrderQty(), err );
        validateExecInstruction( msg.getExecInst(), err );
    }

    @Override
    public final void validate( final CancelReplaceRequest msg, final  ReusableString err, final long now ) {
        validateOpen( msg.getInstrument(), now, err );
        validateTIF( msg.getTimeInForce(), err );
        validateOrderType( msg.getOrdType(), err );
        validateMinQty( msg.getOrderQty(), err );
        validateExecInstruction( msg.getExecInst(), err );
    }
    
    private final void validateMinQty( final int orderQty, final ReusableString err ) {
        if ( orderQty < _minQty ) delim( err ).append( INVALID_ORD_QTY ).append( _minQty ).append( Strings.DELIM ).append( Strings.QUANTITY ).append( orderQty );
    }

    private void validateOrderType( final OrdType ordType, final ReusableString err ) {
        switch( ordType.getID() ) {
        case TypeIds.ORDTYPE_LIMIT:
            break;
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
        case TypeIds.ORDTYPE_STOP:
        case TypeIds.ORDTYPE_STOPLIMIT:
        case TypeIds.ORDTYPE_MARKET:
        case TypeIds.ORDTYPE_PEGGED:
        case TypeIds.ORDTYPE_UNKNOWN:
        default:
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
        default:
            addErrorUnsupported( tif, err );
            return;
        }
    }

    private void validateExecInstruction( ExecInst execInst, ReusableString err ) {
        if ( execInst == null )
            return;
        switch( execInst.getID() ) {
        case TypeIds.EXECINST_NOTHELD:
        case TypeIds.EXECINST_WORK:
        case TypeIds.EXECINST_GOALONG:
        case TypeIds.EXECINST_OVERTHEDAY:
        case TypeIds.EXECINST_CANCELONTRADEHALT:
        case TypeIds.EXECINST_CANCELONSYSFAIL:
            break;
        case TypeIds.EXECINST_HELD:
        case TypeIds.EXECINST_PATICIPATENOTINITIATE:
        case TypeIds.EXECINST_STRICTSCALE:
        case TypeIds.EXECINST_TRYTOSCALE:
        case TypeIds.EXECINST_STAYONBIDSIDE:
        case TypeIds.EXECINST_STAYONOFFERSIDE:
        case TypeIds.EXECINST_NOCROSS:
        case TypeIds.EXECINST_OKTOCROSS:
        case TypeIds.EXECINST_CALLFIRST:
        case TypeIds.EXECINST_PERCENTAGEOFVOLUME:
        case TypeIds.EXECINST_DONOTINCREASE:
        case TypeIds.EXECINST_DONOTREDUCE:
        case TypeIds.EXECINST_ALLORNONE:
        case TypeIds.EXECINST_REINSTATEONSYSFAIL:
        case TypeIds.EXECINST_INSTITUTESONLY:
        case TypeIds.EXECINST_REINSTATEONTRADHALT:
        case TypeIds.EXECINST_LASTPEG:
        case TypeIds.EXECINST_MIDPRICEPEG:
        case TypeIds.EXECINST_NONNEGOTIABLE:
        case TypeIds.EXECINST_OPENINGPEG:
        case TypeIds.EXECINST_MARKETPEG:
        case TypeIds.EXECINST_PRIMARYPEG:
        case TypeIds.EXECINST_SUSPEND:
        case TypeIds.EXECINST_CUSTDISPINST:
        case TypeIds.EXECINST_NETTING:
        case TypeIds.EXECINST_PEGTOVWAP:
        case TypeIds.EXECINST_TRADEALONG:
        case TypeIds.EXECINST_TRYTOSTOP:
        case TypeIds.EXECINST_CANCELIFNOTBEST:
        case TypeIds.EXECINST_TRAILINGSTOPPEG:
        case TypeIds.EXECINST_STRICTLIMIT:
        case TypeIds.EXECINST_IGNOREPRICEVALIDCHECK:
        case TypeIds.EXECINST_PEGTOLIMITPRICE:
        case TypeIds.EXECINST_WORKTOTGTSTRAT:
        case TypeIds.EXECINST_UNKNOWN:
        default:
            addErrorUnsupported( execInst, err );
            return;
        }
    }
}
