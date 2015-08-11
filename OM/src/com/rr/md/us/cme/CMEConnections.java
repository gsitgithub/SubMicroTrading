/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rr.core.lang.ZString;

public class CMEConnections {

    private Map<ZString, CMEConnection> _connections = new HashMap<ZString, CMEConnection>();
    
    public void add( CMEConnection conn )  {
        _connections.put( conn.getId(), conn );
    }
    
    public CMEConnection get( FeedType type, Feed feed ) {
        for ( CMEConnection conn : _connections.values() ) {
            if ( conn.getFeedType() == type && feed == conn.getFeed() ) {
                return conn;
            }
        }
        
        return null;
    }

    public CMEConnection get( FeedType type ) {
        for ( CMEConnection conn : _connections.values() ) {
            if ( conn.getFeedType() == type ) {
                return conn;
            }
        }
        
        return null;
    }

    public Iterator<CMEConnection> getConnectionIterator() {
        return _connections.values().iterator();
    }
}
