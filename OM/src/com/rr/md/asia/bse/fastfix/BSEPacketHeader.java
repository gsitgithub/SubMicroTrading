/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.asia.bse.fastfix;

/**
 * structure representing BSE packet header
 */

// @TODO move to model.xml

public class BSEPacketHeader {

    public int  _partitionId;
    public int  _senderCompId;
    public int  _packetSeqNum; // contiguous per senderCompId PER PORT !
    public long _sendingTime;
    public long _perfIndicator;
    
    @Override
    public String toString() {
        return "BSEPacketHeader [_partitionId=" + _partitionId + ", _senderCompId=" + _senderCompId + ", _packetSeqNum=" + _packetSeqNum + ", _sendingTime="
               + _sendingTime + ", _perfIndicator=" + _perfIndicator + "]";
    }
}
