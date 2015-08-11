/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

// original ReusableString is lost !!
// @NOTE add back all the lost tests

public class TestReusableString extends BaseTestCase {

    public void testDouble() {
        
        checkDouble( Constants.UNSET_DOUBLE, "[null]" );
        checkDouble( Constants.UNSET_LONG, "[null]" );
        checkDouble( 1234567890123.123456, "1234567890123" );
        checkDouble( Long.MAX_VALUE,       "9223372036854775807" );
        
        checkDouble( 0,                 "0.0" );
        checkDouble( 12345,             "12345.0" );
        checkDouble( 12345.765432,      "12345.765432" );
        checkDouble( 123456789.987654,  "123456789.987654" );
        checkDouble( 123456789.000001,  "123456789.000001" );
        checkDouble( 0.000001,          "0.000001" );
        checkDouble( 1.000001,          "1.000001" );
    }
    
    public void testInt() {
    
        checkInt( 0 );
        checkInt( 12345 );
        checkInt( Integer.MAX_VALUE );
    }
    
    public void testHex() {
        ReusableString s = new ReusableString();
        
        String testString = "ABCDEFGHIJ";
        byte[] buf = testString.getBytes(); 
        
        s.appendReadableHEX( buf, 1, 3 );
        
        assertEquals( "  B  C  D", s.toString() );
    }

    public void testReverse() {
        checkReverse( "A", "A" );
        checkReverse( "AB", "BA" );
        checkReverse( "ABC", "CBA" );
        checkReverse( "ABCD", "DCBA" );
        checkReverse( "ABCDE", "EDCBA" );
    }
    
    private void checkDouble( double val, String exp ) {
        ReusableString s = new ReusableString();
        s.append( val );
        assertEquals( exp, s.toString() );
    }

    private void checkInt( int val ) {
        ReusableString s = new ReusableString();
        s.append( val );
        
        assertEquals( "" + val, s.toString() );
    }

    private void checkReverse( String val, String expected ) {
        ReusableString s = new ReusableString();
        s.append( val );
        s.reverse();
        
        assertEquals( expected, s.toString() );
    }
}
