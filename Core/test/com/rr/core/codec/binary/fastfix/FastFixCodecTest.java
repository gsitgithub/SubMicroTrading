/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix;

import java.math.BigDecimal;
import java.math.MathContext;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.utils.HexUtils;

/**
 * no point rebuilding encoder logic to test decoder, instead test both
 *
 * There are some edge cases with dealing with MAX_LONG for optional fields where incrementing the value will wrap it
 * but not sure what exchange behaviour requires so leave for now 
 *
 * timestamps are ulong64 bit 
 * 
 * @author Richard Rose
 */

public class FastFixCodecTest extends BaseTestCase {

    private FastFixDecodeBuilder _decoder = new FastFixDecodeBuilder();
    private FastFixBuilder       _encoder;
    
    private byte[]               _bufDecode = new byte[8192];
    private byte[]               _bufEncode;
    
    private MathContext          _mc;

    @Override
    public void setUp(){
        _bufEncode = new byte[8192];
        _encoder   = new FastFixBuilder( _bufEncode, 0 );
        _mc        = new MathContext( 10 );
    }
    
    @Override
    public String toString() {
        return new String( _bufDecode, 0, _decoder.getNextFreeIdx() );
    }

    public void testDecodeString() {
        doDecodeString( "",           "00 80" );
        doDecodeString( "A",          "C1" );
        doDecodeString( "AB",         "41 C2" );
        doDecodeString( "ABCDEF",     "41 42 43 44 45 C6" );
    }
    
