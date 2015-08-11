/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.Side;
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

public final class TickUpdateImpl implements TickUpdate, Reusable<TickUpdateImpl> {

   // Attrs

    private          TickUpdateImpl _next = null;
    private double _mdEntryPx = Constants.UNSET_DOUBLE;
    private int _mdEntrySize = Constants.UNSET_INT;
    private long _tradeTime = Constants.UNSET_LONG;
    private int _numberOfOrders = Constants.UNSET_INT;

    private MDEntryType _mdEntryType;
    private Side _tickDirection;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final MDEntryType getMdEntryType() { return _mdEntryType; }
    @Override public final void setMdEntryType( MDEntryType val ) { _mdEntryType = val; }

    @Override public final double getMdEntryPx() { return _mdEntryPx; }
    @Override public final void setMdEntryPx( double val ) { _mdEntryPx = val; }

    @Override public final int getMdEntrySize() { return _mdEntrySize; }
    @Override public final void setMdEntrySize( int val ) { _mdEntrySize = val; }

    @Override public final long getTradeTime() { return _tradeTime; }
    @Override public final void setTradeTime( long val ) { _tradeTime = val; }

    @Override public final Side getTickDirection() { return _tickDirection; }
    @Override public final void setTickDirection( Side val ) { _tickDirection = val; }

    @Override public final int getNumberOfOrders() { return _numberOfOrders; }
    @Override public final void setNumberOfOrders( int val ) { _numberOfOrders = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _mdEntryType = null;
        _mdEntryPx = Constants.UNSET_DOUBLE;
        _mdEntrySize = Constants.UNSET_INT;
        _tradeTime = Constants.UNSET_LONG;
        _tickDirection = null;
        _numberOfOrders = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.TickUpdate;
    }

    @Override
    public final TickUpdateImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( TickUpdateImpl nxt ) {
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
        out.append( "TickUpdateImpl" ).append( ' ' );
        out.append( ", mdEntryType=" ).append( getMdEntryType() );
        out.append( ", mdEntryPx=" ).append( getMdEntryPx() );
        out.append( ", mdEntrySize=" ).append( getMdEntrySize() );
        out.append( ", tradeTime=" ).append( getTradeTime() );
        out.append( ", tickDirection=" ).append( getTickDirection() );
        out.append( ", numberOfOrders=" ).append( getNumberOfOrders() );
    }

}
