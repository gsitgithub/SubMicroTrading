/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.exchange;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Enricher;
import com.rr.core.model.Exchange;
import com.rr.core.model.Instrument;
import com.rr.core.tasks.ScheduledEvent;
import com.rr.core.tasks.Scheduler;
import com.rr.om.model.id.IDGenerator;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.model.instrument.ExchangeState;
import com.rr.om.model.instrument.ExchangeValidator;

public class BaseExchangeImpl implements Exchange {

    private static int                _nextIdx = 0;
    
    private final Enricher            _enricher ;
    private final OMExchangeValidator _validator;

    private final ZString           _mic;
            final ZString           _rec;
    private final boolean           _usePrimaryRIC;
    private final boolean           _supportMarketOrders;
    private final boolean           _supportMOCOrders;
    private final TimeZone          _tz;
    private final boolean           _isMTF;
    private final IDGenerator       _execIdGen;
    private final boolean           _requiresExecIdGen; 
    private final ExchangeSession   _exSess;
    private final boolean           _sendCancelToExchangeAtEOD;
    private final boolean           _supportsTradeCorrection;
    private final Calendar          _expireTimeForSendEODEvents;   // @TODO implement EOD send expire events

    private final int               _id = nextId();
    private       boolean           _isHalfDay;
    private       List<Long>        _halfDayHolidays;
    
    private static synchronized int nextId() {
        return _nextIdx++;
    }
    
    public BaseExchangeImpl( Enricher            enricher, 
                             OMExchangeValidator validator,
                             ZString             mic,
                             ZString             rec,
                             TimeZone            tz,
                             boolean             usePrimaryRIC,
                             boolean             supportMarketOrders,
                             boolean             supportMOCOrders,
                             boolean             isMTF,
                             IDGenerator         execIdGen,
                             ExchangeSession     exSess,
                             boolean             sendCancelToExchangeAtEOD,
                             boolean             supportsTradeCorrect,
                             Calendar            expireTimeForSendEODEvents,
                             List<Long>          halfDayHolidaysUTC ) {

        super();

        _enricher                   = enricher;
        _validator                  = validator;
        _mic                        = mic;
        _rec                        = rec;
        _tz                         = tz;
        _usePrimaryRIC              = usePrimaryRIC;
        _supportMarketOrders        = supportMarketOrders;
        _supportMOCOrders           = supportMOCOrders;
        _isMTF                      = isMTF;
        _requiresExecIdGen          = (execIdGen != null);
        _execIdGen                  = execIdGen;
        _exSess                     = exSess;
        _sendCancelToExchangeAtEOD  = sendCancelToExchangeAtEOD;
        _supportsTradeCorrection    = supportsTradeCorrect;
        _expireTimeForSendEODEvents = expireTimeForSendEODEvents;
        _halfDayHolidays            = new ArrayList<Long>();
        
        if ( halfDayHolidaysUTC != null ) _halfDayHolidays.addAll( halfDayHolidaysUTC );
        
        Scheduler.instance().registerForGroupEvent( ScheduledEvent.UTCDateRoll, 
                                                    
                            new Scheduler.Callback() {
                                    private final ZString _cbName = new ViewString( "Exchange_" + _rec + "_DateRoll");
                                    
                                    @Override
                                    public ZString getName() {
                                        return _cbName;
                                    }
            
                                    @Override
                                    public void event( ScheduledEvent event ) {
                                        setToday();
                                    }
                                } );
    }

    @Override
    public final int getId() {
        return _id;
    }
    
    @Override
    public final Enricher getEnricher() {
        return _enricher;
    }

    @Override
    public final ExchangeValidator getExchangeEventValidator() {
        return _validator;
    }

    @Override
    public void generateMarketClOrdId( ReusableString dest, ZString clientClOrdId ) {
        dest.setValue( clientClOrdId );
    }

    @Override
    public final ExchangeState getExchangeState() {
        return _exSess.getExchangeState( System.currentTimeMillis() );
    }

