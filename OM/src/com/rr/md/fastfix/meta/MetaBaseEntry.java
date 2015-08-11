/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix.meta;

import com.rr.core.codec.binary.fastfix.common.FieldDataType;

public class MetaBaseEntry {
    private final String _name;
    private final int    _id;
    private final FieldDataType _type;
    private final boolean       _isOptional;
    
    public MetaBaseEntry( String name, int id, boolean optional, FieldDataType t ) {
        _name = name;
        _id = id;
        _type = t;
        _isOptional = optional;
    }
    
    @Override
    public String toString() {
        return "name=" + getName() + ", id=" + getId() + ", isOptional=" + isOptional() + ", type=" + getType();
        
    }

    public String getName() {
        return _name;
    }

    public int getId() {
        return _id;
    }

    public FieldDataType getType() {
        return _type;
    }
    
    public boolean isOptional() {
        return _isOptional;
    }

    public boolean requiresPresenceBit() {
        return false;
    }
}
