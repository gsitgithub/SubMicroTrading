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
public class LongHashSet implements LongSet {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private static class HashEntry {
        long      _key;
        HashEntry _next;
        
        public HashEntry( long key, HashEntry next ) {
            _key   = key;
            _next  = next;
        }

        static final HashEntry[] newArray( int size ) {
            return new HashEntry[ size ];
        }
    }
    
    private int              _count;
    private HashEntry[]      _table;
    private int              _tableIndexMask;
    private final float      _loadFactor;
    private int              _threshold;

    public LongHashSet( int initialCapacity, float loadFactor ) {
        if ( !(loadFactor > 0) || initialCapacity < 0 )
            throw new IllegalArgumentException();

        if ( initialCapacity > MAXIMUM_CAPACITY )
            initialCapacity = MAXIMUM_CAPACITY;

        int capacity = 1;
        while( capacity < initialCapacity )
            capacity <<= 1;

        _loadFactor = loadFactor;
        HashEntry[] table = HashEntry.newArray( capacity ); 
        setTable( table );
    }

    @Override
    public boolean contains( long key ) {
        final int hash = hash( key );
        HashEntry e = getFirst( hash );
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
    public boolean add( long key ) {
        final int hash = hash( key );
        int c = _count;
        if ( c++ > _threshold ) // ensure capacity
            rehash();
        final HashEntry[]  tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry first = tab[index];
        HashEntry e = first;
        while( e != null && key != e._key )
            e = e._next;

        if ( e != null ) {
            return false;
        } 

        tab[index] = new HashEntry( key, first );
        _count = c; 
        
        return true;
    }

    @Override
    public boolean remove( long key ) {
        final int hash = hash( key );
        final HashEntry[]  tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry first = tab[index];
        HashEntry prev = first;
        HashEntry e = first;
        while( e != null && key != e._key ) {
            prev = e;
            e = e._next;
        }

        boolean removed = false;
        
        if ( e != null ) {
            removed = true;
            if ( e == first ) {
                tab[index] = e._next;
            } else {
                prev._next = e._next;
            }
            --_count; 
        }
        
        return removed;
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
    public Collection<Long> keys() {
        Collection<Long> allKeys = new HashSet<Long>( size() );
        
        for ( int i = 0 ; i < _table.length ; ++i ) {
            HashEntry e = _table[i];
            while( e != null ){
                allKeys.add( new Long( e._key ) );
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
            HashEntry e = _table[i];
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
    
    static int hash( long h ) {
        return (int)h;
    }
    
    private void setTable( HashEntry[] newTable ) {
        _table          = newTable;
        _threshold      = (int) (_table.length * _loadFactor);
        _tableIndexMask = _table.length - 1;    // as table must be power of 2
    }

    private HashEntry getFirst( int hash ) {
        return _table[ hash & _tableIndexMask ];
    }

    private void rehash() {
        HashEntry[]  oldTable = _table;
        int oldCapacity = oldTable.length;
        if ( oldCapacity >= MAXIMUM_CAPACITY )
            return;

        HashEntry[]  newTable = HashEntry.newArray( oldCapacity << 1 );
        int sizeMask = newTable.length - 1;
        for ( int i = 0 ; i < oldCapacity ; i++ ) {
            HashEntry e = oldTable[i];

            if ( e != null ) {
                
                do {
                          HashEntry next            = e._next;
                          int       idx             = hash(e._key) & sizeMask;
                    final HashEntry entryInNewTable = newTable[idx];            // keep current root entry in new table
                    
                    newTable[idx] = e; // hook in e to the root of the new index

                    // try and avoid mutate calls and index ops by moving consecutive entries with same destHashIdx in one go
                    while( next != null ) {
                        final int nextIdx = hash(next._key) & sizeMask;
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
