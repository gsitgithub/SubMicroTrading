/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.newmain;

import com.rr.core.component.SMTInitialisableComponent;
import com.rr.core.component.SMTStartContext;
import com.rr.core.component.SMTWarmableComponent;
import com.rr.core.model.book.WarmupSafeBookReserver;
import com.rr.core.warmup.WarmupRegistry;
import com.rr.om.warmup.units.WarmupCodecs;
import com.rr.om.warmup.units.WarmupJavaSpecific;
import com.rr.om.warmup.units.WarmupLogger;
import com.rr.om.warmup.units.WarmupRecycling;
import com.rr.om.warmup.units.WarmupRouters;


public class WarmupControl implements SMTInitialisableComponent, SMTWarmableComponent {

    private final String          _id;
    private       int             _warmupCount          = 1000;
    private       WarmupRegistry  _warmUpRegistry       = new WarmupRegistry();
    @SuppressWarnings( "unused" )
    private       int             _warmUpPortOffset     = 0;
    @SuppressWarnings( "unused" )
    private       boolean         _enableSendSpinLock   = false;
    
    public WarmupControl( String id ) {
        super();
        _id = id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void init( SMTStartContext ctx ) {
        //
    }

    @Override
    public void prepare() {
        _warmUpRegistry.register( new WarmupCodecs( _warmupCount ) );
        _warmUpRegistry.register( new WarmupRecycling( _warmupCount ) );
        _warmUpRegistry.register( new WarmupLogger( _warmupCount ) );
        _warmUpRegistry.register( new WarmupRouters( _warmupCount ) );
        _warmUpRegistry.register( new WarmupJavaSpecific( _warmupCount ) );
        _warmUpRegistry.register( new WarmupSafeBookReserver( _warmupCount ) );
    }
    
    @Override
    public void warmup() {
        _warmUpRegistry.warmAll();
    }
}
