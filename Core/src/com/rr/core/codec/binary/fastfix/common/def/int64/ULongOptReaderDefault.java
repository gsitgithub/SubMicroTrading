/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.common.def.int64;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.codec.binary.fastfix.common.FixFieldReader;
import com.rr.core.codec.binary.fastfix.common.LongFieldValWrapper;
import com.rr.core.codec.binary.fastfix.common.def.DefaultFieldReader;
import com.rr.core.lang.Constants;

public final class ULongOptReaderDefault extends DefaultFieldReader implements FixFieldReader<LongFieldValWrapper> {

    private final long _init;
    
    public ULongOptReaderDefault( String name, int id, String init ) {
        this( name, id, FieldUtils.parseLong(init) );
    }

    public ULongOptReaderDefault( String name, int id ) {
        this( name, id, Constants.UNSET_LONG );
    }
    
    public ULongOptReaderDefault( String name, int id, long init ) {
        super( name, id, true );
        _init = init;
        reset();
    }

    @Override
    public void reset() {
        // nothing
    }

    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining  
     * 
     * @param encoder
     * @param mapWriter
     * @param _value
     */
    public long read( final FastFixDecodeBuilder decoder, final PresenceMapReader mapReader ) {
        
        final boolean present = mapReader.isNextFieldPresent();
        
        if ( present ) {
            final long value = decoder.decodeOptionalULong();

            return value;
        }
        
        if ( _init != Constants.UNSET_LONG ) {  
            return _init;                       // value not present, but have previous value to copy
        }

        return Constants.UNSET_LONG;            // not present and no previous value 
    }
    
    public long getInitValue() {
        return _init;
    }

    @Override
    public void read( FastFixDecodeBuilder decoder, PresenceMapReader mapReader, LongFieldValWrapper dest ) {
        dest.setVal( read( decoder, mapReader ) );
    }
}
