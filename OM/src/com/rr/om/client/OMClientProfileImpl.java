/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.client;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.MessageHandler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.session.Session;
import com.rr.model.generated.internal.events.factory.ClientAlertLimitBreachFactory;
import com.rr.model.generated.internal.events.impl.ClientAlertLimitBreachImpl;
import com.rr.model.generated.internal.events.interfaces.Alert;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.DoneForDay;
import com.rr.model.generated.internal.events.interfaces.Expired;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.OrderStatus;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.Restated;
import com.rr.model.generated.internal.events.interfaces.Stopped;
import com.rr.model.generated.internal.events.interfaces.Suspended;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.Strings;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;
import com.rr.om.processor.states.StateException;
import com.rr.om.processor.states.ValidationStateException;

/**
 * @WARNING be very careful, the order request ccy and price are in the CLIENT ccy
 *          they can be used in same calc with usd conversion factor BUT nowhere else as
 *          everything else in trading ccy
 *
 * @author Richard Rose
 */
public class OMClientProfileImpl implements OMClientProfile {
    private static final Logger  _log = LoggerFactory.create( OMClientProfileImpl.class );

    private static final ZString LOW_THRESHOLD_BREACH   = new ViewString( "Low total order value limit breached " );
    private static final ZString MEDIUM_THRESHOLD_ALERT = new ViewString( "Medium total order value limit breached " );
    private static final ZString HIGH_THRESHOLD_ALERT   = new ViewString( "High total order value limit breached " );
    private static final ZString SESSION                = new ViewString( ", session=" );
    private static final ZString CLIENT                 = new ViewString( ", client=" );
    private static final ZString ORD_VAL_USD            = new ViewString( "orderValueUSD=" );
    private static final ZString PREV_VAL_USD           = new ViewString( "prevTotalValueUSD=" );
    private static final ZString NEW_VAL_USD            = new ViewString( "newTotalValueUSD=" );
    private static final ZString LIMIT_USD              = new ViewString( "thresholdUSD=" );

    private static final ZString MAX_SINGLE_ORDER_VAL_EXCEEDED = new ViewString( "Maximum single order value exceeded " );
    private static final ZString MAX_SINGLE_ORDER_QTY_EXCEEDED = new ViewString( "Maximum single order quantity exceeded " );
    private static final ZString MAX_TOTAL_ORDER_VAL_EXCEEDED  = new ViewString( "Maximum total order value exceeded " );
    private static final ZString MAX_TOTAL_ORDER_QTY_EXCEEDED  = new ViewString( "Maximum total order quantity exceeded " );

    private final ZString _client;
    private final String  _id;
    
    private       double  _maxSingleOrderValueUSD = ClientProfile.DEFAULT_MAX_SINGLE_VAL;
    private       int     _maxSingleOrderQty      = ClientProfile.DEFAULT_MAX_SINGLE_QTY;
    private       long    _maxTotalQty            = ClientProfile.DEFAULT_MAX_TOTAL_QTY;
    private       double  _maxTotalOrderValueUSD  = ClientProfile.DEFAULT_MAX_TOTAL_VAL;

    private       long    _totalQty;
    private       double  _totalValueUSD;
    
    private       int     _lowThresholdPercent      = ClientProfile.DEFAULT_LOW_THRESHOLD;
    private       int     _medThresholdPercent      = ClientProfile.DEFAULT_MED_THRESHOLD;
    private       int     _highThresholdPercent     = ClientProfile.DEFAULT_HIGH_THRESHOLD;

    private       double  _lowThresholdUSD;
    private       double  _medThresholdUSD;
    private       double  _highThresholdUSD;
    
    private       boolean _sendClientLateFills = true;

    private static ClientAlertLimitBreachFactory _alertFactory = SuperpoolManager.instance().getFactory( ClientAlertLimitBreachFactory.class, 
                                                                                                         ClientAlertLimitBreachImpl.class );

    public OMClientProfileImpl( String componentId, ZString clientName ) {
        _client = clientName;
        _id = componentId;
        recalcThreshold();
    }
    
    public OMClientProfileImpl( ZString client,
                                double  maxSingleOrderValueUSD, 
                                int     maxSingleOrderQty, 
                                long    maxTotalQty, 
                                double  maxTotalValueUSD, 
                                int     lowThresholdPercent,
                                int     medThresholdPercent, 
                                int     highThresholdPercent ) {

        _id                     = null;
        _client                 = client;
        _maxSingleOrderValueUSD = maxSingleOrderValueUSD;
        _maxSingleOrderQty      = maxSingleOrderQty;
        _maxTotalQty            = maxTotalQty;
        
        _maxTotalOrderValueUSD = maxTotalValueUSD;

        setThresholds( lowThresholdPercent, medThresholdPercent, highThresholdPercent );
    }

