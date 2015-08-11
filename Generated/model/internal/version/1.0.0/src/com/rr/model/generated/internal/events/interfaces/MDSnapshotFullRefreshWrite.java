/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.core.model.SecurityIDSource;
import com.rr.model.generated.internal.events.interfaces.MDSnapEntry;

public interface MDSnapshotFullRefreshWrite extends BaseMDResponse, MDSnapshotFullRefresh {

   // Getters and Setters
    public void setSendingTime( long val );

    public void setReceived( long val );

    public void setLastMsgSeqNumProcessed( int val );

    public void setTotNumReports( int val );

    public void setRptSeq( int val );

    public void setMdBookType( int val );

    public void setSecurityIDSource( SecurityIDSource val );

    public void setSecurityID( long val );

    public void setMdSecurityTradingStatus( int val );

    public void setNoMDEntries( int val );

    public void setMDEntries( MDSnapEntry val );

    public void setMsgSeqNum( int val );

    public void setPossDupFlag( boolean val );

}
