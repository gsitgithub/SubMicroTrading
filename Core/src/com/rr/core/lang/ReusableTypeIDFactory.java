/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * represents a reusable type of object with a unique code identifier
 * 
 * enum not used as its not extensible
 *
 * @author Richard Rose
 */
public class ReusableTypeIDFactory {

    private static Map<String, AtomicInteger> _ids = new HashMap<String, AtomicInteger>();

    private static Map<String, Set<Integer>>  _catIdToSet = new HashMap<String, Set<Integer>>();
    private static Set<Integer>               _allIds     = new LinkedHashSet<Integer>();

    private static int _maxId = 0;
    
    
    public static int nextId( ReusableCategory cat ) {

        int id = 0;
        
        synchronized( _ids ) {
            String catId = cat.toString();
            
            AtomicInteger baseId = _ids.get( catId );

            if ( baseId == null ){
                baseId = new AtomicInteger( cat.getBaseId() );
                
                _ids.put( catId, baseId );
            }

            id = baseId.incrementAndGet();
            
            setID( cat, id );
        }
        
        return id;
    }


    public static int maxId() {
        return _maxId;
    }


    public static int setID( ReusableCategory cat, int id ) {
        synchronized( _ids ) {
            String catId = cat.toString();

            Integer idInt = new Integer(id);
            
            if ( _allIds.contains( idInt ) ) {
                throw new RuntimeException( "ReusableTypeID duplicate/overlapping ID, category=" + catId + ", id=" + id );
            }
            
            Set<Integer> catIds = _catIdToSet.get( catId );
            
            if ( catIds == null ) {
                catIds = new LinkedHashSet<Integer>();
                
                _catIdToSet.put( catId, catIds );
            }
            
            if ( catIds.contains( idInt ) ) {
                throw new RuntimeException( "ReusableTypeID duplicate ID, category=" + catId + ", id=" + id );
            }
            
            catIds.add( idInt );
            
            if ( id > _maxId ) {
                _maxId = id;
            }
        }
        
        return id;
    }
}
