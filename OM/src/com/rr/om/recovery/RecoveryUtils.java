/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.model.generated.internal.events.impl.ClientCancelledImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeCancelImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeCorrectImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeNewImpl;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.ClientCancelledUpdate;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.TradeBase;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;
import com.rr.om.model.event.EventBuilderImpl;
import com.rr.om.model.event.EventUtils;

/**
 * functions usable by recovery implementations
 * 
 * has factory methods that would be inappropriate to add to the event builder where they would conceptually be better placed
 * they are here to avoid confusing the eventBuilder interface too much
 */
public final class RecoveryUtils {

    public static ClientTradeNewImpl populateUpTradeNew( OrderRequest src, 
                                                         int          cumQty, 
                                                         double       totTraded, 
                                                         OrdStatus    status, 
                                                         ZString      mktOrdId, 
                                                         TradeNew     mktTrade ) {
        
        final ClientTradeNewImpl trade  = new ClientTradeNewImpl();
        
        final double             lastPx = EventUtils.convertForMajorMinor( src, mktTrade.getLastPx() );
        final double             avgPx  = (cumQty <= 0) ? 0 : (totTraded / cumQty);
        final Instrument         inst   = src.getInstrument();
        final Exchange           ex     = inst.getExchange();
        
        int leavesQty  = src.getOrderQty() - cumQty;
        if ( leavesQty < 0 ) leavesQty = 0;

        trade.setSrcEvent( src );
        trade.setMessageHandler( src.getMessageHandler() );
        trade.getOrderIdForUpdate().setValue( mktOrdId );
        trade.setMktCapacity( mktTrade.getMktCapacity() );
        trade.setAvgPx( avgPx );
        trade.setCumQty( cumQty );
        trade.setLeavesQty( leavesQty );
        trade.setLastPx( lastPx );
        trade.setLastQty( mktTrade.getLastQty() );
        trade.getLastMktForUpdate().setValue( mktTrade.getLastMkt() );
        trade.setLiquidityInd( mktTrade.getLiquidityInd() );
        trade.setMultiLegReportingType( mktTrade.getMultiLegReportingType() );
        
        if ( ex.isGeneratedExecIDRequired() ) {
            ex.makeExecIdUnique( trade.getExecIdForUpdate(), mktTrade.getExecId(), inst );
        } else { // client execId side same as exchangeSide
            trade.getExecIdForUpdate().setValue( mktTrade.getExecId() );
        }
        
        trade.setOrdStatus( status );
        trade.setExecType( ExecType.Trade );
        
        return trade;
    }

    public static ClientTradeCorrectImpl populateUpTradeCorrect( OrderRequest src, 
                                                                 int          cumQty, 
                                                                 double       totTraded, 
                                                                 OrdStatus    status, 
                                                                 ZString      mktOrdId, 
                                                                 TradeCorrect mktTrade ) {
        
        final ClientTradeCorrectImpl trade = new ClientTradeCorrectImpl();
        final double             lastPx = EventUtils.convertForMajorMinor( src, mktTrade.getLastPx() );
        final double             avgPx  = (cumQty <= 0) ? 0 : (totTraded / cumQty);
        final Instrument         inst   = src.getInstrument();
        final Exchange           ex     = inst.getExchange();
        
        int leavesQty  = src.getOrderQty() - cumQty;
        if ( leavesQty < 0 ) leavesQty = 0;

        trade.setSrcEvent( src );
        trade.setMessageHandler( src.getMessageHandler() );
        trade.getOrderIdForUpdate().setValue( mktOrdId );
        trade.setMktCapacity( mktTrade.getMktCapacity() );
        trade.setAvgPx( avgPx );
        trade.setCumQty( cumQty );
        trade.setLeavesQty( leavesQty );
        trade.setLastPx( lastPx );
        trade.setLastQty( mktTrade.getLastQty() );
        trade.getLastMktForUpdate().setValue( mktTrade.getLastMkt() );
        trade.setLiquidityInd( mktTrade.getLiquidityInd() );
        trade.setMultiLegReportingType( mktTrade.getMultiLegReportingType() );
        
        if ( ex.isGeneratedExecIDRequired() ) {
            ex.makeExecIdUnique( trade.getExecIdForUpdate(), mktTrade.getExecId(), inst );
        } else { // client execId side same as exchangeSide
            trade.getExecIdForUpdate().setValue( mktTrade.getExecId() );
        }
        
        trade.setOrdStatus( status );
        trade.setExecType( ExecType.TradeCorrect );

        trade.getExecRefIDForUpdate().copy( mktTrade.getExecRefID() );
        
        return trade;
    }

