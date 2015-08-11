/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf.generics.maps;

/**
 * not real map implementation, just looking at performance 
 * of map type structure using generics
 *
 * @author Richard Rose
 * @param <K>
 * @param <V>
 */
public class DummyMapWithGenerics<K,V> {
    
    public static class MapEntry<K,V> {
        K key;
        V value;
        
        MapEntry() {
            // nada
        }
        
        K getKey()   { return key; }
        V getValue() { return value; }
        
        void setValue( V aVal ) { value = aVal; }
    }
    
    protected MapEntry<K,V>[] _array;

    @SuppressWarnings( "unchecked" )
    public DummyMapWithGenerics( int size ) {
        _array = new MapEntry[ size ];
        
        for( int i=0; i <  size ;i++ ){
            _array[i] = new MapEntry<K,V>();
        }
    }
    
    public K getKey( int idx ) {
        return _array[idx].key;
    }

    public V getValue( int idx ) {
        return _array[idx].value;
    }

    public void set( K key, V value, int idx ) {
        MapEntry<K,V> entry = _array[idx];
        entry.key   = key;
        entry.value = value;
    }
}