    @Override
    public void setMaxTotalOrderValueUSD( double maxTotalValueUSD ) {
        _maxTotalOrderValueUSD = (maxTotalValueUSD<=0) ? Long.MAX_VALUE : maxTotalValueUSD;

        recalcThreshold();
    }

    @Override
    public double getMaxTotalOrderValueUSD() {
        return _maxTotalOrderValueUSD;
    }

    @Override
    public double getMaxSingleOrderValueUSD() {
        return _maxSingleOrderValueUSD;
    }

    @Override
    public void setMaxSingleOrderValueUSD( double maxSingleOrderValueUSD ) {
        _maxSingleOrderValueUSD = (maxSingleOrderValueUSD<=0) ? Long.MAX_VALUE : maxSingleOrderValueUSD;
    }

    @Override
    public int getMaxSingleOrderQty() {
        return _maxSingleOrderQty;
    }

    @Override
    public void setMaxSingleOrderQty( int maxSingleOrderQty ) {
        _maxSingleOrderQty = (maxSingleOrderQty<=0) ? Integer.MAX_VALUE : maxSingleOrderQty;
    }

    @Override
    public long getMaxTotalQty() {
        return _maxTotalQty;
    }

    @Override
    public void setMaxTotalQty( long maxTotalQty ) {
        _maxTotalQty = (maxTotalQty <=0) ? Long.MAX_VALUE : maxTotalQty;
    }

    @Override
    public long getTotalQty() {
        return _totalQty;
    }

    @Override
    public void setThresholds( int lowThresholdPercent, int medThresholdPercent, int highThresholdPercent ) {

        _lowThresholdPercent    = lowThresholdPercent;
        _medThresholdPercent    = medThresholdPercent; 
        _highThresholdPercent   = highThresholdPercent;
        
        recalcThreshold();
    }
        
    private void recalcThreshold() {
        if ( _maxTotalOrderValueUSD == Long.MAX_VALUE ) {
            _log.info( "Client " + _client + " totOrdValue UNLIMITED" ); 

            _lowThresholdUSD        = Double.MAX_VALUE;
            _medThresholdUSD        = Double.MAX_VALUE;
            _highThresholdUSD       = Double.MAX_VALUE;
            
        } else {
            _lowThresholdUSD        = (_maxTotalOrderValueUSD * _lowThresholdPercent)  / 100;
            _medThresholdUSD        = (_maxTotalOrderValueUSD * _medThresholdPercent)  / 100;
            _highThresholdUSD       = (_maxTotalOrderValueUSD * _highThresholdPercent) / 100;
            
            _log.info( "Client " + _client + " totOrdValue thresholds low=" + _lowThresholdUSD + " (" + _lowThresholdPercent + ")" + 
                       ", med="  + _medThresholdUSD + " (" + _medThresholdPercent + ")" +
                       ", high=" + _highThresholdUSD + " (" + _highThresholdPercent + ")" +
                       ", max="  + _maxTotalOrderValueUSD ); 
        }
    }
    
