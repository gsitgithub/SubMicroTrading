/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.SecurityTradingStatus;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.SecurityType;
import com.rr.model.generated.internal.events.interfaces.SecDefEvents;
import com.rr.model.generated.internal.type.SecurityUpdateAction;
import com.rr.model.generated.internal.events.interfaces.SecDefLeg;
import com.rr.model.generated.internal.events.interfaces.SecurityAltID;
import com.rr.core.model.Currency;
import com.rr.model.generated.internal.events.interfaces.SDFeedType;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.Constants;
import com.rr.core.model.MsgFlag;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.Reusable;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.model.internal.type.*;
import com.rr.model.generated.internal.core.ModelReusableTypes;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.*;

@SuppressWarnings( "unused" )

public final class SecurityDefinitionUpdateImpl implements SecurityDefinition, SecurityDefinitionUpdateWrite, Reusable<SecurityDefinitionUpdateImpl> {

   // Attrs

    private          SecurityDefinitionUpdateImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _totNumReports = Constants.UNSET_INT;
    private long _securityID = Constants.UNSET_LONG;
    private final ReusableString _symbol = new ReusableString( SizeType.SYMBOL_LENGTH.getSize() );
    private int _noEvents = Constants.UNSET_INT;
    private int _noLegs = Constants.UNSET_INT;
    private double _tradingReferencePrice = Constants.UNSET_DOUBLE;
    private double _highLimitPx = Constants.UNSET_DOUBLE;
    private double _lowLimitPx = Constants.UNSET_DOUBLE;
    private double _minPriceIncrement = Constants.UNSET_DOUBLE;
    private double _minPriceIncrementAmount = Constants.UNSET_DOUBLE;
    private final ReusableString _securityGroup = new ReusableString( SizeType.INST_SEC_GRP_LEN.getSize() );
    private final ReusableString _securityDesc = new ReusableString( SizeType.INST_SEC_DESC_LENGTH.getSize() );
    private final ReusableString _CFICode = new ReusableString( SizeType.INST_CFI_CODE_LENGTH.getSize() );
    private final ReusableString _underlyingProduct = new ReusableString( SizeType.SYMBOL_LENGTH.getSize() );
    private final ReusableString _securityExchange = new ReusableString( SizeType.SECURITYEXCH_LENGTH.getSize() );
    private int _noSecurityAltID = Constants.UNSET_INT;
    private double _strikePrice = Constants.UNSET_DOUBLE;
    private long _minTradeVol = Constants.UNSET_LONG;
    private long _maxTradeVol = Constants.UNSET_LONG;
    private int _noSDFeedTypes = Constants.UNSET_INT;
    private long _maturityMonthYear = Constants.UNSET_LONG;
    private long _lastUpdateTime = Constants.UNSET_LONG;
    private final ReusableString _applID = new ReusableString( SizeType.INST_APPL_ID_LENGTH.getSize() );
    private double _displayFactor = Constants.UNSET_DOUBLE;
    private double _priceRatio = Constants.UNSET_DOUBLE;
    private int _contractMultiplierType = Constants.UNSET_INT;
    private int _contractMultiplier = 1;
    private int _openInterestQty = Constants.UNSET_INT;
    private int _tradingReferenceDate = Constants.UNSET_INT;
    private int _minQty = 1;
    private double _pricePrecision = Constants.UNSET_DOUBLE;
    private int _msgSeqNum = Constants.UNSET_INT;

    private SecurityTradingStatus _securityTradingStatus;
    private SecurityIDSource _securityIDSource = SecurityIDSource.ExchangeSymbol;
    private SecurityType _securityType;
    private SecDefEvents _events;
    private SecurityUpdateAction _securityUpdateAction;
    private SecDefLeg _legs;
    private SecurityAltID _securityAltIDs;
    private Currency _strikeCurrency;
    private Currency _currency;
    private Currency _settlCurrency;
    private SDFeedType _SDFeedTypes;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getTotNumReports() { return _totNumReports; }
    @Override public final void setTotNumReports( int val ) { _totNumReports = val; }

