/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common.events;

import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.TLC;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.mds.common.MDSReusableType;

public final class Subscribe extends BaseMDSEvent implements Reusable<Subscribe> {

    public static final int     MAX_RIC_IN_SUB      = 50;       // @NOTE MUST be less than 256
    
    private Subscribe    _next;
    private int          _type;     // should match subid from MDSReusableTypeConstants
    private int          _count;    // number of RICS in request
    
    private ReusableString _ricChain = new ReusableString( SizeConstants.DEFAULT_RIC_LENGTH );

    @Override
    public final ReusableType getReusableType() {
        return MDSReusableType.Subscribe;
    }

    @Override
    public final Subscribe getNext() {
        return _next;
    }

    @Override
    public final void setNext( final Subscribe nxt ) {
        _next = nxt;
    }

    /**
     * inserts ric chain to start of subscription list in reverse order
     * 
     * @param ric
     */
    public void addRICChain( ReusableString ric ) {
        
        while( ric != null ) {
            ReusableString copy = TLC.safeCopy( ric );
    
            copy.setNext( _ricChain );
            
            _ricChain = copy;
            ++_count;
            
            ric = ric.getNext();
        }
    }
    
    public ReusableString getRICChain() {
        return _ricChain;
    }
    
    public int getSubscriptionType() {
        return _type;
    }
    
    public void setType( MDSReusableType type ) {
        _type = type.getSubId();
    }
    
    public int getCount() {
        return _count;
    }
    
    @Override
    public final void reset() {
        _next        = null;
        _nextMessage = null;
        _type        = 0;
        _count       = 0;
        
        while( _ricChain != null ) {
            ReusableString s = _ricChain;
            
            TLC.instance().recycle( s );
            
            _ricChain = _ricChain.getNext();
        }
    }
    
    @Override 
    public void dump( ReusableString out )   {
        ReusableString t = _ricChain;
        
        while( t != null ) {
            out.append( ',' ).append( t );
            t = t.getNext();
        }
    }
}
