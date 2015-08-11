/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package sun.misc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

@Deprecated
@SuppressWarnings( {"dep-ann", "rawtypes"} )


public final class Unsafe {

    private static native void registerNatives();

    private static final Unsafe theUnsafe            = new Unsafe();
    public static final int     INVALID_FIELD_OFFSET = -1;

    static {
        registerNatives();
    }

    private Unsafe() { /* nothing */ }

    public static Unsafe getUnsafe() {
        return theUnsafe;
    }

    public native int getInt( Object obj, long l );

    public native void putInt( Object obj, long l, int i );

    public native Object getObject( Object obj, long l );

    public native void putObject( Object obj, long l, Object obj1 );

    public native boolean getBoolean( Object obj, long l );

    public native void putBoolean( Object obj, long l, boolean flag );

    public native byte getByte( Object obj, long l );

    public native void putByte( Object obj, long l, byte byte0 );

    public native short getShort( Object obj, long l );

    public native void putShort( Object obj, long l, short word0 );

    public native char getChar( Object obj, long l );

    public native void putChar( Object obj, long l, char c );

    public native long getLong( Object obj, long l );

    public native void putLong( Object obj, long l, long l1 );

    public native float getFloat( Object obj, long l );

    public native void putFloat( Object obj, long l, float f );

    public native double getDouble( Object obj, long l );

    public native void putDouble( Object obj, long l, double d );

    /**
     * @deprecated Method getInt is deprecated
     */

    public int getInt( Object obj, int i ) {
        return getInt( obj, i );
    }

    /**
     * @deprecated Method putInt is deprecated
     */

    public void putInt( Object obj, int i, int j ) {
        putInt( obj, i, j );
    }

    /**
     * @deprecated Method getObject is deprecated
     */

    public Object getObject( Object obj, int i ) {
        return getObject( obj, i );
    }

    /**
     * @deprecated Method putObject is deprecated
     */

    public void putObject( Object obj, int i, Object obj1 ) {
        putObject( obj, i, obj1 );
    }

    /**
     * @deprecated Method getBoolean is deprecated
     */

    public boolean getBoolean( Object obj, int i ) {
        return getBoolean( obj, i );
    }

    /**
     * @deprecated Method putBoolean is deprecated
     */

    public void putBoolean( Object obj, int i, boolean flag ) {
        putBoolean( obj, i, flag );
    }

    /**
     * @deprecated Method getByte is deprecated
     */

    public byte getByte( Object obj, int i ) {
        return getByte( obj, i );
    }

    /**
     * @deprecated Method putByte is deprecated
     */

    public void putByte( Object obj, int i, byte byte0 ) {
        putByte( obj, i, byte0 );
    }

    /**
     * @deprecated Method getShort is deprecated
     */

    public short getShort( Object obj, int i ) {
        return getShort( obj, i );
    }

    /**
     * @deprecated Method putShort is deprecated
     */

    public void putShort( Object obj, int i, short word0 ) {
        putShort( obj, i, word0 );
    }

    /**
     * @deprecated Method getChar is deprecated
     */

    public char getChar( Object obj, int i ) {
        return getChar( obj, i );
    }

    /**
     * @deprecated Method putChar is deprecated
     */

    public void putChar( Object obj, int i, char c ) {
        putChar( obj, i, c );
    }

    /**
     * @deprecated Method getLong is deprecated
     */

    public long getLong( Object obj, int i ) {
        return getLong( obj, i );
    }

    /**
     * @deprecated Method putLong is deprecated
     */

    public void putLong( Object obj, int i, long l ) {
        putLong( obj, i, l );
    }

    /**
     * @deprecated Method getFloat is deprecated
     */

    public float getFloat( Object obj, int i ) {
        return getFloat( obj, i );
    }

    /**
     * @deprecated Method putFloat is deprecated
     */

    public void putFloat( Object obj, int i, float f ) {
        putFloat( obj, i, f );
    }

    /**
     * @deprecated Method getDouble is deprecated
     */

    public double getDouble( Object obj, int i ) {
        return getDouble( obj, i );
    }

    /**
     * @deprecated Method putDouble is deprecated
     */