    @Override public final SecurityTradingStatus getSecurityTradingStatus() { return _securityTradingStatus; }
    @Override public final void setSecurityTradingStatus( SecurityTradingStatus val ) { _securityTradingStatus = val; }

    @Override public final SecurityIDSource getSecurityIDSource() { return _securityIDSource; }
    @Override public final void setSecurityIDSource( SecurityIDSource val ) { _securityIDSource = val; }

    @Override public final SecurityType getSecurityType() { return _securityType; }
    @Override public final void setSecurityType( SecurityType val ) { _securityType = val; }

    @Override public final long getSecurityID() { return _securityID; }
    @Override public final void setSecurityID( long val ) { _securityID = val; }

    @Override public final ViewString getSymbol() { return _symbol; }

    @Override public final void setSymbol( byte[] buf, int offset, int len ) { _symbol.setValue( buf, offset, len ); }
    @Override public final ReusableString getSymbolForUpdate() { return _symbol; }

    @Override public final int getNoEvents() { return _noEvents; }
    @Override public final void setNoEvents( int val ) { _noEvents = val; }

    @Override public final SecDefEvents getEvents() { return _events; }
    @Override public final void setEvents( SecDefEvents val ) { _events = val; }

    @Override public final SecurityUpdateAction getSecurityUpdateAction() { return _securityUpdateAction; }
    @Override public final void setSecurityUpdateAction( SecurityUpdateAction val ) { _securityUpdateAction = val; }

    @Override public final int getNoLegs() { return _noLegs; }
    @Override public final void setNoLegs( int val ) { _noLegs = val; }

    @Override public final SecDefLeg getLegs() { return _legs; }
    @Override public final void setLegs( SecDefLeg val ) { _legs = val; }

    @Override public final double getTradingReferencePrice() { return _tradingReferencePrice; }
    @Override public final void setTradingReferencePrice( double val ) { _tradingReferencePrice = val; }

    @Override public final double getHighLimitPx() { return _highLimitPx; }
    @Override public final void setHighLimitPx( double val ) { _highLimitPx = val; }

    @Override public final double getLowLimitPx() { return _lowLimitPx; }
    @Override public final void setLowLimitPx( double val ) { _lowLimitPx = val; }

    @Override public final double getMinPriceIncrement() { return _minPriceIncrement; }
    @Override public final void setMinPriceIncrement( double val ) { _minPriceIncrement = val; }

    @Override public final double getMinPriceIncrementAmount() { return _minPriceIncrementAmount; }
    @Override public final void setMinPriceIncrementAmount( double val ) { _minPriceIncrementAmount = val; }

    @Override public final ViewString getSecurityGroup() { return _securityGroup; }

    @Override public final void setSecurityGroup( byte[] buf, int offset, int len ) { _securityGroup.setValue( buf, offset, len ); }
    @Override public final ReusableString getSecurityGroupForUpdate() { return _securityGroup; }

    @Override public final ViewString getSecurityDesc() { return _securityDesc; }

    @Override public final void setSecurityDesc( byte[] buf, int offset, int len ) { _securityDesc.setValue( buf, offset, len ); }
    @Override public final ReusableString getSecurityDescForUpdate() { return _securityDesc; }

    @Override public final ViewString getCFICode() { return _CFICode; }

    @Override public final void setCFICode( byte[] buf, int offset, int len ) { _CFICode.setValue( buf, offset, len ); }
    @Override public final ReusableString getCFICodeForUpdate() { return _CFICode; }

    @Override public final ViewString getUnderlyingProduct() { return _underlyingProduct; }

    @Override public final void setUnderlyingProduct( byte[] buf, int offset, int len ) { _underlyingProduct.setValue( buf, offset, len ); }
    @Override public final ReusableString getUnderlyingProductForUpdate() { return _underlyingProduct; }

