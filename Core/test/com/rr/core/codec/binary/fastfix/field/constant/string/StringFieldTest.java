/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.field.constant.string;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.constant.string.StringOptReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.string.StringOptWriterConst;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;

public class StringFieldTest extends BaseTestCase {
    private FastFixDecodeBuilder _decoder = new FastFixDecodeBuilder();
    private FastFixBuilder      _encoder;
    private PresenceMapWriter   _mapWriter;
    private PresenceMapReader   _mapReader;
    
    private byte[]              _buf = new byte[8192];
    private StringOptReaderConst    _fieldReader;
    private StringOptWriterConst    _fieldWriter;
    
    @Override
    public void setUp(){

        _encoder    = new FastFixBuilder( _buf, 0 );
        _mapWriter  = new PresenceMapWriter( _encoder, 0, 1 );
        _mapReader  = new PresenceMapReader();
        
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
        
        _fieldReader = new StringOptReaderConst( "TestULong", 10018, new ReusableString(sVal) );
        _fieldWriter = new StringOptWriterConst( "TestULong", 10018, new ReusableString(sVal) );
        
        ReusableString value = new ReusableString( sVal );
        
        _fieldWriter.write( _encoder, _mapWriter, true );
        _mapWriter.end();
        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( value.toString() );
    }

    private void readValAndCheck( String sExpValue ) {

        ReusableString expValue = new ReusableString( sExpValue );
        ReusableString value = new ReusableString();

        _fieldReader = new StringOptReaderConst( "TestULong", 10018, expValue );
        
        _fieldReader.read( _decoder, _mapReader, value );
        
        assertEquals( expValue, value );
    }
    
    private void write( String value ) {
        _fieldWriter = new StringOptWriterConst( "LongOptFieldTest", 10018, new ReusableString(value) );
        
        _fieldWriter.write( _encoder, _mapWriter, true );
        assertEquals( new ReusableString(value), _fieldWriter.getInitValue()  );
    }
    
    public void testPrevCopy() {
        write( "ABCDEF" );
        write( "" );
        write( "ABCDEF" );
        write( "ABCDEF" );
        write( "ZYX" );
        
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
