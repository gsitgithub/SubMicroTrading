/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import java.util.LinkedList;
import java.util.List;

public class ShutdownManager {

    private static final ShutdownManager _instance = new ShutdownManager();

    public interface Callback {
    
        public void shuttingDown();
    }

    final List<Callback> _callbacks = new LinkedList<Callback>();
    Callback _loggerCallback = null;
    
    public static ShutdownManager instance() {
        return _instance;
    }

    private ShutdownManager(){
    
        Runtime.getRuntime().addShutdownHook( new Thread( "ShutdownManager" ) {
                                @Override
                                public void run() {
                                    for ( Callback c : _callbacks ) {
                                        c.shuttingDown();
                                    }
                                    
                                    if ( _loggerCallback != null ) {
                                        _loggerCallback.shuttingDown();
                                    }
                                }
                            } );
    }
    
    public synchronized void register( Callback callback ) {
        _callbacks.add( callback );
    }

    public synchronized void deregister( Callback callback ) {
        _callbacks.remove( callback );
    }

    public synchronized void registerLogger( Callback callback ) {
        _loggerCallback = callback;
    }

    public void shutdown( int code ) {
        System.exit( code );
    }
}