    @Override
    public Alert handleNOSGetAlerts( Order order, NewOrderSingle msg ) throws StateException {

        final int    qty      = msg.getOrderQty();
        final double valueUSD = msg.getPrice() * qty * msg.getCurrency().toUSDFactor();
     
        ClientAlertLimitBreachImpl alert = null;
        
        if ( valueUSD > _maxSingleOrderValueUSD ) {
            throwValidationStateException( MAX_SINGLE_ORDER_VAL_EXCEEDED, (long)valueUSD, (long)_maxSingleOrderValueUSD, msg.getClOrdId() );
        }
        
        if ( qty > _maxSingleOrderQty ) {
            throwValidationStateException( MAX_SINGLE_ORDER_QTY_EXCEEDED, qty, _maxSingleOrderQty, msg.getClOrdId() );
        }
        
        final double newTotVal = _totalValueUSD + valueUSD;
        final long   newQty    = _totalQty      + qty;
        
        if ( newQty < _maxTotalQty ) {
            if ( newTotVal < _lowThresholdUSD ) {
                _totalValueUSD = newTotVal;
                _totalQty      = newQty;
            } else if ( newTotVal < _medThresholdUSD ){
                if ( _totalValueUSD < _lowThresholdUSD ) {
                    // dot spam alerts only alert when cross threshold
                    alert = makeAlert( LOW_THRESHOLD_BREACH, alert, msg, valueUSD, _totalValueUSD, newTotVal, _lowThresholdUSD, msg );
                }
                _totalValueUSD = newTotVal;
                _totalQty      = newQty;

            } else if ( newTotVal < _highThresholdUSD ){
                if ( _totalValueUSD < _medThresholdUSD ) {
                    // dot spam alerts only alert when cross threshold
                    alert = makeAlert( MEDIUM_THRESHOLD_ALERT, alert, msg, valueUSD, _totalValueUSD, newTotVal, _medThresholdUSD, msg );
                }
                _totalValueUSD = newTotVal;
                _totalQty      = newQty;
            } else {
                if ( newTotVal > _maxTotalOrderValueUSD ) {
                    throwValidationStateException( MAX_TOTAL_ORDER_VAL_EXCEEDED, (long)newTotVal, (long)_maxTotalOrderValueUSD, msg.getClOrdId() );
                }
                if ( _totalValueUSD < _highThresholdUSD ) {
                    alert = makeAlert( HIGH_THRESHOLD_ALERT, alert, msg, valueUSD, _totalValueUSD, newTotVal, _highThresholdUSD, msg );
                }
                _totalValueUSD = newTotVal;
                _totalQty      = newQty;
            }
        } else{
            throwValidationStateException( MAX_TOTAL_ORDER_QTY_EXCEEDED, newQty, _maxTotalQty, msg.getClOrdId() );
        }
        
        return alert;
    }

    private void throwValidationStateException( ZString baseMsg, long val, long max, ZString clOrdId ) throws ValidationStateException {

        ReusableString msg = TLC.instance().pop();

        msg.setValue( baseMsg );
        msg.append( Strings.DELIM ).append( Strings.CL_ORD_ID ).append( clOrdId );
        msg.append( Strings.DELIM ).append( Strings.VAL ).append( val );
        msg.append( Strings.DELIM ).append( Strings.MAX  ).append( max );
        
        ValidationStateException e = new ValidationStateException( msg );
        TLC.instance().pushback( msg );
        
        throw e;
    }

    private ClientAlertLimitBreachImpl makeAlert( ZString                    breachMsg, 
                                                  ClientAlertLimitBreachImpl alert, 
                                                  OrderRequest               req, 
                                                  double                     ordValUSD, 
                                                  double                     prevTotUSD, 
                                                  double                     newTotUSD,
                                                  double                     limit, 
                                                  OrderRequest               srcEvent ) {
        
        ClientAlertLimitBreachImpl newAlert = _alertFactory.get();

        newAlert.setSrcEvent( srcEvent );
        
        String sessName = "unknown";
        
        MessageHandler srcHandler = srcEvent.getMessageHandler();
        
        if ( srcHandler != null ) {
            if ( srcHandler instanceof Session ) {
                sessName = ((Session)srcHandler).getComponentId();
            }
        }
        
        ReusableString text = newAlert.getTextForUpdate();
        text.setValue( breachMsg );
        text.append( CLIENT ).append( _client ).append( Strings.DELIM );
        text.append( SESSION ).append( sessName ).append( Strings.DELIM );
        text.append( ORD_VAL_USD ).append( ordValUSD ).append( Strings.DELIM );
        text.append( PREV_VAL_USD ).append( prevTotUSD ).append( Strings.DELIM );
        text.append( NEW_VAL_USD ).append( newTotUSD ).append( Strings.DELIM );
        text.append( LIMIT_USD ).append( limit );
        newAlert.attachQueue( alert );
        
        return newAlert;
    }

    @Override
    public void handleNewOrderSingle( Order order, NewOrderSingle msg ) throws StateException {
        throw new RuntimeException( "Use handleNOSGetAlerts as handleNewOrderSingle not appropriate" );
    }

    @Override
    public Alert handleAmendGetAlerts( Order order, CancelReplaceRequest msg ) throws StateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handleCancelReplaceRequest( Order order, CancelReplaceRequest msg ) throws StateException {
        throw new RuntimeException( "Use handleAmendGetAlerts as handleCancelReplaceRequest not appropriate" );
    }

