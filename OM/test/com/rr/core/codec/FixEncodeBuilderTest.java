/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import java.math.BigDecimal;
import java.math.MathContext;

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

public class FixEncodeBuilderTest extends BaseTestCase {
    
    private byte[]               _buf = new byte[8192];
    private FixEncodeBuilderImpl _builder = new FixEncodeBuilderImpl( _buf, 0, (byte)'4', (byte)'4' );
    
    private MathContext          _mc;
    
    @Override
    public void setUp(){
        _mc  = new MathContext( 10 );
    }
    
    @Override
    public String toString() {
        return new String( _buf, 0, _builder.getNextFreeIdx() );
    }
    
    public void testEncodeAckStats() {
        doEncodeAckStats( 123456789, 123456789+12999,  123456789+12999+4321999,           123456789+12999+4321999+5999 );
        doEncodeAckStats( 123456789, 123456789+123999, 123456789+123999+987654321012999l, 123456789+123999+987654321012999l+5999 );
        doEncodeAckStats( 123456789, 123456789+12999,  123456789+12999+4321999,           123456789+12999+4321999+5999 );
        doEncodeAckStats( 0, 12, 16, 21 );
    }

    private void doEncodeAckStats( long orderIn, long orderOut, long ackIn, long ackOut) {

        _builder.start();

        int tag = Constants.FIX_TAG_ACK_STATS;
        String expected = "" + tag + "=" + ((orderOut-orderIn)/1024) + "," + 
                                           ((ackIn-orderOut)/1024) + "," + 
                                           ((ackOut-ackIn)/1024) + 
                                           (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeAckStats( orderIn, orderOut, ackIn, ackOut );
        
        chk( expected );
    }

    public void testEncodeBool() {
        doEncodeBool( 43, true );
        doEncodeBool( 43, false );
    }

    private void doEncodeBool( int tag, boolean val ) {
        _builder.start();

        char cVal = (val) ? 'Y' :'N';
        
        String expected = "" + tag + "=" + cVal + (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeBool( tag, val );
        
        chk( expected );
    }

    private void chk( String expected ) {
        assertEquals( _builder.getBodyOffset() + expected.length(), _builder.getNextFreeIdx() );

        String result = new String( _buf, _builder.getBodyOffset(), _builder.getCurLength() );
        
        assertEquals( expected, result );
    }

    private void chkWithEnvelope( String expected ) {
        assertEquals( expected.length(), _builder.getLength() );

        String result = new String( _buf, _builder.getOffset(), _builder.getLength() );
        
        assertEquals( expected, result );
    }

    public void testEncodeByte() {
        doEncodeByte( 98765, 'A' );
        doEncodeByte( 65536, 'Z' );
        doEncodeByte( 32768, 'a' );
        doEncodeByte( 999, 'z' );
        doEncodeByte( 999, '0' );
        doEncodeByte( 999, '9' );
        doEncodeByte( 999, '-' );
        doEncodeByte( 999, '.' );
        doEncodeByte( 999, '?' );
        doEncodeByte( 999, '/' );
    }

    private void doEncodeByte( int tag, char cVal ) {
        _builder.start();

        String expected = "" + tag + "=" + cVal + (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeByte( tag, (byte)cVal );
        
        chk( expected );
    }

    public void testEncodeInt() {
        doEncodeInt( 38, -1 );
        doEncodeInt( 38, -1000 );
        doEncodeInt( 38, 0 );
        doEncodeInt( 38, Integer.MAX_VALUE );
        doEncodeInt( 38, Integer.MIN_VALUE + 1 );
        doEncodeInt( 38, Integer.MIN_VALUE );
        doEncodeInt( 38, 1000 );
    }

    private void doEncodeInt( int tag, int val ) {
        _builder.start();

        String expected = "" + tag + "=" + val + (char)FixField.FIELD_DELIMITER;
        
        if ( val == Constants.UNSET_INT ) {
            expected = "";
        }
        
        _builder.encodeInt( tag, val );
        
        chk( expected );
    }

    public void testEncodeLong() {
        doEncodeLong( 38, -1 );
        doEncodeLong( 38, -1000 );
        doEncodeLong( 38, 99999 );
        doEncodeLong( 38, 98765432101234l );
        doEncodeLong( 38, 0 );
        doEncodeLong( 38, Long.MAX_VALUE );
        doEncodeLong( 38, Long.MIN_VALUE + 1 );
        doEncodeLong( 38, Long.MIN_VALUE );
        doEncodeLong( 38, 1000 );
    }

    private void doEncodeLong( int tag, long val ) {
        _builder.start();

        String expected = "" + tag + "=" + val + (char)FixField.FIELD_DELIMITER;

        if ( val == Constants.UNSET_LONG ) {
            expected = "";
        }
        
        _builder.encodeLong( tag, val );
        
        chk( expected );
    }

    public void testEncodeMultiByte( ) {

        doEncodeMultiByte( 167, SecurityType.Equity );
        doEncodeMultiByte( 167, SecurityType.Future );
        doEncodeMultiByte( 167, SecurityType.Unknown );

        doEncodeMultiByte( 15, Currency.Other );
        doEncodeMultiByte( 15, Currency.GBP );
        
    }

    private void doEncodeMultiByte( int tag, MultiByteLookup code ) {
        _builder.start();

        String expected = "" + tag + "=" + new String( code.getVal() ) + (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeMultiByte( tag, code );
        
        chk( expected );
    }

    public void testEncodePrice() {
        
//        doEncodePrice( 44, 0.0001, 9999.0, 0.0001 );  // whole required range takes hours to run
        
        doEncodePrice( 44,    0.0001,       2.0, 0.0001 );
        doEncodePrice( 44,  999.0001,    1002.0, 0.0001 );
        doEncodePrice( 44, 9999.0001,   10011.9, 0.0001 );

        doEncodeNullPrice( 44, Constants.UNSET_DOUBLE, new ReusableString() );
    }

    private void doEncodeNullPrice( int tag, double val, ReusableString expStrBuf ) {
        _builder.start();
        _builder.encodePrice( tag, val );        
        String result = getExpVal( _buf, _builder.getBodyOffset(), _builder.getCurLength() );
        getExpectedPrice( tag, val, expStrBuf );
        assertEquals( "Val=" + val + ", expected=" + expStrBuf + ", res=" + result, expStrBuf, result );
    }

    public void doEncodePrice( int tag, double start, double max, double deltaInc ) {
        BigDecimal delta = new BigDecimal( Double.toString( deltaInc ), _mc );
        
        double testVal = start;

        ReusableString expStrBuf = new ReusableString();

        while( testVal < max ) {
            doEncodePrice( 44, testVal, expStrBuf );
            
            BigDecimal tmp = new BigDecimal( Double.toString(testVal) );
            
            tmp     = tmp.add( delta );
            testVal = tmp.doubleValue();
        }
    }

    public void testFailPrice() {
        doEncodePrice( 44, 44.001, new ReusableString("44.001") );
    }

    private void doEncodePrice( int tag, double val, ReusableString expStrBuf ) {
        
        _builder.start();
        _builder.encodePrice( tag, val );        
        String result = getExpVal( _buf, _builder.getBodyOffset(), _builder.getCurLength() );
        getExpectedPrice( tag, val, expStrBuf );
        assertEquals( "Val=" + val + ", expected=" + expStrBuf + ", res=" + result, expStrBuf, result );

        double multUp   = (val + Constants.WEIGHT) * 100;
        _builder.start();
        _builder.encodePrice( tag, multUp );        
        result = getExpVal( _buf, _builder.getBodyOffset(), _builder.getCurLength() );
        getExpectedPrice( tag, val, 100, expStrBuf );
        assertEquals( "Val=" + val + ", multUp expected=" + expStrBuf + ", res=" + result, expStrBuf, result );

        double multDown = (val + Constants.WEIGHT) * 0.01;
        _builder.start();
        _builder.encodePrice( tag, multDown );        
        result = getExpVal( _buf, _builder.getBodyOffset(), _builder.getCurLength() );
        getExpectedPrice( tag, val, 0.01, expStrBuf );
        assertEquals( "Val=" + multDown + ", multDown expected=" + expStrBuf + ", res=" + result, expStrBuf, result );

    }

    private String getExpVal( byte[] buf, int offset, int len ) {
        
        String exp = new String( buf, offset, len );
        
        if( exp.length() == 0 ) return exp; // its NULL value so empty string

        if ( exp.indexOf( '.' ) == -1 ) {
            exp = exp.substring( 0, len-1 ) + ".0" + (char)FixField.FIELD_DELIMITER;
        }
        
        return exp;
    }

    private void getExpectedPrice( int tag, double val, ReusableString expStrBuf ) {
        
        if ( val == Constants.UNSET_DOUBLE ) {
            expStrBuf.reset();
            return;
        }
        
        BigDecimal price = new BigDecimal( val, _mc );
        
        expStrBuf.setValue( "" + tag + "=" + price.stripTrailingZeros().toPlainString().toString() );

        if ( expStrBuf.indexOf( '.' ) == -1 ) {
            expStrBuf.append( ".0" );
        }

        expStrBuf.append( FixField.FIELD_DELIMITER );
    }

    private void getExpectedPrice( int tag, double val, double factor, ReusableString expStrBuf ) {
        if ( val == Constants.UNSET_LONG ) {
            expStrBuf.reset();
            return;
        }

        BigDecimal price = new BigDecimal( val );
        BigDecimal mult = new BigDecimal( factor );
        
        expStrBuf.setValue( "" + tag + "=" + price.multiply( mult, _mc ).stripTrailingZeros().toPlainString().toString() );

        if ( expStrBuf.indexOf( '.' ) == -1 ) {
            expStrBuf.append( ".0" );
        }

        expStrBuf.append( FixField.FIELD_DELIMITER );
    }

    public void testEncodeViewString() {
        
        byte[] buf = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-?=+/.\"\'\\0123456789".getBytes();
        
        ViewString vStart = new ViewString( buf,  0, 5 );
        ViewString vMid   = new ViewString( buf, 10, 20 );
        ViewString vAll   = new ViewString( buf,  0, buf.length );
        
        ViewString vEnd   = new ViewString( buf, buf.length - 5, 5 );
        ViewString vEmpty = new ViewString( buf, 5, 0 );
        
        doEncodeString(  1, vStart, "abcde" );
        doEncodeString( 11, vMid,   "klmnopqrstuvwxyzABCD" );
        doEncodeString(  1, vEnd,   "56789" );
        doEncodeString(  1, vEmpty, "" );
        doEncodeString(  1, vAll,   new String( buf ) );
    }

    public void testEncodeReusableString() {
        
        ReusableString r1 = new ReusableString( "abcde" );
        ReusableString r2 = new ReusableString( "klmnopqrstuvwxyzABCD" );
        ReusableString r3 = new ReusableString( "" );
        
        doEncodeString(  1, r1, "abcde" );
        doEncodeString( 11, r2, "klmnopqrstuvwxyzABCD" );
        doEncodeString(  1, r3, "" );
    }

    private void doEncodeString( int tag, ZString zstr, String expectedVal ) {
        _builder.start();

        String expected = "" + tag + "=" + expectedVal + (char)FixField.FIELD_DELIMITER;
        
        if ( expectedVal.length()== 0 ) expected = "";
        
        _builder.encodeString( tag, zstr );
        
        chk( expected );
        
        _builder.start();

        _builder.encodeString( tag, zstr.getBytes(), zstr.getOffset(), zstr.length() );
        
        chk( expected );
    }


    public void testEncodeTwoByte( int tag, byte[] code ) {
        doEncodeTwoByte( 102, OrdRejReason.ExchangeClosed);
        doEncodeTwoByte( 102, OrdRejReason.Other );
        doEncodeTwoByte( 102, OrdRejReason.Unknown );

        doEncodeTwoByte( 103, OrdRejReason.UnknownAccounts );
    }

    private void doEncodeTwoByte( int tag, TwoByteLookup code ) {
        _builder.start();

        String expected = "" + tag + "=" + new String( code.getVal() ) + (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeTwoByte( tag, code.getVal() );
        
        chk( expected );
    }

    public void testEncodeUTCTimestamp() {
        doEncodeUTCTimestamp( 60,  0,  0,  0,  60, "00:00:00.060" );
        doEncodeUTCTimestamp( 60,  0,  0,  0, 123, "00:00:00.123" );
        doEncodeUTCTimestamp( 52,  0,  0,  0,   0, "00:00:00.000" );
        doEncodeUTCTimestamp( 52,  0,  0,  0,   1, "00:00:00.001" );
        doEncodeUTCTimestamp( 52,  0,  0,  1,   0, "00:00:01.000" );
        doEncodeUTCTimestamp( 52,  1,  2,  3,   4, "01:02:03.004" );
        doEncodeUTCTimestamp( 52, 12, 00, 00, 001, "12:00:00.001" );
        doEncodeUTCTimestamp( 52, 23, 59, 59, 998, "23:59:59.998" );
        doEncodeUTCTimestamp( 52, 23, 59, 59, 999, "23:59:59.999" );
    }

    private void doEncodeUTCTimestamp( int tag, int hh, int mm, int sec, int ms, String expectedTime ) {
        // YYYYMMDD-HH:MM:SS.sss 
        String dateStr = "20120619-";
        
        int msFromStartOfDayUTC = (((((hh * 60) + mm) * 60) + sec) * 1000) + ms;
        
        TimeZoneCalculator tzCalc = new TimeZoneCalculator();
        tzCalc.setDate( dateStr );
        
        _builder.setTimeZoneCalculator( tzCalc );
        
        _builder.start();

        String expected = "" + tag + "=" + dateStr + expectedTime + (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeUTCTimestamp( tag, msFromStartOfDayUTC );
        
        chk( expected );
    }

    public void testEncodeDate() {
        doEncodeDate( 20120101, "20120101" );
        doEncodeDate( 20141231, "20141231" );
        doEncodeDate( 20120607, "20120607" );
    }

    private void doEncodeDate( int yyyymmdd, String expectedTime ) {
        // YYYYMMDD 
        
        _builder.start();

        String expected = "10000=" + expectedTime + (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeDate( 10000, yyyymmdd );
        
        chk( expected );
    }

    public void testEncodeChecksum() {
        _builder.start();

        String expected = "8=FIX.4.4" + (char)FixField.FIELD_DELIMITER + 
                          "9=0"       + (char)FixField.FIELD_DELIMITER +
                          "10=200"    + (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeEnvelope();
        
        chkWithEnvelope( expected );

        _builder.start();
        _builder.encodeByte( 35, (byte)'D' );

        expected = "8=FIX.4.4" + (char)FixField.FIELD_DELIMITER + 
                   "9=5"       + (char)FixField.FIELD_DELIMITER +
                   "35=D"      + (char)FixField.FIELD_DELIMITER +
                   "10=183"    + (char)FixField.FIELD_DELIMITER;
        
        _builder.encodeEnvelope();
        
        chkWithEnvelope( expected );
    }
}
