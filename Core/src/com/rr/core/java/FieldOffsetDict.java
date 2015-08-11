/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.java;

import com.rr.core.lang.HasReusableType;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ReusableTypeIDFactory;

public class FieldOffsetDict {

    private static final long EMPTY = -1;
    
    private final Class<? extends HasReusableType> _superType;
    private final String _fieldName;

    private long[] _offset;
    private Class<?>[] _classCheck;
    
    public FieldOffsetDict( Class<? extends HasReusableType> superType, String fieldName ) {
        _superType = superType;
        
        _fieldName = fieldName;
        
        int maxId = ReusableTypeIDFactory.maxId();
        
        if ( maxId > 8192 ) throw new RuntimeException( "ReusableTypeIDs too high, refactor without big gaps!" );
        _offset = new long[ maxId ];
        _classCheck = new Class<?>[ _offset.length ];
        for( int i=0 ; i < _offset.length ; ++i ) {
            _offset[i] = EMPTY;    
            _classCheck[i] = null;
        }
    }
    
    public long getOffset( HasReusableType r, boolean isCheckVolatile ) {
        
        long offset;
        
        ReusableType t = r.getReusableType();
        
        if ( t == null ) throw new RuntimeException( "FieldOffsetDict reusableType is null for " + r.getClass().getSimpleName() );
        
        int typeId = t.getId();
        
        if ( typeId >= _offset.length ) {
            resize( typeId );
        }
        
        offset = _offset[ typeId ];
        
        Class<?> theClass = r.getClass();
        
        if ( offset == EMPTY ) {

            if ( _superType.isAssignableFrom( theClass ) ) {
        
                offset = JavaSpecific.instance().getOffset( r.getClass(), _fieldName, isCheckVolatile );
                
                if ( offset < 0 ) {
                    throw new RuntimeException( "FieldOffsetDictionary cant find offset of field " + _fieldName + " in " + 
                                                r.getClass().getSimpleName() );
                }

                // dont worry about threading conflict is not problematic
                
                _offset[ typeId ] = offset;
                _classCheck[ typeId ] = theClass;
                
            } else {
                throw new RuntimeException( "FieldOffSetDict expected to be instanceof " + _superType.getSimpleName() +
                                            " but " + r.getClass().getSimpleName() + " is not" );
            }
        } else {
            if ( _classCheck[ typeId ] != theClass ) {
                throw new RuntimeException( "Mismatch in FieldOffsetDict, reusableTypeId=" + typeId + 
                                            ", expected=" + _classCheck[typeId].getSimpleName() + 
                                            ", not=" + theClass.getSimpleName() );
            }
        }
        
        return offset;
    }

    // dont worry about threading here
    private synchronized void resize( int max ) {
        max += 1;
        
        long[] newOffset = new long[ max ];
        Class<?>[] newClass = new Class<?>[ max ];

        for( int i=0 ; i < _offset.length ; ++i ) {
            newOffset[ i ] = _offset[ i ];    
            newClass[ i ]  = _classCheck[ i ];
        }

        for( int i=_offset.length ; i < max ; ++i ) {
            newOffset[i] = EMPTY;    
            newClass[i]  = null;    
        }
        
        _offset     = newOffset;
        _classCheck = newClass;
    }
}
