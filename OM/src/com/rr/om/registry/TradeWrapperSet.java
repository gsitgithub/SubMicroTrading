/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.registry;

import com.rr.core.lang.ZString;
import com.rr.om.order.Order;


/**
 * specialised set just for TradeWrapper, key and identify is from execId in the wrapper
 * 
 * @NOTE ONLY USE FOR EXCHANGES THAT SEND TRADE CANCELS OR CORRECTS
 * 
 * entries put into the set are OWNED by the set and the NEXT pointer MAY be changed by the set
 * as it uses this for the hash clash chains
 * 
 * does NOT use supplemental hash so intended for use on fairly unique keys
 * 
 * doesnt need create ANY temporary objects, resize only creates an array
 * 
 * @TODO very large sets (>32000 would benefit from splitting set into segments ... tho that needs checking for heap impact)
 */
public final class TradeWrapperSet {

    private static final int MAXIMUM_CAPACITY = 1 << 30;
    
    private final float            _loadFactor;
    private       int              _threshold;
    private       TradeWrapper[]   _table;

    private       int              _size = 0;
    
    private       TradeWrapperRecycler _recycler = new TradeWrapperRecycler();
    
    public TradeWrapperSet( int initialCapacity, float loadFactor ) {
        if ( initialCapacity < 0 )                        throw new IllegalArgumentException( "Illegal initial capacity: " + initialCapacity );
        if ( initialCapacity > MAXIMUM_CAPACITY )         initialCapacity = MAXIMUM_CAPACITY;
        if ( loadFactor <= 0 || Float.isNaN(loadFactor) ) throw new IllegalArgumentException( "Illegal load factor: " + loadFactor );

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;

        _loadFactor = loadFactor;
        _threshold  = (int)(capacity * loadFactor);
        _table      = new TradeWrapper[ capacity ];
    }
    
    public TradeWrapperSet( int presize ) {
        this( presize, 0.75f );
    }

    /**
     * @param _key
     * 
     * @NOTE if object is a duplicate it will be recycled immediately
     * 
     * @return true if the key was successfully entered in the set
     */
    public final boolean put( final TradeWrapper wrapper ) {
        
        final ZString execId = wrapper.getExecId();
        if ( execId == null ) return false;
        
        final int hash        = wrapper.hashCode();
        final int bucketIndex = indexFor( hash, _table.length );
        
        final TradeWrapper bucketHead = _table[ bucketIndex ];
        
        for ( TradeWrapper entry = bucketHead ; entry != null; entry = entry.getNextWrapper() ) {
            if ( entry == wrapper || wrapper.equals(entry) ) {
                
                _recycler.recycle( wrapper ); // RECYCLE !!
                
                return false;   // already in set DONT add
            }
        }

        wrapper.setNextWrapper( bucketHead );
        
        _table[ bucketIndex ] = wrapper;
        if ( _size++ >= _threshold )
            resize( _table.length << 1 );
        
        return true;
    }

    public final boolean contains( final Order order, final ZString execId ) {

        if ( execId == null ) return false;
        final int hash = TradeWrapperImpl.hashCode( order, execId );
        
        for ( TradeWrapper e = _table[indexFor(hash, _table.length)] ; e != null ; e = e.getNextWrapper() ) {

            if ( execId.equals(e.getExecId()) && e.getOrder() == order ) return true;
        }
        
        return false;
    }

    public final TradeWrapper get( final Order order, final ZString execId ) {

        if ( execId == null ) return null;
        final int hash = TradeWrapperImpl.hashCode( order, execId );
        
        for ( TradeWrapper e = _table[indexFor(hash, _table.length)] ; e != null ; e = e.getNextWrapper() ) {

            if ( execId.equals(e.getExecId()) && e.getOrder() == order ) return e;
        }
        
        return null;
    }

    private void resize( int newCapacity ) {
        TradeWrapper[] oldTable = _table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            _threshold = Integer.MAX_VALUE;
            return;
        }

        TradeWrapper[] newTable = new TradeWrapper[ newCapacity ];
        transfer( newTable );
        _table = newTable;
        _threshold = (int)(newCapacity * _loadFactor);
    }

    /**
     * Transfers all entries from current table to newTable.
     */
    private void transfer( TradeWrapper[] newTable ) {
        TradeWrapper[] oldTable = _table;
        int oldCapacity = oldTable.length;

        TradeWrapper tmp;
        
        int sizeMask = newTable.length - 1;

        for ( int oldArrIdx = 0 ; oldArrIdx < oldCapacity ; oldArrIdx++ ) {
            TradeWrapper entry = oldTable[oldArrIdx];

            if ( entry != null ) {
                TradeWrapper next = entry.getNextWrapper();

                if ( next == null  ) {                                  //  Single node on list
                    int newBucketIndex = entry.hashCode() & sizeMask;
                    newTable[ newBucketIndex ] = entry;
                } else {                                          
                    TradeWrapper p = entry;
                    int          newBucketIndex;
                    
                    while( p != null ) {
                        newBucketIndex = p.hashCode() & sizeMask;
                        
                        tmp = p;
                        p = p.getNextWrapper();

                        tmp.setNextWrapper( newTable[newBucketIndex] );
                        
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
        
        TradeWrapper         tmp;
        
        for( int idx=0 ; idx < _table.length ; ++idx ) {
 
            TradeWrapper e = _table[idx];
            
            while ( e != null ) {

                tmp = e;
                
                e = e.getNextWrapper();
                
                _recycler.recycle( tmp );
            }
            
            _table[idx] = null;
        }
    }

    public final int size() {
        return _size;
    }
}
