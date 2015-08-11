/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti;

import java.math.BigDecimal;
import java.math.MathContext;

import com.rr.codec.emea.exchange.eti.ETIEncodeBuilderImpl;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Currency;
import com.rr.core.model.MultiByteLookup;
import com.rr.core.model.SecurityType;
import com.rr.core.model.TwoByteLookup;
import com.rr.model.generated.internal.type.OrdRejReason;

public class ETIEncodeBuilderTest extends BaseTestCase {
    
    private byte[]               _buf     = new byte[8192];
    private ETIEncodeBuilderImpl _builder = new ETIEncodeBuilderImpl( _buf, 0, new ViewString("0.1") );
    
    private MathContext          _mc;
    
    @Override
    public void setUp(){
        _mc = new MathContext( 10 );
    }
    
    @Override
    public String toString() {
        return new String( _buf, 0, _builder.getNextFreeIdx() );
    }
    
    private void chk( byte[] expected ) {
        assertEquals( _builder.getOffset() + expected.length, _builder.getNextFreeIdx() );

        byte[] res = _builder.getBuffer();
        for( int i=0 ; i < expected.length ; ++i ) {
            byte exp   = expected[i];
            byte found = res[i+_builder.getOffset()]; 
            assertEquals( "fail at idx " + i + " expected=" + exp + ", found=" + found, exp, found );
        }
    }

    public void testEncodeChar() {
        doEncodeChar( 'A' );
        doEncodeChar( 'Z' );
        doEncodeChar( 'a' );
        doEncodeChar( 'z' );
        doEncodeChar( '0' );
        doEncodeChar( '9' );
        doEncodeChar( '-' );
        doEncodeChar( '.' );
        doEncodeChar( '?' );
        doEncodeChar( '/' );
        doEncodeChar( Constants.UNSET_CHAR );
    }

    private void doEncodeChar( char cVal ) {
        _builder.start();

        if ( cVal == Constants.UNSET_CHAR ){
            byte[] expected = { (byte)0x00 };
            _builder.encodeChar( (byte)cVal );
            chk( expected );
        } else {
            byte[] expected = { (byte)cVal };
            _builder.encodeByte( (byte)cVal );
            chk( expected );
        }
    }

    public void testEncodeByte() {
        doEncodeByte( (byte) 'A' );
        doEncodeByte( (byte) 'Z' );
        doEncodeByte( (byte) 'a' );
        doEncodeByte( (byte) 'z' );
        doEncodeByte( (byte) '0' );
        doEncodeByte( (byte) '9' );
        doEncodeByte( (byte) '-' );
        doEncodeByte( (byte) '.' );
        doEncodeByte( (byte) '?' );
        doEncodeByte( (byte) '/' );
        doEncodeByte( Constants.UNSET_BYTE );
    }

    private void doEncodeByte( byte cVal ) {
        _builder.start();

        if ( cVal == Constants.UNSET_BYTE ){
            byte[] expected = { (byte)0x80 };
            _builder.encodeByte( cVal );
            chk( expected );
        } else {
            byte[] expected = { cVal };
            _builder.encodeByte( cVal );
            chk( expected );
        }
    }

    public void testEncodeUByte() {
        doEncodeUByte( (byte) 'A' );
        doEncodeUByte( (byte) 'Z' );
        doEncodeUByte( (byte) 'a' );
        doEncodeUByte( (byte) 'z' );
        doEncodeUByte( (byte) '0' );
        doEncodeUByte( (byte) '9' );
        doEncodeUByte( (byte) '-' );
        doEncodeUByte( (byte) '.' );
        doEncodeUByte( (byte) '?' );
        doEncodeUByte( (byte) '/' );
        doEncodeUByte( Constants.UNSET_BYTE );
    }

    private void doEncodeUByte( byte cVal ) {
        _builder.start();

        if ( cVal == Constants.UNSET_BYTE ){
            byte[] expected = { (byte)0xFF };
            _builder.encodeUByte( cVal );
            chk( expected );
        } else {
            byte[] expected = { cVal };
            _builder.encodeUByte( cVal );
            chk( expected );
        }
    }

