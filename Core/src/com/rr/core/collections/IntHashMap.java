/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.rr.core.lang.ReusableString;
import com.rr.core.utils.Percentiles;

/**
 * non thread safe templated hash map which uses primitive ints as keys without autoboxing
 *
 * currently does NOT pool entry objects so only populate at startup with minor updates during day
 * 
 * @param <T>
 */
public class IntHashMap<T> implements IntMap<T> {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static class HashEntry<T> {
        int          _key;
        T            _value;
        HashEntry<T> _next;
        
        public HashEntry( int key, HashEntry<T> next, T value ) {
            _key   = key;
            _next  = next;
            _value = value;
        }

        @SuppressWarnings( "unchecked" )
        static final <T> HashEntry<T>[] newArray( int size ) {
            return new HashEntry[ size ];
        }
    }
    
    
    private int              _count;
    private HashEntry<T>[]   _table;
    private int              _tableIndexMask;
    private final float      _loadFactor;
    private int              _threshold;

    public IntHashMap( int initialCapacity, float loadFactor ) {
        if ( !(loadFactor > 0) || initialCapacity < 0 )
            throw new IllegalArgumentException();

        if ( initialCapacity > MAXIMUM_CAPACITY )
            initialCapacity = MAXIMUM_CAPACITY;

        int capacity = 1;
        while( capacity < initialCapacity )
            capacity <<= 1;

        _loadFactor = loadFactor;
        HashEntry<T>[] table = HashEntry.newArray( capacity ); 
        setTable( table );
    }

    @Override
    public T get( int key ) {
        final int hash = hash( key );
        HashEntry<T> e = getFirst( hash );
        while( e != null ) {
            if ( key == e._key ) {
                return e._value;
            }
            e = e._next;
        }
        return null;
    }

    @Override
    public boolean containsKey( int key ) {
        final int hash = hash( key );
        HashEntry<T> e = getFirst( hash );
        while( e != null ) {
            if ( key == e._key )
                return true;
            e = e._next;
        }
        return false;
    }

    @Override
    public int size() {
        return _count;
    }

    @Override
    public boolean isEmpty() {
        return _count == 0;
    }

    @Override
    public boolean containsValue( T value ) {
        if ( value == null ) return false;
        
        final HashEntry<T>[]  tab = _table;
        int len = tab.length;
        for ( int i = 0 ; i < len ; i++ ) {
            for ( HashEntry<T> e = tab[i] ; e != null ; e = e._next ) {
                T v = e._value;
                if ( value.equals( v ) )
                    return true;
            }
        }
        
        return false;
    }

    @Override
    public T put( int key, T value ) {
        final int hash = hash( key );
        int c = _count;
        if ( c++ > _threshold ) // ensure capacity
            rehash();
        final HashEntry<T>[]  tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry<T> first = tab[index];
        HashEntry<T> e = first;
        while( e != null && key != e._key )
            e = e._next;

        T oldValue;
        if ( e != null ) {
            oldValue = e._value;
            e._value = value;
        } else {
            oldValue = null;
            tab[index] = new HashEntry<T>( key, first, value );
            _count = c; 
        }
        return oldValue;
    }

    @Override
    public T remove( int key ) {
        final int hash = hash( key );
        final HashEntry<T>[]  tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry<T> first = tab[index];
        HashEntry<T> prev = first;
        HashEntry<T> e = first;
        while( e != null && key != e._key ) {
            prev = e;
            e = e._next;
        }

        T oldValue = null;
        if ( e != null ) {
            oldValue = e._value;
            if ( e == first ) {
                tab[index] = e._next;
            } else {
                prev._next = e._next;
            }
            --_count; 
        }
        return oldValue;
    }

    @Override
    public void clear() {
        final int max = _table.length;
        for ( int i = 0 ; i < max ; ++i ) {
            _table[i] = null;
        }
        _count = 0;
    }

