/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.core.model.Instrument;
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

public final class StratInstrumentStateImpl implements StratInstrumentState, Reusable<StratInstrumentStateImpl> {

   // Attrs

    private          StratInstrumentStateImpl _next = null;
    private long _lastTickId = Constants.UNSET_LONG;
    private int _totLongContractsExecuted = 0;
    private int _totShortContractsExecuted = 0;
    private int _totLongContractsOpen = 0;
    private int _totShortContractsOpen = 0;
    private double _totLongValueExecuted = 0;
    private double _totShortValueExecuted = 0;
    private int _totalLongOrders = 0;
    private int _totalShortOrders = 0;
    private int _totLongContractsUnwound = 0;
    private int _totShortContractsUnwound = 0;
    private double _bidPx = Constants.UNSET_DOUBLE;
    private double _askPx = Constants.UNSET_DOUBLE;
    private double _lastDecidedPosition = Constants.UNSET_DOUBLE;
    private double _unwindPnl = 0;

    private Instrument _instrument;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final Instrument getInstrument() { return _instrument; }
    @Override public final void setInstrument( Instrument val ) { _instrument = val; }

    @Override public final long getLastTickId() { return _lastTickId; }
    @Override public final void setLastTickId( long val ) { _lastTickId = val; }

    @Override public final int getTotLongContractsExecuted() { return _totLongContractsExecuted; }
    @Override public final void setTotLongContractsExecuted( int val ) { _totLongContractsExecuted = val; }

    @Override public final int getTotShortContractsExecuted() { return _totShortContractsExecuted; }
    @Override public final void setTotShortContractsExecuted( int val ) { _totShortContractsExecuted = val; }

    @Override public final int getTotLongContractsOpen() { return _totLongContractsOpen; }
    @Override public final void setTotLongContractsOpen( int val ) { _totLongContractsOpen = val; }

    @Override public final int getTotShortContractsOpen() { return _totShortContractsOpen; }
    @Override public final void setTotShortContractsOpen( int val ) { _totShortContractsOpen = val; }

    @Override public final double getTotLongValueExecuted() { return _totLongValueExecuted; }
    @Override public final void setTotLongValueExecuted( double val ) { _totLongValueExecuted = val; }

    @Override public final double getTotShortValueExecuted() { return _totShortValueExecuted; }
    @Override public final void setTotShortValueExecuted( double val ) { _totShortValueExecuted = val; }

    @Override public final int getTotalLongOrders() { return _totalLongOrders; }
    @Override public final void setTotalLongOrders( int val ) { _totalLongOrders = val; }

    @Override public final int getTotalShortOrders() { return _totalShortOrders; }
    @Override public final void setTotalShortOrders( int val ) { _totalShortOrders = val; }

    @Override public final int getTotLongContractsUnwound() { return _totLongContractsUnwound; }
    @Override public final void setTotLongContractsUnwound( int val ) { _totLongContractsUnwound = val; }

    @Override public final int getTotShortContractsUnwound() { return _totShortContractsUnwound; }
    @Override public final void setTotShortContractsUnwound( int val ) { _totShortContractsUnwound = val; }

    @Override public final double getBidPx() { return _bidPx; }
    @Override public final void setBidPx( double val ) { _bidPx = val; }

    @Override public final double getAskPx() { return _askPx; }
    @Override public final void setAskPx( double val ) { _askPx = val; }

    @Override public final double getLastDecidedPosition() { return _lastDecidedPosition; }
    @Override public final void setLastDecidedPosition( double val ) { _lastDecidedPosition = val; }

    @Override public final double getUnwindPnl() { return _unwindPnl; }
    @Override public final void setUnwindPnl( double val ) { _unwindPnl = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _instrument = null;
        _lastTickId = Constants.UNSET_LONG;
        _totLongContractsExecuted = 0;
        _totShortContractsExecuted = 0;
        _totLongContractsOpen = 0;
        _totShortContractsOpen = 0;
        _totLongValueExecuted = 0;
        _totShortValueExecuted = 0;
        _totalLongOrders = 0;
        _totalShortOrders = 0;
        _totLongContractsUnwound = 0;
        _totShortContractsUnwound = 0;
        _bidPx = Constants.UNSET_DOUBLE;
        _askPx = Constants.UNSET_DOUBLE;
        _lastDecidedPosition = Constants.UNSET_DOUBLE;
        _unwindPnl = 0;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.StratInstrumentState;
    }

    @Override
    public final StratInstrumentStateImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( StratInstrumentStateImpl nxt ) {
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
        out.append( "StratInstrumentStateImpl" ).append( ' ' );
        out.append( ", instrument=" );
        if ( getInstrument() != null ) getInstrument().id( out );
        out.append( ", lastTickId=" ).append( getLastTickId() );
        out.append( ", totLongContractsExecuted=" ).append( getTotLongContractsExecuted() );
        out.append( ", totShortContractsExecuted=" ).append( getTotShortContractsExecuted() );
        out.append( ", totLongContractsOpen=" ).append( getTotLongContractsOpen() );
        out.append( ", totShortContractsOpen=" ).append( getTotShortContractsOpen() );
        out.append( ", totLongValueExecuted=" ).append( getTotLongValueExecuted() );
        out.append( ", totShortValueExecuted=" ).append( getTotShortValueExecuted() );
        out.append( ", totalLongOrders=" ).append( getTotalLongOrders() );
        out.append( ", totalShortOrders=" ).append( getTotalShortOrders() );
        out.append( ", totLongContractsUnwound=" ).append( getTotLongContractsUnwound() );
        out.append( ", totShortContractsUnwound=" ).append( getTotShortContractsUnwound() );
        out.append( ", bidPx=" ).append( getBidPx() );
        out.append( ", askPx=" ).append( getAskPx() );
        out.append( ", lastDecidedPosition=" ).append( getLastDecidedPosition() );
        out.append( ", unwindPnl=" ).append( getUnwindPnl() );
    }

}
