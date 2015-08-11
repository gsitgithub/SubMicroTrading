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
 * Non Threadsafe OrderMap, map entries spread across segments, each segment with its own array
 * 
 * certain operations like size(), isEmpty(), containsValue() have to loop across segments so can be expensive for lots segments
 */
public final class SegmentOrderMap implements Map<ViewString, Order>, OrderMap {

    private static final int MAX_ARRAY_SIZE = 1 << 30;
    private static final int MAX_SEGMENTS   = 1 << 16; 

    private final int             _segmentMask;              // upper bits of a key's hash code are used to choose the segment.
    private final int             _segmentShift;             // Shift value for indexing within segments.
    private final Segment[]       _segments;
    private       EventProcessor  _proc;

    public SegmentOrderMap( int initialCapacity, float loadFactor, int requestedSegments ) {
        if ( !(loadFactor > 0) || initialCapacity < 0 || requestedSegments <= 0 )
            throw new IllegalArgumentException();
        if ( requestedSegments > MAX_SEGMENTS )
            requestedSegments = MAX_SEGMENTS;

        // Find power-of-two sizes best matching arguments
        int segmentBitShifts = 0;
        int actualSegments = 1;
        while( actualSegments < requestedSegments ) {
            ++segmentBitShifts;
            actualSegments <<= 1;
        }

        _segmentShift = 32 - segmentBitShifts; // highest bits used for segment id
        _segmentMask = actualSegments - 1;
        _segments = Segment.newArray( actualSegments );

        int baseSegmentSize = initialCapacity / actualSegments;
        if ( baseSegmentSize * actualSegments < initialCapacity )
            ++baseSegmentSize;

        int segmentSize = 1;
        while( segmentSize < baseSegmentSize )
            segmentSize <<= 1;

        if ( segmentSize > MAX_ARRAY_SIZE )
            segmentSize = MAX_ARRAY_SIZE;

        for ( int i = 0 ; i < _segments.length ; ++i )
            _segments[i] = new Segment( segmentSize, loadFactor );
    }

