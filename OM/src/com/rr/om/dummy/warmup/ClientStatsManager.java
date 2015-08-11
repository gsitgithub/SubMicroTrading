/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.dummy.warmup;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.utils.Percentiles;

public class ClientStatsManager {

    static class StatsEntry {
        long sent    = 0;
        long replied = 0;
    }
    
    private final Map<ViewString, StatsEntry> _statsMap;
    private final StatsEntry[]                _statsEmptyEntries;
    private       int                         _emptyIdx=0;
    private       int                         _numExpOrders;
    
    public ClientStatsManager( int numOrders ) {
        _numExpOrders = numOrders;
        _statsMap = new ConcurrentHashMap<ViewString,StatsEntry>( numOrders );
        _statsEmptyEntries = new StatsEntry[ numOrders ];
        
        for ( int i=0 ; i < numOrders ; i++ ) {
            _statsEmptyEntries[i] = new StatsEntry();
        }
    }

    public void register( ReusableString key ) {
        getStatsEntry( key );
    }

    /**
     * @param key a key which this stats manager will own
     * @param when
     */
    public synchronized void sent( ViewString key, long when ) {
        StatsEntry entry = getStatsEntry( key );
        
        entry.sent = when;
    }

    private synchronized StatsEntry getStatsEntry( ViewString key ) {
        StatsEntry entry = _statsMap.get( key );
        
        if ( entry == null ) {
            if ( _emptyIdx < _numExpOrders ) {
                entry = _statsEmptyEntries[ _emptyIdx++ ];
            } else {
                entry = new StatsEntry();
            }
            _statsMap.put( key, entry );
        }
        
        return entry;
    }

    /**
     * @param key
     * @param when
     * @return      true if the clOrdId has entry in the map
     */
    public synchronized boolean replyRecieved( ZString key, long when ) {
        StatsEntry entry = _statsMap.get( key );
        
        if ( entry != null ) {
            entry.replied = when;
        }
        
        return entry != null;
    }
    
    public void logStats() {
        final int size = _statsMap.size();
        final int[] timeUSEC = new int[ size ];
        
        final Set<Map.Entry<ViewString,StatsEntry>> entrySet = _statsMap.entrySet();

        int count=0;
        
        for ( Map.Entry<ViewString,StatsEntry> entry : entrySet ) {

            StatsEntry stat = entry.getValue();
            
            if ( stat.sent != 0 && stat.replied != 0 ) {

                timeUSEC[count] = (int) (Math.abs( stat.replied - stat.sent ) / 1000);
                
                ++count;
            }
        }
        
        Percentiles p = new Percentiles( timeUSEC );
        
        System.out.println( "MicroSecond stats "      + " size=" + size +
                            ", count=" + count + 
                            ", med=" + p.median()     + ", ave=" + p.getAverage() + 
                            ", min=" + p.getMinimum() + ", max=" + p.getMaximum() + 
                            "\n"                      +
                            ", p99=" + p.calc( 99 )   + ", p95=" + p.calc( 95 )   + 
                            ", p90=" + p.calc( 90 )   + ", p80=" + p.calc( 80 )   + 
                            ", p70=" + p.calc( 70 )   + ", p50=" + p.calc( 50 )   + "\n" );
    }

    public void reset() {
        _statsMap.clear();
    }
}
