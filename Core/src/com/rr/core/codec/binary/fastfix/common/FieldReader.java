/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.common;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.fastfix.msgdict.TemplateFieldReader;

public abstract class FieldReader implements TemplateFieldReader {

    private final boolean   _isOptional;
    private final String    _name;
    private final int       _id;

    public FieldReader( String name, int id, boolean isOptional ) {
        _isOptional = isOptional;
        _name = name;
        _id = id;
    }

    @Override
    public int getId() {
        return _id;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public boolean isOptional() {
        return _isOptional;
    }
    
    protected final void throwMissingValueException() {
        throw new RuntimeDecodingException( "Missing mandatory value for field " + getName() );
    }
    
    protected final void throwMissingPreviousException() {
        throw new RuntimeDecodingException( "Missing previous value for field " + getName() );
    }
}
