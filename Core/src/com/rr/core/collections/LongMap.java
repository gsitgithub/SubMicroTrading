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

public interface LongMap<T> {

    public interface Cleaner<T> {
        public void clean( T valToClean );
    }
    
    public T get( long key );

    public T put( long key, T value );

    public boolean putIfKeyAbsent( long key, T value );

    public boolean containsKey( long key );

    public int size();

    public boolean isEmpty();

    public boolean containsValue( T value );

    public T remove( long key );

    public void registerCleaner( Cleaner<T> cleaner );
    
    public void clear();

    public void logStats( ReusableString out );
}
