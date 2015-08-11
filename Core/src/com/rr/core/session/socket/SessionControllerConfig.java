/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.socket;


public interface SessionControllerConfig {

    public boolean      isDisconnectOnMissedHB();
    public int          getHeartBeatIntSecs();
    public void         setHeartBeatIntSecs( int heartBeatIntSecs );
    public boolean      isGapFillAllowed();
    public void         setGapFillAllowed( boolean canGapFill );
    
    /**
     * if other side send an nextSeqNum less than expected in log on message then can optionally truncate down automatically
     * 
     * THIS IS NOT USUALLY ADVISABLE WITH EXCHANGE BUT MAYBE SO FOR CLIENTS
     * 
     * @return true if should truncate down expected seq num from other side
     */
    public boolean      isRecoverFromLoginSeqNumTooLow();
    public void         setRecoverFromLoginSeqNumTooLow( boolean isRecoverFromLoginSeqNumTooLow );
    public boolean      isServer();
    
    /**
     * @return maximum sequence number in resync, eg in fix 0/999999 means upto last sent msg depending on FIX version
     */
    public int          getMaxSeqNum();
}
