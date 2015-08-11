/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.properties.AppProps;
import com.rr.core.properties.CoreProps;
import com.rr.core.tasks.ScheduledEvent;
import com.rr.core.tasks.Scheduler;
import com.rr.core.utils.NumberFormatUtils;
import com.rr.core.utils.SMTRuntimeException;

/**
 * times are generally held as int representing num millis since midnight UTC
 * 
 * @TODO WHEN NEED CROSS TIMEZONES NEED REFACTOR TIMES BACK TO LONGS AND REMOVE THE MSfromStartDay OPTIMISATION 
 */
public final class TimeZoneCalculator {

    private static final Logger    _log         = LoggerFactory.console( TimeZoneCalculator.class );

    public  static final int        DATE_STR_LEN = 9;
    private static final ZString    NONE         = new ViewString( "N/A" );

    private static final DateFormat _dateFormat = new SimpleDateFormat( "yyyyMMdd-" );
    
    public static final int[] _hourToMS = genHourToMS();
    public static final int[] _minToMS  = genMinToMS();
    public static final int[] _secToMS  = genSecToMS();

    public static final int[] _msHundreds  = genMSHundreds();
    public static final int[] _msTen       = genMsTens();

    //instance must be last static
    private static final TimeZoneCalculator _instance = new TimeZoneCalculator();
    
    // TODO should support local str > 8 ... should get from TZ format
    private final byte[] _dateStrLocal = new byte[ DATE_STR_LEN ]; // YYYYMMDD-     is constant HH:MM:SS.sss has to be added 
    private final byte[] _dateStrUTC   = new byte[ DATE_STR_LEN ]; // YYYYMMDD-     is constant HH:MM:SS.sss has to be added 
    
    private int      _localDateYYYYMMDD;
    private int      _utcDateYYYYMMDD;
    
    private int      _localOffsetFromUTC;
    private TimeZone _localTimezone;
    private long     _utcLastMidnight; // the ms in UTC upto  the last midnight
    private long     _endTodayUTC;
    private long     _endTodayLocal;
    private long     _nextThreshold; // switch between end UTC day and end local

    private ZString _todayUTC   = new ViewString( _dateStrUTC );
    private ZString _todayLocal = new ViewString( _dateStrLocal );

    public static TimeZoneCalculator instance() {
        return _instance;
    }

    private static int[] genHourToMS() {
        int[] hrToMs = new int[24];
        
        for ( int i=0 ; i < 24 ; i++ ) {
            hrToMs[i] = i * 60 * 60 * 1000;
        }
        
        return hrToMs;
    }

    private static int[] genMinToMS() {
        int[] table = new int[60];
        
        for ( int i=0 ; i < 60 ; i++ ) {
            table[i] = i * 60 * 1000;
        }
        
        return table;
    }

    private static int[] genSecToMS() {
        int[] table = new int[60];
        
        for ( int i=0 ; i < 60 ; i++ ) {
            table[i] = i * 1000;
        }
        
        return table;
    }

    private static int[] genMSHundreds() {
        int[] table = new int[10];
        
        for ( int i=0 ; i < 10 ; i++ ) {
            table[i] = i * 100;
        }
        
        return table;
    }

    private static int[] genMsTens() {
        int[] table = new int[10];
        
        for ( int i=0 ; i < 10 ; i++ ) {
            table[i] = i * 10;
        }
        
        return table;
    }

    private static TimeZone getTimeZone( String id ) {
        if ( id == null ) return TimeZone.getDefault();

        TimeZone tz = TimeZone.getTimeZone( id );
        
        return tz;
    }

    public int getTimeUTC( final long utcTime ) {
        return (int) (utcTime - _utcLastMidnight);
    }
    
    public long currentTimeMillis() {
        final long now = System.currentTimeMillis();
        
        if ( now >= _nextThreshold ) {
            thresholdExceeded( now );
        }
        
        return now;
    }
    
    private void thresholdExceeded( long now ) {
        if ( now > _endTodayUTC ) {
            setUTCDateAsNow( now );
            
            setNewThreshold();
        }
        
        if ( now > _endTodayLocal ) {
            setLocalDateAsNow( now );

            setNewThreshold();
        }
    }

