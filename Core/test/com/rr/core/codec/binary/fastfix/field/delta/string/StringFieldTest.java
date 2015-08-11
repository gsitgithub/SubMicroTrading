/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.field.delta.string;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.msgdict.delta.string.StringReaderDelta;
import com.rr.core.codec.binary.fastfix.msgdict.delta.string.StringWriterDelta;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;

public class StringFieldTest extends BaseTestCase {
    private FastFixDecodeBuilder      _decoder = new FastFixDecodeBuilder();
    private FastFixBuilder      _encoder;
    private PresenceMapWriter   _mapWriter;
    private PresenceMapReader   _mapReader;
    
    private byte[]              _buf = new byte[8192];
    private StringReaderDelta    _fieldReader;
    private StringWriterDelta    _fieldWriter;
    
    @Override
    public void setUp(){


        _encoder    = new FastFixBuilder( _buf, 0 );
        _mapWriter  = new PresenceMapWriter( _encoder, 0, 1 );
        _mapReader  = new PresenceMapReader();
        
        _fieldReader = new StringReaderDelta( "TestULong", 10018 );
        _fieldWriter = new StringWriterDelta( "TestULong", 10018 );
        
    }
    
    @Override
    public String toString() {
        return new String( _buf, 0, _decoder.getNextFreeIdx() );
    }
    
    public void testVals() {
        checkField( "A", 3 );
        checkField( "", 3 );
        checkField( "AABBCCDDEE", 12 );
        checkField( "AABBCCDDEEZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", 74 );
    }

    private void checkField( String sVal, int expPostIndex ) {
        setUp(); // force reset
        
        ReusableString value = new ReusableString( sVal );
        
        _fieldWriter.write( _encoder, value );
        _mapWriter.end();
        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( value.toString(), expPostIndex );
    }

    private void readValAndCheck( String sExpValue, int expPostIndex ) {
        ReusableString expValue = new ReusableString( sExpValue );
        ReusableString value = new ReusableString();
        
        _fieldReader.read( _decoder, value );
        
        assertEquals( expValue, value );

        assertEquals( expPostIndex, _decoder.getCurrentIndex() );
    }

    private void writeField( String val, int expPostIdx ) {
        ReusableString v = new ReusableString();
        
        v.setValue( val );
        _fieldWriter.write( _encoder, v );
        assertEquals( v, _fieldWriter.getPreviousValue() );
        assertEquals( expPostIdx, _encoder.getCurrentIndex() );
    }
    
    public void testPrevCopy() {
        writeField( "ABCDEF",       8 );
        writeField( "",             10 );
        writeField( "ABCDEF",       17 );
        writeField( "ABCDEF",       19 );
        writeField( "ABCDEFGH",     22 );
        writeField( "ABC",          24 );
        writeField( "ZYXABC",       28 );
        writeField( "EDCBAZYXABC",  34 );
        writeField( "ABXABCDEF",    44 );
        writeField( "CDEF",         46 );
        writeField( "",             48 );
        writeField( "ZYX",          52 ); 

        _mapWriter.end();

        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( "ABCDEF",       8 );
        readValAndCheck( "",             10 );
        readValAndCheck( "ABCDEF",       17 );
        readValAndCheck( "ABCDEF",       19 );
        readValAndCheck( "ABCDEFGH",     22 );
        readValAndCheck( "ABC",          24 );
        readValAndCheck( "ZYXABC",       28 );
        readValAndCheck( "EDCBAZYXABC",  34 );
        readValAndCheck( "ABXABCDEF",    44 );
        readValAndCheck( "CDEF",         46 );
        readValAndCheck( "",             48 );
        readValAndCheck( "ZYX",          52 ); 
    }
}
