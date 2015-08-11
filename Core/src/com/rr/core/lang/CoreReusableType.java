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
 * represents a reusable type of object with a unique code identifier
 * 
 * enum not used as its not extensible
 *
 * @author Richard Rose
 */
public enum CoreReusableType implements ReusableType {

    ReusableString( ReusableCategoryEnum.Core ), 
    LogEventSmall( ReusableCategoryEnum.Core ), 
    LogEventLarge( ReusableCategoryEnum.Core ), 
    LogEventHuge( ReusableCategoryEnum.Core ), 
    RejectThrowable( ReusableCategoryEnum.Core ), 
    RejectIndexOutOfBounds( ReusableCategoryEnum.Core ), 
    RejectDecodeException( ReusableCategoryEnum.Core ), 
    HashMapEntry( ReusableCategoryEnum.Core ), 
    PackedMessage( ReusableCategoryEnum.Core ),
    NullMessage( ReusableCategoryEnum.Core ),
    LongMapHashEntry( ReusableCategoryEnum.Collection );
    
    private final int              _id;
    private final ReusableCategory _cat;

    private CoreReusableType( ReusableCategory cat ) {
        _cat  = cat;
        _id   = ReusableTypeIDFactory.nextId( cat );
    }

    @Override
    public int getId() {
        return _id;
    }

    @Override
    public ReusableCategory getReusableCategory() {
        return _cat;
    }

    @Override
    public int getSubId() {
        return 0;
    }
}
