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
import com.rr.core.lang.ViewString;

public class Auction {

    private static final ViewString AUCTION    = new ViewString( "Auction " );
    private static final ViewString START_TIME = new ViewString( ", start=" );
    private static final ViewString END_TIME   = new ViewString( ", end=" );

    public enum  Type { Open, Intraday, Close, Null }
    
    private final Calendar _startCal;
    private final Calendar _endCal;
    private final Type     _type;

    private long _startTimeUTC;
    private long _endTimeUTC;
    
    public Auction( Calendar startCal, Calendar endCal, Type type ) {
        
        _startCal = startCal;
        _endCal   = endCal;
        _type     = type;

        setToday();
    }

    public void setToday() {
        
        if ( _startCal != null ) {
            Calendar c = Calendar.getInstance( _startCal.getTimeZone() );
            _startCal.set( c.get( Calendar.YEAR ), 
                           c.get( Calendar.MONTH ),
                           c.get( Calendar.DAY_OF_MONTH ) );
            _startTimeUTC = _startCal.getTimeInMillis();
        } else {
            _startTimeUTC = 0;
        }
        
        if ( _endCal != null ) {
            Calendar c = Calendar.getInstance( _startCal.getTimeZone() );
            _endCal.set(   c.get( Calendar.YEAR ), 
                           c.get( Calendar.MONTH ),
                           c.get( Calendar.DAY_OF_MONTH ) );
            _endTimeUTC   = _endCal.getTimeInMillis();
        } else {
            _endTimeUTC =  0;
        }
    }
    
    public ReusableString toString( ReusableString s ) {
        s.append( AUCTION ).append( _type );
        s.append( START_TIME );
        TimeZoneCalculator.instance().utcTimeToShortLocal( s, _startTimeUTC );
        s.append( END_TIME );
        TimeZoneCalculator.instance().utcTimeToShortLocal( s, _endTimeUTC );
        return s;
    }

    public long getStartTime() {
        return _startTimeUTC;
    }

    public long getEndTime() {
        return _endTimeUTC;
    }

    public boolean isIn( long timeUTC ) {
        return timeUTC >= _startTimeUTC && timeUTC <= _endTimeUTC;
    }

    public void setHalfDay( long closeTime ) {

        long auctionLengthMS = _endTimeUTC - _startTimeUTC;
        
        if ( _startCal != null ) {
            _startTimeUTC = closeTime - auctionLengthMS;
        } 
        
        if ( _endCal != null ) {
            _endTimeUTC   = closeTime;
        } 
    }

    public final Calendar getStartTimeCalendar() {
        return _startCal;
    }
    
    public final Calendar getEndTimeCalendar() {
        return _endCal;
    }

    public Type getType() {
        return _type;
    }
}
