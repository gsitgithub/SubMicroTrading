/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.model.Instrument;


/**
 * non threadsafe cache of books
 * 
 * intended for use via TLC eg
 * 
 * BookCache<ApiMutatableBook> bookCache = TLC.instance().getInstanceOf( BookCache.class );
 */
public class UnsafeBookCache {

    private static class CacheKey {
        private Instrument _inst;
        private int        _numLevels;

        public CacheKey() {
            // nothing
        }

        public CacheKey( CacheKey that ) {
            _inst       = that._inst;
            _numLevels  = that._numLevels;
        }

        @Override
        public int hashCode() {
            return _inst.hashCode();
        }
        
        @Override
        public boolean equals( Object obj ) {
            if ( this == obj )                      return true;
            if ( obj == null )                      return false;
            if ( getClass() != obj.getClass() )     return false;
            CacheKey other = (CacheKey) obj;
            if ( _inst != other._inst )             return false;
            if ( _numLevels != other._numLevels )   return false;
            
            return true;
        }

        public void set( Instrument inst, int levels ) {
            _inst      = inst;
            _numLevels = levels;
        }
    }
    
    private Map<CacheKey, ApiMutatableBook> _books = new HashMap<CacheKey, ApiMutatableBook>( 128 );
   
    private CacheKey _tmpKey = new CacheKey();
    
    public ApiMutatableBook get( Instrument inst, int levels ) {
        _tmpKey.set( inst, levels );
        
        ApiMutatableBook book = _books.get( _tmpKey );
        
        if ( book == null ) {
            book = create( inst, levels );
            
            _books.put( new CacheKey(_tmpKey), book );
        }

        return book;
    }

    private ApiMutatableBook create( Instrument inst, int levels ) {
        return( (levels == 1) ? new UnsafeL1Book( inst ) :  new UnsafeL2Book( inst, levels ) );
    }
}
