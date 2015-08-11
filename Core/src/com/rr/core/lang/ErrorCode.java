/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;

public class ErrorCode {

    private static Logger _log = LoggerFactory.console( ErrorCode.class );
    private static Map<String,String> _msgs = new HashMap<String,String>(256);
    
    private ZString _msg;
    
    public ErrorCode( String code, String msg ) {
        if ( _msgs.containsKey( code ) )  {
            _log.warn( "Duplicate error message code=" + code );
        }
        
        _msgs.put( code, msg );
        
        _msg  = new ViewString( "[" + code + "] " + msg );
    }

    public ZString getError()  {
        return _msg;
    }
}
