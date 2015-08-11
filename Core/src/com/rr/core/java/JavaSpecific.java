/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.java;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.MappedByteBuffer;

import sun.misc.SunJDK160;
import sun.misc.Unsafe;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;

@SuppressWarnings( "deprecation" )

public final class JavaSpecific {

    private static final Logger _log = LoggerFactory.console( JavaSpecific.class );

    private static final ErrorCode UNMAP_ERR  = new ErrorCode( "JSP100", "Unmap error" );
    private static final ErrorCode OFFSET_ERR = new ErrorCode( "JSP200", "Offset error" );
    
    
    private static final JavaSpecific _instance = new JavaSpecific();

    private final Method  _getCleanerInstance;
    private final Method  _freeMemMap;

    private final Unsafe  _unsafe;

    public static JavaSpecific instance() { return _instance; }
    
    private JavaSpecific(){
        _getCleanerInstance   = SunJDK160.getMemMapCleanerAccessorMethod();
        _freeMemMap           = SunJDK160.getMemMapFreeMethod();
        
        SunJDK160.getMemMapCleanerAccessorMethod(); // open up access
        
        _unsafe = SunJDK160.getUnsafeInstance();
    }
    
    /**
     * access to the CAS spinlock code used in concurrent hashmap implementation
     * @param obj
     * @param fieldOffset
     * @param expVal
     * @param newVal
     * @return
     */
    public final boolean compareAndSwapObject( final Object instance, final long fieldOffset, final Object expVal, final Object newVal ) {
        return _unsafe.compareAndSwapObject( instance, fieldOffset, expVal, newVal );
    }

    public final void unmap( ZString name, MappedByteBuffer b ) {
        try {
            final Object cl = _getCleanerInstance.invoke( b );
            _freeMemMap.invoke( cl );
        } catch( Exception e ) {
            _log.error( UNMAP_ERR, "Unable to free mem map memory for " + name, e );
        }
    }

    public final long getOffset( final Class<?> aclass, final String fieldName, final boolean checkVolative ) {

        try {
            final Field f = aclass.getDeclaredField( fieldName );
            
            f.setAccessible( true );
            
            if ( checkVolative ) {
                int modifiers = 0;
                try {
                    modifiers = f.getModifiers();
                } catch( Exception ex ) {
                    throw new RuntimeException( ex.getMessage(), ex );
                }

                if ( !Modifier.isVolatile(modifiers) )
                    throw new IllegalArgumentException("Must be volatile type");
            }
            
            return _unsafe.objectFieldOffset( f );
             
        } catch( Exception e ) {
            long parent = getOffset( aclass.getSuperclass(), fieldName, checkVolative );
            
            if ( parent >= 0 ) return parent; 
            
            _log.error( OFFSET_ERR, "Unable to get field offset in " + aclass.getSimpleName() + ", field=" + fieldName + ", err="+ e.getMessage() );
            
            return -1;
        }
    }
}