    private void setNewThreshold() {
        if ( _endTodayUTC < _endTodayLocal ) {
            _nextThreshold = _endTodayUTC;
        } else {
            _nextThreshold = _endTodayLocal;
        }
    }

    public long checkToday( long now ) {
        if ( now >= _nextThreshold ) {
            thresholdExceeded( now );
        }
        
        return now;
    }
    
    public int getNowUTC() {
        long utcTime = currentTimeMillis();
        return (int) (utcTime - _utcLastMidnight);
    }
    
    public int utcToLocal( final long utcTime ) {
        return (int)( (utcTime + _localOffsetFromUTC) - _utcLastMidnight); 
    }
    
    public int timeUTCToLocal( final int msFromStartofDayUTC ) {
        return( msFromStartofDayUTC + _localOffsetFromUTC ); 
    }
    
    public int timeLocalToUTC( final int msFromStartofDayUTC ) {
        return( msFromStartofDayUTC - _localOffsetFromUTC ); 
    }
    
    public int localToUTC( final long localTime ) {
        return (int)( (localTime - _localOffsetFromUTC) - _utcLastMidnight); 
    }
    
    public int getTimeUTC() {
        return (int) (currentTimeMillis() - _utcLastMidnight); 
    }
    
    public TimeZoneCalculator() {
        this( AppProps.instance().getProperty( CoreProps.APP_TIMEZONE, false, null ) );
    }

    public TimeZoneCalculator( String t ) {
        this( getTimeZone( t ) );
    }

    public TimeZoneCalculator( TimeZone t ) {

        setLocalTimezone( t );
        
        Scheduler.instance().registerForGroupEvent( ScheduledEvent.UTCDateRoll, 
                                       new Scheduler.Callback() {
                                            private final ZString _name = new ViewString("TimeZoneCalDateRoll");

                                            @Override
                                            public ZString getName() {
                                                return _name;
                                            }

                                            @Override
                                            public void event( ScheduledEvent event ) {
                                                   setDateAsNow();
                                               }
                                           } );
    }
    
    public void setDate( String date ) {
        
        if ( date == null ) return;
        
        if ( date.length() !=  9 ) date = date + "-";
        
        byte[] src = date.getBytes();
        
        System.arraycopy( src, 0, _dateStrLocal, 0, DATE_STR_LEN );
        System.arraycopy( src, 0, _dateStrUTC,   0, DATE_STR_LEN );
    }

    public ZString dateTimeToUTC( ReusableString str, int msFromStartofDayUTC ) {
        int len = dateTimeToUTC( str.getBytes(), 0, msFromStartofDayUTC );
        
        str.setLength( len );
        
        return str;
    }

    public int dateTimeToUTC( byte[] buf, int idx, int msFromStartofDayUTC ) {
        
        int i = 0;
        while ( i < DATE_STR_LEN ) {
            buf[idx++] = _dateStrUTC[i++];
        }
        
        // HH:MM:SS.sss
        
        long ms = msFromStartofDayUTC;
        
        int hour = (int)(((ms >> 7) * 9773437) >> 38);
        ms -= 3600000 * hour;
        int minute = (int)(((ms >> 5) * 2290650) >> 32);
        ms -= 60000 * minute;
        int second = (int)(((ms >> 3) * 67109) >> 23);
        
        int leftOverMillis = (int)(ms - (1000 * second));

        int temp = (hour * 13) >> 7;
        buf[idx++] = (byte)(temp + '0');
        buf[idx++] = (byte)(hour - 10 * temp + '0');
        buf[idx++] = (byte)':';

        temp = (minute * 13) >> 7;
        buf[idx++] = (byte)(temp + '0');
        buf[idx++] = (byte)(minute - 10 * temp + '0');
        buf[idx++] = (byte)':';

        temp = (second * 13) >> 7;
        buf[idx++] = (byte)(temp + '0');
        buf[idx++] = (byte)(second - 10 * temp + '0');
        buf[idx++] = (byte)'.';

        temp = (leftOverMillis * 41) >> 12;
        buf[idx++] = (byte)(temp + '0');
        leftOverMillis -= 100 * temp;
        
        final int t = NumberFormatUtils._dig100[ leftOverMillis ];
        
        buf[ idx++ ] = (byte)(t >> 8);     // tens
        buf[ idx++ ] = (byte)(t & 0xFF);   // units;
                
        return idx;
    }

