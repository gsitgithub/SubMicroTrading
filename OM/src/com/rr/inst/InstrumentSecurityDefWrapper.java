/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Currency;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.model.LegInstrument;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.SecurityType;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.internal.events.impl.SecurityAltIDImpl;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.model.generated.internal.events.interfaces.SecDefLeg;
import com.rr.model.generated.internal.type.SecurityTradingStatus;
import com.rr.om.dummy.warmup.TradingRangeImpl;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.model.instrument.TickType;
import com.rr.om.model.instrument.TradingRange;
import com.rr.om.model.instrument.UnknownTickSize;


public final class InstrumentSecurityDefWrapper implements Instrument {

    private static final int SEGMENT_UNKNOWN = 0;
    
    private SecurityDefinitionImpl _secDef;
    private SecurityTradingStatus _securityTradingStatus = SecurityTradingStatus.Unknown;
    private Exchange _exchange;
    private ZString _cusip;
    private ZString _exSym;
    private ZString _isin;
    private ZString _ric;
    private ZString _sedol;

    private ReusableString _id = new ReusableString();
    
    private long _intId;
    private TickType _ts = new UnknownTickSize();
    private TradingRange _tr;
    private int _segment = SEGMENT_UNKNOWN;

    private final LegInstrument[] _legs;

    private int _secGrpId = 0;
    
    public InstrumentSecurityDefWrapper( Exchange exchange, SecurityDefinitionImpl secDef, int legCnt ) {
        _exchange = exchange;
        setSecurityDefinition( secDef );
        _tr = new TradingRangeImpl();
        _legs = (legCnt == 0) ? null : new LegInstrument[ legCnt ];
    }

    public SecurityDefinitionImpl getSecurityDefinition() {
        return _secDef;
    }

    @Override
    public String toString() {
        return _secDef.getSecurityDesc().toString();
    }
    
    public void setPlaceHolderDefinition( final SecDefLeg def ) {
        _cusip = _exSym = _isin = _ric = _sedol = null; 
        
        _intId = def.getLegSecurityID();
        
        _id.reset();
        _id.append( def.getLegSecurityID() );
        
        setIdBySrc( _id, def.getLegSecurityIDSource() );
    }

    public void setSecurityDefinition( final SecurityDefinitionImpl def ) {
        _secDef = def;
        _cusip = _exSym = _isin = _ric = _sedol = null; 
        
        if ( def == null ) return;
        
        if ( def.getSecurityTradingStatus() != null ) {
            _securityTradingStatus = def.getSecurityTradingStatus();
        }
        
        SecurityAltIDImpl securityAltIDs = (SecurityAltIDImpl) def.getSecurityAltIDs();
        
        while( securityAltIDs != null ) {
            
            final ZString id = securityAltIDs.getSecurityAltID();
            final SecurityIDSource idSrc = securityAltIDs.getSecurityAltIDSource();
            
            setIdBySrc( id, idSrc );
            
            securityAltIDs = securityAltIDs.getNext();
        }

        _intId = def.getSecurityID();
        
        _id.reset();
        _id.append( def.getSecurityID() );
        
        setIdBySrc( _id, def.getSecurityIDSource() );
        
        checkCFICode( def );
        
        try {
            _secGrpId = Integer.parseInt( def.getSecurityGroup().toString() );
        } catch( NumberFormatException e ) {
            _secGrpId = 0;
        }

        try {
            _segment = Integer.parseInt( def.getApplID().toString() );
        } catch( NumberFormatException e ) {
            _segment = SEGMENT_UNKNOWN;
        }
    }

    private void checkCFICode( SecurityDefinitionImpl def ) {
        SecurityType st = def.getSecurityType();
        
        if ( st == null ) {
            ZString cfi = def.getCFICode();
            
            if ( cfi != null ) {
                byte type = cfi.getByte( 0 );
                
                if ( type == 'F' ) {
                    def.setSecurityType( SecurityType.Future );
                } else if ( type == 'O' ) {
                    def.setSecurityType( SecurityType.Option );
                } 
            }
        }
    }

    private void setIdBySrc( final ZString id, final SecurityIDSource idSrc ) {
        if ( id.length() > 0 ) {
            switch( idSrc ) {
            case CUSIP:
                _cusip = id; 
                break;
            case ExchangeSymbol:
                _exSym = id; 
                break;
            case ISIN:
                _isin = id; 
                break;
            case RIC:
                _ric = id; 
                break;
            case SEDOL:
                _sedol = id; 
                break;
            case Belgian:
            case Bloomberg:
            case CTA:
            case ClearingHouse:
            case Common:
            case Dutch:
            case ISDA_FPML:
            case ISO_CcyCode:
            case ISO_CountryCode:
            case OptionsPRA:
            case QUIK:
            case Sicovam:
            case Unknown:
            case Valoren:
            case Wertpapier:
            default:
                break;
            }
        }
    }

    public void setSecurityTradingStatus( SecurityTradingStatus securityTradingStatus ) {
        _securityTradingStatus = securityTradingStatus;
    }

    public SecurityTradingStatus getSecurityTradingStatus() {
        return _securityTradingStatus;
    }

    @Override
    public Exchange getExchange() {
        return _exchange;
    }

    @Override
    public Currency getCurrency() {
        return _secDef.getCurrency();
    }

    @Override
    public ZString getRIC() {
        return _ric;
    }

    @Override
    public long getLongSymbol() {
        return _intId;
    }

    @Override
    public int getIntSegment() {
        return _segment;
    }

    @Override
    public ExchangeSession getExchangeSession() {
        return _exchange.getSession();
    }

    @Override
    public boolean isTestInstrument() {
        return false;
    }

    @Override
    public TickType getTickscale() {
        return _ts;
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public TradingRange getValidTradingRange() {
        return _tr;
    }

    @Override
    public void id( ReusableString out ) {
        out.append( _exSym );
    }

    @Override
    public int getBookLevels() {
        return 10;
    }

    @Override
    public ZString getExchangeSymbol() {
        return _exSym;
    }

    public SecurityDefinitionImpl getSecDef() {
        return _secDef;
    }
    
    public ZString getCusip() {
        return _cusip;
    }

    public ZString getIsin() {
        return _isin;
    }

    public ZString getSedol() {
        return _sedol;
    }
    
    @Override
    public SecurityType getSecurityType() {
        return _secDef.getSecurityType();
    }
    
    @Override
    public ZString getSecurityDesc() {
        return _secDef.getSecurityDesc();
    }

    @Override
    public ZString getSecurityGroup() {
        return _secDef.getSecurityGroup();
    }

    @Override
    public int getSecurityGroupId() {
        return _secGrpId ;
    }

    public void setLeg( int idx, LegInstrument legInst ) {
        _legs[ idx ] = legInst;
    }

    @Override
    public int getNumLegs() {
        return (_legs==null) ? 0 : _legs.length;
    }

    @Override
    public LegInstrument getLeg( final int idx ) throws SMTRuntimeException {
        return _legs[ idx ];
    }
    
    @Override
    public int getContractMultiplier() {
        return _secDef.getContractMultiplier();
    }

    @Override public final int getMinQty() { 
        return _secDef.getMinQty(); 
    }

    @Override
    public ZString getLegSecurityDesc( final int legIdx, final ReusableString secDef ) {
        final LegInstrument leg = _legs[ legIdx ];
        
        final ZString legSecDef = leg.getLegSecurityDesc();
        
        if ( legSecDef.length() != 0 ) {
            secDef.copy( legSecDef );
        } else {
            secDef.copy( leg.getInstrument().getSecurityDesc() );
        }
        
        return secDef;
    }
    
}
