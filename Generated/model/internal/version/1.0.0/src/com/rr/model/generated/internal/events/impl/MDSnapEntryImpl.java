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
import com.rr.model.generated.internal.type.TickDirection;
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

public final class MDSnapEntryImpl implements MDSnapEntry, Reusable<MDSnapEntryImpl> {

   // Attrs

    private          MDSnapEntryImpl _next = null;
    private int _mdPriceLevel = Constants.UNSET_INT;
    private double _mdEntryPx = Constants.UNSET_DOUBLE;
    private int _mdEntrySize = Constants.UNSET_INT;
    private int _mdEntryTime = Constants.UNSET_INT;
    private int _tradeVolume = Constants.UNSET_INT;

    private MDEntryType _mdEntryType;
    private TickDirection _tickDirection;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getMdPriceLevel() { return _mdPriceLevel; }
    @Override public final void setMdPriceLevel( int val ) { _mdPriceLevel = val; }

    @Override public final MDEntryType getMdEntryType() { return _mdEntryType; }
    @Override public final void setMdEntryType( MDEntryType val ) { _mdEntryType = val; }

    @Override public final double getMdEntryPx() { return _mdEntryPx; }
    @Override public final void setMdEntryPx( double val ) { _mdEntryPx = val; }

    @Override public final int getMdEntrySize() { return _mdEntrySize; }
    @Override public final void setMdEntrySize( int val ) { _mdEntrySize = val; }

    @Override public final int getMdEntryTime() { return _mdEntryTime; }
    @Override public final void setMdEntryTime( int val ) { _mdEntryTime = val; }

    @Override public final TickDirection getTickDirection() { return _tickDirection; }
    @Override public final void setTickDirection( TickDirection val ) { _tickDirection = val; }

    @Override public final int getTradeVolume() { return _tradeVolume; }
    @Override public final void setTradeVolume( int val ) { _tradeVolume = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _mdPriceLevel = Constants.UNSET_INT;
        _mdEntryType = null;
        _mdEntryPx = Constants.UNSET_DOUBLE;
        _mdEntrySize = Constants.UNSET_INT;
        _mdEntryTime = Constants.UNSET_INT;
        _tickDirection = null;
        _tradeVolume = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.MDSnapEntry;
    }

    @Override
    public final MDSnapEntryImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( MDSnapEntryImpl nxt ) {
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
        out.append( "MDSnapEntryImpl" ).append( ' ' );
        out.append( ", mdPriceLevel=" ).append( getMdPriceLevel() );
        out.append( ", mdEntryType=" ).append( getMdEntryType() );
        out.append( ", mdEntryPx=" ).append( getMdEntryPx() );
        out.append( ", mdEntrySize=" ).append( getMdEntrySize() );
        out.append( ", mdEntryTime=" ).append( getMdEntryTime() );
        out.append( ", tickDirection=" ).append( getTickDirection() );
        out.append( ", tradeVolume=" ).append( getTradeVolume() );
    }

}