    public int dateTimeSecsToUTC( byte[] buf, int idx, int msFromStartofDayUTC ) {
        
        int i = 0;
        while ( i < DATE_STR_LEN ) {
            buf[idx++] = _dateStrUTC[i++];
        }
        
        // HH:MM:SS
        
        long ms = msFromStartofDayUTC;
        
        int hour = (int)(((ms >> 7) * 9773437) >> 38);
        ms -= 3600000 * hour;
        int minute = (int)(((ms >> 5) * 2290650) >> 32);
        ms -= 60000 * minute;
        int second = (int)(((ms >> 3) * 67109) >> 23);
        
        int temp = (hour * 13) >> 7;
        buf[idx++] = (byte)(temp + '0');
        buf[idx++] = (byte)(hour - 10 * temp + '0');
        buf[idx++] = (byte)':';

        temp = (minute * 13) >> 7;
        buf[idx++] = (byte)(temp + '0');
        buf[idx++] = (byte)(minute - 10 * temp + '0');
        buf[idx++] = (byte)':';

        temp = (second * 13) >> 7;
        buf[idx++] = (byte)(temp + '0');
        buf[idx++] = (byte)(second - 10 * temp + '0');

        return idx;
    }

    public void utcTimeTodayToLocal( ByteBuffer buf, int msFromStartofDayUTC ) {
        
        // HH:MM:SS.sss
        
        long ms = msFromStartofDayUTC + _localOffsetFromUTC;
        
        int hour = (int)(((ms >> 7) * 9773437) >> 38);
        ms -= 3600000 * hour;
        int minute = (int)(((ms >> 5) * 2290650) >> 32);
        ms -= 60000 * minute;
        int second = (int)(((ms >> 3) * 67109) >> 23);
        
        int leftOverMillis = (int)(ms - (1000 * second));

        int temp = (hour * 13) >> 7;
        buf.put( (byte)(temp + '0') );
        buf.put( (byte)(hour - 10 * temp + '0') );
        buf.put( (byte)':' );

        temp = (minute * 13) >> 7;
        buf.put( (byte)(temp + '0') );
        buf.put( (byte)(minute - 10 * temp + '0') );
        buf.put( (byte)':' );

        temp = (second * 13) >> 7;
        buf.put( (byte)(temp + '0') );
        buf.put( (byte)(second - 10 * temp + '0') );
        buf.put( (byte)'.' );

        temp = (leftOverMillis * 41) >> 12;
        buf.put( (byte)(temp + '0') );
        leftOverMillis -= 100 * temp;
        
        final int t = NumberFormatUtils._dig100[ leftOverMillis ];
        
        buf.put( (byte)(t >> 8) );     // tens
        buf.put( (byte)(t & 0xFF) );   // units;
    }

    public void utcFullTimeToLocal( ByteBuffer buf, long fullUTCinMS ) {
        
        // HH:MM:SS.sss
        
        long ms = (fullUTCinMS - _utcLastMidnight) + _localOffsetFromUTC;
        
        int hour = (int)(((ms >> 7) * 9773437) >> 38);
        ms -= 3600000 * hour;
        int minute = (int)(((ms >> 5) * 2290650) >> 32);
        ms -= 60000 * minute;
        int second = (int)(((ms >> 3) * 67109) >> 23);
        
        int leftOverMillis = (int)(ms - (1000 * second));

        int temp = (hour * 13) >> 7;
        buf.put( (byte)(temp + '0') );
        buf.put( (byte)(hour - 10 * temp + '0') );
        buf.put( (byte)':' );

        temp = (minute * 13) >> 7;
        buf.put( (byte)(temp + '0') );
        buf.put( (byte)(minute - 10 * temp + '0') );
        buf.put( (byte)':' );

        temp = (second * 13) >> 7;
        buf.put( (byte)(temp + '0') );
        buf.put( (byte)(second - 10 * temp + '0') );
        buf.put( (byte)'.' );

        temp = (leftOverMillis * 41) >> 12;
        buf.put( (byte)(temp + '0') );
        leftOverMillis -= 100 * temp;
        
        final int t = NumberFormatUtils._dig100[ leftOverMillis ];
        
        buf.put( (byte)(t >> 8) );     // tens
        buf.put( (byte)(t & 0xFF) );   // units;
    }

