/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.instrument;

import java.util.Calendar;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.om.model.instrument.Auction.Type;

public class SingleExchangeSession implements ExchangeSession {

    private final ZString  _id;
    private final Auction  _closeAuction;
    private final Auction  _openAuction;
    private final Auction  _intradayAuction;
    private final Calendar _openCal;
    private final Calendar _halfDayCal;
    private final Calendar _endCal;
    private final Calendar _startContCal;
    private final Calendar _endContCal;
    private       long     _halfDayClose;
    private       long     _openTime;
    private       long     _closeTime;
    private       boolean  _isHalfDay;
    private       long     _startContinuous;
    private       long     _endContinuous;

    public SingleExchangeSession( ZString id, Calendar openCal, Calendar halfDayCal, Calendar endCal, Auction openA, Auction closeA ) {
        this( id, openCal, null, null, halfDayCal, endCal, openA, null, closeA );
    }
    
    public SingleExchangeSession( ZString  id,
                                  Calendar openCal,
                                  Calendar startContinuous,
                                  Calendar endContinuous,
                                  Calendar halfDayCal, 
                                  Calendar endCal,
                                  Auction  openAuction,
                                  Auction  intraDayAuction,
                                  Auction  closeAuction ) {

        _id              = id;
        _openCal         = openCal;
        _halfDayCal      = halfDayCal;
        _endCal          = endCal;
        _openAuction     = (openAuction!=null)     ? openAuction     :  new Auction( null, null, Type.Null );
        _intradayAuction = (intraDayAuction!=null) ? intraDayAuction :  new Auction( null, null, Type.Null );
        _closeAuction    = (closeAuction!=null)    ? closeAuction    :  new Auction( null, null, Type.Null );

        if ( startContinuous == null ) {
            if ( openAuction == null ) {
                startContinuous = (Calendar)openCal.clone();
            } else {
                startContinuous = (Calendar) openAuction.getEndTimeCalendar().clone();
                startContinuous.add( Calendar.MILLISECOND, 1 );
            }
        }
        
        if ( endContinuous == null ) {
            if ( closeAuction == null ) {
                endContinuous = (Calendar)endCal.clone();
            } else {
                endContinuous = (Calendar) closeAuction.getStartTimeCalendar().clone();
                endContinuous.add( Calendar.MILLISECOND, -1 );
            }
        }
        
        _startContCal    = startContinuous;
        _endContCal      = endContinuous;

        setToday();
    }
    
    @Override
    public ZString getId() {
        return _id;
    }
    
    @Override
    public Auction getCloseAuction() {
        return _closeAuction;
    }

    @Override
    public long getCloseTime() {
        return _closeTime;
    }

    @Override
    public ExchangeSession getExchangeSession( ZString marketSegment ) {
        return this;
    }

    @Override
    public ExchangeState getExchangeState( long timeUTC ) {

        if ( _intradayAuction.isIn( timeUTC ) )
            return ExchangeState.IntradayAuction;

        if ( timeUTC >= _startContinuous && timeUTC <= _endContinuous ) {
            return ExchangeState.Continuous;
        }
        
        if ( _openAuction.isIn( timeUTC ) )
            return ExchangeState.OpeningAuction;
        if ( _closeAuction.isIn( timeUTC ) )
            return ExchangeState.ClosingAuction;
        if ( _intradayAuction.isIn( timeUTC ) )
            return ExchangeState.IntradayAuction;
        if ( timeUTC >= _openTime && timeUTC < _openAuction.getStartTime() )
            return ExchangeState.PreOpen;

        return ExchangeState.Closed;
    }

    @Override
    public ExchangeState getExchangeState() {
        return getExchangeState( System.currentTimeMillis() );
    }

    @Override
    public long getHalfDayCloseTime() {
        return _halfDayClose;
    }

    @Override
    public Auction getIntradayAuction() {
        return _intradayAuction;
    }

    @Override
    public Auction getOpenAuction() {
        return _openAuction;
    }

    @Override
    public long getOpenTime() {
        return _openTime;
    }

    @Override
    public boolean isOpen( long time ) {
        return time >= _openTime && time <= _closeTime;
    }

    @Override
    public void setHalfDay( boolean isHalfDay ) {
        _isHalfDay = isHalfDay;
        setCloseTime();
    }

    @Override
    public void setToday() {
        
        _openAuction.setToday();
        _closeAuction.setToday();
        
        Calendar c = Calendar.getInstance( _openCal.getTimeZone() );
        _openCal.set(      c.get( Calendar.YEAR ), 
                           c.get( Calendar.MONTH ),
                           c.get( Calendar.DAY_OF_MONTH ) );
        _endCal.set(       c.get( Calendar.YEAR ), 
                           c.get( Calendar.MONTH ),
                           c.get( Calendar.DAY_OF_MONTH ) );
        _startContCal.set( c.get( Calendar.YEAR ), 
                           c.get( Calendar.MONTH ),
                           c.get( Calendar.DAY_OF_MONTH ) );
        _endContCal.set(   c.get( Calendar.YEAR ), 
                           c.get( Calendar.MONTH ),
                           c.get( Calendar.DAY_OF_MONTH ) );
        
        if ( _halfDayCal != null ) {
            _halfDayCal.set( c.get( Calendar.YEAR ), 
                             c.get( Calendar.MONTH ),
                             c.get( Calendar.DAY_OF_MONTH ) );
        }
        
        _openTime        = _openCal.getTimeInMillis();
        _startContinuous = _startContCal.getTimeInMillis();
        
        setCloseTime();
    }
    
    private void setCloseTime() {
        if ( _isHalfDay ) {
            // @NOTE : horrible hack for half day close auction and end continuous adjustment ... REFACTOR
            
            _closeTime = _halfDayCal.getTimeInMillis();
            
            long auctionLengthMS = _closeAuction.getEndTime() - _closeAuction.getStartTime();
            
            if ( auctionLengthMS == 0 ) {
                _endContinuous = _closeTime;
            } else {
                _endContinuous = _closeTime - auctionLengthMS;
                
                _closeAuction.setHalfDay( _endContinuous );
            }
            
        } else {
            _endContinuous = _endContCal.getTimeInMillis();
            _closeTime     = _endCal.getTimeInMillis();
        }
    }

    @Override
    public void setOpen( long openUTC ) {
        _openTime = openUTC;
    }
    
    @Override
    public ReusableString toString( ReusableString buf ) {
        buf.append( " id=" ).append( _id ).append( ", isHalfDay=" ).append( _isHalfDay ).append( ", openTime=" );
        TimeZoneCalculator.instance().utcTimeToShortLocal( buf, _openTime );
        buf.append( ", startCont=" );
        TimeZoneCalculator.instance().utcTimeToShortLocal( buf, _startContinuous );
        buf.append( ", endCont=" );
        TimeZoneCalculator.instance().utcTimeToShortLocal( buf, _endContinuous );
        buf.append( ", closeTime=" );
        TimeZoneCalculator.instance().utcTimeToShortLocal( buf, _closeTime );
        buf.append( "\n        openAuction=(" );
        if ( _openAuction != null || _openAuction.getType() == Auction.Type.Null ) {
            _openAuction.toString( buf ); 
        } else {
            buf.append( "N/A" );
        }
        buf.append( ")\n        closeAuction=(" );
        if ( _closeAuction != null || _closeAuction.getType() == Auction.Type.Null ) {
            _closeAuction.toString( buf );
        } else {
            buf.append( "N/A" );
        }
        buf.append( ")" );
        return buf;
    }
}
