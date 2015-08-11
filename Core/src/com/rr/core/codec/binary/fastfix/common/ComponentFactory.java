/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.common;

import java.util.Collection;

import com.rr.core.codec.binary.fastfix.fulldict.entry.DictEntry;

public interface ComponentFactory {

    public <T extends FieldWriter, V> T getWriter( Class<T> fieldClass, Object ... args );

    public <T extends FieldReader, V> T getReader( Class<T> fieldClass, Object ... args );

    public Collection<DictEntry> getDictEntries();

    /**
     * return a dictionary type appropriate holder for value (eg used to store previous value)
     */
    public DictEntry getPrevFieldValWrapper( String name, FieldDataType type, String initVal );

    public DictEntry getPrevFieldValInt32Wrapper( String name, int initVal );

    public DictEntry getPrevFieldValInt64Wrapper( String name, long initVal );
}
