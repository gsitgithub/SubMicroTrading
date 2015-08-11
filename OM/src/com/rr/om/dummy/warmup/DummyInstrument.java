/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.dummy.warmup;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Currency;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.model.LegInstrument;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.SecurityType;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.model.instrument.FixedTickSize;
import com.rr.om.model.instrument.InstrumentWrite;
import com.rr.om.model.instrument.PriceToleranceLimits;
import com.rr.om.model.instrument.TickType;
import com.rr.om.model.instrument.TradingRange;

public class DummyInstrument implements InstrumentWrite {

    public static final DummyInstrument DUMMY = new DummyInstrument( new ViewString("DummyId"), DummyExchange.DUMMY, Currency.Other, true, Instrument.DUMMY_INSTRUMENT_ID );

    private static final ViewString ID_START = new ViewString( "[ric=" );
    private static final ViewString ID_END   = new ViewString( "]" );
    
    private final ReusableString _ric;
    private final ReusableString _exchangeSym;
    private final long           _longSymbol;
    private       int            _intSegment;
    private       Currency       _ccy;
    private       Exchange       _exchange;
    private final ZString        _country       = new ViewString( "GB" );
    private final TradingRange   _dummyBand;
    private       TickType       _tickType      = new FixedTickSize( 0.000001 );
    private       boolean        _isRestricted  = false;
    private       boolean        _enabled       = true;
    private       int            _bookLevels    = 0;

    private final boolean        _testSymbol;

    public DummyInstrument( ZString securityId, Exchange exchange, Currency currency ) {
        this( securityId, exchange, currency, false, encodeSymbolToId(securityId) );
    }
    
    public DummyInstrument( ZString securityId, Exchange exchange, Currency currency, boolean isTestSymbol, int id ) {
        _ric         = new ReusableString( securityId );
        _exchangeSym = new ReusableString( securityId );
        
        _ccy        = currency;
        _exchange   = exchange;
        _dummyBand  = new TradingRangeImpl();
        _testSymbol = isTestSymbol;
        _longSymbol  = id;
    }

    public static int encodeSymbolToId( ZString securityId ) {
        
        int id = 0;
        
        try {
            id = Integer.parseInt( securityId.toString() );
        } catch( NumberFormatException e ) {
            byte[] idBytes = securityId.getBytes();
            
            int offset = securityId.getOffset();
            int max = offset + 4;
            
            for( int i=offset ; i < max ; i++ )  {
                byte b = idBytes[ i ];
                if ( b == '.' ) {
                    break;
                }
                id = (id << 8) + (0xFF & b);
            }
        }
        
        return id;
    }

    @Override
    public Currency getCurrency() {
        return _ccy;
    }

    @Override
    public Exchange getExchange() {
        return _exchange;
    }

    @Override
    public ViewString getRIC() {
        return _ric;
    }

    @Override
    public void setExchange( Exchange ex ) {
        _exchange = ex;
    }

    @Override
    public ZString getCountry() {
        return        _country ;
    }

    @Override
    public ZString getCusip() {
        return null;
    }

    @Override
    public ZString getExchangeRef() {
        return _ric;
    }

    @Override
    public ZString getIsin() {
        return null;
    }

    @Override
    public long getLotSize() {
        return 0;
    }

    @Override
    public ZString getMarket() {
        return null;
    }

    @Override
    public ZString getMarketSector() {
        return null;
    }

    @Override
    public ZString getMarketSegment() {
        return null;
    }

    @Override
    public int getIntSegment() {
        return _intSegment;
    }

    public void setIntSegment( int intSegment ) {
        _intSegment = intSegment;
    }

    @Override
    public PriceToleranceLimits getPriceToleranceLimits() {
        return null;
    }

    @Override
    public ZString getPrimaryRIC() {
        return null;
    }

    @Override
    public ZString getRic() {
        return _ric;
    }

    @Override
    public ZString getSecurityID( SecurityIDSource idsource ) {
        
        if ( SecurityIDSource.RIC == idsource ) {
            return _ric;
        }
        
        return null;
    }

    @Override
    public SecurityType getSecurityType() {
        return SecurityType.Cash;
    }

    @Override
    public ZString getSedol() {
        return null;
    }

    @Override
    public Currency getSettlementCurrency() {
        return _ccy;
    }

    @Override
    public ZString getSymbol() {
        return _ric;
    }

    @Override
    public TickType getTickscale() {
        return       _tickType;
    }

    @Override
    public boolean isPrimaryInstrument() {
        return false;
    }

    @Override
    public boolean isRestrictedStock() {
        return false;
    }

    @Override
    public ExchangeSession getExchangeSession() {
        return _exchange.getExchangeSession( getMarketSegment() );
    }

    @Override
    public boolean isTestInstrument() {
        return _testSymbol;
    }

    @Override
    public TradingRange getValidTradingRange() {
        return   _dummyBand;
    }

    @Override
    public boolean isEnabled() {
        return _enabled;
    }

    @Override
    public boolean isRestricted() {
        return _isRestricted;
    }

    @Override
    public long getLongSymbol() {
        return _longSymbol;
    }

    @Override
    public void setCurrency( Currency ccy ) {
        _ccy = ccy;
    }
    
    @Override
    public void setTickType( TickType t ) {
        _tickType = t;
    }

    @Override
    public void setEnabled( boolean isEnabled ) {
        _enabled = isEnabled;
    }

    @Override
    public void setRestricted( boolean isRestricted ) {
        _isRestricted = isRestricted;
    }

    @Override
    public void id( ReusableString out ) {
        out.append( ID_START ).append( _ric ).append( ID_END );
    }

    @Override
    public int getBookLevels() {
        return _bookLevels;
    }

    @Override
    public void setBookLevels( int bookLevels ) {
        _bookLevels = bookLevels;
    }

    @Override
    public ViewString getExchangeSymbol() {
        return _exchangeSym;
    }

    @Override
    public ZString getSecurityDesc() {
        return _ric;
    }

    @Override
    public ZString getSecurityGroup() {
        return _exchangeSym;
    }

    @Override
    public int getSecurityGroupId() {
        return 0;
    }

    @Override
    public int getNumLegs() {
        return 0;
    }

    @Override
    public LegInstrument getLeg( int idx ) {
        return null;
    }

    @Override
    public int getContractMultiplier() {
        return 1;
    }

    @Override 
    public final int getMinQty() { 
        return 1; 
    }

    @Override
    public ZString getLegSecurityDesc( int legIdx, ReusableString secDef ) {
        secDef.reset();
        return secDef;
    }
}