    public static TradeCancel populateUpTradeCancel( OrderRequest src, 
                                                     int          cumQty, 
                                                     double       totTraded, 
                                                     OrdStatus    status, 
                                                     ZString      mktOrdId, 
                                                     TradeCancel  mktTrade,
                                                     TradeBase    origTrade ) {
        
        final ClientTradeCancelImpl trade = new ClientTradeCancelImpl();
        final double             lastPx = EventUtils.convertForMajorMinor( src, origTrade.getLastPx() );
        final double             avgPx  = (cumQty <= 0) ? 0 : (totTraded / cumQty);
        final Instrument         inst   = src.getInstrument();
        final Exchange           ex     = inst.getExchange();
        
        int leavesQty  = src.getOrderQty() - cumQty;
        if ( leavesQty < 0 ) leavesQty = 0;

        trade.setSrcEvent( src );
        trade.setMessageHandler( src.getMessageHandler() );
        trade.getOrderIdForUpdate().setValue( mktOrdId );
        trade.setMktCapacity( mktTrade.getMktCapacity() );
        trade.setAvgPx( avgPx );
        trade.setCumQty( cumQty );
        trade.setLeavesQty( leavesQty );
        trade.setLastPx( lastPx );
        trade.setLastQty( origTrade.getLastQty() );
        trade.getLastMktForUpdate().setValue( mktTrade.getLastMkt() );
        trade.setLiquidityInd( mktTrade.getLiquidityInd() );
        trade.setMultiLegReportingType( mktTrade.getMultiLegReportingType() );
        
        if ( ex.isGeneratedExecIDRequired() ) {
            ex.makeExecIdUnique( trade.getExecIdForUpdate(), mktTrade.getExecId(), inst );
        } else { // client execId side same as exchangeSide
            trade.getExecIdForUpdate().setValue( mktTrade.getExecId() );
        }
        
        trade.setOrdStatus( status );
        trade.setExecType( ExecType.TradeCancel );

        trade.getExecRefIDForUpdate().copy( mktTrade.getExecRefID() );
        
        return trade;
    }

    public static Cancelled createForceCancelled( OrderRequest req, CommonExecRpt lastExec, double avgPx, int cumQty, ZString msg ) {
        final ClientCancelledUpdate ccan = new ClientCancelledImpl(); 
        
        // only fields from cancel request required is the clOrdId
        ccan.getClOrdIdForUpdate().setValue( lastExec.getClOrdId() );
        
        // take the mktOrderId from the cancel req ver
        ccan.getOrderIdForUpdate().setValue( lastExec.getOrderId() );
        
        ccan.setSrcEvent( req );
        ccan.setAvgPx( avgPx );
        ccan.setCumQty( cumQty );
        ccan.setLeavesQty( 0 );
        ccan.setOrdStatus( OrdStatus.Canceled );

        ccan.setMktCapacity( lastExec.getMktCapacity() );

        ccan.setExecType( ExecType.Canceled );

        final ReusableString synthAckExecId = ccan.getExecIdForUpdate();
        synthAckExecId.setValue( EventBuilderImpl.FORCE_CANCEL_PREFIX );
        synthAckExecId.append( lastExec.getClOrdId() );
        
        return ccan;
    }
}
