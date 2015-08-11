/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.codec.emea.exchange.eti;

import com.rr.core.collections.IntToLongHashMap;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;

public final class ETIDecodeContext {

    // context required for exec report seq num uniqueness
    
    private final ReusableString _lastApplMsgID = new ReusableString(16);
    private int _lastPartitionID;
    private final ReusableString _srcLinkId = new ReusableString();
    
    private final IntToLongHashMap _seqNumToMktClOrdId;

    public ETIDecodeContext() {
        this( 1000 );
    }

    public ETIDecodeContext( int expectedRequests ) {
        _seqNumToMktClOrdId = new IntToLongHashMap( expectedRequests, 0.75f );
    }

    public ZString getLastApplMsgID() {
        return _lastApplMsgID;
    }
    
    public int getLastPartitionID() {
        return _lastPartitionID;
    }

    public void setLastApplMsgID( ZString lastApplMsgID ) {
        _lastApplMsgID.copy( lastApplMsgID );
    }

    public void setLastPartitionID( int lastPartitionID ) {
        _lastPartitionID = lastPartitionID;
    }

    public void reset() {
        _lastApplMsgID.reset();
        _lastPartitionID = 0;
    }

    public boolean hasValue() {
        return _lastApplMsgID.length() > 0;
    }
    
    public ZString getSrcLinkId() {
        return _srcLinkId;
    }
    
    public void setSrcLinkId( ZString val ) {
        _srcLinkId.copy( val );
    }

    public ReusableString getSrcLinkIdForUpdate() {
        return _srcLinkId;
    }
    
    public IntToLongHashMap getMapSeqNumClOrdId() {
        return _seqNumToMktClOrdId;
    }
}