    public void utcTimeToShortLocal( ReusableString buf, long msTime ) {
        
        if ( msTime == 0 ) {
            buf.append( NONE );
            return;
        }
        
        // HH:MM:SS
        
        long ms = (msTime + _localOffsetFromUTC) - _utcLastMidnight;
        
        int hour = (int)(((ms >> 7) * 9773437) >> 38);
        ms -= 3600000 * hour;
        int minute = (int)(((ms >> 5) * 2290650) >> 32);
        ms -= 60000 * minute;
        int second = (int)(((ms >> 3) * 67109) >> 23);
        
        int temp = (hour * 13) >> 7;
        buf.append( (byte)(temp + '0') );
        buf.append( (byte)(hour - 10 * temp + '0') );
        buf.append( (byte)':' );

        temp = (minute * 13) >> 7;
        buf.append( (byte)(temp + '0') );
        buf.append( (byte)(minute - 10 * temp + '0') );
        buf.append( (byte)':' );

        temp = (second * 13) >> 7;
        buf.append( (byte)(temp + '0') );
        buf.append( (byte)(second - 10 * temp + '0') );
        buf.append( (byte)'.' );
    }

    public void setLocalTimezone( TimeZone t ) {
        if ( t != null ) {
            _localTimezone      = t;
            _localOffsetFromUTC = _localTimezone.getOffset( new Date().getTime() );
            
            setDateAsNow();
        }
    }

    public void getDateUTC( byte[] today ) {
         System.arraycopy( _dateStrUTC, 0, today, 0, DATE_STR_LEN );
    }
    
    public void getDateUTC( byte[] today, int offset ) {
        System.arraycopy( _dateStrUTC, 0, today, offset, DATE_STR_LEN );
    }
   
    public byte[] getDateLocal() {
        return _dateStrLocal;
    }
    
    public void setDateAsNow() {
        long now = System.currentTimeMillis();

        setLocalDateAsNow( now );
        setUTCDateAsNow( now );
    }

    // return an optional TZ offset
    public int getOffset() {
        return 0;
    }

    public boolean isToday( long dateUTC ) {
        return dateUTC >= _utcLastMidnight && dateUTC < _endTodayUTC;
    }
    
    public long getEndOfDayUTC() { 
        return _endTodayUTC;
    }

    /**
     * @param timezone
     * @param ddmm      "24/12" for 24th December
     * @return number of MS in UTC to the month and day for this year
     * 
     * @NOTE only for use in setup code
     */
    public static long ddmmToUTC( TimeZone timezone, String ddmm ) {
        int dayOfMonth = (ddmm.charAt( 0 ) - '0') * 10 + (ddmm.charAt( 1 ) - '0'); 
        int month      = (ddmm.charAt( 3 ) - '0') * 10 + (ddmm.charAt( 4 ) - '0'); 

        Calendar c = Calendar.getInstance( timezone );

        c.set( Calendar.MONTH,        month );
        c.set( Calendar.DAY_OF_MONTH, dayOfMonth );
        
        return c.getTimeInMillis();
    }
    
    /**
     * @param timezone
     * @param ddmmyyyy      "24/12/2010" for 24th December 2010
     * @return number of MS in UTC to the month and day for this year
     * 
     * @NOTE only for use in setup code
     */
    public static long ddmmyyyyToUTC( TimeZone timezone, String ddmmyyyy ) {
        
        if ( ddmmyyyy.length() != 10 || ddmmyyyy.charAt( 2 ) != '/' || ddmmyyyy.charAt( 5 ) != '/' ) {
            throw new SMTRuntimeException( "TimeZoneCalculator.ddmmyyyyToUTC() Invalid date string expected dd/mm/yyyy not [" + ddmmyyyy + "]" );
        }
        
        int day   = (ddmmyyyy.charAt( 0 ) - '0') * 10   + (ddmmyyyy.charAt( 1 ) - '0'); 
        int month = (ddmmyyyy.charAt( 3 ) - '0') * 10   + (ddmmyyyy.charAt( 4 ) - '0'); 
        int year  = (ddmmyyyy.charAt( 6 ) - '0') * 1000 + (ddmmyyyy.charAt( 7 ) - '0') * 100 + 
                    (ddmmyyyy.charAt( 8 ) - '0') * 10   + (ddmmyyyy.charAt( 9 ) - '0'); 

        try {
            Calendar c = Calendar.getInstance( timezone );

            c.set( Calendar.YEAR,         year );
            c.set( Calendar.MONTH,        month );
            c.set( Calendar.DAY_OF_MONTH, day );
            
            return c.getTimeInMillis();
            
        } catch( Exception e ) {
            throw new SMTRuntimeException( "TimeZoneCalculator.ddmmyyyyToUTC() Invalid date string expected dd/mm/yyyy not [" + ddmmyyyy + "]", e );
        }
    }
    