    public void testEncodeShort() {
        doEncodeShort( (short) -1 );
        doEncodeShort( (short) -1000 );
        doEncodeShort( (short) 0 );
        doEncodeShort( Short.MAX_VALUE );
        doEncodeShort( (short) (Short.MIN_VALUE + 1) );
        doEncodeShort( Constants.UNSET_SHORT );
        doEncodeShort( (short) 1000 );
    }

    private void doEncodeShort( short val ) {
        _builder.start();

        byte b1 = (byte) ((val >>> 8)  & 0xFF);
        byte b2 = (byte)  (val & 0xFF);
        
        byte[] expected = {b2, b1};
        
        if ( val == Constants.UNSET_SHORT ) {
            expected = new byte[] { (byte) 0x00, (byte) 0x80 };
        }
        
        _builder.encodeShort( val );
        
        chk( expected );
    }

    public void testEncodeUShort() {
        doEncodeUShort( (short) -1 );
        doEncodeUShort( (short) -1000 );
        doEncodeUShort( (short) 0 );
        doEncodeUShort( Short.MAX_VALUE );
        doEncodeUShort( (short) (Short.MIN_VALUE + 1) );
        doEncodeUShort( Constants.UNSET_SHORT );
        doEncodeUShort( (short) 1000 );
    }

    private void doEncodeUShort( short val ) {
        _builder.start();

        byte b1 = (byte) ((val >>> 8)  & 0xFF);
        byte b2 = (byte)  (val & 0xFF);
        
        byte[] expected = {b2, b1};
        
        if ( val == Constants.UNSET_SHORT ) {
            expected = new byte[] { (byte) 0xFF, (byte) 0xFF };
        }
        
        _builder.encodeUShort( val );
        
        chk( expected );
    }

    public void testEncodeInt() {
        doEncodeInt( -1 );
        doEncodeInt( -1000 );
        doEncodeInt( 0 );
        doEncodeInt( Integer.MAX_VALUE );
        doEncodeInt( Integer.MIN_VALUE + 1 );
        doEncodeInt( Constants.UNSET_INT );
        doEncodeInt( 1000 );
    }

    private void doEncodeInt( int val ) {
        _builder.start();

        byte b1 = (byte)  (val >>> 24);
        byte b2 = (byte) ((val >>> 16) & 0xFF);
        byte b3 = (byte) ((val >>> 8)  & 0xFF);
        byte b4 = (byte)  (val & 0xFF);
        
        byte[] expected = {b4, b3, b2, b1};
        
        if ( val == Constants.UNSET_INT ) {
            expected = new byte[] { 0x00, 0x00, 0x00, (byte) 0x80 };
        }
        
        _builder.encodeInt( val );
        
        chk( expected );
    }

    public void testEncodeUInt() {
        doEncodeUInt( -1 );
        doEncodeUInt( -1000 );
        doEncodeUInt( 0 );
        doEncodeUInt( Integer.MAX_VALUE );
        doEncodeUInt( Integer.MIN_VALUE + 1 );
        doEncodeUInt( Constants.UNSET_INT );
        doEncodeUInt( 1000 );
    }

    private void doEncodeUInt( int val ) {
        _builder.start();

        byte b1 = (byte)  (val >>> 24);
        byte b2 = (byte) ((val >>> 16) & 0xFF);
        byte b3 = (byte) ((val >>> 8)  & 0xFF);
        byte b4 = (byte)  (val & 0xFF);
        
        byte[] expected = {b4, b3, b2, b1};
        
        if ( val == Constants.UNSET_INT ) {
            expected = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        }
        
        _builder.encodeUInt( val );
        
        chk( expected );
    }

