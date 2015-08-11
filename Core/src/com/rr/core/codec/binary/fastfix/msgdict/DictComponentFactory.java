/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.msgdict;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.common.FieldDataType;
import com.rr.core.codec.binary.fastfix.common.FieldReader;
import com.rr.core.codec.binary.fastfix.common.FieldWriter;
import com.rr.core.codec.binary.fastfix.common.constant.ConstantFieldReader;
import com.rr.core.codec.binary.fastfix.common.constant.ConstantFieldWriter;
import com.rr.core.codec.binary.fastfix.common.noop.NoOpFieldReader;
import com.rr.core.codec.binary.fastfix.common.noop.NoOpFieldWriter;
import com.rr.core.codec.binary.fastfix.fulldict.entry.DictEntry;
import com.rr.core.codec.binary.fastfix.fulldict.entry.DoubleFieldDictEntry;
import com.rr.core.codec.binary.fastfix.fulldict.entry.IntFieldDictEntry;
import com.rr.core.codec.binary.fastfix.fulldict.entry.LongFieldDictEntry;
import com.rr.core.codec.binary.fastfix.fulldict.entry.StringFieldDictEntry;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.utils.SMTRuntimeException;

/**
 * threadsafe factory cache, instance unique by name
 * 
 * factory create methods should check matching initVal and type
 *
 * @author Richard Rose
 */
public class DictComponentFactory implements ComponentFactory {

    private ConcurrentHashMap<String,FieldWriter> _mapWriter = new ConcurrentHashMap<String,FieldWriter>(); 
    private ConcurrentHashMap<String,FieldReader> _mapReader = new ConcurrentHashMap<String,FieldReader>(); 
    
    private ConcurrentHashMap<String,DictEntry> _prevHolders = new ConcurrentHashMap<String,DictEntry>(); 

    @SuppressWarnings( { "unchecked" } )
    @Override
    public <T extends FieldWriter, V> T getWriter( Class<T> fieldClass, Object ... args ) {
        
        T fld = null;
        T registeredFld = null; 
        
        try {
            Constructor<T> c;

            Class<?>[] argClasses = new Class<?>[args.length];
            
            for( int i=0 ; i < args.length ; i++ ) {
                Class<?> curArgClass = args[i].getClass();
                
                if ( curArgClass == Integer.class ) {
                    argClasses[i] = int.class;
                } else if ( curArgClass == Long.class ) {
                    argClasses[i] = long.class;
                } else if ( curArgClass == Double.class ) {
                    argClasses[i] = double.class;
                } else if ( curArgClass == ReusableString.class ) {
                    argClasses[i] = ZString.class;
                } else if ( args[i] instanceof ComponentFactory ) {
                    argClasses[i] = ComponentFactory.class;
                } else {
                    argClasses[i] = curArgClass;
                }
            }
            
            c = fieldClass.getConstructor( argClasses );
            fld = c.newInstance( args );
            
            if ( fld instanceof ConstantFieldWriter ) return fld;   // its a constant field, no reset or dict entry required 
            if ( fld instanceof NoOpFieldWriter )     return fld;   // its a no-op constant field, no reset or dict entry required 

            registeredFld = (T) registerWriter( fld );

        } catch( Exception e ) {
            throw new RuntimeException( e );
        }
        
        if ( fld == registeredFld ) return fld;
        
        try {
            Class<?>[] noArgs = new Class<?>[0]; 
                    
            Method getInitValue = fieldClass.getMethod( "getInitValue", noArgs );
            
            V newInitVal  = (V) getInitValue.invoke( fld );
            V prevInitVal = (V) getInitValue.invoke( registeredFld );
            
            if ( ! prevInitVal.equals( newInitVal ) ) {
                throw new SMTRuntimeException( "FastFix writer component mismatch for name=" + fld.getName() + ", registeredInitVal=" + 
                                               prevInitVal + ", mismatch=" + newInitVal);
            }
        } catch( NoSuchMethodException e ) {
            
            // ok this class doesnt have initVal
            
        } catch( IllegalAccessException e ) {
            throw new RuntimeException( e );
        } catch( IllegalArgumentException e ) {
            throw new RuntimeException( e );
        } catch( InvocationTargetException e ) {
            throw new RuntimeException( e );
        }

        return registeredFld;
    }

