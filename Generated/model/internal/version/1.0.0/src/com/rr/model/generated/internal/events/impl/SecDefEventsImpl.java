/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.Constants;
import com.rr.core.model.MsgFlag;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.Reusable;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.model.internal.type.*;
import com.rr.model.generated.internal.core.ModelReusableTypes;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.*;

@SuppressWarnings( "unused" )

public final class SecDefEventsImpl implements SecDefEvents, Reusable<SecDefEventsImpl> {

   // Attrs

    private          SecDefEventsImpl _next = null;
    private int _eventType = Constants.UNSET_INT;
    private long _eventDate = Constants.UNSET_LONG;
    private long _eventTime = Constants.UNSET_LONG;


    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getEventType() { return _eventType; }
    @Override public final void setEventType( int val ) { _eventType = val; }

    @Override public final long getEventDate() { return _eventDate; }
    @Override public final void setEventDate( long val ) { _eventDate = val; }

    @Override public final long getEventTime() { return _eventTime; }
    @Override public final void setEventTime( long val ) { _eventTime = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _eventType = Constants.UNSET_INT;
        _eventDate = Constants.UNSET_LONG;
        _eventTime = Constants.UNSET_LONG;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.SecDefEvents;
    }

    @Override
    public final SecDefEventsImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( SecDefEventsImpl nxt ) {
        _next = nxt;
    }


   // Helper methods
    @Override
    public String toString() {
        ReusableString buf = new ReusableString();
        dump( buf );
        return buf.toString();
    }

    @Override
    public final void dump( ReusableString out ) {
        out.append( "SecDefEventsImpl" ).append( ' ' );
        out.append( ", eventType=" ).append( getEventType() );
        out.append( ", eventDate=" ).append( getEventDate() );
        out.append( ", eventTime=" ).append( getEventTime() );
    }

}