    /**
     * @param time      example in 24 hour clock "07:00:00"
     * @param tz
     * @return calendar for specified timezone and time based on today
     */
    public static Calendar getTimeAsToday( String time, TimeZone tz ) {
        
        if ( time.length() != 8 || time.charAt(2) != ':' || time.charAt(5) != ':' ) {
            throw new SMTRuntimeException( "TimeZoneCalculator.getTimeAsToday() Invalid time string expected hh:mm:ss not [" + time + "]" );
        }
        
        try {
            Calendar cal = Calendar.getInstance( tz );
            
            int hour = (time.charAt( 0 ) - '0') * 10 + (time.charAt( 1 ) - '0'); 
            int min  = (time.charAt( 3 ) - '0') * 10 + (time.charAt( 4 ) - '0'); 
            int sec  = (time.charAt( 6 ) - '0') * 10 + (time.charAt( 7 ) - '0'); 
            
            cal.set( Calendar.HOUR_OF_DAY, hour );
            cal.set( Calendar.MINUTE,      min );
            cal.set( Calendar.SECOND,      sec );
            
            return cal;
            
        } catch( Exception e ) {
            throw new SMTRuntimeException( "TimeZoneCalculator.getTimeAsToday() Invalid time string expected hh:mm:ss not [" + time + "]", e );
        }
    }

    /**
     * apply offset to cal
     * 
     * @param cal
     * @param offset eg +00:30:00
     * @return
     */
    public static Calendar adjust( Calendar cal, String offset ) {
        if ( offset.length() != 9 || offset.charAt(3) != ':' || offset.charAt(6) != ':' || 
             (offset.charAt( 0 ) != '-' && offset.charAt( 0 ) != '+' ) ) {
            throw new SMTRuntimeException( "TimeZoneCalculator.adjust() Invalid time adjust string expected [sign +|-]hh:mm:ss not [" + 
                                           offset + "]" );
        }
        
        try {
            int signAdjust = (offset.charAt( 0 ) == '-') ? -1 : 1;
            
            int hour = (offset.charAt( 1 ) - '0') * 10 + (offset.charAt( 2 ) - '0'); 
            int min  = (offset.charAt( 4 ) - '0') * 10 + (offset.charAt( 5 ) - '0'); 
            int sec  = (offset.charAt( 7 ) - '0') * 10 + (offset.charAt( 8 ) - '0'); 
            
            cal.add( Calendar.HOUR_OF_DAY, hour * signAdjust );
            cal.add( Calendar.MINUTE,      min  * signAdjust );
            cal.add( Calendar.SECOND,      sec  * signAdjust );
            
            return cal;
            
        } catch( Exception e ) {
            throw new SMTRuntimeException( "TimeZoneCalculator.adjust() Invalid time adjust string expected [sign +|-]hh:mm:ss not [" + 
                                           offset + "]", e );
        }
    }

    public int msFromStartofDayUTCToLocalHHMMSS( byte[] dest, int idx, int msFromStartofDayUTC ) {
        // HHMMSS
        
        long ms = (msFromStartofDayUTC + _localOffsetFromUTC);
        
        int hour = (int)(((ms >> 7) * 9773437) >> 38);
        ms -= 3600000 * hour;
        int minute = (int)(((ms >> 5) * 2290650) >> 32);
        ms -= 60000 * minute;
        int second = (int)(((ms >> 3) * 67109) >> 23);
        
        int temp = (hour * 13) >> 7;
        dest[idx++] = (byte)(temp + '0');
        dest[idx++] = (byte)(hour - 10 * temp + '0');

        temp = (minute * 13) >> 7;
        dest[idx++] = (byte)(temp + '0');
        dest[idx++] = (byte)(minute - 10 * temp + '0');

        temp = (second * 13) >> 7;
        dest[idx++] = (byte)(temp + '0');
        dest[idx++] = (byte)(second - 10 * temp + '0');

        return idx;
    }

