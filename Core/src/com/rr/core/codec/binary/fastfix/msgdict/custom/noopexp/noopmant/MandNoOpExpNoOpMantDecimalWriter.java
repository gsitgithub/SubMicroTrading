/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict.custom.noopexp.noopmant;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.common.FastFixDecimal;
import com.rr.core.codec.binary.fastfix.common.noop.int32.IntMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.LongMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.msgdict.custom.CustomDecimalFieldWriter;
import com.rr.core.lang.Constants;

/**
 * CME decimal are all default exponent of 2 with delta mantissa
 *
 * @author Richard Rose
 */
public class MandNoOpExpNoOpMantDecimalWriter extends CustomDecimalFieldWriter {

    private final FastFixDecimal _prevDecimal = new FastFixDecimal();

    private final IntMandWriterNoOp    _exp;
    private final LongMandWriterNoOp   _mant;
    
    public MandNoOpExpNoOpMantDecimalWriter( ComponentFactory cf, String name, int id, String initExp, String initMant ) {
        this( cf, name, id );
    }
    
    @SuppressWarnings( "boxing" )
    public MandNoOpExpNoOpMantDecimalWriter( ComponentFactory cf, String name, int id ) {
        super( name, id, false );

        _exp  = cf.getWriter( IntMandWriterNoOp.class, name + "Exp",  id );
        _mant = cf.getWriter( LongMandWriterNoOp.class,  name + "Mant", id );
        
        reset();
    }

    public void reset() {
        // nothing
    }
    
    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining  
     * 
     * @param encoder
     * @param mapWriter
     * @param value
     */
    public void write( final FastFixBuilder encoder, final double value ) {
        _prevDecimal.set( value );
        _exp.write ( encoder, _prevDecimal.getExponent() );
        _mant.write ( encoder, _prevDecimal.getMantissa() );
    }

    public double getPreviousValue() {
        return _prevDecimal.get();
    }
    
    public double getInitValue() {
        return Constants.UNSET_DOUBLE;
    }

    @Override
    public boolean requiresPMap() {
        return _exp.requiresPMap() || _mant.requiresPMap();
    }
}
