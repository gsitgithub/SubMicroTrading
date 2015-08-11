/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.field.copy.string;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.msgdict.copy.string.StringReaderCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.string.StringWriterCopy;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;

public class StringFieldTest extends BaseTestCase {
    private FastFixDecodeBuilder      _decoder = new FastFixDecodeBuilder();
    private FastFixBuilder      _encoder;
    private PresenceMapWriter   _mapWriter;
    private PresenceMapReader   _mapReader;
    
    private byte[]              _buf = new byte[8192];
    private StringReaderCopy    _fieldReader;
    private StringWriterCopy    _fieldWriter;
    
    @Override
    public void setUp(){


        _encoder    = new FastFixBuilder( _buf, 0 );
        _mapWriter  = new PresenceMapWriter( _encoder, 0, 1 );
        _mapReader  = new PresenceMapReader();
        
        _fieldReader = new StringReaderCopy( "TestULong", 10018 );
        _fieldWriter = new StringWriterCopy( "TestULong", 10018 );
        
    }
    
    @Override
    public String toString() {
        return new String( _buf, 0, _decoder.getNextFreeIdx() );
    }
    
    public void testVals() {
        checkField( "A" );
        checkField( "" );
        checkField( "AABBCCDDEE" );
        checkField( "AABBCCDDEEZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" );
    }

    private void checkField( String sVal ) {
        setUp(); // force reset
        
        ReusableString value = new ReusableString( sVal );
        
        _fieldWriter.write( _encoder, _mapWriter, value );
        _mapWriter.end();
        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( value.toString() );
    }

    private void readValAndCheck( String sExpValue ) {
        ReusableString expValue = new ReusableString( sExpValue );
        ReusableString value = new ReusableString();
        
        _fieldReader.read( _decoder, _mapReader, value );
        
        assertEquals( expValue, value );
    }

    public void testPrevCopy() {
        ReusableString v = new ReusableString();
        
        v.setValue( "ABCDEF" );
        _fieldWriter.write( _encoder, _mapWriter, v );
        assertEquals( v, _fieldWriter.getPreviousValue() );

        v.setValue( "" );
        _fieldWriter.write( _encoder, _mapWriter, v );
        assertEquals( v, _fieldWriter.getPreviousValue() );

        v.setValue( "ABCDEF" );
        _fieldWriter.write( _encoder, _mapWriter, v );
        assertEquals( v, _fieldWriter.getPreviousValue() );
        
        v.setValue( "ABCDEF" );
        _fieldWriter.write( _encoder, _mapWriter, v );
        assertEquals( v, _fieldWriter.getPreviousValue() );
        
        v.setValue( "ZYX" );
        _fieldWriter.write( _encoder, _mapWriter, v );
        assertEquals( v, _fieldWriter.getPreviousValue() );
        
        _mapWriter.end();

        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( "ABCDEF" );
        readValAndCheck( "" );
        readValAndCheck( "ABCDEF" );
        readValAndCheck( "ABCDEF" );
        readValAndCheck( "ZYX" );
    }
}
