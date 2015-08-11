/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

import com.rr.core.warmup.JITWarmup;

public final class WarmupSafeBookReserver implements JITWarmup {

    private final int     _warmupCount;
    private final String  _name = "WarmupSafeBookReserver";
    private       long    _dontOpt;
    
    public WarmupSafeBookReserver( int warmupCount ) {
        _warmupCount = warmupCount;
    }
    
    @Override
    public String getName() {        
        return _name;
    }

    public final long getDontOpt() {
        return _dontOpt;
    }

    @Override
    public void warmup() throws Exception {
        for( int i=0 ; i < _warmupCount ; ++i ) {
            singleWorkerThreadSimple();
            singleWorkerAging();
        }
    }

    private void singleWorkerThreadSimple() {
        SafeBookReserver sbr = new SafeBookReserver();
        
        sbr.attachReserveWorkerThread( Thread.currentThread() );
        
        _dontOpt += sbr.getReserved();
        _dontOpt += sbr.grabQty( 10, 4, 100*1000*1000 );
        _dontOpt += sbr.getReserved();
        
        sbr.completed( 1 );
        _dontOpt += sbr.getReserved();

        _dontOpt += sbr.grabQty( 1, 6, 100*1000*1000 + 100 );
        _dontOpt += sbr.getReserved();
        
        _dontOpt += sbr.grabQty( 10, 6, 100*1000*1000 + 200 );
        _dontOpt += sbr.getReserved();

        sbr.completed( 6 );
        _dontOpt += sbr.getReserved();

        _dontOpt += sbr.grabQty( 10, 10, 100*1000*1000 + 300 );
        _dontOpt += sbr.getReserved();
        sbr.completed( 10 );
        _dontOpt += sbr.getReserved();

        _dontOpt += sbr.getAttachedWorkerThreads();
    }

    private void singleWorkerAging() {
        SafeBookReserver sbr = new SafeBookReserver();
        
        sbr.attachReserveWorkerThread( Thread.currentThread() );
        
        _dontOpt += sbr.getReserved();
        _dontOpt += sbr.grabQty( 10, 4, 100*1000*1000 );
        _dontOpt += sbr.getReserved();
        
        _dontOpt += sbr.grabQty( 10, 6, 100*1000*1000 + SafeBookReserver.MIN_RESET_DELAY_NANOS + 1 );
        _dontOpt += sbr.getReserved();
        
        sbr.completed( 10 );
        _dontOpt += sbr.getReserved();

        _dontOpt += sbr.grabQty( 10, 10, 100*1000*1000 + 300 );
        _dontOpt += sbr.getReserved();
        sbr.completed( 10 );
        _dontOpt += sbr.getReserved();
    }
}
