/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.state;

import com.rr.core.admin.AdminReply;
import com.rr.core.admin.AdminTableReply;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.session.Session;
import com.rr.om.session.SessionManager;
import com.rr.om.session.fixsocket.FixSocketSession;

public class SessionManagerAdmin implements SessionManagerAdminMBean {

    private static final Logger   _log     = LoggerFactory.create( SessionManagerAdmin.class );
    private static final String[] _columns = { "SessionName", "Status", "NextExpSeqNumIn", "NextSeqNumOut" };

    private static int _nextInstance = 1;
    
    private final SessionManager _sessMgr;
    private final String _name;

    private static int nextId() {
        return _nextInstance++;
    }


    public SessionManagerAdmin( SessionManager sessionManager ) {
        _sessMgr = sessionManager;
        _name = "SessionManagerAdmin" + nextId();
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String listAllSessions() {
        AdminReply reply = new AdminTableReply( _columns );
        Session[] sessions = _sessMgr.getUpStreamSessions();
        for( Session session : sessions ) {
            list( reply, session );
        }
        sessions = _sessMgr.getDownStreamSessions();
        for( Session session : sessions ) {
            list( reply, session );
        }
        list( reply, _sessMgr.getHub() );
        return reply.end();
    }

    @Override
    public String listClientSessions() {
        AdminReply reply = new AdminTableReply( _columns );
        Session[] sessions = _sessMgr.getUpStreamSessions();
        for( Session session : sessions ) {
            list( reply, session );
        }
        return reply.end();
    }

    @Override
    public String listExchangeSessions() {
        AdminReply reply = new AdminTableReply( _columns );
        Session[] sessions = _sessMgr.getDownStreamSessions();
        for( Session session : sessions ) {
            list( reply, session );
        }
        return reply.end();
    }

    @Override
    public String setSessionSeqNums( String sessionName, int nextInSeqNum, int nextOutSeqNum, boolean passiveReset ) {
        boolean forcedReset = !passiveReset;
        
        _log.info( "SessionManagerAdmin.setSessionSeqNums " + sessionName + " nextInSeqNum=" + nextInSeqNum + ", nextOutSeqNum=" + nextOutSeqNum + ", forcedReset=" + forcedReset );

        Session session = _sessMgr.getSession( sessionName );
        
        if ( session == null ) return "Unable to find session " + sessionName;
        
        if ( session instanceof AbstractStatefulSocketSession ) {
            AbstractStatefulSocketSession<?,?> fsess = (AbstractStatefulSocketSession<?,?>)session;
            
            Object ctl = fsess.getController();
            
            if ( ctl instanceof SessionSeqNumController ) {
                SessionSeqNumController c = (SessionSeqNumController) ctl;

                try {
                    c.setSeqNums( nextInSeqNum, nextOutSeqNum, forcedReset );
                } catch( Exception e ) {
                    return "Exception " + e.getMessage();
                }
            }
        }
        
        return "Session " + sessionName + " nextExpectedInSeqNum=" + nextInSeqNum + ", nextOutSeqNum=" + nextOutSeqNum;
    }
    
    private void list( AdminReply reply, Session sess ) {
        if ( sess != null ) {
            reply.add( sess.getComponentId() );
            if ( sess instanceof AbstractStatefulSocketSession ) {
                AbstractStatefulSocketSession<?,?> fsess = (AbstractStatefulSocketSession<?,?>)sess;
                
                Object ctl = fsess.getController();
                
                if ( ctl instanceof SessionSeqNumController ) {
                    SessionSeqNumController c = (SessionSeqNumController) ctl;
                    
                    reply.add( c.getState() );
                    reply.add( c.getNextExpectedInSeqNo() );
                    reply.add( c.getNextOutSeqNum() );
                    
                } else {
                    reply.add( sess.isConnected() ? "Connected" : "Disconnected" );
                    reply.add( "" );
                    reply.add( "" );
                }
                
            } else {
                reply.add( sess.isConnected() ? "Connected" : "Disconnected" );
                reply.add( "" );
                reply.add( "" );
            }
        }
    }

    @Override
    public String pauseSession( String sessionName ) {
        Session session = _sessMgr.getSession( sessionName );
        
        _log.info( "SessionManagerAdmin.pauseSession " + sessionName );
        
        if ( session == null ) return "Unable to find session " + sessionName;
        
        if ( session instanceof AbstractStatefulSocketSession ) {
            AbstractStatefulSocketSession<?,?> fsess = (AbstractStatefulSocketSession<?,?>)session;
            
            try {
                fsess.setPaused( true );

                return "Session " + sessionName + " PAUSED " + fsess.getController().info();

            } catch( Exception e ) {
                return "Exception " + e.getMessage();
            }
        }
        
        return "Session " + sessionName + " is NOT a fix session";
    }

    @Override
    public String logoutSession( String sessionName ) {
        Session session = _sessMgr.getSession( sessionName );
        
        _log.info( "SessionManagerAdmin.logoutSession " + sessionName );
        
        if ( session == null ) return "Unable to find session " + sessionName;
        
        if ( session instanceof FixSocketSession ) {
            FixSocketSession fsess = (FixSocketSession)session;
            
            try {
                fsess.getController().forceLogOut();

                return "Session " + sessionName + " LoggedOut " + fsess.getController().info();

            } catch( Exception e ) {
                return "Exception " + e.getMessage();
            }
        }
        
        return "Session " + sessionName + " is NOT a fix session";
    }

    @Override
    public String loginSession( String sessionName ) {
        Session session = _sessMgr.getSession( sessionName );
        
        _log.info( "SessionManagerAdmin.logoutSession " + sessionName );
        
        if ( session == null ) return "Unable to find session " + sessionName;
        
        if ( session instanceof FixSocketSession ) {
            FixSocketSession fsess = (FixSocketSession)session;
            
            try {
                fsess.getController().forceLogOn();

                return "Session " + sessionName + " LogOn started " + fsess.getController().info();

            } catch( Exception e ) {
                return "Exception " + e.getMessage();
            }
        }
        
        return "Session " + sessionName + " is NOT a fix session";
    }

    @Override
    public String resumeSession( String sessionName ) {
        Session session = _sessMgr.getSession( sessionName );
        
        _log.info( "SessionManagerAdmin.resumeSession " + sessionName );
        
        if ( session == null ) return "Unable to find session " + sessionName;
        
        if ( session instanceof AbstractStatefulSocketSession ) {
            AbstractStatefulSocketSession<?,?> fsess = (AbstractStatefulSocketSession<?,?>)session;
            
            try {
                fsess.setPaused( false );
                
                return "Session " + sessionName + " UNPAUSED " + fsess.getController().info();
                
            } catch( Exception e ) {
                return "Exception " + e.getMessage();
            }
        }
        
        return "Session " + sessionName + " is NOT a fix session";
    }
}
