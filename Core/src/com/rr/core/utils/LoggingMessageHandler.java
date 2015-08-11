/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;

/**
 * message handler that simply logs the message
 * 
 * @NOTE ONLY FOR USE BY SINGLE THREAD
 *
 * @author Richard Rose
 */

public class LoggingMessageHandler implements MessageHandler {

    private static final Logger _log = LoggerFactory.create( LoggingMessageHandler.class );

    private ReusableString _msg = new ReusableString();
    
    private String _id;

    public LoggingMessageHandler( String id ) {
        _id = id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void threadedInit() {
        // 
    }

    @Override
    public void handle( Message msg ) {
        handleNow( msg );
    }

    @Override
    public void handleNow( Message msg ) {
        _msg.copy( _id ).append( " received : " );
        msg.dump( _msg );
        _log.info( _msg );
    }

    @Override
    public boolean canHandle() {
        return true;
    }
}
