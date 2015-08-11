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
import com.rr.model.generated.internal.type.Side;
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

public final class SecDefLegImpl implements SecDefLeg, Reusable<SecDefLegImpl> {

   // Attrs

    private          SecDefLegImpl _next = null;
    private final ReusableString _legSymbol = new ReusableString( SizeType.SYMBOL_LENGTH.getSize() );
    private long _legSecurityID = Constants.UNSET_LONG;
    private int _legRatioQty = Constants.UNSET_INT;
    private final ReusableString _legSecurityDesc = new ReusableString( SizeType.INST_SEC_DESC_LENGTH.getSize() );

    private SecurityIDSource _legSecurityIDSource = SecurityIDSource.ExchangeSymbol;
    private Side _legSide;
    private Instrument _instrument;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getLegSymbol() { return _legSymbol; }

    @Override public final void setLegSymbol( byte[] buf, int offset, int len ) { _legSymbol.setValue( buf, offset, len ); }
    @Override public final ReusableString getLegSymbolForUpdate() { return _legSymbol; }

    @Override public final long getLegSecurityID() { return _legSecurityID; }
    @Override public final void setLegSecurityID( long val ) { _legSecurityID = val; }

    @Override public final SecurityIDSource getLegSecurityIDSource() { return _legSecurityIDSource; }
    @Override public final void setLegSecurityIDSource( SecurityIDSource val ) { _legSecurityIDSource = val; }

    @Override public final int getLegRatioQty() { return _legRatioQty; }
    @Override public final void setLegRatioQty( int val ) { _legRatioQty = val; }

    @Override public final ViewString getLegSecurityDesc() { return _legSecurityDesc; }

    @Override public final void setLegSecurityDesc( byte[] buf, int offset, int len ) { _legSecurityDesc.setValue( buf, offset, len ); }
    @Override public final ReusableString getLegSecurityDescForUpdate() { return _legSecurityDesc; }

    @Override public final Side getLegSide() { return _legSide; }
    @Override public final void setLegSide( Side val ) { _legSide = val; }

    @Override public final Instrument getInstrument() { return _instrument; }
    @Override public final void setInstrument( Instrument val ) { _instrument = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _legSymbol.reset();
        _legSecurityID = Constants.UNSET_LONG;
        _legSecurityIDSource = SecurityIDSource.ExchangeSymbol;
        _legRatioQty = Constants.UNSET_INT;
        _legSecurityDesc.reset();
        _legSide = null;
        _instrument = null;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.SecDefLeg;
    }

    @Override
    public final SecDefLegImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( SecDefLegImpl nxt ) {
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
        out.append( "SecDefLegImpl" ).append( ' ' );
        out.append( ", legSymbol=" ).append( getLegSymbol() );
        out.append( ", legSecurityID=" ).append( getLegSecurityID() );
        out.append( ", legSecurityIDSource=" );
        if ( getLegSecurityIDSource() != null ) getLegSecurityIDSource().id( out );
        out.append( ", legRatioQty=" ).append( getLegRatioQty() );
        out.append( ", legSecurityDesc=" ).append( getLegSecurityDesc() );
        out.append( ", legSide=" ).append( getLegSide() );
        out.append( ", instrument=" );
        if ( getInstrument() != null ) getInstrument().id( out );
    }

}
