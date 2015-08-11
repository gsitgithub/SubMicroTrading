/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict.copy.decimal;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.DoubleFieldValWrapper;
import com.rr.core.codec.binary.fastfix.common.FastFixDecimal;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.codec.binary.fastfix.common.FixFieldReader;
import com.rr.core.codec.binary.fastfix.msgdict.copy.CopyFieldReader;
import com.rr.core.lang.Constants;

public final class DecimalMandReaderCopy extends CopyFieldReader implements FixFieldReader<DoubleFieldValWrapper> {

    private final double         _init;
    private final FastFixDecimal _prevDecimal = new FastFixDecimal();
    private       double         _previous;
    
    public DecimalMandReaderCopy( String name, int id, String init ) {
        this( name, id, FieldUtils.parseDouble(init) );
    }
    
    public DecimalMandReaderCopy( String name, int id ) {
        this( name, id, Constants.UNSET_DOUBLE );
    }
    
    public DecimalMandReaderCopy( String name, int id, double init ) {
        super( name, id, false );
        _init = init;
        reset();
    }

    @Override
    public void reset() {
        _prevDecimal.set( _init );
        _previous = _init; 
    }
    
    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining  
     * 
     * @param encoder
     * @param mapWriter
     * @param _value
     */
    public double read( final FastFixDecodeBuilder decoder, final PresenceMapReader mapReader ) {
        
        final boolean present = mapReader.isNextFieldPresent();
        
        if ( present ) {
            final int  exponent = decoder.decodeMandInt();
            final long value    = decoder.decodeMandLong();

            _previous = _prevDecimal.set( exponent, value );
            
            return _previous;
        }
        
        if ( _previous != Constants.UNSET_DOUBLE ) {  
            return _previous;                           // value not present, but have previous value to copy
        }

        throwMissingValueException();
        
        return Constants.UNSET_DOUBLE; // keep compiler happy as it cant see throw methods runtime exception
    }
    
    public double getInitValue() {
        return _init;
    }

    @Override
    public void read( FastFixDecodeBuilder decoder, PresenceMapReader mapReader, DoubleFieldValWrapper dest ) {
        dest.setVal( read( decoder, mapReader ) );
    }
}
