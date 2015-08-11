/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.validate;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.type.BookingType;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.om.Strings;
import com.rr.om.exchange.OMExchangeValidator;
import com.rr.om.model.instrument.TickType;
import com.rr.om.model.instrument.TradingRange;
import com.rr.om.order.Order;
import com.rr.om.order.OrderVersion;

public class EmeaDmaValidator implements EventValidator {

    private static final Logger       _log = LoggerFactory.create( EmeaDmaValidator.class );
    
    private static final ZString MISSING_CLORDID          = new ViewString( "Missing clOrdId " );
    private static final ZString UNSUPPORTED              = new ViewString( "Unsupported attribute value " );
    private static final ZString UNABLE_TO_CHANGE_CCY     = new ViewString( "Unable to change the currency " );
    private static final ZString UNABLE_TO_CHANGE_ORDTYPE = new ViewString( "Unable to change the order type " );
    private static final ZString UNABLE_TO_CHANGE_SIDE    = new ViewString( "Unable to change the side " );
    private static final ZString UNABLE_TO_CHANGE_SYM     = new ViewString( "Unable to change the symbol " );
    private static final ZString CANNOT_AMEND_BELOW_CQTY  = new ViewString( "Cannot amend qty below cumQty, qty=" );
    private static final ZString MAJOR_FIELDS_UNCHANGED   = new ViewString( "At least one of Qty/Price/TIF must change on an amend" );
    private static final ZString REQ_TOO_OLD              = new ViewString( "Request is older than max allowed seconds " );
    private static final ZString EXDEST_MISMATCH          = new ViewString( "EXDEST doesnt match the instrument REC, received " );
    private static final ZString SECEX_MISMATCH           = new ViewString( "Security Exchange doesnt match the instrument REC, received " );
    private static final ZString INVALID_PRICE            = new ViewString( "Failed tick validation " );
    private static final ZString MISSING_TICK             = new ViewString( "Missing tick type for instrument, RIC=" );
    private static final ZString RESTRICTED               = new ViewString( "Cant trade restricted stock, bookingType=" );
    private static final ZString INSTRUMENT_DISABLED      = new ViewString( "Instrument is disabled, RIC=" );

    private final ReusableString _err             = new ReusableString(256);
    private final int            _maxAgeMS;

    public EmeaDmaValidator( int maxAge ) {
        _maxAgeMS = maxAge;
    }
    
    /**
     * dont do validation that the exchange validator is doing
     */
    @Override
    public boolean validate( NewOrderSingle msg, Order order ) {
        reset();

        final Instrument inst = msg.getInstrument();
        final Exchange   ex   = inst.getExchange();

        if  ( ! inst.isTestInstrument() ) { // dont validate test instruments at ALL
            final long now = System.currentTimeMillis();

            commonValidation( ex, msg, order, now );
            
            final OMExchangeValidator exchangeValidator = (OMExchangeValidator) ex.getExchangeEventValidator();

            // qty validation done in exchangevalidator 
            
            if ( exchangeValidator != null )  exchangeValidator.validate( msg, _err, now );
        }
        
        return _err.length() == 0;
    }

    @Override
    public boolean validate( CancelReplaceRequest newVersion, Order order ) {
        reset();

        final OrderVersion lastAcc  = order.getLastAckedVerion();
        final OrderRequest previous = (OrderRequest) lastAcc.getBaseOrderRequest(); 
        final int          cumQty   = order.getLastAckedVerion().getCumQty();
        
        final Instrument inst = newVersion.getInstrument();
        final Exchange   ex   = inst.getExchange();

        if  ( ! inst.isTestInstrument() ) { // dont validate test instruments at ALL
            final long now       = System.currentTimeMillis();
            final int  newOrdQty = newVersion.getOrderQty();
            
            commonValidation( ex, newVersion, order, now );

            if ( newVersion.getCurrency() != previous.getCurrency() ) {               // CANT CHANGE CCY
                delim().append( UNABLE_TO_CHANGE_CCY ).append( Strings.FROM ).append( previous.getCurrency().toString() )
                       .append( Strings.TO ).append( newVersion.getCurrency().toString() );
            }

            if ( newVersion.getOrdType() !=  previous.getOrdType() ) {
                delim().append( UNABLE_TO_CHANGE_ORDTYPE ).append( Strings.FROM ).append( previous.getOrdType().toString() )
                       .append( Strings.TO ).append( newVersion.getOrdType().toString() );
            }
            
            if ( newVersion.getSide() !=  previous.getSide() ) {
                delim().append( UNABLE_TO_CHANGE_SIDE ).append( Strings.FROM ).append( previous.getSide().toString() )
                       .append( Strings.TO ).append( newVersion.getSide().toString() );
            }
            
            if ( ! newVersion.getSymbol().equals( previous.getSymbol() ) ) {
                delim().append( UNABLE_TO_CHANGE_SYM ).append( Strings.FROM ).append( previous.getSymbol() )
                       .append( Strings.TO ).append( newVersion.getSymbol() );
            }

            if ( newVersion.getPrice()       == previous.getPrice()      && 
                 newOrdQty                   == previous.getOrderQty()   && 
                 newVersion.getTimeInForce() == previous.getTimeInForce() ) {
                delim().append( MAJOR_FIELDS_UNCHANGED );
            }
            
            if ( newOrdQty < cumQty ) {
                delim().append( CANNOT_AMEND_BELOW_CQTY ).append( newOrdQty ).append( Strings.CUMQTY ).append( cumQty );
            }
             
            final OMExchangeValidator exchangeValidator = (OMExchangeValidator) ex.getExchangeEventValidator();
            if ( exchangeValidator != null ) {
                exchangeValidator.validate( newVersion, _err, now );
            }
        }
        
        return _err.length() == 0;
    }

