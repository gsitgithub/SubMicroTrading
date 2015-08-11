/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.dummy.warmup;

import java.util.TimeZone;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Enricher;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.om.client.OMEnricher;
import com.rr.om.exchange.OMExchangeValidator;
import com.rr.om.model.id.DailySimpleIDGenerator;
import com.rr.om.model.id.IDGenerator;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.model.instrument.ExchangeState;
import com.rr.om.model.instrument.ExchangeValidator;

public class DummyExchange implements Exchange {

    public static final Exchange DUMMY = new DummyExchange( new ViewString("DUM"), new DailySimpleIDGenerator( new ViewString("DUM") ), false );
    
    private ZString             _rec;
    private OMEnricher          _enricher        = new DummyEnricher();
    private OMExchangeValidator _dummyValidator  = new DummyValidator();
    private ExchangeState       _state           = ExchangeState.Continuous;
    private ExchangeSession     _session         = new DummyExchangeSession();
    private boolean             _execIdUnqiue    = true;
    
    private final boolean          _isMTF;
    private final IDGenerator      _numericIDGenerator;
    private final ExchangeSession  _exSess;

    public DummyExchange( ZString rec, IDGenerator idGen, boolean isMTF ) {
        _rec = rec;
        _numericIDGenerator = idGen;
        _isMTF = isMTF;
        _exSess = new DummyExchangeSession();
    }

    public boolean isExecIdUnqiue() {
        return _execIdUnqiue;
    }
    
    public boolean isNumericClOrdIdRequired() {
        return _numericIDGenerator != null;
    }

    public boolean setExecIdUnqiue( boolean execIdUnqiue ) {
        boolean old = _execIdUnqiue;
        
        _execIdUnqiue = execIdUnqiue;
        
        return old;
    }

    @Override
    public Enricher getEnricher() {
        return _enricher;
    }

    @Override
    public long getExpireTimeToSendEndOfDayEvents() {
        return 0;
    }

    @Override
    public ZString getMIC() {
        return _rec;
    }

    @Override
    public ZString getRecCode() {
        return _rec;
    }

    @Override
    public TimeZone getTimeZone() {
        return null;
    }

    @Override
    public boolean isGeneratedExecIDRequired() {
        return _execIdUnqiue == false;
    }

    @Override
    public boolean isPrimaryRICRequired() {
        return false;
    }

    @Override
    public void makeExecIdUnique( ReusableString execIdForUpdate, ZString execId, Instrument inst ) {

        if ( _execIdUnqiue ) {
            execIdForUpdate.copy( execId );
        } else {
            execIdForUpdate.copy( inst.getExchange().getRecCode() );
            execIdForUpdate.append( inst.getCurrency().getVal() ).append( execId );
        }
    }

    @Override
    public void generateMarketClOrdId( ReusableString dest, ZString clientClOrdId ) {
        if ( _numericIDGenerator != null ) {

            _numericIDGenerator.genID( dest );
            
        } else {
            dest.copy( clientClOrdId );
        }
    }

    @Override
    public ExchangeState getExchangeState() {
        return _state;
    }

    @Override
    public ExchangeSession getSession() {
        return _session;
    }

    @Override
    public boolean isExchangeAnMTF() {
        return _isMTF;
    }

    @Override
    public boolean isSendCancelToExchangeAtEOD() {
        return false;
    }

    @Override
    public boolean isTradeCorrectionSupported() {
        return true;
    }

    @Override
    public ExchangeValidator getExchangeEventValidator() {
        return _dummyValidator;
    }

    @Override
    public boolean isOpenForTrading( long time ) {
        return _exSess.isOpen( time );
    }

    @Override
    public ExchangeSession getExchangeSession( ZString marketSegment ) {
        return _exSess;
    }

    @Override
    public ExchangeState getExchangeState( long timeUTC ) {
        return ExchangeState.Continuous;
    }

    @Override
    public boolean isHalfDay() {
        return false;
    }

    @Override
    public ReusableString toString( ReusableString buf ) {
        buf.append( "DummyExchange" );
        return buf;
    }

    @Override
    public int getId() {
        return 0;
    }
}