    @Override
    public Collection<Integer> keys() {
        Collection<Integer> allKeys = new HashSet<Integer>( size() );
        
        for ( int i = 0 ; i < _table.length ; ++i ) {
            HashEntry<T> e = _table[i];
            while( e != null ){
                allKeys.add( new Integer( e._key ) );
                e = e._next;
            }
        }
        
        return allKeys;
    }
    
    
    @Override
    public void logStats( ReusableString out ) {
        int cnt = 0;
        out.append( "SimpleTMap logStats() capacity=" + _table.length + ", entries=" + _count ).append( "\n" );
        
        List<Integer> sizes = new ArrayList<Integer>( cnt / 10 );
        for ( int i = 0 ; i < _table.length ; ++i ) {
            HashEntry<T> e = _table[i];
            int entries=0;
            while( e != null ){
                ++entries;
                e = e._next;
            }
            if ( cnt > 0 ) {
                sizes.add( new Integer(entries) );
            }
        }
        
        int[] cnts = new int[ sizes.size() ];
        for( int i=0 ; i < sizes.size() ; i++ ) {
            cnts[i] = sizes.get( i ).intValue();
        }
        
        Percentiles p = new Percentiles( cnts );
        
        out.append( "Map list chain sizes "   + ", chains=" + cnts + 
                   ", med=" + p.median()     + ", ave=" + p.getAverage() + 
                   ", min=" + p.getMinimum() + ", max=" + p.getMaximum() + 
                   "\n"                      +
                   ", p99=" + p.calc( 99 )   + ", p95=" + p.calc( 95 )   + 
                   ", p90=" + p.calc( 90 )   + ", p80=" + p.calc( 80 )   + 
                   ", p70=" + p.calc( 70 )   + ", p50=" + p.calc( 50 )   + "\n" );
    }
    
    public boolean replace( int key, T oldValue, T newValue ) {
        final int hash = hash( key );
        HashEntry<T> e = getFirst( hash );
        while( e != null && key != e._key )
            e = e._next;

        boolean replaced = false;
        if ( e != null && oldValue.equals( e._value ) ) {
            replaced = true;
            e._value = newValue;
        }
        return replaced;
    }

    public T replace( int key, T newValue ) {
        final int hash = hash( key );
        HashEntry<T> e = getFirst( hash );
        while( e != null && e._key != key )
            e = e._next;

        T oldValue = null;
        if ( e != null ) {
            oldValue = e._value;
            e._value = newValue;
        }
        return oldValue;
    }
    
    static int hash( int h ) {
        return h;
    }
    
    private void setTable( HashEntry<T>[] newTable ) {
        _table          = newTable;
        _threshold      = (int) (_table.length * _loadFactor);
        _tableIndexMask = _table.length - 1;    // as table must be power of 2
    }

    private HashEntry<T> getFirst( int hash ) {
        return _table[ hash & _tableIndexMask ];
    }

    private void rehash() {
        HashEntry<T>[]  oldTable = _table;
        int oldCapacity = oldTable.length;
        if ( oldCapacity >= MAXIMUM_CAPACITY )
            return;

        HashEntry<T>[]  newTable = HashEntry.newArray( oldCapacity << 1 );
        int sizeMask = newTable.length - 1;
        for ( int i = 0 ; i < oldCapacity ; i++ ) {
            HashEntry<T> e = oldTable[i];

            if ( e != null ) {
                
                do {
                          HashEntry<T> next            = e._next;
                          int       idx                = e._key & sizeMask;
                    final HashEntry<T> entryInNewTable = newTable[idx];            // keep current root entry in new table
                    
                    newTable[idx] = e; // hook in e to the root of the new index

                    // try and avoid mutate calls and index ops by moving consecutive entries with same destHashIdx in one go
                    while( next != null ) {
                        final int nextIdx = next._key & sizeMask;
                        if ( nextIdx == idx ) {
                            e = next;
                            next = next._next;
                        } else {
                            break;
                        }
                    }
                    
                    e._next = entryInNewTable;
                    e = next;
                } while( e != null );
            }
        }
        setTable( newTable );            
    }
}
