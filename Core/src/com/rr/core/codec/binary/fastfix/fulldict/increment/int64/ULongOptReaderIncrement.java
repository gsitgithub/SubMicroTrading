/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.fulldict.increment.int64;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.FixFieldReader;
import com.rr.core.codec.binary.fastfix.common.LongFieldValWrapper;
import com.rr.core.codec.binary.fastfix.fulldict.entry.LongFieldDictEntry;
import com.rr.core.codec.binary.fastfix.fulldict.increment.IncrementFieldReader;
import com.rr.core.lang.Constants;

public final class ULongOptReaderIncrement extends IncrementFieldReader implements FixFieldReader<LongFieldValWrapper> {

    private final LongFieldDictEntry _previous;
    
    public ULongOptReaderIncrement( String name, int id, LongFieldDictEntry previous ) {
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
     * @param value
     */
    public long read( final FastFixDecodeBuilder decoder, final PresenceMapReader mapReader ) {
        
        if ( mapReader.isNextFieldPresent() ) {
            final long val = decoder.decodeOptionalULong(); 
            _previous.setVal( val );
            return val;
        } 

        final long previous = _previous.getVal();

        if( previous != Constants.UNSET_LONG ) { // no presence bit, but have prev field so just inc
            final long nextSeq = previous + 1;
            _previous.setVal( nextSeq );
            return nextSeq;
        }

        return Constants.UNSET_LONG; // keep compiler happy as it cant see throw methods runtime exception
    }
    
    public long getInitValue() {
        return _previous.getInit();
    }

    @Override
    public void read( FastFixDecodeBuilder decoder, PresenceMapReader mapReader, LongFieldValWrapper dest ) {
        dest.setVal( read( decoder, mapReader ) );
    }
}
