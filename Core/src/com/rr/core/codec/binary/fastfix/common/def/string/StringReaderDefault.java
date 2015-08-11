/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.common.def.string;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.FixFieldReader;
import com.rr.core.codec.binary.fastfix.common.StringFieldValWrapper;
import com.rr.core.codec.binary.fastfix.common.def.DefaultFieldReader;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;

public final class StringReaderDefault extends DefaultFieldReader implements FixFieldReader<StringFieldValWrapper> {

    private final ReusableString _init     = new ReusableString();
    
    public StringReaderDefault( String name, int id, ZString init ) {
        super( name, id, false );
        _init.copy( init );
        reset();
    }

    public StringReaderDefault( String name, int id, String init ) {
        super( name, id, false );
        _init.copy( init );
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
     * @param value
     */
    public void read( final FastFixDecodeBuilder decoder, final PresenceMapReader mapReader, ReusableString dest ) {
        
        final boolean present = mapReader.isNextFieldPresent();
        
        if ( present ) {
            decoder.decodeString( dest );
            
        } else {
            
            // cannot differentiate null from empty string 
            if ( dest != null ) {
                dest.copy( _init );
            }
        }
    }
    
    public ZString getInitValue() {
        return _init;
    }

    @Override
    public void read( FastFixDecodeBuilder decoder, PresenceMapReader mapReader, StringFieldValWrapper dest ) {
        read( decoder, mapReader, dest.getVal() );
    }
}
