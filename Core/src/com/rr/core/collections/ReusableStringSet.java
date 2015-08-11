/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.recycler.ReusableStringRecycler;

/**
 * specialised set just for ReusableString
 * 
 * strings put into the set are OWNED by the set and the NEXT pointer MAY be changed by the set
 * as it uses this for the hash clash chains
 * 
 * does NOT use supplemental hash so intended for use on fairly unique keys
 * 
 * doesnt need create ANY temporary objects, resize only creates an array
 * 
 * @TODO large sets (>32000 would benefit from splitting set into segments ... tho that needs checking for heap impact)
 */
public class ReusableStringSet {

    private static final int MAXIMUM_CAPACITY = 1 << 30;
    
    private final float            _loadFactor;
    private       int              _threshold;
    private       ReusableString[] _table;

    private       int              _size = 0;
    
    public ReusableStringSet( int initialCapacity, float loadFactor ) {
        if ( initialCapacity < 0 )                        throw new IllegalArgumentException( "Illegal initial capacity: " + initialCapacity );
        if ( initialCapacity > MAXIMUM_CAPACITY )         initialCapacity = MAXIMUM_CAPACITY;
        if ( loadFactor <= 0 || Float.isNaN(loadFactor) ) throw new IllegalArgumentException( "Illegal load factor: " + loadFactor );

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;

        _loadFactor = loadFactor;
        _threshold  = (int)(capacity * loadFactor);
        _table      = new ReusableString[ capacity ];
    }
    
    public ReusableStringSet( int presize ) {
        this( presize, 0.75f );
    }

    /**
     * @param key
     * @return true if the key was successfully entered in the set
     */
    public boolean put( ReusableString key ) {
        if ( key == null ) return false;
        
        final int hash        = key.hashCode();
        final int bucketIndex = indexFor( hash, _table.length );
        
        final ReusableString bucketHead = _table[ bucketIndex ];
        
        for ( ReusableString entry = bucketHead ; entry != null; entry = entry.getNext() ) {
            if ( entry == key || key.equals(entry) ) {
                
                ReusableStringRecycler recycler = TLC.instance().getReusableStringRecycleFactory();

                recycler.recycle( key );
                
                return false;   // already in set DONT add
            }
        }

        key.setNext( bucketHead );
        
        _table[ bucketIndex ] = key;
        if ( _size++ >= _threshold )
            resize( _table.length << 1 );
        
        return true;
    }

    public boolean contains( ViewString key ) {
        if ( key == null ) return false;
        
        final int hash = key.hashCode();
        
        for ( ReusableString e = _table[indexFor(hash, _table.length)] ; e != null ; e = e.getNext() ) {

            if ( e == key || key.equals(e) ) return true;
        }
        
        return false;
    }

    private void resize( int newCapacity ) {
        ReusableString[] oldTable = _table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            _threshold = Integer.MAX_VALUE;
            return;
        }

        ReusableString[] newTable = new ReusableString[ newCapacity ];
        transfer( newTable );
        _table = newTable;
        _threshold = (int)(newCapacity * _loadFactor);
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    private void transfer( ReusableString[] newTable ) {
        ReusableString[] oldTable = _table;
        int oldCapacity = oldTable.length;
        if (oldCapacity >= MAXIMUM_CAPACITY)
            return;

        ReusableString tmp;
        
        _threshold = (int)(newTable.length * _loadFactor);
        int sizeMask = newTable.length - 1;

        for ( int oldArrIdx = 0 ; oldArrIdx < oldCapacity ; oldArrIdx++ ) {
            ReusableString entry = oldTable[oldArrIdx];

            if ( entry != null ) {
                ReusableString next = entry.getNext();

                if ( next == null  ) {                                  //  Single node on list
                    int newBucketIndex = entry.hashCode() & sizeMask;
                    newTable[ newBucketIndex ] = entry;
                } else {                                          
                    ReusableString p = entry;
                    int            newBucketIndex;
                    
                    while( p != null ) {
                        newBucketIndex = p.hashCode() & sizeMask;
                        
                        tmp = p;
                        p = p.getNext();

                        tmp.setNext( newTable[newBucketIndex] );
                        
                        newTable[newBucketIndex] = tmp;
                    }
                }
            }
        }
    }
    
    /**
     * Returns index for hash code h.
     */
    private static int indexFor( int h, int length ) {
        return h & (length-1); // as table is power of 2
    }
    
    /**
     * recycles all the entries in the map
     */
    public void clear() {
        _size = 0;
        
        ReusableStringRecycler recycler = TLC.instance().getReusableStringRecycleFactory();
        ReusableString         tmp;
        
        for( int idx=0 ; idx < _table.length ; ++idx ) {
 
            ReusableString e = _table[idx];
            
            while ( e != null ) {

                tmp = e;
                
                e = e.getNext();
                
                recycler.recycle( tmp );
            }
            
            _table[idx] = null;
        }
    }

    public int size() {
        return _size;
    }
}
