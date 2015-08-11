/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.model.generated.internal.events.interfaces.StratInstrumentState;
import com.rr.core.lang.ReusableString;

public interface StrategyStateWrite extends SessionHeader, StrategyState {

   // Getters and Setters
    public void setAlgoId( byte[] buf, int offset, int len );
    public ReusableString getAlgoIdForUpdate();

    public void setTimestamp( int val );

    public void setAlgoEventSeqNum( int val );

    public void setLastTickId( long val );

    public void setPnl( double val );

    public void setLastEventInst( int val );

    public void setNoInstEntries( int val );

    public void setInstState( StratInstrumentState val );

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

    public void setSendingTime( int val );

}
