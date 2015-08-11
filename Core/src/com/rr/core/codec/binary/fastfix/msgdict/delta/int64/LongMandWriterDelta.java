/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict.delta.int64;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.codec.binary.fastfix.msgdict.delta.DeltaFieldWriter;
import com.rr.core.lang.Constants;

public final class LongMandWriterDelta extends DeltaFieldWriter {

    private final long _init;
    private       long _previous;
    
    public LongMandWriterDelta( String name, int id, String init ) {
        this( name, id, FieldUtils.parseLong(init) );
    }

    public LongMandWriterDelta( String name, int id ) {
        this( name, id, 0 );
    }
    
    public LongMandWriterDelta( String name, int id, long init ) {
        super( name, id, false );
        _init = init;
        reset();
    }

    public void reset() {
        _previous = _init;
    }

    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining  
     * 
     * @param encoder
     * @param mapWriter
     * @param value
     */
    public void write( final FastFixBuilder encoder, final long value ) {
        if ( value != Constants.UNSET_LONG ) {
            if ( _previous != Constants.UNSET_LONG ) {
                final long delta = value - _previous;

                boolean overflow = false;
                
                if ( value > -1 && _previous <= (Long.MIN_VALUE + value) ) {
                    overflow = true;
                }
                else if ( value < -1 && _previous > (value - Long.MIN_VALUE) ) {
                    overflow = true;
                }

                encoder.encodeMandLongOverflow( delta, overflow );
            } else {
                throwMissingPreviousException();
            }
        } else {
            throwMissingValueException();
        }

        _previous = value;
    }

    public long getPreviousValue() {
        return _previous;
    }
    
    public long getInitValue() {
        return _init;
    }
}
