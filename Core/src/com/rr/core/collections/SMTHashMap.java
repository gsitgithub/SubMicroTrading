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

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.pool.PoolFactory;
import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Percentiles;

/**
 * non thread safe HashMap which uses SuperPools to avoid GC
 * 
 * @param <K,V>
 */
@SuppressWarnings( "unchecked" )
public class SMTHashMap<K,V> implements SMTMap<K,V> {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    public static final class HashEntryFactory<K, V> implements PoolFactory<HashEntry<K, V>> {

        private SuperPool<HashEntry<K, V>> _superPool;
        private HashEntry<K, V>            _root;

        public HashEntryFactory( SuperPool<HashEntry<K, V>> superPool ) {
            _superPool = superPool;
            _root = _superPool.getChain();
        }

        @Override
        public HashEntry<K, V> get() {
            if ( _root == null ) {
                _root = _superPool.getChain();
            }
            HashEntry<K, V> obj = _root;
            _root = _root.getNext();
            obj.setNext( null );
            return obj;
        }

    }

    public static final class HashEntry<K, V> implements Reusable<HashEntry<K, V>> {

        V                   _value;
        K                   _key;
        HashEntry<K, V>     _next;

        void set( K key, HashEntry<K, V> next, V value ) {
            _key = key;
            _next = next;
            _value = value;
        }

        static final <K, V> HashEntry<K, V>[] newArray( int i ) {
            return new HashEntry[i];
        }

        @Override
        public void reset() {
            _key = null;
            _value = null;
            _next = null;
        }

        @Override
        public HashEntry<K, V> getNext() {
            return _next;
        }

        @Override
        public void setNext( HashEntry<K, V> nxt ) {
            _next = nxt;
        }

        @Override
        public ReusableType getReusableType() {
            return CollectionTypes.MapEntry;
        }
    }

    private HashEntryFactory<K, V>       _entryFactory    = SuperpoolManager.instance().getFactory( HashEntryFactory.class, HashEntry.class );
    private Recycler<HashEntry<K, V>>    _entryRecycler   = SuperpoolManager.instance().getRecycler( HashEntry.class );
    
    private int              _count;
    private HashEntry<K,V>[] _table;
    private int              _tableIndexMask;
    private final float      _loadFactor;
    private int              _threshold;

    public SMTHashMap( int initialCapacity ) {
        this( initialCapacity, 0.75f );
    }
    
    public SMTHashMap( int initialCapacity, float loadFactor ) {
        if ( !(loadFactor > 0) || initialCapacity < 0 )
            throw new IllegalArgumentException();

        if ( initialCapacity > MAXIMUM_CAPACITY )
            initialCapacity = MAXIMUM_CAPACITY;

        int capacity = 1;
        while( capacity < initialCapacity )
            capacity <<= 1;

        _loadFactor = loadFactor;
        HashEntry<K,V>[] table = HashEntry.newArray( capacity ); 
        setTable( table );
    }

    @Override
    public V get( K key ) {
        final int hash = hash( key );
        HashEntry<K,V> e = getFirst( hash );
        while( e != null ) {
            if ( key == e._key ) {
                return e._value;
            }
            e = e._next;
        }
        return null;
    }

