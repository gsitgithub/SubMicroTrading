/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableType;

public final class LongHashEntry<T> implements Reusable<LongHashEntry<T>> {
    
    long               _key;
    T                  _value;
    LongHashEntry<T>   _next;

    void set( long key, LongHashEntry<T> next, T value ) {
        _key   = key;
        _next  = next;
        _value = value;
    }

    @Override
    public void reset() {
        _key   = 0;
        _value = null;
        _next  = null;
    }

    @Override
    public LongHashEntry<T> getNext() {
        return _next;
    }

    @Override
    public void setNext( LongHashEntry<T> nxt ) {
        _next = nxt;
    }

    @Override
    public ReusableType getReusableType() {
        return CoreReusableType.LongMapHashEntry;
    }
}
