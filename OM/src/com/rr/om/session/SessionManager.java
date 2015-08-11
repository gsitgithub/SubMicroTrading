/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.rr.core.admin.AdminAgent;
import com.rr.core.component.SMTComponent;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Exchange;
import com.rr.core.session.Session;
import com.rr.core.session.SessionDirection;
import com.rr.om.session.state.SessionManagerAdmin;

public class SessionManager implements SMTComponent {

    private static final Logger _log  = LoggerFactory.create( SessionManager.class );

    private final Set<Session> _upStream   = new LinkedHashSet<Session>();
    private final Set<Session> _downStream = new LinkedHashSet<Session>();
    private       String       _id         = null;
    private       Session      _hub        = null;

    private final Map<Session,Exchange> _sessToExchange = new HashMap<Session,Exchange>();

    public SessionManager( String id ) {
        _id = id;
        SessionManagerAdmin sma = new SessionManagerAdmin( this );
        AdminAgent.register( sma );
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    public void add( Session sess, boolean isDownstream ) {
        
        if ( isDownstream ) {
            _log.info( "SessionManager.add() DOWNSTREAM " + sess.info() );
            _downStream.add( sess );
            sess.getConfig().setDirection( SessionDirection.Downstream );
        } else {
            _log.info( "SessionManager.add() UPSTREAM " + sess.info() );
            _upStream.add( sess );
            sess.getConfig().setDirection( SessionDirection.Upstream );
        }
    }
    
    public void setHub( Session hub ) {
        _log.info( "SessionManager.setHub() HUB " + hub.info() );
        _hub = hub;
    }

    public Session[] getDownStreamSessions() {
        return _downStream.toArray( new Session[ _downStream.size() ] );
    }

    public Session[] getUpStreamSessions() {
        return _upStream.toArray( new Session[ _upStream.size() ] );
    }

    public Session  getHub() {
        return _hub;
    }

    public Session getSession( String sessionName ) {
        if ( sessionName == null ) return null;
        
        for( Session s : _upStream ) {
            if ( s.getComponentId().equalsIgnoreCase( sessionName ) ) {
                return s;
            }
        }
        
        for( Session s : _downStream ) {
            if ( s.getComponentId().equalsIgnoreCase( sessionName ) ) {
                return s;
            }
        }
        
        if ( _hub != null && _hub.getComponentId().equalsIgnoreCase( sessionName ) ) {
            return _hub;
        }
        
        return null;
    }

    public void associateExchange( Session sess, Exchange e ) {
        _sessToExchange.put( sess, e );
    }
    
    public Exchange getExchange( Session sess ) {
        return _sessToExchange.get( sess );
    }
}
