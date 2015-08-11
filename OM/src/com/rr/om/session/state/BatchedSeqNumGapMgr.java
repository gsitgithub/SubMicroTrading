/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.state;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;


public class BatchedSeqNumGapMgr implements SequenceNumGapMgr {
    
    private static final Logger _log = LoggerFactory.create( BatchedSeqNumGapMgr.class );

    
    private final Queue<SequenceGapRange> _ranges = new ConcurrentLinkedQueue<SequenceGapRange>();
    private       SequenceGapRange        _curGap = null;

    // write unit test for this mgr
    
    /**
     * @return next range to request or null if none left
     */
    @Override
    public SequenceGapRange next() {
        return _ranges.poll();
    }
    
    @Override
    public boolean anyPendingSequenceGaps() { 
        return( !_ranges.isEmpty() ); 
    }
    
    @Override
    public boolean add( int from, int too ) {

        if ( _curGap != null ) {
            if ( _curGap.inRange( from ) ) {
                if ( _curGap.inRange( too ) ) {
                    return false; // whole range within existing gap
                }
                
                from = _curGap._to + 1; // increase from to after end of this gap
            } else if ( _curGap.inRange( from ) ) {
                // start of requested range is not in gap, but end is
                
                too = _curGap._from - 1;
            }
        }
        
        for( SequenceGapRange  gap : _ranges ) {
            if ( gap.inRange( from ) ) {
                if ( gap.inRange( too ) ) {
                    return false; // whole range within existing gap
                }
                
                from = gap._to + 1; // increase from to after end of this gap
            } else if ( gap.inRange( from ) ) {
                // start of requested range is not in gap, but end is
                
                too = gap._from - 1;
            }
        }
        
        if ( from > too || from < 0 || too < 0 ) {
            return false;   // should never happen
        }
        
        _log.info( "Actual gap enqueued from=" + from + ", to=" + too );
        
        SequenceGapRange gap = new SequenceGapRange( from, too );
        
        _ranges.add( gap );
        
        return true;
    }
    
    @Override
    public void clear() {
        _ranges.clear();
        if ( _curGap != null ) _curGap.clear();
        _curGap = null;
    }

    @Override
    public void gapFillRequested( SequenceGapRange gap ) {
        _log.info( "Next gap fill from queue from=" + gap._from + ", to=" + gap._to + ", stillPendingFromPreviousGap=" + gap.size() );

        _curGap = gap;
    }
    
    @Override
    public boolean received( int seq ) {
        boolean removed = false;
        
        if ( _curGap != null ) {
            removed = _curGap.received( seq );
            
            if ( removed ) {
                if ( pending() == 0 ) {
                    _curGap = null;
                }
            }
        }
        
        return removed;
    }
    
    @Override
    public int pending() {
        return (_curGap == null) ? 0 : _curGap.size();
    }

    @Override
    public int queuedGaps() {
        return _ranges.size();
    }

    @Override
    public boolean inGapRequest() {
        return (_curGap != null) ? (_curGap.size() > 0) : false;
    }

    @Override
    public SequenceGapRange checkSubGap( int seqNum ) {
        if ( _curGap != null ) {
            return _curGap.checkSubGap( seqNum );
        }
        return null;
    }

    @Override
    public int nextExpectedSeqNum() {
        if ( _curGap != null ) {
            return _curGap.nextExpectedSeqNum();
        }
        return -1;
    }
}
