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
import java.lang.reflect.Method;



@SuppressWarnings( "deprecation" )

public class SunJDK160 {
    
    public static Unsafe getUnsafeInstance() {
        Unsafe unsafe = null;
        try {
            Class<?> uc = Unsafe.class;
            Field[] fields = uc.getDeclaredFields();
            for(int i = 0; i < fields.length; i++) {
                if(fields[i].getName().equals("theUnsafe")) {
                    fields[i].setAccessible(true);
                    unsafe = (Unsafe) fields[i].get(uc);
                    break;
                }
            }
        } catch( Exception e ) {
            throw new RuntimeException( "Unable to get access to Unsafe instance : " + e.getMessage(), e );
        }
        
        return unsafe;
    }

    public static Method getMemMapCleanerAccessorMethod() {
        return bypassSecurityAndGetMethod( "java.nio.DirectByteBuffer", "cleaner" );
    }
    
    public static Method getMemMapFreeMethod() {
        return bypassSecurityAndGetMethod( "sun.misc.Cleaner", "clean" );
    }

    public static Method getUnsafeCompareAndSwap( ) {

        String className  = "sun.misc.Unsafe";
        String methodName = "compareAndSwapObject";

        Method m = null;
        
        try {
            Class<?> c = Class.forName( className );
            Class<?>[] param = { Object.class, long.class, Object.class, Object.class };
            m = c.getMethod( methodName, param );
            m.setAccessible( true );
        } catch( Exception e ) {
            throw new RuntimeException( "Unable to get method " + methodName + " in " + className + " via reflection : " + e.getMessage(), e );
        }
        
        return m;
    }
    
    private static Method bypassSecurityAndGetMethod( String className, String methodName ) {
        
        Method m = null;
        
        try {
            Class<?> c = Class.forName( className );
            Class<?>[] param = {};
            m = c.getMethod( methodName, param );
            m.setAccessible( true );
        } catch( Exception e ) {
            throw new RuntimeException( "Unable to get method " + methodName + " in " + className + " via reflection : " + e.getMessage(), e );
        }
        
        return m;
    }
}