    public int msFromStartofDayUTCToHHMMSS( byte[] dest, int idx, int msFromStartofDayUTC ) {
        // HHMMSS
        
        long ms = msFromStartofDayUTC;
        
        int hour = (int)(((ms >> 7) * 9773437) >> 38);
        ms -= 3600000 * hour;
        int minute = (int)(((ms >> 5) * 2290650) >> 32);
        ms -= 60000 * minute;
        int second = (int)(((ms >> 3) * 67109) >> 23);
        
        int temp = (hour * 13) >> 7;
        dest[idx++] = (byte)(temp + '0');
        dest[idx++] = (byte)(hour - 10 * temp + '0');

        temp = (minute * 13) >> 7;
        dest[idx++] = (byte)(temp + '0');
        dest[idx++] = (byte)(minute - 10 * temp + '0');

        temp = (second * 13) >> 7;
        dest[idx++] = (byte)(temp + '0');
        dest[idx++] = (byte)(second - 10 * temp + '0');

        return idx;
    }

    public final long msFromStartofDayToFullUTC( int msFromStartofDayUTC ) {
        return msFromStartofDayUTC + _utcLastMidnight;
    }

    public final long msFromStartofDayToFullLocal( int msFromStartofDayUTC ) {
        return msFromStartofDayUTC + _localOffsetFromUTC + _utcLastMidnight;
    }

    public TimeZone getLocalTimeZone() {
        return _localTimezone;
    }

    public int getUTCDateYYYYMMDD() {
        return _utcDateYYYYMMDD;
    }

    public int getLocalDateYYYYMMDD() {
        return _localDateYYYYMMDD;
    }

    private synchronized void setUTCDateAsNow( long now ) {
        _utcLastMidnight = now - (now % Constants.MS_IN_DAY);
        _endTodayUTC = _utcLastMidnight + Constants.MS_IN_DAY;
        
        byte[] src = _dateFormat.format( new Date( now ) ).getBytes();
        
        System.arraycopy( src, 0, _dateStrUTC, 0, DATE_STR_LEN );
        _utcDateYYYYMMDD = getDateYYYYMMDD( _dateStrUTC );

        _log.info( "TimeZoneCalculator : setting todayUTC=" + _todayUTC + ", utcLastMidnight=" + _utcLastMidnight + ", endTodayUTC="     + _endTodayUTC );
    }

    private synchronized void setLocalDateAsNow( long now ) {
        _endTodayLocal = _endTodayUTC + _localOffsetFromUTC;

        long local = now + _localOffsetFromUTC;
        
        byte[] src = _dateFormat.format( new Date( local ) ).getBytes();
        System.arraycopy( src, 0, _dateStrLocal, 0, DATE_STR_LEN );
        _localDateYYYYMMDD = getDateYYYYMMDD( _dateStrLocal );

        _log.info( "TimeZoneCalculator : setting todayLocal=" + _todayLocal + ", endTodayLocal="     + _endTodayLocal );
    }

    private int getDateYYYYMMDD( byte[] dateStrLocal ) {
        int y1 = dateStrLocal[0] - '0';
        int y2 = dateStrLocal[1] - '0';
        int y3 = dateStrLocal[2] - '0';
        int y4 = dateStrLocal[3] - '0';
        int m1 = dateStrLocal[4] - '0';
        int m2 = dateStrLocal[5] - '0';
        int d1 = dateStrLocal[6] - '0';
        int d2 = dateStrLocal[7] - '0';
        
        int yyyy = y1 * 1000 + y2 * 100 + y3 * 10 + y4;
        int mm = m1 * 10 + m2;
        int dd = d1 * 10 + d2;
        
        return yyyy * 10000 + mm * 100 + dd;
    }
}