    @Override
    public boolean containsKey( K key ) {
        final int hash = hash( key );
        HashEntry<K,V> e = getFirst( hash );
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
    public boolean containsValue( V value ) {
        if ( value == null ) return false;
        
        final HashEntry<K,V>[]  tab = _table;
        int len = tab.length;
        for ( int i = 0 ; i < len ; i++ ) {
            for ( HashEntry<K,V> e = tab[i] ; e != null ; e = e._next ) {
                V v = e._value;
                if ( value.equals( v ) )
                    return true;
            }
        }
        
        return false;
    }

    @Override
    public V put( K key, V value ) {
        final int hash = hash( key );
        int c = _count;
        if ( c++ > _threshold ) // ensure capacity
            rehash();
        final HashEntry<K,V>[]  tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry<K,V> first = tab[index];
        HashEntry<K,V> e = first;
        while( e != null && key != e._key )
            e = e._next;

        V oldValue;
        if ( e != null ) {
            oldValue = e._value;
            e._value = value;
        } else {
            oldValue = null;
            tab[index] = newEntry( key, first, value );
            
            _count = c; 
        }
        return oldValue;
    }

    @Override
    public V remove( K key ) {
        final int hash = hash( key );
        final HashEntry<K,V>[]  tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry<K,V> first = tab[index];
        HashEntry<K,V> prev = first;
        HashEntry<K,V> e = first;
        while( e != null && key != e._key ) {
            prev = e;
            e = e._next;
        }

        V oldValue = null;
        if ( e != null ) {
            oldValue = e._value;
            if ( e == first ) {
                tab[index] = e._next;
            } else {
                prev._next = e._next;
            }
            
            e.setNext( null ); // critical detach
            
            recycleEntry( e );
            
            --_count; 
        }
        return oldValue;
    }

    @Override
    public void clear() {
        final int max = _table.length;
        for ( int i = 0 ; i < max ; ++i ) {
            recycleEntry( _table[i] );
            _table[i] = null;
        }
        _count = 0;
    }

    @Override
    public Collection<K> keys() {
        Collection<K> allKeys = new HashSet<K>( size() );
        
        for ( int i = 0 ; i < _table.length ; ++i ) {
            HashEntry<K,V> e = _table[i];
            while( e != null ){
                allKeys.add( e._key );
                e = e._next;
            }
        }
        
        return allKeys;
    }
    
    
    @Override
    public void logStats( ReusableString out ) {
        int cnt = 0;
        out.append( "SMTHashMap logStats() capacity=" + _table.length + ", entries=" + _count ).append( "\n" );
        
        List<Integer> sizes = new ArrayList<Integer>( cnt / 10 );
        for ( int i = 0 ; i < _table.length ; ++i ) {
            HashEntry<K,V> e = _table[i];
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
    
    public boolean replace( K key, V oldValue, V newValue ) {
        final int hash = hash( key );
        HashEntry<K,V> e = getFirst( hash );
        while( e != null && key != e._key )
            e = e._next;

        boolean replaced = false;
        if ( e != null && oldValue.equals( e._value ) ) {
            replaced = true;
            e._value = newValue;
        }
        return replaced;
    }

    public V replace( K key, V newValue ) {
        final int hash = hash( key );
        HashEntry<K,V> e = getFirst( hash );
        while( e != null && e._key != key )
            e = e._next;

        V oldValue = null;
        if ( e != null ) {
            oldValue = e._value;
            e._value = newValue;
        }
        return oldValue;
    }
    
    int hash( K h ) {
        return h.hashCode();
    }
    
    private void setTable( HashEntry<K,V>[] newTable ) {
        _table          = newTable;
        _threshold      = (int) (_table.length * _loadFactor);
        _tableIndexMask = _table.length - 1;    // as table must be power of 2
    }

    private HashEntry<K,V> getFirst( int hash ) {
        return _table[ hash & _tableIndexMask ];
    }

    private HashEntry<K, V> newEntry( K key, HashEntry<K, V> next, V value ) {
        HashEntry<K, V> entry;
        entry = _entryFactory.get();
        entry.set( key, next, value );
        return entry;
    }

    private void recycleEntry( HashEntry<K, V> entry ) {
        if ( entry != null ) {

            HashEntry<K, V> tmp;

            while( entry != null ) {

                // DONT RECYCLE the KEY !!

                tmp = entry;
                entry = entry.getNext();

                _entryRecycler.recycle( tmp );
            }
        }
    }
    
    private void rehash() {
        HashEntry<K,V>[]  oldTable = _table;
        int oldCapacity = oldTable.length;
        if ( oldCapacity >= MAXIMUM_CAPACITY )
            return;

        HashEntry<K,V>[]  newTable = HashEntry.newArray( oldCapacity << 1 );
        int sizeMask = newTable.length - 1;
        for ( int i = 0 ; i < oldCapacity ; i++ ) {
            HashEntry<K,V> e = oldTable[i];

            if ( e != null ) {
                
                do {
                          HashEntry<K,V> next          = e._next;
                          int       idx                = e._key.hashCode() & sizeMask;
                    final HashEntry<K,V> entryInNewTable = newTable[idx];            // keep current root entry in new table
                    
                    newTable[idx] = e; // hook in e to the root of the new index

                    // try and avoid mutate calls and index ops by moving consecutive entries with same destHashIdx in one go
                    while( next != null ) {
                        final int nextIdx = next._key.hashCode() & sizeMask;
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