    public void testDecodeNullString() {

        ReusableString dest = new ReusableString( 1 );
        
        _encoder.clear();
        _encoder.encodeString( null );
        
        logEncodedValue( null );

        assertEquals( "80",  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        _decoder.decodeString( dest );
        
        assertEquals( 0, dest.length() ); // ReusableString doesnt have null just empty string
    }

    private void doDecodeString( final String sVal, String exp ) {

        ZString val = new ViewString(sVal);
        ReusableString dest = new ReusableString( 1 );
        
        _encoder.clear();
        _encoder.encodeString( val );
        
        logEncodedValue( sVal );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        _decoder.decodeString( dest );
        
        assertEquals( val, dest );
    }

    public void testDecodeOptionalByteVector() {
        doDecodeOptionalByteVector( "",           "81" );
        doDecodeOptionalByteVector( "A",          "82 41" );
        doDecodeOptionalByteVector( "AB",         "83 41 42" );
        doDecodeOptionalByteVector( "ABCDEF",     "87 41 42 43 44 45 46" );
    }
    
    private void doDecodeOptionalByteVector( final String sVal, String exp ) {

        ZString val = new ViewString(sVal);
        ReusableString dest = new ReusableString( 1 );
        
        _encoder.clear();
        _encoder.encodeOptionalByteVector( val.getBytes(), 0, sVal.length() );
        
        logEncodedValue( sVal );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        _decoder.decodeOptionalByteVector( dest );
        
        assertEquals( val, dest );
    }

    public void testDecodeMandByteVector() {
        doDecodeMandByteVector( "",           "80" );
        doDecodeMandByteVector( "A",          "81 41" );
        doDecodeMandByteVector( "AB",         "82 41 42" );
        doDecodeMandByteVector( "ABCDEF",     "86 41 42 43 44 45 46" );
    }
    
    private void doDecodeMandByteVector( final String sVal, String exp ) {

        ZString val = new ViewString(sVal);
        ReusableString dest = new ReusableString( 1 );
        
        _encoder.clear();
        _encoder.encodeMandByteVector( val.getBytes(), 0, sVal.length() );
        
        logEncodedValue( sVal );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        _decoder.decodeMandByteVector( dest );
        
        assertEquals( val, dest );
    }

    public void testDecodeOptionalNullByteVector() {

        ReusableString dest = new ReusableString( 1 );
        
        _encoder.clear();
        _encoder.encodeOptionalByteVector( null, 0, 0 );
        
        logEncodedValue( null );

        assertEquals( "80",  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        _decoder.decodeOptionalByteVector( dest );
        
        assertEquals( 0, dest.length() ); // ReusableString doesnt have null just empty string
    }

    public void testDecodeOptionalULong() {
        doDecodeOptionalULong( 99999,                   "06 0D A0" );
        doDecodeOptionalULong( 98765432101234l,         "16 3A 39 73 7C 32 F3" );
        doDecodeOptionalULong( 0,                       "81" );
        doDecodeOptionalULong( Long.MAX_VALUE,          "01 00 00 00 00 00 00 00 00 80" );
        doDecodeOptionalULong( 0xFFFFFFFFFFFFFFFEL,     "01 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        doDecodeOptionalULong( Long.MIN_VALUE+1,        "01 00 00 00 00 00 00 00 00 82" );
        doDecodeOptionalULong( Constants.UNSET_LONG,    "80" );
        doDecodeOptionalULong( 1000,                    "07 E9" );
    }

    private void doDecodeOptionalULong( final long val, String exp ) {

        _encoder.clear();
        _encoder.encodeOptionalULong( val );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        long chkEncoder = _decoder.decodeOptionalULong();
        
        assertEquals( val, chkEncoder );
    }

    public void testDecodeOptionalULongOverflow() {
        doDecodeOptionalULongOverflow( 99999,                   false, "06 0D A0" );
        doDecodeOptionalULongOverflow( 98765432101234l,         false, "16 3A 39 73 7C 32 F3" );
        doDecodeOptionalULongOverflow( 0,                       false, "81" );
        doDecodeOptionalULongOverflow( Long.MAX_VALUE,          false, "01 00 00 00 00 00 00 00 00 80" );
        doDecodeOptionalULongOverflow( 0xFFFFFFFFFFFFFFFEL,     false, "01 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        doDecodeOptionalULongOverflow( Long.MIN_VALUE+1,        false, "01 00 00 00 00 00 00 00 00 82" );
        doDecodeOptionalULongOverflow( Constants.UNSET_LONG,    false, "80" );
        doDecodeOptionalULongOverflow( 1000,                    false, "07 E9" );

        doDecodeOptionalULongOverflow( 99999,                   true, "20 00 00 00 00 00 00 06 0D A0" );
        doDecodeOptionalULongOverflow( 98765432101234l,         true, "20 00 00 16 3A 39 73 7C 32 F3" );
        doDecodeOptionalULongOverflow( 0,                       true, "20 00 00 00 00 00 00 00 00 81" );
        doDecodeOptionalULongOverflow( Long.MAX_VALUE,          true, "21 00 00 00 00 00 00 00 00 80" );
        doDecodeOptionalULongOverflow( 0xFFFFFFFFFFFFFFFEL,     true, "21 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        doDecodeOptionalULongOverflow( Long.MIN_VALUE+1,        true, "21 00 00 00 00 00 00 00 00 82" );
        doDecodeOptionalULongOverflow( 1000,                    true, "20 00 00 00 00 00 00 00 07 E9" );
        doDecodeOptionalULongOverflow( Constants.UNSET_LONG,    true, "80" );
    }

    private void doDecodeOptionalULongOverflow( final long val, boolean overflow, String exp ) {

        _encoder.clear();
        _encoder.encodeOptionalULongOverflow( val, overflow );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        if ( !overflow || val == Constants.UNSET_LONG ) {
            long chkEncoder = _decoder.decodeOptionalULongOverflow();
            
            assertEquals( val, chkEncoder );
            
        } else {
            try {
                _decoder.decodeOptionalULongOverflow();
                
                assertTrue( false ); // FAIL - should be exception
                
            } catch( RuntimeDecodingException e ) {
                // ok
            }
        }
        
    }

    public void testDecodeULong() {
        doDecodeULong( 99999,                   "06 0D 9F" );
        doDecodeULong( 98765432101234l,         "16 3A 39 73 7C 32 F2" );
        doDecodeULong( 0,                       "80" );
        doDecodeULong( Long.MAX_VALUE,          "00 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        doDecodeULong( 0xFFFFFFFFFFFFFFFEL,     "01 7F 7F 7F 7F 7F 7F 7F 7F FE" );
        doDecodeULong( Long.MIN_VALUE+1,        "01 00 00 00 00 00 00 00 00 81" );
        doDecodeULong( Constants.UNSET_LONG,    "01 00 00 00 00 00 00 00 00 80" ); // not real NULL
        doDecodeULong( 1000,                    "07 E8" );
    }

    private void doDecodeULong( final long val, String exp ) {

        _encoder.clear();
        _encoder.encodeMandULong( val );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        long chkEncoder = _decoder.decodeMandULong();
        
        assertEquals( val, chkEncoder );
    }

    public void testDecodeOptionalLong() {
        doDecodeOptionalLong( 99999,                   "06 0D A0" );
        doDecodeOptionalLong( 98765432101234l,         "16 3A 39 73 7C 32 F3" );
        doDecodeOptionalLong( 0,                       "81" );
        doDecodeOptionalLong( Long.MAX_VALUE,          "01 00 00 00 00 00 00 00 00 80" );
        doDecodeOptionalLong( Long.MAX_VALUE-1,        "00 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        doDecodeOptionalLong( 0xFFFFFFFFFFFFFFFEL,     "FE" );
        doDecodeOptionalLong( Long.MIN_VALUE+1,        "7F 00 00 00 00 00 00 00 00 81" );
        doDecodeOptionalLong( Constants.UNSET_LONG,    "80" );
        doDecodeOptionalLong( 1000,                    "07 E9" );
        doDecodeOptionalLong( -1000,                   "78 98" );
        doDecodeOptionalLong( -1,                      "FF" );
    }

    private void doDecodeOptionalLong( final long val, String exp ) {

        _encoder.clear();
        _encoder.encodeOptionalLong( val );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        long chkEncoder = _decoder.decodeOptionalLong();
        
        assertEquals( val, chkEncoder );
    }

    public void testDecodeOptionalLongOverflow() {
        doDecodeOptionalLongOverflow( 99999,                   false, "06 0D A0" );
        doDecodeOptionalLongOverflow( 98765432101234l,         false, "16 3A 39 73 7C 32 F3" );
        doDecodeOptionalLongOverflow( 0,                       false, "81" );
        doDecodeOptionalLongOverflow( -2,                      false, "FE" );
        doDecodeOptionalLongOverflow( Long.MIN_VALUE+1,        false, "5F 00 00 00 00 00 00 00 00 81" );
        doDecodeOptionalLongOverflow( 1000,                    false, "07 E9" );
        doDecodeOptionalLongOverflow( -1000,                   false, "78 98" );
        doDecodeOptionalLongOverflow( -1,                      false, "FF" );

        doDecodeOptionalLongOverflow( Constants.UNSET_LONG,    false, "80" ); 

        doDecodeOptionalLongOverflow( 99999,                   true, "20 00 00 00 00 00 00 06 0D A0" );
        doDecodeOptionalLongOverflow( 98765432101234l,         true, "20 00 00 16 3A 39 73 7C 32 F3" );
        doDecodeOptionalLongOverflow( 0,                       true, "20 00 00 00 00 00 00 00 00 81" );
        doDecodeOptionalLongOverflow( -2,                      true, "7F 7F 7F 7F 7F 7F 7F 7F 7F FE" );
        doDecodeOptionalLongOverflow( Long.MIN_VALUE+1,        true, "7F 00 00 00 00 00 00 00 00 81" );
        doDecodeOptionalLongOverflow( Constants.UNSET_LONG,    true, "80" ); 
        doDecodeOptionalLongOverflow( 1000,                    true, "20 00 00 00 00 00 00 00 07 E9" );
        doDecodeOptionalLongOverflow( -1000,                   true, "7F 7F 7F 7F 7F 7F 7F 7F 78 98" );
        doDecodeOptionalLongOverflow( -1,                      true, "7F 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        
        doDecodeOptionalLongOverflow( Long.MAX_VALUE,          true, "21 00 00 00 00 00 00 00 00 80" );
        doDecodeOptionalLongOverflow( Long.MAX_VALUE,          false, "01 00 00 00 00 00 00 00 00 80" );
    }

    private void doDecodeOptionalLongOverflow( final long val, boolean overflow, String exp ) {

        _encoder.clear();
        _encoder.encodeOptionalLongOverflow( val, overflow );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        if ( !overflow || val == Constants.UNSET_LONG ) {
            long chkEncoder = _decoder.decodeOptionalLongOverflow();
            
            assertEquals( val, chkEncoder );
            
        } else {
            try {
                _decoder.decodeOptionalLongOverflow();
                
                assertTrue( false ); // FAIL - should be exception
                
            } catch( RuntimeDecodingException e ) {
                // ok
            }
        }
        
    }

    public void testDecodeLong() {
        doDecodeLong( 99999,                   "06 0D 9F" );
        doDecodeLong( 98765432101234l,         "16 3A 39 73 7C 32 F2" );
        doDecodeLong( 0,                       "80" );
        doDecodeLong( Long.MAX_VALUE,          "00 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        doDecodeLong( -2,                      "FE" );
        doDecodeLong( Long.MIN_VALUE+1,        "7F 00 00 00 00 00 00 00 00 81" );
        doDecodeLong( Constants.UNSET_LONG,    "7F 00 00 00 00 00 00 00 00 80" ); // not real NULL
        doDecodeLong( 1000,                    "07 E8" );
        doDecodeLong( -1000,                   "78 98" );
        doDecodeLong( -1,                      "FF" );
    }

    private void doDecodeLong( final long val, String exp ) {

        _encoder.clear();
        _encoder.encodeMandLong( val );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        long chkEncoder = _decoder.decodeMandLong();
        
        assertEquals( val, chkEncoder );
    }

    public void testDecodeLongOverflow() {
        doDecodeLongOverflow( 99999,                   false, "06 0D 9F" );
        doDecodeLongOverflow( 98765432101234l,         false, "16 3A 39 73 7C 32 F2" );
        doDecodeLongOverflow( 0,                       false, "80" );
        doDecodeLongOverflow( Long.MAX_VALUE,          false, "00 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        doDecodeLongOverflow( -2,                      false, "FE" );
        doDecodeLongOverflow( Long.MIN_VALUE+1,        false, "5F 00 00 00 00 00 00 00 00 81" );
        doDecodeLongOverflow( Constants.UNSET_LONG,    false, "5F 00 00 00 00 00 00 00 00 80" ); // not real NULL
        doDecodeLongOverflow( 1000,                    false, "07 E8" );
        doDecodeLongOverflow( -1000,                   false, "78 98" );
        doDecodeLongOverflow( -1,                      false, "FF" );

        doDecodeLongOverflow( 99999,                   true, "20 00 00 00 00 00 00 06 0D 9F" );
        doDecodeLongOverflow( 98765432101234l,         true, "20 00 00 16 3A 39 73 7C 32 F2" );
        doDecodeLongOverflow( 0,                       true, "20 00 00 00 00 00 00 00 00 80" );
        doDecodeLongOverflow( Long.MAX_VALUE,          true, "20 7F 7F 7F 7F 7F 7F 7F 7F FF" );
        doDecodeLongOverflow( -2,                      true, "7F 7F 7F 7F 7F 7F 7F 7F 7F FE" );
        doDecodeLongOverflow( Long.MIN_VALUE+1,        true, "7F 00 00 00 00 00 00 00 00 81" );
        doDecodeLongOverflow( Constants.UNSET_LONG,    true, "7F 00 00 00 00 00 00 00 00 80" ); // not real NULL
        doDecodeLongOverflow( 1000,                    true, "20 00 00 00 00 00 00 00 07 E8" );
        doDecodeLongOverflow( -1000,                   true, "7F 7F 7F 7F 7F 7F 7F 7F 78 98" );
        doDecodeLongOverflow( -1,                      true, "7F 7F 7F 7F 7F 7F 7F 7F 7F FF" );
    }

    private void doDecodeLongOverflow( final long val, boolean overflow, String exp ) {

        _encoder.clear();
        _encoder.encodeMandLongOverflow( val, overflow );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        if ( !overflow ) {
            long chkEncoder = _decoder.decodeMandLongOverflow();
            
            assertEquals( val, chkEncoder );
            
        } else {
            try {
                _decoder.decodeMandLongOverflow();
                
                assertTrue( false ); // FAIL - should be exception
                
            } catch( RuntimeDecodingException e ) {
                // ok
            }
        }
        
    }

    public void testDecodeOptionalUInt() {
        doDecodeOptionalUInt( 99999,                  "06 0D A0" );
        doDecodeOptionalUInt( 876543212,              "03 21 7B 79 ED" );
        doDecodeOptionalUInt( 0,                      "81" );
        doDecodeOptionalUInt( Integer.MAX_VALUE,      "08 00 00 00 80" );
        doDecodeOptionalUInt( 0xFFFFFFFE,             "0F 7F 7F 7F FF" );
        doDecodeOptionalUInt( Integer.MIN_VALUE+1,    "08 00 00 00 82" );
        doDecodeOptionalUInt( Constants.UNSET_INT,    "80" );
        doDecodeOptionalUInt( 1000,                   "07 E9" );
    }

    private void doDecodeOptionalUInt( final int val, String exp ) {

        _encoder.clear();
        _encoder.encodeOptionalUInt( val );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        int chkEncoder = _decoder.decodeOptionalUInt();
        
        assertEquals( val, chkEncoder );
    }

    public void testDecodeOptionalUIntOverflow() {
        doDecodeOptionalUIntOverflow( 99999,                  false, "06 0D A0" );
        doDecodeOptionalUIntOverflow( 876543212,              false, "03 21 7B 79 ED" );
        doDecodeOptionalUIntOverflow( 0,                      false, "81" );
        doDecodeOptionalUIntOverflow( Integer.MAX_VALUE,      false, "08 00 00 00 80" );
        doDecodeOptionalUIntOverflow( 0xFFFFFFFE,             false, "0F 7F 7F 7F FF" );
        doDecodeOptionalUIntOverflow( Integer.MIN_VALUE+1,    false, "08 00 00 00 82" );
        doDecodeOptionalUIntOverflow( Constants.UNSET_INT,    false, "80" );
        doDecodeOptionalUIntOverflow( 1000,                   false, "07 E9" );

        doDecodeOptionalUIntOverflow( 99999,                  true, "20 00 06 0D A0" );
        doDecodeOptionalUIntOverflow( 876543212,              true, "23 21 7B 79 ED" );
        doDecodeOptionalUIntOverflow( 0,                      true, "20 00 00 00 81" );
        doDecodeOptionalUIntOverflow( Integer.MAX_VALUE,      true, "28 00 00 00 80" );
        doDecodeOptionalUIntOverflow( 0xFFFFFFFE,             true, "2F 7F 7F 7F FF" );
        doDecodeOptionalUIntOverflow( Integer.MIN_VALUE+1,    true, "28 00 00 00 82" );
        doDecodeOptionalUIntOverflow( Constants.UNSET_INT,    true, "80" );
        doDecodeOptionalUIntOverflow( 1000,                   true, "20 00 00 07 E9" );
    }

    private void doDecodeOptionalUIntOverflow( final int val, boolean overflow, String exp ) {

        _encoder.clear();
        _encoder.encodeOptionalUIntOverflow( val, overflow );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        if ( !overflow || val == Constants.UNSET_INT ) {
            int chkEncoder = _decoder.decodeOptionalUIntOverflow();
            
            assertEquals( val, chkEncoder );
            
        } else {
            try {
                _decoder.decodeOptionalUIntOverflow();
                
                assertTrue( false ); // FAIL - should be exception
                
            } catch( RuntimeDecodingException e ) {
                // ok
            }
        }
    }

    public void testDecodeUInt() {
        doDecodeUInt( 1,                      "81" );
        doDecodeUInt( 99999,                  "06 0D 9F" );
        doDecodeUInt( 543210123,              "02 03 02 75 8B" );
        doDecodeUInt( 0,                      "80" );
        doDecodeUInt( Integer.MAX_VALUE,      "07 7F 7F 7F FF" );
        doDecodeUInt( -1,                     "0F 7F 7F 7F FF" );
        doDecodeUInt( Integer.MIN_VALUE+1,    "08 00 00 00 81" );
        doDecodeUInt( Constants.UNSET_INT,    "08 00 00 00 80" ); // not real NULL
        doDecodeUInt( 1000,                   "07 E8" );
    }

    private void doDecodeUInt( final int val, String exp ) {

        _encoder.clear();
        _encoder.encodeMandUInt( val );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        int chkEncoder = _decoder.decodeMandUInt();
        
        assertEquals( val, chkEncoder );
    }

    public void testDecodeOptionalInt() {
        doDecodeOptionalInt( 99999,                   "06 0D A0" );
        doDecodeOptionalInt( 987654321,               "03 56 79 51 B2" );
        doDecodeOptionalInt( 0,                       "81" );
        doDecodeOptionalInt( Integer.MAX_VALUE-1,     "07 7F 7F 7F FF" );
        doDecodeOptionalInt( -2,                      "FE" );
        doDecodeOptionalInt( Integer.MIN_VALUE+1,     "78 00 00 00 81" );
        doDecodeOptionalInt( Constants.UNSET_INT,     "80" );
        doDecodeOptionalInt( 1000,                    "07 E9" );
        doDecodeOptionalInt( -1000,                   "78 98" );
        doDecodeOptionalInt( -1,                      "FF" );
    }

    private void doDecodeOptionalInt( final int val, String exp ) {

        _encoder.clear();
        _encoder.encodeOptionalInt( val );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        int chkEncoder = _decoder.decodeOptionalInt();
        
        assertEquals( val, chkEncoder );
    }

    public void testDecodeOptionalIntOverflow() {
        doDecodeOptionalIntOverflow( 99999,                   false, "06 0D A0" );
        doDecodeOptionalIntOverflow( 987654321,               false, "03 56 79 51 B2" );
        doDecodeOptionalIntOverflow( 0,                       false, "81" );
        doDecodeOptionalIntOverflow( Integer.MAX_VALUE-1,     false, "07 7F 7F 7F FF" );
        doDecodeOptionalIntOverflow( -2,                      false, "FE" );
        doDecodeOptionalIntOverflow( Integer.MIN_VALUE+1,     false, "58 00 00 00 81" );
        doDecodeOptionalIntOverflow( Constants.UNSET_INT,     false, "80" );
        doDecodeOptionalIntOverflow( 1000,                    false, "07 E9" );
        doDecodeOptionalIntOverflow( -1000,                   false, "78 98" );
        doDecodeOptionalIntOverflow( -1,                      false, "FF" );

        doDecodeOptionalIntOverflow( 99999,                   true, "20 00 06 0D A0" );
        doDecodeOptionalIntOverflow( 987654321,               true, "23 56 79 51 B2" );
        doDecodeOptionalIntOverflow( 0,                       true, "20 00 00 00 81" );
        doDecodeOptionalIntOverflow( Integer.MAX_VALUE-1,     true, "27 7F 7F 7F FF" );
        doDecodeOptionalIntOverflow( -2,                      true, "7F 7F 7F 7F FE" );
        doDecodeOptionalIntOverflow( Integer.MIN_VALUE+1,     true, "78 00 00 00 81" );
        doDecodeOptionalIntOverflow( Constants.UNSET_INT,     true, "80" );
        doDecodeOptionalIntOverflow( 1000,                    true, "20 00 00 07 E9" );
        doDecodeOptionalIntOverflow( -1000,                   true, "7F 7F 7F 78 98" );
        doDecodeOptionalIntOverflow( -1,                      true, "7F 7F 7F 7F FF" );
    }

    private void doDecodeOptionalIntOverflow( final int val, boolean overflow, String exp ) {

        _encoder.clear();
        _encoder.encodeOptionalIntOverflow( val, overflow );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        if ( !overflow || val == Constants.UNSET_INT ) {
            int chkEncoder = _decoder.decodeOptionalIntOverflow();
            
            assertEquals( val, chkEncoder );
        } else {
            try {
                _decoder.decodeOptionalIntOverflow();
                
                assertTrue( false ); // FAIL - should be exception
                
            } catch( RuntimeDecodingException e ) {
                // ok
            }
        }
        
    }

    public void testDecodeInt() {
        doDecodeInt( (1<<1)+1,                "83" );
        doDecodeInt( (1<<6)+1,                "00 C1" );
        doDecodeInt( (1<<7)+1,                "01 81" );
        doDecodeInt( (1<<8)+1,                "02 81" );
        doDecodeInt( (1<<12)+1,               "20 81" );
        doDecodeInt( (1<<13)+1,               "00 40 81" );
        doDecodeInt( (1<<14)+1,               "01 00 81" );
        doDecodeInt( 99999,                   "06 0D 9F" );
        doDecodeInt( 987654321,               "03 56 79 51 B1" );
        doDecodeInt( 0,                       "80" );
        doDecodeInt( Integer.MAX_VALUE,       "07 7F 7F 7F FF" );
        doDecodeInt( -2,                      "FE" );
        doDecodeInt( Integer.MIN_VALUE+1,     "78 00 00 00 81" );
        doDecodeInt( Constants.UNSET_INT,     "78 00 00 00 80" ); // not real NULL
        doDecodeInt( 1000,                    "07 E8" );
        doDecodeInt( -1000,                   "78 98" );
        doDecodeInt( -1,                      "FF" );
    }

    private void doDecodeInt( final int val, String exp ) {

        _encoder.clear();
        _encoder.encodeMandInt( val );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        int chkEncoder = _decoder.decodeMandInt();
        
        assertEquals( val, chkEncoder );
    }
    
    public void testDecodeIntOverflow() {
        doDecodeIntOverflow( (1<<1)+1,                false, "83" );
        doDecodeIntOverflow( (1<<6)+1,                false, "00 C1" );
        doDecodeIntOverflow( (1<<7)+1,                false, "01 81" );
        doDecodeIntOverflow( (1<<8)+1,                false, "02 81" );
        doDecodeIntOverflow( (1<<12)+1,               false, "20 81" );
        doDecodeIntOverflow( (1<<13)+1,               false, "00 40 81" );
        doDecodeIntOverflow( (1<<14)+1,               false, "01 00 81" );
        doDecodeIntOverflow( 99999,                   false, "06 0D 9F" );
        doDecodeIntOverflow( 987654321,               false, "03 56 79 51 B1" );
        doDecodeIntOverflow( 0,                       false, "80" );
        doDecodeIntOverflow( Integer.MAX_VALUE,       false, "07 7F 7F 7F FF" );
        doDecodeIntOverflow( -2,                      false, "FE" );
        doDecodeIntOverflow( Integer.MIN_VALUE,       false, "58 00 00 00 80" );
        doDecodeIntOverflow( Integer.MIN_VALUE+1,     false, "58 00 00 00 81" );
        doDecodeIntOverflow( Constants.UNSET_INT,     false, "58 00 00 00 80" ); // not real NULL
        doDecodeIntOverflow( 1000,                    false, "07 E8" );
        doDecodeIntOverflow( -1000,                   false, "78 98" );
        doDecodeIntOverflow( -1,                      false, "FF" );
        
        doDecodeIntOverflow( (1<<1)+1,                true, "20 00 00 00 83" );
        doDecodeIntOverflow( (1<<6)+1,                true, "20 00 00 00 C1" );
        doDecodeIntOverflow( (1<<7)+1,                true, "20 00 00 01 81" );
        doDecodeIntOverflow( (1<<8)+1,                true, "20 00 00 02 81" );
        doDecodeIntOverflow( (1<<12)+1,               true, "20 00 00 20 81" );
        doDecodeIntOverflow( (1<<13)+1,               true, "20 00 00 40 81" );
        doDecodeIntOverflow( (1<<14)+1,               true, "20 00 01 00 81" );
        doDecodeIntOverflow( 99999,                   true, "20 00 06 0D 9F" );
        doDecodeIntOverflow( 987654321,               true, "23 56 79 51 B1" );
        doDecodeIntOverflow( 0,                       true, "20 00 00 00 80" );
        doDecodeIntOverflow( Integer.MAX_VALUE,       true, "27 7F 7F 7F FF" );
        doDecodeIntOverflow( -2,                      true, "7F 7F 7F 7F FE" );
        doDecodeIntOverflow( Integer.MIN_VALUE+1,     true, "78 00 00 00 81" );
        doDecodeIntOverflow( Constants.UNSET_INT,     true, "78 00 00 00 80" ); // not real NULL
        doDecodeIntOverflow( 1000,                    true, "20 00 00 07 E8" );
        doDecodeIntOverflow( -1000,                   true, "7F 7F 7F 78 98" );
        doDecodeIntOverflow( -1,                      true, "7F 7F 7F 7F FF" );
    }

    private void doDecodeIntOverflow( final int val, boolean overflow, String exp ) {

        _encoder.clear();
        _encoder.encodeMandIntOverflow( val, overflow );
        
        logEncodedValue( val );

        assertEquals( exp,  bufToHexString() );        
        
        startDecoder( _bufEncode );
        
        if ( !overflow ) {
            int chkEncoder = _decoder.decodeMandIntOverflow();
            
            assertEquals( val, chkEncoder );
        } else {
            try {
                _decoder.decodeMandIntOverflow();
                
                assertTrue( false ); // FAIL - should be exception
                
            } catch( RuntimeDecodingException e ) {
                // ok
            }
        }
    }
    
    private String bufToHexString() {
        ReusableString s = new ReusableString( "" );
        
        HexUtils.bytesToHexSpacedString( _bufEncode, 0, _encoder.getCurrentIndex(), s );
        
        return s.toString();
    }

    private void logEncodedValue( String value ) {
        String s = bufToHexString();
        
        System.out.println( "val  " + value + "  as  \"" + s.toString() + "\"" );
    }

    private void logEncodedValue( long value ) {
        String s = bufToHexString();
        
        System.out.println( "val  " + value + "  as  \"" + s.toString() + "\"" );
    }

    
    private void startDecoder( byte[] expected ) {
        _decoder.start( bufferResult(expected), 0, expected.length );
    }

    public void testDecodeChar() {
        doDecodeChar( 'A' );
        doDecodeChar( 'Z' );
        doDecodeChar( 'a' );
        doDecodeChar( 'z' );
        doDecodeChar( '0' );
        doDecodeChar( '9' );
        doDecodeChar( '-' );
        doDecodeChar( '.' );
        doDecodeChar( '?' );
        doDecodeChar( '/' );
        doDecodeChar( Constants.UNSET_CHAR );
    }

    private void doDecodeChar( char cVal ) {

        _encoder.clear();
        try {
            _encoder.encodeChar( (byte)cVal );
            assertFalse( true );
        } catch( RuntimeEncodingException e ) {
            // expected
        }
        
        logEncodedValue( cVal );

        byte[] expected = { (byte) cVal };
        byte[] nullVal = { (byte)0x80 };
        
        if ( cVal == Constants.UNSET_BYTE ){
            expected = nullVal;
        }

        _decoder.start( expected, 0, expected.length );
        try {
            _decoder.decodeChar();
            assertFalse( true );
        } catch( RuntimeDecodingException e ) {
            // expected
        }
    }

    public void testDecodeByte() {
        doDecodeByte( (byte) 'A' );
    }

    public void testInsertByte() {
        _encoder.clear();
        _encoder.encodeString( "ABCDEFGHIJ".getBytes(), 0, 10 );
        _encoder.insertByte( 2 );
        assertEquals( "AB\000CDEFGHI\312", new String(_encoder.getBuffer(), 0, _encoder.getCurLength() ) );
    }
    
    public void testInsertBytes() {
        _encoder.clear();
        _encoder.encodeString( "ABCDEFGHIJ".getBytes(), 0, 10 );
        _encoder.insertBytes( 2, 1 );
        assertEquals( "AB\000CDEFGHI\312", new String(_encoder.getBuffer(), 0, _encoder.getCurLength() ) );

        _encoder.clear();
        _encoder.encodeString( "ABCDEFGHIJ".getBytes(), 0, 10 );
        _encoder.insertBytes( 2, 3 );
        assertEquals( "AB\000\000\000CDEFGHI\312", new String(_encoder.getBuffer(), 0, _encoder.getCurLength() ) );
        _encoder.clear();

        _encoder.encodeString( "ABCDEFGHIJ".getBytes(), 0, 10 );
        _encoder.insertBytes( 0, 3 );
        assertEquals( "\000\000\000ABCDEFGHI\312", new String(_encoder.getBuffer(), 0, _encoder.getCurLength() ) );
    }
    
    private void doDecodeByte( byte cVal ) {

        _encoder.clear();
        try {
            _encoder.encodeByte( cVal );
            assertFalse( true );
        } catch( RuntimeEncodingException e ) {
            // expected
        }
        
        logEncodedValue( cVal );

        byte[] expected = { cVal };
        byte[] nullVal = { (byte)0x80 };
        
        if ( cVal == Constants.UNSET_BYTE ){
            expected = nullVal;
        }

        _decoder.start( expected, 0, expected.length );
        try {
            _decoder.decodeByte();
            assertFalse( true );
        } catch( RuntimeDecodingException e ) {
            // expected
        }
    }

    public void testDecodeUByte() {
        doDecodeUByte( (byte) 'A' );
    }

    private void doDecodeUByte( byte cVal ) {
        _encoder.clear();
        try {
            _encoder.encodeUByte( cVal );
            assertFalse( true );
        } catch( RuntimeEncodingException e ) {
            // expected
        }
        
        logEncodedValue( cVal );

        byte[] expected = { cVal };
        byte[] nullVal = { (byte)0x80 };
        
        if ( cVal == Constants.UNSET_BYTE ){
            expected = nullVal;
        }

        _decoder.start( expected, 0, expected.length );
        try {
            _decoder.decodeUByte();
            assertFalse( true );
        } catch( RuntimeDecodingException e ) {
            // expected
        }
    }

    // the real buffer will be size of packet read, so need buffer small test arrays to avoid exception
    private byte[] bufferResult( byte[] expected ) {
        byte[] buffered = new byte[expected.length+10];
        System.arraycopy( expected, 0, buffered, 0, expected.length );
        return buffered;
    }

    public void testDecodePrice() {
        doDecodePrice( 0.2148, new ReusableString(8) );
    }

    public void doDecodePrice( double start, double max, double deltaInc ) {
        BigDecimal delta = new BigDecimal( Double.toString( deltaInc ), _mc );
        
        double testVal = start;

        ReusableString expStrBuf = new ReusableString(4);

        while( testVal < max ) {
            doDecodePrice( testVal, expStrBuf );
            
            BigDecimal tmp = new BigDecimal( Double.toString(testVal) );
            
            tmp     = tmp.add( delta );
            testVal = tmp.doubleValue();
        }
    }

    private void doDecodePrice( double val, ReusableString expStrBuf ) {

        _encoder.clear();
        try {
            _encoder.encodePrice( val );
            assertFalse( true );
        } catch( RuntimeEncodingException e ) {
            // expected
        }
        
        startDecoder( _bufEncode );
        
        try {
            _decoder.decodePrice();
            assertFalse( true );
        } catch( RuntimeDecodingException e ) {
            // expected
        }
    }
}
