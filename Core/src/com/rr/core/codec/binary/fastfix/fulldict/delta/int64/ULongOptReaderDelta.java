/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.fulldict.delta.int64;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.FixFieldReader;
import com.rr.core.codec.binary.fastfix.common.LongFieldValWrapper;
import com.rr.core.codec.binary.fastfix.fulldict.delta.DeltaFieldReader;
import com.rr.core.codec.binary.fastfix.fulldict.entry.LongFieldDictEntry;
import com.rr.core.lang.Constants;

public final class ULongOptReaderDelta extends DeltaFieldReader implements FixFieldReader<LongFieldValWrapper> {

    private final LongFieldDictEntry _previous;
    
    public ULongOptReaderDelta( String name, int id, LongFieldDictEntry previous ) {
        super( name, id, true );
        _previous = previous;
        reset();
    }

    @Override
    public void reset() {
        _previous.reset();
    }

    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining  
     * 
     * @param encoder
     * @param mapWriter
     * @param _value
     */
    public long read( final FastFixDecodeBuilder decoder ) {
        
        final long previous = _previous.getVal();

        if( previous != Constants.UNSET_LONG ) {
            final long delta = decoder.decodeOptionalULongOverflow();
            
            if ( delta != Constants.UNSET_LONG ) {
                final long value = previous + delta;

                _previous.setVal( value );
                
                return value;
            } 
        }
        else {
            throwMissingPreviousException();
        }

        return Constants.UNSET_LONG; 
    }
    
    public long getInitValue() {
        return _previous.getInit();
    }

    @Override
    public void read( FastFixDecodeBuilder decoder, PresenceMapReader mapReader, LongFieldValWrapper dest ) {
        dest.setVal( read( decoder ) );
    }
}