    @Override public final ViewString getSecurityExchange() { return _securityExchange; }

    @Override public final void setSecurityExchange( byte[] buf, int offset, int len ) { _securityExchange.setValue( buf, offset, len ); }
    @Override public final ReusableString getSecurityExchangeForUpdate() { return _securityExchange; }

    @Override public final int getNoSecurityAltID() { return _noSecurityAltID; }
    @Override public final void setNoSecurityAltID( int val ) { _noSecurityAltID = val; }

    @Override public final SecurityAltID getSecurityAltIDs() { return _securityAltIDs; }
    @Override public final void setSecurityAltIDs( SecurityAltID val ) { _securityAltIDs = val; }

    @Override public final double getStrikePrice() { return _strikePrice; }
    @Override public final void setStrikePrice( double val ) { _strikePrice = val; }

    @Override public final Currency getStrikeCurrency() { return _strikeCurrency; }
    @Override public final void setStrikeCurrency( Currency val ) { _strikeCurrency = val; }

    @Override public final Currency getCurrency() { return _currency; }
    @Override public final void setCurrency( Currency val ) { _currency = val; }

    @Override public final Currency getSettlCurrency() { return _settlCurrency; }
    @Override public final void setSettlCurrency( Currency val ) { _settlCurrency = val; }

    @Override public final long getMinTradeVol() { return _minTradeVol; }
    @Override public final void setMinTradeVol( long val ) { _minTradeVol = val; }

    @Override public final long getMaxTradeVol() { return _maxTradeVol; }
    @Override public final void setMaxTradeVol( long val ) { _maxTradeVol = val; }

    @Override public final int getNoSDFeedTypes() { return _noSDFeedTypes; }
    @Override public final void setNoSDFeedTypes( int val ) { _noSDFeedTypes = val; }

    @Override public final SDFeedType getSDFeedTypes() { return _SDFeedTypes; }
    @Override public final void setSDFeedTypes( SDFeedType val ) { _SDFeedTypes = val; }

    @Override public final long getMaturityMonthYear() { return _maturityMonthYear; }
    @Override public final void setMaturityMonthYear( long val ) { _maturityMonthYear = val; }

    @Override public final long getLastUpdateTime() { return _lastUpdateTime; }
    @Override public final void setLastUpdateTime( long val ) { _lastUpdateTime = val; }

    @Override public final ViewString getApplID() { return _applID; }

    @Override public final void setApplID( byte[] buf, int offset, int len ) { _applID.setValue( buf, offset, len ); }
    @Override public final ReusableString getApplIDForUpdate() { return _applID; }

    @Override public final double getDisplayFactor() { return _displayFactor; }
    @Override public final void setDisplayFactor( double val ) { _displayFactor = val; }

    @Override public final double getPriceRatio() { return _priceRatio; }
    @Override public final void setPriceRatio( double val ) { _priceRatio = val; }

    @Override public final int getContractMultiplierType() { return _contractMultiplierType; }
    @Override public final void setContractMultiplierType( int val ) { _contractMultiplierType = val; }

    @Override public final int getContractMultiplier() { return _contractMultiplier; }
    @Override public final void setContractMultiplier( int val ) { _contractMultiplier = val; }

    @Override public final int getOpenInterestQty() { return _openInterestQty; }
    @Override public final void setOpenInterestQty( int val ) { _openInterestQty = val; }

    @Override public final int getTradingReferenceDate() { return _tradingReferenceDate; }
    @Override public final void setTradingReferenceDate( int val ) { _tradingReferenceDate = val; }

    @Override public final int getMinQty() { return _minQty; }
    @Override public final void setMinQty( int val ) { _minQty = val; }

