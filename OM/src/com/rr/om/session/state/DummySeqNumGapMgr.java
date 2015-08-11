/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.state;

public class DummySeqNumGapMgr implements SequenceNumGapMgr {

    @Override
    public SequenceGapRange next() {
        return null;
    }

    @Override
    public boolean anyPendingSequenceGaps() {
        return false;
    }

    @Override
    public boolean add( int from, int too ) {
        return false;
    }

    @Override
    public void clear() {
        // nothing
    }

    @Override
    public void gapFillRequested( SequenceGapRange gap ) {
        // nothing
    }

    @Override
    public boolean received( int seq ) {
        return false;
    }

    @Override
    public int pending() {
        return 0;
    }

    @Override
    public int queuedGaps() {
        return 0;
    }

    @Override
    public boolean inGapRequest() {
        return false;
    }

    @Override
    public SequenceGapRange checkSubGap( int seqNum ) {
        return null;
    }

    @Override
    public int nextExpectedSeqNum() {
        return -1;
    }
}
