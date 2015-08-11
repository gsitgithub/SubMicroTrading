/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict.delta.string;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.msgdict.delta.DeltaFieldWriter;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;

public final class StringWriterDelta extends DeltaFieldWriter {

    private final ReusableString _init     = new ReusableString();
    private final ReusableString _previous = new ReusableString();
    
    public StringWriterDelta( String name, int id ) {
        this( name, id, null );
    }
    public StringWriterDelta( String name, int id, String init ) {
        super( name, id, false );
        _init.copy( init );
        reset();
    }

    public void reset() {
        _previous.copy( _init );
    }
    
    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining
     * 
     * compares the new value with previous, checking for best matching at start or end of string, then 
     * 
     * @param encoder
     * @param mapWriter
     * @param value
     */
    public void write( final FastFixBuilder encoder, final ReusableString value ) {

        final int matchBytesAtStart = matchStart(value);
        final int matchBytesAtEnd   = matchEnd(value);
        
        final int prevLen = _previous.length();
        final int newLen  = value.length();
        
        int subtractionLength = 0;
        int stDeltaIdx = 0;
        int copyBytes = 0;
        
        if ( matchBytesAtStart >= matchBytesAtEnd ) {               // more bytes match from start strings
            if ( matchBytesAtStart < prevLen ) {
                subtractionLength = prevLen - matchBytesAtStart;    // count of the number of unmatched bytes at end of prev str
            }
            
            if ( matchBytesAtStart < newLen ) {                     // some bytes from end of new value need to be delta's over
                stDeltaIdx = matchBytesAtStart;
                copyBytes = newLen-matchBytesAtStart;
            }
        } else {
            if ( matchBytesAtEnd < prevLen )  {
                subtractionLength = matchBytesAtEnd - prevLen - 1;
            } else {                                                // matched whole of previous string 
                subtractionLength = -1;
            }

            if( matchBytesAtEnd < newLen) {
                stDeltaIdx = 0;
                copyBytes = newLen - matchBytesAtEnd;
            }
        }
        
        encoder.encodeOptionalInt( subtractionLength );
        encoder.encodeStringBytes( value.getBytes(), stDeltaIdx, copyBytes );
        
        _previous.copy( value );
    }

    private int matchEnd( ReusableString value ) {
        final byte[] prevBytes = _previous.getBytes();
        final byte[] valBytes  = value.getBytes();

        int baseIndex = _previous.length();
        int newIndex  = value.length();
        
        int minLength = Math.min(baseIndex--, newIndex--);
        
        int matched = 0;
        
        while( matched < minLength && prevBytes[baseIndex--] == valBytes[newIndex--] ) {
            matched++;
        }
        
        return matched;
    }

    private int matchStart( final ReusableString value ) {
        int matched = 0;
        
        final int minLength = Math.min( _previous.length(), value.length() );
        
        final byte[] prevBytes = _previous.getBytes();
        final byte[] valBytes  = value.getBytes();
        
        while( matched < minLength && (prevBytes[matched] == valBytes[matched] ) ) { 
            matched++;
        }
        
        return matched;
    }

    public ReusableString getPreviousValue() {
        return _previous;
    }
    
    public ZString getInitValue() {
        return _init;
    }
}
