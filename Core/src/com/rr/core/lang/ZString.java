/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

import java.nio.ByteBuffer;

public interface ZString extends Comparable<ZString> {

    public int length();
    
    public int getOffset();

    public void write( ByteBuffer bb );

    public void getBytes( byte[] target, int targetStart );

    public void getBytes( byte[] target, int srcStart, int targetStart, int len );

    public byte getByte( int idx );

    public int indexOf( int c );

    public int indexOf( int c, int offset );

    public int lastIndexOf( int c );

    public int lastIndexOf( int c, int fromIdx );

    /**
     * @NOTE use equals in preference to equalsIgnoreCase as its slower
     */
    public boolean equalsIgnoreCase( ZString s );
    
    public boolean equalsIgnoreCase( String s );
    
    /**
     * @NOTE do not use this method to modify the underlying byte array
     * 
     * @return the underlying byte array
     */
    public byte[] getBytes();
}
