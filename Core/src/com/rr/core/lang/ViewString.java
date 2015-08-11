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

import com.rr.core.lang.stats.SizeConstants;

/**
 * a View String which doesnt own and cant modify the underlying buffer
 * 
 * has an offset into the buffer
 * 
 * Main purpose is allow Strings to share the same byte array in similar way to String but
 * avoid all the temp obj creations with String
 * 
 * The underlying buffer can be changed as can the offset into the buffer
 * thus this is NOT an immutable object
 * 
 * @author Richard Rose
 */

public class ViewString implements AssignableString {

    private static final byte[] NULL_BUF = new byte[10];
    
    protected byte[] _bytes;
    protected int    _hash;
    protected int    _len;
    protected int    _offset;
    
    public ViewString( final ViewString from ) {
        this( from._bytes, from._offset, from._len );
    }

    public ViewString( final byte[] bytes, final int offset, final int len ) {
        _len = len;
        _offset = offset;
        _bytes = bytes;
    }

    public ViewString( final byte[] buf ) {
        this( buf, 0, buf.length );
    }

    public ViewString( final ReusableString buf ) {
        this( buf._bytes, 0, buf.length() );
    }

    public ViewString( final ZString buf ) {
        this( buf.getBytes(), 0, buf.length() );
    }

    public ViewString( final String s ) {
        this( (s==null) ? new byte[0] : s.getBytes(), 0, (s==null) ? 0 : s.length() );
    }
    
    public ViewString() {
        _len = 0;
        _offset = 0;
        _bytes = NULL_BUF;
    }
    
    /**
     * @NOTE CREATES NEW ARRAY AVOID FOR ANYTHING OTHER THAN TEST CODE
     * 
     * @return copy of viewed bytes
     */
    public byte[] cloneBytes() {
        byte[] dest = new byte[ _len ];
        
        System.arraycopy( _bytes, _offset, dest, 0, _len );
        return dest;
    }

    public void setValue( final byte[] buf ) {
        _len = 0;
        _offset = 0;
        _bytes = buf;
        _hash = 0;
    }
    
    public void setValue( final int offset, final int len ) {
        _len = len;
        _offset = offset;
        _hash = 0;
    }
    
    @Override
    public void setValue( final byte[] buf, final int offset, final int len ) {
        _bytes = buf;
        _len = len;
        _offset = offset;
        _hash = 0;
    }
    
    @Override
    public void setValue( ZString from ) {
        throw new RuntimeException( "Cant assign a ZString to a ViewString : cant set to " + from );
    }
    
    @Override
    public int getOffset() {
        return _offset;
    }
    
    @Override
    public int compareTo( final ZString o ) {
        final int len2 = o.length();
        final byte v2[] = o.getBytes();
        int idx2 = o.getOffset();
        int idx = _offset;
        
        if ( _len != len2 ) {
            int minLen = _len;
            int resp = -1;
            if ( _len > len2 ) {
                minLen = len2;
                resp = 1;
            }
            for ( int i = 0 ; i < minLen ; i++ ) {
                byte b1 = _bytes[idx++];
                byte b2 = v2[idx2++];
                if ( b1 != b2 )
                    return b1 - b2;
            }
            return resp;

        }
        for ( int i = 0 ; i < _len ; i++ ) {
            byte b1 = _bytes[idx++];
            byte b2 = v2[idx2++];
            if ( b1 != b2 )
                return b1 - b2;
        }
        return 0;
    }

    public void ensureCapacity( int minimumCapacity ) {
        throw new RuntimeException( "Invalid operation. " + ViewString.class.getName() + " instances cannot be modified" );
    }

    public final boolean equals( final ViewString other ) {
        if ( this == other )
            return true;

        if ( other == null ) return false;
        
        final int len2 = other._len;
        if ( _len != len2 )
            return false;

        final byte v1[] = _bytes;
        final byte v2[] = other._bytes;

        int idx = _offset + _len;
        int idx2 = other._offset + _len;

        for ( int i = _len ; i > 0 ; i-- ) {
            if ( v1[--idx] != v2[--idx2] )
                return false;
        }

        return true;
    }

    @Override
    public final boolean equals( final Object other ) {
        if ( this == other )
            return true;

        if ( other == null ) return false;
        
        Class<?> otherClass = other.getClass();

        // ideally should use instanceOf ZString ... but dont want hit
        if ( otherClass == ViewString.class || otherClass == ReusableString.class ) {
            return equals( (ViewString)other );
        }

        if ( other.getClass() == String.class ) {

            return equals( (String) other );
        }
        return false;
    }