    @Override
    public CxlRejReason getReplaceRejectReason() {
        return CxlRejReason.Other;
    }

    @Override
    public OrdRejReason getOrdRejectReason() {
        return OrdRejReason.UnsupOrdCharacteristic;
    }

    @Override
    public ViewString getRejectReason() {
        return _err;
    }

    private ReusableString delim() {
        if ( _err.length() > 0 ) {
            _err.append( Strings.DELIM );
        }
        
        return _err;
    }

    private void addErrorUnsupported( Enum<?> val ) {

        delim().append( UNSUPPORTED ).append( val.toString() ).append( Strings.TYPE ).append( val.getClass().getSimpleName() );
    }

    private final void reset() {
        _err.reset();
    }

    private void validateHandlingInstruction( HandlInst handlInst ) {
        if ( handlInst == HandlInst.ManualBestExec ) {
            addErrorUnsupported( handlInst );
        }
    }

    private void validateAge( int transactTime, long now ) {
        if ( (TimeZoneCalculator.instance().getTimeUTC(now) - transactTime) > _maxAgeMS ) {
            delim().append( REQ_TOO_OLD ).append( _maxAgeMS / 1000 );
        }
    }

    private void commonValidation( Exchange ex, OrderRequest req, Order order, long now ) {
        if ( req.getClOrdId().length() == 0 ) addError( MISSING_CLORDID );

        validateHandlingInstruction( req.getHandlInst() );
        validateAge( req.getTransactTime(), now );

        Instrument inst = req.getInstrument();
        
        final ViewString exDest = req.getExDest();
        final ViewString secEx  = req.getSecurityExchange();
        final double     price  = order.getPendingVersion().getMarketPrice();
        
        if ( exDest.length() > 0 && ! ex.getRecCode().equals( exDest ) ) {
            delim().append( EXDEST_MISMATCH ).append( exDest ).append( Strings.EXPECTED ).append( ex.getRecCode() );
        }

        if ( secEx.length() > 0 && ! ex.getRecCode().equals( secEx ) ) {
            delim().append( SECEX_MISMATCH ).append( secEx ).append( Strings.EXPECTED ).append( ex.getRecCode() );
        }
        
        validateTicksize( inst, price );
        
        if ( inst.isRestricted() && ! canTradeRestricted( req.getClient(), req.getBookingType(), req.getOrderCapacity() ) ) {
            delim().append( RESTRICTED ).append( req.getBookingType() ).append( Strings.ORDCAP )
                   .append( req.getOrderCapacity() );
        }
        
        if ( ! inst.isEnabled() ) {
            delim().append( INSTRUMENT_DISABLED ).append( inst.getRIC() );
        }
        
        TradingRange band = inst.getValidTradingRange();
        
        band.valid( price, req.getSide().getIsBuySide(), _err );
    }
    
    private boolean canTradeRestricted( ClientProfile client, BookingType bookingType, OrderCapacity orderCapacity ) {
        // TODO  THIS will be client specific
        
        return false;
    }

    private void addError( ZString msg ) {
        delim().append( msg );
    }
    
    private void validateTicksize( Instrument instrument, double price ) {
        TickType ts = instrument.getTickscale();

        if ( ts.canVerifyPrice() ) {
            if ( ! ts.isValid( price ) ) {
    
                delim().append( INVALID_PRICE );
                
                ts.writeError( price, _err );
            }
        } else {
            ReusableString msg = TLC.instance().pop();
            msg.append( MISSING_TICK ).append( instrument.getRIC() );
            _log.warn( msg );
            TLC.instance().pushback( msg );
        }
    }
}
