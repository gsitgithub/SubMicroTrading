/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix.meta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rr.core.codec.binary.fastfix.common.FieldDataType;

public class MetaTemplate extends MetaBaseEntry {
    private String          _dictionaryId;
    private List<MetaBaseEntry> _entries = new ArrayList<MetaBaseEntry>();

    public MetaTemplate( String name, int id, String dictionaryId ) {
        super( name, id, false, FieldDataType.template );
        setDictionaryId( dictionaryId );
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer( "MetaTemplate : " );
        
        s.append( super.toString() );
        
        for ( int i=0 ; i < _entries.size() ; ++i ) {
            s.append( "\n    " ).append( _entries.get( i ) );
        }
        
        return s.toString();
    }
    
    public void addEntry( MetaBaseEntry entry ) {
        _entries.add( entry );
    }

    public String getDictionaryId() {
        return _dictionaryId;
    }

    public void setDictionaryId( String dictionaryId ) {
        _dictionaryId = dictionaryId;
    }

    public Iterator<MetaBaseEntry> getEntryIterator() {
        return _entries.iterator();
    }
}

