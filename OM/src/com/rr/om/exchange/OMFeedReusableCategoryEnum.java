/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.exchange;

import com.rr.core.lang.ReusableCategory;

/**
 * defines the starting id and range of category ids
 * @NOTE the event ids should start from 1 as they are 
 * key to performance in switch statements
 */
public enum OMFeedReusableCategoryEnum implements ReusableCategory {
    
    // @NOTE keep UNDER 32,768
    
    UTP(         8192,  128 );
    
    private final int _baseId;
    private final int _size;

    OMFeedReusableCategoryEnum( int baseId, int size ) {
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
