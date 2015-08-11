/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;


public class ReflectUtils {

    private static final Class<?>[] NULL_CLASS_ARGS = {};
    private static final Object[]   NULL_ARGS       = {};

    public static <T> T create( String className ) {
        T instance;
        try {
            @SuppressWarnings( "unchecked" )
            Class<T> theClass = (Class<T>) Class.forName( className );
            instance = theClass.newInstance();
        } catch( Exception e ) {
            throw new RuntimeException( "Unable to instantiate class " + className, e );
        }
        
        return instance;
    }
    

    public static <T> T create( String className, Class<?>[] pClass, Object[] pArgs ) {
        T instance;
        try {
            @SuppressWarnings( "unchecked" )
            Class<T> tClass = (Class<T>) Class.forName( className );

            Constructor<? extends T> c;

            if ( pClass.length == 0 ) {
                return create( tClass );
            } 
            
            c = tClass.getConstructor( pClass );
            instance = c.newInstance( pArgs );
            
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Unable to instantiate class " + className + " : " + e.getMessage(), e );
        }
        
        return instance;
    }

    public static <T> T create( Class<? extends T> className ) {
        T instance;
        try {
            instance = className.newInstance();
        } catch( Exception e ) {
            throw new RuntimeException( "Unable to instantiate class " + className.getName(), e );
        }
        
        return instance;
    }
    
    @SuppressWarnings( "unchecked" )
    public static <T> T getStaticMember( Class<? extends T> tClass, String memberName ) {

        T val = null;
        
        try{ 
            Field f = tClass.getField( memberName );
            
            if ( f == null ) {
                throw new SMTRuntimeException( "getStaticMember()  memberName " + memberName + " doesnt exist in " + tClass.getSimpleName() );
            }

            val = (T) f.get( null );
            
        } catch( Exception e ) {
            throw new SMTRuntimeException( "getStaticMember()  unable to access memberName " + memberName + " doesnt exist in " + tClass.getSimpleName(), e );
        }

        if ( val == null ) {
            throw new SMTRuntimeException( "getStaticMember()  memberName " + memberName + " is NULL in " + tClass.getSimpleName() );
        }
        
        return val;
    }

    public static <T> T create( Class<? extends T> tClass, Class<?>[] pClass, Object[] pArgs ) {
        T instance;
        try {
            Constructor<? extends T> c;

            if ( pClass.length == 0 ) {
                return create( tClass );
            } 
            
            c = tClass.getConstructor( pClass );
            instance = c.newInstance( pArgs );
            
        } catch( Exception e ) {
            throw new RuntimeException( "Unable to instantiate class " + tClass.getName(), e );
        }
        
        return instance;
    }

    public static Set<Field> getMembers( Object obj ) {
        Set<Field> fields = new LinkedHashSet<Field>();
        
        Class<?> clazz = obj.getClass();
        
        while( clazz != null ) {
            Field[] mFields = clazz.getDeclaredFields();
            
            for( Field f : mFields ) {
                if ( ! fields.contains( f ) ) {
                    fields.add( f );
                }
            }

            clazz = clazz.getSuperclass();
        }
        
        return fields;
    }

    /**
     * use reflection to set the member field to the supplied value
     * 
     * convert to bool/double/int/long as appropriate
     * 
     * for fields that have an array, the value is treated as comma delimited list of values
     * 
     * @NOTE not efficient ..only for use in non time sensitive code
     * 
     * @param obj - instance to set field in 
     * @param f
     * @param value (in string form)
     */
    public static void setMember( Object obj, Field f, String value ) {
        Class<?> type = f.getType();
        
        boolean wasAccessable = f.isAccessible();
        try {
            f.setAccessible( true );
            if ( type.isArray() ) {
                setArrayMember( obj, f, value, type );
            } else if ( ! setPrimitive( obj, f, value, type ) ) {
                throw new SMTRuntimeException( "ReflectUtils.setMember() unable to set field " + f.getName() + ", as type " + type.getSimpleName() +
                                               " not supported" );
            }
        } catch( ClassNotFoundException e ) {
            throw new SMTRuntimeException( "ReflectUtils.setMember() unable to set field " + f.getName() + ", to unfound class" + value, e );
        } catch( NumberFormatException e ) {
            throw new SMTRuntimeException( "ReflectUtils.setMember() unable to set field " + f.getName() + ", to invalid value " + value, e );
        } catch( IllegalArgumentException e ) {
            throw new SMTRuntimeException( "ReflectUtils.setMember() unable to set field " + f.getName() + ", as type " + type.getSimpleName() +
                                           " not supported", e );
        } catch( IllegalAccessException e ) {
            throw new SMTRuntimeException( "ReflectUtils.setMember() unable to set field " + f.getName() + ", as type " + type.getSimpleName() +
                                           " access not allowed", e );
        } finally {
            f.setAccessible( wasAccessable );
        }
    }
    
