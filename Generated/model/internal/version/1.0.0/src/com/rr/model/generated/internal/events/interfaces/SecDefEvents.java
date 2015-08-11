/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.interfaces;

import com.rr.core.lang.ReusableString;
import com.rr.model.internal.type.SubEvent;

public interface SecDefEvents extends SubEvent {

   // Getters and Setters
    public int getEventType();

    public long getEventDate();

    public long getEventTime();

    @Override
    public void dump( ReusableString out );

    public void setEventType( int val );

    public void setEventDate( long val );

    public void setEventTime( long val );

}
