/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order.collections;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.om.order.Order;
import com.rr.om.processor.EventProcessor;

/**
 * a zero GC version of Sun HashMap optimised for use with OrderImpl
 * 
 * need a putIfAbsent which returns false if put failed due to existing entry, this allows keys with null values
 * which is required for recovery/orderEviction of old orders to avoid future dups
 * 
 * @NOTE assumes the order clOrdId never changes ... NEVER recycle order without first removing from map
 * 
 * @NOTE NOT THREADSAFE DESIGNED FOR USE ONLY BY THE ORDER PROCESSOR IN SINGLE THREAD MODE
 */
public interface OrderMap {

    public int size();
    
    public boolean isEmpty();
    
    public Order get( ViewString key );
    
    public boolean containsKey( ViewString key );

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     * 
     * @NOTE the map does NOT own the key, thats owned by the order key chains
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, IF not same object
     */
    public Order put( ViewString id, Order order );
    
    /**
     * Associates the specified value with the specified key in this map only if the key has no entry in map
     * If the key has an value of null it will return false and the put will not take place
     * 
     * @NOTE the map does NOT own the key, thats owned by the order key chains
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return true if the put was successful (ie key didnt already exist in map)
     */
    public boolean putIfKeyAbsent( ViewString id, Order order );
    
    /**
     * rather than own its own recycle factories the order map
     * will delegate to processor
     * 
     * @param proc
     */
    public void setRecycleProcessor( EventProcessor proc );

    /**
     * clear  the map, recycling orders/versions/bases using processor
     */
    public void clear();
    
    /**
     * log stats for the map 
     * 
     * @NOTE LONG OPERATION, intended for end of day logging
     */
    public void logStats( ReusableString out );
}
