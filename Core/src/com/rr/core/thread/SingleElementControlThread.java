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
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ThreadPriority;

public class SingleElementControlThread extends AbstractControlThread {

    private final    Logger            _log  = LoggerFactory.create( SingleElementControlThread.class );
    
    private          ExecutableElement _worker;

    public SingleElementControlThread( String name, ThreadPriority priority ) {
        
        super( name, priority );
    }

    @Override
    public void register( ExecutableElement ex ) {
        _log.info( "SingleElementControlThread : registered " + ex.getComponentId() + " with " + getName() );
        
        _worker = ex;
    }

    @Override
    protected void runControlLoop() {
        if ( _worker == null ) {
            _log.info( "SingleElementControlThread missing worker, controlId=" + getName() );

            ShutdownManager.instance().shutdown( -1 );
        }

        _worker.threadedInit();
        
        while( !isStopping() ) {

            if ( _worker.checkReady() ) {
                try {
    
                    while( !isStopping() ) {
                        _worker.execute();
                    }
                    
                } catch( Exception e ) {
                    
                    _worker.handleExecutionException( e );
                }
            } else {
                _worker.notReady();
                
                goPassive();
            }
        }
        
        _worker.stop();
    }
}
