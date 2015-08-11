/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict.increment.int32;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.codec.binary.fastfix.common.FixFieldReader;
import com.rr.core.codec.binary.fastfix.common.IntFieldValWrapper;
import com.rr.core.codec.binary.fastfix.msgdict.increment.IncrementFieldReader;
import com.rr.core.lang.Constants;

public final class UIntOptReaderIncrement extends IncrementFieldReader implements FixFieldReader<IntFieldValWrapper> {

    private final int _init;
    private       int _previous;
    
    public UIntOptReaderIncrement( String name, int id, String init ) {
        this( name, id, FieldUtils.parseInt(init) );
    }

    public UIntOptReaderIncrement( String name, int id ) {
        this( name, id, Constants.UNSET_INT );
    }
    
    public UIntOptReaderIncrement( String name, int id, int init ) {
        super( name, id, true );
        _init = init;
        reset();
    }

    @Override
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
    public int read( final FastFixDecodeBuilder decoder, final PresenceMapReader mapReader ) {
        
        if ( mapReader.isNextFieldPresent() ) {
            final int val = decoder.decodeOptionalUInt(); 
            _previous = val;
            return val;
        } 

        if( _previous != Constants.UNSET_INT ) { // no presence bit, but have prev field so just inc
            final int nextSeq = _previous + 1;
            _previous = nextSeq;
            return nextSeq;
        }

        return Constants.UNSET_INT; // keep compiler happy as it cant see throw methods runtime exception
    }
    
    public int getInitValue() {
        return _init;
    }

    @Override
    public void read( FastFixDecodeBuilder decoder, PresenceMapReader mapReader, IntFieldValWrapper dest ) {
        dest.setVal( read( decoder, mapReader ) );
    }
}
