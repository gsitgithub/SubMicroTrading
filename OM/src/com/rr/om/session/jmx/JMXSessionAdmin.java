/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.jmx;

import com.rr.core.utils.Utils;



public class JMXSessionAdmin implements JMXSessionAdminMBean {

    private final JMXSession _session;

    public JMXSessionAdmin( JMXSession jmxSession ) {
        _session = jmxSession;
    }

    @Override
    public String getName() {
        return _session.getComponentId() + "Admin";
    }

    @Override
    public String injectMessage( String rawMessage ) {
        return injectMessage( rawMessage, null );
    }
    
    @Override
    public String injectMessage( String rawMessage, String sessionName ) {
        String[] msgs = rawMessage.split("#");
        String ret = null;
        
        for ( String msg : msgs ) {
            String lcMsg = msg.toLowerCase();
            
            String r = "";
            
            if ( lcMsg.startsWith( "sleep" ) || lcMsg.startsWith( "pause" )) {
                String[] bits = msg.split( " " );
                
                try {
                    int delay = Integer.parseInt( bits[1] );

                    if ( delay < 0 || delay > 30000 ) {
                        delay = 1000;
                        r = "sleep override [" + delay + " ms]";
                    } else {
                        r = "sleep [" + delay + " ms]";
                    }
                    
                    Utils.delay( delay );
                } catch( NumberFormatException e ) {
                    r = "Override bad sleep param [" + msg + "]";
                    Utils.delay(1000);
                }
            } else {
                r = _session.injectMessage( msg, sessionName );
            }
            
            if ( ret == null ) {
                ret = r;
            } else {
                ret = ret + "\n" + r;
            }
            
            Utils.delay(100);
        }
        
        return ret;
    }
}