    @Override
    public void handleCancelReject( Order order, CancelReject mktReject ) throws StateException {

        final OrdStatus exchangeOrdStatus = mktReject.getOrdStatus();

        if ( exchangeOrdStatus.getIsTerminal() ) {
            if ( order.getPendingVersion().getOrdStatus() == OrdStatus.PendingCancel ) {
                handleCancelReject( order );
            }
        }
    }

    private void handleCancelReject( Order order ) {

        // cancel failed BUT the exchange believes the order is terminal
        // note order is in pending state ... generally would expect exchange to send some unsol cancel
        // message and already be in terminal state
        
        final OrderVersion lastAcked = order.getLastAckedVerion();
        final OrderRequest req       = (OrderRequest) lastAcked.getBaseOrderRequest();
        
        final int cancelQty          = req.getOrderQty() - lastAcked.getCumQty();
        
        if ( cancelQty > 0 ) {
            final double valueUSD = req.getPrice() * cancelQty * req.getCurrency().toUSDFactor();
         
            final double newTotVal = _totalValueUSD - valueUSD;
            final long   newQty    = _totalQty      - cancelQty;
            
            if ( newQty >= 0 && newTotVal >= 0 ) {
                _totalValueUSD = newTotVal;
                _totalQty      = newQty;
            } else {
                _totalQty = 0;
                _totalValueUSD = 0;
            }
        }
    }

    @Override
    public void handleVagueReject( Order order, VagueOrderReject msg ) throws StateException {

        final OrderVersion pending = order.getPendingVersion();

        if ( pending.getOrdStatus() == OrdStatus.PendingCancel || pending.getOrdStatus() == OrdStatus.PendingReplace ) {
            if ( msg.getIsTerminal() ) {
                handleCancelReject( order );
            }
        } else if ( pending.getOrdStatus() == OrdStatus.PendingNew ) {
            handleRejected( order, null );            
        }
    }

    @Override
    public void handleCancelRequest( Order order, CancelRequest msg ) throws StateException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleCancelled( Order order, Cancelled msg ) throws StateException {
        orderTerminated( order );
    }

    @Override
    public void handleDoneForDay( Order order, DoneForDay msg ) throws StateException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleExpired( Order order, Expired msg ) throws StateException {
        orderTerminated( order );
    }

    @Override
    public void handleNewOrderAck( Order order, NewOrderAck msg ) throws StateException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleOrderStatus( Order order, OrderStatus msg ) throws StateException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleRejected( Order order, Rejected msg ) throws StateException {
         
        final OrderVersion lastAcked = order.getLastAckedVerion();
        final OrderRequest req       = (OrderRequest) lastAcked.getBaseOrderRequest();
        
        final int cancelQty          = req.getOrderQty() - lastAcked.getCumQty();
        
        if ( cancelQty > 0 ) {
            final double valueUSD = req.getPrice() * cancelQty * req.getCurrency().toUSDFactor();
         
            final double newTotVal = _totalValueUSD - valueUSD;
            final long   newQty    = _totalQty      - cancelQty;
            
            if ( newQty >= 0 && newTotVal >= 0 ) {
                _totalValueUSD = newTotVal;
                _totalQty      = newQty;
            } else {
                _totalQty = 0;
                _totalValueUSD = 0;
            }
        }
    }

