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
import java.util.List;

import com.rr.core.lang.ReusableString;
import com.rr.core.pool.Recycler;
import com.rr.core.utils.Percentiles;

/**
 * non thread safe templated hash map which uses primitive longs as keys without autoboxing
 *
 * uses superpool so some overhead from pool management
 * 
 * @param <T>
 */
public class LongHashMap<T> implements LongMap<T> {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private final LongHashEntryHelper<T> _helper;
    
    private int                 _count;
    private LongHashEntry<T>[]  _table;
    private int                 _tableIndexMask;
    private final float         _loadFactor;
    private int                 _threshold;

    @SuppressWarnings( "rawtypes" )
    private static final Cleaner<?> _dummyCleaner = new Cleaner(){
                                                            @Override
                                                            public void clean( Object valToClean ) {
                                                                // nothing
                                                            }};

    @SuppressWarnings( "unchecked" )
    private Cleaner<T>          _cleaner = (com.rr.core.collections.LongMap.Cleaner<T>) _dummyCleaner;

    public LongHashMap( int initialCapacity, float loadFactor ) {
        this( initialCapacity, loadFactor, new LongHashEntryHelper<T>() );    
    }
    
    public LongHashMap( int                        initialCapacity, 
                        float                      loadFactor, 
                        LongHashEntryFactory<T>    entryFactory, 
                        Recycler<LongHashEntry<T>> entryRecycler ) {
        
        this( initialCapacity, loadFactor, new LongHashEntryHelper<T>( entryFactory, entryRecycler ) );    
    }

    public LongHashMap( int initialCapacity, float loadFactor, LongHashEntryHelper<T> helper ) {
        
        if ( !(loadFactor > 0) || initialCapacity < 0 )
            throw new IllegalArgumentException();

        _helper = helper;

        if ( initialCapacity > MAXIMUM_CAPACITY )
            initialCapacity = MAXIMUM_CAPACITY;

        int capacity = 1;
        while( capacity < initialCapacity )
            capacity <<= 1;

        _loadFactor = loadFactor;
        
        setTable( _helper.newArray( capacity ) );
    }

    @Override
    public T get( long key ) {
        final int hash = hash( key );
        LongHashEntry<T> e = getFirst( hash );
        while( e != null ) {
            if ( key == e._key ) {
                return e._value;
            }
            e = e._next;
        }
        return null;
    }

    @Override
    public boolean containsKey( long key ) {
        final int hash = hash( key );
        LongHashEntry<T> e = getFirst( hash );
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
        
        final LongHashEntry<T>[]  tab = _table;
        int len = tab.length;
        for ( int i = 0 ; i < len ; i++ ) {
            for ( LongHashEntry<T> e = tab[i] ; e != null ; e = e._next ) {
                T v = e._value;
                if ( value.equals( v ) )
                    return true;
            }
        }
        
        return false;
    }

    @Override
    public T put( long key, T value ) {
        final int hash = hash( key );
        int c = _count;
        if ( c++ > _threshold ) // ensure capacity
            rehash();
        final LongHashEntry<T>[]  tab = _table;
        int index = hash & _tableIndexMask;
        LongHashEntry<T> first = tab[index];
        LongHashEntry<T> e = first;
        while( e != null && key != e._key )
            e = e._next;

        T oldValue;
        if ( e != null ) {
            oldValue = e._value;
            e._value = value;
        } else {
            oldValue = null;
            tab[index] = _helper.newEntry( key, first, value );
            _count = c; 
        }
        return oldValue;
    }

    @Override
    public boolean putIfKeyAbsent( long key, T value ) {
        final int hash = hash( key );
        int c = _count;
        if ( c++ > _threshold ) // ensure capacity
            rehash();
        final LongHashEntry<T>[]  tab = _table;
        int index = hash & _tableIndexMask;
        LongHashEntry<T> first = tab[index];
        LongHashEntry<T> e = first;
        while( e != null && key != e._key )
            e = e._next;

        if ( e != null ) {
            return false;
        }
        
        tab[index] = _helper.newEntry( key, first, value );
        _count = c;
        
        return true;
    }

    
    @Override
    public T remove( long key ) {
        final int hash = hash( key );
        final LongHashEntry<T>[]  tab = _table;
        int index = hash & _tableIndexMask;
        LongHashEntry<T> first = tab[index];
        LongHashEntry<T> prev = first;
        LongHashEntry<T> e = first;
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
            _helper.recycleEntry( e );
            --_count; 
        }
        return oldValue;
    }

    @Override
    public void clear() {
        final int max = _table.length;
        for ( int i = 0 ; i < max ; ++i ) {
            final LongHashEntry<T> e = _table[i];
            _cleaner.clean( e._value );
            _helper.recycleEntry( e );
            _table[i] = null;
        }
        _count = 0;
    }

    @Override
    public void logStats( ReusableString out ) {
        int cnt = 0;
        out.append( "LongHashMap logStats() capacity=" + _table.length + ", entries=" + _count ).append( "\n" );
        
        List<Integer> sizes = new ArrayList<Integer>( cnt / 10 );
        for ( int i = 0 ; i < _table.length ; ++i ) {
            LongHashEntry<T> e = _table[i];
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
        LongHashEntry<T> e = getFirst( hash );
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
        LongHashEntry<T> e = getFirst( hash );
        while( e != null && e._key != key )
            e = e._next;

        T oldValue = null;
        if ( e != null ) {
            oldValue = e._value;
            e._value = newValue;
        }
        return oldValue;
    }
    
    @Override
    public void registerCleaner( Cleaner<T> cleaner ) {
        _cleaner = cleaner;
    }
    
    static int hash( long h ) {
        return (int)h;
    }
    
    private void setTable( LongHashEntry<T>[] newTable ) {
        _table          = newTable;
        _threshold      = (int) (_table.length * _loadFactor);
        _tableIndexMask = _table.length - 1;    // as table must be power of 2
    }

    private LongHashEntry<T> getFirst( int hash ) {
        return _table[ hash & _tableIndexMask ];
    }

    private void rehash() {
        LongHashEntry<T>[]  oldTable = _table;
        int oldCapacity = oldTable.length;
        if ( oldCapacity >= MAXIMUM_CAPACITY )
            return;

        LongHashEntry<T>[] newTable = _helper.newArray( oldCapacity << 1 );
        int newSizeMask = newTable.length - 1;
        for ( int i = 0 ; i < oldCapacity ; i++ ) {
            LongHashEntry<T> e = oldTable[i];

            if ( e != null ) {
                
                do {
                          LongHashEntry<T> next            = e._next;
                          int              newIdx          = (int)(e._key & newSizeMask);
                    final LongHashEntry<T> entryInNewTable = newTable[newIdx];            // keep current root entry in new table
                    
                    newTable[newIdx] = e; // hook in e to the root of the new index

                    // try and avoid mutate calls and index ops by moving consecutive entries with same destHashIdx in one go
                    while( next != null ) {
                        final int nextIdx = (int)(next._key & newSizeMask);
                        if ( nextIdx == newIdx ) {
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
