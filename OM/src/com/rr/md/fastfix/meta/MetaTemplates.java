/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix.meta;

import java.util.Iterator;

import com.rr.core.collections.IntHashMap;
import com.rr.core.collections.IntMap;

public class MetaTemplates {

    private IntMap<MetaTemplate> _templates = new IntHashMap<MetaTemplate>( 128, 0.75f );

    public void add( MetaTemplate template ) {
        _templates.put( template.getId(), template );
    }

    public int size() {
        return _templates.size();
    }
    
    public MetaTemplate getTemplate( int id ) {
        return _templates.get( id );
    }

    public Iterator<Integer> templateIterator() {
        return _templates.keys().iterator();
    }
}
