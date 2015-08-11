/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict.copy.decimal;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.FastFixDecimal;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.codec.binary.fastfix.msgdict.copy.CopyFieldWriter;
import com.rr.core.lang.Constants;

public final class DecimalOptWriterCopy extends CopyFieldWriter {

    private final double         _init;
    private final FastFixDecimal _prevDecimal = new FastFixDecimal();
    private final FastFixDecimal _tmpValue    = new FastFixDecimal();
    
    public DecimalOptWriterCopy( String name, int id, String init ) {
        this( name, id, FieldUtils.parseDouble(init) );
    }
    
    public DecimalOptWriterCopy( String name, int id ) {
        this( name, id, Constants.UNSET_DOUBLE );
    }
    
    public DecimalOptWriterCopy( String name, int id, double init ) {
        super( name, id, true );
        _init = init;
        reset();
    }

    public void reset() {
        _prevDecimal.set( _init );
    }
    
    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining  
     * 
     * @param encoder
     * @param mapWriter
     * @param value
     */
    public void write( final FastFixBuilder encoder, final PresenceMapWriter mapWriter, final double value ) {

        if ( value != Constants.UNSET_DOUBLE ) {
            _tmpValue.set( value );
            if ( ! _tmpValue.equals(_prevDecimal) ) {       
                _prevDecimal.set( _tmpValue );                      // new value must be encoded
                mapWriter.setCurrentField();
                encoder.encodeOptionalInt( _tmpValue.getExponent() );
                encoder.encodeMandLong( _tmpValue.getMantissa() );
            } else {                                    // value unchanged dont need encode (it will be copied on decode)
                mapWriter.clearCurrentField();
            }
        } else {
            if ( _prevDecimal.isNull() ) {  // current value is null, need null previous value and encode null byte
                _prevDecimal.setNull();
                mapWriter.setCurrentField();
                encoder.encodeNull();
            } else {
                mapWriter.clearCurrentField();
            }
        }
    }

    public double getPreviousValue() {
        return _prevDecimal.get();
    }
    
    public double getInitValue() {
        return _init;
    }
}
