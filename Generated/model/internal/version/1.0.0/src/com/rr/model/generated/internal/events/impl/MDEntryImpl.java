/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.core.model.SecurityIDSource;
import com.rr.model.generated.internal.type.MDUpdateAction;
import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.TradingSessionID;
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

public final class MDEntryImpl implements MDEntry, Reusable<MDEntryImpl> {

   // Attrs

    private          MDEntryImpl _next = null;
    private long _securityID = Constants.UNSET_LONG;
    private int _repeatSeq = Constants.UNSET_INT;
    private int _numberOfOrders = Constants.UNSET_INT;
    private int _mdPriceLevel = Constants.UNSET_INT;
    private double _mdEntryPx = Constants.UNSET_DOUBLE;
    private int _mdEntrySize = Constants.UNSET_INT;
    private int _mdEntryTime = Constants.UNSET_INT;

    private SecurityIDSource _securityIDSource = SecurityIDSource.ExchangeSymbol;
    private MDUpdateAction _mdUpdateAction;
    private MDEntryType _mdEntryType;
    private TradingSessionID _tradingSessionID;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final SecurityIDSource getSecurityIDSource() { return _securityIDSource; }
    @Override public final void setSecurityIDSource( SecurityIDSource val ) { _securityIDSource = val; }

    @Override public final long getSecurityID() { return _securityID; }
    @Override public final void setSecurityID( long val ) { _securityID = val; }

    @Override public final MDUpdateAction getMdUpdateAction() { return _mdUpdateAction; }
    @Override public final void setMdUpdateAction( MDUpdateAction val ) { _mdUpdateAction = val; }

    @Override public final int getRepeatSeq() { return _repeatSeq; }
    @Override public final void setRepeatSeq( int val ) { _repeatSeq = val; }

    @Override public final int getNumberOfOrders() { return _numberOfOrders; }
    @Override public final void setNumberOfOrders( int val ) { _numberOfOrders = val; }

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

    @Override public final TradingSessionID getTradingSessionID() { return _tradingSessionID; }
    @Override public final void setTradingSessionID( TradingSessionID val ) { _tradingSessionID = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _securityIDSource = SecurityIDSource.ExchangeSymbol;
        _securityID = Constants.UNSET_LONG;
        _mdUpdateAction = null;
        _repeatSeq = Constants.UNSET_INT;
        _numberOfOrders = Constants.UNSET_INT;
        _mdPriceLevel = Constants.UNSET_INT;
        _mdEntryType = null;
        _mdEntryPx = Constants.UNSET_DOUBLE;
        _mdEntrySize = Constants.UNSET_INT;
        _mdEntryTime = Constants.UNSET_INT;
        _tradingSessionID = null;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.MDEntry;
    }

    @Override
    public final MDEntryImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( MDEntryImpl nxt ) {
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
        out.append( "MDEntryImpl" ).append( ' ' );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", securityID=" ).append( getSecurityID() );
        out.append( ", mdUpdateAction=" ).append( getMdUpdateAction() );
        out.append( ", repeatSeq=" ).append( getRepeatSeq() );
        out.append( ", numberOfOrders=" ).append( getNumberOfOrders() );
        out.append( ", mdPriceLevel=" ).append( getMdPriceLevel() );
        out.append( ", mdEntryType=" ).append( getMdEntryType() );
        out.append( ", mdEntryPx=" ).append( getMdEntryPx() );
        out.append( ", mdEntrySize=" ).append( getMdEntrySize() );
        out.append( ", mdEntryTime=" ).append( getMdEntryTime() );
        out.append( ", tradingSessionID=" ).append( getTradingSessionID() );
    }

}
