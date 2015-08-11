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
import com.rr.model.generated.internal.type.SecurityTradingStatus;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
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

public final class SecMassStatGrpImpl implements SecMassStatGrp, Reusable<SecMassStatGrpImpl> {

   // Attrs

    private          SecMassStatGrpImpl _next = null;
    private final ReusableString _securityId = new ReusableString( SizeType.SECURITYID_LENGTH.getSize() );
    private boolean _securityStatus = false;

    private SecurityIDSource _securityIDSource;
    private SecurityTradingStatus _securityTradingStatus;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getSecurityId() { return _securityId; }

    @Override public final void setSecurityId( byte[] buf, int offset, int len ) { _securityId.setValue( buf, offset, len ); }
    @Override public final ReusableString getSecurityIdForUpdate() { return _securityId; }

    @Override public final SecurityIDSource getSecurityIDSource() { return _securityIDSource; }
    @Override public final void setSecurityIDSource( SecurityIDSource val ) { _securityIDSource = val; }

    @Override public final SecurityTradingStatus getSecurityTradingStatus() { return _securityTradingStatus; }
    @Override public final void setSecurityTradingStatus( SecurityTradingStatus val ) { _securityTradingStatus = val; }

    @Override public final boolean getSecurityStatus() { return _securityStatus; }
    @Override public final void setSecurityStatus( boolean val ) { _securityStatus = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _securityId.reset();
        _securityIDSource = null;
        _securityTradingStatus = null;
        _securityStatus = false;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.SecMassStatGrp;
    }

    @Override
    public final SecMassStatGrpImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( SecMassStatGrpImpl nxt ) {
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
        out.append( "SecMassStatGrpImpl" ).append( ' ' );
        out.append( ", securityId=" ).append( getSecurityId() );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", securityTradingStatus=" ).append( getSecurityTradingStatus() );
        out.append( ", securityStatus=" ).append( getSecurityStatus() );
    }

}