    @SuppressWarnings( { "unchecked" } )
    @Override
    public <T extends FieldReader, V> T getReader( Class<T> fieldClass, Object ... args ) {
        
        T fld = null;
        T registeredFld = null; 
        
        try {
            Constructor<T> c;

            Class<?>[] argClasses = new Class<?>[args.length];
            
            for( int i=0 ; i < args.length ; i++ ) {
                if ( args[i] == null ) {
                    argClasses[i] = String.class;
                } else {
                    Class<?> curArgClass = args[i].getClass();
                    
                    if ( curArgClass == Integer.class ) {
                        argClasses[i] = int.class;
                    } else if ( curArgClass == Long.class ) {
                        argClasses[i] = long.class;
                    } else if ( curArgClass == Double.class ) {
                        argClasses[i] = double.class;
                    } else if ( curArgClass == ReusableString.class ) {
                        argClasses[i] = ZString.class;
                    } else if ( args[i] instanceof ComponentFactory ) {
                        argClasses[i] = ComponentFactory.class;
                    } else {
                        argClasses[i] = curArgClass;
                    }
                }
            }
            
            c = fieldClass.getConstructor( argClasses );
            fld = c.newInstance( args );
            
            if ( fld instanceof ConstantFieldReader ) return fld;   // its a constant field, no reset or dict entry required 
            if ( fld instanceof NoOpFieldReader )     return fld;   // its a no-op constant field, no reset or dict entry required 

            registeredFld = (T) registerReader( fld );

        } catch( Exception e ) {
            throw new RuntimeException( e );
        }
            
        if ( fld == registeredFld ) return fld;

        try {
            Class<?>[] noArgs = new Class<?>[0]; 
                    
            Method getInitValue = fieldClass.getMethod( "getInitValue", noArgs );
            
            V newInitVal  = (V) getInitValue.invoke( fld );
            V prevInitVal = (V) getInitValue.invoke( registeredFld );
            
            if ( ! prevInitVal.equals( newInitVal ) ) {
                throw new SMTRuntimeException( "FastFix reader component mismatch for name=" + fld.getName() + ", registeredInitVal=" + 
                                               prevInitVal + ", mismatch=" + newInitVal);
            }
        } catch( NoSuchMethodException e ) {
            
            // ok this class doesnt have initVal
            
        } catch( IllegalAccessException e ) {
            throw new RuntimeException( e );
        } catch( IllegalArgumentException e ) {
            throw new RuntimeException( e );
        } catch( InvocationTargetException e ) {
            throw new RuntimeException( e );
        }

        return registeredFld;
    }

    private synchronized FieldWriter registerWriter( FieldWriter fld ) {
        
        FieldWriter existing = _mapWriter.putIfAbsent( fld.getName(), fld );
        
        if ( existing != null ) {
            
            if ( existing.getClass() != fld.getClass() ) {
                throw new SMTRuntimeException( "FastFix component writer mismatch for name=" + fld.getName() + 
                                               ", registered=" + existing.getClass().getName() + 
                                               ", mismatch=" + fld.getClass().getName() );
            }
            
            fld = existing;         // throw away instance and use registered one
        }
        
        return fld;
    }

    private synchronized FieldReader registerReader( FieldReader fld ) {
        
        FieldReader existing = _mapReader.putIfAbsent( fld.getName(), fld );
        
        if ( existing != null ) {
            
            if ( existing.getClass() != fld.getClass() ) {
                throw new SMTRuntimeException( "FastFix component reader mismatch for name=" + fld.getName() + 
                                               ", registered=" + existing.getClass().getName() + 
                                               ", mismatch=" + fld.getClass().getName() );
            }
            
            fld = existing;         // throw away instance and use registered one
        }
        
        return fld;
    }

    @Override
    public Collection<DictEntry> getDictEntries() {
        return _prevHolders.values();
    }

    @Override
    public DictEntry getPrevFieldValWrapper( String name, FieldDataType type, String initVal ) {
        
        DictEntry prev = _prevHolders.get( name );
        
        if ( prev != null ) return prev;
        
        switch ( type ) {
        case length:
        case uInt32:
        case int32:
            prev = new IntFieldDictEntry( initVal ); 
            break;
        case uInt64:
        case int64:
            prev = new LongFieldDictEntry( initVal ); 
            break;
        case string:
            prev = new StringFieldDictEntry( initVal ); 
            break;
        case decimal: // decimal fall thru to exception as should be catered for in add decimal
            prev = new DoubleFieldDictEntry( initVal ); 
            break;
        default:
            throw new SMTRuntimeException( "Unsupported type .. must change code for support of new type" );
        }
        
        _prevHolders.put( name, prev );
        
        return prev;
    }
    
    @Override
    public DictEntry getPrevFieldValInt32Wrapper( String name, int initVal ) {
        DictEntry prev = _prevHolders.get( name );
        
        if ( prev != null ) return prev;
        
        prev = new IntFieldDictEntry( initVal ); 
        
        _prevHolders.put( name, prev );
        
        return prev;
    }

    @Override
    public DictEntry getPrevFieldValInt64Wrapper( String name, long initVal ) {
        DictEntry prev = _prevHolders.get( name );
        
        if ( prev != null ) return prev;
        
        prev = new LongFieldDictEntry( initVal ); 
        
        _prevHolders.put( name, prev );
        
        return prev;
    }
    
}
