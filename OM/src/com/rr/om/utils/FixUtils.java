/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.utils;

import com.rr.core.lang.ReusableType;
import com.rr.model.generated.internal.core.FullEventIds;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.MarketCancelRejectWrite;
import com.rr.model.generated.internal.events.interfaces.MarketCancelledWrite;
import com.rr.model.generated.internal.events.interfaces.MarketDoneForDayWrite;
import com.rr.model.generated.internal.events.interfaces.MarketExpiredWrite;
import com.rr.model.generated.internal.events.interfaces.MarketNewOrderAckWrite;
import com.rr.model.generated.internal.events.interfaces.MarketOrderStatusWrite;
import com.rr.model.generated.internal.events.interfaces.MarketRejectedWrite;
import com.rr.model.generated.internal.events.interfaces.MarketReplacedWrite;
import com.rr.model.generated.internal.events.interfaces.MarketRestatedWrite;
import com.rr.model.generated.internal.events.interfaces.MarketStoppedWrite;
import com.rr.model.generated.internal.events.interfaces.MarketSuspendedWrite;
import com.rr.model.generated.internal.events.interfaces.MarketTradeCancelWrite;
import com.rr.model.generated.internal.events.interfaces.MarketTradeCorrectWrite;
import com.rr.model.generated.internal.events.interfaces.MarketTradeNewWrite;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;

public class FixUtils {

    public static final boolean overrideOrdStatus( CommonExecRpt msg, final ReusableType type, OrdStatus forceStatus ) {
        boolean openOnMkt = false;
        
        switch( type.getId() ) {
        case FullEventIds.ID_MARKET_NEWORDERACK:
            ((MarketNewOrderAckWrite)msg).setOrdStatus( forceStatus );
            openOnMkt = true;
            break;
        case FullEventIds.ID_MARKET_TRADENEW:
            ((MarketTradeNewWrite)msg).setOrdStatus( forceStatus );
            if ( msg.getExecType() != ExecType.Fill ) {
                openOnMkt = true;
            }
            break;
        case FullEventIds.ID_MARKET_CANCELREJECT:
            ((MarketCancelRejectWrite)msg).setOrdStatus( forceStatus );
            break;
        case FullEventIds.ID_MARKET_REJECTED:
            ((MarketRejectedWrite)msg).setOrdStatus( forceStatus );
            break;
        case FullEventIds.ID_MARKET_CANCELLED:
            ((MarketCancelledWrite)msg).setOrdStatus( forceStatus );
            break;
        case FullEventIds.ID_MARKET_REPLACED:
            ((MarketReplacedWrite)msg).setOrdStatus( forceStatus );
            openOnMkt = true;
            break;
        case FullEventIds.ID_MARKET_DONEFORDAY:
            ((MarketDoneForDayWrite)msg).setOrdStatus( forceStatus );
            break;
        case FullEventIds.ID_MARKET_STOPPED:
            ((MarketStoppedWrite)msg).setOrdStatus( forceStatus );
            break;
        case FullEventIds.ID_MARKET_EXPIRED:
            ((MarketExpiredWrite)msg).setOrdStatus( forceStatus );
            break;
        case FullEventIds.ID_MARKET_SUSPENDED:
            ((MarketSuspendedWrite)msg).setOrdStatus( forceStatus );
            break;
        case FullEventIds.ID_MARKET_RESTATED:
            ((MarketRestatedWrite)msg).setOrdStatus( forceStatus );
            openOnMkt = true;
            break;
        case FullEventIds.ID_MARKET_TRADECORRECT:
            ((MarketTradeCorrectWrite)msg).setOrdStatus( forceStatus );
            openOnMkt = true;
            break;
        case FullEventIds.ID_MARKET_TRADECANCEL:
            ((MarketTradeCancelWrite)msg).setOrdStatus( forceStatus );
            openOnMkt = true;
            break;
        case FullEventIds.ID_MARKET_ORDERSTATUS:
            ((MarketOrderStatusWrite)msg).setOrdStatus( forceStatus );
            break;
        case FullEventIds.ID_MARKET_CANCELREQUEST:
        case FullEventIds.ID_MARKET_CANCELREPLACEREQUEST:
        case FullEventIds.ID_MARKET_NEWORDERSINGLE:
            break;                         // not actually possible, here to allow use of tableswitch     
        }
        
        return openOnMkt;
    }
    
    public static final String chkDelim( String rawMessage ) {
        
        rawMessage = ensureUsingFixDelim( rawMessage );
        
        if ( rawMessage.charAt( rawMessage.length()-1 ) != '\001' ) {
            rawMessage = rawMessage + '\001';
        }

        return rawMessage;
    }

    public static final String ensureUsingFixDelim( String rawMessage ) {
        rawMessage = rawMessage.trim();
        
        if ( rawMessage.contains( "\001" ) ) {
            return rawMessage;
        }

        char last = rawMessage.charAt( rawMessage.length()-1 );
        
        if ( last == '|' ) {
            return rawMessage.replace( '|', '\001' );
        }
        
        if ( last == ';' ) {
            return rawMessage.replace( ';', '\001' );
        }
        
        if ( rawMessage.contains( "|" ) ) {
            return rawMessage.replace( '|', '\001' );
        }

        if ( rawMessage.contains( ";" ) ) {
            return rawMessage.replace( ';', '\001' );
        }
        
        return rawMessage;
    }
}
