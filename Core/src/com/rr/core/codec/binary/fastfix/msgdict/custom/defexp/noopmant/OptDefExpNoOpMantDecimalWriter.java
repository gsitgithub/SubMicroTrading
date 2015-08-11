/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.noopmant;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.FastFixDecimal;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.codec.binary.fastfix.common.def.int32.IntOptWriterDefault;
import com.rr.core.codec.binary.fastfix.common.noop.int64.LongMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.msgdict.custom.CustomDecimalFieldWriter;
import com.rr.core.lang.Constants;

/**
 * CME decimal are all default exponent of 2 with delta mantissa
 *
 * @author Richard Rose
 */
public class OptDefExpNoOpMantDecimalWriter extends CustomDecimalFieldWriter {

    private final int  _initExp;
    
    private final FastFixDecimal _prevDecimal = new FastFixDecimal();

    private final IntOptWriterDefault  _exp;
    private final LongMandWriterNoOp   _mant;
    
    public OptDefExpNoOpMantDecimalWriter( ComponentFactory cf, String name, int id, String initExp, String initMant ) {
        this( cf, name, id, FieldUtils.parseInt(initExp) );
    }
    
    @SuppressWarnings( "boxing" )
    public OptDefExpNoOpMantDecimalWriter( ComponentFactory cf, String name, int id, int initExp ) {
        super( name, id, true );
        
        _exp  = cf.getWriter( IntOptWriterDefault.class, name + "Exp",  id, initExp );
        _mant = cf.getWriter( LongMandWriterNoOp.class,  name + "Mant", id );
        
        _initExp  = initExp;

        reset();
    }

    public void reset() {
        _prevDecimal.set( _initExp, 0 );
        _exp.reset();
        _mant.reset();
    }
    
    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining  
     * 
     * @param encoder
     * @param mapWriter
     * @param value
     */
    public void write( final FastFixBuilder encoder, final PresenceMapWriter mapWriter, final double value ) {
        if ( value == Constants.UNSET_DOUBLE ) {
            mapWriter.setCurrentField();
            encoder.encodeNull();
        } else {
            _prevDecimal.set( value );
            _exp.write ( encoder, mapWriter, _prevDecimal.getExponent() );
            _mant.write ( encoder, _prevDecimal.getMantissa() );
        }
    }

    public double getPreviousValue() {
        return _prevDecimal.get();
    }
    
    public double getInitValue( FastFixDecimal t ) {
        return t.set( _exp.getInitValue(), Constants.UNSET_LONG );
    }

    @Override
    public boolean requiresPMap() {
        return _exp.requiresPMap();
    }
}