    /**
     * eg QTY UP / px up
     * 
     *    NOS  qty=100, px=15    risk=1500
     *    FILL qty=15,  px=14    risk=1485
     *    REP  qty=135, px=16    risk change = ((135-100)-15) * (16-15) = 20
     *    
     * eg QTY UP / px down
     * 
     *    NOS  qty=100, px=15    risk=1500
     *    FILL qty=15,  px=14    risk=1485
     *    REP  qty=135, px=13    risk change = ((135-100)-15) * (13-15) = -40
     *    
     * eg QTY DOWN / px up
     * 
     *    NOS  qty=100, px=15    risk=1500
     *    FILL qty=15,  px=14    risk=1485
     *    REP  qty=70,  px=17    risk change = ((70-15) * (17-15) + (70-100) * 15 = 110 - 450 = -340     (tot = 1145)
     *    
     * eg QTY DOWN / px down
     * 
     *    NOS  qty=100, px=15    risk=1500
     *    FILL qty=15,  px=14    risk=1485
     *    REP  qty=70,  px=13    risk change = ((70-15) * (13-15) + (70-100) * 15 = -110 - 450 = -560
     *    
     */
    @Override
    public void handleReplaced( Order order, Replaced rep ) throws StateException {
        final OrderVersion lastAcked   = order.getLastAckedVerion();
        final OrderVersion replacedVer = order.getPendingVersion();
        final OrderRequest origReq     = (OrderRequest) lastAcked.getBaseOrderRequest();
        final OrderRequest replaceReq  = (OrderRequest) replacedVer.getBaseOrderRequest();
        
        // dont use the market side fields which were NOT decoded as on client ack they 
        // are delegated to src request
        
        final int    repQty    = replacedVer.getOrderQty();                     
        final int    origQty   = origReq.getOrderQty();                 
        final double repPx     = replacedVer.getMarketPrice();                     
        final double origPx    = lastAcked.getMarketPrice();                 
        final int    cumQty    = lastAcked.getCumQty();
        final int    qtyChange = repQty - origQty;
        
        double adjustAmt;

        if ( qtyChange >= 0 ) {                                      // amend qty UP
            adjustAmt = ((qtyChange-cumQty) * (repPx-origPx));
        } else {                                                    // amend qty DOWN
            adjustAmt = ((repQty-cumQty) * (repPx-origPx)) + (qtyChange * origPx);
        }
        
        final double valueUSD  = adjustAmt * replaceReq.getInstrument().getCurrency().toUSDFactor();
     
        final double newTotVal = _totalValueUSD + valueUSD;
        final long   newQty    = _totalQty      + qtyChange;
        
        if ( newQty >= 0 && newTotVal >= 0 ) {
            _totalValueUSD = newTotVal;
            _totalQty      = newQty;
        } else {
            _totalQty = 0;
            _totalValueUSD = 0;
        }
    }

    @Override
    public void handleRestated( Order order, Restated msg ) throws StateException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleStopped( Order order, Stopped msg ) throws StateException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void handleSuspended( Order order, Suspended msg ) throws StateException {
        // TODO Auto-generated method stub
        
    }

    /**
     * cancel a trade 
     */
    @Override
    public void handleTradeCancel( Order order, TradeCancel msg ) throws StateException {
        
        final OrderVersion lastAcc = order.getLastAckedVerion();
        final double    usdFactor  = lastAcc.getBaseOrderRequest().getInstrument().getCurrency().toUSDFactor();
        final OrdStatus ordStatus  = lastAcc.getOrdStatus();

        final int    repQty    = msg.getLastQty();                     
        final double origPx    = msg.getLastPx();                 
        
        double adjustAmt;
        double valueUSD;  
        
        if ( ordStatus.getIsTerminal() && ordStatus != OrdStatus.Filled ) { // cancel amount wont be retraded

            adjustAmt  = -repQty * origPx;
            _totalQty -= repQty;
            
            if ( _totalQty < 0 ) _totalQty = 0;
            
        } else { // cancel amount is now open
            
            final double repPx     = lastAcc.getMarketPrice();

            adjustAmt = (repQty * (repPx-origPx));
        }

        valueUSD  = adjustAmt * usdFactor;
        
        final double newTotVal = _totalValueUSD + valueUSD;
        
        if ( newTotVal >= 0 ) {
            _totalValueUSD = newTotVal;
        } else {
            _totalValueUSD = 0;
        }
    }