    @Override
    public final ExchangeState getExchangeState( long timeUTC ) {
        return _exSess.getExchangeState( timeUTC );
    }

    @Override
    public final long getExpireTimeToSendEndOfDayEvents() {
        return _expireTimeForSendEODEvents.getTimeInMillis();
    }

    @Override
    public final ZString getMIC() {
        return _mic;
    }

    @Override
    public final ZString getRecCode() {
        return _rec;
    }

    @Override
    public final ExchangeSession getSession() {
        return _exSess;
    }

    @Override
    public TimeZone getTimeZone() {
        return _tz;
    }

    @Override
    public final boolean isExchangeAnMTF() {
        return _isMTF;
    }

    @Override
    public boolean isGeneratedExecIDRequired() {
        return _requiresExecIdGen;
    }

    @Override
    public boolean isOpenForTrading( long time ) {
        return _exSess.isOpen( time );
    }

    @Override
    public boolean isPrimaryRICRequired() {
        return _usePrimaryRIC;
    }

    @Override
    public boolean isSendCancelToExchangeAtEOD() {
        return _sendCancelToExchangeAtEOD;
    }

    @Override
    public boolean isTradeCorrectionSupported() {
        return _supportsTradeCorrection;
    }
    
    public OMExchangeValidator getValidator() {
        return _validator;
    }

    public boolean isSupportMarketOrders() {
        return _supportMarketOrders;
    }

    public boolean isSupportMOCOrders() {
        return _supportMOCOrders;
    }

    public boolean isExecIdGen() {
        return _requiresExecIdGen;
    }

    public ExchangeSession getExSess() {
        return _exSess;
    }

    public boolean isSupportsTradeCorrection() {
        return _supportsTradeCorrection;
    }

    @Override
    public void makeExecIdUnique( ReusableString execIdForUpdate, ZString execId, Instrument inst ) {
        if ( _execIdGen == null ) {
            execIdForUpdate.copy( execId );
        } else {
            _execIdGen.genID( execIdForUpdate );
        }
    }

    @Override
    public ExchangeSession getExchangeSession( ZString marketSegment ) {
        return _exSess.getExchangeSession( marketSegment );
    }

    @Override
    public boolean isHalfDay() {
        return _isHalfDay;
    }
    
    @Override
    public ReusableString toString( ReusableString s ) {
        s.append( "REC=" ).append( _rec ).append( ", MIC=" ).append( _mic ).append( ", usePrimaryRIC=" ).append( _usePrimaryRIC );
        s.append( ", isMTF=" ).append( _isMTF ).append( ", timezone=" ).append( _tz.getDisplayName() );
        s.append( ", isHalfDay=" ).append( _isHalfDay ).append( _isHalfDay ).append( ", execIdGen=" );
        s.append( _requiresExecIdGen ).append( "\n    " );
        
        s.append( "enricher=" ).append( _enricher.getClass().getSimpleName() );
        s.append( ", validator=" ).append( _validator.getClass().getSimpleName() ).append( "\n    ");
        
        s.append( "mktOrderSup=" ).append( _supportMarketOrders ).append( ", mocSup=" ).append( _supportMOCOrders );
        s.append( ", tradeCorrSupp=" ).append( _supportsTradeCorrection );
        s.append( ", sendEODCancel=" ).append( _sendCancelToExchangeAtEOD ).append( ", expireSendTime=" );
        
        if ( _expireTimeForSendEODEvents != null ) {
            TimeZoneCalculator.instance().utcTimeToShortLocal( s, _expireTimeForSendEODEvents.getTimeInMillis() );
        } else {
            s.append( "N/A" );
        }

        s.append( "\n    exchangeSession=[" );
        _exSess.toString( s ).append( "]" );
        
        return s;
    }

    protected void setToday() {
        _exSess.setToday();
        
        _exSess.setHalfDay( false );
        
        for ( Long date : _halfDayHolidays ) {
            if ( TimeZoneCalculator.instance().isToday( date.longValue() ) )  {
                _exSess.setHalfDay( true );
                return;
            }
        }
    }
}