    public void putDouble( Object obj, int i, double d ) {
        putDouble( obj, i, d );
    }

    public native byte getByte( long l );

    public native void putByte( long l, byte byte0 );

    public native short getShort( long l );

    public native void putShort( long l, short word0 );

    public native char getChar( long l );

    public native void putChar( long l, char c );

    public native int getInt( long l );

    public native void putInt( long l, int i );

    public native long getLong( long l );

    public native void putLong( long l, long l1 );

    public native float getFloat( long l );

    public native void putFloat( long l, float f );

    public native double getDouble( long l );

    public native void putDouble( long l, double d );

    public native long getAddress( long l );

    public native void putAddress( long l, long l1 );

    public native long allocateMemory( long l );

    public native long reallocateMemory( long l, long l1 );

    public native void setMemory( long l, long l1, byte byte0 );

    public native void copyMemory( long l, long l1, long l2 );

    public native void freeMemory( long l );

    /**
     * @deprecated Method fieldOffset is deprecated
     */

    public int fieldOffset( Field field ) {
        if ( Modifier.isStatic( field.getModifiers() ) )
            return (int) staticFieldOffset( field );
        return (int) objectFieldOffset( field );
    }

    /**
     * @deprecated Method staticFieldBase is deprecated
     */

    public Object staticFieldBase( Class class1 ) {
        Field afield[] = class1.getDeclaredFields();
        for ( int i = 0 ; i < afield.length ; i++ )
            if ( Modifier.isStatic( afield[i].getModifiers() ) )
                return staticFieldBase( afield[i] );

        return null;
    }

    public native long staticFieldOffset( Field field );

    public native long objectFieldOffset( Field field );

    public native Object staticFieldBase( Field field );

    public native void ensureClassInitialized( Class class1 );

    public native int arrayBaseOffset( Class class1 );

    public native int arrayIndexScale( Class class1 );

    public native int addressSize();

    public native int pageSize();

    public native Class defineClass( String s, byte abyte0[], int i, int j, ClassLoader classloader, ProtectionDomain protectiondomain );

    public native Class defineClass( String s, byte abyte0[], int i, int j );

    public native Object allocateInstance( Class class1 ) throws InstantiationException;

    public native void monitorEnter( Object obj );

    public native void monitorExit( Object obj );

    public native boolean tryMonitorEnter( Object obj );

    public native void throwException( Throwable throwable );

    public final native boolean compareAndSwapObject( Object obj, long l, Object obj1, Object obj2 );

    public final native boolean compareAndSwapInt( Object obj, long l, int i, int j );

    public final native boolean compareAndSwapLong( Object obj, long l, long l1, long l2 );

    public native Object getObjectVolatile( Object obj, long l );

    public native void putObjectVolatile( Object obj, long l, Object obj1 );

    public native int getIntVolatile( Object obj, long l );

    public native void putIntVolatile( Object obj, long l, int i );

    public native boolean getBooleanVolatile( Object obj, long l );

    public native void putBooleanVolatile( Object obj, long l, boolean flag );

    public native byte getByteVolatile( Object obj, long l );

    public native void putByteVolatile( Object obj, long l, byte byte0 );

    public native short getShortVolatile( Object obj, long l );

    public native void putShortVolatile( Object obj, long l, short word0 );

    public native char getCharVolatile( Object obj, long l );

    public native void putCharVolatile( Object obj, long l, char c );

    public native long getLongVolatile( Object obj, long l );

    public native void putLongVolatile( Object obj, long l, long l1 );

    public native float getFloatVolatile( Object obj, long l );

    public native void putFloatVolatile( Object obj, long l, float f );

    public native double getDoubleVolatile( Object obj, long l );

    public native void putDoubleVolatile( Object obj, long l, double d );

    public native void putOrderedObject( Object obj, long l, Object obj1 );

    public native void putOrderedInt( Object obj, long l, int i );

    public native void putOrderedLong( Object obj, long l, long l1 );

    public native void unpark( Object obj );

    public native void park( boolean flag, long l );

    public native int getLoadAverage( double ad[], int i );
}
