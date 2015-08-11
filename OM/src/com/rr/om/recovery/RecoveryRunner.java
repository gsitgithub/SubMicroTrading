/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import com.rr.core.component.SMTControllableComponent;
import com.rr.core.component.SMTStartContext;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.session.Session;
import com.rr.om.session.SessionManager;

/**
 * coordinates the startup / running of the algos
 */
public class RecoveryRunner implements SMTControllableComponent {
    private static final Logger       _log = LoggerFactory.create( RecoveryRunner.class );

    private RecoveryController  _reconciler;
    private SessionManager      _sessionManager;

    private final String _id;
    
    public RecoveryRunner( String id ) {
        _id = id;
    }

    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }
    
    @Override
    public void prepare() {
        Session[] downStream = _sessionManager.getDownStreamSessions();
        Session[] upStream   = _sessionManager.getUpStreamSessions();

        _log.info( "Starting reconciliation in prepare phase (before GC)" );
        
        _reconciler.start();
        
        for( Session upSess : upStream ) {
            upSess.recover( _reconciler );
        }
        
        for( Session downSess : downStream ) {
            downSess.recover( _reconciler );
        }
        
        for( Session upSess : upStream ) {
            upSess.waitForRecoveryToComplete();
        }
        
        for( Session downSess : downStream ) {
            downSess.waitForRecoveryToComplete();
        }
        
        _reconciler.reconcile();
        _reconciler.commit();

        _log.info( "Ending reconciliation" );
    }
    
    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void startWork() {
        // nothing
    }

    @Override
    public void stopWork() {
        // nothing
    }
}