    @Override public final double getPricePrecision() { return _pricePrecision; }
    @Override public final void setPricePrecision( double val ) { _pricePrecision = val; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _totNumReports = Constants.UNSET_INT;
        _securityTradingStatus = null;
        _securityIDSource = SecurityIDSource.ExchangeSymbol;
        _securityType = null;
        _securityID = Constants.UNSET_LONG;
        _symbol.reset();
        _noEvents = Constants.UNSET_INT;
        _events = null;
        _securityUpdateAction = null;
        _noLegs = Constants.UNSET_INT;
        _legs = null;
        _tradingReferencePrice = Constants.UNSET_DOUBLE;
        _highLimitPx = Constants.UNSET_DOUBLE;
        _lowLimitPx = Constants.UNSET_DOUBLE;
        _minPriceIncrement = Constants.UNSET_DOUBLE;
        _minPriceIncrementAmount = Constants.UNSET_DOUBLE;
        _securityGroup.reset();
        _securityDesc.reset();
        _CFICode.reset();
        _underlyingProduct.reset();
        _securityExchange.reset();
        _noSecurityAltID = Constants.UNSET_INT;
        _securityAltIDs = null;
        _strikePrice = Constants.UNSET_DOUBLE;
        _strikeCurrency = null;
        _currency = null;
        _settlCurrency = null;
        _minTradeVol = Constants.UNSET_LONG;
        _maxTradeVol = Constants.UNSET_LONG;
        _noSDFeedTypes = Constants.UNSET_INT;
        _SDFeedTypes = null;
        _maturityMonthYear = Constants.UNSET_LONG;
        _lastUpdateTime = Constants.UNSET_LONG;
        _applID.reset();
        _displayFactor = Constants.UNSET_DOUBLE;
        _priceRatio = Constants.UNSET_DOUBLE;
        _contractMultiplierType = Constants.UNSET_INT;
        _contractMultiplier = 1;
        _openInterestQty = Constants.UNSET_INT;
        _tradingReferenceDate = Constants.UNSET_INT;
        _minQty = 1;
        _pricePrecision = Constants.UNSET_DOUBLE;
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.SecurityDefinitionUpdate;
    }

    @Override
    public final SecurityDefinitionUpdateImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( SecurityDefinitionUpdateImpl nxt ) {
        _next = nxt;
    }

    @Override
    public final void detachQueue() {
        _nextMessage = null;
    }

    @Override
    public final Message getNextQueueEntry() {
        return _nextMessage;
    }

    @Override
    public final void attachQueue( Message nxt ) {
        _nextMessage = nxt;
    }

    @Override
    public final MessageHandler getMessageHandler() {
        return _messageHandler;
    }

    @Override
    public final void setMessageHandler( MessageHandler handler ) {
        _messageHandler = handler;
    }


   // Helper methods
    @Override
    public void setFlag( MsgFlag flag, boolean isOn ) {
        _flags = (byte) MsgFlag.setFlag( _flags, flag, isOn );
    }

    @Override
    public boolean isFlagSet( MsgFlag flag ) {
        return MsgFlag.isOn( _flags, flag );
    }

    @Override
    public String toString() {
        ReusableString buf = new ReusableString();
        dump( buf );
        return buf.toString();
    }

    @Override
    public final void dump( ReusableString out ) {
        out.append( "SecurityDefinitionUpdateImpl" ).append( ' ' );
        out.append( ", totNumReports=" ).append( getTotNumReports() );
        out.append( ", securityTradingStatus=" ).append( getSecurityTradingStatus() );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", securityType=" );
        if ( getSecurityType() != null ) getSecurityType().id( out );
        out.append( ", securityID=" ).append( getSecurityID() );
        out.append( ", symbol=" ).append( getSymbol() );
        out.append( ", noEvents=" ).append( getNoEvents() );

        SecDefEventsImpl tPtrevents = (SecDefEventsImpl) getEvents();
        int tIdxevents=0;

