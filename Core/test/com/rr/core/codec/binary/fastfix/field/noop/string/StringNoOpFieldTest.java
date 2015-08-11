/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.field.noop.string;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.noop.string.StringReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.string.StringWriterNoOp;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;

public class StringNoOpFieldTest extends BaseTestCase {
    private FastFixDecodeBuilder      _decoder = new FastFixDecodeBuilder();
    private FastFixBuilder      _encoder;
    private PresenceMapWriter   _mapWriter;
    private PresenceMapReader   _mapReader;
    
    private byte[]              _buf = new byte[8192];
    private StringReaderNoOp    _fieldReader;
    private StringWriterNoOp    _fieldWriter;
    
    @Override
    public void setUp(){


        _encoder    = new FastFixBuilder( _buf, 0 );
        _mapWriter  = new PresenceMapWriter( _encoder, 0, 1 );
        _mapReader  = new PresenceMapReader();
        
        _fieldReader = new StringReaderNoOp( "TestULong", 10018 );
        _fieldWriter = new StringWriterNoOp( "TestULong", 10018 );
        
    }
    
    @Override
    public String toString() {
        return new String( _buf, 0, _decoder.getNextFreeIdx() );
    }
    
    public void testVals() {
        checkField( "A", 2 );
        checkField( "", 2 );
        checkField( "AABBCCDDEE", 11 );
        checkField( "AABBCCDDEEZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", 73 );
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
        assertEquals( expPostIdx, _encoder.getCurrentIndex() );
    }
    
    public void testPrevCopy() {
        writeField( "ABCDEF",       7 );
        writeField( "",             8 );
        writeField( "ABCDEF",       14 );
        writeField( "ABCDEF",       20 );
        writeField( "ABCDEFGH",     28 );
        writeField( "ABC",          31 );
        writeField( "ZYXABC",       37 );
        writeField( "EDCBAZYXABC",  48 );
        writeField( "ABXABCDEF",    57 );
        writeField( "CDEF",         61 );
        writeField( "",             62 );
        writeField( "ZYX",          65 ); 

        _mapWriter.end();

        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( "ABCDEF",       7 );
        readValAndCheck( "",             8 );
        readValAndCheck( "ABCDEF",       14 );
        readValAndCheck( "ABCDEF",       20 );
        readValAndCheck( "ABCDEFGH",     28 );
        readValAndCheck( "ABC",          31 );
        readValAndCheck( "ZYXABC",       37 );
        readValAndCheck( "EDCBAZYXABC",  48 );
        readValAndCheck( "ABXABCDEF",    57 );
        readValAndCheck( "CDEF",         61 );
        readValAndCheck( "",             62 );
        readValAndCheck( "ZYX",          65 ); 
    }
}
