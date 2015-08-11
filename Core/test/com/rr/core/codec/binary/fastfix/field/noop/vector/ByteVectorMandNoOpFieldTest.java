/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.field.noop.vector;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.noop.vector.ByteVectorMandReaderNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.vector.ByteVectorMandWriterNoOp;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;

public class ByteVectorMandNoOpFieldTest extends BaseTestCase {
    private FastFixDecodeBuilder      _decoder = new FastFixDecodeBuilder();
    
    private FastFixBuilder      _encoder;
    private PresenceMapWriter   _mapWriter;
    private PresenceMapReader   _mapReader;
    
    private byte[]              _buf = new byte[8192];

    private ByteVectorMandReaderNoOp  _fieldReader;
    private ByteVectorMandWriterNoOp  _fieldWriter;
    
    @Override
    public void setUp(){


        _encoder    = new FastFixBuilder( _buf, 0 );
        _mapWriter  = new PresenceMapWriter( _encoder, 0, 1 );
        _mapReader  = new PresenceMapReader();
        
        _fieldReader = new ByteVectorMandReaderNoOp( "TestByteVector", 10018 );
        _fieldWriter = new ByteVectorMandWriterNoOp( "TestByteVector", 10018 );
    }
    
    @Override
    public String toString() {
        return new String( _buf, 0, _decoder.getNextFreeIdx() );
    }
    
    public void testVals() {
        checkField( "A", 3 );
        checkField( "", 2 );
        checkField( "AABBCCDDEE", 12 );
        checkField( "AABBCCDDEEZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", 75 );
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
        writeField( "ABCDEF",       8 );
        writeField( "",             9 );
        writeField( "ABCDEF",       16 );
        writeField( "ABCDEF",       23 );
        writeField( "ABCDEFGH",     32 );
        writeField( "ABC",          36 );
        writeField( "ZYXABC",       43 );
        writeField( "EDCBAZYXABC",  55 );
        writeField( "ABXABCDEF",    65 );
        writeField( "CDEF",         70 );
        writeField( "",             71 );
        writeField( "ZYX",          75 ); 

        _mapWriter.end();

        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( "ABCDEF",       8 );
        readValAndCheck( "",             9 );
        readValAndCheck( "ABCDEF",       16 );
        readValAndCheck( "ABCDEF",       23 );
        readValAndCheck( "ABCDEFGH",     32 );
        readValAndCheck( "ABC",          36 );
        readValAndCheck( "ZYXABC",       43 );
        readValAndCheck( "EDCBAZYXABC",  55 );
        readValAndCheck( "ABXABCDEF",    65 );
        readValAndCheck( "CDEF",         70 );
        readValAndCheck( "",             71 );
        readValAndCheck( "ZYX",          75 ); 
    }
}
