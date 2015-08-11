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
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ThreadPriority;

/**
 * a control thread which slices time across three executable streams
 */
public class TriElementControlThread extends AbstractControlThread {

    private final    Logger            _log  = LoggerFactory.create( TriElementControlThread.class );
    
    private          ExecutableElement _workerA  = null;
    private          ExecutableElement _workerB  = null;
    private          ExecutableElement _workerC  = null;

    public TriElementControlThread( String name, ThreadPriority priority ) {
        
        super( name, priority );
    }

    @Override
    public void register( ExecutableElement ex ) {
        
        _log.info( "TriElementControlThread : registered " + ex.getComponentId() + " with " + getName() );
        
        if ( _workerA == null ) {
            _workerA = ex;
        } else if ( _workerB == null ) {
            _workerB = ex;
        } else if ( _workerC == null ) {
            _workerC = ex;
        } else {
            if ( _workerA != ex && _workerB != ex && _workerC != ex ) {
                throw new SMTRuntimeException( "TriElementControlThread cannot register >3 execElems, failed with " + ex.getComponentId() );
            }
        }
    }

    @Override
    protected void runControlLoop() {

        if ( _workerA == null || _workerB == null || _workerC == null ) {
            _log.info( "TriElementControlThread missing worker, controlId=" + getName() );

            ShutdownManager.instance().shutdown( -1 );
        }
        
        ExecutableElement cur = _workerC;
        
        _workerA.threadedInit();
        _workerB.threadedInit();
        _workerC.threadedInit();
        
        boolean readyA = false;
        boolean readyB = false;
        boolean readyC = false;
        
        while( !isStopping() ) {
            readyA = _workerA.checkReady();
            readyB = _workerB.checkReady();
            readyC = _workerC.checkReady();

            if ( readyA || readyB || readyC ) {

                try {
    
                    while( !isStopping() ) {
                        if ( cur == _workerC ) { // catch case where workerA/B previously threw exception ie its B/C's turn next
                            cur = _workerA;
                            _workerA.execute();
                        }
                        if ( cur == _workerA ) {
                            cur = _workerB;
                            _workerB.execute();
                        }
                        cur = _workerC;
                        _workerC.execute();
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
        _workerC.stop();
        
        _log.info( "CONTROL LOOP FINISHED " + getName() );
    }
}
