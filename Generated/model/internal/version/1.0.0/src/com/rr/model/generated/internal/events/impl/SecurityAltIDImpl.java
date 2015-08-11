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

public final class SecurityAltIDImpl implements SecurityAltID, Reusable<SecurityAltIDImpl> {

   // Attrs

    private          SecurityAltIDImpl _next = null;
    private final ReusableString _securityAltID = new ReusableString( SizeType.SYMBOL_LENGTH.getSize() );

    private SecurityIDSource _securityAltIDSource;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getSecurityAltID() { return _securityAltID; }

    @Override public final void setSecurityAltID( byte[] buf, int offset, int len ) { _securityAltID.setValue( buf, offset, len ); }
    @Override public final ReusableString getSecurityAltIDForUpdate() { return _securityAltID; }

    @Override public final SecurityIDSource getSecurityAltIDSource() { return _securityAltIDSource; }
    @Override public final void setSecurityAltIDSource( SecurityIDSource val ) { _securityAltIDSource = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _securityAltID.reset();
        _securityAltIDSource = null;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.SecurityAltID;
    }

    @Override
    public final SecurityAltIDImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( SecurityAltIDImpl nxt ) {
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
        out.append( "SecurityAltIDImpl" ).append( ' ' );
        out.append( ", securityAltID=" ).append( getSecurityAltID() );
        out.append( ", securityAltIDSource=" );
        if ( getSecurityAltIDSource() != null ) getSecurityAltIDSource().id( out );
    }

}