    public static void setArrayMember( Object obj, Field f, String arrValue, Class<?> type ) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        
        String vals[] = arrValue.split( "," );
        
        Object arr = Array.newInstance( type.getComponentType(), vals.length );

        for ( int i = 0; i < vals.length ; i++) {
            String value = vals[ i ].trim();

            if ( ZString.class.isAssignableFrom( type ) ) {
                Array.set(arr, i, new ReusableString( value ) );
            } else if ( type == String.class ) {
                Array.set(arr, i, value );
            } else if ( type == Double.class || type == double.class ) {
                double d = Double.parseDouble( value );
                Array.setDouble(arr, i, d );
            } else if ( type == Long.class || type == long.class ) {
                long l = Long.parseLong( value );
                Array.setLong(arr, i, l );
            } else if ( type == Boolean.class || type == boolean.class ) {
                boolean b = Boolean.parseBoolean( value );
                Array.setBoolean( arr, i, b );
            } else if ( type == Integer.class || type == int.class ) {
                int v = Integer.parseInt( value );
                Array.setInt( arr, i, v );
            } else if ( type == Class.class ) {
                Class<?> c = Class.forName( value );
                Array.set(arr, i, c );
            } else if ( Enum.class.isAssignableFrom( type ) ) {
                @SuppressWarnings( { "rawtypes", "unchecked" } )
                Class<? extends Enum> etype = (Class<? extends Enum>) f.getType();
                @SuppressWarnings( "unchecked" )
                Object val = Enum.valueOf( etype, value );
                Array.set(arr, i, val );
            }         
        }

