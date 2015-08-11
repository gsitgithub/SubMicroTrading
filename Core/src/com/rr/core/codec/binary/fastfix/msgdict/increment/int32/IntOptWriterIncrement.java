/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict.increment.int32;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.codec.binary.fastfix.msgdict.increment.IncrementFieldWriter;
import com.rr.core.lang.Constants;

public final class IntOptWriterIncrement extends IncrementFieldWriter {

    private final int _init;
    private       int _previous;
    
    public IntOptWriterIncrement( String name, int id, String init ) {
        this( name, id, FieldUtils.parseInt(init) );
    }
    
    public IntOptWriterIncrement( String name, int id ) {
        this( name, id, Constants.UNSET_INT );
    }
    
    public IntOptWriterIncrement( String name, int id, int init ) {
        super( name, id, true );
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
    public void write( final FastFixBuilder encoder, final PresenceMapWriter mapWriter, final int value ) {
        if ( value != Constants.UNSET_INT ) {
            if ( _previous != Constants.UNSET_INT ) {
                final int nextSeq = _previous + 1;
                
                if ( nextSeq == value ) {               // expected seq num value nothing to encode
                    mapWriter.clearCurrentField();
                    _previous = nextSeq;
                    return;
                }
            }
            
            _previous = value;                          // override expected seqnum and encode
            mapWriter.setCurrentField();
            encoder.encodeOptionalInt( value );
        } else {
            if ( _previous != Constants.UNSET_INT ) {   // set val to null
                mapWriter.setCurrentField();
                _previous = Constants.UNSET_INT;
                encoder.encodeNull();
            } else {
                mapWriter.clearCurrentField();
            }
        }
    }

    public int getPreviousValue() {
        return _previous;
    }
    
    public int getInitValue() {
        return _init;
    }
}