        while( tPtrevents != null ) {
            out.append( " {#" ).append( ++tIdxevents ).append( "} " );
            tPtrevents.dump( out );
            tPtrevents = tPtrevents.getNext();
        }

        out.append( ", securityUpdateAction=" ).append( getSecurityUpdateAction() );
        out.append( ", noLegs=" ).append( getNoLegs() );

        SecDefLegImpl tPtrlegs = (SecDefLegImpl) getLegs();
        int tIdxlegs=0;

        while( tPtrlegs != null ) {
            out.append( " {#" ).append( ++tIdxlegs ).append( "} " );
            tPtrlegs.dump( out );
            tPtrlegs = tPtrlegs.getNext();
        }

        out.append( ", tradingReferencePrice=" ).append( getTradingReferencePrice() );
        out.append( ", highLimitPx=" ).append( getHighLimitPx() );
        out.append( ", lowLimitPx=" ).append( getLowLimitPx() );
        out.append( ", minPriceIncrement=" ).append( getMinPriceIncrement() );
        out.append( ", minPriceIncrementAmount=" ).append( getMinPriceIncrementAmount() );
        out.append( ", securityGroup=" ).append( getSecurityGroup() );
        out.append( ", securityDesc=" ).append( getSecurityDesc() );
        out.append( ", CFICode=" ).append( getCFICode() );
        out.append( ", underlyingProduct=" ).append( getUnderlyingProduct() );
        out.append( ", securityExchange=" ).append( getSecurityExchange() );
        out.append( ", noSecurityAltID=" ).append( getNoSecurityAltID() );

        SecurityAltIDImpl tPtrsecurityAltIDs = (SecurityAltIDImpl) getSecurityAltIDs();
        int tIdxsecurityAltIDs=0;

        while( tPtrsecurityAltIDs != null ) {
            out.append( " {#" ).append( ++tIdxsecurityAltIDs ).append( "} " );
            tPtrsecurityAltIDs.dump( out );
            tPtrsecurityAltIDs = tPtrsecurityAltIDs.getNext();
        }

        out.append( ", strikePrice=" ).append( getStrikePrice() );
        out.append( ", strikeCurrency=" );
        if ( getStrikeCurrency() != null ) getStrikeCurrency().id( out );
        out.append( ", currency=" );
        if ( getCurrency() != null ) getCurrency().id( out );
        out.append( ", settlCurrency=" );
        if ( getSettlCurrency() != null ) getSettlCurrency().id( out );
        out.append( ", minTradeVol=" ).append( getMinTradeVol() );
        out.append( ", maxTradeVol=" ).append( getMaxTradeVol() );
        out.append( ", noSDFeedTypes=" ).append( getNoSDFeedTypes() );

        SDFeedTypeImpl tPtrSDFeedTypes = (SDFeedTypeImpl) getSDFeedTypes();
        int tIdxSDFeedTypes=0;

        while( tPtrSDFeedTypes != null ) {
            out.append( " {#" ).append( ++tIdxSDFeedTypes ).append( "} " );
            tPtrSDFeedTypes.dump( out );
            tPtrSDFeedTypes = tPtrSDFeedTypes.getNext();
        }

        out.append( ", maturityMonthYear=" ).append( getMaturityMonthYear() );
        out.append( ", lastUpdateTime=" ).append( getLastUpdateTime() );
        out.append( ", applID=" ).append( getApplID() );
        out.append( ", displayFactor=" ).append( getDisplayFactor() );
        out.append( ", priceRatio=" ).append( getPriceRatio() );
        out.append( ", contractMultiplierType=" ).append( getContractMultiplierType() );
        out.append( ", contractMultiplier=" ).append( getContractMultiplier() );
        out.append( ", openInterestQty=" ).append( getOpenInterestQty() );
        out.append( ", tradingReferenceDate=" ).append( getTradingReferenceDate() );
        out.append( ", minQty=" ).append( getMinQty() );
        out.append( ", pricePrecision=" ).append( getPricePrecision() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