        f.set( obj, arr );
    }

    public static ReusableString dump( ReusableString dumpBuf, Object obj ) {
        Set<Field> fields = new LinkedHashSet<Field>();
        
        Class<?> clazz = obj.getClass();
        
        dumpBuf.append( ' ' ).append( clazz.getSimpleName() );
        
        while( clazz != null ) {
            Field[] mFields = clazz.getDeclaredFields();
            
            for( Field f : mFields ) {
                if ( ! fields.contains( f ) ) {
                    Class<?> type = f.getType();
                    
                    boolean wasAccessable = f.isAccessible();
                    try {
                        f.setAccessible( true );
                        if ( ZString.class.isAssignableFrom( type ) ) {
                            ReusableString val = (ReusableString) f.get( obj );
                            if ( val != null && val.length() > 0 ) dumpBuf.append( ", " ).append( f.getName() ).append( '=' ).append( val );
                        } else if ( type == String.class ) {
                            String val = (String) f.get( obj );
                            if ( val != null && val.length() > 0 ) dumpBuf.append( ", " ).append( f.getName() ).append( '=' ).append( val );
                        } else if ( type == Double.class || type == double.class ) {
                            dumpBuf.append( ", " ).append( f.getName() ).append( '=' ).append( f.getDouble( obj ) );
                        } else if ( type == Long.class || type == long.class ) {
                            dumpBuf.append( ", " ).append( f.getName() ).append( '=' ).append( f.getLong( obj ) );
                        } else if ( type == Boolean.class || type == boolean.class ) {
                            dumpBuf.append( ", " ).append( f.getName() ).append( '=' ).append( f.getBoolean( obj ) );
                        } else if ( type == Integer.class || type == int.class ) {
                            dumpBuf.append( ", " ).append( f.getName() ).append( '=' ).append( f.getInt( obj ) );
                        }
                    } catch( Exception e ) {
                        // ignore
                    } finally {
                        f.setAccessible( wasAccessable );
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }
        
        return dumpBuf;
    }


    /**
     * silent reflect invocation of method name if it exists
     * 
     * @param methodName
     * @param obj
     * @return true if invoked false if not
     */
    public static boolean invoke( String methodName, Object obj ) {
        Class<?> c = obj.getClass();
        
        try {
            Method m = c.getMethod( methodName, NULL_CLASS_ARGS );
            
            m.invoke( obj, NULL_ARGS );
        } catch( Exception e ) {
            return false;
        }
        
        return true;
    }

    /**
     * if class has a static instance() return that, else create new instance 
     * @param className
     * @return instance of className
     */
    @SuppressWarnings( "unchecked" )
    public static <T> T findInstance( String className ) {
        try {
            Class<T> c = (Class<T>) Class.forName( className );
            
            Method m = c.getMethod( "instance", NULL_CLASS_ARGS );
            
            if ( m != null ) {
                return (T) m.invoke( null, NULL_ARGS );
            }

            return c.newInstance();
            
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Unable to findInstance for class " + className );
        }
    }
    
    public static void setProperties( Object dest, Map<String,String> props ) {
        Set<Field> fields = ReflectUtils.getMembers( dest );
        
        for( Map.Entry<String, String> entry : props.entrySet() ) {
            String propertyEntry = entry.getKey();
            String value = entry.getValue();
            
            setProperty( dest, fields, propertyEntry, value );
        }
    }

    public static void setProperty( Object dest, String fieldName, String value ) {
        Set<Field> fields = ReflectUtils.getMembers( dest );

        setProperty( dest, fields, fieldName, value );
    }

    private static void setProperty( Object dest, Set<Field> fields, String propertyEntry, String value ) {
        boolean set = false;
        
        for( Field f : fields ) {               //   iterate thru all the fields in the propObject looking for match 
            String fieldName = f.getName();
            
            if ( fieldName.charAt( 0 ) == '_' ) fieldName = fieldName.substring( 1 );
       
            if ( fieldName.equalsIgnoreCase( propertyEntry ) ) {

                if ( value != null && value.length() > 0 ) {
                    setMember( dest, f, value );
                }

                set = true;
                break; // DONE - NEXT PROPERTY ENTRY
            }
        }

        if ( !set ) {
            throw new SMTRuntimeException( "Unable to reflect set property [" + propertyEntry + "] as thats not valid member of " + dest.getClass().getSimpleName() );
        }
    }
    
    private static boolean setPrimitive( Object obj, Field f, String value, Class<?> type ) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException {
        if ( ZString.class.isAssignableFrom( type ) ) {
            f.set( obj, new ReusableString( value ) );
            return true;
        } else if ( type == String.class ) {
            f.set( obj, value );
            return true;
        } else if ( type == Double.class || type == double.class ) {
            double d = Double.parseDouble( value );
            f.setDouble( obj, d );
            return true;
        } else if ( type == Long.class || type == long.class ) {
            long l = Long.parseLong( value );
            f.setLong( obj, l );
            return true;
        } else if ( type == Boolean.class || type == boolean.class ) {
            boolean b = Boolean.parseBoolean( value );
            f.setBoolean( obj, b );
            return true;
        } else if ( type == Integer.class || type == int.class ) {
            int i = Integer.parseInt( value );
            f.setInt( obj, i );
            return true;
        } else if ( type == Class.class ) {
            Class<?> c = Class.forName( value );
            f.set( obj, c );
            return true;
        } else if ( Enum.class.isAssignableFrom( type ) ) {
            @SuppressWarnings( { "rawtypes", "unchecked" } )
            Class<? extends Enum> etype = (Class<? extends Enum>) f.getType();
            @SuppressWarnings( "unchecked" )
            Object val = Enum.valueOf( etype, value );
            f.set( obj, val );
            return true;
        }         
        return false;
    }
}