    public void testEncodeLong() {
        doEncodeLong( -1 );
        doEncodeLong( -1000 );
        doEncodeLong( 99999 );
        doEncodeLong( 98765432101234l );
        doEncodeLong( 0 );
        doEncodeLong( Long.MAX_VALUE );
        doEncodeLong( Long.MIN_VALUE + 1 );
        doEncodeLong( Constants.UNSET_LONG );
        doEncodeLong( 1000 );
    }

    private void doEncodeLong( long val ) {
        _builder.start();

        byte b1 = (byte)  (val >>> 56);
        byte b2 = (byte) ((val >>> 48) & 0xFF);
        byte b3 = (byte) ((val >>> 40) & 0xFF);
        byte b4 = (byte) ((val >>> 32) & 0xFF);
        byte b5 = (byte) ((val >>> 24) & 0xFF);
        byte b6 = (byte) ((val >>> 16)  & 0xFF);
        byte b7 = (byte) ((val >>> 8)  & 0xFF);
        byte b8 = (byte)  (val & 0xFF);
        
        byte[] expected = {b8, b7, b6, b5, b4, b3, b2, b1};
        
        if ( val == Constants.UNSET_LONG ) {
            expected = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80 };
        }
        
        _builder.encodeLong( val );
        
        chk( expected );
    }

    public void testEncodeULong() {
        doEncodeULong( -1 );
        doEncodeULong( -1000 );
        doEncodeULong( 99999 );
        doEncodeULong( 98765432101234l );
        doEncodeULong( 0 );
        doEncodeULong( Long.MAX_VALUE );
        doEncodeULong( Long.MIN_VALUE + 1 );
        doEncodeULong( Constants.UNSET_LONG );
        doEncodeULong( 1000 );
    }

    private void doEncodeULong( long val ) {
        _builder.start();

        byte b1 = (byte)  (val >>> 56);
        byte b2 = (byte) ((val >>> 48) & 0xFF);
        byte b3 = (byte) ((val >>> 40) & 0xFF);
        byte b4 = (byte) ((val >>> 32) & 0xFF);
        byte b5 = (byte) ((val >>> 24) & 0xFF);
        byte b6 = (byte) ((val >>> 16) & 0xFF);
        byte b7 = (byte) ((val >>> 8)  & 0xFF);
        byte b8 = (byte)  (val & 0xFF);
        
        byte[] expected = {b8, b7, b6, b5, b4, b3, b2, b1};
        
        if ( val == Constants.UNSET_LONG ) {
            expected = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
        }
        
        _builder.encodeULong( val );
        
        chk( expected );
    }

    public void testEncodePrice() {
        
        doEncodePrice( 0.2148, new ReusableString(8) );
      
        doEncodePrice(    0.0001,       2.0, 0.0001 );
        doEncodePrice(  999.0001,    1002.0, 0.0001 );
        doEncodePrice( 9999.0001,   10011.9, 0.0001 );

        byte[] expected = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80 };
        doEncodeNullPrice( Constants.UNSET_DOUBLE, new ReusableString(expected) );
    }

    private void doEncodeNullPrice( double val, ReusableString expStrBuf ) {
        _builder.start();
        _builder.encodePrice( val );        
        chk( expStrBuf.getBytes() );
    }

    public void doEncodePrice( double start, double max, double deltaInc ) {
        BigDecimal delta = new BigDecimal( Double.toString( deltaInc ), _mc );
        
        double testVal = start;

        ReusableString expStrBuf = new ReusableString(4);

        while( testVal < max ) {
            doEncodePrice( testVal, expStrBuf );
            
            BigDecimal tmp = new BigDecimal( Double.toString(testVal) );
            
            tmp     = tmp.add( delta );
            testVal = tmp.doubleValue();
        }
    }

    private void doEncodePrice( double val, ReusableString expStrBuf ) {

        _builder.start();
        _builder.encodePrice( val );        
        getExpectedPrice( val, expStrBuf );
        chk( expStrBuf.getBytes() );

        double multUp   = (val + Constants.WEIGHT) * 100;
        _builder.start();
        _builder.encodePrice( multUp );        
        getExpectedPrice( val, 100, expStrBuf );
        chk( expStrBuf.getBytes() );

        double multDown = (val + Constants.WEIGHT) * 0.01;
        _builder.start();
        _builder.encodePrice( multDown );        
        getExpectedPrice( val, 0.01, expStrBuf );
        chk( expStrBuf.getBytes() );
    }

    private void getExpectedPrice( double dval, ReusableString expStrBuf ) {
        if ( dval == Constants.UNSET_DOUBLE ) {
            expStrBuf.reset();
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            return;
        }

        long val = ETIEncodeBuilderImpl.priceToExternalLong( dval );
        
        expStrBuf.reset();

        byte b8 = (byte)  (val >>> 56);
        byte b7 = (byte) ((val >>> 48) & 0xFF);
        byte b6 = (byte) ((val >>> 40) & 0xFF);
        byte b5 = (byte) ((val >>> 32) & 0xFF);
        byte b4 = (byte) ((val >>> 24) & 0xFF);
        byte b3 = (byte) ((val >>> 16)  & 0xFF);
        byte b2 = (byte) ((val >>> 8)  & 0xFF);
        byte b1 = (byte)  (val & 0xFF);
        
        byte[] expected = {b1, b2, b3, b4, b5, b6, b7, b8};
        
        expStrBuf.append( expected );
    }

    private void getExpectedPrice( double dval, double factor, ReusableString expStrBuf ) {
        if ( dval == Constants.UNSET_DOUBLE ) {
            expStrBuf.reset();
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            expStrBuf.append( (byte) 0x00 );
            return;
        }

        long val;
        if ( dval > 0 ) {
            val = (long) ((dval + Constants.WEIGHT) * ETIEncodeBuilderImpl.KEEP_DECIMAL_PLACE_FACTOR * factor );
        } else {
            val = (long) ((dval - Constants.WEIGHT) * ETIEncodeBuilderImpl.KEEP_DECIMAL_PLACE_FACTOR * factor );
        }
        
        expStrBuf.reset();

        byte b8 = (byte)  (val >>> 56);
        byte b7 = (byte) ((val >>> 48) & 0xFF);
        byte b6 = (byte) ((val >>> 40) & 0xFF);
        byte b5 = (byte) ((val >>> 32) & 0xFF);
        byte b4 = (byte) ((val >>> 24) & 0xFF);
        byte b3 = (byte) ((val >>> 16)  & 0xFF);
        byte b2 = (byte) ((val >>> 8)  & 0xFF);
        byte b1 = (byte)  (val & 0xFF);
        
        byte[] expected = {b1, b2, b3, b4, b5, b6, b7, b8};
        
        if ( val == Constants.UNSET_LONG ) {
            expStrBuf.append( "\000\000\000\000\000\000\000\000" );
        } else {
            expStrBuf.append( expected );
        }
    }


    public void testEncodeMultiByte( ) {

        doEncodeMultiByte( SecurityType.Equity );
        doEncodeMultiByte( SecurityType.Future );
        doEncodeMultiByte( SecurityType.Unknown );

        doEncodeMultiByte( Currency.Other );
        doEncodeMultiByte( Currency.GBP );
    }

    private void doEncodeMultiByte( MultiByteLookup code ) {
        _builder.start();

        String expected = new String( code.getVal() );
        
        _builder.encodeStringFixedWidth( code.getVal(), 0, expected.length() );
        
        chk( expected.getBytes() );
    }


    public void testEncodeViewString() {
        
        byte[] buf = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-?=+/.\"\'\\0123456789".getBytes();
        
        ViewString vStart = new ViewString( buf,  0, 5 );
        ViewString vMid   = new ViewString( buf, 10, 20 );
        ViewString vAll   = new ViewString( buf,  0, buf.length );
        
        ViewString vEnd   = new ViewString( buf, buf.length - 5, 5 );
        ViewString vEmpty = new ViewString( buf, 5, 0 );
        
        doEncodeString( vStart, "abcde" );
        doEncodeString( vMid,   "klmnopqrstuvwxyzABCD" );
        doEncodeString( vEnd,   "56789" );
        doEncodeString( vEmpty, "" );
        doEncodeString( vAll,   new String( buf ) );
    }

    public void testEncodeReusableString() {
        
        ReusableString r1 = new ReusableString( "abcde" );
        ReusableString r2 = new ReusableString( "klmnopqrstuvwxyzABCD" );
        ReusableString r3 = new ReusableString( "" );
        
        doEncodeString( r1, "abcde" );
        doEncodeString( r2, "klmnopqrstuvwxyzABCD" );
        doEncodeString( r3, "" );
    }

    private void doEncodeString( ZString zstr, String expectedVal ) {
        _builder.start();

        String expected = expectedVal + (char)0;
        
        _builder.encodeStringFixedWidth( zstr, expectedVal.length()+1 );
        
        chk( expected.getBytes() );
        
        _builder.start();

        _builder.encodeStringFixedWidth( zstr.getBytes(), zstr.getOffset(), expectedVal.length() );
        
        chk( expectedVal.getBytes() );
    }


    public void testEncodeTwoByte( byte[] code ) {
        doEncodeTwoByte( OrdRejReason.ExchangeClosed );
        doEncodeTwoByte( OrdRejReason.Other );
        doEncodeTwoByte( OrdRejReason.Unknown );

        doEncodeTwoByte( OrdRejReason.UnknownAccounts );
    }

    private void doEncodeTwoByte( TwoByteLookup code ) {
        _builder.start();

        String expected = new String( code.getVal() );
        
        _builder.encodeStringFixedWidth( code.getVal(), 0, 2 );
        
        chk( expected.getBytes() );
    }

    public void testEncodeUTCTimestamp() {
        doEncodeUTCTimestamp( 0,  0,  0,  60 );
        doEncodeUTCTimestamp( 0,  0,  0, 123 );
        doEncodeUTCTimestamp( 0,  0,  0,   0 );
        doEncodeUTCTimestamp( 0,  0,  0,   1 );
        doEncodeUTCTimestamp( 0,  0,  1,   0 );
        doEncodeUTCTimestamp( 1,  2,  3,   4 );
        doEncodeUTCTimestamp( 12, 00, 00, 001 );
        doEncodeUTCTimestamp( 23, 59, 59, 998 );
        doEncodeUTCTimestamp( 23, 59, 59, 999 );
    }

    private void doEncodeUTCTimestamp( int hh, int mm, int sec, int ms ) {
        
        int msFromStartOfDayUTC = (((((hh * 60) + mm) * 60) + sec) * 1000) + ms;
        
        TimeZoneCalculator tzCalc = new TimeZoneCalculator();
        tzCalc.setDateAsNow();
        
        long now = System.currentTimeMillis();
        int time = tzCalc.getTimeUTC( now );
        long midnightUTC = now - time;

        long expTime = (midnightUTC + msFromStartOfDayUTC);
        _builder.setTimeZoneCalculator( tzCalc );
        _builder.start();

        byte b8 = (byte)  (expTime >>> 56);
        byte b7 = (byte) ((expTime >>> 48) & 0xFF);
        byte b6 = (byte) ((expTime >>> 40) & 0xFF);
        byte b5 = (byte) ((expTime >>> 32) & 0xFF);
        byte b4 = (byte) ((expTime >>> 24) & 0xFF);
        byte b3 = (byte) ((expTime >>> 16)  & 0xFF);
        byte b2 = (byte) ((expTime >>> 8)  & 0xFF);
        byte b1 = (byte)  (expTime & 0xFF);
        
        byte[] expected = {b1, b2, b3, b4, b5, b6, b7, b8};
        
        _builder.encodeTimestampUTC( msFromStartOfDayUTC );
        
        chk( expected );
    }
}
