/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;

public class StringTest extends BaseTestCase {

    public void testBaseString() {
        
        ViewString b = new ViewString( "abc" );
        assertTrue( b.equals( "abc" ) );
        assertFalse( b.equals("abc ") );
        assertFalse( b.equals(" abc") );

        ViewString b2 = new ViewString( "abd" );
        ViewString b3 = new ViewString( b );
        assertTrue( b.compareTo( b2 ) < 0 );
        assertTrue( b2.compareTo( b ) > 0 );
        assertTrue( b.compareTo( b3 ) == 0 );
    }
    
    public void testReusableString() {
        
        ReusableString r1 = new ReusableString( "abc" );
        ReusableString r2 = new ReusableString( "def" );
        ReusableString r3 = new ReusableString( "g" );
        
        r1.append( r2 ).append( r3 );
        
        assertTrue( r1.equals( "abcdefg" ) );

        r1.reset();

        assertEquals( 0, r1.length() );
        
        char charVal = 'B';
        int  intVal1 = 123456789;
        double dblVal1 = 12345.6789;
        long longVal1 = 123456789033445566l;
        
        r1.append( (byte)0x41 );
        r1.append( charVal );
        r1.append( intVal1 );
        r1.append( dblVal1 );
        r1.append( "xyz" );
        r1.append( longVal1 );
        
        String expected = "AB12345678912345.6789xyz123456789033445566";
        
        assertTrue( r1 + " not equal to " + expected, r1.equals( expected ) );
        
        r2.append( "gh   " );
        r2.rtrim();
        
        assertTrue( r2.equals( "defgh" ) );
    }
    
    public void testNullNums() {
    
        ZString exp = new ViewString( "[null]" );
        
        ReusableString r = new ReusableString();
        r.append( Constants.UNSET_LONG );
        assertEquals( exp, r );

        r.reset();
        r.append( Constants.UNSET_INT );
        assertEquals( exp, r );
        
        r.reset();
        r.append( Constants.UNSET_DOUBLE );
        assertEquals( exp, r );
    }
    
    public void testStringOrdering() {
        
        ReusableString r1 = new ReusableString( "abcdefghijklmnopqrstuv" );
        ViewString     b1 = new ViewString( "abcdefghijklmnopqrstuw" );
        
        byte[]         s1 = "abcdefghijkklmnnopqrstu".getBytes();
        ViewString     v1 = new ViewString( s1, 0, s1.length );
        
        assertTrue( r1.compareTo( b1 ) < 0 );
        assertTrue( r1.compareTo( v1 ) > 0 );
        assertTrue( v1.compareTo( r1 ) < 0 );
        assertTrue( v1.compareTo( b1 ) < 0 );
        assertTrue( b1.compareTo( v1 ) > 0 );
        assertTrue( b1.compareTo( r1 ) > 0 );
    }
    
    public void testViewString() {
        String s = "8=FIX.4.4; 9=152; 35=D; 34=12243; 49=PROPA; 52=20100510-12:01:01.100; " +
                    "56=ME; 59=0; 22=5; 48=BT.L; 40=2; 54=1; 55=BT.L; 11=XX2100; 21=1; " +
                    "60=20100510-12:01:01; 38=50; 44=10.23; 10=345;";
        
        byte[] b = s.getBytes();
        
        ViewString msgType = createViewString( b, s, " 35=" );
        
        assertTrue( msgType.equals( "D" ) );
        
        ViewString security = createViewString( b, s, " 48=" );
        ViewString symbol   = createViewString( b, s, " 55=" );
        ViewString sender   = createViewString( b, s, " 49=" );
        ViewString checksum = createViewString( b, s, " 10=" );
        
        assertTrue( security.equals( "BT.L" ) );
        assertTrue( security.equals( symbol ) );
        assertTrue( sender.compareTo( symbol ) > 0 );
        assertTrue( checksum.equals( "345" ) );
    }

    private ViewString createViewString( byte[] buf, String msg, String key ) {
        
        int idx = msg.indexOf( key );
        
        assertTrue( idx >= 0 );
        
        int valStart = idx + key.length();
        
        int len = msg.indexOf( ';', idx ) - valStart;
        
        return new ViewString( buf, valStart, len );
    }
}