    @Override
    public Order get( ViewString key ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).get( key, hashCode );
    }

    @Override
    public boolean containsKey( ViewString key ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).containsKey( key, hashCode );
    }

    @Override
    public void setRecycleProcessor( EventProcessor proc ) {
        _proc = proc;
    }

    @Override
    public int size() {
        int cnt = 0;
        for ( int i = 0 ; i < _segments.length ; i++ ) {
            cnt += _segments[i]._count;
        }
        return cnt;
    }

    @Override
    public boolean isEmpty() {
        for ( int i = 0 ; i < _segments.length ; i++ ) {
            if ( _segments[i]._count > 0 )
                return false;
        }

        return true;
    }

    @Override
    public boolean containsKey( Object key ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).containsKey( key, hashCode );
    }

    @Override
    public boolean containsValue( Object value ) {
        for ( int i = 0 ; i < _segments.length ; i++ ) {
            if ( _segments[i].containsValue( value ) )
                return true;
        }
        return false;
    }

    @Override
    public Order get( Object key ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).get( key, hashCode );
    }

    @Override
    public Order put( ViewString key, Order value ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).put( key, hashCode, value, false );
    }

    @Override
    public Order  putIfAbsent( ViewString key, Order  value ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).put( key, hashCode, value, true );
    }

    @Override
    public boolean putIfKeyAbsent( ViewString key, Order  value ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).putIfKeyAbsent( key, hashCode, value );
    }
    
    @Override
    public Order remove( Object key ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).remove( key, hashCode, null );
    }

    @Override
    public void putAll( Map<? extends ViewString, ? extends Order> map ) {
        for ( Map.Entry<? extends ViewString, ? extends Order> entry : map.entrySet() ) {
            put( entry.getKey(), entry.getValue() );
        }
    }

    @Override
    public void clear() {
        for ( int i = 0 ; i < _segments.length ; ++i ) {
            _segments[i].clear( _proc );
        }
    }

    @Override
    public Set<ViewString> keySet() {
        throw new SMTRuntimeException( "SegmentOrderMap.keySet not yet implemented" );
    }

    @Override
    public Collection<Order> values() {
        throw new SMTRuntimeException( "SegmentOrderMap.values not yet implemented" );
    }

    @Override
    public Set<java.util.Map.Entry<ViewString, Order>> entrySet() {
        throw new SMTRuntimeException( "SegmentOrderMap.entrySet not yet implemented" );
    }

    @Override
    public boolean replace( ViewString key, Order oldValue, Order newValue ) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).replace( key, hashCode, oldValue, newValue );
    }

    @Override
    public Order  replace(ViewString key, Order  value) {
        final int hashCode = rehash( key.hashCode() );
        return segmentFor( hashCode ).replace( key, hashCode, value );
    }

    @Override
    public void logStats( ReusableString out ) {
        int cnt = 0;
        int cap = 0;
        ReusableString buf = new ReusableString();
        for ( int i = 0 ; i < _segments.length ; i++ ) {
            cnt += _segments[i]._count;
            cap += _segments[i]._table.length;
            if ( i < 33 ) buf.append( _segments[i]._count ).append( ", " );
        }
        
        if ( _segments.length  > 32 ) buf.append( "..." );

        out.append( "SegmentOrderMap logStats() entries=" + cnt + ", segmentSizes=" + buf + "\n" );
        
        List<Integer> sizes = new ArrayList<Integer>( cnt / 10 );
        for ( int i = 0 ; i < _segments.length ; i++ ) {
            Segment s = _segments[i];
            s.addStats( sizes );
        }
        
        int[] cnts = new int[ sizes.size() ];
        for( int i=0 ; i < sizes.size() ; i++ ) {
            cnts[i] = sizes.get( i ).intValue();
        }
        
        Percentiles p = new Percentiles( cnts );
        
        out.append( "Map list capacity=" + cap + ", chain sizes "   + ", chains=" + cnts.length + 
                   ", med=" + p.median()     + ", ave=" + p.getAverage() + 
                   ", min=" + p.getMinimum() + ", max=" + p.getMaximum() + 
                   "\n"                      +
                   ", p99=" + p.calc( 99 )   + ", p95=" + p.calc( 95 )   + 
                   ", p90=" + p.calc( 90 )   + ", p80=" + p.calc( 80 )   + 
                   ", p70=" + p.calc( 70 )   + ", p50=" + p.calc( 50 )   + "\n" );
    }
    
    private int rehash( int h ) {
        // Spread bits to regularize both segment and index locations, using variant of single-word Wang/Jenkins hash.
        h += (h <<  15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h <<   3);
        h ^= (h >>>  6);
        h += (h <<   2) + (h << 14);
        return h ^ (h >>> 16);
    }
    
    private final Segment segmentFor( final int hash ) {
        return _segments[(hash >>> _segmentShift) & _segmentMask];
    }

    // ------------------------------------ Inner Classes --------------------------------------------

    private static class Segment {
        public  int         _count = 0;
        public  HashEntry[] _table;
        private int         _tableIndexMask;
        private final float _loadFactor;
        private int         _threshold;

        private final HashEntryHelper _helper = new HashEntryHelper();

        static final Segment[] newArray( int size ) {
            return new Segment[size];
        }

        public void addStats( List<Integer> sizes ) {
            for ( int i = 0 ; i < _table.length ; ++i ) {
                HashEntry e = _table[i];
                int cnt=0;
                while( e != null ){
                    ++cnt;
                    e = e._next;
                }
                if ( cnt > 0 ) {
                    sizes.add( new Integer(cnt) );
                }
            }
        }

        Segment( int initialCapacity, float lf ) {
            _loadFactor = lf;
            setTable( _helper.newArray( initialCapacity ) );
        }

        void setTable( HashEntry[] newTable ) {
            _table          = newTable;
            _threshold      = (int) (_table.length * _loadFactor);
            _tableIndexMask = _table.length - 1;    // as table must be power of 2
        }

        void clear( EventProcessor proc ) {
            final int max = _table.length;
            for ( int i = 0 ; i < max ; ++i ) {
                final HashEntry e = _table[i];
                _helper.recycleEntry( e, proc );
                _table[i] = null;
            }
            _count = 0;
        }

        HashEntry getFirst( int hash ) {
            final HashEntry[] tab = _table;
            final int index = hash & _tableIndexMask;

            return tab[ index ];
        }

        Order get( Object key, int hash ) {
            HashEntry e = getFirst( hash );
            while( e != null ) {
                if ( e._hash == hash && key.equals( e._key ) ) {
                    return e._value;
                }
                e = e._next;
            }
            return null;
        }

        boolean containsKey( ViewString key, int hash ) {
            HashEntry e = getFirst( hash );
            while( e != null ) {
                if ( e._hash == hash && key.equals( e._key ) )
                    return true;
                e = e._next;
            }
            return false;
        }

        boolean containsKey( Object key, int hash ) {
            HashEntry e = getFirst( hash );
            while( e != null ) {
                if ( e._hash == hash && key.equals( e._key ) )
                    return true;
                e = e._next;
            }
            return false;
        }

        boolean containsValue( Object value ) {
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

        boolean replace( ViewString key, int hash, Order oldValue, Order newValue ) {
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

        Order replace( ViewString key, int hash, Order newValue ) {
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

        Order put( ViewString key, int hash, Order value, boolean onlyIfAbsent ) {
            int c = _count;
            if ( c++ > _threshold ) // ensure capacity
                rehash();
            final HashEntry[] tab = _table;
            final int index = hash & _tableIndexMask;
            final HashEntry first = tab[index];
            HashEntry e = first;
            while( e != null && (e._hash != hash || !key.equals( e._key )) )
                e = e._next;

            Order oldValue;
            if ( e != null ) {
                oldValue = e._value;
                if ( !onlyIfAbsent )
                    e._value = value;
            } else {
                oldValue = null;
                tab[index] = _helper.newEntry( key, hash, first, value );
                _count = c; 
            }
            return oldValue;
        }
        
        boolean putIfKeyAbsent( ViewString key, int hash, Order value ) {
            int c = _count;
            if ( c++ > _threshold ) // ensure capacity
                rehash();
            final HashEntry[] tab = _table;
            final int index = hash & _tableIndexMask;
            final HashEntry first = tab[index];
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
        
        void rehash() {
            HashEntry[] oldTable = _table;
            int oldCapacity = oldTable.length;
            if ( oldCapacity >= MAX_ARRAY_SIZE )
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

        /**
         * Remove; match on key only if value null, else match both.
         * 
         * DOESNT RECYLE THE ORDER
         */
        Order remove( Object key, int hash, Object value ) {
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
                Order v = e._value;
                if ( value == null || value.equals( v ) ) {
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
            }
            return oldValue;
        }
    }
}
