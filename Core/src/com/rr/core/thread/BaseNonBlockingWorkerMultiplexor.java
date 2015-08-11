/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.rr.core.component.SMTStartContext;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.thread.NonBlockingWorker.StatusChanged;
import com.rr.core.utils.SMTRuntimeException;

public abstract class BaseNonBlockingWorkerMultiplexor implements ExecutableElement {

    private static final Logger   _log = LoggerFactory.create( BaseNonBlockingWorkerMultiplexor.class );

    
    private static class AllDisconnectedException extends SMTRuntimeException {
        private static final long serialVersionUID = 1L;

        public AllDisconnectedException( String msg ) {
            super( msg );
        }
    }

    private class WorkerWrapper  {
        final NonBlockingWorker _worker;
              boolean           _active = false;

        WorkerWrapper( NonBlockingWorker worker ) {
            _worker   = worker;
        }
    }
    
    private          WorkerWrapper[]                   _workers = new WorkerWrapper[0];

    private final    String                            _id;
    private          int                               _nextWorker=0;
    private          boolean                           _allDisconnected = true;              // not volatile as mem barrier already occurs when get msg off queue 
    private final    Object                            _disconnectLock = new Object();
    
    private final    ControlThread                     _ctl;
    
    private          WorkerWrapper                     _curWorker = null;
    private          String                            _pipeIdList = null; // comma delimitted string list of pipeline ids
    private          List<String>                      _pipeLineIds = null;

    private          AtomicBoolean                     _stopping = new AtomicBoolean(false);

    

    // array index assigned to worker to save map lookup

    public BaseNonBlockingWorkerMultiplexor( String id, ControlThread ctl ) {
        _ctl  = ctl;
        _id   = id;
        
        ctl.register( this );
    }

    @Override
    public void threadedInit() {
        for( int idx=0 ; idx < _workers.length ; ++ idx ) {
            _workers[idx]._worker.threadedInit();
        }
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void execute() throws Exception {
        
        _curWorker = _workers[ _nextWorker ];
        
        final NonBlockingWorker sess     = _curWorker._worker;
        
        if ( ++_nextWorker >= _workers.length ) _nextWorker = 0;
        
        if ( _curWorker._active && sess.isActive() ) {
            sess.doWorkUnit();
        }
    }
    
    @Override
    public void stop() {
        if ( _stopping.compareAndSet( false, true ) ) {
            _ctl.setStopping( true );
            
            final int numSessions = _workers.length;
            
            for ( int i=0 ; i < numSessions ; i++ ) {
                final WorkerWrapper s = _workers[i];
                
                NonBlockingWorker q = s._worker;
                
                q.stop(); 
            }
        }
    }

    @Override
    public void handleExecutionException( Exception ex ) {
        
        if ( ex instanceof AllDisconnectedException ) {
            _log.info( "NonBlockingWorkerMultiplexor " + getComponentId() + " ALL WORKERS NOW PASSIVE, GIVE ControlThread CHANCE TO GO PASSIVE" );
        } else {
            
            final NonBlockingWorker sess = _curWorker._worker;
            
            if ( sess != null ) {
                _log.warn( "NonBlockingWorkerMultiplexor " + getComponentId() + ", sess=" + sess.getComponentId() + " exception " + ex.getMessage() );
            }
            
            // some problem, possibly disconnect, poke controller to wake up anything waiting on controller passive lock
            _ctl.statusChange(); // Mem barrier
        }
    }

    @Override
    public boolean checkReady() {
        return ( _allDisconnected == false );
    }

    @Override
    public void notReady() {
        int           nextSession = 0;
        WorkerWrapper sess        = null;
        
        while( nextSession < _workers.length ) {                    
            sess = _workers[ nextSession++ ];
            
            if ( sess._active ) {
                _allDisconnected = false;
            }
        }
    }
    
    /**
     * @param worker - a non blocking worker
     */
    public synchronized void addWorker( final NonBlockingWorker worker ) {
        
        WorkerWrapper[] newWorkers = new WorkerWrapper[ _workers.length + 1 ];
        
        int idx=0;
        while( idx < _workers.length ) {
            newWorkers[idx] = _workers[idx];
            ++idx;
        }
        
        final WorkerWrapper wrapper = new WorkerWrapper( worker );
        newWorkers[ idx ] = wrapper;
        
        _workers = newWorkers;
        
        worker.registerListener( new StatusChanged() {
                                    @Override
                                    public void isActive( boolean isActive ) {
                                        wrapper._active = isActive;
                                        handlerStatusChange( worker, isActive );
                                    }} );
    }

    @Override
    public String info() {
        return "MultiWorkerThreadedDispatcher( " + _id + " )";
    }


    @Override
    public void init( SMTStartContext ctx ) {
        setPipeIdList( _pipeIdList );
    }

    @Override
    public void prepare() {
        _ctl.start();
    }

    @Override
    public void startWork() {
        // nothing
    }

    @Override
    public void stopWork() {
        stop();
    }
    
    public void setPipeIdList( String pipeIdList ) {
        List<String> pipeLineIds = new ArrayList<String>();
        
        if ( pipeIdList != null ) {
            String[] parts = pipeIdList.split( "," );
            
            for( String part : parts ) {
                part = part.trim();
                
                if ( part.length() > 0 ) {
                    pipeLineIds.add( part );
                }
            }
        }
        
        _pipeLineIds = pipeLineIds;
    }
    
    public boolean hasPipeLineId( String pipeLineId ) {
        return _pipeLineIds.contains( pipeLineId );
    }
    
    /**
     * if pipelineIds is null try and generate the pipeLine list as can be invoked by loader before init is run
     * 
     * @return the list of pipelineIds
     */
    public synchronized List<String> getPipeLineIds() {
        if ( _pipeLineIds == null ) {
            setPipeIdList( _pipeIdList );
        }
        return _pipeLineIds;
    }

    void handlerStatusChange( NonBlockingWorker handler, boolean connected ) {
        final int numWorkers = _workers.length;
        
        boolean allDisconnected = true;
        
        for ( int i=0 ; i < numWorkers ; i++ ) {
            WorkerWrapper sessW = _workers[i];
            
            if ( sessW._worker == handler ){
                if ( connected != sessW._active ) {
                    final NonBlockingWorker sess = sessW._worker;
                    
                    _log.info( "NonBlockingWorkerMultiplexor " + getComponentId() + " : " + 
                               " with " + sess.getComponentId() + ((sess.isActive()) ? " ACTIVE" : " PASSIVE") );
                    
                    sessW._active = connected;
                }
            }
            
            if ( sessW._active ) {
                allDisconnected = false;
            }
        }
        
        synchronized( _disconnectLock ) {       // force mem barrier
            _allDisconnected = allDisconnected;
        }
        
        _ctl.statusChange();
        
        if ( _allDisconnected ) {
            throw new AllDisconnectedException( _id );
        }
    }
}
