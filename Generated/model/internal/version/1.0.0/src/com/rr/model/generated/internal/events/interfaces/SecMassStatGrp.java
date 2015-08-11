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
import com.rr.model.generated.internal.type.SecurityTradingStatus;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
import com.rr.model.internal.type.SubEvent;

public interface SecMassStatGrp extends SubEvent {

   // Getters and Setters
    public ViewString getSecurityId();

    public SecurityIDSource getSecurityIDSource();

    public SecurityTradingStatus getSecurityTradingStatus();

    public boolean getSecurityStatus();

    @Override
    public void dump( ReusableString out );

    public void setSecurityId( byte[] buf, int offset, int len );
    public ReusableString getSecurityIdForUpdate();

    public void setSecurityIDSource( SecurityIDSource val );

    public void setSecurityTradingStatus( SecurityTradingStatus val );

    public void setSecurityStatus( boolean val );

}
