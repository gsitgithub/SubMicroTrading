/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order.collections;

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ViewString;
import com.rr.om.order.Order;

public final class HashEntry implements Reusable<HashEntry> {
    Order                              _value;
    ViewString                         _key;
    HashEntry                          _next;
    int                                _hash;

    void set( ViewString key, int hash, HashEntry next, Order value ) {
        _key   = key;
        _hash  = hash;
        _next  = next;
        _value = value;
    }

    @Override
    public void reset() {
        _key = null;
        _hash = 0;
        _value = null;
        _next = null;
    }

    @Override
    public HashEntry getNext() {
        return _next;
    }

    @Override
    public void setNext( HashEntry nxt ) {
        _next = nxt;
    }

    @Override
    public ReusableType getReusableType() {
        return OrderCollectionTypes.OrderMapHashEntry;
    }

}
