/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.utils.Percentiles;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.om.order.Order;
import com.rr.om.processor.EventProcessor;

/**
 * Non Threadsafe OrderMap 
 */
public final class SimpleOrderMap implements Map<ViewString, Order>, OrderMap {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    private EventProcessor        _proc;

    private int                   _count;
    private HashEntry[]           _table;
    private int                   _tableIndexMask;
    private int                   _threshold;

    private final float           _loadFactor;
    private final HashEntryHelper _helper = new HashEntryHelper();

    public SimpleOrderMap( int initialCapacity, float loadFactor ) {
        if ( !(loadFactor > 0) || initialCapacity < 0 )
            throw new IllegalArgumentException();

        if ( initialCapacity > MAXIMUM_CAPACITY )
            initialCapacity = MAXIMUM_CAPACITY;

        int capacity = 1;
        while( capacity < initialCapacity )
            capacity <<= 1;

        _loadFactor = loadFactor;
        setTable( _helper.newArray( capacity ) );
    }

    @Override
    public Order get( ViewString key ) {
        final int hash = hash( key.hashCode() );
        HashEntry e = getFirst( hash );
        while( e != null ) {
            if ( e._hash == hash && key.equals( e._key ) ) {
                return e._value;
            }
            e = e._next;
        }
        return null;
    }

    @Override
    public boolean containsKey( ViewString key ) {
        final int hash = hash( key.hashCode() );
        HashEntry e = getFirst( hash );
        while( e != null ) {
            if ( e._hash == hash && key.equals( e._key ) )
                return true;
            e = e._next;
        }
        return false;
    }

    @Override
    public void setRecycleProcessor( EventProcessor proc ) {
        _proc = proc;
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
    public boolean containsKey( Object key ) {
        final int hash = hash( key.hashCode() );
        HashEntry e = getFirst( hash );
        while( e != null ) {
            if ( e._hash == hash && key.equals( e._key ) )
                return true;
            e = e._next;
        }
        return false;
    }

    @Override
    public boolean containsValue( Object value ) {
        if ( value == null ) return false;
        
        final HashEntry[] tab = _table;
        int len = tab.length;
        for ( int i = 0 ; i < len ; i++ ) {
            for ( HashEntry e = tab[i] ; e != null ; e = e._next ) {
                Order v = e._value;
                if ( value.equals( v ) )
                    return true;
            }
        }
        
        return false;
    }

    @Override
    public Order get( Object key ) {
        final int hash = hash( key.hashCode() );
        HashEntry e = getFirst( hash );
        while( e != null ) {
            if ( e._hash == hash && key.equals( e._key ) ) {
                return e._value;
            }
            e = e._next;
        }
        return null;
    }

    @Override
    public Order put( ViewString key, Order value ) {
        final int hash = hash( key.hashCode() );
        int c = _count;
        if ( c++ > _threshold ) // ensure capacity
            rehash();
        final HashEntry[] tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry first = tab[index];
        HashEntry e = first;
        while( e != null && (e._hash != hash || !key.equals( e._key )) )
            e = e._next;

        Order oldValue;
        if ( e != null ) {
            oldValue = e._value;
            e._value = value;
        } else {
            oldValue = null;
            tab[index] = _helper.newEntry( key, hash, first, value );
            _count = c; 
        }
        return oldValue;
    }

    @Override
    public boolean putIfKeyAbsent( ViewString key, Order value ) {
        final int hash = hash( key.hashCode() );
        int c = _count;
        if ( c++ > _threshold ) // ensure capacity
            rehash();
        final HashEntry[] tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry first = tab[index];
        HashEntry e = first;
        while( e != null && (e._hash != hash || !key.equals( e._key )) )
            e = e._next;

        if ( e != null ) {
            return false;
        }
        
        tab[index] = _helper.newEntry( key, hash, first, value );
        _count = c;
        
        return true;
    }

    @Override
    public Order remove( Object key ) {
        final int hash = hash( key.hashCode() );
        final HashEntry[] tab = _table;
        int index = hash & _tableIndexMask;
        HashEntry first = tab[index];
        HashEntry prev = first;
        HashEntry e = first;
        while( e != null && (e._hash != hash || !key.equals( e._key )) ) {
            prev = e;
            e = e._next;
        }

        Order oldValue = null;
        if ( e != null ) {
            oldValue = e._value;
            if ( e == first ) {
                tab[index] = e._next;
            } else {
                prev._next = e._next;
            }
            e._next = null; // dont want to recyle next nodes so wipe the ref
            // DONT PASS IN _proc AS DONT WANT RECYCLE ORDER
            _helper.recycleEntry( e, null );
            --_count; 
        }
        return oldValue;
    }

    @Override
    public void putAll( Map<? extends ViewString, ? extends Order> map ) {
        for ( Map.Entry<? extends ViewString, ? extends Order> entry : map.entrySet() ) {
            put( entry.getKey(), entry.getValue() );
        }
    }

    @Override
    public void clear() {
        final int max = _table.length;
        for ( int i = 0 ; i < max ; ++i ) {
            final HashEntry e = _table[i];
            _helper.recycleEntry( e, _proc );
            _table[i] = null;
        }
        _count = 0;
    }

    @Override
    public Set<ViewString> keySet() {
        throw new SMTRuntimeException( "SimpleOrderMap.keySet not yet implemented" );
    }

    @Override
    public Collection<Order> values() {
        throw new SMTRuntimeException( "SimpleOrderMap.values not yet implemented" );
    }

    @Override
    public Set<java.util.Map.Entry<ViewString, Order>> entrySet() {
        throw new SMTRuntimeException( "SimpleOrderMap.entrySet not yet implemented" );
    }

    @Override
    public void logStats( ReusableString out ) {
        int cnt = 0;
        out.append( "SimpleOrderMap logStats() capacity=" + _table.length + ", entries=" + _count ).append( "\n" );
        
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
    
    @Override
    public boolean replace( ViewString key, Order oldValue, Order newValue ) {
        final int hash = hash( key.hashCode() );
        HashEntry e = getFirst( hash );
        while( e != null && (e._hash != hash || !key.equals( e._key )) )
            e = e._next;

        boolean replaced = false;
        if ( e != null && oldValue.equals( e._value ) ) {
            replaced = true;
            e._value = newValue;
        }
        return replaced;
    }

    @Override
    public Order replace( ViewString key, Order newValue ) {
        final int hash = hash( key.hashCode() );
        HashEntry e = getFirst( hash );
        while( e != null && (e._hash != hash || !key.equals( e._key )) )
            e = e._next;

        Order oldValue = null;
        if ( e != null ) {
            oldValue = e._value;
            e._value = newValue;
        }
        return oldValue;
    }
    
    static int hash( int h ) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
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
        HashEntry[] oldTable = _table;
        int oldCapacity = oldTable.length;
        if ( oldCapacity >= MAXIMUM_CAPACITY )
            return;

        HashEntry[] newTable = _helper.newArray( oldCapacity << 1 );
        int sizeMask = newTable.length - 1;
        for ( int i = 0 ; i < oldCapacity ; i++ ) {
            HashEntry e = oldTable[i];

            if ( e != null ) {
                
                do {
                          HashEntry next            = e._next;
                          int       idx             = e._hash & sizeMask;
                    final HashEntry entryInNewTable = newTable[idx];            // keep current root entry in new table
                    
                    newTable[idx] = e; // hook in e to the root of the new index

                    // try and avoid mutate calls and index ops by moving consecutive entries with same destHashIdx in one go
                    while( next != null ) {
                        final int nextIdx = next._hash & sizeMask;
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
