/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import java.util.Collection;

import com.rr.core.lang.ReusableString;

public interface SMTMap<K,T> {

    public T get( K key );

    public T put( K key, T value );

    public boolean containsKey( K key );

    public int size();

    public boolean isEmpty();

    public boolean containsValue( T value );

    public T remove( K key );

    public void clear();

    public void logStats( ReusableString out );

    /**
     * @return set of keys used in the map .. collection and Integer keys created per call
     */
    public Collection<K> keys();
}