    @Override
    public void handleTradeCorrect( Order order, TradeCorrect msg ) throws StateException {
        final OrderVersion lastAcc = order.getLastAckedVerion();
        final double    usdFactor  = lastAcc.getBaseOrderRequest().getInstrument().getCurrency().toUSDFactor();
        final OrdStatus ordStatus  = order.getLastAckedVerion().getOrdStatus();

        final double srcPx     = lastAcc.getMarketPrice();
        
        final int    repQty    = msg.getLastQty();                     
        final int    origQty   = msg.getOrigQty();                 
        final double repPx     = msg.getLastPx();                     
        final double origPx    = msg.getOrigPx();                 
        final int    qtyChange = repQty - origQty;
        
        double adjustAmt;
        double valueUSD;  
        
        if ( ordStatus.getIsTerminal() && ordStatus != OrdStatus.Filled ) { 
            // order is terminal so the open aspect of order already removed from totals

            // 10 * 24 => 5 * 22
            // should be 90 * 25.12 + 5 * 22
            
            // oldTotal = 90 * 25.12 + 10 * 24
            // newTotal = oldTotal + 5 * 22 + 10 * 24

            adjustAmt = (repQty * repPx) - (origQty*origPx);
            
            _totalQty += qtyChange;
            
            if ( _totalQty < 0 ) _totalQty = 0;
            
        } else { // any trade down qty moves to open
            
            if ( qtyChange >= 0 ) {
                // eg srcPx=25.12, origTrade=10*24.0, corr=15*22.0
                // orig=240, new=330, but the qtyChange aleady included in total with srcPx
                // orig trade total       = 25.12 * 90 + 10 * 24 = 2260.8 + 240 = 2500.8
                // new total should equal = 25.12 * 85 + 15 * 22 = 2135.2 + 330 = 2465.2
                
                // adjust = -35.6

                // 10 * (22-24)       = -20     against alreaady traded component
                // - (5 * (25.12-22)) = -15.6   adjust extra qty against src px

                adjustAmt = (origQty * (repPx-origPx)); 
                adjustAmt -= qtyChange * (srcPx-repPx);                         
                
            } else {
                // eg srcPx=25.12, origTrade=10*24.0, corr=8*26.0
                // orig=240, new=108, but the qtyChange aleady included in total with srcPx
                // orig trade total       = 25.12 * 90 + 10 * 24 = 2260.8  + 240 = 2500.8
                // new total should equal = 25.12 * 92 + 8  * 26 = 2311.04 + 208 = 2519.04
                
                // adjust = 18.24

                // 8 * (26-24)        = 16      against alreaady traded component
                // (-2 * (25.12-24.0)) = 2.24   adjust extra qty against src px

                adjustAmt = origQty * (srcPx-origPx); // move trade fully back into open 
                adjustAmt -= (repQty * (srcPx-repPx));         // now adjust down 
            }
        }

        valueUSD  = adjustAmt * usdFactor;
        
        final double newTotVal = _totalValueUSD + valueUSD;
        
        if ( newTotVal >= 0 ) {
            _totalValueUSD = newTotVal;
        } else {
            _totalValueUSD = 0;
            _totalQty = 0;
        }
    }

    @Override
    public void handleTradeNew( Order order, TradeNew msg ) throws StateException {

        final OrderVersion lastAcked = order.getLastAckedVerion();
        final OrderRequest req       = (OrderRequest) lastAcked.getBaseOrderRequest();

        if ( lastAcked.getOrdStatus().getIsTerminal() ) {
         
            // late fill, apply fully to limits
            
            final int    lastQty  = msg.getLastQty();
            final double valueUSD = msg.getLastPx() * lastQty * req.getInstrument().getCurrency().toUSDFactor();

            _totalQty      += lastQty;
            _totalValueUSD += valueUSD;
            
        } else {
            final double adjustAmt   = msg.getLastPx() - lastAcked.getMarketPrice();
            final double valueUSD    = adjustAmt * msg.getLastQty() * req.getInstrument().getCurrency().toUSDFactor();
            final double newTotVal   = _totalValueUSD + valueUSD;
                
            if ( newTotVal >= 0 ) {
                _totalValueUSD = newTotVal;
            } else {
                _totalValueUSD = 0;
            }
        }
    }

    @Override
    public long getTotalOrderQty() {
        return _totalQty;
    }

    @Override
    public double getTotalOrderValueUSD() {
        return _totalValueUSD;
    }

    @Override
    public boolean isSendClientLateFills() {
        return _sendClientLateFills ;
    }

    @Override
    public boolean setSendClientLateFills( boolean sendClientLateFills ) {
        boolean oldVal = _sendClientLateFills;
        
        _sendClientLateFills = sendClientLateFills;
        
        return oldVal;
    }

    @Override
    public void id( ReusableString out ) {
        out.append( _client );
    }

    private void orderTerminated( Order order ) {
        final OrderVersion lastAcked = order.getLastAckedVerion();
        final OrderRequest req       = (OrderRequest) lastAcked.getBaseOrderRequest();
        
        final int cancelQty          = req.getOrderQty() - lastAcked.getCumQty();
        
        if ( cancelQty > 0 ) {
            final double valueUSD = req.getPrice() * cancelQty * req.getCurrency().toUSDFactor();
         
            final double newTotVal = _totalValueUSD - valueUSD;
            final long   newQty    = _totalQty      - cancelQty;
            
            if ( newQty >= 0 && newTotVal >= 0 ) {
                _totalValueUSD = newTotVal;
                _totalQty      = newQty;
            } else {
                _totalQty = 0;
                _totalValueUSD = 0;
            }
        }
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void postConstruction() {
        recalcThreshold();
    }
}
