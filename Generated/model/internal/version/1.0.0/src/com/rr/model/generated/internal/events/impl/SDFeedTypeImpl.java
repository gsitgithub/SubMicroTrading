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

public final class SDFeedTypeImpl implements SDFeedType, Reusable<SDFeedTypeImpl> {

   // Attrs

    private          SDFeedTypeImpl _next = null;
    private final ReusableString _feedType = new ReusableString( SizeType.INST_FEED_TYPE_LENGTH.getSize() );
    private int _marketDepth = Constants.UNSET_INT;


    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getFeedType() { return _feedType; }

    @Override public final void setFeedType( byte[] buf, int offset, int len ) { _feedType.setValue( buf, offset, len ); }
    @Override public final ReusableString getFeedTypeForUpdate() { return _feedType; }

    @Override public final int getMarketDepth() { return _marketDepth; }
    @Override public final void setMarketDepth( int val ) { _marketDepth = val; }


   // Reusable Contract

    @Override
    public final void reset() {
        _feedType.reset();
        _marketDepth = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.SDFeedType;
    }

    @Override
    public final SDFeedTypeImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( SDFeedTypeImpl nxt ) {
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
        out.append( "SDFeedTypeImpl" ).append( ' ' );
        out.append( ", feedType=" ).append( getFeedType() );
        out.append( ", marketDepth=" ).append( getMarketDepth() );
    }

}
