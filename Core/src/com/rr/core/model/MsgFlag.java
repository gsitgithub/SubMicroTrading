/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

/**
 * MessageFlags - misc flags should be relatively agnostic
 * 
 * currently message flags stored in byte, if use more than 8 flags increase size of type in events
 */

public enum MsgFlag { 
    PossDupFlag,    // if a message with this sequence number has been previously received, ignore message, if not, process normally. 
    PossResend,     // forward message to application and determine if previously received (i.e. verify order id and parameters). 
    Reconciliation;   // DUMMY message used to keep CPU warm
    
    private final int _bitMaskOn;
    private final int _bitMaskOff;
    
    private MsgFlag() {
        _bitMaskOn = 1 << ordinal();
        _bitMaskOff = ~_bitMaskOn;        
    }
    
    public static final int setFlag( int curFlags, MsgFlag flag, boolean setOn ) {
        return (setOn) ? (curFlags | flag._bitMaskOn)
                       : (curFlags & flag._bitMaskOff);
    }

    public static boolean isOn( int flags, MsgFlag flag ) {
        return (flags & flag._bitMaskOn) > 0;
    }
}

