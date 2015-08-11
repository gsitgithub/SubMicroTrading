/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.admin;

import java.lang.management.ManagementFactory;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import com.rr.core.component.SMTInitialisableComponent;
import com.rr.core.component.SMTStartContext;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * AdminAgent
 *
 * admin wrapper to JMX, client code should not use any JMX as this may change in future
 * 
 * @NOTE use the html adapter as doesnt spam create temp objs 
 * @NOTE AVOID JConsole it generates 16MB temp objs in the app in 5mins (while jconsole connected) !
 * 
 * if using JConsole run with
     -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1616  -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
 */
public class AdminAgent implements SMTInitialisableComponent {

            static final Logger            _log           = LoggerFactory.create( AdminAgent.class );

    private static final ErrorCode         FAIL_REG       = new ErrorCode( "ADA100", "Unable to register admin command" );
            static final ErrorCode         FAIL_START     = new ErrorCode( "ADA200", "Unable to start HTML admin adapter" );

    private static final int               ADMIN_DISABLED = -1;
    
    private static       MBeanServer       _mbs;
            static       HtmlAdaptorServer _adapter;
    private static       int               _port;

    private final String _id;

    public synchronized static void init( int port ) throws AdminException {
        if ( _mbs == null && port > 0 ) {
            _log.info( "AdminAgent starting JMX html port " + port );
            _port = port;
            _mbs = ManagementFactory.getPlatformMBeanServer();
            _adapter = new HtmlAdaptorServer();
            // Register and start the HTML adaptor
            try {
                String id = "AdminAgent:name=htmladapter,port=" + _port;
                ObjectName adapterName = new ObjectName( id );
                _adapter.setPort( _port );
                _mbs.registerMBean( _adapter, adapterName );
                
                // must start the HTML adapter under a daemon thread for it to inherit isDaemon property
                
                Thread adStart = new Thread( new Runnable(){
                                                @Override
                                                public void run() {
                                                    try {
                                                        _adapter.start();
                                                        _log.info( "HTML Admin Adapter started" );
                                                    } catch( Exception e ) {
                                                        _log.error( FAIL_START, "", e );
                                                    }
                                                    
                                                }}, "AdapterStarter" );
                
                adStart.setDaemon( true );
                adStart.start();
                
                ShutdownManager.instance().register( new Callback() {
                                                        @Override
                                                        public void shuttingDown() {
                                                            close();
                                                        }} );
                
            } catch( Exception e ) {
                throw new AdminException( e );
            }
        } else {
            _port = ADMIN_DISABLED;
        }
    }

    public synchronized static void close() {
        if ( _adapter != null ) {
            _adapter.stop();
        }
    }
    
    public static void register( AdminCommand handler ) {
        if ( _mbs != null && _port != ADMIN_DISABLED ) {
            ObjectName name = null;
    
            try {
               // Uniquely identify the MBeans and register them with the platform MBeanServer 
               name = formName( handler.getName() );
               _mbs.registerMBean( handler, name );
               
            } catch( Exception e ) {
                _log.error( FAIL_REG, "command " + handler.getName(), e );
            }
        }
    }

    private static ObjectName formName( String beanName ) throws MalformedObjectNameException {
        ObjectName name;
        name = new ObjectName("AdminAgent:name=" + beanName );
        return name;
    }

    public static ObjectInstance find( String beanName ) throws AdminException {
        if ( _port == ADMIN_DISABLED ) return null;
        
        ObjectName name;
        try {
            name = formName( beanName );
            return _mbs.getObjectInstance( name );
        } catch( MalformedObjectNameException e ) {
            throw new AdminException( e );
        } catch( InstanceNotFoundException e ) {
            throw new AdminException( e );
        }
    }

    public static Object invokeOperation( String beanName, String operationName, Object params[], String signature[] ) throws AdminException {
        if ( _port == ADMIN_DISABLED ) return null;

        ReusableString msg = new ReusableString("AdminAgent : invokeOperation ");
        msg.append( operationName ).append( " " );
        for( Object param : params ) {
            msg.append( " [" ).append( param.toString() ).append( "]" );
        }
        _log.info( msg );
        
        ObjectName name;
        try {
            name = formName( beanName );
            return _mbs.invoke( name, operationName, params, signature );
        } catch( MalformedObjectNameException e ) {
            throw new AdminException( e );
        } catch( InstanceNotFoundException e ) {
            throw new AdminException( e );
        } catch( ReflectionException e ) {
            throw new AdminException( e );
        } catch( MBeanException e ) {
            throw new AdminException( e );
        }
    }

    public static Object getAttribute( String beanName, String attrName ) throws AdminException {
        if ( _port == ADMIN_DISABLED ) return null;

        ObjectName name;
        try {
            name = formName( beanName );
            return _mbs.getAttribute( name, attrName );
        } catch( MalformedObjectNameException e ) {
            throw new AdminException( e );
        } catch( InstanceNotFoundException e ) {
            throw new AdminException( e );
        } catch( ReflectionException e ) {
            throw new AdminException( e );
        } catch( MBeanException e ) {
            throw new AdminException( e );
        } catch( AttributeNotFoundException e ) {
            throw new AdminException( e );
        }
    }

    public static MBeanInfo info( String beanName ) throws AdminException {
        if ( _port == ADMIN_DISABLED ) return null;

        ObjectName name;
        try {
            name = formName( beanName );
            return _mbs.getMBeanInfo( name );
        } catch( MalformedObjectNameException e ) {
            throw new AdminException( e );
        } catch( InstanceNotFoundException e ) {
            throw new AdminException( e );
        } catch( IntrospectionException e ) {
            throw new AdminException( e );
        } catch( ReflectionException e ) {
            throw new AdminException( e );
        }
    }

    public AdminAgent( String id, int port ) {
        _id = id;
        _port = port;
    }

    @Override
    public void init( SMTStartContext ctx ) {
        try {
            init( _port );
        } catch( AdminException e ) {
            throw new SMTRuntimeException( "Exception initialising JXM", e );
        }
    }

    @Override
    public void prepare() {
        // nothing
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}
