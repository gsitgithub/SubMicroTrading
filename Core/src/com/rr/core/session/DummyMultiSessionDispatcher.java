/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import com.rr.core.component.SMTStartContext;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;


public class DummyMultiSessionDispatcher implements MultiSessionDispatcher {

    private final    String        _id;

    public DummyMultiSessionDispatcher() {
        this( null );
    }
    
    public DummyMultiSessionDispatcher( String id ) {
        _id = id;
    }
    
    @Override
    public String getComponentId() {
        return _id;
    }
    
    @Override
     public void start() {
        /* nothing */
    }

    @Override
    public void setStopping() {
        /* nothing */
    }

    @Override
    public void dispatch( Message msg ) {
        /* nothing */
    }

    @Override
    public void setHandler( MessageHandler handler ) {
        /* nothing */
    }

    @Override
    public void handlerStatusChange( MessageHandler handler, boolean isOk ) {
        /* nothing */
    }

    @Override
    public boolean canQueue() {
        return true;
    }

    @Override
    public String info() {
        return "DummyMultiSessionDispatcher";
    }

    @Override
    public void dispatchForSync( Message msg ) {
        /* nothing */
    }

    @Override
    public void addSession( NonBlockingSession session ) {
        /* nothing */
    }

    @Override
    public void startWork() {
        start();
    }

    @Override
    public void stopWork() {
        setStopping();
    }

    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }

    @Override
    public void prepare() {
        // nothing
    }
}
