/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.field.increment.int64;

import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.ULongMandReaderIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int64.ULongMandWriterIncrement;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;

public class ULongMandIncFieldTest extends BaseTestCase {
    private FastFixDecodeBuilder      _decoder = new FastFixDecodeBuilder();
    private FastFixBuilder      _encoder;
    private PresenceMapWriter   _mapWriter;
    private PresenceMapReader   _mapReader;
    
    private byte[]              _buf = new byte[8192];
    private ULongMandReaderIncrement     _fieldReader;
    private ULongMandWriterIncrement     _fieldWriter;
    
    @Override
    public void setUp(){


        _encoder    = new FastFixBuilder( _buf, 0 );
        _mapWriter  = new PresenceMapWriter( _encoder, 0, 1 );
        _mapReader  = new PresenceMapReader();
        
        _fieldReader = new ULongMandReaderIncrement( "TestULong", 10018 );
        _fieldWriter = new ULongMandWriterIncrement( "TestULong", 10018 );
        
    }
    
    @Override
    public String toString() {
        return new String( _buf, 0, _decoder.getNextFreeIdx() );
    }
    
    public void testVals() {
        checkField( 1 );
        checkField( Long.MAX_VALUE );
        checkField( -2 );
        checkField( -1000 );
        checkField( 1000 );
        checkField( 127 );
        checkField( 128 );
        checkField( 129 );
        checkField( 0x0FFFFFFFFFFFFL );

        try {
            checkField( Constants.UNSET_LONG );
            assertFalse( true );                    // should fail
        } catch( RuntimeEncodingException e ) {
            // expected
        }
    }

    private void checkField( long value ) {
        setUp(); // force reset
        
        _fieldWriter.write( _encoder, _mapWriter, value );
        _mapWriter.end();
        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( value );
    }

    private void readValAndCheck( long value ) {
        long readVal= _fieldReader.read( _decoder, _mapReader );
        
        assertEquals( value, readVal );
    }

    public void testPrevCopy() {
        _fieldWriter.write( _encoder, _mapWriter, 100 );
        assertEquals( 100, _fieldWriter.getPreviousValue() );
        _fieldWriter.write( _encoder, _mapWriter, 100 );
        assertEquals( 100, _fieldWriter.getPreviousValue() );
        _fieldWriter.write( _encoder, _mapWriter, 100 );
        assertEquals( 100, _fieldWriter.getPreviousValue() );
        _fieldWriter.write( _encoder, _mapWriter, 40 );
        assertEquals( 40, _fieldWriter.getPreviousValue() );
        _fieldWriter.write( _encoder, _mapWriter, 100 );
        assertEquals( 100, _fieldWriter.getPreviousValue() );
        _mapWriter.end();

        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( 100 );
        readValAndCheck( 100 );
        readValAndCheck( 100 );
        readValAndCheck( 40 );
        readValAndCheck( 100 );
    }
}
