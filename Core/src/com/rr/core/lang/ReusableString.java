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
import com.rr.core.utils.NumberFormatUtils;

/**
 * mutateable string : combination of java String and StringBuilder but
 * without tempobj creation
 * 
 * this specialisation does OWN its buffer and can grow it as needed
 *
 * offset is always zero to avoid extra addition on each ensureCapacity call
 * 
 * @author Richard Rose
 */
public final class ReusableString extends ViewString implements Reusable<ReusableString> {

    private static final int            DEFAULT_LEN   = SizeConstants.DEFAULT_STRING_LENGTH;

    private static final ReusableType   _reusableType = CoreReusableType.ReusableString;

    private static final ViewString     NULL_STR      = new ViewString( "null" );

    private ReusableString _next = null; 
    
    public ReusableString() {
        this( DEFAULT_LEN );
    }

    public ReusableString( final ViewString from ) {
        this( from._bytes, from._offset, from._len );
    }

    public ReusableString( final byte[] bytes, final int from, final int len ) {
        super( new byte[len], 0, len );

        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            int srcIdx  = from;
            int destIdx = 0;
            while( destIdx < len ) {
                _bytes[ destIdx++ ] = bytes[ srcIdx++ ];
            }
        } else {
            System.arraycopy( bytes, from, _bytes, 0, _len );
        }
    }

    public ReusableString( final int initialCapacity ) {
        super( new byte[initialCapacity], 0, 0 );
    }

    public ReusableString( final String s ) {
        super( (s==null) ? new byte[0] : s.getBytes(), 0, (s==null) ? 0 : s.length() );
    }

    public ReusableString( final byte[] val ) {
        this( val, 0, val.length );
    }
    
    public ReusableString( ZString s ) {
        this( s.getBytes(), s.getOffset(), s.length() );
    }

    /**
     * @NOTE IMPORTANT ensure buffer is big enough as IF ensureCapacity has to grow the array the offset will NOT be considered
     * 
     * @param buf
     * @param offset
     * @param len
     */
    public final void setBuffer( byte[] buf, int len ) {
        _bytes  = buf;
        _len    = len;
        _offset = 0;
    }
    
    /**
     * append a ViewString, DONT append null if from is null 
     * 
     * @param from
     * @return
     */
    public final ReusableString append( final ViewString from ) {
        if ( from != null ) {
            return append( from._bytes, from._offset, from._len );
        }
        return this;
    }

    public final  ReusableString append( final ZString from ) {
        if ( from != null ) {
            return append( from.getBytes(), from.getOffset(), from.length() );
        }
        return this;
    }

    public final ReusableString append( final byte b ) {
        final int newCapacity = _len + 1;
        ensureCapacity( newCapacity );
        _bytes[_len] = b;
        _len = newCapacity;
        return this;
    }

    public final ReusableString append( final byte[] bytes ) {
        final int len = bytes.length;
        final int newCapacity = _len + len;
        ensureCapacity( newCapacity );
        
        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            int srcIdx  = 0;
            int destIdx = _len;
            while( destIdx < newCapacity ) {
                _bytes[ destIdx++ ] = bytes[ srcIdx++ ];
            }
        } else {
            System.arraycopy( bytes, 0, _bytes, _len, len );
        }
        
        _len = newCapacity;

        return this;
    }

    public final ReusableString append( final byte[] bytes, final int from, final int len ) {
        final int newCapacity = _len + len;
        ensureCapacity( newCapacity );
        
        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            int srcIdx  = from;
            int destIdx = _len;
            while( destIdx < newCapacity ) {
                _bytes[ destIdx++ ] = bytes[ srcIdx++ ];
            }
        } else {
            System.arraycopy( bytes, from, _bytes, _len, len );
        }
        
        _len = newCapacity;

        return this;
    }

    public final ReusableString append( final char c ) {
        final int newCapacity = _len + 1;
        ensureCapacity( newCapacity );
        _bytes[_len] = (byte) c;
        _len = newCapacity;
        return this;
    }

    /**
     * appends a double truncated at 6dp to the string
     *
     * truncates decimal places for numbers bigger than 99,999,999,999
     * 
     * @param doubleValue
     * @return
     */
    public final ReusableString append( double doubleValue ) {

        if ( doubleValue == Constants.UNSET_DOUBLE )  return appendNull();
        if ( (long)doubleValue == Constants.UNSET_LONG)     return appendNull();

        if ( doubleValue < 0 ) doubleValue -= Constants.WEIGHT;
        else                   doubleValue += Constants.WEIGHT;
        
        final int estLen = NumberFormatUtils.getPriceLen( doubleValue );
        final int newCapacity = _len + estLen;
        ensureCapacity( newCapacity );

        int fmtLen = NumberFormatUtils.addPrice( _bytes, _len, doubleValue, estLen );

        _len += fmtLen;

        return this;
    }

    public final ReusableString append( final int num ) {
        
        if ( num == Constants.UNSET_INT )  return appendNull();
        
        final int size = (num < 0) ? NumberFormatUtils.getNegIntLen( num ) : NumberFormatUtils.getPosIntLen( num );
        final int newCapacity = _len + size;
        ensureCapacity( newCapacity );
        
        NumberFormatUtils.addInt( _bytes, _len, num, size );
        
        _len = newCapacity;
        
        return this;
    }

    /**
     * append a positive integer as fixed width
     * 
     * @NOTE strictly speaking should NOT assume number is +ve ... although that is guarenteed under  current usage
     * 
     * @param num
     * @param fixedSize
     * @return
     */
    public final ReusableString append( final int num, final int fixedSize ) {
        final int newCapacity = _len + fixedSize;
        ensureCapacity( newCapacity );
        
        NumberFormatUtils.addPositiveIntFixedLength( _bytes, _len, num, fixedSize );
        
        _len = newCapacity;
        
        return this;
    }
    
    /**
     * append a positive long as fixed width
     * 
     * @NOTE strictly speaking should NOT assume number is +ve ... although that is guarenteed under  current usage
     * 
     * @param num
     * @param fixedSize
     * @return
     */
    public final ReusableString append( final long num, final int fixedSize ) {
        final int newCapacity = _len + fixedSize;
        ensureCapacity( newCapacity );
        
        NumberFormatUtils.addPositiveLongFixedLength( _bytes, _len, num, fixedSize );
        
        _len = newCapacity;
        
        return this;
    }
    
    public final ReusableString append( final long num ) {

        if ( num == Constants.UNSET_LONG )  return appendNull();

        final int len = NumberFormatUtils.getLongLen( num );
        final int newCapacity = _len + len;
        ensureCapacity( newCapacity );

        NumberFormatUtils.addLong( _bytes, _len, num, len );

        _len = newCapacity;

        return this;
    }

    public final ReusableString append( final String s ) {
        if ( s != null ) {
            final int sLen  = s.length();
            final int newCapacity = _len + sLen;
            ensureCapacity( newCapacity );
            
            int srcIdx = 0;
            while( srcIdx < sLen ) {
                _bytes[ _len++ ] = (byte)s.charAt( srcIdx++ );
            }
        }
        return this;
    }

    @Override
    public final void setValue( final ZString from ) {
        reset();
        if ( from == null )
            return;
        final int fromLen = from.length();
        ensureCapacity( fromLen );
        from.getBytes( _bytes, 0 );
        _len = fromLen;
    }
    
    public final ReusableString copy( final ZString from ) {
        reset();
        if ( from == null )
            return this;
        final int fromLen = from.length();
        ensureCapacity( fromLen );
        from.getBytes( _bytes, 0 );
        _len = fromLen;
        return this;
    }

    @SuppressWarnings( "deprecation" )
    public final ReusableString copy( final String s ) {
        if ( s != null ) {
            final int len = s.length();
            ensureCapacity( len );
            s.getBytes( 0, len, _bytes, 0 );
            _len = len;
        } else {
            _len = 0;
            _hash = 0;
        }
        return this;
    }
    
    
    public final void copy( final ZString from, final int start, final int len ) {
        reset();
        if ( from == null )
            return;
        ensureCapacity( len );
        from.getBytes( _bytes, start, 0, len );
        _len = len;
    }

    @Override
    public final void ensureCapacity( final int minimumCapacity ) {
        _hash = 0;
        final int curLen = _bytes.length;
        if ( minimumCapacity > curLen ) {
            int newCapacity = curLen + (curLen >> 1);
            if ( newCapacity < 0 || minimumCapacity > newCapacity ) {
                newCapacity = minimumCapacity;
            }
            final byte newValue[] = new byte[newCapacity];
            final int len = _len;
            if ( len > 0 ) {
                if ( curLen < SizeConstants.MIN_MEMCPY_LENGTH ) {
                    int srcIdx  = 0;
                    int destIdx = 0;
                    while( destIdx < len ) {
                        newValue[ destIdx++ ] = _bytes[ srcIdx++ ];
                    }
                } else {
                    System.arraycopy( _bytes, 0, newValue, 0, len );
                }
            }
            _bytes = newValue;
        }
    }

    @Override
    public final ReusableType getReusableType() {
        return _reusableType;
    }

    @Override
    public final void reset() {
        _len = 0;
        _hash = 0;
    }
    
    @Override
    public final int getOffset() {
        return 0;
    }    

    public final ReusableString rtrim() {
        final int oldLen = _len;
        int len = oldLen - 1;
        
        while( len >= 0 && _bytes[len] == ' ' ) {
            len--;
        }
        
        len++;

        if ( len != oldLen ) {
            _hash = 0;
            _len = len;
        }
        
        return this;
    }

    @Override
    /**
     * length = dataLen and EXCLUDES offset
     * 
     * can be used for truncation
     */
    public final void setLength( final int len ) {
        ensureCapacity( len );
        _len = len;
    }

    @Override
    public final void setValue( final byte[] buf ) {
        setValue( buf, 0, buf.length );
    }
    
    @Override
    public final void setValue( final byte[] bytes, final int from, final int len ) {
        ensureCapacity( len );
        
        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            int srcIdx  = from;
            int destIdx = 0;
            while( destIdx < len ) {
                _bytes[ destIdx++ ] = bytes[ srcIdx++ ];
            }
        } else {
            System.arraycopy( bytes, from, _bytes, 0, len );
        }
        
        _len = len;
    }

    public final void setValue( final char ch ) {
        ensureCapacity( 1 );
        _bytes[0] = (byte) ch;
        _len = 1;
    }

    @SuppressWarnings( "deprecation" )
    public final void setValue( final String s ) {
        if ( s != null ) {
            final int len = s.length();
            ensureCapacity( len );
            s.getBytes( 0, len, _bytes, 0 );
            _len = len;
        } else {
            _len = 0;
            _hash = 0;
        }
    }
    
    @Override
    public final ReusableString getNext() {
        return _next;
    }

    @Override
    public final void setNext( final ReusableString nxt ) {
        _next = nxt;
    }

    public final ReusableString append( final Enum<?> val ) {
        
        if ( val == null ) {
            append( NULL_STR );
        } else  {
            append( val.toString() );
        }
        
        return this;
    }

    public final ReusableString append( final boolean val ) {

        append( (val) ? 'Y' : 'N' );
        
        return this;
    }

    public final ReusableString append( ByteBuffer buf ) {
        final int limit = buf.limit();
        final int offset = buf.position();
        
        if ( offset >= limit ) return this;
        
        final int sLen  = limit - offset;
        final int newCapacity = _len + sLen;
        ensureCapacity( newCapacity );

        buf.get( _bytes, _len, sLen );
        
        _len = newCapacity;
        
        return this;
    }

    public void reverse() {
        int end   = _len - 1;
        int start = 0;
        
        while( end > start ) {
            byte tmp      = _bytes[start];
            _bytes[start] = _bytes[end];
            _bytes[end]   = tmp;
            
            end--;
            start++;
        }
    }
    
    public final ReusableString appendHEX( final ZString from ) {
        if ( from != null ) {
            appendReadableHEX( from.getBytes(), from.getOffset(), from.length() );
        }

        return this;
    }

    public final ReusableString appendReadableHEX( final byte[] srcBytes, final int offset, final int len ) {
        final int newCapacity = _len + (len*3);
        ensureCapacity( newCapacity );

        int srcIdx  = offset;
        int destIdx = _len;
        byte b;
        byte major;
        byte minor;
        
        while( destIdx < newCapacity ) {
            b = srcBytes[ srcIdx++ ];

            _bytes[ destIdx++ ] = ' ';

            if ( Character.isISOControl( b ) ) {
                major = (byte)((0xFF & b) >> 4 ); // upper nybble 
                minor = (byte)((0x0F & b) );      // lower nybble
                
                if ( major <= 9 )   major = (byte)(major + '0');
                else                major = (byte)((major + 'A') - 10);
                
                if ( minor <= 9 )   minor = (byte)(minor + '0');
                else                minor = (byte)((minor + 'A') - 10);
                
                _bytes[ destIdx++ ] = major;  
                _bytes[ destIdx++ ] = minor; 
            } else {
                _bytes[ destIdx++ ] = ' ';  
                _bytes[ destIdx++ ] = b;
            }
        }
        
        _len = destIdx;

        return this;
    }

    public final ReusableString appendHEX( final byte[] srcBytes, final int offset, final int len ) {
        final int newCapacity = _len + (len*3);
        ensureCapacity( newCapacity );

        int srcIdx  = offset;
        int destIdx = _len;
        byte b;
        byte major;
        byte minor;
        
        while( destIdx < newCapacity ) {
            b = srcBytes[ srcIdx++ ];

            _bytes[ destIdx++ ] = ' ';

            major = (byte)((0xFF & b) >> 4 ); // upper nybble 
            minor = (byte)((0x0F & b) );      // lower nybble
            
            if ( major <= 9 )   major = (byte)(major + '0');
            else                major = (byte)((major + 'A') - 10);
            
            if ( minor <= 9 )   minor = (byte)(minor + '0');
            else                minor = (byte)((minor + 'A') - 10);
            
            _bytes[ destIdx++ ] = major;  
            _bytes[ destIdx++ ] = minor; 
        }
        
        _len = destIdx;

        return this;
    }

    private ReusableString appendNull() {
        final int newCapacity = _len + 6;
        ensureCapacity( newCapacity );
        _bytes[_len++] = (byte) '[';
        _bytes[_len++] = (byte) 'n';
        _bytes[_len++] = (byte) 'u';
        _bytes[_len++] = (byte) 'l';
        _bytes[_len++] = (byte) 'l';
        _bytes[_len++] = (byte) ']';
        return this;
    }

    public ReusableString append( final ReusableString from, final int substringIdx ) {
        final int len = from.length() - substringIdx;
        final int newCapacity = _len + len;
        final byte[] srcBytes = from.getBytes();
        
        ensureCapacity( newCapacity );
        
        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            int srcIdx  = substringIdx;
            int destIdx = _len;
            while( destIdx < newCapacity ) {
                _bytes[ destIdx++ ] = srcBytes[ srcIdx++ ];
            }
        } else {
            System.arraycopy( srcBytes, substringIdx, _bytes, _len, len );
        }
        
        _len = newCapacity;

        return this;
    }

    public ReusableString append( final ReusableString src, int stIdx, int len ) {
        final int newCapacity = _len + len;
        final byte[] srcBytes = src.getBytes();
        
        ensureCapacity( newCapacity );
        
        if ( len < SizeConstants.MIN_MEMCPY_LENGTH ) {
            int srcIdx  = stIdx;
            int destIdx = _len;
            while( destIdx < newCapacity ) {
                _bytes[ destIdx++ ] = srcBytes[ srcIdx++ ];
            }
        } else {
            System.arraycopy( srcBytes, stIdx, _bytes, _len, len );
        }
        
        _len = newCapacity;

        return this;
    }
}
