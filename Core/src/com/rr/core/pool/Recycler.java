/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.pool;

import com.rr.core.lang.Reusable;

public interface Recycler<T extends Reusable<T>> {

    /**
     * recycle object
     * 
     * @NOTE next pointer must be cleared before invoking recycle or object will NOT be recycled
     * @param obj
     */
    public void recycle( T obj );
}