    public final boolean equals( final String other ) {

        if ( other == null ) return false;

        final int n = _len;
        if ( n != other.length() ) {
            return false;
        }

        final byte v1[] = _bytes;

        int idx = _offset;
        
        for ( int i = 0 ; i < n ; i++ ) {
            if ( v1[ idx++ ] != other.charAt( i ) )
                return false;
        }

        return true;
        
    }
    
    public final boolean equals( final ZString other ) {
        if ( this == other )
            return true;

        if ( other == null ) return false;
        
        final int len2 = other.length();
        if ( _len != len2 )
            return false;

        final byte v1[] = _bytes;
        final byte v2[] = other.getBytes();

        int idx = _offset + _len;
        int idx2 = other.getOffset() + _len;

        for ( int i = _len ; i > 0 ; i-- ) {
            if ( v1[--idx] != v2[--idx2] )
                return false;
        }

        return true;
    }

    @Override
    public final boolean equalsIgnoreCase( final ZString other ) {
        if ( this == other )
            return true;

        if ( other == null ) return false;
        
        final int len2 = other.length();
        if ( _len != len2 )
            return false;

        final byte v1[] = _bytes;
        final byte v2[] = other.getBytes();

        int idx = _offset + _len;
        int idx2 = other.getOffset() + _len;

        for ( int i = _len ; i > 0 ; i-- ) {
            byte b1 = v1[--idx];
            byte b2 = v2[--idx2];
            
            if ( b1 != b2 ) {
                if ( b1 >= 'A' && b1 <= 'Z' ) {
                    b1 += 0x20; // convert to lowercase
                    if ( b1 != b2 ) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public final boolean equalsIgnoreCase( final String other ) {

        if ( other == null ) return false;

        final int n = _len;
        if ( n != other.length() ) {
            return false;
        }

        final byte v1[] = _bytes;

        int idx = _offset;
        
        for ( int i = 0 ; i < n ; i++ ) {
            byte b1 = v1[ idx++ ];
            byte b2 = (byte) other.charAt( i );
            
            if ( b1 != b2 ) {
                if ( b1 >= 'A' && b1 <= 'Z' ) {
                    b1 += 0x20; // convert to lowercase
                    if ( b1 != b2 ) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }

        return true;
    }
    
    @Override
    public final byte getByte( final int idx ) {
        return _bytes[_offset+idx];
    }

    @Override
    public final byte[] getBytes() {
        return _bytes;
    }

    @Override
    public void getBytes( final byte[] target, final int targetStart ) {
        if ( _len != 0 ) {
            final int len = _len;

            if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
                int srcIdx  = _offset;
                int destIdx = targetStart;
                final int max = srcIdx + len; 
                while( srcIdx < max ) {
                    target[ destIdx++ ] = _bytes[ srcIdx++ ];
                }
            } else {
                System.arraycopy( _bytes, _offset, target, targetStart, len );
            }
        }
    }

    @Override
    public void getBytes( final byte[] target, final int srcStart, final int targetStart, final int len ) {
        if ( _len != 0 ) {

            if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
                int srcIdx  = _offset;
                int destIdx = targetStart;
                final int max = srcIdx + len; 
                while( srcIdx < max ) {
                    target[ destIdx++ ] = _bytes[ srcIdx++ ];
                }
            } else {
                System.arraycopy( _bytes, srcStart, target, targetStart, len );
            }
        }
    }
    
    public int getCapacity() {
        return _bytes.length;
    }

    @Override
    public int length() {
        return _len;
    }

    @Override
    public int hashCode() {
        if ( _hash == 0 ) {
            int h = 0;
            final int max = _offset + _len;
            for ( int i = _offset ; i < max ; i++ ) {
                h = 31 * h + _bytes[i];
            }
            _hash = h;
        }
        return _hash;
    }

    @Override
    public int indexOf( final int c ) {

        return indexOf( c, 0 );
    }

    @Override
    public int indexOf( final int c, final int startIdx ) {

        final int max = _offset + _len;
        
        for ( int i = _offset+startIdx ; i < max ; i++ ) {
            if ( _bytes[i] == c ) {
                return i - _offset;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf( final int c ) {

        return lastIndexOf( c, _len - 1 );
    }

    @Override
    public int lastIndexOf( final int c, final int fromIdx ) {

        for ( int i = fromIdx+_offset ; i >= _offset ; i-- ) {
            if ( _bytes[i] == c ) {
                return i - _offset;
            }
        }

        return -1;
    }

    @Override
    public String toString() {
        return new String( _bytes, _offset, _len );
    }

    @Override
    public void write( final ByteBuffer bb ) {

        bb.put( _bytes, _offset, _len );
    }

    public void reset() {
        _len = 0;
        _offset = 0;
    }
    
    /**
     * @NOTE no range checking so caller must check
     * @param length
     */
    public void setLength( int length ) {
        _len = length;
    }
}
