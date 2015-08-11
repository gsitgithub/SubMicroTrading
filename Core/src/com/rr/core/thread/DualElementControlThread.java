/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.thread;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ThreadPriority;

public class DualElementControlThread extends AbstractControlThread {

    private final    Logger            _log  = LoggerFactory.create( DualElementControlThread.class );
    
    private          ExecutableElement _workerA  = null;
    private          ExecutableElement _workerB  = null;

    public DualElementControlThread( String name, ThreadPriority priority ) {
        
        super( name, priority );
    }

    @Override
    public void register( ExecutableElement ex ) {
        
        _log.info( "DualElementControlThread : registered " + ex.getComponentId() + " with " + getName() );
        
        if ( _workerA == null ) {
            _workerA = ex;
        } else if ( _workerB == null ) {
            _workerB = ex;
        } else {
            if ( _workerA != ex && _workerB != ex ) {
                throw new SMTRuntimeException( "DualElementControlThread cannot register 3 execElems, failed with " + ex.getComponentId() );
            }
        }
    }

    @Override
    protected void runControlLoop() {

        if ( _workerA == null || _workerB == null ) {
            abortMissingController();
        }
        
        ExecutableElement cur = _workerB;

        _workerA.threadedInit();
        _workerB.threadedInit();

        boolean readyA = false;
        boolean readyB = false;
        
        while( !isStopping() ) {
            readyA = _workerA.checkReady();
            readyB = _workerB.checkReady();

            if ( readyA || readyB ) {
                try {
                    while( !isStopping() ) {
                        if ( cur == _workerB ) { // catch case where workerA previously threw exception ie its B's turn next 
                            cur = _workerA;
                            _workerA.execute();
                        }
                        cur = _workerB;
                        _workerB.execute();
                    }
                } catch( Exception e ) {
                    cur.handleExecutionException( e );
                }
            } else{
                goPassive();
            }
        }
        
        _workerA.stop();
        _workerB.stop();
    }
}
