/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.state;


public interface SequenceNumGapMgr {

    /**
     * @return next range to request or null if none left
     */
    public SequenceGapRange next();

    public boolean anyPendingSequenceGaps();

    /**
     * add gap fill request 
     * @param from - from seqNum
     * @param to   - upto and including seqNum, 0 = all
     * @return true if new sequence added 
     */
    public boolean add( int from, int to );

    public void clear();

    /**
     * set current gap being processed
     * 
     * @param gap
     */
    public void gapFillRequested( SequenceGapRange gap );

    public boolean received( int seq );

    public int pending();

    /**
     * @return number of enqueued gap requests ... excludes active gap request
     */
    public int queuedGaps();

    /**
     * @return true if gapFill currently set
     */
    public boolean inGapRequest();

    /**
     * if seqNum is in current gap request and denotes a gap within a gap (ie a subgap)
     * 
     * then create a subgap and attach to current gap
     * 
     * @param seqNum
     * @return
     */
    public SequenceGapRange checkSubGap( int seqNum );

    /**
     * @return next expected seqNum in gap, or -1 if not in gap or last seqNum processed (could be open child gaps still)
     */
    public int nextExpectedSeqNum();

}