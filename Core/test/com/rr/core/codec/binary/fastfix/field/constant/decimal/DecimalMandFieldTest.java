/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.field.constant.decimal;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.constant.decimal.DecimalMandReaderConst;
import com.rr.core.codec.binary.fastfix.common.constant.decimal.DecimalMandWriterConst;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;

public class DecimalMandFieldTest extends BaseTestCase {
    private FastFixDecodeBuilder _decoder = new FastFixDecodeBuilder();
    private FastFixBuilder      _encoder;
    private PresenceMapWriter   _mapWriter;
    private PresenceMapReader   _mapReader;
    
    private byte[]              _buf = new byte[8192];
    private DecimalMandReaderConst      _fieldReader;
    private DecimalMandWriterConst      _fieldWriter;
    
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
        checkField( 1 );
        checkField( -2 );
        checkField( -1000 );
        checkField( 1000 );
        checkField( 127 );
        checkField( 128 );
        checkField( 129 );
        checkField( 0x0FFFFFFFFFFFFL );
        checkField( Long.MAX_VALUE-1 );  // MAX_VALUE will overflow when incremented to make room for null

        checkField( 0.1 );
        checkField( 0.999 );
        checkField( 0.0876 );
        checkField( 0.00000000000000999 );
        checkField( 54321.99999999 );
        checkField( 54321.00098 );
        checkField( 54321.98 );
        checkField( 987654321987654321L );
        
        checkField( Constants.UNSET_DOUBLE );
    }

    private void checkField( double value ) {
        setUp(); // force reset

        _fieldReader = new DecimalMandReaderConst( "TestULong", 10018, value );
        _fieldWriter = new DecimalMandWriterConst( "TestULong", 10018, value );
        
        _fieldWriter.write( _encoder, _mapWriter );
        _mapWriter.end();
        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( value );
    }

    private void readValAndCheck( double value ) {
        _fieldReader = new DecimalMandReaderConst( "DecimalOptFieldTest", 10018, value );

        double readVal = _fieldReader.read( _decoder );
        
        assertEquals( value, readVal, Constants.WEIGHT );
    }

    private void write( double value ) {
        _fieldWriter = new DecimalMandWriterConst( "DecimalOptFieldTest", 10018, value );
        
        _fieldWriter.write( _encoder, _mapWriter );
        assertEquals( value, _fieldWriter.getInitValue(), Constants.WEIGHT  );
    }
    
    public void testPrevCopy() {
        write( 100.123 );
        write( 100.00009 );
        write( 1000000000000.01  );
        write( 40 );
        write( 100.05 );
        _mapWriter.end();

        _decoder.start( _buf, 0, _encoder.getCurrentIndex() );
        
        _mapReader.readMap( _decoder );
        
        assertEquals( 1, _decoder.getCurrentIndex() );
        
        readValAndCheck( 100.123 );
        readValAndCheck( 100.00009 );
        readValAndCheck( 1000000000000.01  );
        readValAndCheck( 40 );
        readValAndCheck( 100.05 );
    }
}
