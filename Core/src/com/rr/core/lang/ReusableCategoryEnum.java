/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

/**
 * defines the starting id and range of category ids
 * @NOTE the event ids should start from 1 as they are 
 * key to performance in switch statements
 */
public enum ReusableCategoryEnum implements ReusableCategory {
    
    // @NOTE keep UNDER 32,768
    
    Event(          1, 1023 ), 
    Collection(  1024,  128 ),
    OM(          1280,  128 ),
    Core(        1536,  128 ),
    MDS(         1792,  128 ),
    Strats(      2048,  128 ),
    OMFeedType(  8192,  2048);
    
    private final int _baseId;
    private final int _size;

    ReusableCategoryEnum( int baseId, int size ) {
        _baseId = baseId;
        _size   = size;
    }

    @Override
    public int getBaseId() {
        return _baseId;
    }

    @Override
    public int getSize() {
        return _size;
    }
}
